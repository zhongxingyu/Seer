 package basis.migration.service;
 
 /**
  * Created with IntelliJ IDEA.
  * User: gregorysebert
  * Date: 09/11/12
  * Time: 09:45
  * To change this template use File | Settings | File Templates.
  */
 
 
 import org.exoplatform.container.ExoContainerContext;
 import org.exoplatform.container.PortalContainer;
 import org.exoplatform.services.jcr.RepositoryService;
 import org.exoplatform.services.jcr.config.RepositoryConfigurationException;
 import org.exoplatform.services.jcr.core.ManageableRepository;
 import org.exoplatform.services.jcr.impl.core.SessionImpl;
 import org.exoplatform.services.log.ExoLogger;
 import org.exoplatform.services.log.Log;
 
 import javax.jcr.Node;
 import javax.jcr.Property;
 import javax.jcr.RepositoryException;
 import javax.jcr.Session;
 import java.io.*;
 import java.text.SimpleDateFormat;
 import java.util.*;
 
 
 public class MigrationUtil {
 
     private static final String repository = "repository" ;
     private static final String workspace = "collaboration";
     private static final SimpleDateFormat yearFormat = new SimpleDateFormat("yyyy");
     private static final SimpleDateFormat monthFormat = new SimpleDateFormat("MM");
     private static final SimpleDateFormat dayFormat = new SimpleDateFormat("dd");
     protected static Log log = ExoLogger.getLogger("basis.migration.service.MigrationServiceImpl");
 
     public static Session getSession()
     {
         Session session = null;
         RepositoryService rs = (RepositoryService) ((PortalContainer) ExoContainerContext.getTopContainer()
                 .getComponentInstanceOfType(PortalContainer.class))
                 .getComponentInstanceOfType(RepositoryService.class);
 
         ManageableRepository manageableRepository;
         try {
             manageableRepository = rs.getRepository(repository);
             session = (SessionImpl) manageableRepository.getSystemSession(workspace);
         } catch (RepositoryException e) {
             e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
         } catch (RepositoryConfigurationException e) {
             e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
         }
         return session;
     }
 
 
     public static List<String> getFileList(String path)
     {
         String [] s = new File(path).list();
         List<String> filesList = new ArrayList<String>();
         for (int i=0; i<s.length;i++)
         {
                 filesList.add(s[i]);
         }
         return filesList;
     }
 
     public static boolean addBasisDocument(Session session, String BO, String path, String jcrpath,String documentsErrorPath,String documentsMigratePath, String BOCountPattern) {
         HashMap<String, String>  mapDoc=  readBasisFile(path,documentsErrorPath);
         try {
        BOCountPattern = BOCountPattern;
        Mapping mapping = new Mapping(BO, mapDoc);
 
             Mapping.DB db = Mapping.DB.valueOf(BO);
 
             BasisFolder basisFolder = null;
             BasisDocument  basisDoc = null;
             BasisFiche  basisFiche = null;
 
             switch(db) {
                 case PERS:
                     basisFolder =   Pers.getBasisFolder(BO,mapping, BOCountPattern);
                     basisDoc =   Pers.getBasisDoc(BO,mapping, BOCountPattern);
                     basisFiche =   Pers.getBasisFiche(BO,mapping);
                     break;
                 case GBBT:
                     basisFolder =   Gbbt.getBasisFolder(BO,mapping, BOCountPattern);
                     basisDoc =   Gbbt.getBasisDoc(BO,mapping, BOCountPattern);
                     basisFiche =   Gbbt.getBasisFiche(BO,mapping);
                     break;
                 case GBDO:
                     basisFolder =   Gbdo.getBasisFolder(BO,mapping, BOCountPattern);
                     basisDoc =   Gbdo.getBasisDoc(BO,mapping, BOCountPattern);
                     basisFiche =   Gbdo.getBasisFiche(BO,mapping);
                     break;
         }
 
        String basisFolderPath = createDateFolder(basisDoc.getSysDate(),session,jcrpath);
        String folderPath = createBasisFolder(session,basisFolderPath,basisFolder);
        createBasisDocument(session,folderPath,basisDoc);
        createBasisFiche(session,folderPath,basisFiche);
 
        session.save();
 
        File file=   new File(path);
        file.renameTo(new File(documentsMigratePath+"/"+file.getName()));
 
         } catch (Exception e) {
         File file=   new File(path);
         log.error("Invalid file mapping :" + path);
         file.renameTo(new File(documentsErrorPath+"/"+file.getName()));
         e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
         }
 
 
         return true;
     }
 
     public static HashMap<String, String> readBasisFile(String path,String documentsErrorPath)
     {
         File file = new File(path);
         FileInputStream fis = null;
         BufferedInputStream bis = null;
         DataInputStream dis = null;
         HashMap<String, String> dataBasis = new HashMap<String, String>();
 
         try {
             fis = new FileInputStream(file);
 
             // Here BufferedInputStream is added for fast reading.
             bis = new BufferedInputStream(fis);
             dis = new DataInputStream(bis);
 
 
 
             // dis.available() returns 0 if the file does not have more lines.
             while (dis.available() != 0) {
 
                 String disRead = dis.readLine();
                 String tmpProperty = "" ;
                 if (!disRead.contains("<#FIELD NAME = ") && !disRead.contains("</#FIELD>")){
                     disRead = " <#FIELD NAME = " + tmpProperty + ">" + disRead + " </#FIELD>";
                     //log.info("add all property");
                 }
                 else if(!disRead.contains("</#FIELD>")){
                     disRead = disRead + " </#FIELD>";
                     //log.info("Replace space");
                 }
                 else if(!disRead.contains("<#FIELD NAME = ")){
                     disRead = " <#FIELD NAME = " + tmpProperty + ">" + disRead;
                     //log.info("add property");
                 }
 
                 //log.info(disRead);
 
                 String[] FieldList = disRead.split("</#FIELD>");
                 for (String field : FieldList)
                 {
                     field = field.replace("<#FIELD NAME = ","");
                     String[] temp = field.split(">");
 
                     if (temp.length == 2)
                     {
                         dataBasis.put(temp[0],temp[1]);
                         tmpProperty = temp[0];
                     }
                     else
                     {
                         dis.close();
                         bis.close();
                         fis.close();
                         file.renameTo(new File(documentsErrorPath+"/"+file.getName()));
                         log.error("Unable to read file" + path);
                     }
                 }
 
             }
 
             // dispose all the resources after using them.
             dis.close();
             bis.close();
             fis.close();
 
         } catch (FileNotFoundException e) {
             e.printStackTrace();
         } catch (IOException e) {
             e.printStackTrace();
         } catch (Exception e){
         e.printStackTrace();
     }
 
     return dataBasis;
     }
 
     public static String createDateFolder(Date date, Session session, String path) throws RepositoryException {
         Node rootNode = session.getRootNode().getNode(path);
 
 
         //log.info("Check YEAR folder: "+ yearFormat.format(date));
         if (!rootNode.hasNode(yearFormat.format(date)))
         {
             rootNode.addNode(yearFormat.format(date),"basis:basisDate");
             session.save();
         }
 
         rootNode = session.getRootNode().getNode(path+"/"+yearFormat.format(date));
 
         //log.info("Check MONTH folder: "+ monthFormat.format(date));
         if (!rootNode.hasNode(monthFormat.format(date)))
         {
             rootNode.addNode(monthFormat.format(date),"basis:basisDate");
             session.save();
         }
 
         rootNode = session.getRootNode().getNode(path+"/"+yearFormat.format(date)+"/"+monthFormat.format(date));
 
         //log.info("Check DAY folder: "+ dayFormat.format(date));
         if (!rootNode.hasNode(dayFormat.format(date)))
         {
             rootNode.addNode(dayFormat.format(date),"basis:basisDate");
             session.save();
         }
         return rootNode.getNode(dayFormat.format(date)).getPath().substring(1,rootNode.getNode(dayFormat.format(date)).getPath().length());
     }
 
     public static String createBasisFolder(Session session, String path, BasisFolder basisFolder) throws RepositoryException {
         Node rootNode = session.getRootNode().getNode(path);
         Node nodeBasisFolder = null;
         String folderPath = "";
         if   (!rootNode.hasNode(basisFolder.getFolderId()))
         {
             nodeBasisFolder = rootNode.addNode(basisFolder.getFolderId(),"basis:basisFolder");
             String[] folderId = basisFolder.getFolderId().split("\\.");
 
             String basisFolderNodeTitle = folderId[0] + "." + folderId[1].substring(0, 2) + "." + folderId[1].substring(2, 5) + "." + folderId[1].substring(5);
             nodeBasisFolder.setProperty("exo:title",basisFolderNodeTitle) ;
             nodeBasisFolder.setProperty("basis:folderLanguage", "NL");
             if (basisFolder.getFolderExternalReference()!=null) nodeBasisFolder.setProperty("basis:folderExternalReference", basisFolder.getFolderExternalReference());
             if (basisFolder.getFolderComments()!=null) nodeBasisFolder.setProperty("basis:folderComments", basisFolder.getFolderComments());
         }
         else  nodeBasisFolder =  rootNode.getNode(basisFolder.getFolderId());
 
         path = nodeBasisFolder.getPath().substring(1,nodeBasisFolder.getPath().length());
         session.save();
 
         return path;
         }
 
     public static void createBasisDocument(Session session, String path, BasisDocument basisDocument) throws RepositoryException {
         Node rootNode = session.getRootNode().getNode(path);
         if   (!rootNode.hasNode(basisDocument.getDocId()))
         {
             Node nodeBasisDoc = rootNode.addNode(basisDocument.getDocId(),"basis:basisDocument");
             String[] docid = basisDocument.getDocId().split("\\.");
 
             String basisDocNodeTitle = docid[0] + "." + docid[1].substring(0, 2) + "." + docid[1].substring(2, 5) + "." + docid[1].substring(5);
             nodeBasisDoc.setProperty("exo:title",basisDocNodeTitle) ;
             if (basisDocument.getDocReference()!=null) nodeBasisDoc.setProperty("basis:docReference", basisDocument.getDocReference());
             if (basisDocument.getDocComments()!=null) nodeBasisDoc.setProperty("basis:docComments", basisDocument.getDocComments());
             if (basisDocument.getDocExternSenderAdress()!=null) nodeBasisDoc.setProperty("basis:docExternSenderAdress", basisDocument.getDocExternSenderAdress());
             if (basisDocument.getDocKeywords()!=null)
             {
                Property prop = nodeBasisDoc.getProperty("basis:docKeywords");
                 String[] values = {basisDocument.getDocKeywords()};
                prop.setValue(values);
             }
             if (basisDocument.getDocDate()!=null)
             {
                 Calendar cal=Calendar.getInstance();
                 cal.setTime(basisDocument.getDocDate());
                 nodeBasisDoc.setProperty("basis:docDate", cal);
             }
             session.save();
 
             nodeBasisDoc.checkin();
             nodeBasisDoc.checkout();
             session.save();
         }
 
         session.save();
     }
 
     public static void createBasisFiche(Session session, String path, BasisFiche basisFiche) throws RepositoryException {
         Node rootNode = session.getRootNode().getNode(path);
         if   (!rootNode.hasNode(basisFiche.getFicheId()))
         {
             Node nodeBasisFiche = rootNode.addNode(basisFiche.getFicheId(),"basis:basisFollow");
             nodeBasisFiche.setProperty("exo:title",basisFiche.getFicheId()) ;
             nodeBasisFiche.setProperty("basis:followRequiredAction",basisFiche.getFollowRequiredAction());
             Calendar cal=Calendar.getInstance();
             cal.setTime(basisFiche.getFollowAnswerByDate());
             nodeBasisFiche.setProperty("basis:followAnswerByDate", cal);
             session.save();
 
             nodeBasisFiche.checkin();
             nodeBasisFiche.checkout();
             session.save();
         }
 
         session.save();
     }
 
     public static String checkDosNum (String dosNum, String dosIdt, String BOCountPattern)
     {
         if(dosNum == null || dosNum.contains(" ")){
             log.info("dosIdt split : "+dosIdt.split("\\.")[2]);
             dosNum = dosIdt.split("\\.")[2];
         }
 
 
         while (dosNum.length()<=6)
         {
             dosNum = "0"+dosNum;
         }
 
         dosNum =  BOCountPattern + dosNum;
 
         return dosNum;
     }
 
     }
 
