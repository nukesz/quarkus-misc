package org.acme.reservation.inventory;

import io.quarkus.test.Mock;

import java.util.List;

@Mock
public class MockInventoryClient implements GraphQLInventoryClient {

    @Override
    public List<Car> allCars() {
        return List.of(new Car(1L, "ABC123", "Peugeot", "406"));
    }
}
