
# national-import-duty-adjustment-centre

## Local Setup

1. Checkout this repo
1. Start dependent services with [service-manager](https://github.com/hmrc/service-manager): `sm --start NIDAC_ALL`
1. Stop the `service-manager` owned version of the service: `sm --stop NATIONAL_IMPORT_DUTY_ADJUSTMENT_CENTRE`
1. Start the service: `sbt run`

Ensure you get a 200 response from `curl -i http://localhost:8491/national-import-duty-adjustment-centre/hello-world`

### License

This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html").
