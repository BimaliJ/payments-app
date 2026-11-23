package com.demo.app.payments.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import tools.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import tools.jackson.dataformat.xml.annotation.JacksonXmlProperty;

import java.math.BigDecimal;

@JsonIgnoreProperties(ignoreUnknown = true)
public record FraudResult(
        @JacksonXmlProperty(localName = "Decision")
        String decision,

        @JacksonXmlProperty(localName = "RiskScore")
        BigDecimal riskScore,

        @JacksonXmlElementWrapper(localName = "Reasons")
        @JacksonXmlProperty(localName = "Reason")
        List<FraudReason> reasons
) {
}
