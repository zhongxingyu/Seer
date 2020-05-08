 package de.dhbw.mannheim.cloudraid.net.connector;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.net.URL;
 import java.util.HashMap;
 
 import javax.activation.MimetypesFileTypeMap;
 import javax.net.ssl.HttpsURLConnection;
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.NodeList;
 import org.xml.sax.SAXException;
 
 import de.dhbw.mannheim.cloudraid.util.Config;
 
 public class SugarSyncConnector implements IStorageConnector {
 
 	private String token = "";
 	private String username, password, accessKeyId, privateAccessKey;
 	private DocumentBuilder docBuilder;
 	private String baseURL = null;
 	private final static String AUTH_URL = "https://api.sugarsync.com/authorization";
 	private final static String USER_INFO_URL = "https://api.sugarsync.com/user";
 
 	/**
 	 * Creates an HTTPS connection with some predefined values
 	 * 
 	 * @return A preconfigured connection.
 	 */
 	private static HttpsURLConnection getConnection(String address,
 			String authToken, String method) throws IOException {
 		HttpsURLConnection con = (HttpsURLConnection) new URL(address)
 				.openConnection();
 		con.setRequestMethod(method);
 		con.setRequestProperty("User-Agent", "CloudRAID");
 		con.setRequestProperty("Accept", "*/*");
 		con.setRequestProperty("Authorization", authToken);
 
 		return con;
 	}
 
 	public static void main(String[] args) {
 		try {
 			if (args.length != 5) {
 				System.err
 						.println("usage: username password accessKey privateAccessKey resource");
 				System.out
 						.println("example for 'resource': 'Sample Documents/SugarSync QuickStart Guide.pdf'");
 				return;
 			}
 			HashMap<String, String> params = new HashMap<String, String>(4);
 			params.put("username", args[0]);
 			params.put("password", args[1]);
 			params.put("accessKey", args[2]);
 			params.put("privateAccessKey", args[3]);
 			IStorageConnector ssc = StorageConnectorFactory
 					.create("de.dhbw.mannheim.cloudraid.net.connector.SugarSyncConnector",
 							params);
 			ssc.connect();
 
 			ssc.put(args[4]);
 			System.out.println("Uploading done.");
 			InputStream is = ssc.get(args[4]);
 			File f = new File("/tmp/" + args[4]);
 			f.getParentFile().mkdirs();
 			FileOutputStream fos = new FileOutputStream(f);
 
 			byte[] inputBytes = new byte[02000];
 			int readLength;
 			while ((readLength = is.read(inputBytes)) >= 0) {
 				fos.write(inputBytes, 0, readLength);
 			}
 			System.out.println("Getting done.");
 			System.out.println(ssc.delete(args[4]));
 			System.out.println("Deleting done.");
 		} catch (Exception e) {
 			e.printStackTrace();
 			return;
 		}
 	}
 
 	/**
 	 * Connects to the SugarSync cloud service.
 	 * 
 	 * @param service
 	 * @return true, if the service could be connected, false, if not.
 	 */
 	@Override
 	public boolean connect() {
 		try {
 			// Get the Access Token
 			HttpsURLConnection con = SugarSyncConnector.getConnection(AUTH_URL,
 					"", "POST");
 			con.setDoOutput(true);
 			con.setRequestProperty("Content-Type",
 					"application/xml; charset=UTF-8");
 
 			// Create authentication request
 			StringBuilder authReqBuilder = new StringBuilder(
 					"<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n<authRequest><username>");
 			authReqBuilder.append(username).append("</username><password>");
 			authReqBuilder.append(password).append(
 					"</password>\n\t<accessKeyId>");
 			authReqBuilder.append(accessKeyId).append(
 					"</accessKeyId><privateAccessKey>");
 			authReqBuilder.append(privateAccessKey).append(
 					"</privateAccessKey></authRequest>");
 			String authReq = authReqBuilder.toString();
 
 			con.connect();
 			con.getOutputStream().write(authReq.getBytes());
 			this.token = con.getHeaderField("Location");
 			con.disconnect();
 
 			return true;
 		} catch (Exception e) {
 			return false;
 		}
 	}
 
 	/**
 	 * This function initializes the SugarSyncConnector.
 	 * 
 	 * @param param
 	 *            There are two creation modes. In case the tokens already
 	 *            exist, the HashMap has to contain the following keys:
 	 *            <ul>
 	 *            <li><code>username</li>
 	 *            <li><code>customer_secret</code></li>
 	 *            <li><code>accessKeyId</code></li>
 	 *            <li><code>privateAccessKey</code></li>
 	 *            </ul>
 	 */
 	@Override
 	public IStorageConnector create(HashMap<String, String> parameter) {
 		if (parameter.containsKey("username")
 				&& parameter.containsKey("password")
 				&& parameter.containsKey("accessKeyId")
 				&& parameter.containsKey("privateAccessKey")) {
 			this.username = parameter.get("username");
 			this.password = parameter.get("password");
			this.accessKeyId = parameter.get("username");
			this.privateAccessKey = parameter.get("password");
 		} else {
 			System.err
 					.println("username, password, accessKeyId and privateAccessKey have to be set during creation!");
 		}
 		docBuilder = null;
 		try {
 			docBuilder = DocumentBuilderFactory.newInstance()
 					.newDocumentBuilder();
 			docBuilder.setErrorHandler(null);
 		} catch (ParserConfigurationException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		return this;
 	}
 
 	/**
 	 * Creates a file on SugarSync.
 	 * 
 	 * @param name
 	 *            The file name.
 	 * @param f
 	 *            The file to be uploaded.
 	 * @param parent
 	 *            The URL to the parent.
 	 * @throws IOException
 	 * @throws SAXException
 	 * @throws ParserConfigurationException
 	 */
 	private void createFile(String name, File f, String parent)
 			throws IOException, SAXException, ParserConfigurationException {
 		String mime = new MimetypesFileTypeMap().getContentType(f);
 		String request = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><file><displayName>"
 				+ name
 				+ "</displayName><mediaType>"
 				+ mime
 				+ "</mediaType></file>";
 		HttpsURLConnection con = SugarSyncConnector.getConnection(parent,
 				this.token, "POST");
 		con.setRequestProperty("Content-Type", "text/xml");
 		con.setDoOutput(true);
 
 		con.connect();
 		con.getOutputStream().write(request.getBytes());
 		InputStream is = con.getInputStream();
 		System.out.println(con.getResponseCode() + ": "
 				+ con.getResponseMessage());
 		con.disconnect();
 
 		String file = this.findFileInFolder(name, parent
 				+ "/contents?type=file")
 				+ "/data";
 
 		con = SugarSyncConnector.getConnection(file, this.token, "PUT");
 		con.setDoOutput(true);
 		con.setRequestProperty("Content-Type", mime);
 
 		con.connect();
 		OutputStream os = con.getOutputStream();
 		is = new FileInputStream(f);
 		int i;
 		while ((i = is.read()) >= 0) {
 			os.write(i);
 		}
 		System.out.println(con.getResponseCode() + ": "
 				+ con.getResponseMessage());
 		con.disconnect();
 	}
 
 	/**
 	 * Creates a folder on SugarSync.
 	 * 
 	 * @param name
 	 *            The name of the folder.
 	 * @param parent
 	 *            The URL to the parent folder.
 	 * @throws IOException
 	 */
 	private void createFolder(String name, String parent) throws IOException {
 		String request = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
 				+ "<folder>" + "\t<displayName>" + name + "</displayName>"
 				+ "</folder>";
 		HttpsURLConnection con = SugarSyncConnector.getConnection(parent,
 				this.token, "POST");
 		con.setRequestProperty("Content-Type", "text/xml");
 		con.setDoOutput(true);
 		con.setDoInput(true);
 
 		con.connect();
 		con.getOutputStream().write(request.getBytes());
 		InputStream is = con.getInputStream();
 		int i;
 		while ((i = is.read()) >= 0) {
 			System.out.print((char) i);
 		}
 		con.disconnect();
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public boolean delete(String resource) {
 		String parent;
 		if (resource.contains("/"))
 			parent = this
 					.getResourceURL(resource.substring(0,
 							resource.lastIndexOf("/") + 1), false);
 		else
 			parent = this.getResourceURL("", false);
 
 		if (parent == null) {
 			return true;
 		}
 
 		String resourceURL;
 		try {
 			resourceURL = this.findFileInFolder(
 					resource.substring(resource.lastIndexOf("/") + 1), parent
 							+ "/contents?type=file");
 		} catch (Exception e) {
 			e.printStackTrace();
 			return true;
 		}
 
 		HttpsURLConnection con = null;
 		try {
 			con = SugarSyncConnector.getConnection(resourceURL, this.token,
 					"DELETE");
 			con.connect();
 			con.disconnect();
 		} catch (Exception e) {
 			e.printStackTrace();
 			int returnCode = -1;
 			try {
 				returnCode = con.getResponseCode();
 			} catch (IOException e1) {
 				e1.printStackTrace();
 			}
 			if (!(returnCode == 404 || returnCode == 204)) {
 				return false;
 			}
 		}
 
 		try {
 			while (this.isFolderEmpty(parent)) {
 				String oldParent = parent;
 				parent = this.getParentFolder(parent);
 
 				con = SugarSyncConnector.getConnection(oldParent, this.token,
 						"DELETE");
 
 				con.connect();
 				System.out.println(con.getResponseCode() + ": "
 						+ con.getResponseMessage());
 				con.disconnect();
 			}
 			return true;
 		} catch (Exception e) {
 			e.printStackTrace();
 			return true;
 		}
 	}
 
 	/**
 	 * Checks, if a file is in the specific folder on the SugarSync servers.
 	 * 
 	 * @param name
 	 *            The file name.
 	 * @param parent
 	 *            The URL to the parent folder.
 	 * @return The URL to the file, or null, if it could not be found.
 	 * @throws SAXException
 	 * @throws IOException
 	 * @throws ParserConfigurationException
 	 */
 	private String findFileInFolder(String name, String parent)
 			throws SAXException, IOException, ParserConfigurationException {
 		Document doc;
 		HttpsURLConnection con = SugarSyncConnector.getConnection(parent,
 				this.token, "GET");
 		con.setDoInput(true);
 
 		// Build the XML tree.
 		con.connect();
 		doc = docBuilder.parse(con.getInputStream());
 		con.disconnect();
 		NodeList nl = doc.getDocumentElement().getElementsByTagName("file");
 		for (int i = 0; i < nl.getLength(); i++) {
 			String displayName = ((Element) nl.item(i))
 					.getElementsByTagName("displayName").item(0)
 					.getTextContent();
 			if (displayName.equalsIgnoreCase(name)) {
 				return ((Element) nl.item(i)).getElementsByTagName("ref")
 						.item(0).getTextContent();
 			}
 		}
 		return null;
 	}
 
 	/**
 	 * Checks, if a folder is in the specific folder on the SugarSync servers.
 	 * 
 	 * @param name
 	 *            The folder name.
 	 * @param parent
 	 *            The URL to the parent folder.
 	 * @return The URL to the file, or null, if it could not be found.
 	 * @throws SAXException
 	 * @throws IOException
 	 * @throws ParserConfigurationException
 	 */
 	private String findFolderInFolder(String name, String parent)
 			throws ParserConfigurationException, SAXException, IOException {
 		Document doc;
 		HttpsURLConnection con = SugarSyncConnector.getConnection(parent,
 				this.token, "GET");
 		con.setDoInput(true);
 
 		// Build the XML tree.
 		con.connect();
 		doc = docBuilder.parse(con.getInputStream());
 		con.disconnect();
 		NodeList nl = doc.getDocumentElement().getElementsByTagName(
 				"collection");
 		for (int i = 0; i < nl.getLength(); i++) {
 			String displayName = ((Element) nl.item(i))
 					.getElementsByTagName("displayName").item(0)
 					.getTextContent();
 			if (displayName.equalsIgnoreCase(name)) {
 				return ((Element) nl.item(i)).getElementsByTagName("ref")
 						.item(0).getTextContent();
 			}
 		}
 		return null;
 	}
 
 	/**
 	 * Gets a resource (file)
 	 * 
 	 * @param the
 	 *            path on the SugarSync system.
 	 * @return Either an InputStream, if resource could be found, or
 	 *         <code>null</code>, if the resource could not be found.
 	 */
 	@Override
 	public InputStream get(String resource) {
 		try {
 			String parent;
 			if (resource.contains("/"))
 				parent = this.getResourceURL(
 						resource.substring(0, resource.lastIndexOf("/") + 1),
 						false);
 			else
 				parent = this.getResourceURL("", false);
 			String resourceURL = this.findFileInFolder(
 					resource.substring(resource.lastIndexOf("/") + 1), parent
 							+ "/contents?type=file");
 
 			HttpsURLConnection con;
 			con = SugarSyncConnector.getConnection(resourceURL + "/data",
 					this.token, "GET");
 			con.setDoInput(true);
 
 			return con.getInputStream();
 		} catch (Exception e) {
 			e.printStackTrace();
 			return null;
 		}
 	}
 
 	/**
 	 * Loads and caches the URL to the 'Magic Briefcase' folder.
 	 * 
 	 * @return The URL to the 'Magic Briefcase' folder on SugarSync.
 	 * @throws IOException
 	 * @throws SAXException
 	 * @throws ParserConfigurationException
 	 */
 	private String getBaseUrl() throws IOException, SAXException,
 			ParserConfigurationException {
 		if (baseURL == null) {
 			HttpsURLConnection con = SugarSyncConnector.getConnection(
 					USER_INFO_URL, this.token, "GET");
 			con.setDoInput(true);
 
 			// Build the XML tree.
 			con.connect();
 			Document doc = docBuilder.parse(con.getInputStream());
 			con.disconnect();
 
 			Element node = (Element) doc.getDocumentElement()
 					.getElementsByTagName("syncfolders").item(0);
 			String folder = node.getTextContent().trim();
 
 			this.baseURL = this.findFolderInFolder("Magic Briefcase", folder);
 		}
 		return this.baseURL;
 	}
 
 	/**
 	 * Returns the parent folder of a folder
 	 * 
 	 * @param folder
 	 *            The URL to the folder.
 	 * @return The URL of the parent folder.
 	 * @throws IOException
 	 * @throws SAXException
 	 */
 	private String getParentFolder(String folder) throws IOException,
 			SAXException {
 		HttpsURLConnection con = SugarSyncConnector.getConnection(folder,
 				this.token, "GET");
 		con.setDoInput(true);
 
 		con.connect();
 		Document doc = this.docBuilder.parse(con.getInputStream());
 		con.disconnect();
 
 		return doc.getDocumentElement().getElementsByTagName("parent").item(0)
 				.getTextContent();
 	}
 
 	/**
 	 * Runs recursively through the folders in 'Magic Briefcase' to find the
 	 * specified folder.
 	 * 
 	 * @param resource
 	 *            The folder to be found.
 	 * @param createResource
 	 *            Create missing folders.
 	 * @return The URL to the folder.
 	 */
 	private String getResourceURL(String resource, boolean createResource) {
 		try {
 			String folder = this.getBaseUrl();
 			System.out.println(folder);
 			while (resource.contains("/")) {
 				String parent = folder;
 				this.isFolderEmpty(folder);
 				folder += "/contents?type=folder";
 				String nextName = resource.substring(0, resource.indexOf("/"));
 				System.out.println(resource);
 
 				folder = this.findFolderInFolder(nextName, folder);
 
 				resource = resource.substring(resource.indexOf("/") + 1);
 				if (createResource && folder == null) {
 					this.createFolder(nextName, parent);
 					folder = this.findFolderInFolder(nextName, parent
 							+ "/contents?type=folder");
 				}
 			}
 			return folder;
 		} catch (Exception e) {
 			e.printStackTrace();
 			return null;
 		}
 	}
 
 	@Override
 	public String head(String resource) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	/**
 	 * Returns if a folder is empty and can be deleted.
 	 * 
 	 * @param folder
 	 *            The URL of the folder.
 	 * @return true, if the folder is empty.
 	 * @throws IOException
 	 * @throws SAXException
 	 */
 	private boolean isFolderEmpty(String folder) throws IOException,
 			SAXException {
 		HttpsURLConnection con = SugarSyncConnector.getConnection(folder
 				+ "/contents", this.token, "GET");
 		con.setDoInput(true);
 
 		con.connect();
 		Document doc = this.docBuilder.parse(con.getInputStream());
 		con.disconnect();
 
 		if (!doc.getDocumentElement().hasAttribute("end")
 				|| !doc.getDocumentElement().getAttribute("end").equals("0"))
 			return false;
 
 		con = SugarSyncConnector.getConnection(folder, this.token, "GET");
 		con.setDoInput(true);
 
 		con.connect();
 		doc = this.docBuilder.parse(con.getInputStream());
 		con.disconnect();
 
 		return !doc.getDocumentElement().getElementsByTagName("displayName")
 				.item(0).getTextContent().equals("Magic Briefcase");
 	}
 
 	@Override
 	public String[] options(String resource) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public String post(String resource, String parent) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	/**
 	 * Puts a resource to the SugarSync folder.
 	 * 
 	 * @param resource
 	 *            The path (relative to /tmp) to the file to upload.
 	 * @return true, if it could be uploaded.
 	 */
 	@Override
 	public boolean put(String resource) {
 		File f = new File("/tmp/" + resource);
 		if (f.length() > Config.MAX_FILE_SIZE) {
 			System.err.println("File too big");
 		} else if (!f.exists()) {
 			System.err.println("File does not exist");
 		} else {
 			try {
 				String parent;
 				if (resource.contains("/"))
 					parent = this.getResourceURL(resource.substring(0,
 							resource.lastIndexOf("/") + 1), true);
 				else
 					parent = this.getResourceURL("", true);
 
 				String fileName = resource
 						.substring(resource.lastIndexOf("/") + 1);
 				String resourceURL = this.findFileInFolder(fileName, parent
 						+ "/contents?type=file");
 				try {
 					if (resourceURL != null) {
 						System.err
 								.println("The file already exists. DELETE it. "
 										+ resourceURL);
 						HttpsURLConnection con = SugarSyncConnector
 								.getConnection(resourceURL, this.token,
 										"DELETE");
 						con.connect();
 						con.disconnect();
 					}
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 
 				this.createFile(fileName, f, parent);
 			} catch (Exception e) {
 				e.printStackTrace();
 				return false;
 			}
 			return true;
 		}
 		return false;
 	}
 }
