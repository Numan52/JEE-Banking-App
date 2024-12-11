package net.froihofer.ejb.bank.service;

import jakarta.xml.ws.BindingProvider;
import net.froihofer.common.BankException;
import net.froihofer.dsfinance.ws.trading.api.PublicStockQuote;
import net.froihofer.dsfinance.ws.trading.api.TradingWSException_Exception;
import net.froihofer.dsfinance.ws.trading.api.TradingWebService;
import net.froihofer.dsfinance.ws.trading.api.TradingWebServiceService;

import java.util.ArrayList;
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

    //Get public stock quote list
    public static PublicStockQuote getPSQ(String symbol) throws BankException {

        List<String> symbols = new ArrayList<>();
        symbols.add(symbol);
        List<PublicStockQuote> publicStockQuoteList;

        try {
            publicStockQuoteList = TradingServicesImpl.getTradingWebService().getStockQuotes(symbols);
        } catch (TradingWSException_Exception exception){
            throw new BankException("Could not retrieve stocks information.\n" + exception.getMessage());
        }
        return publicStockQuoteList.get(0);
    }
}
