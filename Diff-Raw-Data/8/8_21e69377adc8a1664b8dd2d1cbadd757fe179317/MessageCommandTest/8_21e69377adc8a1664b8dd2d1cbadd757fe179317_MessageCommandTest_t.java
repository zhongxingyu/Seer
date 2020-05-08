 /**
 * SOEN 490
  * Capstone 2011
  * Test for RateMessagesCommand.
  * Team members: 	Sotirios Delimanolis
  * 			Filipe Martinho
  * 			Adam Harrison
  * 			Vahe Chahinian
  * 			Ben Crudo
  * 			Anthony Boyer
  * 
  * @author Capstone 490 Team Moving Target
  *
  */
 
 package tests.application;
 
 import static org.junit.Assert.*;
 
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.DataInputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.math.BigInteger;
 import java.security.NoSuchAlgorithmException;
 import java.sql.SQLException;
 import java.sql.Timestamp;
 import java.util.ArrayList;
 import java.util.GregorianCalendar;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import javax.servlet.http.HttpServletResponse;
 
 import org.springframework.mock.web.MockHttpServletRequest;
 import org.springframework.mock.web.MockHttpServletResponse;
 import org.springframework.mock.web.MockMultipartHttpServletRequest;
 
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 import org.msgpack.MessagePack;
 import org.msgpack.unpacker.Unpacker;
 
 import technical.UnrecognizedUserException;
 
 
 import application.IOUtils;
 import application.commands.DownvoteMessageCommand;
 import application.commands.GetMessageIDsCommand;
 import application.commands.ReadMessageCommand;
 import application.commands.CreateMessageCommand;
 import application.commands.UpvoteMessageCommand;
 
 import domain.message.Message;
 import domain.message.MessageFactory;
 import domain.message.mappers.MessageInputMapper;
 import domain.message.mappers.MessageOutputMapper;
 import domain.user.User;
 import domain.user.UserFactory;
 import domain.user.mappers.UserOutputMapper;
 import domain.user.UserType;
 import exceptions.CorruptStreamException;
 import exceptions.LostUpdateException;
 import exceptions.MapperException;
 import exceptions.ParameterException;
 import foundation.DbRegistry;
 
 public class MessageCommandTest {
 	
 	boolean previousDatabase = false;
 	
 	/*
 	@Before
 	public void createTables() throws SQLException, IOException
 	{
 		previousDatabase = DbRegistry.isDatabaseCreated();
 		if (!previousDatabase)
 			DbRegistry.createDatabaseTables();
 	}
 	
 	@After
 	public void dropTables() throws SQLException, IOException
 	{
 		
 		if (!previousDatabase)
 			DbRegistry.dropDatabaseTables();
 	}
 	*/
 	@Test
 	public void getCommand() throws SQLException, IOException, NoSuchAlgorithmException, MapperException, ParameterException, exceptions.UnrecognizedUserException, LostUpdateException
 	{
 		
 		if (!DbRegistry.isDatabaseCreated())
 			DbRegistry.createDatabaseTables();
 		
 		final double longitude = 10.0;
 		final double latitude = 30.0;
 		final float speed = 20.0f;
 		final Timestamp createdDate = new Timestamp(GregorianCalendar.getInstance().getTimeInMillis());
 		final int userRating = 2;
 		final byte[] bytes = {0,1,2,3,4,5,6,7,8,9};
 		final String email = "example@example.com";
 		final String password = "capstone";
 		final UserType type = UserType.USER_NORMAL;
 		
 		User user = UserFactory.createNew(email, password, type);
 		Message message = MessageFactory.createNew(user.getUid(), bytes, speed, latitude, longitude, createdDate, userRating);
 		UserOutputMapper.insert(user);
 		MessageOutputMapper.insert(message);
 		
 		MockHttpServletRequest request = new MockHttpServletRequest();
 		MockHttpServletResponse response = new MockHttpServletResponse();
 		
 		request.setParameter("messageid", message.getMid().toString());
 		request.setParameter("responsetype", "bin");
 		
 		ReadMessageCommand getMessageCommand = new ReadMessageCommand();
 		
 		getMessageCommand.execute(request, response);
 		assertEquals(HttpServletResponse.SC_OK, response.getStatus());
 		
 		DataInputStream in = new DataInputStream(new ByteArrayInputStream(response.getContentAsByteArray()));
 		
 		List<Message> messages = null;
 		
 		try {
 			messages = IOUtils.readMessageListFromStream(in);
 		} catch (CorruptStreamException e) {
 		}
 		
 		assertEquals(1, messages.size());
 		assertEquals(message.getMid(), messages.get(0).getMid());
 		assertEquals(message.getOwner().getEmail(), messages.get(0).getOwner().getEmail());
 		assertArrayEquals(message.getMessage(), messages.get(0).getMessage());
 		assertEquals(message.getSpeed(), messages.get(0).getSpeed(), 0.0001);
 		assertEquals(message.getCreatedAt().getTime() >= messages.get(0).getCreatedAt().getTime(), true); // amended here for rounding errors
 		assertEquals(message.getLongitude(), messages.get(0).getLongitude(), 0.000001);
 		assertEquals(message.getLatitude(), messages.get(0).getLatitude(), 0.000001);
 		assertEquals(message.getUserRating(), messages.get(0).getUserRating());
 
 		DbRegistry.dropDatabaseTables();
 	}
 
 	@Test
 	public void putCommand() throws SQLException, IOException, UnrecognizedUserException, ParameterException, NoSuchAlgorithmException, MapperException, exceptions.UnrecognizedUserException, LostUpdateException
 	{		
 		boolean previousDBCreated = DbRegistry.isDatabaseCreated();
 		if (!previousDBCreated)
 			DbRegistry.createDatabaseTables();
 		String fileName = "test.amr";
 		File file = new File(fileName);
 		int fileSize = (int)file.length();
 		byte[] fileBytes = new byte[fileSize];
 		assertEquals(fileSize, new FileInputStream(fileName).read(fileBytes));
 		String email = "example2@example.com";
 		String password = "capstone";
 		User user = UserFactory.createNew(email, password, UserType.USER_NORMAL);
 		UserOutputMapper.insert(user);
 		
 		List<Message> messages = MessageInputMapper.findByUser(user);
 		assertEquals(0, messages.size());
 		MockHttpServletResponse response = new MockHttpServletResponse();
 		MockHttpServletRequest request = new MockMultipartHttpServletRequest();
 		Map<String, String> parameters = new HashMap<String, String>();
 		parameters.put("longitude", "20.0");
 		parameters.put("latitude", "20.0");
 		parameters.put("speed", "20.0");
 		parameters.put("email", email);
 		byte[] contentArray = createContent("bin", fileBytes, parameters);
 		request.setContentType("multipart/mixed; boundary=" + BOUNDARY);
 		request.setContent(contentArray);
 		CreateMessageCommand createMessageCommand = new CreateMessageCommand();
 		createMessageCommand.execute(request, response);
 		assertEquals(HttpServletResponse.SC_OK, response.getStatus());
 		messages = MessageInputMapper.findByUser(user);
 		assertEquals(1, messages.size());
 		Message message = messages.get(0);
 		assertArrayEquals(message.getMessage(), fileBytes);
 		assertEquals(1, MessageOutputMapper.delete(message));
 		assertEquals(1, UserOutputMapper.delete(user));
 		DbRegistry.dropDatabaseTables();
 	}
 	
 	private static final String BOUNDARY = "AaB03x";
 	private static final String ENDLINE = "\r\n";
 	
 	private byte[] createContent(String fileName, byte[] fileBytes, Map<String, String> parameters) throws IOException
 	{
 		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
 		PrintWriter writer = new PrintWriter(byteArrayOutputStream);
 		for(Map.Entry<String, String> e : parameters.entrySet()) {
 			writer.append("--" + BOUNDARY + ENDLINE);
 			writer.append("Content-Disposition: form-data; name=\"" + e.getKey() + "\"" + ENDLINE);
 			writer.append(ENDLINE);
 			writer.append(e.getValue() + ENDLINE);
 		}
         writer.append("--" + BOUNDARY + ENDLINE);
         writer.append("Content-Disposition: form-data; ");
         writer.append("name=\"bin\"; filename=\"" + fileName + "\"" + ENDLINE);
         writer.append("Content-Type: application/octet-stream" + ENDLINE);
         writer.append("Content-Transfer-Encoding: binary" + ENDLINE);
         writer.append(ENDLINE);
         writer.flush();
         byteArrayOutputStream.write(fileBytes);
         writer.append(ENDLINE);
         writer.append("--" + BOUNDARY + "--");
         writer.flush();
         return byteArrayOutputStream.toByteArray();
 	}
 	
 	@Test
 	public void upvoteCommand() throws SQLException, IOException, NoSuchAlgorithmException, MapperException, ParameterException, exceptions.UnrecognizedUserException
 	{
 		
 		if (!DbRegistry.isDatabaseCreated())
 			DbRegistry.createDatabaseTables();
 		
 		final byte[] bytes = new byte[10];
 		final float speed = 10.0f;
 		final double latitude = 20.0;
 		final double longitude = 20.0;
 		Timestamp createdDate = new Timestamp(GregorianCalendar.getInstance().getTimeInMillis());
 		final int userRating = 0;
 		Message message = MessageFactory.createNew(new BigInteger("0"), bytes, speed, latitude, longitude, createdDate, userRating);
 		assertEquals(MessageOutputMapper.insert(message),1);
 		assertEquals(MessageInputMapper.find(message.getMid()), message);
 		UpvoteMessageCommand upvoteMessageCommand = new UpvoteMessageCommand();
 		MockHttpServletRequest request = new MockHttpServletRequest();
 		MockHttpServletResponse response = new MockHttpServletResponse();
 		request.setParameter("messageid", message.getMid().toString());
 		upvoteMessageCommand.execute(request, response);
 		assertEquals(HttpServletResponse.SC_OK, response.getStatus());
 		message = MessageInputMapper.find(message.getMid());
 		assertEquals(userRating + 1, message.getUserRating());
 		assertEquals(1, MessageOutputMapper.delete(message));
 		DbRegistry.dropDatabaseTables();
 	}
 	
 	@Test
 	public void downvoteCommand() throws NoSuchAlgorithmException, IOException, SQLException, MapperException, ParameterException, exceptions.UnrecognizedUserException
 	{
 		if (!DbRegistry.isDatabaseCreated())
 			DbRegistry.createDatabaseTables();
 		
 		final byte[] bytes = new byte[10];
 		final float speed = 10.0f;
 		final double latitude = 20.0;
 		final double longitude = 20.0;
 		Timestamp createdDate = new Timestamp(GregorianCalendar.getInstance().getTimeInMillis());
 		final int userRating = 0;
 		Message message = MessageFactory.createNew(new BigInteger("0"), bytes, speed, latitude, longitude, createdDate, userRating);
 		assertEquals(MessageOutputMapper.insert(message),1);
 		assertEquals(MessageInputMapper.find(message.getMid()), message);
 		DownvoteMessageCommand downVoteCommand = new DownvoteMessageCommand();
 		MockHttpServletRequest request = new MockHttpServletRequest();
 		MockHttpServletResponse response = new MockHttpServletResponse();
 		request.setParameter("messageid", message.getMid().toString());
 		downVoteCommand.execute(request, response);
 		assertEquals(HttpServletResponse.SC_OK, response.getStatus());
 		message = MessageInputMapper.find(message.getMid());
 		assertEquals(userRating - 1, message.getUserRating());
 		assertEquals(1, MessageOutputMapper.delete(message));
 		DbRegistry.dropDatabaseTables();
 	}
 	
 	@Test
 	public void getMessageIDcommandTest() throws NoSuchAlgorithmException, SQLException, IOException, CorruptStreamException, MapperException, ParameterException, exceptions.UnrecognizedUserException 
 	{
 		if (!DbRegistry.isDatabaseCreated())
 			DbRegistry.createDatabaseTables();
 		
 		// Attributes for a User
 		final BigInteger uid1 = new BigInteger("3425635465657");
 		final String email1 = "example@example.com";
 		final String password1 = "password";
 		final UserType userType1 = UserType.USER_NORMAL;
 		final int userVersion1 = 1;
 		
 		final BigInteger uid2 = new BigInteger("3425635465699");
 		final String email2 = "example@example.com";
 		final String password2 = "password";
 		final UserType userType2 = UserType.USER_ADVERTISER;
 		final int userVersion2 = 1;
 		
 		final BigInteger uid3 = new BigInteger("3425635465699");
 		final String email3 = "example@example.com";
 		final String password3 = "password";
 		final UserType userType3 = UserType.USER_ADVERTISER;
 		final int userVersion3 = 1;
 		
 		// Attributes for a Message
 		BigInteger mid1 = new BigInteger("158749857936");
 		User owner1 = new User(uid1, email1, password1, userType1, userVersion1);
 		byte[] message1 = { 1, 2, 3, 4, 5, 6 };
 		float speed1 = 5.5f;
 		double latitude1 = 35;
 		double longitude1 = 45;
 		Timestamp createdAt1 = new Timestamp(new GregorianCalendar(2011, 9, 10).getTimeInMillis());
 		int userRating1 = 7;
 		
 		BigInteger mid2 = new BigInteger("158749857910");
 		User owner2 = new User(uid2, email2, password2, userType2, userVersion2);
 		byte[] message2 = { 1, 2, 3, 4, 5, 6 };
 		float speed2 = 10.5f;
 		double latitude2 = 35;
 		double longitude2 = 45;
 		Timestamp createdAt2 = new Timestamp(new GregorianCalendar(2012, 10, 11).getTimeInMillis());
 		int userRating2 = 10;
 		
 		BigInteger mid3 = new BigInteger("158749857905");
 		User owner3 = new User(uid3, email3, password3, userType3, userVersion3);
 		byte[] message3 = { 1, 2, 3, 4, 5, 6 };
 		float speed3 = 12.5f;
 		double latitude3 = 35;
 		double longitude3 = 45;
 		Timestamp createdAt3 = new Timestamp(new GregorianCalendar(2012, 10, 11).getTimeInMillis());
 		int userRating3 = 10;
 		
 		Message m1 = new Message(mid1, owner1, message1, speed1, latitude1, longitude1, createdAt1, userRating1);
 		Message m2 = new Message(mid2, owner2, message2, speed2, latitude2, longitude2, createdAt2, userRating2);
 		Message m3 = new Message(mid3, owner3, message3, speed3, latitude3, longitude3, createdAt3, userRating3);
 		
 		MessageOutputMapper.insert(m1);
 		MessageOutputMapper.insert(m2);
 		MessageOutputMapper.insert(m3);
 		UserOutputMapper.insert(owner1);
 		UserOutputMapper.insert(owner2);
 		
 		GetMessageIDsCommand getcommand = new GetMessageIDsCommand();
 		MockHttpServletRequest request = new MockHttpServletRequest();
 		MockHttpServletResponse response = new MockHttpServletResponse();
 		
 		request.setParameter("longitude","45");
 		request.setParameter("latitude","35");
 		request.setParameter("sorttype","type");
 		request.setParameter("speed","100");
 		
 		getcommand.execute(request, response);
 		
 		assertEquals(HttpServletResponse.SC_OK, response.getStatus());
 		//new DataOutputStream(response.getOutputStream())
 		DataInputStream in = new DataInputStream(new ByteArrayInputStream(response.getContentAsByteArray()));
 		
 		ArrayList<BigInteger> returned  = (ArrayList<BigInteger>)IOUtils.readListMessageIDsFromStream(in);
 		
 		assertEquals(returned.get(0), mid3);
 		assertEquals(returned.get(1), mid2);
 		assertEquals(returned.get(2), mid1);
		
 		DbRegistry.dropDatabaseTables();
 			
 	}
 }
