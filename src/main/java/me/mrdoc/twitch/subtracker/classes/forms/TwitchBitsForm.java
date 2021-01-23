package me.mrdoc.twitch.subtracker.classes.forms;

import io.github.stepio.jgforms.question.MetaData;

public enum TwitchBitsForm implements MetaData {
    //TODO: Configurable values to form
    DATE_BITS(1356753080),
    NICK_BITS_BUY(218662419),
    ID_BITS_BUY(1151464388),
    IS_ANON(216872829),
    BITS(1970344239),
    ;

    private long id;

    TwitchBitsForm(long id) {
        this.id = id;
    }

    @Override
    public long getId() {
        return this.id;
    }
}
