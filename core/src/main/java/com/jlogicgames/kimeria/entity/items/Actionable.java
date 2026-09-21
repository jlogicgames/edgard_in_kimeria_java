package com.jlogicgames.kimeria.entity.items;

/** Port of Dart's {@code Actionable} mixin: something a {@code Trigger} can activate. */
public interface Actionable {
    String getTargetId();

    void performAction();
}
