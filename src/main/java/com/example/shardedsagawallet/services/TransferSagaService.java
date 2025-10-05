package com.example.shardedsagawallet.services;

import com.example.shardedsagawallet.services.saga.SagaOrchestrator;
import com.example.shardedsagawallet.services.saga.WalletSagaOrchestrator;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class TransferSagaService {
    private final SagaOrchestrator walletSagaOrchestrator;

    public void startSaga() {
        walletSagaOrchestrator.startSaga();
    }

}
