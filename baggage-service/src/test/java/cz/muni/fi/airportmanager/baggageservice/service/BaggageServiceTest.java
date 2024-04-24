package cz.muni.fi.airportmanager.baggageservice.service;

import cz.muni.fi.airportmanager.baggageservice.kafka.producer.BaggageStateChangeProducer;
import cz.muni.fi.airportmanager.baggageservice.model.CreateBaggageDto;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.vertx.RunOnVertxContext;
import io.quarkus.test.vertx.UniAsserter;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

@QuarkusTest
class BaggageServiceTest {

    @InjectMock
    BaggageStateChangeProducer baggageStateChangeProducer;

    @Inject
    BaggageService baggageService;

    @Test
    @RunOnVertxContext
    void shouldSendKafkaMessageOnCreateBaggage(UniAsserter asserter) {
        CreateBaggageDto createBaggageDto = new CreateBaggageDto();
        createBaggageDto.passengerId = 100L;
        createBaggageDto.weight = 20;

        asserter.execute(() ->
                baggageService.createBaggage(createBaggageDto)
                        .onItem().invoke(() ->
                                Mockito.verify(baggageStateChangeProducer, Mockito.times(1)).send(Mockito.any())
                        )
        );
    }

    @Test
    @RunOnVertxContext
    void shouldSendKafkaMessageOnClaimBaggage(UniAsserter asserter) {
        asserter.execute(() -> baggageService.createBaggage(new CreateBaggageDto() {{
            passengerId = 100L;
            weight = 20;
        }}).onItem().invoke(created ->
                baggageService.claimBaggage(created.id).onItem().invoke(claimed ->
                        Mockito.verify(baggageStateChangeProducer, Mockito.times(1))
                                .send(Mockito.any())
                )
        ));
    }

    @Test
    @RunOnVertxContext
    void shouldSendKafkaMessageOnLostBaggage(UniAsserter asserter) {
        asserter.execute(() -> baggageService.createBaggage(new CreateBaggageDto() {{
            passengerId = 100L;
            weight = 20;
        }}).onItem().invoke(created ->
                baggageService.lostBaggage(created.id).onItem().invoke(lost ->
                        Mockito.verify(baggageStateChangeProducer, Mockito.times(1))
                                .send(Mockito.any())
                )
        ));
    }

}
