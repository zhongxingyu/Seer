 package pl.edu.agh.two.mud.common.command.definition;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map;
 
 public class CommandDefinition implements ICommandDefinition {
 
 	private static final long serialVersionUID = -4083137119523819979L;
 	private String id;
 	private Collection<String> names;
 	private String description;
	private Map<String, ICommandParameterDefinition> parametersMap = new LinkedHashMap<String, ICommandParameterDefinition>();
 	boolean textParam;
 
 	public CommandDefinition(String id, Collection<String> names,
 			String description, List<ICommandParameterDefinition> parameters,
 			boolean textParam) {
 
 		if (id == null) {
 			throw new IllegalArgumentException("Parameter id must be provided");
 		}
 		if (names == null || names.size() == 0) {
 			throw new IllegalArgumentException(
 					"There must be at least one name for command");
 		}
 		if (description == null) {
 			throw new IllegalArgumentException(
 					"Parameter description must be provided");
 		}
 		if ((parameters == null || parameters.size() == 0) && textParam) {
 			throw new IllegalArgumentException(
 					"Cannot handle textParamater if there are no paramaters for command");
 		}
 
 		this.id = id;
 		this.names = names;
 		this.description = description;
 
 		for (ICommandParameterDefinition parameter : parameters) {
 			parametersMap.put(parameter.getName(), parameter);
 		}
 
 		this.textParam = textParam;
 	}
 
 	@Override
 	public String getId() {
 		return id;
 	}
 
 	@Override
 	public Collection<String> getNames() {
 		return names;
 	}
 
 	@Override
 	public String getDescription() {
 		return description;
 	}
 
 	@Override
 	public List<ICommandParameterDefinition> getParameters() {
 		return Collections
 				.unmodifiableList(new ArrayList<ICommandParameterDefinition>(
 						parametersMap.values()));
 	}
 
 	@Override
 	public Map<String, ICommandParameterDefinition> getParamtersMap() {
 		return Collections.unmodifiableMap(parametersMap);
 	}
 
 	@Override
 	public ICommandParameterDefinition getParamater(String parameterName) {
 		return parametersMap.get(parameterName);
 	}
 
 	@Override
 	public boolean isTextParam() {
 		return textParam;
 	}
 
 }
