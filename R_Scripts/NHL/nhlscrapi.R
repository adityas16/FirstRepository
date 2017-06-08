library(nhlscrapr)
library(devtools)
library(zoo)
library(data.table)
source("./R_Code/PSO/utils.R")

build("~/Research Data/NHL/nhlscrapr/")
install("~/Research Data/NHL/nhlscrapr/")


#Process games in parallel!
library(doMC)
registerDoMC(2)
res <- foreach (kk=1:dim(game.table)[1]) %dopar%
{
  message (paste(kk, game.table[kk,1], game.table$gcode[kk]))
  item <- process.single.game(
    game.table[kk,1],
    game.table$gcode[kk],
    save.to.file=TRUE)
}
load("~/nhlr-data/20052006-20006-processed.RData")
load("~/nhlr-data/20112012-20051-processed.RData")
load("~/nhlr-data/20052006-20404-processed.RData")
load("~/nhlr-data/20142015-21230-processed.RData")
#keeper pulled
load("~/nhlr-data/20142015-20388-processed.RData")

game.info$playbyplay
pbp = game.info[[1]]
game.pbp.pso = pbp[pbp$period == 5,]
if(nrow(game.pbp.pso)==0) next
game.pbp.pso = game.pbp.pso[game.pbp.pso$etype== "GOAL" |  game.pbp.pso$etype== "SHOT",]
game.pbp.pso$isConverted = ifelse(game.pbp.pso$etype == "GOAL",1,0)
game.pbp.pso$kickNumber = seq(1,nrow(game.pbp.pso))
game.pbp.pso$homeShotFirst = ifelse(game.pbp.pso$ev.team[1] == game.pbp.pso$hometeam[1],1,0)
game.pbp.pso$shooter = game.pbp.pso$ev.player.1
all.pso = rbind(all.pso,game.pbp.pso[,c("isConverted","kickNumber","homeShotFirst","shooter")])
all.pso

if(length(game.info$date==4)){
  game.info$date[3]
  month_name_to_int(game.info$date[2])
  game.info$date[4]
}
game.info$playbyplay$hometeam[1]
game.info$playbyplay$awayteam[1]
as.integer(game.info$score["homescore"])

pbp = game.info[[1]]
pbp[pbp$etype == "GOAL",]

process.games.pso <- function (games=full.game.database()) {
  #games=full.game.database(); games = games[5341:5456,]
  rdata.folder="nhlr-data"
  override.download=FALSE
  bogus.count <- 0
  #item <- list()
  all.pso = data.frame()
  all.games = data.frame()
  all.pso.games = data.frame()
  all.goals = data.frame()
  all.penalties = data.frame()
  all.events =  data.frame()
  
  event_columns = c("seconds","side","game_id","etype","home.skaters","away.skaters","home.G","away.G","home.score","away.score")
  #all.events = as.data.table(matrix(0, nrow = nrow(g) * 360, ncol = length(event_columns)))
  colnames(all.events) = c("seconds","side","game_id","etype","home.skaters","away.skaters","home.G","away.G","home.score","away.score")
  
  for (kk in which(games$status > 0)) {
    
    message (paste(kk, games[kk,1], paste0(2+1*(games$session[kk]=="Playoffs"), games$gamenumber[kk])))
    game.info <- process.single.game(
      games$season[kk],
      games$gcode[kk],
      rdata.folder=rdata.folder,
      override.download=override.download,
      save.to.file=TRUE)
    
    if (is.null(game.info)) next
    if (game.info$status == 1) bogus.count <- bogus.count+1 else bogus.count <- 0
    if (bogus.count >= 10) break
    
    if(length(game.info$date==4)){
      games[kk,c("day")] = game.info$date[3]
      games[kk,c("month")] = month_name_to_int(game.info$date[2])
      games[kk,c("year")] = game.info$date[4]
    }
    games[kk,c("hometeam")] = game.info$playbyplay$hometeam[1]
    games[kk,c("awayteam")] = game.info$playbyplay$awayteam[1]
    games[kk,c("homescore")] = as.integer(game.info$score["homescore"])
    games[kk,c("awayscore")] = as.integer(game.info$score["awayscore"])
    games[kk,c("isPSO")] = 0
    
    
    all.games = rbind(all.games,games[kk,])
    
    
    
    pbp = game.info[[1]]
    pbp$side = ifelse(pbp$ev.team == pbp$hometeam,1,0) + 1
    pbp$game_id = paste(pbp$season,pbp$gcode,sep="-")
    
    #pbp$seconds>57*60
    #pbp[,c(event_columns)]
    all.events = rbindlist(list(all.events,pbp[,c(event_columns)]))
    #all.events = rbindlist(list(all.events,pbp))
    
    #goals
    pbp.goals = pbp[pbp$etype == "GOAL",]
    if(nrow(pbp.goals) > 0){
    pbp.goals$time = ceiling(pbp.goals$seconds/60)
    pbp.goals$season_start_year =  floor(as.integer(as.character(pbp.goals$season))/10000)
    all.goals = rbind(all.goals,pbp.goals[,c("time","side","season_start_year","game_id")])
    }
    
    #penalties
    pbp.penalties = pbp[pbp$etype == "PENL",]
    if(nrow(pbp.penalties) > 0){
      pbp.penalties$time = ceiling(pbp.penalties$seconds/60)
      pbp.penalties$season_start_year =  floor(as.integer(as.character(pbp.penalties$season))/10000)
      all.penalties = rbind(all.penalties,pbp.penalties[,c("time","side","season_start_year","game_id","type")])
    }
    
    game.pbp.pso = pbp[pbp$period == 5,]
    if(nrow(game.pbp.pso)==0) next
    
    game.pbp.pso = game.pbp.pso[game.pbp.pso$etype== "GOAL" |  game.pbp.pso$etype== "SHOT",]
    game.pbp.pso$isConverted = ifelse(game.pbp.pso$etype == "GOAL",1,0)
    game.pbp.pso$kickNumber = seq(1,nrow(game.pbp.pso))
    game.pbp.pso$homeShotFirst = ifelse(game.pbp.pso$ev.team[1] == game.pbp.pso$hometeam[1],1,0)
    game.pbp.pso$shooter = game.pbp.pso$ev.player.1
    all.pso = rbind(all.pso,game.pbp.pso[,c("isConverted","kickNumber","homeShotFirst","shooter")])
    games[kk,c("isPSO")] = 1
    all.pso.games = rbind(all.pso.games,games[kk,])
  }
  all.games$game_id = paste(all.games$season,all.games$gcode,sep="-")
  write.csv(all.pso,paste(R_OUTPUT_FOLDER,"nhl_pso.csv",sep = '/'))
  write.csv(all.games[!is.na(all.games$day),],paste(R_OUTPUT_FOLDER,"nhl_games.csv",sep = '/'))
  write.csv(all.goals,paste(R_OUTPUT_FOLDER,"nhl_goals.csv",sep = '/'),row.names = FALSE)
  write.csv(all.penalties,paste(R_OUTPUT_FOLDER,"nhl_penalties.csv",sep = '/'),row.names = FALSE)
  write.csv(all.events,paste(R_OUTPUT_FOLDER,"nhl_events.csv",sep = '/'),row.names = FALSE)
  #save(item, file="all-archived.RData")
  return(all.pso)
}


