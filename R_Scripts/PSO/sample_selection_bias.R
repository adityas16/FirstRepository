#Read data in Marcelo format
known_shootouts=read.csv("C:\\Users\\aditya\\Dropbox\\penalties\\Data\\Combined\\Backup\\20171020\\Dataset1_combined.csv")

#Drop CDL for now, it has already been analyzed in detail
known_shootouts=known_shootouts[known_shootouts$competition != "Copa del Rey",]

known_shootouts$no_sequence = 1 - known_shootouts$has_sequence
by_competition = ddply(known_shootouts,c("competition"),observed=sum(has_sequence),known_unobserved=sum(no_sequence),n=length(uri),percetage_observed = myprop(has_sequence),summarize)

#Cross with AER competitions?
aer_known_shootouts = filter_aer_games(known_shootouts)
aer_by_competition = ddply(aer_known_shootouts,c("competition"),observed=sum(has_sequence),known_unobserved=sum(no_sequence),summarize)


by_round = ddply(known_shootouts,c("dist_from_final"),observed=sum(has_sequence),known_unobserved=sum(no_sequence),n=length(uri),percetage_observed = myprop(has_sequence),summarize)


by_year = ddply(aer_known_shootouts,c("year"),observed=sum(has_sequence),known_unobserved=sum(no_sequence),n=length(uri),percetage_observed = myprop(has_sequence),summarize)


non_aer = setdiff(senior_male$uri,aer$uri)
non_aer = data.frame(non_aer)
colnames(non_aer)=c("uri")
non_aer = myjoin(non_aer,final_scores)
z.2vector(aer$is_team_A_winner,non_aer$is_team_A_winner)


final_scores$is_team_B_winner = 1- final_scores$is_team_A_winner
x=ddply(final_scores,c("competition"),a=sum(is_team_A_winner),b=sum(is_team_B_winner),summarise)
x=x[x$a>5 & x$b>5,]
#Games belonging to competitions that have at least 5 shootouts that was won by A and B
games_with_enough_data = myjoin(final_scores,x,c1="competition",c2="competition",join_type="")

aer_games_with_enough_data = filter_aer_games(games_with_enough_data)

run_homogeneity_tests(aer_games_with_enough_data)

#Test of homogeneity of team A win proportion across competitions
run_homogeneity_tests = function(games){
  #Chi square test
  print(chisq.test(table(games$competition,games$is_team_A_winner)))
  #ANOVA test across competitions
  model = lm(formula = is_team_A_winner ~ competition, data = games)
  anova(model)
}


#Chi-square test on AER book

#All competitions
chisq.test(matrix(c(13,9, 5,10, 11,7, 12,8, 7,3, 9,7, 31,18, 20,12, 61,49, 91,92, 96,83, 251,96),ncol=2,byrow = T))

#Without copa del rey
chisq.test(matrix(c(13,9, 5,10, 11,7, 12,8, 7,3, 9,7, 31,18, 20,12, 61,49, 91,92, 96,83),ncol=2,byrow = T))


