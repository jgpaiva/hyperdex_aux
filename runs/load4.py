#!/usr/bin/env python

import json
import hyperclient
import os
import time
import sys


Repeats = 100
coord_host = str(sys.argv[1])
coord_port = 1982

def loaddata(c):
    data = []
    asyncs = []
    count = dict()
    print "reading file"
    with open('data.dat') as f:
        for i, line in enumerate(f):
            s_line = line.split(',')
            factual_id=str(s_line[0].strip())
            name     = str(s_line[1].strip())
            category = str(s_line[2].strip())
            lowest_pr= str(int(s_line[3].strip()))
            # note that lowest_pr is now a string
            highest_pr=int(s_line[4].strip())
            ratings  = float(s_line[5].strip())
            status   = str(s_line[6].strip())
            stars    = int(s_line[7].strip())
            tel      = str(s_line[8].strip())
            region   = str(s_line[9].strip())
            locality = str(s_line[10].strip())
            postcode = str(s_line[11].strip())
            longitude= float(s_line[12].strip())
            latitude = float(s_line[13].strip())
            address  = str(s_line[14].strip())

            s = count.get(region)
            if s is None:
                count[region] = 0
                s = 0

            s += 1
            count[region] = s

            if s <= 50:
                async = c.async_put('hotels', factual_id+'-0', {'name':name+'-0', 'category':category+'-0', 'lowest_price':lowest_pr+'-0', 
                    'highest_price':highest_pr, 'ratings':ratings, 'status':status+'-0', 'stars':stars, 'tel':tel+'-0', 'region':region+'-0', 
                    'locality':locality+'-0', 'postcode':region+'-0', 'longitude':longitude, 'latitude':latitude, 'address':address+'-0'})

                asyncs.append(async)
                #update_progress(i,98722)

                for j in xrange(1,Repeats):
                    async = c.async_put('hotels', factual_id+'-'+str(j), {'name':name+'-'+str(j), 'category':category+'-'+str(j), 
                        'lowest_price':lowest_pr+'-0', 'highest_price':highest_pr, 'ratings':ratings, 'status':status+'-'+str(j), 'stars':stars, 
                        'tel':tel+'-'+str(j), 'region':region+'-'+str(j), 'locality':locality+'-'+str(j), 'postcode':region+'-'+str(j), 
                        'longitude':longitude, 'latitude':latitude, 'address':address+'-'+str(j)})
                    asyncs.append(async)

                if len(asyncs) > 50000:
                    for i, async in enumerate(asyncs):
                        async.wait()
                    asyncs = []

    print " "
    print "asyncs"
    for i, async in enumerate(asyncs):
        async.wait()
        #update_progress(i,len(asyncs))
    print " "

def update_progress(p,total):
    p+=1
    progress = int(100* (p/float(total)))
    toprint=progress/10
    if p < total:
        sys.stdout.write('\r[{0}] {1}% of {2}'.format('#'*toprint+" "*(10-toprint), progress, total))
        sys.stdout.flush()
    else:
        sys.stdout.write('\r[{0}] {1}'.format('#'*10, "DONE        "))
        sys.stdout.flush()

if __name__ == "__main__":
    c = hyperclient.Client(coord_host, coord_port)
    loaddata(c)
