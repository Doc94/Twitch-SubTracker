package me.mrdoc.twitch.subtracker.classes;

import com.github.philippheuer.credentialmanager.domain.OAuth2Credential;
import com.github.philippheuer.events4j.core.EventManager;
import com.github.philippheuer.events4j.simple.SimpleEventHandler;
import com.github.twitch4j.pubsub.TwitchPubSub;
import com.github.twitch4j.pubsub.TwitchPubSubBuilder;
import com.github.twitch4j.pubsub.domain.ChannelBitsData;
import com.github.twitch4j.pubsub.domain.SubscriptionData;
import com.github.twitch4j.pubsub.events.ChannelBitsEvent;
import com.github.twitch4j.pubsub.events.ChannelSubscribeEvent;
import io.github.stepio.jgforms.Configuration;
import io.github.stepio.jgforms.Submitter;
import io.github.stepio.jgforms.answer.Builder;
import io.github.stepio.jgforms.exception.NotSubmittedException;

import java.net.MalformedURLException;
import java.net.URL;

public class TwitchInstance {

    private final String twitch_token;
    private final String twitch_channelId;
    private final String subs_formId;
    private final String bits_formId;

    private TwitchPubSub clientPubSub;

    public TwitchInstance(String token, String channelId, String subsFormId, String bitsFormId) {
        this.twitch_token = token;
        this.twitch_channelId = channelId;
        this.subs_formId = subsFormId;
        this.bits_formId = bitsFormId;
    }

    public void build() {
        OAuth2Credential auth2Credential = new OAuth2Credential("twitch", this.twitch_token);

        EventManager eventManager = new EventManager();
        eventManager.registerEventHandler(new SimpleEventHandler());

        clientPubSub = TwitchPubSubBuilder.builder().withEventManager(eventManager).build();
        clientPubSub.listenForSubscriptionEvents(auth2Credential, this.twitch_channelId);
        clientPubSub.listenForCheerEvents(auth2Credential, this.twitch_channelId);

        System.out.println("Register sub-event from channel " + this.twitch_channelId);
        eventManager.getEventHandler(SimpleEventHandler.class).onEvent(ChannelSubscribeEvent.class, this::onSub);
        System.out.println("Register bits-event from channel " + this.twitch_channelId);
        eventManager.getEventHandler(SimpleEventHandler.class).onEvent(ChannelBitsEvent.class, this::onBits);
    }

    public void onSub(ChannelSubscribeEvent event) {
        System.out.println("[INFO] Trigger sub-event from " + event.getData().getChannelName() + " (" + event.getData().getChannelId() + ") with context " + event.getData().getContext().toString());
        sendSubToForm(event.getData());
    }

    public void onBits(ChannelBitsEvent event) {
        System.out.println("[INFO] Trigger bits-event from " + event.getData().getChannelName() + " (" + event.getData().getChannelId() + ") with context " + event.getData().getContext());
        sendBitsToForm(event.getData());
    }

    public void sendBitsToForm(ChannelBitsData channelBitsData) {
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
        } catch (MalformedURLException | NotSubmittedException e) {
            System.out.println("[ERROR] Detected error in send bits-event to form. [" + channelBitsData.toString() + "]");
            e.printStackTrace();
        }
    }

    public void sendSubToForm(SubscriptionData subscriptionData) {
        boolean isAnon = subscriptionData.getContext().toString().contains("ANON");
        boolean isGift = subscriptionData.getIsGift();

        String nickBuy = (isAnon) ? "Anonimo" : subscriptionData.getUserName();
        String idBuy = (isAnon) ? "0" : subscriptionData.getUserId();
        String nickGift = (isGift) ? subscriptionData.getRecipientUserName() : "N/A";
        String idGift = (isGift) ? subscriptionData.getRecipientId() : "0";

        int months = (subscriptionData.getCumulativeMonths() == null) ? 0 : subscriptionData.getCumulativeMonths();
        int monthsMulti = (subscriptionData.getMultiMonthDuration() == null) ? 0 : subscriptionData.getMultiMonthDuration();
        int strike = (subscriptionData.getStreakMonths() == null) ? 0 : subscriptionData.getStreakMonths();

        try {
            URL url = Builder.formKey(this.subs_formId)
                    .put(TwitchSubForm.DATE_SUB, subscriptionData.getTime())
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
        } catch (MalformedURLException | NotSubmittedException e) {
            System.out.println("[ERROR] Detected error in send sub-event to form. [" + subscriptionData.toString() + "]");
            e.printStackTrace();
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

    /**
     * ChannelSubscribeEvent(data=SubscriptionData(userName=osamu_miya, displayName=osamu_miya, channelName=jaidefinichon, userId=407388960, channelId=30610294, time=2020-09-04T22:01:21.914691038Z, subPlan=Prime, subPlanName=Sub del GOI, months=0, cumulativeMonths=1, streakMonths=null, context=sub, isGift=false, multiMonthDuration=0, subMessage=CommerceMessage(message=, emotes=null), recipientId=407388960, recipientUserName=osamu_miya, recipientDisplayName=osamu_miya))
     */

}
