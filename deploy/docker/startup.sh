#!/usr/bin/env bash

# Starts up Xlator service

set -e

# Set temporary administrator password for Grafana
export GF_SECURITY_ADMIN_PASSWORD=admin

# Pick an ISO 639-1 code for locale (e.g., en for English)
export APP_DEFAULTS_LOCALE=en

# Default service choices are: frengly, google
export APP_DEFAULTS_SERVICE=frengly

# Choose a number <= 100

export APP_LIMITS_TRANSLATIONS_PER_REQUEST=100

# OPTION 1: FRENGLY
# Be sure to register for an account on http://frengly.com before attempting to use this service!
# You should update the configuration below, adding email and password values matching a valid pair of credentials.

export APP_FRENGLY_EMAIL=fastnsilver@gmail.com
export APP_FRENGLY_PASSWORD=redFish#1
  
# OPTION 2: GOOGLE TRANSLATE
# Another pay-for option is the Google Translate API (@see https://cloud.google.com/translate/v2/using_rest).
# Update (and uncomment) the configuration below to add your API key.  
# Be sure to comment out the lines that start with APP_FRENGLY above if you wish to employ the Google Translate API instead.

export APP_GOOGLE_KEY= 

# Exposed management endpoints
export MANAGEMENT_ENDPOINTS_WEB_EXPOSURE_INCLUDE=env,beans,info,health,metrics,httptrace,prometheus

docker-compose up -d
