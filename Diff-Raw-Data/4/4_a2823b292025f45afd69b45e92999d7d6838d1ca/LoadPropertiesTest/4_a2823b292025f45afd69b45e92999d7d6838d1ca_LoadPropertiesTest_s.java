 package ca.cutterslade.utilities;
 
 import java.io.IOException;
 import java.util.Collection;
 
 import junit.framework.Assert;
 
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.junit.runners.Parameterized;
 import org.junit.runners.Parameterized.Parameters;
 
 import com.google.common.base.Function;
 import com.google.common.collect.ImmutableList;
 import com.google.common.collect.ImmutableMap;
 import com.google.common.collect.Iterables;
 
 @RunWith(Parameterized.class)
 public class LoadPropertiesTest {
 
   private static final ImmutableList<ImmutableList<Object>> PARAMETERS;
   static {
     final ImmutableList.Builder<ImmutableList<Object>> builder = ImmutableList.<ImmutableList<Object>> builder();
     ImmutableList.Builder<Object> subBuilder;
 
     subBuilder = ImmutableList.builder();
     subBuilder.add("empty.properties");
     subBuilder.add(ImmutableMap.of());
     subBuilder.add(ImmutableMap.of());
     builder.add(subBuilder.build());
 
     subBuilder = ImmutableList.builder();
     subBuilder.add("simple.properties");
     final ImmutableMap<String, String> simpleValues = ImmutableMap.of("one", "1", "two", "2", "three", "3");
     subBuilder.add(simpleValues);
     subBuilder.add(simpleValues);
     builder.add(subBuilder.build());
 
     subBuilder = ImmutableList.builder();
     subBuilder.add("replace.properties");
     final ImmutableMap<String, String> frenchUnreplaced = ImmutableMap.<String, String> builder()
         .putAll(simpleValues).put("un", "${one}").put("deux", "${two}").put("trois", "${three}")
         .build();
     subBuilder.add(frenchUnreplaced);
     final ImmutableMap<String, String> frenchReplaced = ImmutableMap.<String, String> builder()
         .putAll(simpleValues).put("un", "1").put("deux", "2").put("trois", "3")
         .build();
     subBuilder.add(frenchReplaced);
     builder.add(subBuilder.build());
 
     subBuilder = ImmutableList.builder();
     subBuilder.add("multi-replace.properties");
     final ImmutableMap<String, String> spanishUnreplaced = ImmutableMap.<String, String> builder()
         .putAll(frenchUnreplaced).put("uno", "${un}").put("dos", "${deux}").put("tres", "${trois}")
         .build();
     subBuilder.add(spanishUnreplaced);
     final ImmutableMap<String, String> spanishReplaced = ImmutableMap.<String, String> builder()
         .putAll(frenchReplaced).put("uno", "1").put("dos", "2").put("tres", "3")
         .build();
     subBuilder.add(spanishReplaced);
     builder.add(subBuilder.build());
 
     subBuilder = ImmutableList.builder();
     subBuilder.add("concat.properties");
     final ImmutableMap<String, String> countUnreplaced = ImmutableMap.<String, String> builder()
         .putAll(simpleValues).put("count", "${one} ${two} ${three}")
         .build();
     subBuilder.add(countUnreplaced);
     final ImmutableMap<String, String> countReplaced = ImmutableMap.<String, String> builder()
         .putAll(simpleValues).put("count", "1 2 3")
         .build();
     subBuilder.add(countReplaced);
     builder.add(subBuilder.build());
 
     subBuilder = ImmutableList.builder();
     subBuilder.add("no-replacement.properties");
     final ImmutableMap<String, String> noReplacementUnreplaced = ImmutableMap.<String, String> builder()
         .putAll(frenchUnreplaced).put("quatre", "${four}")
         .build();
     subBuilder.add(noReplacementUnreplaced);
     final ImmutableMap<String, String> noReplacementReplaced = ImmutableMap.<String, String> builder()
         .putAll(frenchReplaced).put("quatre", "${four}")
         .build();
     subBuilder.add(noReplacementReplaced);
     builder.add(subBuilder.build());
 
     subBuilder = ImmutableList.builder();
     subBuilder.add("unused-defaults.properties");
     final ImmutableMap<String, String> unusedDefaultsUnreplaced = ImmutableMap.<String, String> builder()
         .putAll(simpleValues).put("un", "${one:?}").put("deux", "${two:?}").put("trois", "${three:?}")
         .build();
     subBuilder.add(unusedDefaultsUnreplaced);
     subBuilder.add(frenchReplaced);
     builder.add(subBuilder.build());
 
     subBuilder = ImmutableList.builder();
     subBuilder.add("defaults.properties");
     final ImmutableMap<String, String> defaultsUnreplaced = ImmutableMap.<String, String> builder()
         .putAll(unusedDefaultsUnreplaced).put("quatre", "${four:?}")
         .build();
     subBuilder.add(defaultsUnreplaced);
     final ImmutableMap<String, String> defautlsReplaced = ImmutableMap.<String, String> builder()
         .putAll(frenchReplaced).put("quatre", "?")
         .build();
     subBuilder.add(defautlsReplaced);
     builder.add(subBuilder.build());
 
     subBuilder = ImmutableList.builder();
     subBuilder.add("echo.properties");
     final ImmutableMap<String, String> echoUnreplaced = ImmutableMap.<String, String> builder()
         .putAll(simpleValues).put("count", "${$echo(one,two,three)}")
         .build();
     subBuilder.add(echoUnreplaced);
     subBuilder.add(countReplaced);
     builder.add(subBuilder.build());
 
     subBuilder = ImmutableList.builder();
     subBuilder.add("echo-defaults.properties");
     final ImmutableMap<String, String> echoDefaultsUnreplaced = ImmutableMap.<String, String> builder()
         .putAll(simpleValues).put("count", "${$echo(one,two,three,four:?)}")
         .build();
     subBuilder.add(echoDefaultsUnreplaced);
     final ImmutableMap<String, String> echoDefaultsReplaced = ImmutableMap.<String, String> builder()
         .putAll(simpleValues).put("count", "1 2 3 ?")
         .build();
     subBuilder.add(echoDefaultsReplaced);
     builder.add(subBuilder.build());
 
     subBuilder = ImmutableList.builder();
     subBuilder.add("pangram.properties");
     final ImmutableMap<String, String> pangramUnreplaced = ImmutableMap.<String, String> builder()
         .put("phrase", "${$read('fox.txt')}")
         .build();
     subBuilder.add(pangramUnreplaced);
     final ImmutableMap<String, String> pangramReplaced = ImmutableMap.<String, String> builder()
         .put("phrase", "The quick brown fox jumps over the lazy dog.")
         .build();
     subBuilder.add(pangramReplaced);
     builder.add(subBuilder.build());
 
     PARAMETERS = builder.build();
   }
 
   @Parameters
   public static Collection<Object[]> getParameters() {
     return ImmutableList.copyOf(Iterables.transform(PARAMETERS, new Function<ImmutableList<Object>, Object[]>() {
 
       @Override
       public Object[] apply(final ImmutableList<Object> input) {
        return input.toArray();
       }
 
     }));
   }
 
   private final String resource;
 
   private final ImmutableMap<String, String> loaded;
 
   private final ImmutableMap<String, String> resolved;
 
   public LoadPropertiesTest(final String resource, final ImmutableMap<String, String> loaded,
       final ImmutableMap<String, String> resolved) {
     this.resource = resource;
     this.loaded = loaded;
     this.resolved = resolved;
   }
 
   @Test
   public void testLoad() throws IOException {
     Assert.assertEquals(loaded, PropertiesUtils.loadProperties(resource));
   }
 
   @Test
   public void testResolve() throws IOException {
     Assert.assertEquals(resolved, PropertiesUtils.resolveProperties(PropertiesUtils.loadProperties(resource)));
   }
 }
