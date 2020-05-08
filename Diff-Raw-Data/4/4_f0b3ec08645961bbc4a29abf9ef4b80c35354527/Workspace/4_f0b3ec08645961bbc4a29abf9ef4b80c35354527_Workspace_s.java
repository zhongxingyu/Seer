 /**
  * Copyright (c) 2011 Sourcepit.org contributors and others. All rights reserved. This program and the accompanying
  * materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this distribution,
  * and is available at http://www.eclipse.org/legal/epl-v10.html
  */
 
 package org.sourcepit.common.testing;
 
 import java.io.File;
 import java.io.IOException;
 
 import org.apache.commons.io.FileUtils;
 import org.junit.rules.ExternalResource;
 import org.junit.runner.Description;
 import org.junit.runners.model.Statement;
 
 /**
  * @author Bernd
  */
 public class Workspace extends ExternalResource
 {
    private String path;
 
    private File baseDir, workspaceDir;
 
    private boolean delete;
 
    public Workspace()
    {
       this(null, true);
    }
 
    public Workspace(File baseDir, String path, boolean delete)
    {
       this(new File(baseDir, path), delete);
    }
    
    public Workspace(File baseDir, boolean delete)
    {
       this.baseDir = baseDir;
       this.delete = delete;
    }
 
    public Statement apply(Statement base, Description description)
    {
       String className = description.getClassName();
       int idx = className.lastIndexOf('.');
       if (idx > -1)
       {
          className = className.substring(idx + 1);
       }
       path = className + "/" + description.getMethodName();
       return super.apply(base, description);
    }
 
    @Override
    protected void before() throws Exception
    {
       if (baseDir == null)
       {
          workspaceDir = newDir();
       }
       else
       {
          workspaceDir = new File(baseDir, path);
          if (workspaceDir.exists())
          {
             delete();
          }
          workspaceDir.mkdirs();
       }
    }
 
    @Override
    protected void after()
    {
       if (delete)
       {
          delete();
       }
       super.after();
    }
 
    public void delete()
    {
       try
       {
          FileUtils.deleteDirectory(workspaceDir);
       }
       catch (IOException e)
       {
       }
    }
 
    /**
     * @return the location of this workspace directory.
     */
    public File getRoot()
    {
       if (workspaceDir == null)
       {
          throw new IllegalStateException("the workspace directory has not yet been created");
       }
       return workspaceDir;
    }
 
    /**
     * Returns a new fresh file with the given name under the workspace directory.
     */
    public File newFile(String fileName) throws IOException
    {
       File file = new File(getRoot(), fileName);
       file.createNewFile();
       return file;
    }
 
    /**
     * Returns a new fresh file with a random name under the workspace directory.
     */
    public File newFile() throws IOException
    {
       return File.createTempFile("file", null, workspaceDir);
    }
 
    /**
     * Returns a new fresh directory with the given name under the workspace directory.
     */
    public File newDir(String... dirNames)
    {
       File file = getRoot();
       for (String dirName : dirNames)
       {
          file = new File(file, dirName);
          file.mkdir();
       }
       return file;
    }
 
    /**
     * Returns a new fresh directory with a random name under the workspace directory.
     */
    public File newDir() throws IOException
    {
       File createdDir = File.createTempFile("junit", "", workspaceDir);
       createdDir.delete();
       createdDir.mkdir();
       return createdDir;
    }
 
    public File importDir(File dir) throws IOException
    {
       File dst = newDir(dir.getName());
       if (dst.isDirectory())
       {
          FileUtils.deleteDirectory(dst);
       }
       else if (dst.isFile())
       {
          if (!dst.delete())
          {
             throw new IOException("Can't delete file " + dst.toString());
          }
       }
       FileUtils.copyDirectory(dir, dst);
       return dst;
    }
 
 }
