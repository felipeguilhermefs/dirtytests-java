package eu.qwan.exercises.dirtytests.obscure.domain;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class TransportOrganisation {
  private String organisationReferenceNumber;
  private OrganisationType organisationType;

  public String getOrganisationReferenceNumber() {
    return organisationReferenceNumber;
  }

  public void setOrganisationReferenceNumber(String organisationReferenceNumber) {
    this.organisationReferenceNumber = organisationReferenceNumber;
  }

  public OrganisationType getOrganisationType() {
    return organisationType;
  }

  public void setOrganisationType(OrganisationType organisationType) {
    this.organisationType = organisationType;
  }
}
