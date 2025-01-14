package net.froihofer.common;

import jakarta.ejb.Remote;
import net.froihofer.common.dtos.CustomerDto;
import net.froihofer.common.dtos.StockDto;

import java.math.BigDecimal;
import java.util.List;

@Remote
public interface BankService {
     String getUserRole();
     long getCurrentUserId();
     String addCustomer(CustomerDto customerDto);
     CustomerDto findCustomer(String customerId);
     List<CustomerDto> findCustomerByName(String firstName, String lastName) throws BankException; // Multiple Customer can have same Name -> List<Customer>
     List<StockDto> findStock(String companyName) throws BankException;
     String  buyStock(long customerId, String stockSymbol, int shares) throws BankException;
     String  sellStock(long customerId, String stockSymbol, int shares) throws BankException;
     List<StockDto> getCustomerPortfolio(long customerId);
     BigDecimal getInvestableVolume() throws BankException;
}
