 package com.amee.platform.resource.itemdefinition.v_3_4;
 
 import com.amee.base.domain.Since;
 import com.amee.base.resource.MissingAttributeException;
 import com.amee.base.resource.NotFoundException;
 import com.amee.base.resource.RequestWrapper;
 import com.amee.base.resource.ResponseHelper;
 import com.amee.base.transaction.AMEETransaction;
 import com.amee.domain.data.ItemDefinition;
 import com.amee.platform.resource.itemdefinition.ItemDefinitionResource;
 import com.amee.service.auth.ResourceAuthorizationService;
 import com.amee.service.definition.DefinitionService;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.context.annotation.Scope;
 import org.springframework.stereotype.Service;
 import org.springframework.transaction.annotation.Propagation;
 import org.springframework.transaction.annotation.Transactional;
 
 @Service
 @Scope("prototype")
 @Since("3.4.0")
 public class ItemDefinitionRemover_3_4_0 implements ItemDefinitionResource.Remover {
 
     @Autowired
     private DefinitionService definitionService;
 
     @Autowired
     private ResourceAuthorizationService resourceAuthorizationService;
 
     @Override
     @AMEETransaction
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = {Exception.class})
     public Object handle(RequestWrapper requestWrapper) {
 
         // Get ItemDefinition identifier
         String itemDefinitionIdentifier = requestWrapper.getAttributes().get("itemDefinitionIdentifier");
         if (itemDefinitionIdentifier != null) {
             ItemDefinition itemDefinition = definitionService.getItemDefinitionByUid(itemDefinitionIdentifier);
             if (itemDefinition != null) {
                 resourceAuthorizationService.ensureAuthorizedForRemove(
                        requestWrapper.getAttributes().get("activeUserUid"), itemDefinition);
 
                 definitionService.remove(itemDefinition);
                 definitionService.invalidate(itemDefinition);
                 return ResponseHelper.getOK(requestWrapper);
             } else {
                 throw new NotFoundException();
             }
         } else {
             throw new MissingAttributeException("itemDefinitionIdentifier");
         }
     }
 }
