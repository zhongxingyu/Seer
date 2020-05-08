 /*
  * JBoss, Home of Professional Open Source.
  * Copyright 2012, Red Hat, Inc., and individual contributors
  * as indicated by the @author tags. See the copyright.txt file in the
  * distribution for a full listing of individual contributors.
  *
  * This is free software; you can redistribute it and/or modify it
  * under the terms of the GNU Lesser General Public License as
  * published by the Free Software Foundation; either version 2.1 of
  * the License, or (at your option) any later version.
  *
  * This software is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this software; if not, write to the Free
  * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
  * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
  */
 package org.picketlink.as.subsystem.model.idp;
 
 import static org.picketlink.as.subsystem.model.ModelElement.COMMON_ALIAS;
 import static org.picketlink.as.subsystem.model.ModelElement.COMMON_URL;
 
 import org.jboss.as.controller.OperationContext;
 import org.jboss.as.controller.OperationContext.Stage;
import org.jboss.as.controller.descriptions.ModelDescriptionConstants;
 import org.jboss.as.controller.OperationFailedException;
 import org.jboss.as.controller.OperationStepHandler;
 import org.jboss.as.controller.PathAddress;
 import org.jboss.dmr.ModelNode;
 import org.picketlink.as.subsystem.model.ModelElement;
 import org.picketlink.as.subsystem.service.IDPConfigurationService;
 
 /**
  * @author <a href="mailto:psilva@redhat.com">Pedro Silva</a>
  */
 public class IgnoreInSignMsgHandler implements OperationStepHandler {
  
     public static final IgnoreInSignMsgHandler INSTANCE = new IgnoreInSignMsgHandler();
  
     /* (non-Javadoc)
      * @see org.jboss.as.controller.OperationStepHandler#execute(org.jboss.as.controller.OperationContext, org.jboss.dmr.ModelNode)
      */
     @Override
     public void execute(OperationContext context, ModelNode operation) throws OperationFailedException {
         final boolean ignoreIncomingSignatures = operation.require("value").asBoolean();
         
         ModelNode node = context.readResourceForUpdate(PathAddress.EMPTY_ADDRESS).getModel();
         
         node.get(ModelElement.IDENTITY_PROVIDER_IGNORE_INCOMING_SIGNATURES.getName()).set(ignoreIncomingSignatures);
         
        final String alias = operation.get(ModelDescriptionConstants.OP_ADDR).asPropertyList().get(2).getValue().asString();
         
         context.addStep(new OperationStepHandler() {
             @Override
             public void execute(OperationContext context, ModelNode operation) throws OperationFailedException {
                 IDPConfigurationService service = (IDPConfigurationService) context.getServiceRegistry(true).getRequiredService(IDPConfigurationService.createServiceName(alias)).getValue();
                 service.getIdpConfiguration().setIgnoreIncomingSignatures(ignoreIncomingSignatures);
                 context.completeStep();
             }
         }, Stage.RUNTIME);
 
         
         context.completeStep();
     }
 }
