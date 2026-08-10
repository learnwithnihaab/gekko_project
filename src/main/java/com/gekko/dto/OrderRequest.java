package com.gekko.dto;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * DTO for creating an order. This models the payload Storefront or Argon would POST to Gekko.
 */
public class OrderRequest {

    @NotNull
    private String externalOrderId; // sales order number from store

    @NotNull
    private String customerExternalId; // customer id in frontend store / Argon

    @NotNull
    private String productCode;

    @NotNull
    private BigDecimal amount;

    @NotNull
    private String currency;

    // getters/setters

    public String getExternalOrderId() {
        return externalOrderId;
    }

    public void setExternalOrderId(String externalOrderId) {
        this.externalOrderId = externalOrderId;
    }

    public String getCustomerExternalId() {
        return customerExternalId;
    }

    public void setCustomerExternalId(String customerExternalId) {
        this.customerExternalId = customerExternalId;
    }

    public String getProductCode() {
        return productCode;
    }

    public void setProductCode(String productCode) {
        this.productCode = productCode;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }
}
