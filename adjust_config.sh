#!/bin/bash

PLAY_CONF_FILE="conf/application.conf"
sed -i "s;^sphere.project=.*;sphere.project="${SPHERE_PROJECT}";g" "${PLAY_CONF_FILE}"
sed -i "s;^sphere.clientId=.*;sphere.clientId="${SPHERE_CLIENT_ID}";g" "${PLAY_CONF_FILE}"
sed -i "s;^sphere.clientSecret=.*;sphere.clientSecret="${SPHERE_CLIENT_SECRET}";g" "${PLAY_CONF_FILE}"
sed -i "s;^sphere.clientSecret=.*;paymill.apiKey="${PAYMILL_API_KEY}";g" "${PLAY_CONF_FILE}"
sed -i "s;^sphere.clientSecret=.*;mail.auth.key="${MAIL_AUTH_KEY}";g" "${PLAY_CONF_FILE}"
sed -i "s;^sphere.clientSecret=.*;mail.auth.secret="${MAIL_AUTH_SECRET}";g" "${PLAY_CONF_FILE}"
sed -i "s;^application.secret=.*;application.secret="${PLAY_APP_SECRET}";g" "${PLAY_CONF_FILE}"

# we commit the changes in order to have them included in our push to heroku
git config user.name "Travis CI"
git config user.email "automation@commercetools.de"
git commit -m "Apply configuration at Travis CI." "${PLAY_CONF_FILE}"
