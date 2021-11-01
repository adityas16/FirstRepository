# -*- coding: utf-8 -*-

# Define here the models for your scraped items
#
# See documentation in:
# https://doc.scrapy.org/en/latest/topics/items.html

import scrapy


class BetexplorerItem(scrapy.Item):
    # define the fields for your item here like:
    # name = scrapy.Field()
    # gameurl = scrapy.Field()
    game_id = scrapy.Field()
    URL = scrapy.Field()
    Sport = scrapy.Field()
    Competition = scrapy.Field()
    season = scrapy.Field()
    match_day = season = scrapy.Field()
    match_month = season = scrapy.Field()
    match_year = season = scrapy.Field()
    start_time = scrapy.Field()
    participant_1 = scrapy.Field()
    participant_2 = scrapy.Field()
    participant_1_score = scrapy.Field()
    participant_2_score = scrapy.Field()
    full_score = scrapy.Field()
    went_to_pso = scrapy.Field()
    pass
