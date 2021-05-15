package com.revature.models;

import com.revature.util.annotation.*;

/**
 * Created by IntelliJ IDEA.
 * User: Jbialon
 * Date: 5/14/2021
 * Time: 5:21 PM
 * Description: {Insert Description}
 */
@Entity
@Table(table_name = "person")
public class Person {

    @Id
    @Column(name = "person_id", data_type = "serial")
    private int id;

    @Column(name = "first_name", data_type = "var_char")
    private String firstName;

    @Column(name = "last_name", data_type = "var_char")
    private String lastName;

    @Column(name = "age", data_type = "smallint")
    private int age;

    @Column(name = "birthday", data_type = "var_char")
    private String birthday;

    public Person(String firstName, String lastName, int age, String birthday) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.age = age;
        this.birthday = birthday;
    }

    @Getter
    @IdGetter
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @StringType
    @Getter
    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    @StringType
    @Getter
    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    @Getter
    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    @StringType
    @Getter
    public String getBirthday() {
        return birthday;
    }

    public void setBirthday(String birthday) {
        this.birthday = birthday;
    }
}
