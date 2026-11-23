package com.demo.app.payments.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import tools.jackson.dataformat.xml.annotation.JacksonXmlProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record FraudError(
        @JacksonXmlProperty(localName = "Code")
        String code,

        @JacksonXmlProperty(localName = "Message")
        String message,

        @JacksonXmlProperty(localName = "Retryable")
        Boolean retryable
) {
}
