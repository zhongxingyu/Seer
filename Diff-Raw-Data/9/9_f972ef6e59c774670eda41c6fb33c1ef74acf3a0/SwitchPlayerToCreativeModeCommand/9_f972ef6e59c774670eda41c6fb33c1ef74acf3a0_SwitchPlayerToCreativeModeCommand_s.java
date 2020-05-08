 package commands;
 
 import actions.SwitchPlayerToCreativeMode;
 import entities.PluginUser;
 
 public class SwitchPlayerToCreativeModeCommand extends Command {
 
 	private static final String	MESSAGE	= "Successfully switched to Creative Mode";
 
 	public SwitchPlayerToCreativeModeCommand(final String commandString, final String commandDescription) {
 		super(commandString, commandDescription);
 		getActionList().add(new SwitchPlayerToCreativeMode());
 	}
 
	private final SwitchPlayerToCreativeMode	action	= new SwitchPlayerToCreativeMode();

 	@Override
 	protected void performCommandFor(final PluginUser player) {
		action.performActionFor(player);
 	}
 
 	@Override
 	protected String getCommandSuccessfulMessage() {
 		return MESSAGE;
 	}
 
 }
