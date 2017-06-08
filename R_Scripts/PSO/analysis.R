library(plyr)
library(sqldf)
library(ggplot2)
source("./R_Code/PSO/utils.R")
source("./R_Code/PSO/dimension_pre_processing.R")

source("./R_Code/PSO/data_loader.R")
load_all()


#shootout analysis
#first mover
g=glm(final_scores$is_home_winner ~  final_scores$homeShotFirst,family=binomial("logit"))
summary(g)

#random sample
final_scores$random_winner = runif(1525,0,1)>0.5
g=glm(final_scores$random_winner ~  final_scores$homeShotFirst,family=binomial("logit"))
summary(g)

random_sample = sample(nrow(final_scores),100)
g=glm(final_scores[random_sample,]$is_home_winner ~  final_scores[random_sample,]$homeShotFirst,family=binomial("logit"))
summary(g)
n_samples = 10000
beta_1=c()
for(i in 1:n_samples){
  random_sample=sample(nrow(final_scores),500)
  g=glm(final_scores[random_sample,]$is_home_winner ~  final_scores[random_sample,]$homeShotFirst,family=binomial("logit"))
  beta_1 = c(beta_1,as.numeric(g$coefficients[2]))
}
ggplot() + aes(beta_1)+ geom_histogram(binwidth=0.01, colour="black", fill="white")

#competition
joined = myjoin(final_scores,competitions,"competition","competition",join_type = "")
g=glm(joined$is_home_winner ~  joined$homeShotFirst + joined$homeShotFirst*joined$competition_level,family=binomial("logit"))
summary(g)
g=glm(joined$is_team_A_winner ~   joined$competition_is_international + joined$competition_club_level + joined$competition_international_level,family=binomial("logit"))
summary(g)
g=glm(joined$is_team_A_winner ~   joined$competition_is_international + joined$competition_level,family=binomial("logit"))
summary(g)
g=glm(joined$is_team_A_winner ~  joined$competition_is_international + joined$competition_level + joined$dist_from_final,family=binomial("logit"))
summary(g)


nrow(fitted(g))

#extra shot
ddply(final_scores,c("is_team_A_shot"),summarize,count = length(game_id))
ddply(final_scores,c("is_team_A_shot"),summarize,count = sum(is_A_win)/length(game_id))

#keeper
#Regression on different keeper sets
keepers_with_pen_stats = read_keepers_with_penalty_stats()

joined = myjoin(final_scores,keepers_with_pen_stats,"home_keeper",join_type="")
joined = myjoin(joined,keepers_with_pen_stats,"away_keeper",join_type="")
g=glm(joined$is_home_winner ~  joined$homeShotFirst + joined$home_keeper_adj_save_ratio + joined$away_keeper_adj_save_ratio,family=binomial("logit"))
summary(g)
length(fitted(g))

keepers_with_pen_stats$save_ratio = keepers_with_pen_stats$n_saved_penalties / (keepers_with_pen_stats$n_saved_penalties + keepers_with_pen_stats$n_conceeded_penalties)

joined = myjoin(final_scores,keepers_with_pen_stats,"home_keeper",join_type="")
joined = myjoin(joined,keepers_with_pen_stats,"away_keeper",join_type="")

g=glm(joined$is_home_winner ~  joined$homeShotFirst + + joined$home_keeper_save_ratio + joined$away_keeper_save_ratio,family=binomial("logit"))
summary(g)

joined = myjoin(joined,keepers,"away_keeper")

pso <- read.csv(paste(WELT_FOLDER,"PSO_analysis.csv",sep="/"))

pso$shootergd = ifelse(pso$is_team_A_shot,pso$gd,pso$gd_b)
pso$round_adjusted = ifelse(pso$round>6,6,pso$round)

c_1970_2003 = (pso$year<2003 & pso$year >= 1970)
c_2003_2008 = (pso$year<2008 & pso$year >= 2003)
ddply(pso[pso$year<2008 & pso$year >= 2003,], c("competition"), summarise,N_kicks = length(competition),N_Shootouts = sum(isFirstShot))


#t test of conversion rate
teamAShots = pso[pso$is_team_A_shot==1,]
teamBShots = pso[pso$is_team_B_shot==1,]
t.test(teamAShots$isConverted,teamBShots$isConverted)
t.test(ddply(teamAShots, c("game_id"), summarise,conversion_rate = sum(isConverted)/length(isConverted))$conversion_rate,
       ddply(teamBShots, c("game_id"), summarise,conversion_rate = sum(isConverted)/length(isConverted))$conversion_rate)




# round wise scoring probability
round_wise_summary = ddply(pso, c("round_adjusted","is_team_A_shot"), summarise,score_proportion = sum(isConverted)/length(isConverted))

ggplot(round_wise_summary, aes(x=round_wise_summary$round_adjusted,y=round_wise_summary$score_proportion,fill=factor(1-round_wise_summary$is_team_A_shot))) +
  geom_bar(position="dodge", stat="identity") +
  geom_text(aes(label=round(round_wise_summary$score_proportion,2),hjust=round_wise_summary$is_team_A_shot,vjust=0),size=4)+
  ggtitle("Score Proportions by round") +
   labs(x="Round",y="Score Proportion") +
  guides(fill=guide_legend(title="Team"))+
  ylim(0,1)

for(i in 1:6){
  print(t.test(teamAShots[teamAShots$round_adjusted==i,"isConverted"],teamBShots[teamBShots$round_adjusted==i,"isConverted"])$p.value)
}

round_factor = factor(pso$round_adjusted)
competition_factor = factor(pso$competition)
I_shooter_leading = sign(pso$shootergd>0)
I_shooter_losing = sign(pso$shootergd<0)

#note that team A is the team that goes first
g=glm(pso$isConverted~  pso$is_team_A_shot + round_factor + I_shooter_leading + I_shooter_losing + competition_factor,family=binomial("logit"))
summary(g)


joined=final_scores
joined$one = 1
g=glm(joined[joined$year<2003,]$is_team_A_winner ~ joined[joined$year<2003,]$one  ,family=binomial("logit"))
summary(g)

g=glm(joined[joined$year>2003,]$is_team_A_winner ~ joined[joined$year>2003,]$one  ,family=binomial("logit"))
summary(g)


joined = myjoin(final_scores,odds,join_type="")
joined$one = 1


g=glm(joined$is_team_A_winner ~ joined$one  ,family=binomial("logit"))
summary(g)

joined$is_home_shot = ifelse(joined$homeShotFirst,joined$is_team_A_shot,!joined$is_team_A_shot)
joined$A_superiority = ifelse(joined$is_home_shot,joined$adjusted_ph - 0.5,joined$adjusted_pa - 0.5)
g=glm(joined$is_team_A_winner ~  joined$A_superiority,family=binomial("logit"))
summary(g)
