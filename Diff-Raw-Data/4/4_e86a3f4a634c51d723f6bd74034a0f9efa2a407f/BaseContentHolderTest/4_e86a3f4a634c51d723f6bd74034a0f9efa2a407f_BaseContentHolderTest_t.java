 package idx_coursors;
 
 import com.google.common.base.Optional;
 import com.google.common.cache.CacheBuilder;
 import com.google.common.cache.CacheLoader;
 import com.google.common.cache.LoadingCache;
 import com.google.common.collect.ImmutableList;
 import com.google.common.collect.ImmutableMap;
 import common.Util;
 import org.junit.Test;
 
 import static org.junit.Assert.assertEquals;
 import static org.mockito.Mockito.*;
 
 import java.util.*;
 import java.util.concurrent.ExecutionException;
 
 class InMemorySentencesAccessor implements ContentStorageAccessor {
   private final ImmutableList<String> CONTENT;
   public InMemorySentencesAccessor(ImmutableList<String> content) {
     CONTENT = content;
   }
 
   @Override
   public String getSentence(Integer key) {
     return CONTENT.get(key);
   }
 }
 
 class ReadException extends Exception {
   public ReadException() {super();}
 }
 
 class FailReadSentence extends RuntimeException {
   public FailReadSentence(Throwable e) {super(e);}
 }
 
 class CashedContentAccessor implements ContentStorageAccessor {
   private Optional<String> getSentenceFromFile(Integer key) throws ReadException {
     ImmutableList<String> allSentences = READER.fileToSentences();
     if (key < allSentences.size()) {
       return Optional.of(allSentences.get(key));
     }
     return Optional.absent();
   }
 
   public CashedContentAccessor (TextFileReader reader) {
     CASHE_SENTENCES = CacheBuilder.newBuilder()
       .maximumSize(1)
       .build(
         new CacheLoader<Integer, String>() {
           @Override
           public String load(Integer key) throws ReadException {
               return getSentenceFromFile(key).get();
              }
         });
     READER = reader;
   }
   @Override
   public String getSentence(Integer key) {
     try {
       return CASHE_SENTENCES.get(key);
     } catch (ExecutionException e) {
       throw new FailReadSentence(e.getCause());
     }
   }
 
   private final LoadingCache<Integer, String> CASHE_SENTENCES;
   private final TextFileReader READER;
 }
 
 interface TextFileReader {
   // Получаем сразу все предложения
   ImmutableList<String> fileToSentences() throws ReadException;
 }
 
// Можно читать учитываю индекс, мы же все равно читаем
//   строку за строкой. Тогда в худшем случае считаем весь файл, но хранить его не будем.
// O(n) - по сложности и память в одну строку.

 
 public class BaseContentHolderTest {
   @Test
   public void testGetContentItem() throws Exception {
     ImmutableMap<String, List<Integer>> keys = ImmutableMap.of(
       "hello", Arrays.asList(1, 2), "hay", Arrays.asList(1));
 
     // Порядок предложений важен! Поэтому список с сохранением порядка
     // Коллекции Guava хранят порядок, но для сипска рекомендуюется использовать
     //   JDK версию данной коллекции.
     ImmutableList<String> sentences = ImmutableList.of("hello hay", "hay");
 
     // Плохо передавать предложения, лучше передать кэш. Предложений может быть много
     ContentStorageAccessor accessor = new InMemorySentencesAccessor(sentences);
     ContentHolder contentHolder = new BaseContentHolder(keys, accessor);
   }
 
   @Test
   public void testFileAccess() throws Exception {
     TextFileReader reader = mock(TextFileReader.class);
     when(reader.fileToSentences())
         .thenReturn(ImmutableList.of("one", "two"));
 
     ImmutableMap<String, List<Integer>> keys = ImmutableMap.of(
         "hello", Arrays.asList(1, 2),
         "hay", Arrays.asList(1));
 
     ContentStorageAccessor accessor = new CashedContentAccessor(reader);
     ContentHolder contentHolder = new BaseContentHolder(keys, accessor);
     contentHolder.getContentItem("hello");
   }
 
   @Test(expected = FailReadSentence.class)
   public void testExceptionOnReadFile() throws Exception {
     TextFileReader reader = mock(TextFileReader.class);
     when(reader.fileToSentences())
         .thenThrow(new ReadException());
 
     ImmutableMap<String, List<Integer>> keys = ImmutableMap.of(
         "hello", Arrays.asList(1, 2),
         "hay", Arrays.asList(1));
 
     ContentStorageAccessor accessor = new CashedContentAccessor(reader);
     ContentHolder contentHolder = new BaseContentHolder(keys, accessor);
     contentHolder.getContentItem("hello");
   }
 }
 
