package net.froihofer.common;

import jakarta.ejb.Remote;
import net.froihofer.common.dtos.CustomerDto;
import net.froihofer.common.dtos.StockDto;

import java.math.BigDecimal;
import java.util.List;

@Remote
public interface BankService {
    public String getUserRole();
    public String addCustomer(CustomerDto customerDto);
    public CustomerDto findCustomer(String customerId);
    public List<CustomerDto> findCustomerByName(String firstName, String lastName); // Multiple Customer can have same Name -> List<Customer>
    public List<StockDto> findStock(String companyName) throws BankException;
    public String  buyStock(long customerId, String stockSymbol, int shares);
    public String  sellStock(long customerId, String stockSymbol, int shares);
    public List<StockDto> getCustomerPortfolio(long customerId);
    public BigDecimal getInvestableVolume();
}
