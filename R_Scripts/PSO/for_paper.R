library(sqldf)
library(plyr)
library(ggplot2)

n=540
true_estimate = 0.545
ms_sample_size = 540
our_sample_size = 1800

sample_sizes = seq(100,2000,50)
power = rep(0,length(sample_sizes))
i=0
for(i in 1:length(sample_sizes)){
  power[i] =  calculate_power(sample_sizes[i],true_estimate)   
}
df = data.frame(sample_sizes)
df = cbind(df,power)


ggplot(data=df, aes(x=sample_sizes, y=power, group=1)) + 
  geom_line(colour="red", size=1.5) + 
  geom_point(colour="red", size=4, shape=21, fill="white") +
  geom_point(colour="black", size=4, shape=21,x=ms_sample_size,y=calculate_power(ms_sample_size,true_estimate)) +
  geom_point(colour="black", size=4, shape=21,x=our_sample_size,y=calculate_power(our_sample_size,true_estimate))

calculate_power = function(n,true_estimate,test_estimate=0.5){
  for(i in floor(n/2):n){
    if(binom.test(i,n,test_estimate)$p.value<=0.05){
      required = i
      break
    }
  }
  return(pbinom(required,n,true_estimate,lower.tail = F))
}


#standard errors
se = rep(0,length(sample_sizes))
i=0
for(i in 1:length(sample_sizes)){
  se[i] =  (0.25/sample_sizes[i])^0.5
}
df = data.frame(sample_sizes)
df = cbind(df,se)

ggplot(data=df, aes(x=sample_sizes, y=se, group=1)) + 
  geom_line(colour="red", size=1.5) + 
  geom_point(colour="red", size=4, shape=21, fill="white") +
  geom_point(colour="black", size=4, shape=21,x=ms_sample_size,y=(0.25/ms_sample_size)^0.5) +
  geom_point(colour="black", size=4, shape=21,x=our_sample_size,y=(0.25/our_sample_size)^0.5)

df = superset

p_obs = 0.727
p_true = 0.71
p_null = 0.74
n=1000*5
(p_obs - p_null)/(p_null*(1-p_null)/n)^0.5

z_required = (p_obs- p_true) / (p_true*(1-p_true)/n)^0.5

pnorm(z_required,lower.tail =  T)

df$is_ko = df$dist_from_final <=2
ddply(df,c("is_ko"),prop=myprop(is_team_A_winner),n=length(is_ko),summarise)

