 /**
  * 
  */
 package Paxos;
 
 import localServices.Status;
 
 public class PaxosInstance {
 	
 	static public class Proposal implements java.io.Serializable {
 		private static final long serialVersionUID = 1L;
 		static int START_NUMBER = 0;
 		static int MAX_NUMBER = Integer.MAX_VALUE;
 		public int number = START_NUMBER;
 		public Object value = null;
 		
 		Proposal(int number, Object value) {
 			this.number = number;
 			this.value = value;
 		}
 		
 		public String toString() {
 			if (value != null) {
 				return String.format("%d#%s", number, value.toString());
 			}
 			return String.format("%d#null", number);
 		}
 
 		static public Proposal parse(String str) {
 			String[] s = str.split("#");
 			if (s[1] != "null") {
 				return new Proposal(Integer.parseInt(s[0]), s[1]);
 			}
 			return new Proposal(Integer.parseInt(s[0]), null);
 		}
 	}
 	
 	/**
 	/* An acceptor needs to remember only the highest numbered proposal that it has ever accepted 
 	/* and the number of the highest numbered prepare request to which it has responded.
 	 */
 	int prepareNumber = 0;
 	/**
 	 * 	To be chosen, a proposal must be accepted by at least one acceptor.
 	 */
 	Proposal choosen = null;
 
 	public String toString() {
 		if (choosen != null) {
 			return String.format("%d$%s", prepareNumber, choosen.toString());
 		}
 		return String.format("%d$null", prepareNumber);
 	}
 
 	static public PaxosInstance parse(String str) {
 		String[] s = str.split("#");
 		PaxosInstance paxos = new PaxosInstance();
 		paxos.prepareNumber = Integer.parseInt(s[0]);
 		if (s[1] != null) {
 			paxos.choosen = Proposal.parse(s[1]);
 		} else {
 			paxos.choosen = null;
 		}
 		return paxos;
 	}
 	
 	/**
 	 * A promise never again to accept a proposal numbered less than n.
 	 */
 	public Status prepare(int number, Proposal choosen) {
 		
 		choosen = this.choosen;
 		
 		//
 		// If an acceptor receives a prepare request with number n greater
 		// than that of any prepare request to which it has already responded,
 		// then it responds to the request with a promise not to accept any more
 		// proposals numbered less than n and with the highest-numbered proposal
 		// (if any) that it has accepted.
 		//
 		
 		if (number >= prepareNumber) {
 			prepareNumber = number;
 			return Status.SUCCESS;
 		}
 	
 		return Status.DENIED;
 	}
 
 	public Status accept(Proposal proposal) {
		
 		//
 		// P1a . An acceptor can accept a proposal numbered n iff it has not responded to a prepare 
 		// request having a number greater than n.
 		//
 		
 		if (proposal.number < prepareNumber) {
 			return Status.DENIED;
 		}
 		
 		//
 		// P1. An acceptor must accept the first proposal that it receives.
 		//
 		
 		if (choosen == null) {
 			choosen = proposal;
 			return Status.SUCCESS;
 		}
 		
 		//
 		// P2. If a proposal with value v is chosen, then every higher-numbered proposal that is 
 		// chosen has value v.
 		//
 		
 		if (proposal.number >= choosen.number &&
 			proposal.value.equals(choosen.value)) {
 			choosen = proposal;
 			prepareNumber = proposal.number; 
 			return Status.SUCCESS;
 		}
 		
 		//
 		// Proposal was not choosen.
 		//
 		
 		return Status.DENIED;
 	}
 	
 	public void learn(Proposal proposal) {
 		proposal.number = Proposal.MAX_NUMBER;
 		prepareNumber = proposal.number;
 		choosen = proposal;
 	}
 	
 	public Object getLearnedValue() {
 		if (prepareNumber != Proposal.MAX_NUMBER) {
 			return null;
 		}
 		return choosen.value;
 	}
 }
