#! python2
# -*- coding: utf-8 -*-
import scrapy
from scrapy.http import TextResponse
from datetime import datetime,timedelta
import codecs
import requests
from lxml import html
import csv
import re
import math
from betexplorer.items import BetexplorerItem

# list of sports
gameslist = ['soccer']
#gameslist = ['baseball','basketball','handball','hockey','soccer','tennis','volleyball']
scrapingtime = str(datetime.now()).replace(':','_').replace('.','_')



class betexplorerpider(scrapy.Spider):
    name = "betexplorer"
    with open('spider_input.txt') as f:
        start_urls = [url.strip() for url in f.readlines()]

    def csvheaders (self,game):
        # create csv files and its headers
        with open(game + '_Games_list' + '_' + scrapingtime + '.csv', 'wb') as csvfile:
            csv_writer = csv.writer(csvfile)
            csv_writer.writerow(['game_id','URL','Sport','Competition','season','match_day','match_month','match_year','start_time','participant_1','participant_2','participant_1_score','participant_2_score','full_score','went_to_pso'])
        with open(game + '_Games_Odds_1x2_Odds' + '_' + scrapingtime + '.csv', 'wb') as csvfile:
            csv_writer = csv.writer(csvfile)
            csv_writer.writerow(['game_id','bookmaker','1','X', '2'])
        with open(game + '_Games_Odds_OverUnder_Odds' + '_' + scrapingtime + '.csv', 'wb') as csvfile:
            csv_writer = csv.writer(csvfile)
            csv_writer.writerow(['game_id','bookmaker','TOTAL','OVER', 'UNDER'])
        with open(game + '_Games_Odds_Asian_Handicap_Odds' + '_' + scrapingtime + '.csv', 'wb') as csvfile:
            csv_writer = csv.writer(csvfile)
            csv_writer.writerow(['game_id','bookmaker','HANDICAP','1', '2'])
        with open(game + '_Games_Odds_HomeAway_Odds' + '_' + scrapingtime + '.csv', 'wb') as csvfile:
            csv_writer = csv.writer(csvfile)
            csv_writer.writerow(['game_id','bookmaker','1', '2'])
        with open(game + '_Games_Odds_DoubleChance_Odds' + '_' + scrapingtime + '.csv', 'wb') as csvfile:
            csv_writer = csv.writer(csvfile)
            csv_writer.writerow(['game_id','bookmaker','1X','12', 'X2'])
        with open(game + '_Games_Odds_Both_Teams_To_Score_Odds' + '_' + scrapingtime + '.csv', 'wb') as csvfile:
            csv_writer = csv.writer(csvfile)
            csv_writer.writerow(['game_id','bookmaker','YES','NO'])
    
    def start_requests(self):
        if self.start_urls : 
            print ('Working on specific inputs')
            for url in self.start_urls:
                if url.count('/') > 6:
                    print ('specific match link found!')
                    game = url.replace('https://www.betexplorer.com/','').split('/')[0]
                    self.csvheaders(game)
                    yield scrapy.Request(url,callback=self.parse_details,)
                if url.count('/') == 6:
                    print ('specific competition link found!')
                    game = url.replace('https://www.betexplorer.com/','').split('/')[0]
                    self.csvheaders(game)
                    yield scrapy.Request(url,callback=self.parse,)
                if url.count('/') == 0:
                    
                    if '-' in url:
                        print ('could be a date query')
                        dates = url.split(':')
                        datescount = len (dates)
                        if datescount == 1:
                            print ('Single Date Query')
                            searchdate = datetime.strptime(dates[0], '%Y-%m-%d')
                            for game in gameslist:
                                self.csvheaders(game)
                                yield scrapy.Request('https://www.betexplorer.com/results/' + game + '/?year=' + str(searchdate.year) + '&month=' + str(searchdate.month)  + '&day=' + str(searchdate.day),callback=self.parse,)
                        elif datescount == 2:
                            print ('Date Range Query')
                            startdate = dates[0]
                            enddate = dates[1]
                            startdate = datetime.strptime(startdate, '%Y-%m-%d')
                            enddate = datetime.strptime(enddate, '%Y-%m-%d')
                            dateslist = [startdate + timedelta(days=i) for i in range((enddate-startdate).days+1)]
                            for game in gameslist:
                                self.csvheaders(game)
                                for dte in dateslist:
                                    yield scrapy.Request('https://www.betexplorer.com/results/' + game + '/?year=' + str(dte.year) + '&month=' + str(dte.month)  + '&day=' + str(dte.day),callback=self.parse,)
        else:
            print ('***Warning***')
            print ('There are No specific inputs/queries in the spider_input.txt file!')
            print ('Scraping all site content!')
            # starting urls to cover all history range 1900 : 2020
            for y in range (1900,2020):
                for m in range (1,13):
                    for d in range (1,32):
                        for game in gameslist:
                            self.start_urls.append('https://www.betexplorer.com/results/' + game + '/?year=' + str(y) + '&month=' + str(m)  + '&day=' + str(d))
            for game in gameslist:
                self.csvheaders(game)
            for url in self.start_urls:
                yield scrapy.Request(url,callback=self.parse,)
    
    def parse(self, response):
        # parsing urls of different matches on each day
        gamelist = response.xpath('//*[@class="table-main__tt"]/a/@href | //*[@class="in-match"]/@href').extract()
        gamelist = list(set(gamelist))
        gamescount = len (gamelist)
        print (gamescount, 'matches found on this page!',response.request.url)
        for x in gamelist:
            yield response.follow('https://www.betexplorer.com' + x, callback=self.parse_details)
        
    def parse_details(self, response):
        # parsing match details
        
        item = BetexplorerItem()
        item['game_id'] =  response.request.url.split('/')[-2]
        print (item['game_id'],'scraping match details!')
        game_id = item['game_id'] 
        item['URL'] =  response.request.url
        item['Sport'] = response.xpath('//li[2]//*[@class="list-breadcrumb__item__in"]/@href').extract_first().replace('/','')
        game = item['Sport']
        item['Competition'] = response.xpath('//*[@class="wrap-section__header__title"]/a/text()').extract_first()
        item['participant_1'] =  response.xpath('//li[1]//*[@class="list-details__item__title"]/a/text()').extract_first()
        item['participant_2'] =  response.xpath('//li[3]//*[@class="list-details__item__title"]/a/text()').extract_first()
        try:
            item['participant_1_score'] =  response.xpath('//*[@id="js-score"]/text()').extract_first().split(':')[0]
            item['participant_2_score'] =  response.xpath('//*[@id="js-score"]/text()').extract_first().split(':')[-1]
        except:
            item['participant_1_score'] =  ''
            item['participant_2_score'] =  ''
        try:
            item['went_to_pso'] =  int(response.xpath('//*[@id="js-eventstage"]/text()').extract_first().find("Pen")>-1)
        except:
            item['went_to_pso'] =  0
                
        item['full_score'] =  response.xpath('//*[@id="js-partial"]/text()').extract_first()
        if item['full_score'] :
            if '(' not in item['full_score']:
                item['full_score'] = ''
        else:
            item['full_score']  = ''
        item['season'] =  response.xpath('//*[@class="wrap-section__header__title"]/a/text()').extract_first()
        item['season'] = re.findall(r'\d\d\d\d',item['season'])[0]
        item['start_time'] =  response.xpath('//*[@id="match-date"]/@data-dt').extract_first().split(',')
        item['start_time'] = item['start_time'][-2] + ':' + item['start_time'][-1]
        item['match_day'] =  response.xpath('//*[@id="match-date"]/@data-dt').extract_first().split(',')[0]
        item['match_month'] =  response.xpath('//*[@id="match-date"]/@data-dt').extract_first().split(',')[1]
        item['match_year'] =  response.xpath('//*[@id="match-date"]/@data-dt').extract_first().split(',')[2]
        # saving match details
        
        with open(game + '_Games_list' + '_' + scrapingtime + '.csv', 'ab') as csvfile:
            csv_writer = csv.writer(csvfile)
            csv_writer.writerow([item['game_id'].encode("utf8"),item['URL'].encode("utf8"),item['Sport'].encode("utf8"),item['Competition'].encode("utf8") ,item['season'].encode("utf8"),item['match_day'].encode("utf8"),item['match_month'].encode("utf8"),item['match_year'].encode("utf8"),item['start_time'].encode("utf8"),item['participant_1'].encode("utf8"),item['participant_2'].encode("utf8"),item['participant_1_score'].encode("utf8"),item['participant_2_score'].encode("utf8"),item['full_score'].encode("utf8"),item['went_to_pso']])
            print (item['game_id'] ,'match details saved!')


        
        # odd list
        oddlist = []
        #oddlist = ['1x2']
        #oddlist = ['1x2','ou','ah','ha','dc','bts']
        
        for odd in oddlist:
            # generating custom request with custom header to access odds tables
            url = "https://www.betexplorer.com/gres/ajax/matchodds.php?p=1&e=" + game_id + "&b=" + odd
            path = url.replace('https://www.betexplorer.com','')
            headers = {'authority': 'www.betexplorer.com',
            'method': 'GET',
            'path': path,
            'scheme': 'https',
            'accept': 'application/json, text/javascript, */*; q=0.01',
            'accept-encoding': 'gzip, deflate, br',
            'accept-language': 'en-GB,en;q=0.9,en-US;q=0.8,ar;q=0.7',
            'cache-control': 'no-cache',
            'cookie': '_ga=GA1.2.1115577784.1540242557; js_cookie=1; my_cookie_id=83830625; my_cookie_hash=5e85aabc0f93900f57fad21beb0bd813; my_timezone=%2B1; _gid=GA1.2.914814308.1540753206; upcoming=1-1%2C4-5; page_cached=1; widget_timeStamp=1540890536; _session_UA-191939-1=true; widget_pageViewCount=15; _gat_UA-191939-1=1',
            'pragma': 'no-cache',
            'referer': item['URL'],
            'user-agent': 'Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/69.0.3497.100 Safari/537.36',
            'x-requested-with': 'XMLHttpRequest',

            }
            yield scrapy.Request(url,callback=self.parseodds,headers=headers,meta={'oddtype':odd,'game_id':game_id,'game':game})

    def parseodds(self, response):
            # parsing odds table data
            odd = response.meta.get('oddtype')
            gamename = response.meta.get('game')
            game_id = response.meta.get('game_id')
            print (game_id,'scraping match odds!',odd)
            draft = response.text
            draft = draft.replace('{"odds":"','')
            draft = draft.replace('\\n"}','')
            draft = draft.replace('\\n','')
            draft = draft.replace('\\','')
            r_html = html.fromstring(draft)
            oddrows = r_html.xpath('//tr[@data-bid] | //tfoot')
            temp_total = ''
            temp_HANDICAP = ''
            for row in oddrows:
                if odd == '1x2':
                    bookmaker = row.xpath('.//a[@target]/text() | .//tr[1]/td[1]/text()')
                    odd1 = row.xpath('.//td[last()-2]/@data-odd | .//td[last()-2][not(@*)]/text()')
                    oddX = row.xpath('.//td[last()-1]/@data-odd | .//td[last()-1][not(@*)]/text()')
                    odd2 = row.xpath('.//td[last()]/@data-odd | .//td[last()][not(@*)]/text()')
                    
                    try:
                        float(odd1[0])
                    except:
                        odd1[0] = ''
                    try:
                        float(oddX[0])
                    except:
                        oddX[0] = ''
                    try:
                        float(odd2[0])
                    except:
                        odd2[0] = ''
                    with open(gamename + '_Games_Odds_1x2_Odds' + '_' + scrapingtime + '.csv', 'ab') as csvfile:
                        csv_writer = csv.writer(csvfile)
                        csv_writer.writerow([game_id.encode("utf8"),bookmaker[0].encode("utf8"),odd1[0].encode("utf8"),oddX[0].encode("utf8"), oddX[0].encode("utf8")])
                        print (game_id,'saving match odds!',odd)
                if odd == 'ou':
                    bookmaker = row.xpath('.//a[@target]/text() | .//tr[1]/td[1]/text()')
                    TOTAL = row.xpath('.//td[@class="table-main__doubleparameter"]/text() | .//td[@class="table-main__doubleparameter"]/text()')
                    totalcount = len(TOTAL)
                    if totalcount > 0:
                        temp_total = TOTAL[0]
                    else:
                        TOTAL = [temp_total]
                    OVER = row.xpath('.//td[last()-1]/@data-odd | .//td[last()-1][not(@*)]/text()')
                    UNDER = row.xpath('.//td[last()]/@data-odd | .//td[last()][not(@*)]/text()')
                    
                    try:
                        float(OVER[0])
                    except:
                        OVER[0] = ''
                    try:
                        float(UNDER[0])
                    except:
                        UNDER[0] = ''
                    
                   
                    with open(gamename + '_Games_Odds_OverUnder_Odds' + '_' + scrapingtime + '.csv', 'ab') as csvfile:
                        csv_writer = csv.writer(csvfile)
                        csv_writer.writerow([game_id.encode("utf8"),bookmaker[0].encode("utf8"),TOTAL[0].encode("utf8"),OVER[0].encode("utf8"), UNDER[0].encode("utf8")])
                        print (game_id,'saving match odds!',odd)
                if odd == 'ah':
                    bookmaker = row.xpath('.//a[@target]/text() | .//tr[1]/td[1]/text()')
                    HANDICAP = row.xpath('.//td[@class="table-main__doubleparameter"]/text() | .//td[@class="table-main__doubleparameter"]/text()')
                    HANDICAPcount = len(HANDICAP)
                    if HANDICAPcount > 0:
                        temp_HANDICAP = HANDICAP[0]
                    else:
                        HANDICAP = [temp_HANDICAP]
                    odd1 = row.xpath('.//td[last()-1]/@data-odd | .//td[last()-1][not(@*)]/text()')
                    odd2 = row.xpath('.//td[last()]/@data-odd | .//td[last()][not(@*)]/text()')
                    
                    try:
                        float(odd1[0])
                    except:
                        odd1[0] = ''
                    try:
                        float(odd2[0])
                    except:
                        odd2[0] = ''
                   
                    with open(gamename + '_Games_Odds_Asian_Handicap_Odds' + '_' + scrapingtime + '.csv', 'ab') as csvfile:
                        csv_writer = csv.writer(csvfile)
                        csv_writer.writerow([game_id.encode("utf8"),bookmaker[0].encode("utf8"),HANDICAP[0].encode("utf8"),odd1[0].encode("utf8"), odd2[0].encode("utf8")])
                        print (game_id,'saving match odds!',odd)
                if odd == 'ha':
                    bookmaker = row.xpath('.//a[@target]/text() | .//tr[1]/td[1]/text()')
                    odd1 = row.xpath('.//td[last()-1]/@data-odd | .//td[last()-1][not(@*)]/text()')
                    odd2 = row.xpath('.//td[last()]/@data-odd | .//td[last()][not(@*)]/text()')
                    
                    try:
                        float(odd1[0])
                    except:
                        odd1[0] = ''
                    try:
                        float(odd2[0])
                    except:
                        odd2[0] = ''
                    
                    with open(gamename + '_Games_Odds_HomeAway_Odds' + '_' + scrapingtime + '.csv', 'ab') as csvfile:
                        csv_writer = csv.writer(csvfile)
                        csv_writer.writerow([game_id.encode("utf8"),bookmaker[0].encode("utf8"),odd1[0].encode("utf8"), odd2[0].encode("utf8")])
                        print (game_id,'saving match odds!',odd)
                if odd == 'dc':
                    bookmaker = row.xpath('.//a[@target]/text() | .//tr[1]/td[1]/text()')
                    odd1X = row.xpath('.//td[last()-2]/@data-odd | .//td[last()-2][not(@*)]/text()')
                    odd12 = row.xpath('.//td[last()-1]/@data-odd | .//td[last()-1][not(@*)]/text()')
                    oddX2 = row.xpath('.//td[last()]/@data-odd | .//td[last()][not(@*)]/text()')
                    
                    try:
                        float(odd1X[0])
                    except:
                        odd1X[0] = ''
                    try:
                        float(odd12[0])
                    except:
                        odd12[0] = ''
                    try:
                        float(oddX2[0])
                    except:
                        oddX2[0] = ''
                    
                    with open(gamename + '_Games_Odds_DoubleChance_Odds' + '_' + scrapingtime + '.csv', 'ab') as csvfile:
                        csv_writer = csv.writer(csvfile)
                        csv_writer.writerow([game_id.encode("utf8"),bookmaker[0].encode("utf8"),odd1X[0].encode("utf8"),odd12[0].encode("utf8"), oddX2[0].encode("utf8")])
                        print (game_id,'saving match odds!',odd)
                if odd == 'bts':
                    bookmaker = row.xpath('.//a[@target]/text() | .//tr[1]/td[1]/text()')
                    YES = row.xpath('.//td[last()-1]/@data-odd | .//td[last()-1][not(@*)]/text()')
                    NO = row.xpath('.//td[last()]/@data-odd | .//td[last()][not(@*)]/text()')
                    with open(gamename + '_Games_Odds_Both_Teams_To_Score_Odds' + '_' + scrapingtime + '.csv', 'ab') as csvfile:
                        csv_writer = csv.writer(csvfile)
                        csv_writer.writerow([game_id.encode("utf8"),bookmaker[0].encode("utf8"),YES[0].encode("utf8"),NO[0].encode("utf8")])
                        print (game_id,'saving match odds!',odd)