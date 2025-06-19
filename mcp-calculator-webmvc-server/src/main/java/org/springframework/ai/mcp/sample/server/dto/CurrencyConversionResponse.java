package org.springframework.ai.mcp.sample.server.dto;

/**
 * Data Transfer Object for currency conversion responses.
 * Contains both input details and the conversion result.
 */
public class CurrencyConversionResponse {

    private double originalAmount;
    private String from;
    private String to;
    private double convertedAmount;
    private double exchangeRate;

    public CurrencyConversionResponse() {
    }

    public CurrencyConversionResponse(double originalAmount, String from, String to, 
                                    double convertedAmount, double exchangeRate) {
        this.originalAmount = originalAmount;
        this.from = from;
        this.to = to;
        this.convertedAmount = convertedAmount;
        this.exchangeRate = exchangeRate;
    }

    public double getOriginalAmount() {
        return originalAmount;
    }

    public void setOriginalAmount(double originalAmount) {
        this.originalAmount = originalAmount;
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

    public double getConvertedAmount() {
        return convertedAmount;
    }

    public void setConvertedAmount(double convertedAmount) {
        this.convertedAmount = convertedAmount;
    }

    public double getExchangeRate() {
        return exchangeRate;
    }

    public void setExchangeRate(double exchangeRate) {
        this.exchangeRate = exchangeRate;
    }
}
