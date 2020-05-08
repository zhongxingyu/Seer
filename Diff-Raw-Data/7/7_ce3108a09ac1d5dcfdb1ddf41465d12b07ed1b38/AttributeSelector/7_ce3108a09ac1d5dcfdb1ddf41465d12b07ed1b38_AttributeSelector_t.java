 package org.vfsutils.selector;
 
 import java.util.Map;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.apache.commons.vfs.FileSelectInfo;
 import org.apache.commons.vfs.FileSelector;
 import org.apache.commons.vfs.FileType;
import org.vfsutils.StringSplitter;
 import org.vfsutils.attrs.ArraySplitter;
 
 public class AttributeSelector extends FilenameSelector implements FileSelector {
 	
 	private String ageConstraint = null;
 	private String sizeConstraint = null;
 	private String attributeConstraints = null;
 
 	public boolean includeFile(FileSelectInfo fileInfo) throws Exception {
 		
 		boolean result = super.includeFile(fileInfo);
 		
 		if (result && sizeConstraint != null) {
 			if (fileInfo.getFile().getType().equals(FileType.FILE)) {
 				long size = fileInfo.getFile().getContent().getSize();
 				result = (includeSize(size, sizeConstraint) == !negated);
 			}
 			else {
 				// folders are excluded
 				result = false;
 			}
 		}		
 		if (result && ageConstraint != null) {
 			long age = System.currentTimeMillis() - fileInfo.getFile().getContent().getLastModifiedTime();
 			result = (includeAge(age, ageConstraint) == !negated);
 		}
 		if (result && attributeConstraints!=null) {
 			result = (includeAttributes(fileInfo.getFile().getContent().getAttributes(), attributeConstraints) == !negated);
 		}
 		return result;		
 	}
 
 		
 	protected boolean includeSize(long size, String constraint) {
 		boolean result = false;
 
 		//this pattern does not allow negative values
 		String constraintPattern = "(\\D+)(\\d+)(\\D?)";
 		Pattern p = Pattern.compile(constraintPattern);
 		Matcher m = p.matcher(constraint);
 		if (m.matches()) {
 			String operator = m.group(1);
 			String value = m.group(2);
 			String modifier = m.group(3);
 			
 			long valueAsBytes = Long.parseLong(value);
 			if (modifier==null || modifier.equals("") || modifier.equals("b")) {
 				// do nothing
 			}
 			else if (modifier.equalsIgnoreCase("k")){
 				valueAsBytes = valueAsBytes * 1024;
 			}
 			else if (modifier.equalsIgnoreCase("m")) {
 				valueAsBytes = valueAsBytes * 1024 * 1024;
 			}
 			else if (modifier.equalsIgnoreCase("g")) {
 				valueAsBytes = valueAsBytes * 1024 * 1024 * 1024;
 			}
 			else {
 				throw new IllegalArgumentException("invalid modifier " + modifier);
 			}
 			
 			if (operator.equals("gt") || operator.equals(">")) {
 				result = (size > valueAsBytes);
 			} else if (operator.equals("gte")) {
 				result = (size >= valueAsBytes);
 			} else if (operator.equals("lt")) {
 				result = (size < valueAsBytes);
 			} else if (operator.equals("lte")) {
 				result = (size <= valueAsBytes);
 			} else if (operator.equals("eq")) {
 				result = (size == valueAsBytes);
 			} else if (operator.equals("neq")) {
 				result = (size != valueAsBytes);
 			}
 			else {
 				throw new IllegalArgumentException("invalid operator " + operator);
 			}
 		}
 		
 		return result;
 	}
 
 	
 	protected boolean includeAge(long age, String constraint) {
 		boolean result = false;
 		
 		//this pattern does not allow negative values
 		String constraintPattern = "(\\D+)(\\d+)(\\D?\\D?)";
 		Pattern p = Pattern.compile(constraintPattern);
 		Matcher m = p.matcher(constraint);
 		if (m.matches()) {
 			String operator = m.group(1);
 			String value = m.group(2);
 			String modifier = m.group(3);
 			
 			long valueAsMs = Long.parseLong(value);
 			if (modifier==null || modifier.equals("")) {
 				// assume days
 				valueAsMs = valueAsMs * 1000 * 60 * 60 * 24;
 			}
 			else if (modifier.equalsIgnoreCase("ms")){
 				// do nothing
 			}
 			else if (modifier.equalsIgnoreCase("s")) {
 				valueAsMs = valueAsMs * 1000;
 			}
 			else if (modifier.equalsIgnoreCase("m")) {
 				valueAsMs = valueAsMs * 1000 * 60;
 			}
 			else if (modifier.equalsIgnoreCase("h")) {
 				valueAsMs = valueAsMs * 1000 * 60 * 60;
 			}
 			else if (modifier.equalsIgnoreCase("d")) {
 				valueAsMs = valueAsMs * 1000 * 60 * 60 * 24;
 			}
 			else if (modifier.equalsIgnoreCase("w")) {
 				valueAsMs = valueAsMs * 1000 * 60 * 60 * 24 * 7;
 			}
 			else {
 				throw new IllegalArgumentException("invalid modifier " + modifier);
 			}
 			
 			if (operator.equals("gt")) {
 				result = (age > valueAsMs);
 			} else if (operator.equals("gte")) {
 				result = (age >= valueAsMs);
 			} else if (operator.equals("lt")) {
 				result = (age < valueAsMs);
 			} else if (operator.equals("lte")) {
 				result = (age <= valueAsMs);
 			} else if (operator.equals("eq")) {
 				result = (age == valueAsMs);
 			} else if (operator.equals("neq")) {
 				result = (age != valueAsMs);
 			}
 			else {
 				throw new IllegalArgumentException("invalid operator " + operator);
 			}
 		}
 		
 		return result;
 	}
 		
 	protected boolean includeAttributes(Map attributes, String constraint) {
 		boolean result = true;
 		
 		ArraySplitter splitter = new ArraySplitter();
 		String[] constraints = splitter.split(constraint);
 		
		StringSplitter normalizer = new StringSplitter();
		
 		for (int i=0; i<constraints.length && result; i++) {
 		
 			String c = constraints[i];
 			
 			String constraintPattern = "(.+)(!=|==|<>)(.+)";
 			Pattern p = Pattern.compile(constraintPattern);
 			Matcher m = p.matcher(c);
 			if (m.matches()) {
 				String attrName = m.group(1);
 				String operator = m.group(2);
				String value = normalizer.removeQuotesAndEscapes(m.group(3));			
 				
 				result = includeAttribute(attributes.get(attrName), operator, value);			
 				
 			}
 			else {
 				result = false;
 			}
 		}
 		return result;			
 	}
 	
 	protected boolean includeAttribute(Object attrValue, String operator, String compareTo) {
 		boolean result = false;
 		
 		//TODO: make type specific
 		
 		if (attrValue==null || compareTo==null) {
 			// do nothing
 		}
 		else if (operator.equals("==")){
 			result = attrValue.toString().equals(compareTo);
 		}
 		else if (operator.equals("!=") || operator.equals("<>")){
 			result = !attrValue.toString().equals(compareTo);
 		}
 		else {
 			throw new IllegalArgumentException("Invalid operator " + operator);
 		}
 		return result;
 	}
 
 
 	public void setAge(String ageConstraint) {
 		this.ageConstraint = ageConstraint;
 	}
 
 
 	public void setSize(String sizeConstraint) {
 		this.sizeConstraint = sizeConstraint;
 	}
 	
 	public void setAttributes(String attributeConstraints) {
 		this.attributeConstraints = attributeConstraints;
 	}
 	
 }
