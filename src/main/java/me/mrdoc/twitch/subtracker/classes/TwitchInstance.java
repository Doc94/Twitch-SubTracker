package me.mrdoc.twitch.subtracker.classes;

import com.github.philippheuer.credentialmanager.domain.OAuth2Credential;
import com.github.twitch4j.pubsub.TwitchPubSub;
import com.github.twitch4j.pubsub.TwitchPubSubBuilder;
import com.github.twitch4j.pubsub.domain.ChannelBitsData;
import com.github.twitch4j.pubsub.domain.ChannelPointsRedemption;
import com.github.twitch4j.pubsub.domain.ChannelPointsReward;
import com.github.twitch4j.pubsub.domain.SubscriptionData;
import com.github.twitch4j.pubsub.events.ChannelBitsEvent;
import com.github.twitch4j.pubsub.events.ChannelSubscribeEvent;
import com.github.twitch4j.pubsub.events.RewardRedeemedEvent;
import io.github.stepio.jgforms.Configuration;
import io.github.stepio.jgforms.Submitter;
import io.github.stepio.jgforms.answer.Builder;
import io.github.stepio.jgforms.exception.NotSubmittedException;
import lombok.Setter;
import me.mrdoc.twitch.subtracker.classes.forms.TwitchBitsForm;
import me.mrdoc.twitch.subtracker.classes.forms.TwitchRewardsForm;
import me.mrdoc.twitch.subtracker.classes.forms.TwitchSubForm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.MalformedURLException;
import java.net.URL;

public class TwitchInstance {

    public static Logger LOGGER = LoggerFactory.getLogger(TwitchInstance.class);

    private final String twitch_token;
    private final String twitch_channelId;
    @Setter
    private String subs_formId = "";
    @Setter
    private String bits_formId = "";
    @Setter
    private String rewards_formId = "";
    @Setter
    private String reward_id = "";

    private TwitchPubSub clientPubSub;

    public TwitchInstance(String token, String channelId) {
        this.twitch_token = token;
        this.twitch_channelId = channelId;
    }

    public void build() {
        OAuth2Credential auth2Credential = new OAuth2Credential("twitch", this.twitch_token);

        clientPubSub = TwitchPubSubBuilder.builder().build();
        clientPubSub.listenForSubscriptionEvents(auth2Credential, this.twitch_channelId);
        clientPubSub.listenForCheerEvents(auth2Credential, this.twitch_channelId);
        clientPubSub.listenForChannelPointsRedemptionEvents(auth2Credential, this.twitch_channelId);

        LOGGER.info("Register subs-event from channel " + this.twitch_channelId);
        clientPubSub.getEventManager().onEvent(ChannelSubscribeEvent.class, this::onSub);

        LOGGER.info("Register bits-event from channel " + this.twitch_channelId);
        clientPubSub.getEventManager().onEvent(ChannelBitsEvent.class, this::onBits);

        LOGGER.info("Register rewards-event from channel " + this.twitch_channelId);
        clientPubSub.getEventManager().onEvent(RewardRedeemedEvent.class, this::onReward);
    }

    public void onSub(ChannelSubscribeEvent event) {
        String context = (event.getData().getContext() == null) ? "Desconocido" : event.getData().getContext().toString();
        LOGGER.info("Trigger sub-event from " + event.getData().getChannelName() + " (" + event.getData().getChannelId() + ") with context " + context + " for " + event.getData().getUserName());
        sendSubToForm(event.getData());
    }

    public void onBits(ChannelBitsEvent event) {
        LOGGER.info("Trigger bits-event from " + event.getData().getChannelName() + " (" + event.getData().getChannelId() + ") with context " + event.getData().getContext() + " for " + event.getData().getUserName());
        sendBitsToForm(event.getData());
    }

    public void onReward(RewardRedeemedEvent event) {
        ChannelPointsReward channelPointsReward = event.getRedemption().getReward();
        if(!channelPointsReward.getId().equals(reward_id)) {
            //No hay match con el reward a trackear, no quiero basura de logs
            return;
        }
        LOGGER.info("Trigger rewards-event from " + "channel" + " (" + event.getRedemption().getChannelId() + ") for " + event.getRedemption().getUser().getDisplayName());
        sendRewardsToForm(event.getRedemption());
    }

    public void sendRewardsToForm(ChannelPointsRedemption channelPointsRedemption) {
        if(rewards_formId == null || rewards_formId.isEmpty()) {
            LOGGER.info("You cant have a registry of rewards in form because you dont have set a form ID.");
            return;
        }

        try {
            URL url = Builder.formKey(this.rewards_formId)
                    .put(TwitchRewardsForm.NICK_REDEEMED, channelPointsRedemption.getUser().getDisplayName())
                    .put(TwitchRewardsForm.ID_REDEEMED, channelPointsRedemption.getUser().getId())
                    .put(TwitchRewardsForm.ID_REWARD, channelPointsRedemption.getReward().getId())
                    .put(TwitchRewardsForm.TITLE_REWARD, channelPointsRedemption.getReward().getTitle())
                    .toUrl();
            Submitter submitter = new Submitter(new Configuration());
            submitter.submitForm(url);
        } catch (MalformedURLException | IllegalArgumentException | NotSubmittedException e) {
            LOGGER.error("Detected error in send rewards-event to form. [" + channelPointsRedemption.toString() + "]",e);
        }
    }

