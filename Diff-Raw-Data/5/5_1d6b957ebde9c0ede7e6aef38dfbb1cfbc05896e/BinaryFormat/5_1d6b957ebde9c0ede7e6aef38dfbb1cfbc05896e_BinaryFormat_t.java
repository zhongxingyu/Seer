 /*
  * This program is free software; you can redistribute it and/or
  * modify it under the terms of the GNU General Public License version 2
  * as published by the Free Software Foundation.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License along
  * with this program; if not, see <http://www.gnu.org/licenses/>
  *
  */
 package de.ing_poetter.binview;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.nio.charset.Charset;
 import java.util.NoSuchElementException;
 import java.util.Vector;
 
 import javax.swing.table.AbstractTableModel;
 
 import de.ing_poetter.binview.variables.Variable;
 import de.ing_poetter.binview.variables.VariableFactory;
 
 /**
  * @author Lars P&ouml;tter
  * (<a href=mailto:Lars_Poetter@gmx.de>Lars_Poetter@gmx.de</a>)
  */
 public class BinaryFormat extends AbstractTableModel
 {
     private static final long serialVersionUID = 1L;
     private final Vector<Variable> Variables = new Vector<Variable>();
 
     public static BinaryFormat loadFromFile(final String fileName)
     {
         final File f = new File(fileName);
         return loadFromFile(f);
     }
 
     public static BinaryFormat loadFromFile(final File f)
     {
         final BinaryFormat res = new BinaryFormat();
         if(true == f.canRead())
         {
             BufferedReader br = null;
             InputStreamReader fr = null;
             try
             {
                 fr = new InputStreamReader(new FileInputStream(f), Charset.forName("UTF-8"));
                 br = new BufferedReader(fr);
                 String curLine = null;
                 do{
                     curLine = br.readLine();
                     if(null != curLine)
                     {
                         final Variable v = VariableFactory.createVariableFrom(curLine);
                         res.addVariable(v);
                     }
                 }while(null != curLine);
             }
             catch (final FileNotFoundException e)
             {
                 e.printStackTrace();
             }
             catch (final IOException e)
             {
                 e.printStackTrace();
             }
             catch(final NoSuchElementException e)
             {
                 e.printStackTrace();
             }
         }
         return res;
     }
 
     public BinaryFormat()
     {
     }
 
     public void saveToFile(final File f) throws IOException
     {
         final FileWriter fw = new FileWriter(f);
         for(int i = 0; i < Variables.size(); i++)
         {
             final Variable v = Variables.get(i);
             final String res = v.save();
             fw.write(res + "\n");
         }
         fw.close();
     }
 
     public String aplyTo(final String source)
     {
         // false = 0;
         // true  = 1;
         if(null == source)
         {
             return null;
         }
         final Vector<Boolean> bits = getBitsFromString(source);
        String res = "Found " + bits.size() + " bits / " + bits.size()/8 + " bytes in Data !\n";
 
         int maxDescriptionLength = 0;
         for(int i = 0; i < Variables.size(); i++)
         {
             final Variable v = Variables.get(i);
             final int curDescrLength = v.getDescriptionLength();
             if(curDescrLength > maxDescriptionLength)
             {
                 maxDescriptionLength = curDescrLength;
             }
         }
         // match bits with format
         int posInBits = 0;
         for(int i = 0; i < Variables.size(); i++)
         {
             final Variable v = Variables.get(i);
             final int numBits = v.getNumberBits();
             if(posInBits + numBits > bits.size())
             {
                 res = res + "End of Data reached !\n";
                 break;
             }
             else
             {
                 final boolean[] curData = new boolean[numBits];
                 for(int j = 0; j < numBits; j++)
                 {
                     curData[j] = bits.get(posInBits);
                     posInBits++;
                 }
                 res = res + v.describeValue(curData, maxDescriptionLength) + "\n";
             }
         }
         if(bits.size() > posInBits)
         {
             final int numAdditionalBits = bits.size() - posInBits;
             final StringBuilder sb = new StringBuilder();
             for(int i = 0; i < numAdditionalBits; i++)
             {
                final Boolean b = bits.get(posInBits + i);
                 if(true == b)
                 {
                     sb.append('1');
                 }
                 else
                 {
                     sb.append('0');
                 }
             }
             res = res + "Additional Data at end : " + sb.toString() + "\n";
         }
         return res;
     }
 
     private Vector<Boolean> getBitsFromString(final String source)
     {
         final Vector<Boolean> bits = new Vector<Boolean>();
         for(int i = 0; i < source.length(); i++)
         {
             final char c = source.charAt(i);
             switch(c)
             {
             case '0':bits.add(false);bits.add(false);bits.add(false);bits.add(false);break;
             case '1':bits.add(false);bits.add(false);bits.add(false);bits.add(true); break;
             case '2':bits.add(false);bits.add(false);bits.add(true); bits.add(false);break;
             case '3':bits.add(false);bits.add(false);bits.add(true); bits.add(true); break;
             case '4':bits.add(false);bits.add(true); bits.add(false);bits.add(false);break;
             case '5':bits.add(false);bits.add(true); bits.add(false);bits.add(true); break;
             case '6':bits.add(false);bits.add(true); bits.add(true); bits.add(false);break;
             case '7':bits.add(false);bits.add(true); bits.add(true); bits.add(true); break;
             case '8':bits.add(true); bits.add(false);bits.add(false);bits.add(false);break;
             case '9':bits.add(true); bits.add(false);bits.add(false);bits.add(true); break;
             case 'a':
             case 'A':bits.add(true); bits.add(false);bits.add(true); bits.add(false);break;
             case 'b':
             case 'B':bits.add(true); bits.add(false);bits.add(true); bits.add(true); break;
             case 'c':
             case 'C':bits.add(true); bits.add(true); bits.add(false);bits.add(false);break;
             case 'd':
             case 'D':bits.add(true); bits.add(true); bits.add(false);bits.add(true); break;
             case 'e':
             case 'E':bits.add(true); bits.add(true); bits.add(true); bits.add(false);break;
             case 'f':
             case 'F':bits.add(true); bits.add(true); bits.add(true); bits.add(true); break;
             case ' ': break;
             case '\n': break;
             case '\r': break;
             case '\t': break;
             default:
                 if(0 != bits.size())
                 {
                     // Crazy chars after data -> ignore + end of data reached
                     // break is not getting us out of the for loop
                     i = source.length();
                 }
                 // else ignore crazy chars before data
             }
         }
         return bits;
     }
 
     private void addVariable(final Variable v)
     {
         if(null != v)
         {
             Variables.add(v);
         }
     }
 
     public void removeVariable(final int idx)
     {
         Variables.remove(idx);
         this.fireTableDataChanged();
     }
 
     public void addVariableAt(final Variable v, final int idx)
     {
         Variables.add(idx, v);
         this.fireTableDataChanged();
     }
 
     @Override
     public int getRowCount()
     {
         return Variables.size();
     }
 
     @Override
     public int getColumnCount()
     {
         return 3;
     }
 
     @Override
     public Object getValueAt(final int rowIndex, final int columnIndex)
     {
         final Variable v = Variables.get(rowIndex);
         if(0 == columnIndex)
         {
             return v.getName();
         }
         else if(1 == columnIndex)
         {
             return v.getTypeName();
         }
         else
         {
             return v.getNumberBits();
         }
     }
 
     @Override
     public String getColumnName(final int col)
     {
         if(0 == col)
         {
             return "Name";
         }
         else if(1 == col)
         {
             return "type";
         }
         else
         {
             return "number of Bits";
         }
     }
 
     @SuppressWarnings({ "rawtypes", "unchecked" })
     @Override
     public Class getColumnClass(final int c)
     {
         switch(c)
         {
         case 0:
         case 1:return String.class;
         case 2:return Integer.class;
         }
         return String.class;
     }
 
 }
