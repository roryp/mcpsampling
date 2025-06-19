package org.springframework.ai.mcp.sample.server.dto;

/**
 * Data Transfer Object for calculation responses containing the computed result.
 */
public class CalculationResponse {

    private double result;

    public CalculationResponse() {
    }

    public CalculationResponse(double result) {
        this.result = result;
    }

    public double getResult() {
        return result;
    }

    public void setResult(double result) {
        this.result = result;
    }
}
