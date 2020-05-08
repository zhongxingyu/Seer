 /****************************************************************************
  * Copyright (C) 2011 GGA Software Services LLC
  *
  * This file may be distributed and/or modified under the terms of the
  * GNU General Public License version 3 as published by the Free Software
  * Foundation.
  *
  * This file is provided AS IS with NO WARRANTY OF ANY KIND, INCLUDING THE
  * WARRANTY OF DESIGN, MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, see <http://www.gnu.org/licenses>.
  ***************************************************************************/
 
 package com.ggasoftware.indigo.knime.cell;
 
 import com.ggasoftware.indigo.*;
 import com.ggasoftware.indigo.knime.plugin.IndigoPlugin;
 
 import java.io.IOException;
 
 import org.knime.core.data.*;
 import org.knime.core.node.NodeLogger;
 
 import sun.misc.BASE64Decoder;
 import sun.misc.BASE64Encoder;
 
 @SuppressWarnings("serial")
 public class IndigoMolCell extends IndigoDataCell implements IndigoMolValue
 {
    private static final NodeLogger LOGGER = NodeLogger
          .getLogger(IndigoMolCell.class);
 
    private static class Serializer implements
          DataCellSerializer<IndigoMolCell>
    {
       /**
        * {@inheritDoc}
        */
       public void serialize (final IndigoMolCell cell,
             final DataCellDataOutput out) throws IOException
       {
          try
          {
             IndigoPlugin.lock();
             BASE64Encoder encoder = new BASE64Encoder();
             String str = encoder.encodeBuffer(cell.getIndigoObject().serialize());
             out.writeUTF(str);            
          }
          catch (IndigoException ex)
          {
             LOGGER.error("Error while serializing Indigo object", ex);
             throw new IOException(ex.getMessage());
          }
          finally
          {
             IndigoPlugin.unlock();
          }
       }
 
       /**
        * {@inheritDoc}
        */
       public IndigoMolCell deserialize (final DataCellDataInput input)
             throws IOException
       {
          String str = input.readUTF();
 
          BASE64Decoder decoder = new BASE64Decoder();
          byte[] buf = decoder.decodeBuffer(str);
          try
          {
             IndigoPlugin.lock();
             return new IndigoMolCell(IndigoPlugin.getIndigo().unserialize(buf));
          }
          catch (IndigoException ex)
          {
            LOGGER.error("Error while unserializing Indigo object: " + ex.getMessage(), ex);
             throw new IOException(ex.getMessage());
          }
          finally
          {
             IndigoPlugin.unlock();
          }
       }
    }
 
    private static final DataCellSerializer<IndigoMolCell> SERIALIZER = new Serializer();
 
    public static final DataCellSerializer<IndigoMolCell> getCellSerializer ()
    {
       return SERIALIZER;
    }
 
    public static final DataType TYPE = DataType.getType(IndigoMolCell.class);
 
    public IndigoMolCell (IndigoObject obj)
    {
       super(obj);
 
       // Try to serialize to check unexpected configurations: extraordinary charge or etc.
      try
      {
         IndigoPlugin.lock();
         obj.serialize();
      }
      finally
      {
         IndigoPlugin.unlock();
      }
    }
 
    @Override
    public String toString ()
    {
       try
       {
          IndigoPlugin.lock();
          
          // Return a SMILES string if it can be calculated
          try
          {
             return _object.smiles();
          }
          catch (IndigoException e)
          {
             // If SMILES is not an option, return the unique Indigo's object ID
             return "<Indigo object #" + _object.self + ">";
          }
       }
       finally
       {
          IndigoPlugin.unlock();
       }
    }
 
    @Override
    protected boolean equalsDataCell (DataCell dc)
    {
       IndigoMolCell other = (IndigoMolCell)dc;
       
       try
       {
          IndigoPlugin.lock();
          
          IndigoObject match = IndigoPlugin.getIndigo().exactMatch(_object, other._object);
          
          if (match != null)
             return true;
       }
       catch (IndigoException e)
       {
          // ignore the exception; default to the false result
       }
       finally
       {
          IndigoPlugin.unlock();
       }
       
       return false;
    }
 
    @Override
    public int hashCode ()
    {
       return 0;
    }
 
}
