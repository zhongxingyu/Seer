 package cn.mobiledaily.web.controller;
 
 import cn.mobiledaily.common.exception.InternalServerError;
 import cn.mobiledaily.common.exception.InvalidValueException;
 import cn.mobiledaily.common.exception.ValidationException;
 import cn.mobiledaily.common.validation.ExchangePassword;
 import cn.mobiledaily.common.validation.Key;
 import cn.mobiledaily.common.validation.NotBlank;
 import cn.mobiledaily.module.applicant.domain.Applicant;
 import cn.mobiledaily.module.applicant.service.ApplicantService;
 import cn.mobiledaily.module.exhibition.domain.Exhibition;
 import cn.mobiledaily.module.exhibition.service.ExhibitionService;
 import cn.mobiledaily.module.exhibition.validation.ExKey;
 import cn.mobiledaily.module.mobilepush.domain.Message;
 import cn.mobiledaily.module.mobilepush.domain.MessageReceipt;
 import cn.mobiledaily.module.mobilepush.service.MessageService;
 import org.apache.commons.lang3.StringUtils;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.http.HttpStatus;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.ResponseBody;
 import org.springframework.web.bind.annotation.ResponseStatus;
 
 import java.util.*;
 
 @Controller
 @RequestMapping("messages")
 public class MessageController {
     private Logger logger = LoggerFactory.getLogger(MessageController.class);
     @Autowired
     private MessageService messageService;
     @Autowired
     private ExhibitionService exhibitionService;
     @Autowired
     private ApplicantService applicantService;
 
     @RequestMapping(value = "send", method = RequestMethod.POST)
     @ResponseStatus(HttpStatus.OK)
     public void sendMessage(
             @ExchangePassword String pwd,
             @ExKey String exKey,
             @Key("msgKey") String msgKey,
             @NotBlank String content,
             String token
     ) {
         try {
             Exhibition exhibition = exhibitionService.findByExKey(exKey);
             if (StringUtils.isNotEmpty(token)) {
                 Applicant applicant = applicantService.findByToken(token, exhibition);
                 if (applicant == null) {
                     throw new InvalidValueException("exKey+token", "valid combination",
                             String.format("exKey: %s, token: %s", exKey, token));
                 }
             }
             Message message = new Message();
             message.setExhibition(exhibition);
             message.setContent(content);
             message.setMsgKey(msgKey);
             message.setToken(token);
             messageService.save(message);
         } catch (ValidationException e){
             throw e;
         } catch (Exception e) {
             logger.error("/messages/send", e);
             throw new InternalServerError("/messages/send", e);
         }
     }
 
     @RequestMapping(value = "read", method = RequestMethod.POST)
     @ResponseStatus(HttpStatus.OK)
     public void saveReceipt(
             @ExKey String exKey,
             @NotBlank String token,
             @Key("msgKey") String msgKey
     ) {
         try {
             Exhibition exhibition = exhibitionService.findByExKey(exKey);
             Applicant applicant = applicantService.findByToken(token, exhibition);
             if (applicant == null) {
                 throw new InvalidValueException("exKey+token", "valid combination",
                         String.format("exKey: %s, token: %s", exKey, token));
             }
             Message message = messageService.findByMsgKey(msgKey, exhibition);
             if (message == null) {
                 throw new InvalidValueException("msgKey", "existed message", msgKey);
             }
             MessageReceipt receipt = messageService.findMessageReceipt(applicant);
             if (receipt == null) {
                 receipt = new MessageReceipt();
                 receipt.setApplicant(applicant);
                 receipt.setMessage(message);
             }
             receipt.setReadAt(new Date());
             messageService.save(receipt);
         } catch (ValidationException e){
             throw e;
         } catch (Exception e) {
             logger.error("/messages/send", e);
             throw new InternalServerError("/messages/send", e);
         }
     }
 
     @RequestMapping(value = "find", method = RequestMethod.GET)
     @ResponseBody
     public ExhibitionMessageWrapper findMessage(
             @ExKey String exKey,
             @NotBlank String token
     ) {
         try {
             Exhibition exhibition = exhibitionService.findByExKey(exKey);
             Applicant applicant = applicantService.findByToken(token, exhibition);
             ExhibitionMessageWrapper wrapper = new ExhibitionMessageWrapper();
             wrapper.setExKey(exKey);
             if (applicant != null) {
                 List<MessageReceipt> receipts = messageService.findMessageReceipts(applicant);
                 for (MessageReceipt receipt : receipts) {
                     Message message = receipt.getMessage();
                     ExhibitionMessage exhibitionMessage = new ExhibitionMessage();
                     exhibitionMessage.setMsgKey(message.getMsgKey());
                     exhibitionMessage.setContent(message.getContent());
                     exhibitionMessage.setCreatedAt(message.getCreatedAt().getTime());
                     if (receipt.getReadAt() != null) {
                         exhibitionMessage.setRead("Y");
                     }
                     wrapper.getList().add(exhibitionMessage);
                 }
                 Collections.sort(wrapper.getList(), new Comparator<ExhibitionMessage>() {
                     @Override
                     public int compare(ExhibitionMessage o1, ExhibitionMessage o2) {
                         return (int) (o2.getCreatedAt() - o1.getCreatedAt());
                     }
                 });
             }
             return wrapper;
         } catch (ValidationException e) {
             throw e;
         } catch (Exception e) {
             logger.error("/messages/find", e);
             throw new InternalServerError("/messages/find", e);
         }
     }
 
     public static class ExhibitionMessageWrapper {
         String exKey;
         List<ExhibitionMessage> list = new LinkedList<>();
 
         public String getExKey() {
             return exKey;
         }
 
         public void setExKey(String exKey) {
             this.exKey = exKey;
         }
 
         public List<ExhibitionMessage> getList() {
             return list;
         }
     }
 
     public static class ExhibitionMessage {
         String msgKey;
         String content;
         long createdAt;
         String read = "N";
 
         public String getMsgKey() {
             return msgKey;
         }
 
         public void setMsgKey(String msgKey) {
             this.msgKey = msgKey;
         }
 
         public String getContent() {
             return content;
         }
 
         public void setContent(String content) {
             this.content = content;
         }
 
         public long getCreatedAt() {
             return createdAt;
         }
 
         public void setCreatedAt(long createdAt) {
             this.createdAt = createdAt;
         }
 
         public String getRead() {
             return read;
         }
 
         public void setRead(String read) {
             this.read = read;
         }
     }
 }
