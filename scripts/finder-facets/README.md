finder-facets
=============

Processes a finder YAML file to produce a list of paths to hit.

Most finders have far too many facet values to hit all combinations, so this script generates a nondeterministic sample.


Dependencies
------------

This needs `activesupport`, for the query string encoding.


How to use it
-------------

Download the finder's definition.  This can be a .json with a `facets` hash, [like specialist-publisher][sp], or a .yml with a `details.facets` hash, [like rummager][r].

[sp]: https://github.com/alphagov/specialist-publisher/blob/master/lib/documents/schemas/aaib_reports.json
[r]: https://github.com/alphagov/rummager/blob/master/config/find-eu-exit-guidance-business.yml

Then run `generate.rb`, which writes to stdout:

```
$ generate.rb <path to finder definition>
```

There are a few options which can be set in environment variables:

- `NUM_PATHS` the number of paths to generate, defaults to 100
- `BASE_PATH` the base path of the finder, defaults to the `base_path` value in the finder definition

For example:

```
$ NUM_PATHS=5 BASE_PATH=/the-aaib-finder generate.rb aaib_reports.json
/the-aaib-finder?aircraft_category%5B%5D=general-aviation-fixed-wing&date_of_occurrence%5Bfrom%5D=43-1-28&date_of_occurrence%5Bto%5D=53-12-21&report_type%5B%5D=safety-study
/the-aaib-finder?aircraft_category%5B%5D=general-aviation-fixed-wing&date_of_occurrence%5Bfrom%5D=99-11-21&date_of_occurrence%5Bto%5D=19-3-12&report_type%5B%5D=special-bulletin
/the-aaib-finder?aircraft_category%5B%5D=commercial-fixed-wing&aircraft_category%5B%5D=sport-aviation-and-balloons&aircraft_category%5B%5D=unmanned-aircraft-systems&aircraft_category%5B%5D=general-aviation-rotorcraft&date_of_occurrence%5Bfrom%5D=63-1-19&date_of_occurrence%5Bto%5D=30-3-8&report_type%5B%5D=formal-report&report_type%5B%5D=safety-study&report_type%5B%5D=annual-safety-report&report_type%5B%5D=correspondence-investigation&report_type%5B%5D=foreign-report&report_type%5B%5D=pre-1997-monthly-report&report_type%5B%5D=special-bulletin&report_type%5B%5D=field-investigation
/the-aaib-finder?aircraft_category%5B%5D=unmanned-aircraft-systems&date_of_occurrence%5Bfrom%5D=61-5-26&date_of_occurrence%5Bto%5D=66-6-20&report_type%5B%5D=safety-study
/the-aaib-finder?aircraft_category%5B%5D=commercial-rotorcraft&aircraft_category%5B%5D=commercial-fixed-wing&aircraft_category%5B%5D=sport-aviation-and-balloons&aircraft_category%5B%5D=general-aviation-fixed-wing&aircraft_category%5B%5D=unmanned-aircraft-systems&aircraft_category%5B%5D=general-aviation-rotorcraft&date_of_occurrence%5Bfrom%5D=75-10-14&date_of_occurrence%5Bto%5D=86-6-23&report_type%5B%5D=annual-safety-report&report_type%5B%5D=field-investigation&report_type%5B%5D=pre-1997-monthly-report&report_type%5B%5D=safety-study&report_type%5B%5D=special-bulletin&report_type%5B%5D=formal-report&report_type%5B%5D=correspondence-investigation
```
