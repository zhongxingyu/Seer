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
 import org.springframework.transaction.annotation.Transactional;
 
 @Service
 @Scope("prototype")
 @Since("3.1.0")
 public class ReturnValueDefinitionBuilder implements ResourceBuilder {
 
     // TODO: Include ValueDefinition.
     // TODO: Add audit (created, modified).
 
     @Autowired
     private EnvironmentService environmentService;
 
     @Autowired
     private DefinitionService definitionService;
 
     @Autowired
     private RendererBeanFinder rendererBeanFinder;
 
     private ReturnValueDefinitionRenderer returnValueDefinitionRenderer;
 
     @Transactional(readOnly = true)
     public Object handle(RequestWrapper requestWrapper) {
         // Get Environment.
         Environment environment = environmentService.getEnvironmentByName("AMEE");
         // Get ItemDefinition identifier.
         String itemDefinitionIdentifier = requestWrapper.getAttributes().get("itemDefinitionIdentifier");
         if (itemDefinitionIdentifier != null) {
             // Get ItemDefinition.
             ItemDefinition itemDefinition = definitionService.getItemDefinitionByUid(
                     environment, itemDefinitionIdentifier);
             if (itemDefinition != null) {
                 // Get ReturnValueDefinition identifier.
                 String returnValueDefinitionIdentifier = requestWrapper.getAttributes().get("returnValueDefinitionIdentifier");
                 if (returnValueDefinitionIdentifier != null) {
                     // Get ReturnValueDefinition.
                    ReturnValueDefinition returnValueDefinition = definitionService.getReturnValueDefinitionByUid(
                             itemDefinition, returnValueDefinitionIdentifier);
                     if (returnValueDefinition != null) {
                         // Handle the ReturnValueDefinition.
                         handle(requestWrapper, returnValueDefinition);
                         ReturnValueDefinitionRenderer renderer = getReturnValueDefinitionRenderer(requestWrapper);
                         renderer.ok();
                         return renderer.getObject();
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
 
     protected void handle(
             RequestWrapper requestWrapper,
             ReturnValueDefinition returnValueDefinition) {
 
         ReturnValueDefinitionRenderer renderer = getReturnValueDefinitionRenderer(requestWrapper);
         renderer.start();
 
         boolean full = requestWrapper.getMatrixParameters().containsKey("full");
         boolean itemDefinition = requestWrapper.getMatrixParameters().containsKey("itemDefinition");
         boolean type = requestWrapper.getMatrixParameters().containsKey("type");
         boolean units = requestWrapper.getMatrixParameters().containsKey("units");
         boolean flags = requestWrapper.getMatrixParameters().containsKey("flags");
 
         // New ReturnValueDefinition & basic.
         renderer.newReturnValueDefinition(returnValueDefinition);
         renderer.addBasic();
 
         // Optional attributes.
         if ((itemDefinition || full) && (returnValueDefinition.getItemDefinition() != null)) {
             ItemDefinition id = returnValueDefinition.getItemDefinition();
             renderer.addItemDefinition(id);
         }
         if (units || full) {
             renderer.addUnits();
         }
         if (type || full) {
             renderer.addType();
         }
         if (flags || full) {
             renderer.addFlags();
         }
     }
 
     public ReturnValueDefinitionRenderer getReturnValueDefinitionRenderer(RequestWrapper requestWrapper) {
         if (returnValueDefinitionRenderer == null) {
             returnValueDefinitionRenderer = (ReturnValueDefinitionRenderer) rendererBeanFinder.getRenderer(ReturnValueDefinitionRenderer.class, requestWrapper);
         }
         return returnValueDefinitionRenderer;
     }
 }
