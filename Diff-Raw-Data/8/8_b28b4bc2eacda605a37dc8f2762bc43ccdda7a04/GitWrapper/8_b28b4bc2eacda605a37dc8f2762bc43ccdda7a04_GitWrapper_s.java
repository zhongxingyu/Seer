 /*
  * Copyright (C) 2012 eXo Platform SAS.
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
 package org.jvss.git;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.Reader;
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.Collection;
 import java.util.Date;
 
 /**
  * Wraps execution of Git and implements the common Git commands.
  */
 public class GitWrapper implements GitCommandHandler
 {
    private final File repoPath;
 
    private final String gitExecutable;
 
    private final String gitInitialArguments;
 
    private final boolean shellQuoting;
 
    private final String commitEncoding;
 
    private final DateFormat gitDate = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZZZZZ");
 
    /**
     * @param gitExecutable
     * @param gitInitialArguments
     * @param shellQuoting
     * @param commitEncoding
     */
    public GitWrapper(String repoPath, String gitExecutable, String gitInitialArguments, boolean shellQuoting,
       String commitEncoding)
    {
       super();
       this.repoPath = new File(repoPath);
       this.gitExecutable = gitExecutable;
       this.gitInitialArguments = gitInitialArguments;
       this.shellQuoting = shellQuoting;
       this.commitEncoding = commitEncoding;
    }
 
    /**
     * @see org.jvss.git.GitCommandHandler#init()
     */
    @Override
    public boolean init()
    {
       return gitExec("init");
    }
 
    /**
     * @see org.jvss.git.GitCommandHandler#setConfig(java.lang.String,
     *      java.lang.String)
     */
    @Override
    public void setConfig(String name, String value)
    {
       gitExec("config " + name + " " + Quote(value));
 
    }
 
    /**
     * @see org.jvss.git.GitCommandHandler#add(java.lang.String)
     */
    @Override
    public boolean add(String path)
    {
       // add fails if there are no files (directories don't count)
       return gitExec("add -- " + Quote(path), "did not match any files");
    }
 
    /**
     * @see org.jvss.git.GitCommandHandler#add(java.util.Collection)
     */
    @Override
    public boolean add(Collection<String> paths)
    {
       if (paths.isEmpty())
       {
          return false;
       }
       for (String path : paths)
       {
          add(path);
       }
       // add fails if there are no files (directories don't count)
       return true;
    }
 
    /**
     * @see org.jvss.git.GitCommandHandler#addAll()
     */
    @Override
    public boolean addAll()
    {
 
       // add fails if there are no files (directories don't count)
       return gitExec("add -A", "did not match any files");
    }
 
    /**
     * @see org.jvss.git.GitCommandHandler#remove(java.lang.String, boolean)
     */
    @Override
    public void remove(String path, boolean recursive)
    {
       gitExec("rm " + (recursive ? "-r " : "") + "-- " + Quote(path));
 
    }
 
    /**
     * @see org.jvss.git.GitCommandHandler#move(java.lang.String,
     *      java.lang.String)
     */
    @Override
    public void move(String sourcePath, String destPath)
    {
       gitExec("mv -- " + Quote(sourcePath) + " " + Quote(destPath));
    }
 
    /**
     * @see org.jvss.git.GitCommandHandler#commit(java.lang.String,
     *      java.lang.String, java.lang.String, java.util.Date)
     */
    @Override
    public boolean commit(String authorName, String authorEmail, String comment, Date localTime)
    {
 
       String[] env = new String[6];
       env[0] = "GIT_AUTHOR_NAME=" + authorName;
       env[1] = "GIT_AUTHOR_EMAIL=" + authorEmail;
       env[2] = "GIT_AUTHOR_DATE=" + gitDate.format(localTime);//localTime.getTime();
       env[3] = "GIT_COMMITTER_NAME=" + authorName;
       env[4] = "GIT_COMMITTER_EMAIL=" + authorEmail;
       env[5] = "GIT_COMMITTER_DATE=" + gitDate.format(localTime);
 
       // ignore empty commits, since they are non-trivial to detect
       // (e.g. when renaming a directory)
       return gitExec("commit -m " + Quote(comment), "nothing to commit", env);
    }
 
    /**
     * @see org.jvss.git.GitCommandHandler#tag(java.lang.String,
     *      java.lang.String, java.lang.String, java.lang.String, java.util.Date)
     */
    @Override
    public boolean tag(String name, String taggerName, String taggerEmail, String comment, Date localTime)
    {
 
       String[] env = new String[6];
       env[0] = "GIT_AUTHOR_NAME=" + taggerName;
       env[1] = "GIT_AUTHOR_EMAIL=" + taggerEmail;
      env[2] = "GIT_AUTHOR_DATE=" + localTime.getTime();
       env[3] = "GIT_COMMITTER_NAME=" + taggerName;
       env[4] = "GIT_COMMITTER_EMAIL=" + taggerEmail;
      env[5] = "GIT_COMMITTER_DATE=" + localTime.getTime();
 
      return gitExec("tag  -m" + Quote(comment) + " -- " + name, "nothing to commit", env);
    }
 
    private final char QuoteChar = '"';
 
    private final char EscapeChar = '\\';
 
    /// <summary>
    /// Puts quotes around a command-line argument if it includes whitespace
    /// or quotes.
    /// </summary>
    /// <remarks>
    /// There are two things going on in this method: quoting and escaping.
    /// Quoting puts the entire String in quotes, whereas escaping is per-
    /// character. Quoting happens only if necessary, when whitespace or a
    /// quote is encountered somewhere in the String, and escaping happens
    /// only within quoting. Spaces don't need escaping, since that's what
    /// the quotes are for. Slashes don't need escaping because apparently a
    /// backslash is only interpreted as an escape when it precedes a quote.
    /// Otherwise both slash and backslash are just interpreted as directory
    /// separators.
    /// </remarks>
    /// <param name="arg">A command-line argument to quote.</param>
    /// <returns>The given argument, possibly in quotes, with internal
    /// quotes escaped with backslashes.</returns>
    private String Quote(String arg)
    {
       if (isNullOrEmpty(arg))
       {
          return "\"\"";
       }
 
       StringBuilder buf = null;
       for (int i = 0; i < arg.length(); ++i)
       {
          char c = arg.charAt(i);
          if (buf == null && needsQuoting(c))
          {
             buf = new StringBuilder(arg.length() + 2);
             buf.append(QuoteChar);
             buf.append(arg, 0, i);
          }
          if (buf != null)
          {
             if (c == QuoteChar)
             {
                buf.append(EscapeChar);
             }
             buf.append(c);
          }
       }
       if (buf != null)
       {
          buf.append(QuoteChar);
          return buf.toString();
       }
       return arg;
    }
 
    private boolean needsQuoting(char c)
    {
       return Character.isWhitespace(c) || c == QuoteChar || shellQuoting
          && (c == '&' || c == '|' || c == '<' || c == '>' || c == '^' || c == '%');
    }
 
    private boolean gitExec(String args)
    {
       return gitExec(args, null);
    }
 
    private boolean gitExec(String args, String unless)
    {
       return gitExec(args, unless, null);
    }
 
    private boolean gitExec(String args, String unless, String[] envp)
    {
 
       if (gitInitialArguments != null && gitInitialArguments.length() > 0)
       {
          args = gitInitialArguments + " " + args;
       }
 
       try
       {
          Process p = Runtime.getRuntime().exec(gitExecutable + " " + args, envp, repoPath);
 
          String stdout = readString(p.getInputStream());
          String stderr = readString(p.getErrorStream());
 
          int exitCode = p.waitFor();
          if (exitCode != 0)
          {
             if (isNullOrEmpty(unless) || (isNullOrEmpty(stdout) || !stdout.contains(unless))
                && (isNullOrEmpty(stderr) || !stderr.contains(unless)))
             {
                failExitCode(gitExecutable, args, stdout, stderr, exitCode);
             }
          }
          return exitCode == 0;
       }
       catch (IOException e)
       {
          throw new ProcessException("IOException", e, gitExecutable, args);
       }
       catch (InterruptedException e)
       {
          throw new ProcessException("InterruptedException", e, gitExecutable, args);
       }
    }
 
    private static void failExitCode(String exec, String args, String stdout, String stderr, int exitCode)
    {
       throw new ProcessExitException(String.format("git returned exit code %d", exitCode), exec, args, stdout, stderr);
    }
 
    private static String readString(InputStream is) throws IOException
    {
       char[] buf = new char[2048];
       Reader r = new InputStreamReader(is, "UTF-8");
       StringBuilder s = new StringBuilder();
       while (true)
       {
          int n = r.read(buf);
          if (n < 0)
          {
             break;
          }
          s.append(buf, 0, n);
       }
       return s.toString();
    }
 
    private boolean isNullOrEmpty(String param)
    {
       return param == null || param.length() < 1;
    }
 }
