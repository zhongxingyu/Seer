 package org.oecd.ant.git;
 
 import static java.lang.Character.valueOf;
 
 import java.io.IOException;
 import java.text.MessageFormat;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.tools.ant.BuildException;
 import org.eclipse.jgit.api.Git;
 import org.eclipse.jgit.api.PushCommand;
 import org.eclipse.jgit.lib.Constants;
 import org.eclipse.jgit.lib.ObjectId;
 import org.eclipse.jgit.lib.ObjectReader;
 import org.eclipse.jgit.lib.Ref;
 import org.eclipse.jgit.transport.PushResult;
 import org.eclipse.jgit.transport.RefSpec;
 import org.eclipse.jgit.transport.RemoteRefUpdate;
 import org.eclipse.jgit.transport.RemoteRefUpdate.Status;
 import org.eclipse.jgit.transport.Transport;
 import org.eclipse.jgit.transport.URIish;
 import org.oecd.ant.git.nested.CredentialsElement;
 
 public class PushTask extends AbstractGitTask {
 	private boolean dryRun;
 	private int timeout = -1;
 	private String remote = Constants.DEFAULT_REMOTE_NAME;
 	private String refSpecs;
 	private boolean all;
 	private boolean tags;
 	private boolean thin = Transport.DEFAULT_PUSH_THIN;
 	private boolean force;
 	private String receivePack;
 
 	private CredentialsElement credentials;
 
 	private boolean shownURI = true;
 
 	public boolean isDryRun() {
 		return dryRun;
 	}
 
 	public void setDryRun(boolean dryRun) {
 		this.dryRun = dryRun;
 	}
 
 	public int getTimeout() {
 		return timeout;
 	}
 
 	public void setTimeout(int timeout) {
 		this.timeout = timeout;
 	}
 
 	public String getRemote() {
 		return remote;
 	}
 
 	public void setRemote(String remote) {
 		this.remote = remote;
 	}
 
 	public String getRefSpecs() {
 		return refSpecs;
 	}
 
 	public void setRefSpecs(String refSpecs) {
 		this.refSpecs = refSpecs;
 	}
 
 	public boolean isAll() {
 		return all;
 	}
 
 	public void setAll(boolean all) {
 		this.all = all;
 	}
 
 	public boolean isTags() {
 		return tags;
 	}
 
 	public void setTags(boolean tags) {
 		this.tags = tags;
 	}
 
 	public boolean isThin() {
 		return thin;
 	}
 
 	public void setThin(boolean thin) {
 		this.thin = thin;
 	}
 
 	public boolean isForce() {
 		return force;
 	}
 
 	public void setForce(boolean force) {
 		this.force = force;
 	}
 
 	public String getReceivePack() {
 		return receivePack;
 	}
 
 	public void setReceivePack(String receivePack) {
 		this.receivePack = receivePack;
 	}
 
 	public CredentialsElement getCredentials() {
 		return credentials;
 	}
 
 	public void addCredentials(CredentialsElement credentials) {
 		if (this.credentials != null)
 			throw new BuildException(":only_one");
 		this.credentials = credentials;
 	}
 
 	@Override
 	protected void checkProperties() throws Exception {
 
 	}
 
 	@Override
 	protected void executeCustom(Git git) throws Exception {
 		PushCommand push = git.push();
 
 		push.setDryRun(dryRun);
 		push.setForce(force);
 		push.setProgressMonitor(new SimpleProgressMonitor(this));
 		push.setReceivePack(receivePack);
 
 		List<RefSpec> rs = new ArrayList<RefSpec>();
		if (refSpecs != null) {
			for (String refspec : refSpecs.split("[ ,]")) {
				rs.add(new RefSpec(refspec));
			}
 		}
 		push.setRefSpecs(rs);
 
 		if (all)
 			push.setPushAll();
 		if (tags)
 			push.setPushTags();
 
 		push.setRemote(remote);
 		push.setThin(thin);
 		push.setTimeout(timeout);
 
 		if (credentials != null)
 			push.setCredentialsProvider(credentials.toCredentialsProvider());
 
 		Iterable<PushResult> results = push.call();
 
 		for (PushResult result : results) {
 			ObjectReader reader = git.getRepository().newObjectReader();
 			try {
 				printPushResult(reader, result.getURI(), result);
 			} finally {
 				reader.release();
 			}
 		}
 	}
 
 	/**
 	 * @see org.eclipse.jgit.pgm.Push
 	 */
 	private void printPushResult(final ObjectReader reader, final URIish uri, final PushResult result) throws IOException {
 		shownURI = false;
 		boolean everythingUpToDate = true;
 
 		// at first, print up-to-date ones...
 		for (final RemoteRefUpdate rru : result.getRemoteUpdates()) {
 			if (rru.getStatus() == Status.UP_TO_DATE) {
 				if (isVerbose())
 					printRefUpdateResult(reader, uri, result, rru);
 			} else
 				everythingUpToDate = false;
 		}
 
 		for (final RemoteRefUpdate rru : result.getRemoteUpdates()) {
 			// ...then successful updates...
 			if (rru.getStatus() == Status.OK)
 				printRefUpdateResult(reader, uri, result, rru);
 		}
 
 		for (final RemoteRefUpdate rru : result.getRemoteUpdates()) {
 			// ...finally, others (problematic)
 			if (rru.getStatus() != Status.OK && rru.getStatus() != Status.UP_TO_DATE)
 				printRefUpdateResult(reader, uri, result, rru);
 		}
 
 		log(result.getMessages());
 		if (everythingUpToDate)
 			log("Everything up-to-date");
 	}
 
 	/**
 	 * @see org.eclipse.jgit.pgm.Push
 	 */
 	private void printRefUpdateResult(final ObjectReader reader, final URIish uri, final PushResult result, final RemoteRefUpdate rru) throws IOException {
 		if (!shownURI) {
 			shownURI = true;
 			log(MessageFormat.format("To {0}", uri));
 		}
 
 		final String remoteName = rru.getRemoteName();
 		final String srcRef = rru.isDelete() ? null : rru.getSrcRef();
 
 		switch (rru.getStatus()) {
 		case OK:
 			if (rru.isDelete())
 				printUpdateLine('-', "[deleted]", null, remoteName, null);
 			else {
 				final Ref oldRef = result.getAdvertisedRef(remoteName);
 				if (oldRef == null) {
 					final String summary;
 					if (remoteName.startsWith(Constants.R_TAGS))
 						summary = "[new tag]";
 					else
 						summary = "[new branch]";
 					printUpdateLine('*', summary, srcRef, remoteName, null);
 				} else {
 					boolean fastForward = rru.isFastForward();
 					final char flag = fastForward ? ' ' : '+';
 					final String summary = safeAbbreviate(reader, oldRef.getObjectId()) + (fastForward ? ".." : "...") //$NON-NLS-1$ //$NON-NLS-2$
 							+ safeAbbreviate(reader, rru.getNewObjectId());
 					final String message = fastForward ? null : "forced update";
 					printUpdateLine(flag, summary, srcRef, remoteName, message);
 				}
 			}
 			break;
 
 		case NON_EXISTING:
 			printUpdateLine('X', "[no match]", null, remoteName, null);
 			break;
 
 		case REJECTED_NODELETE:
 			printUpdateLine('!', "[rejected]", null, remoteName, "remote side does not support deleting refs");
 			break;
 
 		case REJECTED_NONFASTFORWARD:
 			printUpdateLine('!', "[rejected]", srcRef, remoteName, "non-fast forward");
 			break;
 
 		case REJECTED_REMOTE_CHANGED:
 			final String message = MessageFormat.format("remote ref object changed - is not expected one {0}",
 					safeAbbreviate(reader, rru.getExpectedOldObjectId()));
 			printUpdateLine('!', "[rejected]", srcRef, remoteName, message);
 			break;
 
 		case REJECTED_OTHER_REASON:
 			printUpdateLine('!', "[remote rejected]", srcRef, remoteName, rru.getMessage());
 			break;
 
 		case UP_TO_DATE:
 			if (isVerbose())
 				printUpdateLine('=', "[up to date]", srcRef, remoteName, null);
 			break;
 
 		case NOT_ATTEMPTED:
 		case AWAITING_REPORT:
 			printUpdateLine('?', "[unexpected push-process behavior]", srcRef, remoteName, rru.getMessage());
 			break;
 		}
 	}
 
 	/**
 	 * @see org.eclipse.jgit.pgm.Push
 	 */
 	private static String safeAbbreviate(ObjectReader reader, ObjectId id) {
 		try {
 			return reader.abbreviate(id).name();
 		} catch (IOException cannotAbbreviate) {
 			return id.name();
 		}
 	}
 
 	/**
 	 * @see org.eclipse.jgit.pgm.Push
 	 */
 	private void printUpdateLine(final char flag, final String summary, final String srcRef, final String destRef, final String message) throws IOException {
 		StringBuilder builder = new StringBuilder();
 
 		builder.append(String.format(" %c %-17s", valueOf(flag), summary)); //$NON-NLS-1$
 
 		if (srcRef != null)
 			builder.append(String.format(" %s ->", abbreviateRef(srcRef, true))); //$NON-NLS-1$
 		builder.append(String.format(" %s", abbreviateRef(destRef, true))); //$NON-NLS-1$
 
 		if (message != null)
 			builder.append(String.format(" (%s)", message)); //$NON-NLS-1$
 
 		log(builder.toString());
 	}
 
 }
