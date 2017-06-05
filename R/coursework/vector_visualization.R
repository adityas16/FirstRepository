library(rgl)
rgl.init()
rgl.open()
data("iris")
x <- sep.l <- iris$Sepal.Length
y <- pet.l <- iris$Petal.Length
z <- sep.w <- iris$Sepal.Width
lim <- function(x){c(-max(abs(x)), max(abs(x))) * 1.1}
rgl.lines(c(-2,2), c(0, 0), c(0, 0), color = "black")
rgl.lines(c(0, 0), c(-2,2), c(0, 0), color = "black")
rgl.lines(c(0, 0), c(0, 0), c(-2,2), color = "black")
rgl.bg(color = "white")

v1=c(1,2,2)/3
v2=c(-2,-1,2)/3
v4=c(1,1,1)
v3=sum((t(v1)*v4))*v1 +sum((t(v2)*v4)) * v2
rgl.lines(c(0, v1[1]), c(0,v1[2]), c(0, v1[3]), color = "red")
rgl.lines(c(0, v2[1]), c(0,v2[2]), c(0, v2[3]), color = "red")
rgl.lines(c(0, v3[1]), c(0,v3[2]), c(0, v3[3]), color = "green")
rgl.lines(c(0, v4[1]), c(0,v4[2]), c(0, v4[3]), color = "blue")
rgl.lines(c(0, -2), c(0,-1), c(0, 2), color = "red")
rgl.lines(c(0, 1), c(0,1), c(0, 1), color = "blue")
rgl.lines(c(0, 1), c(0,1), c(0, 1), color = "orange")


