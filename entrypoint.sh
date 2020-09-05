#!/bin/bash
cd /usr/local/

# Output Current Java Version
java -version

java -jar app.jar --twitch-token=${TWITCH_TOKEN} --twitch-channelId=${TWITCH_CHANNELID} --formId=${GOOGLE_FORM_ID}