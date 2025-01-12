package net.froihofer.ejb.bank.dao;

import jakarta.enterprise.context.RequestScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.PersistenceException;
import net.froihofer.common.BankException;
import net.froihofer.ejb.bank.entity.Stock;

import java.util.List;

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
    public List<Stock> getAllStocks(long customerId) throws BankException {
        try {
            // Erstellen der Query und Festlegen des Parameters
            List<Stock> stocks = entityManager.createQuery(
                            "SELECT c FROM Stock c WHERE c.customer.customerId = :customerId", Stock.class)
                    .setParameter("customerId", customerId)
                    .getResultList();
            // Ergebnisse ausgeben
            if (stocks.isEmpty()) {
                System.out.println("No stocks found for customer ID: " + customerId);
                return null;
            } else {
               return stocks;
            }
        } catch (PersistenceException e) {
            System.err.println("Error saving stock: " + e.getMessage());
            throw new BankException("Error saving stock: " + e.getMessage());
        }
    }
}
