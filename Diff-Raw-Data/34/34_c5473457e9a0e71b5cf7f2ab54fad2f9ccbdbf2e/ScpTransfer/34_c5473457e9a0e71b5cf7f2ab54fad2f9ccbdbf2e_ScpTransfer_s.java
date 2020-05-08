 package xtremweb.serv.dt.scp;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 
 import com.jcraft.jsch.Channel;
 import com.jcraft.jsch.ChannelExec;
 import com.jcraft.jsch.JSch;
 import com.jcraft.jsch.JSchException;
 import com.jcraft.jsch.Session;
 
 import xtremweb.core.log.Logger;
 import xtremweb.core.log.LoggerFactory;
 import xtremweb.core.obj.dc.Data;
 import xtremweb.core.obj.dc.Locator;
 import xtremweb.core.obj.dr.Protocol;
 import xtremweb.core.obj.dt.Transfer;
 import xtremweb.serv.dt.BlockingOOBTransferImpl;
 import xtremweb.serv.dt.OOBException;
 
 /**
  * Scp transfer implementation
  * 
  * @author jsaray
  * 
  */
 public class ScpTransfer extends BlockingOOBTransferImpl {
 
 	/**
 	 * Log
 	 */
 	protected static Logger log = LoggerFactory.getLogger(ScpTransfer.class);
 
 	/**
 	 * User attempting to stablish a scp connection
 	 */
 	private String user;
 
 	/**
 	 * Host the user wants to connect to
 	 */
 	private String host;
 
 	/**
 	 * Name of Local file
 	 */
 	private String lfile;
 
 	/**
 	 * Name of remote file
 	 */
 	private String rfile;
 
 	/**
 	 * Session between user and host
 	 */
 	private Session session;
 
 	/**
 	 * Path to private key
 	 */
 	private String PRIVATE_KEY;
 
 	/**
 	 * Private key passphrase
 	 */
 	private String PASSPHRASE;
 
 	/**
 	 * Known Hosts path
 	 */
 	private String KNOWN_HOSTS;
 
 	/**
 	 * Scp transfer contructor
 	 * 
 	 * @param d
 	 *            data, the data to transfer
 	 * @param t
 	 *            transfer, the transfer
 	 * @param l1
 	 *            Local locator
 	 * @param l2
 	 *            Remote locator
 	 * @param p1
 	 *            Local protocol
 	 * @param p2
 	 *            Remote protocol
 	 */
 	public ScpTransfer(Data d,Transfer t,Locator l1, Locator l2, Protocol p1, Protocol p2)
     {	super(d,t,l1,l2,p1,p2);
     	this.user = remote_protocol.getlogin();
     	this.host = remote_protocol.getserver();
     	this.lfile = local_locator.getref();
     	System.out.println("lfile is " + lfile);
     	this.rfile = remote_locator.getref();
     	System.out.println("rfile is " + rfile);
     	PRIVATE_KEY = remote_protocol.getprivatekeypath();
     	KNOWN_HOSTS = remote_protocol.getknownhosts();
     	PASSPHRASE = remote_protocol.getpassphrase();
 	/*scpm = new ScpManager(remote_protocol.getlogin(),remote_protocol.getserver(),local_locator.getref(),remote_locator.getref(),
 		remote_protocol.getprivatekeypath(),remote_protocol.getknownhosts(),remote_protocol.getpassphrase());*/
     }
 
 	/**
 	 * String representation
 	 */
 	public String toString() {
 		return "Send : " + remote_protocol.getserver() + " user : "
 				+ remote_protocol.getlogin() + " key: "
 				+ remote_protocol.getpassword();
 	}
 
 	/**
 	 * Securely send a file
 	 */
 	public void blockingSendSenderSide() throws OOBException {
 		FileInputStream fis = null;
 		try {
 			log.debug("enter send scpmanager session is " + session);
 			// exec 'scp -t rfile' remotely
 			String command = "scp -p -t " + rfile;
 			Channel channel = session.openChannel("exec");
 			((ChannelExec) channel).setCommand(command);
 			// get I/O streams for remote scp
 			OutputStream out = channel.getOutputStream();
 			InputStream in = channel.getInputStream();
 			channel.connect();
 			if (checkAck(in) != 0) {
 				System.exit(0);
 			}
 			// send "C0644 filesize filename", where filename should not include
 			// '/'
 			log.debug("lfile value " + lfile);
 			long filesize = (new File(lfile)).length();
 			command = "C0644 " + filesize + " ";
 			if (lfile.lastIndexOf('/') > 0) {
 				command += lfile.substring(lfile.lastIndexOf('/') + 1);
 			} else {
 				command += lfile;
 			}
 			command += "\n";
 			out.write(command.getBytes());
 			out.flush();
 			if (checkAck(in) != 0) {
 				System.exit(0);
 			}
 
 			// send a content of lfile
 			fis = new FileInputStream(lfile);
 			byte[] buf = new byte[1024];
 			while (true) {
 				int len = fis.read(buf, 0, buf.length);
 				if (len <= 0)
 					break;
 				out.write(buf, 0, len); // out.flush();
 			}
 			fis.close();
 			fis = null;
 			// send '\0'
 			buf[0] = 0;
 			out.write(buf, 0, 1);
 			out.flush();
 			if (checkAck(in) != 0) {
 				System.exit(0);
 			}
 			out.close();
 			channel.disconnect();
 		} catch (Exception e) {
 			e.printStackTrace();
 			try {
 				if (fis != null)
 					fis.close();
 			} catch (Exception ee) {
 			}
 		}
 		log.debug("out send scpmanager");
 	}
 
 	/**
 	 * This method is unimplemented due to the scp transfer's nature
 	 */
 	public void blockingReceiveSenderSide() throws OOBException {
 	}
 
 	/**
 	 * Securely connect
 	 */
 	public void blockingReceiveReceiverSide() throws OOBException {
 		File f = null;
 		FileOutputStream fos = null;
 		try {
 
 			String prefix = null;
 			if (new File(lfile).isDirectory()) {
 				prefix = lfile + File.separator;
 			}
 			// exec 'scp -f rfile' remotely
 			String command = "scp -f " + rfile;
 			Channel channel = session.openChannel("exec");
 			((ChannelExec) channel).setCommand(command);
 			// get I/O streams for remote scp
 			OutputStream out = channel.getOutputStream();
 			InputStream in = channel.getInputStream();
 			channel.connect();
 			byte[] buf = new byte[1024];
 			// send '\0'
 			buf[0] = 0;
 			out.write(buf, 0, 1);
 			out.flush();
 			while (true) {
 				int c = checkAck(in);
 				if (c != 'C') {
 					break;
 				}
 
 				// read '0644 '
 				in.read(buf, 0, 5);
 
 				long filesize = 0L;
 				while (true) {
 					if (in.read(buf, 0, 1) < 0) {
 						// error
 						break;
 					}
 					if (buf[0] == ' ')
 						break;
 					filesize = filesize * 10L + (long) (buf[0] - '0');
 				}
 
 				String file = null;
 				for (int i = 0;; i++) {
 					in.read(buf, i, 1);
 					if (buf[i] == (byte) 0x0a) {
 						file = new String(buf, 0, i);
 						break;
 					}
 				}
 
 				// System.out.println("filesize="+filesize+", file="+file);
 
 				// send '\0'
 				buf[0] = 0;
 				out.write(buf, 0, 1);
 				out.flush();
 
 				// read a content of lfile
 				f = new File(prefix == null ? lfile : prefix + file);
 				fos = new FileOutputStream(f);
 				int foo;
 				while (true) {
 					if (buf.length < filesize)
 						foo = buf.length;
 					else
 						foo = (int) filesize;
 					foo = in.read(buf, 0, foo);
 					if (foo < 0) {
 						// error
 						break;
 					}
 					fos.write(buf, 0, foo);
 					filesize -= foo;
 					if (filesize == 0L)
 						break;
 				}
 				fos.close();
 				fos = null;
 
 				if (checkAck(in) != 0) {
 					System.exit(0);
 				}
 
 				// send '\0'
 				buf[0] = 0;
 				out.write(buf, 0, 1);
 				out.flush();
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 			try {
 				if (fos != null)
 					fos.close();
 			} catch (Exception ee) {
 			}
 		}
 
 	}
 
 	/**
 	 * Securely connect to the host
 	 */
 	public void connect() throws OOBException {
 		log.debug("enter to connect ");
 		JSch jsch = new JSch();
 		try {
			jsch.addIdentity(PRIVATE_KEY, PASSPHRASE);
 			jsch.setKnownHosts(KNOWN_HOSTS);
 			log.debug("user " + user + "host : " + host);
 			session = jsch.getSession(user, host, 22);
 			session.connect();
 		} catch (JSchException e) {
 			e.printStackTrace();
 		}
 	}
 
 	/**
 	 * Securely disconnect from the host
 	 */
 	public void disconnect() throws OOBException {
 		session.disconnect();
 	}
 
 	/**
 	 * Pool the transfer (blocking transfer, so it returns inmediatly
 	 */
 	public boolean poolTransfer() {
 		return !isTransfering();
 	}
 
 	public void blockingSendReceiverSide() throws OOBException {
 	}
 
 	private int checkAck(InputStream in) throws IOException {
 		int b = in.read();
 		// b may be 0 for success,
 		// 1 for error,
 		// 2 for fatal error,
 		// -1
 		if (b == 0)
 			return b;
 		if (b == -1)
 			return b;
 
 		if (b == 1 || b == 2) {
 			StringBuffer sb = new StringBuffer();
 			int c;
 			do {
 				c = in.read();
 				sb.append((char) c);
 			} while (c != '\n');
 			if (b == 1) { // error
 				System.out.print(sb.toString());
 			}
 			if (b == 2) { // fatal error
 				System.out.print(sb.toString());
 			}
 		}
 		return b;
 	}
 }
