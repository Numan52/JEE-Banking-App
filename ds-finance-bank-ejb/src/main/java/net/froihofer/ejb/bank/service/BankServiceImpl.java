package net.froihofer.ejb.bank.service;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import jakarta.annotation.security.DeclareRoles;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ejb.SessionContext;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import jakarta.persistence.PersistenceException;
import jakarta.transaction.Transactional;
import net.froihofer.common.BankException;
import net.froihofer.common.BankService;
import net.froihofer.common.dtos.CustomerDto;
import net.froihofer.common.dtos.StockDto;
import net.froihofer.dsfinance.ws.trading.api.TradingWSException_Exception;
import net.froihofer.ejb.bank.Utils.PSQHelper;
import net.froihofer.ejb.bank.dao.BankDAO;
import net.froihofer.ejb.bank.dao.CustomerDAO;
import net.froihofer.ejb.bank.dao.StockDAO;
import net.froihofer.ejb.bank.entity.Customer;
import net.froihofer.dsfinance.ws.trading.api.PublicStockQuote;
import net.froihofer.ejb.bank.entity.Stock;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Stateless(name = "BankService")
@DeclareRoles({"customer", "employee"}) //RolesAllowed need to be changed at each methode!
public class BankServiceImpl implements BankService {

    @Resource
    private SessionContext sessionContext;
    @Inject
    CustomerDAO customerDAO;

    @Inject
    BankDAO bankDAO;

    @Inject
    StockDAO stockDAO;

    @PostConstruct
    private void initBank() {
        bankDAO.createInitialBank();
    }

    @Override
    @RolesAllowed({"employee", "customer"})
    public String getUserRole() // To get Role of client
    {
        if(sessionContext.isCallerInRole("customer"))
        {
            return "customer";
        }
        else if(sessionContext.isCallerInRole("employee"))
        {
            return "employee";
        }
        return "Unauthorized!";
    }
    @Override
    @RolesAllowed({"employee", "customer"})
    public long getCurrentUserId() // To get current userId
    {
        try {
            String userIdString = sessionContext.getCallerPrincipal().getName();
            return Long.parseLong(userIdString);
        } catch (NumberFormatException e) {
            // Fehlerbehandlung: Wenn der String keine gültige Zahl ist
            System.err.println("Error: Unable to convert user ID to long. Input was: " + sessionContext.getCallerPrincipal().getName());
            e.printStackTrace();
            return -1;
        }
    }
    @Override
    @RolesAllowed({"employee"})
    public String addCustomer(CustomerDto customerDto) {
        try {
            Customer customer = new Customer(customerDto.getFirstName(),customerDto.getLastName(),customerDto.getAddress());
            System.out.println("Saving new customer...");
            customerDAO.persist(customer);
            System.out.println("New customer saved: " + customer);
        } catch (PersistenceException e) {
            return "Error occurred while adding customer";
        }

        return customerDto.getFirstName() + " added successfully!";
    }

    @Override
    @RolesAllowed({"employee"})
    public CustomerDto findCustomer(String customerId) {

        return null;
    }

    @Override
    @RolesAllowed({"employee"})
    public List<CustomerDto> findCustomerByName(String firstName, String lastName) throws BankException {
        List<CustomerDto> customerDtos = new ArrayList<>();
        List<Customer> customers = customerDAO.findCustomerByName(firstName, lastName);

        for (Customer customer : customers) {
            customerDtos.add(new CustomerDto(
                    customer.getCustomerId(),
                    customer.getFirstName(),
                    customer.getLastName(),
                    customer.getAddress()
            ));
        }

        return customerDtos;
    }

    @Override
    @RolesAllowed({"employee", "customer"})
    public List<StockDto> findStock(String companyName) throws BankException {
        if (companyName == null || companyName.isEmpty()) {
            throw  new BankException("Stock Symbol is empty");
        }

        List<StockDto> stockDtos = new ArrayList<StockDto>();

        try {
            List<PublicStockQuote> stockQuotes = TradingServicesImpl.getPSQByCompanyName(companyName);
            System.out.println("The first stock symbol found was: " + stockQuotes.get(0).getSymbol());
            for (PublicStockQuote stock : stockQuotes) {
                stockDtos.add(new StockDto(stock.getSymbol(), stock.getCompanyName(), stock.getLastTradePrice()));
            }

        } catch (BankException e) {
            System.err.println("Error fetching stocks: " + e.getMessage());
            throw e;
        }

        return stockDtos;
    }

