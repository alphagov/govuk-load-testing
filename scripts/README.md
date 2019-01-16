Test Data Generating Scripts
============================

These generate test data you can use with the `govuk.Frontend` test plan: CSVs of URLs and hit counts.

The scripts are:

- `cache-logs/`, analyses logs from the cache boxes to find (a) the busiest minute, and all the paths hit during that minute; and (b) the busiest minute for each path.
- `finder-facets/`, produces a test plan to hit combinations of facet values, given the finder YAML file.

See the README.md in each script directory for usage information.
