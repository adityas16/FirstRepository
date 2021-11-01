Step 0
Check Installations.txt for software requirements.

First step: 
1- Define inputs for the spider, to make it scrape scpecific content:
	A) to scrape all data on the site, make sure that file (spider_input.txt) clear and empty.
	B) to scrape odds for specific matches, copy pate those matches links like the examples below:
		https://www.betexplorer.com/handball/czech-republic/extraliga-2016-2017/dukla-prague-zubri/Gt0Qs9wE/
		https://www.betexplorer.com/handball/russia/superleague-2013-2014/fakel-tkz-chekhovskiye-medvedi/6R2dp4gd/
		https://www.betexplorer.com/handball/czech-republic/czech-slovak-handball-league-2003-2004/allrisk-kosice/rwwp0ENk/
		https://www.betexplorer.com/soccer/spain/segunda-division-b-group-3-2012-2013/mallorca-b-reus-deportiu/MDVtnrGa/
		https://www.betexplorer.com/soccer/romania/liga-1-2002-2003/sr-brasov-sp-studentesc/xxlKmObH/
		https://www.betexplorer.com/soccer/belgium/jupiler-league-2000-2001/club-brugge-kv-beveren/Wvjujhcm/
	C) to scrape odds for matches for specific competition copy paste those competitions links like the examples below:
		https://www.betexplorer.com/tennis/wta-singles/fed-cup-world-group/
		https://www.betexplorer.com/tennis/wta-singles/fed-cup-world-group-2015/
	D) to scrape all odds for all matches on all competitions , all sports for spcific date, use the folowing format:
		2015-12-31
		2014-08-05
	E) to scrape all odds for all matches on all competitions , all sports for range of days, use the folowing format:
		2015-12-01:2015-12-31
		2014-08-05:2014-12-31
	F) Note that you can mix those inputs to make spider scrape different content types, like the following:
		https://www.betexplorer.com/handball/czech-republic/extraliga-2016-2017/dukla-prague-zubri/Gt0Qs9wE/
		https://www.betexplorer.com/tennis/wta-singles/fed-cup-world-group/
		2015-12-31
		2015-12-01:2015-12-31
Second step: 
2- Run the file (runspider.bat), it has command in it (scrapy crawl betexplorer), you can run it from your command prompt/powershell/.... in the root directoy of the spider
Third step: 
3- Data will be stored in csv files with the same format you provided, file names will be appended by date and time (to milliseconds) of initiating the scraping process,
	so each run will create different set of files (due to different milliseonds value)
	
========================
Contact Details
my profile link on upwork
https://www.upwork.com/fl/arabicfreelancer
my email address
amr.alaa.alex@gmail.com

Thanks a lot
