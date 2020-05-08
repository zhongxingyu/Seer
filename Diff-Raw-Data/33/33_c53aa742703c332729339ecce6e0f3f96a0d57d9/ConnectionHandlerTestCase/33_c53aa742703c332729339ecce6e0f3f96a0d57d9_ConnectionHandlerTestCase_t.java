 /*
  * 
  */
 package AP2DX.test;
 
 import mockit.Expectations;
 import mockit.Mock;
 import mockit.MockClass;
 import mockit.MockUp;
 import mockit.Mockit;
 import static mockit.Deencapsulation.*;
 
 import org.junit.After;
 import org.junit.AfterClass;
 import org.junit.Before;
 import org.junit.BeforeClass;
 import org.junit.Test;
 import org.junit.Ignore;
 import static org.junit.Assert.*;
 
 import AP2DX.*;
 
 import java.io.*;
 import java.lang.Thread;
 import java.net.ServerSocket;
 import java.net.Socket;
 import java.util.ArrayList;
 import java.util.Map;
 
 /**
  * @author Maarten de Waard
  */
 public class ConnectionHandlerTestCase{
 
     private ConnectionHandler test;
     private ConcreteBase fakeBase;
     private Socket socket;
 
     @BeforeClass 
         public static void beforeClass() throws Exception 
         {
             Mockit.setUpMocks(FakeBase.class);                        
            /*new MockUp<Socket>() 
            {
                @Mock
                    void $init(String host, int port) 
                    {
                        System.out.printf("Host: %s, Port: %d\n", host, port);
                        //pass
                    }
                @Mock
                    public InputStream getInputStream()
                    {   
                        //TODO: Fix this to have the right type
                        //return new BufferedReader(new StringReader("HELLO WORLD!!!\n"));
                        return new StringBufferInputStream("HELLO WORLD");
                    }
                @Mock 
                    public boolean isClosed()
                    {
                        return false;
                    }
                @Mock
                    public OutputStream getOutputStream()
                    {
                			return null;
                    }
            };*/
         }
     @Before
         public void before() throws Exception
         {
             //Instantiate the fake base
             fakeBase = new ConcreteBase(Module.TEST);        
             //instantiate some socket
             socket = new Socket("uva.nl", 80);
             //Standardly, use a ConnectionHandler with no usar, 
             //after the Todo in the mocked getInputStream is done, we can uncomment this again.
             ConnectionHandler test = new ConnectionHandler(false, fakeBase, socket, Module.TEST, Module.TEST);
         }
 
     @Test
         public void testConstructorFalse()
         {
             try{
                 ConnectionHandler myTest = new ConnectionHandler(false, fakeBase, socket, Module.TEST, Module.TEST);
                 assertNotNull(myTest);
             }
             catch(Exception e)
             {
                 fail("Error in connectionhandler with false as argument");
             }
         }
 
     @Test
         public void testConstructorTrue()
         {
             try
             {
                 ConnectionHandler myTest = new ConnectionHandler(true, fakeBase, socket, Module.TEST, Module.TEST);
                 assertNotNull(myTest);
             }
             catch(Exception e)
             {
                 fail("Error in connectionhandler with true as argument");
             }
         }
 
     @Test
         public void testOtherConstructor()
         {
             try{
                 ConnectionHandler myTest = new ConnectionHandler(fakeBase, socket, Module.TEST);
                 assertNotNull(myTest);
             }
             catch(Exception e)
             {
                 fail("Error in short connectionhandler");
             }
         }
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
     @MockClass(realClass = AP2DXMessage.class)
         public static final class FakeAP2DXMessage 
         {
             @Mock
                 public void $init(String in, Module origin)
                 {
                     System.out.printf("In: %s, Module: %s", in, origin);
                 }
 
             @Mock
                 public void parseMessage()
                 {
                     System.out.println("parsing message");
                 }
 
         }
 
 
     /**
      * This is the mocked AP2DXBase.
      * 
      * @author Jasper
      * 
      */
     @MockClass(realClass = AP2DXBase.class)
         public static final class FakeBase 
         {		
             /**
              * Override the constructor of AP2DXBase to do nothing.
              */
             @Mock
                 public void $init(Module module) {
                     //instead of connecting and configuring, do nothing
                     System.out.printf("Module: %s\n", module);
                 }
         }
 
     public class ConcreteBase extends AP2DXBase {
 
         public ConcreteBase(Module module)
         {
             super(module);
         }
 
         public Map getConfig() 
         {
             return this.config;
         }
 
         @Override
             public ArrayList<AP2DXMessage> componentLogic(Message msg) 
             {
                 return null;
             }
 
     }
 }
 
 
 
