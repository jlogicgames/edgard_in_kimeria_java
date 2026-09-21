package com.jlogicgames.kimeria.ui;

import java.util.function.Supplier;

/** One button in a {@link Menu}. */
public class MenuItem {
    public final Supplier<String> label;
    public final Runnable action;

    public MenuItem(Supplier<String> label, Runnable action) {
        this.label = label;
        this.action = action;
    }

    public MenuItem(String label, Runnable action) {
        this(() -> label, action);
    }
}
