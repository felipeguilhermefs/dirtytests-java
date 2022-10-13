package eu.qwan.exercises.dirtytests.obscure.repositories;

import eu.qwan.exercises.dirtytests.obscure.domain.TransportOrganisation;
import java.util.HashMap;
import java.util.Map;

public class InMemoryOrganisationRepository implements OrganisationRepository {

    private final Map<String, TransportOrganisation> organisations = new HashMap<>();

    @Override
    public TransportOrganisation findByOrn(String orn) {
        return organisations.get(orn);
    }

    public void save(String orn, TransportOrganisation organisation) {
        organisations.put(orn, organisation);
    }
}
