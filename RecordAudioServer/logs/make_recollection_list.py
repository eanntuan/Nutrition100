#! /usr/bin/env python
uttids_to_recollect = []
fp = open('uttids_to_recollect.txt', 'r')
for line in fp:
  line = line.rstrip()
  uttids_to_recollect.append(line)
fp.close()
recollection_lines = []
fp = open('Flickr8k_numbered_shuffled.txt', 'r')
for line in fp:
  tokens = line.rstrip().split()
  uttid = tokens[0]
  if uttid in uttids_to_recollect:
    recollection_lines.append(line)
fp.close()
fp = open('Flickr8k_numbered_shuffled_recollect.txt', 'w')
for line in recollection_lines:
  fp.write(line)
fp.close()