 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.marbl.verplaatsingsysteem.service;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Date;
 import javax.inject.Inject;
 import javax.ws.rs.GET;
 import javax.ws.rs.Path;
 import javax.ws.rs.Produces;
 import javax.ws.rs.QueryParam;
 import javax.ws.rs.core.MediaType;
 import com.marbl.autosysteem.Movement;
import javax.ejb.Stateless;
 
 /**
  *
  * @author Eagle
  */
 @Path("/movement")
@Stateless
 public class RestfulMovementService
 {
     @Inject
     VerplaatsingSysteemService vpService;
 
     @GET
     @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
     public Collection<Movement> getAllMovements(
             @QueryParam("driverBsn") Integer driverBsn,
             @QueryParam("carTrackerId") String carTrackerId,
             @QueryParam("date") Date date
             )
     {
         Collection<Movement> movements = new ArrayList<Movement>();
         
        System.out.println(vpService);
        
         for (Movement m : vpService.findAllMovements())
         {
             if (true 
                     && (driverBsn == null || driverBsn == m.getDriverBsn() )
                     && (carTrackerId == null || carTrackerId.equals(m.getCarTrackerId()))
                     && (date == null || date.equals(m.getMovementDate())))
             {
                 movements.add(m);
             }
         }
         
         return movements;
     }
 }
