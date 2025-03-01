package eu.qwan.exercises.dirtytests.obscure.service;

import eu.qwan.exercises.dirtytests.obscure.domain.Transport;
import eu.qwan.exercises.dirtytests.obscure.domain.TransportOrganisation;
import eu.qwan.exercises.dirtytests.obscure.repositories.OrganisationRepository;
import eu.qwan.exercises.dirtytests.obscure.repositories.TransportRepository;
import eu.qwan.exercises.dirtytests.obscure.request.AssignCarrierRequest;
import eu.qwan.exercises.dirtytests.obscure.request.OrganisationDto;

public class CarrierUpdater {
  private final TransportRepository transportRepository;
  private final OrganisationRepository organisationRepository;

  public CarrierUpdater(TransportRepository transportRepository, OrganisationRepository organisationRepository) {
    this.transportRepository = transportRepository;
    this.organisationRepository = organisationRepository;
  }

  public void updateCarrier(Transport transport, AssignCarrierRequest assignCarrierRequest) {
    var carrierDto = assignCarrierRequest.getCarrier();
    var carrier = organisationRepository.findByOrn(carrierDto.getOrn());
    transport.setCarrier(carrier);
    transportRepository.save(transport);
  }
}
