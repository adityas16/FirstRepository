source(paste0(R_CODE,"/PSO/utils.R"))
require(gridExtra)

DATA_FOLDER = paste(BASE_FOLDER,"weltfussball",sep="/")
TRANSFERMRKT_FOLDER = paste(BASE_FOLDER,"transfermrkt",sep="/")
R_OUTPUT_FOLDER = paste(BASE_FOLDER,"R_output",sep="/")

read_competition <- function (){
  competitions = read.csv(paste(DATA_FOLDER,"extractedCSV/competitions.csv",sep="/"))
  competitions$competition = trim(competitions$competition)
  competitions$level[competitions$market_value<=0.1] = 0
  competitions$level[is.na(competitions$market_value)] = 0
  competitions$level[competitions$market_value>0.1] = 1
  competitions$level[competitions$market_value>1] = 2
  competitions$level[competitions$market_value>3] = 3
  competitions[competitions$is_international==1,]$level = competitions[competitions$is_international==1,]$FIFA_ranking -1
  
  competitions$club_level = 0
  competitions[competitions$is_international==0,]$club_level = competitions[competitions$is_international==0,]$level + 1 
  competitions$international_level = 0
  competitions[competitions$is_international==1,]$international_level = competitions[competitions$is_international==1,]$level + 1
  return (competitions);
}

read_pso <- function (){
  include_model_data = 0
  include_shooter_data = 0
  pso = read.csv(paste(DATA_FOLDER,"processedCSV/pso_extended.csv",sep="/"))
  pso$team_A_score_pre_shot = pso$teamAScore - pso$isConverted
  pso$team_B_score_pre_shot = pso$teamBScore - pso$isConverted
  pso$team_A_miss = pso$round - pso$team_A_score_pre_shot - 1
  pso$team_B_miss = pso$round - pso$team_B_score_pre_shot - 1
  pso$shooter_team_miss = ifelse(pso$is_team_A_shot,pso$team_A_miss,pso$team_B_miss)
  pso$shot_id = paste(pso$uri,pso$kickNumber,sep = "_")
  pso$competition=NULL
  pso$year=NULL
  pso = myjoin(pso,read_games(),join_type="")
  pso$competition = trim(pso$competition)
  pso$is_A_goal = pso$is_team_A_shot * pso$isConverted
  pso$is_B_goal = pso$is_team_B_shot * pso$isConverted
  pso$is_home_shot = ifelse(pso$homeShotFirst,pso$is_team_A_shot,pso$is_team_B_shot)
  pso$round_adjusted = pso$round
  pso$round_adjusted[pso$round>6] = 6
  pso$kickNumber_adjusted = pso$kickNumber
  pso$kickNumber_adjusted[pso$kickNumber>10 & pso$kickNumber %%2 ==1] = 11
  pso$kickNumber_adjusted[pso$kickNumber>10 & pso$kickNumber %%2 ==0] = 12
  pso$is_prev_converted = c(0,head(pso$isConverted,-1))
  pso$is_prev_converted[pso$isFirstShot==1]=0
  uri = pso[pso$is_last_shot == T,]$uri
  df = data.frame(uri)
  #A takes extra shot?
  df = myjoin(df,ddply(pso,c("uri"),
                       A_takes_extra_shot = sum(is_team_A_shot)-sum(is_team_B_shot),
                       is_team_A_winner = sum(is_A_goal)>sum(is_B_goal),
                       num_kicks = length(kickNumber),
                       summarise),join_type="")
  pso = myjoin(pso,df,join_type = "")
  pso$shooter_gd = ifelse(pso$is_team_A_shot,pso$gd,-1*pso$gd)
  pso$shooter_gd_sign = sign(pso$shooter_gd)
  if(include_model_data ==1){
    model_output = read.csv(paste(DATA_FOLDER,"processedCSV/pso_model_output.csv",sep="/"))
    pso = myjoin(pso,model_output,c1="shot_id",c2="shot_id",prepend_c1_label=F)
    pso$pr_shooter_pre_shot = ifelse(pso$is_team_A_shot,pso$pr_A_pre_shot,1-pso$pr_A_pre_shot)
    pso$pr_shooter_if_scored = ifelse(pso$is_team_A_shot,pso$pr_A_if_scored,1-pso$pr_A_if_scored)
    pso$pr_shooter_if_missed = ifelse(pso$is_team_A_shot,pso$pr_A_if_missed,1-pso$pr_A_if_missed)
  }
  if(include_shooter_data ==1){
    in_play_pen = read_in_play_penalties()
    by_scorer = ddply(in_play_pen,c("scorer"),n_in_play_penalties = length(scorer),summarise)
    df = myjoin(pso,by_scorer,c1="shooter",c2="scorer")
    pso$shooter_n_in_play_penalties_scored = 0
    pso[!is.na(df$shooter_n_in_play_penalties),]$shooter_n_in_play_penalties_scored = df[!is.na(df$shooter_n_in_play_penalties),]$shooter_n_in_play_penalties
    pso$shooter_experienced = 0
    pso[!is.na(df$shooter_n_in_play_penalties),]$shooter_experienced = 1
    #pso$shooter_position = factor(pso$shooter_position,levels=c("defender","midfielder","forward","na"))
  }
  write.csv(pso,paste(R_OUTPUT_FOLDER,"pso.csv",sep = '/'))
  return (pso);
}

