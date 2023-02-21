package com.rifqi.dude2;

import androidx.annotation.NonNull;

public class ItemManis {
    private final String text;
    final int icon;

    ItemManis(String text, Integer icon) {
        this.text = text;
        this.icon = icon;
    }
    @NonNull
    @Override
    public String toString() {
        return text;
    }
}
