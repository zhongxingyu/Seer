 /*
  * Copyright by Apaq 2011-2013
  */
 package dk.drb.blacktiger.controller;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import javax.annotation.PostConstruct;
 import javax.servlet.AsyncContext;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import dk.drb.blacktiger.model.Participant;
 import dk.drb.blacktiger.service.BlackTigerEventListener;
 import dk.drb.blacktiger.service.IBlackTigerService;
 import dk.drb.blacktiger.service.ParticipantEvent;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestBody;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.ResponseBody;
 import org.springframework.web.servlet.ModelAndView;
 
 /**
  * Javadoc
  */
 @Controller
 public class RoomController {
 
     private static final Logger LOG = LoggerFactory.getLogger(RoomController.class);
     private final IBlackTigerService service;
     private final List<ChangeListenerEntry> changeListeners = Collections.synchronizedList(new ArrayList<ChangeListenerEntry>());
     private final BlackTigerEventListener tigerEventListener = new BlackTigerEventListener() {
         @Override
         public void onParticipantEvent(ParticipantEvent event) {
             String roomNo = event.getRoomNo();
             LOG.debug("Recieved event for room '{}'.", roomNo);
 
             List<ChangeListenerEntry> clonedList = new ArrayList<ChangeListenerEntry>(changeListeners);
             Iterator<ChangeListenerEntry> it = clonedList.iterator();
             while (it.hasNext()) {
                 ChangeListenerEntry entry = it.next();
                 if (entry.getRoomNo().equals(roomNo)) {
                     try {
                         AsyncContext ctx = entry.getAsyncContext();
                         respondChanged((HttpServletResponse) ctx.getResponse(), true);
                         ctx.complete();
                     } catch (IllegalStateException ex) {
                     }
                 } else {
                     it.remove();
                 }
             }
             
             LOG.debug("Removing {} listeners.", clonedList.size());
             changeListeners.removeAll(clonedList);
 
         }
     };
 
     private class ChangeListenerEntry {
 
         private AsyncContext asyncContext;
         private String roomNo;
 
         public ChangeListenerEntry(AsyncContext asyncContext, String roomNo) {
             this.asyncContext = asyncContext;
             this.roomNo = roomNo;
         }
 
         public AsyncContext getAsyncContext() {
             return asyncContext;
         }
 
         public String getRoomNo() {
             return roomNo;
         }
     }
 
     @Autowired
     public RoomController(IBlackTigerService service) {
         this.service = service;
     }
 
     @PostConstruct
     public void init() {
         service.addEventListener(tigerEventListener);
     }
 
     @RequestMapping(value = "/rooms/{roomNo}/changes")
     public void listenForChange(HttpServletRequest request, HttpServletResponse response, @PathVariable final String roomNo) {
         LOG.debug("Adding new listener [remoteIp={};roomNo={}]", request.getRemoteAddr(), roomNo);
         AsyncContext asyncContext = request.startAsync();
         asyncContext.setTimeout(60000);
         changeListeners.add(new ChangeListenerEntry(asyncContext, roomNo));
     }
 
     @RequestMapping(value = "/rooms/{roomNo}", headers = "Accept=application/json")
     @ResponseBody
     public List<Participant> showRoomAsJson(@PathVariable final String roomNo) {
         LOG.debug("Got JSON request for room '{}'.", roomNo);
         return service.listParticipants(roomNo);
     }
 
     @RequestMapping(value = "/rooms/{roomNo}/{participantId}", headers = "Accept=application/json")
     @ResponseBody
     public Participant showParticipanteAsJson(@PathVariable final String roomNo, @PathVariable final String participantId) {
         LOG.debug("Got JSON request for participant in room [room={};participant={}].", roomNo, participantId);
         return service.getParticipant(roomNo, participantId);
     }
 
     @RequestMapping(value = "/rooms/{roomNo}/{participantId}/kick", method = RequestMethod.POST, headers = "Accept=application/json")
     @ResponseBody
     public int kickParticipantAsJson(@PathVariable final String roomNo, @PathVariable final String participantId) {
         LOG.debug("Kicking participant from room [room={};participant={}].", roomNo, participantId);
         service.kickParticipant(roomNo, participantId);
         return 1;
     }
 
     @RequestMapping(value = "/rooms/{roomNo}/{participantId}/mute", method = RequestMethod.POST, headers = "Accept=application/json")
     @ResponseBody
     public int muteParticipantAsJson(@PathVariable final String roomNo, @PathVariable final String participantId) {
         LOG.debug("Muting participant in room [room={};participant={}].", roomNo, participantId);
        service.kickParticipant(roomNo, participantId);
         return 1;
     }
 
     @RequestMapping(value = "/rooms/{roomNo}/{participantId}/unmute", method = RequestMethod.POST, headers = "Accept=application/json")
     @ResponseBody
     public int unmuteParticipantAsJson(@PathVariable final String roomNo, @PathVariable final String participantId) {
         LOG.debug("Unmuting participant in room [room={};participant={}].", roomNo, participantId);
        service.kickParticipant(roomNo, participantId);
         return 1;
     }
 
     @RequestMapping("/rooms/{roomNo}")
     public ModelAndView showRoom(@PathVariable final String roomNo) {
         final List<Participant> list = service.listParticipants(roomNo);
         return new ModelAndView("room", "participants", list);
     }
 
     private void respondChanged(HttpServletResponse response, boolean value) {
         try {
             response.setContentType("application/json");
             response.getOutputStream().print(value);
             response.getOutputStream().close();
             response.flushBuffer();
         } catch (IOException ex) {
             LOG.info("Unable to respond.", ex);
         }
     }
 }
