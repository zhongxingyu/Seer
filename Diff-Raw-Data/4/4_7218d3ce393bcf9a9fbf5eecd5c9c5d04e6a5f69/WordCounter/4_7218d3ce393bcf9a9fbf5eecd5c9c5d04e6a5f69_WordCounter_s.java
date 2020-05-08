 package task2;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 /**
  * Поиск количества вхождений слова в тексте.
  * 
  */
 public class WordCounter {
 	public static final String DELIMITER = " ";
 
 	private File file;
 
 	/**
 	 * имя файла с текстом
 	 * 
 	 * @param file
 	 */
 	public WordCounter(String file) {
 		this(new File(file));
 	}
 
 	/**
 	 * имя файла с текстом
 	 * 
 	 * @param file
 	 */
 	public WordCounter(File file) {
 		this.file = file;
 	}
 
 	/**
 	 * Поиск количества вхождений слова в тексте. На вход программе подается имя
 	 * файла с текстом и слово, на выходе число вхождений данного слова в
 	 * тексте.
 	 * 
 	 * @param word
 	 *            слово
 	 * @return количество вхождений
 	 * @throws IOException
 	 */
 	int count(String word) throws IOException {
 		Pattern pattern = Pattern.compile(word, Pattern.CASE_INSENSITIVE);
 		BufferedReader reader = null;
 		int num = 0;
 		try {
 			reader = new BufferedReader(new FileReader(file));
 			String line = null;
 			while ((line = reader.readLine()) != null) {
 				Matcher m = pattern.matcher(line);
 				while (m.find()) {
 					++num;
 				}
 			}
 		} finally {
			reader.close();
 		}
 		return num;
 	}
 }
