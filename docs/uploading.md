# Uploading results

To make sure that load testing results are available again in the future, we
can upload the results to an S3 bucket. This is especially useful if we need to
reprovision the Gatling machine which means we lose the results kept on the
machine itself.

## 1. Find a good place to upload the results

The S3 bucket to store the results in Staging is
[`gatling-results-staging`][bucket]. In the future we might have buckets in
other environments, but for now, load testing has only been happening against
staging.

[bucket]: https://s3.console.aws.amazon.com/s3/buckets/gatling-results-staging/?region=eu-west-2&tab=overview

To make sure results are easy to find in the future, make sure to follow the
existing directory structure:

```erb
/<%= test_plan %>/<%= date %>-<%= number_of_workers %>-workers-%<= number_of_seconds %>-seconds
```

If it makes sense to deviate from the number of workers and number of seconds
format (if different parameters are used) then that's fine.

An example of a good upload path:
`/dynamic-lists/2019-09-05-80000-workers-600-seconds`

## 2. Get the results off the Gatling machine

After the load test has finished, Gatling will tell you where to find the
results. Something like this:

```sh
Reports generated in 285s.
Please open the following file: /usr/local/bin/gatling/results/dynamiclists-20190906091551610/index.html
gatling@ec2-staging-govuk-gatling-ip-10-12-4-83:/usr/local/bin/gatling$
```

You can use `scp` to copy that onto your own computer ready for uploading:

```sh
$ scp -r 10.12.4.83.staging-aws:/usr/local/bin/gatling/results/dynamiclists-20190906091551610 ~/Downloads
```

## 3. Upload Gatling HTML files

Once you have a place to upload the results, all you need to do is upload all
the HTML, CSS and JS files that are part of a Gatling results directory.

You can do this using the S3 web interface, by clicking the "Upload" button
once you're in the right directory. You can leave all the upload settings as
the defaults.

## 4. Test the results can be seen

When the upload has finished, it should be possible to view the results by
going to:

https://gatling-results-staging.s3-eu-west-1.amazonaws.com/<directory>/index.html

**Note:** you will need to be either in the office or connected to the VPN to
see the results.
