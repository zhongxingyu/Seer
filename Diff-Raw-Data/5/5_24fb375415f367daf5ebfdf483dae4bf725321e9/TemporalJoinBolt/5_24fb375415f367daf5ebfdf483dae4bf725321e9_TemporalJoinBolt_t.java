 package ch.uzh.ddis.katts.bolts.join;
 
 import java.lang.reflect.Constructor;
 import java.util.Date;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 
 import backtype.storm.task.OutputCollector;
 import backtype.storm.task.TopologyContext;
 import ch.uzh.ddis.katts.bolts.AbstractStreamSynchronizedBolt;
 import ch.uzh.ddis.katts.bolts.Event;
 import ch.uzh.ddis.katts.bolts.VariableBindings;
 import ch.uzh.ddis.katts.query.processor.join.JoinConditionConfiguration;
 import ch.uzh.ddis.katts.query.processor.join.TemporalJoinConfiguration;
 import ch.uzh.ddis.katts.query.stream.Stream;
 import ch.uzh.ddis.katts.query.stream.StreamConsumer;
 import ch.uzh.ddis.katts.query.stream.Variable;
 
 /**
  * The temporal join basically happens in two steps. First, all events arrive over the input streams are kept in the
  * join cache. The semantic join operation that checks for the actual join conditions is then executed over the data in
  * this cache. Events that do not satisfy the temporal conditions anymore are evicted from the join cache.
  * 
  * There are three steps that are being executed on the arrival of each new element in the cache. First, a configurable
  * set of eviction rules will be applied to the cache, in order to remove all data entries that have to be removed from
  * the cache <i>before</i> the join conditions are checked. Second, the actual join operation is executed, which will
  * emit all variable bindings that satisfy all join conditions. Lastly, there is a second set of eviction rules that can
  * be configured to be executed <i>after</i> the join has been executed.
  * 
  * @author lfischer
  */
 public class TemporalJoinBolt extends AbstractStreamSynchronizedBolt {
 
 	// TODO lorenz: use global storage facility
 
 	private static final long serialVersionUID = 1L;
 
 	/** This object holds the configuration details for this bolt. */
 	private final TemporalJoinConfiguration configuration;
 
 	/** A set containing all ids of all incoming streams. */
 	private Set<String> incomingStreamIds;
 
 	/** The join condition this bolt uses. */
 	private JoinCondition joinCondition;
 
 	/**
 	 * This manager manages the eviction indices and provides methods for invoking the eviction rules.
 	 */
 	private EvictionRuleManager evictionRuleManager;
 
 	/** We store all of our state variables into this object. */
 	// private Storage<Object, Map<StreamConsumer, PriorityQueue<Event>>>
 	// queues;
 	// private Logger logger = LoggerFactory.getLogger(TemporalJoinBolt.class);
 
 	/**
 	 * Creates a new instance of this bolt type using the configuration provided.
 	 * 
 	 * @param configuration
 	 *            the jaxb configuration object.
 	 */
 	public TemporalJoinBolt(TemporalJoinConfiguration configuration) {
 		this.configuration = configuration;
 	}
 
 	@Override
 	public void prepare(@SuppressWarnings("rawtypes") Map stormConf, TopologyContext context, OutputCollector collector) {
 		super.prepare(stormConf, context, collector);
 
 		// Create a set containing the identifiers of all incoming streams.
 		this.incomingStreamIds = new HashSet<String>();
 		for (StreamConsumer consumer : this.configuration.getConsumers()) {
 			this.incomingStreamIds.add(consumer.getStream().getId());
 		}
 
 		// Create and configure the join condition
 		JoinConditionConfiguration config;
 		Constructor<? extends JoinCondition> constructor;
 
 		// there is always exactly one condition!
 		config = this.configuration.getJoinCondition();
 		try {
 			constructor = config.getImplementingClass().getConstructor();
 			this.joinCondition = constructor.newInstance();
 		} catch (Exception e) {
 			throw new RuntimeException("Could not instantiate the configured join condition.", e);
 		}
 
 		this.joinCondition.prepare(config, this.incomingStreamIds);
 
 		// Create and configure the eviction rules
 		this.evictionRuleManager = new EvictionRuleManager(this.configuration.getBeforeEvictionRules(),
 				this.configuration.getAfterEvictionRules(), this.joinCondition, this.incomingStreamIds);
 	}
 
 	@Override
 	public void execute(Event event) {
 		Set<SimpleVariableBindings> joinResults;
 		SimpleVariableBindings newBindings = new SimpleVariableBindings(event.getTuple());
 		String streamId = event.getEmittedOn().getStream().getId();
 
 		this.evictionRuleManager.addBindingsToIndices(newBindings, streamId);
 		this.evictionRuleManager.executeBeforeEvictionRules(newBindings, streamId);
 		joinResults = this.joinCondition.join(newBindings, streamId);
 		this.evictionRuleManager.executeAfterEvictionRules(newBindings, streamId);
 
 		/*
 		 * Emit the joined results by creating a variableBindings object for each stream
 		 */
 		for (Stream stream : this.getStreams()) {
 
 			/*
 			 * copy all variable bindings from the result of the join into the new bindings variable which we emit from
 			 * this bolt.
 			 */
 			for (SimpleVariableBindings simpleBindings : joinResults) {
 				VariableBindings bindingsToEmit = getEmitter().createVariableBindings(stream, event);
 
 				for (Variable variable : stream.getVariables()) {
					if (simpleBindings.get(variable.getReferencesTo()) == null) {					
 						throw new NullPointerException();
 					}
					bindingsToEmit.add(variable, simpleBindings.get(variable.getReferencesTo()));
 				}
 				bindingsToEmit.setStartDate((Date) simpleBindings.get("startDate"));
 				bindingsToEmit.setEndDate((Date) simpleBindings.get("endDate"));
 
 				bindingsToEmit.emit();
 			}
 		}
 
 		ack(event);
 	}
 
 	@Override
 	public String getId() {
 		return this.configuration.getId();
 	}
 
 	@Override
 	public String getSynchronizationDateExpression() {
 		// TODO Make this configurable
 		return "#event.endDate";
 	}
 
 }
