 package edu.umich.lsa.cscs.gridsweeper;
 
 import java.math.BigDecimal;
 import java.util.*;
 
 import org.xml.sax.Attributes;
 import org.xml.sax.SAXException;
 import org.xml.sax.helpers.DefaultHandler;
 
 import edu.umich.lsa.cscs.gridsweeper.parameters.*;
 
 import static edu.umich.lsa.cscs.gridsweeper.Logger.*;
 
 /**
  * The SAX-based XML parser for experiment XML (.gsweep) files.
  * TODO: Write a complete specification for the file format. For now, see the example files. 
  * @author Ed Baskerville
  *
  */
 public class ExperimentXMLHandler extends DefaultHandler
 {
 	private List<Object> stack;
 	private Experiment experiment;
 
 	/**
 	 * An enum representing the supported XML tags.
 	 * @author Ed Baskerville
 	 *
 	 */
 	private enum Tag
 	{
 		SETTING,
 		ABBREV,
 		ITEM,
 		INPUT,
 		OUTPUT
 	}
 	
 	/**
 	 * Default constructor.
 	 * @param experiment The experiment to write values into.
 	 */
 	public ExperimentXMLHandler(Experiment experiment)
 	{
 		this.experiment = experiment;
 		stack = new ArrayList<Object>();
 	}
 
 	/**
 	 * @see org.xml.sax.helpers.DefaultHandler#startDocument()
 	 */
 	@Override
 	public void startDocument()
 	{
 		fine("beginning parsing");
 	}
 	
 	/**
 	 * @see org.xml.sax.helpers.DefaultHandler#endDocument()
 	 */
 	@Override
 	public void endDocument() throws SAXException
 	{
 		fine("ending parsing");
 		if(peek() != null)
 		{
 			SAXException exception = new SAXException("Encountered end of document before closing experiment tag.");
 			throwing(getClass().toString(), "endDocument", exception); 
 			throw exception;
 		}
 	}
 
 	/**
 	 * Parses the start tag of an XML element. Performs error checking
 	 * and updates the parse stack and the Experiment object as appropriate.
 	 * @see org.xml.sax.helpers.DefaultHandler#startElement(java.lang.String, java.lang.String, java.lang.String, org.xml.sax.Attributes)
 	 */
 	@Override
 	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException
 	{
 		finer("startElement: " + qName);
 		
 		StringMap attrMap = getMapFromAttributes(attributes);
 		Object top = peek();
 		
 		try
 		{
 			if(qName.equals("experiment"))
 			{
 				if(top != null)
 					throw new SAXException("experiment tag with non-empty stack");
 				
 				experiment.setName(attrMap.get("name"));
 				
 				String numRunsStr = attrMap.get("numRuns");
 				if(numRunsStr != null)
 				{
 					experiment.setNumRuns(Integer.parseInt(numRunsStr));
 				}
 				
 				String rngSeedStr = attrMap.get("rngSeed");
 				if(rngSeedStr != null)
 				{
 					experiment.setRngSeed(Long.parseLong(rngSeedStr));
 				}
 				
 				push(experiment);
 			}
 			else if(qName.equals("input"))
 			{
 				if(top != experiment)
 					throw new SAXException("input tag with non-experiment on top of stack");
 				
 				String source = attrMap.get("source");
 				String destination = attrMap.get("destination");
 				
 				if(source == null)
 					throw new SAXException("source attribute missing from input tag");
 				if(destination == null)
 					throw new SAXException("destination attribute missing from input tag");
 				
 				experiment.getInputFiles().put(source, destination);
 				
 				push(Tag.INPUT);
 			}
 			else if(qName.equals("output"))
 			{
 				if(top != experiment)
 					throw new SAXException("output tag with non-experiment on top of stack");
 				
 				String path = attrMap.get("path");
 				
 				if(path == null)
 					throw new SAXException("path attribute missing from output tag");
 				
 				experiment.getOutputFiles().add(path);
 				
 				push(Tag.OUTPUT);
 			}
 			else if(qName.equals("setting"))
 			{
 				if(top != experiment)
 					throw new SAXException("setting tag with non-experiment on top of stack");
 				
 				String key = attrMap.get("key");
 				String value = attrMap.get("value");
 				
 				if(key == null)
 					throw new SAXException("key attribute missing from setting tag");
 				if(value == null)
 					throw new SAXException("value attribute missing from setting tag");
 				
 				experiment.getSettings().put(key, value);
 				
 				push(Tag.SETTING);
 			}
 			else if(qName.equals("abbrev"))
 			{
 				if(top != experiment)
 					throw new SAXException("abbrev tag with non-experiment on top of stack");
 				
 				String param = attrMap.get("param");
 				String abbrev = attrMap.get("abbrev");
 				
 				if(param == null)
 					throw new SAXException("param attribute missing from abbrev tag");
 				if(abbrev == null)
 					throw new SAXException("abbrev attribute missing from abbrev tag");
 				
 				experiment.getAbbreviations().put(param, abbrev);
 				push(Tag.ABBREV);
 			}
 			else if(qName.equals("value"))
 			{
 				startSweepElement(qName, attrMap);
 			}
 			else if(qName.equals("list"))
 			{
 				startSweepElement(qName, attrMap);
 			}
 			else if(qName.equals("item"))
 			{
 				if(!(top instanceof ListSweep))
 					throw new SAXException("item tag with non-list on top of stack");
 				ListSweep listSweep = (ListSweep)top;
 				
 				String value = attrMap.get("value");
 				
 				if(value == null)
 					throw new SAXException("value attribute missing from item tag");
 				
 				listSweep.add(value);
 				
 				push(Tag.ITEM);
 			}
 			else if(qName.equals("range"))
 			{
 				startSweepElement(qName, attrMap);
 			}
 			else if(qName.equals("uniform"))
 			{
 				startSweepElement(qName, attrMap);
 			}
 			else if(qName.equals("multiplicative"))
 			{
 				startSweepElement(qName, attrMap);
 			}
 			else if(qName.equals("linear"))
 			{
 				startSweepElement(qName, attrMap);
 			}
 			else
 			{
 				throw new SAXException("unknown tag " + qName);
 			}
 		}
 		catch(SAXException e)
 		{
 			throwing(getClass().toString(), "startElement", e);
 			throw e;
 		}
 	}
 
 	/**
 	 * Parses the end tag of an XML element. Performs error checking and updates
 	 * the state of the stack and Experiment object as appropriate.
 	 * @see org.xml.sax.helpers.DefaultHandler#endElement(java.lang.String, java.lang.String, java.lang.String)
 	 */
 	@Override
 	public void endElement(String uri, String localName, String qName) throws SAXException
 	{
 		finer("endElement: " + qName);
 		Object top = peek();
 		
 		try
 		{
 			if(top == null)
 				throw new SAXException("end tag found with empty stack");
 			
 			if(qName.equals("experiment"))
 			{
 				if(top != experiment)
 					throw new SAXException("mismatched experiment end tag");
 			}
 			else if(qName.equals("input"))
 			{
 				if(top != Tag.INPUT)
 					throw new SAXException("mismatched input end tag");
 			}
 			else if(qName.equals("output"))
 			{
 				if(top != Tag.OUTPUT)
 					throw new SAXException("mismatched input end tag");
 			}
			else if(qName.equals("property"))
 			{
 				if(top != Tag.SETTING)
					throw new SAXException("mismatched property end tag");
 			}
 			else if(qName.equals("abbrev"))
 			{
 				if(top != Tag.ABBREV)
 					throw new SAXException("mismatched abbrev end tag");
 			}
 			else if(qName.equals("value"))
 			{
 				if(!(top instanceof SingleValueSweep))
 					throw new SAXException("mismatched value end tag");
 			}
 			else if(qName.equals("list"))
 			{
 				if(!(top instanceof ListSweep))
 					throw new SAXException("mismatched list end tag");
 			}
 			else if(qName.equals("item"))
 			{
 				if(top != Tag.ITEM)
 					throw new SAXException("mismatched item end tag");
 			}
 			else if(qName.equals("range"))
 			{
 				if(!(top instanceof RangeListSweep))
 					throw new SAXException("mismatched range end tag");
 			}
 			else if(qName.equals("uniform"))
 			{
 				if(!(top instanceof UniformDoubleSweep))
 					throw new SAXException("mismatched uniform end tag");
 			}
 			else if(qName.equals("multiplicative"))
 			{
 				if(!(top instanceof MultiplicativeCombinationSweep))
 					throw new SAXException("mismatched multiplicative end tag");
 			}
 			else if(qName.equals("linear"))
 			{
 				if(!(top instanceof LinearCombinationSweep))
 					throw new SAXException("mismatched linear end tag");
 			}
 			else
 			{
 				throw new SAXException("unknown end tag " + qName);
 			}
 			
 			pop();
 		}
 		catch(SAXException e)
 		{
 			throwing(getClass().toString(), "endElement", e);
 			throw e;
 		}
 	}
     
 	/**
 	 * Converts an XML attributes object to a simple string map.
 	 * @param attributes The XML attributes object.
 	 * @return A string map representing the attributes.
 	 */
     private StringMap getMapFromAttributes(Attributes attributes)
     {
         StringMap expressionMap = new StringMap();
         int length = attributes.getLength();
         
         for(int i = 0; i < length; i++)
         {
             expressionMap.put(attributes.getQName(i), attributes.getValue(i));
         }
         
         return expressionMap;
     }
     
     /**
      * Peeks at the top of the parse stack without removing the top object.
      * @return The top object on the stack, or {@code null} if the stack is empty.
      */
     private Object peek()
     {
     	if(stack.size() > 0) return stack.get(stack.size() - 1);
     	return null;
     }
 
     /**
      * Pushes an object to the top of the parse stack.
      * @param obj The object to push.
      */
 	private void push(Object obj)
 	{
 		stack.add(obj);
 	}
 	
 	/**
 	 * Pops the top object off of the stack.
 	 */
 	private void pop()
 	{
 		if(stack.size() > 0) stack.remove(stack.size() - 1);
 	}
 	
 	/**
 	 * Parses a sweep element, passing the parent on the appropriate method for the
 	 * appropriate type of sweep.
 	 * @param qName The tag name.
 	 * @param attrMap The attributes of the sweep.
 	 * @throws SAXException
 	 */
 	private void startSweepElement(String qName, StringMap attrMap) throws SAXException
 	{
 		Object top = peek();
 		
 		CombinationSweep parent;
 		if(top == experiment)
 		{
 			parent = experiment.getRootSweep();
 		}
 		else if(top instanceof CombinationSweep)
 		{
 			parent = (CombinationSweep)top;
 		}
 		else
 			throw new SAXException("value tag with non-sweep on top of stack");
 		
 		if(qName.equals("value"))
 		{
 			startValueElement(parent, attrMap);
 		}
 		else if(qName.equals("list"))
 		{
 			startListElement(parent, attrMap);
 		}
 		else if(qName.equals("range"))
 		{
 			startRangeElement(parent, attrMap);
 		}
 		else if(qName.equals("uniform"))
 		{
 			startUniformElement(parent, attrMap);
 		}
 		else if(qName.equals("multiplicative"))
 		{
 			startMultiplicativeElement(parent, attrMap);
 		}
 		else if(qName.equals("linear"))
 		{
 			startLinearElement(parent, attrMap);
 		}
 	}
 	
 	private void startValueElement(CombinationSweep parent, StringMap attrMap) throws SAXException
 	{
 		String param = attrMap.get("param");
 		String value = attrMap.get("value");
 		
 		if(param == null)
 			throw new SAXException("param attribute missing from value tag");
 		if(value == null)
 			throw new SAXException("value attribute missing from value tag");
 		
 		SingleValueSweep sweep = new SingleValueSweep(param, value); 
 		parent.add(sweep);
 		push(sweep);
 	}
 	
 	/**
 	 * Parses the start tag of a list sweep.
 	 * @param parent The sweep's parent.
 	 * @param attrMap The sweep's attributes.
 	 * @throws SAXException If the parameter name attribute is missing.
 	 */
 	private void startListElement(CombinationSweep parent, StringMap attrMap) throws SAXException
 	{
 		String param = attrMap.get("param");
 		
 		if(param == null)
 			throw new SAXException("param attribute missing from list tag");
 		
 		ListSweep listSweep = new ListSweep(param);
 		parent.add(listSweep);
 		
 		push(listSweep);		
 	}
 	
 	/**
 	 * Parses the start tag of a range sweep.
 	 * @param parent The sweep's parent.
 	 * @param attrMap The sweep's attributes.
 	 * @throws SAXException If any required attributes are missing.
 	 */
 	private void startRangeElement(CombinationSweep parent, StringMap attrMap) throws SAXException
 	{
 		String param = attrMap.get("param");
 		String start = attrMap.get("start");
 		String end = attrMap.get("end");
 		String increment = attrMap.get("increment");
 		
 		if(param == null)
 			throw new SAXException("param attribute missing from range tag");
 		if(start== null)
 			throw new SAXException("start attribute missing from range tag");
 		if(end == null)
 			throw new SAXException("end attribute missing from range tag");
 		if(increment == null)
 			throw new SAXException("increment attribute missing from range tag");
 		
 		try
 		{
 			RangeListSweep sweep = new RangeListSweep(param, new BigDecimal(start),
 					new BigDecimal(end), new BigDecimal(increment)); 
 			parent.add(sweep);
 			push(sweep);
 		}
 		catch(NumberFormatException e)
 		{
 			throw new SAXException("badly formatted number in range tag");
 		}
 	}
 	
 	/**
 	 * Parses the start tag of a uniform distribution sweep.
 	 * @param parent The sweep's parent.
 	 * @param attrMap The sweep's attributes.
 	 * @throws SAXException If any of the required attributes is missing.
 	 */
 	private void startUniformElement(CombinationSweep parent, StringMap attrMap) throws SAXException
 	{
 		String param = attrMap.get("param");
 		String type = attrMap.get("type");
 		String start = attrMap.get("start");
 		String end = attrMap.get("end");
 		String count = attrMap.get("count");
 		
 		if(param == null)
 			throw new SAXException("param attribute missing from uniform tag");
 		if(type == null)
 			throw new SAXException("type attribute missing from uniform tag");
 		if(start == null)
 			throw new SAXException("start attribute missing from uniform tag");
 		if(end == null)
 			throw new SAXException("end attribute missing from uniform tag");
 		if(count == null)
 			throw new SAXException("count attribute missing from uniform tag");
 		
 		try
 		{
 			if(type.equals("double"))
 			{
 				UniformDoubleSweep sweep =new UniformDoubleSweep(param,
 						Double.parseDouble(start), Double.parseDouble(end), Integer.parseInt(count)); 
 				parent.add(sweep);
 				push(sweep);
 			}
 			else
 				throw new SAXException("unsupported type " + type + "in uniform tag");
 		}
 		catch(NumberFormatException e)
 		{
 			throw new SAXException("badly formatted number in uniform tag");
 		}
 	}
 	
 	/**
 	 * Parses the start of a multiplicative combination sweep element.
 	 * @param parent The sweep's parent.
 	 * @param attrMap The sweep's attributes.
 	 * @throws SAXException Currently, never.
 	 */
 	private void startMultiplicativeElement(CombinationSweep parent, StringMap attrMap) throws SAXException
 	{
 		MultiplicativeCombinationSweep sweep = new MultiplicativeCombinationSweep();
 		parent.add(sweep);
 		push(sweep);
 	}
 	
 	/**
 	 * Parses the start of a linear/parallel sweep.
 	 * @param parent The sweep's parent.
 	 * @param attrMap The sweep's attributes
 	 * @throws SAXException Currently, never.
 	 */
 	private void startLinearElement(CombinationSweep parent, StringMap attrMap) throws SAXException
 	{
 		LinearCombinationSweep sweep = new LinearCombinationSweep();
 		parent.add(sweep);
 		push(sweep);
 	}
 }
