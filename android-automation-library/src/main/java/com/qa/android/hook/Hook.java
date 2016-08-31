package com.qa.android.hook;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The interface Hook.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Hook {
    /**
     * Value string.
     *
     * @return the string
     */
    String value() default "";
}
