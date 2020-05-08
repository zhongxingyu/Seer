 package com.inmapper.ws.service;
 
 import java.io.File;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.ws.rs.core.Context;
 import javax.ws.rs.core.Response;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Service;
 
 import com.inmapper.ws.evaluation.components.DataAnalysis;
 import com.inmapper.ws.evaluation.components.FileGenerator;
 import com.inmapper.ws.evaluation.components.SessionAuditor;
 import com.inmapper.ws.exception.InvalidMobilePositionException;
 import com.inmapper.ws.exception.ResourceNotFoundException;
 import com.inmapper.ws.model.to.MobileSessionTo;
 import com.inmapper.ws.model.to.RoomMappingTo;
 
 @Service
 public class MappingRESTFacadeImpl implements MappingRESTFacade {
     
     private static final Logger LOGGER = LoggerFactory.getLogger(MappingRESTFacadeImpl.class);
     
     @Context
     private HttpServletRequest request;
     
     @Autowired
     private IdGenerator generator;
     
     @Autowired
     private MappingService service;
     
     @Autowired
     private DataAnalysis analysis;
     
     @Autowired
     private SessionAuditor auditor;
     
     @Override
     public Response health() {
         LOGGER.debug("Health check received from {}", this.request.getRemoteHost()); //$NON-NLS-1$
         return Response.ok("Health check: Alive").build();
     }
     
     @Override
     public Response token() {
         String token = this.generator.next();
         
         LOGGER.debug("GET token received. Replying {}", token); //$NON-NLS-1$
         return Response.ok(String.format("{ \"token\": \"%s\" }", token)).build();
     }
     
     @Override
     public Response positions(MobileSessionTo session) throws InvalidMobilePositionException {
         this.analysis.recordSession(session);
         
         String roomId = this.service.handlePosition(session);
         
         LOGGER.debug("POST session received with {}", session); //$NON-NLS-1$
         return Response.ok(String.format("{ \"room\": \"%s\" }", roomId)).build();
     }
     
     @Override
     public Response mappings(String roomId) throws ResourceNotFoundException {
         RoomMappingTo mapping = this.service.retrieveRoomLocations(roomId);
         
         LOGGER.debug("GET room locations received with room id {}", roomId); //$NON-NLS-1$
         return Response.ok(mapping).build();
     }
     
     @Override
     public Response convert(String operation, String filename) throws InvalidMobilePositionException {
        File file = FileGenerator.existentFileForData(operation, filename);
         MobileSessionTo session = this.auditor.loadSession(file);
         
         String roomId = this.service.handlePosition(session);
         
        LOGGER.debug("GET conversion session received with {}", session); //$NON-NLS-1$
         return Response.ok(String.format("{ \"room\": \"%s\" }", roomId)).build();
     }
 }
