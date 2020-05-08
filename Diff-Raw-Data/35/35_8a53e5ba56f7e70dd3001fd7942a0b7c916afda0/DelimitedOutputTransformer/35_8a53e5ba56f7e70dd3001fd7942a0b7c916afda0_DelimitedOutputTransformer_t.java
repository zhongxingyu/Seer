 /*
  * $Id:$
  * --------------------------------------------------------------------------------------
  * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
  *
  * The software in this package is published under the terms of the CPAL v1.0
  * license, a copy of which has been included with this distribution in the
  * LICENSE.txt file.
  */
 package org.mule.module.datapack;
 
 import org.apache.commons.lang.StringUtils;
 import org.mule.api.MuleMessage;
 import org.mule.api.transformer.TransformerException;
 import org.mule.module.datapack.columns.Column;
 
 public class DelimitedOutputTransformer extends DataPackTransformer
 {
     
     private String newlineChar = "\n";
     private String delimiterChar = "\t";
     private Boolean trimToLength = false;
     private Boolean fillToLength = false;
     private Boolean prefixToLength = false;
     private String fillLengthChar = StringUtils.EMPTY;
     private Boolean addSpace = false;
     private String encloseChar = null;
     private String headersToVariable = null;
 
     /*
      * (non-Javadoc)
      * @see org.mule.module.datapack.DataPackTransformer#datapack(org.mule.api.MuleMessage, java.lang.String)
      */
     @Override
     public Object datapack(MuleMessage message, String outputEncoding) throws TransformerException
     {
         this.generateHeadersIfNecessary(message);
     	
     	StringBuilder output = new StringBuilder();
 
         for (int i = 0; i < columns.size(); i++)
         {
             Column column = columns.get(i);
             String value = this.evaluate(message, column); 
             
             String encloseChar = determineEncloseChar(column);
             
             // Opening enclose 
             this.encloseIfNessesary(output, encloseChar);
             
            int columnLength = 0;
             if (!StringUtils.isEmpty(column.getLength())) {
             	try {
             		columnLength = Integer.parseInt(column.getLength());
             	} catch (NumberFormatException e) {
             		columnLength = 0;
             	}
             }
 
             if (columnLength > 0) {
             	int vlen = value.length();
             	
             	if (this.trimToLength) {
 	                value = value.substring(0, vlen < columnLength ? vlen : columnLength);
             	} else if (this.fillToLength && vlen < columnLength) {
            		value = StringUtils.rightPad(value, columnLength, this.fillLengthChar);
             	} else if (this.prefixToLength && vlen < columnLength) {
            		value = StringUtils.leftPad(value, columnLength, this.fillLengthChar);
             	}
             }
             
             output.append(value);
             
             // check to see if a space is needed
             if (addSpace && value.length() == 0)
             {
             	output.append(' ');
             }
 
             // Closing enclose 
             this.encloseIfNessesary(output, encloseChar);
 
             
             // column marked as a linebreak
             if (column.getLineBreak() != null && Boolean.parseBoolean(column.getLineBreak()))
             {
             	output.append(newlineChar);
             }
             // Only put the delimiter on everything except for the last column or column marked as line break
             else if (i < columns.size() - 1)
             {
                 output.append(delimiterChar);
             }
         }
 
         output.append(newlineChar);
 
         return output.toString();
     }
     
     private void generateHeadersIfNecessary(MuleMessage message) {
     	if (StringUtils.isEmpty(this.headersToVariable)) {
     		return;
     	}
     	
     	StringBuilder headers = new StringBuilder();
     	
     	for (int i = 0, count = this.columns.size() -1; i <= count; i++) {
     		String columnName = this.columns.get(i).getColumnName();
     		
     		if (StringUtils.isEmpty(columnName)) {
     			throw new IllegalArgumentException("column at index " + i + " does not have a column-name");
     		}
     		
     		this.encloseIfNessesary(headers, this.encloseChar);
     		headers.append(columnName);
     		this.encloseIfNessesary(headers, this.encloseChar);
     		
     		if (i < count) {
     			headers.append(this.delimiterChar);
     		}
     	}
     	
     	message.setOutboundProperty(this.headersToVariable, headers.toString());
     }
 
 	private String determineEncloseChar(Column column) {
 		String encloseChar = this.encloseChar;
 		if (encloseChar == null) {
 			encloseChar = column.getEncloseChar();
 		}
 		return encloseChar;
 	}
     
     private void encloseIfNessesary(StringBuilder output, String encloseChar) {
     	if (encloseChar != null) {
     		output.append(encloseChar);
     	}
     }
 
 
     public String getNewlineChar()
     {
         return newlineChar;
     }
 
     public void setNewlineChar(String newlineChar)
     {
         this.newlineChar = newlineChar;
     }
 
     public String getDelimiterChar()
     {
         return delimiterChar;
     }
 
     public void setDelimiterChar(String delimiterChar)
     {
 		if(delimiterChar.equals("\\t"))
 		{
 			this.delimiterChar = "\t";
 		} else
 		{
 			this.delimiterChar = delimiterChar;
 		}
     }
 
     public Boolean getTrimToLength()
     {
         return trimToLength;
     }
 
     public void setTrimToLength(Boolean trimToLength)
     {
         this.trimToLength = trimToLength;
     }
     
     public Boolean getAddSpace()
     {
     	return addSpace;
     }
     
     public void setAddSpace(Boolean addSpace)
     {
     	this.addSpace = addSpace;
     }
 
 	public String getEncloseChar() {
 		return encloseChar;
 	}
 
 	public void setEncloseChar(String encloseChar) {
 		this.encloseChar = encloseChar;
 	}
 
 	public String getHeadersToVariable() {
 		return headersToVariable;
 	}
 
 	public void setHeadersToVariable(String headersToVariable) {
 		this.headersToVariable = headersToVariable;
 	}
 
 	public Boolean getFillToLength() {
 		return fillToLength;
 	}
 
 	public void setFillToLength(Boolean fillToLength) {
 		this.fillToLength = fillToLength;
 	}
 
 	public String getFillLengthChar() {
 		return fillLengthChar;
 	}
 
 	public void setFillLengthChar(String fillLengthChar) {
 		this.fillLengthChar = fillLengthChar;
 	}
 
 	public Boolean getPrefixToLength() {
 		return prefixToLength;
 	}
 
 	public void setPrefixToLength(Boolean prefixToLength) {
 		this.prefixToLength = prefixToLength;
 	}
 	
     
 }
