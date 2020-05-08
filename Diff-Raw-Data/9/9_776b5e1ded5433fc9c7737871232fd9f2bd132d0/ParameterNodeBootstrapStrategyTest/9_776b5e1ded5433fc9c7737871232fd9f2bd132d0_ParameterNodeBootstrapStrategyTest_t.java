 package com.bt.pi.core.bootstrap;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertTrue;
 import static org.mockito.Mockito.mock;
 import static org.mockito.Mockito.verify;
 import static org.mockito.Mockito.when;
 
 import java.net.InetSocketAddress;
 import java.net.UnknownHostException;
 import java.util.List;
 
 import org.junit.Before;
 import org.junit.Test;
 
 import rice.environment.params.Parameters;
 
 import com.bt.pi.core.environment.KoalaParameters;
 
 public class ParameterNodeBootstrapStrategyTest {
     private Parameters parameters;
     private ParameterNodeBootstrapStrategy bootstrapStrategy;
     private InetSocketAddress inetAddress1, inetAddress2;
     private InetSocketAddress[] addressArray;
 
     @Before
     public void before() {
        inetAddress1 = new InetSocketAddress(2345);
        inetAddress2 = new InetSocketAddress(6789);
 
         parameters = mock(Parameters.class);
 
         bootstrapStrategy = new ParameterNodeBootstrapStrategy();
         bootstrapStrategy.setParameters(parameters);
 
         addressArray = new InetSocketAddress[2];
         addressArray[0] = inetAddress1;
         addressArray[1] = inetAddress2;
     }
 
     @Test
     public void testParamsConstructor() {
         // act
         bootstrapStrategy = new ParameterNodeBootstrapStrategy(parameters);
 
         // assert
         assertEquals(parameters, bootstrapStrategy.getParameters());
     }
 
     @Test
     public void testGetBootStrapsBootStrap() throws UnknownHostException {
         // setup
         when(parameters.getString(ParameterNodeBootstrapStrategy.KOALA_PREFERRED_BOOTSTRAPS_PARAM)).thenReturn("10.20.30.40:5555,10.20.30.40:5556");
         when(parameters.getInetSocketAddressArray(ParameterNodeBootstrapStrategy.KOALA_PREFERRED_BOOTSTRAPS_PARAM)).thenReturn(addressArray);
 
         // act
         List<InetSocketAddress> result = bootstrapStrategy.getBootstrapList();
 
         // assert
        assertTrue(result.contains(inetAddress1));
        assertTrue(result.contains(inetAddress2));
     }
 
     @Test
     public void testGetBootStrapListIsRandomized() throws UnknownHostException {
         // setup
         String configuredList = "10.20.30.40:5555,10.20.30.40:5556,10.20.30.40:5557,10.20.30.40:5558,10.20.30.40:5559,10.20.30.40:5560,10.20.30.40:5561";
         when(parameters.getString(ParameterNodeBootstrapStrategy.KOALA_PREFERRED_BOOTSTRAPS_PARAM)).thenReturn(configuredList);
         addressArray = buildAddressArray(configuredList);
         when(parameters.getInetSocketAddressArray(ParameterNodeBootstrapStrategy.KOALA_PREFERRED_BOOTSTRAPS_PARAM)).thenReturn(addressArray);
 
         // act
         List<InetSocketAddress> result = bootstrapStrategy.getBootstrapList();
 
         // assert
         assertEquals(7, result.size());
         StringBuffer buff = new StringBuffer();
         String sep = "";
         for (InetSocketAddress isa : result) {
             buff.append(sep + isa.getHostName() + ":" + isa.getPort());
             sep = ",";
         }
         assertEquals(configuredList.length(), buff.toString().length());
         assertFalse(configuredList.equals(buff.toString()));
     }
 
     @Test
     public void shouldTrimEmptySpacesAroundPreferredBootstrapAddresses() throws UnknownHostException {
         // setup
         parameters = new KoalaParameters();
         parameters.setString(ParameterNodeBootstrapStrategy.KOALA_PREFERRED_BOOTSTRAPS_PARAM, "10.20.30.40:5555,  10.20.30.40:5556");
 
         bootstrapStrategy = new ParameterNodeBootstrapStrategy(parameters);
 
         // act
         List<InetSocketAddress> result = bootstrapStrategy.getBootstrapList();
 
         // assert
         assertTrue(result.contains(new InetSocketAddress("10.20.30.40", 5555)));
         assertTrue(result.contains(new InetSocketAddress("10.20.30.40", 5556)));
     }
 
     private InetSocketAddress[] buildAddressArray(String configuredList) {
         String[] items = configuredList.split(",");
         InetSocketAddress[] result = new InetSocketAddress[items.length];
         int i = 0;
         for (String s : items) {
             result[i] = InetSocketAddress.createUnresolved(s.split(":")[0], Integer.parseInt(s.split(":")[1]));
             i++;
         }
         return result;
     }
 
     @Test
     public void testGetBootStrapsUsesDefaults() throws UnknownHostException {
         // setup
         when(parameters.getString(ParameterNodeBootstrapStrategy.KOALA_PREFERRED_BOOTSTRAPS_PARAM)).thenReturn(null);
         when(parameters.getInetSocketAddressArray(ParameterNodeBootstrapStrategy.KOALA_PREFERRED_BOOTSTRAPS_PARAM)).thenReturn(addressArray);
 
         // act
         bootstrapStrategy.getBootstrapList();
 
         // assert
         verify(parameters).setStringArray(ParameterNodeBootstrapStrategy.KOALA_PREFERRED_BOOTSTRAPS_PARAM, new String[] { "127.0.0.1:4524" });
     }
 }
