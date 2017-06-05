#Needs aggregated_data
p3_end_events = ddply(aggregated_data,c("game_id"),home.score=sum(home_goals),away.score=sum(away_goals),summarise)
scoring_rates = read.csv("/home/aditya/BetifyData/AdjustmentEstimation/NHL_data/2014/m1_scoring_rates.csv")
x = myjoin(p3_end_events,scoring_rates,c1="game_id",c2="game_id",join_type="")
x$total_scoring_rate = x$game_id_lamdaA + x$game_id_lamdaH
x$total_score = x$home.score + x$away.score

x$val = x$total_scoring_rate
y = x[,c("game_id","val")]
y$type="scoring_rate"
x$type="actual_score"
x$val = x$total_score
y = rbind(y,x[,c("game_id","val","type")])
ggplot(y, aes(val, color=type)) + stat_ecdf()

EPL_scoring_rates = read.csv("/home/aditya/output.csv")
summary(EPL_scoring_rates$lamdaH + EPL_scoring_rates$lamdaA)
epl_probs = read.csv("/home/aditya/BetifyData/AdjustmentEstimation/EPL_data/pre_match_probs.csv")

betexplorer_probs = 

pT = seq(0,1,0.01)
home_supremacy = 0.5
test_scoring_rates = data.frame(pT)
test_scoring_rates$pA = (1-pT) * (1-home_supremacy)
test_scoring_rates$pH = (1-pT)*home_supremacy
to_csv(test_scoring_rates)
#run_model
test_scoring_rates = read.csv("/home/aditya/BetifyData/scoring_rates_test.csv")
test_scoring_rates$pT = 1-test_scoring_rates$pH-test_scoring_rates$pA
test_scoring_rates$total_scoring_rate = test_scoring_rates$lamdaH + test_scoring_rates$lamdaA
ggplot(test_scoring_rates, aes(x=pT,y=total_scoring_rate)) + geom_line() + 
  geom_point(mapping = aes(x=0.23,y=3.306,color = "NHL mean"))+ 
  geom_point(mapping = aes(x=0.26,y=2.66,color = "EPL mean"))


a = 10:20
 b= 1:length(a)
 c=b/length(a)
 d=pnorm(a,mean = mean(a))
 qqplot(d,c)
 
#qqplots
x$home_win = x$home.score > x$away.score
x$away_win = x$home.score < x$away.score
x$tie = x$home.score == x$away.score
x$pT = 1 -x$game_id_pH - x$game_id_pA
qqplot(cumsum(x$game_id_pH),cumsum(x$home_win),xlab = "odds probability",ylab = "home wins",cex=0.01,ylim=c(0,4000))
abline(a=0,b=1)

qqplot(cumsum(x$game_id_pA),cumsum(x$away_win),xlab = "odds probability",ylab = "away wins",cex=0.01,ylim=c(0,4000))
abline(a=0,b=1)
qqplot(cumsum(x$pT),cumsum(x$tie),xlab = "odds probability",ylab = "ties",cex=0.01,ylim=c(0,4000))
abline(a=0,b=1)
