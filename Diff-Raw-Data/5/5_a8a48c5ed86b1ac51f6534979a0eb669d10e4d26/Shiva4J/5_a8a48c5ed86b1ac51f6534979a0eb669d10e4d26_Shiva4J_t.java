 package at.tripwire.shiva4j;
 
 import java.io.IOException;
 import java.io.StringWriter;
 import java.sql.Timestamp;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Collection;
 
 import org.apache.commons.io.IOUtils;
 import org.apache.http.HttpResponse;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import at.tripwire.shiva4j.objects.Album;
 import at.tripwire.shiva4j.objects.Artist;
 import at.tripwire.shiva4j.objects.Lyric;
 import at.tripwire.shiva4j.objects.Show;
 import at.tripwire.shiva4j.objects.Show.OtherArtist;
 import at.tripwire.shiva4j.objects.Track;
 import at.tripwire.shiva4j.objects.impl.AlbumImpl;
 import at.tripwire.shiva4j.objects.impl.ArtistImpl;
 import at.tripwire.shiva4j.objects.impl.LyricImpl;
 import at.tripwire.shiva4j.objects.impl.ShowImpl;
 import at.tripwire.shiva4j.objects.impl.ShowImpl.OtherArtistImpl;
 import at.tripwire.shiva4j.objects.impl.ShowImpl.VenueImpl;
 import at.tripwire.shiva4j.objects.impl.TrackImpl;
 
 public class Shiva4J {
 
 	private static final String URL_ALBUMS = "/albums";
 	private static final String URL_ARTISTS = "/artists";
 	private static final String URL_TRACKS = "/tracks";
 	private static final String URL_ARTIST = "/artist/";
 	private static final String URL_ALBUM = "/album/";
 	private static final String URL_TRACK = "/track/";
 	private static final String URL_TRACK_LYRIC = "/lyrics";
 	private static final String URL_ARTIST_SHOWS = "/shows";
 
 	private static final String PARAM_PAGE_SIZE = "page_size";
 	private static final String PARAM_PAGE_INDEX = "page";
 	private static final String PARAM_ARTIST = "artist";
 	private static final String PARAM_ALBUM = "album";
 
 	private static final String COMMON_ID = "id";
 	private static final String COMMON_SLUG = "slug";
 	private static final String COMMON_URI = "uri";
 
 	private static final String ARTIST_NAME = "name";
 	private static final String ARTIST_IMAGE = "image";
 	private static final String ARTIST_DOWNLOAD_URI = "download_uri";
 
 	private static final String ALBUM_NAME = "name";
 	private static final String ALBUM_DOWNLOAD_URI = "download_uri";
 	private static final String ALBUM_YEAR = "year";
 	private static final String ALBUM_COVER = "cover";
 	private static final String ALBUM_ARTISTS = "artists";
 
 	private static final String TRACK_LENGTH = "length";
 	private static final String TRACK_STREAM_URI = "stream_uri";
 	private static final String TRACK_NUMBER = "number";
 	private static final String TRACK_BITRATE = "bitrate";
 	private static final String TRACK_TITLE = "title";
 	private static final String TRACK_ALBUM = "album";
 	private static final String TRACK_ARTIST = "artist";
 
 	private static final String LYRIC_SOURCE_URI = "source_uri";
 	private static final String LYRIC_TEXT = "text";
 	private static final String LYRIC_TRACK = "track";
 
 	private static final String SHOW_DATETIME = "datetime";
 	private static final String SHOW_VENUE = "venue";
 	private static final String SHOW_TITLE = "title";
 	private static final String SHOW_TICKETS_LEFT = "tickets_left";
 	private static final String SHOW_ARTISTS = "artists";
 	private static final String SHOW_OTHER_ARTISTS = "other_artists";
 
 	private static final String VENUE_LATITUDE = "latitude";
 	private static final String VENUE_LONGITUDE = "longitude";
 	private static final String VENUE_NAME = "name";
 
 	private static final String OTHER_ARTISTS_MBID = "mbid";
 	private static final String OTHER_ARTISTS_FACEBOOK = "facebook_tour_dates_url";
 	private static final String OTHER_ARTISTS_IMAGE_URL = "image_url";
 	private static final String OTHER_ARTISTS_NAME = "name";
 
 	private SimpleDateFormat showDatetimeParser = new SimpleDateFormat();
 
 	private String baseUri;
 	private HttpClient httpClient;
 
 	/**
 	 * Create a new <code>Shiva4J</code> instance using shiva server's host and
 	 * port.
 	 * 
 	 * @param host
 	 *            The shiva server's host.
 	 * @param port
 	 *            The shiva server's port.
 	 */
 	public Shiva4J(String host, int port) {
 		this.baseUri = "http://" + host + ":" + port + "/";
 		this.httpClient = new DefaultHttpClient();
 	}
 
 	private String requestUrl(String uri) throws IOException {
 		return requestUrl(uri, -1, -1);
 	}
 
 	private String requestUrl(String uri, int pageSize, int pageIndex) throws IOException {
 		if (pageSize > 0) {
 			uri = uri + "?" + PARAM_PAGE_SIZE + "=" + pageSize + "&" + PARAM_PAGE_INDEX + "=" + pageIndex;
 		}
 
 		HttpGet get = new HttpGet(baseUri + uri);
 		HttpResponse response = httpClient.execute(get);
 		StringWriter writer = new StringWriter();
 		IOUtils.copy(response.getEntity().getContent(), writer);
 		return writer.toString();
 	}
 
 	/**
 	 * Get all artists available in shiva database.
 	 * 
 	 * @return A collection of all artists. <code>null</code> if JSON content is
 	 *         not well formatted.
 	 * @throws IOException
 	 *             if network errors occurs.
 	 * @see #getArtists(int, int)
 	 * @see #getArtists(Album)
 	 */
 	public Collection<Artist> getArtists() throws IOException {
 		return getArtists(-1, -1);
 	}
 
 	/**
 	 * Get paginated artists available in shiva database.
 	 * 
 	 * @param pageSize
 	 *            Size of artists which should be returned per page.
 	 * @param pageIndex
 	 *            Index of the current page.
 	 * @return A collection of artists. <code>null</code> if JSON content is not
 	 *         well formatted.
 	 * @throws IOException
 	 *             if network errors occurs.
 	 * @see #getArtists()
 	 * @see #getArtists(Album)
 	 */
 	public Collection<Artist> getArtists(int pageSize, int pageIndex) throws IOException {
 		try {
 			String data = requestUrl(URL_ARTISTS, pageSize, pageIndex);
 			JSONArray array = new JSONArray(data);
 			ArrayList<Artist> artists = new ArrayList<Artist>();
 
 			for (int i = 0; i < array.length(); i++) {
 				JSONObject obj = array.getJSONObject(i);
 				Artist a = parseArtist(obj);
 				artists.add(a);
 			}
 
 			return artists;
 		} catch (JSONException e) {
 		}
 		return null;
 	}
 
 	/**
 	 * Get all artists performing in the given album.
 	 * 
 	 * @param album
 	 *            The album.
 	 * @return A collection of album's artists. <code>null</code> if JSON
 	 *         content is not well formatted.
 	 * @throws IOException
 	 *             if network errors occurs.
 	 * @see #getArtists(int, int)
 	 * @see #getArtists()
 	 */
 	public Collection<Artist> getArtists(Album album) throws IOException {
 		ArrayList<Artist> artists = new ArrayList<Artist>();
 		for (Artist empty : album.getArtists()) {
 			Artist filled = getArtist(empty.getId());
 			artists.add(filled);
 		}
 		return artists;
 	}
 
 	/**
 	 * Get the artist performing the given track.
 	 * 
 	 * @param track
 	 *            The track.
 	 * @return The artist which performs the track. <code>null</code> if JSON
 	 *         content is not well formatted.
 	 * @throws IOException
 	 *             if network errors occurs.
 	 * @see #getArtist(int)
 	 */
 	public Artist getArtist(Track track) throws IOException {
 		return getArtist(track.getArtist().getId());
 	}
 
 	/**
 	 * Get a artist.
 	 * 
 	 * @param id
 	 *            The artist's ID.
 	 * @return The artist. <code>null</code> if JSON content is not well
 	 *         formatted.
 	 * @throws IOException
 	 *             if network errors occurs.
 	 * @see #getArtist(Track)
 	 */
 	public Artist getArtist(int id) throws IOException {
 		try {
 			String data = requestUrl(URL_ARTIST + id);
 			JSONObject obj = new JSONObject(data);
 			return parseArtist(obj);
 		} catch (JSONException e) {
 		}
 		return null;
 	}
 
 	/**
 	 * Get all artist shows provided by BandsInTown.
 	 * 
 	 * @param artist
 	 *            The artist.
 	 * @return A collection of all artist's shows. <code>null</code> if JSON
 	 *         content is not well formatted.
 	 * @throws IOException
	 * @see #getArtistShows(int)
 	 */
 	public Collection<Show> getArtistShows(Artist artist) throws IOException {
 		return getArtistShows(artist.getId());
 	}
 
 	/**
 	 * Get all artist shows provided by BandsInTown.
 	 * 
 	 * @param artistId
 	 *            The artist's ID.
 	 * @return A collection of all artist's shows. <code>null</code> if JSON
 	 *         content is not well formatted.
 	 * @throws IOException
	 * @see #getArtistShows(Artist)
 	 */
 	public Collection<Show> getArtistShows(int artistId) throws IOException {
 		try {
 			String data = requestUrl(URL_ARTIST + artistId + URL_ARTIST_SHOWS);
 			JSONArray array = new JSONArray(data);
 			ArrayList<Show> shows = new ArrayList<Show>();
 
 			for (int i = 0; i < array.length(); i++) {
 				JSONObject obj = array.getJSONObject(i);
 				Show show = parseShow(obj);
 				shows.add(show);
 			}
 
 			return shows;
 		} catch (JSONException e) {
 		}
 		return null;
 	}
 
 	/**
 	 * Get all albums available in shiva database.
 	 * 
 	 * @return A collection of all albums. <code>null</code> if JSON content is
 	 *         not well formatted.
 	 * @throws IOException
 	 *             if network errors occurs.
 	 * @see #getAlbums(int, int)
 	 */
 	public Collection<Album> getAlbums() throws IOException {
 		return getAlbums(-1, -1);
 	}
 
 	/**
 	 * Get paginated albums available in shiva database.
 	 * 
 	 * @param pageSize
 	 *            Size of albums which should be returned per page.
 	 * @param pageIndex
 	 *            Index of the current page.
 	 * @return A collection of albums. <code>null</code> if JSON content is not
 	 *         well formatted.
 	 * @throws IOException
 	 *             if network errors occurs.
 	 * @see #getAlbums()
 	 */
 	public Collection<Album> getAlbums(int pageSize, int pageIndex) throws IOException {
 		return getAlbums(-1, pageSize, pageIndex);
 	}
 
 	/**
 	 * Get all albums performed by the given artist available in shiva database.
 	 * 
 	 * @param artist
 	 *            The performing artist.
 	 * 
 	 * @return A collection of all albums. <code>null</code> if JSON content is
 	 *         not well formatted.
 	 * @throws IOException
 	 *             if network errors occurs.
 	 * @see #getAlbums(int)
 	 */
 	public Collection<Album> getAlbums(Artist artist) throws IOException {
 		return getAlbums(artist.getId());
 	}
 
 	/**
 	 * Get all albums performed by the given artist available in shiva database.
 	 * 
 	 * @param artistId
 	 *            The performing artist's ID.
 	 * 
 	 * @return A collection of all albums. <code>null</code> if JSON content is
 	 *         not well formatted.
 	 * @throws IOException
 	 *             if network errors occurs.
 	 * @see #getAlbums(Artist)
 	 */
 	public Collection<Album> getAlbums(int artistId) throws IOException {
 		return getAlbums(artistId, -1, -1);
 	}
 
 	/**
 	 * Get paginated albums performed by the given artist available in shiva
 	 * database.
 	 * 
 	 * @param artist
 	 *            The performing artist.
 	 * @param pageSize
 	 *            Size of albums which should be returned per page.
 	 * @param pageIndex
 	 *            Index of the current page.
 	 * @return A collection of albums. <code>null</code> if JSON content is not
 	 *         well formatted.
 	 * @throws IOException
 	 *             if network errors occurs.
 	 * @see #getAlbums(Artist)
 	 */
 	public Collection<Album> getAlbums(Artist artist, int pageSize, int pageIndex) throws IOException {
 		return getAlbums(artist.getId(), pageSize, pageIndex);
 	}
 
 	/**
 	 * Get paginated albums performed by the given artist available in shiva
 	 * database.
 	 * 
 	 * @param artistId
 	 *            The performing artist's id.
 	 * @param pageSize
 	 *            Size of albums which should be returned per page.
 	 * @param pageIndex
 	 *            Index of the current page.
 	 * @return A collection of albums. <code>null</code> if JSON content is not
 	 *         well formatted.
 	 * @throws IOException
 	 *             if network errors occurs.
 	 * @see #getAlbums(int)
 	 */
 	public Collection<Album> getAlbums(int artistId, int pageSize, int pageIndex) throws IOException {
 		try {
 			String url = URL_ALBUMS;
 			if (artistId != -1) {
 				url = url + "?" + PARAM_ARTIST + "=" + artistId;
 			}
 
 			String data = requestUrl(url, pageSize, pageIndex);
 			JSONArray array = new JSONArray(data);
 
 			ArrayList<Album> albums = new ArrayList<Album>();
 
 			for (int i = 0; i < array.length(); i++) {
 				JSONObject obj = array.getJSONObject(i);
 				Album a = parseAlbum(obj);
 				albums.add(a);
 			}
 
 			return albums;
 		} catch (JSONException e) {
 		}
 		return null;
 	}
 
 	/**
 	 * Get a album.
 	 * 
 	 * @param id
 	 *            The album's ID.
 	 * @return The album. <code>null</code> if JSON content is not well
 	 *         formatted.
 	 * @throws IOException
 	 *             if network errors occurs.
 	 */
 	public Album getAlbum(int id) throws IOException {
 		try {
 			String data = requestUrl(URL_ALBUM + id);
 			JSONObject obj = new JSONObject(data);
 			return parseAlbum(obj);
 		} catch (JSONException e) {
 		}
 		return null;
 	}
 
 	/**
 	 * Get all tracks available in shiva database.
 	 * 
 	 * @return A collection of all tracks. <code>null</code> if JSON content is
 	 *         not well formatted.
 	 * @throws IOException
 	 *             if network errors occurs.
 	 * @see #getTracks(int, int)
 	 */
 	public Collection<Track> getTracks() throws IOException {
 		return getTracks(-1, -1);
 	}
 
 	/**
 	 * Get paginated tracks available in shiva database.
 	 * 
 	 * @param pageSize
 	 *            Size of tracks which should be returned per page.
 	 * @param pageIndex
 	 *            Index of the current page.
 	 * @return A collection of tracks. <code>null</code> if JSON content is not
 	 *         well formatted.
 	 * @throws IOException
 	 *             if network errors occurs.
 	 * @see #getTracks()
 	 */
 	public Collection<Track> getTracks(int pageSize, int pageIndex) throws IOException {
 		try {
 			String data = requestUrl(URL_TRACKS, pageSize, pageIndex);
 			JSONArray array = new JSONArray(data);
 			ArrayList<Track> tracks = new ArrayList<Track>();
 
 			for (int i = 0; i < array.length(); i++) {
 				JSONObject obj = array.getJSONObject(i);
 				Track track = parseTrack(obj);
 				tracks.add(track);
 			}
 
 			return tracks;
 		} catch (JSONException e) {
 		}
 		return null;
 	}
 
 	/**
 	 * Get paginated tracks performed by the given artist available in shiva
 	 * database.
 	 * 
 	 * @param artist
 	 *            The performing artist.
 	 * @param pageSize
 	 *            Size of tracks which should be returned per page.
 	 * @param pageIndex
 	 *            Index of the current page.
 	 * @return A collection of tracks. <code>null</code> if JSON content is not
 	 *         well formatted.
 	 * @throws IOException
 	 *             if network errors occurs.
 	 * @see #getArtistsTracks(int, int, int)
 	 */
 	public Collection<Track> getArtistsTracks(Artist artist, int pageSize, int pageIndex) throws IOException {
 		return getArtistsTracks(artist.getId(), pageSize, pageIndex);
 	}
 
 	/**
 	 * Get paginated tracks performed by the given artist available in shiva
 	 * database.
 	 * 
 	 * @param artistId
 	 *            The performing artist's ID.
 	 * @param pageSize
 	 *            Size of tracks which should be returned per page.
 	 * @param pageIndex
 	 *            Index of the current page.
 	 * @return A collection of tracks. <code>null</code> if JSON content is not
 	 *         well formatted.
 	 * @throws IOException
 	 *             if network errors occurs.
 	 * @see #getArtistsTracks(Artist, int, int)
 	 */
 	public Collection<Track> getArtistsTracks(int artistId, int pageSize, int pageIndex) throws IOException {
 		try {
 			String url = URL_TRACKS;
 
 			if (artistId != -1) {
 				url = url + "?" + PARAM_ARTIST + "=" + artistId;
 			}
 
 			String data = requestUrl(url, pageSize, pageIndex);
 			JSONArray array = new JSONArray(data);
 			ArrayList<Track> tracks = new ArrayList<Track>();
 
 			for (int i = 0; i < array.length(); i++) {
 				JSONObject obj = array.getJSONObject(i);
 				Track track = parseTrack(obj);
 				tracks.add(track);
 			}
 
 			return tracks;
 		} catch (JSONException e) {
 		}
 		return null;
 	}
 
 	/**
 	 * Get all tracks performed by the given artist available in shiva database.
 	 * 
 	 * @param artist
 	 *            The performing artist.
 	 * 
 	 * @return A collection of all tracks. <code>null</code> if JSON content is
 	 *         not well formatted.
 	 * @throws IOException
 	 *             if network errors occurs.
 	 * @see #getArtistsTracks(int)
 	 */
 	public Collection<Track> getArtistsTracks(Artist artist) throws IOException {
 		return getArtistsTracks(artist, -1, -1);
 	}
 
 	/**
 	 * Get all tracks performed by the given artist available in shiva database.
 	 * 
 	 * @param artistId
 	 *            The performing artist's ID.
 	 * 
 	 * @return A collection of all tracks. <code>null</code> if JSON content is
 	 *         not well formatted.
 	 * @throws IOException
 	 *             if network errors occurs.
 	 * @see #getArtistsTracks(Artist)
 	 */
 	public Collection<Track> getArtistsTracks(int artistId) throws IOException {
 		return getArtistsTracks(artistId, -1, -1);
 	}
 
 	/**
 	 * Get paginated album tracks available in shiva database.
 	 * 
 	 * @param album
 	 *            The album.
 	 * 
 	 * @param pageSize
 	 *            Size of tracks which should be returned per page.
 	 * @param pageIndex
 	 *            Index of the current page.
 	 * @return A collection of tracks. <code>null</code> if JSON content is not
 	 *         well formatted.
 	 * @throws IOException
 	 *             if network errors occurs.
 	 * @see #getAlbumTracks(int, int, int)
 	 */
 	public Collection<Track> getAlbumTracks(Album album, int pageSize, int pageIndex) throws IOException {
 		return getAlbumTracks(album.getId(), pageSize, pageIndex);
 	}
 
 	/**
 	 * Get paginated album tracks available in shiva database.
 	 * 
 	 * @param albumId
 	 *            The album's ID.
 	 * 
 	 * @param pageSize
 	 *            Size of tracks which should be returned per page.
 	 * @param pageIndex
 	 *            Index of the current page.
 	 * @return A collection of tracks. <code>null</code> if JSON content is not
 	 *         well formatted.
 	 * @throws IOException
 	 *             if network errors occurs.
 	 * @see #getAlbumTracks(Album, int, int)
 	 */
 	public Collection<Track> getAlbumTracks(int albumId, int pageSize, int pageIndex) throws IOException {
 		try {
 			String url = URL_TRACKS;
 
 			if (albumId != -1) {
 				url = url + "?" + PARAM_ALBUM + "=" + albumId;
 			}
 
 			String data = requestUrl(url, pageSize, pageIndex);
 			JSONArray array = new JSONArray(data);
 			ArrayList<Track> tracks = new ArrayList<Track>();
 
 			for (int i = 0; i < array.length(); i++) {
 				JSONObject obj = array.getJSONObject(i);
 				Track track = parseTrack(obj);
 				tracks.add(track);
 			}
 
 			return tracks;
 		} catch (JSONException e) {
 		}
 		return null;
 	}
 
 	/**
 	 * Get all album tracks available in shiva database.
 	 * 
 	 * @param album
 	 *            The album.
 	 * 
 	 * @return A collection of all tracks. <code>null</code> if JSON content is
 	 *         not well formatted.
 	 * @throws IOException
 	 *             if network errors occurs.
 	 * @see #getAlbumTracks(int)
 	 */
 	public Collection<Track> getAlbumTracks(Album album) throws IOException {
 		return getAlbumTracks(album, -1, -1);
 	}
 
 	/**
 	 * Get all album tracks available in shiva database.
 	 * 
 	 * @param albumId
 	 *            The album's ID.
 	 * 
 	 * @return A collection of all tracks. <code>null</code> if JSON content is
 	 *         not well formatted.
 	 * @throws IOException
 	 *             if network errors occurs.
 	 * @see #getAlbumTracks(Album)
 	 */
 	public Collection<Track> getAlbumTracks(int albumId) throws IOException {
 		return getAlbumTracks(albumId, -1, -1);
 	}
 
 	/**
 	 * Get a track.
 	 * 
 	 * @param id
 	 *            The track's ID.
 	 * @return The track. <code>null</code> if JSON content is not well
 	 *         formatted.
 	 * @throws IOException
 	 *             if network errors occurs.
 	 */
 	public Track getTrack(int id) throws IOException {
 		try {
 			String data = requestUrl(URL_TRACK + id);
 			JSONObject obj = new JSONObject(data);
 			return parseTrack(obj);
 		} catch (JSONException e) {
 		}
 		return null;
 	}
 
 	/**
 	 * Get a track.
 	 * 
 	 * @param lyric
 	 *            The tracks lyrics.
 	 * @return The track. <code>null</code> if JSON content is not well
 	 *         formatted.
 	 * @throws IOException
 	 *             if network errors occurs.
 	 */
 	public Track getTrack(Lyric lyric) throws IOException {
 		return getTrack(lyric.getTrack().getId());
 	}
 
 	/**
 	 * Get a tracks lyrics.
 	 * 
 	 * @param trackId
 	 *            The lyric's ID.
 	 * @return The lyric. <code>null</code> if JSON content is not well
 	 *         formatted.
 	 * @throws IOException
 	 *             if network errors occurs.
 	 */
 	public Lyric getLyric(int trackId) throws IOException {
 		try {
 			String data = requestUrl(URL_TRACK + trackId + URL_TRACK_LYRIC);
 			JSONObject obj = new JSONObject(data);
 			return parseLyric(obj);
 		} catch (JSONException e) {
 		}
 		return null;
 	}
 
 	private Artist parseArtist(JSONObject obj) throws JSONException {
 		ArtistImpl a = new ArtistImpl();
 		a.setId(obj.getInt(COMMON_ID));
 		a.setName(obj.getString(ARTIST_NAME));
 		a.setImageUrl(obj.getString(ARTIST_IMAGE));
 		a.setSlug(obj.getString(COMMON_SLUG));
 		a.setDownloadUri(obj.getString(ARTIST_DOWNLOAD_URI));
 		a.setUri(obj.getString(COMMON_URI));
 		return a;
 	}
 
 	private Album parseAlbum(JSONObject obj) throws JSONException {
 		AlbumImpl a = new AlbumImpl();
 		a.setId(obj.getInt(COMMON_ID));
 		a.setSlug(obj.getString(COMMON_SLUG));
 		a.setCoverUrl(obj.getString(ALBUM_COVER));
 		a.setUri(obj.getString(COMMON_URI));
 		a.setYear(obj.getInt(ALBUM_YEAR));
 		a.setName(obj.getString(ALBUM_NAME));
 		a.setDownloadUri(obj.getString(ALBUM_DOWNLOAD_URI));
 
 		JSONArray jsonArtists = obj.getJSONArray(ALBUM_ARTISTS);
 		ArtistImpl[] artists = new ArtistImpl[jsonArtists.length()];
 
 		for (int j = 0; j < jsonArtists.length(); j++) {
 			JSONObject artistObj = jsonArtists.getJSONObject(j);
 			artists[j] = new ArtistImpl();
 			artists[j].setId(artistObj.getInt(COMMON_ID));
 			artists[j].setUri(artistObj.getString(COMMON_URI));
 		}
 		a.setArtists(artists);
 		return a;
 	}
 
 	private Track parseTrack(JSONObject obj) throws JSONException {
 		TrackImpl track = new TrackImpl();
 		track.setId(obj.getInt(COMMON_ID));
 		track.setLength(obj.getInt(TRACK_LENGTH));
 		track.setStreamUri(obj.getString(TRACK_STREAM_URI));
 		track.setUri(obj.getString(COMMON_URI));
 		track.setNumber(obj.getInt(TRACK_NUMBER));
 		track.setBitrate(obj.getInt(TRACK_BITRATE));
 		track.setSlug(obj.getString(COMMON_SLUG));
 		track.setTitle(obj.getString(TRACK_TITLE));
 		JSONObject albumObj = obj.getJSONObject(TRACK_ALBUM);
 		AlbumImpl album = new AlbumImpl();
 		album.setId(albumObj.getInt(COMMON_ID));
 		album.setUri(albumObj.getString(COMMON_URI));
 		track.setAlbum(album);
 		JSONObject artistObj = obj.getJSONObject(TRACK_ARTIST);
 		ArtistImpl artist = new ArtistImpl();
 		artist.setId(artistObj.getInt(COMMON_ID));
 		artist.setUri(artistObj.getString(COMMON_URI));
 		track.setArtist(artist);
 		return track;
 	}
 
 	private Lyric parseLyric(JSONObject obj) throws JSONException {
 		LyricImpl lyric = new LyricImpl();
 		lyric.setId(obj.getInt(COMMON_ID));
 		lyric.setUri(obj.getString(COMMON_URI));
 		lyric.setSourceUri(obj.getString(LYRIC_SOURCE_URI));
 		lyric.setText(obj.getString(LYRIC_TEXT));
 
 		JSONObject trackObj = obj.getJSONObject(LYRIC_TRACK);
 		TrackImpl track = new TrackImpl();
 		track.setId(trackObj.getInt(COMMON_ID));
 		track.setUri(trackObj.getString(COMMON_URI));
 		lyric.setTrack(track);
 		return lyric;
 	}
 
 	private Show parseShow(JSONObject obj) throws JSONException {
 		ShowImpl show = new ShowImpl();
 		show.setId(obj.getInt(COMMON_ID));
 		long time;
 		try {
 			time = showDatetimeParser.parse(obj.getString(SHOW_DATETIME)).getTime();
 			show.setDatetime(new Timestamp(time));
 		} catch (ParseException e) {
 		}
 		show.setTicketsLeft(obj.getBoolean(SHOW_TICKETS_LEFT));
 		show.setTitle(obj.getString(SHOW_TITLE));
 
 		JSONObject venueObj = obj.getJSONObject(SHOW_VENUE);
 		VenueImpl venue = new VenueImpl();
 		venue.setName(venueObj.getString(VENUE_NAME));
 		venue.setLatitude(venueObj.getString(VENUE_LATITUDE));
 		venue.setLongitude(venueObj.getString(VENUE_LONGITUDE));
 		show.setVenue(venue);
 
 		JSONArray jsonArtists = obj.getJSONArray(SHOW_ARTISTS);
 		ArtistImpl[] artists = new ArtistImpl[jsonArtists.length()];
 		for (int j = 0; j < jsonArtists.length(); j++) {
 			JSONObject artistObj = jsonArtists.getJSONObject(j);
 			artists[j] = new ArtistImpl();
 			artists[j].setId(artistObj.getInt(COMMON_ID));
 			artists[j].setUri(artistObj.getString(COMMON_URI));
 		}
 		show.setArtists(artists);
 
 		JSONArray jsonOtherArtists = obj.getJSONArray(SHOW_OTHER_ARTISTS);
 		OtherArtist[] otherArtists = new OtherArtist[jsonOtherArtists.length()];
 		for (int j = 0; j < jsonOtherArtists.length(); j++) {
 			JSONObject jsonOtherArtist = jsonOtherArtists.getJSONObject(j);
 			OtherArtistImpl otherArtist = new OtherArtistImpl();
 			otherArtist.setFacebookTourDatesUrl(jsonOtherArtist.getString(OTHER_ARTISTS_FACEBOOK));
 			otherArtist.setImageUrl(jsonOtherArtist.getString(OTHER_ARTISTS_IMAGE_URL));
 			otherArtist.setMusicBrainzId(jsonOtherArtist.getString(OTHER_ARTISTS_MBID));
 			otherArtist.setName(jsonOtherArtist.getString(OTHER_ARTISTS_NAME));
 			otherArtists[j] = otherArtist;
 		}
 		show.setOtherArtists(otherArtists);
 
 		return show;
 	}
 }
