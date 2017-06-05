provided_ph = 0.73721;
provided_pa = 0.07712;
mydata <- read.csv("/home/aditya/workspace/model.risk-assessment/sensitivity_analysis_255755.csv")

#provided_ph = 0.43609;
#provided_pa = 0.2689;
#mydata <- read.csv("/home/aditya/workspace/model.risk-assessment/sensitivity_analysis_255763.csv")
mydata <- read.csv("/home/aditya/workspace/model.risk-assessment/sensitivity_analysis.csv")
colnames(mydata) <- 
  c("pH","pT","Ex")
head(mydata,4)
mydata[3] = mydata[3] * -1
#mydata[3] = log(mydata[3])
mydata[2] = 1 - mydata[2] - mydata[1]

mydata[1] = mydata[1] + mydata[2] / 2
mydata[2] = mydata[2] / 2 * 1.76


library("ggplot2")
provided_pt = 1- provided_ph - provided_pa
x = c(provided_ph + provided_pt /2)
y = c(provided_pt / 2 * 1.73)
dataF = data.frame(x,y)
dataF
g <- ggplot(NULL) + 
    geom_point(data =mydata, aes(pH, pT,colour=Ex)) +
   scale_colour_gradient(low = "blue")
   g
g+ geom_point(data = dataF,aes(x=x,y=y))
