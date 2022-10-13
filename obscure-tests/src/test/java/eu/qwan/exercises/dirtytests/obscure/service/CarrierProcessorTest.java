package eu.qwan.exercises.dirtytests.obscure.service;

import eu.qwan.exercises.dirtytests.obscure.domain.OrganisationType;
import eu.qwan.exercises.dirtytests.obscure.domain.Transport;
import eu.qwan.exercises.dirtytests.obscure.domain.TransportOrganisation;
import eu.qwan.exercises.dirtytests.obscure.notifications.NotificationPublisher;
import eu.qwan.exercises.dirtytests.obscure.process.*;
import eu.qwan.exercises.dirtytests.obscure.process.Process;
import eu.qwan.exercises.dirtytests.obscure.repositories.InMemoryOrganisationRepository;
import eu.qwan.exercises.dirtytests.obscure.repositories.InMemoryProcessRepository;
import eu.qwan.exercises.dirtytests.obscure.repositories.InMemoryTransportRepository;
import eu.qwan.exercises.dirtytests.obscure.repositories.OrganisationRepository;
import eu.qwan.exercises.dirtytests.obscure.repositories.ProcessRepository;
import eu.qwan.exercises.dirtytests.obscure.repositories.TransportRepository;
import eu.qwan.exercises.dirtytests.obscure.request.AssignCarrierRequest;
import eu.qwan.exercises.dirtytests.obscure.request.OrganisationDto;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import static eu.qwan.exercises.dirtytests.obscure.process.AssignmentTaskDefinition.ASSIGN_CARRIER;
import static eu.qwan.exercises.dirtytests.obscure.process.AssignmentTaskState.ASSIGNED;
import static eu.qwan.exercises.dirtytests.obscure.process.AssignmentTaskState.INITIAL;
import static eu.qwan.exercises.dirtytests.obscure.process.AssignmentTaskState.NOMINATED;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@MockitoSettings(strictness = Strictness.LENIENT)
public class CarrierProcessorTest {

  private static final String TRN = "Some transport reference number";
  private static final String ORN_PRIME = "Some organisation reference number";
  private static final String ORN_OTHER = "Other organisation reference number";

  private final ProcessRepository processRepository = new InMemoryProcessRepository();
  private final TransportRepository transportRepository = new InMemoryTransportRepository();
  private final InMemoryOrganisationRepository organisationRepository = new InMemoryOrganisationRepository();
  private final CarrierUpdater carrierUpdater = new CarrierUpdater(transportRepository, organisationRepository);
  private final NotificationPublisher notificationPublisher = mock(NotificationPublisher.class);
  private final CarrierProcessor carrierProcessor = new CarrierProcessor(
      transportRepository,
      processRepository,
      carrierUpdater,
      notificationPublisher
  );

  @BeforeEach
  void setup() {
    var primeOrganisation = new TransportOrganisation(ORN_PRIME, OrganisationType.CARRIER);
    organisationRepository.save(ORN_PRIME, primeOrganisation);
    var otherOrganisation = new TransportOrganisation(ORN_OTHER, OrganisationType.CARRIER);
    organisationRepository.save(ORN_OTHER, otherOrganisation);

    var transport = new Transport(primeOrganisation, TRN, primeOrganisation);
    transportRepository.save(transport);
  }

  @Test
  public void processCarrier() {
    setupProcessState(INITIAL);

    var assignCarrierRequest = new AssignCarrierRequest(TRN, new OrganisationDto(ORN_OTHER));
    carrierProcessor.processAssignCarrierRequest(assignCarrierRequest);

    var expectedProcess = processRepository.findByDefinitionAndBusinessObject(ProcessDefinition.CARRIER_ASSIGNMENT, TRN);
    assertEquals(NOMINATED, expectedProcess.getTask(ASSIGN_CARRIER).getState());
  }

  @Test
  public void changeToSameCarrier() {
    setupProcessState(INITIAL);

    var assignCarrierRequest = new AssignCarrierRequest(TRN, new OrganisationDto(ORN_PRIME));

    carrierProcessor.processAssignCarrierRequest(assignCarrierRequest);

    var expectedProcess = processRepository.findByDefinitionAndBusinessObject(ProcessDefinition.CARRIER_ASSIGNMENT, TRN);
    assertEquals(INITIAL, expectedProcess.getTask(ASSIGN_CARRIER).getState());
  }

  @Test
  public void changeCarrierToOtherOrganisation() {
    setupProcessState(INITIAL);

    var assignCarrierRequest = new AssignCarrierRequest(TRN, new OrganisationDto(ORN_OTHER));

    carrierProcessor.processAssignCarrierRequest(assignCarrierRequest);

    var expectedTransport = transportRepository.findByTrn(TRN);
    assertEquals(ORN_OTHER, expectedTransport.getCarrier().getOrganisationReferenceNumber());
  }

  @Test
  public void sendsNotificationWhenChangingCarrierToOtherOrganisation() {
    setupProcessState(INITIAL);

    var assignCarrierRequest = new AssignCarrierRequest(TRN, new OrganisationDto(ORN_OTHER));

    carrierProcessor.processAssignCarrierRequest(assignCarrierRequest);

    verify(notificationPublisher).sendYouHaveBeenAssignedAsCarierNotification(
        transportRepository.findByTrn(TRN),
        ORN_OTHER
    );
  }

  @Test
  public void changeCarrierToOtherOrganisationNominatesProcess() {
    setupProcessState(INITIAL);

    var assignCarrierRequest = new AssignCarrierRequest(TRN, new OrganisationDto(ORN_OTHER));

    carrierProcessor.processAssignCarrierRequest(assignCarrierRequest);

    var expectedProcess = processRepository.findByDefinitionAndBusinessObject(ProcessDefinition.CARRIER_ASSIGNMENT, TRN);
    assertEquals(NOMINATED, expectedProcess.getTask(ASSIGN_CARRIER).getState());
  }

  @Test
  public void changeFromAssignedToNominatedNotAllowed() {
    setupProcessState(ASSIGNED);

    var assignCarrierRequest = new AssignCarrierRequest(TRN, new OrganisationDto(ORN_OTHER));

    carrierProcessor.processAssignCarrierRequest(assignCarrierRequest);

    var expectedProcess = processRepository.findByDefinitionAndBusinessObject(ProcessDefinition.CARRIER_ASSIGNMENT, TRN);
    assertEquals(ASSIGNED, expectedProcess.getTask(ASSIGN_CARRIER).getState());
  }

  private void setupProcessState(AssignmentTaskState state) {
    var assignmentCarrierTask = new AssignmentCarrierTask();
    assignmentCarrierTask.setState(state);

    var process = new Process(TRN, Map.of(ASSIGN_CARRIER, assignmentCarrierTask));
    processRepository.save(process, null);
  }

}
