dynamic-lists
=============

Processes a dynamic lists YAML file to produce a list of paths to hit.

This generates all possible paths for a given dynamic list generator, which have
significantly less variations than normal finders.


Dependencies
------------

This needs `activesupport`, for the query string encoding.


How to use it
-------------

Download the dynamic list's question definition. This will be a .yaml with a `questions` hash, [as here][dl].

[dl]: https://github.com/alphagov/finder-frontend/blob/master/lib/checklists/questions.yaml

Then run `generate.rb`, which will write a csv file of paths to `<base_path>_paths.csv` to the project directory.

There are a few options which can be set in environment variables:

- `BASE_PATH` the base path of the dynamic list - REQUIRED
- `CACHE_BUST` for each generated path, will create an extra given amount with random cache bust string. OPTIONAL - defaults to 0

```
$ ruby generate.rb <path to questions definition> <BASE_PATH> <CACHE_BUST>
```

For example:

```
$ ruby generate.rb questions.yaml /dynamic-lists 1
```
```
base_path, hits
/dynamic-lists?c%5B%5D=owns-business,1
/dynamic-lists?c%5B%5D=owns-business&page=1,1
/dynamic-lists?c%5B%5D=owns-business&cache_bust=07d98caa-b0f7-4280-b034-08f3833c9cc8,1
/dynamic-lists?c%5B%5D=does-not-own-business,1
/dynamic-lists?c%5B%5D=does-not-own-business&page=1,1
/dynamic-lists?c%5B%5D=does-not-own-business&cache_bust=0e08f71b-9a3e-3803-1a3b6f7cd9bb,1
```
