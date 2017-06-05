my_games = read.csv("/home/aditya/Research Data/weltfussball/extractedCSV/games.csv",stringsAsFactors=FALSE)
ms_games = read.csv("/home/aditya/Research Data/other_papers/Reconciliation/ms.csv",stringsAsFactors=FALSE)
aer_games = read.csv("/home/aditya/Research Data/other_papers/AER/aer.csv",stringsAsFactors=FALSE)
aer_games = aer_games[aer_games$season<=2003,]
all_scores = myjoin(my_games,final_scores)
ms_games_joined = myjoin(ms_games,all_scores,c1="our_uri")

ms_our_intersect = ms_games_joined[!is.na(ms_games_joined$our_uri_uri),]
ddply(ms_our_intersect,c("our_uri_dist_from_final"),n=length(dist_from_final),prop = myprop(is_teamA_win),summarise)

#ms_our_intersect = ms_games_joined[!is.na(ms_our_intersect$our_uri_is_team_A_winner),]
#ms_our_intersect[
#!is.na(ms_our_intersect$our_uri_is_team_A_winner) & (ms_our_intersect$is_teamA_win != ms_our_intersect$our_uri_is_team_A_winner),]$our_uri_dist_from_final
ms_games_joined = ms_games_joined[,c("game_id","our_uri_uri","aer_id","competition_name","dist_from_final","is_teamA_win","our_uri_dist_from_final")]


#pick round info from ms if I dont have it
ms_games_joined$our_uri_dist_from_final[is.na(ms_games_joined$our_uri_dist_from_final)] = ms_games_joined$dist_from_final[is.na(ms_games_joined$our_uri_dist_from_final)] 
ms_games_joined$dist_from_final = ms_games_joined$our_uri_dist_from_final


#Rename fields
names(ms_games_joined)[names(ms_games_joined)=="our_uri_uri"] <- "our_id"
names(ms_games_joined)[names(ms_games_joined)=="game_id"] <- "ms_id"


ddply(ms_games_joined,c("dist_from_final"),n=length(dist_from_final),prop = length(dist_from_final)/length(ms_games_joined$dist_from_final),summarise)


sum(!is.na(ms_games_joined$our_id))
sum(!is.na(ms_games_joined$aer_id))

ddply(ms_games_joined,c("competition_name"),n=length(dist_from_final),prop = length(dist_from_final)/length(ms_games_joined$dist_from_final),summarise)

ddply(ms_games_joined,c("dist_from_final"),n=length(dist_from_final),prop = myprop(is_teamA_win),summarise)


#aer ms intersect
ms_aer_intersect = ms_games_joined[!is.na(ms_games_joined$aer_id),];
ddply(ms_aer_intersect,c("dist_from_final"),n=length(dist_from_final),prop = myprop(is_teamA_win),summarise)

sum(ms_games_joined[!is.na(ms_games_joined$aer_id),]$is_teamA_win)
length(ms_games_joined[!is.na(ms_games_joined$aer_id),]$is_teamA_win)
write.csv(ms_games_joined,"/home/aditya/Research Data/other_papers/Reconciliation/mapped_data.csv",row.names = F)




#testing of matching with our data
all_scores = myjoin(my_games,final_scores)
all_scores$homeScore = all_scores$teamAScore
all_scores$awayScore = all_scores$teamBScore

my_games$homeScore = all_scores$homeScore
my_games$awayScore = all_scores$awayScore
ms_our_games = myjoin(ms_games,my_games,c1="our_uri")

ms_for_test = ms_our_games[,c("game_id","season","our_uri","home_team","away_team","our_uri_awayTeam","our_uri_homeTeam","home_score","away_score","our_uri_homeScore","our_uri_awayScore")]
ms_for_test$diff = (ms_for_test$home_score * ms_for_test$away_score) - (ms_for_test$our_uri_homeScore * ms_for_test$our_uri_awayScore)

write.csv(ms_for_test,"/home/aditya/Research Data/other_papers/Reconciliation/ms_our_games.csv",row.names = F)



#testing of matching with aer data
ms_aer_games = myjoin(ms_games,aer_games,c1="aer_id",c2 = "game_id")

ms_for_test = ms_aer_games[,c("game_id","season","aer_id_season","aer_id","home_team","away_team","aer_id_home_team","aer_id_away_team","home_score","away_score","aer_id_home_score","aer_id_away_score","is_teamA_win")]
ms_for_test$diff = (ms_for_test$home_score * ms_for_test$away_score) - (ms_for_test$aer_id_home_score * ms_for_test$aer_id_away_score)

write.csv(ms_for_test,"/home/aditya/Research Data/other_papers/Reconciliation/ms_aer_games.csv",row.names = F)
sum(!is.na(ms_our_games$aer_id))

ms_our_games$ms_id_home_teamAdj =  ifelse(ms_our_games$homeScore == ms_our_games$ms_id_home_score,ms_our_games$ms_id_home_team,ms_our_games$ms_id_away_team)
ms_our_games$ms_id_away_teamAdj =  ifelse(ms_our_games$homeScore != ms_our_games$ms_id_home_score,ms_our_games$ms_id_home_team,ms_our_games$ms_id_away_team)

ms_our_games$ms_id_home_team = ms_our_games$ms_id_home_teamAdj
ms_our_games$ms_id_away_team = ms_our_games$ms_id_away_teamAdj

ms_for_test = ms_our_games[,c("uri","homeTeam","awayTeam","ms_id_home_team","ms_id_away_team","homeScore","awayScore","ms_id_home_score","ms_id_away_score")]

write.csv(ms_for_test,"/home/aditya/Research Data/other_papers/Reconciliation/ms_our_games.csv",row.names = F)