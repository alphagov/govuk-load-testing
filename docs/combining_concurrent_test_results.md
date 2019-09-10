# Running multiple concurrent tests

To generate extremely high levels of load it may be necessary to run concurrent tests via different machines. If these are both started at the same time with the same parameters, for example with 50,000 workers, the results can then be combined to provide results for a single 100,000 worker test.

## 1. Run concurrent load tests

Co-ordinate simultaneous load tests with desired parameters, including a `-nr` for `no reports`. Each Gatling test produces a `simulation.log` file as it runs, which is used to generate the final html report pages.

In this case each `simulation.log` is only part of the picture, so we do not need to yet generate these reports. Following the load-test, only the log will be present in the `/usr/local/bin/gatling/results/<your_load_test_timestamp>` directory.

## 2. Collect simulation logs

Download or `scp` all required `simulation.log` files to a `gatling/results/<your_combined_load_test>` directory on a machine with Gatling installed.

### 3. Generate combined Reports

From the Gatling directory, run `./gatling.sh -ro your_combined_load_test`. This will generate `reports only` from each simulation log in the given directory.
