#Score proportions by round, histogram
round_wise_summary = ddply(pso, c("round_adjusted","is_team_A_shot"), summarise,score_proportion = sum(isConverted)/length(isConverted))

ggplot(round_wise_summary, aes(x=round_wise_summary$round_adjusted,y=round_wise_summary$score_proportion,fill=factor(1-round_wise_summary$is_team_A_shot))) +
  geom_bar(position="dodge", stat="identity") +
  geom_text(aes(label=round(round_wise_summary$score_proportion,2),hjust=round_wise_summary$is_team_A_shot,vjust=0),size=4)+
  ggtitle("Score Proportions by round") +
  labs(x="Round",y="Score Proportion") +
  guides(fill=guide_legend(title="Team"))+
  ylim(0,1)

round_wise_summary = ddply(pso, c("round_adjusted"), summarise,
                           scored_a = sum(isConverted[is_team_A_shot==1]),n_a = length(isConverted[is_team_A_shot==1]),
                           scored_b = sum(isConverted[is_team_B_shot==1]),n_b = length(isConverted[is_team_B_shot==1]))
x = round_wise_summary
round_wise_summary$p_value = mapply(z.2sample,x$scored_a,x$scored_b,x$n_a,x$n_b)
round_wise_summary$pr_a = round_wise_summary$scored_a / round_wise_summary$n_a
round_wise_summary$pr_b = round_wise_summary$scored_b / round_wise_summary$n_b

my_xtable(round_wise_summary[,c("round_adjusted","pr_a","pr_b","n_a","n_b","p_value")],4)

#Score proportions by round, line graph
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
  #geom_ribbon(data=xa, aes(x=round_adjusted ,ymax=prop + prop_sd,ymin=prop - prop_sd),fill="dodgerblue2",alpha=0.3)+
  geom_line(data=xa,aes(x=round_adjusted,y=prop),color=" dodgerblue2")+
  #geom_ribbon(data=xb, aes(x=round_adjusted ,ymax=prop + prop_sd,ymin=prop - prop_sd),fill="sienna1",alpha=0.4)+
  geom_line(data=xb,aes(x=round_adjusted,y=prop),color="sienna1")+ 
  scale_fill_manual(name = "Teams",
                    values = c('dodgerblue2','sienna1'),
                    labels = c('team A','team B'))+
  scale_x_continuous(breaks=1:6)+
  scale_y_continuous(limits = c(0.60,0.85))+
  scale_size_area(limits = c(1,1525),max_size = 20)

#By round and gd
x=ddply(pso[pso$kickNumber%%2 == 1,],c("round_adjusted","gd"),n = length(uri),summarise)
x=ddply(pso,c("uri","round"),gd = head(gd,1),n_a_goals = sum(is_A_goal),n_b_goals = sum(is_B_goal),round_adjusted = head(round_adjusted,1),summarise)
x$round_transition = x$n_a_goals - x$n_b_goals
r = ddply(x,c("round_adjusted","gd"),up = myprop(round_transition == 1),down = myprop(round_transition == -1),
          neutral = myprop(round_transition == 0), n = length(uri),summarise)
r$up_to_down_ratio = r$up/r$down
#biased data: (round,gd) 4 2, 4 -2, 5 1, 5 -1
r$imp = r$up + r$down
pr_avg_scoring = mean(pso$isConverted)
pr_avg_scoring*pr_avg_scoring + (1-pr_avg_scoring)*(1-pr_avg_scoring)
2 * pr_avg_scoring * (1-pr_avg_scoring)
my_xtable(r,4)

r_b = r
r_b$temp = r_b$up
r_b$up = r_b$down
r_b$down = r_b$temp
r_b$gd = r$gd * -1

colnames(r_b) <- paste("b", colnames(r_b), sep = "_")

x=sqldf("select * from r,r_b where r.gd = r_b.b_gd and r.round_adjusted = r_b.b_round_adjusted")
x$b_gd = NULL
x$b_round_adjusted = NULL
x$b_temp = NULL
x$b_n = NULL

#By round
#r = r[-c(10,14,15,17),]
r$n_up = r$n * r$up
r$n_down = r$n * r$down
round_wise = ddply(r,c("round_adjusted"),up = sum(n_up)/sum(n),down = sum(n_down)/sum(n),
                   neutral = 1-((sum(n_up) +sum(n_down))/sum(n)), n = sum(n),summarise)
round_wise$up_to_down_ratio = round_wise$up/round_wise$down
round_wise


#correlation between a and b scoring within a round
pso$previous_converted = c(0,head(pso$isConverted,-1))
ddply(pso[pso$is_team_B_shot==1,],c("round_adjusted","previous_converted"),pr = myprop(isConverted), n = length(uri),summarise)

#conditional prob of B scoring if A has scored
ddply(x,c("round_adjusted","gd","n_a_goals"),pr_b = myprop(n_b_goals), n = length(uri),summarise)

r=ddply(x,c("round_adjusted","gd"),pr_a = myprop(n_a_goals),pr_b = myprop(n_b_goals), n = length(uri),summarise)
r = r[-c(10,14,15,17),]
r$ratio = r$pr_a / r$pr_b
plot(r$pr_a,r$pr_b)
cor(r$pr_a,r$pr_b)

#Competition wise p values
x = ddply(final_scores,c("competition"),A_win_prop = sum(is_team_A_winner)/length(competition),n = length(competition),p=binom.test(sum(is_team_A_winner),length(competition))$p.value,summarize)
x=x[order(x$n*-1),] 
my_xtable(x)

binom.test(sum(final_scores$is_team_A_winner),nrow(final_scores))
