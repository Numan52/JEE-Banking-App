package net.froihofer.common;

import net.froihofer.common.dtos.CustomerDto;
import net.froihofer.common.dtos.StockDto;

import java.math.BigDecimal;
import java.util.List;

public interface BankService {
    public void addCustomer(CustomerDto customerDto);
    public CustomerDto findCustomer(String customerId);
    public CustomerDto findCustomerByName(String firstName, String lastName);
    public List<StockDto> findStock(String companyName) throws BankException;
    public BigDecimal  buyStock(long customerId, String stockSymbol, int shares);
    public BigDecimal  sellStock(long customerId, String stockSymbol, int shares);
    public List<StockDto> getCustomerPortfolio(long customerId);
    public BigDecimal getInvestableVolume();
}
