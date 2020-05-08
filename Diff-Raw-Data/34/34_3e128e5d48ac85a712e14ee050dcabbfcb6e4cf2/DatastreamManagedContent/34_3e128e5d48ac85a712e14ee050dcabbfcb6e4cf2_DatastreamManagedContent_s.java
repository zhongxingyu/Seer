 package fedora.server.storage.types;
 
 import java.io.File;
 import java.io.InputStream;
 
 import fedora.common.Constants;
 import fedora.server.Server;
 import fedora.server.errors.InitializationException;
 import fedora.server.errors.StreamIOException;
 import fedora.server.storage.lowlevel.ILowlevelStorage;
 
 /**
  * 
  * <p>
  * <b>Title:</b> DatastreamManagedContent.java
  * </p>
  * <p>
  * <b>Description:</b> Managed Content.
  * </p>
  * 
  * @author cwilper@cs.cornell.edu
  * @version $Id: DatastreamManagedContent.java,v 1.14 2006/02/02 21:05:07
  *          cwilper Exp $
  */
 public class DatastreamManagedContent extends Datastream {
 
	private static ILowlevelStorage m_llstore;

	static {
		try {
			Server s_server = Server.getInstance(new File(Constants.FEDORA_HOME));
			m_llstore = (ILowlevelStorage)s_server.getModule("fedora.server.storage.lowlevel.ILowlevelStorage");
		} catch (InitializationException ie) {
			System.err.println(ie.getMessage());
		}
	}
 
 	public DatastreamManagedContent() {
		
 	}
 
 	public Datastream copy() {
 		DatastreamManagedContent ds = new DatastreamManagedContent();
 		copy(ds);
 		return ds;
 	}
 
 	public InputStream getContentStream() throws StreamIOException {
 		try {
			return m_llstore.retrieveDatastream(this.DSLocation);

 		} catch (Throwable th) {
 			throw new StreamIOException("[DatastreamManagedContent] returned "
 					+ " the error: \"" + th.getClass().getName()
 					+ "\". Reason: " + th.getMessage());
 		}
 	}
}
