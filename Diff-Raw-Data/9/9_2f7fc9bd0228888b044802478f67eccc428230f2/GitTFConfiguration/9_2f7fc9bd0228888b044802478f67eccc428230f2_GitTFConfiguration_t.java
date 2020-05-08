 /***********************************************************************************************
  * Copyright (c) Microsoft Corporation All rights reserved.
  * 
  * MIT License:
  * 
  * Permission is hereby granted, free of charge, to any person obtaining a copy
  * of this software and associated documentation files (the "Software"), to deal
  * in the Software without restriction, including without limitation the rights
  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  * copies of the Software, and to permit persons to whom the Software is
  * furnished to do so, subject to the following conditions:
  * 
  * The above copyright notice and this permission notice shall be included in
  * all copies or substantial portions of the Software.
  * 
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
  * SOFTWARE.
  ***********************************************************************************************/
 
 package com.microsoft.gittf.core.config;
 
 import java.io.IOException;
 import java.net.URI;
 import java.net.URISyntaxException;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
import org.eclipse.jgit.lib.ConfigConstants;
import org.eclipse.jgit.lib.CoreConfig.AutoCRLF;
 import org.eclipse.jgit.lib.Repository;
 
 import com.microsoft.gittf.core.GitTFConstants;
 import com.microsoft.gittf.core.Messages;
 import com.microsoft.gittf.core.OutputConstants;
 import com.microsoft.gittf.core.util.Check;
 
 /**
  * Configuration data for the git-tf command, read from the .git/config file.
  * 
  * Note: all data is saved in plain text to the git configuration, no attempt to
  * obscure (or otherwise poorly encrypt) potentially sensitive data (for
  * example, username or password) is provided. Clients must provide warning to
  * the user before saving sensitive information to the git repository and should
  * use other secure storage mechanisms (eg, DPAPI, Keychain, etc) whenever
  * possible.
  * 
  * @threadsafety unknown
  */
 public class GitTFConfiguration
 {
     private static final Log log = LogFactory.getLog(GitTFConfiguration.class);
 
     private final URI serverURI;
     private final String tfsPath;
     private final String username;
     private final String password;
     private final boolean deep;
     private final boolean tag;
     private final int fileFormatVersion;
     private final String buildDefinition;
 
     /**
      * Creates a new git-tf configuration, suitable for use by the command or
      * for writing to the repository's git configuration.
      * 
      * @param serverURI
      *        The URI of the TFS server (must not be <code>null</code>)
      * @param tfsPath
      *        The server path that will be bridged to the git repository (must
      *        not be <code>null</code>)
      * @param username
      *        The username to connect to TFS as or <code>null</code> if no
      *        username should be saved
      * @param password
      *        The password to connect to TFS as or <code>null</code> if no
      *        password should be saved
      * @param depth
      *        The default "depth" for operations
      */
     public GitTFConfiguration(
         final URI serverURI,
         final String tfsPath,
         final String username,
         final String password,
         final boolean deep,
         final boolean tag,
         final int fileFormatVersion,
         final String buildDefinition)
     {
         Check.notNull(serverURI, "serverURI"); //$NON-NLS-1$
         Check.notNullOrEmpty(tfsPath, "tfsPath"); //$NON-NLS-1$
 
         this.serverURI = serverURI;
         this.tfsPath = tfsPath;
         this.username = username;
         this.password = password;
         this.deep = deep;
         this.tag = tag;
         this.fileFormatVersion = fileFormatVersion;
         this.buildDefinition = buildDefinition;
     }
 
     /**
      * @return The URI of the TFS server (never <code>null</code>)
      */
     public URI getServerURI()
     {
         return serverURI;
     }
 
     /**
      * @return The server path bridged to the git repository (never
      *         <code>null</code>)
      */
     public String getServerPath()
     {
         return tfsPath;
     }
 
     /**
      * Returns the username to connect to TFS as. If none has been saved, the
      * client should attempt to connect with default credentials (Kerberos) if
      * available and provide a prompt to the user if those are not available or
      * do not succeed.
      * 
      * @return The username to connect to TFS as, or <code>null</code> if none
      *         has been saved
      */
     public String getUsername()
     {
         return username;
     }
 
     /**
      * Returns the password to connect to TFS with. If none has been saved but
      * the username has been saved, the client should prompt for password.
      * 
      * @return The password to connect to TFS with, or <code>null</code> if none
      *         has been saved
      */
     public String getPassword()
     {
         return password;
     }
 
     /**
      * Returns the default "depth" for operations - if this value is
      * <code>1</code>, operations are "shallow" by default, meaning that
      * multiple git commits will be squashed to a single TFS changeset when
      * checking in, and multiple TFS changesets will be squashed to a single git
      * commit when fetching. If this value is {@link Integer#MAX_VALUE}, then
      * operations are "deep" and there will be a 1:1 correspondence between git
      * commits and TFS changesets whenever possible. Values between
      * <code>1</code> and {@link Integer#MAX_VALUE} will preserve some history.
      * 
      * @return the default depth
      */
     public boolean getDeep()
     {
         return deep;
     }
 
     public boolean getTag()
     {
         return tag;
     }
 
     public int getFileFormatVersion()
     {
         return fileFormatVersion;
     }
 
     public String getBuildDefinition()
     {
         return buildDefinition;
     }
 
     /**
      * Saves this configuration to the given git repository's configuration.
      * 
      * @param repository
      *        The {@link Repository} to save this configuration to (must not be
      *        <code>null</code>)
      * @return <code>true</code> if the configuration was saved successfully
      */
     public boolean saveTo(Repository repository)
     {
         Check.notNull(repository, "repository"); //$NON-NLS-1$
 
         repository.getConfig().setString(
             ConfigurationConstants.CONFIGURATION_SECTION,
             ConfigurationConstants.SERVER_SUBSECTION,
             ConfigurationConstants.SERVER_COLLECTION_URI,
             serverURI.toASCIIString());
 
         repository.getConfig().setString(
             ConfigurationConstants.CONFIGURATION_SECTION,
             ConfigurationConstants.SERVER_SUBSECTION,
             ConfigurationConstants.SERVER_PATH,
             tfsPath);
 
         repository.getConfig().setInt(
             ConfigurationConstants.CONFIGURATION_SECTION,
             ConfigurationConstants.GENERAL_SUBSECTION,
             ConfigurationConstants.DEPTH,
             deep ? Integer.MAX_VALUE : 1);
 
         repository.getConfig().setInt(
             ConfigurationConstants.CONFIGURATION_SECTION,
             ConfigurationConstants.GENERAL_SUBSECTION,
             ConfigurationConstants.FILE_FORMAT_VERSION,
             GitTFConstants.GIT_TF_CURRENT_FORMAT_VERSION);
 
         repository.getConfig().setBoolean(
             ConfigurationConstants.CONFIGURATION_SECTION,
             ConfigurationConstants.GENERAL_SUBSECTION,
             ConfigurationConstants.TAG,
             tag);
 
         if (buildDefinition != null && buildDefinition.length() > 0)
         {
             repository.getConfig().setString(
                 ConfigurationConstants.CONFIGURATION_SECTION,
                 ConfigurationConstants.SERVER_SUBSECTION,
                 ConfigurationConstants.GATED_BUILD_DEFINITION,
                 buildDefinition);
         }
 
        repository.getConfig().setEnum(
            ConfigConstants.CONFIG_CORE_SECTION,
            null,
            ConfigConstants.CONFIG_KEY_AUTOCRLF,
            AutoCRLF.FALSE);

         try
         {
             repository.getConfig().save();
         }
         catch (IOException e)
         {
             log.error("Could not save server configuration to repository", e); //$NON-NLS-1$
             return false;
         }
 
         return true;
     }
 
     public String toString()
     {
         StringBuilder result = new StringBuilder();
 
         result.append(Messages.formatString("GitTFConfiguration.ToString.ServerURIFormat", this.serverURI) + OutputConstants.NEW_LINE); //$NON-NLS-1$
         result.append(Messages.formatString("GitTFConfiguration.ToString.TfsPathFormat", this.tfsPath) + OutputConstants.NEW_LINE); //$NON-NLS-1$
 
         if (buildDefinition != null && buildDefinition.length() > 0)
         {
             result.append(Messages.formatString("GitTFConfiguration.ToString.GatedBuildFormat", this.buildDefinition) + OutputConstants.NEW_LINE); //$NON-NLS-1$
         }
 
         result.append(Messages.formatString("GitTFConfiguration.ToString.DepthFormat", getDepthString()) + OutputConstants.NEW_LINE); //$NON-NLS-1$
         result.append(Messages.formatString("GitTFConfiguration.ToString.TagFormat", this.tag)); //$NON-NLS-1$
 
         return result.toString();
     }
 
     private String getDepthString()
     {
         if (getDeep())
         {
             return Messages.getString("GitTFConfiguration.Deep"); //$NON-NLS-1$
         }
         else
         {
             return Messages.getString("GitTFConfiguration.Shallow"); //$NON-NLS-1$
         }
     }
 
     /**
      * Loads the git-tf configuration from the given git repository.
      * 
      * @param repository
      *        The {@link Repository} to load git-tf configuration data from
      *        (must not be <code>null</code>)
      * @return A new {@link GitTFConfiguration}, or <code>null</code> if the git
      *         repository does not contain a valid git-tf configuration
      */
     public static GitTFConfiguration loadFrom(Repository repository)
     {
         Check.notNull(repository, "repository"); //$NON-NLS-1$
 
         final String projectCollection =
             repository.getConfig().getString(
                 ConfigurationConstants.CONFIGURATION_SECTION,
                 ConfigurationConstants.SERVER_SUBSECTION,
                 ConfigurationConstants.SERVER_COLLECTION_URI);
 
         final String tfsPath =
             repository.getConfig().getString(
                 ConfigurationConstants.CONFIGURATION_SECTION,
                 ConfigurationConstants.SERVER_SUBSECTION,
                 ConfigurationConstants.SERVER_PATH);
 
         final String username =
             repository.getConfig().getString(
                 ConfigurationConstants.CONFIGURATION_SECTION,
                 ConfigurationConstants.SERVER_SUBSECTION,
                 ConfigurationConstants.USERNAME);
 
         final String password =
             repository.getConfig().getString(
                 ConfigurationConstants.CONFIGURATION_SECTION,
                 ConfigurationConstants.SERVER_SUBSECTION,
                 ConfigurationConstants.PASSWORD);
 
         final int depth =
             repository.getConfig().getInt(
                 ConfigurationConstants.CONFIGURATION_SECTION,
                 ConfigurationConstants.GENERAL_SUBSECTION,
                 ConfigurationConstants.DEPTH,
                 GitTFConstants.GIT_TF_SHALLOW_DEPTH);
 
         final boolean tag =
             repository.getConfig().getBoolean(
                 ConfigurationConstants.CONFIGURATION_SECTION,
                 ConfigurationConstants.GENERAL_SUBSECTION,
                 ConfigurationConstants.TAG,
                 true);
 
         final int fileFormatVersion =
             repository.getConfig().getInt(
                 ConfigurationConstants.CONFIGURATION_SECTION,
                 ConfigurationConstants.GENERAL_SUBSECTION,
                 ConfigurationConstants.FILE_FORMAT_VERSION,
                 0);
 
         final String buildDefinition =
             repository.getConfig().getString(
                 ConfigurationConstants.CONFIGURATION_SECTION,
                 ConfigurationConstants.SERVER_SUBSECTION,
                 ConfigurationConstants.GATED_BUILD_DEFINITION);
 
         if (projectCollection == null)
         {
             log.error("No project collection configuration in repository"); //$NON-NLS-1$
             return null;
         }
 
         if (tfsPath == null)
         {
             log.error("No TFS server path configuration in repository"); //$NON-NLS-1$
             return null;
         }
 
         URI serverURI;
 
         try
         {
             serverURI = new URI(projectCollection);
         }
         catch (URISyntaxException e)
         {
             log.error("TFS project collection URI is malformed", e); //$NON-NLS-1$
             return null;
         }
 
         return new GitTFConfiguration(
             serverURI,
             tfsPath,
             username,
             password,
             depth > GitTFConstants.GIT_TF_SHALLOW_DEPTH,
             tag,
             fileFormatVersion,
             buildDefinition);
     }
 
     /**
      * Removes the git-tf configuration data from a git repository.
      * 
      * @param repository
      *        The {@link Repository} to remove configuration from (must not be
      *        <code>null</code>)
      */
     public static void removeFrom(final Repository repository)
     {
         Check.notNull(repository, "repository"); //$NON-NLS-1$
 
         repository.getConfig().unsetSection(
             ConfigurationConstants.CONFIGURATION_SECTION,
             ConfigurationConstants.SERVER_SUBSECTION);
 
         repository.getConfig().unsetSection(
             ConfigurationConstants.CONFIGURATION_SECTION,
             ConfigurationConstants.GENERAL_SUBSECTION);
     }
 }
