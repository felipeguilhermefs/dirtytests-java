package eu.qwan.exercises.dirtytests.obscure.repositories;

import eu.qwan.exercises.dirtytests.obscure.domain.TransportOrganisation;

public class InMemoryOrganisationRepository implements OrganisationRepository {

    @Override
    public TransportOrganisation findByOrn(String orn) {
        return null;
    }
}
