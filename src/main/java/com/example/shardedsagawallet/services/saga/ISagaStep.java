package com.example.shardedsagawallet.services.saga;

public interface ISagaStep {
    boolean execute(SagaContext sagaContext);

    boolean compensate(SagaContext sagaContext);

    String getStepName();


}
