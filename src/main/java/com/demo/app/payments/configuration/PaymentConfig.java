package com.demo.app.payments.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

@Configuration
public class PaymentConfig {

    @Value("${payment.supported-currencies}")
    private String supportedCurrencies;

    @Bean
    public Set<String> supportedCurrencies() {
        return Arrays.stream(supportedCurrencies.split(","))
                .map(String::trim)
                .map(currency -> currency.toUpperCase(Locale.ROOT))
                .collect(Collectors.toUnmodifiableSet());
    }
}
