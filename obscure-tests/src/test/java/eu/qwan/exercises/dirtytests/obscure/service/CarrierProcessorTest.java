package eu.qwan.exercises.dirtytests.obscure.service;

import eu.qwan.exercises.dirtytests.obscure.domain.OrganisationType;
import eu.qwan.exercises.dirtytests.obscure.domain.Transport;
import eu.qwan.exercises.dirtytests.obscure.domain.TransportOrganisation;
import eu.qwan.exercises.dirtytests.obscure.notifications.NotificationPublisher;
import eu.qwan.exercises.dirtytests.obscure.process.*;
import eu.qwan.exercises.dirtytests.obscure.process.Process;
import eu.qwan.exercises.dirtytests.obscure.repositories.InMemoryTransportRepository;
import eu.qwan.exercises.dirtytests.obscure.repositories.ProcessRepository;
import eu.qwan.exercises.dirtytests.obscure.repositories.TransportRepository;
import eu.qwan.exercises.dirtytests.obscure.request.AssignCarrierRequest;
import eu.qwan.exercises.dirtytests.obscure.request.OrganisationDto;
import java.util.HashMap;
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
  private Process process;

  @Mock
  private ProcessRepository processRepository;

  @Mock
  private AssignCarrierProcessController assignCarrierProcessController;
  private final TransportRepository transportRepository = new InMemoryTransportRepository();
  private final CarrierProcessor carrierProcessor = new CarrierProcessor(transportRepository);
  @Mock
  private CarrierUpdater                 carrierUpdaterMock;
  @Mock
  private NotificationPublisher          notificationPublisher;

  @BeforeEach
  void setup() {
    var organisation = new TransportOrganisation("CAR1", OrganisationType.CARRIER);
    var transport = new Transport(organisation, "TRN", organisation);
    transportRepository.save(transport);
  }

  @Test
  public void processCarrier() {
    initMocks(true);

    var assignCarrierRequest = new AssignCarrierRequest("TRN", new OrganisationDto("CAR2"));
    carrierProcessor.processAssignCarrierRequest(assignCarrierRequest);

    verify(assignCarrierProcessController, times(1)).//
        changeState(process, assignmentCarrierTask, AssignmentTaskState.NOMINATED, null);
  }

  @Test
  public void changeToSameCarrier() {
    initMocks(true);

    var assignCarrierRequest = new AssignCarrierRequest("TRN", new OrganisationDto("CAR1"));

    carrierProcessor.processAssignCarrierRequest(assignCarrierRequest);
    verify(assignCarrierProcessController, times(0)).//
        changeState(any(Process.class), any(Task.class), any(AssignmentTaskState.class), eq(null));
  }

  @Test
  public void changeCarrierToOther() {
    initMocks(true);

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
    initMocks(false);

    var assignCarrierRequest = new AssignCarrierRequest("TRN", new OrganisationDto("CAR2"));

    carrierProcessor.processAssignCarrierRequest(assignCarrierRequest);
    verify(assignCarrierProcessController, times(0)).//
            changeState(any(Process.class), any(Task.class), any(AssignmentTaskState.class), eq(null));
  }

  private void initMocks(boolean stateChangeAllowed) {
    assignmentCarrierTask = mock(AssignmentCarrierTask.class);
    when(assignmentCarrierTask.isStateChangeAllowed(any(AssignmentTaskState.class))).thenReturn(stateChangeAllowed);
    when(assignmentCarrierTask.getState()).thenReturn(AssignmentTaskState.NOMINATED);

    process = mock(Process.class);
    when(process.getTask(any(AssignmentTaskDefinition.class))).thenReturn(assignmentCarrierTask);

    when(processRepository.findByDefinitionAndBusinessObject(any(ProcessDefinition.class), anyString())).thenReturn(process);

    carrierProcessor.setController(assignCarrierProcessController);
    carrierProcessor.setProcessRepository(processRepository);
    carrierProcessor.setCarrierUpdater(carrierUpdaterMock);
    carrierProcessor.setNotificationPublisher(notificationPublisher);
  }

}
