package com.winsonchiu.aria.framework.util.arch;

import android.arch.lifecycle.Observer;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

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
