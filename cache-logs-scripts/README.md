cache-logs-scripts
==================

Processes the `lb-access` log files from `cache-[123]` to find:

* The busiest minute overall, and how many times each path was requested in that minute
* The busiest minute for each path, and how many times the path was requested in that minute

This information can be used to produce a test data file for the `govuk.Frontend` test plan: perhaps you want to re-run the busiest minute, but with traffic increased by a factor of 10.

This only considers `GET` requests, removes querystrings, and doesn't count requests to `/government/uploads`.


How to use it
-------------

Before you start, make sure you have about 100GB free disk space.

Firstly, download the log files:

```
$ mkdir cache-1
$ mkdir cache-2
$ mkdir cache-3
$ scp cache-1.production:/var/log/nginx/lb-access.log.{1,{2..28}.gz} cache-1
$ scp cache-2.production:/var/log/nginx/lb-access.log.{1,{2..28}.gz} cache-2
$ scp cache-3.production:/var/log/nginx/lb-access.log.{1,{2..28}.gz} cache-3
```

Then run `cache-logs.sh`, which will take a few hours so don't sit around waiting.  This first decompresses all the log files, which is why you need all the free space.

It produces:

* For each `lb-access` log file, a csv file summarising the `GET` requests in that file.  These are sorted by path and then by timestamp.
* An aggregate of all the csvs for each of `cache-1`, `cache-2`, and `cache-3`, called `cache-[123]/cache-logs.csv`
* An overall aggregate, called `cache-logs.csv`
* The busiest "bucket" (minute) overall, and all the paths requested in that minute more than 2 times, called `top-bucket.csv`
* The bustest "bucket" for each path, and how many times each path was requested, called `top-bucket-foreach-path.csv`

The bucket size can be changed by editing `aggregate-single.rb`, deleting all the generated files, and re-running `cache-logs.sh`.


Results
-------

The outputs look like this:

```
$ head -n5 cache-1/lb-access.log.1.csv
"/","2018-10-18 05:43","1"
"/","2018-10-18 05:44","10"
"/","2018-10-18 05:45","3"
"/","2018-10-18 05:46","2"
"/","2018-10-18 05:47","3"

$ head -n5 cache-1/cache-logs.csv
"%20:%20World%20:%20Coming%20to%20the%20UK","2018-10-02 07:08","1"
"/!!Secure%20Data/SFR/2014/KS4/Revised/Working/GCSE_revised_master%20file_National_Tables_Working_v.14.xlsx","2018-10-02 11:47","1"
"/!!Secure%20Data/SFR/2014/KS4/Revised/Working/GCSE_revised_master%20file_National_Tables_Working_v.14.xlsx","2018-10-04 14:20","1"
"/!!Secure%20Data/SFR/2015/KS4/Provisional/Working/SFRxx_2015_National_Tables.xlsx","2018-10-02 11:47","2"
"/!","2018-10-01 08:54","1"

$ head -n5 cache-logs.csv
"%20:%20...%20:%20Contact%20HM%20Revenue%20&%20Customs","2018-10-02 07:08","1"
"%20:%20World%20:%20Coming%20to%20the%20UK","2018-10-02 07:08","1"
"/!!Secure%20Data/SFR/2010/KS5/Jan%202011/Skeleton%20Tables%20and%20Early%20Access%20list/Skeleton_Tables_14_15.xls","2018-10-04 15:39","2"
"/!!Secure%20Data/SFR/2014/KS4/Revised/Working/GCSE_revised_master%20file_National_Tables_Working_v.14.xlsx","2018-10-02 11:47","1"
"/!!Secure%20Data/SFR/2014/KS4/Revised/Working/GCSE_revised_master%20file_National_Tables_Working_v.14.xlsx","2018-10-02 11:47","1"

$ head -n5 top-bucket.csv
"/__canary__","2018-10-17 15:53","3"
"/aaib-reports.atom","2018-10-17 15:53","23"
"/aaib-reports.json","2018-10-17 15:53","3"
"/adult-dependants-grant","2018-10-17 15:53","15"
"/api/search.json","2018-10-17 15:53","82"

$ head -n5 top-bucket-foreach-path.csv
"%20:%20...%20:%20Contact%20HM%20Revenue%20&%20Customs","2018-10-02 07:08","1"
"%20:%20World%20:%20Coming%20to%20the%20UK","2018-10-02 07:08","1"
"/!!Secure%20Data/SFR/2010/KS5/Jan%202011/Skeleton%20Tables%20and%20Early%20Access%20list/Skeleton_Tables_14_15.xls","2018-10-04 15:39","2"
"/!!Secure%20Data/SFR/2014/KS4/Revised/Working/GCSE_revised_master%20file_National_Tables_Working_v.14.xlsx","2018-10-02 11:47","2"
"/!!Secure%20Data/SFR/2015/KS4/Provisional/Working/SFRxx_2015_National_Tables.xlsx","2018-10-02 11:47","2"
```

Some paths get requested quite a few times:

```
$ grep '"/"' top-bucket-foreach-path.csv
"/","2018-10-11 03:34","455"
```

There are a lot of unique paths:

```
$ wc -l top*csv
 4178052 top-bucket-foreach-path.csv
     108 top-bucket.csv
 4178160 total
```
