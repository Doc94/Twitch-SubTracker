#!/bin/bash
cd /usr/local/

# Output Current Java Version
java -version

java -jar app.jar --twitch-token=${TWITCH_TOKEN} --twitch-channelId=${TWITCH_CHANNELID} --rewardId=${TWITCH_REWARD_ID} --rewardsformId=${GOOGLE_FORM_ID_REWARDS} --subsformId=${GOOGLE_FORM_ID_SUBS} --bitsformId=${GOOGLE_FORM_ID_BITS}