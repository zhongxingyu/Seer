 package parte.jphpd;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 /**
  * A Java interface to the PHP execution daemon.
  * 
  * @author nelson17
  */
 public class Daemon {
 
 	/**
 	 * Indicates success.
 	 */
 	private static final int STATUS_SUCCESS = 0;
 
 	/**
 	 * Indicates that the daemon is not ready for a command.
 	 */
 	private static final int STATUS_NOT_READY = 1;
 
 	/**
 	 * The value of the last variable obtained with the {@link #var(String)}
 	 * method.
 	 */
 	private String varValue = null;
 
 	/**
	 * The format string containing the command to execute to control the
	 * daemon.
 	 */
	private static final String command = "@PHPD_SCRIPT@ %s %s";
 
 	/**
 	 * The instance of Daemon.
 	 */
 	private static Daemon instance = null;
 	
 	/**
 	 * Gets an instance of Daemon.
 	 *
 	 * @return The instance.
 	 */
 	public static Daemon getInstance() {
 		if (Daemon.instance == null) {
 			Daemon.instance = new Daemon();
 		}
 		return Daemon.instance;
 	}
 
 	/**
 	 * Creates a new Daemon instance. Package-private to enforce singleton
 	 * usage.
 	 */
 	Daemon() {}
 
 	/**
 	 * Starts the PHP executor daemon.
 	 *
 	 * @return Whether the daemon was successfully started.
 	 * @throws DaemonUpException If the daemon is already running.
 	 */
 	public boolean start() {
 		int status = Daemon.exec("start");
 		if (status == Daemon.STATUS_NOT_READY) {
 			throw new DaemonUpException();
 		}
 		return (status == Daemon.STATUS_SUCCESS);
 	}
 
 	/**
 	 * Stops the PHP executor daemon. If it does not respond to the signal
 	 * to stop in an appropriate amount of time, false is returned.
 	 *
 	 * @return Whether the daemon was successfully stopped.
 	 * @throws DaemonDownException If the daemon is not running.
 	 */
 	public boolean stop() {
 		int status = Daemon.exec("stop");
 		if (status == Daemon.STATUS_NOT_READY) {
 			throw new DaemonDownException();
 		}
 		return (status == Daemon.STATUS_SUCCESS);
 	}
 
 	/**
 	 * Inputs code to the PHP executor daemon.
 	 *
 	 * @param code The code to execute. If it is PHP code, it must start
 	 * with a PHP open tag.
 	 * @return Whether the code was successfully executed. Will be false if
 	 * the code contains syntax errors.
 	 * @throws DaemonDownException If the daemon is not running.
 	 */
 	public boolean input(String code) {
 		int status = Daemon.exec("input", code);
 		if (status == Daemon.STATUS_NOT_READY) {
 			throw new DaemonDownException();
 		}
 		return (status == Daemon.STATUS_SUCCESS);
 	}
 
 	/**
 	 * Executes a PHP file with the PHP executor daemon.
 	 *
 	 * @param filename The file to execute.
 	 * @return Whether the file was successfully executed.
 	 * @throws DaemonDownException If the daemon is not running.
 	 */
 	public boolean read(String filename) {
 		int status = Daemon.exec("read", filename);
 		if (status == Daemon.STATUS_NOT_READY) {
 			throw new DaemonDownException();
 		}
 		return (status == Daemon.STATUS_SUCCESS);
 	}
 
 	/**
 	 * Gets the value of a variable.
 	 *
 	 * @param name The name of the variable to get the value of.
 	 * @return Whether the value was successfully obtained. Will be false if
 	 * the requested variable does not exist.
 	 * @throws DaemonDownException If the daemon is not running.
 	 */
 	public boolean var(String name) {
 		StringBuffer val = new StringBuffer();
 		int status = Daemon.exec("var", name, val);
 		if (status == Daemon.STATUS_NOT_READY) {
 			throw new DaemonDownException();
 		}
 		if (status == Daemon.STATUS_SUCCESS) {
 			varValue = val.toString();
 			return true;
 		} else {
 			varValue = null;
 			return false;
 		}
 	}
 
 	/**
 	 * Parses a string.
 	 *
 	 * @param string The string to parse.
 	 * @return Whether the string was successfully parsed.
 	 * @throws DaemonDownException If the daemon is not running.
 	 */
 	public boolean parse(String string) {
 		StringBuffer val = new StringBuffer();
 		int status = Daemon.exec("parse", string, val);
 		if (status == Daemon.STATUS_NOT_READY) {
 			throw new DaemonDownException();
 		}
 		if (status == Daemon.STATUS_SUCCESS) {
 			varValue = val.toString();
 			return true;
 		} else {
 			varValue = null;
 			return false;
 		}
 	}
 
 	/**
 	 * Gets the value of the last response.
 	 *
 	 * @return The value.
 	 */
 	public String getResponseValue() {
 		return varValue;
 	}
 
 	/**
 	 * Executes a command with the daemon that has no argument. Parses the
 	 * arguments with the command. The result is executed by calling
 	 * {@code Runtime.getRuntime().exec(parsedCommand)}. The result of the
 	 * process is returned.
 	 *
 	 * @param subcmd The subcommand to execute.
 	 * @return The status of the command.
 	 * @throws DaemonExecutionException If the command could not be
 	 * executed.
 	 */
 	private static int exec(String subcmd) {
		return Daemon.exec(subcmd, "");
 	}
 
 	/**
 	 * Executes a command with the daemon. Parses the arguments with the
 	 * command and its argument. The result is executed by calling
 	 * {@code Runtime.getRuntime().exec(parsedCommand)}. The result of the
 	 * process is returned.
 	 *
 	 * @param subcmd The subcommand to execute.
 	 * @param arg The argument to the subcommand.
 	 * @return The status of the command.
 	 * @throws DaemonExecutionException If the command could not be
 	 * executed.
 	 */
 	private static int exec(String subcmd, String arg) {
 		return Daemon.exec(subcmd, arg, null);
 	}
 
 	/**
 	 * Executes a command with the daemon. Parses the arguments with the
 	 * command and its argument. The result is executed by calling
 	 * {@code Runtime.getRuntime().exec(parsedCommand)}. The result of the
 	 * process is returned.
 	 *
 	 * @param subcmd The subcommand to execute.
 	 * @param arg The argument to the subcommand.
 	 * @param buf A StringBuffer to put the output in.
 	 * @return The status of the command.
 	 * @throws DaemonExecutionException If the command could not be
 	 * executed.
 	 */
 	private static int exec(String subcmd, String arg, StringBuffer buf) {
		String command = String.format(Daemon.command, subcmd, arg);
 		Runtime r = Runtime.getRuntime();
 		Process p = null;
 		try {
 			p = r.exec(command);
 			p.waitFor();
 			if (buf != null) {
 				InputStreamReader is = null;
 				is = new InputStreamReader(p.getInputStream());
 				int ch;
				while ((ch = is.read()) != -1) {
 					buf.append((char) ch);
 				}
 			}
 		} catch (IOException e) {
 			throw new DaemonExecutionException(e);
 		} catch (InterruptedException e) {
 			throw new DaemonExecutionException(e);
 		} catch (NullPointerException e) {
 			throw new DaemonExecutionException(e);
 		}
 		return p.exitValue();
 	}
 	
 
 }
 
