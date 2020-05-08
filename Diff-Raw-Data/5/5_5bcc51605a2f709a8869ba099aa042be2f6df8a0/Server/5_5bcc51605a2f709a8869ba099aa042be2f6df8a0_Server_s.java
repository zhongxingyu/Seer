 package server;
 
 import java.io.BufferedReader;
 import java.io.DataOutputStream;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.net.ServerSocket;
 import java.net.Socket;
 import java.nio.file.AccessDeniedException;
 import java.security.InvalidParameterException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map.Entry;
 import java.util.regex.Pattern;
 
 import javax.net.ssl.*;
 
import com.sun.org.apache.xerces.internal.impl.dv.dtd.ENTITYDatatypeValidator;

 import java.security.*;
 
 import util.*;
 
 public class Server {
 	private static HashMap<Integer, Division> divisions;
 	private static ArrayList<Doctor> docs;
 	private static ArrayList<Nurse> nurses;
 	private static ArrayList<Patient> patients;
 	private static ArrayList<Record> records;
 
 	private static GovernmentAgent agent;
 	private static Log log = new Log();
 
 	// trying to initiate a SSLSocketfactory to the handshake
 	private SSLServerSocketFactory socketFac = (SSLServerSocketFactory) SSLServerSocketFactory
 			.getDefault();
 
 	public static void main(String[] args) {
 
 		divisions = new HashMap<Integer, Division>();
 
 		// http://en.wikipedia.org/wiki/Uppsala_University_Hospital#Divisions
 		divisions.put(0, new Division(
 				"Diagnostics, Anesthesia and Technology Division"));
 		divisions.put(1, new Division("Emergency and Rehabilitation Division"));
 		divisions.put(2, new Division("Oncology, Thorax and Medical Division"));
 		divisions.put(3, new Division("Neurology Division"));
 		divisions.put(4, new Division("Psychiatry Division"));
 		divisions.put(5, new Division("Surgery Division"));
 		divisions
 				.put(6, new Division("Women's Health and Pediatrics Division"));
 		divisions.put(7, new Division("Socialstyrelsen"));
 
 		docs = new ArrayList<Doctor>();
 		docs.add(new Doctor("Fulgore d0_0", divisions.get(0)));
 		docs.add(new Doctor("Fulgore d0_1", divisions.get(0)));
 
 		docs.add(new Doctor("Riptor d1_0", divisions.get(1)));
 
 		nurses = new ArrayList<Nurse>();
 		nurses.add(new Nurse("Sabrewulf n0_0", divisions.get(0)));
 		nurses.add(new Nurse("Sabrewulf n0_1", divisions.get(0)));
 
 		nurses.add(new Nurse("Eyedol n1_0", divisions.get(1)));
 
 		patients = new ArrayList<Patient>();
 		patients.add(new Patient("Spinal p5_0", "Broken back", divisions.get(0)));
 		patients.add(new Patient("Spinal p5_1", "Broken toe", divisions.get(0)));
 
 		patients.add(new Patient("Spinal p4_0", "Fractured skull", divisions
 				.get(1)));
 
 		agent = new GovernmentAgent("FRA", divisions.get(7));
 
 		records = new ArrayList<Record>();
 		records.add(createJournal(patients.get(0), docs.get(0), nurses.get(0)));
 		records.add(createJournal(patients.get(1), docs.get(1), nurses.get(1)));
 		records.add(createJournal(patients.get(2), docs.get(2), nurses.get(2)));
 
 		Server s = new Server();
 		s.run();
 	}
 
 	private void run() {
 		HashMap<String, Pattern> commands = new HashMap<String, Pattern>();
 
 		/*
 		 * Commands:
 		 * 
 		 * All: list records read record [record_id]
 		 * 
 		 * Nurse: list patients write record [record_id] [data]
 		 * 
 		 * Doctor: list patients list nurses write record [record_id] [data]
 		 * create record [patient_id] [nurse_id] [data] assign [nurse_id] to
 		 * [patient_id]
 		 * 
 		 * Government Agent: delete [record_id]
 		 */
 
 		commands.put("list records", Pattern.compile("list records"));
 		commands.put("list nurses", Pattern.compile("list nurses"));
 		commands.put("list patients", Pattern.compile("list patients"));
 
 		commands.put("read record",
 				Pattern.compile("read record (?<recordid>\\d+)"));
 		commands.put("write record",
 				Pattern.compile("write record (?<recordid>\\d+) (?<data>).*"));
 		commands.put("delete record",
 				Pattern.compile("delete record (?<recordid>\\d+)"));
 
 		commands.put(
 				"create record",
 				Pattern.compile("create record (?<patientid>\\d+) (?<nurseid>\\d+) (?<data>).*"));
 		commands.put("assign nurse", Pattern
 				.compile("assign (?<nurseid>\\d+) to (?<patientid>\\d+)"));
 
 		// Temporary tcp-connection
 		// TODO: FIXME: Make this an SSLServersocket instead...
 		//SSLSocket ss;
 		ServerSocket ss;
 		
 		try {
 			//creates server socket
 			//ss = socketFac.createServerSocket();
 			ss = new ServerSocket(6789);
 
 			System.out.println("Running server ...");
 
 			Socket client;
 			BufferedReader fromClient;
 			DataOutputStream toClient;
 			String readLine = null;
 
 			while (true) {
 			
 			//listens on a connection
 			//do we need to bind it?
 			//SSLSocket socket =(SSLSocket)ss.accept();
 			
 			//sets up the handshake
 			//SSLSession session = socket.getSession();
 			
 			//forces the client to authenticate itself. Men hur gr //man det?
 			//TODO server sends it's cert to client
 			//TODO SSLengine
 				//socket.setNeedClientAuth(true);
 				
 				client = ss.accept();
 				System.out.println("Client connected ...");
 
 				fromClient = new BufferedReader(new InputStreamReader(
 						client.getInputStream()));
 				toClient = new DataOutputStream(client.getOutputStream());
 				// TODO: Fix login, fetch real logged in entity
 				Entity entity = patients.get(0);
 
 				toClient.writeBytes(String.format("Welcome %s! %s\n\n", entity.getName(), entity.getClass().getName()));
 				
 				loginClient(fromClient, toClient);
 				
 
 				do {
 					toClient.writeBytes("Enter your command: ");
 					readLine = fromClient.readLine();
 					
 					for (Entry<String, Pattern> e : commands.entrySet()) {
 						if (e.getValue().matcher(readLine).matches()) {
 							toClient.writeChars(handleCommand(entity,
 									e.getKey(), e.getValue()));
 						}
 					}
				} while (readLine != null && readLine.equals("quit"));
 					
 				
 				// Check username
 				//trying to get the name of the "client"
 				//	X509Certificate cert = (X509Certificate)session getPeerCertificateChain()[0];
 				//	String subject = cert.getSubjectDN().getName();
 				//	System.out.println (subject);
 
 			}
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 
 	public interface CommandHandler {
 		public String handleCommand(EntityWithAccessControl entity, Pattern p);
 	}
 
 	public List<Record> getReadableRecords(EntityWithAccessControl entity) {
 		List<Record> result = new ArrayList<Record>();
 
 		for (Record r : records) {
 			if (entity.canAccess(r, EntityWithAccessControl.READ))
 				result.add(r);
 		}
 
 		return result;
 	}
 
 	@SuppressWarnings("serial")
 	HashMap<String, CommandHandler> m = new HashMap<String, CommandHandler>() {
 		{
 			put("list records", new CommandHandler() {
 
 				@Override
 				public String handleCommand(EntityWithAccessControl entity,
 						Pattern p) {
 					StringBuilder sb = new StringBuilder();
 
 					for (Record r : getReadableRecords(entity))
 						sb.append(r.toString() + "\n");
 
 					return sb.toString();
 				}
 
 			});
 		}
 	};
 
 	private String handleCommand(Entity entity, String command, Pattern p) {
 		System.out.println(String.format("Handling command [%s] for [#%d, %s]",
 				command, entity.getId(), entity.getName()));
 		return m.get(command).handleCommand(entity, p);
 	}
 
 	private void loginClient(BufferedReader fromClient,
 			DataOutputStream toClient) {
 
 	}
 
 	public static Record createJournal(Patient patient, Doctor doctor,
 			Nurse nurse) throws InvalidParameterException {
 		if (patient.getDivision() != nurse.getDivision()
 				&& nurse.getDivision() != doctor.getDivision()) {
 			//logs the false case. Should message be included?
 			log.updateLog(new Events(1, doctor, null, false));
 			
 			throw new InvalidParameterException(
 					String.format(
 							"Nurse [%s] is not from the same division [%s] as doctor [%s]",
 							nurse.getName(), patient.getDivision().getId(),
 							doctor.getName()));
 		}
 
 		Record j = new Record(patient, doctor, nurse, patient.getData());
 
 		// log update in true case.
 		log.updateLog(new Events(1, doctor, j, true));
 
 		return j;
 	}
 
 	public String readData(Record journal, EntityWithAccessControl entity)
 			throws AccessDeniedException {
 		try {
 			return journal.readData(entity);
 		} catch (AccessDeniedException e) {
 		
 		//logs in false case
 		log.updateLog(new Events(2, entity, journal, false));
 			e.printStackTrace();
 		}
 		//logs in true case
 		log.updateLog(new Events(2, entity, journal, true));
 		return null;
 	}
 
 	public void writeData(Record journal, EntityWithAccessControl entity,
 			String data) throws AccessDeniedException {
 		try {
 			journal.writeData(entity, data);
 		} catch (AccessDeniedException e) {
 			//logs in false case
 			log.updateLog(new Events(3, entity, journal, false));
 			e.printStackTrace();
 		}
 		//logs in true case
 		log.updateLog(new Events(3, entity, journal, true));
 	}
 
 	public void deleteJournal(Record journal, EntityWithAccessControl entity)
 			throws AccessDeniedException {
 		try {
 			journal.delete(entity);
 		} catch (AccessDeniedException e) {
 		//logs in false case
 		log.updateLog(new Events(4, entity, journal, false));
 			e.printStackTrace();
 		}
 		//logs in true case
 		log.updateLog(new Events(4, entity, journal, true));
 	}
 }
