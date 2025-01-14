package net.froihofer.ejb.bank.dao;

import jakarta.enterprise.context.RequestScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.PersistenceException;
import jakarta.persistence.TypedQuery;
import net.froihofer.ejb.bank.entity.Bank;
import net.froihofer.ejb.bank.entity.Customer;

import java.math.BigDecimal;

@RequestScoped
public class BankDAO {
    @PersistenceContext
    private EntityManager entityManager;

    public BigDecimal getAvailableVolume() throws PersistenceException {
        String queryString = "SELECT b.availableVolume FROM Bank b";

        try {
            TypedQuery<BigDecimal> query = entityManager.createQuery(queryString, BigDecimal.class);
            return query.getSingleResult();
        } catch (PersistenceException e) {
            System.err.println("Error getting available volume: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    public void updateAvailableVolume(BigDecimal availableVolume) {
        String queryString = "UPDATE Bank b " +
                "SET b.availableVolume = :availableVolume";

        try {
            entityManager.createQuery(queryString)
                    .setParameter("availableVolume", availableVolume)
                    .executeUpdate();
        } catch (PersistenceException e) {
            System.err.println("Error getting available volume: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }
    public void createInitialBank() {
        String queryString = "SELECT b FROM Bank b";
        try {
            // check if a bank entity exist
            TypedQuery<Bank> query = entityManager.createQuery(queryString, Bank.class);
            if (query.getResultList().isEmpty()) {
                // bank doesn't exist
                Bank bank = new Bank(new BigDecimal("1000000000"));
                entityManager.persist(bank);
                System.out.println("Initial Bank created with available volume: " + new BigDecimal("1000000000"));
            } else {
                System.out.println("Bank already exists, no action taken.");
            }
        } catch (PersistenceException e) {
            System.err.println("Error creating initial bank: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }
}
