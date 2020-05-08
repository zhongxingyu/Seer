 package net.javacrumbs.springws.test.validator;
 
 import org.custommonkey.xmlunit.Diff;
 import org.custommonkey.xmlunit.Difference;
 import org.w3c.dom.Attr;
 import org.w3c.dom.Document;
 import org.w3c.dom.Node;
 
 /**
  * Diff that ignores "${IGNORE}" placeholder and is able to correctly compare namespace prefixes in the attribute values.
  * @author Lukas Krecan
  *
  */
 final class EnhancedDiff extends Diff {
 	EnhancedDiff(Document controlDoc, Document testDoc) {
 		super(controlDoc, testDoc);
 	}
 
 	public int differenceFound(Difference difference) {
 		//ignore dissimilarities
 		if (difference.isRecoverable())
 		{
 			return RETURN_ACCEPT_DIFFERENCE;
 		}
 		if ("${IGNORE}".equals(difference.getControlNodeDetail().getValue())) {
 			return RETURN_IGNORE_DIFFERENCE_NODES_SIMILAR;
 		} 
 		else if (isDifferenceOnlyInAttributeValuePrefix(difference)) 
 		{
 			return RETURN_IGNORE_DIFFERENCE_NODES_SIMILAR;
 		}
 		else
 		{
 			return super.differenceFound(difference);
 		}
 
 	}
 
 	boolean isDifferenceOnlyInAttributeValuePrefix(Difference difference) {
 		Node testNode = difference.getTestNodeDetail().getNode();
		if (testNode==null)
		{
			return false;
		}
 		String testNodeValue = testNode.getNodeValue();
 		
 		Node controlNode = difference.getControlNodeDetail().getNode();
 		String controlNodeValue = controlNode.getNodeValue();
 		
 		if (isAttr(testNode) && isAttr(controlNode) && (hasNsPrefix(testNodeValue) || hasNsPrefix(controlNodeValue)))
 		{
 			String testValueNsResolved = resolveNamespaces(testNode, testNodeValue);
 			String controlValueNsResolved = resolveNamespaces(controlNode, controlNodeValue);
 			
 			return testValueNsResolved.equals(controlValueNsResolved);
 			
 		}
 		return false;
 	}
 
 	/**
 	 * Replaces namespace prefixes with their URLs.
 	 * @param node
 	 * @param value
 	 * @return
 	 */
 	private String resolveNamespaces(Node node, String value) {
 		int prefixLength = value.indexOf(':');
 		if (prefixLength>=0)
 		{
 			String prefix = value.substring(0, prefixLength);
 			String nsUri = node.lookupNamespaceURI(prefix);
 			if (nsUri==null)//prefix not resolved, let's use prefix instead
 			{
 				nsUri = prefix;
 			}
 			return nsUri+value.substring(prefixLength);
 		}
 		else
 		{
 			String nsUri = node.lookupNamespaceURI(null);
 			return nsUri+":"+value;
 		}
 	}
 
 	private boolean hasNsPrefix(String testNodeValue) {
 		return testNodeValue.contains(":");
 	}
 
 	private boolean isAttr(Node testNode) {
 		return testNode instanceof Attr;
 	}
 }
