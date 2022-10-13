package eu.qwan.exercises.dirtytests.obscure.service;

import eu.qwan.exercises.dirtytests.obscure.domain.OrganisationType;
import eu.qwan.exercises.dirtytests.obscure.domain.Transport;
import eu.qwan.exercises.dirtytests.obscure.domain.TransportOrganisation;
import eu.qwan.exercises.dirtytests.obscure.notifications.NotificationPublisher;
import eu.qwan.exercises.dirtytests.obscure.process.*;
import eu.qwan.exercises.dirtytests.obscure.process.Process;
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
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@MockitoSettings(strictness = Strictness.LENIENT)
public class CarrierProcessorTest {

  private Task    assignmentCarrierTask;

  @Mock
  private AssignCarrierProcessController assignCarrierProcessController;

  private final ProcessRepository processRepository = new InMemoryProcessRepository();
  private final TransportRepository transportRepository = new InMemoryTransportRepository();
  private final CarrierUpdater carrierUpdater = new CarrierUpdater(transportRepository, mock(OrganisationRepository.class));
  private final CarrierProcessor carrierProcessor = new CarrierProcessor(
      transportRepository,
      processRepository,
      carrierUpdater
  );


  @Mock
  private NotificationPublisher          notificationPublisher;

  @BeforeEach
  void setup() {
    var organisation = new TransportOrganisation("CAR1", OrganisationType.CARRIER);
    var transport = new Transport(organisation, "TRN", organisation);
    transportRepository.save(transport);

    assignmentCarrierTask = new AssignmentCarrierTask();
    assignmentCarrierTask.setState(AssignmentTaskState.INITIAL);

    var process = new Process("TRN", Map.of(AssignmentTaskDefinition.ASSIGN_CARRIER, assignmentCarrierTask));
    processRepository.save(process, null, null);
  }

  @Test
  public void processCarrier() {
    initMocks();

    var assignCarrierRequest = new AssignCarrierRequest("TRN", new OrganisationDto("CAR2"));
    carrierProcessor.processAssignCarrierRequest(assignCarrierRequest);

    verify(assignCarrierProcessController, times(1)).//
        changeState(processRepository.findByDefinitionAndBusinessObject(ProcessDefinition.CARRIER_ASSIGNMENT, "TRN"), assignmentCarrierTask, AssignmentTaskState.NOMINATED, null);
  }

  @Test
  public void changeToSameCarrier() {
    initMocks();

    var assignCarrierRequest = new AssignCarrierRequest("TRN", new OrganisationDto("CAR1"));

    carrierProcessor.processAssignCarrierRequest(assignCarrierRequest);
    verify(assignCarrierProcessController, times(0)).//
        changeState(any(Process.class), any(Task.class), any(AssignmentTaskState.class), eq(null));
  }

  @Test
  public void changeCarrierToOther() {
    initMocks();

    var assignCarrierRequest = new AssignCarrierRequest("TRN", new OrganisationDto("CAR2"));

    carrierProcessor.processAssignCarrierRequest(assignCarrierRequest);
    verify(assignCarrierProcessController, times(1)).//
        changeState(any(Process.class), any(Task.class), any(AssignmentTaskState.class), eq(null));
    verify(notificationPublisher).sendYouHaveBeenAssignedAsCarierNotification(
        transportRepository.findByTrn("TRN"),
        "CAR2"
    );
  }

  @Test
  public void changeCarrierToOtherStateChangeNotAllowed() {
    assignmentCarrierTask.setState(AssignmentTaskState.NOMINATED);
    initMocks();

    var assignCarrierRequest = new AssignCarrierRequest("TRN", new OrganisationDto("CAR2"));

    carrierProcessor.processAssignCarrierRequest(assignCarrierRequest);
    verify(assignCarrierProcessController, times(0)).//
            changeState(any(Process.class), any(Task.class), any(AssignmentTaskState.class), eq(null));
  }

  private void initMocks() {
    carrierProcessor.setController(assignCarrierProcessController);
    carrierProcessor.setNotificationPublisher(notificationPublisher);
  }

}
