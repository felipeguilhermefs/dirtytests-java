package eu.qwan.exercises.dirtytests.obscure.repositories;

import eu.qwan.exercises.dirtytests.obscure.process.Process;
import eu.qwan.exercises.dirtytests.obscure.process.ProcessDefinition;
import eu.qwan.exercises.dirtytests.obscure.process.State;
import eu.qwan.exercises.dirtytests.obscure.process.Task;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InMemoryProcessRepository implements ProcessRepository {

    private final Map<String, Process> processes = new HashMap<>();

    @Override
    public Process findByDefinitionAndBusinessObject(
        ProcessDefinition definition,
        String businessObject
    ) {
        return processes.get(businessObject);
    }

    @Override
    public void save(Process process, Task<?, ? extends State> task, List<String> theWhys) {
        processes.put(process.getBusinessObject(), process);
    }
}
