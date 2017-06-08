library(sqldf)
library(plyr)
library(xtable)
library(ggplot2)

x=read.csv(paste0(R_OUTPUT_FOLDER,"/nhl_gd_aggregate.csv"))
x=read.csv("~/nhl_aggregate.csv")
x$home_goals = x$is_home_goal
x$away_goals = x$is_away_goal
sum(x$is_home_goal +x$is_away_goal)
sum(x$time) / 3600

#games with no goals
goals = events[events$etype == "GOAL",]
setdiff(events$game_id,events[events$seconds<=3600,]$game_id)
x$gd = x$home_score - x$away_score
x$adjusted_gd = x$gd
x$adjusted_gd[x$gd>3] = 3
x$adjusted_gd[x$gd< -3] = -3
java_aggregate = x
s=ddply(java_aggregate,c("adjusted_gd"),n_seconds=sum(time),home_goals=sum(is_home_goal),away_goals=sum(is_away_goal),summarise)

s$T_h = s$n_seconds / s$home_goals
s$T_a = s$n_seconds / s$away_goals
s$ratioH = 1/(s$T_h/s$T_h[4])
s$ratioA = 1/(s$T_a/s$T_a[4])
s
my_xtable(s)

scoring_rates = read.csv("/home/aditya/BetifyData/AdjustmentEstimation/NHL_data/2014/m1_scoring_rates.csv")
x = myjoin(java_aggregate,scoring_rates,c1="game_id",c2="game_id",join_type="")
x = sqldf("select a.*, s.lamdaH as lamdaH,s.lamdaA as lamdaA from 
            java_aggregate as a , scoring_rates as s 
            where a.game_id = s.game_id")
x$exp_lamdaH = x$lamdaH / 3600 * x$time
x$exp_lamdaA = x$lamdaA / 3600 * x$time
java_aggregate = x
aggregated_data = java_aggregate

ddply(second_wise,c("game_id","home_score","away_score"),time=length(time),home_goals = sum(home_goal),away_goals = sum(away_goal),summarise)
sum(java_aggregate$is_away_goal==1)
ddply(java_aggregate,c("bucket_index"),n=sum(time),summarise)


#Tests on the java aggregate
all.goals$gd = all.goals$home.score - all.goals$away.score
all.goals$adjusted_gd = all.goals$gd
all.goals$adjusted_gd[all.goals$gd>3] = 3
all.goals$adjusted_gd[all.goals$gd< -3] = -3
n_games = length(unique(java_aggregate$game_id))
gdh = ddply(all.goals[all.goals$side==2,],c("adjusted_gd"),n=length(gd),summarise)
all.goals$previous_goal_time = c(0,all.goals$seconds[1:nrow(all.goals)-1])
all.goals$arrival_time = all.goals$seconds - all.goals$previous_goal_time
all.goals[all.goals$arrival_time<0,]$previous_goal_time = 0
all.goals$arrival_time = all.goals$seconds - all.goals$previous_goal_time
n_buckets = 13

#total goals
nrow(all.goals) == sum(java_aggregate$home_goals + java_aggregate$away_goals)
#home goals by gd
gdh$n==ddply(java_aggregate,c("adjusted_gd"),n_seconds=sum(time),home_goals=sum(is_home_goal),summarise)$home_goals
#total time
n_games * 3600 == sum(java_aggregate$time)
#all games start with 0-0
n_games ==sum(java_aggregate$home_score + java_aggregate $away_score ==0 & java_aggregate $bucket_index ==1)
#total number of rows
nrow(java_aggregate) <= n_buckets * n_games + nrow(all.goals)
ddply(all.goals[all.goals$side==2,],c("adjusted_gd"),mean_arrival_time=mean(arrival_time),summarise)