    @Override
    @RolesAllowed({"employee", "customer"})
    @Transactional
    public String buyStock(long customerId, String stockSymbol, int shares) throws BankException{
        stockSymbol = stockSymbol.trim();
        System.out.println("Stocksymbol: ." + stockSymbol + ".");

        try {
            Customer customer = customerDAO.findCustomerById(customerId);
            if(sessionContext.isCallerInRole("customer")) //checking if customer is not buying for other customer
            {
                if (Long.parseLong(sessionContext.getCallerPrincipal().getName()) != customerId) {
                    throw new BankException("You are not allowed to buy stock for other customers.");
                }
            }
            List<PublicStockQuote> stocks = TradingServicesImpl.getPSQBySymbol(List.of(stockSymbol));
            if (stocks.isEmpty()) {
                throw new BankException("No stock found for symbol: " + stockSymbol);
            }
            Stock stock = PSQHelper.psqToStock(stocks.get(0), customer, shares);
            System.out.println(stocks.size());

            stockDAO.persist(stock);
            BigDecimal pricePerShare = TradingServicesImpl.buyStock(stockSymbol, shares);

            BigDecimal totalCost = pricePerShare.multiply(new BigDecimal(shares));
            BigDecimal availableVolume = bankDAO.getAvailableVolume();
            bankDAO.updateAvailableVolume(availableVolume.subtract(totalCost));

            return "Successfully bought " + shares + " shares for " + pricePerShare +
                    " per share." ;

        } catch (BankException e) {
            throw new BankException("Error occurred. Could not buy shares:  \n"  + e.getMessage());
        }

    }

    @Override
    @RolesAllowed({"employee", "customer"})
    public String sellStock(long customerId, String stockSymbol, int shares) {
        return null;
    }

    @Override
    @RolesAllowed({"employee", "customer"})
    public List<StockDto> getCustomerPortfolio(long customerId) {
        List<StockDto> customerStocks = new ArrayList<>();
        try{
            List<Stock> stocks = stockDAO.getAllStocks(customerId);
            if (stocks != null)
            {
                Map<String, Integer> stockSummary = new HashMap<>();

                for (Stock stock : stocks) {
                    String symbol = stock.getStockSymbol();
                    int quantity = stock.getQuantity();
                    // addiere Anzahl zum aktuellen Wert in der mapliste
                    stockSummary.put(symbol, stockSummary.getOrDefault(symbol, 0) + quantity);
                }
                List<String> stockSymbols = stocks.stream()
                        .map(Stock::getStockSymbol) // holt symbol
                        .distinct()
                        .toList();

                List<PublicStockQuote> currentvalues = TradingServicesImpl.getTradingWebService().getStockQuotes(stockSymbols);
                for (Map.Entry<String, Integer> entry : stockSummary.entrySet()) {
                    String symbol = entry.getKey();
                    int totalQuantity = entry.getValue();

                    for (PublicStockQuote stock : currentvalues) {
                        if (symbol.equals(stock.getSymbol())) { // checkt ob symbol gleich ist
                            customerStocks.add(new StockDto(customerId, stock.getSymbol(), stock.getCompanyName(), totalQuantity, stock.getLastTradePrice()));
                            break; // Verlasse die innere Schleife, da das Symbol gefunden wurde
                        }
                    }
                }
                return customerStocks;
            }
        }catch (BankException e) {

        } catch (TradingWSException_Exception e) {
            throw new RuntimeException(e);
        }
        return List.of();
    }

    @Override
    @RolesAllowed({"employee", "customer"})
    public BigDecimal getInvestableVolume() throws BankException {
        try {
            return bankDAO.getAvailableVolume();
        } catch (PersistenceException e) {
            System.err.println("Error getting investable volume: " + e);
            e.printStackTrace();
            throw new BankException("Error getting investable volume: " + e.getMessage());
        }

    }
}
