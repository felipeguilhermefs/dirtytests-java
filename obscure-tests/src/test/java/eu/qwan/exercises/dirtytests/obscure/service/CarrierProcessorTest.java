package eu.qwan.exercises.dirtytests.obscure.service;

import eu.qwan.exercises.dirtytests.obscure.domain.OrganisationType;
import eu.qwan.exercises.dirtytests.obscure.domain.Transport;
import eu.qwan.exercises.dirtytests.obscure.domain.TransportOrganisation;
import eu.qwan.exercises.dirtytests.obscure.notifications.NotificationPublisher;
import eu.qwan.exercises.dirtytests.obscure.process.*;
import eu.qwan.exercises.dirtytests.obscure.process.Process;
import eu.qwan.exercises.dirtytests.obscure.repositories.ProcessRepository;
import eu.qwan.exercises.dirtytests.obscure.repositories.TransportRepository;
import eu.qwan.exercises.dirtytests.obscure.request.AssignCarrierRequest;
import eu.qwan.exercises.dirtytests.obscure.request.OrganisationDto;
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

  private CarrierProcessor carrierProcessor = new CarrierProcessor();
  private Transport transport = new Transport(
      new TransportOrganisation("CAR1", OrganisationType.CARRIER),
      "TRN",
      new TransportOrganisation("CAR1", OrganisationType.CARRIER)
  );
  @Mock
  private AssignCarrierProcessController assignCarrierProcessController;
  private final TransportRepository transportRepository = new InMemoryTransportRepository();
  @Mock
  private CarrierUpdater                 carrierUpdaterMock;
  @Mock
  private NotificationPublisher          notificationPublisher;

  @BeforeEach
  void setup() {
  }

  @Test
  public void processCarrier() {
    initMocks(true);

    carrierProcessor.updateProcess(transport, AssignmentTaskState.NOMINATED);
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
    verify(notificationPublisher, times(1)).//
        sendYouHaveBeenAssignedAsCarierNotification(transport, "CAR2");
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
    carrierProcessor.setTransportRepository(transportRepository);
    carrierProcessor.setCarrierUpdater(carrierUpdaterMock);
    carrierProcessor.setNotificationPublisher(notificationPublisher);
  }

  private static class InMemoryTransportRepository implements TransportRepository {

    @Override
    public Transport findByTrn(Object any) {
      var organisation = new TransportOrganisation("CAR1", OrganisationType.CARRIER);
      return new Transport(organisation, "TRN", organisation);
    }

    @Override
    public void save(Transport transport) {

    }
  }

}
