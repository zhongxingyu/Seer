 package application;
 
 import java.io.InputStream;
 import java.net.URI;
 import java.net.URISyntaxException;
 
 import model.HttpRequestImpl;
 import model.HttpResponseImpl;
 import connection.CollectionConnectionHandler;
 import connection.CollectionConnectionHandlerImpl;
 import connection.Connection;
 import connection.EndPointConnectionHandler;
 
 public class ResolverThread implements Runnable {
 
 	// Cliente
 	Connection client;
 	Connection server;
 	EndPointConnectionHandler hostHandler;
 
 	// Configuracion del proxy
 	private ProxyConfiguration configuration = DinamicProxyConfiguration
 			.getInstance();
 
 	private CollectionConnectionHandler connections = CollectionConnectionHandlerImpl
 			.getInstance();
 
 	public ResolverThread(final Connection client) {
 		this.client = client;
 	}
 
 	public void run() {
 		HttpRequestImpl request = null;
 		HttpResponseImpl response = null;
 
 		// request = this.getRequest();
 		// response = this.getResponse(request);
 		// this.sendResponse(response);
 
 		do {
 			// Obtenemos Request y Response.
 			try {
 				request = this.getRequest();
 				response = this.getResponse(request);
 			} catch (final Exception e) {
 				System.out.println("Fallo el R & Response");
 				e.printStackTrace();
 			}
 
 			// Retornamos la respuesta.
 			try {
				final boolean respKeepAlive = this.keepAlive(response);
 				this.setHeaders(response, request);
 				this.sendResponse(response);
 				this.server = null;
				if (this.hostHandler != null) {
					if (respKeepAlive) {
						this.hostHandler.free(this.server);
					} else {
						this.hostHandler.drop(this.server);
					}
				}
 			} catch (final Exception e) {
 				System.out.println("Fallo el R & Response");
 				e.printStackTrace();
 			}
 
 		} while (this.keepAlive(request) && !this.client.isClosed());
 		this.close();
 	}
 
 	private void close() {
 		if (this.server != null) {
 			this.hostHandler.drop(this.server);
 		}
 		this.client.close();
 
 	}
 
 	private boolean keepAlive(final HttpRequestImpl request) {
 		if (!this.configuration.isClientPersistent() || request == null) {
 			return false;
 		}
 
 		boolean hasToClose;
 
 		if ("HTTP/1.1".equals(request.getProtocol())) {
 			hasToClose = "close".equals(request.getHeader("Connection"));
 			hasToClose |= "close".equals(request.getHeader("Proxy-Connection"));
 		} else {
 			hasToClose = true;
 		}
 
 		return !hasToClose;
 	}
 
 	private void setHeaders(final HttpResponseImpl response,
 			final HttpRequestImpl request) {
 		if (this.keepAlive(request)) {
 			response.replaceHeader("Connection", "keep-alive");
 		} else {
 			response.replaceHeader("Connection", "close");
 		}
 
 	}
 
 	private boolean keepAlive(final HttpResponseImpl response) {
 		boolean keepAlive;

 		keepAlive = "keep-alive".equals(response.getHeader("Connection"));

 		keepAlive &= response.getProtocol().equals("HTTP/1.1");
 
 		return keepAlive;
 	}
 
 	private void sendResponse(final HttpResponseImpl response) {
 		this.client.send(response);
 
 	}
 
 	private HttpResponseImpl getResponse(final HttpRequestImpl request) {
 
 		HttpResponseImpl response = null;
 
 		this.server = this.getConnection(request.getHost());
 		this.server.send(request);
 		response = this.server.receive();
 
 		// System.out.println(response);
 		return response;
 	}
 
 	// TODO: Implementar el getConnection desde un host porque no hay otra
 	// forma, agregar un mapa con conexiones a host y que el mismo tenga varias
 	// conexiones disponbiles para ese host.
 
 	public Connection getConnection(final String host) {
 		final EndPointConnectionHandler hostConnections = this.connections
 				.getEndPointConnectionHandler(host);
 		return hostConnections.getConnection();
 	}
 
 	private void modifyRequest(final HttpRequestImpl req) {
 		String url = null;
 		if (this.configuration.hasProxy()) {
 			if (req.getRequestURI().isAbsolute()) {
 				url = req.getRequestURI().toString();
 			} else {
 				final String query = req.getRequestURI().getRawQuery() != null ? "?"
 						+ req.getRequestURI().getRawQuery()
 						: "";
 				url = "http://" + req.getHeader("Host")
 						+ req.getRequestURI().getRawPath() + query;
 			}
 		} else {
 			final String query = req.getRequestURI().getRawQuery() != null ? "?"
 					+ req.getRequestURI().getRawQuery()
 					: "";
 			url = req.getRequestURI().getRawPath() + query;
 		}
 
 		try {
 			req.setRequestURI(new URI(url));
 		} catch (final URISyntaxException e) {
 		}
 	}
 
 	private HttpRequestImpl getRequest() {
 
 		// HttpRequestImpl request;
 		// final InputStream input = this.client.getInputStream();
 		// request = new HttpRequestImpl(input);
 		// return request;
 
 		HttpRequestImpl request = null;
 		InputStream stream = null;
 		try {
 			stream = this.client.getInputStream();
			System.out.println("STREAM: " + stream);
 		} catch (final Exception e) {
 			System.out.println("El cliente no responde o cerro la conexion");
 		}
 		try {
 			request = new HttpRequestImpl(stream);
 			this.modifyRequest(request);
 		} catch (final Exception e) {
 			System.out.println("El cliente no responde o cerro la conexion");
 		}
 		return request;
 	}
 
 }