g = full.game.database()
g$season_start_year = floor(as.integer(g$season)/10000)
g = g[g$session=="Regular",]
g = g[g$season_start_year > 2006,]
games=g
#Multiple goals in one second
erroneous_games =c("20122013-20437","20122013-20454")

x = extract_events(g[1:5,])
extract_events = function(games){
  rdata.folder="nhlr-data"
  override.download=FALSE
  bogus.count <- 0
  all.events =  data.frame()
  
  event_columns = c("seconds","side","game_id","etype","home.skaters","away.skaters","home.G","away.G","home.score","away.score","type")
  #all.events = as.data.table(matrix(0, nrow = nrow(g) * 360, ncol = length(event_columns)))
  
  for (kk in which(games$status > 0)) {
    
    message (paste(kk, games[kk,1], paste0(2+1*(games$session[kk]=="Playoffs"), games$gamenumber[kk])))
    game.info <- process.single.game(
      games$season[kk],
      games$gcode[kk],
      rdata.folder=rdata.folder,
      override.download=override.download,
      save.to.file=TRUE)
    
    if (is.null(game.info)) next
    if (game.info$status == 1) bogus.count <- bogus.count+1 else bogus.count <- 0
    if (bogus.count >= 10) break
    
    
    pbp = game.info[[1]]
    pbp$side = ifelse(pbp$ev.team == pbp$hometeam,1,0) + 1
    pbp$game_id = paste(pbp$season,pbp$gcode,sep="-")
    if(!is.na(match(pbp$game_id,erroneous_games)[1])) next
    #pbp$seconds>57*60
    #all.events[all.events$etype=="GOAL",]
    #pbp$etype=="GOAL"
    #pbp[pbp$etype=="GOAL" | pbp$etype =="PENL"
    #pbp[,c(event_columns)]
    all.events = rbindlist(list(all.events,pbp))
    #all.events = rbindlist(list(all.events,pbp))
  
  }
  #write.csv(all.events,paste(R_OUTPUT_FOLDER,"nhl_events.csv",sep = '/'),row.names = FALSE)
  return(all.events)
}


all.events$new_game = 1
all.events$new_game[2:nrow(all.events)] = all.events[2:nrow(all.events),]$game_id != all.events[1:nrow(all.events)-1,]$game_id
all.events$is_goal[2:nrow(all.events)] = all.events[2:nrow(all.events),]$game_id != all.events[1:nrow(all.events)-1,]$game_id

