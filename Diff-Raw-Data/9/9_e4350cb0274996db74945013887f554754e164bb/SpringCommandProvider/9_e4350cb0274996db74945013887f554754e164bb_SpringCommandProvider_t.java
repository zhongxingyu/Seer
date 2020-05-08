 package pl.edu.agh.two.mud.common.command.provider;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.factory.config.BeanDefinition;
 import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
 
 import pl.edu.agh.two.mud.common.command.Command;
 import pl.edu.agh.two.mud.common.command.UICommand;
 
 import com.google.common.collect.ImmutableSet;
 
 public class SpringCommandProvider implements CommandProvider {
 
 	private Map<String, Class<? extends Command>> commandClassesById = new HashMap<String, Class<? extends Command>>();
 
 	@Autowired
 	private ConfigurableListableBeanFactory beanFactory;
 
 	@Override
 	public Command getCommandById(String commandId) {
 		Class<? extends Command> commandClass = commandClassesById
 				.get(commandId);
 		Command command = beanFactory.getBean(commandClass);
 		return command;
 	}
 
 	@Override
 	public List<Command> getAvailableCommands() {
 		List<Command> availableCommands = new LinkedList<Command>();
 		for (Class<? extends Command> commandClass : commandClassesById
 				.values()) {
 			Command command = beanFactory.getBean(commandClass);
 			availableCommands.add(command);
 		}
 		return availableCommands;
 	}
 
 	@Override
 	public List<UICommand> getUICommands() {
 		List<UICommand> uiCommands = new ArrayList<UICommand>();
 		for (Class<? extends Command> commandClass : commandClassesById
 				.values()) {
 			Command command = beanFactory.getBean(commandClass);
 			if (command instanceof UICommand) {
 				uiCommands.add((UICommand) command);
 			}
 		}
 
 		return uiCommands;
 	}
 
 	@Override
 	public boolean isCommandAvailable(String commandId) {
 		return commandClassesById.containsKey(commandId);
 	}
 
 	protected void registerCommand(String id,
 			Class<? extends Command> commandClass) {
 		commandClassesById.put(id, commandClass);
 	}
 
 	@SuppressWarnings({ "unchecked", "unused" })
 	private void buildProvider() {
 		String[] commandBeanNames = beanFactory
 				.getBeanNamesForType(Command.class);
 		for (String commandBeanName : commandBeanNames) {
 			BeanDefinition commandBeanDefinition = beanFactory
 					.getBeanDefinition(commandBeanName);
 			try {
 				Class<? extends Command> commandClass = (Class<? extends Command>) Class
 						.forName(commandBeanDefinition.getBeanClassName());
 				String id = commandClass.getName();
 				registerCommand(id, commandClass);
 
 			} catch (ClassNotFoundException e) {
 				throw new RuntimeException();
 			}
 		}
 	}
 
 	@Override
 	public List<UICommand> getUICommandsWithout(
 			Class<? extends UICommand>... classesToExclude) {
		Set<Class<? extends UICommand>> exclude = new ImmutableSet.Builder<Class<? extends UICommand>>()
				.add(classesToExclude).build();
 
 		List<UICommand> uiCommands = new ArrayList<UICommand>();
 		for (UICommand uiCommand : getUICommands()) {
 			if (exclude.contains(uiCommand.getClass())) {
 				continue;
 			}
 
 			uiCommands.add(uiCommand);
 		}
 
 		return uiCommands;
 	}
 
 	@Override
 	public List<UICommand> getUICommands(Class<? extends UICommand>... classes) {
		Set<Class<? extends UICommand>> classesToInclude = new ImmutableSet.Builder<Class<? extends UICommand>>()
				.add(classes).build();
 
 		List<UICommand> uiCommands = new ArrayList<UICommand>();
 		for (UICommand uiCommand : getUICommands()) {
 			if (classesToInclude.contains(uiCommand.getClass())) {
 				uiCommands.add(uiCommand);
 			}
 		}
 
 		return uiCommands;
 
 	}
 
 	@Override
 	public List<Class<? extends UICommand>> convertUICommandsToClasses(
 			List<UICommand> uiCommands) {
 		List<Class<? extends UICommand>> classes = new ArrayList<Class<? extends UICommand>>(
 				uiCommands.size());
 		for (UICommand command : uiCommands) {
 			classes.add(command.getClass());
 		}
 
 		return classes;
 	}
 
 }
