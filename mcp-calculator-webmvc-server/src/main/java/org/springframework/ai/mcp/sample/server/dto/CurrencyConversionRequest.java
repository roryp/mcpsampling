package org.springframework.ai.mcp.sample.server.dto;

/**
 * Data Transfer Object for currency conversion requests.
 * Contains the amount to convert and source/target currency codes (ISO 4217 format).
 */
public class CurrencyConversionRequest {

    private double amount;
    private String from;  // Source currency code (e.g., "USD")
    private String to;    // Target currency code (e.g., "EUR")

    public CurrencyConversionRequest() {
    }

    public CurrencyConversionRequest(double amount, String from, String to) {
        this.amount = amount;
        this.from = from;
        this.to = to;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }
}
