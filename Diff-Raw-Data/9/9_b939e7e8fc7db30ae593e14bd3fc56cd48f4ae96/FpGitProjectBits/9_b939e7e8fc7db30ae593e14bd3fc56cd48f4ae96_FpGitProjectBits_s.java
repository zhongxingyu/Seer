 /*******************************************************************************
  * Copyright (c) 2010-2011 Red Hat Inc. and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     Red Hat Inc. - initial API and implementation
  *******************************************************************************/
 package org.fedoraproject.eclipse.packager.git;
 
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Set;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.egit.core.project.RepositoryMapping;
 import org.eclipse.jgit.api.FetchCommand;
 import org.eclipse.jgit.api.Git;
 import org.eclipse.jgit.api.errors.InvalidRemoteException;
 import org.eclipse.jgit.api.errors.JGitInternalException;
 import org.eclipse.jgit.errors.NoWorkTreeException;
 import org.eclipse.jgit.lib.ConfigConstants;
 import org.eclipse.jgit.lib.Constants;
 import org.eclipse.jgit.lib.ObjectId;
 import org.eclipse.jgit.lib.Ref;
 import org.eclipse.jgit.lib.Repository;
 import org.eclipse.jgit.revwalk.RevCommit;
 import org.eclipse.jgit.revwalk.RevWalk;
 import org.eclipse.jgit.transport.RefSpec;
 import org.fedoraproject.eclipse.packager.FedoraSSLFactory;
 import org.fedoraproject.eclipse.packager.IFpProjectBits;
 import org.fedoraproject.eclipse.packager.IProjectRoot;
 
 /**
  * Git specific project bits (branches management and such).
  * Implementation of
  * org.fedoraproject.eclipse.packager.vcsContribution
  * extension point.
  * 
  * @author Red Hat Inc.
  *
  */
 public class FpGitProjectBits implements IFpProjectBits {
 	
 	private IResource project; // The underlying project
 	private HashMap<String, String> branches; // All branches
 	private Git git; // The Git repository abstraction for this project
 	private boolean initialized = false; // keep track if instance is initialized
 	private String currentBranch = null;
 	// String regexp pattern used for branch mapping this should basically be the
 	// same pattern as fedpkg uses. ATM this pattern is:
 	// BRANCHFILTER = 'f\d\d\/master|master|el\d\/master|olpc\d\/master'
 	// Severin, 2011-01-11: Make '/master' postfix of branch name optional.
 	private final Pattern BRANCH_PATTERN = Pattern
 			.compile("(?:origin/)?(fc?)(\\d\\d?)(?:/master)?|" + //$NON-NLS-1$
 					"(?:origin/)?(master)|(?:origin/)?(el)(\\d)(?:/master)?|" + //$NON-NLS-1$
 					"(?:origin/)?(olpc)(\\d)(?:/master)?" //$NON-NLS-1$
 			);
 	
 	/**
 	 * See {@link IFpProjectBits#getBranchName(String)}
 	 */
 	@Override
 	public String getBranchName(String branchName) {
 		if (!isInitialized()) {
 			return null;
 		}
 		return this.branches.get(branchName);
 	}
 
 	/**
 	 * Parse current branch from active local branch.
 	 * 
 	 * See {@link IFpProjectBits#getCurrentBranchName()}
 	 */
 	@Override
 	public String getCurrentBranchName() {
 		if (!isInitialized()) {
 			return null;
 		}
 		currentBranch = null;
 		try {
 			// make sure it's a named branch
 			if (!isNamedBranch(this.git.getRepository().getFullBranch())) {
 				return null; // unknown branch!
 			}
 			// get the current head target
 			currentBranch = this.git.getRepository().getBranch();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		return mapBranchName(currentBranch);
 	}
 	
 	@Override
 	public String getRawCurrentBranchName(){
 		getCurrentBranchName();
 		return currentBranch;
 	}
 
 	/**
 	 * See {@link IFpProjectBits#getScmUrl()}
 	 */
 	@Override
 	public String getScmUrl() {
 		if (!isInitialized()) {
 			return null;
 		}
 		String username = FedoraSSLFactory.getInstance().getUsernameFromCert();
 		String packageName = this.project.getProject().getName();
 		if (username.equals("anonymous")) { //$NON-NLS-1$
 			return "git://pkgs.fedoraproject.org/" + packageName + ".git"; //$NON-NLS-1$ //$NON-NLS-2$
 		} else {
 			return "ssh://" + username + "@pkgs.fedoraproject.org/" //$NON-NLS-1$ //$NON-NLS-2$
 					+ packageName + ".git"; //$NON-NLS-1$
 		}
 	}
 	
 	/**
 	 * Git should always return anonymous checkout with git protocol for koji.
 	 * 
 	 * @see org.fedoraproject.eclipse.packager.IFpProjectBits#getScmUrlForKoji(IProjectRoot)
 	 */
 	@Override
 	public String getScmUrlForKoji(IProjectRoot projectRoot) {
 		if (!isInitialized()) {
 			return null;
 		}
 		String packageName = this.project.getProject().getName();
 		return "git://pkgs.fedoraproject.org/" + packageName + ".git?#" + getCommitHash(); //$NON-NLS-1$ //$NON-NLS-2$
 	}
 	
 	/**
 	 * Get the SHA1 representing the current branch.
 	 * 
 	 * @return The SHA1 as hex in String form.
 	 */
 	private String getCommitHash() {
 		String commitHash = null;
 		try {
 			String currentBranchRefString = git.getRepository().getFullBranch();
 			Ref ref = git.getRepository().getRef(currentBranchRefString);
 			commitHash = ref.getObjectId().getName();
 		} catch (IOException ioException) {
 			ioException.printStackTrace();
 		}
 		return commitHash;
 	}
 
 	/**
 	 * Parse available branch names from Git remote branches.
 	 * 
 	 * @return
 	 */
 	private HashMap<String, String> getBranches() {
 		HashMap<String, String> branches = new HashMap<String, String>();
 		try {
 			Map<String, Ref> remotes = git.getRepository().getRefDatabase().getRefs(
 					Constants.R_REMOTES);
 			Set<String> keyset = remotes.keySet();
 			String branch;
 			for (String key : keyset) {
 				// use shortenRefName() to get rid of refs/*/ prefix
 				branch = Repository.shortenRefName(remotes.get(key).getName());
 				branch = mapBranchName(branch); // do the branch name mapping
 				if (branch != null) {
 					branches.put(branch, branch);
 				}
 			}
 		} catch (IOException ioException) {
 			ioException.printStackTrace();
 		}
 		return branches;
 	}
 
 	/**
 	 * Do instance specific initialization.
 	 * 
 	 * See {@link IFpProjectBits#initialize(IProjectRoot)}
 	 */
 	@Override
 	public void initialize(IProjectRoot fedoraprojectRoot) {
 		this.project = fedoraprojectRoot.getProject();
 		// now set Git Repository object
 		this.git = new Git(getGitRepository());
 		this.branches = getBranches();
 		this.initialized = true;
 	}
 	
 	/**
 	 * Determine if instance has been properly initialized
 	 */
 	private boolean isInitialized() {
 		return this.initialized;
 	}
 
 	/**
 	 * Determine distribution qualifier. This is VCS specific because
 	 * branch determination is VCS specific.
 	 * 
 	 * See {@link IFpProjectBits#getDist()}
 	 */
 	@Override
 	public String getDist() {
 		String currBranch = getCurrentBranchName();
 		if (currBranch.startsWith("F-") || currBranch.startsWith("FC-")) { //$NON-NLS-1$ //$NON-NLS-2$
 			return ".fc" + getDistVal(); //$NON-NLS-1$
 		} else if (currBranch.startsWith("EL-")) { //$NON-NLS-1$
 			return ".el" + getDistVal(); //$NON-NLS-1$
 		} else if (currBranch.startsWith("OLPC-")) {  //$NON-NLS-1$
 			return ".olpc" + getDistVal(); //$NON-NLS-1$
 		} else if (currBranch.equals("devel")) { //$NON-NLS-1$
 			return ".fc" + determineNextReleaseNumber(); //$NON-NLS-1$
 		}
 		return null;
 	}
 	
 	/**
 	 * See {@link IFpProjectBits#getDistVal()}
 	 */
 	@Override
 	public String getDistVal() {
 		String currBranch = getCurrentBranchName();
 		if (currBranch.equals("devel")) { //$NON-NLS-1$
 			return determineNextReleaseNumber();
 		}
 		return currBranch.split("-")[1]; //$NON-NLS-1$
 	}
 
 	/**
 	 * See {@link IFpProjectBits#getDistVariable()}
 	 */
 	@Override
 	public String getDistVariable() {
 		String currBranch = getCurrentBranchName();
 		if (currBranch.startsWith("F-") || currBranch.startsWith("FC-")) { //$NON-NLS-1$ //$NON-NLS-2$
 			return "fedora"; //$NON-NLS-1$" +
 		} else if (currBranch.startsWith("EL-")) { //$NON-NLS-1$
 			return "rhel"; //$NON-NLS-1$
 		} else if (currBranch.startsWith("OLPC-")) {  //$NON-NLS-1$
 			return "olpc"; //$NON-NLS-1$
 		} else if (currBranch.equals("devel")) { //$NON-NLS-1$
 			return "fedora"; //$NON-NLS-1$
 		}
 		return null;
 	}
 
 	/**
 	 * See {@link IFpProjectBits#getTarget()}
 	 */
 	@Override
 	public String getTarget() {
 		String currBranch = getCurrentBranchName();
 		if (currBranch.startsWith("F-") || currBranch.startsWith("FC-")) { //$NON-NLS-1$ //$NON-NLS-2$
			return "dist-f" + getDistVal() + "-updates-candidate"; //$NON-NLS-1$" //$NON-NLS-2$
 		} else if (currBranch.startsWith("EL-")) { //$NON-NLS-1$
 			return "dist-" + getDistVal() + "E-epel-testing-candidate"; //$NON-NLS-1$ //$NON-NLS-2$
 		} else if (currBranch.startsWith("OLPC-")) {  //$NON-NLS-1$
 			return "dist-olpc" + getDistVal(); //$NON-NLS-1$
 		} else if (currBranch.equals("devel")) { //$NON-NLS-1$
 			return "dist-rawhide"; //$NON-NLS-1$
 		}
 		return null;
 	}
 
 	/**
 	 * Maps branch names to the internal format used by all IFpProjectBits
 	 * implementations. For example <code>mapBranchName("f8")</code> would
 	 * return <code>"F-8"</code> and <code>mapBranchName("master")</code> would
 	 * return <code>"devel"</code>.
 	 * 
 	 * @param from
 	 *            The original raw branch name with "refs/something"
 	 *            prefixes omitted.
 	 * @return The mapped branch name.
 	 */
 	private String mapBranchName(String from) {
 		String prefix, version;
 		Matcher branchMatcher = BRANCH_PATTERN.matcher(from);
 		if (!branchMatcher.matches()) {
 			// This should never happen. Maybe something wrong with the regular
 			// expression?
 			return null;
 		}
 		for (int i = 1; i < branchMatcher.groupCount(); i++) {
 			prefix = branchMatcher.group(i); // null if group didn't match at all
 			version = branchMatcher.group(i+1);
 			if (version == null && prefix != null && prefix.equals(Constants.MASTER)) {
 				// matched master
 				return "devel"; //$NON-NLS-1$
 			} else if (version != null && prefix != null) {
 				// F, EPEL, OLPC matches
 				return prefix.toUpperCase() + "-" + version; //$NON-NLS-1$
 			}
 		}
 		// something's fishy
 		return null;
 	}
 	
 	/**
 	 * Returns true if given branch name is NOT an ObjectId in string format.
 	 * I.e. if branchName has been created by doing repo.getBranch(), it would
 	 * return SHA1 Strings for remote branches. We don't want that.
 	 * 
 	 * @param branchName
 	 * @return
 	 */
 	private boolean isNamedBranch(String branchName) {
 		if (branchName.startsWith(Constants.R_HEADS)
 				|| branchName.startsWith(Constants.R_TAGS)
 				|| branchName.startsWith(Constants.R_REMOTES)) {
 			return true;
 		}
 		return false;
 	}
 
 	/**
 	 * See {@link IFpProjectBits#updateVCS(IProjectRoot, IProgressMonitor)}
 	 */
 	@Override
 	public IStatus updateVCS(IProjectRoot projectRoot,
 			IProgressMonitor monitor) {
 		// FIXME: Not working just, yet. Use projectRoot and monitor!.
 //		return performPull();
 		// Return OK status to not see NPEs
 		return Status.OK_STATUS;
 	}
 	
 	/**
 	 * Pull "sources" and ".gitignore".
 	 * 
 	 * TODO: Clean this up a little
 	 */
 	private IStatus performPull() {
 		//FIXME: NO-OP. Use git.pull() API
 		return null;
 	}
 	
 	/**
 	 * Get the JGit repository.
 	 */
 	private Repository getGitRepository() {
 		RepositoryMapping  repoMapping = RepositoryMapping.getMapping(project);
 		return repoMapping.getRepository();
 	}
 	
 	/**
 	 * Determine what the next release number (in terms of the
 	 * distribution) will be.
 	 * 
 	 * @return The next release number in String representation
 	 */
 	private String determineNextReleaseNumber() {
 		if (!isInitialized()) {
 			return null;
 		}
 		// Try to guess the next release number based on existing branches
 		Set<String> keySet = this.branches.keySet();
 		String branchName;
 		int maxRelease = -1;
 		for (String key : keySet) {
 			branchName = this.branches.get(key);
 			if (branchName.startsWith("F-") || branchName.startsWith("FC-")) { //$NON-NLS-1$ //$NON-NLS-2$
 				// fedora
 				maxRelease = Math.max(maxRelease, Integer.parseInt(branchName.substring("F-".length()))); //$NON-NLS-1$
 			} else if (branchName.startsWith("EL-")) { //$NON-NLS-1$
 				// EPEL
 				maxRelease = Math.max(maxRelease, Integer.parseInt(branchName.substring("EL-".length()))); //$NON-NLS-1$
 			} else if (branchName.startsWith("OLPC-")) {  //$NON-NLS-1$
 				// OLPC
 				maxRelease = Math.max(maxRelease, Integer.parseInt(branchName.substring("OLPC-".length()))); //$NON-NLS-1$
 			}
 			// ignore
 		}
 		if (maxRelease == -1) {
 			// most likely a new package. ATM this is F-15
 			return "15"; //$NON-NLS-1$
 		} else {
 			return Integer.toString(maxRelease + 1);
 		}
 	}
 	
 	@Override
 	public IStatus ignoreResource(IResource resourceToIgnore) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	/**
 	 * Determine if Git tag exists.
 	 * 
 	 * See {@link IFpProjectBits#isVcsTagged(IProjectRoot, String)}
 	 */
 	@Override
 	public boolean isVcsTagged(IProjectRoot fedoraProjectRoot, String tag) {
 		if (!isInitialized()) {
 			return false; // If we are not initialized we can't go any further!
 		}
 		// Look at tags and see if we can find the tag in question.
 		Map<String, Ref> remotes = this.git.getRepository().getTags();
 		if (remotes != null) {
 			Set<String> keyset = remotes.keySet();
 			String currentTag;
 			for (String key : keyset) {
 				// use shortenRefName() to get rid of refs/*/ prefix
 				currentTag = Repository.shortenRefName(remotes.get(key)
 						.getName());
 				if (tag.equals(currentTag)) {
 					return true; // tag found
 				}
 			}
 		}
 		return false;
 	}
 
 	/**
 	 * Create new Git tag.
 	 * 
 	 * See {@link IFpProjectBits#tagVcs(IProjectRoot, IProgressMonitor)}
 	 */
 	@Override
 	public IStatus tagVcs(IProjectRoot projectRoot,
 			IProgressMonitor monitor) {
 		if (!isInitialized()) {
 			return new Status(IStatus.ERROR, Activator.PLUGIN_ID, "Git tag error. Not initialized!"); //$NON-NLS-1$
 		}
 		//FIXME: no-op ATM. use git.tag().
 		return new Status(IStatus.OK, Activator.PLUGIN_ID, "Tag succeeded!"); //$NON-NLS-1$
 	}
 
 	/**
 	 * Fedora git doesn't need to tag because commit hashes are used.
 	 * 
 	 * @see org.fedoraproject.eclipse.packager.IFpProjectBits#needsTag()
 	 */
 	@Override
 	public boolean needsTag() {
 		return false;
 	}
 
 	/**
 	 * Determine if there are unpushed changes on the current branch.
 	 * @return If there are unpushed changes.
 	 */
 	@Override
 	public boolean hasLocalChanges(IProjectRoot fedoraProjectRoot) {
 		if (!isInitialized()) {
 			// FIXME: raise exception instead.
 			return true; // If we are not initialized we can't go any further!
 		}
 		try {
 			// get remote ref from config
 			String branchName = git.getRepository().getBranch();
 			String trackingRemoteBranch = git
 			.getRepository()
 			.getConfig()
 			.getString(ConfigConstants.CONFIG_BRANCH_SECTION,
 					branchName, ConfigConstants.CONFIG_KEY_MERGE);
 			///////////////////////////////////////////////////////////
 			// FIXME: Temp work-around for Eclipse EGit/JGit BZ #317411
 			FetchCommand fetch = git.fetch();
 			fetch.setRemote("origin"); //$NON-NLS-1$
 			fetch.setTimeout(0);
 			// Fetch refs for current branch; account for f14 + f14/master like
 			// branch names. Need to fetch into remotes/origin/f14/master since
 			// this is what is later used for local changes comparison.
 			String fetchBranchSpec = Constants.R_HEADS + branchName + ":" + //$NON-NLS-1$
 				Constants.R_REMOTES + "origin/" + branchName; //$NON-NLS-1$
 			if (trackingRemoteBranch != null) {
 				// have f14/master like branch
 				trackingRemoteBranch = trackingRemoteBranch.substring(Constants.R_HEADS.length());
 				fetchBranchSpec = Constants.R_HEADS + trackingRemoteBranch + ":" + //$NON-NLS-1$
 					Constants.R_REMOTES + "origin/" + trackingRemoteBranch; //$NON-NLS-1$
 			}
 			RefSpec spec = new RefSpec(fetchBranchSpec);
 			fetch.setRefSpecs(spec);
 			try {
 				fetch.call();
 			} catch (JGitInternalException e) {
 				e.printStackTrace();
 			} catch (InvalidRemoteException e) {
 				e.printStackTrace();
 			}
 			//--- End temp work-around for EGit/JGit bug.
 			
 			RevWalk rw = new RevWalk(git.getRepository());
 			ObjectId objHead = git.getRepository().resolve(branchName);
 			if (trackingRemoteBranch == null) {
 				// no config yet, assume plain brach name.
 				trackingRemoteBranch = branchName;
 			}
 			RevCommit commitHead = rw.parseCommit(objHead);
 			ObjectId objRemoteTrackingHead = git.getRepository().resolve("origin/" +  //$NON-NLS-1$
 					trackingRemoteBranch);				
 		    RevCommit remoteCommitHead = rw.parseCommit(objRemoteTrackingHead);
 			return !commitHead.equals(remoteCommitHead);
 		} catch (NoWorkTreeException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		return true;
 	}
 
 
 }
