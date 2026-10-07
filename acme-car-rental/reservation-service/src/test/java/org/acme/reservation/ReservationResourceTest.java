package org.acme.reservation;

import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.common.http.TestHTTPResource;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.acme.reservation.reservation.Reservation;
import org.acme.reservation.reservation.ReservationResource;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.time.LocalDate;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

@QuarkusTest
public class ReservationResourceTest {

    @TestHTTPEndpoint(ReservationResource.class)
    @TestHTTPResource
    private URI reservationResource;

    @Test
    void testReservationIds() {
        Reservation reservation = new Reservation();
        reservation.carId = 12345L;
        reservation.startDay = LocalDate.parse("2025-03-20");
        reservation.endDay = LocalDate.parse("2025-03-29");
        RestAssured
                .given()
                    .contentType(ContentType.JSON)
                    .body(reservation)
                .when()
                    .post(reservationResource)
                .then()
                    .statusCode(HttpStatus.SC_OK)
                    .body("id", notNullValue());
    }

    @Test
    void testAvailableCars() {
        RestAssured
            .given()
            .when()
                .get(reservationResource + "/availability")
            .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("size()", is(1),
                        "[0].id", is(1),
                        "[0].licensePlateNumber", is("ABC123"),
                        "[0].manufacturer", is("Peugeot"),
                        "[0].model", is("406"));
    }
}
