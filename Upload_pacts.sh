#!/bin/bash

export username=$1
export password=$2

# #####################################
#	Delete All Existing PACT files
# #####################################
curl -X DELETE \
	https://hcl.pact.dius.com.au/pacts/provider/Provider/consumer/CustomeIos/version/1.0.0 \
	-u $username:$password

curl -X DELETE \
	https://hcl.pact.dius.com.au/pacts/provider/Provider/consumer/CustomeAndroid/version/1.0.0 \
	-u $username:$password


	
# #####################################
#	Update PACT files
# #####################################
	
curl -X PUT \
  https://hcl.pact.dius.com.au/pacts/provider/Provider/consumer/CustomeIos/version/1.0.0 \
  -H 'cache-control: no-cache' \
  -H 'content-type: application/json' \
  -u $username:$password \
  -d @./target/pacts/CustomeIos_Provider.json


curl -X PUT \
    https://hcl.pact.dius.com.au/pacts/provider/Provider/consumer/CustomeAndroid/version/1.0.0 \
    -H 'cache-control: no-cache' \
    -H 'content-type: application/json' \
    -u $username:$password \
    -d @./target/pacts/CustomeAndroid_Provider.json
