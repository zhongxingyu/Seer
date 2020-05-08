 package eu.stratosphere.sopremo.pact;
 
 import java.util.Iterator;
 
 import eu.stratosphere.nephele.configuration.Configuration;
 import eu.stratosphere.pact.common.stubs.Collector;
 import eu.stratosphere.pact.generic.stub.AbstractStub;
 import eu.stratosphere.pact.generic.stub.GenericReducer;
 import eu.stratosphere.sopremo.EvaluationContext;
 import eu.stratosphere.sopremo.SopremoEnvironment;
 import eu.stratosphere.sopremo.serialization.SopremoRecord;
 import eu.stratosphere.sopremo.serialization.SopremoRecordLayout;
 import eu.stratosphere.sopremo.type.ArrayNode;
 import eu.stratosphere.sopremo.type.IJsonNode;
 import eu.stratosphere.sopremo.type.IStreamNode;
 import eu.stratosphere.sopremo.type.StreamNode;
 
 /**
  * An abstract implementation of the {@link GenericReducer}. SopremoReduce provides the functionality to convert the
  * standard input of the ReduceStub to a more manageable representation (the input is converted to an
  * {@link IStreamNode} ).
  */
 public abstract class GenericSopremoReduce<Elem extends IJsonNode, Out extends IJsonNode> extends AbstractStub
 		implements GenericReducer<SopremoRecord, SopremoRecord>, SopremoStub {
 	private EvaluationContext context;
 
 	JsonCollector<Out> collector;
 
 	private RecordToJsonIterator<? extends Elem> iterator;
 
 	private final StreamNode<Elem> array = new StreamNode<Elem>();
 
 	/*
 	 * (non-Javadoc)
 	 * @see eu.stratosphere.pact.common.stubs.Stub#open(eu.stratosphere.nephele.configuration.Configuration)
 	 */
 	@Override
 	public void open(final Configuration parameters) {
		SopremoEnvironment.getInstance().setConfiguration(parameters);
		//	SopremoEnvironment.getInstance().setConfigurationAndContext(parameters, getRuntimeContext());
 		this.context = SopremoEnvironment.getInstance().getEvaluationContext();
 		this.iterator = createIterator();
 		this.collector = createCollector(SopremoEnvironment.getInstance().getLayout());
 		SopremoUtil.configureWithTransferredState(this, GenericSopremoReduce.class, parameters);
 		this.array.setNodeIterator(this.iterator);
 	}
 
 	protected JsonCollector<Out> createCollector(final SopremoRecordLayout layout) {
 		return new JsonCollector<Out>(layout);
 	}
 
 	protected RecordToJsonIterator<? extends Elem> createIterator() {
 		return new RecordToJsonIterator<Elem>();
 	}
 
 	@Override
 	public final EvaluationContext getContext() {
 		return this.context;
 	}
 
 	/**
 	 * This method must be implemented to provide a user implementation of a reduce.
 	 * 
 	 * @param values
 	 *        an {@link IArrayNode} that holds all elements that belong to the same key
 	 * @param out
 	 *        a collector that collects all output nodes
 	 */
 	protected abstract void reduce(IStreamNode<Elem> values, JsonCollector<Out> collector);
 
 	/*
 	 * (non-Javadoc)
 	 * @see eu.stratosphere.pact.generic.stub.GenericReducer#combine(java.util.Iterator,
 	 * eu.stratosphere.pact.common.stubs.Collector)
 	 */
 	@Override
 	public void combine(Iterator<SopremoRecord> records, Collector<SopremoRecord> collector) throws Exception {
 		this.collector.configure(collector, this.context);
 		this.iterator.setIterator(records);
 
 		try {
 			if (SopremoUtil.DEBUG && SopremoUtil.LOG.isTraceEnabled()) {
 				ArrayNode<Elem> array = new ArrayNode<Elem>(this.array);
 				SopremoUtil.LOG.trace(String.format("%s %s", this.getContext().getOperatorDescription(), array));
 				this.combine(array, this.collector);
 			} else
 				this.combine(this.array, this.collector);
 		} catch (final RuntimeException e) {
 			SopremoUtil.LOG.error(String.format("Error occurred @ %s with %s: %s",
 				this.getContext().getOperatorDescription(),
 				this.array, e));
 			throw e;
 		}
 	}
 
 	/**
 	 * This method can be overridden by reduce stubs that want to make use of the combining feature.
 	 * In addition, the ReduceStub extending class must be annotated as Combinable.
 	 * <p>
 	 * The use of the combiner is typically a pre-reduction of the data. It works similar as the reducer, only that is
 	 * is not guaranteed to see all values with the same key in one call to the combine function. Since it is called
 	 * prior to the <code>reduce()</code> method, input and output types of the combine method are the input types of
 	 * the <code>reduce()</code> method.
 	 * 
 	 * @see eu.stratosphere.pact.common.contract.ReduceContract.Combinable
 	 * @param records
 	 *        The records to be combined. Unlike in the reduce method, these are not necessarily all records
 	 *        belonging to the given key.
 	 * @param out
 	 *        The collector to write the result to.
 	 * @throws Exception
 	 *         Implementations may forward exceptions, which are caught by the runtime. When the
 	 *         runtime catches an exception, it aborts the combine task and lets the fail-over logic
 	 *         decide whether to retry the combiner execution.
 	 */
 	protected void combine(IStreamNode<Elem> values, JsonCollector<Out> out) {
 		reduce(values, out);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * @see eu.stratosphere.pact.common.stubs.ReduceStub#reduce(java.util.Iterator,
 	 * eu.stratosphere.pact.common.stubs.Collector)
 	 */
 	@Override
 	public void reduce(final Iterator<SopremoRecord> records, final Collector<SopremoRecord> out) {
 		this.collector.configure(out, this.context);
 		this.iterator.setIterator(records);
 
 		try {
 			if (SopremoUtil.DEBUG && SopremoUtil.LOG.isTraceEnabled()) {
 				ArrayNode<Elem> array = new ArrayNode<Elem>(this.array);
 				SopremoUtil.LOG.trace(String.format("%s %s", this.getContext().getOperatorDescription(), array));
 				this.reduce(array, this.collector);
 			} else
 				this.reduce(this.array, this.collector);
 		} catch (final RuntimeException e) {
 			SopremoUtil.LOG.error(String.format("Error occurred @ %s with %s: %s",
 				this.getContext().getOperatorDescription(),
 				this.array, e));
 			throw e;
 		}
 	}
 }
