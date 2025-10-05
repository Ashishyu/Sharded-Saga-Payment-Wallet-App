package com.example.shardedsagawallet.entities;

public class SagaStepRegistry {
    public static final String CREDIT_DESTINATION_WALLET = "CreditDestinationWalletStep";
    public static final String DEBIT_SOURCE_WALLET = "DebitSourceWalletStep";

    // Or use enum for type safety
    public enum WalletSteps {
        CREDIT_DESTINATION_WALLET,
        DEBIT_SOURCE_WALLET,
    }
}
