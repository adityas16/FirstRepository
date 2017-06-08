library(plyr)

n_friends = 1000
n_clashes = 10
c = 0
n=1000
for(i in (1:n) ){
  birthdays = sample(1:365,n_friends,replace=TRUE)
  
  mydata = data.frame(birthdays)
  
  grouped_birthdays = ddply(mydata, c("birthdays"), summarise,total = length(birthdays))
  
  clashing_birthdays = ddply(grouped_birthdays, c("total"), summarise,gr_total = length(birthdays))
  
  if(sum(clashing_birthdays[clashing_birthdays$total>n_clashes-1,c("gr_total")]) > 0){
    c = c + 1
  }

}

c/n


n_installments = 16

installment = 9000
amount = data.frame()
for(rate in seq(8,20,0.5)){
  interest = 0
  for(month in 0:n_installments-1){
    interest = interest + installment * rate / 1200 * month
  }
  amount =  rbind(amount,c(rate,interest + installment * n_installments))
}
amount
