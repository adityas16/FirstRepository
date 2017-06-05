#forecast <- read.csv("/home/aditya/BetifyData/AdjustmentEstimation/EPL_data/probGenerated.csv",header = FALSE)
forecast <- read.csv("/home/aditya/BetifyData/AdjustmentEstimation/NHL_data/2014/probGenerated.csv")
goals <- read.csv("/home/aditya/BetifyData/AdjustmentEstimation/NHL_data/2014/goals_test.csv")

max_gd = 3



first_gd_col = 3
last_gd_col = (first_gd_col+2*max_gd )

for (i in 4:last_gd_col){
  forecast[i] = forecast[i] + forecast[i-1]
}

first_RPS_col = last_gd_col + 2
for (i in (-1*max_gd):max_gd){
  forecast[first_RPS_col  + i + 3] <- ifelse(forecast[last_gd_col+1] <= i,1,0)
}
last_RPS_col = first_RPS_col + 2*max_gd
forecast[last_RPS_col] = 1
forecast$rps_score = 0
for (i in (-1*max_gd):max_gd){
  forecast$rps_score = forecast$rps_score + (forecast[first_gd_col + max_gd + i] - forecast[first_RPS_col + max_gd + i])^2
}

forecast$rps_score = forecast$rps_score/(2*max_gd)


for (i in last_gd_col:4){
  forecast[i] = forecast[i] - forecast[i-1]
}


forecast$pA = 0
forecast$pH = 0
for (i in (-1*max_gd):-1){
  forecast$pA = forecast$pA + forecast[first_gd_col + max_gd + i]
}
for (i in 1:max_gd){
  forecast$pH = forecast$pH + forecast[first_gd_col + max_gd + i]
}
forecast$pT = 0

forecast$pT = forecast[first_gd_col + max_gd]
forecast$result = sign(forecast$finalGD)+1
forecast$pH_RPS = 1
forecast$pT_RPS = ifelse(forecast$result <=1,1,0)
forecast$pA_RPS = ifelse(forecast$result ==0 ,1,0)

forecast$rps_result = 0
forecast$rps_result = forecast$rps_result + (forecast$pT + forecast$pA - forecast$pT_RPS)^2
forecast$rps_result = forecast$rps_result + (forecast$pA - forecast$pA_RPS)^2
forecast$rps_result = forecast$rps_result/2

aggdata  =  ddply(forecast, c("time"),RPS_score = mean(rps_score),RPS_result = mean(rps_result),summarise)


require(ggplot2)
#all = subset(aggdata, method %in% list("w0_all_full","w3_all_full"))
#all
ggplot(aggdata, aes(time, RPS)) + 
  geom_line() + 
  geom_point()

observed_goal_aggregate = ddply(goals[goals$time<=60,],c("time"),summarise,n=length(time))
ggplot(observed_goal_aggregate, aes(time, n)) + 
  geom_line() + 
  geom_point()



colnames(aggdata) <- c("time", "method", "RPS")


hsup = subset(aggdata, method %in% list("w5_all_hsup","w5_CT_hsup_hsup"))
hsup
ggplot(hsup, aes(time, RPS,colour=method)) + 
  geom_line() + 
  geom_point()



asup = subset(aggdata, method %in% list("w5_all_asup","w5_CT_asup_asup"))
asup
ggplot(asup, aes(time, RPS,colour=method)) + 
  geom_line() + 
  geom_point()



neutral = subset(aggdata, method %in% list("w4_all_neutral","w4_neutral_neutral"))
neutral
ggplot(neutral, aes(time, RPS,colour=method)) + 
  geom_line() + 
  geom_point()



type = "w5_neutral"
file = paste(paste("/home/aditya/BetifyData/AdjustmentEstimation/EPL_data/s2013_", 
                    type,sep = ""),"_state_adj.csv",sep = "")
s1 <- read.csv(file,header = FALSE)
s1[4] = type
colnames(s1) <- c("state", "home_adj", "away_adj","type")

type = "w5_CT_hsup"
file = paste(paste("/home/aditya/BetifyData/AdjustmentEstimation/EPL_data/s2013_", 
                    type,sep = ""),"_state_adj.csv",sep = "")
s2 <- read.csv(file,header = FALSE)
s2[4] = type
colnames(s2) <- c("state", "home_adj", "away_adj","type")


type = "w5_CT_asup"
file = paste(paste("/home/aditya/BetifyData/AdjustmentEstimation/EPL_data/s2013_", 
                   type,sep = ""),"_state_adj.csv",sep = "")
s3 <- read.csv(file,header = FALSE)
s3[4] = type
colnames(s3) <- c("state", "home_adj", "away_adj","type")

ggplot(rbind(s1,s2,s3), aes(state, away_adj,colour=type)) + 
  geom_line() + 
  geom_point()


type = "w5_CT_asup"
file = paste(paste("/home/aditya/BetifyData/AdjustmentEstimation/EPL_data/s2013_", 
                   type,sep = ""),"_time_adj.csv",sep = "")
s1 <- read.csv(file,header = FALSE)
s1[4] = "w5_CT_away"
colnames(s1) <- c("time", "home_adj", "away_adj","type")
ggplot(rbind(s1), aes(time, away_adj,colour=type)) + 
  geom_line() + 
  geom_point()
