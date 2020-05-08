 package mdettlaff.javagit.index;
 
 import java.io.IOException;
 import java.nio.file.Path;
 import java.nio.file.Paths;
 import java.util.ArrayList;
 import java.util.List;
 
 import mdettlaff.javagit.common.Constants;
 import mdettlaff.javagit.common.FilesWrapper;
 import mdettlaff.javagit.common.ObjectId;
 
 import org.apache.commons.lang3.ArrayUtils;
 
 import com.google.common.base.Preconditions;
 import com.google.common.collect.ImmutableList;
 
 public class IndexIO {
 
 	private static final Path INDEX_PATH = Paths.get(Constants.GIT_DIR, "index");
 	private static final int HEADER_SIZE_IN_BYTES = 12;
 
 	private final FilesWrapper files;
 
 	public IndexIO(FilesWrapper files) {
 		this.files = files;
 	}
 
 	public Index read() throws IOException {
 		Preconditions.checkState(files.exists(INDEX_PATH), "Index file not found");
 		byte[] bytes = files.readAllBytes(INDEX_PATH);
 		int i = HEADER_SIZE_IN_BYTES;
 		List<IndexEntry> entries = new ArrayList<>();
 		while (i < bytes.length) {
 			int entryStart = i;
 			String signature = new String(ArrayUtils.subarray(bytes, entryStart, entryStart + 4));
 			if (signature.equals("TREE")) {
 				break;
 			}
 			int idStart = entryStart + 40;
 			int idEnd = idStart + 20;
 			ObjectId id = new ObjectId(ArrayUtils.subarray(bytes, idStart, idEnd));
 			i = idEnd + 2;
 			int pathStart = i;
			while (i < bytes.length && bytes[i] != (byte) 0) {
 				i++;
 			}
 			int pathEnd = i;
 			Path path = Paths.get(new String(ArrayUtils.subarray(bytes, pathStart, pathEnd)));
 			i++;
			while (i < bytes.length && bytes[i] == (byte) 0 && (entryStart + i) % 8 != 0) {
 				i++;
 			}
 			entries.add(new IndexEntry(id, path));
 		}
 		return new Index(ImmutableList.copyOf(entries));
 	}
 
 	public void write(Index index) {
 		// TODO implement
 		throw new UnsupportedOperationException("Not yet implemented");
 	}
 }
