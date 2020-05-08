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
 package org.Transformer.exporter;
 
 import java.io.IOException;
 import java.io.OutputStream;
 import java.util.Map;
 
 import org.Transformer.JobUtils;
 import org.Transformer.dataset.DataSet;
 
 /**
  * @author Lars P&ouml;tter
  * (<a href=mailto:Lars_Poetter@gmx.de>Lars_Poetter@gmx.de</a>)
  */
 public class CsvExportStyle extends ExportStyle
 {
     private String[] mapping;
    private String seperator = ",";
 
     /**
      *
      */
     public CsvExportStyle()
     {
     }
 
     public void setSeperator(String theSeperator)
     {
        seperator = theSeperator;
     }
 
     public void setMapping(String[] theMapping)
     {
         mapping = theMapping;
     }
 
     /**
      * @see org.Transformer.exporter.ExportStyle#formatTheData(org.Transformer.dataset.DataSet[], java.io.OutputStream)
      */
     @Override
     public boolean formatTheData(DataSet[] theData, OutputStream out) throws IOException
     {
         if((null == mapping) || (null == theData) || (null == out))
         {
             return false;
         }
         for(int i = 0; i < theData.length; i++)
         {
             String line = "";
             DataSet curSet = theData[i];
             if(null != curSet)
             {
                 int maxLength;
                 if(mapping.length > curSet.getNumberOfAtoms())
                 {
                     maxLength = curSet.getNumberOfAtoms();
                 }
                 else
                 {
                     maxLength = mapping.length;
                 }
                 for(int j = 0; j < maxLength; j++)
                 {
                     Object val = curSet.getDataAtom(mapping[j]);
                     if(null != val)
                     {
                        line = line + val.toString() + seperator;
                     }
                 }
             }
             line = line + "\n";
             out.write(line.getBytes());
         }
         return true;
     }
 
 
     @Override
     public String getConfig()
     {
        return "seperator = " + seperator + "\n"
                + JobUtils.getConfigTextFor(mapping, "mapping");
     }
 
     @Override
     public void setConfig(Map<String, String> cfg)
     {
         mapping = JobUtils.getStringArrayFromSettingMap(cfg, "mapping");
        seperator = cfg.get("seperator");
     }
 
     @Override
     public String getName()
     {
         return "CsvExportStyle";
     }
 
 }
