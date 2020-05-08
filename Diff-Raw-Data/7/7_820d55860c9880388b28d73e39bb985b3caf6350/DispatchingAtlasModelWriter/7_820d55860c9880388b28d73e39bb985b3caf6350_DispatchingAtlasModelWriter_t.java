 package org.atlasapi.feeds.www;
 
 import static com.metabroadcast.common.media.MimeType.APPLICATION_JSON;
 import static com.metabroadcast.common.media.MimeType.APPLICATION_OEMBED_JSON;
 import static com.metabroadcast.common.media.MimeType.APPLICATION_OEMBED_XML;
 import static com.metabroadcast.common.media.MimeType.APPLICATION_RDF_XML;
 import static com.metabroadcast.common.media.MimeType.APPLICATION_XML;
 import static com.metabroadcast.common.media.MimeType.TEXT_HTML;
 import static com.metabroadcast.common.media.MimeType.TEXT_PLAIN;
 
 import java.io.IOException;
 import java.util.Collection;
 import java.util.List;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.atlasapi.beans.AtlasErrorSummary;
 import org.atlasapi.beans.AtlasModelWriter;
 import org.atlasapi.beans.FullToSimpleModelTranslator;
 import org.atlasapi.beans.JaxbXmlTranslator;
 import org.atlasapi.beans.JsonTranslator;
 import org.atlasapi.beans.OembedJsonTranslator;
 import org.atlasapi.beans.OembedXmlTranslator;
 import org.atlasapi.beans.html.HtmlTranslator;
 import org.atlasapi.rdf.beans.RdfXmlTranslator;
 
 import com.google.common.base.Charsets;
 import com.google.common.collect.Lists;
 import com.metabroadcast.common.http.HttpStatusCode;
 import com.metabroadcast.common.media.MimeType;
 
 public class DispatchingAtlasModelWriter implements AtlasModelWriter {
 
 	private final List<MappedWriter> writers = Lists.newArrayList();
 	
 	public DispatchingAtlasModelWriter() {
 		add("json", APPLICATION_JSON,  new FullToSimpleModelTranslator(new JsonTranslator()));
		add("rdf.xml", APPLICATION_RDF_XML, new RdfXmlTranslator());
		add("oembed.xml", APPLICATION_OEMBED_XML,  new OembedXmlTranslator());
		add("oembed.json",APPLICATION_OEMBED_JSON,  new OembedJsonTranslator());
 		add("xml", APPLICATION_XML,  new FullToSimpleModelTranslator(new JaxbXmlTranslator()));
 		add("html", TEXT_HTML, new HtmlTranslator());
 	}
 
 	private void add(String extension, MimeType mimeType, AtlasModelWriter writer) {
 		writers.add(new MappedWriter(extension, mimeType, writer));
 	}
 
 	@Override
 	public void writeError(HttpServletRequest request, HttpServletResponse response, AtlasErrorSummary exception) throws IOException {
 		MappedWriter writer = findWriterOrWriteNotFound(request, response);
 		if (writer != null) {
 			writer.write(request, response, exception);
 		}
 	}
 
 	@Override
 	public void writeTo(HttpServletRequest request, HttpServletResponse response, Collection<Object> graph) throws IOException {
 		MappedWriter writer = findWriterOrWriteNotFound(request, response);
 		if (writer != null) {
 			writer.write(request, response, graph);
 		}
 	}
 
 	private MappedWriter findWriterOrWriteNotFound(HttpServletRequest request, HttpServletResponse response) throws IOException {
 		for (MappedWriter mappedWriter : writers) {
 			if (mappedWriter.matches(request.getRequestURI())) {
 				return mappedWriter;
 			}
 		}
 		response.setStatus(HttpStatusCode.NOT_FOUND.code());
 		response.setCharacterEncoding(Charsets.UTF_8.toString());
 		response.setContentType(TEXT_PLAIN.toString());
 		response.getOutputStream().print("Not found");
 		return null;
 	}
 	
 	private static class MappedWriter {
 		
 		private final String extension;
 		private final AtlasModelWriter writer;
 		private final MimeType mimeType;
 
 		MappedWriter(String extension, MimeType mimeType, AtlasModelWriter writer) {
 			this.mimeType = mimeType;
 			this.extension = "." + extension;
 			this.writer = writer;
 		}
 		
 		void write(HttpServletRequest request, HttpServletResponse response, Collection<Object> graph) throws IOException {
 			response.setStatus(HttpStatusCode.OK.code());
 			response.setCharacterEncoding(Charsets.UTF_8.toString());
 			response.setContentType(mimeType.toString());
 			writer.writeTo(request, response, graph);
 		}
 		
 		void write(HttpServletRequest request, HttpServletResponse response, AtlasErrorSummary error) throws IOException {
 			response.setStatus(error.statusCode());
 			response.setCharacterEncoding(Charsets.UTF_8.toString());
 			response.setContentType(mimeType.toString());
 			writer.writeError(request, response, error);
 		}
 
 		boolean matches(String url) {
 			return url.endsWith(extension);
 		}
 	}
 }
