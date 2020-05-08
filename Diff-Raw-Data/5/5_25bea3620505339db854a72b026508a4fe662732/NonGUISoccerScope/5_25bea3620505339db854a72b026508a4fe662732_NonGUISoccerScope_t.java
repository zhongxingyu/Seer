 package soccerscope;
 
 import java.io.ByteArrayInputStream;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.PrintWriter;
 import java.net.DatagramPacket;
 import java.net.DatagramSocket;
 import java.net.InetAddress;
 import java.net.SocketException;
 import java.util.Properties;
 
 import org.w3c.tools.sexpr.Cons;
 import org.w3c.tools.sexpr.SExprStream;
 import org.w3c.tools.sexpr.SimpleSExprStream;
 import org.w3c.tools.sexpr.Symbol;
 
 import soccerscope.file.LogFileReader;
 import soccerscope.model.Player;
 import soccerscope.model.Scene;
 import soccerscope.model.SceneSet;
 import soccerscope.model.SceneSetMaker;
 import soccerscope.model.Team;
 import soccerscope.model.WorldModel;
 import soccerscope.util.GameAnalyzer;
 import soccerscope.util.analyze.SceneAnalyzer;
 import soccerscope.util.analyze.Xmling;
 
 import com.jamesmurty.utils.XMLBuilder;
 
 public class NonGUISoccerScope {
 
 	public static void run(final String filename, final String xmlFilename)
 	throws Exception {
 		// initiate the world model...
 		final WorldModel wm = WorldModel.getInstance();
 		wm.clear(); // don't think this is necessary... but ok...
 		final File file = new File(filename);
 		if (file == null || !file.canRead()) {
 			throw new FileNotFoundException(String.format(
 					"invalid file '%s' or permissions...\n", filename));
 		}
 		// open the log file and analyze it (by doing SceneSet.analyze())
 		NonGUISoccerScope.openAndAnalyzeLogFile(wm.getSceneSet(), file
 				.getPath());
 		System.out.println("final calculations and xml output:");
 		NonGUISoccerScope.printXML(wm.getSceneSet(), xmlFilename);
 		System.out.println("DONE!");
 	}
 
 	private static void openAndAnalyzeLogFile(final SceneSet sceneSet,
 			final String filename) throws IOException, InterruptedException {
 		final LogFileReader lfr = new LogFileReader(filename);
 		final SceneSetMaker ssm = new SceneSetMaker(lfr, sceneSet);
 		ssm.run();
 	}
 
 	private static void printXML(final SceneSet sceneSet,
 			final String xmlFilename) throws Exception {
 		final XMLBuilder builder = XMLBuilder.create("analysis");
 		builder.attr("version", "1.02");
 
 		final Scene lscene = WorldModel.getInstance().getSceneSet().lastScene();
 
 		final XMLBuilder left = builder.elem("leftteam").attr("name",
 				lscene.left.name);
 		int[] plindex = Team.firstAndLastPlayerIndexes(Team.LEFT_SIDE);
 		for (int iter = plindex[0]; iter < plindex[1]; iter++) {
 			final Player p = lscene.player[iter];
 			left.elem("player").attr("unum", String.valueOf(p.unum)).attr(
 					"viewQuality", p.viewStr()).attr("type", p.typeStr());
 		}
 
 		final XMLBuilder right = builder.elem("rightteam").attr("name",
 				lscene.right.name);
 		plindex = Team.firstAndLastPlayerIndexes(Team.RIGHT_SIDE);
 		for (int iter = plindex[0]; iter < plindex[1]; iter++) {
 			final Player p = lscene.player[iter];
 			right.elem("player").attr("unum", String.valueOf(p.unum)).attr(
 					"viewQuality", p.viewStr()).attr("type", p.typeStr());
 		}
 
 		for (final SceneAnalyzer analyzer : GameAnalyzer.analyzerList) {
 			// all the scene analyzers that can output to XML will do it...
 			if (Xmling.class.isInstance(analyzer)) {
 				((Xmling) analyzer).xmlElement(builder);
 			}
 		}
 
 		final Properties outputProperties = new Properties();
 		// Explicitly identify the output as an XML document
 		outputProperties.put(javax.xml.transform.OutputKeys.METHOD, "xml");
 
 		// Pretty-print the XML output (doesn't work in all cases)
 		outputProperties.put(javax.xml.transform.OutputKeys.INDENT, "yes");
 
 		// Get 2-space indenting when using the Apache transformer
 		outputProperties.put("{http://xml.apache.org/xslt}indent-amount", "2");
 
 		// output file...
 		final PrintWriter out = new PrintWriter(new File(xmlFilename));
 
 		builder.toWriter(out, outputProperties);
 		out.flush();
 	}
 
 	public static void run(int port) throws SocketException, IOException {
 		final int BUFFERSIZE = 16 * 2048;
 		final Symbol endSymbol = Symbol.makeSymbol("end", null);
 		final Symbol timeSymbol = Symbol.makeSymbol("time", null);
 
 		DatagramSocket sock = new DatagramSocket();
 
 		NonGUISoccerScope.sendStartPacket(sock, port);
 
 		DatagramPacket pack;
 		String data;
 		while (true) {
 			// receive
 			pack = new DatagramPacket(new byte[BUFFERSIZE], BUFFERSIZE);
 			sock.receive(pack);
 			System.out.println("rcvd: \"" + new String(pack.getData()).trim() +"\"" );
 
 			// check if it is over...
 			data = new String(pack.getData()).trim();
 			try {
 				InputStream datastream = new ByteArrayInputStream(data.getBytes("UTF-8"));
 				SExprStream p = new SimpleSExprStream(datastream);
 				Object e = p.parse();
 				if(e instanceof Cons) {
 					Cons expr = (Cons) e;
 					if( expr.left().equals(endSymbol) ) {
 						sock.close();
 						System.out.println("connection closed");
 						break;
 					} else if(expr.left().equals(timeSymbol)) {
 						assert expr.right() instanceof Cons;
 						Cons expr_right = (Cons) expr.right();
 						assert expr_right.left() instanceof Integer;
 						int time = ((Integer) expr_right.left()).intValue();
 						if((time % 10) == 0) { // every 10th message
 							byte[] message = ("(recvd "+ time +")").getBytes();
 							pack.setData(message);
 							sock.send(pack);
 							System.out.println("sent: \"" + new String(pack.getData()).trim() +"\"" );
 						}
 
					} else {
						System.err.println("unkown message \""+ data +"\"");
 					}
				} else {
					System.err.println("unkown message \""+ data +"\"");
 				}
 			} catch(Exception e) {
 				System.err.println("could not parse \""+ data +"\"");
 			}
 		}
 	}
 
 	private static void sendStartPacket(DatagramSocket sock, int port) throws IOException {
 		String host = "localhost";
 		byte[] message = "(start)".getBytes();
 
 		// send "(start)"
 		InetAddress address = InetAddress.getByName(host);
 		// Initialize a datagram packet with data and address
 		DatagramPacket packet = new DatagramPacket(message, message.length,
 				address, port);
 		sock.send(packet);
 
 	}
 
 }
