load_nhl()
ddply(final_scores,c("season"),n_games = length(uri),summarise)

myprop(final_scores$is_team_A_winner)
sum(final_scores$is_team_A_winner)
binom.test(sum(final_scores$is_team_A_winner),length(final_scores$is_team_A_winner))

hsf = final_scores[final_scores$homeShotFirst ==F,]
binom.test(sum(hsf$is_team_A_winner),length(hsf$is_team_A_winner))
binom.test(sum(1-hsf$is_team_A_winner),length(1-hsf$is_team_A_winner))
df = final_scores
summary(glm(is_team_A_winner ~ homeShotFirst  ,data = df,family=binomial("logit")))

final_scores$rand_team = sample(2,nrow(final_scores),replace = T)>1
final_scores$is_rand_team_winner = final_scores$is_home_winner * final_scores$rand_team | (!final_scores$is_home_winner & !final_scores$rand_team)
final_scores$is_home_team = final_scores$rand_team
final_scores$is_team_1 = final_scores$homeShotFirst * final_scores$rand_team | (!final_scores$homeShotFirst & !final_scores$rand_team)

summary(glm(is_rand_team_winner ~ is_home_team + is_team_1 ,data = final_scores,family=binomial("logit")))
myprop(final_scores$is_rand_team_winner)
myprop(final_scores$is_home_team)
myprop(final_scores$is_team_1)

myprop(pso$isConverted)
pso$round_adjusted = pso$round
pso$round_adjusted[pso$round > 3] =4
pso$kickNumber_adjusted = pso$kickNumber
pso$kickNumber_adjusted[pso$kickNumber>6 & pso$kickNumber %%2 ==1] = 7
pso$kickNumber_adjusted[pso$kickNumber>6 & pso$kickNumber %%2 ==0] = 8

ddply(pso,c("round_adjusted"),n_shots = length(uri),prop_scored = myprop(isConverted),summarise)

ddply(pso,c("shooter_gd_sign"),n_shots = length(uri),prop_scored = myprop(isConverted),summarise)


df =pso
x = ddply(df,c("kickNumber_adjusted","gd"),proportion_scored=myprop(isConverted),n=length(isConverted),summarise)
x[x$prop>0.37,]$proportion_scored = 0.37
x[x$prop<0.29,]$proportion_scored = 0.29
ggplot(data=x) +
  geom_point(aes(x=kickNumber_adjusted,y=gd,size=n,color = proportion_scored))+
  scale_x_continuous(breaks=1:8)+ 
  scale_color_gradient2(midpoint=0.323, low="red", mid="white",
                        high="green", space ="Lab" ,limits=c(0.37, 0.29))+
  scale_size_area(limits = c(1,2000),max_size = 25)

#Shooting experience and round
df = pso
#df=pso[pso$homeShotFirst==1,]
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
  scale_y_continuous(limits = c(0.15,0.40))+
  scale_size_area(limits = c(1,1525),max_size = 20)



  