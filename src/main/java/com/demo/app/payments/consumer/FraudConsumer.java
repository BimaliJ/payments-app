
package com.demo.app.payments.consumer;

import com.demo.app.payments.exception.PaymentNotFoundException;
import com.demo.app.payments.model.entities.PaymentEntity;
import com.demo.app.payments.model.enums.FraudDecision;
import com.demo.app.payments.repositories.PaymentRepository;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;
import tools.jackson.dataformat.xml.XmlMapper;
import tools.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import tools.jackson.dataformat.xml.annotation.JacksonXmlProperty;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class FraudConsumer {

    private final PaymentRepository paymentRepository;

    @Value("${payment.fraud.url}")
    private String fraudUrl;

    private final XmlMapper xmlMapper = new XmlMapper();

    @Transactional
    public void accept(UUID paymentId) throws Exception {

        PaymentEntity payment = paymentRepository.findById(paymentId)
                .orElseThrow(() ->
                        new PaymentNotFoundException("No payment found"));

        if (payment.getFraudDecision() != FraudDecision.PENDING) {
            return;
        }

        // Call the simulated fraud provider
        String xml = RestClient.create()
                .post()
                .uri(fraudUrl)
                .body("{}")
                .retrieve()
                .body(String.class);

        if (xml == null || xml.isBlank()) {
            throw new IllegalStateException("Empty fraud response");
        }

        // Convert XML into Java objects
        ScreeningResponse response =
                xmlMapper.readValue(xml, ScreeningResponse.class);

        // Vendor can return errors with HTTP 200
        if (response.error() != null) {
            throw new IllegalStateException(
                    "Fraud provider error: " + response.error().message()
            );
        }

        if (response.result() == null) {
            throw new IllegalStateException("Missing fraud result");
        }

        FraudResult result = response.result();

        if (result.decision() == null || result.riskScore() == null) {
            throw new IllegalStateException("Incomplete fraud result");
        }

        // Update payment
        payment.setFraudDecision(
                FraudDecision.valueOf(result.decision().trim())
        );

        payment.setFraudRiskScore(result.riskScore());

        payment.setFraudReasons(
                result.reasons() == null
                        ? ""
                        : String.join("; ", result.reasons())
        );

        paymentRepository.save(payment);
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ScreeningResponse(
            @JacksonXmlProperty(localName = "Result")
            FraudResult result,

            @JacksonXmlProperty(localName = "Error")
            FraudError error
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record FraudResult(
            @JacksonXmlProperty(localName = "Decision")
            String decision,

            @JacksonXmlProperty(localName = "RiskScore")
            BigDecimal riskScore,

            @JacksonXmlElementWrapper(localName = "Reasons")
            @JacksonXmlProperty(localName = "Reason")
            List<String> reasons
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record FraudError(
            @JacksonXmlProperty(localName = "Code")
            String code,

            @JacksonXmlProperty(localName = "Message")
            String message,

            @JacksonXmlProperty(localName = "Retryable")
            Boolean retryable
    ) {}
}
