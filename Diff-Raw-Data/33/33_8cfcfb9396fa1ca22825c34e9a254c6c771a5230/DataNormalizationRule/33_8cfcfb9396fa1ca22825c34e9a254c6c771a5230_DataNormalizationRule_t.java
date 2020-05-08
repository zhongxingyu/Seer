 package cz.cuni.mff.odcleanstore.datanormalization.rules;
 
 import cz.cuni.mff.odcleanstore.datanormalization.exceptions.DataNormalizationException;
 
 import java.util.ArrayList;
 
 public class DataNormalizationRule {
 	/**
 	 * The type of the modification to be done by one component of the rule
 	 * @author Jakub Daniel
 	 */
 	public enum EnumRuleComponentType {
 		RULE_COMPONENT_INSERT {
 			@Override
			public String toString() {
 				return "INSERT";
 			}
 		},
 		RULE_COMPONENT_DELETE {
 			@Override
			public String toString() {
 				return "DELETE";
 			}
		},
		RULE_COMPONENT_MODIFY {
			@Override
			public String toString() {
				return "MODIFY";
			}
 		}
 	}
 
 	/**
 	 * One transformation (either INSERTION or DELETION)
 	 * @author Jakub Daniel
 	 */
 	public class Component {
 
 		public Component(EnumRuleComponentType type, String modification, String description) {
 			this.type = type;
 			this.modification = modification;
 			this.description = description;
 		}
 
 		private EnumRuleComponentType type;
 		private String modification;
 		private String description;
 
 		public EnumRuleComponentType getType() {
 			return type;
 		}
 
 		public String getModification() {
 			return modification;
 		}
 
 		public String getDescription() {
 			return description;
 		}
 
 		/**
 		 * compiles the rule into SPARUL
 		 * @param graph the name of the graph to apply the rule to
 		 * @return the SPARUL
 		 */
 		public String toString(String graph) {
 			/**
 			 * Restrict the rule to the transformed graph
 			 * Replace 'GRAPH $$graph$$' with the correct GraphPattern restriction
 			 */
 			String output = "<" + graph + "> " + modification.replaceAll("GRAPH\\s*\\$\\$graph\\$\\$", "GRAPH <" + graph + ">");
 
 			switch (type) {
 			case RULE_COMPONENT_INSERT:
 				output = "SPARQL DEFINE input:default-graph-uri <" + graph + "> INSERT INTO " + output;
 				break;
 			case RULE_COMPONENT_DELETE:
 				output = "SPARQL DEFINE input:default-graph-uri <" + graph + "> DELETE FROM " + output;
 				break;
			case RULE_COMPONENT_MODIFY:
				output = "SPARQL DEFINE input:default-graph-uri <" + graph + "> MODIFY " + output;
				break;
 			}
 
 			return output;
 		}
 	}
 
 	Integer id;
 	Integer groupId;
 	String description;
 	ArrayList<Component> components = new ArrayList<Component>();
 
 	/**
 	 * constructs new rule with the specified components
 	 * @param id the rule ID
 	 * @param groupId the group ID the rule should belong to
 	 * @param description the description of what the rule is supposed to do
 	 * @param components the list of triples (component type, component code, component description)
 	 * @throws DataNormalizationException
 	 */
 	public DataNormalizationRule(Integer id, Integer groupId, String description, String... components) throws DataNormalizationException {
 		this.id = id;
 		this.groupId = groupId;
 		this.description = description;
 
 		if (components.length % 3 != 0) throw new DataNormalizationException("Incomplete rule initialization list");
 
 		for (int i = 0; i < components.length; i += 3) {
 			addComponent(components[i], components[i + 1], components[i + 2]);
 		}
 	}
 
 	public Integer getId() {
 		return id;
 	}
 
 	public Integer getGroupId() {
 		return groupId;
 	}
 
 	public String getDescription() {
 		return description;
 	}
 
 	public Component[] getComponents() {
 		return components.toArray(new Component[this.components.size()]);
 	}
 
 	/**
 	 * constructs SPARULs for all the components restricted to a concrete graph
 	 * @param graph the graph name to restrict the SPARULs to
 	 * @return an array of SPARULs
 	 */
 	public String[] getComponents(String graph) {
 		String[] componentStrings = new String[this.components.size()];
 
 		Component[] components = this.components.toArray(new Component[this.components.size()]);
 
 		for (int i = 0; i < components.length; ++i) {
 			componentStrings[i] = components[i].toString(graph);
 		}
 
 		return componentStrings;
 	}
 
 	/**
 	 * adds new component to a rule
 	 * @param type the type of the component (either "INSERT" or "DELETE")
 	 * @param modification the code of the component
 	 * @param description the description explaining what the component should do
 	 * @throws DataNormalizationException
 	 */
 	public void addComponent(String type, String modification, String description) throws DataNormalizationException {
 		if (type.equals("INSERT")) {
 			addComponent(EnumRuleComponentType.RULE_COMPONENT_INSERT, modification, description);
 		} else if (type.equals("DELETE")) {
 			addComponent(EnumRuleComponentType.RULE_COMPONENT_DELETE, modification, description);
		} else if (type.equals("MODIFY")) {
			addComponent(EnumRuleComponentType.RULE_COMPONENT_MODIFY, modification, description);
 		} else {
 			throw new DataNormalizationException("Unknown Data Normalization Rule type");
 		}
 	}
 
 	/**
 	 * adds new component to a rule
 	 * @param type the type of the component
 	 * @param modification the code of the component
 	 * @param description the description explaining what the component should do
 	 * @throws DataNormalizationException
 	 */
 	public void addComponent(EnumRuleComponentType type, String modification, String description) {
 		components.add(new Component(type, modification, description));
 	}
 }
