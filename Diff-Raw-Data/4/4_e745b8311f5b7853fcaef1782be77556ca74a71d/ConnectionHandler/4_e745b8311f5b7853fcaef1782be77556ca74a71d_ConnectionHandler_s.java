 package de.uni.stuttgart.informatik.ToureNPlaner.Net.Handler;
 
 import de.uni.stuttgart.informatik.ToureNPlaner.Net.Observer;
 import de.uni.stuttgart.informatik.ToureNPlaner.Net.Session;
 
 import java.io.OutputStream;
 import java.net.HttpURLConnection;
 
 public abstract class ConnectionHandler extends RawHandler {
 	protected final Session session;
 
 	public ConnectionHandler(Observer listener, Session session) {
 		super(listener);
 		this.session = session;
 	}
 
 	protected abstract boolean isPost();
 
 	protected abstract String getSuffix();
 
 	protected void handleOutput(OutputStream outputStream) throws Exception {
 	}
 
 	@Override
 	protected HttpURLConnection getHttpUrlConnection() throws Exception {
 		if (!isPost())
 			return session.openGetConnection(getSuffix());
 
 		HttpURLConnection connection = session.openPostConnection(getSuffix());
 
 		try {
 			handleOutput(connection.getOutputStream());
		} finally {
 			connection.disconnect();
 		}
 
 		return connection;
 	}
 }
