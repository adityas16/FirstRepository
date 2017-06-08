source("./R_Code/PSO/utils.R")
source("./R_Code/PSO/dimension_pre_processing.R")

library(plyr)
library(ggplot2)

nrow(pso)

#scoring probability by team
a=ddply(pso[pso$is_team_A_shot==1,],c("round_adjusted"),prop=myprop(isConverted),n=length(isConverted),summarise)
b=ddply(pso[pso$is_team_A_shot==0,],c("round_adjusted"),prop=myprop(isConverted),n=length(isConverted),summarise)

a=ddply(pso[pso$is_team_A_shot==1,],c("round_adjusted"),prop=myprop(isConverted),n=length(isConverted),summarise)
b=ddply(pso[pso$is_team_A_shot==0,],c("round_adjusted"),prop=myprop(isConverted),n=length(isConverted),summarise)
a_inexp = ddply(pso[pso$is_team_A_shot==1 & pso$shooter_experienced == 0,],c("round_adjusted"),prop=myprop(isConverted),n=length(isConverted),summarise)
b_inexp = ddply(pso[pso$is_team_A_shot==0 & pso$shooter_experienced == 0,],c("round_adjusted"),prop=myprop(isConverted),n=length(isConverted),summarise)
a_exp = ddply(pso[pso$is_team_A_shot==1 & pso$shooter_experienced == 1,],c("round_adjusted"),prop=myprop(isConverted),n=length(isConverted),summarise)
b_exp = ddply(pso[pso$is_team_A_shot==0 & pso$shooter_experienced == 1,],c("round_adjusted"),prop=myprop(isConverted),n=length(isConverted),summarise)

ggplot() + 
  geom_line(data=a, aes(x=round_adjusted, y=prop), color='green') + 
  geom_line(data=a_inexp, aes(x=round_adjusted, y=prop), color='green' , lty=2) +
  geom_line(data=a_exp, aes(x=round_adjusted, y=prop), color='green' , lty=6) +
  geom_line(data=b, aes(x=round_adjusted, y=prop), color='red')+
  geom_line(data=b_inexp, aes(x=round_adjusted, y=prop), color='red', lty=2) + 
  geom_line(data=b_exp, aes(x=round_adjusted, y=prop), color='red' , lty=6) +
  #geom_abline(intercept = c[[1]],slope = c[[2]])+
  scale_x_discrete(breaks=1:6)

c_a_inexp = coef(lm(prop ~ round_adjusted, weights = n,data=a_inexp))
c_a_exp = coef(lm(prop ~ round_adjusted, weights = n,data=a_exp))
c_b_inexp = coef(lm(prop ~ round_adjusted, weights = n,data=b_inexp))
c_b_exp = coef(lm(prop ~ round_adjusted, weights = n,data=b_exp))

ggplot() + 
  geom_line(data=a, aes(x=round_adjusted, y=prop), color='green') + 
  geom_line(data=b, aes(x=round_adjusted, y=prop), color='red')+
  geom_abline(intercept = c_a_inexp[[1]],slope = c_a_inexp[[2]],color='green' , lty=2) +
  geom_abline(intercept = c_a_exp[[1]],slope = c_a_exp[[2]],color='green' , lty=6) +
  geom_abline(intercept = c_b_inexp[[1]],slope = c_b_inexp[[2]],color='red' , lty=2) +
  geom_abline(intercept = c_b_exp[[1]],slope = c_b_exp[[2]],color='red' , lty=6) +
  #geom_line(data=b_exp, aes(x=round_adjusted, y=prop), color='red' , lty=6) +
  #geom_abline(intercept = c[[1]],slope = c[[2]])+
  scale_x_discrete(breaks=1:6)


#Plot win probabilities in different states
df = pso
pso$col = pso$pr_shooter_if_scored - pso$pr_shooter_if_missed
x = ddply(df,c("kickNumber_adjusted","gd"),
          prop = myprop(col),
          n = length(col),
          summarise)

#x[x$kickNumber_adjusted %%2 ==0,]$shooter_gd = x[x$kickNumber_adjusted %%2 ==0,]$shooter_gd +1

ggplot() + aes(x=x$kickNumber_adjusted,y=x$gd)+
  geom_point(aes(size=x$n,color = x$prop))+
  scale_x_continuous(breaks=1:12)+ 
  scale_colour_gradient(limits=c(0, 1))+
  scale_size_area(max_size = 25)



ddply(pso,c("is_team_A_winner","A_takes_extra_shot"),
      prop_a = sum(is_A_goal)/sum(is_team_A_shot),
      prop_b = sum(is_B_goal)/sum(is_team_B_shot),
      n = length(unique(uri)),summarise)

#minus last shot
ddply(pso[pso$is_last_shot == 0,],c("is_team_A_winner","A_takes_extra_shot"),
      prop_a = sum(is_A_goal)/sum(is_team_A_shot),
      prop_b = sum(is_B_goal)/sum(is_team_B_shot),
      n = length(unique(uri)),summarise)

ddply(pso[pso$kickNumber==5,],c("gd_direction"),prop=myprop(is_team_A_winner),n=length(is_team_A_winner),summarise)

#when teams are even after each round, what is the proportion of team A win
ddply(pso[pso$is_team_A_shot==1 & pso$gd==0,],c("round_adjusted"),prop=myprop(is_team_A_winner),n=length(is_team_A_winner),summarise)

#who wins at the end of each round?
ddply(pso[pso$is_team_A_shot==1  & pso$gd!=0,],c("round_adjusted"),prop=myprop(gd>0),n=length(gd),summarise)
ddply(pso,c("kickNumber"),prop=myprop(gd>0),n=length(gd),summarise)

