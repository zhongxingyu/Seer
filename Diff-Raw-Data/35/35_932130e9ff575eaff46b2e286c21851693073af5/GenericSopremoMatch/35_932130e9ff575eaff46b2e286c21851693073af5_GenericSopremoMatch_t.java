 package eu.stratosphere.sopremo.pact;
 
 import eu.stratosphere.nephele.configuration.Configuration;
 import eu.stratosphere.pact.common.stubs.Collector;
 import eu.stratosphere.pact.generic.stub.AbstractStub;
 import eu.stratosphere.pact.generic.stub.GenericMatcher;
 import eu.stratosphere.sopremo.EvaluationContext;
 import eu.stratosphere.sopremo.SopremoEnvironment;
 import eu.stratosphere.sopremo.serialization.SopremoRecord;
 import eu.stratosphere.sopremo.serialization.SopremoRecordLayout;
 import eu.stratosphere.sopremo.type.IJsonNode;
 
 /**
  * An abstract implementation of the {@link GenericMatcher}. SopremoMatch provides the functionality to convert the
  * standard input of the GenericMatcher to a more manageable representation (both inputs are converted to a subclass of
  * {@link IJsonNode}).
  */
 public abstract class GenericSopremoMatch<Left extends IJsonNode, Right extends IJsonNode, Out extends IJsonNode>
 		extends AbstractStub
 		implements GenericMatcher<SopremoRecord, SopremoRecord, SopremoRecord>, SopremoStub {
 	private EvaluationContext context;
 
 	private JsonCollector<Out> collector;
 
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
 		SopremoUtil.configureWithTransferredState(this, GenericSopremoMatch.class, parameters);
 	}
 
 	protected JsonCollector<Out> createCollector(final SopremoRecordLayout layout) {
 		return new JsonCollector<Out>(layout);
 	}
 
 	@Override
 	public final EvaluationContext getContext() {
 		return this.context;
 	}
 
 	/**
 	 * This method must be implemented to provide a user implementation of a match.
 	 * 
 	 * @param value1
 	 *        an {@link IJsonNode} that comes from the first input
 	 * @param value2
 	 *        an {@link IJsonNode} that comes from the second input
 	 * @param out
 	 *        a collector that collects all output pairs
 	 */
 	protected abstract void match(Left value1, Right value2, JsonCollector<Out> out);
 
 	/*
 	 * (non-Javadoc)
 	 * @see eu.stratosphere.pact.common.stubs.MatchStub#match(eu.stratosphere.pact.common.type.PactRecord,
 	 * eu.stratosphere.pact.common.type.PactRecord, eu.stratosphere.pact.common.stubs.Collector)
 	 */
 	@SuppressWarnings("unchecked")
 	@Override
 	public void match(final SopremoRecord record1, final SopremoRecord record2, final Collector<SopremoRecord> out) {
 		this.collector.configure(out, this.context);
 		final Left input1 = (Left) record1.getNode();
 		final Right input2 = (Right) record2.getNode();
 		if (SopremoUtil.LOG.isTraceEnabled())
 			SopremoUtil.LOG.trace(String.format("%s %s/%s", this.getContext().getOperatorDescription(), input1,
 				input2));
 		try {
 			this.match(input1, input2, this.collector);
 		} catch (final RuntimeException e) {
 			SopremoUtil.LOG.error(String.format("Error occurred @ %s with %s/%s: %s", this.getContext()
 				.getOperatorDescription(), input1, input2, e));
 			throw e;
 		}
 	}
 }
