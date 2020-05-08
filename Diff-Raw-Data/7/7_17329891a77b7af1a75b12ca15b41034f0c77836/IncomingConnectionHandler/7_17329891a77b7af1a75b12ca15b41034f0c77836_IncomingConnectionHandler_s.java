 package nl.groep5.xchange.externalInput;
 
 import java.io.BufferedOutputStream;
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileFilter;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.PrintWriter;
 import java.io.RandomAccessFile;
 import java.net.Socket;
 import java.util.Arrays;
 
 import nl.groep5.xchange.Settings;
 
 public class IncomingConnectionHandler extends Thread {
 
 	private static final String SEARCH_COMMAND = "SEARCH ";
 	private static final String GET_COMMAND = "GET ";
 	private Socket client;
 	private BufferedReader bufferedReader;
 	private PrintWriter printWriter;
 	private BufferedOutputStream bufferedOutputStream;
 
 	public IncomingConnectionHandler(Socket client) {
 		this.client = client;
 	}
 
 	@Override
 	public void run() {
 		try {
 			handle(client);
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 
 	/**
 	 * handle incoming messages all messages are strings ending on "\n"
 	 */
 	private void handle(Socket socket) throws Exception {
 		if (Settings.DEBUG) {
 			System.out.println("New incoming connection from "
 					+ socket.getInetAddress().getHostAddress());
 		}
 
 		bufferedReader = new BufferedReader(new InputStreamReader(
 				socket.getInputStream()));
 
 		bufferedOutputStream = new BufferedOutputStream(
 				socket.getOutputStream(), Settings.getBlockSize());
 
 		printWriter = new PrintWriter(socket.getOutputStream(), true);
 
 		String line = null;
 		do {
 			line = bufferedReader.readLine();
 		} while (line == null);
 
 		if (Settings.DEBUG) {
 			System.out.println("command from client " + line);
 		}
 
 		if (line.startsWith(SEARCH_COMMAND)) {
 			System.out.println("Search command");
 			handleSearch(line.substring(SEARCH_COMMAND.length()));
 		} else if (line.startsWith(GET_COMMAND)) {
 			System.out.println("Get command");
 			handleGet(line.substring(GET_COMMAND.length()));
 		} else {
 			handleError();
 		}
 
 		socket.close();
 	}
 
 	private void handleGet(String substring) {
 		String[] command = substring.split(Settings.getSplitCharRegEx());
 		String fileName = command[0];
 		int blockNr = Integer.parseInt(command[command.length - 1]);
 
 		File file = new File(Settings.getSharedFolder() + fileName);
 		if (!file.exists()) {
 
 			file = new File(Settings.getSharedFolder() + fileName
 					+ Settings.getTmpExtension());
 			if (!file.exists()) {
 				handleError();
 				return;
 			}
 			try {
 				RandomAccessFile randomAccessFileStatus = new RandomAccessFile(
 						Settings.getInfoFolder() + fileName
 								+ Settings.getInfoExtension(), "r");
 				randomAccessFileStatus.seek(blockNr);
 				if (randomAccessFileStatus.readByte() == '0') {
 					handleError();
 					return;
 				}
 			} catch (IOException e) {
 				handleError();
 				return;
 			}
 		}
 
 		try {
 			RandomAccessFile randomAccessFile = new RandomAccessFile(file, "r");
 			randomAccessFile.seek(Settings.getBlockSize() * blockNr);
 			byte[] content = new byte[Settings.getBlockSize()];
 			randomAccessFile.read(content, 0, Settings.getBlockSize());
 			randomAccessFile.close();
 			System.out.println(Arrays.toString(content));
 			bufferedOutputStream.write(content);
 			bufferedOutputStream.flush();
 			return;
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 
 	private void handleSearch(final String pattern) {
 		File[] foundFiles = new File(Settings.getSharedFolder())
 				.listFiles(new FileFilter() {
 
 					@Override
 					public boolean accept(File file) {
						if (pattern.equals("*")
								|| (file.getName().toLowerCase()
										.contains(pattern.toLowerCase()))
								|| file.getName().endsWith(
 										Settings.getTmpExtension())) {
 							return true;
 						}
 						return false;
 					}
 				});
 
 		for (File file : foundFiles) {
 			printWriter.print(file.getName() + Settings.getSplitChar()
 					+ file.length() + Settings.getSplitChar());
 		}
 
 		printWriter.println();
 	}
 
 	private void handleError() {
 		printWriter.println("FAIL");
 	}
 }
