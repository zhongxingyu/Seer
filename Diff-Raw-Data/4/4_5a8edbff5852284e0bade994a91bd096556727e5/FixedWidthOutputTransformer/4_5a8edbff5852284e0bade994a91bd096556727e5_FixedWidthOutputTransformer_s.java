 package org.mule.module.datapack;
 
 import org.mule.api.MuleMessage;
 import org.mule.api.expression.ExpressionManager;
 import org.mule.api.lifecycle.InitialisationException;
 import org.mule.api.transformer.TransformerException;
 import org.mule.module.datapack.columns.Column;
 import org.mule.module.datapack.i18n.DataPackMessages;
 import org.mule.transformer.AbstractMessageTransformer;
 import org.mule.util.TemplateParser;
 
 import java.util.List;
 
 public class FixedWidthOutputTransformer extends AbstractMessageTransformer
 {
     public enum PadFormat
     {
         LEFT("left"),
         RIGHT("right");
 
         private final String value;
 
         PadFormat(String padFormat)
         {
             this.value = padFormat;
         }
 
         public String getValue()
         {
             return value;
         }
     }
 
     private List<Column> columns;
     private String padChar = " ";
     private PadFormat padFormat = PadFormat.LEFT;
     private String newlineChar = "\n";
 
     protected final TemplateParser.PatternInfo patternInfo = TemplateParser.createMuleStyleParser().getStyle();
 
     protected ExpressionManager expressionManager;
 
     @Override
     public void initialise() throws InitialisationException
     {
         super.initialise();
 
         expressionManager = muleContext.getExpressionManager();
     }
 
     @Override
     public Object transformMessage(MuleMessage message, String outputEncoding) throws TransformerException
     {
         if (columns == null || columns.size() == 0)
         {
             throw new TransformerException(DataPackMessages.noColumnsDefinedMessage());
         }
 
         StringBuilder output = new StringBuilder();
 
         for (Column column : this.columns)
         {
             String value = column.evaluateColumn(message, muleContext, expressionManager, patternInfo);
 
             int length = Integer.parseInt(column.getLength());
 
             if (value.length() > length)
             {
                 output.append(value.substring(0, length));
             }
             else
             {
                 String pad = padChar;
 
                 if (column.getPadChar() != null)
                 {
                     pad = column.getPadChar();
                 }
 
                 if (padFormat == PadFormat.LEFT)
                 {
                     for (int i = 0; i < (length - value.length()); i++)
                     {
                         output.append(pad);
                     }
 
                     output.append(value);
                 }
                 else
                 {
                     output.append(value);
 
                     for (int i = 0; i < (length - value.length()); i++)
                     {
                         output.append(pad);
                     }
                 }
             }
         }
 
         output.append(newlineChar);
 
         return output.toString();
     }
 
     public List<Column> getColumns()
     {
         return columns;
     }
 
     public void setColumns(List<Column> columns)
     {
         this.columns = columns;
     }
 
     public String getPadChar()
     {
         return padChar;
     }
 
     public void setPadChar(String padChar)
     {
         this.padChar = padChar;
     }
 
     public String getPadFormat()
     {
         return padFormat.getValue();
     }
 
     public void setPadFormat(String padFormat)
     {
         this.padFormat = PadFormat.valueOf(padFormat.toUpperCase());
     }
 
     public String getNewlineChar()
     {
         return newlineChar;
     }
 
     public void setNewlineChar(String newlineChar)
     {
         this.newlineChar = newlineChar;
     }
 }
