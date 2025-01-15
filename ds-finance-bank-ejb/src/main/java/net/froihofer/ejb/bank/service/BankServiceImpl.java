package net.froihofer.ejb.bank.service;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import jakarta.annotation.security.DeclareRoles;
import jakarta.annotation.security.RolesAllowed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jakarta.ejb.SessionContext;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import jakarta.persistence.PersistenceException;
import jakarta.transaction.Transactional;
import net.froihofer.common.BankException;
import net.froihofer.common.BankService;
import net.froihofer.common.dtos.CustomerDto;
import net.froihofer.common.dtos.StockDto;
import net.froihofer.ejb.bank.Utils.PSQHelper;
import net.froihofer.ejb.bank.dao.BankDAO;
import net.froihofer.ejb.bank.dao.CustomerDAO;
import net.froihofer.ejb.bank.dao.StockDAO;
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

    private static final Logger log = LoggerFactory.getLogger(BankServiceImpl.class);

    @PostConstruct // automatically called after dependency injection to initialize the bank
    private void initBank() {
        bankDAO.createInitialBank();
    }

    @Override
    @RolesAllowed({"employee", "customer"})
    public String getUserRole() // To get Role of client
    {
        log.info("Getting Role for User: {}", getCurrentUserId());
        if(sessionContext.isCallerInRole("customer"))
        {
            return "customer";
        }
        else if(sessionContext.isCallerInRole("employee"))
        {
            return "employee";
        }
        log.error("User {} has not role 'customer' or 'employee'", getCurrentUserId());
        return "Unauthorized!"; //exception?
    }
    @Override
    @RolesAllowed({"employee", "customer"})
    public long getCurrentUserId() // To get current userId
    {
        try {
            String userIdString = sessionContext.getCallerPrincipal().getName();
            return Long.parseLong(userIdString);
        } catch (NumberFormatException e) {
            log.error("Error: Unable to convert user ID to long. Input was: {}", sessionContext.getCallerPrincipal().getName()); // wenn der string keine gültige Zahl ist
            return -1;
        }
    }

    @Override
    @RolesAllowed({"employee"})
    public long addCustomer(CustomerDto customerDto) throws BankException {
        log.info("Adding new customer");
        if (customerDto.getFirstName() == null || customerDto.getFirstName().isBlank()) {
            throw new BankException("Firstname is empty!");
        }
        if (customerDto.getLastName() == null || customerDto.getLastName().isBlank()) {
            throw new BankException("Lastname is empty!");
        }
        if (customerDto.getAddress() == null || customerDto.getAddress().isBlank()) {
            throw new BankException("Address is empty!");
        }
        if (customerDto.getPassword() == null || customerDto.getPassword().isBlank()) {
            throw new BankException("Password is empty!");
        }
        customerDto.setFirstName(customerDto.getFirstName().trim());
        customerDto.setLastName(customerDto.getLastName().trim());
        customerDto.setAddress(customerDto.getAddress().trim());
        log.info("Customer details are valid");

        WildflyAuthDBHelper wildflyAuthDBHelper;
        List<Customer> customerList = customerDAO.findCustomerByName(customerDto.getFirstName(), customerDto.getLastName());;

        try {
            for(Customer customer : customerList )
            {
                if (Objects.equals(customer.getAddress().toLowerCase(), customerDto.getAddress().toLowerCase()))
                {
                    log.error("Customer already exists");
                    throw new BankException("Customer already exists with this address!");
                }
            }
            Customer customer = new Customer(customerDto.getFirstName(), customerDto.getLastName(), customerDto.getAddress());
            customerDAO.persist(customer);
            wildflyAuthDBHelper = new WildflyAuthDBHelper(new File(System.getenv("JBOSS_HOME")));
            wildflyAuthDBHelper.addUser(String.valueOf(customer.getCustomerId()), customerDto.getPassword(), new String[]{"customer"});
            log.info("Customer added successfully id={}", customer.getCustomerId());
            return customer.getCustomerId();

        } catch (IOException | PersistenceException e) {
            log.error("Error while adding customer: {}", e.getMessage());
            throw new BankException("Could not add customer");
        }
    }

    @Override
    @RolesAllowed({"employee"})
    public CustomerDto findCustomer(long customerId) throws BankException {
        log.info("searching for customer with ID={}", customerId);
        Customer customer = customerDAO.findCustomerById(customerId);
        return new CustomerDto(customer.getCustomerId(), customer.getFirstName(), customer.getLastName(), customer.getAddress());
    }

    @Override
    @RolesAllowed({"employee"})
    public List<CustomerDto> findCustomerByName(String firstName, String lastName) throws BankException {
        if (firstName == null || firstName.isBlank()) {
            throw new BankException("Firstname is empty!");
        }
        if (lastName == null || lastName.isBlank()) {
            throw new BankException("Lastname is empty!");
        }
        firstName = firstName.trim();
        lastName = lastName.trim();

        List<CustomerDto> customerDtos = new ArrayList<>();
        List<Customer> customers = customerDAO.findCustomerByName(firstName, lastName);
        if (customers.isEmpty()) {
            throw new BankException("No customers found");
        }
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
            log.error("Company name is empty by findStock.");
            throw  new BankException("Stock Symbol is empty");
        }

        log.info("Attempting to find stocks for company: {}", companyName);
        List<StockDto> stockDtos = new ArrayList<StockDto>();

        try {
            List<PublicStockQuote> stockQuotes = TradingServicesImpl.getPSQByCompanyName(companyName);
            if (stockQuotes.isEmpty()) {
                log.error("No stock found for company: {}", companyName);
                throw new BankException("No stock found form " + companyName);
            }
            for (PublicStockQuote stock : stockQuotes) {
                stockDtos.add(new StockDto(stock.getSymbol(), stock.getCompanyName(), stock.getLastTradePrice()));
            }

        } catch (BankException e) {
            log.error("Error fetching stocks: {}", e.getMessage());
            throw e;
        }

        log.info("Found {} stocks for company: {}", stockDtos.size(), companyName);
        return stockDtos;
    }

    @Override
    @RolesAllowed({"employee", "customer"})
    @Transactional
    public String buyStock(long customerId, String stockSymbol, int shares) throws BankException{
        if (stockSymbol == null || stockSymbol.isEmpty()) {
            log.error("Stock symbol is empty");
            throw new BankException("Stock symbol is empty");
        }

        if (shares < 0) {
            log.error("Number of shares must be positive");
            throw new BankException("Number of shares must be positive");
        }

        stockSymbol = stockSymbol.trim();
        log.info("Attempting to buy stock. Customer ID: {}, Stock Symbol: {}, Shares: {}", customerId, stockSymbol, shares);


        try {
            Customer customer = customerDAO.findCustomerById(customerId);
            if(sessionContext.isCallerInRole("customer")) //checking if customer is not buying for other customer
            {
                if (Long.parseLong(sessionContext.getCallerPrincipal().getName()) != customerId) {
                    throw new BankException("You are not allowed to buy stock for other customers.");
                }
            }
            List<PublicStockQuote> stocks = TradingServicesImpl.getPSQBySymbol(List.of(stockSymbol));
            if (stocks == null || stocks.isEmpty()) {
                log.error("No stock found for symbol: {}", stockSymbol);
                throw new BankException("No stock found for symbol: " + stockSymbol);
            }
            Stock stock = PSQHelper.psqToStock(stocks.get(0), customer, shares);
            stockDAO.persist(stock);
            BigDecimal totalCost = stock.getPurchasePrice().multiply(new BigDecimal(shares));
            BigDecimal availableVolume = bankDAO.getAvailableVolume();
            if (totalCost.compareTo(availableVolume) > 0) {
                log.error("Order exceeds available volume. Total Cost: {}, Available Volume: {}", totalCost, availableVolume);
                throw new BankException("Your current order exceeds the bank's currently available volume.");
            }

            BigDecimal pricePerShare = TradingServicesImpl.buyStock(stockSymbol, shares);

            stock.setPurchasePrice(pricePerShare);
            stockDAO.update(stock);


            bankDAO.updateAvailableVolume(availableVolume.subtract(totalCost));

            log.info("Successfully bought {} shares of {} for {} per share.", shares, stockSymbol, pricePerShare);

            return "Successfully bought " + shares + " shares for " + pricePerShare +
                    " per share.";

        } catch (BankException e) {
            log.error("Error buying stock: {}", e.getMessage(), e);
            throw new BankException("Error occurred. Could not buy shares:  \n"  + e.getMessage());
        }

    }

    @Override
    @RolesAllowed({"employee", "customer"})
    public String sellStock(long customerId, String stockSymbol, int shares) throws BankException {
        if (stockSymbol == null || stockSymbol.isEmpty()) {
            log.error("Stock symbol is empty");
            throw new BankException("Stock symbol is empty");
        }


        if (shares <= 0) {
            log.warn("Invalid number of shares to sell: {}", shares);
            return "Enter a number of shares greater than 0.";
        }


        stockSymbol = stockSymbol.trim();
        log.info("Attempting to sell stock. Customer ID: {}, Stock Symbol: {}, Shares: {}", customerId, stockSymbol, shares);

        try {
            Customer customer = customerDAO.findCustomerById(customerId);
            if(sessionContext.isCallerInRole("customer"))
            {
                if (Long.parseLong(sessionContext.getCallerPrincipal().getName()) != customerId) {
                    throw new BankException("You are not allowed to sell stocks for other customers.");
                }
            }

            List<Stock> stocks = stockDAO.findStockByCustomer(stockSymbol, customer);
            if (stocks == null || stocks.isEmpty()) {
                log.error("No stocks found for symbol: {} and customer ID: {}", stockSymbol, customerId);
                throw new BankException("No stocks found for symbol: " + stockSymbol);
            }

            int totalShares = stocks.stream()
                                    .map((stock -> stock.getQuantity()))
                                    .reduce(0, (a, b) -> a + b);

            if (totalShares < shares) {
                log.error("Not enough shares to sell. Total Shares: {}, Requested: {}", totalShares, shares);
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

            log.info("Successfully sold {} shares of {} for {} per share.", shares, stockSymbol, pricePerShare);
            return "Successfully sold " + shares + " shares for " + pricePerShare +
                    " per share." ;

        } catch (BankException e) {
            throw new BankException("Error occurred. Could not sell shares:  \n"  + e.getMessage());
        }
    }

    @Override
    @RolesAllowed({"employee", "customer"})
    public List<StockDto> getCustomerPortfolio(long customerId) throws BankException {
        log.info("Getting Portfolio for customer {}", customerId);
        Customer customer = customerDAO.findCustomerById(customerId);
        if(sessionContext.isCallerInRole("customer")) //checking if customer is not buying for other customer
        {
            if (Long.parseLong(sessionContext.getCallerPrincipal().getName()) != customerId) {
                throw new BankException("You are not allowed to buy stock for other customers.");
            }
        }
        List<StockDto> customerStocks = new ArrayList<>();
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

                List<PublicStockQuote> currentvalues = TradingServicesImpl.getPSQBySymbol(stockSymbols);
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
        {
            log.info("Customer {} has no stocks", customerId);
        }
        return List.of();
    }

    @Override
    @RolesAllowed({"employee"})
    public BigDecimal getInvestableVolume() throws BankException {
        try {
            return bankDAO.getAvailableVolume();
        } catch (PersistenceException e) {
            log.error("Error getting investable volume: {}", String.valueOf(e));
            throw new BankException("Error getting investable volume: " + e.getMessage());
        }

    }
}
