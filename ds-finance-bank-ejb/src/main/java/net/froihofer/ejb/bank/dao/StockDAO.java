package net.froihofer.ejb.bank.dao;

import jakarta.enterprise.context.RequestScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.PersistenceException;
import net.froihofer.common.BankException;
import net.froihofer.ejb.bank.entity.Customer;
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



    public void update(Stock stock) throws BankException {
        try {
            entityManager.merge(stock);
        } catch (PersistenceException e) {
            System.err.println("Error updating stock: " + e.getMessage());
            throw new BankException("Error updating stock: " + e.getMessage());
        }
    }

    public List<Stock> getAllStocks(long customerId) throws BankException {
        try {
            List<Stock> stocks = entityManager.createQuery(
                            "SELECT c FROM Stock c WHERE c.customer.customerId = :customerId", Stock.class)
                    .setParameter("customerId", customerId)
                    .getResultList();
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


    public List<Stock> findStockByCustomer(String stockSymbol, Customer customer) throws BankException {
        try {
            List<Stock> stocks = entityManager.createQuery(
                            "SELECT c " +
                                    "FROM Stock c " +
                                    "WHERE c.customer.customerId = :customerId AND " +
                                    "c.stockSymbol = :stockSymbol " +
                                    "ORDER BY c.purchaseDate DESC",
                            Stock.class)
                    .setParameter("customerId", customer.getCustomerId())
                    .setParameter("stockSymbol", stockSymbol)
                    .getResultList();

            if (stocks.isEmpty()) {
                System.out.println("No stocks found for customer ID: " + customer.getCustomerId());
                return null;
            } else {
                return stocks;
            }
        } catch (PersistenceException e) {
            System.err.println("Error saving stock: " + e.getMessage());
            throw new BankException("Error saving stock: " + e.getMessage());
        }
    }

    public void delete(Stock stock) throws BankException {
        try {
            entityManager.remove(stock);
        } catch (PersistenceException e) {
            System.err.println("Error deleting stock: " + e.getMessage());
            throw new BankException("Error deleting stock: " + e.getMessage());
        }
    }
}
