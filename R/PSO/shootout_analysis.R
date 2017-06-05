source("./R_Code/PSO/utils.R")
source("./R_Code/PSO/dimension_pre_processing.R")

library(plyr)
library(ggplot2)

source("./R_Code/PSO/data_loader.R")
load_all()
pre_2003 = read_pre_2003_games()
post_2003 = read_post_2003_games()


lm_summary_table = function(g){
  for(i in 1:length(g$coefficients)){
    print(as.numeric(g$coefficients[i]))
    print(sprintf("%g,%g",round(coef(summary(g))[i,2],3),round(coef(summary(g))[i,4],3)))
  }
  print(nobs(g))
  print(summary(g))
}

#first mover
df=final_scores
g=glm(df$is_team_A_winner ~  1,family=binomial("logit"))
lm_summary_table(g)
#pre_2003
df = pre_2003
g=glm(df$is_team_A_winner ~  1,family=binomial("logit"))
lm_summary_table(g)
#post_2003
df = post_2003
g=glm(df$is_team_A_winner ~  1,family=binomial("logit"))
lm_summary_table(g)


#odds
df = myjoin(final_scores,odds)
df$na_col = df$odds_h

#all,no odds
df_no_has = get_uncovered_rows(df)
g=glm(df_no_has$is_team_A_winner ~  1,family=binomial("logit"))
lm_summary_table(g)

#all, has odds
df_has = get_covered_rows(df)
#base
g=glm(df_has$is_team_A_winner ~  1,family=binomial("logit"))
lm_summary_table(g)
#run with odds predictor
df_has$team_A_superiority = ifelse(df_has$homeShotFirst,df_has$home_superiority,df_has$away_superiority)
mean(df_has$team_A_superiority)
g=glm(df_has$is_team_A_winner ~  df_has$team_A_superiority,family=binomial("logit"))
lm_summary_table(g)



#keeper market value
keepers_with_market_value = read_keepers_with_market_value()
df = games_keeper_join(final_scores,keepers_with_market_value)

#all,no keeper value
df_no_has = get_uncovered_rows(df)
g=glm(df_no_has$is_team_A_winner ~  1,family=binomial("logit"))
lm_summary_table(g)

#has market value
df_has = get_covered_rows(df)
#base
g=glm(df_has$is_team_A_winner ~  1,family=binomial("logit"))
lm_summary_table(g)
#with keeper value predictor
df_has$home_keeper_superiority = log(df_has$home_keeper_market_value) - log(df_has$away_keeper_market_value)
df_has$A_keeper_superiority = ifelse(df_has$homeShotFirst, df_has$home_keeper_superiority,df_has$home_keeper_superiority * -1)
g=glm(df_has$is_team_A_winner ~  df_has$A_keeper_superiority,family=binomial("logit"))
lm_summary_table(g)


#keeper penalty stats
keepers_with_penalty_stats = read_keepers_with_penalty_stats()
df = games_keeper_join(final_scores,keepers_with_penalty_stats)

#no keeper penalty stats
df_no_has = get_uncovered_rows(df)
g=glm(df_no_has$is_team_A_winner ~  1,family=binomial("logit"))
lm_summary_table(g)

#has penalty stats
df_has = get_covered_rows(df)
#base
g=glm(df_has$is_team_A_winner ~  1,family=binomial("logit"))
lm_summary_table(g)
#with keeper penalty stats predictor
df_has$home_keeper_superiority = df_has$home_keeper_adj_save_ratio - df_has$away_keeper_adj_save_ratio
df_has$A_keeper_superiority = ifelse(df_has$homeShotFirst, df_has$home_keeper_superiority,df_has$home_keeper_superiority * -1)
mean(df_has$A_keeper_superiority)
g=glm(df_has$is_team_A_winner ~  df_has$A_keeper_superiority,family=binomial("logit"))
lm_summary_table(g)



#on AER data
#round wise win proportion
ddply(pre_2003,c("dist_from_final"),n=length(dist_from_final),win_ratio = sum(is_team_A_winner)/length(is_team_A_winner),summarise)
ddply(post_2003,c("dist_from_final"),n=length(dist_from_final),win_ratio = sum(is_team_A_winner)/length(is_team_A_winner),summarise)
ddply(final_scores,c("dist_from_final"),n=length(dist_from_final),win_ratio = sum(is_team_A_winner)/length(is_team_A_winner),summarise)



create_superset_with_MS = function(our_games){
  #merged with ms data
  ms_games_joined = read_ms_games()
  #get more games from our data set
  df = myjoin(ms_games,our_games,c1="our_uri")
  uri = setdiff(our_games$uri,df$our_uri_uri)
  non_ms_games = data.frame(uri)
  non_ms_games = myjoin(non_ms_games,our_games,join_type="")
  
  non_ms_games = non_ms_games[,c("uri","dist_from_final","is_team_A_winner","month","year"
                                 ,"competition_is_aer_competition")]
  ms_games_joined$competition_is_aer_competition = 1
  superset = data.frame(rbind(ms_games_joined,non_ms_games))
  df = superset
  
  df = df[!is.na(df$dist_from_final),]
  return(df)
}

#distance from final
g=glm(df$is_team_A_winner ~  1,family=binomial("logit"))
lm_summary_table(g)
g=glm(df$is_team_A_winner ~  df$dist_from_final,family=binomial("logit"))
lm_summary_table(g)

#is ko
df$is_ko = df$dist_from_final <=2
g=glm(df$is_team_A_winner ~  df$is_ko,family=binomial("logit"))
lm_summary_table(g)

