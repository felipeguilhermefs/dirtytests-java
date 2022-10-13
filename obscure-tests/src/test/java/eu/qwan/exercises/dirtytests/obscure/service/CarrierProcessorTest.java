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

import static eu.qwan.exercises.dirtytests.obscure.process.AssignmentTaskState.INITIAL;
import static eu.qwan.exercises.dirtytests.obscure.process.AssignmentTaskState.NOMINATED;
import static org.mockito.Mockito.*;

@MockitoSettings(strictness = Strictness.LENIENT)
public class CarrierProcessorTest {

  private static final String TRN = "Some transport reference number";

  private Task    assignmentCarrierTask;

  private final ProcessRepository processRepository = spy(new InMemoryProcessRepository());
  private final TransportRepository transportRepository = new InMemoryTransportRepository();
  private final OrganisationRepository organisationRepository = new InMemoryOrganisationRepository();
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
    var organisation = new TransportOrganisation("CAR1", OrganisationType.CARRIER);
    var transport = new Transport(organisation, TRN, organisation);
    transportRepository.save(transport);
  }

  @Test
  public void processCarrier() {
    setupProcessState(INITIAL);

    var assignCarrierRequest = new AssignCarrierRequest(TRN, new OrganisationDto("CAR2"));
    carrierProcessor.processAssignCarrierRequest(assignCarrierRequest);

    var expectedProcess = processRepository.findByDefinitionAndBusinessObject(ProcessDefinition.CARRIER_ASSIGNMENT, TRN);
    //AssignmentTaskState.NOMINATED
    verify(processRepository).save(expectedProcess, assignmentCarrierTask);
  }

  @Test
  public void changeToSameCarrier() {
    setupProcessState(INITIAL);

    var assignCarrierRequest = new AssignCarrierRequest(TRN, new OrganisationDto("CAR1"));

    carrierProcessor.processAssignCarrierRequest(assignCarrierRequest);
    verify(processRepository, never()).save(any(Process.class), any(Task.class));
  }

  @Test
  public void changeCarrierToOther() {
    setupProcessState(INITIAL);

    var assignCarrierRequest = new AssignCarrierRequest(TRN, new OrganisationDto("CAR2"));

    carrierProcessor.processAssignCarrierRequest(assignCarrierRequest);
    verify(processRepository).save(any(Process.class), any(Task.class));
    verify(notificationPublisher).sendYouHaveBeenAssignedAsCarierNotification(
        transportRepository.findByTrn(TRN),
        "CAR2"
    );
  }

  @Test
  public void changeCarrierToOtherStateChangeNotAllowed() {
    setupProcessState(NOMINATED);

    var assignCarrierRequest = new AssignCarrierRequest(TRN, new OrganisationDto("CAR2"));

    carrierProcessor.processAssignCarrierRequest(assignCarrierRequest);
    verify(processRepository, never()).save(any(Process.class), any(Task.class));
  }

  private void setupProcessState(AssignmentTaskState state) {
    assignmentCarrierTask = new AssignmentCarrierTask();
    assignmentCarrierTask.setState(state);

    var process = new Process(TRN, Map.of(AssignmentTaskDefinition.ASSIGN_CARRIER, assignmentCarrierTask));
    processRepository.save(process, null);
  }

}
