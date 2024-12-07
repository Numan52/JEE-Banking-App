package net.froihofer.ejb.bank.service;

import net.froihofer.common.BankException;
import net.froihofer.common.BankService;
import net.froihofer.common.dtos.CustomerDto;
import net.froihofer.common.dtos.StockDto;
import net.froihofer.dsfinance.ws.trading.api.PublicStockQuote;
import net.froihofer.dsfinance.ws.trading.api.TradingWSException_Exception;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class BankServiceImpl implements BankService {

    @Override
    public void addCustomer(CustomerDto customerDto) {
        // TODO: add customer to database

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
    public List<StockDto> findStock(String companyName) throws BankException {
        if (companyName == null || companyName.isEmpty()) {
            throw  new BankException("Stock Symbol is empty");
        }

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
