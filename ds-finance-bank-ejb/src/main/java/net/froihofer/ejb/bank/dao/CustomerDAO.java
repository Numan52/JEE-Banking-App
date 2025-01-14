package net.froihofer.ejb.bank.dao;

import jakarta.enterprise.context.RequestScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.PersistenceException;
import net.froihofer.common.BankException;
import net.froihofer.ejb.bank.entity.Customer;

import java.util.List;

@RequestScoped
public class CustomerDAO {
    @PersistenceContext
    private EntityManager entityManager;

    public void persist(Customer customer) {
        try {
            entityManager.persist(customer);
            System.out.println("Customer saved: " + customer);
        } catch (PersistenceException e) {
            System.err.println("Error saving customer: " + e.getMessage());
            throw e;
        }
    }

    public Customer findCustomerById(long id) throws BankException {
        try {
            Customer customer = entityManager.find(Customer.class, id); // automatically looks for primary key!
            if (customer == null) {
                throw new BankException("Customer with id " + id + " not found");
            }
            System.out.println("Customer found: " + customer);
            return customer;
        } catch (BankException e) {
            System.err.println("No customer with id " + id + " found");
            throw e;
        }
    }

    public List<Customer> findCustomerByName(String firstName, String lastName) throws BankException {
        try {
            String query = "SELECT c " +
                    "FROM Customer c " +
                    "WHERE LOWER(c.firstName) = LOWER(:firstName) AND LOWER(c.lastName) = LOWER(:lastName)";

            List<Customer> customers = entityManager.createQuery(query, Customer.class)
                    .setParameter("firstName", firstName)
                    .setParameter("lastName", lastName)
                    .getResultList();
            if (customers.isEmpty()) {
                throw new BankException("Customer with name " + firstName + " " + lastName + " not found");
            }

            return customers;
        } catch (PersistenceException e) {
            System.err.println(e + e.getMessage());
            throw new BankException("Error getting customers by name: " + e.getMessage());
        }
    }
}
