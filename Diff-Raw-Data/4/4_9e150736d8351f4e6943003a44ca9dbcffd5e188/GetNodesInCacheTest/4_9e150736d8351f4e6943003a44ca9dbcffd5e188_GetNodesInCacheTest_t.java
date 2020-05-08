 package org.exoplatform.jcr.benchmark.usecases;
 
 import com.sun.japex.TestCase;
 
 import org.exoplatform.jcr.benchmark.JCRTestBase;
 import org.exoplatform.jcr.benchmark.JCRTestContext;
 
 import java.io.ByteArrayInputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.sql.Connection;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.List;
 
 import javax.jcr.Node;
 import javax.jcr.NodeIterator;
 import javax.naming.InitialContext;
 import javax.sql.DataSource;
 
 public class GetNodesInCacheTest extends JCRTestBase
 {
    private static volatile boolean prepared;
 
    private static volatile boolean finished;
 
    private static byte[] contentOfFile;
 
    @Override
    public void doFinish(TestCase tc, JCRTestContext context) throws Exception
    {
       if (!finished)
       {
          synchronized (GetNodesInCacheTest.class)
          {
             if (!finished)
             {
               //               cleanDB();
               //               deleteDirectory(new File("../temp"));
                finished = true;
             }
          }
       }
    }
 
    @Override
    public void doPrepare(TestCase tc, JCRTestContext context) throws Exception
    {
       if (!prepared)
       {
          synchronized (GetNodesInCacheTest.class)
          {
             if (!prepared)
             {
                loadFile();
 
                int total = Integer.parseInt(tc.getParam("jcr.total.subnodes"));
                for (int i = 0; i < total; i++)
                {
                   addNode(context);
                }
                prepared = true;
             }
          }
       }
    }
 
    @Override
    public void doRun(TestCase tc, JCRTestContext context) throws Exception
    {
       Node rootNode = context.getSession().getRootNode();
       NodeIterator it = rootNode.getNodes();
       while (it.hasNext())
       {
          it.next();
       }
    }
 
    private static boolean deleteDirectory(File path)
    {
       if (path.exists())
       {
          File[] files = path.listFiles();
          for (int i = 0; i < files.length; i++)
          {
             if (files[i].isDirectory())
             {
                deleteDirectory(files[i]);
             }
             else
             {
                files[i].delete();
             }
          }
       }
       return (path.delete());
    }
 
    private static void cleanDB()
    {
       Connection dbConnection = null;
       try
       {
          DataSource ds = (DataSource)new InitialContext().lookup("jdbcexo");
          dbConnection = ds.getConnection();
          dbConnection.setAutoCommit(false);
          // =============MYSQL=============
          List<String> oracleQueryList = new ArrayList<String>();
          oracleQueryList.add("DROP TABLE jcr_scontainer");
          oracleQueryList.add("DROP TABLE jcr_svalue");
          oracleQueryList.add("DROP TABLE jcr_sref");
          oracleQueryList.add("DROP TABLE jcr_sitem");
          // oracleQueryList.add("DROP SEQUENCE JCR_SVALUE_SEQ");
          for (String query : oracleQueryList)
          {
             try
             {
                dbConnection.prepareStatement(query).execute();
             }
             catch (Exception e)
             {
                e.printStackTrace();
             }
          }
          // ================================
          dbConnection.commit();
       }
       catch (Exception e)
       {
          e.printStackTrace();
          throw new RuntimeException(e);
       }
       finally
       {
          if (dbConnection != null)
          {
             try
             {
                dbConnection.close();
             }
             catch (SQLException e)
             {
                e.printStackTrace();
             }
          }
       }
    }
 
    private static void addNode(JCRTestContext context) throws Exception
    {
       Node rootNode =
          context.getSession().getRootNode().addNode(context.generateUniqueName("rootNode"), "nt:unstructured");
       Node nodeToAdd = rootNode.addNode(context.generateUniqueName("node"), "nt:file");
       Node contentNodeOfNodeToAdd = nodeToAdd.addNode("jcr:content", "nt:resource");
       contentNodeOfNodeToAdd.setProperty("jcr:data", new ByteArrayInputStream(contentOfFile));
       contentNodeOfNodeToAdd.setProperty("jcr:mimeType", "application/pdf");
       contentNodeOfNodeToAdd.setProperty("jcr:lastModified", Calendar.getInstance());
       // dc:elementset property will be setted automatically
       context.getSession().save();
    }
 
    private static void loadFile() throws Exception
    {
       File file = new File("../resources/benchmark.pdf");
       contentOfFile = new byte[(int)file.length()];
       int offset = 0;
       int numRead = 0;
       InputStream is = new FileInputStream(file);
       try
       {
          while (offset < contentOfFile.length
             && (numRead = is.read(contentOfFile, offset, contentOfFile.length - offset)) >= 0)
          {
             offset += numRead;
          }
          if (offset < contentOfFile.length)
          {
             throw new IOException("Could not completely read file ");
          }
       }
       finally
       {
          is.close();
       }
    }
 }
