source("./R_Code/PSO/utils.R")
source("./R_Code/PSO/dimension_pre_processing.R")
library(plyr)

source("./R_Code/PSO/data_loader.R")
load_all()

compare_shootouts = function(df1,df2){
  a = z.prop(sum(df1$is_team_A_winner),sum(df2$is_team_A_winner),nrow(df1),nrow(df2))
  print(sprintf("%g,%f",nrow(df1),myprop(df1$is_team_A_winner)))
  print(sprintf("%g,%f",nrow(df2),myprop(df2$is_team_A_winner)))
  print(as.numeric(a))
}

compare_shots = function(df1,df2){
  a = z.prop(sum(df1$isConverted),sum(df2$isConverted),nrow(df1),nrow(df2))
  print(as.numeric(a))
}
compare_samples = function(df1,df2,na_col1,na_col2){
  df1_covered = df1[!is.na(na_col1),]
  df2_covered = df2[!is.na(na_col2),]
  
  df1_uncovered = df1[is.na(na_col1),]
  df2_uncovered = df2[is.na(na_col2),]
  
  compare_data_sets(df1_covered,df2_covered)
  compare_data_sets(df1_uncovered,df2_uncovered)
}

#For before and after 2003
#all
compare_data_sets(pre_2003,post_2003)

#odds
df_pre_2003 = myjoin(pre_2003,odds)
df_post_2003 = myjoin(post_2003,odds)
df_pre_2003$na_col = df_pre_2003$odds_h
df_post_2003$na_col = df_post_2003$odds_h

compare_samples(df_pre_2003,df_post_2003,df_pre_2003$odds_h,df_post_2003$odds_h)


#keeper stats
keepers_with_cumulate_stats = read_keepers_with_cumulate_stats()
df_pre_2003 = games_keeper_join(pre_2003,keepers_with_cumulate_stats)
df_post_2003 = games_keeper_join(post_2003,keepers_with_cumulate_stats)
compare_samples(df_pre_2003,df_post_2003,df_pre_2003$na_col,df_post_2003$na_col)


#keeper penalty stats
keeper_with_penalty_stats = read_keepers_with_penalty_stats()
df_pre_2003 = games_keeper_join(pre_2003,keeper_with_penalty_stats)
df_post_2003 = games_keeper_join(post_2003,keeper_with_penalty_stats)
compare_samples(df_pre_2003,df_post_2003,df_pre_2003$na_col,df_post_2003$na_col)


#With and without coverage
#odds
df=myjoin(final_scores,odds)
df$na_col = df$odds_h
compare_data_sets(get_covered_rows(df),get_uncovered_rows(df))

#keeper stats
keepers_with_cumulate_stats = read_keepers_with_cumulate_stats()
df = games_keeper_join(final_scores,keepers_with_cumulate_stats)
compare_data_sets(get_covered_rows(df),get_uncovered_rows(df))


#keeper penalty stats
keeper_with_penalty_stats = read_keepers_with_penalty_stats()
df = games_keeper_join(final_scores,keeper_with_penalty_stats)
compare_data_sets(get_covered_rows(df),get_uncovered_rows(df))
