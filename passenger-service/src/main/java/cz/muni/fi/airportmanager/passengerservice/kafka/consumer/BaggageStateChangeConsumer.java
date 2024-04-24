package cz.muni.fi.airportmanager.passengerservice.kafka.consumer;

import cz.muni.fi.airportmanager.passengerservice.entity.Notification;
import cz.muni.fi.airportmanager.passengerservice.kafka.model.BaggageStateChange;
import cz.muni.fi.airportmanager.passengerservice.service.PassengerService;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.reactive.messaging.Incoming;


@ApplicationScoped
public class BaggageStateChangeConsumer {

    // TODO use passengerService to add notification for passenger


    /**
     * Process baggage state change
     *
     * @param baggageStateChange baggage state change
     */
//    TODO process baggage state change from Kafka using Incoming annotation
    public Uni<Void> process(BaggageStateChange baggageStateChange) {
    }

}
