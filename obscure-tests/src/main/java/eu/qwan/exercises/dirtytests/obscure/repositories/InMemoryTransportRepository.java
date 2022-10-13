package eu.qwan.exercises.dirtytests.obscure.repositories;

import eu.qwan.exercises.dirtytests.obscure.domain.Transport;
import java.util.HashMap;
import java.util.Map;

public class InMemoryTransportRepository implements TransportRepository {

    private final Map<String, Transport> transports = new HashMap<>();

    @Override
    public Transport findByTrn(Object trn) {
        return transports.get((String) trn);
    }

    @Override
    public void save(Transport transport) {
        transports.put(transport.getTransportReferenceNumber(), transport);
    }
}
