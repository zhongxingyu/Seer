 package org.kevoree.library.javase.fileSystemGitRepository;
 
 import org.eclipse.egit.github.core.Repository;
 import org.eclipse.egit.github.core.service.RepositoryService;
 import org.eclipse.jgit.api.CloneCommand;
 import org.eclipse.jgit.api.CommitCommand;
 import org.eclipse.jgit.api.errors.InvalidRemoteException;
 import org.eclipse.jgit.api.errors.NoFilepatternException;
 import org.eclipse.jgit.lib.PersonIdent;
 import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
 import org.kevoree.annotation.*;
 import org.kevoree.library.javase.fileSystem.client.AbstractItem;
 import org.kevoree.library.javase.fileSystem.client.FolderItem;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 
 /**
  * Created with IntelliJ IDEA.
  * User: pdespagn
  * Date: 5/23/12
  * Time: 3:20 PM
  * To change this template use File | Settings | File Templates.
  */
 @Provides({
         @ProvidedPort(name = "createRepo", type= PortType.SERVICE, className = GitRepositoryActions.class)
 })
 @ComponentType
 public class FileSystemGitRepositoryImpl extends GitFileSystem implements GitRepositoryActions {
 
     private Logger logger = LoggerFactory.getLogger(GitFileSystem.class);
 
     // FROM GitFileSystem
     // protected File baseClone = null;
     // protected org.eclipse.jgit.lib.Repository repository = null;
     // protected Git git = null;
     @Start
     public void start() throws Exception {
         if(!this.getDictionary().get("url").toString().isEmpty() && !this.getDictionary().get("login").toString().isEmpty() && !this.getDictionary().get("pass").toString().isEmpty())
             super.start();
     }
 
     @Stop
     public void stop() {
     }
 
     @Override
     @Port(name="createRepo", method = "importRepository")
     public AbstractItem importRepository(String login, String password, String url, String nameRepository, String pathRepository) {
 
         if(isRepoExist(login,password, nameRepository)){
             baseClone = new File(pathRepository+nameRepository);
             deleteDir(baseClone);
             cloneRepository(url, nameRepository, pathRepository);
             return new FolderItem(baseClone.getPath());
         }
        logger.debug("Can't import the repository, because {} doesn't exist in {}'s account or he's not a collaborator of that repository ",nameRepository,login );
         return null;
     }
 
     // Deletes all files and subdirectories under dir
     public static boolean deleteDir(File dir) {
         if (dir.isDirectory()) {
             String[] children = dir.list();
             for (int i=0; i<children.length; i++) {
                 boolean success = deleteDir(new File(dir, children[i]));
                 if (!success) {
                     return false;
                 }
             }
         }
         // The directory is now empty so delete it
         return dir.delete();
     }
 
     @Override
     @Port(name="createRepo", method = "initRepository")
     public AbstractItem initRepository(String login, String password, String nameRepository, String pathRepository){
         boolean isCreated = createRepository(login,password,nameRepository);
         if(isCreated){
             logger.debug(" the Repository {} is created " , nameRepository);
             cloneRepository("https://" + login + "@github.com/" + login + "/" + nameRepository + ".git", nameRepository, pathRepository);
             createFileToInitRepository("https:  //" + login + "@github.com/" + login + "/" + nameRepository + ".git", nameRepository, pathRepository);
             commitRepository("commit init", login, "Email@login.org");
             pushRepository(login, password);
             return new FolderItem(baseClone.getPath());
         }
         return null;
     }
 
     public boolean isRepoExist(String login, String password, String nameRepository ){
         RepositoryService service = new RepositoryService();
         service.getClient().setCredentials(login, password);
         try {
             service.getRepository(login, nameRepository);
 
             logger.debug("The repository exists ");
             return true;
         } catch (IOException e) {
             logger.debug("The repository : {} doesn't exist " , nameRepository);
             return false;
         }
     }
 
     @Override
     @Port(name="createRepo", method = "createRepository")
     public boolean  createRepository(String login, String password, String nameRepository) {
         RepositoryService service = new RepositoryService();
         service.getClient().setCredentials(login, password);
         if(!isRepoExist(login,password,nameRepository)){
             Repository repo = new Repository();
             repo.setName(nameRepository);
             try {
                 service.createRepository(repo);
                 return true;
             } catch (IOException e) {
                 logger.debug("Could not create repository: ", e);
                 return false;
             }
         }
        logger.debug(" Can't create the repository {} because it already exists in {}'s account", nameRepository, login);
         return false;
     }
 
     @Override
     @Port(name="createRepo", method = "createFileToInitRepository")
     public void createFileToInitRepository(String url, String nomRepo, String directoryPath) {
         File file = new File(directoryPath + nomRepo + "/README.md");
         try {
             file.createNewFile();
             addFileToRepository(file);
             commitRepository("Init Repository with a README.md ","","");
         } catch (IOException e) {
             logger.debug("Cannot create the file ",e);
         }
     }
 
     @Override
     @Port(name="createRepo", method = "cloneRepository")
     public void cloneRepository(String url, String nameRepository, String pathRepository) {
         baseClone = new File(pathRepository+nameRepository);
         CloneCommand clone = new CloneCommand();
         clone.setURI(url);
         clone.setDirectory(new File(pathRepository+nameRepository));
         clone.setBare(false);
         git = clone.call();
         repository = git.getRepository();
     }
 
     @Override
     @Port(name="createRepo", method = "commitRepository")
     public void commitRepository(String message, String nom, String email) {
         CommitCommand commit = git.commit();
         commit.setMessage(message);
         commit.setAuthor(new PersonIdent(nom, email));
         try {
             commit.call();
         } catch (Exception e) {
             logger.debug("Cannot commit repository ",e);
         }
     }
 
     @Override
     @Port(name="createRepo", method = "pushRepository")
     public boolean pushRepository(String login, String password) {
         UsernamePasswordCredentialsProvider user = new UsernamePasswordCredentialsProvider(login, password);
         try {
             git.push().setCredentialsProvider(user).call();
             return true;
         } catch (InvalidRemoteException e) {
             logger.debug("Cannot push repository ",e);
             return false;
         }
     }
 }
