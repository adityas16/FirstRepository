#TODO: handle the diff in timezone between the two sources
library(sqldf)
odds = read.csv("/home/aditya/Research Data/NHL/odds mapping/secondary.csv")
mapping = read.csv("/home/aditya/Research Data/NHL/odds mapping/team_name_mapping.csv")
all_games = read.csv("/home/aditya/Research Data/R_output/nhl_games.csv")
all_games = sqldf("select a.*,mh.odds_name as odds_hometeam,ma.odds_name as odds_awayteam from all_games a,mapping mh,mapping ma where
      a.hometeam = mh.NHL_name
      and a.awayteam = ma.NHL_name")

joined =sqldf("select g.season as season,* from odds o,all_games g where
      (o.day = g.day or o.day-1 = g.day)
      and o.month = g.month
      and o.year=g.year
      and o.home_team = g.odds_hometeam
      and o.away_team = g.odds_awayteam")
joined$game_id = paste( joined$season, joined$gcode,sep="-")
normal = 1/joined$X1 + 1/joined$X2 + 1/joined$X
joined$pH = 1/(normal*joined$X1)
joined$pA = 1/(normal*joined$X2)
joined$pT = 1/(normal*joined$X)
x=joined[order(joined$game_id),]
x$is_repeated = 0
x$is_repeated[1:nrow(x)-1] = x$game_id[1:nrow(x)-1] == x$game_id[2:nrow(x)]
x = x[x$is_repeated==0,]
joined = x
write.csv(joined[,c("game_id","pH","pA")],paste(R_OUTPUT_FOLDER,"nhl_games_probs.csv",sep = '/'),row.names = FALSE)
summary(joined$pH + joined$pA)

test_season = 2014
window = 3
write.csv(joined[joined$season_start_year>=(test_season-window) & joined$season_start_year<test_season,c("game_id","pH","pA")],paste(R_OUTPUT_FOLDER,"nhl_odds_train.csv",sep = '/'),row.names = FALSE)
write.csv(joined[joined$season_start_year == test_season,c("game_id","pH","pA")],paste(R_OUTPUT_FOLDER,"nhl_odds_test.csv",sep = '/'),row.names = FALSE)

write.csv(all_goals[all_goals$season_start_year>(test_season-window)  & all_goals$season_start_year<test_season,],paste(R_OUTPUT_FOLDER,"nhl_goals_train.csv",sep = '/'),row.names = FALSE)
write.csv(all_goals[all_goals$season_start_year == test_season,],paste(R_OUTPUT_FOLDER,"nhl_goals_test.csv",sep = '/'),row.names = FALSE)


joined$diff = as.numeric(as.character(joined$home_score))- joined$homescore
x=joined[joined$diff!=0,]

as.integer(head(joined$home_score))