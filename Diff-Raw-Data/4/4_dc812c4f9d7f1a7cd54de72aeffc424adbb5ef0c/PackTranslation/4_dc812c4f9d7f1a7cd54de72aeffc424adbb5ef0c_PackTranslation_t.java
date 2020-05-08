 import java.io.BufferedOutputStream;
 import java.io.DataOutputStream;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.nio.charset.Charset;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.TreeMap;
 import java.util.regex.Matcher;
 import java.util.zip.GZIPOutputStream;
 
 import javax.xml.bind.JAXBContext;
 
 import org.alex73.android.Assert;
 import org.omegat.core.Core;
 import org.omegat.core.data.EntryKey;
 import org.omegat.core.data.IProject;
 import org.omegat.core.data.ProjectProperties;
 import org.omegat.core.data.RealProject;
 import org.omegat.core.data.TMXEntry;
 import org.omegat.core.segmentation.Rule;
 import org.omegat.core.segmentation.Segmenter;
 import org.omegat.filters2.master.PluginUtils;
 import org.omegat.util.Language;
 import org.omegat.util.PatternConsts;
import org.omegat.util.ProjectFileStorage;
 import org.omegat.util.RuntimePreferences;
 
 import android.control.App;
 import android.control.Translation;
 
 public class PackTranslation {
     static String projectPath = "../../Android.OmegaT/Android/";
 
     static final Charset UTF8 = Charset.forName("UTF-8");
     static int countDefault, countMultiple, countOrphanedDefault, countOrphanedMultiple;
     static Map<String, String> defaults = new HashMap<String, String>(20000);
     static Map<EntryKey, String> multiples = new HashMap<EntryKey, String>(1000);
     static DataOutputStream out;
     static Map<String, String> dirToPackages = new HashMap<String, String>();
 
     static StringBuilder outstr = new StringBuilder(1000000);
     static Map<String, Integer> outstrpos = new HashMap<String, Integer>();
 
     public static void main(String[] args) throws Exception {
         RuntimePreferences.setConfigDir("../../Android.OmegaT/Android.settings/");
         Map<String, String> params = new TreeMap<String, String>();
         params.put("alternate-filename-from", "/.+.xml$");
         params.put("alternate-filename-to", "/");
         Core.initializeConsole(params);
         PluginUtils.loadPlugins(params);
 
        ProjectProperties props = ProjectFileStorage.loadProjectProperties(new File("../../Android.OmegaT/Android/"));
         RealProject project = new RealProject(props);
         project.loadProject();
 
         List<Rule> segmentationRules = Segmenter.srx.lookupRulesForLanguage(new Language("be"));
         Assert.assertTrue("Too many rules", segmentationRules.size() < 32);
 
         JAXBContext ctx = JAXBContext.newInstance(Translation.class);
         Translation translationInfo = (Translation) ctx.createUnmarshaller().unmarshal(
                 new File(projectPath + "../translation.xml"));
 
         Set<String> packages = new HashSet<String>();
         for (App app : translationInfo.getApp()) {
             packages.add(app.getPackageName());
             dirToPackages.put(app.getDirName() + '/', app.getPackageName());
         }
 
         project.iterateByDefaultTranslations(new IProject.DefaultTranslationsIterator() {
             public void iterate(String source, TMXEntry trans) {
                 if (!source.equals(trans.translation)) {
                     countDefault++;
                     defaults.put(source, trans.translation);
                 }
             }
         });
 
         project.iterateByMultipleTranslations(new IProject.MultipleTranslationsIterator() {
             public void iterate(EntryKey source, TMXEntry trans) {
                 if (!source.sourceText.equals(trans.translation)) {
                     String packageName = dirToPackages.get(source.file);
                     if (packageName != null) {// TODO: say to translator
                         countMultiple++;
                         multiples.put(source, trans.translation);
                     }
                 }
             }
         });
 
         project.iterateByOrphanedDefaultTranslations(new IProject.DefaultTranslationsIterator() {
             public void iterate(String source, TMXEntry trans) {
                 countOrphanedDefault++;
             }
         });
 
         project.iterateByOrphanedMultipleTranslations(new IProject.MultipleTranslationsIterator() {
             public void iterate(EntryKey source, TMXEntry trans) {
                 countOrphanedMultiple++;
             }
         });
 
         System.out.println("countDefault = " + countDefault);
         System.out.println("countMultiple = " + countMultiple);
         System.out.println("countOrphanedDefault = " + countOrphanedDefault);
         System.out.println("countOrphanedMultiple = " + countOrphanedMultiple);
         System.out.println("countSegmentationRules = " + segmentationRules.size());
         System.out.println("countPackages = " + packages.size());
 
         for (Map.Entry<String, String> en : defaults.entrySet()) {
             collectString(en.getKey());
             collectString(en.getValue());
         }
         for (Map.Entry<EntryKey, String> en : multiples.entrySet()) {
             String packageName = dirToPackages.get(en.getKey().file);
             collectString(en.getKey().sourceText);
             collectString(packageName);
             collectString(en.getKey().id);
             collectString(en.getValue());
         }
         for (Rule rule : segmentationRules) {
             collectString(rule.getBeforebreak());
             collectString(rule.getAfterbreak());
         }
         for (String pkg : packages) {
             collectString(pkg);
         }
 
         out = new DataOutputStream(new BufferedOutputStream(new GZIPOutputStream(new FileOutputStream(
                 "../Installer/res/raw/translation.bin"))));
 
         byte[] ostr = outstr.toString().getBytes(UTF8);
         out.writeInt(ostr.length);
         out.write(ostr);
 
         out.writeInt(defaults.size());
         for (Map.Entry<String, String> en : defaults.entrySet()) {
             writeString(en.getKey());
             writeString(en.getValue());
             collectStyledString(en.getKey(), en.getValue());
         }
         out.writeInt(multiples.size());
         for (Map.Entry<EntryKey, String> en : multiples.entrySet()) {
             String packageName = dirToPackages.get(en.getKey().file);
             writeString(packageName);
             writeString(en.getKey().id);
             writeString(en.getKey().sourceText);
             writeString(en.getValue());
             writeStyledString(en.getKey().sourceText, en.getValue());
         }
         out.writeInt(segmentationRules.size());
         for (Rule rule : segmentationRules) {
             out.writeBoolean(rule.isBreakRule());
             writeString(rule.getBeforebreak());
             writeString(rule.getAfterbreak());
         }
         out.writeInt(packages.size());
         for (String pkg : packages) {
             writeString(pkg);
         }
 
         out.close();
 
         System.exit(0);
     }
 
     static void collectString(String str) throws Exception {
         if (str == null) {
             throw new Exception("Empty string");
         }
         if (outstrpos.containsKey(str)) {
             return;
         }
         outstrpos.put(str, outstr.length());
         outstr.append(str);
     }
 
     static void writeString(String str) throws Exception {
         if (str == null) {
             // TODO check filename for multiple
             throw new Exception("Empty string");
         }
         int pos = outstrpos.get(str);
         int len = str.length();
         out.writeInt(pos);
         out.writeInt(len);
     }
 
     static void collectStyledString(String source, String translation) {
         // Map<String, BegEnd> sourceTags = extractShortTags(source);
         // Map<String, BegEnd> translationTags = extractShortTags(translation);
         // Assert.assertEquals("Wrong tags", sourceTags.size(), translationTags.size());
     }
 
     static void writeStyledString(String source, String translation) {
         // Map<String, BegEnd> tags = extractShortTags(source);
     }
 
     private static String extractedString;
 
     private static Map<String, BegEnd> extractShortTags(String str) {
         Map<String, BegEnd> tags = new TreeMap<String, BegEnd>();
         while (true) {
             Matcher m = PatternConsts.OMEGAT_TAG.matcher(str);
             if (!m.find()) {
                 break;
             }
             String tag = m.group();
             if (tag.startsWith("</")) {
                 // end tag
                 tag = tag.substring(2, tag.length() - 1);
                 BegEnd be = tags.get(tag);
                 Assert.assertNotNull("Tag not exist", be);
                 be.end = m.start();
             } else if (tag.endsWith("/>")) {
                 // one tag
                 tag = tag.substring(1, tag.length() - 2);
                 Assert.assertNull("Tag already exist", tags.get(tag));
                 BegEnd be = new BegEnd();
                 be.beg = m.start();
                 be.end = m.start();
                 tags.put(tag, be);
             } else {
                 // start tag
                 tag = tag.substring(1, tag.length() - 1);
                 Assert.assertNull("Tag already exist", tags.get(tag));
                 BegEnd be = new BegEnd();
                 be.beg = m.start();
                 tags.put(tag, be);
             }
             str = str.substring(0, m.start()) + str.substring(m.end());
         }
         extractedString = str;
         return tags;
     }
 
     public static class BegEnd {
         int beg = -1, end = -1;
     }
 }
