 /**
  * VirtuosoRDFImporter.java
  */
 package de.uni_leipzig.informatik.swp13_sc.virtuoso;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.zip.ZipEntry;
 import java.util.zip.ZipInputStream;
 
 import virtuoso.jena.driver.VirtGraph;
 import virtuoso.jena.driver.VirtModel;
 
 import com.hp.hpl.jena.graph.Graph;
 import com.hp.hpl.jena.graph.GraphUtil;
 import com.hp.hpl.jena.rdf.model.Model;
 
 import de.uni_leipzig.informatik.swp13_sc.converter.ChessDataModelToRDFConverter;
 import de.uni_leipzig.informatik.swp13_sc.converter.ChessDataModelToRDFConverter.OutputFormats;
 import de.uni_leipzig.informatik.swp13_sc.converter.PGNToChessDataModelConverter;
 import de.uni_leipzig.informatik.swp13_sc.util.FileUtils;
 
 /**
  * 
  *
  * @author Erik
  *
  */
 public class VirtuosoRDFImporter
 {
     private VirtGraph virtuosoGraph;
     
     public VirtuosoRDFImporter(String dbConnectionString, String username, String password)
     {
         this.virtuosoGraph = new VirtGraph(
                 //"http://pcai042.informatik.uni-leipzig.de:1358/pgnconvert",
                 //"http://pcai042.informatik.uni-leipzig.de:1358/DAV",
                 dbConnectionString, username, password);
         // or VirtModel
     }
     
     public boolean importFromZipArchive(String filename)
     {
         // simply opening a stream
         InputStream is = FileUtils.openInputStream(filename);
         
         // processing the stream as a zip stream
         boolean ok = this.importFromZipArchive(is);
         
         // closing the stream after using it
         try
         {
             is.close();
         }
         catch (IOException e)
         {
             e.printStackTrace();
         }
         
         return ok;
     }
     
     public boolean importFromZipArchive(InputStream inputStream)
     {
         if (inputStream == null)
         {
             return false;
         }
         
         // opening zip stream
         ZipInputStream zis = null;
         if (inputStream instanceof ZipInputStream)
         {
             zis = (ZipInputStream) inputStream;
         }
         else
         {
             zis = new ZipInputStream(inputStream);
         }
         
         boolean ok = true;
         
         // processing each entry
         ZipEntry ze = null;
         try
         {
             while ((ze = zis.getNextEntry()) != null)
             {
                 // checking for directories - not needed here
                 if (ze.isDirectory())
                 {
                     System.out.format("Skipping Zip-Entry: %s (Directory):%n", ze.getName());
                     continue;
                 }
                 
                 // reading a entry and working on it
                 System.out.format("Reading Zip-Entry: %s:%n", ze.getName());
                 
                 // getting the format form the extension or Turtle as default
                 OutputFormats fo = OutputFormats.TURTLE;
                 for (OutputFormats f : OutputFormats.values())
                 {
                     if (ze.getName().endsWith(fo.getExtension()))
                     {
                         fo = f;
                         break;
                     }
                 }
                 
                 ok = ok && this.importFromRDFFileStream(zis, fo);
                 
                 zis.closeEntry();
             }
         }
         catch (Exception e)
         {
             // ZipException
             // IOException
             e.printStackTrace();
         }
         
         return ok;
     }
     
     public boolean importFromRDFFileStream(InputStream inputStream, OutputFormats format)
             throws IOException
     {
         if (inputStream == null)
         {
             return false;
         }
         
         // TODO:        
         //virtuosoGraph.read(url, type)        
         VirtModel m = new VirtModel(virtuosoGraph);
         
         // read in transaction (if possible?)
         m.begin();
         m.read(inputStream, null, format.getFormat());
         m.commit();
         
         return true;
     }
     
     
     // TODO: can i use a virtModel ??
     public boolean convertModelToGraph(Model m, Graph g)
     {
         if (m == null || m.isClosed())
         {
             return false;
         }
         if (g == null || g.isClosed())
         {
             return false;
         }
         
         boolean transActionStarted = false;
         if (g.getTransactionHandler().transactionsSupported())
         {
             try
             {
                 g.getTransactionHandler().begin();
                 transActionStarted = true;
             }
             catch (UnsupportedOperationException e)
             {
                 e.printStackTrace();
             }
         }
         
        GraphUtil.addInto(g, m.getGraph());
         
         // better way to add all statements?
         //StmtIterator stmtIter = m.listStatements();
         //while(stmtIter.hasNext())
         //{
         //    g.add(stmtIter.nextStatement().asTriple());
         //    stmtIter.remove();
         //}
         
         // or -----
         //VirtModel vm = new VirtModel((VirtGraph) g);
         //vm.add(m);
         
         if (g.getTransactionHandler().transactionsSupported() && transActionStarted)
         {
             g.getTransactionHandler().commit();
         }
         
         return true;
     }
     
     public boolean convertAndImportFromPGNFile(String inputFilename)
     {
         InputStream is = FileUtils.openInputStream(inputFilename);
         
         boolean ok = this.convertAndImportFromPGNFile(is);
         
         try
         {
             is.close();
         }
         catch (IOException e)
         {
             e.printStackTrace();
         }
         
         return ok;
     }
     
     public boolean convertAndImportFromPGNFile(InputStream inputStream)
     {
         PGNToChessDataModelConverter pgn2cdm = new PGNToChessDataModelConverter();
         pgn2cdm.setInputStream(inputStream);
         ChessDataModelToRDFConverter cdm2rdf = new ChessDataModelToRDFConverter();
         
         boolean ok = true;
         while(ok)
         {
             // see if finished input
             if (pgn2cdm.finishedInputFile())
             {
                 break;
             }
             
             // parse
             ok = ok && pgn2cdm.parse(500);
             if (! ok)
             {
                 // parsing error
                 break;
             }
             
             // convert
             ok = ok && cdm2rdf.convert(pgn2cdm.getGames());
             if (! ok)
             {
                 // converting error
                 break;
             }
             
             //System.out.println(cdm2rdf.getConvertedGameNames().keySet().toArray()[0]);
             
             // put into store
             Graph dataGraph = cdm2rdf.getTripelGraph();
             System.out.println(dataGraph.size());
             
             // inside transaction
             boolean transActionStarted = false;
             try
             {
                 // error when transaction is enabled !
                 // //this.virtuosoGraph.getTransactionHandler().transactionsSupported()
                 this.virtuosoGraph.getTransactionHandler().begin();
                 transActionStarted = true;
             }
             catch (UnsupportedOperationException e)
             {
                 e.printStackTrace();
             }
             catch (Exception e)
             {
                 e.printStackTrace();
             }
             try
             {
                 System.out.println(this.virtuosoGraph.size());
                GraphUtil.addInto(this.virtuosoGraph, dataGraph);
                 System.out.println(this.virtuosoGraph.size());
             }
             catch (Exception e)
             {
                 e.printStackTrace();
                 if (transActionStarted)
                 {
                     this.virtuosoGraph.getTransactionHandler().abort();
                 }
             }
             //if (transActionStarted)
             //{
                 try
                 {
                     this.virtuosoGraph.getTransactionHandler().commit();
                 }
                 catch (UnsupportedOperationException e)
                 {
                     e.printStackTrace();
                 }
             //}
             
             //dataGraph.clear();
             dataGraph.close();
         }
         
         return ok;
     }
     
     public static void main(String[] args)
     {
         if (args.length < 4)
         {
             System.out.println("JavaClass/Archive: <db-connection-string> <username> <password> <file> [<ignored> ...]");
             return;
         }
         VirtuosoRDFImporter vri = new VirtuosoRDFImporter(args[0], args[1], args[2]);
         vri.convertAndImportFromPGNFile(args[3]);
     }
 }
