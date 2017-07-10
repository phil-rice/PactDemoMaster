#!/bin/bash

# #####################################
#	Delete All Existing PACT files
# #####################################
curl -X DELETE \
	https://hcl.pact.dius.com.au/pacts/provider/Provider/consumer/CustomeIos/version/1.0.0 \
	-H 'authorization: Basic aGxIUGZjdzl4TGxLT2g2ZDMxWkZlTDM3dE14azFtVzpFZWlRQTN4b1RZM2ZFZmpmOTRPMzZ6YVdYc3JuYUda'

curl -X DELETE \
	https://hcl.pact.dius.com.au/pacts/provider/Provider/consumer/CustomeAndroid/version/1.0.0 \
	-H 'authorization: Basic aGxIUGZjdzl4TGxLT2g2ZDMxWkZlTDM3dE14azFtVzpFZWlRQTN4b1RZM2ZFZmpmOTRPMzZ6YVdYc3JuYUda'


	
# #####################################
#	Update PACT files
# #####################################
	
curl -X PUT \
  https://hcl.pact.dius.com.au/pacts/provider/Provider/consumer/CustomeIos/version/1.0.0 \
  -H 'authorization: Basic aGxIUGZjdzl4TGxLT2g2ZDMxWkZlTDM3dE14azFtVzpFZWlRQTN4b1RZM2ZFZmpmOTRPMzZ6YVdYc3JuYUda' \
  -H 'cache-control: no-cache' \
  -H 'content-type: application/json' \
  -d @./target/pacts/CustomeIos_Provider.json


curl -X PUT \
    https://hcl.pact.dius.com.au/pacts/provider/Provider/consumer/CustomeAndroid/version/1.0.0 \
    -H 'authorization: Basic aGxIUGZjdzl4TGxLT2g2ZDMxWkZlTDM3dE14azFtVzpFZWlRQTN4b1RZM2ZFZmpmOTRPMzZ6YVdYc3JuYUda' \
    -H 'cache-control: no-cache' \
    -H 'content-type: application/json' \
    -d @./target/pacts/CustomeAndroid_Provider.json
