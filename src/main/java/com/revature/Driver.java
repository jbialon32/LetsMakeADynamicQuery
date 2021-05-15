package com.revature;

import com.revature.models.Person;
import com.revature.util.querinator.PostgresQueryBuilder;

/**
 * Created by IntelliJ IDEA.
 * User: Jbialon
 * Date: 5/14/2021
 * Time: 5:21 PM
 * Description: {Insert Description}
 */
public class Driver {

    public static void main(String[] args) {

        PostgresQueryBuilder qBuild = new PostgresQueryBuilder();
        Person testPerson = new Person("Test", "Person", 25, "01-18-1996");

        try {
            System.out.println(qBuild.buildQuery(testPerson));
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}
