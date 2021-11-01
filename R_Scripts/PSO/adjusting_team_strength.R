odds = read_odds()
odds$home_dominance = log(odds$adjusted_ph / odds$adjusted_pa)
hist(odds$home_dominance)


shootouts_with_odds=myjoin(final_scores,odds,c1="uri",c2="uri",join_type="")
hist(shootouts_with_odds$year)


g=glm(is_home_winner ~  homeShotFirst + home_dominance,data = shootouts_with_odds,family=binomial("logit"))
summary(g)

kt=keeper_stats
kt_filtered = keeper_stats[keeper_stats$n>10,]

#Split keepers, naive approach
kt_filtered = kt_filtered[order(kt_filtered$p_adj),]
kt_filtered$skill = 0
kt_filtered[1:floor(nrow(kt_filtered)/3),]$skill = 1
kt_filtered[floor(nrow(kt_filtered)/3):nrow(kt_filtered),] = -1

shootouts = final_scores
shootouts=myjoin(shootouts,kt_filtered,c1="home_keeper",c2="shot_keeper")
shootouts[is.na(shootouts$home_keeper_skill),]$home_keeper_skill = 0

shootouts=myjoin(shootouts,kt_filtered,c1="away_keeper",c2="shot_keeper")
shootouts[is.na(shootouts$away_keeper_skill),]$away_keeper_skill = 0

shootouts$keeper_skill_diff = shootouts$home_keeper_skill - shootouts$away_keeper_skill

ddply(shootouts,c("keeper_skill_diff"),n=length(uri),p=myprop(is_home_winner),summarize)

#Beta biomial approximation of keeper quality
kt_filtered = kt_filtered[kt_filtered$p!=1,]
kt_filtered[kt_filtered$p==0,]$p=0.01


m=MASS::fitdistr(kt_filtered_adj$p,"beta",start = list(shape1 = 4, shape2 = 2))
alpha0 <- m$estimate[1]
beta0 <- m$estimate[2]
ggplot(kt_filtered) +
  geom_histogram(aes(p, y = ..density..), binwidth = .005) +
  stat_function(fun = function(x) dbeta(x, alpha0, beta0), color = "red",
                size = 1) +
  xlab("keeper concede ratio")
#Can I use the distribution to update the 
kt$p_adj = (kt$n * kt$p + alpha0)/(kt$n + alpha0 + beta0)
kt[kt$p==1,]$p_adj = alpha0/(alpha0 + beta0)

multiplot(
  ggplot(kt) +
  geom_histogram(aes(p, y = ..density..), binwidth = .005),
  ggplot(kt) +
    geom_histogram(aes(p_adj, y = ..density..), binwidth = .005))
  
shootouts = final_scores
shootouts=myjoin(shootouts,kt,c1="home_keeper",c2="shot_keeper")

shootouts=myjoin(shootouts,kt,c1="away_keeper",c2="shot_keeper")

shootouts$keeper_concede_ratio_diff = shootouts$home_keeper_p_adj - shootouts$away_keeper_p_adj
shootouts$keeper_info_strength = ifelse(shootouts$home_keeper_n+shootouts$away_keeper_n>10,1,0)
g=glm(is_home_winner ~  keeper_concede_ratio_diff * keeper_info_strength + homeShotFirst,family=binomial("logit"),data = shootouts)
summary(g)

