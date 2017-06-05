source("./R_Code/PSO/dimension_pre_processing.R")
keepers = read_keepers()
nrow(keepers)

hist(keepers$league_level[!is.na(keepers$league_level)])

keepers$log_market_value = log(keepers$market_value)
keepers_with_value = keepers[!is.na(keepers$market_value),]
keepers$age = 2016 - keepers$birth_year

peak = 35
delta_1 = 20
delta_2 = 35
keepers$dist_from_career_peak[keepers$age<= delta_1] = delta_1 - peak
keepers$dist_from_career_peak[keepers$age>delta_1 & keepers$age<=peak & !is.na(keepers$age)] = keepers$age[keepers$age>delta_1 & keepers$age<=peak & !is.na(keepers$age)] - peak
keepers$dist_from_career_peak[keepers$age>peak & keepers$age<=delta_2 & !is.na(keepers$age)] = peak - keepers$age[keepers$age>peak & keepers$age<=delta_2 & !is.na(keepers$age)] 
keepers$dist_from_career_peak[keepers$age>delta_2] = peak - delta_2


g = lm(keepers$log_market_value ~ keepers$league_level + keepers$birth_year + keepers$c_matches + keepers$dist_from_career_peak)
summary(g)
hist(keepers_with_value$market_value/100000000)

keepers$market_value==0
summary((keepers_with_value$market_value/1000))

ddply(keepers,c("league_level"),n=length(league_level),mean_value = mean(market_value),var = var(market_value - mean(market_value)),summarize)
plot(keepers_with_value$league_level,keepers_with_value$market_value)

minus_top_10 = keepers_with_value[order(keepers_with_value$market_value*-1)[500:length(keepers_with_value$market_value)],]
plot(minus_top_10$league_level,minus_top_10$market_value)

hist(keepers_with_value[keepers_with_value$league_level>1,]$market_value)
