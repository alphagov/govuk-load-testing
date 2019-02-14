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

3. Copy or symlink the `src/test/scala/govuk` directory in this repository to `$GATLING_HOME/user-files/simulations/`

### Setting up the symlink
NOTE: Don't copy the directory if you set up the symlink as this may throw errors
```
$ ln -s /path/to/target /path/to/gatling/folder/
```

A symlink is required for Gatling to access the simulation files in this repo:
```
$ ln -s /Users/username/govuk/govuk-load-testing/src/test/resources/test-data /Users/username/gatling/user-files/resources/
```

Check the symlink has been set up correctly:
```
$ ls -la /Users/username/gatling/user-files/simulation
lrwxr-xr-x  1 username  staff  65 14 Feb 13:54 /Users/username/gatling/user-files/simulation -> /Users/username/govuk/govuk-load-testing/src/test/scala/govuk/

```

In `$GATLING_HOME/user-files/simulations/test-plans` delete any duplicate .scala files as they should now come from the symlink.


How to run a test plan
----------------------

We use Java properties to pass options to the script which we don't want to hard-code.  These can be set using the `JAVA_OPTS` environment variable:

```
> export JAVA_OPTS="-Dkey1=value1 -Dkey2=value2 ..."
```

The following property is required:

- `baseUrl`, prepended to all requests, at the least it should include the scheme and domain name

The following properties are necessary depending on the environment or scenario:

- `username`, the HTTP basic auth username
- `password`, the HTTP basic auth password

The property `signonUrl` and environment variables `USERNAME` and `PASSWORD` are required for scenarios authenticating with a signon application.

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


### On AWS

First make sure you are [logged in to AWS and have switched your role to production](https://docs.publishing.service.gov.uk/manual/aws-console-access.html#header).

#### Launching an instance

To launch a Gatling instance, follow the instructions below. You may find there is already an existing Gatling instance available in EC2,  in which case you can just start it without having to create a new one.

1. Go to "AWS Marketplace Solutions" from the services list (you may need to switch region to N. Virginia).
1. Select "Manage" on Gatling FrontLine, click on "Actions" and then "Launch new instance."
1. Set the region to "EU (Ireland)" and click "Continue to Launch".
1. Select your desired EC2 Instance Type (recommended `t2.2xlarge`).
1. Choose the `vpc-07069e8dd026cc725` VPC, `subnet-00103e6927dd1fb36` subnet and `govuk_gatling_access` security group.
1. Click "Launch" and you will be given a link to the EC2 instance.
1. Find the Public DNS name for that instance and go to it in your browser. It should provide you with a wizard to complete the set up of the instance.
1. You may find it useful to rename the instance in AWS to `gatling` so you can find it again easily.

> **Note:** Gatling FrontLine instances cost $9/hour, so it's important to switch off the instance while it's not in use.

#### Loading a plan

Once you have a Gatling FrontLine EC2 instance, you can use it to load a plan.

1. Click on "Create" at the top left to create your plan.
1. Give it a name, and choose the classname that corresponds to the test plan class you want. For example, `govuk.Frontend` for [Frontend.scala](src/test/resources/scala/Frontend.scala). See below for the list of plans.
1. Click next and choose "Build from sources" (should be the default option). Enter `git clone https://github.com/alphagov/govuk-load-testing.git` into the repository command box, choose `SBT Project` in the build command drop down and click next.
1. If you're only using this one instance, choose the "Local" pool with a weight of 100%.
1. Click on "More options" and here you can enter the `JAVA_OPTS` value in the second box. For example, to use 100 workers, you would enter `-Dworkers=100`. Please see the other parts of this documentation for all the parameters.
1. Now you can click save and your plan should appear in the list. Click the play button to build it.
1. Once your plan has built, it will go ahead and run it for you. You can click on the icon of a graph to view live updating results from the load test.

List of test plans
------------------

Tets plans live in the `src/test/scala` directory.  Their data files live in the `src/test/resources` directory.

### govuk.Frontend

**Data files:** paths.csv

**Properties:** `factor` (default: 1), the multiplier to apply to the amount of desired traffic

For an entry `base_path,hits`, each worker requests `base_path` `ceil(hits * factor / workers)` times, with no delay between requests.  Each worker proceeds through the csv in order.

If you are having difficulty running the entire test plan on a single machine within your desired duration, try splitting up the data file and [running multiple instances of Gatling simultaneously on different machines](#across-multiple-machines).

### govuk.WhitehallPublishing

**Requires:** `signonUrl` property. `USERNAME` and `PASSWORD` environment variables.

**Optional:** `schedule` property will schedule publication.  
This value must be a timestamp in the format `yyyy-MM-ddTHH:mm` (eg. `2019-01-10T17:30`).
The value must be at least 15 minutes before the test run as Whitehall enforces this rule for scheduled publishing.


Example:
```
$ export JAVA_OPTS="-DbaseUrl=https://whitehall-admin.staging.publishing.service.gov.uk/ -Dworkers=1 -DsignonUrl=https://signon.staging.publishing.service.gov.uk/"
```

Steps:

- Authenticates with signon
- Drafts a publication
- Attaches an HTML attachment
- Tags to taxonomy
- Force publishes or force schedules


### govuk.WhitehallPublishingCollections

**Requires:** `signonUrl` property. `USERNAME` and `PASSWORD` environment variables.

**Optional:** `documentSearches` property - How many document searches to make when adding to the collection.

Steps:

- Authenticates with signon
- Drafts a collection
- Searches for Gatling Test publications
- Adds search results to collection
- Tags to taxonomy
- Force publishes


Troubleshooting
---------------

### My requests are being rate limited

Set the `rateLimitToken` property, and make sure the token is valid for the environment you're testing.  These tokens live in the encrypted hieradata in [govuk-secrets](https://github.com/alphagov/govuk-secrets).

### Authentication issues
- Ensure the right permissions have been set for your test user
- Disable 2FA

You may need to use a different variable if `USERNAME` does not keep the user details you have set. Update this in [signon.scala](https://github.com/alphagov/govuk-load-testing/blob/master/src/test/scala/govuk/Signon.scala#L30
)

NOTE: `USERNAME` needs to be an email address

### No such file or directory
If you see an error such as:
```
15:16:48.524 [ERROR] i.g.a.Gatling$ - Run crashed
java.io.FileNotFoundException: test-data/lorem-ipsum.txt (No such file or directory)
```

Ensure you run gatling in this repo, `govuk-load-testing`:
```
export GATLING_HOME=/Users/username/gatling/
$GATLING_HOME/bin/gatling.sh
```
