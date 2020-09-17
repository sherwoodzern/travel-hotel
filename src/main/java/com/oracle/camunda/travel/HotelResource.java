
package com.oracle.camunda.travel;


import javax.inject.Inject;


public class HotelResource {


    @Inject
    public HotelResource() {
    }


    public static void main(String... args) {
        System.out.println("Invoking the main method and connecting to the Camunda engine");
        System.setProperty("sun.net.http.allowRestrictedHeaders", "true");
        HotelScheduler scheduler = new HotelScheduler();
        while (true) {
            scheduler.scheduleHotel();
        }
        

    }
}
