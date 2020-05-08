 package com.amee.platform.resource.returnvaluedefinition;
 
 import com.amee.base.domain.Since;
 import com.amee.base.resource.*;
 import com.amee.domain.data.ItemDefinition;
 import com.amee.domain.data.ReturnValueDefinition;
 import com.amee.domain.environment.Environment;
 import com.amee.service.definition.DefinitionService;
 import com.amee.service.environment.EnvironmentService;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.context.annotation.Scope;
 import org.springframework.stereotype.Service;
 
 @Service
 @Scope("prototype")
 @Since("3.1.0")
 public class ReturnValueDefinitionRemover implements ResourceRemover {
 
     @Autowired
     private EnvironmentService environmentService;
 
     @Autowired
     private DefinitionService definitionService;
 
     public Object handle(RequestWrapper requestWrapper) {
         String itemDefinitionIdentifier = requestWrapper.getAttributes().get("itemDefinitionIdentifier");
         if (itemDefinitionIdentifier != null) {
             Environment environment = environmentService.getEnvironmentByName("AMEE");
             ItemDefinition itemDefinition = definitionService.getItemDefinitionByUid(environment, itemDefinitionIdentifier);
             if (itemDefinition != null) {
                 String returnValueDefinitionIdentifier = requestWrapper.getAttributes().get("returnValueDefinitionIdentifier");
                 if (returnValueDefinitionIdentifier != null) {
                    ReturnValueDefinition returnValueDefinition = definitionService.getReturnValueDefinitionByUid(itemDefinition, returnValueDefinitionIdentifier);
                     if (returnValueDefinition != null) {
                         definitionService.remove(returnValueDefinition);
                         return ResponseHelper.getOK(requestWrapper);
                     } else {
                         throw new NotFoundException();
                     }
                 } else {
                     throw new MissingAttributeException("returnValueDefinitionIdentifier");
                 }
             } else {
                 throw new NotFoundException();
             }
         } else {
             throw new MissingAttributeException("itemDefinitionIdentifier");
         }
     }
 }
