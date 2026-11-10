package org.acme.inventory.service;

import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.acme.inventory.model.Car;
import org.acme.inventory.repository.CarRepository;
import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Mutation;
import org.eclipse.microprofile.graphql.Query;

import java.util.List;
import java.util.Optional;

@GraphQLApi
public class GraphQLInventoryService {

    @Inject
    CarRepository carRepository;

    @Query
    public List<Car> cars() {
        return carRepository.listAll();
    }

    @Mutation
    @Transactional
    public Car register(Car car) {
        carRepository.persist(car);
        return car;
    }

    @Mutation
    @Transactional
    public boolean remove(String licensePlateNumber) {
        Optional<Car> toBeRemoved = carRepository.findByLicensePlateNumber(licensePlateNumber);
        return toBeRemoved.map(car -> {
            carRepository.delete(car);
            return true;
        }).orElse(false);
    }
}
