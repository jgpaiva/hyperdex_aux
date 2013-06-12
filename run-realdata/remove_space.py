#!/usr/bin/python
import hyperclient
import sys

c = hyperclient.Client(str(sys.argv[1]), 1982)
c.rm_space('hotels')
