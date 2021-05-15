package com.revature.util.annotation;

import java.lang.annotation.*;

/**
 * Created by IntelliJ IDEA.
 * User: Jbialon
 * Date: 5/14/2021
 * Time: 7:59 PM
 * Description: {Insert Description}
 */

@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Getter {
}
