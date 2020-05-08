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
 
 import static nl.surfnet.bod.web.WebUtils.*;
 
 import java.util.Collection;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.validation.Valid;
 
 import nl.surfnet.bod.domain.PhysicalResourceGroup;
 import nl.surfnet.bod.domain.VirtualPort;
 import nl.surfnet.bod.domain.VirtualResourceGroup;
 import nl.surfnet.bod.domain.validator.VirtualPortValidator;
 import nl.surfnet.bod.service.PhysicalResourceGroupService;
 import nl.surfnet.bod.service.VirtualPortService;
 import nl.surfnet.bod.service.VirtualResourceGroupService;
 import nl.surfnet.bod.web.security.RichUserDetails;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.security.core.context.SecurityContextHolder;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.validation.BindingResult;
 import org.springframework.web.bind.annotation.*;
 
 import com.google.common.base.Function;
 import com.google.common.collect.Collections2;
 
 @SessionAttributes({ "userContext" })
 @RequestMapping(VirtualPortController.PAGE_URL_PREFIX + VirtualPortController.PAGE_URL)
 @Controller
 public class VirtualPortController {
   public static final String PAGE_URL_PREFIX = "/manager/";
   static final String PAGE_URL = "virtualports";
 
   static final String MODEL_KEY = "virtualPort";
   static final String MODEL_KEY_LIST = MODEL_KEY + LIST_POSTFIX;
 
   @Autowired
   private VirtualPortService virtualPortService;
 
   @Autowired
   private PhysicalResourceGroupService physicalResourceGroupService;
 
   @Autowired
   private VirtualResourceGroupService virtualResourceGroupService;
 
   @Autowired
   private VirtualPortValidator virtualPortValidator;
 
   @RequestMapping(method = RequestMethod.POST)
   public String create(@Valid VirtualPort virtualPort, final BindingResult bindingResult, final Model uiModel,
       final HttpServletRequest httpServletRequest) {
 
     virtualPortValidator.validate(virtualPort, bindingResult);
     if (bindingResult.hasErrors()) {
       uiModel.addAttribute(MODEL_KEY, virtualPort);
       return PAGE_URL + CREATE;
     }
 
     uiModel.asMap().clear();
     virtualPortService.save(virtualPort);
 
     return "redirect:" + PAGE_URL;
   }
 
   @RequestMapping(value = CREATE, method = RequestMethod.GET)
   public String createForm(final Model uiModel) {
     uiModel.addAttribute(MODEL_KEY, new VirtualPort());
 
     return PAGE_URL + CREATE;
   }
 
   @RequestMapping(params = ID_KEY, method = RequestMethod.GET)
   public String show(@RequestParam(ID_KEY) final Long id, final Model uiModel) {
     uiModel.addAttribute(MODEL_KEY, virtualPortService.find(id));
     // Needed for the default icons
     uiModel.addAttribute(ICON_ITEM_KEY, id);
 
     return PAGE_URL + SHOW;
   }
 
   @RequestMapping(value = "/{name}", method = RequestMethod.GET)
   /* , headers = "accept=application/json") */
   public @ResponseBody
   Collection<VirtualPort> listForVirtualResourceGroup(@PathVariable String name) {
     Collection<VirtualPort> ports = virtualResourceGroupService.findByName(name).getVirtualPorts();
 
     //Prevent loop in json output
     return Collections2.transform(ports, new Function<VirtualPort, VirtualPort>() {
       @Override
       public VirtualPort apply(VirtualPort port) {
         VirtualPort vp = new VirtualPort();
         vp.setId(port.getId());
         vp.setName(port.getName());
         return vp;
       }
     });
   }
 
   @RequestMapping(method = RequestMethod.GET)
   public String list(@RequestParam(value = PAGE_KEY, required = false) final Integer page, final Model uiModel) {
     uiModel.addAttribute(MODEL_KEY_LIST, virtualPortService.findEntries(calculateFirstPage(page), MAX_ITEMS_PER_PAGE));
 
     uiModel.addAttribute(MAX_PAGES_KEY, calculateMaxPages(virtualPortService.count()));
 
     return PAGE_URL + LIST;
   }
 
   @RequestMapping(method = RequestMethod.PUT)
   public String update(@Valid final VirtualPort virtualPort, final BindingResult bindingResult, final Model uiModel,
       final HttpServletRequest httpServletRequest) {
 
     virtualPortValidator.validate(virtualPort, bindingResult);
     if (bindingResult.hasErrors()) {
       uiModel.addAttribute(MODEL_KEY, virtualPort);
       return PAGE_URL + UPDATE;
     }
 
     uiModel.asMap().clear();
     virtualPortService.update(virtualPort);
 
     return "redirect:" + PAGE_URL;
   }
 
   @RequestMapping(value = EDIT, params = ID_KEY, method = RequestMethod.GET)
   public String updateForm(@RequestParam(ID_KEY) final Long id, final Model uiModel) {
     uiModel.addAttribute(MODEL_KEY, virtualPortService.find(id));
     return PAGE_URL + UPDATE;
   }
 
   @RequestMapping(value = DELETE, params = ID_KEY, method = RequestMethod.DELETE)
   public String delete(@RequestParam(ID_KEY) final Long id,
       @RequestParam(value = PAGE_KEY, required = false) final Integer page, final Model uiModel) {
 
     VirtualPort virtualPort = virtualPortService.find(id);
     virtualPortService.delete(virtualPort);
 
     uiModel.asMap().clear();
     uiModel.addAttribute(PAGE_KEY, (page == null) ? "1" : page.toString());
 
     return "redirect:";
   }
 
   /**
    * Puts all {@link PhysicalResourceGroup}s related to the user on the model.
    * NOW just selects the first.
    * 
    * @param userContext
    *          {@link UserContext}
    * 
    * @return {@link PhysicalResourceGroup}
    * 
    * @throws {@link IllegalStateException} in case multiple groups are found.
    */
   @ModelAttribute(PhysicalResourceGroupController.MODEL_KEY)
   public PhysicalResourceGroup populatePhysicalResourceGroups() {
 
     RichUserDetails user = (RichUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
     Collection<PhysicalResourceGroup> findAllForUser = physicalResourceGroupService.findAllForUser(user.getNameId());
 
     // TODO View should be able to handle list
     if (findAllForUser.size() > 1) {
       throw new IllegalStateException("User [" + user + "] has more then one PhysicalResourceGroups: " + findAllForUser);
     }
 
     return findAllForUser.iterator().hasNext() ? findAllForUser.iterator().next() : new PhysicalResourceGroup();
   }
 
   @ModelAttribute(VirtualResourceGroupController.MODEL_KEY_LIST)
  public Collection<VirtualResourceGroup> populateVirtualResourceGroups() {    
    return virtualResourceGroupService.findAll();
   }
 
   /**
    * Setter to enable depedency injection from testcases.
    * 
    * @param virtualPortService
    *          {@link VirtualPortService}
    */
   protected void setVirtualPortService(VirtualPortService virtualPortService) {
     this.virtualPortService = virtualPortService;
   }
 }
