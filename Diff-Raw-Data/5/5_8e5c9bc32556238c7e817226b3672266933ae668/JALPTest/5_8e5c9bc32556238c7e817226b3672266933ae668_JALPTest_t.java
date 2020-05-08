 /**
  * JALPTest is a util file to test JALoP.
  * <p>
  * Source code in 3rd-party is licensed and owned by their respective
  * copyright holders.
  * <p>
  * All other source code is copyright Tresys Technology and licensed as below.
  * <p>
  * Copyright (c) 2012 Tresys Technology LLC, Columbia, Maryland, USA
  * <p>
  * This software was developed by Tresys Technology LLC
  * with U.S. Government sponsorship.
  * <p>
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * <p>
  *    http://www.apache.org/licenses/LICENSE-2.0
  * <p>
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 import java.io.BufferedReader;
 import java.io.DataInputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileReader;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.security.KeyFactory;
 import java.security.PrivateKey;
 import java.security.PublicKey;
 import java.security.cert.CertificateFactory;
 import java.security.cert.X509Certificate;
 import java.security.spec.KeySpec;
 import java.security.spec.PKCS8EncodedKeySpec;
 import java.security.spec.X509EncodedKeySpec;
 import java.lang.OutOfMemoryError;
 
 import javax.xml.bind.JAXBContext;
 import javax.xml.bind.JAXBElement;
 import javax.xml.bind.Unmarshaller;
 
 import org.apache.commons.cli.CommandLine;
 import org.apache.commons.cli.CommandLineParser;
 import org.apache.commons.cli.Options;
 import org.apache.commons.cli.PosixParser;
 
 import com.tresys.jalop.common.JALUtils.DMType;
 import com.tresys.jalop.producer.ApplicationMetadataXML;
 import com.tresys.jalop.producer.CustomXML;
 import com.tresys.jalop.producer.Producer;
 import com.tresys.jalop.producer.LoggerXML;
 import com.tresys.jalop.producer.SyslogXML;
 import com.tresys.jalop.schemas.mil.dod.jalop_1_0.applicationmetadatatypes.ApplicationMetadataType;
 
 public class JALPTest {
 
 	/**
 	 * The main method that gets called to test JALoP.
 	 *
 	 * @param args	the command line arguments
 	 */
 	public static void main(String[] args) {
 		try {
 			Options options = createOptions();
 			CommandLineParser parser = new PosixParser();
 			CommandLine cmd = parser.parse(options, args);
 
 			String pathToXML = null;
 			String type = null;
 			String input = null;
 			String socketPath = null;
 			String privateKeyPath = null;
 			String publicKeyPath = null;
 			String certPath = null;
 			Boolean hasDigest = false;
 			File file = null;
 			ApplicationMetadataXML xml = null;
 
 			if(cmd.hasOption("h")) {
 				System.out.println(usage);
 				return;
 			}
 			if(cmd.hasOption("a")) {
 				pathToXML = cmd.getOptionValue("a");
 			}
 			if(cmd.hasOption("t")) {
 				type = cmd.getOptionValue("t");
 			}
 			if(cmd.hasOption("p")) {
 				file = new File(cmd.getOptionValue("p"));
 			}
 			if(cmd.hasOption("s")) {
 				BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
 				StringBuilder sb = new StringBuilder();
 				String s;
 				while((s = in.readLine()) != null && s.length() != 0) {
 					sb.append(s);
 					sb.append("\n");
 				}
 				input = sb.toString();
 			}
 			if(cmd.hasOption("j")) {
 				socketPath = cmd.getOptionValue("j");
 			}
 			if(cmd.hasOption("k")) {
 				privateKeyPath = cmd.getOptionValue("k");
 			}
 			if(cmd.hasOption("b")) {
 				publicKeyPath = cmd.getOptionValue("b");
 			}
 			if(cmd.hasOption("c")) {
 				certPath = cmd.getOptionValue("c");
 			}
 			if(cmd.hasOption("d")) {
 				hasDigest = true;
 			}
 
 			if(pathToXML != null) {
 				xml = createXML(readXML(pathToXML));
 			}
 
 			Producer producer = createProducer(xml, socketPath, privateKeyPath, publicKeyPath, certPath, hasDigest);
 			callSend(producer, type, input, file);
 
 		} catch (Exception e) {
 			error(e.toString());
 			return;
 		}
 	}
 
 	/**
 	 * Unmarshals an xml file into an ApplicationMetadataType.
 	 *
 	 * @param path	the path to the xml file to be unmarshalled
 	 * @return	the created ApplicationMetadataType
 	 * @throws Exception
 	 */
 	private static ApplicationMetadataType readXML(String path) throws Exception {
 		JAXBContext jc = JAXBContext.newInstance("com.tresys.jalop.schemas.mil.dod.jalop_1_0.applicationmetadatatypes");
 		Unmarshaller unmarshaller = jc.createUnmarshaller();
 		JAXBElement<ApplicationMetadataType> jaxAmt = (JAXBElement<ApplicationMetadataType>) unmarshaller.unmarshal(new File(path));
 		return jaxAmt.getValue();
 	}
 
 	/**
 	 * Creates an ApplicationMetadataXML of the proper type (LoggerXML, SyslogXML, CustomXML).
 	 *
 	 * @param amt	the ApplicationMetadataType that has been unmarshalled from the xml
 	 * @return		the ApplicationMetadataXML of the proper type
 	 * @throws Exception
 	 */
 	private static ApplicationMetadataXML createXML(ApplicationMetadataType amt) throws Exception {
 		ApplicationMetadataXML xml = null;
 		if(amt.getLogger() != null) {
 			xml = new LoggerXML(amt.getLogger());
 		} else if(amt.getSyslog() != null) {
 			xml = new SyslogXML(amt.getSyslog());
 		} else {
 			xml = new CustomXML(amt.getCustom().toString());
 		}
 		xml.setEventId(amt.getEventID());
 		xml.setJournalMetadata(amt.getJournalMetadata());
 		return xml;
 	}
 
 	/**
 	 * Creates a Producer using the given command line params.
 	 *
 	 * @param xml				the ApplicationMetadataXML
 	 * @param socketPath		a String which is the path to the socket
 	 * @param privateKeyPath	a String which is the path to the private key in DER format
 	 * @param publicKeyPath		a String which is the path to the public key in DER format
 	 * @param certPath			a String which is the path to the certificate
 	 * @param hasDigest			a Boolean, true to set a digest method in the producer
 	 * @return	the created Producer
 	 * @throws Exception
 	 */
 	private static Producer createProducer(ApplicationMetadataXML xml,
 												String socketPath,
 												String privateKeyPath,
 												String publicKeyPath,
 												String certPath,
 												Boolean hasDigest) throws Exception {
 		Producer producer  = new Producer(xml);
 		producer.setSocketFile(socketPath);
 
 		if(privateKeyPath != null && !"".equals(privateKeyPath)) {
 
 			File privateKeyFile = new File(privateKeyPath);
 			DataInputStream privateDis = new DataInputStream(new FileInputStream(privateKeyFile));
 			byte[] privateKeyBytes = new byte[(int)privateKeyFile.length()];
 			privateDis.readFully(privateKeyBytes);
 			privateDis.close();
 
 			KeyFactory keyFactory = KeyFactory.getInstance("RSA");
 			KeySpec privateKs = new PKCS8EncodedKeySpec(privateKeyBytes);
 			PrivateKey privateKey = keyFactory.generatePrivate(privateKs);
 
 			File publicKeyFile = new File(publicKeyPath);
 			DataInputStream publicDis = new DataInputStream(new FileInputStream(publicKeyFile));
 			byte[] publicKeyBytes = new byte[(int)publicKeyFile.length()];
 			publicDis.readFully(publicKeyBytes);
 			publicDis.close();
 
 			KeySpec publicKs = new X509EncodedKeySpec(publicKeyBytes);
 			PublicKey publicKey = keyFactory.generatePublic(publicKs);
 
 			producer.setPrivateKey(privateKey);
 			producer.setPublicKey(publicKey);
 		}
 
 		if(certPath != null && !"".equals(certPath)) {
 			InputStream inputStream = new FileInputStream(certPath);
 			CertificateFactory cf = CertificateFactory.getInstance("X.509");
 			X509Certificate cert = (X509Certificate)cf.generateCertificate(inputStream);
 			inputStream.close();
 			producer.setCertificate(cert);
 		}
 
 		if(hasDigest) {
 			producer.setDigestMethod(DMType.SHA256);
 		}
 
 		return producer;
 	}
 
 	/**
 	 * Calls the right method in Producer to sign and send the data based on the type.
 	 *
 	 * @param producer	the Producer
 	 * @param type		the type input (l, a, j, f)
 	 * @param input		a String that is a buffer
 	 * @param file		a File which contains the buffer
 	 * @throws Exception
 	 */
 	private static void callSend(Producer producer, String type, String input, File file) throws Exception {
 
 		if("l".equals(type)) {
 			if(file != null) {
 				producer.jalpLog(file);
 			} else {
 				producer.jalpLog(input);
 			}
 		} else if("a".equals(type)) {
 			if(file != null) {
 				producer.jalpAudit(file);
 			} else {
 				producer.jalpAudit(input);
 			}
 		} else if("j".equals(type)) {
 			if(file != null) {
 				producer.jalpJournal(file);
 			} else {
 				producer.jalpJournal(input);
 			}
 		} else if("f".equals(type)) {
 			//File descriptors will be added to a later release
 			error("file descriptor handling has not been implemented yet.");
 		} else {
 			error("record type of 'j', 'a', 'l', or 'f', must be specified.");
 		}
 	}
 
 	static final String usage = "Usage:\n"
 		+"-a      (optional) the full, or relative path to a file to use for generating the application metadata.\n"
 		+"-p      The full or relative path to a file that should be used as the payload for this particular record.\n"
 		+"-s      Indicates the payload should be taken from <stdin>.\n"
		+"-t, --type=T    Indicates which type of data to send: 'j' (journal record), 'a' (audit record), 'l' (log entry), or 'f' (journal record using file descriptor passing).\n"
 		+"-h      Print a summary of options.\n"
         +"-j      The full or relative path to the JALoP socket.\n"
         +"-k      The full or relative path to a private key file to be used for signing. Must also specify '-a'.\n"
         +"-b      The full or relative path to a public key file to be used for signing. Must also specify '-a'.\n"
         +"-c      The full or relative path to a certificate file to be used for signing. Requires '-k'.\n"
         +"-d      Calculates and adds a SHA256 digest of the payload to the application metadata. Must also specify '-a'.\n";
 
 	/**
 	 * Prints an error message along with proper usage.
 	 *
 	 * @param message	The String to print out as the error message
 	 */
 	private static void error(String message) {
 		StringBuilder error = new StringBuilder("Error: bad usage, ");
 		error.append(message);
 		error.append("\n");
 		error.append(usage);
 		System.out.println(error);
 	}
 
 	/**
 	 * Creates the options that can be used on the command line.
 	 *
 	 * @return	the Options for the command line parser
 	 */
 	private static Options createOptions() {
 		Options options = new Options();
 		options.addOption("a", true, "(optional) the full, or relative path to a file to use for generating the application metadata.");
 		options.addOption("p", true, "The full or relative path to a file that should be used as the payload for this particular record.");
 		options.addOption("s", false, "Indicates the payload should be taken from <stdin>.");
		options.addOption("t", "type", true, "Indicates which type of data to send: 'j' (journal record), 'a' (audit record), 'l' (log entry), or 'f' (journal record using file descriptor passing).");
 		options.addOption("h", false, "Print a summary of options.");
 		options.addOption("j", true, "The full or relative path to the JALoP socket.");
 		options.addOption("k", true, "The full or relative path to a private key file to be used for signing. Must also specify '-a'.");
 		options.addOption("b", true, "The full or relative path to a public key file to be used for signing. Must also specify '-a'.");
 		options.addOption("c", true, "The full or relative path to a certificate file to be used for signing. Requires '-k'.");
 		options.addOption("d", false, "Calculates and adds a SHA256 digest of the payload to the application metadata. Must also specify '-a'.");
 		return options;
 	}
 }
