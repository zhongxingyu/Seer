 /***********************************************************************************************************************
  *
  * Copyright (C) 2010-2013 by the Stratosphere project (http://stratosphere.eu)
  *
  * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
  * the License. You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
  * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
  * specific language governing permissions and limitations under the License.
  *
  **********************************************************************************************************************/
 
 package eu.stratosphere.pact.testing;
 
 import java.io.Closeable;
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.IdentityHashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 
 import junit.framework.Assert;
 import eu.stratosphere.nephele.configuration.Configuration;
 import eu.stratosphere.nephele.configuration.GlobalConfiguration;
 import eu.stratosphere.nephele.fs.FileStatus;
 import eu.stratosphere.nephele.fs.FileSystem;
 import eu.stratosphere.nephele.fs.Path;
 import eu.stratosphere.nephele.types.Record;
 import eu.stratosphere.nephele.util.StringUtils;
 import eu.stratosphere.pact.client.LocalExecutor;
 import eu.stratosphere.pact.common.contract.FileDataSink;
 import eu.stratosphere.pact.common.contract.FileDataSource;
 import eu.stratosphere.pact.common.contract.GenericDataSink;
 import eu.stratosphere.pact.common.contract.GenericDataSource;
 import eu.stratosphere.pact.common.plan.Plan;
 import eu.stratosphere.pact.common.util.PactConfigConstants;
 import eu.stratosphere.pact.common.util.Visitor;
 import eu.stratosphere.pact.generic.contract.Contract;
 import eu.stratosphere.pact.generic.io.FileInputFormat;
 import eu.stratosphere.pact.generic.io.FileOutputFormat;
 import eu.stratosphere.pact.generic.io.GenericInputFormat;
 import eu.stratosphere.pact.generic.io.SequentialInputFormat;
 import eu.stratosphere.pact.generic.io.SequentialOutputFormat;
 
 /**
  * The primary resource to test one or more implemented PACT stubs. It is
  * created in a unit tests and performs the following operations.
  * <ul>
  * <li>Adds {@link GenericDataSource<?>}s and {@link GenericDataSink}s if not explicitly specified,
  * <li>locally runs the PACT stubs,
  * <li>checks the results against the pairs as specified in {@link #getExpectedOutput()}, and
  * <li>provides comfortable access to the results with {@link #getActualOutput()}. <br>
  * </ul>
  * <br>
  * The typical usage is inside a unit test. And might look like one of the
  * following examples. <br>
  * <br>
  * <b>Test complete plan<br>
  * <code><pre>
  *    // build plan
  *    GenericDataSource<?>&lt;Key, Value&gt; source = ...;
  *    MapContract&lt;Key, Value, Key, Value&gt; map = new MapContract&lt;Key, Value, Key, Value&gt;(IdentityMap.class, "Map");
  *    map.setInput(source);    
  *    GenericDataSink&lt;Key, Value&gt; output = ...;
  *    output.setInput(map);
  *    // configure test
  *    TestPlan testPlan = new TestPlan(output);
  *    testPlan.getExpectedOutput(output).fromFile(...);
  *    testPlan.run();
  * </pre></code> <b>Test plan with ad-hoc source and sink<br>
  * <code><pre>
  *    // build plan
  *    MapContract&lt;Key, Value, Key, Value&gt; map = new MapContract&lt;Key, Value, Key, Value&gt;(IdentityMap.class, "Map");
  *    // configure test
  *    TestPlan testPlan = new TestPlan(map);
  *    testPlan.getInput().add(pair1).add(pair2).add(pair3);
  *    testPlan.getExpectedOutput(output).add(pair1).add(pair2).add(pair3);
  *    testPlan.run();
  * </pre></code> <b>Access ad-hoc source and sink of Testplan<br>
  * <code><pre>
  *    // build plan
  *    MapContract&lt;Key, Value, Key, Value&gt; map = new MapContract&lt;Key, Value, Key, Value&gt;(IdentityMap.class, "Map");
  *    // configure test
  *    TestPlan testPlan = new TestPlan(map);
  *    testPlan.getInput().add(randomInput1).add(randomInput2).add(randomInput3);
  *    testPlan.run();
  *    // custom assertions
  *    Assert.assertEquals(testPlan.getInput(), testPlan.getOutput());
  * </pre></code> <br>
  * 
  * @author Arvid Heise
  */
 
 public abstract class GenericTestPlan<T extends Record, Records extends GenericTestRecords<T>> implements Closeable {
 
 	private final Map<GenericDataSink, Records> actualOutputs =
 		new IdentityHashMap<GenericDataSink, Records>();
 
 	private final Contract[] contracts;
 
 	private int degreeOfParallelism = 1;
 
 	private final Map<GenericDataSink, Records> expectedOutputs =
 		new IdentityHashMap<GenericDataSink, Records>();
 
 	private final Map<GenericDataSource<?>, Records> inputs =
 		new IdentityHashMap<GenericDataSource<?>, Records>();
 
 	private final List<GenericDataSink> sinks = new ArrayList<GenericDataSink>();
 
 	private final List<GenericDataSource<?>> sources = new ArrayList<GenericDataSource<?>>();
 
 	private TypeConfig<T> defaultConfig;
 
 	/**
 	 * Initializes TestPlan with the given {@link Contract}s. Like the original {@link Plan}, the contracts may be
 	 * {@link GenericDataSink}s. However, it
 	 * is also possible to add arbitrary Contracts, to which GenericDataSinkContracts
 	 * are automatically added.
 	 * 
 	 * @param contracts
 	 *        a list of Contracts with at least one element.
 	 */
 	public GenericTestPlan(final Collection<? extends Contract> contracts) {
 		this(contracts.toArray(new Contract[contracts.size()]));
 	}
 
 	/**
 	 * Initializes TestPlan with the given {@link Contract}s. Like the original {@link Plan}, the contracts may be
 	 * {@link GenericDataSink}s. However, it
 	 * is also possible to add arbitrary Contracts, to which GenericDataSinkContracts
 	 * are automatically added.
 	 * 
 	 * @param contracts
 	 *        a list of Contracts with at least one element.
 	 */
 	public GenericTestPlan(final Contract... contracts) {
 		if (contracts.length == 0)
 			throw new IllegalArgumentException();
 
 		final Configuration config = new Configuration();
 		config.setString(PactConfigConstants.DEFAULT_INSTANCE_TYPE_KEY, "standard,1,1,200,1,1");
 		GlobalConfiguration.includeConfiguration(config);
 
 		this.contracts = new InputOutputAdder().process(contracts);
 
 		this.findSinksAndSources();
 		this.configureSinksAndSources();
 	}
 
 	@Override
 	public void close() throws IOException {
 		ClosableManager closableManager = new ClosableManager();
 
 		for (Records pairs : this.inputs.values())
 			closableManager.add(pairs);
 		for (Records pairs : this.actualOutputs.values())
 			closableManager.add(pairs);
 		for (Records pairs : this.expectedOutputs.values())
 			closableManager.add(pairs);
 
 		closableManager.close();
 	}
 
 	/**
 	 * Returns the first output {@link GenericTestRecords} of the TestPlan associated with the
 	 * given sink. This is the recommended method to get output records for more
 	 * complex TestPlans.<br>
 	 * The values are only meaningful after a {@link #run()}.
 	 * 
 	 * @return the output {@link GenericTestRecords} of the TestPlan associated with the
 	 *         first sink
 	 */
 	public Records getActualOutput() {
 		return this.getActualOutput(0);
 	}
 
 	/**
 	 * Returns the first output {@link GenericTestRecords} of the TestPlan associated with the
 	 * given sink. This is the recommended method to get output records for more
 	 * complex TestPlans.<br>
 	 * The values are only meaningful after a {@link #run()}.
 	 * 
 	 * @return the output {@link GenericTestRecords} of the TestPlan associated with the
 	 *         first sink
 	 */
 	public Records getActualOutput(final TypeConfig<T> typeConfig) {
 		return this.getActualOutput(0, typeConfig);
 	}
 
 	/**
 	 * Returns the output {@link GenericTestRecords} of the TestPlan associated with the
 	 * given sink. This is the recommended method to get output records for more
 	 * complex TestPlans.<br>
 	 * The values are only meaningful after a {@link #run()}.
 	 * 
 	 * @param sink
 	 *        the sink of which the associated output GenericTestRecords should be
 	 *        returned
 	 * @return the output {@link GenericTestRecords} of the TestPlan associated with the
 	 *         given sink
 	 */
 	public Records getActualOutput(final GenericDataSink sink) {
 		return this.getActualOutput(sink, this.defaultConfig);
 	}
 
 	/**
 	 * Returns the output {@link GenericTestRecords} of the TestPlan associated with the
 	 * <i>i</i>th sink.<br>
 	 * The values are only meaningful after a {@link #run()}.
 	 * 
 	 * @param sinkNumber
 	 *        the <i>i</i>th sink of which the associated output GenericTestRecords should be
 	 *        returned
 	 * @return the <i>i</i>th output of the TestPlan
 	 */
 	public Records getActualOutput(final int sinkNumber, final TypeConfig<T> typeConfig) {
 		return this.getActualOutput(this.sinks.get(sinkNumber), typeConfig);
 	}
 
 	/**
 	 * Returns the output {@link GenericTestRecords} of the TestPlan associated with the
 	 * given sink. This is the recommended method to get output records for more
 	 * complex TestPlans.<br>
 	 * The values are only meaningful after a {@link #run()}.
 	 * 
 	 * @param sink
 	 *        the sink of which the associated output GenericTestRecords should be
 	 *        returned
 	 * @return the output {@link GenericTestRecords} of the TestPlan associated with the
 	 *         given sink
 	 */
 	public Records getActualOutput(final GenericDataSink sink, final TypeConfig<T> typeConfig) {
 		Records values = this.actualOutputs.get(sink);
 		if (values == null)
 			this.actualOutputs.put(sink,
 				values = this.createTestRecords(typeConfig == null ? this.defaultConfig : typeConfig));
 		else if (values.getTypeConfig() == null)
 			values.setTypeConfig(typeConfig == null ? this.defaultConfig : typeConfig);
 		return values;
 	}
 
 	protected abstract Records createTestRecords(final TypeConfig<T> typeConfig);
 
 	/**
 	 * Returns the output {@link TestPairs} associated with the <i>i</i>th
 	 * output of the TestPlan. If multiple contracts are tested in the TestPlan,
 	 * it is recommended to use the {@link #getActualOutput(GenericDataSink)} method to unambiguously get the
 	 * values.<br>
 	 * The values are only meaningful after a {@link #run()}.
 	 * 
 	 * @param number
 	 *        the number of the output.
 	 * @return the <i>i</i>th output of the TestPlan
 	 */
 	public Records getActualOutput(final int number) {
 		return this.getActualOutput(this.getDataSinks().get(number));
 	}
 
 	/**
 	 * Returns the degreeOfParallelism.
 	 * 
 	 * @return the degreeOfParallelism
 	 */
 	public int getDegreeOfParallelism() {
 		return this.degreeOfParallelism;
 	}
 
 	/**
 	 * Returns the first expected output {@link TestPairs} of the TestPlan. If
 	 * multiple contracts are tested in the TestPlan, it is recommended to use
 	 * the {@link #getExpectedOutput(GenericDataSink)} method to unambiguously
 	 * set the values.
 	 * 
 	 * @return the first expected output of the TestPlan
 	 */
 	public Records getExpectedOutput(TypeConfig<T> typeConfig) {
 		return this.getExpectedOutput(0, typeConfig);
 	}
 
 	/**
 	 * Returns the expected output {@link GenericTestRecords} with the given {@link TypeConfig} of the TestPlan
 	 * associated with the given sink. This is the recommended method to set expected
 	 * output records for more complex TestPlans.
 	 * 
 	 * @param sink
 	 *        the sink of which the associated expected output GenericTestRecords
 	 *        should be returned
 	 * @param typeConfig
 	 *        the TypeConfig that should be used to create a new GenericTestRecords if needed
 	 * @return the expected output {@link GenericTestRecords} of the TestPlan associated
 	 *         with the given sink
 	 */
 	public Records getExpectedOutput(final GenericDataSink sink, TypeConfig<T> typeConfig) {
 		Records values = this.expectedOutputs.get(sink);
 		if (values == null) {
 			this.expectedOutputs.put(sink,
 				values = this.createTestRecords(typeConfig == null ? this.defaultConfig : typeConfig));
 			Records actualOutput = this.getActualOutput(sink);
 			actualOutput.setTypeConfig(typeConfig);
 		}
 		else if (values.getTypeConfig() == null)
 			values.setTypeConfig(typeConfig == null ? this.defaultConfig : typeConfig);
 		return values;
 	}
 
 	/**
 	 * Returns the expected output {@link TestPairs} associated with the
 	 * <i>i</i>th expected output of the TestPlan. If multiple contracts are
 	 * tested in the TestPlan, it is recommended to use the {@link #getExpectedOutput(GenericDataSink)} method to
 	 * unambiguously set
 	 * the values.
 	 * 
 	 * @param number
 	 *        the number of the expected output.
 	 * @return the <i>i</i>th expected output of the TestPlan
 	 */
 	public Records getExpectedOutput(final int number, TypeConfig<T> typeConfig) {
 		return this.getExpectedOutput(this.getDataSinks().get(number), typeConfig);
 	}
 
 	/**
 	 * Returns the first input {@link TestPairs} of the TestPlan. If multiple
 	 * contracts are tested in the TestPlan, it is recommended to use the {@link #getInput(GenericDataSource<?>)} method
 	 * to unambiguously set the
 	 * values.
 	 * 
 	 * @return the first input of the TestPlan
 	 */
 	public Records getInput() {
 		return this.getInput(0);
 	}
 
 	/**
 	 * Returns the input {@link GenericTestRecords} of the TestPlan associated with the
 	 * given source. This is the recommended method to set input records for more
 	 * complex TestPlans.
 	 * 
 	 * @param source
 	 *        the source of which the associated input GenericTestRecords should be
 	 *        returned
 	 * @return the input {@link GenericTestRecords} of the TestPlan associated with the
 	 *         given source
 	 */
 	public Records getInput(final GenericDataSource<?> source, TypeConfig<T> typeConfig) {
 		Records values = this.inputs.get(source);
 		if (values == null)
 			this.inputs.put(source,
 				values = this.createTestRecords(typeConfig == null ? this.defaultConfig : typeConfig));
 		else if (values.getTypeConfig() == null)
 			values.setTypeConfig(typeConfig == null ? this.defaultConfig : typeConfig);
 		return values;
 	}
 
 	/**
 	 * Returns the input {@link GenericTestRecords} of the TestPlan associated with the
 	 * given source. This is the recommended method to set input records for more
 	 * complex TestPlans.
 	 * 
 	 * @param source
 	 *        the source of which the associated input GenericTestRecords should be
 	 *        returned
 	 * @return the input {@link GenericTestRecords} of the TestPlan associated with the
 	 *         given source
 	 */
 	public Records getInput(final GenericDataSource<?> source) {
 		return this.getInput(source, this.defaultConfig);
 	}
 
 	/**
 	 * Returns the input {@link GenericTestRecords} associated with the <i>i</i>th input
 	 * of the TestPlan. If multiple contracts are tested in the TestPlan, it is
 	 * recommended to use the {@link #getInput(GenericDataSource<?>)} method to
 	 * unambiguously set the values.
 	 * 
 	 * @param number
 	 *        the number of the input.
 	 * @return the <i>i</i>th input of the TestPlan
 	 */
 	public Records getInput(final int number, TypeConfig<T> typeConfig) {
 		return this.getInput(this.getDataSources().get(number), typeConfig);
 	}
 
 	/**
 	 * Returns the input {@link GenericTestRecords} associated with the <i>i</i>th input
 	 * of the TestPlan. If multiple contracts are tested in the TestPlan, it is
 	 * recommended to use the {@link #getInput(GenericDataSource<?>)} method to
 	 * unambiguously set the values.
 	 * 
 	 * @param number
 	 *        the number of the input.
 	 * @return the <i>i</i>th input of the TestPlan
 	 */
 	public Records getInput(final int number) {
 		return this.getInput(number, this.defaultConfig);
 	}
 
 	/**
 	 * Traverses the test plan and returns the first contracts that process the
 	 * data of the given contract.
 	 * 
 	 * @param contract
 	 *        the contract of which one preceding contracts should be
 	 *        returned
 	 * @return returns the first contract that process the data of the given
 	 *         contract
 	 */
 	public Contract getOutputOfContract(Contract contract) {
 		return this.getOutputsOfContract(contract)[0];
 	}
 
 	/**
 	 * Sets the defaultConfig to the specified value.
 	 * 
 	 * @param defaultConfig
 	 *        the defaultConfig to set
 	 */
 	public void setDefaultConfig(TypeConfig<T> defaultConfig) {
 		if (defaultConfig == null)
 			throw new NullPointerException("defaultConfig must not be null");
 
 		this.defaultConfig = defaultConfig;
 	}
 
 	/**
 	 * Returns the defaultConfig.
 	 * 
 	 * @return the defaultConfig
 	 */
 	public TypeConfig<T> getDefaultConfig() {
 		return this.defaultConfig;
 	}
 
 	public GenericTestPlan<T, Records> withDefaultConfig(TypeConfig<T> DefaultConfig) {
 		this.setDefaultConfig(DefaultConfig);
 		return this;
 	}
 
 	/**
 	 * Traverses the test plan and returns all contracts that process the data
 	 * of the given contract.
 	 * 
 	 * @param contract
 	 *        the contract of which preceding contracts should be returned
 	 * @return returns all contracts that process the data of the given contract
 	 */
 	public Contract[] getOutputsOfContract(final Contract contract) {
 		final ArrayList<Contract> outputs = new ArrayList<Contract>();
 
 		for (final Contract sink : this.sinks)
 			sink.accept(new Visitor<Contract>() {
 				LinkedList<Contract> outputStack = new LinkedList<Contract>();
 
 				@Override
 				public void postVisit(final Contract visitable) {
 				}
 
 				@Override
 				public boolean preVisit(final Contract visitable) {
 					if (visitable == contract)
 						outputs.add(this.outputStack.peek());
 					this.outputStack.push(visitable);
 					return true;
 				}
 			});
 
 		return outputs.toArray(new Contract[outputs.size()]);
 	}
 
 	/**
 	 * Returns all {@link GenericDataSink}s of this test plan.
 	 * 
 	 * @return the sinks
 	 */
 	public List<GenericDataSink> getSinks() {
 		return this.sinks;
 	}
 
 	/**
 	 * Returns the sources.
 	 * 
 	 * @return the sources
 	 */
 	public List<GenericDataSource<?>> getSources() {
 		return this.sources;
 	}
 
 	/**
 	 * Compiles the plan to an {@link ExecutionGraph} and executes it. If
 	 * expected values have been specified, the actual outputs values are
 	 * compared to the expected values.
 	 */
 	public void run() {
 		try {
 			final Plan plan = this.buildPlanWithReadableSinks();
 			this.syncDegreeOfParallelism(plan);
 			this.initAdhocInputs();
 
 			LocalExecutor.execute(plan);
 		} catch (final Exception e) {
 			Assert.fail("plan scheduling: " + StringUtils.stringifyException(e));
 		}
 
 		try {
 			this.validateResults();
 		} finally {
 			try {
 				this.close();
 			} catch (IOException e) {
 			}
 		}
 	}
 
 	/**
 	 * Sets the degreeOfParallelism to the specified value.
 	 * 
 	 * @param degreeOfParallelism
 	 *        the degreeOfParallelism to set
 	 */
 	public void setDegreeOfParallelism(final int degreeOfParallelism) {
 		this.degreeOfParallelism = degreeOfParallelism;
 	}
 
 	/**
 	 * Actually builds the plan but guarantees that the output can be read
 	 * without additional knowledge. Currently the {@link SequentialOutputFormat} is used for a guaranteed
 	 * deserializable
 	 * output.<br>
 	 * If a data source is not {@link SequentialOutputFormat}, it is replaced by
 	 * a {@link SplittingOutputFormat}, with two outputs: the original one and
 	 * one {@link SequentialOutputFormat}.
 	 */
 	private Plan buildPlanWithReadableSinks() {
 		final Collection<GenericDataSink> existingSinks = this.getDataSinks();
 		final Collection<GenericDataSink> wrappedSinks = new ArrayList<GenericDataSink>();
 		for (final GenericDataSink fileSink : existingSinks) {
			if (!(fileSink instanceof FileDataSink))
				continue;
 			// need a format which is deserializable without configuration
 			if (!fileSink.getFormatClass().equals(SequentialOutputFormat.class)) {
 				Records expectedValues = this.expectedOutputs.get(fileSink);
 
 				final FileDataSink safeSink = createDefaultSink(fileSink.getName());
 
 				safeSink.setInputs(fileSink.getInputs());
 
 				wrappedSinks.add(fileSink);
 				wrappedSinks.add(safeSink);
 
 				// only add to expected outputs if we need to check for values
 				if (expectedValues != null)
 					this.expectedOutputs.put(safeSink, expectedValues);
 				this.actualOutputs.put(safeSink, this.getActualOutput(fileSink));
 				this.getActualOutput(fileSink).load(SequentialInputFormat.class, safeSink.getFilePath());
 
 			} else {
 				wrappedSinks.add(fileSink);
 				this.getActualOutput(fileSink).load(SequentialInputFormat.class,
 					((FileDataSink) fileSink).getFilePath());
 			}
 		}
 
 		return createPlan(wrappedSinks);
 	}
 
 	protected Plan createPlan(final Collection<GenericDataSink> wrappedSinks) {
 		return new Plan(wrappedSinks);
 	}
 
 	/**
 	 * 
 	 */
 	private void configureSinksAndSources() {
 		for (GenericDataSink sink : this.sinks)
 			sink.getParameters().setLong(FileOutputFormat.OUTPUT_STREAM_OPEN_TIMEOUT_KEY, 0);
 		for (GenericDataSource<?> source : this.sources)
 			source.getParameters().setLong(FileInputFormat.INPUT_STREAM_OPEN_TIMEOUT_KEY, 0);
 	}
 
 	/**
 	 * Traverses the plan for all sinks and sources.
 	 */
 	@SuppressWarnings({ "unchecked", "rawtypes" })
 	private void findSinksAndSources() {
 		for (final Contract contract : this.contracts)
 			contract.accept(new Visitor<Contract>() {
 				@Override
 				public void postVisit(final Contract visitable) {
 				}
 
 				@Override
 				public boolean preVisit(final Contract visitable) {
 					if (visitable instanceof GenericDataSink && !GenericTestPlan.this.sinks.contains(visitable))
 						GenericTestPlan.this.sinks.add((GenericDataSink) visitable);
 					if (visitable instanceof GenericDataSource<?> && !GenericTestPlan.this.sources.contains(visitable))
 						GenericTestPlan.this.sources.add((GenericDataSource<?>) visitable);
 					return true;
 				}
 			});
 
 		for (GenericDataSource<?> source : this.sources)
 			if (source instanceof FileDataSource)
 				this.getInput(source).load((Class<? extends FileInputFormat>) source.getFormatClass(),
 					((FileDataSource) source).getFilePath(), source.getParameters());
 			else
 				this.getInput(source).load((Class<? extends GenericInputFormat>) source.getFormatClass(),
 					source.getParameters());
 	}
 
 	private List<GenericDataSink> getDataSinks() {
 		return this.sinks;
 	}
 
 	private List<? extends GenericDataSource<?>> getDataSources() {
 		return this.sources;
 	}
 
 	private void initAdhocInputs() throws IOException {
 		for (final GenericDataSource<?> source : this.sources) {
 			final Records input = this.getInput(source);
 			if (input.isAdhoc() && source instanceof FileDataSource)
 				input.saveToFile(((FileDataSource) source).getFilePath());
 		}
 	}
 
 	/**
 	 * Sets the degree of parallelism for every node in the plan.
 	 */
 	private void syncDegreeOfParallelism(final Plan plan) {
 		plan.accept(new Visitor<Contract>() {
 
 			@Override
 			public void postVisit(final Contract visitable) {
 			}
 
 			@Override
 			public boolean preVisit(final Contract visitable) {
 				int degree = GenericTestPlan.this.getDegreeOfParallelism();
 				if (visitable instanceof GenericDataSource<?>)
 					degree = 1;
 				else if (degree > 1 && visitable instanceof FileDataSink)
 					try {
 						Path path = new Path(((FileDataSink) visitable).getFilePath());
 
 						final FileSystem fs = path.getFileSystem();
 
 						final FileStatus f = fs.getFileStatus(path);
 
 						if (!f.isDir()) {
 							fs.delete(path, false);
 							fs.mkdirs(path);
 						}
 					} catch (IOException e) {
 						e.printStackTrace();
 					}
 				if (visitable.getDegreeOfParallelism() == -1)
 					visitable.setDegreeOfParallelism(degree);
 				return true;
 			}
 		});
 	}
 
 	private void validateResults() {
 		for (final GenericDataSink sinkContract : this.getDataSinks()) {
 			Records expectedValues = this.expectedOutputs.get(sinkContract);
 			// need a format which is deserializable without configuration
 			if (sinkContract.getFormatClass() == SequentialOutputFormat.class && expectedValues != null
 				&& expectedValues.isInitialized()) {
 				final Records actualValues = this.getActualOutput(sinkContract);
 
 				try {
 					actualValues.assertEquals(expectedValues);
 				} catch (AssertionError e) {
 					AssertionError assertionError = new AssertionError(sinkContract.getName() + ": " + e.getMessage());
 					assertionError.initCause(e.getCause());
 					throw assertionError;
 				} finally {
 					actualValues.close();
 				}
 			}
 		}
 	}
 
 	/**
 	 * Creates a default sink with the given name. This sink may be used with ad-hoc values added to the corresponding
 	 * {@link TestPairs}.
 	 * 
 	 * @param name
 	 *        the name of the sink
 	 * @return the created sink
 	 */
 	public static FileDataSink createDefaultSink(final String name) {
 		return new FileDataSink(SequentialOutputFormat.class, getTestPlanFile("output"), name);
 	}
 
 	/**
 	 * Creates a default source with the given name. This sink may be used with ad-hoc values added to the corresponding
 	 * {@link TestPairs}.
 	 * 
 	 * @param name
 	 *        the name of the source
 	 * @return the created source
 	 */
 	@SuppressWarnings({ "unchecked", "rawtypes" })
 	public static FileDataSource createDefaultSource(final String name) {
 		return new FileDataSource((Class) SequentialInputFormat.class, 
 			getTestPlanFile("input"), name);
 	}
 
 	static String getTestPlanFile(final String prefix) {
 		return createTemporaryFile("testPlan", prefix);
 	}
 
 	private static String createTemporaryFile(String suffix, String prefix) {
 		try {
 			final File tempFile = File.createTempFile(suffix, prefix);
 			tempFile.deleteOnExit();
 			return tempFile.toURI().toString();
 		} catch (final IOException e) {
 			throw new IllegalStateException("Cannot create temporary file for prefix " + prefix, e);
 		}
 	}

 }
