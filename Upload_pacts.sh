#!/bin/bash

curl -X PUT \
  https://hcl.pact.dius.com.au/pacts/provider/Provider/consumer/CustomeIos/version/1.0.0 \
  -H 'authorization: Basic aGxIUGZjdzl4TGxLT2g2ZDMxWkZlTDM3dE14azFtVzpFZWlRQTN4b1RZM2ZFZmpmOTRPMzZ6YVdYc3JuYUda' \
  -H 'cache-control: no-cache' \
  -H 'content-type: application/json' \
  -H 'postman-token: daba02ed-2c00-befe-cdc5-4917366286c9' \
  -d @/home/travis/build/phil-rice/PactDemoMaster/target/pacts/CustomeIos_Provider.json


curl -X PUT \
    https://hcl.pact.dius.com.au/pacts/provider/Provider/consumer/CustomeAndroid/version/1.0.0 \
    -H 'authorization: Basic aGxIUGZjdzl4TGxLT2g2ZDMxWkZlTDM3dE14azFtVzpFZWlRQTN4b1RZM2ZFZmpmOTRPMzZ6YVdYc3JuYUda' \
    -H 'cache-control: no-cache' \
    -H 'content-type: application/json' \
    -H 'postman-token: daba02ed-2c00-befe-cdc5-4917366286c9' \
    -d @/home/travis/build/phil-rice/PactDemoMaster/target/pacts/CustomeAndroid_Provider.json
