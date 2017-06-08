source("./R_Code/PSO/utils.R")
source("./R_Code/PSO/dimension_pre_processing.R")

library(plyr)
library(ggplot2)

source("./R_Code/PSO/data_loader.R")
load_all()


print_stats = function(df){
  b = binom.test(sum(df$is_team_A_winner),nrow(df),alternative = "g")
  b2 = binom.test(sum(df$is_team_A_winner),nrow(df))
  t = t.test()
  print(sprintf("%g,%f",nrow(df),b$p.value))
  a = c(nrow(df),round(as.numeric(b$p.value),3),round(as.numeric(b2$p.value),3),round(sum(df$is_team_A_winner)/length(df$is_team_A_winner),3))
  return(a)
}

process_dimension = function(df,dim){
  print_stats(df[!is.na(dim),])
  print_stats(df[is.na(dim),])
  c(print_stats(df[!is.na(dim),]),print_stats(df[is.na(dim),]))
}

games_keeper_join = function(games_df,keepers_df){
  df = myjoin(games_df,keepers_df,c1="home_keeper")
  df = myjoin(df,keepers_df,c1="away_keeper")
  df$na_col = df$away_keeper_uri
  df$na_col[is.na(df$home_keeper_uri)]=NA
  return(df)
}
pre_2003 = final_scores[final_scores$year<2003 | (final_scores$year==2003 & final_scores$month<=6),]
post_2003 = final_scores[final_scores$year>2003 | (final_scores$year==2003 & final_scores$month>6),]
results_pre_2003 = data.frame()
results_post_2003 = data.frame()

#odds
df = myjoin(pre_2003,odds)
results_pre_2003 = rbind(results_pre_2003,process_dimension(df,df$odds_h))

df = myjoin(post_2003,odds)
results_post_2003 = rbind(results_post_2003,process_dimension(df,df$odds_h))


#keeper stats
keepers_with_cumulate_stats = read_keepers_with_cumulate_stats()
df = games_keeper_join(pre_2003,keepers_with_cumulate_stats)
results_pre_2003 = rbind(results_pre_2003,process_dimension(df,df$na_col))

df = games_keeper_join(post_2003,keepers_with_cumulate_stats)
results_post_2003 = rbind(results_post_2003,process_dimension(df,df$na_col))


#keeper_penalty_stats
keeper_with_penalty_stats = read_keepers_with_penalty_stats()
df = games_keeper_join(pre_2003,keeper_with_penalty_stats)
results_pre_2003 = rbind(results_pre_2003,process_dimension(df,df$na_col))

df = games_keeper_join(post_2003,keeper_with_penalty_stats)
results_post_2003 = rbind(results_post_2003,process_dimension(df,df$na_col))


write.csv(cbind(results_pre_2003,results_post_2003),row.names = F)

