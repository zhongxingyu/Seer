 
 package nyu.ads.conctrl.site;
 
 import java.util.ArrayList;
 
 import nyu.ads.conctrl.entity.*;
 
 import nyu.ads.conctrl.site.entity.*;
 /**
  * Class Site
  * 
  * @author Yaxing Chen (N16929794)
  *
  */
 public class Site{
 	
 	private String buffer;// message buffer, containing instructions for different transactions
 	
 	private LockManager lockMng;// lock manager obj, to handle locks in a certain site
 	
 	private DataManager dataMng;// data manager obj, to manage all data in this site, including r/w operations
 	
 	private int status = 1; //0: failed, 1: running
 	
 	//private ArrayList<Integer> commitLog; //committed transaction id list, sorted based on timestamp
 	
 	public Site() {
 		buffer = "";
 		lockMng = new LockManager();
 		dataMng = new DataManager();
 		//commitLog = new ArrayList<Integer>();
 	}
 	
 	/**
 	 * site processor,
 	 * parse messages from buffer and execute,
 	 * require locks from lock manager,
 	 * execute W/R operation through data manager,
 	 * 
 	 * able to distinguish between read-only transaction and normal transaction and process differently
 	 * @return String containing execution result that can be parsed by TM
 	 */
 	public String process() {
		if(this.status == 0) {
			return InstrCode.EXE_RESP + " -1";
		}
 		String[] msg = buffer.split(" ");
 		InstrCode opcode = InstrCode.valueOf(msg[0]);
 		String result = null;
 		switch(opcode) {
 		case INSTR:
 			result = op_instr(msg);
 			break;
 		case PREPARE_COMMIT:
 			result = op_commit_query(Integer.parseInt(msg[1]));
 			break;
 		case COMMIT:
 			result = op_commit(Integer.parseInt(msg[1]));
 			break;
 		case ABORT:
 			result = op_abort(Integer.parseInt(msg[1]));
 			break;
 		case DUMP:
 			String resName = "";
 			if(msg.length >= 2) {
 				resName = msg[1];
 			}
 			result = op_dump(resName);
 			break;
 		case FAIL:
 			op_fail();
 			break;
 		case RECOVER:
 			op_recover();
 			break;
 		case INIT:
 			op_init_res(msg);
 			break;
 		case SNAPSHOT:
 			op_snapshot(msg[1]);
 			break;
 		default:
 			break;
 		}
 		return result;
 	}
 	
 	/**
 	 * Fail this site
 	 * clear lock manager & data manager
 	 * change site status
 	 */
 	public void op_fail() {
 		this.status = 0;
 		lockMng.clearLocks();
 	}
 	
 	/**
 	 * recover this site
 	 * change status
 	 * recover transactions after server failed and restarted
 	 * recover from transaction log and commit log, recover only committed transactions
 	 * require recover lock for all replicated resources
 	 */
 	public void op_recover() {
 		this.status = 1;
 		String[] uniq = dataMng.getUniqRes();
 		for(String res : uniq) {
 			lockMng.recoverLock(res);
 		}
 	}
 	
 	public int getStatus() {
 		return this.status;
 	}
 	
 	/**
 	 * dump all information on this site, 
 	 * including all locks, resources, status, etc.
 	 * encapsulated as an object and return to TM
 	 * @return SiteQueryEnty query result obj
 	 */
 	public SiteQueryEnty query() {
 		SiteQueryEnty enty = new SiteQueryEnty();
 		/*
 		 * query code
 		 */
 		return enty;
 	}
 	
 	/**
 	 * pass message to buffer
 	 * @param String instr
 	 */
 	public void setBuffer(String instr) {
 		this.buffer = instr;
 	}
 	
 	/**
 	 * resList containing opcode INIT, so res list starts from resList[1]
 	 * @param resList
 	 */
 	public void op_init_res(String[] msg) {
 		ArrayList<String> uniq = new ArrayList<String>();
 		for(int i = 1; i < msg.length; i ++) {
 			String[] tmp = msg[i].split(":");
 			if(tmp.length == 3 && tmp[2].equals("UNIQ")) {
 				uniq.add(tmp[0]);
 			}
 			dataMng.newRes(tmp[0], tmp[1]);
 		}
 		String[] q = new String[uniq.size()];
 		dataMng.setUniqRes(uniq.toArray(q));
 	}
 	
 	
 	/**
 	 * w/r/ro operation handler
 	 * @param msg
 	 * @return
 	 */
 	public String op_instr(String[] msg) {
 		if(msg.length < 5) {
 			return "EXEE_RESP 0 UNKNOWN_MSG";
 		}
 		int transacId = Integer.parseInt(msg[1]);
 		String res = msg[4];
 		if(msg[3].equals("W")) {
 			if(msg.length < 6) {
 				return "EXEE_RESP 0 UNKNOWN_MSG";
 			}
 			String resp = lockMng.lock(transacId, res, LockType.WRITE);
 			if(resp == null) {
 				dataMng.write(transacId, res, msg[5]);
 				return InstrCode.EXE_RESP + " 1";
 			}
 			else {
 				return resp;
 			}
 		}
 		else if(msg[3].equals("R")) {
 			String resp = lockMng.lock(transacId, res, LockType.READ);
 			if(resp == null) {
 				return InstrCode.EXE_RESP + " " + res + ":" + dataMng.read(transacId, res);
 			}
 			else {
 				return resp;
 			}
 		}
 		else if(msg[3].equals("RO")) {
 			return InstrCode.EXE_RESP + " " + res + ":" + dataMng.roRead(msg[4], new TimeStamp(Long.parseLong(msg[2])));
 		}
 		return "EXEE_RESP 0 UNKNOWN_OPERATION";
 	}
 	
 	public String op_commit_query(int transactionId) {
 		
 		return null;
 	}
 	
 	/**
 	 * write log to db,
 	 * clear recover locks
 	 * write commit log
 	 * @param trasactionId
 	 * @return
 	 */
 	public String op_commit(int transacId) {
 		dataMng.commitT(transacId);
 		lockMng.unlockTransac(transacId);
 		return "COMMIT_RESP 1";
 	}
 	
 	/**
 	 * abort certain transaction T
 	 * clear all T's info in transaction log, lock table
 	 * @param transacId
 	 * @return boolean
 	 */
 	public String op_abort(int transactionId) {
 		lockMng.unlockTransac(transactionId);
 		dataMng.abortT(transactionId);
 		return null;
 	}
 	
 	public String op_dump(String resName) {
 		if(resName == null || resName.length() == 0) {
 			return dataMng.dump();
 		}
 		return dataMng.dump(resName);
 	}
 	
 	/**
 	 * based on given timestamp, take a snapshot of committed source values
 	 * @param timestamp
 	 */
 	public void op_snapshot(String timestamp) {
 		dataMng.snapshot(timestamp);
 	}
 	/*
 	 * test
 	 */
 	public static void main(String[] args) {
 		Site site = new Site();
 		site.setBuffer("INIT X1:19 X2:12:UNIQ X6:15");
 		site.process();
 		site.setBuffer("DUMP");
 		System.out.println(site.process());
 		
 		site.setBuffer("INSTR 1 02366662 W X1 6");
 		System.out.println(site.process());
 		
 		site.setBuffer("DUMP");
 		System.out.println(site.process());
 		
 		site.setBuffer("INSTR 1 02366662 R X1");
 		System.out.println(site.process());
 		
 		site.setBuffer("INSTR 2 02366662 R X1 6");
 		System.out.println(site.process());
 		
 		String timestamp = Long.toString(new TimeStamp().getTime());
 		site.setBuffer("SNAPSHOT " + timestamp);
 		site.process();
 		
 		site.setBuffer("COMMIT 1");
 		System.out.println(site.process());
 		
 		site.setBuffer("DUMP");
 		System.out.println(site.process());
 		
 		timestamp = Long.toString(new TimeStamp().getTime());
 		site.setBuffer("INSTR 6 " + timestamp + " RO X1");
 		System.out.println(site.process());
 		
 		/*expected output:
 			{X1=19, X2=12, X6=15}
 			EXE_RESP 1
 			{X1=19, X2=12, X6=15}
 			EXE_RESP X1:6
 			EXE_RESP 0 {1} 2
 			COMMIT_RESP 1
 			{X1=6, X2=12, X6=15}
 			X1:19
 		 */
 	}
 }
