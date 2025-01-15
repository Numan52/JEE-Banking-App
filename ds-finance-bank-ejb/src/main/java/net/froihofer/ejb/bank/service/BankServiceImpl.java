package net.froihofer.ejb.bank.service;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import jakarta.annotation.security.DeclareRoles;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ejb.EJBException;
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
import net.froihofer.ejb.bank.entity.Bank;
import net.froihofer.ejb.bank.entity.Customer;
import net.froihofer.dsfinance.ws.trading.api.PublicStockQuote;
import net.froihofer.ejb.bank.entity.Stock;
import net.froihofer.util.jboss.WildflyAuthDBHelper;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;

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
            System.out.println("Userid String: " + userIdString);
            return Long.parseLong(userIdString);
        } catch (NumberFormatException e) {
            // Fehlerbehandlung: Wenn der String keine gültige Zahl ist
            System.err.println("Error: Unable to convert user ID to long. Input was: " + sessionContext.getCallerPrincipal().getName());
            e.printStackTrace();
            return -1;
        }
    }

    @Override
    public String addCustomer(CustomerDto customerDto) {
        return "";
    }

    @RolesAllowed({"employee"})
    @Override
    public void addCustomer(String username, String firstname, String lastname, String address, String password) throws BankException {
        if (username == null || username.isBlank()) {
            throw new BankException("Username is empty!");
        }
        if (firstname == null || firstname.isBlank()) {
            throw new BankException("Firstname is empty!");
        }
        if (lastname == null || lastname.isBlank()) {
            throw new BankException("Lastname is empty!");
        }
        if (address == null || address.isBlank()) {
            throw new BankException("Address is empty!");
        }
        if (password == null || password.isBlank()) {
            throw new BankException("Password is empty!");
        }

        WildflyAuthDBHelper wildflyAuthDBHelper;
        List<Customer> customerList;

        try {
            customerList = customerDAO.findCustomerByName(firstname, lastname);
        } catch (EJBException ex) {
            throw new BankException("Error " + ex);
        }


        try {
            if (customerList.isEmpty()) {
                customerDAO.persist(new Customer(username, lastname, address));
                wildflyAuthDBHelper = new WildflyAuthDBHelper(new File(System.getenv("JBOSS_HOME")));
                wildflyAuthDBHelper.addUser(username, password, new String[]{getUserRole()});
                //return "User successfully added!";
            } else {
                throw new BankException("User already exists");
            }
        } catch (IOException iox) {
            throw new BankException("Error" + iox);
        }

        /*
        try {
            Customer customer = new Customer(customerDto.getFirstName(),customerDto.getLastName(),customerDto.getAddress());
            System.out.println("Saving new customer...");
            customerDAO.persist(customer);
            System.out.println("New customer saved: " + customer);
        } catch (PersistenceException e) {
            return "Error occurred while adding customer";
        }
        return customerDto.getFirstName() + " added successfully!";
        */

    }

    @Override
    @RolesAllowed({"employee"})
    public CustomerDto findCustomer(long customerId) throws BankException {

        Customer customer = customerDAO.findCustomerById(customerId);
        return new CustomerDto(customer.getCustomerId(), customer.getFirstName(), customer.getLastName(), customer.getAddress());
    }

    @Override
    @RolesAllowed({"employee"})
    public List<CustomerDto> findCustomerByName(String firstName, String lastName) throws BankException {
        firstName = firstName.trim();
        lastName = lastName.trim();
        if(firstName.isEmpty() || lastName.isEmpty())
        {
            throw new BankException("First name and last name cannot be empty");
        }

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
            if (stockQuotes.isEmpty()) {
                throw new BankException("No stock found form " + companyName);
            }
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

            stock.setPurchasePrice(pricePerShare);
            stockDAO.update(stock);

            BigDecimal totalCost = pricePerShare.multiply(new BigDecimal(shares));
            BigDecimal availableVolume = bankDAO.getAvailableVolume();

            if (totalCost.compareTo(availableVolume) > 0) {
                throw new BankException();
            }


            bankDAO.updateAvailableVolume(availableVolume.subtract(totalCost));

            return "Successfully bought " + shares + " shares for " + pricePerShare +
                    " per share." ;

        } catch (BankException e) {
            throw new BankException("Error occurred. Could not buy shares:  \n"  + e.getMessage());
        }

    }

    @Override
    @RolesAllowed({"employee", "customer"})
    public String sellStock(long customerId, String stockSymbol, int shares) throws BankException {
        if (shares <= 0) {
            return "Enter a number of shares greater than 0.";
        }
        stockSymbol = stockSymbol.trim();

        try {
            Customer customer = customerDAO.findCustomerById(customerId);
            if(sessionContext.isCallerInRole("customer"))
            {
                if (Long.parseLong(sessionContext.getCallerPrincipal().getName()) != customerId) {
                    throw new BankException("You are not allowed to sell stocks for other customers.");
                }
            }

            List<Stock> stocks = stockDAO.findStockByCustomer(stockSymbol, customer);
            if (stocks.isEmpty()) {
                throw new BankException("No stocks found for symbol: " + stockSymbol +
                        "\n and user: " + customer.getFirstName());
            }

            int totalShares = stocks.stream()
                                    .map((stock -> stock.getQuantity()))
                                    .reduce(0, (a, b) -> a + b);

            if (totalShares < shares) {
                throw new BankException("You don't have enough shares to sell.");
            }

            BigDecimal pricePerShare = TradingServicesImpl.sellStock(stockSymbol, shares);

            int sharesToSell = shares;

            // stock entity löschen oder die share-Anzahl aktualisieren
            for (Stock stock : stocks) {
                if (sharesToSell == 0) break;

                if (stock.getQuantity() <= sharesToSell) {
                    sharesToSell -= stock.getQuantity();
                    stockDAO.delete(stock);
                } else {
                    stock.setQuantity(stock.getQuantity() - sharesToSell);
                    stockDAO.update(stock);
                    sharesToSell = 0;

                }
            }

            BigDecimal availableVolume = bankDAO.getAvailableVolume();
            BigDecimal totalCost = pricePerShare.multiply(BigDecimal.valueOf(shares));
            bankDAO.updateAvailableVolume(availableVolume.add(totalCost));

            return "Successfully sold " + shares + " shares for " + pricePerShare +
                    " per share." ;

        } catch (BankException e) {
            throw new BankException("Error occurred. Could not sell shares:  \n"  + e.getMessage());
        }
    }

    @Override
    @RolesAllowed({"employee", "customer"})
    public List<StockDto> getCustomerPortfolio(long customerId) throws BankException {
        List<StockDto> customerStocks = new ArrayList<>();
        try{
            findCustomer(customerId); //check if user exist
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
        } catch (TradingWSException_Exception e) {
            throw new BankException("Something went wrong while trying to get current values for shares from Trading Service " + e.getMessage());
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
