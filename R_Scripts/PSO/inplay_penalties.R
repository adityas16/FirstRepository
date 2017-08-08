library(plyr)
library(dplyr)
library(sqldf)
library(ggplot2)
require(gridExtra)
in_play_penalties = read.csv(paste(WELT_FOLDER,"extractedCSV/incidents.csv",sep="/"))
in_play_penalties = in_play_penalties[!is.na(in_play_penalties$is_home),]
in_play_penalties$is_score = 1 - in_play_penalties$is_miss

in_play_penalties$gd = in_play_penalties$current_home_score - in_play_penalties$current_away_score
in_play_penalties$shooter_gd = ifelse(in_play_penalties$is_home,in_play_penalties$gd,-1*in_play_penalties$gd)
in_play_penalties$shooter_gd_sign = sign(in_play_penalties$shooter_gd)

find_shot_keeper <- function(is_home, home_keeper,away_keeper) if (is.na(is_home)) {NA} else if(is_home==1) {away_keeper} else {home_keeper}
in_play_penalties$shot_keeper = as.character(in_play_penalties$home_keeper)
in_play_penalties$shot_keeper[in_play_penalties$is_home == 1] = as.character(in_play_penalties$away_keeper[in_play_penalties$is_home == 1])
#in_play_penalties$shot_keeper <- mapply(find_shot_keeper, as.character(in_play_penalties$is_home), as.character(in_play_penalties$home_keeper),in_play_penalties$away_keeper)

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


#Predictability of scoring from player's previous kicks
seasons = seasons[seasons$p < 0.9,]
penalties_with_miss_information = sqldf("select a.* from in_play_penalties a, seasons s where s.year = a.year and s.competition = a.competition")

#home/Away
ddply(penalties_with_miss_information,c("is_home"),n=length(is_home),prop=myprop(is_score),summarise)

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

#Effect of time on conversion rate
seasons = seasons[seasons$p < 0.9,]
penalties_with_miss_information = sqldf("select a.* from in_play_penalties a, seasons s where s.year = a.year and s.competition = a.competition")

hist(penalties_with_miss_information$time)
penalties_with_miss_information$bucket = floor(penalties_with_miss_information$time/15)
ddply(penalties_with_miss_information,c("shooter_gd_sign"),prop=myprop(is_score),n=length(is_score),summarize)

ddply(penalties_with_miss_information,c("is_home_corrected"),prop=myprop(is_score),n=length(is_score),summarize)


#Plot season graphs
seasons=ddply(in_play_penalties,c("competition","year"),n=length(uri),scored = sum(1-is_miss),missed = sum(is_miss),p=sum(1-is_miss)/length(uri),summarise)
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


#Keeper analysis
keeper_stats = ddply(penalties_with_miss_information, c("shot_keeper"),n=length(uri),p=myprop(is_score),summarise)
shooter_stats = ddply(penalties_with_miss_information, c("shooter"),n=length(uri),p=myprop(is_score),summarise)

my_hist(keeper_stats$n)
my_hist(shooter_stats$n)
summary(keeper_stats$n)
summary(shooter_stats$n)

keeper_threshold_shots = 5
keeper_stats_g10_shots = keeper_stats[keeper_stats$n>keeper_threshold_shots,]
keeper_stats_l10_shots = keeper_stats[keeper_stats$n<keeper_threshold_shots-1,]
bad_keepers = keeper_stats_g10_shots[keeper_stats_g10_shots$p>=median(keeper_stats_g10_shots$p),]
good_keepers = keeper_stats_g10_shots[keeper_stats_g10_shots$p<median(keeper_stats_g10_shots$p),]

f=final_scores
f=sqldf("select f.*,g.shot_keeper as good_home_keeper from f left outer join good_keepers as g on f.home_keeper = g.shot_keeper")
f=sqldf("select f.*,g.shot_keeper as good_away_keeper from f left outer join good_keepers as g on f.away_keeper = g.shot_keeper")

