 package org.n3r.sshe;
 
 import com.google.common.collect.Lists;
 import com.google.common.collect.Maps;
 import static org.apache.commons.lang3.StringUtils.*;
 
 import org.apache.commons.lang3.StringUtils;
 import org.n3r.sshe.collector.CollectorMatcher;
 import org.n3r.sshe.collector.OperationCollector;
 import org.n3r.sshe.operation.HostOperation;
 import org.n3r.sshe.parser.*;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import java.io.*;
 import java.nio.charset.Charset;
 import java.util.List;
 import java.util.Map;
 
 public class SsheConf {
     private static final Logger logger = LoggerFactory.getLogger(SsheConf.class);
 
     public static List<SsheHost> ssheHosts = Lists.newArrayList();
     public static List<CollectorMatcher> collectors = Lists.newArrayList();
     public static List<HostOperation> operations = Lists.newArrayList();
     public static Map<String, String> settings = Maps.newHashMap();
     public static SsheOutput console;
     public static String key = "UpHJVmxF2GbXz3uMkGgmHw==";
     private static int maxWaitMillis = -1;
 
     public static void parseConf(String configurationContent) throws IOException {
         BufferedReader br = new BufferedReader(new StringReader(configurationContent));
         parseConfLines(br);
     }
     public static void parseConf(File configurationFile) throws IOException {
         BufferedReader br = new BufferedReader(
                 new InputStreamReader(new FileInputStream(configurationFile), "UTF-8"));
 
         parseConfLines(br);
     }
 
     private static void parseConfLines(BufferedReader br) throws IOException {
         SectionParser sectionParser = null;
         for (String line = br.readLine(); line != null; line = br.readLine()) {
             line = trim(line);
 
             // Inore blank lines and comment lines
             if (isEmpty(line) || line.startsWith("#")) continue;
 
             if (line.startsWith("*")) { // new section
                 sectionParser = processSection(trim(line.substring(1)));
             } else if (sectionParser != null) {
                 sectionParser.parse(line);
             } else {
                 logger.warn("line {} is not recognized", line);
             }
         }
 
         br.close();
     }
 
     private static SectionParser processSection(String sectionName) {
         if ("hosts".equals(sectionName)) return new HostsParser();
         if ("operations".equals(sectionName)) return new OperationsParser();
         if ("settings".equals(sectionName)) return new SettingsParser();
         if ("collectors".equals(sectionName)) return new CollectorsParser();
 
         logger.warn("section {} was not recognized", sectionName);
 
         return null;
     }
 
     public static String getCharset() {
         String charset = settings.get(SettingKey.charset);
         return charset != null ? charset : Charset.defaultCharset().name();
     }
 
     public static void collect(OperationCollector operationCollector,
                                String fullResponse, String command) {
         for (CollectorMatcher collectorMatcher : collectors) {
             if (collectorMatcher.match(fullResponse))
                 operationCollector.add(command, fullResponse);
         }
     }
 
     public static void confirmByOp() {
         if (parseConfirmType() == ConfirmType.ByOp)
             console.waitConfirm(parseMaxWaitMilis());
     }
 
     public static void confirmByHost() {
         if (parseConfirmType() == ConfirmType.ByHost)
             console.waitConfirm(parseMaxWaitMilis());
     }
 
     public static void confirm(int maxWaitMilis) {
         console.waitConfirm(maxWaitMilis);
     }
 
 
     private static enum ConfirmType {ByOp, ByHost, None};
     private static ConfirmType parseConfirmType() {
         String confirmType = settings.get(SettingKey.confirm);
         if ("byOp".equalsIgnoreCase(confirmType)) return ConfirmType.ByOp;
         if ("byHost".equalsIgnoreCase(confirmType)) return ConfirmType.ByHost;
         if (isEmpty(confirmType) || "none".equalsIgnoreCase(confirmType)) return ConfirmType.None;
 
         logger.warn("confirm {} was not recognized, it should be byOp, byHost or none", confirmType);
 
         return ConfirmType.None;
     }
 
 
     public static int parseMaxWaitMilis() {
         if (maxWaitMillis >= 0) return maxWaitMillis;
 
         String confirmMaxWaitMillis = settings.get(SettingKey.confirmMaxWaitMillis);
        if (StringUtils.isEmpty(confirmMaxWaitMillis)) {
            maxWaitMillis = 0;
            return maxWaitMillis;
        }

         try {
             maxWaitMillis = Integer.parseInt(confirmMaxWaitMillis);
         } catch (NumberFormatException e) {
             logger.warn("confirmMaxWaitMillis {} was not recognized, it should be number",
                     maxWaitMillis);
         }
 
         return maxWaitMillis;
     }
 
 }
