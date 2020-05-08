 package org.nexusbpm.service.r;
 
 
 import org.nexusbpm.common.NexusTestCase;
 import org.nexusbpm.service.NexusServiceException;
 import java.net.URI;
 import java.net.URL;
 import javax.swing.Icon;
 import javax.swing.ImageIcon;
 import javax.swing.JOptionPane;
 import junit.framework.Assert;
 import org.apache.commons.vfs.FileObject;
 import org.apache.commons.vfs.VFS;
 import org.junit.Test;
 import static org.hamcrest.CoreMatchers.*;
 import static org.junit.Assert.assertThat;
 
 public class RServiceTest extends NexusTestCase {
     private String unique = "output-" + System.currentTimeMillis();
     
    
     private RWorkItem getPlotData() throws Exception {
         RWorkItem data = new RWorkItem();
         data.setServerAddress("localhost");
         data.getParameters().put("radius", new Integer(1000));
         data.getParameters().put("imageLocation", "test.png");
         data.setCode( 
             "t=seq(0,2*pi,length=10000);\n" +
             "png(filename=imageLocation, width=800, height=600, bg=\"grey\");\n" +
             "plot(radius*cos(t * 5),radius*sin(t * 3), type=\"l\", col=\"blue\");\n" +
             "dev.off();\n" +
             "myfile = file(imageLocation);\n" +
             "radius <- radius + 1;\n" 
         );
         return data;
     }
     
     @Test
     public void testRPlottingWithOutputGraph() throws Exception{
         RServiceImpl r = new RServiceImpl();
         RWorkItem data = getPlotData();
         r.execute(data);
         if (data.getErr() != null) {
             Assert.fail("R command did not complete properly due to " + data.getErr());
         }
         else {
            ImageIcon icon = new ImageIcon(((URI)data.getResults().get("myfile")).toURL());
 //            JOptionPane.showConfirmDialog(null, "", "", JOptionPane.OK_CANCEL_OPTION, JOptionPane.INFORMATION_MESSAGE, icon);
         }
         assertThat("radius should reflect change from R code", (Double) data.getResults().get("radius"), equalTo(1001.0D));
         URI uri = (URI) data.getResults().get("myfile");
         FileObject file = VFS.getManager().resolveFile(uri.toString());
         Assert.assertEquals(9585, file.getContent().getSize());
     }
 
     @Test
     public void testRSyntaxExceptionHandling() throws Exception {
         RServiceImpl r = new RServiceImpl();
         RWorkItem data = new RWorkItem();
         data.setCode("xxx");
         data.setServerAddress(getProperty("test.r.server"));
         try {
             r.execute(data);
             Assert.fail("Exception should have been thrown");
         } catch(NexusServiceException e) {
         }
         System.out.println("Code:\n" + data.getCode());
         System.out.println("Output:\n" + data.getOut());
         System.out.println("Error:\n" + data.getErr());
         Assert.assertTrue(data.getErr().contains("Error in try({ : object 'xxx' not found"));
     }
     
 //	private RParameterMap getDBData() throws Exception {
 //		RParameterMap data = new RParameterMap();
 //		setSharedData(data);
 //		data.put(new Parameter("fileName", null, null, ParameterType.FILE.getType(), URI.create("test.csv"), false, Parameter.DIRECTION_INPUT));
 //		String code = 
 //			"library(\"rJava\");\nlibrary(RJDBC);\n" +
 //			"location<-sprintf(\"%s.csv\", reqId);\n" +
 //			"fileName=sprintf(\"//%s/rserve/%s\", serverAddress, location);\n" + 
 //			"drv<-JDBC(\"org.postgresql.Driver\",\"D:/workspace/yawl/build/3rdParty/lib/postgresql-8.0-311.jdbc3.jar\");\n" + 
 //			"conn<-dbConnect(drv,\"jdbc:postgresql:yawl\",\"postgres\",\"admin\");\n" + 
 //			"d<-dbGetQuery(conn, \"select * from yspecification\");\n" +
 //			"write.table(d, file=location,sep=\",\",row.names=FALSE);\n"  
 //			;
 //			data.setCode(code); 
 //
 //		return data;
 //	}
     private RWorkItem get2WayData() throws Exception {
         RWorkItem data = new RWorkItem();
         data.setServerAddress("localhost");
 //        URI testUri = DataflowStreamProviderFactory.getInstance().getOutputProvider("testService", "testproc", "12", "111", "test.csv").getURI();
 //        data.put(new Parameter("file", null, null, ParameterType.ASCII_FILE, testUri, false, Parameter.DIRECTION_INPUT));
 //        data.put(new Parameter("imageLocation", null, null, ParameterType.BINARY_FILE, URI.create("my2.png"), false, Parameter.DIRECTION_OUTPUT));
         data.setCode( 
             "mydata<-read.csv(file);\n" + 
             "png(filename=imageLocation, width=800, height=600, bg=\"grey\");\n" +
             "plot(mydata);\n" +
             "dev.off();\n"
         );
         return data;
     }
     
     public void xtestDBR() throws Exception { //first i installed the rjdbc package on R...
         RServiceImpl r = new RServiceImpl();
         RWorkItem data = get2WayData();
         r.execute(data);
         Assert.assertNotNull(data.getResults().get("file"));
         System.out.println(data.getOut());
         System.out.println(data.getErr());
         
     }
 }
