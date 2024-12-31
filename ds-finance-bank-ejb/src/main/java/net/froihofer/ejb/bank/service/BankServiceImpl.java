package net.froihofer.ejb.bank.service;

import jakarta.annotation.Resource;
import jakarta.annotation.security.DeclareRoles;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ejb.SessionContext;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import net.froihofer.common.BankException;
import net.froihofer.common.BankService;
import net.froihofer.common.dtos.CustomerDto;
import net.froihofer.common.dtos.StockDto;
import net.froihofer.ejb.bank.dao.BankDAO;
import net.froihofer.ejb.bank.dao.CustomerDAO;
import net.froihofer.ejb.bank.entity.Customer;
import net.froihofer.dsfinance.ws.trading.api.PublicStockQuote;
import net.froihofer.dsfinance.ws.trading.api.TradingWSException_Exception;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Stateless(name = "BankService")
@DeclareRoles({"customer", "employee"}) //RolesAllowed need to be changed at each methode!
public class BankServiceImpl implements BankService {

    @Resource
    private SessionContext sessionContext;
    @Inject
    CustomerDAO customerDAO;

    @Inject
    BankDAO bankDAO;

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
    public void addCustomer(CustomerDto customerDto) {
        // TODO: add customer to database
        Customer customer = new Customer(customerDto.getFirstName(),customerDto.getLastName(),customerDto.getAddress());
        System.out.println("Saving new customer...");
        customerDAO.persist(customer);
        System.out.println("New customer saved: " + customer);

        System.out.println(customerDto.getFirstName() + " added!");
    }

    @Override
    @RolesAllowed({"employee", "customer"})
    public CustomerDto findCustomer(String customerId) {

        return null;
    }

    @Override
    @RolesAllowed({"employee", "customer"})
    public List<CustomerDto> findCustomerByName(String firstName, String lastName) {
        return null;
    }

    @Override
    @RolesAllowed({"employee", "customer"})
    public List<StockDto> findStock(String companyName) throws BankException {
       if (companyName == null || companyName.isEmpty()) {
            throw  new BankException("Stock Symbol is empty");
        }
       String symbol = TradingServicesImpl.getPSQ("a").getSymbol();
       System.out.println("The first symbol found: " + symbol);
       /*
        List<StockDto> stockDtos = new ArrayList<StockDto>();
        try {
            List<PublicStockQuote> stocks = new PublicStockQuote(); // TODO: Von Web Service Klasse holen
            for (PublicStockQuote stock : stocks) {
                stockDtos.add(new StockDto(stock.getSymbol(), stock.getCompanyName(), stock.getLastTradePrice()));
            }


        } catch (TradingWSException_Exception e) {
            throw new BankException("An error occurred while fetching stocks: " + e.getMessage());
        }

        return stockDtos;
        */
        return null;
    }

    @Override
    @RolesAllowed({"employee", "customer"})
    public BigDecimal buyStock(long customerId, String stockSymbol, int shares) {

        return null;
    }

    @Override
    @RolesAllowed({"employee", "customer"})
    public BigDecimal sellStock(long customerId, String stockSymbol, int shares) {
        return null;
    }

    @Override
    @RolesAllowed({"employee", "customer"})
    public List<StockDto> getCustomerPortfolio(long customerId) {
        return List.of();
    }

    @Override
    @RolesAllowed({"employee", "customer"})
    public BigDecimal getInvestableVolume() {
        return bankDAO.getAvailableVolume();
    }
}
