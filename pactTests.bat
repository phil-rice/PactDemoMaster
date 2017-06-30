To make the batch files

sbt  stage

To launch the provider
PactDemoProvider/target/universal/stage/bin/provider.bat

To launch the pact verify
sbt "pact-verify --host localhost --protocol http --port 9000 --source target/pacts"