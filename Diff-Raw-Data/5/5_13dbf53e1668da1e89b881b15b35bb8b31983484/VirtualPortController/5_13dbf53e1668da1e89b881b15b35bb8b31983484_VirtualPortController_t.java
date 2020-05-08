 /**
  * The owner of the original code is SURFnet BV.
  *
  * Portions created by the original owner are Copyright (C) 2011-2012 the
  * original owner. All Rights Reserved.
  *
  * Portions created by other contributors are Copyright (C) the contributor.
  * All Rights Reserved.
  *
  * Contributor(s):
  *   (Contributors insert name & email here)
  *
  * This file is part of the SURFnet7 Bandwidth on Demand software.
  *
  * The SURFnet7 Bandwidth on Demand software is free software: you can
  * redistribute it and/or modify it under the terms of the BSD license
  * included with this distribution.
  *
  * If the BSD license cannot be found with this distribution, it is available
  * at the following location <http://www.opensource.org/licenses/BSD-3-Clause>
  */
 package nl.surfnet.bod.web;
 
 import static nl.surfnet.bod.web.WebUtils.EDIT;
 import static nl.surfnet.bod.web.WebUtils.ID_KEY;
 
 import java.util.List;
 
 import javax.annotation.Resource;
 
 import nl.surfnet.bod.domain.VirtualPort;
 import nl.surfnet.bod.service.AbstractFullTextSearchService;
 import nl.surfnet.bod.service.VirtualPortService;
 import nl.surfnet.bod.web.base.AbstractSearchableSortableListController;
 import nl.surfnet.bod.web.security.Security;
 import nl.surfnet.bod.web.view.VirtualPortView;
 
 import org.springframework.data.domain.Sort;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.validation.BindingResult;
 import org.springframework.validation.Errors;
 import org.springframework.validation.ValidationUtils;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 
 import com.google.common.collect.ImmutableList;
 
 @Controller
 @RequestMapping("/virtualports")
 public class VirtualPortController extends AbstractSearchableSortableListController<VirtualPortView, VirtualPort> {
 
   @Resource
   private VirtualPortService virtualPortService;
 
   @RequestMapping(value = EDIT, params = ID_KEY, method = RequestMethod.GET)
   public String updateForm(@RequestParam(ID_KEY) final Long id, final Model uiModel) {
     VirtualPort virtualPort = virtualPortService.find(id);
 
     if (virtualPort == null || Security.userMayNotEdit(virtualPort)) {
       return "redirect:";
     }
 
     uiModel.addAttribute("virtualPort", virtualPort);
     uiModel.addAttribute("updateUserLabelCommand", new UpdateUserLabelCommand(virtualPort));
 
     return "virtualports/update";
   }
 
   @RequestMapping(method = RequestMethod.PUT)
   public String update(final UpdateUserLabelCommand command, final BindingResult bindingResult, final Model uiModel) {
     VirtualPort virtualPort = virtualPortService.find(command.getId());
 
     if (virtualPort == null || Security.userMayNotEdit(virtualPort)) {
       return "redirect:/virtualports";
     }
 
     validateUpdateUserLabelCommand(command, bindingResult);
 
     if (bindingResult.hasErrors()) {
       uiModel.addAttribute("updateUserLabelCommand", command);
       uiModel.addAttribute("virtualPort", virtualPort);
 
       return "virtualports/update";
     }
 
     uiModel.asMap().clear();
 
     virtualPort.setUserLabel(command.getUserLabel());
     virtualPortService.update(virtualPort);
 
     return "redirect:/virtualports";
   }
 
   private void validateUpdateUserLabelCommand(UpdateUserLabelCommand command, Errors errors) {
     ValidationUtils.rejectIfEmptyOrWhitespace(errors, "userLabel", "validation.not.empty");
 
     VirtualPort existingVirtualPort = virtualPortService.findByUserLabel(command.getUserLabel());
 
     if (existingVirtualPort != null && existingVirtualPort.getUserLabel().equalsIgnoreCase(command.getUserLabel())
         && !existingVirtualPort.getId().equals(command.getId())) {
       errors.rejectValue("userLabel", "validation.not.unique");
     }
   }
 
   @Override
   protected String listUrl() {
     return "virtualports/list";
   }
 
   @Override
   protected List<VirtualPortView> list(int firstPage, int maxItems, Sort sort, Model model) {
     final List<VirtualPortView> transformToView = virtualPortService.transformToView(
         virtualPortService.findEntriesForUser(Security.getUserDetails(), firstPage, maxItems, sort),
         Security.getUserDetails());
 
     return transformToView;
   }
 
   @Override
   protected long count() {
     return virtualPortService.countForUser(Security.getUserDetails());
   }
 
   @Override
   protected String getDefaultSortProperty() {
     return "userLabel";
   }
 
   @Override
   protected List<String> translateSortProperty(String sortProperty) {
     if ("physicalResourceGroup".equals(sortProperty)) {
       return ImmutableList.of("physicalPort.physicalResourceGroup.institute.name");
     }
 
     // Optional field, might be null, then sort on managerLabel which is shown
     // when no userLabel is present
     if ("userLabel".equals(sortProperty)) {
       return ImmutableList.of("userLabel", "managerLabel");
     }
 
     return super.translateSortProperty(sortProperty);
   }
 
   public static class UpdateUserLabelCommand {
     private String userLabel;
     private Long id;
     private Integer version;
 
     public UpdateUserLabelCommand() {
     }
 
     public UpdateUserLabelCommand(VirtualPort port) {
       this.userLabel = port.getUserLabel();
       this.id = port.getId();
       this.version = port.getVersion();
     }
 
     public String getUserLabel() {
       return userLabel;
     }
 
     public void setUserLabel(String userLabel) {
       this.userLabel = userLabel;
     }
 
     public Long getId() {
       return id;
     }
 
     public void setId(Long id) {
       this.id = id;
     }
 
     public Integer getVersion() {
       return version;
     }
 
     public void setVersion(Integer version) {
       this.version = version;
     }
   }
 
   @Override
   protected AbstractFullTextSearchService<VirtualPortView, VirtualPort> getFullTextSearchableService() {
     return virtualPortService;
   }
 
   @Override
   protected String mapLabelToTechnicalName(String search) {
     if (search.startsWith("team:")) {
       return search.replace("team:", "virtualResourceGroup.name:");
     }
 
     return super.mapLabelToTechnicalName(search);
   }
 
 }
