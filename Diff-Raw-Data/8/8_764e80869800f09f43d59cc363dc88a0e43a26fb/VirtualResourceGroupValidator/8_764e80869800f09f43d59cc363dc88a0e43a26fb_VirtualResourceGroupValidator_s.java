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
 package nl.surfnet.bod.domain.validator;
 
 import nl.surfnet.bod.domain.VirtualResourceGroup;
 import nl.surfnet.bod.service.VirtualResourceGroupService;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Component;
 import org.springframework.validation.Errors;
 import org.springframework.validation.Validator;
 
 /**
  * Validator for the {@link VirtualResourceGroup}. Validates that the
  * {@link VirtualResourceGroup#getSurfConextGroupName()} is unique and exists
  * in SurfConext.
  *
  * @author Franky
  *
  */
 @Component
 public class VirtualResourceGroupValidator implements Validator {
 
   @Autowired
   private VirtualResourceGroupService virtualResourceGroupService;
 
   private ValidatorHelper validatorHelper = new ValidatorHelper();
 
   @Override
   public boolean supports(Class<?> clazz) {
     return VirtualResourceGroup.class.isAssignableFrom(clazz);
   }
 
   @Override
   public void validate(Object objToValidate, Errors errors) {
     VirtualResourceGroup virtualResourceGroup = (VirtualResourceGroup) objToValidate;
 
     VirtualResourceGroup existingVirtualResourceGroup = virtualResourceGroupService.findByName(virtualResourceGroup.getName());
 
     if (existingVirtualResourceGroup == null) {
       return;
     }
 
    if (!validatorHelper.validateNameUniqueness(virtualResourceGroup.getId().equals(existingVirtualResourceGroup.getId()),
        virtualResourceGroup.getName().equalsIgnoreCase(existingVirtualResourceGroup.getName()),
        virtualResourceGroup.getId() != null)) {
       errors.rejectValue("name", "validation.not.unique");
     }
   }
 
   void setVirtualResourceGroupService(VirtualResourceGroupService virtualResourceGroupService) {
     this.virtualResourceGroupService = virtualResourceGroupService;
   }
 }
