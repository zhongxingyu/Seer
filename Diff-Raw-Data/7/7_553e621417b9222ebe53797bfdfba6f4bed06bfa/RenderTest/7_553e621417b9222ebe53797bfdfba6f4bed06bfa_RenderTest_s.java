 package com.dc2f.renderer;
 
 import static org.junit.Assert.*;
 
 import java.io.ByteArrayOutputStream;
 import java.io.CharArrayWriter;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.OutputStream;
 import java.io.Writer;
 import java.net.URISyntaxException;
 import java.util.Collections;
 import java.util.logging.Logger;
 
 import org.junit.Test;
 
 import com.dc2f.contentrepository.BranchAccess;
 import com.dc2f.contentrepository.CRSession;
 import com.dc2f.contentrepository.ContentRepository;
 import com.dc2f.contentrepository.ContentRepositoryFactory;
 import com.dc2f.contentrepository.Node;
 import com.dc2f.renderer.ContentRenderResponse;
 import com.dc2f.renderer.NodeRenderer;
 import com.dc2f.renderer.NodeRendererFactory;
 import com.dc2f.renderer.impl.ContentRenderRequestImpl;
 import com.dc2f.renderer.url.URLMapper;
 
 public class RenderTest {
 	private static final Logger logger = Logger.getLogger(RenderTest.class.getName());
 
 	@Test
 	public void testRendering() throws FileNotFoundException, URISyntaxException {
 		NodeRendererFactory factory = NodeRendererFactory.getInstance();
 		NodeRenderer renderer = factory.getRenderer("com.dc2f.renderer.web");
 		
 		String crdirPath = "/example";
		
		File crdir = new File(RenderTest.class.getResource(crdirPath).toURI());
 		assertNotNull("cannot find the example repository in my libraries.", crdir);
 		ContentRepository cr = ContentRepositoryFactory.getInstance().getContentRepository("simplejsonfile", Collections.singletonMap("directory", (Object)crdir.getAbsolutePath()));
 		assertNotNull("cannot initialize the json repository for " + crdir, cr);
 		CRSession conn = cr.authenticate(null);
 		BranchAccess craccess = conn.openTransaction(null);
 		//ContentRepository cr = new SimpleFileContentRepository(crdir);
 		
 		//Node node = cr.getNode("/cmsblog/articles/my-first-article");
 //		Node node = cr.getNode("/cmsblog");
 //		logger.info("We got a node: {" + node + "}");
 		
 		
 		final Writer writer = new CharArrayWriter();
 		final OutputStream stream = new ByteArrayOutputStream();
 		
 		renderer.renderNode(new ContentRenderRequestImpl(cr, craccess, craccess.getNodesInPath("/website"), new URLMapper() {
 
 			public String getRenderURL(Node node) {
 				return node.getPath();
 			}
 			
 		}), new ContentRenderResponse() {
 			
 			public Writer getWriter() {
 				return writer;
 			}
 			
 			public OutputStream getOutputStream() {
 				return stream;
 			}
 
 			public void setMimeType(String mimeType) {
 				logger.info("Setting mimeType to {" + mimeType + "}");
 			}
 		});
 		
 		logger.info("We rendered something: " + writer.toString());
 	}
 
 }
