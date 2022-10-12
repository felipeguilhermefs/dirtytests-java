package eu.qwan.exercises.dirtytests.obscure.domain;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class TransportOrganisation {
  private final String organisationReferenceNumber;
  private final OrganisationType organisationType;
}
