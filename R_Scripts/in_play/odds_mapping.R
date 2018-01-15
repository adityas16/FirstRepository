in_play_pen = read.csv(paste0(WELT_FOLDER,"/extractedCSV/incidents.csv"))

team_wf=read.csv("C:\\Users\\aditya\\Downloads\\team_WF.csv")

x=in_play_pen
x=myjoin(x,team_wf,join_type="",c1="homeTeam",c2="Team_WF")
x=myjoin(x,team_wf,join_type="",c1="awayTeam",c2="Team_WF")


odds=read.csv(paste0(BASE_FOLDER,"/betexplorer/football_complete.csv"))
team_be=read.csv("C:\\Users\\aditya\\Downloads\\team_be.csv")

y=odds
y=myjoin(y,team_be,join_type="",c1="home_team",c2="Team_BE")
y=myjoin(y,team_be,join_type="",c1="away_team",c2="Team_BE")


z=sqldf("select x.*,y.X1 from x,y 
      where x.day=y.match_day and x.month=y.match_month and x.year=y.match_year
      and x.homeTeam_TeamID = y.home_team_TeamID
      and x.awayTeam_TeamID = y.away_team_TeamID"
)


#Coverage analysis
a=ddply(in_play_pen,c("competition"),n=length(uri),summarise)
View(a)
b=ddply(in_play_pen,c("competition"),n=length(uri),summarise)
b=ddply(z,c("competition"),n=length(uri),summarise)
c=myjoin(a,b,c1="competition",c2="competition",join_type="LEFT OUTER")
c$diff = c$n - c$competition_n
c$coverage = 1-(c$diff/c$n)


#join by date and competition