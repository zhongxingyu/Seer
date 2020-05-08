 package com.seeburger.vfs2.provider.jdbctable.test;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.sql.SQLException;
 import java.util.Date;
 
 import javax.sql.DataSource;
 
 import org.apache.commons.vfs2.CacheStrategy;
 import org.apache.commons.vfs2.FileContent;
 import org.apache.commons.vfs2.FileObject;
 import org.apache.commons.vfs2.FileSystemException;
 import org.apache.commons.vfs2.FileSystemManager;
 import org.apache.commons.vfs2.FileType;
 import org.apache.commons.vfs2.impl.DefaultFileSystemManager;
 import org.apache.derby.jdbc.EmbeddedDataSource40;
 
 import com.googlecode.flyway.core.Flyway;
 import com.seeburger.vfs2.provider.jdbctable.JdbcTableProvider;
 
 
 public class StandaloneClient
 {
     public static void main(String[] args) throws SQLException, IOException
     {
         DataSource ds = createDatasource();
         FileSystemManager manager = createManager(ds);
 
         final FileObject key = manager.resolveFile("seejt:/key");
         final FileObject root = key.getFileSystem().getRoot();
 
         System.out.println("key=" + key + " rootFile=" + root + " rootURI=" + key.getFileSystem().getRootURI()) ;
 
         listFiles("|", root);
 
         System.out.println("-- resolving a1+b2");
         FileObject a1 = manager.resolveFile("seejt:///key/a1");
         FileObject b2 = manager.resolveFile("seejt:///key/b2");
         listFiles("|  a1=", a1);
         listFiles("|  b2=", b2);
         System.out.println("-- resolving again");
         a1 = manager.resolveFile("seejt:///key/a1");
         b2 = manager.resolveFile("seejt:///key/b2");
         listFiles("|  a1=", a1);
         listFiles("|  b2=", b2);
 
         System.out.println("-- getting named child a1+b2");
         a1 = key.getChild("a1");
         b2 = key.getChild("b2");
         listFiles("|  a1=", a1);
         listFiles("|  b2=", b2);
 
         System.out.println("-- refreshing");
         a1.refresh();
         b2.refresh();
         listFiles("|  a1=", a1);
         listFiles("|  b2=", b2);
 
         int count = 1000;
         FileObject ax = null;
         long now = System.currentTimeMillis();
         long start = System.nanoTime();
 
         System.out.println("Reading ("+count+") ...");
         for(int i=0; i<count; i++)
         {
             ax = manager.resolveFile("seejt:///key/abcd_" + now + "_" +  i);
             //ax.createFile();
             OutputStream os = ax.getContent().getOutputStream();
             os.write(1); os.write(2); os.write(3); os.close();
             ax = manager.resolveFile(ax.toString());
             if (i % 200 == 0)
                 listFiles("("+i+") ", ax);
         }
 
         long middle = System.nanoTime();
         System.out.printf("Write Time: %,.3f ms.%n", (middle - start) / 1000000.0);
 
         System.out.println("Reading ("+count+") ...");
         for(int i=0; i<count; i++)
         {
             ax = manager.resolveFile("seejt:///key/abcd_" + now + "_"+  i);
             InputStream is = ax.getContent().getInputStream();
             if (is.read() != 1 || is.read() != 2 || is.read() != 3)
                 System.out.println("not 1 2 3");
             is.close();
             if (i % 200 == 0)
                 listFiles("("+i+")  ", ax);
         }
 
         long end = System.nanoTime();
         System.out.printf("Read Time: %,.3f ms. Have a good time.%n", (end - middle) / 1000000.0);
     }
 
     private static FileSystemManager createManager(DataSource ds) throws FileSystemException
     {
         DefaultFileSystemManager manager = new DefaultFileSystemManager();
         manager.addProvider("seejt", new JdbcTableProvider(ds));
         manager.setCacheStrategy(CacheStrategy.ON_RESOLVE);
         manager.init();
 
         return manager;
     }
 
     private static DataSource createDatasource() throws SQLException
     {
         EmbeddedDataSource40 ds = new EmbeddedDataSource40();
         ds.setUser("SEEASOWN");
         ds.setPassword("secret");
        ds.setCreateDatabase("true");
         ds.setDatabaseName("target/SimpleDerbyTestDB");
 
         Flyway flyway = new Flyway();
         flyway.setDataSource(ds);
        flyway.setLocations("db/migration/h2_derby");
         flyway.setValidateOnMigrate(true);
         flyway.setCleanOnValidationError(true);
         flyway.migrate();
 
        ds.setCreateDatabase("false");

         return ds;
     }
 
     static void listFiles(String prefix, FileObject file) throws FileSystemException
     {
         String type = "";
         if (file.isHidden())
             type+="H";
         else
             type+=".";
         if (file.isReadable())
             type+="R";
         else
             type+=".";
         if (file.isWriteable())
             type+="W";
         else
             type+=".";
         type+=")";
         FileContent content = file.getContent();
         if (content != null)
         {
             try { type += " date=" + new Date(content.getLastModifiedTime()); }catch (Exception ig) { }
             try { type += " size=" + content.getSize();  }catch (Exception ig) { }
             try { type += " att=" + content.getAttributes();  }catch (Exception ig) { }
         }
 
 
         if (file.getType().hasChildren())
         {
             FileObject[] children = null;
             try
             {
                 children = file.getChildren();
                 System.out.println(prefix + file + " (d" + type);
             }
             catch (FileSystemException ignored)
             {
                 System.out.println(prefix + file + " (d"+type + " (" + ignored + ")");
             }
             if (children != null)
             {
                 for(FileObject fo : children)
                 {
                     listFiles(prefix + "  ", fo);
                 }
             }
         }
         else if (file.getType() == FileType.FILE)
         {
             System.out.println(prefix + file + " (." + type);
         }
         else
         {
             System.out.println(prefix + file + " (-" + type);
         }
     }
 
 }
