source("./R_Code/PSO/utils.R")
source("./R_Code/PSO/dimension_pre_processing.R")
library(plyr)

source("./R_Code/PSO/data_loader.R")
load_all()

#by competition
x = ddply(final_scores[final_scores$year<=2003,],c("competition"),summarise,total_matches=length(competition),p_value=binom.test(sum(is_team_A_winner),length(competition))$p.value,proportion_won = sum(is_team_A_winner)/length(competition))
x[order(x$total_matches * -1),]
write.csv(x[order(x$total_matches * -1),],row.names = F)

#Differences in transfer and welt players
mean(keeper_with_penalty_stats$c_matches,na.rm=T)
mean(keepers[is.na(keepers$n_conceeded_penalties) | keepers$n_saved_penalties == 0,]$c_matches,na.rm=T)

mean(shooters_with_pen_stats$c_matches,na.rm=T)
mean(shooters[is.na(shooters$n_scored_penalties) | shooters$n_scored_penalties == 0,]$c_matches,na.rm=T)

shooters_whtout_penalty_stats = shooters[is.na(shooters$n_scored_penalties) | shooters$n_scored_penalties == 0,]
mean(shooters$c_goals/shooters$c_matches,na.rm=T)
mean(shooters_whtout_penalty_stats$c_goals/shooters_whtout_penalty_stats$c_matches,na.rm=T)

df = myjoin(post_2003,odds)
df$has_odds = !is.na(df$odds_h)
x = ddply(df,c("year"),summarise,total_matches=length(competition),proportion_won = sum(is_team_A_winner)/length(competition),coverage = sum(has_odds)/length(has_odds))
x[order(x$total_matches * -1),]