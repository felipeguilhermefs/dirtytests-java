package eu.qwan.exercises.dirtytests.obscure.service;

import eu.qwan.exercises.dirtytests.obscure.domain.Transport;
import eu.qwan.exercises.dirtytests.obscure.notifications.NotificationPublisher;
import eu.qwan.exercises.dirtytests.obscure.process.AssignmentTaskDefinition;
import eu.qwan.exercises.dirtytests.obscure.process.AssignmentTaskState;
import eu.qwan.exercises.dirtytests.obscure.process.Process;
import eu.qwan.exercises.dirtytests.obscure.process.ProcessDefinition;
import eu.qwan.exercises.dirtytests.obscure.repositories.ProcessRepository;
import eu.qwan.exercises.dirtytests.obscure.process.Task;
import eu.qwan.exercises.dirtytests.obscure.repositories.TransportRepository;
import eu.qwan.exercises.dirtytests.obscure.request.AssignCarrierRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CarrierProcessor {
  private static final Logger LOG = LoggerFactory.getLogger(CarrierProcessor.class);

  private final TransportRepository transportRepository;
  private final ProcessRepository processRepository;
  private final CarrierUpdater carrierUpdater;
  private final NotificationPublisher notificationPublisher;

  public CarrierProcessor(
      TransportRepository transportRepository,
      ProcessRepository processRepository,
      CarrierUpdater carrierUpdater,
      NotificationPublisher notificationPublisher) {
    this.transportRepository = transportRepository;
    this.processRepository = processRepository;
    this.carrierUpdater = carrierUpdater;
    this.notificationPublisher = notificationPublisher;
  }

  public void processAssignCarrierRequest(AssignCarrierRequest assignCarrierRequest) {
    Transport transport = transportRepository.findByTrn(assignCarrierRequest.getTrn());
    boolean carrierUpdated = !transport.getCarrier().getOrganisationReferenceNumber().equals(assignCarrierRequest.getCarrier().getOrn());
    if(carrierUpdated) {
      carrierUpdater.updateCarrier(transport, assignCarrierRequest);
      updateProcess(transport, AssignmentTaskState.NOMINATED);
      notificationPublisher.sendYouHaveBeenAssignedAsCarierNotification(transport, assignCarrierRequest.getCarrier().getOrn());
    }
  }

  private void updateProcess(Transport transport, AssignmentTaskState taskState) {
    final Process process = processRepository
        .findByDefinitionAndBusinessObject(ProcessDefinition.CARRIER_ASSIGNMENT, transport.getTransportReferenceNumber());
    final Task task = process.getTask(AssignmentTaskDefinition.ASSIGN_CARRIER);
    if (task.isStateChangeAllowed(AssignmentTaskState.NOMINATED)) {
      task.setState(AssignmentTaskState.NOMINATED);
      processRepository.save(process, task);
    } else {
      LOG.info("State change not allowed from state:" + task.getState().getName() + " to state:"
                      + taskState.getName());
    }
  }
}
