package com.demo.app.payments.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import tools.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import tools.jackson.dataformat.xml.annotation.JacksonXmlText;

@JsonIgnoreProperties(ignoreUnknown = true)
public record FraudReason(
        @JacksonXmlProperty(isAttribute = true, localName = "code")
        String code,

        @JacksonXmlText
        String text
) {
}
