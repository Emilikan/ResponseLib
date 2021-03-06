package ru.emilnasyrov.lib.response.helper;

import java.lang.reflect.Type;

public enum NotificationEmailsParams {
    name {
        private final Type type = String.class;

        @Override
        public Type getType() { return type; }
    },

    email {
        private final Type type = String.class;

        @Override
        public Type getType() { return type; }
    };

    public abstract Type getType();
}
