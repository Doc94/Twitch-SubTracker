package me.mrdoc.twitch.subtracker.classes.forms;

import io.github.stepio.jgforms.question.MetaData;

public enum TwitchRewardsForm implements MetaData {
    ID_REDEEMED(1438793387),
    NICK_REDEEMED(1820502547),
    ID_REWARD(2102415750),
    TITLE_REWARD(1413301137),
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
