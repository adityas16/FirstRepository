library(plyr)
library(dplyr)
library(sqldf)
library(ggplot2)
require(gridExtra)
in_play_penalties = read.csv(paste(WELT_FOLDER,"extractedCSV/incidents.csv",sep="/"))
in_play_penalties$is_score = 1 - in_play_penalties$is_miss
#correction of error in home team (comes from Java Code)
in_play_penalties$is_home_corrected  = in_play_penalties$is_home
in_play_penalties$is_home_corrected[in_play_penalties$is_miss==0]  = 1- in_play_penalties$is_home[in_play_penalties$is_miss==0]

seasons=ddply(in_play_penalties,c("competition","year"),n=length(uri),scored = sum(1-is_miss),missed = sum(is_miss),p=sum(1-is_miss)/length(uri),summarise)
seasons = seasons[order(seasons$n * -1),]

#seasons = seasons[seasons$missed > 0,]
#seasons = seasons[seasons$p < 0.9,]
hist(seasons$p)
sum(seasons$n)
seasons$z_score = z.1prop(seasons$p,seasons$n,0.8)
seasons[order(seasons$n * -1),]$n
data_depth = seasons[seasons$p<1 ,] %>% 
  group_by(competition) %>%
  summarise(earliest=min(year))
data_depth = data_depth[order(data_depth$earliest),]

proportion_over_time = function(seasons,competition_name){
  ggplot(seasons[seasons$competition == competition_name,]) + 
    geom_point(aes(x=year,y=p)) +
    geom_abline(slope = 0,intercept = 0.8) +
    scale_y_continuous(limits=c(0,1)) + 
    scale_x_continuous(limits = c(1900,2018))+
    ggtitle(competition_name)
}
competitions_sorted_by_max_depth = union(data_depth$competition,unique(in_play_penalties$competition))
plots = lapply(competitions_sorted_by_max_depth,proportion_over_time,seasons=seasons)
pdf("~aditya/Dropbox/Research/Documents/in_play_penalty_proportions.pdf",onefile = T)
for(i in 1:(floor(length(plots)/6)+1)){
  page = plots[((i-1)*6+1):min((i*6),length(plots))]
  do.call("grid.arrange", c(page, ncol=2))
}
dev.off()

#Predictability of scoring from player's previous kicks
seasons = seasons[seasons$p < 0.9,]
penalties_with_miss_information = sqldf("select a.* from in_play_penalties a, seasons s where s.year = a.year and s.competition = a.competition")

#home/Away
ddply(penalties_with_miss_information,c("is_home_corrected"),n=length(is_home),prop=myprop(is_score),summarise)

x= ddply(penalties_with_miss_information,c("shooter"),n=length(uri),scored = sum(1-is_miss),missed = sum(is_miss),p=sum(1-is_miss)/length(uri),summarise)
x = x[order(x$n * -1),]
summary(x$n)

x=penalties_with_miss_information
x = x[order(x$shooter,x$year,x$month,x$day),]
x$is_shot = 1
df=x
x=df %>% 
  group_by(shooter) %>%
  mutate(shot_number = cumsum(is_shot)) %>%
  mutate(n_missed = cumsum(is_miss))
x$n_missed = c(0,head(x$n_missed,-1))
x$n_missed[c(TRUE,tail(x$shooter,-1) != head(x$shooter,-1))] = 0
x$is_goal = ifelse(x$is_miss==1,0,1)
player_timeline = ddply(x,c("shot_number","n_missed"),prop = myprop(is_goal),n=length(is_goal),summarise)

ggplot(data=player_timeline[player_timeline$shot_number<10,]) +
  geom_point(aes(x=shot_number,y=n_missed,size=n,color = prop))+
  #geom_abline(slope = 0.2,intercept = 0)+
  scale_color_gradient2(midpoint=0.75, low="red", mid="white",
                       high="green", space ="Lab" ,limits=c(0.5, 0.9))+
  scale_size_area(max_size = 15)
