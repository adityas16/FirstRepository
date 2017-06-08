
create_events_dump = function(){
  g = full.game.database()
  g$season_start_year = floor(as.integer(g$season)/10000)
  g = g[g$session=="Regular",]
  for(i in 2007:2014){
    events=extract_events(g[g$season_start_year == i,])
    
    #games = ddply(events,c("game_id"),home_team=unique(hometeam),away_team = unique(awayteam),days_from_2002=unique(refdate),season=unique(season),summarize)
    #games$date = as.Date(as.POSIXct(1009843200 + games$days_from_2002 * 24 * 60 * 60, origin="1970-01-01"))
    #games$days_from_2002=NULL
    #games$season_start_year = floor(as.integer(as.character(games$season))/10000)
    #games$season = NULL
    
    events$season=NULL
    events$gcode=NULL
    events$refdate = NULL
    filename = paste(R_OUTPUT_FOLDER,"nhl_events_complete.csv",sep = '/')
    write.table(events,filename,append = TRUE,row.names = FALSE,sep=",",col.names=!file.exists(filename))
    
    #filename = paste(R_OUTPUT_FOLDER,"nhl_games_complete.csv",sep = '/')
    #write.table(games,filename,append = TRUE,row.names = FALSE,sep=",",col.names=!file.exists(filename))
  }
}

#event types
event.types <- matrix(c("BLOCK", "BLOCKED SHOT     ",  "GOAL",  "GOAL             ",
                        "MISS",  "MISSED SHOT      ",  "SHOT",  "SHOT             ",
                        "FAC",   "FACE-OFF         ",  "GIVE",  "GIVEAWAY         ",
                        "TAKE",  "TAKEAWAY         ",  "PENL",  "PENALTY          ",
                        "HIT",   "HIT              ",  "STOP",  "STOPPAGE         ",
                        "PULL",  "GOALIE           ",  "HIT",   "HIT (!)          ",
                        "HIT",   "HIT (*)          ",  "SHOT",  "SHOT (!)         ",
                        "SHOT",  "SHOT (*)         "), nrow=2)