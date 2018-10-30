GOV.UK Load Testing
===================

Test plans for load testing GOV.UK frontend apps using [Gatling](https://gatling.io/).

In this README, `$GATLING_HOME` is the directory where Gatling is installed to.

For example, if you download version 3.0.0-RC4 of the Gatling bundle zip and extract it in your `~/Downloads` folder, `$GATLING_HOME` is `~/Downloads/gatling-charts-highcharts-bundle-3.0.0-RC4`

- [Set-up](#set-up)
- [How to run a test plan](#how-to-run-a-test-plan)
- [List of test plans](#list-of-test-plans)
- [Troubleshooting](#troubleshooting)


Set up
------

1. Install a JDK, Gatling needs at least version 8

2. Download and extract Gatling, these test plans are written for version 3

4. Copy or symlink the `test-plans` directory in this repository to `$GATLING_HOME/user-files/simulations/`


How to run a test plan
----------------------

We use Java properties to pass options to the script which we don't want to hard-code.  These can be set using the `JAVA_OPTS` environment variable:

```
> export JAVA_OPTS="-Dkey1=value1 -Dkey2=value2 ..."
```

The following properties are required:

- `baseUrl`, prepended to all requests, at the least it should include the scheme and domain name
- `username`, the HTTP basic auth username
- `password`, the HTTP basic auth password

The following properties are optional:

- `dataDir` (default: "test-data"), the directory to look in for test data files
- `rateLimitToken` (default: no header sent), the value of the `Rate-Limit-Token` header
- `workers` (default: 1), the number of threads making requests
- `ramp` (default: 0), the duration, in seconds, over which the workers are started
- `bust` (default: false), whether to pass a unique cache-busting string with every request or not

These properties can be set using the `JAVA_OPTS` environment variable:

###  On a single machine

Gatling provides a wrapper script to compile and launch test plans in its `user-files` directory:

```
> $GATLING_HOME/bin/gatling.sh
GATLING_HOME is set to /Users/michaelswalker/Downloads/gatling-charts-highcharts-bundle-3.0.0-RC4
Choose a simulation number:
     [0] computerdatabase.BasicSimulation
     [1] computerdatabase.advanced.AdvancedSimulationStep01
     [2] computerdatabase.advanced.AdvancedSimulationStep02
     [3] computerdatabase.advanced.AdvancedSimulationStep03
     [4] computerdatabase.advanced.AdvancedSimulationStep04
     [5] computerdatabase.advanced.AdvancedSimulationStep05
     [6] govuk.Frontend
```

"computerdatabase" is a collection of example test plans for http://computer-database.gatling.io


### Across multiple machines

to do

https://gatling.io/docs/3.0/cookbook/scaling_out/


List of test plans
------------------

Tets plans live in the `test-plans` directory.  Their data files live in the `test-data` directory.

### govuk.Frontend

**Data files:** paths.csv

**Properties:** `factor` (default: 1), the multiplier to apply to the amount of desired traffic

For an entry `base_path,hits`, each worker requests `base_path` `ceil(hits * factor / workers)` times, with no delay between requests.  Each worker proceeds through the csv in order.

If you are having difficulty running the entire test plan on a single machine within your desired duration, try splitting up the data file and [running multiple instances of Gatling simultaneously on different machines](#across-multiple-machines).


Troubleshooting
---------------

### My requests are being rate limited

Set the `rateLimitToken` property, and make sure the token is valid for the environment you're testing.  These tokens live in the encrypted hieradata in [govuk-secrets](https://github.com/alphagov/govuk-secrets).
