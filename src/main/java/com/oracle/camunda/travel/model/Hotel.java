package com.oracle.camunda.travel.model;

import javax.json.Json;
import javax.json.JsonObject;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Column;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Entity;
import javax.persistence.NamedQuery;
import javax.persistence.Id;

@Entity
@Table (name="Hotel")
@NamedQuery (name="findAllHotels",
            query="Select h from Hotel h")
@NamedQuery (name="findHotel", 
            query="Select h from Hotel h where h.city = :city and h.arrivalDate = :arrivalDate")
@NamedQuery (name="reserveHotel",
            query="Update Hotel h set h.rooms = :availableRooms where h.hotelId = :hotelId")
@SequenceGenerator(name="HOTEL_ID_SEQ", initialValue = 100, allocationSize = 10)
public class Hotel {

    @Id
    @GeneratedValue(strategy=GenerationType.SEQUENCE, generator="HOTEL_ID_SEQ")
    @Column (
        name="HOTELID",
        insertable = true,
        nullable = false,
        updatable = true 
    )
    private int hotelId;

    @Column (
        name="NAME",
        insertable = true,
        nullable = false,
        updatable = true 
    )
    private String name;

    @Column (
        name="ARRIVALDATE",
        insertable = true, 
        nullable = false, 
        updatable = true 
    )
    private Date arrivalDate;

    @Column (
        name="ROOMS",
        insertable = true, 
        nullable = false, 
        updatable = true 
    )
    private int rooms;

    @Column (
        name="CITY",
        insertable = true, 
        nullable = false, 
        updatable = true 
    )
    private String city; 

    /**
     * @return String return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return Date return the arrivalDate
     */
    public Date getArrivalDate() {
        return arrivalDate;
    }

    /**
     * @param arrivalDate the arrivalDate to set
     */
    public void setArrivalDate(Date arrivalDate) {
        this.arrivalDate = arrivalDate;
    }

    /**
     * @return int return the rooms
     */
    public int getRooms() {
        return rooms;
    }

    /**
     * @param rooms the rooms to set
     */
    public void setRooms(int rooms) {
        this.rooms = rooms;
    }

    /**
     * @return String return the city
     */
    public String getCity() {
        return city;
    }

    /**
     * @param city the city to set
     */
    public void setCity(String city) {
        this.city = city;
    }

    /**
     * @return int return the hotelId
     */
    public int getHotelId() {
        return hotelId;
    }

    /**
     * @param hotelId the hotelId to set
     */
    public void setHotelId(int hotelId) {
        this.hotelId = hotelId;
    }

    public String asJson() {
        DateFormat dtFormat = new SimpleDateFormat("MM-dd-yyyy");
        String departureDateStr = dtFormat.format(getArrivalDate());
        JsonObject json = Json.createObjectBuilder()
            .add("HotelId", getHotelId())
            .add("Name", getName())
            .add("DepartureDate", departureDateStr)
            .add("ArrivalCity", getCity())
            .build();
        
        return json.toString();
    }

}