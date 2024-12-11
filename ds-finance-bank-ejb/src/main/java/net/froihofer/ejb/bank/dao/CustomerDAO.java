package net.froihofer.ejb.bank.dao;

import jakarta.enterprise.context.RequestScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.PersistenceException;
import jakarta.persistence.Query;
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
}
