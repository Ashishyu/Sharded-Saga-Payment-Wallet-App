package com.example.shardedsagawallet.services.saga;

import com.example.shardedsagawallet.entities.*;
import com.example.shardedsagawallet.repositories.SagaInstanceRepository;
import com.example.shardedsagawallet.repositories.SagaStepRepository;
import com.example.shardedsagawallet.repositories.TransactionRepository;
import com.example.shardedsagawallet.services.saga.steps.SagaStepFactory;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class WalletSagaOrchestrator implements SagaOrchestrator{

    private final SagaInstanceRepository sagaInstanceRepository;
    private final TransactionRepository transactionRepository;

    private final ObjectMapper objectMapper;
    private final SagaStepRepository sagaStepRepository;
    private final SagaStepFactory sagaStepFactory;

    @Override
    public Long startSaga(SagaContext context) {
        try {
            String contextJson = objectMapper.writeValueAsString(context); // convert the context to a json as a string

            SagaInstance sagaInstance = SagaInstance
                    .builder()
                    .context(contextJson)
                    .status(SagaStatus.STARTED)
                    .build();

            sagaInstance = sagaInstanceRepository.save(sagaInstance);

            log.info("Started saga with id {}", sagaInstance.getId());

            return sagaInstance.getId();

        } catch (Exception e) {
            log.error("Error starting saga", e);
            throw new RuntimeException("Error starting saga", e);
        }

    }

    @Override
    @Transactional
    public boolean executeStep(Long sagaInstanceId, String stepName) {

        if (null == sagaInstanceId)
            throw new RuntimeException("Saga instance not found");

        SagaInstance sagaInstance = getSagaInstance(sagaInstanceId);

        ISagaStep step = sagaStepFactory.getSagaStep(stepName);

        if(step == null) {
            log.error("Saga step not found for step name {}", stepName);
            throw new RuntimeException("Saga step not found");
        }

        SagaStep sagaStep = sagaStepRepository.findByNameAndSagaInstanceId(stepName, sagaInstanceId);

        if (sagaStep == null) {

            sagaStep = SagaStep.builder()
                        .sagaInstanceId(sagaInstanceId)
                        .stepName(stepName)
                        .status(StepStatus.PENDING)
                        .build();

            sagaStepRepository.save(sagaStep);
        }

        try {

            SagaContext sagaContext = objectMapper.readValue(sagaInstance.getContext(), SagaContext.class);
            sagaStep.setStatus(StepStatus.RUNNING);
            sagaStepRepository.save(sagaStep); // updating the status to running in db

            boolean success = step.execute(sagaContext);

            if(success) {
                sagaStep.setStatus(StepStatus.COMPLETED);
                sagaStepRepository.save(sagaStep); // updating the status to completed in db

                sagaInstance.setCurrentStep(stepName); // step we just completed
                sagaInstance.setStatus(SagaStatus.RUNNING);
                sagaInstanceRepository.save(sagaInstance); // updating the status to running in db

                log.info("Step {} executed successfully", stepName);
                return true;
            } else {
                sagaStep.setStatus(StepStatus.FAILED);
                sagaStepRepository.save(sagaStep); // updating the status to failed in db
                log.error("Step {} failed", stepName);
                return false;
            }

        } catch (Exception e) {
            sagaStep.setStatus(StepStatus.FAILED);
            sagaStepRepository.save(sagaStep);
            log.error("Failed to execute step {}", stepName);
            return false;
        }
    }

    @Override
    public boolean compensateStep(Long sagaInstanceId, String stepName) {

        SagaInstance sagaInstance = getSagaInstance(sagaInstanceId);

        ISagaStep step = sagaStepFactory.getSagaStep(stepName);

        SagaStep sagaStep = sagaStepRepository.findByNameAndSagaInstanceId(stepName, sagaInstanceId);

        try {
            SagaContext sagaContext = objectMapper.readValue(sagaInstance.getContext(), SagaContext.class);
            sagaStep.setStatus(StepStatus.COMPENSATING);
            sagaStepRepository.save(sagaStep);

            boolean success = step.compensate(sagaContext);

            if (success) {
                sagaStep.setStatus(StepStatus.COMPENSATED);
                sagaStepRepository.save(sagaStep); // updating the status to COMPENSATED in db

                sagaInstance.setCurrentStep(stepName); // step we just COMPENSATED
                sagaInstance.setStatus(SagaStatus.COMPENSATED);
                sagaInstanceRepository.save(sagaInstance); // updating the status to COMPENSATED in db

                return true;

            } else {
                sagaStep.setStatus(StepStatus.FAILED);
                sagaStepRepository.save(sagaStep); // updating the status to failed in db
                return false;
            }

        } catch (Exception e) {
            sagaStep.setStatus(StepStatus.FAILED);
            sagaStepRepository.save(sagaStep);
            return false;
        }

    }

    @Override
    public SagaInstance getSagaInstance(Long sagaInstanceId) {
        return sagaInstanceRepository.findById(sagaInstanceId)
                .orElseThrow(() -> new RuntimeException("Saga instance not found"));
    }

    @Override
    public void compensateSaga(Long sagaInstanceId) {
        SagaInstance sagaInstance = getSagaInstance(sagaInstanceId);

    }

    @Override
    public void failSaga(Long sagaInstanceId) {

    }

    @Override
    public void completeSaga(Long sagaInstanceId) {

    }
}
