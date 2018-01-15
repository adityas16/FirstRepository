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
  k=k[order(k$j,k$s * -1,k$c * -1),]
  return(k)
}


#Different approach, counting edges instead of nodes, and one line per round,gd
#append previous shot result

count_edges=function(shootouts,team_A_shot,
                     converted,
                     prev_converted){
  return(ddply(shootouts[shootouts$is_team_A_shot==team_A_shot & shootouts$isConverted==converted & shootouts$is_prev_converted == prev_converted,],
               c("round_adjusted","gd"),n=length(uri),summarise))
}

append_count = function(x,y,colname = "n1a"){
  x=sqldf("select x.*,y.n from x left join y on x.round_adjusted = y.round_adjusted and x.gd=y.gd")
  x$n[is.na(x$n)]=0
  colnames(x)[length(colnames(x))] = colname
  return (x)
}

create_binom_trinom_table = function(shootouts){
  x=ddply(shootouts[shootouts$is_team_A_shot==1,],c("round_adjusted","gd"),n0=length(uri),n_team_A_wins=sum(is_team_A_winner),summarise)
  x=x[order(x$round_adjusted,x$gd *-1),]
  
  x=append_count(x,count_edges(shootouts,1,1,0),"n1a")
  x=append_count(x,count_edges(shootouts,1,1,1),"n1b")
  x$n1=x$n1a+x$n1b
  x$n1a=NULL
  x$n1b=NULL
  
  x=append_count(x,count_edges(shootouts,1,0,0),"n2a")
  x=append_count(x,count_edges(shootouts,1,0,1),"n2b")
  x$n2=x$n2a+x$n2b
  x$n2a=NULL
  x$n2b=NULL
  
  
  x=append_count(x,count_edges(shootouts,0,0,0),"n6")
  x=append_count(x,count_edges(shootouts,0,1,0),"n5")
  
  x=append_count(x,count_edges(shootouts,0,0,1),"n12")
  x=append_count(x,count_edges(shootouts,0,1,1),"n11")
  
  y=count_edges(shootouts,0,0,1)
  y$gd = y$gd -1
  x=append_count(x,y,"n4")
  y=count_edges(shootouts,0,1,1)
  y$gd = y$gd -1
  x=append_count(x,y,"n3")
  
  y=count_edges(shootouts,0,0,0)
  y$gd = y$gd -1
  x=append_count(x,y,"n10")
  y=count_edges(shootouts,0,1,0)
  y$gd = y$gd -1
  x=append_count(x,y,"n9")
  
  #tests
  x$n1+x$n2 - x$n0
  x$n4+x$n3 - x$n1
  x$n5+x$n6 - x$n2
  
  x$n_tri_up = x$n4
  x$n_tri_0 = x$n3+x$n6
  x$n_tri_0[x$n3==0 | x$n6==0]=0
  x$n_tri_down = x$n5
  x$has_trinomial=(x$n_tri_up !=0 & x$n_tri_down !=0 & x$n_tri_0 !=0)
  tri_total = x$n_tri_up + x$n_tri_down + x$n_tri_0
  x$tri_up = x$n_tri_up / tri_total
  x$tri_0 = x$n_tri_0 / tri_total
  x$tri_down = x$n_tri_down / tri_total
  
  
  x$p_s = x$n1/x$n0
  x$q_s = x$n5/(x$n5 + x$n6)
  x$q_splus1 = x$n3/(x$n3 + x$n4)
  
  x$q_prime_s = (x$n11 + x$n5)/(x$n5 + x$n6 + x$n11 + x$n12)
  x$q_prime_splus1 = (x$n3 + x$n9)/(x$n3 + x$n4 + x$n9 + x$n10)
  return(x)
}

run_bootstrap = function(num_of_iterations,table_generator,page_size=num_of_iterations+1){
  filtered_games = data.frame(pso[pso$is_last_shot==1,c("uri")])
  colnames(filtered_games)=c("uri")
  
  k=data.frame()
  set.seed(42);
  iteration_seeds = sample(1000000,size=100000)
  entries_on_page = 0
  for(i in 1:num_of_iterations){
    set.seed(iteration_seeds[i])
    itertation_games = data.frame(filtered_games[sample(1:nrow(filtered_games),replace = T),])
    colnames(itertation_games)=c("uri")
    itertation_games$unique_game_id = 1:nrow(itertation_games)
    
    iteration_shootouts = myjoin(pso,itertation_games,join_type="")
    iteration_k=table_generator(iteration_shootouts)
    #iteration_k=create_pairwise_transitions_table(iteration_shootouts)
    #iteration_k=create_binomial_transitions_table(iteration_shootouts)
    iteration_k$iteration_number = i
    iteration_k=iteration_k[,c(length(iteration_k),1:length(iteration_k-1))]
    iteration_k$iteration_number.1=NULL
    k=rbind(k,iteration_k)
    
    #Append to temp at the end of each page
    entries_on_page=entries_on_page+1
    if(entries_on_page==page_size){
      #Create file for first page, but append for all others
      if(i==entries_on_page){
      print("Creating new file")  
      to_csv(k)
      }
      else{
        append_to_csv(k)
      }
      k=data.frame()
      entries_on_page = 0
    }
    print(i)
    print(entries_on_page)
  }
  return(k)
}

num_iterations=10
#Setup the shootouts to consider
#For AER competitions only
load_all()
pso = myjoin(pso,read_aer_games(),join_type="")
a=run_bootstrap(num_iterations,create_binom_trinom_table)
to_csv(a,filename = "AER")

#For senior men competitions only
load_all()
pso = myjoin(pso,read_senior_men_games(),join_type="")
shootouts=pso
shootouts$unique_game_id=shootouts$uri
k=create_binom_trinom_table(shootouts)
to_csv(k[order(k$j,k$s * -1,k$c * -1),])

b=run_bootstrap(num_iterations,create_binom_trinom_table)
to_csv(b,"senior_male")








#Testing bootstrapping code
x=read.csv("/home/aditya/Dropbox/penalties/Analysis/By Round/PairWiseTransitions/PairwiseTransitions_Bootstrap_AER_20171127.csv")
y=ddply(x,c("j","s","c"),tr_pr=mean(tr_probability),qs_1=mean(q_s_1),n=mean(n),summarize)
to_csv(y[order(y$j,y$s * -1,y$c * -1),])


