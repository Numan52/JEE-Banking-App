package net.froihofer.ejb.bank.service;

import jakarta.xml.ws.BindingProvider;
import net.froihofer.common.BankException;
import net.froihofer.dsfinance.ws.trading.api.PublicStockQuote;
import net.froihofer.dsfinance.ws.trading.api.TradingWSException_Exception;
import net.froihofer.dsfinance.ws.trading.api.TradingWebService;
import net.froihofer.dsfinance.ws.trading.api.TradingWebServiceService;
import net.froihofer.ejb.bank.entity.Bank;

import java.math.BigDecimal;
import java.util.List;

public class TradingServicesImpl {
    private static final String WEB_SERVICE_URL = "https://edu.dedisys.org/ds-finance/ws/TradingService";
    private static final String WEB_SERVICE_USERNAME = "csdc25vz_06";
    private static final String WEB_SERVICE_PASSWORD = "io8Thoaphuh";

    private static TradingWebService tradingWebService;

    //Connection to trading service
    public static TradingWebService getTradingWebService() {

        if (tradingWebService == null) {
            TradingWebServiceService tradingWebServiceService = new TradingWebServiceService();
            tradingWebService = tradingWebServiceService.getTradingWebServicePort();
            ((BindingProvider) tradingWebService).getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY,
                    WEB_SERVICE_URL);
            ((BindingProvider) tradingWebService).getRequestContext().put(BindingProvider.USERNAME_PROPERTY, WEB_SERVICE_USERNAME);
            ((BindingProvider) tradingWebService).getRequestContext().put(BindingProvider.PASSWORD_PROPERTY, WEB_SERVICE_PASSWORD);

        }
        return tradingWebService;
    }

    //Search for stock quotes based on a part of the company name.
    public static List<PublicStockQuote> getPSQByCompanyName(String partOfCompanyName) throws BankException {
        List<PublicStockQuote> publicStockQuoteList;
        try {
            publicStockQuoteList = TradingServicesImpl.getTradingWebService().findStockQuotesByCompanyName(partOfCompanyName);
        } catch (TradingWSException_Exception exception){
            throw new BankException("Could not retrieve stocks information.\n" + exception.getMessage());
        }
        return publicStockQuoteList;
    }


    public static BigDecimal buyStock(String symbol, int shares) throws BankException{
        BigDecimal pricePerShare;
        try {
            pricePerShare = TradingServicesImpl.getTradingWebService().buy(symbol, shares);
        } catch (TradingWSException_Exception e) {
            System.err.println("Could not buy stock with symbol: " + symbol + "\n" + e.getMessage());
            throw new BankException("Could not buy stock with symbol: " + symbol + "\n" + e.getMessage());
        }

        return pricePerShare;
    }


    public static List<PublicStockQuote> getPSQBySymbol(List<String> symbols) throws BankException {
        List<PublicStockQuote> publicStockQuoteList;
        try {
            publicStockQuoteList = TradingServicesImpl.getTradingWebService().getStockQuotes(symbols);
        } catch (TradingWSException_Exception e){
            System.err.println("Could not get psq by symbol"  + e.getMessage());
            throw new BankException("Could not retrieve stocks information.\n" + e.getMessage());
        }
        return publicStockQuoteList;
    }
}
