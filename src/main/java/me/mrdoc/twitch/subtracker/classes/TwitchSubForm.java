package me.mrdoc.twitch.subtracker.classes;

import io.github.stepio.jgforms.question.MetaData;

public enum TwitchSubForm implements MetaData {
    //TODO: Configurable values to form
    DATE_SUB(475033903),
    NICK_SUB_BUY(540344883),
    ID_SUB_BUY(2055918815),
    SUB_TYPE(443175380),
    IS_GIFT(1264530195),
    IS_ANON(296757062),
    NICK_SUB_GIFT(1090538013),
    ID_SUB_GIFT(1802713913),
    SUB_MONTHS(818511332),
    SUB_MONTHS_MULTI(978952370),
    SUB_STRIKE(1040419606)
    ;

    private long id;

    TwitchSubForm(long id) {
        this.id = id;
    }

    @Override
    public long getId() {
        return this.id;
    }
}
