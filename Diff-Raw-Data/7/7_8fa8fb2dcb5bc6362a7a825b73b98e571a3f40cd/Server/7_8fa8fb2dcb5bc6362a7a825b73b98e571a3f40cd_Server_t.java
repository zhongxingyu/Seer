 package desperatehousepi.Server;
 
 import desperatehousepi.Crust.Crust;
 import desperatehousepi.Crust.Interest;
 import desperatehousepi.Crust.Interests;
 import desperatehousepi.Crust.Relationship;
 
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.net.*;
 import java.util.Date;
 import java.util.Iterator;
 import java.util.Random;
 
 public class Server implements Runnable{
 	
 	public static final int SOCKET_DEFAULT = 9999;
 	public static final int SOCKET_TEST = 9998;
 	
 	ServerSocket myServer = null;
 	Socket clientSocket = null;
 	static Socket interactSocket = null;
 	ObjectInputStream inputStream;
 	static ObjectInputStream intInStream;
 	ObjectOutputStream outputStream;
 	static ObjectOutputStream intOutStream;
 	Package recvPackage;
 	Crust myCrust;
 	int mySocket;
 	boolean listening = true;
 	
 	public Server(Crust c, int socketNum){
 		myCrust = c;
 		mySocket = socketNum;
 	}
 	
 	public void interact(Relationship rel, int socketNum){
 		
 		Package sendPackage = new Package(myCrust.get("fullName"));
 		sendPackage.setInterests(myCrust.getInterests());
 		sendPackage.setTraits(myCrust.getTraits());
 		sendPackage.setNeeds(myCrust.getNeed("Hunger"), myCrust.getNeed("Energy"), myCrust.getNeed("Entertainment"));
 		
 		Package receivePackage = null;
 		
 		//Create the socket
 		try {
 			interactSocket = new Socket(InetAddress.getByName(rel.getContactAddress()), socketNum);
 		} catch (Exception e){ e.printStackTrace(); }
 		
 		//Create the output stream to write to server
 		try {
 			intOutStream = new ObjectOutputStream(interactSocket.getOutputStream());
 		} catch (IOException e) { e.printStackTrace(); }
 		
 		//Write the object to the server
 		try {
 			intOutStream.writeObject(sendPackage);
 		} catch (IOException e) { e.printStackTrace(); }
 		
 		//Create the input stream to read from the server
 		try {
 			intInStream = new ObjectInputStream(interactSocket.getInputStream());
 		} catch (IOException e) { e.printStackTrace(); }
 		
 		//Read from the server
 		try {
 			receivePackage = (Package) intInStream.readObject();
 			conversate(receivePackage, InetAddress.getByName(rel.getContactAddress()));
 		} catch (Exception e){ e.printStackTrace(); }
 		
 		//Close all of the streams
 		try {
 			intInStream.close();
 			intOutStream.close();
 			interactSocket.close();
 		} catch (IOException e) { e.printStackTrace(); }
 	}
 	
 	public double posCorrelation(int A, int B){
 		return (100-(A-B))/1000;
 	}
 	
 	public double negCorrelation(int A, int B){
 		return ((A-B)-100)/1000;
 	}
 	
 	public double negGivenHigh(int A, int B){
 		return ((-A-B)/2000);
 	}
 	
 	public Factors calcTraitFactors(Package pack, Factors factor){
 		
 		double result;
 		
 		//Warmth is a positive correlation trait
 		result = posCorrelation(pack.traits[0].getValue(), myCrust.getTraits()[0].getValue());
 		if(result<factor.largestFactor){
 			factor.largestFactor = result;
 			factor.largestFactorString = "Warmth";
 		}
 		factor.odds+=result;
 		
 		//Reasoning is a positive correlation trait
 		result = posCorrelation(pack.traits[1].getValue(), myCrust.getTraits()[1].getValue());
 		if(result<factor.largestFactor){
 			factor.largestFactor = result;
 			factor.largestFactorString = "Reasoning";
 		}
 		factor.odds+=result;
 		
 		//Emotional Stability is a positive correlation trait
 		result = posCorrelation(pack.traits[2].getValue(), myCrust.getTraits()[2].getValue());
 		if(result<factor.largestFactor){
 			factor.largestFactor = result;
 			factor.largestFactorString = "Emotional Stability";
 		}
 		factor.odds+=result;
 		
 		//Dominance is a negative correlation trait
 		result = negCorrelation(pack.traits[3].getValue(), myCrust.getTraits()[3].getValue());
 		if(result<factor.largestFactor){
 			factor.largestFactor = result;
 			factor.largestFactorString = "Dominance";
 		}
 		factor.odds+=result;
 		
 		//Liveliness is a positive correlation trait
 		result = posCorrelation(pack.traits[4].getValue(), myCrust.getTraits()[4].getValue());
 		if(result<factor.largestFactor){
 			factor.largestFactor = result;
 			factor.largestFactorString = "Liveliness";
 		}
 		factor.odds+=result;
 		
 		//Rule Consciousness is a positive correlation trait
 		result = posCorrelation(pack.traits[5].getValue(), myCrust.getTraits()[5].getValue());
 		if(result<factor.largestFactor){
 			factor.largestFactor = result;
 			factor.largestFactorString = "Rule Consciousness";
 		}
 		factor.odds+=result;
 		
 		//Social Boldness is a positive correlation trait
 		result = posCorrelation(pack.traits[6].getValue(), myCrust.getTraits()[6].getValue());
 		if(result<factor.largestFactor){
 			factor.largestFactor = result;
 			factor.largestFactorString = "Social Boldness";
 		}
 		factor.odds+=result;
 		
 		//Sensitivity is a positive correlation trait
 		result = posCorrelation(pack.traits[7].getValue(), myCrust.getTraits()[7].getValue());
 		if(result<factor.largestFactor){
 			factor.largestFactor = result;
 			factor.largestFactorString = "Sensitivity";
 		}
 		factor.odds+=result;
 		
 		//Vigilance is negative to everything given high levels
 		result = negGivenHigh(pack.traits[8].getValue(), myCrust.getTraits()[8].getValue());
 		if(result<factor.largestFactor){
 			factor.largestFactor = result;
 			factor.largestFactorString = "Vigilance";
 		}
 		factor.odds+=result;
 		
 		//Abstractedness is a positive correlation trait
 		result = posCorrelation(pack.traits[9].getValue(), myCrust.getTraits()[9].getValue());
 		if(result<factor.largestFactor){
 			factor.largestFactor = result;
 			factor.largestFactorString = "Abstractedness";
 		}
 		factor.odds+=result;
 		
 		//Privateness is negative to everything given high levels
 		result = negGivenHigh(pack.traits[10].getValue(), myCrust.getTraits()[10].getValue());
 		if(result<factor.largestFactor){
 			factor.largestFactor = result;
 			factor.largestFactorString = "Privateness";
 		}
 		factor.odds+=result;
 		
 		//Apprehensiveness is negative to everything given high levels
 		result = negGivenHigh(pack.traits[11].getValue(), myCrust.getTraits()[11].getValue());
 		if(result<factor.largestFactor){
 			factor.largestFactor = result;
 			factor.largestFactorString = "Apprehensiveness";
 		}
 		factor.odds+=result;
 		
 		//Openness To Change is a positive correlation trait
 		result = posCorrelation(pack.traits[12].getValue(), myCrust.getTraits()[12].getValue());
 		if(result<factor.largestFactor){
 			factor.largestFactor = result;
 			factor.largestFactorString = "Openess to Change";
 		}
 		factor.odds+=result;
 		
 		//Self Reliance is a positive correlation trait
 		result = posCorrelation(pack.traits[13].getValue(), myCrust.getTraits()[13].getValue());
 		if(result<factor.largestFactor){
 			factor.largestFactor = result;
 			factor.largestFactorString = "Self Reliance";
 		}
 		factor.odds+=result;
 		
 		//Perfectionism is a positive correlation trait
 		result = posCorrelation(pack.traits[14].getValue(), myCrust.getTraits()[14].getValue());
 		if(result<factor.largestFactor){
 			factor.largestFactor = result;
 			factor.largestFactorString = "Perfectionism";
 		}
 		factor.odds+=result;
 		
 		//Tension is negative to everything given high levels
 		result = posCorrelation(pack.traits[15].getValue(), myCrust.getTraits()[15].getValue());
 		if(result<factor.largestFactor){
 			factor.largestFactor = result;
 			factor.largestFactorString = "Tension";
 		}
 		factor.odds+=result;
 		
 		return factor;
 	}
 	
 	public Factors calcInterestFactors(Package pack, Factors factor){
 		
 		Iterator<Integer> interestIter = pack.interests.iterator();
 		Iterator<Integer> intLevelIter = pack.interestLevel.iterator();
 		
 		while(interestIter.hasNext() && intLevelIter.hasNext()){
 			int interestID = interestIter.next();
 			int interestLevel = intLevelIter.next();
 			
 			Interest localInt = Interests.containsValue(myCrust.getInterests(), interestID);
 			
 			if(localInt == null) continue;
 			
 			double result = ((((localInt.getImportance()+interestLevel+200)/2)-100)/1000);
 			if(result<factor.largestFactor){
 				factor.largestFactor = result;
 				factor.largestFactorString = "Interest: "+localInt.getName();
 			}
 			factor.odds+=result;
 		}
 		
 		return factor;
 	}
 	
 	public Factors calcNeedFactors(Package pack, Factors factor){
 		
 		//Apply hunger
 		double result = ((((pack.hunger+myCrust.getNeed("Hunger")+200)/2)-100)/1000);
 		if(result<factor.largestFactor){
 			factor.largestFactor = result;
 			factor.largestFactorString = "Hunger";
 		}
 		factor.odds+=result;
 		
 		//Apply energy
 		result = ((((pack.energy+myCrust.getNeed("Energy")+200)/2)-100)/1000);
 		if(result<factor.largestFactor){
 			factor.largestFactor = result;
 			factor.largestFactorString = "Energy";
 		}
 		factor.odds+=result;
 		
 		//Apply hunger
 		result = ((((pack.entertainment+myCrust.getNeed("Entertainment")+200)/2)-100)/1000);
 		if(result<factor.largestFactor){
 			factor.largestFactor = result;
 			factor.largestFactorString = "Entertainment";
 		}
 		factor.odds+=result;
 		
 		return factor;
 	}
 	
 	public void conversate(Package pack, InetAddress address){
 		
 		boolean interactionStored = false;
		Date d = new Date();
 		int chemistry = 0;
 		Factors factor = new Factors(Factors.ODDS_BASE, Double.POSITIVE_INFINITY, "None");
 		
 		factor = calcTraitFactors(pack, factor);
 		factor = calcInterestFactors(pack, factor);
 		factor = calcNeedFactors(pack, factor);
 		
 		Random rand = new Random();
 		
 		int interactionResult = rand.nextInt(101);
 		interactionResult*=factor.odds;
 		interactionResult-=Factors.ODDS_BALANCE;
 		interactionResult/=1000;
 		
 		for(Relationship r : myCrust.getRelationships()){
 			if(r.getContactName()==pack.name){
 				
 				r.setChemistry(chemistry+interactionResult);
 				r.log.add("Interacted ["+d.toString()+"] : Result "+interactionResult+" : Biggest Negative Factor: "+factor.largestFactorString);
 				r.setLastMeeting(d.toString());
 				interactionStored = true;
 				break;
 			}
 		}
 		
 		if(!interactionStored){
 			myCrust.addRelationship(pack.name, address.toString(), interactionResult);
			myCrust.getRelationships().getLast().setFirstMet(d.toString());
			myCrust.getRelationships().getLast().setLastMeeting(d.toString());
 			interactionStored = true;
 		}
 		
 		return;
 	}
 
 	@Override
 	public void run() {
 		
 		try{
 			myServer = new ServerSocket(mySocket);
 		}catch (IOException e){
 			System.out.println(e);
 		}
 		try{
 			
 			while(true){
 				while(listening){
 					
 					clientSocket = myServer.accept();
 					
 					if(!listening) return;
 					
 					inputStream = new ObjectInputStream(clientSocket.getInputStream());
 					outputStream = new ObjectOutputStream(clientSocket.getOutputStream());
 					
 					try {
 						recvPackage = (Package) inputStream.readObject();
 						conversate(recvPackage, clientSocket.getInetAddress());
 					} catch (ClassNotFoundException e) { e.printStackTrace(); }
 					
 					//Generate return package
 					Package sndPackage = new Package(myCrust.get("fullName"));
 					sndPackage.setInterests(myCrust.getInterests());
 					sndPackage.setTraits(myCrust.getTraits());
 					sndPackage.setNeeds(myCrust.getNeed("Hunger"), myCrust.getNeed("Energy"), myCrust.getNeed("Entertainment"));
 					outputStream.writeObject(sndPackage);
 					
 					inputStream.close();
 					outputStream.close();
 					clientSocket.close();
 					
 				}
 			}
 		}
 		catch (IOException e){
 			System.out.println(e);
 			
 		}
 	}
 	
 	public void setListening(boolean val){
 		listening = val;
 	}
 	
 }
