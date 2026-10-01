package org.acme.inventory.grpc;

import io.quarkus.grpc.GrpcService;
import io.quarkus.logging.Log;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;

import java.util.Optional;

import org.acme.inventory.database.CarInventory;
import org.acme.inventory.model.Car;
import org.acme.inventory.model.CarResponse;
import org.acme.inventory.model.InsertCarRequest;
import org.acme.inventory.model.RemoveCarRequest;
import org.acme.inventory.model.InventoryService;

@GrpcService 
public class GrpcInventoryService implements InventoryService {
    
    @Inject
    CarInventory inventory;
    
	@Override
	public Uni<CarResponse> uniAdd(InsertCarRequest request) {
        Car car = toCar(request);
        Log.info("Persisting " + car);
        inventory.getCars().add(car);
        return Uni.createFrom().item(toCarResponse(car));
	}

    @Override
    public Multi<CarResponse> add(Multi<InsertCarRequest> requests) {
        return requests.map(this::toCar).onItem().invoke(car -> {
            Log.info("Persisting " + car);
            inventory.getCars().add(car);
        }).map(this::toCarResponse);
    }

    private Car toCar(InsertCarRequest request) {
        Car car = new Car();
        car.licensePlateNumber = request.getLicensePlateNumber();
        car.manufacturer = request.getManufacturer();
        car.model = request.getModel();
        car.id = CarInventory.ids.incrementAndGet();
        return car;
    }

    private CarResponse toCarResponse(Car car) {
        return CarResponse.newBuilder()
            .setId(car.id)
            .setLicensePlateNumber(car.licensePlateNumber)
            .setManufacturer(car.manufacturer)
            .setModel(car.model)
            .build();
    }
    
    @Override
    public Uni<CarResponse> remove(RemoveCarRequest request) {
        Optional<Car> optionalCar = inventory.getCars().stream()
            .filter(car -> request.getLicensePlateNumber().equals(car.licensePlateNumber))
            .findFirst();
        if (optionalCar.isPresent()) {
            Car removedCar = optionalCar.get();
            Log.info("Removing " + removedCar);
            inventory.getCars().remove(removedCar);
            return Uni.createFrom().item(toCarResponse(removedCar));
        } else {
            return Uni.createFrom().nullItem();
        }
    }

}
