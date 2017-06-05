match_id="640.csv"
striker_id=43318
keeper_id=43298
time_taken=47880
foul_time=379320

match_id="638.csv"
striker_id=43168
keeper_id=43172
time_taken=45400
foul_time=27100


shot_time=time_taken + foul_time
data <- read.csv( paste("/home/aditya/Research Data/Tracking/tracking_",match_id,sep=""),header = TRUE)
data
colnames(data) <- c("match_id", "match_half","match_time","player_squad_id","team_id","x","y")
summary(data)

striker_movement = data[data$player_squad_id==striker_id,]
par(mfrow=c(2,1))
plot(striker_movement$match_time, striker_movement$y)
abline(v=427200)
plot(striker_movement$match_time, striker_movement$x)
abline(v=427200)
plot(abs(filter(diff(striker_movement$x),rep(1,20),sides=1)))
abline(v=(427200-319400)/100)
plot(abs(filter(diff(striker_movement$y),rep(1,20),sides=1)))
abline(v=(427200-319400)/100)


player_id=keeper_id
player_movement = data[data$player_squad_id==player_id,]
par(mfrow=c(2,2))
plot(player_movement$match_time, player_movement$y,ylab="keeper_y",xlab="matchi time")
abline(v=shot_time)
abline(h=0)
plot(player_movement$match_time, player_movement$x,ylab="keeper_x",xlab="matchi time")
abline(v=shot_time)

plot(abs(filter(diff(striker_movement$x),rep(1,20),sides=1)))
abline(v=(427200-319400)/100)
plot(abs(filter(diff(striker_movement$y),rep(1,20),sides=1)))
abline(v=(427200-319400)/100)

plot(speed_smooth)
library(rgl)
plot3d(striker_movement$x,striker_movement$y,striker_movement$match_time - 319400)

?plot3d

