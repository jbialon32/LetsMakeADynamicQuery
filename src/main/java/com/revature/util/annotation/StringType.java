package com.revature.util.annotation;

import java.lang.annotation.*;

/**
 * Created by IntelliJ IDEA.
 * User: James Bialon
 * Date: 5/14/2021
 * Time: 7:58 PM
 * Description: This annotation lets the compiler know this is a string type and must be treated differently
 *              when building queries.
 */

@Documented
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface StringType {
}
