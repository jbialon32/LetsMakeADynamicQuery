package com.revature.util.querinator;

import com.revature.exceptions.AnnotationNotFound;
import com.revature.exceptions.InvalidInput;
import com.revature.util.annotation.*;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by IntelliJ IDEA.
 * User: James Bialon
 * Date: 5/14/2021
 * Time: 5:33 PM
 * Description: Dynamically build queries to interact with a postgres database
 */
public class PostgresQueryBuilder<T> {

    public PostgresQueryBuilder(){};

    /**
     *
     * Description: Wraps everything nicely into a switch for multiple query types.
     *
     * @param obj
     * @param queryType
     * @return query
     * @throws IllegalAccessException
     * @throws InvalidInput
     * @throws AnnotationNotFound
     */

    synchronized public String buildQuery(T obj, String queryType) throws IllegalAccessException, InvalidInput, AnnotationNotFound {

        // Set of valid queryType entries
        Set<String> validQueryTypes = Stream.of("insert", "update", "select", "delete")
                                            .collect(Collectors.toCollection(HashSet::new));

        // Ensures a good entry for query type
        if (!validQueryTypes.contains(queryType)) { throw new InvalidInput("Bad query type value!"); }

        // Ensures the pojo is supposed to be persisted to a table
        if (!obj.getClass().isAnnotationPresent(Entity.class)) { throw new AnnotationNotFound("Entity annotation not found!!"); }

        // Holds the table name related to our POJO
        String tableName = getTableName(obj);

        // Get the queries column names
        ArrayDeque<String> queryColumns = getColumnNames(obj);

        // Get the queries values
        ArrayDeque<Object> queryValues = getValues(obj);

        // Primary key info [0] will be the pk column name [1] will be the key itself
        Object[] pkInfo = getPrimaryKey(obj);

        // The return value
        String query = "";

        switch (queryType) {

            case "insert":

                // We have our dynamic/generic query :)
                query = buildInsertQueryString(tableName, queryColumns, queryValues);
                break;

            case "update":
                query = buildUpdateQueryString(tableName, pkInfo, queryColumns, queryValues);
                break;

            case "select":

                query = buildSelectAllByPK(tableName, pkInfo);

                break;

            case "delete":
                query = buildDeleteByPK(tableName, pkInfo);
                break;
        }

        return query;

    }

    /**
     *
     * @param obj
     * @return tableName
     *
     */
    private String getTableName(Object obj) {

        // Get the objects class
        Class clazz = obj.getClass();

        // Return value
        String tableName = "";

        // POJO Class level annotations
        Annotation[] classAnnotations = clazz.getAnnotations();

        // Loop through our class level annotations
        for(Annotation ano : classAnnotations) {

            // If there is a table annotation...
            if (ano instanceof Table) {
                Table table = (Table) clazz.getAnnotation(ano.annotationType());

                // Set our table name variable to the annotation table_name value
                tableName = table.table_name();

            }

        }

        return tableName;
    }

    /**
     *
     * @param obj
     * @return ArrayDeque of column names
     */
    private ArrayDeque<String> getColumnNames(Object obj) {

        // Get the objects class
        Class clazz = obj.getClass();

        // POJO Class's fields
        Field[] classFields = clazz.getDeclaredFields();

        // Return value
        ArrayDeque<String> queryColumns = new ArrayDeque<>();

        // Field level annotations
        Annotation[] fieldAnno;

        // Start looping through the class fields
        for(Field field : classFields) {

            // Grab the current fields annotations
            fieldAnno = field.getAnnotations();

            // Loop through the annotations in the previous step
            for (Annotation ano : fieldAnno) {

                // Check for the Column annotation
                if (ano instanceof Column) {

                    Column column = (Column) field.getAnnotation(ano.annotationType());

                    // If so add the column name to our column deque
                    queryColumns.add(column.name());

                }
            }
        }

        return queryColumns;

    }

