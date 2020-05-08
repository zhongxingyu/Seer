 package de.schadix.biertoto.dom;
 
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.FileOutputStream;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Properties;
 
 import javax.xml.namespace.QName;
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.xpath.XPath;
 import javax.xml.xpath.XPathConstants;
 import javax.xml.xpath.XPathExpression;
 import javax.xml.xpath.XPathExpressionException;
 import javax.xml.xpath.XPathFactory;
 
 import org.apache.commons.cli.CommandLine;
 import org.apache.commons.cli.CommandLineParser;
 import org.apache.commons.cli.HelpFormatter;
 import org.apache.commons.cli.OptionBuilder;
 import org.apache.commons.cli.Options;
 import org.apache.commons.cli.PosixParser;
 import org.w3c.dom.Document;
 import org.w3c.dom.NodeList;
 import org.w3c.tidy.Tidy;
 
 import de.schadix.biertoto.model.Game;
 import de.schadix.biertoto.model.GameTipp;
 import de.schadix.biertoto.model.Result;
 import de.schadix.biertoto.model.Spieltag;
 import de.schadix.biertoto.model.Tipp;
 import de.schadix.general.CustomEntityResolver;
 
 public class Test {
 
 	private static String TIPPERNAME = "tipperName";
 	private static String TIPPERTIPP = "tipperTipp";
 	private static String GAMEHOME = "game1Hometeam";
 	private static String GAMEGUEST = "game1Guestteam";
 	private static String GAMERESULT = "game1Result";
 	
 	private static String GAMEHOMERESULT = "game1HomeResult";
 	private static String GAMEGUESTRESULT = "game1GuestResult";
 	
 	private static String TIPPS = "tipps";
 
 	static XPathFactory xPathFactory = XPathFactory.newInstance();
 	static XPath xpath = xPathFactory.newXPath();
 	static String year = "2010";
 	static URL url = null;
 	static ArrayList<String> tipperOrder = new ArrayList<String>();
 	static boolean debug = false;
 
 	public Test() {
 	}
 
 	@SuppressWarnings("static-access")
 	private static void makeOptions(String args[]) throws Exception {
 		Options options = new Options();
 		options.addOption(OptionBuilder.withArgName("tipperOrder").withLongOpt(
 				"tipperOrder").hasArgs().withValueSeparator(',')
 				.withDescription("comma seperated list of tippernames").create(
 						'o'));
 
 		options.addOption("h", "help", false, "print this message");
 		options.addOption("q", "version", false, "print version information");
 		options.addOption("y", "year", true, "year to parse");
 		options.addOption("u", "url", true, "url to access");
 		options.addOption("d", "debug", false, "debug output");
 
 		CommandLineParser parser = new PosixParser();
 		CommandLine cmd = parser.parse(options, args);
 		if (cmd.hasOption('o')) {
 			String values[] = cmd.getOptionValues('o');
 			for (String tippername : values) {
 				tipperOrder.add(tippername);
 			}
 		} else {
 			System.out.println("specify Tippnames like-o Spaceman,Schadix");
 			System.exit(1);
 		}
 		if (cmd.hasOption("y")) {
 			year = cmd.getOptionValue('y');
 		}
 		if (cmd.hasOption("d")) {
 			debug = true;
 		}
 		if (cmd.hasOption("q"))
 			System.out.println("version 20090725");
 		if (cmd.hasOption("h")) {
 			HelpFormatter formatter = new HelpFormatter();
 			formatter.printHelp("ant", options);
 		}
 		if (cmd.hasOption("u")) {
 			Test.url = new URL(cmd.getOptionValue('u'));
 		} else {
 			System.out.println("please specify -u URL to access, like: ");
 			System.exit(1);
 		}
 
 	}
 
 	public static void main(String args[]) {
 		try {
 			makeOptions(args);
 			Properties props = new Properties();
 			if (year.equals("2008")) {
 				props.load(Test.class
 						.getResourceAsStream("/kicktipp_xpath2008.properties"));
 			} else if (year.equals("2009")) {
 				props.load(Test.class
 						.getResourceAsStream("/kicktipp_xpath2009.properties"));
			} else {
 				props.load(Test.class
						.getResourceAsStream("/kicktipp_xpath2010_2.properties"));
 			}
 			Tidy xmlDoc = new Tidy();
 			xmlDoc.setXmlOut(true);
 
 			DocumentBuilderFactory factory = DocumentBuilderFactory
 					.newInstance();
 			DocumentBuilder docBuilder = factory.newDocumentBuilder();
 			docBuilder.setEntityResolver(new CustomEntityResolver());
 			Spieltag spieltag = new Spieltag();
 			spieltag.setPrintOrder(tipperOrder);
 
 			ByteArrayOutputStream outStream = new ByteArrayOutputStream();
 			xmlDoc.parseDOM(Test.url.openStream(), outStream);
 			if (debug) {
 				FileOutputStream fileOutputStream = new FileOutputStream(
 						"test.xml");
 				outStream.writeTo(fileOutputStream);
 				outStream.flush();
 			}
 
 			Document doc3 = docBuilder.parse(new ByteArrayInputStream(outStream
 					.toByteArray()));
 
 			NodeList nl = ((NodeList) findXPath(doc3, props.getProperty(TIPPS),
 					XPathConstants.NODESET));
 
 			int nrOfTippsInt = nl.getLength();
 			int nrOfGamesInt = 9;
 			for (int i = 1; i <= nrOfGamesInt; i++) {
 				Game game = new Game(
 						getXpathValue(props, GAMEHOME, doc3, i),
 						getXpathValue(props, GAMEGUEST, doc3, i),
 						year.equals("2010") ? (new Result((getXpathValue(props, GAMEHOMERESULT, doc3, i)) 
 								   + ":" + (getXpathValue(props, GAMEGUESTRESULT, doc3, i))))
 								   	: new Result(getXpathValue(props, GAMERESULT, doc3, i)));
 				List<Tipp> tippList = new ArrayList<Tipp>();
 
 				for (int i1 = 1; i1 <= nrOfTippsInt; i1++) {
 					String tipperName = (String) findXPath(doc3, replaceTipper(
 							i1, props.getProperty(TIPPERNAME)),
 							XPathConstants.STRING);
 
 					String tippString = (String) findXPath(doc3, replaceVar(i,
 							replaceTipper(i1, props.getProperty(TIPPERTIPP))),
 							XPathConstants.STRING);
 
 					Tipp tipp = new Tipp(tipperName, new Result(
 							tippString != null ? tippString : ""));
 					tippList.add(tipp);
 				}
 
 				GameTipp gameTipp = new GameTipp(game, tippList);
 				spieltag.addGame(gameTipp);
 			}
 			System.out.println(spieltag);
 
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 
 	private static String getXpathValue(Properties props, String property, Document doc3, int i) {
 		return (String) findXPath(doc3, replaceVar(i, props
 				.getProperty(property)), XPathConstants.STRING);
 	}
 
 	private static Object findXPath(Object doc, String xpath,
 			QName xpathConstant) {
 
 		XPathExpression expr;
 		Object returnValue = null;
 		try {
 			expr = Test.xpath.compile(xpath);
 			returnValue = expr.evaluate(doc, xpathConstant);
 		} catch (XPathExpressionException e) {
 			e.printStackTrace();
 		}
 		return returnValue;
 	}
 
 	private static String replaceVar(int i, String tippername2) {
 		return tippername2.replaceAll("\\{var\\}", (new Integer(i)).toString());
 	}
 
 	private static String replaceTipper(int i, String tippername2) {
 		return tippername2.replaceAll("\\{tipper\\}", (new Integer(i))
 				.toString());
 	}
 
 }
