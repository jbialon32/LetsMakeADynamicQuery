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

    synchronized public String buildQuery(T obj) throws InvocationTargetException, IllegalAccessException {

        Class<?> clazz = obj.getClass();

        // Return string
        String query;

        // POJO Class level annotations
        Annotation[] classAnnotations = clazz.getAnnotations();

        // TODO: Add a check for entity annotation and throw exception if no present.

        // POJO Class's fields
        Field[] classFields = clazz.getDeclaredFields();

        // This will hold out values part of the query
        ArrayDeque fieldHolder = new ArrayDeque();

        // Field level annotations
        Annotation[] fieldAnno;

        // Holds the table name related to our POJO
        String tableName = "";

        // Holds POJO's related column names
        ArrayDeque<String> queryColumns = new ArrayDeque<String>();

        // These will come in handy when populating the column deque
        String lastQueryColumn;
        Object lastValueValue;

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

                // Check for the Column annotation
                if (ano instanceof Column) {

                    // Allow this script to grab the private fields
                    field.setAccessible(true);

                    // If the StringType annotation is present...
                    if (field.getAnnotation(StringType.class) != null) {

                        // ...Isolate it...
                        Object tempObjHolder = field.get(obj);

                        // ...Surround it in single quotes...
                        String tempStrHolder = "'" + tempObjHolder.toString() + "'";

                        ///...And add to the ArrayDeque
                        fieldHolder.add(tempStrHolder);

                    } else {

                        //...Otherwise just add it to the ArrayDeque as is
                        fieldHolder.add(field.get(obj));

                    }

                    // Set the private field back to inaccessible
                    field.setAccessible(false);

                    Column column = (Column) field.getAnnotation(ano.annotationType());

                    // If so add the column name to our column deque
                    queryColumns.add(column.name());

                }
            }
        }

        // Set a quick reference for comaprison later
        lastValueValue = fieldHolder.peekLast();

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

        // While our arrayDeque of values is not empty...
        while (!fieldHolder.isEmpty()) {

            // Check if it's the last value to get
            if (!fieldHolder.peek().equals(lastValueValue)) {

                // ...If it isn't present just toss it in with toString and end with a comma for the next value
                query = query + fieldHolder.poll().toString() + ", ";

            } else {

                // ...If it is just toss it in with toString and end with a ); to finish the query
                query = query + fieldHolder.poll().toString() + ");";

            }
        }

        // We have our dynamic/generic query :)
        return query;
    }
}
