package com.oracle.camunda.travel.dataaccess;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;

import com.oracle.camunda.travel.model.Hotel;
import java.util.Date;

public class HotelDAO {

    @PersistenceContext(unitName="HotelService")
    private EntityManager entityManager;


    @Transactional
    public Hotel getHotelInCityForDate(String city, Date startDate) {

        return entityManager
            .createNamedQuery("findHotel", Hotel.class)
            .setParameter("city", city)
            .setParameter("arrivalDate", startDate)
            .getSingleResult();

    }

    @Transactional
    public void reserveHotel(Hotel hotel) {

        entityManager
            .createNamedQuery("reserveHotel", Hotel.class)
            .setParameter("hotelId", hotel.getHotelId())
            .setParameter("availableCount", hotel.getRooms())
            .getSingleResult();
        
    }

    public Hotel getMockHotel() {
        Hotel hotel = new Hotel();
        hotel.setCity("SFO");
        hotel.setName("Marriott");
        hotel.setHotelId(100);
        hotel.setArrivalDate(new Date());
        hotel.setRooms(100);
        return hotel;

    }

    public Hotel reserveMockHotel(Hotel hotel) {
        return hotel;
    }
    
}