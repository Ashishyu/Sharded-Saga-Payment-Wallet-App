package com.example.shardedsagawallet.services.saga.steps;

import java.util.Map;

import org.springframework.stereotype.Component;

import com.example.shardedsagawallet.services.saga.ISagaStep;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class SagaStepFactory {

    private final Map<String, ISagaStep> sagaStepMap;

    public static enum SagaStepType {
        DEBIT_SOURCE_WALLET_STEP,
        CREDIT_DESTINATION_WALLET_STEP,
        UPDATE_TRANSACTION_STATUS_STEP
    }


    public ISagaStep getSagaStep(String stepName) {
        return sagaStepMap.get(stepName);
    }

}