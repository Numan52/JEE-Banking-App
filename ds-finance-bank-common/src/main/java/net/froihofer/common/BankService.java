package net.froihofer.common;

import jakarta.annotation.security.RolesAllowed;
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


     long addCustomer(String firstname, String lastname, String address, String password) throws BankException;

     CustomerDto findCustomer(long customerId) throws BankException;
     List<CustomerDto> findCustomerByName(String firstName, String lastName) throws BankException; // Multiple Customer can have same Name -> List<Customer>
     List<StockDto> findStock(String companyName) throws BankException;
     String  buyStock(long customerId, String stockSymbol, int shares) throws BankException;
     String  sellStock(long customerId, String stockSymbol, int shares) throws BankException;
     List<StockDto> getCustomerPortfolio(long customerId)  throws BankException;
     BigDecimal getInvestableVolume() throws BankException;
}
