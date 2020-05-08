 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package my.triviagame.bll;
 
 import java.io.File;
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.net.URL;
 import my.triviagame.dal.IDAL;
import my.triviagame.dal.IDAL.DALException;
import my.triviagame.xmcd.XmcdDiscStream;
 import my.triviagame.xmcd.XmcdImporter;
 import org.apache.commons.io.FileUtils;
 
 /**
  *
  * @author guy
  */
 public class Importer {
    public static void importArchive(IDAL dal, String filePath) throws MalformedURLException, IOException, DALException {
         XmcdImporter importer = new XmcdImporter(dal);
         File file = FileUtils.toFile(new URL(filePath));
        importer.importFreedb(new XmcdDiscStream(file));
     }
 }
