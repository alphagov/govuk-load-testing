GOV.UK Load Testing
===================

Test plans for load testing GOV.UK frontend apps using [Gatling](https://gatling.io/).

### Table of Contents
1. [Terminology](#terminology)
2. [Methods](#methods)  
  2.1 [Virtual Machine](#virtual-machine)  
  2.2 [Rental of an enterprise Gatling Instance via AWS Marketplace](#aws-gatling)
3. [Configuration Options](#configuration)
4. [Simulation Plans](#plans)
5. [Troubleshooting](#troubleshooting)


# <a name="terminology">1. Terminology </a>

1. `$GATLING_HOME`: In this README, `$GATLING_HOME` is the directory where Gatling is installed to.
    For example, if you download version 3.0.0-RC4 of the Gatling bundle zip and extract it in your `~/Downloads` folder, `$GATLING_HOME` is `~/Downloads/gatling-charts-highcharts-bundle-3.0.0-RC4`

2. simulation plan: a set of scenarios, where each scenario represents how a user will interact with the website.

# <a name="methods">2. Methods</a>

There are 2 main methods to install and run Gatling:
  1. install and run Gatling on a virtual machine which can reside on your laptop or a AWS instance
  2. rent an enterprise Gatling instance via AWS Marketplace

## <a name="virtual-machine">2.1 Virtual Machine</a>

### Installation Steps

The installation steps are:
1. Install a JDK, Gatling needs at least version 8
1. Clone this repository into your `~/govuk/` directory
1. [Download](https://gatling.io/download/) and extract Gatling, these test plans are written for version 3
1. Rename and move the extracted Gatling directory to `~/govuk/gatling` to make it available in `/var/govuk/gatling` in your Virtual Machine
1. Symlink the `src/test/resources/test-data` directory of this repository to `$GATLING_HOME/user-files/resources/`
  ```
  $ ln -s ~/govuk/govuk-load-testing/src/test/resources/test-data ~/govuk/gatling/user-files/resources/
  ```
1. Set the needed environment variables:
  - `$ export 'JAVA_OPTS=<required-options>'` (see [Configuration Options](#configuration))
  - `$ export 'GATLING_USERNAME=<test-user-email>'`
  - `$ export 'GATLING_PASSWORD=<test-user-password>'`

    **Note:** `GATLING_USERNAME` and `GATLING_PASSWORD` are only needed for test plans using Signon.
    These are the credentials for the test user that has been set up in Signon in the environment it's going to test in, e.g. `staging`.

### Running a Plan

In order to run a simulation plan, Gatling provides a wrapper script to compile and launch test plans in its `user-files` directory.

You can run Gatling by:
1. running the following command:
  ```
  $ $GATLING_HOME/bin/gatling.sh
  ```
  **Note:** This command must be run from within `~/govuk/govuk-load-testing` (or `/var/govuk/govuk-load-testing` if you are using the Virtual Machine).

2. selecting the simulation plan number that you wish to run.
  ```
  Choose a simulation number:
      [0] computerdatabase.BasicSimulation
      [1] computerdatabase.advanced.AdvancedSimulationStep01
      [2] computerdatabase.advanced.AdvancedSimulationStep02
      [3] computerdatabase.advanced.AdvancedSimulationStep03
      [4] computerdatabase.advanced.AdvancedSimulationStep04
      [5] computerdatabase.advanced.AdvancedSimulationStep05
      [6] govuk.Frontend
  ```
  A description of relevant simulation plans available for the gov.uk website
  is available [here](#plans)


## <a name="aws-gatling">2.2 Rental of an enterprise Gatling Instance via AWS Marketplace</a>

**Note:** Gatling FrontLine instances cost $9/hour, so it's important to switch off the instance while it's not in use.

### Launching a Gatling instance

To launch a Gatling instance, follow the instructions below. You may find there is already an existing Gatling instance available in EC2,  in which case you can just start it without having to create a new one.

1. First make sure you are [logged in to AWS and have switched your role to production](https://docs.publishing.service.gov.uk/manual/aws-console-access.html#header)
1. Go to "AWS Marketplace Solutions" from the services list (you may need to switch region to N. Virginia).
1. Select "Manage" on Gatling FrontLine, click on "Actions" and then "Launch new instance."
1. Set the region to "EU (Ireland)" and click "Continue to Launch".
1. Select your desired EC2 Instance Type (recommended `t2.2xlarge`).
1. Choose the `vpc-07069e8dd026cc725` VPC, `subnet-00103e6927dd1fb36` subnet and `govuk_gatling_access` security group.
1. Click "Launch" and you will be given a link to the EC2 instance.
1. Find the Public DNS name for that instance and go to it in your browser. It should provide you with a wizard to complete the set up of the instance.
1. You may find it useful to rename the instance in AWS to `gatling` so you can find it again easily.

#### Running a Plan

Once you have a Gatling FrontLine EC2 instance, you can use it to load a plan.

1. Click on "Create" at the top left to create your plan.
1. Give it a name, and choose the classname that corresponds to the test plan class you want. For example, `govuk.Frontend` for [Frontend.scala](src/test/resources/scala/Frontend.scala). See this [section](#plans) for a list of plans.
1. Click next and choose "Build from sources" (should be the default option). Enter `git clone https://github.com/alphagov/govuk-load-testing.git` into the repository command box, choose `SBT Project` in the build command drop down and click next.
1. If you're only using this one instance, choose the "Local" pool with a weight of 100%.
1. Click on "More options" and here you can enter the `JAVA_OPTS` value in the second box. For example, to use 100 workers, you would enter `-Dworkers=100`. Please see [section](#configuration) for further configuration options.
1. Now you can click save and your plan should appear in the list. Click the play button to build it.
1. Once your plan has built, it will go ahead and run it for you. You can click on the icon of a graph to view live updating results from the load test.


## <a name="configuration">3. Configuration Options</a>

We use Java properties to pass options to the script which we don't want to hard-code.  These can be set using the `JAVA_OPTS` environment variable:

```
$ export JAVA_OPTS="-Dkey1=value1 -Dkey2=value2 ..."
```

The following property is required:

- `baseUrl`, prepended to all requests, at the least it should include the scheme and domain name

The following properties are necessary depending on the environment or scenario:

- `username`, the HTTP basic auth username
- `password`, the HTTP basic auth password

The property `signonUrl` and environment variables `GATLING_USERNAME` and `GATLING_PASSWORD` are required for scenarios authenticating with a signon application.

The following properties are optional:

- `dataDir` (default: "test-data"), the directory to look in for test data files
- `rateLimitToken` (default: no header sent), the value of the `Rate-Limit-Token` header
- `workers` (default: 1), the number of threads making requests
- `ramp` (default: 0), the duration, in seconds, over which the workers are started
- `bust` (default: false), whether to pass a unique cache-busting string with every request or not

## <a name="plans">4. Simulation Plans </a>

The simulation plans for gov.uk are located in the `src/test/scala` directory of this repository
while their data files live in the `src/test/resources` directory.

1. **govuk.Frontend**

    **Data files:** paths.csv

    **Properties:** `factor` (default: 1), the multiplier to apply to the amount of desired traffic

    For an entry `base_path,hits`, each worker requests `base_path` `ceil(hits * factor / workers)` times, with no delay between requests.  Each worker proceeds through the csv in order.

    If you are having difficulty running the entire test plan on a single machine within your desired duration, try splitting up the data file and running multiple instances of Gatling simultaneously on different machines.

2. **govuk.WhitehallPublishing**

    **Requires:** `signonUrl` property. `GATLING_USERNAME` and `GATLING_PASSWORD` environment variables.

    **Optional:** `schedule` property will schedule publication.  
    This value must be a timestamp in the format `yyyy-MM-ddTHH:mm` (eg. `2019-01-10T17:30`).
    The value must be at least 15 minutes before the test run as Whitehall enforces this rule for scheduled publishing.

    **Optional:** `maxTryAttempt` property will set how many times each step will be attempting before logging as a failure.
    This is to stimulate a user refreshing or reattempting after receiving an error response.
    If nothing is passed, it will default to 1.

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


3. **govuk.WhitehallPublishingCollections**

    **Requires:** `signonUrl` property. `GATLING_USERNAME` and `GATLING_PASSWORD` environment variables.

    **Optional:** `documentSearches` property - How many document searches to make when adding to the collection.

    Steps:

    - Authenticates with signon
    - Drafts a collection
    - Searches for Gatling Test publications
    - Adds search results to collection
    - Tags to taxonomy
    - Force publishes


## <a name="troubleshooting">5. Troubleshooting</a>

1. **My requests are being rate limited**

    Set the `rateLimitToken` property, and make sure the token is valid for the environment you're testing.  These tokens live in the encrypted hieradata in [govuk-secrets](https://github.com/alphagov/govuk-secrets).

2. **Authentication issues**
    - Ensure the right permissions have been set for your test user
    - Disable 2FA
    - `GATLING_USERNAME` needs to be an email address

3. **No such file or directory**

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

4. **Exit scenario**

    This may be useful to remove workers that fail at any step:
    ```
    .exec{
      //CODE
    }.exitHereIfFailed
    ```

5. **Output html**

    ```
    .exec(session => {
      val response = session("BODY").as[String]
      println(s"Response body: \n$response")
      session
    })
    ```
