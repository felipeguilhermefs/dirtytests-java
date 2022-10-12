package eu.qwan.exercises.dirtytests.obscure.request;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class AssignCarrierRequest {
  private final String trn;
  private final OrganisationDto carrier;
}
