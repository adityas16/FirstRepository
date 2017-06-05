mydata <- read.csv("/home/aditya/workspace/model.risk-assessment/payout_dist_bl_continuous.csv")
colnames(mydata) <- c("c", "pr")
mydata = mydata[order(-mydata$c),]
head(mydata[order(-mydata$pr),])
sum(mydata$pr)
ecdf(mydata$pr)
mycdf <- mydata$pr
i = 1;
for(p in mydata$pr){
  if(i>1){
  mycdf[i] =  mycdf[i-1]+ p}
  i = i + 1
}
tail(mycdf)
tail(mydata$pr)
head(mycdf)
head(mydata$pr)
plot(head(mydata[, 1],200), head(mycdf,200),xlab="payout", ylab="Pr(payout)")
plot(mydata[, 1], mycdf,xlab="payout", ylab="Pr(payout)",log="y")
plot(c(1,2),c(1,2))
  logccdf=cbind(mydata[, 1], mycdf)
write.table(logccdf,file="/home/aditya/logccdf.csv")
