package eu.qwan.exercises.dirtytests.obscure.process;

import java.util.HashMap;
import java.util.Map;
import lombok.EqualsAndHashCode;
import lombok.Getter;

/**
 Process represents the whole process a shipment goes through, from leaving the
 factory, through all involved distribution centres to the final destination.
 A shipment's process involves multiple tasks, like assigning a new carrier.
 Each task has one or more states.
 */
@EqualsAndHashCode
public class Process<TaskDefinitionType, StateType extends State> {
  @Getter
  private final String businessObject;

  private final Map<TaskDefinitionType, Task<TaskDefinitionType, StateType>> tasks = new HashMap<>();

  public Process(String businessObject, Map<TaskDefinitionType, Task<TaskDefinitionType, StateType>> tasks) {
    this.businessObject = businessObject;
    this.tasks.putAll(tasks);
  }

  public Task<TaskDefinitionType, StateType> getTask(TaskDefinitionType taskDefinition) {
    return this.tasks.get(taskDefinition);
  }
}
