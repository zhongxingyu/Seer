 package org.eclipse.dltk.ssh.internal.core;
 
 import java.io.BufferedInputStream;
 import java.io.BufferedOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.util.Vector;
 
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.dltk.core.DLTKCore;
 import org.eclipse.dltk.ssh.core.ISshConnection;
 import org.eclipse.dltk.ssh.core.ISshFileHandle;
 import org.eclipse.jsch.core.IJSchService;
 
 import com.jcraft.jsch.ChannelSftp;
 import com.jcraft.jsch.JSchException;
 import com.jcraft.jsch.Session;
 import com.jcraft.jsch.SftpATTRS;
 import com.jcraft.jsch.SftpException;
 import com.jcraft.jsch.UIKeyboardInteractive;
 import com.jcraft.jsch.UserInfo;
 
 /**
  * TODO: Add correct operation synchronization.
  * 
  */
 public class SshConnection implements ISshConnection {
 	private long disabledTime = 0;
 
 	private final class LocalUserInfo implements UserInfo,
 			UIKeyboardInteractive {
 		public void showMessage(String arg0) {
 		}
 
 		public boolean promptYesNo(String arg0) {
 			return false;
 		}
 
 		public boolean promptPassword(String arg0) {
 			return true;
 		}
 
 		public boolean promptPassphrase(String arg0) {
 			return false;
 		}
 
 		public String getPassword() {
 			return password;
 		}
 
 		public String getPassphrase() {
 			return "";
 		}
 
 		public String[] promptKeyboardInteractive(String destination,
 				String name, String instruction, String[] prompt, boolean[] echo) {
 			final String p = password;
 			return p != null ? new String[] { p } : null;
 		}
 	}
 
 	private class Operation {
 		boolean finished = false;
 
 		public void perform() throws JSchException, SftpException {
 
 		}
 
 		public void setFinished() {
 			finished = true;
 		}
 
 		public boolean isFinished() {
 			return finished;
 		}
 
 		public void failed() {
 		}
 	}
 
 	private class GetStatOperation extends Operation {
 		private IPath path;
 		private SftpATTRS attrs;
 
 		public GetStatOperation(IPath path) {
 			this.path = path;
 		}
 
 		public void perform() throws JSchException, SftpException {
 			attrs = getChannel().stat(path.toString());
 		}
 
 		public SftpATTRS getAttrs() {
 			return attrs;
 		}
 	}
 
 	private class ResolveLinkOperation extends Operation {
 		private IPath path;
 		private IPath resolvedPath;
 
 		public ResolveLinkOperation(IPath path) {
 			this.path = path;
 		}
 
 		public void perform() throws JSchException, SftpException {
 			SftpATTRS attrs = channel.stat(path.toString());
 			boolean isRoot = (path.segmentCount() == 0);
 			String linkTarget = null;
 			String canonicalPath;
 			String parentPath = path.removeLastSegments(1).toString();
 			if (attrs.isLink() && !isRoot) {
 				try {
 					String fullPath = path.toString();
 					boolean readlinkDone = false;
 					try {
 						linkTarget = channel.readlink(fullPath);
 						readlinkDone = true;
 					} catch (Throwable t) {
 						channel.cd(fullPath);
 						linkTarget = channel.pwd();
 						canonicalPath = linkTarget;
 					}
 					if (linkTarget != null && !linkTarget.equals(fullPath)) {
 						if (readlinkDone) {
 							String curdir = channel.pwd();
 							if (!parentPath.equals(curdir)) {
 								channel.cd(parentPath);
 							}
 						}
 						SftpATTRS attrsTarget = channel.stat(linkTarget);
 						if (readlinkDone && attrsTarget.isDir()) {
 							channel.cd(fullPath);
 							canonicalPath = channel.pwd();
 						}
 					} else {
 						linkTarget = null;
 					}
 				} catch (Exception e) {
 					if (e instanceof SftpException
 							&& ((SftpException) e).id == ChannelSftp.SSH_FX_NO_SUCH_FILE) {
 						if (linkTarget == null) {
 							linkTarget = ":dangling link"; //$NON-NLS-1$
 						} else {
 							linkTarget = ":dangling link:" + linkTarget; //$NON-NLS-1$
 						}
 					}
 				}
 				resolvedPath = new Path(linkTarget);
 			}
 		}
 
 		public IPath getResolvedPath() {
 			return resolvedPath;
 		}
 	}
 
 	private class GetOperation extends Operation {
 		private IPath path;
 		private InputStream stream;
 
 		public GetOperation(IPath path) {
 			this.path = path;
 		}
 
 		public void perform() throws JSchException, SftpException {
 			stream = channel.get(path.toString());
 			performStreamOperation = true;
 		}
 
 		@Override
 		public void failed() {
 			// channel.disconnect();
 			// channel = null;
 		}
 
 		public InputStream getStream() {
 			if (stream != null) {
 				InputStream wrapperStream = new BufferedInputStream(stream,
 						32000) {
 					@Override
 					public void close() throws IOException {
 						super.close();
 						// channel.disconnect();
 						// channel = null;
 						doneStreamOperation();
 					}
 				};
 				return wrapperStream;
 			}
 			return stream;
 		}
 
 	}
 
 	protected synchronized void doneStreamOperation() {
 		performStreamOperation = false;
 		notifyAll();
 	}
 
 	private class PutOperation extends Operation {
 		private IPath path;
 		private OutputStream stream;
 
 		public PutOperation(IPath path) {
 			this.path = path;
 		}
 
 		public void perform() throws JSchException, SftpException {
 			stream = channel.put(path.toString(), getChannel().OVERWRITE);
 			performStreamOperation = true;
 		}
 
 		public OutputStream getStream() {
 			if (stream != null) {
 				OutputStream wrapperStream = new BufferedOutputStream(stream,
 						32000) {
 					@Override
 					public void close() throws IOException {
 						super.close();
 						channel.disconnect();
 						channel = null;
 						doneStreamOperation();
 					}
 				};
 				return wrapperStream;
 			}
 			return stream;
 		}
 	}
 
 	private class ListFolderOperation extends Operation {
 		private IPath path;
 		private Vector v;
 
 		public ListFolderOperation(IPath path) {
 			this.path = path;
 		}
 
 		public void perform() throws JSchException, SftpException {
 			v = getChannel().ls(path.toString());
 		}
 
 		public Vector getVector() {
 			return v;
 		}
 	}
 
 	private static final int DEFAULT_RETRY_COUNT = 2;
 
 	private static final long TIMEOUT = 3000; // One second timeout
 
 	private Session session;
 	private String userName;
 	private String password;
 	private int port;
 	private ChannelSftp channel;
 
 	private String hostName;
 
 	public SshConnection(String userName, String hostName, int port) {
 		this.userName = userName;
 		this.hostName = hostName;
 		this.port = port;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.eclipse.dltk.ssh.core.ISshConnection#setPassword(java.lang.String)
 	 */
 	public void setPassword(String password) {
 		this.password = password;
 	}
 
 	public boolean connect() {
 		return connect(0);
 	}
 
 	public synchronized boolean connect(int trycount) {
 		try {
 			if (session == null) {
 				IJSchService service = Activator.getDefault().getJSch();
 				session = service.createSession(hostName, port, userName);
 				session.setTimeout(0);
 				session.setServerAliveInterval(300000);
 				session.setServerAliveCountMax(6);
 				session.setPassword(password); // Set password directly
 				UserInfo ui = new LocalUserInfo();
 				session.setUserInfo(ui);
 			}
 
 			if (!session.isConnected()) {
 				session.connect(60 * 1000); // Connect with defautl timeout
 			}
 
 			if (channel == null) {
 				channel = (ChannelSftp) session.openChannel("sftp");
 			}
 			if (!channel.isConnected()) {
 				channel.connect();
 			}
 		} catch (JSchException e) {
 			if (e.toString().indexOf("Auth cancel") >= 0 || e.toString().indexOf("Auth fail") >= 0) { //$NON-NLS-1$
 				if (session.isConnected()) {
 					session.disconnect();
 					session = null;
 				}
 			}
 			DLTKCore.error("Failed to create direct connection", e);
 			if (session.isConnected() && !channel.isConnected()) {
 				try {
 					Thread.sleep(50);
 				} catch (InterruptedException e1) {
 					e1.printStackTrace();
 				}
 				// Try to reconnect
 			}
 			if (session != null) {
 				session.disconnect();
 				session = null;
 			}
 		}
 		if (session == null || !session.isConnected() || channel == null
 				|| !channel.isConnected()) {
 			if (trycount > 0) {
 				if (session == null || !session.isConnected()) {
 					session = null;
 				}
 				channel = null;
 				return connect(trycount - 1);
 			} else {
 				// Lets disable connection for a while.
 				setDisabled(1000 * 10); // 10 seconds
 				// seconds
 			}
 			return false;
 		} else {
 			return true;
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.dltk.ssh.core.ISshConnection#disconnect()
 	 */
 	public void disconnect() {
 		if (channel.isConnected()) {
 			channel.disconnect();
 		}
 		if (session.isConnected()) {
 			session.disconnect();
 		}
 	}
 
 	private boolean performStreamOperation = false;
 
 	private synchronized void performOperation(final Operation op, int tryCount) {
 		while (performStreamOperation) {
 			try {
 				this.wait(10);
 			} catch (InterruptedException e) {
 				e.printStackTrace();
 			}
 		}
 		try {
 			connect();
 			op.perform();
 			op.setFinished();
 		} catch (JSchException ex) {
 			Activator.log(ex);
 			// if (!channel.isConnected()) {
 			if (tryCount > 0) {
 				performOperation(op, tryCount - 1);
 			}
 			// }
 		} catch (SftpException e) {
 			if (e.id != ChannelSftp.SSH_FX_NO_SUCH_FILE) {
 				Activator.log(e);
 			}
 		} finally {
 			if (!op.isFinished()) {
 				op.failed();
 			}
 		}
 
 	}
 
 	private ChannelSftp getChannel() {
 		return (ChannelSftp) channel;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.eclipse.dltk.ssh.core.ISshConnection#getHandle(org.eclipse.core.runtime
 	 * .IPath)
 	 */
 	public ISshFileHandle getHandle(IPath path) throws Exception {
 		if (isDisabled()) {
 			return null;
 		}
 		GetStatOperation op = new GetStatOperation(path);
 		performOperation(op, DEFAULT_RETRY_COUNT);
 		if (op.isFinished()) {
 			return new SshFileHandle(this, path, op.getAttrs());
 		}
 		return new SshFileHandle(this, path, null);
 	}
 
 	public boolean isDisabled() {
 		return disabledTime > System.currentTimeMillis();
 	};
 
 	public void setDisabled(int timeout) {
 		disabledTime = System.currentTimeMillis() + timeout;
 	};
 
 	SftpATTRS getAttrs(IPath path) {
 		GetStatOperation op = new GetStatOperation(path);
 		performOperation(op, DEFAULT_RETRY_COUNT);
 		if (op.isFinished()) {
 			return op.getAttrs();
 		}
 		return null;
 	}
 
 	IPath getResolvedPath(IPath path) {
 		ResolveLinkOperation op = new ResolveLinkOperation(path);
 		performOperation(op, DEFAULT_RETRY_COUNT);
 		if (op.isFinished()) {
 			return op.getResolvedPath();
 		}
 		return null;
 	}
 
 	Vector list(IPath path) {
 		ListFolderOperation op = new ListFolderOperation(path);
 		performOperation(op, DEFAULT_RETRY_COUNT);
 		if (op.isFinished()) {
 			return op.getVector();
 		}
 		return null;
 	}
 
 	void setLastModified(final IPath path, final long timestamp) {
 		Operation op = new Operation() {
 			@Override
 			public void perform() throws JSchException, SftpException {
 				getChannel().setMtime(path.toString(),
 						(int) (timestamp / 1000L));
 			}
 		};
 		performOperation(op, DEFAULT_RETRY_COUNT);
 	}
 
 	void delete(final IPath path, final boolean dir) {
 		Operation op = new Operation() {
 			@Override
 			public void perform() throws JSchException, SftpException {
 				if (!dir) {
 					getChannel().rm(path.toString());
 				} else {
 					getChannel().rmdir(path.toString());
 				}
 			}
 		};
 		performOperation(op, DEFAULT_RETRY_COUNT);
 	}
 
 	void mkdir(final IPath path) {
 		Operation op = new Operation() {
 			@Override
 			public void perform() throws JSchException, SftpException {
 				getChannel().mkdir(path.toString());
 			}
 		};
 		performOperation(op, DEFAULT_RETRY_COUNT);
 	}
 
 	InputStream get(IPath path) {
 		GetOperation op = new GetOperation(path);
 		performOperation(op, DEFAULT_RETRY_COUNT);
 		if (op.isFinished()) {
 			return op.getStream();
 		}
 		return null;
 	}
 
 	OutputStream put(IPath path) {
 		PutOperation op = new PutOperation(path);
 		performOperation(op, DEFAULT_RETRY_COUNT);
 		if (op.isFinished()) {
 			return op.getStream();
 		}
 		return null;
 	}
 
 	public boolean isConnected() {
 		if (session != null) {
 			return session.isConnected();
 		}
 		return false;
 	}
 }
