 /*  RapidMiner Integration for KNIME
  *  Copyright (C) 2012 Mind Eratosthenes Kft.
  *
  *  This program is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU Affero General Public License as
  *  published by the Free Software Foundation, either version 3 of the
  *  License, or (at your option) any later version.
  *
  *  This program is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU Affero General Public License for more details.
  *
  *  You should have received a copy of the GNU Affero General Public License
  *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package com.mind_era.knime_rapidminer.knime.nodes;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.AbstractMap;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map.Entry;
 import java.util.Set;
 import java.util.concurrent.ArrayBlockingQueue;
 import java.util.concurrent.Callable;
 import java.util.concurrent.Future;
 import java.util.concurrent.ThreadPoolExecutor;
 import java.util.concurrent.TimeUnit;
 
 import javax.annotation.Nullable;
 
 import org.knime.core.data.DataCell;
 import org.knime.core.data.DataColumnDomainCreator;
 import org.knime.core.data.DataColumnSpec;
 import org.knime.core.data.DataColumnSpecCreator;
 import org.knime.core.data.DataTableSpec;
 import org.knime.core.data.container.WrappedTable;
 import org.knime.core.data.date.DateAndTimeCell;
 import org.knime.core.data.def.DefaultRow;
 import org.knime.core.data.def.DoubleCell;
 import org.knime.core.data.def.IntCell;
 import org.knime.core.data.def.StringCell;
 import org.knime.core.node.BufferedDataContainer;
 import org.knime.core.node.BufferedDataTable;
 import org.knime.core.node.CanceledExecutionException;
 import org.knime.core.node.ExecutionContext;
 import org.knime.core.node.ExecutionMonitor;
 import org.knime.core.node.InvalidSettingsException;
 import org.knime.core.node.NodeLogger;
 import org.knime.core.node.NodeModel;
 import org.knime.core.node.NodeSettingsRO;
 import org.knime.core.node.NodeSettingsWO;
 import org.knime.core.node.defaultnodesettings.DialogComponentRapidMinerProject;
 import org.knime.core.node.defaultnodesettings.HasTableSpecAndRowId;
 import org.knime.core.node.defaultnodesettings.SettingsModelRapidMinerProject;
 import org.knime.core.node.defaultnodesettings.SettingsModelString;
 import org.knime.core.node.port.PortObject;
 import org.knime.core.node.port.PortObjectSpec;
 import org.knime.core.node.port.PortType;
 
 import com.google.common.base.Function;
 import com.google.common.base.Predicate;
 import com.google.common.collect.Collections2;
 import com.google.common.collect.Iterables;
 import com.google.common.collect.Lists;
 import com.google.common.collect.Sets;
 import com.mind_era.knime_rapidminer.knime.nodes.util.KnimeExampleTable;
 import com.rapidminer.Process;
 import com.rapidminer.example.Attribute;
 import com.rapidminer.example.ExampleSet;
 import com.rapidminer.example.table.DataRow;
 import com.rapidminer.example.table.ExampleTable;
 import com.rapidminer.example.table.MemoryExampleTable;
 import com.rapidminer.operator.IOContainer;
 import com.rapidminer.operator.Operator;
 import com.rapidminer.operator.ports.metadata.AttributeMetaData;
 import com.rapidminer.operator.ports.metadata.ExampleSetMetaData;
 import com.rapidminer.operator.ports.metadata.MetaData;
 import com.rapidminer.repository.RepositoryAccessor;
 import com.rapidminer.tools.Ontology;
 import com.rapidminer.tools.math.container.Range;
 
 /**
  * This is the model implementation of RapidMiner. Executes a RapidMiner
  * workflow.
  * 
  * @author Gabor Bakos
  */
 public class RapidMinerNodeModel extends NodeModel implements
 		HasTableSpecAndRowId, RepositoryAccessor {
 
 	// the logger instance
 	private static final NodeLogger logger = NodeLogger
 			.getLogger(RapidMinerNodeModel.class);
 
 	private static final PortType OptionalBufferedDataTableType = new PortType(
 			BufferedDataTable.class, true);
 
 	/**
 	 * Constructor for the node model.
 	 */
 	protected RapidMinerNodeModel() {
 		super(new PortType[] { OptionalBufferedDataTableType,
 				OptionalBufferedDataTableType, OptionalBufferedDataTableType,
 				OptionalBufferedDataTableType }, new PortType[] {
 				OptionalBufferedDataTableType, OptionalBufferedDataTableType,
 				OptionalBufferedDataTableType, OptionalBufferedDataTableType });
 	}
 
 	static final String CFGKEY_PROCESS_CUSTOM = "Process custom";
 	static final String DEFAULT_PROCESS_CUSTOM = null;
 	static final boolean DEFAULT_EDITABILITY = true;
 	static final boolean DEFAULT_SNAPSHOT = true;
 	static final byte[] DEFAULT_CONTENT = null;
 
 	static final String CFGKEY_ROWID_COLUMN_NAME = "RowID column name";
 	static final String DEFAULT_ROWID_COLUMN_NAME = "__KNIME\u00a0RowID__";
 	static final boolean DEFAULT_ENABLED_ROWID_COLUMN_NAME = true;
 
 	private SettingsModelRapidMinerProject processModel = new SettingsModelRapidMinerProject(
 			CFGKEY_PROCESS_CUSTOM, DEFAULT_PROCESS_CUSTOM, DEFAULT_EDITABILITY,
 			DEFAULT_SNAPSHOT, DEFAULT_CONTENT);
 	private SettingsModelString rowIdColumnName = new SettingsModelString(
 			CFGKEY_ROWID_COLUMN_NAME, DEFAULT_ROWID_COLUMN_NAME);
 
 	private DataTableSpec[] lastTableSpecs;
 	private DataTableSpec[] lastResultTableSpecs;
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	protected PortObject[] execute(final PortObject[] inData,
 			final ExecutionContext exec) throws Exception {
 		RapidMinerInit.init(false);
 		RapidMinerInit.setPreferences();
 		final Process process = processModel.loadProject(false);
 		final ThreadPoolExecutor executor = new ThreadPoolExecutor(1, 1, 1,
 				TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(1));
 		final Future<IOContainer> future = executor
 				.submit(new Callable<IOContainer>() {
 					@Override
 					public IOContainer call() throws Exception {
 						return process.run(new IOContainer(Iterables.toArray(
 								Collections2.transform(Collections2.filter(
 										Arrays.asList(inData),
 										new Predicate<PortObject>() {
 											@Override
 											public boolean apply(
 													final PortObject input) {
 												return input != null;
 											}
 										}),
 										new Function<PortObject, ExampleSet>() {
 											@Override
 											public ExampleSet apply(
 													final PortObject input) {
 												return MemoryExampleTable
 														.createCompleteCopy(
 																/*
 																 * new
 																 * SimpleExampleSet
 																 * (
 																 */new KnimeExampleTable(
 																		new WrappedTable(
 																				(BufferedDataTable) input),
 																		rowIdColumnName
 																				.isEnabled(),
 																		rowIdColumnName
 																				.getStringValue())
 														).createExampleSet();
 											}
 										}), ExampleSet.class)));
 					}
 				});
 		while (!future.isDone()) {
 			Thread.sleep(500);
 			try {
 				exec.checkCanceled();
 				Operator currentOperator = process == null ? null : process
 						.getCurrentOperator();
 				if (currentOperator != null) {
 					exec.setProgress("Operator: " + currentOperator.getName());
 				} else {
 					exec.setProgress("");
 				}
 			} catch (final CanceledExecutionException e) {
 				process.stop();
 				Thread.sleep(200);
 				future.cancel(true);
 				throw e;
 			}
 		}
 		final IOContainer container = future.get();
 		final ArrayList<BufferedDataTable> ret = new ArrayList<BufferedDataTable>();
 		for (int resultIndex = 0; resultIndex < container.size(); ++resultIndex) {
 			logger.debug("Converting the " + (resultIndex + 1)
 					+ "th result table.");
 			final ExampleSet result = container.get(ExampleSet.class,
 					resultIndex);
 			ret.add(convertExampleSet(
 					exec,
 					result,
 					rowIdColumnName.isEnabled(),
 					rowIdColumnName.getStringValue(),
 					lastResultTableSpecs != null
 							&& lastResultTableSpecs.length > resultIndex ? lastResultTableSpecs[resultIndex]
 							: null));
 		}
 		if (ret.size() > getNrOutPorts()) {
 			logger.warn("The last " + (ret.size() - getNrOutPorts())
 					+ " output were discarded, only the first "
 					+ getNrOutPorts() + " exampleset were returned.");
 		}
 		for (int i = ret.size(); i-- > getNrOutPorts();) {
 			ret.remove(i);
 		}
 		for (int i = getNrOutPorts() - ret.size(); i-- > 0;) {
 			final BufferedDataContainer c = exec
 					.createDataContainer(new DataTableSpec());
 			c.close();
 			ret.add(c.getTable());
 		}
 		return ret.toArray(new BufferedDataTable[ret.size()]);
 	}
 
 	/**
 	 * Converts {@code result} to a {@link BufferedDataTable}.
 	 * 
 	 * @param exec
 	 *            A KNIME {@link ExecutionContext}.
 	 * @param result
 	 *            The {@link ExampleSet} to convert.
 	 * @param withRowIds
 	 *            If set, the {@code rowIdColumnName} attribute will be used
 	 * @param rowIdColName
 	 *            This will be used as row id input column from RapidMiner. Can
 	 *            be {@code null} if not {@code withRowIds}.
 	 * @param referenceTableSpec
 	 *            The reference table specification.
 	 * @return The converted {@link BufferedDataTable}.
 	 * @throws CanceledExecutionException
 	 *             If execution has been cancelled.
 	 */
 	private BufferedDataTable convertExampleSet(final ExecutionContext exec,
 			final ExampleSet result, final boolean withRowIds,
 			final @Nullable String rowIdColName,
 			final DataTableSpec referenceTableSpec)
 			throws CanceledExecutionException {
 		final BufferedDataContainer dataContainer = exec
 				.createDataContainer(createSpec(result, withRowIds,
 						rowIdColName, referenceTableSpec));
 		try {
 			int i = 0;
 			final Entry<? extends Iterable<Attribute>, Attribute> attribsEntry = selectAttributes(
 					result, withRowIds, rowIdColName, referenceTableSpec);
 			final Iterable<Attribute> attribs = attribsEntry.getKey();
 			// final Iterator<Attribute> attributes =
 			final ExampleTable exampleTable = result.getExampleTable();
 			for (final Iterator<DataRow> it = exampleTable.getDataRowReader(); it
 					.hasNext();) {
 				final DataRow row = it.next();
 				++i;
 				if (i > result.size()) {
 					break;
 				}
 				final Function<Attribute, DataCell> transformFunction = new Function<Attribute, DataCell>() {
 					@Override
 					public DataCell apply(final Attribute a) {
 						return a.isNominal() ? new StringCell(a.getMapping()
 								.mapIndex((int) row.get(a)))
 								: a.getValueType() == Ontology.INTEGER ? new IntCell(
 										(int) row.get(a)) : new DoubleCell(
 										row.get(a));
 					}
 				};
 				dataContainer.addRowToTable(new DefaultRow(attribsEntry
 						.getValue() == null ? String.valueOf(i)
 						: ((org.knime.core.data.StringValue) transformFunction
 								.apply(attribsEntry.getValue()))
 								.getStringValue(), Iterables.toArray(
 						Iterables.transform(attribs, transformFunction),
 						DataCell.class)));
 				exec.checkCanceled();
 			}
 		} finally {
 			dataContainer.close();
 		}
 		return dataContainer.getTable();
 	}
 
 	/**
 	 * Filters the {@link Attribute}s of the {@code exampleSet} based on the
 	 * rowId related parameters.
 	 * 
 	 * @param exampleSet
 	 *            The input {@link ExampleSet}.
 	 * @param withRowIds
 	 *            If set, the {@code rowIdColumnName} attribute will be used
 	 * @param rowIdColName
 	 *            This will be used as row id input column from RapidMiner. Can
 	 *            be {@code null} if not {@code withRowIds}.
 	 * @param referenceTableSpec
 	 *            The reference table specification to specify the order of
 	 *            columns already set.
 	 * @return The {@link Attribute}s applicable, and the rowId attribute.
 	 */
 	public Entry<? extends Iterable<Attribute>, Attribute> selectAttributes(
 			final ExampleSet exampleSet, final boolean withRowIds,
 			final @Nullable String rowIdColName,
 			final DataTableSpec referenceTableSpec) {
 		final Iterator<Attribute> attribs = exampleSet.getAttributes()
 				.allAttributes();// .filter(_.isNumerical)*/.toSeq
 		final List<Attribute> attribList = Lists.newArrayList(attribs);
 		if (referenceTableSpec != null) {
 			for (int i = 0; i < referenceTableSpec.getNumColumns(); ++i) {
 				final String refName = referenceTableSpec.getColumnSpec(i)
 						.getName();
 				if (!attribList.get(i).getName().equals(refName)) {
 					int foundIndex = -1;
 					for (int j = 0; j < attribList.size(); ++j) {
 						if (attribList.get(j).getName().equals(refName)) {
 							foundIndex = j;
 							break;
 						}
 					}
 					if (foundIndex != -1) {
 						final Attribute attribute = attribList
 								.remove(foundIndex);
 						attribList.add(i, attribute);
 					}
 				}
 			}
 		}
 		if (withRowIds) {
 			Attribute a = null;
 			for (final Iterator<Attribute> it = attribList.iterator(); it
 					.hasNext();) {
 				final Attribute attribute = it.next();
 				if (attribute.getName().equals(rowIdColName)) {
 					it.remove();
 					a = attribute;
 					break;
 				}
 			}
 			return new AbstractMap.SimpleImmutableEntry<List<Attribute>, Attribute>(
 					attribList, a);
 		}
 		return new AbstractMap.SimpleImmutableEntry<Iterable<Attribute>, Attribute>(
 				attribList, null);
 	}
 
 	/**
 	 * Filters the {@link AttributeMetaData} based on the rowId related
 	 * parameters.
 	 * 
 	 * @param attribMetaData
 	 *            The input {@link ExampleSet}.
 	 * @param withRowIds
 	 *            If set, the {@code rowIdColumnName} attribute will be used
 	 * @param rowIdColName
 	 *            This will be used as row id input column from RapidMiner. Can
 	 *            be {@code null} if not {@code withRowIds}.
 	 * @return The {@link Attribute}s applicable, and the rowId attribute.
 	 */
 	public Collection<AttributeMetaData> selectAttributeMetaData(
 			final Collection<AttributeMetaData> attribMetaData,
 			final boolean withRowIds, final @Nullable String rowIdColName) {
 		final List<AttributeMetaData> attribList = Lists
 				.newArrayList(attribMetaData);
 		// .filter(_.isNumerical)*/.toSeq
 		if (withRowIds) {
 			for (final Iterator<AttributeMetaData> it = attribList.iterator(); it
 					.hasNext();) {
 				final AttributeMetaData attribute = it.next();
 				if (attribute.getName().equals(rowIdColName)) {
 					it.remove();
 					break;
 				}
 			}
 		}
 		return attribList;
 	}
 
 	/**
 	 * Creates the {@link DataTableSpec} based on the {@code examples}.
 	 * 
 	 * @param examples
 	 *            The {@link ExampleSet}.
 	 * @param withRowIds
 	 *            Used to find the rowId {@link Attribute} if available.
 	 * @param rowIdColumn
 	 *            The name of the rowId, can be {@code null}.
 	 * @param referenceTableSpec
 	 *            The reference table specification.
 	 * @return The new {@link DataTableSpec}.
 	 * @see #selectAttributes(ExampleSet, boolean, String, DataTableSpec)
 	 */
 	private DataTableSpec createSpec(final ExampleSet examples,
 			final boolean withRowIds, final @Nullable String rowIdColumn,
 			final DataTableSpec referenceTableSpec) {
 		final Entry<? extends Iterable<Attribute>, Attribute> attribsEntry = selectAttributes(
 				examples, withRowIds, rowIdColumn, referenceTableSpec);
 		return new DataTableSpec(Iterables.toArray(Iterables.transform(
 				attribsEntry.getKey(),
 				new Function<Attribute, DataColumnSpec>() {
 					@Override
 					public DataColumnSpec apply(final Attribute a) {
 						return new DataColumnSpecCreator(
 								a.getName(),
 								a.isNominal() ? StringCell.TYPE
 										: a.getValueType() == Ontology.INTEGER ? IntCell.TYPE
 												: DoubleCell.TYPE).createSpec();
 					}
 				}), DataColumnSpec.class));
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	protected void reset() {
 		// Models build during execute are cleared here.
 		// Also data handled in load/saveInternals will be erased here.
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	protected DataTableSpec[] configure(final DataTableSpec[] inSpecs)
 			throws InvalidSettingsException {
 		lastTableSpecs = inSpecs == null ? null : inSpecs.clone();
 		if (inSpecs != null) {
 			try {
 				final Process process = processModel.loadProject(false);
 				process.setRepositoryAccessor(this);
 				final ArrayList<ExampleSet> args = Lists
 						.newArrayList(Collections2.transform(
 								Collections2.filter(Arrays.asList(inSpecs),
 										new Predicate<PortObjectSpec>() {
 											@Override
 											public boolean apply(
 													final PortObjectSpec input) {
 												return input != null;
 											}
 										}),
 								new Function<DataTableSpec, ExampleSet>() {
 
 									@Override
 									public ExampleSet apply(
 											final DataTableSpec input) {
 										return new MemoryExampleTable(
 												KnimeExampleTable
 														.createAttributes(
 																input,
 																rowIdColumnName
 																		.isEnabled(),
 																rowIdColumnName
 																		.getStringValue()))
 												.createExampleSet();
 									}
 								}));
 				if (!process.checkProcess(new IOContainer(args))) {
 					logger.warn("Problem with the process.");
 				}
 				process.getContext().setInputRepositoryLocations(
 						DialogComponentRapidMinerProject.generateLocations(
 								process, this));
 				process.getRootOperator().checkAll();
 
 				logger.warn(process.getRootOperator().getErrorList());
 				final List<MetaData> resultMetaData = process.getRootOperator()
 						.getResultMetaData();
 				final ArrayList<DataTableSpec> resultList = Lists
 						.newArrayList(Lists.transform(resultMetaData,
 								new Function<MetaData, DataTableSpec>() {
 									@Override
 									public DataTableSpec apply(
 											final MetaData input) {
 										if (input instanceof ExampleSetMetaData) {
 											final ExampleSetMetaData esmd = (ExampleSetMetaData) input;
 											return new DataTableSpec(
 													Lists.newArrayList(
 															Collections2
 																	.transform(
 																			selectAttributeMetaData(
 																					esmd.getAllAttributes(),
 																					isWithRowIds(),
 																					getRowIdColumnName()),
 																			new Function<AttributeMetaData, DataColumnSpec>() {
 																				@Override
 																				public DataColumnSpec apply(
 																						final AttributeMetaData amd) {
 																					switch (amd
 																							.getValueType()) {
 																					case Ontology.INTEGER:
 																						final DataColumnSpecCreator intSpecCreator = new DataColumnSpecCreator(
 																								amd.getName(),
 																								IntCell.TYPE);
 																						final Range intRange = amd
 																								.getValueRange();
 																						if (!(Double
 																								.isNaN(intRange
 																										.getLower()) || Double
 																								.isNaN(intRange
 																										.getUpper()))) {
 																							intSpecCreator
 																									.setDomain(new DataColumnDomainCreator(
 																											new IntCell(
 																													(int) intRange
 																															.getLower()),
 																											new IntCell(
 																													(int) intRange
 																															.getUpper()))
 																											.createDomain());
 																						}
 																						return intSpecCreator
 																								.createSpec();
 																					case Ontology.NUMERICAL:
 																					case Ontology.REAL:
 																						final DataColumnSpecCreator doubleSpecCreator = new DataColumnSpecCreator(
 																								amd.getName(),
 																								DoubleCell.TYPE);
 																						final Range doubleRange = amd
 																								.getValueRange();
 																						if (!(Double
 																								.isNaN(doubleRange
 																										.getLower()) || Double
 																								.isNaN(doubleRange
 																										.getUpper()))) {
 																							doubleSpecCreator
 																									.setDomain(new DataColumnDomainCreator(
 																											new DoubleCell(
 																													doubleRange
 																															.getLower()),
 																											new DoubleCell(
 																													doubleRange
 																															.getUpper()))
 																											.createDomain());
 																						}
 																						return doubleSpecCreator
 																								.createSpec();
 																					case Ontology.STRING:
 																					case Ontology.NOMINAL:
 																					case Ontology.BINOMINAL:
 																					case Ontology.POLYNOMINAL:
 																						final DataColumnSpecCreator stringSpecCreator = new DataColumnSpecCreator(
 																								amd.getName(),
 																								StringCell.TYPE);
 																						final Set<String> possValues = amd
 																								.getValueSet();
 																						if (possValues != null
 																								&& !(isWithRowIds() && amd
 																										.getName()
 																										.equals(getRowIdColumnName()))) {
 																							stringSpecCreator
 																									.setDomain(new DataColumnDomainCreator(
 																											toCells(possValues))
 																											.createDomain());
 																						}
 																						return stringSpecCreator
 																								.createSpec();
 																					case Ontology.DATE:
 																					case Ontology.DATE_TIME:
 																					case Ontology.TIME:
 																						return new DataColumnSpecCreator(
 																								amd.getName(),
 																								DateAndTimeCell.TYPE)
 																								.createSpec();
 																					default:
 																						throw new UnsupportedOperationException(
 																								"Not supported value type: "
 																										+ amd.getValueType());
 																					}
 																				}
 																			}))
 															.toArray(
 																	new DataColumnSpec[0]));
 										}
 										// Not supported metadata.
										throw new UnsupportedOperationException("Not supported result format" + input.getClass());
 									}
 								}));
 				if (resultList.size() > getNrOutPorts()) {
 					for (int i = resultList.size() - getNrOutPorts(); i-- > 0;) {
 						resultList.remove(i);
 					}
 				}
 				if (resultList.size() < getNrOutPorts()) {
 					for (int i = getNrOutPorts() - resultList.size(); i-- > 0;) {
 						resultList.add(new DataTableSpec());
 					}
 				}
 				return lastResultTableSpecs = resultList
 						.toArray(new DataTableSpec[0]);
 			} catch (final Exception e) {
 				lastResultTableSpecs = null;
 				throw new InvalidSettingsException(e);
 			}
 		}
 		return lastResultTableSpecs = null;
 	}
 
 	private static Set<DataCell> toCells(final Set<String> possValues) {
 		final Set<DataCell> cells = Sets.newHashSet();
 		for (final String value : possValues) {
 			cells.add(new StringCell(value));
 		}
 		return cells;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	protected void saveSettingsTo(final NodeSettingsWO settings) {
 		// processFile.saveSettingsTo(settings);
 		processModel.saveSettingsTo(settings);
 		rowIdColumnName.saveSettingsTo(settings);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	protected void loadValidatedSettingsFrom(final NodeSettingsRO settings)
 			throws InvalidSettingsException {
 		// processFile.loadSettingsFrom(settings);
 		processModel.loadSettingsFrom(settings);
 		rowIdColumnName.loadSettingsFrom(settings);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	protected void validateSettings(final NodeSettingsRO settings)
 			throws InvalidSettingsException {
 		// processFile.validateSettings(settings);
 		processModel.validateSettings(settings);
 		rowIdColumnName.validateSettings(settings);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	protected void loadInternals(final File internDir,
 			final ExecutionMonitor exec) throws IOException,
 			CanceledExecutionException {
 		// Everything handed to output ports is loaded automatically (data
 		// returned by the execute method, models loaded in loadModelContent,
 		// and user settings set through loadSettingsFrom - is all taken care
 		// of). Load here only the other internals that need to be restored
 		// (e.g. data used by the views).
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	protected void saveInternals(final File internDir,
 			final ExecutionMonitor exec) throws IOException,
 			CanceledExecutionException {
 		// Everything written to output ports is saved automatically (data
 		// returned by the execute method, models saved in the saveModelContent,
 		// and user settings saved through saveSettingsTo - is all taken care
 		// of). Save here only the other internals that need to be preserved
 		// (e.g. data used by the views).
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.knime.core.node.defaultnodesettings.HasTableSpec#getFilteredTableSpecs
 	 * ()
 	 */
 	@Override
 	public List<? extends DataTableSpec> getFilteredTableSpecs() {
 		if (lastTableSpecs == null) {
 			return Collections.emptyList();
 		}
 		final Collection<DataTableSpec> filtered = Collections2.filter(
 				Arrays.asList(lastTableSpecs), new Predicate<PortObjectSpec>() {
 
 					@Override
 					public boolean apply(final PortObjectSpec input) {
 						return input != null && input instanceof DataTableSpec;
 					}
 				});
 		return new ArrayList<DataTableSpec>(filtered);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.knime.core.node.defaultnodesettings.HasTableSpecAndRowId#
 	 * getRowIdColumnName()
 	 */
 	@Override
 	public String getRowIdColumnName() {
 		return rowIdColumnName.getStringValue();
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.knime.core.node.defaultnodesettings.HasTableSpecAndRowId#isWithRowIds
 	 * ()
 	 */
 	@Override
 	public boolean isWithRowIds() {
 		return rowIdColumnName.isEnabled();
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see java.lang.Object#hashCode()
 	 */
 	@Override
 	public int hashCode() {
 		return 1;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see java.lang.Object#equals(java.lang.Object)
 	 */
 	@Override
 	public boolean equals(final Object obj) {
 		if (this == obj) {
 			return true;
 		}
 		return false;
 	}
 }
