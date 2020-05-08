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
 
 /**
  *
  */
 package org.Transformer.importer;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.util.Map;
 import java.util.Vector;
 
 import org.Transformer.JobUtils;
 import org.Transformer.dataset.DataSet;
 
 /**
  * @author Lars P&ouml;tter
  * (<a href=mailto:Lars_Poetter@gmx.de>Lars_Poetter@gmx.de</a>)
  */
 public class CsvImportSelector extends ImportSelector
 {
    private String separator = ",";
     private String[] mapping;
 
     /**
      *
      */
     public CsvImportSelector()
     {
         super();
     }
 
     public void setMapping(String[] theMapping)
     {
         mapping = theMapping;
     }
 
     public void setSeperator(String theSeperator)
     {
        separator = theSeperator;
     }
 
 
     /**
      * @see org.Transformer.importer.ImportFilter#parseToDataSets(java.io.InputStream)
      */
     @Override
     public boolean parseToDataSets(InputStream src)
     {
         Vector<DataSet> res = new Vector<DataSet>();
         InputStreamReader in = new InputStreamReader(src);
         StringBuffer sb = new StringBuffer();
         int r;
         try
         {
             do{
                 r = in.read();
                 if(-1 != r)
                 {
                     char c = (char)r;
                     if(c == '\n')
                     {
                         // end of Line reached
                         String line = sb.toString();
                         DataSet LineSet = parseLineToDataSet(line);
                         if(null != LineSet)
                         {
                             res.add(LineSet);
                         }
                         sb = new StringBuffer();
                     }
                     else
                     {
                         sb.append(c);
                     }
                 }
             }while(-1 != r);
         }
         catch(IOException e)
         {
             e.printStackTrace();
         }
         // end of Line reached
         String line = sb.toString();
         DataSet LineSet = parseLineToDataSet(line);
         if(null != LineSet)
         {
             res.add(LineSet);
         }
 
         data = res.toArray(new DataSet[1]);
         return true;
     }
 
 
     private DataSet parseLineToDataSet(String line)
     {
         System.out.println("Line : " + line);
        String[] Values = line.split(separator);
        System.out.println("Split with Separator : " + separator + " !");
         for(int i = 0; i < Values.length; i++)
         {
             System.out.println("Atom " + i + " : " + Values[i]);
         }
         DataSet row = new DataSet();
         System.out.println("mapping.length : " + mapping.length);
         for(int i = 0; i < mapping.length; i++)
         {
             System.out.println("Mapping " + i + " : " + mapping[i]);
         }
         int maxLength;
         if(mapping.length > Values.length)
         {
             maxLength = Values.length;
         }
         else
         {
             maxLength = mapping.length;
         }
         for(int j = 0; j < maxLength; j++)
         {
             String Name = mapping[j];
             if(null != Name)
             {
                 row.addDataAtom(Values[j], Name);
             }
             // else this field is not mapped
         }
         return row;
     }
 
     @Override
     public String getConfig()
     {
        return "separator = " + separator + "\n"
                + JobUtils.getConfigTextFor(mapping, "mapping");
     }
 
     @Override
     public void setConfig(Map<String, String> cfg)
     {
         mapping = JobUtils.getStringArrayFromSettingMap(cfg, "mapping");
        separator = cfg.get("separator");
     }
 
     @Override
     public String getName()
     {
         return "CsvImportSelector";
     }
 
 }
