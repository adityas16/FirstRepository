library(plyr)

BASE_FOLDER="/home/aditya/Research Data/weltfussball"
CSV_FOLDER=paste(BASE_FOLDER,"extractedCSV",sep="/")
mydata <- read.csv(paste(BASE_FOLDER,"PSO_combined.csv",sep="/"))
goals <- read.csv(paste(CSV_FOLDER,"goals.csv",sep="/"))
players <- read.csv(paste(BASE_FOLDER,"players.csv",sep="/"))

f=factor(players$position,levels=c("DEFENDER","MIDFIELDER","FORWARD"),ordered=T)
players$positionValue= as.numeric(f)
players$striker = players$id

mydata$shooterPosition = as.numeric( join(mydata,players,by="striker")[,"positionValue"])
mydata$isShooterForward = ifelse(mydata$shooterPosition==3,1,0)
mydata$isShooterDefender = ifelse(mydata$shooterPosition==1,1,0)
mydata$isShooterMidfielder = ifelse(mydata$shooterPosition==2,1,0)

mydata$shooterPosition = 
  factor(players$position,levels=c("DEFENDER","MIDFIELDER","FORWARD"))

#mydata = mydata[mydata$kickNumber<11,]
mydata$isTeamAKick = mydata$kickNumber%%2
mydata$isTeamBKick = 1 - mydata$isTeamAKick
mydata$stageImportance = (3 - mydata$distFromFinal)

mydata$PrPreShot = ifelse(mydata$isTeamAKick == 1, mydata$pAPreShot,1 - mydata$pAPreShot)
mydata$PrIfScored = ifelse(mydata$isTeamAKick == 1, mydata$pAIfscored,1 - mydata$pAIfscored)
mydata$PrIfMissed = ifelse(mydata$isTeamAKick == 1, mydata$pAIfMissed,1 - mydata$pAIfMissed)

mydata$teamAGoal = mydata$isTeamAKick * mydata$isConverted
mydata$teamBGoal = mydata$isTeamBKick * mydata$isConverted

attach(mydata)


sum(teamAGoal[round==1])/sum(isTeamAKick[round==1])
sum(teamBGoal[round==1])/sum(isTeamBKick[round==1])

sum(teamAGoal[round==2])/sum(isTeamAKick[round==2])
sum(teamBGoal[round==2])/sum(isTeamBKick[round==2])

sum(teamAGoal[round>5])/sum(isTeamAKick[round>5])
sum(teamBGoal[round>5])/sum(isTeamBKick[round>5])

sum(teamAGoal)/sum(isTeamAKick)
sum(teamBGoal)/sum(isTeamBKick)
sum(isConverted)/length(isConverted)

#impact params
mydata$impact = (PrIfScored - PrIfMissed) 
sum(pIfMissed - PrIfMissed)
sum(pPreShot - PrPreShot)

mydata$gain = (mydata$PrIfScored - PrPreShot) 
mydata$distFromCentre = abs(0.5 - pAPreShot)


g=glm(isConverted~ mydata$gain +  mydata$PrPreShot + mydata$distFromCentre + mydata$impact ,family=binomial("logit"))
summary(g)

g=glm(isConverted~  mydata$PrPreShot + mydata$distFromCentre ,family=binomial("logit"))
summary(g)

g=glm(isConverted~  mydata$PrPreShot + mydata$distFromCentre +  endIfScore + endIfMiss ,family=binomial("logit"))
summary(g)

g=glm(isConverted~   mydata$distFromCentre +  endIfScore + endIfMiss ,family=binomial("logit"))
summary(g)

g=glm(isConverted~   mydata$distFromCentre +  endIfScore + endIfMiss + kickNumber,family=binomial("logit"))
summary(g)


g=glm(isConverted~   mydata$distFromCentre +  endIfScore + endIfMiss + kickNumber +  kickNumber:isTeamAKick,family=binomial("logit"))
summary(g)

g=glm(isConverted~ mydata$stageImportance + mydata$distFromCentre + endIfScore + endIfMiss + kickNumber + kickNumber:isTeamAKick,family=binomial("logit"))
summary(g)

g=glm(isConverted~ mydata$stageImportance + mydata$distFromCentre + endIfScore + endIfMiss + kickNumber + mydata$stageImportance:mydata$distFromCentre + kickNumber:isTeamAKick,family=binomial("logit"))
summary(g)

g=glm(isConverted~ isShooterForward + mydata$stageImportance + mydata$distFromCentre + endIfScore + endIfMiss + kickNumber + mydata$stageImportance:mydata$distFromCentre + kickNumber:isTeamAKick,family=binomial("logit"))
summary(g)

g=glm(isConverted~ isShooterForward + mydata$stageImportance + mydata$distFromCentre + endIfScore + endIfScore:isShooterForward + endIfMiss + kickNumber + mydata$stageImportance:mydata$distFromCentre + kickNumber:isTeamAKick,family=binomial("logit"))
summary(g)





