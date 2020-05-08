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
 
 package com.ggasoftware.indigo.knime.convert.base;
 
 import java.io.File;
 import java.io.IOException;
 
 import org.knime.chem.types.CMLCell;
 import org.knime.chem.types.CMLCellFactory;
 import org.knime.chem.types.MolCell;
 import org.knime.chem.types.MolCellFactory;
 import org.knime.chem.types.RxnCell;
 import org.knime.chem.types.RxnCellFactory;
 import org.knime.chem.types.SdfCell;
 import org.knime.chem.types.SdfCellFactory;
 import org.knime.chem.types.SmilesCell;
 import org.knime.core.data.DataCell;
 import org.knime.core.data.DataColumnSpec;
 import org.knime.core.data.DataColumnSpecCreator;
 import org.knime.core.data.DataRow;
 import org.knime.core.data.DataTableSpec;
 import org.knime.core.data.DataType;
 import org.knime.core.data.DataValue;
 import org.knime.core.data.RowKey;
 import org.knime.core.data.container.CellFactory;
 import org.knime.core.data.container.ColumnRearranger;
 import org.knime.core.data.def.StringCell;
 import org.knime.core.node.BufferedDataTable;
 import org.knime.core.node.CanceledExecutionException;
 import org.knime.core.node.ExecutionContext;
 import org.knime.core.node.ExecutionMonitor;
 import org.knime.core.node.InvalidSettingsException;
 import org.knime.core.node.NodeSettingsRO;
 import org.knime.core.node.NodeSettingsWO;
 
 import com.ggasoftware.indigo.IndigoException;
 import com.ggasoftware.indigo.IndigoInchi;
 import com.ggasoftware.indigo.IndigoObject;
 import com.ggasoftware.indigo.knime.cell.IndigoDataValue;
 import com.ggasoftware.indigo.knime.common.IndigoNodeModel;
 import com.ggasoftware.indigo.knime.convert.base.IndigoSaverSettings.Format;
 import com.ggasoftware.indigo.knime.plugin.IndigoPlugin;
 
 abstract public class IndigoSaverNodeModel extends IndigoNodeModel
 {
    private final IndigoSaverSettings _settings = new IndigoSaverSettings();
 
 //   private static final NodeLogger LOGGER = NodeLogger
 //         .getLogger(IndigoSaverNodeModel.class);
 
    private final Class<? extends DataValue> _dataValueClass;
    
    /**
     * Constructor for the node model.
     */
    protected IndigoSaverNodeModel(Class<? extends DataValue> dataValueClass)
    {
       super(1, 1);
       _dataValueClass = dataValueClass;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected BufferedDataTable[] execute (final BufferedDataTable[] inData,
          final ExecutionContext exec) throws Exception
    {
       ColumnRearranger crea = createRearranger(inData[0].getDataTableSpec());
       
 
       BufferedDataTable rearrangeTable = exec.createColumnRearrangeTable(
             inData[0], crea, exec);
       
       handleWarningMessages();
       
       return new BufferedDataTable[] { rearrangeTable };
    }
 
    /**
     * {@inheritDoc}
     */
    @Override
    protected void reset ()
    {
    }
 
    class Converter implements CellFactory
    {
       int _colIndex;
       private final DataColumnSpec[] m_colSpec;
 
       Converter(final DataTableSpec inSpec, final DataColumnSpec cs,
             final IndigoSaverSettings settings, final int colIndex)
       {
          _colIndex = colIndex;
 
          DataType type = null;
 
          if (_settings.destFormat.getStringValue().equals(Format.Mol.toString()))
             type = MolCell.TYPE;
          else if (_settings.destFormat.getStringValue().equals(Format.Rxn.toString()))
              type = RxnCell.TYPE;
          else if (_settings.destFormat.getStringValue().equals(Format.SDF.toString()))
             type = SdfCell.TYPE;
          else if (_settings.destFormat.getStringValue().equals(Format.Smiles.toString()) || _settings.destFormat.getStringValue().equals(Format.CanonicalSmiles.toString()))
             type = SmilesCell.TYPE;
          else if (_settings.destFormat.getStringValue().equals(Format.Smiles.toString()) || _settings.destFormat.getStringValue().equals(Format.CML.toString()))
             type = CMLCell.TYPE;
          else
             type = StringCell.TYPE;
 
          if (settings.appendColumn.getBooleanValue())
          {
             m_colSpec = new DataColumnSpec[] { new DataColumnSpecCreator(
                   DataTableSpec
                   .getUniqueColumnName(inSpec, settings.newColName.getStringValue()),
                   type).createSpec() };
          }
          else
          {
             m_colSpec = new DataColumnSpec[] { new DataColumnSpecCreator(
                   settings.colName.getStringValue(), type).createSpec() };
          }
       }
 
       public DataCell getCell (final DataRow row)
       {
          DataCell cell = row.getCell(_colIndex);
          if (cell.isMissing())
          {
             return cell;
          }
          else
          {
             IndigoObject io = ((IndigoDataValue)cell).getIndigoObject();
             try
             {
                IndigoPlugin.lock();
                
                String destFormat = _settings.destFormat.getStringValue();
                if (destFormat.equals(Format.Mol.toString()) || 
                      destFormat.equals(Format.SDF.toString()) ||
                      destFormat.equals(Format.CML.toString()) ||
                      destFormat.equals(Format.Rxn.toString()))
                   if (_settings.generateCoords.getBooleanValue() && !io.hasCoord())
                   {
                      io = io.clone();
                      io.layout();
                   }
                
                if (destFormat.equals(Format.Mol.toString()))
                   return MolCellFactory.create(io.molfile());
                if (destFormat.equals(Format.Rxn.toString()))
                    return RxnCellFactory.create(io.rxnfile());
                if (destFormat.equals(Format.SDF.toString())) {
                   return SdfCellFactory.create(io.molfile() + "\n$$$$\n");
                }
                if (destFormat.equals(Format.Smiles.toString()))
                   return new SmilesCell(io.smiles());
                if (destFormat.equals(Format.CanonicalSmiles.toString()))
                {
                   IndigoObject clone = io.clone();
                   clone.aromatize();
                   return new SmilesCell(clone.canonicalSmiles());
                }
                if (destFormat.equals(Format.InChI.toString()) ||
                      destFormat.equals(Format.InChIKey.toString()))
                {
                   IndigoInchi indigo_inchi = IndigoPlugin.getIndigoInchi(); 
                   String result = indigo_inchi.getInchi(io);
                   // Uncomment this only after implementing InChI 
                   // stereolayer support:
                   //String warning = indigo_inchi.getWarning(); 
                   //if (warning != "")
                   //   LOGGER.info("InChI warning: " + warning);
                   if (destFormat.equals(Format.InChIKey.toString()))
                      result = indigo_inchi.getInchiKey(result); 
                   return new StringCell(result);
                }
                return CMLCellFactory.create(io.cml());
             }
             catch (IndigoException ex)
             {
               appendWarningMessage("Could not convert molecule with RowId=" + 
                     row.getKey() + ": " + ex.getMessage());
                return DataType.getMissingCell();
             }
             finally
             {
                IndigoPlugin.unlock();
             }
          }
       }
 
       @Override
       public DataCell[] getCells (DataRow row)
       {
          return new DataCell[] { getCell(row) };
       }
 
       @Override
       public DataColumnSpec[] getColumnSpecs ()
       {
          return m_colSpec;
       }
 
       @Override
       public void setProgress (int curRowNr, int rowCount, RowKey lastKey,
             ExecutionMonitor exec)
       {
          exec.setProgress((double)curRowNr / rowCount);
       }
    }
 
    private ColumnRearranger createRearranger (final DataTableSpec inSpec)
    {
       ColumnRearranger crea = new ColumnRearranger(inSpec);
 
       DataType type = null;
       String destFormat = _settings.destFormat.getStringValue();
       if (destFormat.equals(Format.Mol.toString()))
          type = MolCell.TYPE;
       else if (destFormat.equals(Format.Rxn.toString()))
           type = RxnCell.TYPE;
       else if (destFormat.equals(Format.SDF.toString()))
          type = SdfCell.TYPE;
       else if (destFormat.equals(Format.Smiles.toString()) ||
             destFormat.equals(Format.CanonicalSmiles.toString()))
          type = SmilesCell.TYPE;
       else if (destFormat.equals(Format.CML.toString()))
          type = CMLCell.TYPE;
       else if (destFormat.equals(Format.InChI.toString()) || destFormat.equals(Format.InChIKey.toString()))
          type = StringCell.TYPE;
 
       DataColumnSpec cs;
       if (_settings.appendColumn.getBooleanValue())
       {
          String name = DataTableSpec.getUniqueColumnName(inSpec,
                _settings.newColName.getStringValue());
          cs = new DataColumnSpecCreator(name, type).createSpec();
       }
       else
       {
          cs = new DataColumnSpecCreator(_settings.colName.getStringValue(), type).createSpec();
       }
 
       Converter conv = new Converter(inSpec, cs, _settings,
             inSpec.findColumnIndex(_settings.colName.getStringValue()));
 
       if (_settings.appendColumn.getBooleanValue())
       {
          crea.append(conv);
       }
       else
       {
          crea.replace(conv, _settings.colName.getStringValue());
       }
 
       return crea;
    }
 
    /**
     * {@inheritDoc}
     */
    @Override
    protected DataTableSpec[] configure (final DataTableSpec[] inSpecs)
          throws InvalidSettingsException
    {
 	   _settings.colName.setStringValue(searchIndigoColumn(inSpecs[0], _settings.colName.getStringValue(), _dataValueClass));
 	   /*
        * Set loading parameters warning message
        */
       if(_settings.warningMessage != null) {
          setWarningMessage(_settings.warningMessage);
       }
       return new DataTableSpec[] { createRearranger(inSpecs[0]).createSpec() };
    }
 
    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveSettingsTo (final NodeSettingsWO settings)
    {
       _settings.saveSettingsTo(settings);
    }
 
    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadValidatedSettingsFrom (final NodeSettingsRO settings)
          throws InvalidSettingsException
    {
       _settings.loadSettingsFrom(settings);
    }
 
    /**
     * {@inheritDoc}
     */
    @Override
    protected void validateSettings (final NodeSettingsRO settings)
          throws InvalidSettingsException
    {
       IndigoSaverSettings s = new IndigoSaverSettings();
       s.loadSettingsFrom(settings);
       if (s.appendColumn.getBooleanValue())
          if (s.newColName.getStringValue() == null || s.newColName.getStringValue().length() < 1)
             throw new InvalidSettingsException("No name for the new column given");
       
    }
 
    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadInternals (final File internDir,
          final ExecutionMonitor exec) throws IOException,
          CanceledExecutionException
    {
    }
 
    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveInternals (final File internDir,
          final ExecutionMonitor exec) throws IOException,
          CanceledExecutionException
    {
    }
 }
