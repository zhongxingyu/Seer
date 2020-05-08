 package org.lee.hackpad.jackpad;
 
 import static org.junit.Assert.*;
 
 import java.io.BufferedReader;
 import java.io.FileReader;
 import java.io.IOException;
 
 import org.junit.Before;
 import org.junit.Test;
 import org.lee.hackpad.jackpad.content.Pad;
 
 public class JackpadClientTest
 {
 	private static final String API_KEYS_FILE = "api_keys.txt";
 	
 	private String HACKPAD_CLIENT_ID;
 	private String HACKPAD_SECRET;
 	private JackpadClient jackpadClient;
 	private Pad pad;
 	
 	@Before
 	public void setUp()
 	{
 		setApiKey();
 		jackpadClient = new JackpadClient(HACKPAD_CLIENT_ID, HACKPAD_SECRET);
 		jackpadClient.build();
 		pad = new Pad("text/plain", "ddd");
 		pad.setTitle("hi");
 	}
 	
 	public void setApiKey()
 	{
 		try
 		{
 			FileReader fr = new FileReader(API_KEYS_FILE);
 			BufferedReader br = new BufferedReader(fr);
 			String line;
 			line = br.readLine();
 			String[] para= line.split(" ");
 			HACKPAD_CLIENT_ID = para[0];
 			HACKPAD_SECRET = para[1];
 			br.close();
 			fr.close();
 		}
 		catch(IOException e)
 		{
			HACKPAD_CLIENT_ID = "";
			HACKPAD_SECRET = "";
 			System.out.println(e.toString());
 		}
 	}
 	
 	/*@Test
 	public void TestCreatePad()
 	{
 		String test = jackpadClient.createPad(pad);
 		System.out.println(test);
 		assertEquals(false ,test.isEmpty());
 	}*/
 	
	/*@Test
 	public void TestGetPadContent()
 	{
 		String padText = jackpadClient.getPadContent("ClziL81VPO9", "latest", "html");
 		System.out.println(padText);
 		assertEquals(false, padText.isEmpty());
 	}
 	
 	@Test
 	public void TestUpdatePadContent()
 	{
 		pad.setPadID("ClziL81VPO9");
 		boolean test = jackpadClient.updatePadContent(pad);
 		assertEquals(true, test);
	}*/
 	
 }
