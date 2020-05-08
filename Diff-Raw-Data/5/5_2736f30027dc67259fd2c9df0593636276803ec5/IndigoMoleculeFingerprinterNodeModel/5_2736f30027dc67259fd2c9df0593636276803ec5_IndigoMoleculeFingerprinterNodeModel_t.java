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
 
 package com.ggasoftware.indigo.knime.molfp;
 
 import java.io.File;
 import java.io.IOException;
 
 import org.knime.core.data.DataCell;
 import org.knime.core.data.DataColumnSpec;
 import org.knime.core.data.DataColumnSpecCreator;
 import org.knime.core.data.DataRow;
 import org.knime.core.data.DataTableSpec;
 import org.knime.core.data.DataType;
 import org.knime.core.data.RowKey;
 import org.knime.core.data.def.DefaultRow;
 import org.knime.core.data.vector.bitvector.SparseBitVectorCell;
 import org.knime.core.data.vector.bitvector.SparseBitVectorCellFactory;
 import org.knime.core.node.*;
 
 import com.ggasoftware.indigo.*;
 import com.ggasoftware.indigo.knime.IndigoNodeSettings;
 import com.ggasoftware.indigo.knime.IndigoNodeSettings.STRUCTURE_TYPE;
 import com.ggasoftware.indigo.knime.cell.IndigoDataCell;
 import com.ggasoftware.indigo.knime.cell.IndigoMolValue;
 import com.ggasoftware.indigo.knime.cell.IndigoReactionValue;
 import com.ggasoftware.indigo.knime.common.IndigoNodeModel;
 import com.ggasoftware.indigo.knime.plugin.IndigoPlugin;
 
 public class IndigoMoleculeFingerprinterNodeModel extends IndigoNodeModel
 {
 
    private final IndigoMoleculeFingerprinterSettings _settings = new IndigoMoleculeFingerprinterSettings();
    
    /**
     * Constructor for the node model.
     */
    protected IndigoMoleculeFingerprinterNodeModel ()
    {
       super(1, 1);
    }
 
    /**
     * {@inheritDoc}
     */
    @Override
    protected BufferedDataTable[] execute (final BufferedDataTable[] inData,
          final ExecutionContext exec) throws Exception
    {
       BufferedDataTable bufferedDataTable = inData[IndigoMoleculeFingerprinterSettings.INPUT_PORT];
       _defineStructureType(bufferedDataTable.getDataTableSpec());
       
       DataTableSpec spec = getDataTableSpec(bufferedDataTable.getDataTableSpec());
 
       BufferedDataContainer outputContainer = exec.createDataContainer(spec);
 
       int colIdx = spec.findColumnIndex(_settings.colName.getStringValue());
 
       if (colIdx == -1)
          throw new Exception("column not found");
 
       int rowNumber = 1;
 
       for (DataRow inputRow : bufferedDataTable)
       {
          RowKey key = inputRow.getKey();
          DataCell[] cells = new DataCell[inputRow.getNumCells() + 1];
          
          int cellIdx;
          String fp = null;
          
          if (!inputRow.getCell(colIdx).isMissing())
             try {
                IndigoObject io = ((IndigoDataCell) (inputRow.getCell(colIdx))).getIndigoObject();
                IndigoPlugin.lock();
 
                IndigoPlugin.getIndigo().setOption("fp-sim-qwords", _settings.fpSizeQWords.getIntValue());
                IndigoPlugin.getIndigo().setOption("fp-tau-qwords", 0);
                IndigoPlugin.getIndigo().setOption("fp-any-qwords", 0);
                IndigoPlugin.getIndigo().setOption("fp-ord-qwords", 0);
                io = io.clone();
                
                io.aromatize();
 
                fp = io.fingerprint("sim").toString();
            }catch (IndigoException e) {
               appendWarningMessage("Error while aromatizing structure with RowId = '" + inputRow.getKey()+ "': " + e.getMessage());
             } finally {
                IndigoPlugin.unlock();
             }
 
          for (cellIdx = 0; cellIdx < inputRow.getNumCells(); cellIdx++)
             cells[cellIdx] = inputRow.getCell(cellIdx);
          
          if(fp != null)
             cells[cellIdx] = new SparseBitVectorCellFactory(fp.substring(6)).createDataCell();
          else
             cells[cellIdx] = DataType.getMissingCell();
          
          outputContainer.addRowToTable(new DefaultRow(key, cells));
          exec.checkCanceled();
          exec.setProgress(rowNumber / (double) bufferedDataTable.getRowCount(),
                "Adding row " + rowNumber);
 
          rowNumber++;
       }
      
      handleWarningMessages();
 
       outputContainer.close();
       return new BufferedDataTable[] { outputContainer.getTable() };
    }
 
    /**
     * {@inheritDoc}
     */
    @Override
    protected void reset ()
    {
    }
 
    protected DataTableSpec getDataTableSpec (DataTableSpec inSpec) throws InvalidSettingsException
    {
       DataColumnSpec[] specs = new DataColumnSpec[inSpec.getNumColumns() + 1];
 
       if (_settings.newColName.getStringValue() == null || _settings.newColName.getStringValue().length() < 1)
          throw new InvalidSettingsException("No new column name specified");
       
       int i;
 
       for (i = 0; i < inSpec.getNumColumns(); i++)
          specs[i] = inSpec.getColumnSpec(i);
 
       specs[i] = new DataColumnSpecCreator(_settings.newColName.getStringValue(), SparseBitVectorCell.TYPE).createSpec(); 
          
       return new DataTableSpec(specs);
    }
    
    private STRUCTURE_TYPE _defineStructureType(DataTableSpec tSpec) {
       STRUCTURE_TYPE stype = IndigoNodeSettings.getStructureType(tSpec, _settings.colName.getStringValue());
       _settings.structureType = stype;
       return stype;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected DataTableSpec[] configure (final DataTableSpec[] inSpecs)
          throws InvalidSettingsException
    {
       DataTableSpec inSpec = inSpecs[IndigoMoleculeFingerprinterSettings.INPUT_PORT];
 
       searchMixedIndigoColumn(inSpec, _settings.colName, IndigoMolValue.class, IndigoReactionValue.class);
 
       STRUCTURE_TYPE stype = _defineStructureType(inSpec);
 
       if (stype.equals(STRUCTURE_TYPE.Unknown))
          throw new InvalidSettingsException("can not define structure type: reaction or molecule columns");
       
       if (_settings.newColName.getStringValue() == null)
          _settings.newColName.setStringValue(_settings.colName.getStringValue() + " (fingerprint)");
       
       /*
        * Set loading parameters warning message
        */
       if(_settings.warningMessage != null) {
          setWarningMessage(_settings.warningMessage);
       }
       
       return new DataTableSpec[] { getDataTableSpec(inSpec) };
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
       IndigoMoleculeFingerprinterSettings s = new IndigoMoleculeFingerprinterSettings();
       s.loadSettingsFrom(settings);
       
       if (s.fpSizeQWords.getIntValue() < 1)
          throw new InvalidSettingsException("fingerprint size must be a positive integer");
       if (s.colName.getStringValue() == null || s.colName.getStringValue().equals(""))
          throw new InvalidSettingsException("No column name given");
       if (s.newColName.getStringValue() == null || s.newColName.getStringValue().equals(""))
          throw new InvalidSettingsException("No name for new column given");
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
