package net.froihofer.ejb.bank.service;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import jakarta.annotation.security.DeclareRoles;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ejb.EJBException;
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

import static java.lang.System.in;

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

    @PostConstruct
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
    @RolesAllowed({"employee"})
    public String addCustomer(CustomerDto customerDto) throws BankException {

        WildflyAuthDBHelper wildflyAuthDBHelper;
        List<Customer> customerList = customerDAO.findCustomerByName(customerDto.getFirstName(), customerDto.getLastName());;

        try {
            for(Customer customer : customerList )
            {
                if (Objects.equals(customer.getAddress(), customerDto.getAddress()))
                {
                    throw new BankException("Customer already exists at this address!");
                }
            }

            if (customerList.isEmpty()) {
                Customer customer = new Customer(customerDto.getFirstName(), customerDto.getLastName(), customerDto.getAddress());
                customerDAO.persist(customer);
                wildflyAuthDBHelper = new WildflyAuthDBHelper(new File(System.getenv("JBOSS_HOME")));
                wildflyAuthDBHelper.addUser(String.valueOf(customer.getCustomerId()), customerDto.getPassword(), new String[]{"customer"});
                return "Added " + customer.getCustomerId();
            } else {
                throw new BankException("User already exists");
            }
        } catch (IOException | PersistenceException e) {
            throw new BankException("Could not add customer");
        }
    }

    @Override
    public long addCustomer(String firstname, String lastname, String address, String password) throws BankException {
        return 0;
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
            if (stocks == null || stocks.isEmpty()) {
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
            if (stocks == null || stocks.isEmpty()) {
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
