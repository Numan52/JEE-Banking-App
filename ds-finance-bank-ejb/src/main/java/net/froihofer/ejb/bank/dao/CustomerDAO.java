package net.froihofer.ejb.bank.dao;

import jakarta.enterprise.context.RequestScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.PersistenceException;
import jakarta.persistence.Query;
import net.froihofer.common.BankException;
import net.froihofer.ejb.bank.entity.Customer;

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
            Customer customer = entityManager.find(Customer.class, id);
            System.out.println("Customer found: " + customer);
            return customer;
        } catch (PersistenceException e) {
            System.err.println("Error finding Customer " + e.getMessage());
            throw new BankException("Error finding Customer " + e.getMessage());
        }

    }
}
