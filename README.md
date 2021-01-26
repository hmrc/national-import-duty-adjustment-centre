# national-import-duty-adjustment-centre

Backend microservice supporting the submission of NIDAC claims from the [national-import-duty-adjustment-centre-frontend](https://github.com/hmrc/national-import-duty-adjustment-centre-frontend) microservice

## Local Setup

1. Checkout this repo
1. Start dependent services with [service-manager](https://github.com/hmrc/service-manager): `sm --start NIDAC_ALL`
1. Stop the `service-manager` owned version of the service: `sm --stop NATIONAL_IMPORT_DUTY_ADJUSTMENT_CENTRE`
1. Start the service: `sbt run`

Ensure you get a JSON response from `curl -i http://localhost:8491/`

## API

| Method | Url | Required Headers | RequestBody | Response | 
| --- | --- | --- | --- | --- |
| POST | /create-claim | x-correlation-id | JSON - [request model](./app/uk/gov/hmrc/nationalimportdutyadjustmentcentre/models/CreateClaimRequest.scala) | JSON - [response model](./app/uk/gov/hmrc/nationalimportdutyadjustmentcentre/models/CreateClaimResponse.scala) |

### License

This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html").
