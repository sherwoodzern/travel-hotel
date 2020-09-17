package com.oracle.camunda.travel;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.oracle.camunda.travel.dataaccess.HotelDAO;
import com.oracle.camunda.travel.model.Hotel;

public class HotelScheduler {

    private final WebTarget client;

    @Inject
    private HotelDAO hotelDAO;
    

    public HotelScheduler() {
        this.client = ClientBuilder
            .newClient()
            .target(System.getenv("OrchestrationEngine_URL"));
    }

    public void scheduleHotel() {

        Future<Response> response = null;
        try {

            System.out.printf("The fetchAndLock payload %s\n", fetchAndLockPayload().toString());
            System.setProperty("sun.net.http.allowRestrictedHeaders", "true");
            response = client
                .path("/external-task/fetchAndLock")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .header("Host", "camunda-local.com")
                .async()
                .post(Entity.json(fetchAndLockPayload().toString()));
        
            try {
                
                Response result = response.get();
                System.out.println("Fetch and Lock completed");
                if (result.getStatus() == 200) {
                    JsonArray entities = (JsonArray) result.readEntity(JsonArray.class);
                    entities.forEach( elem -> {   
                        
                        System.out.printf("The result status %d\n", result.getStatus());
                        JsonObject entity = (JsonObject) elem;
                        System.out.printf("The entity %s\n", entity.asJsonObject().toString());
                        JsonObject variables = (JsonObject) entity.get("variables");
                        Hotel hotel = reserveARoom(variables);
                        if (hotel != null) {
                            System.out.printf("Hotel Selected: %s\n", hotel.asJson());
                            completeTheTask(entity.getString("id"), hotel, variables.getJsonObject("ecid").getString("value"));
                        }
                        else
                            System.out.println("The hotel no longer has available rooms. We need to compensate");
                    });
                } else {
                    System.out.printf("The HTTP status %d status info %s\n", 
                        result.getStatus(), 
                        result.getStatusInfo().getReasonPhrase());
                    System.out.printf("The entity class %s\n", result.getEntity().getClass().getName());
                    System.out.printf("The entity %s\n", result.getEntity().toString());

                }
            } catch (InterruptedException iex) {
                System.out.printf("Interrupted Exception %s\n", iex.getMessage());
                iex.printStackTrace();
            } catch (ExecutionException eex) {

                System.out.printf("ExecutionException %s\n", eex.getMessage());
                eex.printStackTrace();
            }
        } catch (Exception ex) {
                System.out.printf("fetchAndLock exception %s\n", ex.getMessage());
                ex.printStackTrace();
        }
    }

    private Hotel reserveARoom(JsonObject variables) {
        try {
            String origination = ((JsonObject) variables.get("origination")).getString("value");
            String startStr = ((JsonObject) variables.get("startDate")).getString("value");
            SimpleDateFormat dtFormat = new SimpleDateFormat("MM-dd-yyyy");
            Date departureDate = dtFormat.parse(startStr);
            // Airline airline = airlineDAO.getAirline(origination, departureDate);
            hotelDAO = new HotelDAO();
            Hotel hotel = hotelDAO.getMockHotel();
            if (hotel.getRooms() > 0) {
                hotel.setRooms(hotel.getRooms() - 1);
                return hotelDAO.reserveMockHotel(hotel);
                // return airlineDAO.reserveAirline(airline);
            } else {
                // Need to throw an exception for no rooms available
                return null;
            }
        } catch (ParseException pe) {
        }

        return null;
    }
    
    private void completeTheTask(String activityId, Hotel hotel, String ecid) {
        
        System.out.println("************** Entering completeTheTask ****************");
        JsonObject payload = Json.createObjectBuilder()
            .add("workerId", "travel-request")
            .add("variables", Json.createObjectBuilder())
                .add("ecid", Json.createObjectBuilder()
                    .add("value", ecid)
                    .add("type", "String"))
                .add("hotel", Json.createObjectBuilder()
                    .add("value", hotel.asJson())
                    .add("type", "String"))
            .build();
        
        try {

            System.out.println("Invoking the completeTheTask");
            System.setProperty("sun.net.http.allowRestrictedHeaders", "true");
            Future<Response> response = client
                .path("/external-task/" + activityId + "/complete")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .header("Host", "camunda-local.com")
                .async()
                .post(Entity.json(payload.toString()));
            
            Response result = response.get();
            System.out.println("The response has been returned");
            if (result.getStatus() == 204) {
                System.out.printf("Success in processing the travel request for ecid %s\n", ecid);
            } else {
                System.out.printf("Failure in processing the travel request for ecid %s %d\n", ecid, result.getStatus());
            }
        } catch (InterruptedException iex) {
            System.out.printf("InterruptedException from completeTheTask %s\n", iex.getMessage());

        } catch (ExecutionException eex) {
            System.out.printf("ExecutionException from completeTheTask %s\n", eex.getMessage());
        }
        
    }
    
    private JsonObject fetchAndLockPayload() {
        JsonObject payload = Json.createObjectBuilder()
            .add("workerId", "travel-request")
            .add("maxTasks", 1)
            .add("usePriority", true)
            .add("asyncResponseTimeout", 5000)
            .add("topics", Json.createArrayBuilder()
                .add(Json.createObjectBuilder()
                    .add("topicName", "hotel")
                    .add("lockDuration", 60000))
                .build())
            .build();
        return payload;
    }
}