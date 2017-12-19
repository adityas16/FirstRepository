#Nils format
format = read.csv(paste0(DROPBOX_FOLDER , "/penalties/Analysis/By Round/PairWiseTransitions/format.csv"))

create_pairwise_transitions_table = function(shootouts){
  #URI is NOT unique always, because of the bootstrapping case
  x=ddply(shootouts,c("unique_game_id","round"),gd = head(gd,1),n_a_goals = sum(is_A_goal),n_b_goals = sum(is_B_goal),round_adjusted = head(round_adjusted,1),uri=head(uri,1),summarise)
  x$round_transition = x$n_a_goals - x$n_b_goals
  n=x
  n=sqldf("select * from n,final_scores as f where n.uri = f.uri")
  k = ddply(n,c("round_adjusted","gd","round_transition"),
            tr_n=length(uri),n_team_a_win_POST_transition = sum(is_team_A_winner),summarise)
  l = ddply(n,c("round_adjusted","gd"),
            n=length(uri),n_team_a_win_PRE_transition = sum(is_team_A_winner),summarise)
  k = sqldf("select k.*,l.n,l.n_team_a_win_PRE_transition from k,l where k.round_adjusted = l.round_adjusted and k.gd = l.gd")
  
  k=sqldf("select k.* from k,format as f where k.round_adjusted = f.round_adjusted and k.gd = f.gd and k.round_transition = f.round_transition")
  k$tr_probability = k$tr_n/k$n
  k=k[,c(1,2,3,8,6,4,7,5)]
  
  
  #Appending kick level data
  q=ddply(shootouts,c("round_adjusted","gd","is_team_A_shot"),p=myprop(isConverted),n=length(uri),summarise)
  
  k=sqldf("select k.*,q.p as q_s_1 from k,q where 
      k.round_adjusted=q.round_adjusted 
        and k.gd = q.gd
        and is_team_A_shot = 1")    
  
  k=sqldf("select k.*,q.p as q_s_2 from k left join q on 
        k.round_adjusted=q.round_adjusted 
        and k.gd = q.gd
        and is_team_A_shot = 0")    
  
  k=sqldf("select k.*,q.p as q_splus1_2 from k left join q on 
        k.round_adjusted=q.round_adjusted 
        and k.gd + 1 = q.gd
        and is_team_A_shot = 0")    
  
  k=sqldf("select k.*,q.n as n_s_2 from k left join q on 
        k.round_adjusted=q.round_adjusted 
        and k.gd = q.gd
        and is_team_A_shot = 0")    
  
  k=sqldf("select k.*,q.n as n_splus1_2 from k left join q on  
        k.round_adjusted=q.round_adjusted 
        and k.gd + 1= q.gd
        and is_team_A_shot = 0")    
  
  
  colnames(k)[1] = "j"
  colnames(k)[2] = "s"
  colnames(k)[3] = "c"
  return(k)
}


#For AER competitions only
load_all()
pso = myjoin(pso,read_aer_games(),join_type="")

#pso = myjoin(pso,read_games(),join_type="")

#For senior men competitions only
load_all()
pso = myjoin(pso,read_senior_men_games(),join_type="")

shootouts=pso
shootouts$unique_game_id=shootouts$uri
k=create_pairwise_transition_table(shootouts)
to_csv(k[order(k$j,k$s * -1,k$c * -1),])





#Bootstrapping
filtered_games = data.frame(pso[pso$is_last_shot==1,c("uri")])
colnames(filtered_games)=c("uri")

k=data.frame()
for(i in 1:10){
itertation_games = data.frame(filtered_games[sample(1:nrow(filtered_games),replace = T),])
colnames(itertation_games)=c("uri")
itertation_games$unique_game_id = 1:nrow(itertation_games)

iteration_shootouts = myjoin(pso,itertation_games,join_type="")
#iteration_k=create_pairwise_transitions_table(iteration_shootouts)
iteration_k=binomial_transitions_table(iteration_shootouts)
iteration_k$iteration_number = i
k=rbind(k,iteration_k)
}
k=k[,c(length(k),1:length(k-1))]
k$iteration_number.1=NULL
to_csv(k[order(k$iteration_number,k$j,k$s * -1,k$c * -1),])


#Testing bootstrapping code
x=read.csv("/home/aditya/Dropbox/penalties/Analysis/By Round/PairWiseTransitions/PairwiseTransitions_Bootstrap_AER_20171127.csv")
y=ddply(x,c("j","s","c"),tr_pr=mean(tr_probability),qs_1=mean(q_s_1),n=mean(n),summarize)
to_csv(y[order(y$j,y$s * -1,y$c * -1),])
