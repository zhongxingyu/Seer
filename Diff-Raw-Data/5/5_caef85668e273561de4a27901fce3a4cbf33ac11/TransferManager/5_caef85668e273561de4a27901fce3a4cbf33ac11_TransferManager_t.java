 package xtremweb.api.transman;
 
 /**
  * TransferManager is the engine responsible for processing, monitoring 
  * and launching transfers.
  * 
  * Created: Sun Feb 18 17:54:41 2007
  *
  * @author <a href="mailto:fedak@lri.fr">Gilles Fedak</a>
  * @version 1.0
  */
 
 import xtremweb.core.iface.*;
 import xtremweb.core.obj.dr.Protocol;
 import xtremweb.core.obj.dt.Transfer;
 import xtremweb.core.obj.dc.*;
 import xtremweb.serv.dt.*;
 import xtremweb.serv.dt.amazonS3.AmazonS3Transfer;
 import xtremweb.serv.dt.bittorrent.BittorrentTransfer;
 import xtremweb.serv.dt.dummy.DummyTransfer;
 import xtremweb.serv.dt.ftp.FtpTransfer;
 import xtremweb.serv.dt.http.HttpTransfer;
 import xtremweb.serv.dt.scp.ScpTransfer;
 import xtremweb.core.util.*;
 
 import xtremweb.core.log.Logger;
 import xtremweb.core.log.LoggerFactory;
 import xtremweb.core.perf.PerfMonitor;
 import xtremweb.core.perf.PerfMonitorFactory;
 
 import java.util.*;
 import java.rmi.RemoteException;
 import java.util.Collection;
 import java.util.Iterator;
 import javax.jdo.PersistenceManager;
 import javax.jdo.PersistenceManagerFactory;
 import javax.jdo.Query;
 import javax.jdo.Transaction;
 import java.util.concurrent.locks.Lock;
 import java.util.concurrent.locks.ReentrantLock;
 
 import xtremweb.core.util.uri.*;
 import xtremweb.dao.DaoFactory;
 import xtremweb.dao.DaoJDOImpl;
 import xtremweb.dao.InterfaceDao;
 import xtremweb.dao.transfer.DaoTransfer;
 
 /**
  * <code>TransferManager</code>.
  * 
  * @author <a href="mailto:fedak@xtremciel.local">Gilles Fedak</a>
  * @version 1.0
  */
 public class TransferManager {
 
     private InterfaceRMIdt dt = null;
 
     /** time between two periodic activities (in milli seconds) */
     private int timeout = 1000;
     private DaoTransfer daocheck;
     private Timer timer;
     private Lock timerLock = new ReentrantLock();
     private static int timerSchedules = 0;
     private DaoTransfer dao;
     private long maxDownloads = 10;
     private static Vector ongoingUid = new Vector();
 
    // private final static PerfMonitor perf = PerfMonitorFactory
    //	    .createPerfMonitor("TransferManager", "hits per second", 3000);
 
     /*
      * <code>oobTransfers</code> is a Hashtable associating an OOBTransfer to
      * each transfer. It is used to cache OOBTransfer and to avoid creating
      * OOBTransfer for each Transfer scanned in the Database
      */
     private SortedVector oobTransfers;
 
     private Logger log = LoggerFactory.getLogger("Transfer Manager (transman)");
 
     /**
      * Creates a new <code>TransferManager</code> instance.
      * 
      * @param ldt
      *            an <code>InterfaceRMIdt</code> value
      * @param ldr
      *            an <code>InterfaceRMIdr</code> value
      */
     public TransferManager(InterfaceRMIdt ldt) {
 	daocheck = (DaoTransfer) DaoFactory
 		.getInstance("xtremweb.dao.transfer.DaoTransfer");
 	dao = (DaoTransfer) DaoFactory
 		.getInstance("xtremweb.dao.transfer.DaoTransfer");
 	dt = ldt;
 	init();
     }
 
     /**
      * Creates a new <code>TransferManager</code> instance.
      * 
      */
     public TransferManager() {
 	daocheck = (DaoTransfer) DaoFactory
 		.getInstance("xtremweb.dao.transfer.DaoTransfer");
 	dao = (DaoTransfer) DaoFactory
 		.getInstance("xtremweb.dao.transfer.DaoTransfer");
 	init();
     }
 
     /**
      * Creates a new <code>TransferManager</code> instance.
      * 
      * @param comms
      *            a <code>Vector</code> value
      */
     public TransferManager(Vector comms) {
 	daocheck = (DaoTransfer) DaoFactory
 		.getInstance("xtremweb.dao.transfer.DaoTransfer");
 	dao = (DaoTransfer) DaoFactory
 		.getInstance("xtremweb.dao.transfer.DaoTransfer");
 	for (Object o : comms) {
 	    if (o instanceof InterfaceRMIdt)
 		dt = (InterfaceRMIdt) o;
 	}
 	init();
     }
 
     private void init() {
 	oobTransfers = new SortedVector(new OOBTransferOrder());
     }
 
     /**
      * <code>registerTransfer</code> adds a tranfer to the TransferManager The
      * transfer is persisted in the database. It will be later read by the main
      * loop
      * 
      * @param tuid
      *            a <code>String</code> value
      * @param oobt
      *            an <code>OOBTransfer</code> value
      */
     public void registerTransfer(OOBTransfer oobt) {
 	String tuid = oobt.getTransfer().getuid();
 	if ((oobt.getTransfer() != null)
 		&& (oobt.getTransfer().getuid() != null))
 	    log.debug("Transfer already persisted : "
 		    + oobt.getTransfer().getuid());
 	log.debug(" data snapshot just before persisting uid"
 		+ oobt.getData().getuid() + "md5 "
 		+ oobt.getData().getchecksum() + " size "
 		+ oobt.getData().getsize());
 
 	dao.makePersistent(oobt.getData(), true);
 	dao.makePersistent(oobt.getRemoteProtocol(), true);
 	dao.makePersistent(oobt.getLocalProtocol(), true);
 
 	oobt.getRemoteLocator().setdatauid(oobt.getData().getuid());
 	oobt.getLocalLocator().setdatauid(oobt.getData().getuid());
 
 	oobt.getRemoteLocator().setprotocoluid(
 		oobt.getRemoteProtocol().getuid());
 	oobt.getLocalLocator().setprotocoluid(oobt.getLocalProtocol().getuid());
 
 	dao.makePersistent(oobt.getRemoteLocator(), true);
 	dao.makePersistent(oobt.getLocalLocator(), true);
 
 	oobt.getTransfer().setlocatorremote(oobt.getRemoteLocator().getuid());
 	oobt.getTransfer().setlocatorlocal(oobt.getLocalLocator().getuid());
 	oobt.getTransfer().setdatauid(oobt.getData().getuid());
 	dao.makePersistent(oobt.getTransfer(), true);
 
 	// FIXME: should have an assert here
 	if ((tuid != null) && (!tuid.equals(oobt.getTransfer().getuid())))
 	    log.debug(" Transfer has been incorrectly persisted    " + tuid
 		    + "  !=" + oobt.getTransfer().getuid());
     }
 
     public int getTransferStatus(String tid) {
 	try {
 	    dao.beginTransaction();
 	    Transfer t = (Transfer) dao.getByUid(Transfer.class, tid);
 	    dao.commitTransaction();
 	    return t.getstatus();
 	} catch (Exception e) {
 	    log.info("an error occurred while interacting with bd ");
 	    e.printStackTrace();
 	}
 	return -1;
     }
 
     /**
      * <code>start</code> launches periodic TM engine
      */
     public void start(boolean isDaemon) {
 	log.debug("Starting TM Engine");
 	try {
 	    timerLock.lock();
 	    if (timer == null) {
 		timer = new Timer("TransferManagerTimer", isDaemon);
 		timer.schedule(new TimerTask() {
 		    public void run() {
 			checkTransfer();
 		    }
 		}, 0, timeout);
 		timerSchedules++;
 		log.debug("timer schedules : " + timerSchedules);
 	    }
 	} finally {
 	    timerLock.unlock();
 	}
     }
 
     /**
      * <code>start</code> launches periodic Active Data engine
      */
     public void start() {
 	// by default, do not start as a daemon
 	start(false);
     }
 
     /**
      * <code>stop</code> stops periodic TM Engine
      */
     public void stop() {
 	log.debug("Stopping TM Engine");
 	try {
 	    timerLock.lock();
 	    timer.cancel();
 	    timer.purge();
 	    timer = null;
 	} finally {
 	    timerLock.unlock();
 
 	}
     }
 
     public void removeOOBTransfer(Transfer trans) throws OOBException {
 	oobTransfers.removeElement(trans.getuid());
     }
 
     public OOBTransfer createOOBTransfer(Transfer t, DaoTransfer daocheck)
 	    throws OOBException {
 	Data d = null;
 	Locator rl = null;
 	Locator ll = null;
 	Protocol lp = null;
 	Protocol rp = null;
 
 	ll = (Locator) daocheck.getByUid(xtremweb.core.obj.dc.Locator.class,
 		t.getlocatorlocal());
 
 	rl = (Locator) daocheck.getByUid(xtremweb.core.obj.dc.Locator.class,
 		t.getlocatorremote());
 
 	rp = (Protocol) daocheck.getByUid(xtremweb.core.obj.dr.Protocol.class,
 		rl.getprotocoluid());
 
 	lp = (Protocol) daocheck.getByUid(xtremweb.core.obj.dr.Protocol.class,
 		ll.getprotocoluid());
 	// FIXME to use transfet.getdatauid() instead
 	if (!ll.getdatauid().equals(rl.getdatauid()))
 	    throw new OOBException(
 		    "O-O-B Transfers refers to two different data ");
 
 	d = (Data) daocheck.getByUid(xtremweb.core.obj.dc.Data.class,
 		ll.getdatauid());
 	log.debug("OOBTransferFactory create " + t.getuid() + ":" + t.getoob()
 		+ ":" + TransferType.toString(t.gettype()));
 
 	return createOOBTransfer(d, t, rl, ll, rp, lp);
 
     }
 
     /**
      * <code>createOOBTransfer</code> creates an OOBTransfer from the Transfer,
      * Data, Protocol and Locator. The correct class corresponding to the right
      * protocol is selected (Http, Ftp etc..)
      * 
      * @param d
      *            a <code>Data</code> value
      * @param t
      *            a <code>Transfer</code> value
      * @param rl
      *            a <code>Locator</code> value
      * @param ll
      *            a <code>Locator</code> value
      * @param rp
      *            a <code>Protocol</code> value
      * @param lp
      *            a <code>Protocol</code> value
      * @return an <code>OOBTransfer</code> value
      * @exception OOBException
      *                if an error occurs
      */
     public static OOBTransfer createOOBTransfer(Data d, Transfer t, Locator rl,
 	    Locator ll, Protocol rp, Protocol lp) throws OOBException {
 	if (d == null)
 	    throw new OOBException(
 		    "Unable to find the correct OOB Transfers for transfer : "
 			    + t.getuid() + " data (d) is null");
 	if (t == null)
 	    throw new OOBException(
 		    "Unable to find the correct OOB Transfers for transfer : "
 			    + t.getuid() + " transfer (t) is null");
 	if (rl == null)
 	    throw new OOBException(
 		    "Unable to find the correct OOB Transfers for transfer : "
 			    + t.getuid() + " remote locator (rl) is null");
 	if (ll == null)
 	    throw new OOBException(
 		    "Unable to find the correct OOB Transfers for transfer : "
 			    + t.getuid() + " local locator (ll) is null");
 	if (lp == null)
 	    throw new OOBException(
 		    "Unable to find the correct OOB Transfers for transfer : "
 			    + t.getuid() + " local protocol (lp) is null");
 	if (rp == null)
 	    throw new OOBException(
 		    "Unable to find the correct OOB Transfers for transfer : "
 			    + t.getuid() + " remote protocol (rp) is null");
 
 	if (lp.getname().toLowerCase().equals("local")) {
 	    if (rp.getname().toLowerCase().equals("ftp"))
 		return new FtpTransfer(d, t, rl, ll, rp, lp);
 	    if (rp.getname().toLowerCase().equals("http"))
 		return new HttpTransfer(d, t, rl, ll, rp, lp);
 	    if (rp.getname().toLowerCase().equals("dummy"))
 		return new DummyTransfer(d, t, rl, ll, rp, lp);
 	    if (rp.getname().toLowerCase().equals("scp"))
 		return new ScpTransfer(d, t, rl, ll, rp, lp);
 	    if (rp.getname().toLowerCase().equals("s3"))
 		return new AmazonS3Transfer(d, t, rl, ll, rp, lp);
 	    if (rp.getname().toLowerCase().equals("bittorrent"))
 		return new BittorrentTransfer(d, t, rl, ll, rp, lp);
 	} else if (rp.getname().toLowerCase().equals("local")) {
 	    if (lp.getname().toLowerCase().equals("ftp"))
 		return new FtpTransfer(d, t, rl, ll, rp, lp);
 	    if (lp.getname().toLowerCase().equals("http"))
 		return new HttpTransfer(d, t, rl, ll, rp, lp);
 	    if (rp.getname().toLowerCase().equals("dummy"))
 		return new DummyTransfer(d, t, rl, ll, rp, lp);
 	}
 	throw new OOBException(
 		"Unable to find the correct OOB Transfers for transfer : "
 			+ t.getuid() + "[rl:" + lp.getname() + "|" + "rp:"
 			+ lp.getname() + "]");
     }
 
     public OOBTransfer getOOBTransfer(Transfer trans) throws OOBException {
 	OOBTransfer oob = null;
 	int idx = oobTransfers.search(trans.getuid());
 	if (idx != -1)
 	    oob = (OOBTransfer) oobTransfers.elementAt(idx);
 	else {
 	    oob = createOOBTransfer(trans, daocheck);
 	    oobTransfers.addElement(oob);
 	    log.debug("TransferManager new transfer " + trans.getuid() + " : "
 		    + oob.toString());
 	}
 	/*
 	 * OOBTransfer oob = (OOBTransfer) oobTransfers.get(trans.getuid()); if
 	 * (oob==null) { oob = OOBTransferFactory.createOOBTransfer(trans);
 	 * oobTransfers.put(trans.getuid(), oob); log.debug("RESUMING TRANSFER "
 	 * + trans.getuid() + " : " + oob.toString()); }
 	 */
 	return oob;
     }
 
     /**
      * <code>checkTransfer</code> scans the database and take decision according
      * to the status of the trabsfer
      * <ol>
      * <li>prepare transfert (check for data availability)
      * <li>check for finished transfer
      * <li>cleanup transfer
      * </ol>
      */
     private void checkTransfer() {
 	long start = System.currentTimeMillis();
 
 	try {
 
 	    daocheck.beginTransaction();
 
 	    /* begin nouveau */
 	    Collection results = (Collection) daocheck
 		    .getTransfersDifferentStatus(TransferStatus.TODELETE);
 	    if (results == null) {
 		log.debug("nothing to check");
 		return;
 	    }
 	    Iterator iter = results.iterator();
 
 	    OOBTransfer oob;
 	    while (iter.hasNext()) {
 		Transfer trans = (Transfer) iter.next();
 		log.debug("Checking Transfer in " + this + " : "
 			+ trans.getuid() + ":"
 			+ TransferType.toString(trans.gettype()));
 		switch (trans.getstatus()) {
 		// Register the transfer remotely if it
 		// succeed, set the local transfer to READY
 		// if not set the transfert to INVALID
 		// set the remote transfer to READY, if it
 		// fails set the local transfer to INVALID
 
 		case TransferStatus.PENDING:
 		    log.debug("PENDING");
 		    try {
 			oob = getOOBTransfer(trans);
 			if (TransferType.isLocal(trans.gettype())) {
 
 			    log.debug("transfer " + trans + " | data "
 				    + oob.getData() + " | remote protocol "
 				    + oob.getRemoteProtocol()
 				    + " | remote locator "
 				    + oob.getRemoteLocator());
 			    log.debug("value of dt is " + dt + " in " + this);
 			    log.debug("is about to insert one transfer in "
 				    + this);
 			    dt.registerTransfer(trans, oob.getData(),
 				    oob.getRemoteProtocol(),
 				    oob.getRemoteLocator());
 			    log.debug("Transfer registred");
 			}
 
 		    } catch (Exception re) {
 			log.info("An error has occurred " + re.getMessage());
 			log.debug("cannot register transfer " + re);
 			re.printStackTrace();
 			trans.setstatus(TransferStatus.INVALID);
 			break;
 		    }
 		    trans.setstatus(TransferStatus.READY);
 		    break;
 
 		case TransferStatus.READY:
 		    log.debug("READY");
 		    if (ongoingTransfers() < maxDownloads) {
 			try {
 			    log.debug("start tranfer : " + trans.getuid());
 			    // correct transfer creation
 
 			    // going to start the transfer
 			    oob = getOOBTransfer(trans);
 			    if (TransferType.isLocal(trans.gettype())) {
 				log.debug("oob connect");
 				oob.connect();
 			    }
 			    if (trans.gettype() == TransferType.UNICAST_SEND_SENDER_SIDE) {
 				log.debug("oob sendSenderSide");
 				oob.sendSenderSide();
 			    }
 			    if (trans.gettype() == TransferType.UNICAST_SEND_RECEIVER_SIDE) {
 				log.debug("oob sendReceiverSide");
 				oob.sendReceiverSide();
 			    }
 			    if (trans.gettype() == TransferType.UNICAST_RECEIVE_RECEIVER_SIDE) {
 				log.debug("oob receiveReceiverSide");
 				oob.receiveReceiverSide();
 			    }
 			    if (trans.gettype() == TransferType.UNICAST_RECEIVE_SENDER_SIDE) {
 				log.debug("oob receiveSenderSide");
 				oob.receiveSenderSide();
 			    }
 
 			} catch (OOBException oobe) {
 			    log.info("An error has occurred "
 				    + oobe.getMessage());
 			    log.info("The transfer could not succesfully finish "
 				    + oobe.getMessage()
 				    + " it will be erased from cache");
 			    oobe.printStackTrace();
 			    trans.setstatus(TransferStatus.INVALID);
 			    break;
 			}
 			trans.setstatus(TransferStatus.TRANSFERING);
 		    }
 		    break;
 
 		case TransferStatus.INVALID:
 		    log.debug("INVALID");
 
 		    try {
 			if (TransferType.isLocal(trans.gettype()))
 			    dt.setTransferStatus(trans.getuid(),
 				    TransferStatus.INVALID);
 		    } catch (RemoteException re) {
 			log.info("An error has occurred " + re.getMessage());
 			re.printStackTrace();
 			trans.setstatus(TransferStatus.INVALID);
 			break;
 		    }
 		    trans.setstatus(TransferStatus.TODELETE);
 		    break;
 
 		case TransferStatus.TRANSFERING:
 		    // check the status
 		    log.debug("TRANSFERING");
 		    boolean complete = false;
 		    // check if transfer is complete
 		    try {
 			oob = getOOBTransfer(trans);
 			log.debug("transfer type "
 				+ TransferType.toString(trans.gettype()));
 			if (trans.gettype() == TransferType.UNICAST_SEND_SENDER_SIDE)
 			    complete = dt.poolTransfer(trans.getuid());
 			if (trans.gettype() == TransferType.UNICAST_SEND_RECEIVER_SIDE)
 			    complete = oob.poolTransfer();
 			if (trans.gettype() == TransferType.UNICAST_RECEIVE_RECEIVER_SIDE)
 			    complete = oob.poolTransfer();
 			if (trans.gettype() == TransferType.UNICAST_RECEIVE_SENDER_SIDE)
 			    complete = oob.poolTransfer();
 			log.debug("Complete is !!!" + complete);
 			// Transfer is finished
 			if (complete) {
 			    trans.setstatus(TransferStatus.COMPLETE);
 			}
 			// FIXME check for errors
 			if (oob.error()) {
			    throw new OOBException("There was an exception on the Transfer Manager, your transfer of data "+trans.getdatauid() +" is marked as INVALID");
 			}
 		    } catch (RemoteException re) {
 			log.info("An error has occurred " + re.getMessage());
 			trans.setstatus(TransferStatus.INVALID);
 			re.printStackTrace();
 			break;
 		    } catch (OOBException oobe) {
 			// go in the state INVALID (should be ABORT ?)
			log.info("Error on TRANSFERRING step : " + oobe.getMessage());
 			trans.setstatus(TransferStatus.INVALID);
 			oobe.printStackTrace();
 			break;
 		    }
 
 		    break;
 
 		case TransferStatus.COMPLETE:
 		    // check the status
 		    // we stay in the complete status up to when we have been
 		    // checked as complete.
 		    log.debug("COMPLETE");
 		    // The transfer ends when the sender is aware that the
 		    try {
 			oob = getOOBTransfer(trans);
 			oob.disconnect();
 			trans.setstatus(TransferStatus.TODELETE);
 
 		    } catch (Exception re) {
 			log.info("An error has occurred " + re.getMessage());
 			re.printStackTrace();
 			trans.setstatus(TransferStatus.INVALID);
 			break;
 		    }
 		    break;
 
 		case TransferStatus.STALLED:
 		    log.debug("STALLED");
 		    break;
 		case TransferStatus.TODELETE:
 		    // check the status
 		    log.debug("TODELETE");
 		    try {
 			// TODO DELETE TRANSFER FROM THE DATABASE
 			removeOOBTransfer(trans);
 		    } catch (OOBException oobe) {
 			log.info("An error has occurred " + oobe.getMessage());
 			trans.setstatus(TransferStatus.INVALID);
 			oobe.printStackTrace();
 			break;
 		    }
 		    break;
 
 		default:
 		    log.debug("ERROR");
 
 		}
 		log.debug("Trans status of id " + trans.getuid()
 			+ "before persisting is " + trans.getstatus());
 		daocheck.makePersistent(trans, false);
 	    }
 
 	    daocheck.commitTransaction();
 
 	} finally {
 
 	    if (daocheck.transactionIsActive())
 		daocheck.transactionRollback();
 
 	}
 	long end = System.currentTimeMillis();
 	//perf.addSample(end - start);
     }
 
     public long ongoingTransfers() {
 	long result = -1;
 	result = new Long((Long) daocheck.getTransfersByStatus(
 		TransferStatus.TRANSFERING, true, "uid")).longValue();
 	return result;
     }
 
     // FIXME c pas top; mettre une limite au premier resultat retourne
     public boolean downloadComplete() {
 	DaoTransfer daot = (DaoTransfer) DaoFactory
 		.getInstance("xtremweb.dao.transfer.DaoTransfer");
 	try {
 	    daot.beginTransaction();
 	    Collection results = daot
 		    .getAll(xtremweb.core.obj.dt.Transfer.class);
 	    if (results == null) {
 		return true;
 	    } else {
 		Iterator iter = results.iterator();
 		while (iter.hasNext()) {
 		    Transfer trans = (Transfer) iter.next();
 		    log.debug("scanning transfer " + trans.getuid() + " "
 			    + trans.getdatauid() + trans.getstatus());
 		    if (trans.getstatus() != TransferStatus.TODELETE)
 			return false;
 		}
 	    }
 	    daot.commitTransaction();
 	} finally {
 	    if (daot.transactionIsActive())
 		daot.transactionRollback();
 	}
 
 	return true;
     }
 
     public void setMaximumConcurrentDownloads(long cd) {
 	maxDownloads = cd;
     }
 
     public long getMaximumConcurrentDonwloads() {
 	return maxDownloads;
     }
 
     /**
      * <code>waitForAllData</code> waits for all transfers to complete.
      * 
      */
     public void waitForAllData() {
 	while (!downloadComplete()) {
 	    try {
 		Thread.sleep(timeout);
 	    } catch (Exception e) {
 	    }
 	    ;
 	    log.debug("Still in the barrier");
 	}
     }
 
     /**
      * <code>isTransferComplete</code> check if a transfer of a given data is
      * complete.
      * 
      * @param data
      *            a <code>Data</code> value
      * @return a <code>boolean</code> value
      */
     public boolean isTransferComplete(Data data) {// Jos Francisco : I have to
 						  // build another dao here
 						  // because it is not taking
 						  // into account
 	// the commits of daocheck this is not a sensful solution but i need to
 	// keep going.
 	boolean isComplete = true;
 	DaoTransfer daot = null;
 	try {
 	    daot = (DaoTransfer) DaoFactory
 		    .getInstance("xtremweb.dao.transfer.DaoTransfer");
 	    daot.beginTransaction();
 	    Collection results = (Collection) daot.getTransfersByDataUid(data
 		    .getuid());
 	    if (results == null) {
 		log.debug("pas de resultat");
 		return true;
 	    } else {
 		Iterator iter = results.iterator();
 		while (iter.hasNext()) {
 		    Transfer trans = (Transfer) iter.next();
 		    log.debug("scanning transfer locator "
 			    + trans.getlocatorlocal() + " tuid: "
 			    + trans.getuid() + " " + trans.getdatauid()
 			    + " status : " + trans.getstatus());
 		    if (trans.getstatus() != TransferStatus.TODELETE)
 			return false;
 		}
 	    }
 	    daot.commitTransaction();
 	} finally {
 	    if (daot.transactionIsActive())
 		daot.transactionRollback();
 	    daot.close();
 	}
 
 	return isComplete;
     }
 
     /**
      * <code>waitFor</code> waits for a specific data to be transfered.
      * 
      * @param data
      *            a <code>Data</code> value
      */
     public void waitFor(Data data) throws TransferManagerException {
 	while (!isTransferComplete(data)) {
 	    try {
 		Thread.sleep(timeout);
 	    } catch (Exception e) {
 		throw new TransferManagerException();
 	    }
 	    ;
 	    log.debug("waitFor data " + data.getuid());
 	}
     }
 
     public void retriveData() {
 	try {
 
 	    dao.beginTransaction();
 	    Collection results = (Collection) dao
 		    .getAll(xtremweb.core.obj.dc.Data.class);
 	    if (results == null) {
 		log.debug("pas de resultat");
 		return;
 	    } else {
 		Iterator iter = results.iterator();
 		while (iter.hasNext()) {
 		    Data d = (Data) iter.next();
 		    System.out.println("scanning Data: uid= " + d.getuid());
 		}
 	    }
 	    dao.commitTransaction();
 	} finally {
 	    if (dao.transactionIsActive())
 		dao.transactionRollback();
 	}
     }
 
     public void waitFor(Vector<String> uidList) throws TransferManagerException {
 	log.debug("begin waitFor!");
 	try {
 
 	    int N = uidList.size();
 	    try {
 
 		dao.beginTransaction();
 		for (int i = 0; i < N; i++) {
 		    String uid = uidList.elementAt(i);
 		    Data dataStored = (Data) dao.getByUid(
 			    xtremweb.core.obj.dc.Data.class, uid);
 
 		    waitFor(dataStored);
 		    log.debug("Oh ha Transfer waitfor data uid="
 			    + dataStored.getuid());
 		}
 
 		dao.commitTransaction();
 	    } finally {
 		if (dao.transactionIsActive())
 		    dao.transactionRollback();
 	    }
 
 	} catch (Exception e) {
 	    System.out.println(e);
 	    throw new TransferManagerException();
 	}
 
     }
 
     public void waitFor(BitDewURI uri) throws TransferManagerException {
 	log.debug("begin waitFor!");
 	try {
 	    dao.beginTransaction();
 	    String uid = uri.getUid();
 	    Data dataStored = (Data) dao.getByUid(
 		    xtremweb.core.obj.dc.Data.class, uid);
 	    waitFor(dataStored);
 	    log.debug("Oh ha Transfer waitfor data uid=" + dataStored.getuid());
 	    dao.commitTransaction();
 	} catch (Exception e) {
 	    System.out.println(e);
 	    throw new TransferManagerException();
 	} finally {
 	    if (dao.transactionIsActive())
 		dao.transactionRollback();
 	}
     }
 
     // This a comparator for the test
     class OOBTransferOrder implements Comparator {
 
 	public int compare(Object p1, Object p2) {
 	    String s1;
 	    String s2;
 	    if (p1 instanceof String)
 		s1 = (String) p1;
 	    else
 		s1 = ((OOBTransfer) p1).getTransfer().getuid();
 	    if (p2 instanceof String)
 		s2 = (String) p2;
 	    else
 		s2 = ((OOBTransfer) p2).getTransfer().getuid();
 	    return s1.compareTo(s2);
 	}
     }
 
 }
