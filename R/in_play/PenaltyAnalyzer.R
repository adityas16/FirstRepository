library(plyr)
library(sqldf)
# mydata <- read.csv("/home/aditya/Research Data/penalties.csv")
mydata <- read.csv("/home/aditya/workspace/data-service/penalties.csv")

mydata$GD = mydata$home_score - mydata$away_score
mydata$shooting_side = mydata$is_awarded_to_home * 2 -1
mydata$pressure = sign(mydata$GD) * mydata$shooting_side
mydata$shot_coordinateY = mydata$horizontal_accuracy
mydata$is_good_shot = abs(mydata$horizontal_accuracy)/120 * mydata$is_on_target
mydata$is_good_shot = mydata$is_good_shot > 0
head(mydata)

#pressure 
ddply(mydata, c("pressure"), summarise,goal_ratio = sum(is_goal)/length(event_id),total = length(event_id))

data_shot_info = mydata[mydata$is_on_target>-1,]
mydata$is_on_target
ddply(data_shot_info, c("pressure"), summarise,on_target_ratio = sum(is_on_target)/length(event_id),total = length(event_id))
ddply(data_shot_info, c("pressure"), summarise,is_good_shot_ratio = sum(is_good_shot)/length(event_id),total = length(event_id))
ddply(data_shot_info, c("pressure"), summarise,is_goal_ratio = sum(is_goal)/length(event_id),total = length(event_id))

lm.pressure = glm(is_goal ~ pressure,data=mydata)
summary(lm.pressure)

#striker-keeper
keeper_aggregate = ddply(mydata, c("keeper"), summarise,prevention = 1-sum(is_goal)/length(event_id),total = length(event_id))
striker_aggregate = ddply(mydata, c("striker"), summarise,conversion = sum(is_goal)/length(event_id),total = length(event_id))

striker_aggregate = striker_aggregate[order(-striker_aggregate$conversion),]
keeper_aggregate = keeper_aggregate[order(-keeper_aggregate$prevention),]

keeper_aggregate = keeper_aggregate[keeper_aggregate$total>10,]
striker_aggregate = striker_aggregate[striker_aggregate$total>5,]

striker_aggregate$strength = sign(striker_aggregate$conversion - 0.8)
striker_aggregate = within(striker_aggregate, {
  strength = ifelse(conversion >= 0.8, 1, -1)
})
keeper_aggregate = within(keeper_aggregate, {
  strength = ifelse(prevention >= 0.3, 1, -1)
})

data_striker_keeper = mydata[c("event_id","keeper","striker","is_goal","pressure")]
data_striker_keeper = sqldf("select event_id,keeper,striker,is_goal,keeper_aggregate.strength as keeper_strength, striker_aggregate.strength as striker_strength from keeper_aggregate join data_striker_keeper using(keeper) join striker_aggregate using (striker)")

ddply(data_striker_keeper, c("keeper_strength","striker_strength"), summarise,goals = sum(is_goal)/length(event_id),total = length(event_id))

hist(keeper_aggregate[keeper_aggregate$total>10,]$prevention)
hist(striker_aggregate[striker_aggregate$total>10,]$conversion)

hist(data_shot_info[data_shot_info$is_goal==1 & abs(data_shot_info$shot_coordinateY) <= 750,]$shot_coordinateY,breaks = 14)

hist(data_shot_info[data_shot_info$is_goal==0 & abs(data_shot_info$shot_coordinateY) <= 750,]$shot_coordinateY,breaks = 14)

hist(mydata[mydata$is_goal==0 & abs(mydata$horizontal_accuracy) <= 350,]$horizontal_accuracy,breaks = 14)


#time taken to conversion rate
mydata$time_taken_bucketized = floor(mydata$time_taken /1000 / 5)
conversion_by_time_taken = ddply(mydata, c("time_taken_bucketized"), summarise,conversion_rate = sum(is_goal)/length(event_id),total = length(event_id))
conversion_by_time_taken
conversion_by_time_taken = conversion_by_time_taken[conversion_by_time_taken$total>=30,]
plot(x =  conversion_by_time_taken$time_taken_bucketized,y=conversion_by_time_taken$conversion_rate)


lm.pressure = glm(is_goal ~ time_taken,data=mydata)
summary(lm.pressure)
