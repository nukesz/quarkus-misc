package org.acme.reservation.reservation;

import io.quarkus.logging.Log;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.acme.reservation.inventory.Car;
import org.acme.reservation.inventory.InventoryClient;
import org.acme.reservation.rental.Rental;
import org.acme.reservation.rental.RentalClient;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.logging.Logger;
import org.jboss.resteasy.reactive.RestQuery;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Path("reservation")
@Produces(MediaType.APPLICATION_JSON)
public class ReservationResource {

    private static final Logger LOG = Logger.getLogger(ReservationResource.class);
    private final ReservationsRepository reservationsRepository;
    private final InventoryClient inventoryClient;
    private final RentalClient rentalClient;

    public ReservationResource(ReservationsRepository reservationsRepository,
                               InventoryClient inventoryClient,
                               @RestClient RentalClient rentalClient
    ) {
        this.reservationsRepository = reservationsRepository;
        this.inventoryClient = inventoryClient;
        this.rentalClient = rentalClient;
    }

    @GET
    @Path("availability")
    public Collection<Car> availability(@RestQuery LocalDate startDate, @RestQuery LocalDate endDate) {
        Log.infov("Stardate #1: {0}", startDate);
        LOG.info("Stardate #2: " + startDate);

        // obtain all cars from inventory
        List<Car> availableCars = inventoryClient.allCars();

        // create a map from id to car
        Map<Long, Car> carsById = new HashMap<>();
        for (Car car : availableCars) {
            carsById.put(car.id, car);
        }

        // get all current reservations
        List<Reservation> reservations = reservationsRepository.findAll();

        for (Reservation reservation : reservations) {
            if (reservation.isReserved(startDate, endDate)) {
                carsById.remove(reservation.carId);
            }
        }

        return carsById.values();
    }

    @Consumes(MediaType.APPLICATION_JSON)
    @POST
    public Reservation make(Reservation reservation) {
        Log.info("Making reservation " + reservation);
        Reservation result = reservationsRepository.save(reservation);
        String userId = "x";
        if (reservation.startDay.equals(LocalDate.now())) {
            Rental rental = rentalClient.start(userId, result.id);
            Log.info("Successfully started rental " + rental);
        }
        return result;
    }
}
