odds = read_odds()
odds$home_dominance = log(odds$adjusted_ph / odds$adjusted_pa)
hist(odds$home_dominance)


shootouts_with_odds=myjoin(final_scores,odds,c1="uri",c2="uri",join_type="")
hist(shootouts_with_odds$year)


g=glm(is_home_winner ~  homeShotFirst + home_dominance,data = shootouts_with_odds,family=binomial("logit"))
summary(g)
tidy(g)

kt = keeper_stats[keeper_stats$n>5,]
kt = kt[order(kt$p),]

kt$skill = 0
kt[1:floor(nrow(kt)/3),]$skill = 1
kt[floor(nrow(kt)/3):nrow(kt),] = -1

shootouts = final_scores
shootouts=myjoin(shootouts,kt,c1="home_keeper",c2="shot_keeper")
shootouts[is.na(shootouts$home_keeper_skill),]$home_keeper_skill = 0

shootouts=myjoin(shootouts,kt,c1="away_keeper",c2="shot_keeper")
shootouts[is.na(shootouts$away_keeper_skill),]$away_keeper_skill = 0

shootouts$keeper_skill_diff = shootouts$home_keeper_skill - shootouts$away_keeper_skill

to_csv(ddply(shootouts,c("keeper_skill_diff"),n=length(uri),p=myprop(is_home_winner),summarize))
