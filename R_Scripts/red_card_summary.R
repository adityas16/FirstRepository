red_cards  = read.csv(paste(DATA_FOLDER,"extractedCSV/red_cards.csv",sep="/"))
ddply(red_cards[red_cards$competition == "Premier League",],c("season"),n=length(uri) / 380,summarise)
ddply(red_cards[red_cards$competition == "Bundesliga",],c("season"),n=length(uri),summarise)


games  = read.csv("C:\\Users\\adity\\Dropbox\\Nils_Aditya_Shetty_Share\\Welt_Betexplorer_data\\games.csv")

x=myjoin(red_cards,games,join_type = "")


ddply(x[x$competition == "Premier League",],c("season"),n=length(uri),summarise)
to_csv(x[,c("uri","is_home","time")])


goals  = read.csv("C:\\Users\\adity\\Dropbox\\Nils_Aditya_Shetty_Share\\Welt_Betexplorer_data\\goals.csv")
x=myjoin(goals,games,join_type = "")
to_csv(ddply(x[x$isPenalty==1,],c("competition"),n=length(uri),summarise))
