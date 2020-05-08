 package plugins.SiteToolPlugin.sessions;
 
 import java.io.BufferedInputStream;
 import java.io.BufferedOutputStream;
 import java.io.IOException;
 import java.io.OutputStream;
 import java.util.HashMap;
 import java.util.Map.Entry;
 import java.util.zip.GZIPOutputStream;
 
 import SevenZip.Compression.LZMA.Encoder;
 
 import plugins.SiteToolPlugin.Constants;
import plugins.SiteToolPlugin.toadlets.SessionsToadlet;
 import plugins.SiteToolPlugin.toadlets.siteexport.SiteCollector;
 import plugins.SiteToolPlugin.toadlets.siteexport.SiteParser;
 
 import freenet.client.FetchException;
 import freenet.client.HighLevelSimpleClient;
 import freenet.client.async.ClientContext;
 import freenet.clients.http.ToadletContext;
 import freenet.clients.http.ToadletContextClosedException;
 import freenet.keys.FreenetURI;
 import freenet.pluginmanager.PluginNotFoundException;
 import freenet.pluginmanager.PluginReplySender;
 import freenet.support.HTMLNode;
 import freenet.support.Logger;
 import freenet.support.MultiValueTable;
 import freenet.support.SimpleFieldSet;
 import freenet.support.api.Bucket;
 import freenet.support.api.BucketFactory;
 import freenet.support.io.Closer;
 
 public class SiteDownloadSession extends AbstractSiteToolSession {
 
 	private final FreenetURI _uri;
 	private String currentProgress = "Idle";
 	private Bucket _result;
 	private final BucketFactory _bf;
 	private final String _archiveType;
 	private final HighLevelSimpleClient _hlsc;
 	private final ClientContext _clientContext;
 	private SiteParser _parser;
 	private boolean canRetry = true;
 
 	public SiteDownloadSession(String identifier, FreenetURI uri, BucketFactory bf, String archiveType, HighLevelSimpleClient hlsc, ClientContext clientContext) {
 		super(identifier);
 		_uri = uri;
 		_bf = bf;
 		_archiveType = archiveType;
 		_hlsc = hlsc;
 		_clientContext = clientContext;
 	}
 
 	@Override
 	public void cancel() {
 		_parser.cancel(true);
 	}
 
 	@Override
 	public void destroySession() {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void execute() {
 		Logger.error(this, "executione begin");
 		currentProgress = "begin";
 		try {
 			setNewTempBucket(_bf);
 			OutputStream os = new BufferedOutputStream(_result.getOutputStream());
 			if (Constants.DL_TYPE_TARGZ.equals(_archiveType)) {
 				os = new GZIPOutputStream(os);
 			}
 
 			SiteCollector collector = new SiteCollector(os, Constants.DL_TYPE_ZIP.equals(_archiveType), _bf);
 			_parser = new SiteParser(collector, _uri, true, true, _hlsc, _clientContext);
 			try {
 				currentProgress = "init done";
 				_parser.parseSite();
 				currentProgress = "started";
 				_parser.waitForCompletion();
 			} finally {
 				Closer.close(os);
 				os = null;
 			}
 
 			if (Constants.DL_TYPE_TAR7Z.equals(_archiveType)) {
 				Bucket input = _result;
 				_result = _bf.makeBucket(-1);
 				BufferedInputStream cis = null;
 				BufferedOutputStream cos = null;
 				try {
 					cis = new BufferedInputStream(input.getInputStream());
 					cos = new BufferedOutputStream(_result.getOutputStream());
 					Encoder encoder = new Encoder();
 					encoder.SetEndMarkerMode( true );
 					encoder.SetDictionarySize( 1 << 20 );
 					// enc.WriteCoderProperties( out );
 					// 5d 00 00 10 00
 					encoder.Code( cis, cos, -1, -1, null );
 					cis.close();
 					cos.close();
 				} finally {
 					Closer.close(cis);
 					Closer.close(cos);
 					Closer.close(input);
 				}
 			}
 
 			currentProgress = "done?";
 		} catch (IOException e) {
 			Logger.error(this, "DEBUG", e);
 			setError(e);
 		} catch (FetchException e) {
 			Logger.error(this, "DEBUG", e);
 			canRetry = !e.isFatal();
 			setError(e);
 		}
 		Logger.error(this, "executione end");
 	}
 
 	@Override
 	public void getExtraStatusPanel(HTMLNode node) {
 		if (_parser != null) {
 			node.addChild("#", "Items total: " + _parser.getItemsTotal());
 			node.addChild("br");
 			node.addChild("#", "Items left: " + _parser.getItemsLeft());
 			node.addChild("br");
 			node.addChild("#", "Items done: " + _parser.getItemsDone());
 			node.addChild("br");
 			node.addChild("#", "Items failed: " + _parser.getItemsError());
 			node.addChild("br");
 			HashMap<String, String> stats = _parser.getProgressStats();
 			for (Entry<String, String> entry : stats.entrySet()) {
 				node.addChild("#", "Name: " + entry.getKey());
 				node.addChild("br");
 				node.addChild("#", "Stat: " + entry.getValue());
 				node.addChild("br");
 			}
 		} else {
			node.addChild("#", "<Empty>");
 		}
 	}
 
 	@Override
 	public void handleFCP(PluginReplySender replysender, SimpleFieldSet params,
 			Bucket data, int accesstype) throws PluginNotFoundException {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public boolean canRetry() {
 		return canRetry;
 	}
 
 	private void setNewTempBucket(BucketFactory bf) throws IOException {
 		if (_result != null) {
 			_result.free();
 		}
 		_result = bf.makeBucket(-1);
 	}
 
 	@Override
 	public void sendResult(ToadletContext ctx) throws ToadletContextClosedException, IOException {
 		String mime;
 		String ext;
 		if (Constants.DL_TYPE_TAR7Z.equals(_archiveType)) {
 			mime = "application/x-lzma-compressed-tar";
 			ext = "tar.lzma";
 		} else if (Constants.DL_TYPE_TARGZ.equals(_archiveType)) {
 			mime = "application/x-gtar";
 			ext = "tar.gz";	
 		} else if (Constants.DL_TYPE_TAR.equals(_archiveType)) {
 			mime = "application/x-tar";
 			ext = "tar";
 		} else if (Constants.DL_TYPE_ZIP.equals(_archiveType)) {
 			mime = "application/zip";
 			ext = "zip";
 		} else {
 			throw new IllegalStateException("Invalid archive type '"+ _archiveType+"'");
 		}
 		MultiValueTable<String, String> head = new MultiValueTable<String, String>();
 		head.put("Content-Disposition", "attachment; filename=\"" + "sitearchive."+ ext + '"');
 		ctx.sendReplyHeaders(200, "Found", head, mime, _result.size());
 		ctx.writeData(_result);
 	}
 
 	@Override
 	public boolean haveResult() {
 		return true;
 	}
 }
