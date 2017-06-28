DATA_FOLDER = paste0(BASE_FOLDER , "/combined/")
games_raw = read.csv(paste(DATA_FOLDER,"games_raw.csv",sep="/"))

#Filter games to remove duplicates across cources
#remove copa del rey before 2003
games = games_raw[!(games_raw$competition=="Copa del Rey" & games_raw$season <= 2003 & games_raw$source == "weltfussball"),]

write.csv(games,paste(DATA_FOLDER,"/extractedCSV/games.csv",sep="/"),row.names = F)
load_all();
DATA_FOLDER = paste0(BASE_FOLDER , "/weltfussball/")


