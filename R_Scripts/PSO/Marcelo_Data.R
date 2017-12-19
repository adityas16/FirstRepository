library(sqldf)
setdiff(games$uri,final_scores$uri)
uri=setdiff(games$uri,final_scores$uri)
games_invalid_seqence = data.frame(uri)
myjoin(games_invalid_seqence,games,join_type="")
x=myjoin(games_invalid_seqence,games,join_type="")

#Write dataset 1
dataset1_columns = c("uri","homeTeam","awayTeam","homeScore","awayScore","competition","season","round","dist_from_final","day","month","year","home_keeper","away_keeper","homeShotFirst")
x=sqldf("select g.*,f.homeShotFirst from games as g, final_scores as f where f.uri = g.uri")
to_csv((x[x$competition!="Copa del Rey",dataset1_columns]))
to_csv(x)
x=pso
to_csv((x[x$competition!="Copa del Rey",]))

#Write Dataset 2
dataset2_columns = c("uri","isConverted","kickNumber","is_home_shot","striker")
to_csv(pso[,dataset2_columns])

#Write Dataset 3, from processedCSV/pso_processed
model_output = read.csv(paste(DATA_FOLDER,"processedCSV/pso_model_output.csv",sep="/"))
model_output$endIfMiss = NULL
model_output$endIfScore = NULL
x=sqldf("select m.* from model_output as m, pso as p where p.uri = m.uri and p.kickNumber = m.kickNumber")
to_csv(x)


dataset3_columns = c("uri","isConverted","kickNumber","is_home_shot","striker")
to_csv(pso[,dataset2_columns])


all_competitions=read.csv(paste(TEMP_FOLDER,"Dataset1.csv",sep="/"))
x=data.frame(unique(all_competitions$competition))
x$competition = x$unique.all_competitions.competition.
x$unique.all_competitions.competition. = NULL
all_competitions = x

classification = read.csv(paste(WELT_FOLDER,"extractedCSV/competitions.csv",sep="/"))
all_competitions = myjoin(all_competitions,classification,c1="competition",c2="competition")
all_competitions$competition_transfermrkt_url = NULL
all_competitions$competition_num_shootouts = NULL
all_competitions$competition_competition = NULL

aer_competitions = read.csv(paste(RESEARCH_DATA_FOLDER,"other_papers/AER/competition_mapping.csv",sep="/"))
all_competitions = myjoin(all_competitions,aer_competitions,c1="competition",c2="my_competition")
all_competitions$is_aer_competition = ifelse(!is.na(all_competitions$competition_my_competition),1,0)
all_competitions$competition_aer_competition = NULL
all_competitions$competition_my_competition = NULL


#Error checking for cdl data
library(xlsx)
d1 = read.xlsx("/home/aditya/Research Data/DataEntry/combined/Dataset1.xls",sheetIndex = 1)
d2 = read.xlsx("/home/aditya/Research Data/DataEntry/combined/Dataset2.xls",sheetIndex = 1)
p = read.csv("/home/aditya/Research Data/DataEntry/combined/pso_extended.csv")

f = p[p$is_last_shot==1,]
f$is_home_winner = ifelse(f$homeScore> f$awayScore,1,0)

f$homeScore = ifelse(f$homeShotFirst,f$teamAScore,f$teamBScore)
f$awayScore = ifelse(f$homeShotFirst,f$teamBScore,f$teamAScore)
to_csv(f)
x = sqldf("select * from f,d1 where f.uri = d1.uri")
x = sqldf("select * from d2,d1 where d2.uri = d1.uri")
x = myjoin(f,d1,join_type="")

x = p[p$isFirstShot==1,]
sum(x$homeShotFirst == x$is_home_shot)

#CDL 2016
f = final_scores[final_scores$season==2016 & final_scores$competition == "Copa del Rey",]
