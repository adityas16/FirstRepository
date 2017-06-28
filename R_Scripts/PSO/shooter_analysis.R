library(wesanderson)

in_play_pen = read_in_play_penalties()
pso = read_pso()

#histogram of number of converted in-play penalties (0,1,2,3+)
by_scorer = ddply(pso,c("shooter"),n_in_play_penalties_scored = shooter_n_in_play_penalties_scored,summarise)
by_scorer$n_in_play_penalties_scored = ifelse(by_scorer$n_in_play_penalties_scored > 2,2,by_scorer$n_in_play_penalties_scored)
ggplot() + aes(by_scorer$n_in_play_penalties_scored)+ geom_histogram(binwidth = 1, colour="black", fill="white")

#Histogram of age-wise scoring proportion
pso = myjoin(pso,shooters,c1="shooter",join_type="")
pso$shooter_age = pso$year - pso$shooter_birth_year
my_hist(pso$shooter_age,bin_width = 1)
by_shooter_age = ddply(pso,c("shooter_age"),conversion_ratio = myprop(isConverted),n=length(isConverted),summarise)
by_shooter_age = by_shooter_age[by_shooter_age$n>200,]
plot(by_shooter_age$shooter_age,by_shooter_age$conversion_ratio)

c = coef(lm(conversion_ratio ~ shooter_age, weights = n,data=by_shooter_age))
c
ggplot() + aes(x=by_shooter_age$shooter_age,y=by_shooter_age$conversion_ratio) + geom_point(aes(size=by_shooter_age$n)) +
geom_abline(intercept = c[["(Intercept)"]],slope = c[["shooter_age"]])

ggplot() + aes(x=by_shooter_age$shooter_age,y=by_shooter_age$conversion_ratio) + geom_point(aes(size=by_shooter_age$n)) +
geom_smooth(method = lm,se=FALSE)

lm_summary_table(glm(isConverted ~  shooter_age + factor(shooter_n_in_play_penalties_scored),data = pso,family=binomial("logit")))

#3*3 table of 3 positions * [0,1,2,3+] penalty goals
pso$shooter_n_in_play_penalties_scored = ifelse(pso$shooter_n_in_play_penalties_scored > 2,2,pso$shooter_n_in_play_penalties_scored)
ddply(pso,c("shooter_n_in_play_penalties_scored"),
      defender = myprop(isConverted[shooter_position=="defender"]),
      midfielder = myprop(isConverted[shooter_position=="midfielder"]),
      forward = myprop(isConverted[shooter_position=="forward"]),
      summarise)

ddply(pso,c("shooter_n_in_play_penalties_scored"),
      defender = length(isConverted[shooter_position=="defender"]),
      midfielder = length(isConverted[shooter_position=="midfielder"]),
      forward = length(isConverted[shooter_position=="forward"]),
      summarise)

#Regression of scoring probability based on position and 
df = pso[pso$shooter_position=="defender",]
df = pso
lm_summary_table(glm(df$isConverted ~  factor(shooter_n_in_play_penalties_scored) * shooter_position,data = df  ,family=binomial("logit")))
lm_summary_table(glm(df$isConverted ~  df$shooter_position + factor(df$shooter_n_in_play_penalties_scored),family=binomial("logit")))

#3*3 table of 3 positions * [leading,neutral,lagging]
pso$situation = sign(pso$pr_shooter_pre_shot - 0.5)
ddply(pso,c("situation"),
      defender = myprop(isConverted[shooter_position=="defender"]),
      midfielder = myprop(isConverted[shooter_position=="midfielder"]),
      forward = myprop(isConverted[shooter_position=="forward"]),summarise)


#endifscore, endif miss
to_csv(ddply(pso,c("endIfMiss"),
      defender = myprop(isConverted[shooter_position=="defender"]),
      midfielder = myprop(isConverted[shooter_position=="midfielder"]),
      forward = myprop(isConverted[shooter_position=="forward"]),
      summarise))

to_csv(ddply(pso,c("endIfMiss"),
      defender = length(isConverted[shooter_position=="defender"]),
      midfielder = length(isConverted[shooter_position=="midfielder"]),
      forward = length(isConverted[shooter_position=="forward"]),
      summarise))

#shooting order
x=ddply(pso,c("round"),
      defender = sum(shooter_position=="defender")/sum(pso$shooter_position=="defender"),
      midfielder = sum(shooter_position=="midfielder")/sum(pso$shooter_position=="midfielder"),
      forward = sum(shooter_position=="forward")/sum(pso$shooter_position=="forward"),
      summarise)
x
to_csv(x)

x=ddply(pso,c("round"),
      exp_0 = sum(shooter_n_in_play_penalties_scored==0)/sum(pso$shooter_n_in_play_penalties_scored==0),
      exp_1 = sum(shooter_n_in_play_penalties_scored==1)/sum(pso$shooter_n_in_play_penalties_scored==1),
      exp_2 = sum(shooter_n_in_play_penalties_scored==2)/sum(pso$shooter_n_in_play_penalties_scored==2),
      summarise)
x
to_csv(x)
pso$shooter_

x=ddply(pso[pso$is_team_A_shot == F,],c("round_adjusted","shooter_n_in_play_penalties_scored"),
      defender_goals = sum(isConverted[shooter_position=="defender"]),
      defender_shots = length(isConverted[shooter_position=="defender"]),
      midfielder_goals = sum(isConverted[shooter_position=="midfielder"]),
      midfielder_shots = length(isConverted[shooter_position=="midfielder"]),
      forward_goals = sum(isConverted[shooter_position=="forward"]),
      forward_shots = length(isConverted[shooter_position=="forward"]),
      summarise)


