#!/bin/bash

PLAY_CONF_FILE="conf/application.conf"
sed -i "s;^paymill.key.private *=.*;paymill.key.private=\""${PAYMILL_KEY_PRIVATE}"\";g" "${PLAY_CONF_FILE}"
sed -i "s;^paymill.key.public *=.*;paymill.key.public=\""${PAYMILL_KEY_PUBLIC}"\";g" "${PLAY_CONF_FILE}"
sed -i "s;^mail.auth.key *=.*;mail.auth.key=\""${MAIL_AUTH_KEY}"\";g" "${PLAY_CONF_FILE}"
sed -i "s;^mail.auth.secret *=.*;mail.auth.secret=\""${MAIL_AUTH_SECRET}"\";g" "${PLAY_CONF_FILE}"

# we commit the changes in order to have them included in our push to heroku
git config user.name "Travis CI"
git config user.email "automation@commercetools.de"
git commit -m "Apply configuration at Travis CI." "${PLAY_CONF_FILE}"