read_games <- function (){
  games = read.csv(paste(DATA_FOLDER,"extractedCSV/games.csv",sep="/"))
  #games = read_aer_games()
  #games = read_manually_marked_competitions()
  games$is_home_winner = ifelse(games$homeScore > games$awayScore,1,0)
  games$competition = trim(games$competition)
  games$final_home_score = games$homeScore
  games$final_away_score = games$awayScore
  return (games);
}

read_final_scores = function(){
  pso = read_pso()
  games = read_games()
  final_scores = pso[pso$is_last_shot==1,]
  final_scores$is_home_winner = ifelse(final_scores$homeScore> final_scores$awayScore,1,0)
  final_scores_year = NULL
  return(myjoin(final_scores,games,join_type=""))
}
read_in_play_penalties = function(){
  inplay_goals = read.csv(paste(DATA_FOLDER,"extractedCSV/goals.csv",sep="/"))
    return(inplay_goals[inplay_goals$isPenalty == "true",])
}
read_odds <- function (){
  odds = read.csv(paste(WELT_FOLDER,"joined/bet_ex_odds.csv",sep="/"))
  odds = odds[!is.na(odds$odds_h) & !is.na(odds$odds_a) & !is.na(odds$odds_t),]
  odds$p_h = (1/odds$odds_h) / (1/odds$odds_h + 1/odds$odds_a + 1/odds$odds_t)
  odds$p_a = (1/odds$odds_a) / (1/odds$odds_h + 1/odds$odds_a + 1/odds$odds_t)
  odds$p_t = (1/odds$odds_t) / (1/odds$odds_h + 1/odds$odds_a + 1/odds$odds_t)
  odds$adjusted_ph = odds$p_h / (odds$p_h + odds$p_a)
  odds$adjusted_pa = odds$p_a / (odds$p_h + odds$p_a)
  #odds$home_superiority = odds$adjusted_ph - 0.5
  #odds$away_superiority = odds$adjusted_pa - 0.5
  odds$home_superiority = -1 * log(odds$odds_h/odds$odds_a)
  odds$away_superiority = -1 * log(odds$odds_a/odds$odds_h)
  return (odds);
}

read_keepers <- function (){
  keepers = read.csv(paste(DATA_FOLDER,"joined/keeper_penalty_stats.csv",sep="/"))
  return (keepers);
}

read_keepers_with_cumulate_stats = function(){
  keepers = read.csv(paste(DATA_FOLDER,"joined/keeper_penalty_stats.csv",sep="/"))
  keepers_with_cumulate_stats = keepers[!is.na(keepers$c_matches),]
  keepers_with_cumulate_stats = keepers_with_cumulate_stats[keepers_with_cumulate_stats$c_matches>10,]
  return (keepers_with_cumulate_stats);
}

read_keepers_with_market_value = function(){
  keepers = read.csv(paste(DATA_FOLDER,"joined/keeper_penalty_stats.csv",sep="/"))
  keepers_with_market_value = keepers[!is.na(keepers$market_value),]
  return (keepers_with_market_value);
}

read_keepers_with_penalty_stats = function(){
  keepers = read.csv(paste(DATA_FOLDER,"joined/keeper_penalty_stats.csv",sep="/"))
  keepers_with_pen_stats = keepers[!is.na(keepers$n_saved_penalties) & !is.na(keepers$n_conceeded_penalties),]
  keepers_with_pen_stats$n_faced_penalties = keepers_with_pen_stats$n_saved_penalties + keepers_with_pen_stats$n_conceeded_penalties
  keepers_with_pen_stats = keepers_with_pen_stats[keepers_with_pen_stats$n_saved_penalties>0 & keepers_with_pen_stats$n_conceeded_penalties>0,]
  keepers_with_pen_stats$save_ratio = keepers_with_pen_stats$n_saved_penalties / (keepers_with_pen_stats$n_faced_penalties)
  hist1= ggplot() + aes(keepers_with_pen_stats$save_ratio)+ geom_histogram(binwidth=0.01, colour="black", fill="white")
  plot1 = ggplot() + aes(y=keepers_with_pen_stats$save_ratio,x=keepers_with_pen_stats$n_faced_penalties) + geom_point()
  bdist = MASS::fitdistr(keepers_with_pen_stats$save_ratio, dbeta,start = list(shape1 = 1, shape2 = 10))
  bdist$estimate[1] / (bdist$estimate[1] + bdist$estimate[2])
  keepers_with_pen_stats$adj_save_ratio = (bdist$estimate[1] + keepers_with_pen_stats$n_saved_penalties)/ (bdist$estimate[1] + bdist$estimate[2] + keepers_with_pen_stats$n_faced_penalties)
  hist2=ggplot() + aes(keepers_with_pen_stats$adj_save_ratio)+ geom_histogram(binwidth=0.01, colour="black", fill="white")
  plot2 = ggplot() + aes(y=keepers_with_pen_stats$adj_save_ratio,x=keepers_with_pen_stats$n_faced_penalties) + geom_point()
  #grid.arrange(plot1, plot2, ncol=2)
  #grid.arrange(plot1, plot2, ncol=2)
  return (keepers_with_pen_stats);
}

