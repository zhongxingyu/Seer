 import java.io.IOException;
 import java.io.InputStream;
 import java.math.BigInteger;
 import java.security.KeyPair;
 import java.security.KeyPairGenerator;
 import java.security.interfaces.RSAPublicKey;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.eclipse.jetty.server.Server;
 import org.eclipse.jetty.servlet.ServletContextHandler;
 import org.eclipse.jetty.servlet.ServletHolder;
 import org.i5y.json.stream.JSONTypeSafeWriters.ObjectWriter;
 import org.i5y.json.stream.impl.JSONStreamFactoryImpl;
 
 public class HelloWorld extends HttpServlet {
 
 	private static BigInteger n;
 	private static BigInteger e;
 
 	@Override
 	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
 			throws ServletException, IOException {
 		resp.getWriter().print("Hello from Java!\n");
 	}
 
 	public static class PublicServlet extends HttpServlet {
 		@Override
 		protected void doGet(HttpServletRequest req, HttpServletResponse resp)
 				throws ServletException, IOException {
 			resp.setContentType("application/json");
 			resp.addHeader("Cache-Control", "no-store, max-age=0");
			ObjectWriter objectWriter = new JSONStreamFactoryImpl()
					.createObjectWriter(resp.getWriter());
			objectWriter.startObject().defineProperty("public-key")
					.startObject().defineProperty("algorithm").literal("RS")
 					.defineProperty("n").literal(n.toString())
 					.defineProperty("e").literal(e.toString()).endObject()
 					.defineProperty("authentication")
 					.literal("/browserid/sign_in")
 					.defineProperty("provisioning")
 					.literal("/browserid/provision").endObject().close();
 		}
 	}
 
 	public static class ProvisionServlet extends HttpServlet {
 		@Override
 		protected void doGet(HttpServletRequest req, HttpServletResponse resp)
 				throws ServletException, IOException {
 			InputStream is = getClass().getResourceAsStream("provision.html");
 			int nextByte = is.read();
 			while (nextByte >= 0) {
 				resp.getOutputStream().write(nextByte);
 				nextByte = is.read();
 			}
 			resp.getOutputStream().close();
 		}
 	}
 
 	public static void main(String[] args) throws Exception {
 		Server server = new Server(Integer.valueOf(System.getenv("PORT")));
 		ServletContextHandler context = new ServletContextHandler(
 				ServletContextHandler.SESSIONS);
 		context.setContextPath("/");
 		server.setHandler(context);
 		context.addServlet(new ServletHolder(new HelloWorld()), "/*");
 		context.addServlet(new ServletHolder(new PublicServlet()),
 				"/.well-known/browserid");
 		context.addServlet(new ServletHolder(new ProvisionServlet()),
 				"/browserid/provision");
 
 		// Init public/private key...
 		KeyPairGenerator gen = KeyPairGenerator.getInstance("RSA");
 		KeyPair keyPair = gen.generateKeyPair();
 		RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
 		n = publicKey.getModulus();
 		e = publicKey.getPublicExponent();
 		System.out.println(n);
 		System.out.println(e);
 		server.start();
 		server.join();
 	}
 }
