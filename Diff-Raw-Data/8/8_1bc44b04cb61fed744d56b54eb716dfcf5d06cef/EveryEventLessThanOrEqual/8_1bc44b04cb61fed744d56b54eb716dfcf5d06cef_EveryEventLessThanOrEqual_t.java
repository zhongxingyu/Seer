 package cz.muni.fi.publishsubscribe.countingtree.benchmark;
 
 import com.google.caliper.SimpleBenchmark;
 
 import cz.muni.fi.publishsubscribe.countingtree.Attribute;
 import cz.muni.fi.publishsubscribe.countingtree.AttributeValue;
 import cz.muni.fi.publishsubscribe.countingtree.Constraint;
 import cz.muni.fi.publishsubscribe.countingtree.CountingTree;
 import cz.muni.fi.publishsubscribe.countingtree.Event;
 import cz.muni.fi.publishsubscribe.countingtree.Operator;
 
 public class EveryEventLessThanOrEqual extends SimpleBenchmark {
 
 	private static final int PREDICATE_COUNT = 500;
 	private static final int EVENT_COUNT = 10000;
	private static final long LONG_MIN_VALUE = 0L;
	private static final long LONG_MAX_VALUE = 3L;
 	
 	private CountingTree tree;
 
 	@Override
 	protected void setUp() throws Exception {
 		super.setUp();
 
 		this.tree = new CountingTree();
 
 		// Less than or equal to a number from 0 to 3
		Long number = LONG_MIN_VALUE;
 		for (int i = 0; i < PREDICATE_COUNT; i++) {
 			Constraint<Long> constraint = new Constraint<Long>("longAttribute",
 					new AttributeValue<Long>(number, Long.class),
 					Operator.LESS_THAN_OR_EQUAL_TO);
			number = number >= LONG_MAX_VALUE ? LONG_MIN_VALUE : number + 1;
 
 			tree.subscribe(UtilityMethods
 					.createPredicateFromConstraint(constraint));
 		}
 	}
 
 	private void matchEvents(long value) {
 		Event event = new Event();
 		event.addAttribute(new Attribute<Long>("longAttribute",
 				new AttributeValue<Long>(value, Long.class)));
 
 		for (int i = 0; i < EVENT_COUNT; i++) {
 			tree.match(event);
 		}
 	}
 
 	public void timeMatchEvents25(int reps) {
 		for (int i = 0; i < reps; i++) {
 			matchEvents(3);
 		}
 	}
 
 	public void timeMatchEvents50(int reps) {
 		for (int i = 0; i < reps; i++) {
 			matchEvents(2);
 		}
 	}
 
 	public void timeMatchEvents75(int reps) {
 		for (int i = 0; i < reps; i++) {
 			matchEvents(1);
 		}
 	}
 
 	public void timeMatchEvents100(int reps) {
 		for (int i = 0; i < reps; i++) {
 			matchEvents(0);
 		}
 	}
 
 }
