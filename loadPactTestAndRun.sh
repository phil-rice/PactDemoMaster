#!/bin/bash

export TRAVIS_SCALA_VERSION=$1
export username=$2
export password=$3

export tempPactFileLocation=$4

# Delete all files from temp location before download fresh one

# Check and delete
if [ -d $tempPactFileLocation ] ;
then 
	rm -fr $tempPactFileLocation
fi

# Create temp location`
echo ''
echo ''
echo 'create folder $tempPactFileLocation'
echo ''
mkdir -p $tempPactFileLocation

# Download pact files in temp location
wget --user=$username --password=$password -O $tempPactFileLocation/android.json https://hcl.pact.dius.com.au/pacts/provider/Provider/consumer/Android/latest

wget --user=$username --password=$password -O $tempPactFileLocation/ios.json https://hcl.pact.dius.com.au/pacts/provider/Provider/consumer/Ios/latest


# SBT BUILD
sbt ++$TRAVIS_SCALA_VERSION clean pactTest