read_shooters_with_cumulate_stats = function(){
  shooters = read.csv(paste(DATA_FOLDER,"joined/shooter_penalty_stats.csv",sep="/"))
  shooters_with_cumulate_stats = shooters[!is.na(shooters$c_matches),]
  shooters_with_cumulate_stats = shooters_with_cumulate_stats[shooters_with_cumulate_stats$c_matches>10,]
  return (shooters_with_cumulate_stats);
}
read_shooters_with_penalty_stats = function(){
  shooters = read.csv(paste(DATA_FOLDER,"joined/shooter_penalty_stats.csv",sep="/"))
  shooters_with_pen_stats = shooters[!is.na(shooters$n_missed_penalties) & !is.na(shooters$n_scored_penalties),]
  shooters_with_pen_stats = shooters_with_pen_stats[shooters_with_pen_stats$n_missed_penalties>0 & shooters_with_pen_stats$n_scored_penalties>0,]
  return (shooters_with_pen_stats);
}

read_pre_2003_games = function(){
  final_scores = read_final_scores()
  return(final_scores[final_scores$year<2003 | (final_scores$year==2003 & final_scores$month<=6),])
}


read_post_2003_games = function(){
  final_scores = read_final_scores()
  return(final_scores[final_scores$year>2003 | (final_scores$year==2003 & final_scores$month>6),])
}

read_aer_games = function(){
  return(filter_aer_games(games))
}

read_aer_competition_map = function(){
  return(read.csv(paste0(BASE_FOLDER,"/other_papers/AER/competition_mapping.csv")))
}

filter_aer_games = function(games){
  aer_competition_map = read_aer_competition_map();
  return(myjoin(games,aer_competition_map,c1="competition",c2="my_competition",join_type = ""))
}

read_senior_men_games = function(){
  senior_men_map = read.csv(paste0(WELT_FOLDER, "/extractedCSV/senior_male_competitions.csv"))
  return(myjoin(games,senior_men_map,c1="competition",c2="competition",join_type = ""))
}

read_manually_marked_competitions = function(){
  manually_marked_competitions = read.csv(paste(DATA_FOLDER,"extractedCSV/manually_marked_competitions.csv",sep="/"))
  return(myjoin(games,manually_marked_competitions,c1="competition",c2="my_competition",join_type = ""))
}

read_ms_games = function(){
  my_games = read.csv("/home/aditya/Research Data/weltfussball/extractedCSV/games.csv",stringsAsFactors=FALSE)
  ms_games = read.csv("/home/aditya/Research Data/other_papers/Reconciliation/ms.csv",stringsAsFactors=FALSE)
  ms_games_joined = myjoin(ms_games,all_scores,c1="our_uri")
  
  ms_games_joined = ms_games_joined[,c("game_id","our_uri_uri","aer_id","competition_name","dist_from_final","is_teamA_win","our_uri_dist_from_final")]
  
  
  #pick round info from ms if I dont have it
  ms_games_joined$our_uri_dist_from_final[is.na(ms_games_joined$our_uri_dist_from_final)] = ms_games_joined$dist_from_final[is.na(ms_games_joined$our_uri_dist_from_final)] 
  ms_games_joined$dist_from_final = ms_games_joined$our_uri_dist_from_final
  
  
  #Rename fields
  names(ms_games_joined)[names(ms_games_joined)=="our_uri_uri"] <- "our_id"
  names(ms_games_joined)[names(ms_games_joined)=="game_id"] <- "ms_id"
  
  ms_games_joined$our_uri_dist_from_final = NULL
  ms_games_joined$aer_id = NULL
  ms_games_joined$competition_name = NULL
  #set month and date to -1 to enable filtering on 2003
  ms_games_joined$month = -1
  ms_games_joined$year = -1
  names(ms_games_joined)[names(ms_games_joined) == 'our_id'] <- 'uri'
  names(ms_games_joined)[names(ms_games_joined) == 'is_teamA_win'] <- 'is_team_A_winner'
  #get ms uris for games that we dont have
  ms_games_joined$uri[is.na(ms_games_joined$uri)] = ms_games_joined$ms_id[is.na(ms_games_joined$uri)]
  ms_games_joined$ms_id = NULL
  return(ms_games_joined)
}
