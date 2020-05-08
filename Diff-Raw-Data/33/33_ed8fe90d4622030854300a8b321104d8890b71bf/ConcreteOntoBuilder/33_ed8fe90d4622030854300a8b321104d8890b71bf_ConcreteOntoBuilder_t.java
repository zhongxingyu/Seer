 package ontology;
 
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
 import java.rmi.RemoteException;
 import java.rmi.registry.LocateRegistry;
 import java.rmi.registry.Registry;
 import java.rmi.server.UnicastRemoteObject;
 import java.util.Collection;
 
 import com.hp.hpl.jena.query.Dataset;
 import com.hp.hpl.jena.query.ReadWrite;
 import com.hp.hpl.jena.rdf.model.Model;
 import com.hp.hpl.jena.tdb.TDBFactory;
 
 public class ConcreteOntoBuilder implements OntoBuilder {
 
 	private static final String domain;
 	private static final String dbDir;
 	private static final int suggestedMaximumNumberOfTriples;
	private static final String myIp;
 	private Dataset dataset;
 
 	static {
 		dbDir = Conf.dbDir;
 		domain = Conf.domain;
 		suggestedMaximumNumberOfTriples = Conf.suggestedMaxNumberOfTriples;
		myIp = Conf.myIp;
 	}
 
 	public static void main(String[] args) {
		try {
			String logFilePath = "log";
			System.setErr(new PrintStream(new File(logFilePath)));
		}
		catch (FileNotFoundException e1) {
			e1.printStackTrace();
		}
		
		System.setProperty("java.rmi.server.hostname", myIp);
 		System.setProperty("java.security.policy", "file:config/rmi.policy");
 		if (System.getSecurityManager() == null) {
 			System.setSecurityManager(new SecurityManager());
 		}
 		String name = "ontoBuilder";
 		OntoBuilder ob = new ConcreteOntoBuilder();
 		try {
 			OntoBuilder stub = (OntoBuilder) UnicastRemoteObject.exportObject(ob, 0);
 			Registry reg = LocateRegistry.createRegistry(1099); // porta di
 																// default
 			reg.rebind(name, stub);
			System.out.println("Bind eseguito. In attesa dei client");
 		}
 		catch (RemoteException e) {
 			e.printStackTrace();
 		}
 	}
 
 	public ConcreteOntoBuilder() {
 		this.dataset = TDBFactory.createDataset(dbDir);
 	}
 
 	@Override
 	public int suggestedMaximumNumberOfTriples() throws RemoteException {
 		return suggestedMaximumNumberOfTriples;
 	}
 
 	@Override
 	public void addTriples(Collection<Triple> triples) throws RemoteException {
 
 		dataset.begin(ReadWrite.WRITE);
 
 		Model model = dataset.getDefaultModel();
 		for (Triple t : triples) {
 			model.add(model.createResource(domain + t.subject),
 					model.createProperty(domain + t.property),
 					model.createResource(domain + t.object));
 		}
 
 		dataset.commit();
 		dataset.end();
 	
 	}
 }
