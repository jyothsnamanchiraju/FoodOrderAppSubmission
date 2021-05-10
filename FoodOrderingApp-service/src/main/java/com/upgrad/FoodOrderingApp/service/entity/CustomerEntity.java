package com.upgrad.FoodOrderingApp.service.entity;

import org.apache.commons.lang3.builder.ToStringExclude;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;

@Entity
@Table(name="customer")
@NamedQueries(
        {
                @NamedQuery(name = "customerByContactNumber", query = "select c from CustomerEntity c where c.contactNumber = :contactNumber")
        }
)
public class CustomerEntity implements Serializable {

    @Id
    @Column(name="id")                  //id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "uuid")              //uuid
    @NotNull
    @Size(max = 200)
    private String uuid;

    @Column(name = "firstname")         //firstname
    @NotNull
    @Size(max = 30)
    private String firstName;

    @Column(name = "lastname")          //lastname
    @Size(max = 30)
    private String lastName;

    @Column(name = "email")             //email
    @Size(max = 50)
    private String email;

    @Column(name = "contact_number")     //contact_number
    @NotNull
    @Size(max = 30)
    private String contactNumber;

    @Column(name = "password")          //password
    @NotNull
    @Size(max = 255)
    @ToStringExclude
    private String password;

    @Column(name = "salt")              //salt
    @NotNull
    @Size(max = 255)
    @ToStringExclude
    private String salt;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getContactNumber() {
        return contactNumber;
    }

    public void setContactNumber(String contactNumber) {
        this.contactNumber = contactNumber;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getSalt() {
        return salt;
    }

    public void setSalt(String salt) {
        this.salt = salt;
    }
}
