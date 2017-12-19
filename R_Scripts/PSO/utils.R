library(sqldf)
library(plyr)
library(ggplot2)
library(xtable)

#trim both sides of a string
trim <- function (x) gsub("^\\s+|\\s+$", "", x)

#join two data frames
myjoin <- function(df1, df2,c1="uri",c2="uri",join_type="LEFT OUTER" ,prepend_c1_label = T){
  df1_name = deparse(substitute(df1))
  df2_name = deparse(substitute(df2))
  if(c1!="uri" & prepend_c1_label ==T){
    colnames(df2) <- paste(c1, colnames(df2), sep = "_")
    c2 = paste(c1, c2, sep = "_")
  }
  
  query = sprintf("SELECT * 
              FROM 
              %s 
               %s JOIN  %s on %s.%s = %s.%s", "df1",join_type,"df2","df1",c1,"df2",c2)
  joined = sqldf(query)
  joined <- joined[, !duplicated(colnames(joined))]
  return(joined)
}

z.2sample = function(x1,x2,n1,n2){
  numerator = (x1/n1) - (x2/n2)
  p.common = (x1+x2) / (n1+n2)
  denominator = sqrt(p.common * (1-p.common) * (1/n1 + 1/n2))
  z.prop.ris = numerator / denominator
  return(2*pnorm(-abs(z.prop.ris)))
}

z.2prop = function(p1,p2,n1,n2){
  x1 = round(p1*n1)
  x2 = round(p2*n2)
  return(z.prop(x1,x2,n1,n2))
}

z.1prop = function(p,n,p0){
  variance = ((p0 * (1-p0))/n) ^ 0.5
  zscore = (p - p0)/variance
  return(2*pnorm(-abs(zscore)))
}

get_covered_rows = function(df){
  return(df[!is.na(df$na_col),])
}
get_uncovered_rows = function(df){
  return(df[is.na(df$na_col),])
}
my_hist = function(col,bin_width=0.1){
  ggplot() + aes(col)+ geom_histogram(binwidth = bin_width, colour="black", fill="white")
}

logit = function(x){
  return(exp(x)/(1 + exp(x)))
}

zero_mean = function(col){
  return(col - mean(col,na.rm=T))
}

myprop = function(col){
  return(sum(col)/length(col))
}
pre_2003 = function(df){
  return(df[df$year<2003 | (df$year==2003 & df$month<=7),])
}
post_2003 = function(df){
  return(df[df$year>2003 | (df$year==2003 & df$month>7),])
}
to_csv = function(df){
  write.csv(df,paste0(TEMP_FOLDER , "/temp.csv"),row.names = F)
}
my_xtable = function(df,digits = 3){
  precision = rep(0,ncol(df)+1)
  for(i in 1:ncol(df)){
    if(is.numeric(df[,i])){
      if(sum(df[,i]%%1) > 0){
        precision[i+1]=digits
      }
    }
  }
  print(xtable(df,digits = precision),include.rownames = F)
}

month_name_to_int = function(name){
  names = c("January","February","March","April","May","June","July","August","September","October","November","December")
  return(match(name,names))
}
