package net.froihofer.common.dtos;

import java.math.BigDecimal;
import java.util.Date;

public class StockDto {
    private long shareId;
    private long customerId;
    private String stockSymbol;
    private int quantity;
    private BigDecimal  pricePerShare;
    private Date purchaseDate;

    public StockDto(long shareId, long customerId, String stockSymbol, int quantity, BigDecimal pricePerShare, Date purchaseDate) {
        this.shareId = shareId;
        this.customerId = customerId;
        this.stockSymbol = stockSymbol;
        this.quantity = quantity;
        this.pricePerShare = pricePerShare;
        this.purchaseDate = purchaseDate;
    }

    public long getShareId() {
        return shareId;
    }

    public void setShareId(long shareId) {
        this.shareId = shareId;
    }

    public long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(long customerId) {
        this.customerId = customerId;
    }

    public String getStockSymbol() {
        return stockSymbol;
    }

    public void setStockSymbol(String stockSymbol) {
        this.stockSymbol = stockSymbol;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public BigDecimal  getPricePerShare() {
        return pricePerShare;
    }

    public void setPricePerShare(BigDecimal  pricePerShare) {
        this.pricePerShare = pricePerShare;
    }

    public Date getPurchaseDate() {
        return purchaseDate;
    }

    public void setPurchaseDate(Date purchaseDate) {
        this.purchaseDate = purchaseDate;
    }
}