    /**
     *
     * @param obj
     * @return ArrayDeque of mixed type values
     * @throws IllegalAccessException
     */
    private ArrayDeque<Object> getValues(Object obj) throws IllegalAccessException {

        // Get the objects class
        Class clazz = obj.getClass();

        // POJO Class's fields
        Field[] classFields = clazz.getDeclaredFields();

        // This will hold out values part of the query
        ArrayDeque fieldHolder = new ArrayDeque();

        // Field level annotations
        Annotation[] fieldAnno;

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
                    if (field.isAnnotationPresent(StringType.class)) {

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
                }
            }
        }

        return fieldHolder;

    }

    /**
     *
     * @param obj
     * @return int (primary key)
     * @throws IllegalAccessException
     */
    private Object[] getPrimaryKey(Object obj) throws IllegalAccessException, InvalidInput {

        // Get the objects class
        Class clazz = obj.getClass();

        // POJO Class's fields
        Field[] classFields = clazz.getDeclaredFields();

        // Annotation holder
        Annotation[] fieldAnno;

        // Return values
        int primaryKey = -1;
        String primaryColumnName = "";
        Object[] returnArray = new Object[2];

        for (Field field : classFields) {

            // Grab the current fields annotations
            fieldAnno = field.getAnnotations();

            // Loop through the annotations in the previous step
            for (Annotation ano : fieldAnno) {

                // Check for the Column annotation
                if (ano instanceof Primary) {

                    Primary pkAnno = (Primary) field.getAnnotation(ano.annotationType());

                    primaryColumnName = pkAnno.name();

                    // Allow this script to grab the private fields
                    field.setAccessible(true);

                    if (field.get(obj) != null) {

                        primaryKey = (int) field.get(obj);

                    } else { throw new InvalidInput("Primary key cannot be null!"); }

                    // Stop others from grabbing the private field
                    field.setAccessible(false);

                }
            }
        }

        if (primaryKey == -1) { throw new InvalidInput("Primary key is non-existent!"); }

        returnArray[0] = primaryColumnName;
        returnArray[1] = primaryKey;


        return returnArray;

    }

    /**
     *
     * @param tableName
     * @param queryColumns
     * @param queryValues
     * @return Insert query
     */
    private String buildInsertQueryString(String tableName, ArrayDeque<String> queryColumns, ArrayDeque<Object> queryValues) {

        // Return value
        String query = "insert into " + tableName + "(";

        // These will come in handy when populating the query with our Dequeues
        String lastQueryColumn = queryColumns.peekLast();
        Object lastValueValue = queryValues.peekLast();

        // While we still have column data in our deque...
        while (!queryColumns.isEmpty()) {

            // If it's not the last item in the deque
            if (!queryColumns.peek().equals(lastQueryColumn)) {

                // Add it to our query followed by a comma
                query = query + queryColumns.poll() + ", ";

            } else {

                // If it is the last item add it to the query but close it off and start the values portion of our query
                query = query + queryColumns.poll() + ") values(";
            }

        }

        // While our arrayDeque of values is not empty...
        while (!queryValues.isEmpty()) {

            // Check if it's the last value to get
            if (!queryValues.peek().equals(lastValueValue)) {

                // ...If it isn't present just toss it in with toString and end with a comma for the next value
                query = query + queryValues.poll().toString() + ", ";

            } else {

                // ...If it is just toss it in with toString and end with a ); to finish the query
                query = query + queryValues.poll().toString() + ");";

            }
        }

        return query;

    }

    private String buildUpdateQueryString(String tableName, Object[] pkInfo, ArrayDeque<String> queryColumns, ArrayDeque<Object> queryValues) {

        // Return value
        String query = "update " + tableName + " set ";


        // While we still have column data in our deque...
        while (!queryColumns.isEmpty()) {

            query = query + queryColumns.poll() + " = " + queryValues.poll().toString() + " ";

        }

        query = query + "where " + pkInfo[0] + " = " + pkInfo[1] + ";";

        return query;

    }

    private String buildSelectAllByPK(String tableName, Object[] pkInfo) {

        return "select * from " + tableName + " where " + pkInfo[0] + " = " + pkInfo[1].toString();

    }

    private String buildDeleteByPK(String tableName, Object[] pkInfo) {

        return "delete from " + tableName + " where " + pkInfo[0] + " = " + pkInfo[1].toString();

    }
}
