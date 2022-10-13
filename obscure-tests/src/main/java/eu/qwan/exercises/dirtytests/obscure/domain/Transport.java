package eu.qwan.exercises.dirtytests.obscure.domain;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class Transport {
  private final TransportOrganisation owner;
  private final String transportReferenceNumber;
  private TransportOrganisation carrier;
}