#Team A advantage in games that end in first 10 shots vs those that end in rapid fire
myprop(pso$is_team_A_winner)
myprop(pso[pso$num_kicks<11,]$is_team_A_winner)
myprop(pso[pso$num_kicks>10,]$is_team_A_winner)

#shooting probability by shooter gd
ddply(pso[pso$kickNumber<=10,],c("shooter_gd_sign","is_team_A_shot"),prop=myprop(isConverted),n=length(isConverted),summarise)

#what happens in rapid fire
final_scores$rapid_fire = ifelse(final_scores$kickNumber>10,1,0)
ddply(final_scores,c("rapid_fire"),p=myprop(is_team_A_winner),n=length(is_team_A_winner),summarise)

ddply(pso,c("endIfMiss"),prop=myprop(isConverted),n=length(isConverted),summarise)


#get all transition proportions
ddply(pso[pso$is_team_A_shot==T,],c("round","shooter_gd_sign"),prop=myprop(isConverted),n=length(isConverted),summarise)

#Average pressure by team and round
ddply(pso[pso$is_team_A_shot==T,],c("round"),m=mean(importance),n=length(isConverted),summarise)

#Based on number of missed shot till now
pso$opponent_team_miss = ifelse(pso$is_team_A_shot,pso$round-pso$teamBScore - 1, pso$round-pso$teamAScore)
pso$equal_shots_missed = pso$opponent_team_miss == pso$shooter_team_miss
df=pso[pso$equal_shots_missed == 1,]
#df =df[pso$is_team_A_shot==0,]
x = ddply(df,c("round_adjusted","shooter_team_miss"),prop=myprop(isConverted),n=length(isConverted),summarise)
x$proportion_scored = x$prop;
x[x$prop>0.8,]$proportion_scored = 0.8
x[x$prop<0.66,]$proportion_scored = 0.66
ggplot(data=x) +
  geom_point(aes(x=round_adjusted,y=shooter_team_miss,size=n,color = proportion_scored))+
  scale_x_continuous(breaks=1:6)+ 
  scale_color_gradient2(midpoint=0.734, low="red", mid="white",
                        high="green", space ="Lab" ,limits=c(0.66, 0.8))+
  scale_size_area(limits = c(1,3050),max_size = 25)

#pressure metrics
pso$incentive = pso$pr_shooter_if_scored - pso$pr_shooter_pre_shot
pso$negative_pressure = exp(pso$pr_shooter_pre_shot - pso$pr_shooter_if_missed)
pso$if_scored_minus_if_missed = (pso$pr_shooter_if_scored - pso$pr_shooter_if_missed)
pso$is_rapid_fire = pso$kickNumber > 10
pso$gain = pso$pr_shooter_if_scored - pso$pr_shooter_pre_shot
pso$loose = pso$pr_shooter_pre_shot - pso$pr_shooter_if_missed
pso$gain_ratio = pso$gain/ pso$if_scored_minus_if_missed
pso$aer = pso$if_scored_minus_if_missed / pso$pr_shooter_pre_shot
df = pso
df$round1_3 = df$round_adjusted <4
df$round4_6 = df$round_adjusted >3
#Regression
lm_summary_table(glm(isConverted ~  1,data=df,family=binomial("logit")))
lm_summary_table(glm(isConverted ~  is_team_A_shot,data=df,family=binomial("logit")))
lm_summary_table(glm(isConverted ~  is_team_A_shot + round_adjusted,data=df,family=binomial("logit")))
lm_summary_table(glm(isConverted ~  is_team_A_shot + factor(round_adjusted),data=df,family=binomial("logit")))
lm_summary_table(glm(isConverted ~  is_team_A_shot * factor(round_adjusted),data=df,family=binomial("logit")))
lm_summary_table(glm(isConverted ~  is_team_A_shot * factor(round_adjusted) * factor(shooter_experienced) + endIfScore  ,data=df,family=binomial("logit")))
lm_summary_table(glm(isConverted ~  is_team_A_shot * round4_6 * shooter_experienced   ,data=df,family=binomial("logit")))


lm_summary_table(glm(isConverted ~  is_team_A_shot + factor(round_adjusted),data=df,family=binomial("logit")))
#difference
lm_summary_table(glm(isConverted ~  is_team_A_shot + factor(round_adjusted) + if_scored_minus_if_missed,data=df,family=binomial("logit")))
lm_summary_table(glm(isConverted ~  is_team_A_shot + factor(round_adjusted) + gain_ratio,data=df,family=binomial("logit")))
lm_summary_table(glm(isConverted ~  is_team_A_shot + factor(round_adjusted) + gain + loose,data=df,family=binomial("logit")))
lm_summary_table(glm(isConverted ~  is_team_A_shot + factor(round_adjusted) + pr_shooter_if_scored + pr_shooter_if_missed,data=df,family=binomial("logit")))
lm_summary_table(glm(isConverted ~  is_team_A_shot + endIfMiss,data=df,family=binomial("logit")))


lm_summary_table(glm(isConverted ~  round_adjusted *  shooter_experienced * is_team_A_shot,data=df,family=binomial("logit")))
lm_summary_table(glm(isConverted ~  is_team_A_shot * kickNumber + shooter_experienced*kickNumber ,data=df,family=binomial("logit")))
lm_summary_table(glm(isConverted ~  is_team_A_shot * kickNumber + shooter_experienced ,data=df,family=binomial("logit")))

lm_summary_table(glm(isConverted ~  is_team_A_shot,family=binomial("logit")))

lm_summary_table(glm(isConverted ~  is_team_A_shot * shooter_experienced * round4_6,data=df,family=binomial("logit")))
