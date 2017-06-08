library(sqldf)
library(plyr)
library(xtable)
library(ggplot2)
#source("./R_Code/NHL/nhlscrapi.R")

one_row_per_second=function(x){
  x$is_repeated = 0
  x$is_repeated[1:nrow(x)-1] = x$time[1:nrow(x)-1] == x$time[2:nrow(x)]
  return(x[x$is_repeated==0,])
}

create_second_wise = function(all_events,time=1:3600){
  #Create second wise summary
  all_times = data.frame(time)
  all_games = data.frame(unique(all_events$game_id))
  bucket_index = create_bucket_index()
  colnames(all_games) = c("game_id")
  second_wise = sqldf("select g.game_id,time from all_games as g, all_times")
  second_wise$bucket_index = bucket_index[floor((second_wise$time-1)/60)+1]
  x = sqldf("select  s.*,side as scoring_team from 
            second_wise as s left join all_events e 
            on e.game_id = s.game_id 
            and e.seconds = s.time
            and e.etype = 'GOAL'")
  second_wise$home_goal = 0
  second_wise$away_goal = 0
  second_wise$home_goal[!is.na(x$scoring_team) & x$scoring_team ==2] = 1
  second_wise$away_goal[!is.na(x$scoring_team) & x$scoring_team ==1] = 1
  
  all_events$home_keeper = all_events$home.G
  all_events$away_keeper = all_events$away.G
  x = sqldf("select s.*, e.home_keeper as home_keeper,e.away_keeper as away_keeper from 
            second_wise as s left join all_events e 
            on e.game_id = s.game_id 
            and e.seconds = s.time")
  x$is_repeated = 0
  x$is_repeated[1:nrow(x)-1] = x$time[1:nrow(x)-1] == x$time[2:nrow(x)]
  x = x[x$is_repeated==0,]
  x[x$time==time[1],]$home_keeper = 1
  x[x$time==time[1],]$away_keeper = 1
  
  x$home_keeper = na.locf(x$home_keeper)
  x$away_keeper = na.locf(x$away_keeper)
  x$home_keeper_present = x$home_keeper != ""
  x$away_keeper_present = x$away_keeper != ""
  
  second_wise$home_keeper_present = x$home_keeper_present
  second_wise$away_keeper_present = x$away_keeper_present

  #final score
  
  #Number of skaters
  all_events = remove_duplicate_events(all_events)
  all_events$home_skaters = all_events$home.skaters
  all_events$away_skaters = all_events$away.skaters
  all_events$home_score = all_events$home.score
  all_events$away_score = all_events$away.score
  x = sqldf("select s.*, e.home_skaters as home_skaters,e.away_skaters as away_skaters, e.home_score as home_score,e.away_score as away_score  from 
            second_wise as s left join all_events e 
            on e.game_id = s.game_id 
            and e.seconds = s.time")
  x[x$time==time[1],]$home_skaters = 6
  x[x$time==time[1],]$away_skaters = 6
  
  #x$player_diff = sign(x$home_skaters - x$away_skaters)
  
  #x[x$player_diff==1 & x$home_goal==1,]$away_skater = x[x$player_diff==1 & x$home_goal==1,]$away_skater + 1
  #x[x$player_diff==-1 & x$away_goal==1,]$home_skater = x[x$player_diff==-1 & x$away_goal,]$home_skater + 1
  x$home_skaters = na.locf(x$home_skaters)
  x$away_skaters = na.locf(x$away_skaters)
  second_wise$home_skaters = x$home_skaters
  second_wise$away_skaters = x$away_skaters
  
  x[x$time==time[1],]$home_score = 0
  x[x$time==time[1],]$away_score = 0
  x$home_score = na.locf(x$home_score)
  x$away_score = na.locf(x$away_score)
  second_wise$home_score = x$home_score
  second_wise$away_score = x$away_score
  
  
  second_wise$home_keeper = 1
  second_wise$away_keeper = 1
  second_wise$home_skaters = 1
  second_wise$away_skaters = 1
  return(second_wise)
}

get_p3_end_events = function(all_events){
  p3_end_events = all_events[all_events$etype=="PEND" & all_events$seconds==3600,]
  p3_end_events$is_repeated = 0
  p3_end_events$is_repeated[1:nrow(p3_end_events)-1] = p3_end_events$game_id[1:nrow(p3_end_events)-1] == p3_end_events$game_id[2:nrow(p3_end_events)]
  p3_end_events = p3_end_events[p3_end_events$is_repeated==0,]
  return(p3_end_events)
}

remove_duplicate_events = function(all_events){
  all_events$order = 0
  all_events[all_events$etype=="GOAL",]$order = 1
  x = all_events[order(all_events$game_id,all_events$seconds,all_events$order),]
  x$is_repeated = 0
  x$is_repeated[1:nrow(x)-1] = x$seconds[1:nrow(x)-1] == x$seconds[2:nrow(x)]
  x = x[x$is_repeated==0,]
  return(x)
}

create_aggregated_data = function(){
  aggregated_data = data.frame()
  g = full.game.database()
  g$season_start_year = floor(as.integer(g$season)/10000)
  g = g[g$session=="Regular",]
  for(i in 2007:2014){
    events=extract_events(g[g$season_start_year == i,])
    second_wise = create_second_wise(events)
    second_wise$gd = second_wise$home_score-second_wise$away_score
    aggregated_data=rbind(aggregated_data,ddply(second_wise,c("game_id","home_keeper_present","away_keeper_present","home_skaters","away_skaters","gd","bucket_index"),time=length(time),home_goals = sum(home_goal),away_goals = sum(away_goal),summarise))
  }
  return(aggregated_data)
}
aggregated_data = read.csv(paste(R_OUTPUT_FOLDER,"nhl_aggregate.csv",sep = '/'))
aggregated_data = create_aggregated_data()
write.csv(aggregated_data,paste(R_OUTPUT_FOLDER,"nhl_aggregate.csv",sep = '/'))

#append scoring rates
scoring_rates = read.csv("/home/aditya/BetifyData/AdjustmentEstimation/NHL_data/2014/m1_scoring_rates.csv")
x = myjoin(aggregated_data,scoring_rates,c1="game_id",c2="game_id",join_type="")
x = sqldf("select a.*, s.lamdaH as lamdaH,s.lamdaA as lamdaA from 
            aggregated_data as a , scoring_rates as s 
            where a.game_id = s.game_id")
x$exp_lamdaH = x$lamdaH / 3600 * x$time
x$exp_lamdaA = x$lamdaA / 3600 * x$time
aggregated_data = x


aggregated_data$player_diff = sign(aggregated_data$home_skaters - aggregated_data$away_skaters)

x=ddply(aggregated_data,c("home_keeper_present"),exp_home_goals = sum(exp_lamdaH),actual_home_goals = sum(home_goals),exp_away_goals = sum(exp_lamdaA),actual_away_goals = sum(away_goals),n_seconds=sum(time),summarise)

x=ddply(aggregated_data,c("away_keeper_present"),exp_home_goals = sum(exp_lamdaH),actual_home_goals = sum(home_goals),exp_away_goals = sum(exp_lamdaA),actual_away_goals = sum(away_goals),n_seconds=sum(time),summarise)

x=aggregated_data[!(aggregated_data$home_keeper_present & aggregated_data$away_keeper_present),]$time
x=ddply(aggregated_data[aggregated_data$time>3540,],c("home_keeper_present"),expected_goals = sum(home_rate),actual_goals = sum(home_goal),n_seconds=length(game_id),summarise)
aggregated_data$player_diff = sign(aggregated_data$home_skaters - aggregated_data$away_skaters)

#difference in no of players
x=ddply(aggregated_data,c("player_diff"),exp_home_goals = sum(exp_lamdaH),actual_home_goals = sum(home_goals),exp_away_goals = sum(exp_lamdaA),actual_away_goals = sum(away_goals),n_seconds=sum(time),summarise)
x=ddply(aggregated_data,c("player_diff"),exp_home_goals = sum(exp_lamdaH),actual_home_goals = sum(home_goals),n_seconds=sum(time),summarise)
x$time_between_goals =x$n_seconds / x$actual_home_goals
x$scoring_rate_compared_to_even =  x$time_between_goals[2] /x$time_between_goals 
x=x[,c("player_diff","time_between_goals","scoring_rate_compared_to_even")]

x=ddply(aggregated_data,c("player_diff"),exp_away_goals = sum(exp_lamdaA),actual_away_goals = sum(away_goals),n_seconds=sum(time),summarise)
x$time_between_goals =x$n_seconds / x$actual_away_goals
x$scoring_rate_compared_to_even =  x$time_between_goals[2] /x$time_between_goals 
x=x[,c("player_diff","time_between_goals","scoring_rate_compared_to_even")]

