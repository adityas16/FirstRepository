na=227
na1=57
na_minus1=52

nb=212
nb1=39
nb_minus1=42

mu_1 = (na1+nb1)/(na+nb)
mu_minus1 = (na_minus1+nb_minus1)/(na+nb)
mu=mu_1 - mu_minus1


pa1=na1/na
pa_minus1= na_minus1/na
var_a =((pa1*(1-pa1)) + (pa_minus1*(1- pa_minus1)))/na

pb1=nb1/nb
pb_minus1= nb_minus1/nb
var_b =(pb1*(1-pb1) + pb_minus1*(1- pb_minus1))/nb

xa=(pa1 - pa_minus1)
xb=(pb1 - pb_minus1)

df= na+nb

z=(xa-xb)/sqrt(var_a + var_b)

(1-pnorm(z))
  