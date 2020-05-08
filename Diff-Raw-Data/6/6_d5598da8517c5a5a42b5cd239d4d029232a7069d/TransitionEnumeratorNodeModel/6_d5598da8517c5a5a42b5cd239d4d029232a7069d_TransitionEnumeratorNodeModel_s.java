 package org.knime.knip.tracking.nodes.transition.transitionEnumerator;
 
 import java.io.File;
 import java.io.IOException;
 
 import net.imglib2.meta.ImgPlus;
 import net.imglib2.type.NativeType;
 import net.imglib2.type.numeric.IntegerType;
 
 import org.knime.core.data.DataCell;
 import org.knime.core.data.DataRow;
 import org.knime.core.data.DataTableSpec;
 import org.knime.core.data.container.DataContainer;
 import org.knime.core.data.def.DefaultRow;
 import org.knime.core.data.def.DoubleCell;
 import org.knime.core.data.def.StringCell;
 import org.knime.core.node.BufferedDataTable;
 import org.knime.core.node.CanceledExecutionException;
 import org.knime.core.node.ExecutionContext;
 import org.knime.core.node.ExecutionMonitor;
 import org.knime.core.node.InvalidSettingsException;
 import org.knime.core.node.NodeModel;
 import org.knime.core.node.NodeSettingsRO;
 import org.knime.core.node.NodeSettingsWO;
 import org.knime.core.node.defaultnodesettings.SettingsModelString;
 import org.knime.knip.base.data.img.ImgPlusCellFactory;
 import org.knime.knip.base.data.img.ImgPlusValue;
 import org.knime.knip.base.node.NodeUtils;
 import org.knime.knip.tracking.data.features.FeatureProvider;
 import org.knime.knip.tracking.data.graph.TransitionGraph;
 import org.knime.knip.tracking.data.graph.renderer.TransitionGraphRenderer;
 import org.knime.network.core.knime.cell.GraphCellFactory;
 import org.knime.network.core.knime.cell.GraphValue;
 
 /**
  * This is the model implementation of TransitionEnumerator. Enumerates all
  * possible transitions of a given transition graph.
  * 
  * @author Stephan Sellien
  */
 public class TransitionEnumeratorNodeModel<T extends NativeType<T> & IntegerType<T>>
 		extends NodeModel {
 
 	/**
 	 * Constructor for the node model.
 	 */
 	protected TransitionEnumeratorNodeModel() {
 		super(2, 1);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	protected BufferedDataTable[] execute(final BufferedDataTable[] inData,
 			final ExecutionContext exec) throws Exception {
 
 		DataContainer cont = exec.createDataContainer(inData[0]
 				.getDataTableSpec());
 
 		int graphColIdx = NodeUtils.autoColumnSelection(inData[0]
 				.getDataTableSpec(), new SettingsModelString("bla", null),
 				GraphValue.class, this.getClass(), new Integer[0]);
 
 		if (inData[1].getRowCount() < 1) {
 			throw new InvalidSettingsException(
 					"Input table #2 must contain the original image.");
 		}
 
 		@SuppressWarnings("unchecked")
 		ImgPlus<T> baseImg = ((ImgPlusValue<T>) inData[1].iterator().next()
 				.getCell(0)).getImgPlus();
 
 		for (DataRow row : inData[0]) {
 			TransitionGraph tg = new TransitionGraph(
 					((GraphValue) row.getCell(graphColIdx)).getView());
 			int variantCounter = 0;
 			for (TransitionGraph tgVariant : TransitionGraph
 					.createAllPossibleGraphs(tg)) {
 				DataCell[] cells = new DataCell[cont.getTableSpec()
 						.getNumColumns()];
 				cells[0] = new ImgPlusCellFactory(exec)
 						.createCell(TransitionGraphRenderer
 								.renderTransitionGraph(tg, baseImg, tgVariant));
 				cells[1] = new StringCell(tgVariant.toString());
 				cells[2] = new StringCell(tgVariant.toNodeString());
 				cells[3] = GraphCellFactory.createCell(tgVariant.getNet());
 				double[] distVec = FeatureProvider.getFeatureVector(tgVariant);
 				for (int i = 0; i < distVec.length; i++) {
 					cells[i + 4] = new DoubleCell(distVec[i]);
 				}
 				cells[cells.length - 1] = row.getCell(row.getNumCells() - 1);
 				cont.addRowToTable(new DefaultRow(row.getKey() + ";"
 						+ variantCounter, cells));
 				variantCounter++;
 			}
 		}
 
 		cont.close();
 
 		return new BufferedDataTable[] { exec.createBufferedDataTable(
 				cont.getTable(), exec) };
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	protected void reset() {
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	protected DataTableSpec[] configure(final DataTableSpec[] inSpecs)
 			throws InvalidSettingsException {
 		if (!inSpecs[0].containsCompatibleType(GraphValue.class))
 			throw new InvalidSettingsException(
 					"Input table #1 must contain a transition graph column");
 		if (!inSpecs[1].containsCompatibleType(ImgPlusValue.class))
 			throw new InvalidSettingsException(
 					"Input table #2 must contain the original image.");
 
 		return new DataTableSpec[] { inSpecs[0] };
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	protected void saveSettingsTo(final NodeSettingsWO settings) {
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	protected void loadValidatedSettingsFrom(final NodeSettingsRO settings)
 			throws InvalidSettingsException {
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	protected void validateSettings(final NodeSettingsRO settings)
 			throws InvalidSettingsException {
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	protected void loadInternals(final File internDir,
 			final ExecutionMonitor exec) throws IOException,
 			CanceledExecutionException {
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	protected void saveInternals(final File internDir,
 			final ExecutionMonitor exec) throws IOException,
 			CanceledExecutionException {
 	}
 
 }