#Visualizing transition probabilities
df = pso[pso$shooter_experienced==0,]
#df = pso
x = ddply(df,c("kickNumber_adjusted","gd"),
          prop = myprop(isConverted),
          n = length(isConverted),
          summarise)

#x[x$kickNumber_adjusted %%2 ==0,]$shooter_gd = x[x$kickNumber_adjusted %%2 ==0,]$shooter_gd +1
x$proportion_scored = x$prop
x[x$prop>0.8,]$proportion_scored = 0.8
x[x$prop<0.66,]$proportion_scored = 0.66
ggplot(data=x) +
  geom_point(aes(x=kickNumber_adjusted,y=gd,size=n,color = proportion_scored))+
  scale_x_continuous(breaks=1:12)+ 
  scale_color_gradient2(midpoint=0.734, low="red", mid="white",
                         high="green", space ="Lab" ,limits=c(0.66, 0.8))+
  scale_size_area(limits = c(1,1525),max_size = 25)
  
#Pressure metrics visualization
#pso$pr_shooter_pre_shot
#pso$pr_shooter_if_scored
#pso$pr_shooter_if_missed
#df$pr_shooter_score_miss_diff
df = pso
df$pr_shooter_score_miss_diff = pso$pr_shooter_if_scored - pso$pr_shooter_if_missed
x = ddply(df,c("kickNumber_adjusted","gd"),
          pr_shooter_if_missed = myprop(pr_shooter_if_missed),
          n = length(isConverted),
          summarise)
ggplot(data=x) +
  geom_point(aes(x=kickNumber_adjusted,y=gd,size=n,color = pr_shooter_if_missed))+
  scale_x_continuous(breaks=1:12)+ 
  scale_color_gradient2(midpoint=0.5, low="red", mid="white",
                        high="green", space ="Lab")+
  scale_size_area(limits = c(1,1525),max_size = 25)

#Scroring probability w.r.t PrPreShot
#df = pso[pso$shooter_experienced==1,]
df = pso
df$col = df$pr_shooter_if_scored - df$pr_shooter_if_missed
x = ddply(df,c("col"),
          prop = myprop(isConverted),
          n = length(isConverted),
          summarise)

c = coef(lm(prop ~ col, weights = n,data=x))
c
ggplot() + aes(x=x$col,y=x$prop)+
  geom_point(aes(size=x$n))+
  geom_abline(intercept = c[[1]],slope = c[[2]])+
  scale_y_continuous(limits = c(0.6,0.95))+ 
  scale_size_area(max_size = 25)


#Shooting experience and round
df = pso
#df=pso[pso$shooter_experienced==0,]
x=ddply(df,c("round_adjusted","is_team_A_shot"),
        prop = myprop(isConverted),
        n = length(isConverted),
        summarise)
x$prop_sd = (x$prop * (1-x$prop) / x$n)^0.5
xa = data=x[x$is_team_A_shot==1,]
xb = data=x[x$is_team_A_shot==0,]
ggplot() +
  geom_ribbon(data=xa, aes(x=round_adjusted ,ymax=prop + prop_sd,ymin=prop - prop_sd),fill="dodgerblue2",alpha=0.3)+
  geom_line(data=xa,aes(x=round_adjusted,y=prop),color=" dodgerblue2")+
  geom_ribbon(data=xb, aes(x=round_adjusted ,ymax=prop + prop_sd,ymin=prop - prop_sd),fill="sienna1",alpha=0.4)+
  geom_line(data=xb,aes(x=round_adjusted,y=prop),color="sienna1")+ 
  scale_fill_manual(name = "Teams",
                    values = c('dodgerblue2','sienna1'),
                    labels = c('team A','team B'))+
  scale_x_continuous(breaks=1:6)+
  scale_y_continuous(limits = c(0.60,0.85))+
  scale_size_area(limits = c(1,1525),max_size = 20)

#proportion tests on effect of shooter exp and starting
df = pso
df=pso[pso$shooter_experienced==1,]
x=ddply(df,c("round_adjusted"),
        prop_a = myprop(isConverted[is_team_A_shot==1]),
        n_a = length(isConverted[is_team_A_shot==1]),
        prop_b = myprop(isConverted[is_team_A_shot==0]),
        n_b = length(isConverted[is_team_A_shot==0]),
        summarise)
x$p_value = mapply(z.prop1,x$prop_a,x$prop_b,x$n_a,x$n_b)
x$p

df=pso[pso$is_team_A_shot==0,]
x=ddply(df,c("round_adjusted"),
        prop_exp = myprop(isConverted[shooter_experienced==1]),
        n_exp = length(isConverted[shooter_experienced==1]),
        prop_inexp = myprop(isConverted[shooter_experienced==0]),
        n_inexp = length(isConverted[shooter_experienced==0]),
        summarise)
x$p_value = mapply(z.prop1,x$prop_exp,x$prop_inexp,x$n_exp,x$n_inexp)

#Are experienced shooters really better?
x=ddply(pso,c("round_adjusted"),exp_shooters_prop = myprop(isConverted[shooter_experienced==1]),n_exp=length(isConverted[shooter_experienced==1])
        ,inexp_shooters_prop = myprop(isConverted[shooter_experienced==0]),n_inexp=length(isConverted[shooter_experienced==0]),summarize)
x$ratio = x$exp_shooters_prop / x$inexp_shooters_prop
x
#Shooting sequence by team
x = ddply(pso,c("round_adjusted"),proportion_exp_shooters_A = myprop(shooter_experienced[is_team_A_shot==1]),proportion_exp_shooters_B = myprop(shooter_experienced[is_team_B_shot==1]),summarize)
x$exp_shooter_ratio = x$proportion_exp_shooters_A / x$proportion_exp_shooters_B
x
