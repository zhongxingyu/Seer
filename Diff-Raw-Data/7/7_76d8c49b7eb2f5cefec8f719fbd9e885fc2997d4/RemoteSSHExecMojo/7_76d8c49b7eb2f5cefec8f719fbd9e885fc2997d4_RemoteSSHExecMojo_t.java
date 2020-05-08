 package pl.radical.maven;
 
 import java.io.IOException;
 import java.io.PipedInputStream;
 import java.io.PipedOutputStream;
 import java.util.List;
 
 import org.apache.maven.plugin.AbstractMojo;
 import org.apache.maven.plugin.MojoExecutionException;
 import org.apache.maven.project.MavenProject;
 import org.apache.maven.settings.Server;
 import org.apache.maven.settings.Settings;
 
 import pl.radical.maven.data.SSHUserInfo;
 
 import com.jcraft.jsch.ChannelExec;
 import com.jcraft.jsch.JSch;
 import com.jcraft.jsch.JSchException;
 import com.jcraft.jsch.Session;
 
 /**
  * A plugin to execute a single command or sequence of commands via SSH.
  * 
  * @goal ssh-remote-exec
  * @requiresProject true
  * @author <a href="mailto:lukasz.rzanek@radical.com.pl">Łukasz Rżanek</a>
  */
 public class RemoteSSHExecMojo extends AbstractMojo {
 
 	/**
 	 * The current user system settings for use in Maven.
 	 * 
 	 * @parameter expression="${settings}"
 	 * @readonly
 	 */
 	protected Settings settings;
 
 	/**
 	 * Internal Maven's project
 	 * 
 	 * @parameter expression="${project}"
 	 * @readonly
 	 */
 	protected MavenProject project;
 
 	/**
 	 * A server ID from settings.xml's to be used. This is needed to provide a
 	 * authentication information.
 	 * 
 	 * @parameter
 	 * @required
 	 */
 	private String serverId;
 
 	/**
 	 * Changes the port used to connect to the remote host.
 	 * 
 	 * @parameter default-value=22;
 	 */
 	private int port;
 
 	/**
 	 * Remote host, either DNS name or IP address to connect to.
 	 * 
 	 * @parameter
 	 * @required
 	 */
 	private String host;
 
 	/**
 	 * Max wait for command to execute in seconds
 	 * 
 	 * @parameter default-value=30;
 	 */
 	private int maxWait;
 
 	/**
 	 * A command list to be executed, one by one
 	 * 
 	 * @parameter
 	 */
 	private List<String> commands;
 
 	/**
 	 * Command to be executed on the remote server
 	 * 
 	 * @parameter
 	 */
 	private String command;
 
 	private Session session;
 
 	private Thread thread;
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.apache.maven.plugin.Mojo#execute()
 	 */
 	public void execute() throws MojoExecutionException {
 		getLog().debug("Using information from settings.xml about the server: " + serverId);
 
 		final Server server = settings.getServer(serverId);
 		final SSHUserInfo userInfo = new SSHUserInfo();
 		userInfo.setTrust(true);
 
 		try {
 			getLog().debug("Setting up the username to be used in connection: " + server.getUsername());
 			userInfo.setName(server.getUsername());
 			if (server.getPrivateKey() != null) {
 				getLog().debug("Setting up private key settings.xml file");
 				userInfo.setKeyfile(server.getPrivateKey());
 				if (server.getPassphrase() != null) {
 					userInfo.setPassphrase(server.getPassphrase());
 				}
 			} else {
 				getLog().debug("Setting up password from settings.xml file");
 				if (server.getPassword() != null) {
 					userInfo.setPassword(server.getPassword());
 				}
 			}
 
 			final JSch jsch = new JSch();
 			session = jsch.getSession(settings.getServer(serverId).getUsername(), host, port);
 			session.setUserInfo(userInfo);
 			session.setTimeout(maxWait * 1000);
 
 			getLog().info("Connecting to " + host + ":" + port);
 			session.connect();
 			if (session.isConnected()) {
 				getLog().info("-- connection to SSH server established");
 			}
 
 			if (command != null && !"".equals(command)) {
 				getLog().debug("Executing single command: " + command);
 				executeCommand(command);
 			} else if (commands != null && !commands.isEmpty()) {
 				getLog().debug("Using a list of commands to execute. Number of commands: " + commands.size());
 				for (final String command : commands) {
 					executeCommand(command);
 				}
 			} else {
 				throw new MojoExecutionException("At least one command or commandSequence must be specified");
 			}
 
 			getLog().info("Command(s) executed succesfully!");
 		} catch (final JSchException e) {
 			throw new MojoExecutionException("Unable to connec to SSH server", e);
 		} finally {
 			if (session != null && session.isConnected()) {
 				session.disconnect();
 			}
 		}
 	}
 
 	private void executeCommand(String cmd) throws MojoExecutionException {
		String actualCommand = cmd.replaceAll("\\&amp\\;", "&");

		getLog().info("Executing command: " + actualCommand);
 
 		try {
 			final PipedOutputStream out = new PipedOutputStream();
 			final PipedInputStream readEndOfPipe = new PipedInputStream(out);
 
 			final ChannelExec channel;
 			session.setTimeout(maxWait * 1000);
 			/* execute the command */
 			channel = (ChannelExec) session.openChannel("exec");
			channel.setCommand(actualCommand);
 			channel.setOutputStream(out);
 			channel.setExtOutputStream(out);
 			channel.connect();
 
 			// wait for it to finish
 			thread = new Thread() {
 				@Override
 				public void run() {
 					while (!channel.isClosed()) {
 						if (thread == null) {
 							return;
 						}
 						try {
 							byte[] tmp = new byte[1024];
 							while (readEndOfPipe.available() > 0) {
 								int in = readEndOfPipe.read(tmp, 0, 1024);
 								if (in < 0) {
 									break;
 								}
 
 								System.out.print(new String(tmp, 0, in));
 							}
 						} catch (Exception e) {
 							// ignored
 						}
 					}
 				}
 			};
 
 			thread.start();
 			thread.join(maxWait * 1000);
 
 			if (thread.isAlive()) {
 				// ran out of time
 				thread = null;
 				throw new MojoExecutionException("Timeout period exceeded, connection dropped.");
 			} else {
 				// this is the wrong test if the remote OS is OpenVMS,
 				// but there doesn't seem to be a way to detect it.
 				int ec = channel.getExitStatus();
 				if (ec != 0) {
 					String msg = "Remote command failed with exit status " + ec;
 					throw new MojoExecutionException(msg);
 				}
 				channel.disconnect();
 			}
 		} catch (JSchException e) {
 			throw new MojoExecutionException("Error detected while executing the command.", e);
 		} catch (InterruptedException e) {
 			throw new MojoExecutionException("The execution was interrupted.", e);
 		} catch (IOException e) {
 			throw new MojoExecutionException("Unable to create output writer", e);
 		}
 	}
 }
