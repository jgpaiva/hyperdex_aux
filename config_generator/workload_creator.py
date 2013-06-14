#!/usr/bin/env python


def getP(lst):
    total = 0.0
    for (n,count) in lst:
        total += count
    ret = []
    for (n,count) in lst:
        ret.append((n,count/total))
    return ret

def combine(lst1,lst2):
    ret = []
    for (n1,p1) in lst1:
        for (n2,p2) in lst2:
            if n1 != "":
                ret.append((n1+" "+n2,p1*p2*100))
            else:
                ret.append((n2,p1*p2*100))
    return ret



local = [("region",1), ("city",100), ("zip",50)]
q = [("price",20),("rating",20),("price rating",20),("price category",1),("rating category",1),("stars rating",2),("stars price",2)]


local = getP(local)
q = getP(q)

final = combine(local,q)

print "0.0 10.0 90.0"
print "M 50.0 factual_id price"
print "M 50.0 factual_id rating"
for (name,p) in final:
    print "S", p, name
