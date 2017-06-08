source("./R_Code/PSO/utils.R")
source("./R_Code/PSO/dimension_pre_processing.R")

load_all = function(){
  BASE_FOLDER="/home/aditya/Research Data/"
  WELT_FOLDER = paste(BASE_FOLDER,"weltfussball",sep="/")
  TRANSFERMRKT_FOLDER = paste(BASE_FOLDER,"transfermrkt",sep="/")
  
  assign("games" ,read_games(),envir = .GlobalEnv)
  assign("pso" ,read_pso(),envir = .GlobalEnv)
  assign("pso_raw" , read.csv(paste(WELT_FOLDER,"extractedCSV/pso_raw.csv",sep="/")),envir = .GlobalEnv)
  assign("final_scores" ,read_final_scores(),envir = .GlobalEnv)
  assign("shooters" ,read.csv(paste(WELT_FOLDER,"joined/shooter_penalty_stats.csv",sep="/")),envir = .GlobalEnv)
  assign("keepers" ,read.csv(paste(WELT_FOLDER,"joined/keeper_penalty_stats.csv",sep="/")), .GlobalEnv)
  assign("odds" ,read_odds(),envir = .GlobalEnv)
}

load_nhl = function(){
  BASE_FOLDER="/home/aditya/Research Data/"
  WELT_FOLDER = paste(BASE_FOLDER,"hockeyref",sep="/")
  
  assign("games" ,read_games(),envir = .GlobalEnv)
  assign("pso" ,read_pso(),envir = .GlobalEnv)
  assign("final_scores" ,read_final_scores(),envir = .GlobalEnv)
}

