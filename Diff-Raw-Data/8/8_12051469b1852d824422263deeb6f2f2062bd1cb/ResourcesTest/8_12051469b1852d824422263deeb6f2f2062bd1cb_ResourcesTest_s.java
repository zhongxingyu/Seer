 package pl.jsolve.sweetener.io;
 
 import static org.fest.assertions.Assertions.assertThat;
 
 import java.nio.file.Path;
 import java.util.List;
 
 import org.junit.Test;
 
 public class ResourcesTest {
 
 	@Test
 	public void shouldFindPathForGivenFile() {
 		// given
 		String fileName = "testFile.txt";
 
 		// when
 		List<Path> paths = Resources.findFilePaths(fileName);
 
 		// then
 		assertThat(paths.size()).isGreaterThan(1);
		assertThat(paths.toString()).contains("src/main/resources/testFile.txt");
 	}
 
 	@Test
 	public void shouldFindPathForGivenFilePath() {
 		// given
 		String fileName = "testFile.txt";
 		String startingDir = "src/main/resources/";
 
 		// when
 		List<Path> paths = Resources.findFilePaths(startingDir, fileName);
 
 		// then
 		assertThat(paths).hasSize(1);
		assertThat(paths.get(0).toString()).isEqualTo("src/main/resources/testFile.txt");
 	}
 
 }
