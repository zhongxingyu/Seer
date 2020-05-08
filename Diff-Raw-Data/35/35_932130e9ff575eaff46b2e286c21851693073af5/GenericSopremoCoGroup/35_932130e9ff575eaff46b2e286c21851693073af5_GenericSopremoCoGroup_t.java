 package eu.stratosphere.sopremo.pact;
 
 import java.util.Iterator;
 
 import eu.stratosphere.nephele.configuration.Configuration;
 import eu.stratosphere.pact.common.stubs.Collector;
 import eu.stratosphere.pact.generic.stub.AbstractStub;
 import eu.stratosphere.pact.generic.stub.GenericCoGrouper;
 import eu.stratosphere.sopremo.EvaluationContext;
 import eu.stratosphere.sopremo.SopremoEnvironment;
 import eu.stratosphere.sopremo.serialization.SopremoRecord;
 import eu.stratosphere.sopremo.serialization.SopremoRecordLayout;
 import eu.stratosphere.sopremo.type.ArrayNode;
 import eu.stratosphere.sopremo.type.IJsonNode;
 import eu.stratosphere.sopremo.type.IStreamNode;
 import eu.stratosphere.sopremo.type.StreamNode;
 
 /**
  * An abstract implementation of the {@link GenericCoGrouper}. SopremoCoGroup provides the functionality to convert the
  * standard input of the GenericCoGrouper to a more manageable representation (both inputs are converted to an
  * {@link IStreamNode}).
  */
 public abstract class GenericSopremoCoGroup<LeftElem extends IJsonNode, RightElem extends IJsonNode, Out extends IJsonNode>
 		extends AbstractStub
 		implements GenericCoGrouper<SopremoRecord, SopremoRecord, SopremoRecord>, SopremoStub {
 	private EvaluationContext context;
 
 	private JsonCollector<Out> collector;
 
 	private RecordToJsonIterator<LeftElem> cachedIterator1;
 
 	private RecordToJsonIterator<RightElem> cachedIterator2;
 
 	private final StreamNode<LeftElem> leftArray = new StreamNode<LeftElem>();
 
 	private final StreamNode<RightElem> rightArray = new StreamNode<RightElem>();
 
 	/**
 	 * This method must be overridden by CoGoup UDFs that want to make use of the combining feature
 	 * on their first input. In addition, the extending class must be annotated as CombinableFirst.
 	 * <p>
 	 * The use of the combiner is typically a pre-reduction of the data.
 	 * 
 	 * @param records
 	 *        The records to be combined.
 	 * @param out
 	 *        The collector to write the result to.
 	 * @throws Exception
 	 *         Implementations may forward exceptions, which are caught by the runtime. When the
 	 *         runtime catches an exception, it aborts the combine task and lets the fail-over logic
 	 *         decide whether to retry the combiner execution.
 	 */
 	@Override
 	public void combineFirst(Iterator<SopremoRecord> records, Collector<SopremoRecord> out) {
 		throw new UnsupportedOperationException();
 	}
 
 	/**
 	 * This method must be overridden by CoGoup UDFs that want to make use of the combining feature
 	 * on their second input. In addition, the extending class must be annotated as CombinableSecond.
 	 * <p>
 	 * The use of the combiner is typically a pre-reduction of the data.
 	 * 
 	 * @param records
 	 *        The records to be combined.
 	 * @param out
 	 *        The collector to write the result to.
 	 * @throws Exception
 	 *         Implementations may forward exceptions, which are caught by the runtime. When the
 	 *         runtime catches an exception, it aborts the combine task and lets the fail-over logic
 	 *         decide whether to retry the combiner execution.
 	 */
 	@Override
 	public void combineSecond(Iterator<SopremoRecord> records, Collector<SopremoRecord> out) {
 		throw new UnsupportedOperationException();
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * @see eu.stratosphere.pact.common.stubs.CoGroupStub#coGroup(java.util.Iterator, java.util.Iterator,
 	 * eu.stratosphere.pact.common.stubs.Collector)
 	 */
 	@Override
 	public void coGroup(final Iterator<SopremoRecord> records1, final Iterator<SopremoRecord> records2,
 			final Collector<SopremoRecord> out) {
 		this.collector.configure(out, this.context);
 		this.cachedIterator1.setIterator(records1);
 		this.cachedIterator2.setIterator(records2);
 
 		try {
 			if (SopremoUtil.DEBUG && SopremoUtil.LOG.isTraceEnabled()) {
 				ArrayNode<LeftElem> leftArray = new ArrayNode<LeftElem>(this.leftArray);
 				ArrayNode<RightElem> rightArray = new ArrayNode<RightElem>(this.rightArray);
 
 				SopremoUtil.LOG.trace(String.format("%s %s/%s", this.getContext().getOperatorDescription(), leftArray,
 					rightArray));
 				this.coGroup(new StreamNode<LeftElem>(leftArray.iterator()),
 					new StreamNode<RightElem>(rightArray.iterator()), this.collector);
 			} else
 				this.coGroup(this.leftArray, this.rightArray, this.collector);
 		} catch (final RuntimeException e) {
 			SopremoUtil.LOG.error(String.format("Error occurred @ %s with %s/%s: %s", this.getContext()
 				.getOperatorDescription(), this.leftArray, this.rightArray, e));
 			throw e;
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * @see eu.stratosphere.pact.common.stubs.Stub#open(eu.stratosphere.nephele.configuration.Configuration)
 	 */
 	@Override
 	public void open(final Configuration parameters) throws Exception {
		SopremoEnvironment.getInstance().setConfiguration(parameters);
		//	SopremoEnvironment.getInstance().setConfigurationAndContext(parameters, getRuntimeContext());
 		this.context = SopremoEnvironment.getInstance().getEvaluationContext();
 		this.collector = createCollector(SopremoEnvironment.getInstance().getLayout());
 		this.cachedIterator1 = new RecordToJsonIterator<LeftElem>();
 		this.cachedIterator2 = new RecordToJsonIterator<RightElem>();
 		SopremoUtil.configureWithTransferredState(this, GenericSopremoCoGroup.class, parameters);
 		this.leftArray.setNodeIterator(this.cachedIterator1);
 		this.rightArray.setNodeIterator(this.cachedIterator2);
 	}
 
 	protected JsonCollector<Out> createCollector(final SopremoRecordLayout layout) {
 		return new JsonCollector<Out>(layout);
 	}
 
 	/**
 	 * This method must be implemented to provide a user implementation of a CoGroup.
 	 * 
 	 * @param values1
 	 *        an {@link OneTimeArrayNode} that holds all elements of the first input which were paired with the key
 	 * @param values2
 	 *        an {@link OneTimeArrayNode} that holds all elements of the second input which were paired with the key
 	 * @param out
 	 *        a collector that collects all output pairs
 	 */
 	protected abstract void coGroup(IStreamNode<LeftElem> values1, IStreamNode<RightElem> values2,
 			JsonCollector<Out> out);
 
 	@Override
 	public final EvaluationContext getContext() {
 		return this.context;
 	}
 }
