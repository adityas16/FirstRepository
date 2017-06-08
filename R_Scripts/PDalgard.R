library ("foreign")
iq.data <- read.dta ("ARM_Data/child.iq/child.iq.dta")
iq.lm = lm(ppvt~momage + educ_cat,data = iq.data)
summary(iq.lm)
plot(iq.data$momage,iq.data$ppvt)
abline(iq.lm)

#are the residuals normally distributed
qqnorm(resid(iq.lm))


par(mfrow=c(2,1))

colours = c('#eff3ff','#bdd7e7','#6baed6','#2171b5')
plot(iq$momage, iq$ppvt, xlab="Mother age", ylab="Child test score", col=colours, pch=20)
for (i in 1:4) {
  curve(cbind(1, x, i) %*% coef(m2), add=TRUE, col=colours[i])
}

invlogit = function(x) {1/(1+exp(-x))}
curve(invlogit(cbind(0,x) %*% coef(fit.1)))
