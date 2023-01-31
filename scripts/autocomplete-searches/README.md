autocomplete-searches
=============

The frontend of search autocomplete hits the API (at most) for every keystroke, after the third character has been entered.

This processes raw search term from Google BigQuery to create a list of what those would be, and encodes them, for Gatling to use.


Dependencies
------------

This needs `addressable`, for the url encoding.


How to use it
-------------

### Getting raw data

The raw data may include personally identifiable information, so should not be checked into git.
You also (probably) want fresh data that include up to date searches
Therefore, you should get a set of data from recent real searches by using the following SQL to get a sample of data 
from Google BigQuery

```
SELECT
  LOWER(hits.page.searchKeyword) as search_term
FROM
  `govuk-bigquery-analytics.87773428.ga_sessions_*`,
  UNNEST(hits) AS hits
WHERE
  _table_suffix BETWEEN FORMAT_DATE('%Y%m%d',DATE_SUB(CURRENT_DATE(), INTERVAL 1 DAY))
                AND FORMAT_DATE('%Y%m%d',DATE_SUB(CURRENT_DATE(), INTERVAL 1 DAY))
  AND hits.page.searchKeyword IS NOT NULL
```

Download the result.
You can then use it as input to this script, to produce the output, as follows
`ruby generate.rb ~/Downloads/raw_search_terms.csv`