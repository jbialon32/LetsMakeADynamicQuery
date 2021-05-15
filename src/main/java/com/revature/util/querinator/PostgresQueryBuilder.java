package com.revature.util.querinator;

import com.revature.util.annotation.*;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: Jbialon
 * Date: 5/14/2021
 * Time: 5:33 PM
 * Description: {Insert Description}
 */
public class PostgresQueryBuilder<T> {

    public PostgresQueryBuilder(){};

    public String buildQuery(T obj) throws InvocationTargetException, IllegalAccessException {

        Class<?> clazz = obj.getClass();

        // Return string
        String query;

        // POJO Class level annotations
        Annotation[] classAnnotations = clazz.getAnnotations();

        // POJO Class's fields
        Field[] classFields = clazz.getDeclaredFields();

        // Field level annotations
        Annotation[] fieldAnno;

        // T's methods
        Method[] classMethods = clazz.getDeclaredMethods();

        // Array deque that holds getter methods
        ArrayDeque<Method> objGetters = new ArrayDeque<Method>();

        // Populate getter deque
        for (Method method : classMethods) {

            /*
                Checks if the method has the the Getter annotation
                but also ensures it doesn't have the IdGetter annotation
             */

            if (method.getAnnotation(Getter.class) != null
                    && method.getAnnotation(IdGetter.class) == null) {
                objGetters.add(method);
            }
        }

        // Will be used to invoke getters later
        Method getter;

        // Holds the table name related to our POJO
        String tableName = "";


        // Holds POJO's related column names
        ArrayDeque<String> queryColumns = new ArrayDeque<String>();

        // These will come in handy when populating the column deque
        String lastQueryColumn;
        Method lastGetterMethod = objGetters.peekLast();

        // Loop through our class level annotations
        for(Annotation ano : classAnnotations) {

            // If there is a table annotation...
            if (ano instanceof Table) {
                Table table = (Table) clazz.getAnnotation(ano.annotationType());

                // Set our table name variable to the annotation table_name value
                tableName = table.table_name();

            }

        }

        // Let's start building our query with the info currently available to us
        query = "insert into " + tableName + "(";

        // Start looping through the class fields
        for(Field field : classFields) {

            // Grab the current fields annotations
            fieldAnno = field.getAnnotations();


            // Loop through the annotations in the previous step
            for (Annotation ano : fieldAnno) {

                if (ano instanceof Column) {

                    Column column = (Column) field.getAnnotation(ano.annotationType());

                    // If so add the column name to our column deque
                    queryColumns.add(column.name());

                }
            }
        }

        // Best keep track of that last entry now
        lastQueryColumn = queryColumns.peekLast();

        // While we still have column data in our deque...
        while (!queryColumns.isEmpty()) {

            // If it's not the last item in the deque
            if (!queryColumns.peek().equals(lastQueryColumn)) {

                // Add it to our query followed by a comma
                query = query + queryColumns.poll() + ", ";

            } else {

                // If it is the last item add it to the query but close it off and start the values portion of our query
                query = query + queryColumns.poll() + ") values (";
            }

        }

        // While the getter methods are still in our getter deque
        while (!objGetters.isEmpty()) {

            // Check if it's the last getter method to call
            if (!objGetters.peek().equals(lastGetterMethod)) {

                // Set the head as the current method
                getter = objGetters.poll();

                // If the StringType annotation is present...
                if (getter.getAnnotation(StringType.class) != null) {

                    // ...wrap it up in single quotes and end with a comma for the next value
                    query = query + "'" + getter.invoke(obj, null) + "', ";

                } else {

                    // If it isn't present just toss it in with toString and end with a comma for the next value
                    query = query + getter.invoke(obj, null).toString() + ", ";

                }

            } else {

                // Grab the last getter method
                getter = objGetters.poll();

                // If the StringType annotation is present...
                if (getter.getAnnotation(StringType.class) != null) {

                    // ...wrap it up in single quotes and finish our query with );
                    query = query + "'" + getter.invoke(obj, null) + "');";

                } else {

                    // Otherwise toString it and finish our query with );
                    query = query + getter.invoke(obj, null).toString() + ");";

                }

            }

        }

        // We have our dynamic/generic query :)
        return query;
    }
}
