package com.winsonchiu.aria.framework.util.arch;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.Observer;

/**
 * This class needs to be in Java so that Kotlin supports NonNullObserver { } construction
 * @param <ValueType>
 */
public interface NonNullObserver<ValueType> extends Observer<ValueType> {

    @Override
    default void onChanged(@Nullable ValueType valueType) {
        if (valueType != null) {
            changed(valueType);
        }
    }

    void changed(@NonNull ValueType valueType);
}
