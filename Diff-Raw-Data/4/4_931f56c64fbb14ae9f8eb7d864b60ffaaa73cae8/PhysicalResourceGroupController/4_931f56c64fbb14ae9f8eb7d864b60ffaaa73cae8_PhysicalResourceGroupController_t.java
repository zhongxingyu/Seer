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
 package nl.surfnet.bod.web.noc;
 
 import static nl.surfnet.bod.web.WebUtils.CREATE;
 import static nl.surfnet.bod.web.WebUtils.DELETE;
 import static nl.surfnet.bod.web.WebUtils.EDIT;
 import static nl.surfnet.bod.web.WebUtils.ID_KEY;
 import static nl.surfnet.bod.web.WebUtils.LIST;
 import static nl.surfnet.bod.web.WebUtils.MAX_ITEMS_PER_PAGE;
 import static nl.surfnet.bod.web.WebUtils.MAX_PAGES_KEY;
 import static nl.surfnet.bod.web.WebUtils.PAGE_KEY;
 import static nl.surfnet.bod.web.WebUtils.SHOW;
 import static nl.surfnet.bod.web.WebUtils.UPDATE;
 import static nl.surfnet.bod.web.WebUtils.addInfoMessage;
 import static nl.surfnet.bod.web.WebUtils.calculateFirstPage;
 import static nl.surfnet.bod.web.WebUtils.calculateMaxPages;
 
 import java.util.Collection;
 import java.util.Collections;
 
 import javax.validation.Valid;
 import javax.validation.constraints.NotNull;
 
 import nl.surfnet.bod.domain.Institute;
 import nl.surfnet.bod.domain.PhysicalPort;
 import nl.surfnet.bod.domain.PhysicalResourceGroup;
 import nl.surfnet.bod.domain.validator.PhysicalResourceGroupValidator;
 import nl.surfnet.bod.service.InstituteService;
 import nl.surfnet.bod.service.PhysicalResourceGroupService;
 import nl.surfnet.bod.web.WebUtils;
 
 import org.hibernate.validator.constraints.Email;
 import org.hibernate.validator.constraints.NotEmpty;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.validation.BindingResult;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 import org.springframework.web.bind.annotation.ResponseBody;
 import org.springframework.web.servlet.mvc.support.RedirectAttributes;
 
 @Controller("nocPhysicalResourceGroupController")
 @RequestMapping("/noc/" + PhysicalResourceGroupController.PAGE_URL)
 public class PhysicalResourceGroupController {
 
   static final String PAGE_URL = "physicalresourcegroups";
   static final String MODEL_KEY = "physicalResourceGroupCommand";
   static final String MODEL_KEY_LIST = MODEL_KEY + WebUtils.LIST_POSTFIX;
 
   @Autowired
   private PhysicalResourceGroupService physicalResourceGroupService;
 
   @Autowired
   private InstituteService instituteIddService;
 
   @Autowired
   private PhysicalResourceGroupValidator physicalResourceGroupValidator;
 
   @RequestMapping(method = RequestMethod.POST)
   public String create(@Valid final PhysicalResourceGroupCommand command, final BindingResult bindingResult,
       final RedirectAttributes redirectAttributes, final Model model) {
 
     PhysicalResourceGroup physicalResourceGroup = new PhysicalResourceGroup();
     command.copyFieldsTo(physicalResourceGroup);
     fillInstitute(command, physicalResourceGroup);
 
     physicalResourceGroupValidator.validate(physicalResourceGroup, bindingResult);
     if (bindingResult.hasErrors()) {
       model.addAttribute(MODEL_KEY, command);
       return PAGE_URL + CREATE;
     }
 
     model.asMap().clear();
 
     physicalResourceGroupService.save(physicalResourceGroup);
 
     physicalResourceGroupService.sendAndPersistActivationRequest(physicalResourceGroup);
 
     WebUtils.addInfoMessage(redirectAttributes, "An activation request for physical resource group {} was sent to {}",
         physicalResourceGroup.getName(), physicalResourceGroup.getManagerEmail());
 
     return "redirect:" + PAGE_URL;
   }
 
   @RequestMapping(value = CREATE, method = RequestMethod.GET)
   public String createForm(final Model model) {
     model.addAttribute(MODEL_KEY, new PhysicalResourceGroupCommand());
 
     return PAGE_URL + CREATE;
   }
 
   @RequestMapping(params = ID_KEY, method = RequestMethod.GET)
   public String show(@RequestParam(ID_KEY) final Long id, final Model uiModel) {
     uiModel.addAttribute(MODEL_KEY, physicalResourceGroupService.find(id));
 
     return PAGE_URL + SHOW;
   }
 
   @RequestMapping(method = RequestMethod.GET)
   public String list(@RequestParam(value = PAGE_KEY, required = false) final Integer page, final Model model) {
     model.addAttribute(MODEL_KEY_LIST,
         physicalResourceGroupService.findEntries(calculateFirstPage(page), MAX_ITEMS_PER_PAGE));
 
     model.addAttribute(MAX_PAGES_KEY, calculateMaxPages(physicalResourceGroupService.count()));
 
     return PAGE_URL + LIST;
   }
 
   @RequestMapping(value = "/{id}/ports", method = RequestMethod.GET, produces = "application/json")
   @ResponseBody
   public Collection<PhysicalPort> listPortsJson(@PathVariable Long id) {
     PhysicalResourceGroup group = physicalResourceGroupService.find(id);
 
     if (group == null) {
       return Collections.emptyList();
     }
 
     return group.getPhysicalPorts();
   }
 
   @RequestMapping(method = RequestMethod.PUT)
   public String update(@Valid final PhysicalResourceGroupCommand command, final BindingResult result,
       final Model model, final RedirectAttributes redirectAttributes) {
 
     PhysicalResourceGroup physicalResourceGroup = physicalResourceGroupService.find(command.id);
     command.copyFieldsTo(physicalResourceGroup);
     fillInstitute(command, physicalResourceGroup);
 
     physicalResourceGroupValidator.validate(physicalResourceGroup, result);
 
     if (result.hasErrors()) {
       model.addAttribute(MODEL_KEY, command);
       return PAGE_URL + UPDATE;
     }
 
     model.asMap().clear();
 
     if (command.isManagerEmailChanged()) {
       physicalResourceGroupService.sendAndPersistActivationRequest(physicalResourceGroup);
       addInfoMessage(redirectAttributes, "A new activation email has been sent to {}",
           physicalResourceGroup.getManagerEmail());
     }
     else {
       physicalResourceGroupService.update(physicalResourceGroup);
     }
 
     return "redirect:" + PAGE_URL;
   }
 
   @RequestMapping(value = EDIT, params = ID_KEY, method = RequestMethod.GET)
   public String updateForm(@RequestParam(ID_KEY) final Long id, final Model uiModel) {
     PhysicalResourceGroup group = physicalResourceGroupService.find(id);
     instituteIddService.fillInstituteForPhysicalResourceGroup(group);
 
     uiModel.addAttribute(MODEL_KEY, new PhysicalResourceGroupCommand(group));
     return PAGE_URL + UPDATE;
   }
 
   @RequestMapping(value = DELETE, params = ID_KEY, method = RequestMethod.DELETE)
   public String delete(@RequestParam(ID_KEY) final Long id,
       @RequestParam(value = PAGE_KEY, required = false) final Integer page, final Model uiModel) {
 
     PhysicalResourceGroup physicalResourceGroup = physicalResourceGroupService.find(id);
     physicalResourceGroupService.delete(physicalResourceGroup);
 
     uiModel.asMap().clear();
 
     uiModel.addAttribute(PAGE_KEY, (page == null) ? "1" : page.toString());
 
     return "redirect:";
   }
 
   protected void setPhysicalResourceGroupValidator(PhysicalResourceGroupValidator physicalResourceGroupValidator) {
     this.physicalResourceGroupValidator = physicalResourceGroupValidator;
   }
 
   private void fillInstitute(final PhysicalResourceGroupCommand command, PhysicalResourceGroup physicalResourceGroup) {
     instituteIddService.fillInstituteForPhysicalResourceGroup(physicalResourceGroup);
     command.setInstitute(physicalResourceGroup.getInstitute());
   }
 
   public static final class PhysicalResourceGroupCommand {
 
     private Long id;
 
     private Integer version;
 
     @NotNull
     private Long instituteId;
 
     private Institute institute;
 
     @NotEmpty
     private String adminGroup;
 
     @NotEmpty
     @Email(message = "Not a valid email address")
     private String managerEmail;
 
     private boolean active = false;
 
     private boolean managerEmailChanged;
 
     public PhysicalResourceGroupCommand() {
 
     }
 
     public PhysicalResourceGroupCommand(PhysicalResourceGroup physicalResourceGroup) {
       this.id = physicalResourceGroup.getId();
       this.version = physicalResourceGroup.getVersion();
 
       this.instituteId = physicalResourceGroup.getInstituteId();
       this.institute = physicalResourceGroup.getInstitute();
       this.adminGroup = physicalResourceGroup.getAdminGroup();
       this.active = physicalResourceGroup.isActive();
       this.managerEmail = physicalResourceGroup.getManagerEmail();
     }
 
     /**
      * Copies fields this command object to the given domainOjbect. Only the
      * fields that can be changed in the UI will be copied. Determines if the
      * {@link #managerEmail} has changed.
      * 
      * @param physicalResourceGroup
      *          The {@link PhysicalResourceGroup} the copy the field to.
      */
     public void copyFieldsTo(PhysicalResourceGroup physicalResourceGroup) {
       managerEmailChanged = hasManagerEmailChanged(managerEmail, physicalResourceGroup.getManagerEmail());
 
       // Never copy id, should be generated by jpa
       physicalResourceGroup.setInstituteId(instituteId);
       physicalResourceGroup.setAdminGroup(adminGroup);
       physicalResourceGroup.setManagerEmail(managerEmail);
     }
 
     private boolean hasManagerEmailChanged(String commandEmail, String groupEmail) {
       return commandEmail == null || !commandEmail.equals(groupEmail);
     }
 
    public void setId(Long id) {
      this.id = id;
    }

     public Long getId() {
       return id;
     }
 
     public Integer getVersion() {
       return version;
     }
 
     public Long getInstituteId() {
       return instituteId;
     }
 
     public void setInstituteId(Long instituteId) {
       this.instituteId = instituteId;
     }
 
     public String getAdminGroup() {
       return adminGroup;
     }
 
     public Institute getInstitute() {
       return institute;
     }
 
     public void setInstitute(Institute institute) {
       this.institute = institute;
     }
 
     public void setAdminGroup(String adminGroup) {
       this.adminGroup = adminGroup;
     }
 
     public String getManagerEmail() {
       return managerEmail;
     }
 
     public void setManagerEmail(String managerEmail) {
       this.managerEmail = managerEmail;
     }
 
     public boolean isActive() {
       return active;
     }
 
     public boolean isManagerEmailChanged() {
       return managerEmailChanged;
     }
 
     public String getName() {
       return institute != null ? institute.getName() : String.valueOf(instituteId);
     }
 
   }
 }
