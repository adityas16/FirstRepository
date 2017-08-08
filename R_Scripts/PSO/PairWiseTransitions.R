#Nils format
#For AER competitions only
pso = myjoin(pso,read_aer_games(),join_type="")

x=ddply(pso,c("uri","round"),gd = head(gd,1),n_a_goals = sum(is_A_goal),n_b_goals = sum(is_B_goal),round_adjusted = head(round_adjusted,1),summarise)
x$round_transition = x$n_a_goals - x$n_b_goals
n=x
n=sqldf("select * from n,final_scores as f where n.uri = f.uri")
k = ddply(n,c("round_adjusted","gd","round_transition"),
          tr_n=length(uri),n_team_a_win_POST_transition = sum(is_team_A_winner),summarise)
l = ddply(n,c("round_adjusted","gd"),
          n=length(uri),n_team_a_win_PRE_transition = sum(is_team_A_winner),summarise)
k = sqldf("select k.*,l.n,l.n_team_a_win_PRE_transition from k,l where k.round_adjusted = l.round_adjusted and k.gd = l.gd")

format = read.csv("/home/aditya/Dropbox/penalties/Analysis/By Round/PairWiseTransitions/format.csv")
k=sqldf("select k.* from k,format as f where k.round_adjusted = f.round_adjusted and k.gd = f.gd and k.round_transition = f.round_transition")
k$tr_probability = k$tr_n/k$n
k=k[,c(1,2,3,8,6,4,7,5)]
colnames(k)[1] = "j"
colnames(k)[2] = "s"
colnames(k)[3] = "c"
to_csv(k[order(k$j,k$s * -1,k$c * -1),])
  