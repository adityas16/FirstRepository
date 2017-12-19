create_binomial_transitions_table = function(shootouts){
  #URI is NOT unique always, because of the bootstrapping case
  k=ddply(shootouts,c("round_adjusted","gd","is_team_A_shot"),n=length(gd),prop_converted=myprop(isConverted),summarise)
  colnames(k)[1] = "j"
  colnames(k)[2] = "s"
  colnames(k)[3] = "i"
  k=k[order(k$j,k$s * -1,k$i * -1),]
  return(k)
}
