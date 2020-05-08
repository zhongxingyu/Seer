 package com.mmounirou.spoty4j.xml;
 
 import static org.fest.assertions.Assertions.assertThat;
 
 import java.io.IOException;
 import java.io.InputStream;
 
 import javax.ws.rs.WebApplicationException;
 
 import org.junit.Before;
 import org.junit.Test;
 
 import com.google.common.collect.ImmutableList;
 import com.mmounirou.spoty4j.core.Artist;
 import com.mmounirou.spoty4j.xml.ArtistSearchResultProvider.ArtistSearchResult;
 
 public class ArtistSearchResultProviderTest
 {
 
 	private ArtistSearchResultProvider m_artistSearchResultProvider;
 
 	@Before
 	public void createArtistSearchResultProvider() throws Exception
 	{
 		m_artistSearchResultProvider = new ArtistSearchResultProvider();
 	}
 
 	@Test
 	public void testIsReadable()
 	{
 		assertThat(m_artistSearchResultProvider.isReadable(ArtistSearchResult.class, null, null, null)).isTrue();
 	}
 
 	@Test
 	public void testIsNotReadable()
 	{
 		assertThat(m_artistSearchResultProvider.isReadable(Object.class, null, null, null)).isFalse();
 	}
 
 	@Test
 	public void testReadFrom() throws WebApplicationException, IOException
 	{
 		InputStream inputStream = ArtistSearchResultProvider.class.getResourceAsStream("/search-artist.xml");
 		ArtistSearchResult result = m_artistSearchResultProvider.readFrom(ArtistSearchResult.class, null, null, null, null, inputStream);
 
 		ImmutableList<Artist> artists = result.getArtists();
 		assertThat(artists).hasSize(2);
 		Artist artist = artists.get(0);
 		assertThat(artist.getName()).isEqualTo("Foo Fighters");
		assertThat(artist.getId()).isEqualTo("P 144725");
 		assertThat(artist.getHref()).isEqualTo("spotify:artist:7jy3rLJdDQY21OgRLCZ9sD");
 		assertThat(artist.getPopularity()).isEqualTo(0.76);
 
 	}
 
 }
