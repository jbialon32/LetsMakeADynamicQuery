package com.revature.util.annotation;

import java.lang.annotation.*;

/**
 * Created by IntelliJ IDEA.
 * User: Jbialon
 * Date: 5/14/2021
 * Time: 5:15 PM
 * Description: {Insert Description}
 */

@Documented
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Column {
    String name() default "";
    String data_type() default "";
}
