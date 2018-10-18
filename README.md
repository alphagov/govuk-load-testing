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

We use Java properties to pass options to the script which we don't want to hard-code:

- `dataDir` is the directory to look in for data files (optional, defaults to "test-data")
- `baseUrl` is prepended to all requests, at the least it should include the scheme and domain name
- `username` is the HTTP basic auth username
- `password` is the HTTP basic auth password
- `rateLimitToken` is the value of the `Rate-Limit-Token` header (optional, defaults to unset)
- `users` is the number of simulated users (optional, defaults to 1)
- `ramp` is the duration, in seconds,  over which the users are started (optional, defaults to 0)
- `bust` is whether to pass a unique cache-busting string with every request or not (optional, defaults to false)
- `factor` is the multiplier to apply to the amount of desired traffic (optional, defaults to 1)

These properties can be set using the `JAVA_OPTS` environment variable:

```
> export JAVA_OPTS="-DbaseUrl=https://... -Dusername=... -Dpassword=... -DrateLimitToken=..."
```

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

For an entry `base_path,hits`, each worker requests `base_path` `ceil(hits * factor / users)` times, with no delay between requests.  Each worker proceeds through the csv in order.


Troubleshooting
---------------

### My requests are being rate limited

Set the `rateLimitToken` property, and make sure the token is valid for the environment you're testing.  These tokens live in the encrypted hieradata in [govuk-secrets](https://github.com/alphagov/govuk-secrets).