f$good_home_keeper = !is.na(f$good_home_keeper)
f$good_away_keeper = !is.na(f$good_away_keeper)

f=sqldf("select f.*,g.shot_keeper as l_10_shots_home_keeper from f left outer join keeper_stats_l10_shots as g on f.home_keeper = g.shot_keeper")
f=sqldf("select f.*,g.shot_keeper as l_10_shots_away_keeper from f left outer join keeper_stats_l10_shots as g on f.away_keeper = g.shot_keeper")

f$l_10_shots_home_keeper = !is.na(f$l_10_shots_home_keeper)
f$l_10_shots_away_keeper = !is.na(f$l_10_shots_away_keeper)

#f$bad_home_keeper = ifelse(f$good_home_keeper==0 & f$l_10_shots_home_keeper==0,1,0)
#f$bad_away_keeper = ifelse(f$good_away_keeper==0 & f$l_10_shots_away_keeper==0,1,0)

f=sqldf("select f.*,g.shot_keeper as bad_home_keeper from f left outer join bad_keepers as g on f.home_keeper = g.shot_keeper")
f=sqldf("select f.*,g.shot_keeper as bad_away_keeper from f left outer join bad_keepers as g on f.away_keeper = g.shot_keeper")
f$bad_home_keeper = !is.na(f$bad_home_keeper)
f$bad_away_keeper = !is.na(f$bad_away_keeper)


home_keeper_advantage <- function(good_home_keeper, good_away_keeper,bad_home_keeper,bad_away_keeper) {
  if (good_home_keeper & good_away_keeper) {"0g"}
  else if(bad_home_keeper & bad_away_keeper) {"0b"} 
  else if(good_home_keeper & bad_away_keeper) {1} 
  else if(bad_home_keeper & good_away_keeper) {-1}
  else {NA}
}
f$home_keeper_advantage <- mapply(home_keeper_advantage,f$good_home_keeper,f$good_away_keeper, f$bad_home_keeper,f$bad_away_keeper)

ddply(f,c("home_keeper_advantage"),n_shootouts=length(uri),p=myprop(is_home_winner),summarise)

sum(f$good_away_keeper)
sum(f$good_home_keeper)
sum(f$bad_away_keeper)
sum(f$bad_home_keeper)
sum(f$l_10_shots_home_keeper)
sum(f$l_10_shots_away_keeper)


pso$shot_keeper <- mapply(find_shot_keeper, as.character(pso$is_home_shot), as.character(pso$home_keeper),pso$away_keeper)
p=myjoin(pso,keeper_stats,c1="shot_keeper",c2="shot_keeper",prepend_c1_label =TRUE)
p=myjoin(p,shooter_stats,c1="striker",c2="shooter",prepend_c1_label =TRUE)
p$shot_keeper_n[is.na(p$shot_keeper_n)]=0
p$striker_n[is.na(p$striker_n)]=0

p$keeper_0_shots = ifelse(p$shot_keeper_n==0,1,0)
p$keeper_5_shots = ifelse(p$shot_keeper_n>=5,1,0)
p$keeper_10_shots = ifelse(p$shot_keeper_n>=10,1,0)

#p$keeper_n_shots_adj[p$shot_keeper_n==0] = 0
#p$keeper_n_shots_adj[p$shot_keeper_n==1] = 1
p$keeper_n_shots_adj = 0
p$keeper_n_shots_adj[p$shot_keeper_n>=5] = 2
p$keeper_n_shots_adj[p$shot_keeper_n>=10] = 3

#0,1,2-4,>4
p$shooter_n_shots_adj = 0
p$shooter_n_shots_adj[p$striker_n>=1] = 1
p$shooter_n_shots_adj[p$striker_n>=4] = 2

ddply(p,c("shooter_n_shots_adj","keeper_n_shots_adj"),n=length(uri),summarize)

