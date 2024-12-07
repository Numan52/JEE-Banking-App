package net.froihofer.ejb.bank.service;

import net.froihofer.common.BankService;
import net.froihofer.common.dtos.CustomerDto;
import net.froihofer.common.dtos.StockDto;

import java.math.BigDecimal;
import java.util.List;

public class BankServiceImpl implements BankService {

    @Override
    public void addCustomer(CustomerDto customerDto) {

    }

    @Override
    public CustomerDto findCustomer(String customerId) {
        return null;
    }

    @Override
    public CustomerDto findCustomerByName(String firstName, String lastName) {
        return null;
    }

    @Override
    public StockDto findStock(String stockSymbol) {
        return null;
    }

    @Override
    public BigDecimal buyStock(long customerId, String stockSymbol, int shares) {
        return null;
    }

    @Override
    public BigDecimal sellStock(long customerId, String stockSymbol, int shares) {
        return null;
    }

    @Override
    public List<StockDto> getCustomerPortfolio(long customerId) {
        return List.of();
    }

    @Override
    public BigDecimal getInvestableVolume() {
        return null;
    }
}
