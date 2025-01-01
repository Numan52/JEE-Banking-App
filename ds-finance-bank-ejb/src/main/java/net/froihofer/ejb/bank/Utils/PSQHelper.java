package net.froihofer.ejb.bank.Utils;

import net.froihofer.dsfinance.ws.trading.api.PublicStockQuote;
import net.froihofer.ejb.bank.entity.Customer;
import net.froihofer.ejb.bank.entity.Stock;

import java.time.LocalDate;

public class PSQHelper {
    public static Stock psqToStock(PublicStockQuote publicStockQuote, Customer customer, int quantity) {
        return new Stock(
                customer,
                publicStockQuote.getSymbol(),
                quantity,
                publicStockQuote.getLastTradePrice(),
                LocalDate.now()
        );
    }
}
