 package org.jenkinsci.backend.recipe;
 
 import com.jcraft.jsch.JSch;
 import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
 import org.apache.commons.io.FileUtils;
 import org.eclipse.jgit.errors.TransportException;
 import org.eclipse.jgit.transport.CredentialsProvider;
 import org.eclipse.jgit.transport.JschSession;
 import org.eclipse.jgit.transport.RemoteSession;
 import org.eclipse.jgit.transport.SshSessionFactory;
 import org.eclipse.jgit.transport.URIish;
 import org.eclipse.jgit.util.FS;
 
 import java.io.File;
 import java.io.IOException;
 
 /**
  * @author Kohsuke Kawaguchi
  */
 public class SshSessionFactoryImpl extends SshSessionFactory {
     private final SshSessionFactory delegate;
     private final Parameters params;
 
     public SshSessionFactoryImpl(Parameters params, SshSessionFactory delegate) {
         this.delegate = delegate;
         this.params = params;
     }
 
     @Override
     public RemoteSession getSession(URIish uri, CredentialsProvider credentialsProvider, FS fs, int tms) throws TransportException {
         if (uri.getHost().equals("github.com")) {
             try {
                 File privateKey = params.privateKey();
                 if (privateKey==null) {
                     privateKey = File.createTempFile("private","key");
                     FileUtils.copyURLToFile(getClass().getResource("deploykey"),privateKey);
                 }
                 File publicKey = params.publicKey();
                 if (publicKey==null) {
                     publicKey = File.createTempFile("public","key");
                     FileUtils.copyURLToFile(getClass().getResource("deploykey.pub"),publicKey);
                 }
 
                 JSch jsch = new JSch();
                 jsch.setHostKeyRepository(new NoCheckHostKeyRepository());
                 jsch.addIdentity(privateKey.getAbsolutePath(), publicKey.getAbsolutePath(),
                         params.passphrase().getBytes());

                Session session = jsch.getSession("git", "github.com");
                session.connect(tms);

                return new JschSession(session, uri);
             } catch (IOException e) {
                 throw new TransportException(uri, "Failed to connect", e);
             } catch (JSchException e) {
                 throw new TransportException(uri, "Failed to connect", e);
             }
         } else {
             return delegate.getSession(uri,credentialsProvider,fs,tms);
         }
     }
 }
