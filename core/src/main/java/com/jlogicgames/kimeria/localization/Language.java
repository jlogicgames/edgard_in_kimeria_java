package com.jlogicgames.kimeria.localization;

/** Port of Dart... no -- port of the Rust {@code localization::Language}. */
public enum Language {
    ENGLISH, UKRAINIAN;

    /** The language's own name, in its own language -- used as the label of the button that selects it. */
    public String nativeName() {
        return switch (this) {
            case ENGLISH -> "English";
            case UKRAINIAN -> "Українська";
        };
    }

    public Language next() {
        Language[] values = values();
        return values[(ordinal() + 1) % values.length];
    }
}
