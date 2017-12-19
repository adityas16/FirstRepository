library(plyr)
library(sqldf)
goals = read.csv(paste(WELT_FOLDER,"extractedCSV/goals.csv",sep="/"))
#goals$isAwayGoal = 1- goals$isHomeGoal
last_goals = goals[!rev(duplicated(rev(goals$uri))),]
#sum(last_goals$homeScore)
#scores_computed=ddply(goals,c("uri"),homeScore_computed = sum(isHomeGoal),awayScore_computed = sum(isAwayGoal),summarise)
#last_goals=myjoin(last_goals,scores_computed,join_type="")


games = read.csv(paste(WELT_FOLDER,"extractedCSV/games.csv",sep="/"))
games$has_goals = games$homeScore!=0 | games$awayScore!=0
#games = myjoin(games,scores_computed,join_type="left")
#games$homeScore_computed[is.na(games$homeScore_computed)]=0
#games$homeScore_diff = games$homeScore - games$homeScore_computed
#sum(games$homeScore_diff>0,na.rm = T)
#penalty_games=games[games$homeScore_diff>0,]
#penalty_games$gd = penalty_games$homeScore_computed - penalty_games$awayScore_computed

#Testing
no_scoring_games = games[games$has_goals==F,]
scoring_games=games[games$has_goals==T,]

#No scoring games should not be in goals
length(setdiff(goals$uri,no_scoring_games$uri))==length(unique(goals$uri))

#This should only have PSO games that were 0-0
length(setdiff(scoring_games$uri,goals$uri))

hist(goals$time)
max(goals$time<130)
View(goals[goals$time==0,])

#Creating the final CSVs
games$home_keeper=NULL
games$away_keeper=NULL
#left join with with goals to get so we havea ll scoring games
games_cleaned=sqldf("select games.* from games,last_goals where games.uri=last_goals.uri")
#append all 0-0 matches
games_cleaned=rbind(games_cleaned,no_scoring_games)
#append all pso matches that were actually 0-0
x=setdiff(scoring_games$uri,goals$uri)
x=data.frame(setdiff(scoring_games$uri,goals$uri))
colnames(x)=c("uri")
x=sqldf("select games.* from games,x where games.uri=x.uri")
x$homeScore=0
x$awayScore=0
games_cleaned=rbind(games_cleaned,x)
games_cleaned$has_goals = games_cleaned$homeScore!=0 | games_cleaned$awayScore!=0

#Remove unnecessary columns
games_cleaned$home_keeper=NULL
games_cleaned$away_keeper=NULL
games_cleaned$homeScore=NULL
games_cleaned$awayScore=NULL
games_cleaned$season_uri=NULL
to_csv(games_cleaned)

goals_cleaned = sqldf("select goals.* from goals,games where games.uri=goals.uri")
goals_cleaned$homeScore=NULL
goals_cleaned$awayScore=NULL
to_csv(goals_cleaned)
