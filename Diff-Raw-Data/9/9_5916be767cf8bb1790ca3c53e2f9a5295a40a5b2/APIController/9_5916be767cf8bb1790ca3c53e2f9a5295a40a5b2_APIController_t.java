 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 package drinkcounter.web.controllers.api;
 
 import com.csvreader.CsvWriter;
 import com.google.common.base.Charsets;
 import drinkcounter.DrinkCounterService;
 import drinkcounter.HistoryService;
 import drinkcounter.model.Participant;
 import drinkcounter.model.ParticipantHistory;
 import drinkcounter.model.PartyHistory;
 import drinkcounter.util.PartyMarshaller;
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.io.OutputStreamWriter;
 import java.util.List;
 import org.joda.time.DateTime;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.http.HttpHeaders;
 import org.springframework.http.HttpStatus;
 import org.springframework.http.ResponseEntity;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestParam;
 import org.springframework.web.bind.annotation.ResponseBody;
 
 /**
  *
  * @author Toni
  */
 @Controller
 public class APIController {
 
     private static final Logger log = LoggerFactory.getLogger(APIController.class);
 
 
     @Autowired
     private PartyMarshaller partyMarshaller;
 
     @Autowired
     private DrinkCounterService service;
 
     @Autowired
     private HistoryService historyService;
 
     @RequestMapping("/{partyId}")
     public @ResponseBody byte[] printXml(@PathVariable String partyId) throws IOException{
         ByteArrayOutputStream baos = new ByteArrayOutputStream();
         partyMarshaller.marshall(partyId, baos);
         byte[] bytesXml = baos.toByteArray();
         return bytesXml;
     }
 
     @RequestMapping("/{partyId}/add-drink")
     public @ResponseBody String addDrink(@PathVariable String partyId, @RequestParam("participant-id") String participantId){
         service.addDrink(participantId);
         Participant participant = service.getParticipant(participantId);
         return participant.getTotalDrinks().toString();
     }
 
     @RequestMapping("/{partyId}/add-participant")
     public @ResponseBody String addDrink(@PathVariable String partyId,
             @RequestParam("name") String name,
             @RequestParam("sex") String sex,
             @RequestParam("weight") float weight){
         Participant participant = new Participant();
         participant.setName(name);
         participant.setSex(Participant.Sex.valueOf(sex));
         participant.setWeight(weight);
         participant = service.addParticipant(participant, partyId);
         return participant.getId();
     }
 
     @RequestMapping("/{partyId}/remove-participant")
     public void removeParticipant(@RequestParam("participant-id") String participantId){
         service.deleteParticipant(participantId);
     }
 
     @RequestMapping("/{partyId}/show-history")
     public ResponseEntity<byte[]> showHistory(@PathVariable String partyId) throws IOException{
         HttpHeaders headers = new HttpHeaders();
         headers.set("Content-Type", "text/plain;charset=utf-8");
         ByteArrayOutputStream baos = new ByteArrayOutputStream();
         CsvWriter csvWriter = new CsvWriter(new OutputStreamWriter(baos, Charsets.UTF_8), ',');
         csvWriter.writeRecord(new String[]{"Time", "Name", "Alcohol Level", "Drinks"});
         List<PartyHistory> partyHistories = historyService.getPartyHistory(partyId);
         for (PartyHistory partyHistory : partyHistories) {
             DateTime time = new DateTime(partyHistory.getSnapshotTime());
             for (ParticipantHistory participantHistory : partyHistory.getParticipants()) {
                 csvWriter.writeRecord(
                         new String[]{
                             time.toString(),
                             participantHistory.getParticipantName(),
                             Float.toString(participantHistory.getAlcoholLevel()),
                             participantHistory.getTotalDrinks().toString()
                         }
                 );
             }
         }
         csvWriter.close();
         byte[] bytes = baos.toByteArray();
         return new ResponseEntity<byte[]>(bytes, headers, HttpStatus.OK);
     }
 }
