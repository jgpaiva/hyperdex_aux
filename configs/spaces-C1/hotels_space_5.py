#!/usr/bin/python
import hyperclient
import sys

c = hyperclient.Client(str(sys.argv[1]), 1982)
c.add_space('''
    space hotels
    key k
    attributes string name, string address, string ratings, string stars, 
               string category, string status, string tel, 
               string region, string locality, string postcode,
               string lowest_price, int highest_price, 
               float longitude, float latitude
    subspace region
    subspace locality
    subspace postcode
    subspace category, stars, ratings
    tolerate 1 failure
    ''')
