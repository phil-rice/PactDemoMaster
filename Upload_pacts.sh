#!/bin/bash

export username=$1
export password=$2

# #####################################
#	Delete All Existing PACT files
# #####################################
curl -X DELETE \
	https://hcl.pact.dius.com.au/pacts/provider/Provider/consumer/CustomerIos/version/1.0.0 \
	-u $username:$password

curl -X DELETE \
	https://hcl.pact.dius.com.au/pacts/provider/Provider/consumer/CustomerAndroid/version/1.0.0 \
	-u $username:$password


	
# #####################################
#	Update PACT files
# #####################################



curl -X PUT \
  https://hcl.pact.dius.com.au/pacts/provider/RawProvider/consumer/Android/version/1.0.0 \
  -H 'cache-control: no-cache' \
  -H 'content-type: application/json' \
  -u $username:$password \
  -d @./target/pacts/Android_RawProvider.json


curl -X PUT \
    https://hcl.pact.dius.com.au/pacts/provider/RawProvider/consumer/Ios/version/1.0.0 \
    -H 'cache-control: no-cache' \
    -H 'content-type: application/json' \
    -u $username:$password \
    -d @./target/pacts/Ios_RawProvider.json

curl -X PUT \
    https://hcl.pact.dius.com.au/pacts/provider/Android/consumer/AkkaActorClient/version/1.0.0 \
    -H 'cache-control: no-cache' \
    -H 'content-type: application/json' \
    -u $username:$password \
    -d @./target/pacts/AkkaActorClient_Android.json


curl -X PUT \
    https://hcl.pact.dius.com.au/pacts/provider/Android/consumer/JavaConsumer/version/1.0.0 \
    -H 'cache-control: no-cache' \
    -H 'content-type: application/json' \
    -u $username:$password \
    -d @./target/pacts/JavaConsumer_Android.json
