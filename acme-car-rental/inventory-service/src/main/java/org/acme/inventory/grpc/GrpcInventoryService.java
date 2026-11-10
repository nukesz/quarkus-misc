package org.acme.inventory.grpc;

import io.quarkus.grpc.GrpcService;
import io.quarkus.logging.Log;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;

import java.util.Optional;

import jakarta.transaction.Transactional;
import org.acme.inventory.model.Car;
import org.acme.inventory.model.CarResponse;
import org.acme.inventory.model.InsertCarRequest;
import org.acme.inventory.model.RemoveCarRequest;
import org.acme.inventory.model.InventoryService;
import org.acme.inventory.repository.CarRepository;

@GrpcService 
public class GrpcInventoryService implements InventoryService {
    
    @Inject
    CarRepository carRepository;

    @Override
    @Transactional
	public Uni<CarResponse> uniAdd(InsertCarRequest request) {
        Car car = toCar(request);
        Log.info("Persisting " + car);
        carRepository.persist(car);
        return Uni.createFrom().item(toCarResponse(car));
	}

    @Override
    @Transactional
    public Multi<CarResponse> add(Multi<InsertCarRequest> requests) {
        return requests.map(this::toCar).onItem().invoke(car -> {
            Log.info("Persisting " + car);
            carRepository.persist(car);
        }).map(this::toCarResponse);
    }

    private Car toCar(InsertCarRequest request) {
        Car car = new Car();
        car.licensePlateNumber = request.getLicensePlateNumber();
        car.manufacturer = request.getManufacturer();
        car.model = request.getModel();
        return car;
    }

    private CarResponse toCarResponse(Car car) {
        return CarResponse.newBuilder()
            .setId(car.getId())
            .setLicensePlateNumber(car.licensePlateNumber)
            .setManufacturer(car.manufacturer)
            .setModel(car.model)
            .build();
    }
    
    @Override
    @Transactional
    public Uni<CarResponse> remove(RemoveCarRequest request) {
        Optional<Car> optionalCar = carRepository.findByLicensePlateNumber(request.getLicensePlateNumber());
        if (optionalCar.isPresent()) {
            Car removedCar = optionalCar.get();
            Log.info("Removing " + removedCar);
            carRepository.delete(removedCar);
            return Uni.createFrom().item(toCarResponse(removedCar));
        } else {
            return Uni.createFrom().nullItem();
        }
    }

}
