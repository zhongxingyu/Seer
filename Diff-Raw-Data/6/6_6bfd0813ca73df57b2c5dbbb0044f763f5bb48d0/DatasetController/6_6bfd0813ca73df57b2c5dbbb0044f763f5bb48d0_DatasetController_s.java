 package org.datacite.mds.web.ui.controller;
 
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.List;
 
 import javax.annotation.PostConstruct;
 import javax.validation.Valid;
 
 import org.apache.log4j.Logger;
 import org.datacite.mds.domain.AllocatorOrDatacentre;
 import org.datacite.mds.domain.Datacentre;
 import org.datacite.mds.domain.Dataset;
 import org.datacite.mds.domain.Metadata;
 import org.datacite.mds.service.HandleException;
 import org.datacite.mds.service.HandleService;
 import org.datacite.mds.service.SecurityException;
 import org.datacite.mds.util.SecurityUtils;
 import org.datacite.mds.util.Utils;
 import org.datacite.mds.web.ui.Converters;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.core.convert.support.GenericConversionService;
 import org.springframework.roo.addon.web.mvc.controller.RooWebScaffold;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.validation.BindingResult;
 import org.springframework.validation.FieldError;
 import org.springframework.validation.ObjectError;
 import org.springframework.web.bind.annotation.ModelAttribute;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 
 @RooWebScaffold(path = "datasets", formBackingObject = Dataset.class, delete = false)
 @RequestMapping("/datasets")
 @Controller
 public class DatasetController {
 
     private static Logger log = Logger.getLogger(DatasetController.class);
 
     @Autowired
     private GenericConversionService myConversionService;
 
     @Autowired
     HandleService handleService;
 
     @PostConstruct
     void registerConverters() {
         myConversionService.addConverter(Converters.getSimpleDatacentreConverter());
         myConversionService.addConverter(Converters.getSimpleDatasetConverter());
     }
 
     @RequestMapping(value = "/{id}", method = RequestMethod.GET)
     public String show(@PathVariable("id") Long id, Model model) {
         Dataset dataset = Dataset.findDataset(id);
         model.addAttribute("dataset", dataset);
         List<Metadata> metadatas = Metadata.findMetadatasByDataset(dataset).getResultList();
         model.addAttribute("metadatas", metadatas);
         try {
             Metadata metadata = metadatas.get(0);
             model.addAttribute("metadata", metadata);
             String xml = new String(metadata.getXml());
             model.addAttribute("prettyxml", Utils.formatXML(xml));
         } catch (Exception e) {
         }
         model.addAttribute("itemId", id);
         return "datasets/show";
     }
 
     @ModelAttribute("datacentres")
     public Collection<Datacentre> populateDatacentres() throws SecurityException {
         if (SecurityUtils.isLoggedInAsDatacentre()) {
             Datacentre datacentre = SecurityUtils.getCurrentDatacentre();
             return Arrays.asList(datacentre);
         } else {
            //TODO
            return null;
         }
     }
 
     @RequestMapping(method = RequestMethod.GET)
     public String list(@RequestParam(value = "page", required = false) Integer page,
             @RequestParam(value = "size", required = false) Integer size, Model model) throws SecurityException {
         AllocatorOrDatacentre user = SecurityUtils.getCurrentAllocatorOrDatacentre();
         if (page != null || size != null) {
             int sizeNo = size == null ? 10 : size.intValue();
             model.addAttribute("datasets", Dataset.findDatasetEntriesByAllocatorOrDatacentre(user, page == null ? 0 : (page
                     .intValue() - 1)
                     * sizeNo, sizeNo));
             float nrOfPages = (float) Dataset.countDatasetsByAllocatorOrDatacentre(user) / sizeNo;
             model.addAttribute("maxPages", (int) ((nrOfPages > (int) nrOfPages || nrOfPages == 0.0) ? nrOfPages + 1
                     : nrOfPages));
         } else {
             model.addAttribute("datasets", Dataset.findDatasetsByAllocatorOrDatacentre(user));
         }
         return "datasets/list";
     }
 
     @RequestMapping(method = RequestMethod.POST)
     public String create(@Valid Dataset dataset, BindingResult result, Model model) {
         if (dataset.getUrl().isEmpty()) {
             result.addError(new FieldError("", "url", "must not be empty"));
         }
 
         try {
             SecurityUtils.checkQuota(dataset.getDatacentre());
         } catch (SecurityException e) {
             ObjectError error = new ObjectError("", e.getMessage());
             result.addError(error);
         }
 
         if (!dataset.getUrl().isEmpty() && !result.hasErrors()) {
             try {
                 handleService.create(dataset.getDoi(), dataset.getUrl());
                 log.info(dataset.getDatacentre().getSymbol() + " successfuly minted (via UI) " + dataset.getDoi());
             } catch (HandleException e) {
                 String message = "HandleService: " + e.getMessage();
                 FieldError error = new FieldError("", "doi", dataset.getDoi(), false, null, null, message);
                 result.addError(error);
             }
         }
 
         if (result.hasErrors()) {
             model.addAttribute("dataset", dataset);
             return "datasets/create";
         }
 
         dataset.persist();
         dataset.getDatacentre().incQuotaUsed(Datacentre.ForceRefresh.YES);
         return "redirect:/datasets/" + dataset.getId().toString();
     }
 
     @RequestMapping(method = RequestMethod.PUT)
     public String update(@Valid Dataset dataset, BindingResult result, Model model) {
         if (!dataset.getUrl().isEmpty() && !result.hasErrors()) {
             try {
                 handleService.update(dataset.getDoi(), dataset.getUrl());
                 log.info(dataset.getDatacentre().getSymbol() + " successfuly updated (via UI) " + dataset.getDoi());
             } catch (HandleException e) {
                 log.debug("updating DOI failed; try to mint it");
                 try {
                     handleService.create(dataset.getDoi(), dataset.getUrl());
                     log.info(dataset.getDatacentre().getSymbol() + " successfuly minted (via UI) " + dataset.getDoi());
                 } catch (HandleException e1) {
                     ObjectError error = new ObjectError("", "HandleService: " + e.getMessage());
                     result.addError(error);
                 }
             }
         }
 
         if (result.hasErrors()) {
             model.addAttribute("dataset", dataset);
             return "datasets/update";
         }
         dataset.merge();
         return "redirect:/datasets/" + dataset.getId().toString();
     }
 
     @RequestMapping(params = "find=ByDoiEquals", method = RequestMethod.GET)
     public String findDatasetsByDoiEquals(@RequestParam("doi") String doi, Model model) {
         Dataset dataset = Dataset.findDatasetByDoi(doi);
         return (dataset == null) ? "datasets/show" : "redirect:/datasets/" + dataset.getId();
     }
 }
