package com.revature.util.annotation;

import java.lang.annotation.*;

/**
 * Created by IntelliJ IDEA.
 * User: Jbialon
 * Date: 5/15/2021
 * Time: 9:09 AM
 * Description: {Insert Description}
 */


@Documented
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Foreign {
    String name() default "";
}
