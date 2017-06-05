library(plyr)
library(sqldf)
# mydata <- read.csv("/home/aditya/Research Data/penalties.csv")
mydata <- read.csv("/home/aditya/Research Data/PSO/PSO_Big.csv")
mydata

head(mydata)
mydata$id=seq(1,length(mydata$match_id))
mydata$gd = mydata$home_goals - mydata$away_goals
mydata$is_scored_previous = c(0,mydata$is_scored[1:length(mydata$is_scored)-1])
mydata$is_home_shot = mydata$is_scored * (mydata$kick_number %% 2)
mydata$home_previous = mydata$home_goals - mydata$is_scored * mydata$is_home_shot
mydata$away_previous = mydata$away_goals - mydata$is_scored * (1- mydata$is_home_shot)
mydata$gd_previous = mydata$home_previous - mydata$away_previous
mydata[mydata$gd_previous==-2 & mydata$kick_number==8,]
mydata$shooter_gd_previous = mydata$gd_previous * (-1 + 2 * mydata$is_home_shot)

#statewise
ddply(mydata, c("gd","kick_number"), summarise,goal_ratio = sum(is_scored)/length(id),total = length(id))

analysis = ddply(mydata, c("shooter_gd_previous","kick_number"), summarise,goal_ratio = sum(is_scored)/length(id),total = length(id))
analysis = ddply(mydata, c("gd_previous","kick_number"), summarise,goal_ratio = sum(is_scored)/length(id),total = length(id))
analysis = analysis[analysis$kick_number<=10,]

analysis = analysis[analysis$total>10,]
analysis = analysis[analysis$goal_ratio<0.99,]
analysis = analysis[analysis$goal_ratio>0.01,]

analysis =analysis[order(analysis$kick_number),]
analysis
write.csv(analysis,file="/home/aditya/temp_files/WCPenaltyShootoutsAnalysis.csv",row.names = FALSE)

  ggplot(data=analysis, aes(x=analysis$kick_number, y=analysis$shooter_gd_previous)) +
  geom_point(aes(size=analysis$goal_ratio^2)) +
  scale_size_continuous(range=c(0,10)) +
  theme(legend.position = "none")
sum(mydata$is_scored)
length(mydata$is_scored)
