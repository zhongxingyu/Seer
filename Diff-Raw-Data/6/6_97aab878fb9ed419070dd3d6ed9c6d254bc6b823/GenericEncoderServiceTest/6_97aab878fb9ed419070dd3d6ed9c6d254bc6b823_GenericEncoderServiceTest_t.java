 package uk.co.unclealex.music.encoder.service;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.SortedSet;
 
 import org.apache.commons.collections15.CollectionUtils;
 import org.apache.commons.collections15.Predicate;
 import org.apache.commons.collections15.Transformer;
 import org.apache.commons.lang.StringUtils;
 
 import uk.co.unclealex.music.base.dao.FlacTrackDao;
 import uk.co.unclealex.music.base.model.EncodedAlbumBean;
 import uk.co.unclealex.music.base.model.EncodedArtistBean;
 import uk.co.unclealex.music.base.model.EncodedTrackBean;
 import uk.co.unclealex.music.base.model.EncoderBean;
 import uk.co.unclealex.music.base.model.OwnerBean;
 import uk.co.unclealex.music.base.service.titleformat.TitleFormatService;
 import uk.co.unclealex.music.encoder.action.AlbumAddedAction;
 import uk.co.unclealex.music.encoder.action.AlbumCoverAddedAction;
 import uk.co.unclealex.music.encoder.action.AlbumCoverRemovedAction;
 import uk.co.unclealex.music.encoder.action.AlbumRemovedAction;
 import uk.co.unclealex.music.encoder.action.ArtistAddedAction;
 import uk.co.unclealex.music.encoder.action.ArtistRemovedAction;
 import uk.co.unclealex.music.encoder.action.EncodingAction;
 import uk.co.unclealex.music.encoder.action.FileAddedAction;
 import uk.co.unclealex.music.encoder.action.FileRemovedAction;
 import uk.co.unclealex.music.encoder.action.NoAlbumCoversFoundAction;
 import uk.co.unclealex.music.encoder.action.TrackEncodedAction;
 import uk.co.unclealex.music.encoder.action.TrackOwnedAction;
 import uk.co.unclealex.music.encoder.action.TrackRemovedAction;
 import uk.co.unclealex.music.encoder.action.TrackTaggedAction;
 import uk.co.unclealex.music.encoder.action.TrackUnownedAction;
 import uk.co.unclealex.music.test.TestFlacProvider;
 
 public class GenericEncoderServiceTest extends EncoderServiceTest {
 
 	private FlacTrackDao i_flacTrackDao;
 	
 	@Override
 	protected String getTestBase() {
 		return "generic";
 	}
 
 	public void testInitialEncoding() throws Exception {
 		doTestEncoding("Inital Encoding");
 	}
 	
 	public void testUpdate() throws Exception {
 		String keepYourselfAlive = "queen/queen/01_keep_yourself_alive.flac";
 		TestFlacProvider testFlacProvider = getTestFlacProvider();
 		testFlacProvider.touchFiles(keepYourselfAlive);
 		String keepYourselfAliveUrl = new File(testFlacProvider.getWorkingDirectory(), keepYourselfAlive).toURI().toString();
 		List<EncodingAction> expectedActions = new ArrayList<EncodingAction>();
 		TitleFormatService titleFormatService = getTitleFormatService();
 		for (EncodedTrackBean encodedTrackBean : getEncodedTrackDao().findByUrl(keepYourselfAliveUrl)) {
 			expectedActions.add(new TrackRemovedAction(encodedTrackBean));
 			SortedSet<OwnerBean> ownerBeans = encodedTrackBean.getOwnerBeans();
 			for (OwnerBean ownerBean : ownerBeans) {
 				String path = titleFormatService.createTitle(encodedTrackBean, ownerBean); 
 				expectedActions.add(new FileRemovedAction(path));
 			}
 			expectedActions.add(new TrackEncodedAction(encodedTrackBean));
 			expectedActions.add(new TrackTaggedAction(encodedTrackBean));
 			for (OwnerBean ownerBean : ownerBeans) {
 				expectedActions.add(new TrackOwnedAction(ownerBean, encodedTrackBean));
 			  String path = titleFormatService.createTitle(encodedTrackBean, ownerBean); 
 				expectedActions.add(new FileAddedAction(path));
 			}
 			expectedActions.add(new TrackEncodedAction(encodedTrackBean));
 		}
 		doTestEncoding("Update", expectedActions);
 	}
 
 	public void testDelete() throws Exception {
 		List<EncodingAction> expectedEncodingActions = new ArrayList<EncodingAction>();
 		Collection<String> queenPaths = 
 			CollectionUtils.select(
 				getFileDao().findAllPaths(),
 				new Predicate<String>() {
 					public boolean evaluate(String path) {
 						return path.matches("(Alex|Trevor)/(ogg|mp3)/Q.*");
 					}
 				});
 		CollectionUtils.collect(
 			queenPaths, new Transformer<String, EncodingAction>() {
 				@Override
 				public EncodingAction transform(String path) {
 					return new FileRemovedAction(path);
 				}
 			},
 			expectedEncodingActions);
 		EncodedArtistBean queen = getEncodedArtistDao().findByCode("QUEEN");
 		expectedEncodingActions.add(new ArtistRemovedAction(queen));
 		for (EncodedAlbumBean encodedAlbumBean : queen.getEncodedAlbumBeans()) {
 			expectedEncodingActions.add(new AlbumRemovedAction(encodedAlbumBean));
 			for (EncodedTrackBean encodedTrackBean : encodedAlbumBean.getEncodedTrackBeans()) {
 				expectedEncodingActions.add(new TrackRemovedAction(encodedTrackBean));
 			}
 		}
 		expectedEncodingActions.add(new AlbumCoverRemovedAction("QUEEN", "QUEEN"));
 		getTestFlacProvider().removeFiles("queen");
 		doTestEncoding("Remove", expectedEncodingActions);
 	}
 	
 	public void testAdd() throws Exception {
 		List<EncodingAction> expectedEncodingActions = new ArrayList<EncodingAction>();
 		addMetallicaAlbum(expectedEncodingActions, "Ride The Lightning", true, "Fight Fire With Fire", "Ride The Lightning");
		addMetallicaAlbum(expectedEncodingActions, "Death Magnetic", false, "That Was Just Your Life", "The End Of The Line");
 		getTestFlacProvider().mergeResource("add");
 		doTestEncoding("Add", expectedEncodingActions);
 	}
 	
 	protected void addMetallicaAlbum(
 			List<EncodingAction> expectedEncodingActions, String album, boolean coversExpected, String... tracks) {
 		String artistCode = "METALLICA";
 		String albumCode = album.toUpperCase();
 		expectedEncodingActions.add(new ArtistAddedAction(artistCode));
 		expectedEncodingActions.add(new AlbumAddedAction(artistCode, albumCode));
 		for (EncoderBean encoderBean : getEncoderDao().getAll()) {
 			String extension = encoderBean.getExtension();
 			for (int idx = 0; idx < tracks.length; idx++) {
 				int trackNumber = idx + 1;
 				String trackCode = tracks[idx].toUpperCase();
 				String filename = String.format("%02d - %s", trackNumber, tracks[idx]);
 				expectedEncodingActions.add(new TrackEncodedAction(artistCode, albumCode, trackNumber, trackCode, extension));
 				expectedEncodingActions.add(new TrackOwnedAction("Alex", artistCode, albumCode, trackNumber, trackCode, extension));
 				if (coversExpected) {
 					expectedEncodingActions.add(new TrackTaggedAction(artistCode, albumCode, trackNumber, trackCode, extension));
 				}
 				List<String> parts = new LinkedList<String>(Arrays.asList(new String[] { "Alex", extension }));
 				for (String part : new String[] { "M", "Metallica", album, filename + "." + extension }) {
 					parts.add(part);
 					expectedEncodingActions.add(new FileAddedAction(StringUtils.join(parts, '/')));
 				}
 			}
 		}
 		EncodingAction albumCoverAction;
 		if (coversExpected) {
 			albumCoverAction = new AlbumCoverAddedAction(artistCode, albumCode);
 		}
 		else {
 			albumCoverAction = new NoAlbumCoversFoundAction(artistCode, albumCode);
 		}
 		expectedEncodingActions.add(albumCoverAction);
 	}
 
 	public void testChangeOwner() throws Exception {
 		List<EncodingAction> expectedEncodingActions = new ArrayList<EncodingAction>();
 		Collection<String> metallicaPaths = 
 			CollectionUtils.select(
 				getFileDao().findAllPaths(),
 				new Predicate<String>() {
 					public boolean evaluate(String path) {
 						return path.matches("Alex/(ogg|mp3)/M.*");
 					}
 				});
 		for (String metallicaPath : metallicaPaths) {
 			expectedEncodingActions.add(new FileRemovedAction(metallicaPath));
 			expectedEncodingActions.add(new FileAddedAction(metallicaPath.replace("Alex/", "Trevor/")));
 		}
 		EncodedArtistBean metallica = getEncodedArtistDao().findByCode("METALLICA");
 		for (EncodedTrackBean encodedTrackBean : getEncodedTrackDao().findByArtist(metallica)) {
 			expectedEncodingActions.add(new TrackOwnedAction("Trevor", encodedTrackBean));
 			expectedEncodingActions.add(new TrackUnownedAction("Alex", encodedTrackBean));
 		}
 		TestFlacProvider testFlacProvider = getTestFlacProvider();
 		testFlacProvider.removeFiles("metallica/owner.alex");
 		testFlacProvider.mergeResource("changeowner");
 		doTestEncoding("ChangeOwner", expectedEncodingActions);
 	}
 	
 	public FlacTrackDao getFlacTrackDao() {
 		return i_flacTrackDao;
 	}
 
 	public void setFlacTrackDao(FlacTrackDao flacTrackDao) {
 		i_flacTrackDao = flacTrackDao;
 	}
 }
