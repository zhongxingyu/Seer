 package org.motechproject.carereporting.web.controller;
 
 import org.motechproject.carereporting.domain.LanguageEntity;
 import org.motechproject.carereporting.domain.dto.MessageDto;
 import org.motechproject.carereporting.service.LanguageService;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.http.HttpStatus;
 import org.springframework.http.MediaType;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestBody;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.ResponseBody;
 import org.springframework.web.bind.annotation.ResponseStatus;
 
 import javax.validation.Valid;
 import java.util.Set;
 
 @Controller
 @RequestMapping("/api/languages")
 public class LanguageController {
 
    private static final String TEXT_PLAIN_UTF_8 = "text/plain;charset=UTF-8";

     @Autowired
     private LanguageService messageService;
 
     @RequestMapping(method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
     @ResponseStatus(HttpStatus.OK)
     @ResponseBody
     public Set<LanguageEntity> getAllLanguages() {
         return messageService.getAllLanguages();
     }
 
     @RequestMapping(value = "{languageCode}", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
     @ResponseStatus(HttpStatus.OK)
     @ResponseBody
     public LanguageEntity getLanguageByCode(@PathVariable String languageCode) {
         return messageService.getLanguageByCode(languageCode);
     }
 
     @RequestMapping(value = "/messages", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
     @ResponseStatus(HttpStatus.OK)
     @ResponseBody
     public Set<MessageDto> getMessagedForDefaultLanguage() {
         return messageService.getMessagesForLanguage(null);
     }
 
     @RequestMapping(value = "/messages/{languageCode}", method = RequestMethod.GET,
             produces = { MediaType.APPLICATION_JSON_VALUE })
     @ResponseStatus(HttpStatus.OK)
     @ResponseBody
     public Set<MessageDto> getMessagesForLanguage(@PathVariable String languageCode) {
         return messageService.getMessagesForLanguage(languageCode);
     }
 
     @RequestMapping(value = "/messages/plain/{languageCode}", method = RequestMethod.GET,
            produces = { TEXT_PLAIN_UTF_8 })
     @ResponseStatus(HttpStatus.OK)
     @ResponseBody
     public String getPlainMessagesForLanguage(@PathVariable String languageCode) {
         return messageService.getMessagesForLanguagePlain(languageCode);
     }
 
     @RequestMapping(method = RequestMethod.POST,
             consumes = { MediaType.APPLICATION_JSON_VALUE },
             produces = { MediaType.APPLICATION_JSON_VALUE })
     @ResponseStatus(HttpStatus.OK)
     @ResponseBody
     public void defineMessagesForLanguage(@RequestBody @Valid LanguageEntity languageEntity) {
         messageService.defineLanguage(languageEntity);
     }
 
     @RequestMapping(value = "/{languageCode}", method = RequestMethod.PUT,
             consumes = { MediaType.APPLICATION_JSON_VALUE },
             produces = { MediaType.APPLICATION_JSON_VALUE })
     @ResponseStatus(HttpStatus.OK)
     @ResponseBody
     public void setMessagesForLanguage(@RequestBody @Valid LanguageEntity languageEntity) {
         messageService.updateLanguage(languageEntity);
     }
 
     @RequestMapping(value = "/{languageCode}", method = RequestMethod.DELETE,
             produces = { MediaType.APPLICATION_JSON_VALUE })
     @ResponseStatus(HttpStatus.OK)
     @ResponseBody
     public void removeLanguage(@PathVariable String languageCode) {
         messageService.removeLanguage(languageCode);
     }
 
 }
