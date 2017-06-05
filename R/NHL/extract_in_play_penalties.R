penalty_events = all_events$etype=="PENL"
event_after_penalty=c(FALSE,penalty_events[1:length(penalty_events)-1])
unique(all_events[event_after_penalty,]$etype)