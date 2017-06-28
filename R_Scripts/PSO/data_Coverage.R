source("./R_Code/PSO/utils.R")
source("./R_Code/PSO/dimension_pre_processing.R")

library(plyr)
library(ggplot2)

source("./R_Code/PSO/data_loader.R")
load_all()

nrow(odds)
length(unique(odds$uri))

#games = myjoin(games,competitions[competitions$level>=3,],"competition","Competition",join_type = "")
#games = games[games$year>2003,]


nrow(games)
final_scores = pso[pso$is_last_shot==1,]
nrow(final_scores)

nrow(pso)

#Coverage analysis
#shootout coverage
nrow(myjoin(games,final_scores))
games$is_covered = 1;
games$is_covered[is.na(myjoin(games,final_scores)$is_last_shot)] = 0;

#keepers coverage
with_keepers = myjoin(final_scores,keepers,"home_keeper")
with_keepers = myjoin(with_keepers,keepers,"away_keeper")
length(unique(c(with_keepers$home_keeper_uri,with_keepers$away_keeper_uri)))
sum(with_keepers$home_keeper_c_matches > 10 & !is.na(with_keepers$home_keeper_c_matches) &
      with_keepers$away_keeper_c_matches > 10 & !is.na(with_keepers$away_keeper_c_matches))
sum(with_keepers$home_keeper_n_saved_penalties > 0 & !is.na(with_keepers$home_keeper_n_saved_penalties) &
      with_keepers$away_keeper_n_saved_penalties > 0 & !is.na(with_keepers$away_keeper_n_saved_penalties))
#transfermarkt keepers
sum(is.na(keepers$n_saved_penalties))
sum(!is.na(keepers$n_conceeded_penalties) & keepers$n_conceeded_penalties>0)
sum(!is.na(keepers$c_matches) & keepers$c_matches>0)


#shooter coverage
with_shooter = myjoin(pso,shooters,"shooter")
length(unique(with_shooter$shooter_uri))
sum(with_shooter$shooter_c_matches > 10 & !is.na(with_shooter$shooter_c_matches))
sum(with_shooter$shooter_n_missed_penalties > 0 & !is.na(with_shooter$shooter_n_missed_penalties))
#transfermarkt shooters
sum(is.na(shooters$n_missed_penalties))
nrow(shooters)

#odds coverage
nrow(myjoin(games,odds))
nrow(myjoin(games,odds,join_type=""))
nrow(myjoin(final_scores,odds,join_type=""))

games$is_covered = 1;
games$is_covered[is.na(myjoin(games,odds)$odds_h)] = 0;

#Analysis of uncovered games
year_wise = ddply(games,c("year"),summarise,n_matches=length(is_covered),covered_matches = sum(is_covered),coverage = sum(is_covered)/length(is_covered))
year_wise[order(year_wise$n_matches * -1),]
comp_wise = ddply(games,c("competition"),summarise,total=length(is_covered),valid = sum(is_covered),coverage = sum(is_covered)/length(is_covered))
comp_wise[order(comp_wise$total * -1),]
n_competitions_by_year = ddply(games,c("year"),summarise,n_competitions=length(unique(competition)))
n_competitions_by_year[order(n_competitions_by_year$n_competitions * -1),]

games$after_2000 = ifelse(games$year>2000,1,0)
x = ddply(games,c("competition"),summarise,total_matches=length(is_covered),matches_after_2000 = sum(after_2000),coverage_shootout = sum(is_covered)/length(is_covered))
x[order(x$total_matches * -1),]

load_all()
#Comparison with Management Science data
pre_2003 = final_scores[final_scores$year<2003 | (final_scores$year==2003 & final_scores$month<=6),]
x = ddply(pre_2003,c("competition"),summarise,total_matches=length(competition))
x[order(x$total_matches * -1),]

ms = read.csv("/home/aditya/Research Data/other_papers/management_science_data_summary.csv")
x = ddply(games[games$year<2003 | (games$year==2003 & games$month<=6),],c("competition"),summarise,total_matches=length(competition))
sum(x$total_matches)
x[order(x$total_matches * -1),]
myjoin(ms,x,c1="competition",c2="competition")


aer = read.csv("/home/aditya/Research Data/other_papers/aer_data_summary.csv")

aer_map = read.csv("/home/aditya/Research Data/other_papers/AER/competition_mapping.csv")
df = myjoin(final_scores[final_scores$year<2008 | (final_scores$year==2008 & final_scores$month<=6),],aer_map,c1="competition",c2="my_competition",join_type="")
x = ddply(df,c("competition_aer_competition"),summarise,total_matches=length(competition))
x
myjoin(aer,x,c1="competition",c2="competition")
j$competition_total_matches
head(aer)
aer = read.csv("/home/aditya/Research Data/other_papers/aer.csv")


#Comparison with AER book
pre_2013 = final_scores[final_scores$season<2013,]
x = ddply(pre_2013,c("competition"),summarise,total_matches=length(competition))
x[order(x$total_matches * -1),]

ms = read.csv("/home/aditya/Research Data/other_papers/management_science_data_summary.csv")
x = ddply(games[games$year<2003 | (games$year==2003 & games$month<=6),],c("competition"),summarise,total_matches=length(competition))
sum(x$total_matches)
x[order(x$total_matches * -1),]
myjoin(ms,x,c1="competition",c2="competition")
