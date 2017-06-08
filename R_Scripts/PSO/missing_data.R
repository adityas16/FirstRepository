matches_with_no_page = read.csv(paste(WELT_FOLDER,"shootout_games_without_match_page.csv",sep="/"))
matches_with_no_page_pre_2013 = matches_with_no_page[matches_with_no_page$season<=2012,]
x=ddply(matches_with_no_page,c("competition"),n=length(competition),summarize)
x[order(x$n * -1),]


#Games with invalid sequence
uri = setdiff(games$uri,pso$uri)
invalid_games = data.frame(uri)
invalid_games =myjoin(invalid_games,games)
pre_2013 = invalid_games[invalid_games$season<2013,]
x = ddply(pre_2013,c("competition"),summarise,total_matches=length(competition))
x[order(x$total_matches * -1),]


english_cups = pre_2013[pre_2013$competition == "FA Cup" | pre_2013$competition == "League Cup",]
ddply(english_cups,c("season"),summarise,total_matches=length(competition))

german_cups = pre_2013[pre_2013$competition == "DFB-Pokal",]
ddply(german_cups,c("season"),summarise,total_matches=length(competition))

russian_cups = final_scores[final_scores$competition == "Superkubok",]
ddply(russian_cups,c("season"),summarise,total_matches=length(competition))
