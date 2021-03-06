 /*
  * Copyright (c) WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package org.wso2.developerstudio.eclipse.gmf.esb.internal.persistence;
 
 import java.util.List;
 import java.util.Map.Entry;
 
 import org.apache.synapse.endpoints.Endpoint;
 import org.apache.synapse.mediators.base.SequenceMediator;
 import org.apache.synapse.util.xpath.SynapseXPath;
 import org.eclipse.core.runtime.Assert;
 import org.eclipse.emf.ecore.EObject;
 import org.wso2.carbon.mediator.transform.Input;
 import org.wso2.carbon.mediator.transform.Output;
 import org.wso2.carbon.mediator.transform.SmooksMediator.TYPES;
 import org.wso2.developerstudio.eclipse.gmf.esb.EsbNode;
 import org.wso2.developerstudio.eclipse.gmf.esb.NamespacedProperty;
 import org.wso2.developerstudio.eclipse.gmf.esb.OutputMethod;
 import org.wso2.developerstudio.eclipse.gmf.esb.SmooksIODataType;
 import org.wso2.developerstudio.eclipse.gmf.esb.SmooksMediator;
 import org.wso2.developerstudio.eclipse.gmf.esb.persistence.TransformationInfo;
 
 public class SmooksMediatorTransformer extends AbstractEsbNodeTransformer {
 
 	public void transform(TransformationInfo information, EsbNode subject)
 			throws Exception {
 		information.getParentSequence().addChild(createSmooksMediator(information,subject));
 		// Transform the property mediator output data flow path.
 		doTransform(information,
 				((SmooksMediator) subject).getOutputConnector());
 		
 		
 	}
 
 	public void createSynapseObject(TransformationInfo info, EObject subject,
 			List<Endpoint> endPoints) {		
 	}
 
 	public void transformWithinSequence(TransformationInfo information,
 			EsbNode subject, SequenceMediator sequence) throws Exception {
 		sequence.addChild(createSmooksMediator(information,subject));
 		doTransformWithinSequence(information,((SmooksMediator) subject).getOutputConnector().getOutgoingLink(),sequence);
 		
 		
 	}
 	private org.wso2.carbon.mediator.transform.SmooksMediator createSmooksMediator(TransformationInfo information,EsbNode subject) throws Exception{
 		// Check subject.
 		Assert.isTrue(subject instanceof SmooksMediator, "Invalid subject.");
 		SmooksMediator visualSmooks = (SmooksMediator) subject;
 		
 		org.wso2.carbon.mediator.transform.SmooksMediator smooksMediator=new org.wso2.carbon.mediator.transform.SmooksMediator();
 		{
 			Input input = new Input();
			input.setExpression(new SynapseXPath(visualSmooks.getInputExpression()
					.getPropertyValue()));
 			input.setType((visualSmooks.getInputType().equals(SmooksIODataType.XML)) ? TYPES.XML
 					: TYPES.TEXT);
 			smooksMediator.setInput(input);
 			Output output = new Output();
 			output.setType((visualSmooks.getOutputType().equals(SmooksIODataType.XML)) ? TYPES.XML
 					: TYPES.TEXT);
 			if (visualSmooks.getOutputMethod().equals(OutputMethod.PROPERTY)) {
 				output.setProperty(visualSmooks.getOutputProperty());
 			} else if (visualSmooks.getOutputMethod().equals(OutputMethod.EXPRESSION)) {
 				output.setAction(visualSmooks.getOutputAction().getLiteral().toLowerCase());
 				NamespacedProperty namespacedProperty = visualSmooks.getOutputExpression();
 				SynapseXPath expression = new SynapseXPath(namespacedProperty.getPropertyValue());
 				for (Entry<String, String> entry : namespacedProperty.getNamespaces().entrySet()) {
 					expression.addNamespace(entry.getKey(), entry.getValue());
 				}
 				output.setExpression(expression);
 			}
 			smooksMediator.setOutput(output);
 			smooksMediator.setConfigKey(visualSmooks.getConfigurationKey().getKeyValue());
 		}
 		return smooksMediator;
 	}
 
 
 }
