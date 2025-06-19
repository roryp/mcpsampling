/*
 * Copyright 2025 - 2025 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.ai.mcp.sample.server;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for CalculatorService arithmetic operations and currency conversion.
 */
class CalculatorServiceTest {

    private CalculatorService calculatorService;

    @BeforeEach
    void setUp() {
        calculatorService = new CalculatorService();
    }

    @Test
    void testAdd() {
        assertEquals(5.0, calculatorService.add(2.0, 3.0), 0.001);
        assertEquals(0.0, calculatorService.add(-5.0, 5.0), 0.001);
        assertEquals(-8.0, calculatorService.add(-3.0, -5.0), 0.001);
    }

    @Test
    void testSubtract() {
        assertEquals(-1.0, calculatorService.subtract(2.0, 3.0), 0.001);
        assertEquals(8.0, calculatorService.subtract(10.0, 2.0), 0.001);
        assertEquals(2.0, calculatorService.subtract(-3.0, -5.0), 0.001);
    }

    @Test
    void testMultiply() {
        assertEquals(6.0, calculatorService.multiply(2.0, 3.0), 0.001);
        assertEquals(-15.0, calculatorService.multiply(-3.0, 5.0), 0.001);
        assertEquals(0.0, calculatorService.multiply(5.0, 0.0), 0.001);
    }    @Test
    void testDivide() {
        assertEquals(2.0, calculatorService.divide(6.0, 3.0), 0.001);
        assertEquals(-2.5, calculatorService.divide(-5.0, 2.0), 0.001);
        // Test division by zero should throw exception
        assertThrows(ArithmeticException.class, () -> 
            calculatorService.divide(5.0, 0.0));
    }

    @Test
    void testDivideByZero() {
        assertThrows(ArithmeticException.class, () -> 
            calculatorService.divide(10.0, 0.0));
    }

    @Test
    void testCurrencyConversionSameCurrency() {
        // Test same currency conversion (should return same amount)
        double result = calculatorService.convertCurrency(100.0, "USD", "USD");
        assertEquals(100.0, result, 0.001);
    }

    @Test
    void testCurrencyConversionInvalidAmount() {
        // Test negative amount
        assertThrows(IllegalArgumentException.class, () -> 
            calculatorService.convertCurrency(-100.0, "USD", "EUR"));
    }

    @Test
    void testCurrencyConversionInvalidCurrency() {
        // Test null currency codes
        assertThrows(IllegalArgumentException.class, () -> 
            calculatorService.convertCurrency(100.0, null, "EUR"));
        
        assertThrows(IllegalArgumentException.class, () -> 
            calculatorService.convertCurrency(100.0, "USD", null));
        
        // Test empty currency codes
        assertThrows(IllegalArgumentException.class, () -> 
            calculatorService.convertCurrency(100.0, "", "EUR"));
        
        assertThrows(IllegalArgumentException.class, () -> 
            calculatorService.convertCurrency(100.0, "USD", ""));
    }

    @Test
    void testGetExchangeRate() {
        // Test same currency exchange rate (should be 1.0)
        double rate = calculatorService.getExchangeRate("USD", "USD");
        assertEquals(1.0, rate, 0.001);
    }
}
