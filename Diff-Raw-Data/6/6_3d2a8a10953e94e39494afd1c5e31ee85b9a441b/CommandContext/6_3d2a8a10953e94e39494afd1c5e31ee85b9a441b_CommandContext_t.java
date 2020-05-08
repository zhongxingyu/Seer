 package cn.ac.sec.exhibition.domain;
 
 import cn.ac.sec.exhibition.util.Constants;
 
 public class CommandContext implements ICommandContext {
 	private ITimelineContext timelineContext;
 	private Command command;
 
 	public CommandContext(ITimelineContext timelineContext, Command command) {
 		this.timelineContext = timelineContext;
 		this.command = command;
 	}
 
 	@Override
 	public String buildCommand() {
 		String deviceId = command.getDeviceId();
 		String operationName = command.getOperationName();
 		Device device = timelineContext.getArea().getItemByKey(deviceId);
 		Operation operation = device.getItemByKey(operationName);
 		String commandContent = operation.getCommand();
 		// {server.id}
 		commandContent = commandContent.replace("{" + Constants.SERVER_ID_KEY
 				+ "}", timelineContext.getPropertyValue(Constants.SERVER_ID_KEY));
 		// {deviceId}
 		commandContent = commandContent.replace("{" + Constants.CMD_DEVICE_ID
 				+ "}", device.getId());
 		int length = device.getId().length();
 		for (Parameter parameter : operation.getParameter()) {
 			String name = parameter.getName();
 			String value = command.getPropertyValue(name);
			if (name != null && value != null) {
				length += value.length();
 				commandContent = commandContent
 						.replace("{" + name + "}", value);
			}
 		}
 		// {length}
 		commandContent = commandContent.replace("{" + Constants.CMD_LENGTH_ID
 				+ "}", "" + length);
 		return commandContent;
 	}
 
 }
