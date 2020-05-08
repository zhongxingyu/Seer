 package org.s23m.cell.communication.xml.sax;
 
 import org.s23m.cell.communication.xml.XmlSchemaTerminology;
 import org.s23m.cell.communication.xml.model.dom.Namespace;
 import org.s23m.cell.communication.xml.model.dom.Node;
 import org.s23m.cell.communication.xml.model.schemainstance.Identity;
 import org.s23m.cell.communication.xml.model.schemainstance.StringElement;
 import org.xml.sax.Attributes;
 
 public class IdentityProcessor implements SaxElementProcessor<Identity> {
 
 	@Override
 	public Identity startElement(Namespace namespace,
 			XmlSchemaTerminology terminology,
 			Node top,
 			Attributes attributes) {
 		
 		final String identifier = attributes.getValue(terminology.identifier());
 		final String name = attributes.getValue(terminology.name());
 		final String pluralName = attributes.getValue(terminology.pluralName());
 		final String technicalName = attributes.getValue(terminology.technicalName());
 		
 		final Identity identity = new Identity(namespace, terminology);
 		identity.setIdentifier(identifier);
		identity.setName(name);
 		identity.setPluralName(pluralName);
 		identity.setTechnicalName(technicalName);
 		return identity;
 	}
 
 	@Override
 	public void endElement(Node removed, Node top, String textContent) {
 		if (top instanceof Identity) {
 			StringElement removedElement = (StringElement) removed;
 			String removedElementContents = removedElement.getText();
 			((Identity) top).setPayload(removedElementContents);
 		}
 	}
 
 }
