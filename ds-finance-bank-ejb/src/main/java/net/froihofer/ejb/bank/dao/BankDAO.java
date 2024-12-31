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

    public BigDecimal getAvailableVolume() {
        String queryString = "SELECT b.availableVolume FROM Bank b";

        try {
            TypedQuery<BigDecimal> query = entityManager.createQuery(queryString, BigDecimal.class);
            return query.getSingleResult();
        } catch (PersistenceException e) {
            System.err.println("Error getting available volume: " + e.getMessage());
            throw e;
        }
    }
}
