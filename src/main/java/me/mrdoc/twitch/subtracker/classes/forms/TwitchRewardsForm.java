package me.mrdoc.twitch.subtracker.classes.forms;

import io.github.stepio.jgforms.question.MetaData;

public enum TwitchRewardsForm implements MetaData {
    NICK_REDEEMED(0L),
    ID_REDEEMED(0L),
    ID_REWARD(0L),
    TITLE_REWARD(0L),
    ;

    private long id;

    TwitchRewardsForm(long id) {
        this.id = id;
    }

    @Override
    public long getId() {
        return this.id;
    }
}
