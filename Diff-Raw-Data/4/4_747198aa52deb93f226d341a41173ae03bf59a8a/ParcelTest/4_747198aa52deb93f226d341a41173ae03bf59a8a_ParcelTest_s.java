 package ch.bergturbenthal.image.provider.model;
 
 import java.io.IOException;
 import java.util.Date;
 import java.util.UUID;
 import java.util.zip.GZIPInputStream;
 
 import org.codehaus.jackson.JsonProcessingException;
 import org.codehaus.jackson.map.ObjectMapper;
 import org.springframework.core.io.ClassPathResource;
 
 import android.os.Parcel;
 import android.os.Parcelable;
 import android.test.AndroidTestCase;
 import android.test.AssertionFailedError;
 import ch.bergturbenthal.raoa.data.model.AlbumDetail;
 import ch.bergturbenthal.raoa.data.model.AlbumImageEntry;
 import ch.bergturbenthal.raoa.provider.model.dto.AlbumEntries;
 import ch.bergturbenthal.raoa.provider.model.dto.AlbumEntryDto;
 import ch.bergturbenthal.raoa.provider.model.dto.AlbumMeta;
 
 public class ParcelTest extends AndroidTestCase {
 	private <P extends Parcelable> void checkCycle(final P original) {
 		final Parcel parcel = Parcel.obtain();
 		parcel.writeParcelable(original, 0);
 		parcel.setDataPosition(0);
 		final P copy = parcel.readParcelable(getClass().getClassLoader());
 		parcel.recycle();
 		try {
 			assertEquals(original, copy);
 		} catch (final AssertionFailedError ex) {
 			System.out.println(original);
 			System.out.println(copy);
 			throw ex;
 		}
 	}
 
 	private Date daysInThePast(final int days) {
 		return new Date(System.currentTimeMillis() - days * 24 * 60 * 6 * 1000);
 	}
 
 	private AlbumDetail readNiederwilJson() throws IOException, JsonProcessingException {
 		final ObjectMapper objectMapper = new ObjectMapper();
 		final AlbumDetail detail = objectMapper.reader(AlbumDetail.class).readValue(new GZIPInputStream(new ClassPathResource("niederwil.json.gz").getInputStream()));
 		return detail;
 	}
 
 	public void testAllNiederwilEntries() throws JsonProcessingException, IOException {
 		final AlbumDetail detail = readNiederwilJson();
 		final AlbumEntries albumEntries = new AlbumEntries();
 		for (final AlbumImageEntry entry : detail.getImages()) {
 			final AlbumEntryDto dto = AlbumEntryDto.fromServer(entry);
 			albumEntries.getEntries().add(dto);
 		}
 		checkCycle(albumEntries);
 	}
 
 	public void testEmptyAlbumEntryDto() {
 		checkCycle(new AlbumEntryDto());
 	}
 
 	public void testEmptyAlbumMeta() {
 		final AlbumMeta albumMeta = new AlbumMeta();
 		checkCycle(albumMeta);
 	}
 
 	public void testFilledAlbumEntryDto() throws JsonProcessingException, IOException {
 		final AlbumDetail detail = readNiederwilJson();
 		final AlbumImageEntry entry = detail.getImages().iterator().next();
 		checkCycle(AlbumEntryDto.fromServer(entry));
 	}
 
 	public void testFilledAlbumMeta() {
 		final AlbumMeta albumMeta = new AlbumMeta();
 		albumMeta.setAlbumDate(daysInThePast(2));
 		albumMeta.setAlbumId(UUID.randomUUID().toString());
 		albumMeta.setArchiveName("Test-Archive");
		albumMeta.setAutoAddDate(daysInThePast(4));
 		albumMeta.setEntryCount(42);
 		albumMeta.setLastModified(daysInThePast(1));
 		albumMeta.setName("Test-Album");
 		albumMeta.setOriginalsSize(5 * 1024 * 1024);
 		albumMeta.setRepositorySize(3 * 1024 * 1024);
 		albumMeta.setThumbnailId("thumb-1");
 		albumMeta.setThumbnailSize(512 * 1024);
 		checkCycle(albumMeta);
 	}
 
 }
