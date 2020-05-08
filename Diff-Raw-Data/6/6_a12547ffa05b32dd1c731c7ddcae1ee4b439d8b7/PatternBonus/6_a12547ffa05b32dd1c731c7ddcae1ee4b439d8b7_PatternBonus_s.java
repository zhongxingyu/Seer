 package weka.util;
 
 import java.util.Collection;
 import java.util.LinkedList;
 
 import weka.associations.ItemSet;
 import attributes.MethodAttribute;
 
 /**
  * PatternBonus stores rules for giving bonus on patterns
  * 
  * e.g.
  * A=X and B=Y ==> C=Y and D=""  has bonus 10
  * A=X and B=Y ==> ""=""         has bonus 2
  * 
  * PatternBonus consists of a condition collection, a consequence 
  * collection and its bonus value 
  * 
  * @see Bonus
  */
 public class PatternBonus extends Bonus {
 	public Collection<Item> conditions;
 	public Collection<Item> consequences;
 
 	/**
 	 * constructor
 	 * @param cond
 	 * @param cons
 	 * @param bonus
 	 */
 	public PatternBonus(Collection<Item> cond, Collection<Item> cons, int bonus) {
 		this.conditions = cond;
 		this.consequences = cons;
 		this.setBonus(bonus);
 	}
 
 	/**
 	 * convenient constructor
 	 * @param cond
 	 * @param cons
 	 * @param bonus
 	 */
 	public PatternBonus(Item cond, Item cons, int bonus) {
 		this(packageItem(cond), packageItem(cons), bonus);
 	}
 
 
 	/**
 	 * conditions ==> consequences    bonus
 	 */
 	public String toString() {
 		StringBuffer sb = new StringBuffer();
 		for (Item i : conditions) {
 			sb.append(i.getName() + " " + i.getValue());
 		}
 		sb.append("==>");
 		for (Item i : consequences) {
 			sb.append(i.getName() + " " + i.getValue());
 		}
 		sb.append(Bonus.toString + getBonus() + "\n");
 		return sb.toString();
 	}
 
 	/**
 	 * @return a bonus set for patterns
 	 */
 	public static Collection<Bonus> getSampleBonusSet() {
 		return getSampleMethodBonusSet();
 	}
 
 	/**
 	 * @return a bonus set for patterns
 	 */
 	public static Collection<Bonus> getSampleMethodBonusSet() {
 		Collection<Bonus> bonusSet = new LinkedList<Bonus>();
 		
 		//public vs private
 		bonusSet.add(new PatternBonus(
 				new Item(MethodAttribute.PUBLIC_METHODS, "1"),
 				new Item(MethodAttribute.PRIVATE_METHODS, "0"), -10));
 		bonusSet.add(new PatternBonus(
 				new Item(MethodAttribute.PUBLIC_METHODS, "0"),
 				new Item(MethodAttribute.PRIVATE_METHODS, "1"), -10));
 		bonusSet.add(new PatternBonus(
 				new Item(MethodAttribute.PRIVATE_METHODS, "1"),
 				new Item(MethodAttribute.PUBLIC_METHODS, "0"), -10));
 		bonusSet.add(new PatternBonus(
 				new Item(MethodAttribute.PRIVATE_METHODS, "0"),
 				new Item(MethodAttribute.PUBLIC_METHODS, "1"), -10));
 
 		//lock vs unlock
 		bonusSet.add(new PatternBonus(
 				new Item(MethodAttribute.LOCK_CALLS, "0"),
 				new Item(MethodAttribute.UNLOCK_CALLS, "0"), -10));
 
 		// [^0] all but 0 method calls
 		bonusSet.add(new PatternBonus(
 				new Item(MethodAttribute.METHOD_CALLS, "[^0]"),
 				new Item(MethodAttribute.PUBLIC_METHODS, "1"),
 				-10));
 		bonusSet.add(new PatternBonus(
 				new Item(MethodAttribute.PUBLIC_METHODS, "0"),
 				new Item(MethodAttribute.PRIVATE_METHODS, "1"), -10));
 		bonusSet.add(new PatternBonus(
 				new Item(MethodAttribute.PRIVATE_METHODS, "1"),
 				new Item(MethodAttribute.PUBLIC_METHODS, "0"), -10));
 		bonusSet.add(new PatternBonus(
 				new Item(MethodAttribute.PRIVATE_METHODS, "0"),
 				new Item(MethodAttribute.PUBLIC_METHODS, "1"), -10));
 
 		//lock vs unlock
 		bonusSet.add(new PatternBonus(
 				new Item(MethodAttribute.LOCK_CALLS, "0"),
 				new Item(MethodAttribute.UNLOCK_CALLS, "0"), -10));
 
 		// [^0] all but 0 method calls
 		bonusSet.add(new PatternBonus(
 				new Item(MethodAttribute.METHOD_CALLS, "[^0]"),
 				new Item(".*", ".*"), 100));
 
 		// [^01] all but 0 or 1
 		bonusSet.add(new PatternBonus(
 		new Item(MethodAttribute.METHOD_CALLS, "[^01]"),
 		new Item(".*", ".*"), 100));
 
 		bonusSet.add(new PatternBonus(
 				new Item(MethodAttribute.PRIVATE_METHODS, "0"),
 				new Item(MethodAttribute.PUBLIC_METHODS, "1"), -10));
		new Item(MethodAttribute.METHOD_CALLS, "[^01]"),
		new Item(".*", ".*"), 100));

 		bonusSet.add(new PatternBonus(
 				new Item(MethodAttribute.PRIVATE_METHODS, "0"),
 				new Item(MethodAttribute.PUBLIC_METHODS, "1"), -10));
 
 		/**
 		 * A=Z and B=X ==> "" is possible
 		 * A=X and B=Y ==> C="" is possible
 		 */
 		Collection<Item> temp = new LinkedList<Item>();
 		temp.add(new Item(".*", ".*"));
 		bonusSet.add(new PatternBonus(temp, packageItem(new Item("LOCK_CALLS", ".*")), 30));
 		bonusSet.add(new PatternBonus(twoItems("SYNCHRONIZED_METHODS","1","","1"), packageItem(new Item(".*",".*")), 11));
 		return bonusSet;
 	}
 	
 	/**
 	 * give a bonus on special patterns
 	 * e.g. @see PatternBonus
 	 * @param rule
 	 */
 	public int rate(Rule rule) {
 		if (!matches(rule, rule.getConditionItems(), conditions)) return 0;
 		if (!matches(rule, rule.getConsequenceItems(), consequences)) return 0;
 		
 		return getBonus();
 	}
 
 	/**
 	 * checks if an itemSet matches all pattern items 
 	 * @param rule
 	 * @param itemSet
 	 * @param patternItems
 	 * @return
 	 */
 	private boolean matches(Rule rule, ItemSet itemSet, Collection<Item> patternItems) {
 		//for all attributes in instances
 		for (int i=0 ; i< rule.getInstances().numAttributes(); i++){
 			if (itemSet.items()[i] != -1) {
 				String name = rule.getInstances().attribute(i).name();
 				String value = rule.getInstances().attribute(i).value(itemSet.items()[i]);
 				for (Item item : patternItems){					
 					if (!name.matches(item.getName())) return false;
 					if (!value.matches(item.getValue()))return false;
 				}
 			}
 		}
 		return true;
 	}
 
 	
 	/**
 	 * packs an item into a collection
 	 * @param name
 	 * @param value
 	 * @return
 	 */
 	private static Collection<Item> packageItem(Item item) {
 		Collection<Item> collection = new LinkedList<Item>();
 		collection.add(item);
 		return collection;
 	}
 	
 	/**
 	 * packs two items in a collection
 	 * @param name
 	 * @param value
 	 * @return
 	 */
 	private static Collection<Item> twoItems(String name, String value,String name2, String value2) {
 		Collection<Item> collection = new LinkedList<Item>();
 		collection.add(new Item(name, value));
 		collection.add(new Item(name2, value2));
 		return collection;
 	}
 
 
 
 }
