#!/usr/bin/env python

import json
import os
import time
import sys
import string
import random

def loaddata(counters,items):
    '''
    function to transform the hotels dataset from the original strings to random strings with the same length.
    '''
    data = []
    with open('data.dat') as f:
        for line in f:
            data.append(json.loads(line))

    def convert(d):
        if isinstance(d, int):
            return d
        if isinstance(d, float):
            return d
        return d.encode('ascii','ignore')

    entries=[]
    values = {}
    for i in items:
        values[i] = {}

    for line, d in enumerate(data):
        dd = d['response'];
        hotels = dd['data'];
        for hotel in hotels:
            factual_id=convert(hotel['factual_id'])
            name     = convert(hotel['name'])
            category = convert(hotel['category'])
            lowest_pr= int(convert(hotel.get('lowest_price', 0)))
            highest_pr=int(convert(hotel.get('highest_price', 0)))
            ratings  = float(convert(hotel.get('rating', 0)))
            status   = convert(hotel['status'])
            stars    = int(convert(hotel.get('stars', 0)))
            tel      = convert(hotel.get('tel', "no-tel"))
            region   = convert(hotel['region'])
            locality = convert(hotel['locality'])
            postcode = convert(hotel['postcode'])
            longitude= float(convert(hotel['longitude']))
            latitude = float(convert(hotel['latitude']))
            address  = convert(hotel.get('address', "no-address"))

            vals = [factual_id,name,category,lowest_pr,highest_pr,ratings,status,stars,tel,region,locality,postcode,longitude,latitude,address]
            t_vals = []
            for index,(i,j) in enumerate(zip(items,vals)):
                if index in [3,4,5,7,12,13]:
                    t_vals.append(j)
                else:
                    d = values[i]
                    v = d.get(j)
                    if v is None:
                        v = randomstr(len(j) - len(str(counters[i]))) + str(counters[i])
                        d[j] = v
                        counters[i]+=1
                    t_vals.append(v)
            print t_vals

charset=string.letters + string.digits + ". !" 
def randomstr(size):
    '''
    generate a random string of 'charset', with a specific size
    '''
    return ''.join(random.choice(charset) for x in range(size))


items=['factual_id', 'name', 'category', 'lowest_pr', 'highest_pr', 'ratings', 'status', 'stars', 'tel', 'region', 'locality', 'postcode', 'longitude', 'latitude', 'address']
counters={}
for i in items:
    counters[i] = 0;
loaddata(counters,items)
