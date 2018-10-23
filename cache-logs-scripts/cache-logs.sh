#!/usr/bin/env bash

echo -e "decompressing logs..."
gunzip cache-*/*.gz

echo -e "\nextracting GET requests..."
for f in cache-*/*; do
  ruby aggregate-single.rb $f.csv $f
  gzip $f
done

# top-bucket-foreach-path.rb assumes that the CSV is grouped by path
# name.  Rather than `cat`ing all the files together and then
# `sort`ing them, `sort` each file individually and then merge the
# sorted files.  Merging sorted files takes linear time, so we get:
#
# O([max size of individual file] * log [max size of individual file] + [combined size of all files]) vs
# O([combined size of all files] * log [combined size of all files] + [combined size of all files])

echo -e "\nmerging logs..."
for f in cache-*/*.csv; do
  sort -o $f.sorted $f
  mv $f.sorted $f
done
for d in cache-*; do
  sort -m -o $d/cache-logs.csv $d/*.csv
done
sort -m -o cache-logs.csv cache-*/cache-logs.csv

echo -e "\nfinding top bucket..."
ruby top-bucket.rb top-bucket.csv cache-logs.csv

echo -e "\nfinding top bucket for each path..."
ruby top-bucket-foreach-path.rb top-bucket-foreach-path.csv cache-logs.csv
