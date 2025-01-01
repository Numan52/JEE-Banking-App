package net.froihofer.ejb.bank.dao;

import jakarta.enterprise.context.RequestScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.PersistenceException;
import net.froihofer.common.BankException;
import net.froihofer.ejb.bank.entity.Customer;
import net.froihofer.ejb.bank.entity.Stock;

@RequestScoped
public class StockDAO {
    @PersistenceContext
    private EntityManager entityManager;

    public void persist(Stock stock) throws BankException {
        try {
            entityManager.persist(stock);
        } catch (PersistenceException e) {
            System.err.println("Error saving stock: " + e.getMessage());
            throw new BankException("Error saving stock: " + e.getMessage());
        }

    }
}
