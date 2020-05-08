 package fr.cormier.vra.service.impl;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 
 import org.junit.Assert;
 import org.junit.Test;
 
 import fr.cormier.domain.SailEnum;
 import fr.cormier.domain.db.Command;
 import fr.cormier.domain.db.CommandTypeEnum;
 import fr.cormier.vra.service.ICommandService;
 
 public class CommandServiceImplUTest extends AbstractServiceUTest {
 
 	private static final int TEST_COMMAND_ID = 1;
 	private static final int TEST_VR_USER_ID = 1557492;
 	private static final int TEST_RACE_ID = 1;
 	private static final CommandTypeEnum TEST_COMMAND_TYPE = CommandTypeEnum.HEADING;
 	private static final String TEST_VALUE = "16";
 	private static final String TEST_R = "VptrAyAtwfKSMqxq6BeH";
 	private static final String TEST_CHECKSUM = "c521c953713d97c2d707d7ca03dcb7f0d507a9e6";
 	
 	private static ICommandService serviceCommande;
 	
 	static {
 		serviceCommande = getBean(ICommandService.class);
 	}
 	
 	private Command createCommandTest() {
 		Command command = new Command();
 		command.setCommandId(TEST_COMMAND_ID);
 		command.setVrUserId(TEST_VR_USER_ID);
 		command.setRaceId(TEST_RACE_ID);
 		command.setCommandType(TEST_COMMAND_TYPE);
 		command.setValue(TEST_VALUE);
 		command.setR(TEST_R);
 		command.setChecksum(TEST_CHECKSUM);
 		return command;
 	}
 
 	private String getTestUrl() {
 		return "http://vendeeglobevirtuel.virtualregatta.com/core/Service/ServiceCaller.php?service=Update"+
				"&id_user="+TEST_VR_USER_ID+"&id_boat="+TEST_VR_USER_ID+"&cap="+TEST_VALUE+"&r="+TEST_R+"&checksum="+TEST_CHECKSUM;
 	
 	}
 	
 	@Test
 	public void addCommand() {
 		
 		//setup
 		Command command = createCommandTest();
 		command.setCommandId(0);
 		
 		//action
 		int result = serviceCommande.addCommand(command);
 		
 		//assert
 		Assert.assertEquals(1, result);
 	}
 	
 	@Test
 	public void addCommandList() {
 
 		//setup
 		int length = 5;
 		List<Command> commands = new ArrayList<Command>(length);
 		for(int i=0; i<length; i++) {
 			Command command = createCommandTest();
 			command.setCommandId(0);
 			commands.add(command);
 		}
 		
 		//action
 		serviceCommande.addCommandList(commands);
 				
 	}
 	
 	@Test
 	public void parseUrl() {
 		
 		//setup
 		String url = getTestUrl();
 				
 		//action
 		Command command = serviceCommande.parseUrl(url);
 		
 		//assert
 		Assert.assertEquals(TEST_VR_USER_ID, command.getVrUserId());
 		Assert.assertEquals(TEST_VALUE, command.getValue());
 		Assert.assertEquals(TEST_R, command.getR());
 		Assert.assertEquals(TEST_CHECKSUM, command.getChecksum());
 		
 	}
 	
 	@Test
 	public void generateStringUrl() {
 		
 		//setuo
 		Command command = createCommandTest();
 		
 		//action
 		String result = serviceCommande.generateStringUrl(command);
 		
 		//asert
 		Assert.assertEquals(getTestUrl(), result);
 	}
 		
 	@Test
 	public void getCommand() {
 		
 	}
 	
 	@Test
 	public void runCommand() {
 		
 	}
 
 	@Test
 	public void getCommands() {
 		
 	}
 
 	@Test
 	public void generateCommandFile() {
 		
 	}
 	
 	@Test
 	public void retrieveMissingCommands_returnCommands() {
 		
 		// action		
 		Map<CommandTypeEnum, List<String>> result = serviceCommande.retrieveMissingCommands(123, 456);
 		
 		// assert
 		Assert.assertEquals(result.get(CommandTypeEnum.HEADING).size(), 300);
 		Assert.assertEquals(result.get(CommandTypeEnum.SAIL).size(), 0);
 		
 	}
 }
