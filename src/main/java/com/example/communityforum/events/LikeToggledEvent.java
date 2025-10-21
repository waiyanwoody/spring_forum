package com.example.communityforum.events;

import com.example.communityforum.persistence.entity.Like;

public class LikeToggledEvent {
    private final Like like;
    private final boolean created; // true = liked, false = unliked

    public LikeToggledEvent(Like like, boolean created) {
        this.like = like;
        this.created = created;
    }

    public Like getLike() {
        return like;
    }

    public boolean isCreated() {
        return created;
    }
}
