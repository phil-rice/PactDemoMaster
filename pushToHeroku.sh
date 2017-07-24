#!/bin/sh

setup_git() {
  git config --global user.email "travis@pactDemo.com"
  git config --global user.name "Travis CI"
  git remote remove heroku_provider
  git remote remove heroku_android
  git remote remove heroku_ios
  git remote add heroku_provider https://ignored:${HEROKU_API_KEY}@git.heroku.com/pact-demo-provider.git
  git remote add heroku_android https://ignored:${HEROKU_API_KEY}@git.heroku.com/pact-demo-android.git
  git remote add heroku_ios https://ignored:${HEROKU_API_KEY}@git.heroku.com/pact-demo-ios.git
}

upload_files() {
  git push --force heroku_provider master
  git push --force heroku_android master
  git push --force heroku_ios master
}

setup_git
upload_files