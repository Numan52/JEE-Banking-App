package net.froihofer.common.dtos;

import java.io.Serializable;

public class CustomerDto implements Serializable {
    private long customerId;
    private String firstName;
    private String lastName;
    private String address;
    private String password;

   /* public CustomerDto(String customerId, String firstName, String lastName, String address) {
        this.customerId = customerId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.address = address;
    }*/

    public CustomerDto(String firstName, String lastName, String address, String password) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.address = address;
        this.password = password;
    }


    public CustomerDto(Long customerId, String firstName, String lastName, String address) {
        this.customerId = customerId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.address = address;
    }





    public long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(long customerId) {
        this.customerId = customerId;
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

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPassword() {return password;} //not so sure about access modifiers

    public void setPassword(String password) {this.password = password;}

    @Override
    public String toString() {
        return "CustomerDto{" +
                "customerId=" + customerId +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", address='" + address + '\'' +
                '}';
    }
}
