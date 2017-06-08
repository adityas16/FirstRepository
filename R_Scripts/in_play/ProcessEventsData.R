library("RMySQL");
mydb = dbConnect(MySQL(), user='root', password='root', dbname='football', host='localhost')
dbListTables(mydb)
d <- dbReadTable(mydb, "events")
str(d)
unique(d$match_id)
