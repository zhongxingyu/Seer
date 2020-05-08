 package com.mmounirou.spoty4j.xml;
 
 import java.io.ByteArrayInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.UnsupportedEncodingException;
 import java.lang.annotation.Annotation;
 import java.lang.reflect.Type;
 import java.util.List;
 
 import javax.ws.rs.WebApplicationException;
 import javax.ws.rs.core.MediaType;
 import javax.ws.rs.core.MultivaluedMap;
 import javax.ws.rs.ext.MessageBodyReader;
 
 import org.apache.commons.digester3.Digester;
 import org.xml.sax.SAXException;
 
 import com.google.common.base.Charsets;
 import com.google.common.collect.ImmutableList;
 import com.google.common.collect.Lists;
 import com.google.common.io.CharStreams;
 import com.mmounirou.spoty4j.core.Artist;
 import com.mmounirou.spoty4j.xml.ArtistSearchResultProvider.ArtistSearchResult;
 
 public class ArtistSearchResultProvider implements MessageBodyReader<ArtistSearchResult>
 {
 
 	public static class ArtistSearchResult
 	{
 
 		private List<Artist> m_artists = Lists.newArrayList();
 
 		public ImmutableList<Artist> getArtists()
 		{
 			return ImmutableList.copyOf(m_artists);
 		}
 
 		public void addArtist(Artist artist)
 		{
 			m_artists.add(artist);
 		}
 
 	}
 
 	public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType)
 	{
 		return ArtistSearchResult.class.isAssignableFrom(type);
 	}
 
 	public ArtistSearchResult readFrom(Class<ArtistSearchResult> type, Type genericType, Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, String> httpHeaders,
 			InputStream entityStream) throws IOException, WebApplicationException
 	{
 
 		Digester digester = new Digester();
 		addRules(digester);
 
 		try
 		{
 			return digester.parse(cleanStream(entityStream));
 		}
 		catch ( SAXException e )
 		{
 			throw new IOException(e);
 		}
 	}
 
 	private ByteArrayInputStream cleanStream(InputStream entityStream) throws IOException, UnsupportedEncodingException
 	{
 		String strStream = CharStreams.toString(new InputStreamReader(entityStream, Charsets.UTF_8.name()));
		String clean = strStream.replaceAll("&([^;]+(?!(?:\\w|;)))", "&amp;$1");
 		return new ByteArrayInputStream(clean.getBytes());
 	}
 
 	private void addRules(Digester digester)
 	{
 		digester.addObjectCreate("artists", ArtistSearchResult.class);
 
 		digester.addObjectCreate("artists/artist", Artist.class);
 		digester.addSetNext("artists/artist", "addArtist");
 
 		digester.addBeanPropertySetter("artists/artist/name");
 		digester.addBeanPropertySetter("artists/artist/id");
 		digester.addBeanPropertySetter("artists/artist/popularity");
 		digester.addSetProperties("artists/artist", "href", "href");
 	}
 
 }