g=glm(isConverted~ mydata$gain + mydata$impact ,family=binomial("logit"))
summary(g)

g=glm(isConverted~ mydata$gain + mydata$impact ,family=binomial("logit"))
summary(g)


l = lm(gain ~ distFromCentre + endIfMiss + endIfScore + kickNumber + PrIfMissed)
summary(l)

g=glm(isConverted~  mydata$distFromCentre +  endIfScore + endIfMiss ,family=binomial("logit"))
summary(g)

g=glm(isConverted~ mydata$PrPreShot  + expKicksLeft,family=binomial("logit"))
summary(g)

g=glm(isConverted~ mydata$PrIfScored + mydata$expKicksLeft +mydata$shooterPosition  ,family=binomial("logit"))
summary(g)

mydata$expKicksLeft = mydata$expKicksLeft^2
g=glm(isConverted~ mydata$expKicksLeft  + endIfScore + endIfMiss + mydata$isShooterForward:endIfScore ,family=binomial("logit"))
summary(g)

g=glm(isConverted~   mydata$distFromCentre + mydata$PrPreShot,family=binomial("logit"))
summary(g)


g=glm(isConverted~ isTeamAKick + kickNumber + mydata$distFromCentre + isShooterDefender,family=binomial("logit"))
summary(g)


g=glm(isConverted~ endIfScore + endIfMiss + kickNumber + kickNumber:isTeamAKick + endIfMiss:isShooterDefender,family=binomial("logit"))
summary(g)


g=glm(isConverted~ mydata$gain + mydata$PrPreShot + mydata$stageImportance:mydata$PrPreShot + mydata$distFromCentre + mydata$impact + mydata$distFromCentre + mydata$impact ,family=binomial("logit"))
summary(g)



g=glm(isConverted~ mydata$stageImportance + expKicksLeft + endIfScore +  isTeamAKick + isTeamAKick:expKicksLeft + endIfScore:mydata$isShooterForward + endIfScore:mydata$isShooterMidfielder,family=binomial("logit"))
summary(g)

ddply(mydata[competition=="World Cup " | competition=="EURO ",], c("shooterPosition"), summarise,conversion_ratio = sum(isConverted)/length(isConverted),total = length(isConverted))
ddply(mydata, c("shooterPosition"), summarise,conversion_ratio = sum(isConverted)/length(isConverted),total = length(isConverted))

ddply(mydata[competition=="World Cup ",], c("round"), summarise,conversion_ratio = sum(isConverted)/length(isConverted),total = length(isConverted))

mydata[mydata$competition=="World Cup ",]
mydata$competition

write.csv(ddply(mydata, c("competition"), summarise,total = length(unique(game_id))),file="/home/aditya/temp_files/temp.csv",row.names=FALSE)
sum(a$total)


ddply(mydata, c("round"), summarise,conversion_ratio_a = sum(teamAGoal)/sum(isTeamAKick),conversion_ratio_b = sum(teamBGoal)/sum(isTeamBKick),total = length(isConverted))
sum(teamAGoal)/sum(isTeamAKick)
sum(teamBGoal)/sum(isTeamBKick)


ddply(mydata, c("gd_direction"), summarise,conversion_ratio_a = sum(teamAGoal)/sum(isTeamAKick),conversion_ratio_b = sum(teamBGoal)/sum(isTeamBKick),total = length(isConverted))
(gd_direction - sign(teamAScore-teamBScore))[isTeamAKick==1]

sum(teamBGoal[round==2])/sum(isTeamBKick[round==2])
mydata$isPreviousConverted = c(1,1,isConverted[1:(length(isConverted)-2)])
attach(mydata)
ddply(mydata[round!=1,], c("isPreviousConverted"), summarise,conversion_ratio = sum(isConverted)/length(isConverted),total = length(isConverted))

round2_5 = mydata[round>1 & round<6,]

round2_5$shooter_gd_direction = gd_direction * (1 - (2 * isTeamBKick))
round2_5$shooter_gd_direction[isTeamBKick==1] = gd_b[isTeamBKick==1]
attach(round2_5)
g=glm(isConverted~    isPreviousConverted + kickNumber + isTeamAKick  ,family=binomial("logit"))
summary(g)


inplayPenalties = goals[goals$isPenalty=="true",]
topShooters = data.frame(unique(inplayPenalties$scorer))
colnames(topShooters)  = c("scorer")

head(mydata)
library(sqldf)
topShooterPenalties = sqldf("SELECT mydata.*, topShooters.scorer as 
              FROM mydata left outer join topShooters
            on mydata.striker = topShooters.scorer")

sum(topShooterPenalties$isConverted)/length(topShooterPenalties$isConverted)
sum(isConverted)/length(isConverted)

ddply(topShooterPenalties[topShooterPenalties$kickNumber<11,], c("kickNumber"), summarise,total = length(isConverted))
ddply(mydata[mydata$kickNumber<11,], c("kickNumber"), summarise,total = length(isConverted))

ddply(topShooterPenalties, c(""), summarise,total = length(isConverted))

