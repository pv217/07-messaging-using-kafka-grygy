package cz.muni.fi.airportmanager.passengerservice.kafka.consumer;

import cz.muni.fi.airportmanager.passengerservice.entity.Notification;
import cz.muni.fi.airportmanager.passengerservice.kafka.model.BaggageStateChange;
import cz.muni.fi.airportmanager.passengerservice.kafka.model.BaggageStatus;
import cz.muni.fi.airportmanager.passengerservice.service.PassengerService;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.vertx.RunOnVertxContext;
import io.quarkus.test.vertx.UniAsserter;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

@QuarkusTest
class BaggageStateChangeConsumerTest {

    @Inject
    BaggageStateChangeConsumer consumer;

    @InjectMock
    PassengerService passengerService;

    @Test
    @RunOnVertxContext
    void testProcessBaggageStateChange(UniAsserter asserter) {
        // Arrange
        BaggageStateChange change = new BaggageStateChange();
        change.baggageId = 100L;
        change.passengerId = 1L;
        change.newStatus = BaggageStatus.CHECKED_IN;


        asserter.execute(
                () -> consumer.process(change)
                        .onItem().invoke(() ->
                                Mockito.verify(passengerService, Mockito.times(1))
                                        .addNotificationForPassenger(Mockito.anyLong(), Mockito.any())
                        )
        );

    }
}