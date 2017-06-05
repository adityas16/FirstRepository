create_bucket_index = function(bucket_boundaries = c(5,10,15,20,25,30,35,40,45,50,58,60)){
  n_buckets = length(bucket_boundaries)
  bucket_index=rep(NA,bucket_boundaries[n_buckets])
  bucket_index[1] = 1
  bucket_boundaries = bucket_boundaries[1:n_buckets-1]
  bucket_index[bucket_boundaries+1] = 2:n_buckets
  bucket_index = na.locf(bucket_index)
  return(bucket_index)
}
#INPUT : aggregated_data

aggregated_data$adjusted_gd = aggregated_data$gd
aggregated_data$adjusted_gd[aggregated_data$gd>3] = 3
aggregated_data$adjusted_gd[aggregated_data$gd< -3] = -3

x=ddply(aggregated_data,c("adjusted_gd"),exp_home_goals = sum(exp_lamdaH),actual_home_goals = sum(home_goals),exp_away_goals = sum(exp_lamdaA),actual_away_goals = sum(away_goals),n_seconds=sum(time),summarise)
x$adjustment_h = x$actual_home_goals / x$exp_home_goals
x$adjustment_a = x$actual_away_goals / x$exp_away_goals
state_adj=x


#time adjustments initial estimate
x=ddply(aggregated_data,c("bucket_index"),exp_home_goals = sum(exp_lamdaH),actual_home_goals = sum(home_goals),exp_away_goals = sum(exp_lamdaA),actual_away_goals = sum(away_goals),summarise)
x$adjustment_h = x$actual_home_goals / x$exp_home_goals
x$adjustment_a = x$actual_away_goals / x$exp_away_goals
time_adj = x


#Recursive time and state adj
x=aggregated_data
for(i in 1:20){
  x$adjusted_lamdaH = x$exp_lamdaH * state_adj$adjustment_h[x$adjusted_gd + 4] 
  x$adjusted_lamdaA = x$exp_lamdaA * state_adj$adjustment_a[x$adjusted_gd + 4] 
  
  y=ddply(x,c("bucket_index"),exp_home_goals = sum(adjusted_lamdaH),actual_home_goals = sum(home_goals),exp_away_goals = sum(adjusted_lamdaA),actual_away_goals = sum(away_goals),summarise)
  y$adjustment_h = y$actual_home_goals / y$exp_home_goals
  y$adjustment_a = y$actual_away_goals / y$exp_away_goals
  y$adjustment_h=y$adjustment_h/sum(y$adjustment_h) * length(y$adjustment_h)
  y$adjustment_a=y$adjustment_a/sum(y$adjustment_a) * length(y$adjustment_a)
  #0.8114,0.9706
  time_adj = y
  
  x$adjusted_lamdaH = x$exp_lamdaH * time_adj$adjustment_h[x$bucket_index]
  x$adjusted_lamdaA = x$exp_lamdaA * time_adj$adjustment_a[x$bucket_index]
  y=ddply(x,c("adjusted_gd"),exp_home_goals = sum(adjusted_lamdaH),actual_home_goals = sum(home_goals),exp_away_goals = sum(adjusted_lamdaA),actual_away_goals = sum(away_goals),n_seconds=sum(time),summarise)
  y$adjustment_h = y$actual_home_goals / y$exp_home_goals
  y$adjustment_a = y$actual_away_goals / y$exp_away_goals
  state_adj=y
}

ggplot(time_adj,aes(bucket_index)) + scale_x_discrete()+
  geom_line(aes(y = adjustment_h, colour = "Home")) + 
  geom_line(aes(y = adjustment_a, colour = "Away")) +
  ylab('adjustment')+
  labs(color = "Side\n")

ggplot(state_adj,aes(adjusted_gd)) + scale_x_continuous(breaks = -3:3)+
  geom_line(aes(y = adjustment_h, colour = "Home")) + 
  geom_line(aes(y = adjustment_a, colour = "Away"))+
  ylab('adjustment')+
  labs(color = "Side\n")

my_xtable(state_adj[,c("adjusted_gd","adjustment_h","adjustment_a")])
my_xtable(time_adj[,c("bucket_index","adjustment_h","adjustment_a")])
time_adj
state_adj