    public void sendBitsToForm(ChannelBitsData channelBitsData) {
        if(bits_formId == null || bits_formId.isEmpty()) {
            LOGGER.debug("You cant have a registry of bits in form because you dont have set a form ID.");
            return;
        }

        boolean isAnon = channelBitsData.getUserId() == null || channelBitsData.getUserId().isEmpty();

        String nickBuy = (isAnon) ? "Anonimo" : channelBitsData.getUserName();
        String idBuy = (isAnon) ? "0" : channelBitsData.getUserId();

        int bits = channelBitsData.getBitsUsed();
        try {
            URL url = Builder.formKey(this.bits_formId)
                    .put(TwitchBitsForm.DATE_BITS, channelBitsData.getTime())
                    .put(TwitchBitsForm.BITS, bits)
                    .put(TwitchBitsForm.ID_BITS_BUY, idBuy)
                    .put(TwitchBitsForm.NICK_BITS_BUY, nickBuy)
                    .put(TwitchBitsForm.IS_ANON, Boolean.toString(isAnon))
                    .toUrl();
            Submitter submitter = new Submitter(new Configuration());
            submitter.submitForm(url);
        } catch (MalformedURLException | IllegalArgumentException | NotSubmittedException e) {
            LOGGER.error("Detected error in send bits-event to form. [" + channelBitsData.toString() + "]",e);
        }
    }

    public void sendSubToForm(SubscriptionData subscriptionData) {
        if(subs_formId == null || subs_formId.isEmpty()) {
            LOGGER.debug("You cant have a registry of subs in form because you dont have set a form ID.");
            return;
        }

        String context = (subscriptionData.getContext() == null) ? "???" : subscriptionData.getContext().toString();

        boolean isAnon = context.toUpperCase().contains("ANON");
        boolean isGift = subscriptionData.getIsGift();

        String nickBuy = (isAnon) ? "Anonimo" : (subscriptionData.getUserName() == null) ? "null" : subscriptionData.getUserName(); //Maybe more fix for null?
        String idBuy = (isAnon) ? "0" : subscriptionData.getUserId();
        String nickGift = (isGift) ? subscriptionData.getRecipientUserName() : "N/A";
        String idGift = (isGift) ? subscriptionData.getRecipientId() : "0";

        int months = (subscriptionData.getCumulativeMonths() == null) ? 0 : subscriptionData.getCumulativeMonths();
        int monthsMulti = (subscriptionData.getMultiMonthDuration() == null) ? 0 : subscriptionData.getMultiMonthDuration();
        int strike = (subscriptionData.getStreakMonths() == null) ? 0 : subscriptionData.getStreakMonths();

        try {
            URL url = Builder.formKey(this.subs_formId)
                    .put(TwitchSubForm.DATE_SUB, subscriptionData.getTimestamp().toString())
                    .put(TwitchSubForm.NICK_SUB_BUY, nickBuy)
                    .put(TwitchSubForm.ID_SUB_BUY,idBuy)
                    .put(TwitchSubForm.SUB_TYPE,getTypeSub(subscriptionData.getSubPlan().ordinalName()))
                    .put(TwitchSubForm.IS_GIFT,Boolean.toString(isGift))
                    .put(TwitchSubForm.IS_ANON,Boolean.toString(isAnon))
                    .put(TwitchSubForm.ID_SUB_GIFT,idGift)
                    .put(TwitchSubForm.NICK_SUB_GIFT,nickGift)
                    .put(TwitchSubForm.SUB_STRIKE,strike)
                    .put(TwitchSubForm.SUB_MONTHS,months)
                    .put(TwitchSubForm.SUB_MONTHS_MULTI,monthsMulti)
                    .toUrl();
            Submitter submitter = new Submitter(new Configuration());
            submitter.submitForm(url);
        } catch (MalformedURLException | IllegalArgumentException | NotSubmittedException e) {
            LOGGER.error("[ERROR] Detected error in send sub-event to form. [" + subscriptionData.toString() + "]",e);
        }
    }

    public String getTypeSub(String raw) {
        switch (raw.toUpperCase()) {
            default:
                return "Desconocido";
            case "PRIME":
                return "Prime";
            case "1000":
                return "Tier 1";
            case "2000":
                return "Tier 2";
            case "3000":
                return "Tier 3";
        }
    }

    /*
     * ChannelSubscribeEvent(data=SubscriptionData(userName=osamu_miya, displayName=osamu_miya, channelName=jaidefinichon, userId=407388960, channelId=30610294, time=2020-09-04T22:01:21.914691038Z, subPlan=Prime, subPlanName=Sub del GOI, months=0, cumulativeMonths=1, streakMonths=null, context=sub, isGift=false, multiMonthDuration=0, subMessage=CommerceMessage(message=, emotes=null), recipientId=407388960, recipientUserName=osamu_miya, recipientDisplayName=osamu_miya))
     */

}
