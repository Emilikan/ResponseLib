package ru.emilnasyrov.lib.response.helper;

import java.lang.reflect.Type;
import java.util.Date;

public enum GlobalErrorsParams {
    id {
        private final Type type = Long.class;

        @Override
        public Type getType() { return this.type; }
    },
    code {
        private final Type type = int.class;

        @Override
        public Type getType() { return this.type; }
    },
    message {
        private final Type type = String.class;

        @Override
        public Type getType() { return this.type; }
    },
    location {
        private final Type type = String.class;

        @Override
        public Type getType() { return this.type; }
    },
    stackTrace {
        private final Type type = String.class;

        @Override
        public Type getType() { return this.type; }
    },
    importance {
        private final Type type = int.class;

        @Override
        public Type getType() { return this.type; }
    },
    date {
        private final Type type = Date.class;

        @Override
        public Type getType() { return this.type; }
    };

    public abstract Type getType();
}
