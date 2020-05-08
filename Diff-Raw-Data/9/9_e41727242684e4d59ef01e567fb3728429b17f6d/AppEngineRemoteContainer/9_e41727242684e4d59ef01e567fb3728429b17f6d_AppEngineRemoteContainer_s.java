 /*
  * JBoss, Home of Professional Open Source.
  * Copyright 2011, Red Hat, Inc., and individual contributors
  * as indicated by the @author tags. See the copyright.txt file in the
  * distribution for a full listing of individual contributors.
  *
  * This is free software; you can redistribute it and/or modify it
  * under the terms of the GNU Lesser General Public License as
  * published by the Free Software Foundation; either version 2.1 of
  * the License, or (at your option) any later version.
  *
  * This software is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this software; if not, write to the Free
  * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
  * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
  */
 
 package org.jboss.arquillian.container.appengine.remote_1_5;
 
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.io.OutputStreamWriter;
 import java.io.PipedInputStream;
 import java.io.PipedOutputStream;
 import java.io.PrintStream;
 import java.io.UnsupportedEncodingException;
 import java.lang.reflect.Method;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 import java.util.logging.Level;
 
 import org.jboss.arquillian.container.appengine.cli.AppEngineCLIContainer;
 import org.jboss.arquillian.container.spi.ConfigurationException;
 import org.jboss.arquillian.container.spi.client.container.DeploymentException;
 import org.jboss.arquillian.container.spi.client.protocol.metadata.ProtocolMetaData;
 import org.jboss.shrinkwrap.api.Archive;
 
 /**
  * Remote / production AppEngine container.
  *
  * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
  */
 public class AppEngineRemoteContainer extends AppEngineCLIContainer<AppEngineRemoteConfiguration>
 {
    private AppEngineRemoteConfiguration configuration;
 
    public Class<AppEngineRemoteConfiguration> getConfigurationClass()
    {
       return AppEngineRemoteConfiguration.class;
    }
 
    public void setup(AppEngineRemoteConfiguration configuration)
    {
       this.configuration = configuration;
 
       if (configuration.getSdkDir() == null)
          throw new ConfigurationException("AppEngine SDK root is null.");
 
       if (configuration.getPassword() == null)
          throw new ConfigurationException("AppEngine password is null.");
 
       System.setProperty(AppEngineRemoteConfiguration.SDK_ROOT, configuration.getSdkDir());
    }
 
    protected ProtocolMetaData doDeploy(Archive<?> archive) throws DeploymentException
    {
       String sdkDir = configuration.getSdkDir();
       if (new File(sdkDir).isDirectory() == false)
          throw new DeploymentException("SDK root is not a directory: " + sdkDir);
 
       String app;
       try
       {
          app = getAppLocation().getCanonicalPath();
       }
       catch (IOException e)
       {
          throw new DeploymentException("Cannot get app location.", e);
       }
 
       try
       {
          log.info(archive.toString(true));
 
          List<String> args = new ArrayList<String>();
 
          addArg(args, "email", configuration.getEmail(), false);
          addArg(args, "host", configuration.getHost(), true);
          addArg(args, "compile_encoding", configuration.getEncoding(), true);
          addArg(args, "proxy", configuration.getProxy(), true);
          addArg(args, "passin", configuration.isPassIn());
          if (configuration.isPassIn() == false)
             addArg(args, "disable_prompt", configuration.isPrompt());
          addArg(args, "enable_jar_splitting", configuration.isSplitJars());
          addArg(args, "retain_upload_dir", configuration.isKeepTempUploadDir());
          args.add("update");
          args.add(app);
 
          invokeAppEngine(sdkDir, "com.google.appengine.tools.admin.AppCfg", args.toArray(new String[args.size()]));
 
         delayArchiveDeploy(configuration.getServerURL(), configuration.getStartupTimeout(), 30000L);
 
          return getProtocolMetaData(configuration.getServerURL(), 80, archive);
       }
       catch (Exception e)
       {
          List<String> args = Arrays.asList("rollback", app);
          try
          {
             invokeAppEngine(sdkDir, "com.google.appengine.tools.admin.AppCfg", args.toArray(new String[args.size()]));
          }
          catch (Exception ignored)
          {
          }
          throw new DeploymentException("Cannot deploy to local GAE.", e);
       }
    }
 
    protected void invokeAppEngine(String sdkDir, String appEngineClass, Object args) throws Exception
    {
       final ThreadGroup threads = new ThreadGroup("AppCfgThreadGroup");
       invokeAppEngine(threads, sdkDir, appEngineClass, args);
    }
 
    protected Runnable createRunnable(final ThreadGroup threads, final Method main, final Object args)
    {
       return new Runnable()
       {
          public void run()
          {
             final PrintStream outOrig = System.out;
             final InputStream inOrig = System.in;
             try
             {
                final PipedInputStream inReplace = new PipedInputStream();
                OutputStream stdin;
                try
                {
                   stdin = new PipedOutputStream(inReplace);
                }
                catch (final IOException e)
                {
                   log.log(Level.SEVERE, "Unable to redirect input", e);
                   return;
                }
                System.setIn(inReplace);
 
                final BufferedWriter stdinWriter = new BufferedWriter(new OutputStreamWriter(stdin));
                final Runnable onExpected = new Runnable()
                {
                   public void run()
                   {
                      try
                      {
                         stdinWriter.write(configuration.getPassword());
                         stdinWriter.newLine();
                         stdinWriter.flush();
                      }
                      catch (final IOException e)
                      {
                         log.log(Level.SEVERE, "Unable to enter password", e);
                      }
                   }
                };
                System.setOut(new PrintStream(new PasswordOutputStream(threads, outOrig, onExpected), true));
 
                main.invoke(null, args);
             }
             catch (Exception e)
             {
                throw new RuntimeException(e);
             }
             finally
             {
                System.setOut(outOrig);
                System.setIn(inOrig);
             }
          }
       };
    }
 
    // @author vlads
    private static class PasswordOutputStream extends OutputStream
    {
       private final ThreadGroup threads;
       private final PrintStream out;
       private final Runnable onExpected;
       private final byte[] expect;
       private int match = 0;
 
       public PasswordOutputStream(ThreadGroup threads, PrintStream out, Runnable onExpected)
       {
          this.threads = threads;
          this.out = out;
          this.onExpected = onExpected;
          try
          {
             this.expect = "Password for".getBytes("ASCII");
          }
          catch (UnsupportedEncodingException e)
          {
             throw new RuntimeException(e);
          }
       }
 
       private boolean isRedirectThread()
       {
          ThreadGroup tg = Thread.currentThread().getThreadGroup();
          while ((threads != tg) && (tg != null))
          {
             tg = tg.getParent();
          }
          return (threads == tg);
       }
 
       public void write(int b) throws IOException
       {
          if (isRedirectThread())
          {
             expect((byte) (0xFF & b));
          }
          this.out.write(b);
       }
 
       private synchronized void expect(byte b)
       {
          if (expect[match] == b)
          {
             match++;
             if (match == expect.length)
             {
                match = 0;
                Thread t = new Thread(onExpected, "EnterPasswordThread");
                t.setDaemon(true);
                t.start();
             }
          }
          else
          {
             match = 0;
          }
       }
    }
 }
