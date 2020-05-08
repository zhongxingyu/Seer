 package utils;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.PrintStream;
 import java.util.Map;
 
 import org.apache.commons.logging.Log;
 
 import com.dropbox.client2.DropboxAPI;
 import com.dropbox.client2.DropboxAPI.ChunkedUploader;
 import com.dropbox.client2.DropboxAPI.Entry;
 import com.dropbox.client2.exception.DropboxException;
 import com.dropbox.client2.exception.DropboxServerException;
 import com.dropbox.client2.exception.DropboxUnlinkedException;
 import com.dropbox.client2.session.AccessTokenPair;
 import com.dropbox.client2.session.AppKeyPair;
 import com.dropbox.client2.session.Session;
 import com.dropbox.client2.session.WebAuthSession;
 
 public class Connection {
 
 	public static final String STATE_FILE = "state.json";
 
 	// ------------------------------------------------------------------------
 	// Reset state
 
 	public static void doReset(String appKey, String appScreet)
 			throws DropboxException {
 
 		AppKeyPair appKeyPair = new AppKeyPair(appKey, appScreet);
 
 		// Save state
 		State state = new State(appKeyPair);
 		state.save(STATE_FILE);
 	}
 
 	// ------------------------------------------------------------------------
 	// Link another account.
 
 	public static void doLink() throws DropboxException {
 
 		State state;
 		// Load state.
 		try {
 			state = State.load(STATE_FILE);
 		} catch (Exception ex) {
 			doReset("atpzm7nb23oa2ac", "udqftxi7nfiq553");
 			state = State.load(STATE_FILE);
 		}
 
 		WebAuthSession was = new WebAuthSession(state.appKey,
 				Session.AccessType.APP_FOLDER);
 
 		// Make the user log in and authorize us.
 		WebAuthSession.WebAuthInfo info = was.getAuthInfo();
 		System.out.println("1. Go to: " + info.url);
 		System.out.println("2. Allow access to this app.");
 		System.out.println("3. Press ENTER.");
 
 		try {
 			while (System.in.read() != '\n') {
 			}
 		} catch (IOException ex) {
 			throw die("I/O error: " + ex.getMessage());
 		}
 
 		// This will fail if the user didn't visit the above URL and hit
 		// 'Allow'.
 		String uid = was.retrieveWebAccessToken(info.requestTokenPair);
 		AccessTokenPair accessToken = was.getAccessTokenPair();
 		System.out.println("Link successful.");
 
 		state.links.put(uid, accessToken);
 		state.save(STATE_FILE);
 	}
 
 	// ------------------------------------------------------------------------
 	// Link another account.
 
 	public static void doList() throws DropboxException {
 
 		// Load state.
 		State state = State.load(STATE_FILE);
 
 		if (state.links.isEmpty()) {
 			System.out.println("No links.");
 		} else {
 			System.out.println("[uid: access token]");
 			for (Map.Entry<String, AccessTokenPair> link : state.links
 					.entrySet()) {
 				AccessTokenPair at = link.getValue();
 				System.out.println(link.getKey() + ": " + at.key + " "
 						+ at.secret);
 			}
 		}
 	}
 
 	// ------------------------------------------------------------------------
 	// Copy a file
 
 	public static void doCopy(String[] args) throws DropboxException {
 		if (args.length != 3) {
 			throw die("ERROR: \"copy\" takes exactly two arguments");
 		}
 
 		// Load cached state.
 		State state = State.load(STATE_FILE);
 
 		GlobalPath source, target;
 		try {
 			source = GlobalPath.parse(args[1]);
 		} catch (GlobalPath.FormatException ex) {
 			throw die("ERROR: Bad <source>: " + ex.getMessage());
 		}
 		try {
 			target = GlobalPath.parse(args[2]);
 		} catch (GlobalPath.FormatException ex) {
 			throw die("ERROR: Bad <source>: " + ex.getMessage());
 		}
 
 		AccessTokenPair sourceAccess = state.links.get(source.uid);
 		if (sourceAccess == null) {
 			throw die("ERROR: <source> refers to UID that isn't linked.");
 		}
 		AccessTokenPair targetAccess = state.links.get(target.uid);
 		if (targetAccess == null) {
 			throw die("ERROR: <target> refers to UID that isn't linked.");
 		}
 
 		// Connect to the <source> UID and create a copy-ref.
 		WebAuthSession sourceSession = new WebAuthSession(state.appKey,
 				Session.AccessType.DROPBOX, sourceAccess);
 		DropboxAPI<?> sourceClient = new DropboxAPI<WebAuthSession>(
 				sourceSession);
 		DropboxAPI.CreatedCopyRef cr = sourceClient.createCopyRef(source.path);
 
 		// Connect to the <target> UID and add the target file.
 		WebAuthSession targetSession = new WebAuthSession(state.appKey,
 				Session.AccessType.DROPBOX, targetAccess);
 		DropboxAPI<?> targetClient = new DropboxAPI<WebAuthSession>(
 				targetSession);
 		targetClient.addFromCopyRef(cr.copyRef, target.path);
 
 		System.out.println("Copied.");
 	}
 
 	// ------------------------------------------------------------------------
 	// upload a file
 
 	public static void doUpload(String sourceFile, String TargetFile) {
 
 		// Load cached state.
 		State state = State.load(STATE_FILE);
 
 		String linkKey = state.links.entrySet().iterator().next().getKey();
 
 		AccessTokenPair targetAccess = state.links.get(linkKey);
 		if (targetAccess == null) {
 			throw die("ERROR: <source> refers to UID that isn't linked.");
 		}
 
 		WebAuthSession session = new WebAuthSession(state.appKey,
 				Session.AccessType.APP_FOLDER, targetAccess);
 
 		DropboxAPI<?> client = new DropboxAPI<WebAuthSession>(session);
 
 		FileInputStream inputStream = null;
 		try {
 			File file = new File(sourceFile);
 			inputStream = new FileInputStream(file);
 			Entry newEntry = client.putFile(TargetFile, inputStream,
 					file.length(), null, null);
 			System.out.println("The uploaded file's rev is: " + newEntry.rev);
 		} catch (DropboxUnlinkedException e) {
 			// User has unlinked, ask them to link again here.
 			System.out.println("User has unlinked.");
 		} catch (DropboxException e) {
 			System.out.println("Something went wrong while uploading.");
 		} catch (FileNotFoundException e) {
 			System.out.println("File not found.");
 		} finally {
 			if (inputStream != null) {
 				try {
 					inputStream.close();
 				} catch (IOException e) {
 				}
 			}
 		}
 
 	}
 
 	// ------------------------------------------------------------------------
 	// upload bigger files
 
 	public static void doChunkUpload(String sourceFile) {
 
 		// Load cached state.
 		State state = State.load(STATE_FILE);
 
 		String linkKey = state.links.entrySet().iterator().next().getKey();
 
 		AccessTokenPair targetAccess = state.links.get(linkKey);
 		if (targetAccess == null) {
 			throw die("ERROR: <source> refers to UID that isn't linked.");
 		}
 
 		WebAuthSession session = new WebAuthSession(state.appKey,
 				Session.AccessType.APP_FOLDER, targetAccess);
 
 		DropboxAPI<?> client = new DropboxAPI<WebAuthSession>(session);
 
 		FileInputStream inputStream = null;
 		try {
 			File file = new File(sourceFile);
 			inputStream = new FileInputStream(file);
 
 			@SuppressWarnings("rawtypes")
 			ChunkedUploader uploader = client.getChunkedUploader(
 					inputStream, file.length());
 
 			while (!uploader.isComplete()) {
 				try {
 					uploader.upload();
 				} catch (IOException e) {
 					e.printStackTrace();
 				}
 			}
 
 			if (uploader.isComplete()) {
 				 String parentRev = null;
 				    try {
				        Entry metadata = client.metadata(sourceFile, 1, null, false, null);
 				        parentRev = metadata.rev;
 				    } catch (DropboxServerException e) {
 				        //if (e.error!= DropboxServerException._404_NOT_FOUND)
 				    	System.err.println(e);
 				    }
				    uploader.finish(sourceFile, parentRev);
 				System.out.println("File is Uploaded!");
 			}
 		} catch (DropboxUnlinkedException e) {
 			// User has unlinked, ask them to link again here.
 			System.out.println("User has unlinked.");
 		} catch (DropboxException e) {
 			System.out.println("Something went wrong while uploading.");
 			e.printStackTrace();
 		} catch (FileNotFoundException e) {
 			System.out.println("File not found.");
 		} finally {
 			if (inputStream != null) {
 				try {
 					inputStream.close();
 				} catch (IOException e) {
 				}
 			}
 		}
 
 	}
 
 	// ------------------------------------------------------------------------
 
 	public static void printUsage(PrintStream out) {
 		out.println("Usage:");
 		out.println("    ./run reset <app-key> <secret>  Initialize the state with the given app key.");
 		out.println("    ./run link                      Link an account to this app.");
 		out.println("    ./run list                      List accounts that have been linked.");
 		out.println("    ./run copy <source> <target>    Copy a file; paths are of the form <uid>:<path>.");
 	}
 
 	public static RuntimeException die(String message) {
 		System.err.println(message);
 		return die();
 	}
 
 	public static RuntimeException die() {
 		System.exit(1);
 		return new RuntimeException();
 	}
 
 }
