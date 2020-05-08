 /**
 Copyright (C) 2012  Delcyon, Inc.
 
 This program is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.
 
 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.
 
 You should have received a copy of the GNU General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package com.delcyon.capo.xml;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.transform.OutputKeys;
 import javax.xml.transform.Transformer;
 import javax.xml.transform.TransformerFactory;
 
 import org.w3c.dom.Attr;
 import org.w3c.dom.DOMException;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.NamedNodeMap;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 import org.w3c.dom.Text;
 
 import com.delcyon.capo.util.XMLSerializer;
 import com.delcyon.capo.util.diff.InputStreamTokenizer;
 import com.delcyon.capo.util.diff.Window;
 import com.delcyon.capo.util.diff.WindowItem;
 import com.delcyon.capo.util.diff.WindowItemLink;
 import com.delcyon.capo.util.diff.XMLTextDiff;
 import com.delcyon.capo.util.diff.Diff.Side;
 import com.delcyon.capo.util.diff.InputStreamTokenizer.TokenList;
 import com.delcyon.capo.xml.cdom.CNode;
 
 /**
  * @author jeremiah
  *
  */
 public class XMLDiff
 {
 	
 	public static final String XDIFF_ROOT_ELEMENT_NAME = "root";
 	
 	public static final String XDIFF_ATTRIBUTE_ELEMENT_NAME = "attribute";
 	public static final String XDIFF_ATTRIBUTE_ATTRIBUTE_NAME = "attribute";
 	public static final String XDIFF_VALUE_ATTRIBUTE_NAME = "value";
 	public static final String XDIFF_NAME_ATTRIBUTE_NAME = "name";
 	
 	public static final String XDIFF_TEXT_ELEMENT_NAME = "text";
 	public static final String XDIFF_TEXT_ATTRIBUTE_NAME = "text";
 	public static final String XDIFF_DIFF_ELEMENT_NAME = "diff";
 	public static final String XDIFF_POSITION_ATTRIBUTE_NAME = "pos";
 	public static final String XDIFF_TOKENLIST_ATTRIBUTE_NAME = "tokenList";
 	
 	public static final String XDIFF_ELEMENT_ATTRIBUTE_NAME = "element";
 	
 	public static final String XDIFF_PREFIX = "xdiff";
 	public static final String XDIFF_NAMESPACE_URI = "http://www.delcyon.com/xdiff";
 	public static final String NAMESPACE_NAMESPACE_URI = "http://www.w3.org/2000/xmlns/";
 	
 	public static final int DEFAULT_USE_DIFF_TEXT_LENGTH = 5;
 
 	private static final String XDIFF_DIFF_ATTRIBUTE_NAME = "diff";
 	
 	public static final String EQUALITY = Side.BASE+" = "+Side.MOD;
 	public static final String INEQUALITY = Side.BASE+" != "+Side.MOD;
 	
 	
 
 	
 	public enum TextDiffType
 	{
 		ALWAYS_USE_DIFFERENCE,
 		NEVER_USE_DIFFERENCE,
 		USE_DIFFERENCE_BASED_ON_LENGTH
 	}
 	
 	private TokenList tokenList = TokenList.NEW_LINE;
 	private ArrayList<ArrayList<Integer>> tokenLists = null;
 	
 	private Transformer transformer;
 	private DocumentBuilder documentBuilder;
 	private TextDiffType textDiffType = TextDiffType.USE_DIFFERENCE_BASED_ON_LENGTH;
 	private int useDiffTextLength = DEFAULT_USE_DIFF_TEXT_LENGTH;
 	private HashMap<String, String> ignoreableAttributeMap = new HashMap<String, String>();
     private boolean allowNamespaceMismatches = false;
 	private boolean ignoreContentDifferences = false;
 	private boolean ignoreNamespaceDeclarations = false;
 	
 	public XMLDiff() throws Exception
 	{
 		//setup xml input
 		DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
 		documentBuilderFactory.setNamespaceAware(true);
 		documentBuilder = documentBuilderFactory.newDocumentBuilder();
 		
 		//setup xml output
 		TransformerFactory tFactory = TransformerFactory.newInstance();
 		transformer = tFactory.newTransformer();
 		transformer.setOutputProperty(OutputKeys.INDENT, "yes");
 	}
 	
 	public Document getDifferences(Document baseDocument, Document otherDocument) throws Exception
 	{
 		
 		Document differenceDocument = documentBuilder.newDocument();
 		Element rootDifferenceElement = createElement(differenceDocument, XDIFF_NAMESPACE_URI, XDIFF_PREFIX, XDIFF_ROOT_ELEMENT_NAME);
 		getDifferences(differenceDocument,rootDifferenceElement,baseDocument.getDocumentElement(), otherDocument.getDocumentElement());
 		differenceDocument.appendChild(rootDifferenceElement);
 		
 		return differenceDocument;
 	}
 	
 	public TextDiffType getTextDiffType()
 	{
 		return textDiffType;
 	}
 	
 	public void setTextDiffType(TextDiffType textDiffType)
 	{
 		this.textDiffType = textDiffType;
 	}
 	
 	public int getUseDiffTextLength()
 	{
 		return useDiffTextLength;
 	}
 	
 	public void setUseDiffTextLength(int useDiffTextLength)
 	{
 		this.useDiffTextLength = useDiffTextLength;
 	}
 	
 	public void setTokenList(TokenList tokenList)
 	{
 		this.tokenList = tokenList;
 	}
 	
 	public TokenList getTokenList()
 	{
 		return tokenList;
 	}
 	
 	public void setTokenLists(ArrayList<ArrayList<Integer>> tokenLists)
 	{
 		this.tokenLists = tokenLists;
 		this.tokenList = TokenList.CUSTOM;
 	}
 	
 	public ArrayList<ArrayList<Integer>> getTokenLists()
 	{
 		return tokenLists;
 	}
 	
 	public boolean isAllowNamespaceMismatches()
     {
         return allowNamespaceMismatches;
     }
 	
 	/**
 	 * This defaults to false. This attempts to turn off namespace aware diffing for the document tress. 
 	 * It's results can be fairly unpredictable. So don't use unless there is no other option. 
 	 * @param allowNamespaceMismatches
 	 */
 	public void setAllowNamespaceMismatches(boolean allowNamespaceMismatches)
     {
         this.allowNamespaceMismatches = allowNamespaceMismatches;
     }
 	
 	/**
 	 * This defaults to false. This can be used to compare only the structure of two trees.
 	 * @param ignoreContentDifferences
 	 */
 	public void setIgnoreContentDifferences(boolean ignoreContentDifferences)
     {
         this.ignoreContentDifferences = ignoreContentDifferences;
     }
 	
 	public boolean isIgnoreContentDifferences()
     {
         return ignoreContentDifferences;
     }
 	
 	/**
 	 * defaults to false. This can be used to ignore xmlns attributes. 
 	 * Different implementations and versions of XML parsers can relocate these declarations all over the place. 
 	 * @param ignoreNamespaceDeclarations
 	 */
 	public void setIgnoreNamespaceDeclarations(boolean ignoreNamespaceDeclarations)
     {
         this.ignoreNamespaceDeclarations = ignoreNamespaceDeclarations;
     }
 	
 	public boolean isIgnoreNamespaceDeclarations()
     {
         return ignoreNamespaceDeclarations;
     }
 	
 	public Element getDifferences(Element baseElement, Element otherElement) throws Exception
 	{
 		Document differenceDocument = documentBuilder.newDocument();
 		Element differenceElement = getDifferences(differenceDocument, null, baseElement, otherElement);
 		return differenceElement;
 	}
 	
 	/**
 	 * comparison must start out at a point where two element are the same name, and namespace
 	 * @param baseElement
 	 * @param otherElement
 	 * @return differenceElement
 	 * @throws Exception
 	 */
 	public Element getDifferences(Document differenceDocument, Element parentDifferenceElement ,Element baseElement, Element otherElement) throws Exception
 	{
 		
 		String baseLocalName = baseElement.getLocalName();
 		String basePrefix = baseElement.getPrefix();
 		String baseNamespaceURI = baseElement.getNamespaceURI();
 		
 		Element differenceElement = createElement(differenceDocument, baseNamespaceURI, basePrefix, baseLocalName);		
 		setAttribute(differenceElement, XDIFF_NAMESPACE_URI, XDIFF_PREFIX, XDIFF_ELEMENT_ATTRIBUTE_NAME, EQUALITY);
 		if (parentDifferenceElement != null)
 		{
 			parentDifferenceElement.appendChild(differenceElement);
 		}
 		
 		//process attributes
 		boolean attributesAreEqual = processAttributes(differenceDocument,differenceElement,baseElement,otherElement,Side.BASE);
 		if (attributesAreEqual == false)
 		{			
 			setAttribute(differenceElement, XDIFF_NAMESPACE_URI, XDIFF_PREFIX, XDIFF_ELEMENT_ATTRIBUTE_NAME, INEQUALITY);
 		}
 
 		attributesAreEqual = processAttributes(differenceDocument,differenceElement,otherElement,baseElement,Side.MOD);
 		if (attributesAreEqual == false)
 		{			
 			setAttribute(differenceElement, XDIFF_NAMESPACE_URI, XDIFF_PREFIX, XDIFF_ELEMENT_ATTRIBUTE_NAME, INEQUALITY);
 		}
 		
 		//process children
 		NodeList baseChildNodeList = baseElement.getChildNodes();
 		NodeList otherChildNodeList = otherElement.getChildNodes();
 		
 		
 		
 		Window baseWindow = new Window(Side.BASE, baseChildNodeList.getLength());	
 		readIntoWindow(baseWindow,baseChildNodeList);
 		
 		
 		
 		Window otherWindow = new Window(Side.MOD, otherChildNodeList.getLength());		
 		readIntoWindow(otherWindow, otherChildNodeList);
 		boolean hasMatches = false;
 		if (otherChildNodeList.getLength() > baseChildNodeList.getLength())
 		{
 
 			//buildMatch Table
 			for (WindowItem otherWindowItem : otherWindow.getWindowItems())
 			{			
 				if (baseWindow.hasMatch(otherWindowItem) == true)
 				{	
 					hasMatches = true;
 					otherWindowItem.addMatches(baseWindow.getMatches(otherWindowItem));				
 				}
 				else
 				{
 					//skip		
 				}
 			}
 
 		}
 		else
 		{
 			//buildMatch Table
 			for (WindowItem baseWindowItem : baseWindow.getWindowItems())
 			{			
 				if (otherWindow.hasMatch(baseWindowItem) == true)
 				{	
 					hasMatches = true;
 					baseWindowItem.addMatches(otherWindow.getMatches(baseWindowItem));				
 				}
 				else
 				{
 					//skip		
 				}
 			}
 		}
 		
 		boolean hasMatchesButNoChains = false;
 		if (baseWindow.getChains().size() == 0 && hasMatches == true)
 		{
 			hasMatchesButNoChains = true;
 		}
 		
 		//Diff.printMatchTable(baseWindow, otherWindow);
 		/*
 		 * basic algorithm
 		 * read from base children comparing to currentOtherIndex 
 		 * outputting base children until match is found
 		 * output matches from both sides incrementing both positions until no match is found
 		 * read from other children comparing to currentBaseIndex 
 		 * outputting other children until match is found
 		 * output matches from both sides incrementing both positions until no match is found
 		 * repeat
 		 * when end of children is reached, output opposite side until that list is exhausted  
 		 */
 		
 		while(true)
 		{	
 			
 			
 			int baseWindowItemsIndex = 0;
 			//get list of window items to walk through
 			ArrayList<WindowItem> baseWindowItems = baseWindow.getWindowItems();
 			ArrayList<WindowItem> otherWindowItems = otherWindow.getWindowItems();
 			if(baseWindowItems.isEmpty() && otherWindowItems.isEmpty())
 			{
 				break;
 			}
 			
 			//get cheapest chain for first window item
 			ArrayList<WindowItemLink> currentChain = baseWindow.getCheapestChain(baseWindow,otherWindow);
 			
 			//because XML can have data where the number of children is commonly one, but there is a match
 			//we have to do this because the standard diff algorithm only works for matched pairs and larger
 			//so we essentially fake a chain
 			if (currentChain == null && hasMatchesButNoChains == true)
 			{
 				WindowItemLink firstMatch = baseWindow.getFirstMatch();
 				if (firstMatch != null)
 				{
 					currentChain = new ArrayList<WindowItemLink>();
 					currentChain.add(firstMatch);
 				}
 			}
 			
 			long baseWindowChainStartPosition = -1;
 			long otherWindowChainStartPosition = -1;
 			long otherWindowChainEndPosition = -1;
 			
 			int currentChainSize = 0;
 			
 			if(currentChain == null)
 			{
 				//give us a bogus end to read to
 				//add one to the ends to make sure we read fully when dealing with the difference below.
 				if (otherWindowItems.isEmpty() == false)
 				{					
 					otherWindowChainStartPosition = otherWindowItems.get(otherWindowItems.size() - 1).getStreamPosition()+1l;					
 					otherWindowChainEndPosition = otherWindowItems.get(otherWindowItems.size() - 1).getStreamPosition()+1l;
 				}
 				if (baseWindowItems.isEmpty() == false)
 				{					
 					baseWindowChainStartPosition = baseWindowItems.get(baseWindowItems.size()-1).getStreamPosition()+1l;
 				}
 				
 			}
 			else 
 			{
 				 baseWindowChainStartPosition = currentChain.get(0).getBaseWindowItem().getStreamPosition();
 				 
 				 otherWindowChainStartPosition = currentChain.get(0).getOtherWindowItem().getStreamPosition();				 
 				 otherWindowChainEndPosition = currentChain.get(currentChain.size()-1).getOtherWindowItem().getStreamPosition();
 			}
 			
 			
 			if (currentChain != null)
 			{
 				currentChainSize = currentChain.size();
 			}
 			//figure out distance from currentWindowItem.streamPosition to start of chain
 			//walk to cheapest chain start position in the base window
 			
 			if (baseWindowItems.isEmpty() == false)
 			{
 				int diffrence = (int) (baseWindowChainStartPosition - baseWindowItems.get(0).getStreamPosition());
 				for(int currentIndex = 0;currentIndex < diffrence; currentIndex++)
 				{					
 					Node outputNode = (Node) baseWindowItems.get(currentIndex).getObject();					
 					
 					if (outputNode instanceof Element)
 					{
 						Element tempDifferenceElement = (Element) differenceDocument.importNode(outputNode, true);
 						differenceElement.appendChild(tempDifferenceElement);
 						setAttribute(differenceElement, XDIFF_NAMESPACE_URI, XDIFF_PREFIX, XDIFF_ELEMENT_ATTRIBUTE_NAME, INEQUALITY);
 						setAttribute((Element)tempDifferenceElement, XDIFF_NAMESPACE_URI, XDIFF_PREFIX, XDIFF_ELEMENT_ATTRIBUTE_NAME, baseWindowItems.get(currentIndex).getSide());
 					}
 					else if (outputNode instanceof Text)
 					{
 						Element differenceTextElement = createElement(differenceDocument, XDIFF_NAMESPACE_URI, XDIFF_PREFIX, XDIFF_TEXT_ATTRIBUTE_NAME);								
 						setAttribute(differenceTextElement, XDIFF_NAMESPACE_URI, XDIFF_PREFIX, XDIFF_TEXT_ATTRIBUTE_NAME, Side.BASE);
 						differenceTextElement.appendChild(differenceDocument.importNode(outputNode, true));
 						differenceElement.appendChild(differenceTextElement);
 						setAttribute(differenceElement, XDIFF_NAMESPACE_URI, XDIFF_PREFIX, XDIFF_ELEMENT_ATTRIBUTE_NAME, INEQUALITY);
 					}
 
 					baseWindowItemsIndex++;				
 					
 					if (parentDifferenceElement != null)
 					{
 						setAttribute(parentDifferenceElement, XDIFF_NAMESPACE_URI, XDIFF_PREFIX, XDIFF_ELEMENT_ATTRIBUTE_NAME, INEQUALITY);
 					}
 				}
 			}
 			
 			
 			//walk to cheapest chain start position in the other window
 			if (otherWindowItems.isEmpty() == false)
 			{
 				int otherWindowItemsIndex = 0;
 				while(otherWindowItems.size() > otherWindowItemsIndex && otherWindowItems.get(otherWindowItemsIndex).getStreamPosition() < otherWindowChainStartPosition)
 				{
 					Node outputNode = null;
 					outputNode = differenceDocument.importNode((Node) otherWindowItems.get(otherWindowItemsIndex).getObject(),true);
 					
 					if (outputNode instanceof Element)
 					{
 						differenceElement.appendChild(outputNode);
 						setAttribute((Element)outputNode, XDIFF_NAMESPACE_URI, XDIFF_PREFIX, XDIFF_ELEMENT_ATTRIBUTE_NAME, Side.MOD);
 						setAttribute(differenceElement, XDIFF_NAMESPACE_URI, XDIFF_PREFIX, XDIFF_ELEMENT_ATTRIBUTE_NAME, INEQUALITY);
 					}
 					else if (outputNode instanceof Text)
 					{
 						Element differenceTextElement = createElement(differenceDocument, XDIFF_NAMESPACE_URI, XDIFF_PREFIX, XDIFF_TEXT_ATTRIBUTE_NAME);								
 						setAttribute(differenceTextElement, XDIFF_NAMESPACE_URI, XDIFF_PREFIX, XDIFF_TEXT_ATTRIBUTE_NAME, Side.MOD);
 						differenceTextElement.appendChild(outputNode);
 						differenceElement.appendChild(differenceTextElement);
 						setAttribute(differenceElement, XDIFF_NAMESPACE_URI, XDIFF_PREFIX, XDIFF_ELEMENT_ATTRIBUTE_NAME, INEQUALITY);
 					}
 					
 					otherWindowItemsIndex++;									
 					if (parentDifferenceElement != null)
 					{
 						setAttribute(parentDifferenceElement, XDIFF_NAMESPACE_URI, XDIFF_PREFIX, XDIFF_ELEMENT_ATTRIBUTE_NAME, INEQUALITY);
 					}
 				}
 			}
 			//walk the chain
 			if (currentChain != null)
 			{
 				for (WindowItemLink windowItemLink : currentChain)
 				{
 					Node baseOutputNode = (Node) windowItemLink.getWindowItemForSide(Side.BASE).getObject();
 					Node otherOutputNode = (Node) windowItemLink.getWindowItemForSide(Side.MOD).getObject();
 					if (baseOutputNode instanceof Element)
 					{
 						
 						getDifferences(differenceDocument, differenceElement, (Element) baseOutputNode, (Element) otherOutputNode);
 						//XXX this may not work at all!!!
 						//this is based on the assumption, that any child node that finds itself not equal, will set it's parent to not equal
 						//since the difference element at this point is the parent, after we get done processing, we check to see if it is not equal, and if so, set it's parent to not equal
 						if (differenceElement.getAttributeNS(XDIFF_NAMESPACE_URI, XDIFF_ELEMENT_ATTRIBUTE_NAME).equals(INEQUALITY))
 						{
 							if (parentDifferenceElement != null)
 							{
 								setAttribute(parentDifferenceElement, XDIFF_NAMESPACE_URI, XDIFF_PREFIX, XDIFF_ELEMENT_ATTRIBUTE_NAME, INEQUALITY);
 							}
 						}
 					}
 					else if (baseOutputNode instanceof Text)
 					{
 					    String baseOutputNodeValue = baseOutputNode.getNodeValue();
 					    String otherOutputNodeValue = otherOutputNode.getNodeValue();
 					    if(isIgnoreContentDifferences())
 	                    {
 					        baseOutputNodeValue = "";
 	                        otherOutputNodeValue = "";
 	                    }
 					    
 						if (baseOutputNodeValue.equals(otherOutputNodeValue) == true)
 						{
 							//these are the same so just add it							
 							differenceElement.appendChild(differenceDocument.createTextNode(otherOutputNodeValue));
 						}
 						else
 						{
 							if (textDiffType == TextDiffType.ALWAYS_USE_DIFFERENCE || (textDiffType == TextDiffType.USE_DIFFERENCE_BASED_ON_LENGTH && (baseOutputNode.getTextContent().length() >= useDiffTextLength && otherOutputNode.getTextContent().length() >= useDiffTextLength)))
 							{
 								//run a diff on the text nodes
 								XMLTextDiff xmlTextDiff = new XMLTextDiff();
 								Element diffTextElement = xmlTextDiff.getDifferenceElement((Text)baseOutputNode, (Text)otherOutputNode,tokenList,tokenLists);
								differenceElement.appendChild(differenceDocument.adoptNode(diffTextElement));								
 							}
 							else
 							{
 								//too small or diff not wanted 
 								//these aren't the same so add it as a diff
 								Element differenceBaseTextElement = createElement(differenceDocument, XDIFF_NAMESPACE_URI, XDIFF_PREFIX, XDIFF_TEXT_ELEMENT_NAME);									
 								setAttribute(differenceBaseTextElement, XDIFF_NAMESPACE_URI, XDIFF_PREFIX, XDIFF_TEXT_ATTRIBUTE_NAME, Side.BASE);
 								differenceBaseTextElement.appendChild(differenceDocument.importNode(baseOutputNode, true));
 								differenceElement.appendChild(differenceBaseTextElement);	
 
 								Element differenceOtherTextElement = createElement(differenceDocument, XDIFF_NAMESPACE_URI, XDIFF_PREFIX, XDIFF_TEXT_ELEMENT_NAME);									
 								setAttribute(differenceOtherTextElement, XDIFF_NAMESPACE_URI, XDIFF_PREFIX, XDIFF_TEXT_ATTRIBUTE_NAME, Side.MOD);
 								differenceOtherTextElement.appendChild(differenceDocument.importNode(otherOutputNode, true));
 								differenceElement.appendChild(differenceOtherTextElement);

								//because we're creating a couple of sub child elements, we need to make sure the parent knows we've got an inequality  
								if (parentDifferenceElement != null)
                                {
                                    setAttribute(parentDifferenceElement, XDIFF_NAMESPACE_URI, XDIFF_PREFIX, XDIFF_ELEMENT_ATTRIBUTE_NAME, INEQUALITY);
                                }
 							}
 							setAttribute(differenceElement, XDIFF_NAMESPACE_URI, XDIFF_PREFIX, XDIFF_ELEMENT_ATTRIBUTE_NAME, INEQUALITY);								
 						}
 					}					
 				}
 			}
 			//cleanup
 			if (baseWindowItems.isEmpty() == false)
 			{
 				long baseWindowEndStreamPosition = baseWindowItems.get(0).getStreamPosition()+baseWindowItemsIndex+(long)currentChainSize-1l;
 				baseWindow.removeUntil(baseWindowEndStreamPosition);
 			}
 			if (otherWindowItems.isEmpty() == false)
 			{
 				otherWindow.removeUntil(otherWindowChainEndPosition);
 			}
 		}
 		if (parentDifferenceElement != null && parentDifferenceElement.hasAttributeNS(XDIFF_NAMESPACE_URI, XDIFF_ELEMENT_ATTRIBUTE_NAME) == false)
 		{
 		    if(differenceElement.hasAttributeNS(XDIFF_NAMESPACE_URI, XDIFF_ELEMENT_ATTRIBUTE_NAME))
 		    {
 		        setAttribute(parentDifferenceElement, XDIFF_NAMESPACE_URI, XDIFF_PREFIX, XDIFF_ELEMENT_ATTRIBUTE_NAME, differenceElement.getAttributeNS(XDIFF_NAMESPACE_URI, XDIFF_ELEMENT_ATTRIBUTE_NAME));
 		    }
 		    else
 		    {
 		        setAttribute(parentDifferenceElement, XDIFF_NAMESPACE_URI, XDIFF_PREFIX, XDIFF_ELEMENT_ATTRIBUTE_NAME, EQUALITY);
 		    }
 		}
		
		//if all of our children where un-diffed, then we need to remove any notations from them
		if( differenceElement.hasAttributeNS(XDIFF_NAMESPACE_URI, XDIFF_ELEMENT_ATTRIBUTE_NAME) && differenceElement.hasChildNodes() && differenceElement.getAttributeNS(XDIFF_NAMESPACE_URI, XDIFF_ELEMENT_ATTRIBUTE_NAME).equals(EQUALITY))
		{
		    NodeList nodeList = differenceElement.getChildNodes();
		    for(int index = 0; index < nodeList.getLength(); index++)
		    {
		        if(nodeList.item(index) instanceof Element)
		        {
		            ((Element) nodeList.item(index)).removeAttribute(XDIFF_PREFIX+":"+XDIFF_ELEMENT_ATTRIBUTE_NAME);
		        }
		    }
		}
 		return differenceElement;
 	}
 	
 	
 	private void readIntoWindow(Window window,NodeList nodeList)
 	{
 		for(int index = 0; index < nodeList.getLength(); index++)
 		{
 			Node childNode = nodeList.item(index);
 			if (childNode instanceof Element)
 			{
 				window.addWindowItem((childNode.getNodeName()).getBytes(),childNode);
 			}
 			else if(childNode instanceof Text)
 			{
 				if (childNode.getNodeValue().trim().isEmpty() == false)
 				{
 					window.addWindowItem("#text:".getBytes(),childNode);
 				}
 			}
 				
 		}
 	}
 
 	
 	
 	private boolean processAttributes(Document differenceDocument, Element differenceElement, Element baseElement, Element otherElement, Side direction)
 	{
 		boolean attributesAreEqual = true;
 		//compare attributes
 		NamedNodeMap baseElementAttributeNamedNodeMap = baseElement.getAttributes();
 		for(int index = 0; index < baseElementAttributeNamedNodeMap.getLength(); index++ )
 		{
 			Attr attribute = (Attr) baseElementAttributeNamedNodeMap.item(index);
 			String attributeLocalName = attribute.getLocalName();
 			String attributePrefix = attribute.getPrefix();
 			String attributeNamesapceURI = attribute.getNamespaceURI();
			if(attributeLocalName == null && attributeNamesapceURI == null)
			{
			    attributeLocalName = attribute.getName();
			}
 			
 			if(isIgnoreNamespaceDeclarations() && NAMESPACE_NAMESPACE_URI.equals(attributeNamesapceURI))
 			{
 			    continue;
 			}
 			
 			//see if we are supposed to ignore this
 			if (ignoreableAttributeMap.containsKey(attributeNamesapceURI+":"+attributeLocalName))
 			{				
 			    continue;
 			}
 
 			
 			if (attributeNamesapceURI != null || allowNamespaceMismatches == false)
 			{
 
 			    String attributeValue = attribute.getValue();
 			    if(isIgnoreContentDifferences())
 			    {
 			        attributeValue = "";
 			    }
 			    String attributeName = attribute.getName();
 			    if (otherElement.hasAttributeNS(attributeNamesapceURI, attributeLocalName) == false)
 			    {
 			        Element xdiffAttributeElement = createElement(differenceDocument, XDIFF_NAMESPACE_URI, XDIFF_PREFIX, XDIFF_ATTRIBUTE_ELEMENT_NAME);				
 			        setAttribute(xdiffAttributeElement, XDIFF_NAMESPACE_URI, XDIFF_PREFIX, XDIFF_ATTRIBUTE_ATTRIBUTE_NAME, direction);
 			        setAttribute(xdiffAttributeElement, XDIFF_NAMESPACE_URI, XDIFF_PREFIX, XDIFF_NAME_ATTRIBUTE_NAME, attributeName);
 			        setAttribute(xdiffAttributeElement, XDIFF_NAMESPACE_URI, XDIFF_PREFIX, XDIFF_VALUE_ATTRIBUTE_NAME, attributeValue);
 
 			        differenceElement.appendChild(xdiffAttributeElement);
 			        attributesAreEqual = false;
 			    }
 			    else //has the attribute
 			    {
 			        //compare values
 			        String otherValue = otherElement.getAttributeNS(attributeNamesapceURI, attributeLocalName);
 			        if(isIgnoreContentDifferences())
 	                {
 			            otherValue = "";
 			            
 	                }
 			        if(attributeValue.equals(otherValue))
 			        {			            
 			            setAttribute(differenceElement, attributeNamesapceURI, attributePrefix, attributeLocalName,attributeValue);
 			        }
 			        //only compare values in one direction
 			        else if(direction == Side.BASE)
 			        {
 			            Element xdiffAttributeElement = createElement(differenceDocument, XDIFF_NAMESPACE_URI, XDIFF_PREFIX, XDIFF_ATTRIBUTE_ELEMENT_NAME);
 			            setAttribute(xdiffAttributeElement, XDIFF_NAMESPACE_URI, XDIFF_PREFIX, XDIFF_NAME_ATTRIBUTE_NAME, attributeName);
 			            setAttribute(xdiffAttributeElement, XDIFF_NAMESPACE_URI, XDIFF_PREFIX, Side.BASE, attributeValue);
 			            setAttribute(xdiffAttributeElement, XDIFF_NAMESPACE_URI, XDIFF_PREFIX, Side.MOD, otherValue);					
 			            differenceElement.appendChild(xdiffAttributeElement);
 			            attributesAreEqual = false;
 			        }
 			    }
 			}
 			else if(allowNamespaceMismatches == true)//dealing with empty namespaces
 			{
 			    String attributeValue = attribute.getValue();
                 String attributeName = attribute.getName();
                 if(isIgnoreContentDifferences())
                 {
                     attributeValue = "";
                 }
                 if (otherElement.hasAttribute(attributeName) == false)
                 {
                     Element xdiffAttributeElement = createElement(differenceDocument, XDIFF_NAMESPACE_URI, XDIFF_PREFIX, XDIFF_ATTRIBUTE_ELEMENT_NAME);             
                     setAttribute(xdiffAttributeElement, XDIFF_NAMESPACE_URI, XDIFF_PREFIX, XDIFF_ATTRIBUTE_ATTRIBUTE_NAME, direction);
                     setAttribute(xdiffAttributeElement, XDIFF_NAMESPACE_URI, XDIFF_PREFIX, XDIFF_NAME_ATTRIBUTE_NAME, attributeName);
                     setAttribute(xdiffAttributeElement, XDIFF_NAMESPACE_URI, XDIFF_PREFIX, XDIFF_VALUE_ATTRIBUTE_NAME, attributeValue);
 
                     differenceElement.appendChild(xdiffAttributeElement);
                     attributesAreEqual = false;
                 }
                 else //has the attribute
                 {
                     //compare values
                     String otherValue = otherElement.getAttribute( attributeName);
                     if(isIgnoreContentDifferences())
                     {
                         otherValue = "";
                         
                     }
                     if(attributeValue.equals(otherValue))
                     {                        
                         setAttribute(differenceElement, attributeNamesapceURI, attributePrefix, attributeLocalName,attributeValue);
                     }
                     //only compare values in one direction
                     else if(direction == Side.BASE)
                     {
                         Element xdiffAttributeElement = createElement(differenceDocument, XDIFF_NAMESPACE_URI, XDIFF_PREFIX, XDIFF_ATTRIBUTE_ELEMENT_NAME);
                         setAttribute(xdiffAttributeElement, XDIFF_NAMESPACE_URI, XDIFF_PREFIX, XDIFF_NAME_ATTRIBUTE_NAME, attributeName);
                         setAttribute(xdiffAttributeElement, XDIFF_NAMESPACE_URI, XDIFF_PREFIX, Side.BASE, attributeValue);
                         setAttribute(xdiffAttributeElement, XDIFF_NAMESPACE_URI, XDIFF_PREFIX, Side.MOD, otherValue);                   
                         differenceElement.appendChild(xdiffAttributeElement);
                         attributesAreEqual = false;
                     }
                 }
 			}
 		}
 		return attributesAreEqual;
 	}
 
 	public Element getElementForSide(Element differenceElement, Side side) throws Exception
 	{
 		//XPath.dumpNode(differenceElement, System.out);
 		Document sideDocument = documentBuilder.newDocument();
 		
 		Element tempRootElement = createElement(sideDocument, differenceElement.getNamespaceURI(), differenceElement.getPrefix(), differenceElement.getLocalName());
 		
 		sideDocument.appendChild(tempRootElement);
 		getElementForSide(sideDocument, differenceElement, tempRootElement, side);		
 		return (Element) tempRootElement.getFirstChild();
 	}
 	
 	public Element getElementForSide(Document sideDocument,Element differenceElement, Side side) throws Exception
 	{
 		//XPath.dumpNode(differenceElement, System.out);
 		
 		
 		Element tempRootElement = sideDocument.getDocumentElement();
 		
 		
 		getElementForSide(sideDocument, differenceElement, tempRootElement, side);		
 		return (Element) tempRootElement;
 	}
 	
 	/**
 	 * The only thing that should be passed to this method are difference elements that belong to the original document/side
 	 * @param sideDocument
 	 * @param differenceElement
 	 * @param parentSideElement
 	 * @param side
 	 * @return
 	 * @throws Exception 
 	 */
 	private Element getElementForSide(Document sideDocument, Element differenceElement, Element parentSideElement, Side side) throws Exception
 	{
 		//check to see if this is already good to go
 		if (differenceElement.getAttributeNS(XDIFF_NAMESPACE_URI, XDIFF_ELEMENT_ATTRIBUTE_NAME).equals(Side.BASE + " = " +Side.MOD))
		{
			differenceElement.removeAttribute(XDIFF_PREFIX+":"+XDIFF_ELEMENT_ATTRIBUTE_NAME);
 			parentSideElement.appendChild(sideDocument.adoptNode(differenceElement));
 		}
 		//see if this element is declared only for this side,
 		else if (differenceElement.getAttributeNS(XDIFF_NAMESPACE_URI, XDIFF_ELEMENT_ATTRIBUTE_NAME).equals(side.toString()))
 		{
 			differenceElement.removeAttribute(XDIFF_PREFIX+":"+XDIFF_ELEMENT_ATTRIBUTE_NAME);			
 			parentSideElement.appendChild(sideDocument.adoptNode(differenceElement));
 		}		
 		else if (differenceElement.getAttributeNS(XDIFF_NAMESPACE_URI, XDIFF_ELEMENT_ATTRIBUTE_NAME).equals(Side.BASE + " != " +Side.MOD))
 		{
 			Element sideElement = createElement(sideDocument, differenceElement.getNamespaceURI(), differenceElement.getPrefix(), differenceElement.getLocalName());
 			parentSideElement.appendChild(sideElement);
 			//process this elements actual attributes
 			NamedNodeMap attributeNamedNodeMap = differenceElement.getAttributes();
 			//System.out.println(attributeNamedNodeMap.getLength());
 			for(int index = 0; index < attributeNamedNodeMap.getLength(); index++)
 			{
 				Attr attribute = (Attr) attributeNamedNodeMap.item(index);
 				if (areSame(attribute.getNamespaceURI(), XDIFF_NAMESPACE_URI) && areSame(attribute.getPrefix(), XDIFF_PREFIX))
 				{
 					//this is one of our attributes, so don't do anything
 				}
 				else
 				{						
 					setAttribute(sideElement, attribute.getNamespaceURI(), attribute.getPrefix(), attribute.getLocalName(), attribute.getValue());	
 				}
 
 			}
 			
 			NodeList nodeList = differenceElement.getChildNodes();
 			int currentLength = nodeList.getLength();
 			for(int index = 0; index < nodeList.getLength(); index++)
 			{
 				Node node = nodeList.item(index);
 				short nodeType = node.getNodeType();
 				if (nodeType == Node.ELEMENT_NODE)
 				{
 					
 					Element element = (Element) node;
 					//check to see if this is one of ours
 					if (areSame(element.getNamespaceURI(), XDIFF_NAMESPACE_URI) && areSame(element.getPrefix(), XDIFF_PREFIX))
 					{
 						if (element.getLocalName().equals(XDIFF_ATTRIBUTE_ELEMENT_NAME))
 						{
 							//check if attribute belongs to a single side
 							if (element.hasAttribute(XDIFF_PREFIX+":"+XDIFF_ATTRIBUTE_ATTRIBUTE_NAME))
 							{
 								//see if this belong to our side
 								if (element.getAttribute(XDIFF_PREFIX+":"+XDIFF_ATTRIBUTE_ATTRIBUTE_NAME).equals(side.toString()))
 								{
 									setAttribute(sideElement, null, null, element.getAttribute(XDIFF_PREFIX+":"+XDIFF_NAME_ATTRIBUTE_NAME), element.getAttribute(XDIFF_PREFIX+":"+XDIFF_VALUE_ATTRIBUTE_NAME));
 								}
 								else
 								{
 									//ignore it, since it only belongs to the other side
 								}
 							}
 							else //attribute belongs to both sides
 							{
 								//check for prefix
 								String sideAttrName = element.getAttribute(XDIFF_PREFIX+":"+XDIFF_NAME_ATTRIBUTE_NAME);
 								String[] attributeNameSplit = sideAttrName.split(":");
 								if (attributeNameSplit.length == 1) //no namespace, so just use the declaration as is.
 								{
 									setAttribute(sideElement, null, null, sideAttrName, element.getAttribute(XDIFF_PREFIX+":"+side.toString()));
 								}
 								else //has prefix, figure out what it is
 								{								
 									setAttribute(sideElement, differenceElement.lookupNamespaceURI(attributeNameSplit[0]), attributeNameSplit[0], attributeNameSplit[1], element.getAttribute(XDIFF_PREFIX+":"+side.toString()));
 								}
 							}
 						}
 						else if (element.getLocalName().equals(XDIFF_TEXT_ELEMENT_NAME))
 						{
 							//check to see if this is a complete text element
 							if (element.hasAttribute(XDIFF_PREFIX+":"+XDIFF_TEXT_ATTRIBUTE_NAME))
 							{
 								//see if this is one we care about
 								if (element.getAttribute(XDIFF_PREFIX+":"+XDIFF_TEXT_ATTRIBUTE_NAME).equals(side.toString()))
 								{
 									sideElement.appendChild(sideElement.getOwnerDocument().createTextNode(element.getTextContent()));
 								}								
 								else
 								{
 									//skip it
 								}
 							}
 							//this is an xdiff:text/xdiff:xdiff element
 							else
 							{
 								
 								XMLTextDiff xmlTextDiff = new XMLTextDiff();
 								Text text = xmlTextDiff.getTextForSide(element, side);								
 								sideElement.appendChild(sideElement.getOwnerDocument().adoptNode(text));
 							}
 						}
 					}
 					else //not an xdiff element
 					{
 						getElementForSide(sideDocument, element, sideElement, side);
 						if (currentLength > nodeList.getLength())
 						{
 							currentLength = nodeList.getLength();
 							index--;
 						}
 					}
 				}
 				else if (nodeType == Node.TEXT_NODE)
 				{
 					sideElement.appendChild(sideElement.getOwnerDocument().adoptNode(node));
 					currentLength = nodeList.getLength();
 					index--;
 				}
 			}
 			//process this side's modified attributes
 			
 			//process this side's elements
 
 		}
 		return parentSideElement;
 	}
 	
 	
 	@SuppressWarnings({ "unchecked", "rawtypes" })
 	private void setAttribute(Element element, String namespaceURI, String prefix, Enum name, Object value)
 	{
 		setAttribute(element, namespaceURI, prefix, name.toString(), value);
 	}
 	
 	private void setAttribute(Element element, String namespaceURI, String prefix, String name, Object value)
 	{
 		if (prefix == null || prefix.isEmpty())
 		{
 			element.setAttributeNS(namespaceURI, name, value.toString());
 		}
 		else
 		{
 		    try
 		    {
 			element.setAttributeNS(namespaceURI, prefix+":"+name, value.toString());
 		    } catch (DOMException domException)
 		    {
 		        domException.printStackTrace();
 		    }
 		}
 	}
 	
 	private Element createElement(Document forDocument, String namespaceURI, String prefix, String name)
 	{
 		Element createdElement = null;
 		if (prefix == null || prefix.isEmpty())
 		{
 			createdElement = forDocument.createElementNS(namespaceURI, name);
 		}
 		else
 		{
 			createdElement = forDocument.createElementNS(namespaceURI, prefix+":"+name);
 		}
 		return createdElement;
 	}
 
 	private boolean areSame(Object object1, Object object2) {
 		if (object1 == null ^ object2 == null){
 			return false;
 		}
 		else if (object1 == null & object2 == null){
 			return true;
 		}
 		else return object1.equals(object2);
 	}
 
 	/**
 	 * This method should only be used where storage. It removes all data from the xdiff file that can be found in the original file. 
 	 * For example if an element is found to have no differences, it's content will be removed. Using the repopulate method and the original element, will repopulate the content.
 	 * The purpose of this method is to allow efficient storage of a series of changes to a document.
 	 * @param differenceElement
 	 * @param side
 	 */
 	public void trimDifferenceToSide(Element differenceElement, Side side)
 	{
 		
 		//process non-xdiff element
 		String elementAttributeValue = differenceElement.getAttributeNS(XDIFF_NAMESPACE_URI, XDIFF_ELEMENT_ATTRIBUTE_NAME);
 		//if we don't have one, then we need to see if we are dealing with an xdiff element 
 		if (elementAttributeValue == null || elementAttributeValue.isEmpty())
 		{
 			if (differenceElement.getNamespaceURI().equals(XDIFF_NAMESPACE_URI))
 			{
 				//we are dealing with an xdiff attribute element, which means we need to clear out any oppsiteSide values
 				if (differenceElement.getLocalName().equals(XDIFF_ATTRIBUTE_ELEMENT_NAME))
 				{
 					if (differenceElement.getAttributeNS(XDIFF_NAMESPACE_URI, XDIFF_ATTRIBUTE_ATTRIBUTE_NAME).equals(side.getOppositeSide().toString()))
 					{
 						differenceElement.removeAttributeNS(XDIFF_NAMESPACE_URI, XDIFF_VALUE_ATTRIBUTE_NAME);
 					}
 				}
 				//diff element compress content and rewrite start and stop attributes
 				else if (differenceElement.getLocalName().equals(XDIFF_DIFF_ELEMENT_NAME))
 				{
 					if (differenceElement.getAttributeNS(XDIFF_NAMESPACE_URI, XDIFF_DIFF_ATTRIBUTE_NAME).equals(side.getOppositeSide().toString()))
 					{						
 						//remove content
 						XPath.removeContent(differenceElement);						
 					}
 					//process position attributes					
 					String positionAttributeValue = differenceElement.getAttributeNS(XDIFF_NAMESPACE_URI, XDIFF_DIFF_ATTRIBUTE_NAME).charAt(0)+","+differenceElement.getAttributeNS(XDIFF_NAMESPACE_URI, "baseStart")+","+differenceElement.getAttributeNS(XDIFF_NAMESPACE_URI, "modStart")+"-"+differenceElement.getAttributeNS(XDIFF_NAMESPACE_URI, "baseStop")+","+differenceElement.getAttributeNS(XDIFF_NAMESPACE_URI, "modStop");
 					XPath.removeAttributes(differenceElement);
 					differenceElement.setAttributeNS(XDIFF_NAMESPACE_URI, XDIFF_PREFIX+":pos", positionAttributeValue);					
 				}
 				//process xdiff:text element
 				else if (differenceElement.getLocalName().equals(XDIFF_TEXT_ELEMENT_NAME))
 				{
 					if (differenceElement.hasAttributeNS(XDIFF_NAMESPACE_URI, XDIFF_TEXT_ATTRIBUTE_NAME))
 					{
 						//if there is a xdiff:text attribute equal to the opposite side, remove the content, since we don't need it.
 						if (differenceElement.getAttributeNS(XDIFF_NAMESPACE_URI, XDIFF_TEXT_ATTRIBUTE_NAME).equals(side.getOppositeSide().toString()))
 						{
 							XPath.removeContent(differenceElement);
 						}
 					}
 					//this is going to have xdiff:diff  children so we need to dig deeper 
 					else
 					{
 						NodeList nodeList = differenceElement.getChildNodes();
 						for(int currentNode = 0;currentNode < nodeList.getLength();currentNode++)
 						{
 							if (nodeList.item(currentNode) instanceof Element)
 							{
 								if (((Element) nodeList.item(currentNode)).getAttributeNS(XDIFF_NAMESPACE_URI, XDIFF_DIFF_ATTRIBUTE_NAME).equals(EQUALITY))
 								{
 									differenceElement.removeChild(nodeList.item(currentNode));
 									currentNode--;
 								}
 								else
 								{
 									trimDifferenceToSide((Element) nodeList.item(currentNode), side);
 								}
 							}
 							
 						}
 					}
 				}
 			}
 		}
 		else if (elementAttributeValue.equals(side.toString()))
 		{
 			//we need to keep this information, so move on
 			return;
 		}
 		else if (elementAttributeValue.equals(side.getOppositeSide().toString()))
 		{
 			//remove content, since this data for the other side, and will not be in the document for this side
 			XPath.removeContent(differenceElement);
 		}		
 		else if (elementAttributeValue.equals(EQUALITY))
 		{
 			//remove content, since we can get it from the original document			
 			XPath.removeContent(differenceElement);
 			//remove any attributes
 			XPath.removeAttributes(differenceElement);
 			//put original diff attribute back, since we need it to un-trim
 			differenceElement.setAttributeNS(XDIFF_NAMESPACE_URI, XDIFF_PREFIX+":"+XDIFF_ELEMENT_ATTRIBUTE_NAME,EQUALITY);
 		}
 		//INEQUALITY
 		else 
 		{
 		
 			NamedNodeMap namedNodeMap = differenceElement.getAttributes();
 			for(int index = 0; index < namedNodeMap.getLength(); index++)
 			{
 				Attr attribute = (Attr) namedNodeMap.item(index);
 				//skip anything that is one of our or a namespace declaration
 				if (XDIFF_NAMESPACE_URI.equals(attribute.getNamespaceURI()) == false && "http://www.w3.org/2000/xmlns/".equals(attribute.getNamespaceURI()) == false)
 				{
 					
 					//we are trying to compress thing a little bit, so unless an attribute value is longer than the element needed to compress it, skip it.
 					if(attribute.getNodeValue().length() > 70)
 					{						
 						Element attributeElement = differenceElement.getOwnerDocument().createElementNS(XDIFF_NAMESPACE_URI, XDIFF_PREFIX+":"+XDIFF_ATTRIBUTE_ELEMENT_NAME);
 						attributeElement.setAttributeNS(XDIFF_NAMESPACE_URI, XDIFF_PREFIX+":"+XDIFF_ATTRIBUTE_ATTRIBUTE_NAME, EQUALITY);
 						attributeElement.setAttributeNS(XDIFF_NAMESPACE_URI, XDIFF_PREFIX+":"+XDIFF_NAME_ATTRIBUTE_NAME, attribute.getName());
 						differenceElement.insertBefore(attributeElement, differenceElement.getFirstChild());
 						differenceElement.removeAttributeNode(attribute);						
 						index--;
 					}
 				}
 			}
 			//requires further processing			
 			NodeList nodeList = differenceElement.getChildNodes();
 			for(int currentNode = 0;currentNode < nodeList.getLength();currentNode++)
 			{
 				if (nodeList.item(currentNode) instanceof Element)
 				{
 					trimDifferenceToSide((Element) nodeList.item(currentNode), side);
 				}
 				
 			}
 		}
 		
 		
 		//walk tree
 		//if (element.side != side)
 		//	remove content
 		//if side = BOTH
 		//	remove element
 		
 	}
 	/**
 	 * repopulate
 	 * These must start at the same point
 	 * @param originalElement
 	 * @param differenceElement
 	 * @param side
 	 * @throws Exception 
 	 * @throws DOMException 
 	 */
 	public void repopulate(Element originalElement, Element differenceElement, Side side) throws DOMException, Exception
 	{
 		
 		//this should always work in pairs, so if we don't have a matching original element, just return
 		if (originalElement == null)
 		{
 			return;
 		}
 		
 		NodeList differenceElementChildNodeList = differenceElement.getChildNodes();
 		
 		int differenceNodeIndex = 0;
 		
 		/*
 		 * Walk through the list of child nodes, and for everyone that isn't xdiff, and insn't our side only, pull a child off of the opposite side
 		 */
 		while (differenceNodeIndex < differenceElementChildNodeList.getLength())
 		{
 			Node differenceElementChildNode = differenceElementChildNodeList.item(differenceNodeIndex);
 			differenceNodeIndex++;
 			
 			//we only care about elements
 			if ( differenceElementChildNode instanceof Element)
 			{
 				Element differenceElementChildElement = (Element) differenceElementChildNode;
 				//System.out.println(differenceNode.getNodeName());
 				
 				//see if we are dealing with a non-xdiff element
 				if (differenceElementChildElement.getNamespaceURI() == null || differenceElementChildElement.getNamespaceURI().equals(XDIFF_NAMESPACE_URI) == false)				
 				{
 					//first check if we are dealing with an element of this side only, if so skip everything, and move on, as it should all be here already
 					if (differenceElementChildElement.getAttributeNS(XDIFF_NAMESPACE_URI, XDIFF_ELEMENT_ATTRIBUTE_NAME).equals(side.toString()) == false)
 					{
 						//cleanup unneeded spaces
 						while(originalElement.getFirstChild() instanceof Text)
 						{
 							originalElement.removeChild(originalElement.getFirstChild());
 						}
 
 						Element childOriginalElement = (Element) originalElement.getFirstChild();
 						if (childOriginalElement != null)
 						{
 							
 							originalElement.removeChild(childOriginalElement);
 						}
 
 						if (differenceElementChildElement.getAttributeNS(XDIFF_NAMESPACE_URI, XDIFF_ELEMENT_ATTRIBUTE_NAME).equals(INEQUALITY))
 						{
 							repopulate(childOriginalElement, differenceElementChildElement, side);
 						}
 						else if (differenceElementChildElement.getAttributeNS(XDIFF_NAMESPACE_URI, XDIFF_ELEMENT_ATTRIBUTE_NAME).equals(EQUALITY))
 						{
 							//get content from original and append into difference						
 							childOriginalElement = (Element) differenceElementChildElement.getOwnerDocument().adoptNode(childOriginalElement);
 							differenceElement.replaceChild(childOriginalElement, differenceElementChildElement);
 							childOriginalElement.setAttributeNS(XDIFF_NAMESPACE_URI, XDIFF_PREFIX+":"+XDIFF_ELEMENT_ATTRIBUTE_NAME, EQUALITY);
 						}
 					}
 				}
 				//is this an xdiff:attribute element
 				else if (differenceElementChildElement.getLocalName().equals(XDIFF_ATTRIBUTE_ELEMENT_NAME))
 				{
 					//System.out.println(childDifferenceElement);
 					//at this point in time, if the attr was in both, then we didn't remove it, so no need to add it back in.
 					if ((EQUALITY).equals(differenceElementChildElement.getAttributeNS(XDIFF_NAMESPACE_URI, XDIFF_ATTRIBUTE_ATTRIBUTE_NAME)))
 					{						
 						String attributeName = differenceElementChildElement.getAttributeNS(XDIFF_NAMESPACE_URI, XDIFF_NAME_ATTRIBUTE_NAME);
 						differenceElement.setAttributeNode((Attr) differenceElement.getOwnerDocument().adoptNode(originalElement.getAttributeNode(attributeName)));
 						differenceElement.removeChild(differenceElementChildElement);
 					}
 				}
 				//is this some sort of diffed text
 				else if (differenceElementChildElement.hasAttributeNS(XDIFF_NAMESPACE_URI, XDIFF_TOKENLIST_ATTRIBUTE_NAME))
 				{
 					repopulateTextData(differenceElementChildElement, originalElement, side);
 				}
 				else
 				{
 					//System.out.println("skipping-->"+differenceElementChildElement);
 				}
 			}
 
 			else
 			{
 				//System.out.println("--->"+differenceElementChildNode.toString());
 			}
 		}
 	}
 	
 	
 	private void repopulateTextData(Element differenceElementChildElement,Element originalElement,Side side) throws DOMException, Exception
 	{
 		//walk through all of the children
 		//rebuild start and stop attributes from position attribute
 		NodeList childNodeList = differenceElementChildElement.getChildNodes();
 		
 		TokenList tokenList = TokenList.valueOf(differenceElementChildElement.getAttributeNS(XDIFF_NAMESPACE_URI, XDIFF_TOKENLIST_ATTRIBUTE_NAME));
 		InputStreamTokenizer inputStreamTokenizer = null;
 		if (tokenList == TokenList.CUSTOM)
 		{
 			XMLSerializer xmlSerializer = new XMLSerializer();			
 			xmlSerializer.marshall(differenceElementChildElement, tokenList);			
 			inputStreamTokenizer = new InputStreamTokenizer(originalElement.getTextContent().getBytes(), tokenList.getTokenLists());
 		}
 		else
 		{
 			 inputStreamTokenizer = new InputStreamTokenizer(originalElement.getTextContent().getBytes(), tokenList);	
 		}
 		
 		int currentTokenPosition = 1; //for readability, our diff starts at one, so we should too.
 		for (int index = 0; index < childNodeList.getLength(); index++)
 		{
 			Element diffElement = (Element) childNodeList.item(index);
 			
 			//skip over any elements that don't belong here
 			if (diffElement.getLocalName().equals(XDIFF_DIFF_ELEMENT_NAME) == false)
 			{
 				continue;
 			}
 			
 			String[] position = diffElement.getAttributeNS(XDIFF_NAMESPACE_URI, XDIFF_POSITION_ATTRIBUTE_NAME).split("[,-]");
 			diffElement.setAttributeNS(XDIFF_NAMESPACE_URI, XDIFF_PREFIX+":baseStart",position[1]);
 			diffElement.setAttributeNS(XDIFF_NAMESPACE_URI, XDIFF_PREFIX+":baseStop",position[3]);
 			diffElement.setAttributeNS(XDIFF_NAMESPACE_URI, XDIFF_PREFIX+":modStart",position[2]);
 			diffElement.setAttributeNS(XDIFF_NAMESPACE_URI, XDIFF_PREFIX+":modStop",position[4]);
 			if (position[0].equals("M"))
 			{
 				diffElement.setAttributeNS(XDIFF_NAMESPACE_URI, XDIFF_PREFIX+":"+XDIFF_DIFF_ATTRIBUTE_NAME,Side.MOD.toString());
 				//create new base = mod element
 				int start = 0;
 				int stop = 0;
 				Element equalsElement = diffElement.getOwnerDocument().createElementNS(XDIFF_NAMESPACE_URI,XDIFF_PREFIX+":"+XDIFF_DIFF_ELEMENT_NAME);
 				equalsElement.setAttributeNS(XDIFF_NAMESPACE_URI, XDIFF_PREFIX+":"+XDIFF_DIFF_ATTRIBUTE_NAME,EQUALITY);
 
 				int baseStart = Integer.parseInt(position[3]);
 				int modStart = Integer.parseInt(position[4])+1;
 				
 				equalsElement.setAttributeNS(XDIFF_NAMESPACE_URI, XDIFF_PREFIX+":baseStart",baseStart+"");							
 				equalsElement.setAttributeNS(XDIFF_NAMESPACE_URI, XDIFF_PREFIX+":modStart",modStart+"");
 				
 				if (diffElement.getNextSibling() != null)
 				{
 					String[] nextPosition = ((Element) diffElement.getNextSibling()).getAttributeNS(XDIFF_NAMESPACE_URI, XDIFF_POSITION_ATTRIBUTE_NAME).split("[,-]");
 					int baseStop = Integer.parseInt(nextPosition[1])-1;
 					int modStop = Integer.parseInt(nextPosition[2])-1;
 					
 					equalsElement.setAttributeNS(XDIFF_NAMESPACE_URI, XDIFF_PREFIX+":baseStop",baseStop+"");
 					equalsElement.setAttributeNS(XDIFF_NAMESPACE_URI, XDIFF_PREFIX+":modStop",modStop+"");
 					
 					//set the values for reading from the token stream
 					if (side == Side.MOD)
 					{
 						start = baseStart;
 						stop = baseStop;									
 					}
 					else
 					{
 						start = modStart;
 						stop = modStop;
 					}
 					
 					
 				}
 				else
 				{
 					//XXX not really sure if this is the right thing to do. Will there ever be a situation where we will end with anything other than a MOD?
 					continue;
 				}
 				//now go get the text content from the original
 				StringBuilder stringBuilder = new StringBuilder();
 				for(;currentTokenPosition <= stop; currentTokenPosition++)
 				{
 					String data = new String(inputStreamTokenizer.readBytes());
 					//System.out.print(currentTokenPosition+"\t"+data);
 					
 					if (currentTokenPosition >= start)
 					{									
 						stringBuilder.append(data);
 					}
 				}
 				equalsElement.setTextContent(stringBuilder.toString());
 				differenceElementChildElement.insertBefore(equalsElement, diffElement.getNextSibling());
 				index++;
 			}
 			else
 			{
 				diffElement.setAttributeNS(XDIFF_NAMESPACE_URI, XDIFF_PREFIX+":"+XDIFF_DIFF_ATTRIBUTE_NAME,Side.BASE.toString());
 			}
 			diffElement.removeAttributeNS(XDIFF_NAMESPACE_URI, XDIFF_POSITION_ATTRIBUTE_NAME);
 		}
 	}
 
 	public void addIgnoreableAttribute(String namespaceURI, String localName)
 	{
 		ignoreableAttributeMap.put(namespaceURI+":"+localName, null);		
 	}
 }
