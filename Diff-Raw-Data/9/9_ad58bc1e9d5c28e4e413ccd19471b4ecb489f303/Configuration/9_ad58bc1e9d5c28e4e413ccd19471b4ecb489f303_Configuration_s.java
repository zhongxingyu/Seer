 package ep.geoschem;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 
 import com.fasterxml.jackson.annotation.JsonIgnore;
 import com.fasterxml.jackson.annotation.JsonInclude;
 import com.fasterxml.jackson.annotation.JsonProperty;
 import com.fasterxml.jackson.annotation.JsonPropertyOrder;
 import com.fasterxml.jackson.databind.JavaType;
 import com.fasterxml.jackson.databind.MappingIterator;
 import com.fasterxml.jackson.databind.ObjectMapper;
 import com.fasterxml.jackson.dataformat.csv.CsvMapper;
 import com.fasterxml.jackson.dataformat.csv.CsvSchema;
 import com.google.common.base.Splitter;
 import com.google.common.base.Strings;
 import com.google.common.collect.Iterables;
 import ep.common.EmissionSource;
 import ep.common.EmissionSourceConfig;
 import ep.common.FileSystemEmissionSource;
 import ep.common.FileSystemEmissionSourceConfig;
 import ucar.ma2.InvalidRangeException;
 
 
 @JsonPropertyOrder({
   "root", "conf", "emissions", "species", "sectors",
   "esconf", "beginYear", "endYear", "yearmap"})
 @JsonInclude(JsonInclude.Include.NON_NULL)
 public class Configuration {
   public String root;
   public String conf;
   public String defaultEmission;
 
   // @JsonIgnore
   public String species[];
   // @JsonIgnore
   public String sectors[];
   // @JsonIgnore
   public String emissions[];
   @JsonIgnore public Map<String, EmissionSource> emissionSources;
   //@JsonIgnore
   public Map<String, SectorTable> sectorMapper;
   //@JsonIgnore
   @JsonProperty("yearmap")
   public Map<String, String> yearIndex;
   //@JsonIgnore
   public int beginYear;
   //@JsonIgnore
   public int endYear;
 
   public List<Target> targets;
 
   @JsonProperty("esconf")
   public Map<String, EmissionSourceConfig> emissionConfigs;
 
   public static class SectorTable {
     public Map<String, String[]> sectors;
   }
 
 
   void initEmissionSources() throws IOException, InvalidRangeException {
     emissionSources = new HashMap<String, EmissionSource>();
     emissions = Iterables.toArray(emissionConfigs.keySet(), String.class);
 
     Arrays.sort(emissions);
     emissionSources = new HashMap<String, EmissionSource>();
     emissions = Iterables.toArray(emissionConfigs.keySet(), String.class);
 
     Arrays.sort(emissions);
 
     for (Map.Entry<String, EmissionSourceConfig> e : emissionConfigs.entrySet()) {
       EmissionSource es = null;
       EmissionSourceConfig esc = e.getValue();
 
       if (esc instanceof FileSystemEmissionSourceConfig) {
         es = new FileSystemEmissionSource((FileSystemEmissionSourceConfig) esc);
       }
 
       if (es != null) {
         emissionSources.put(e.getKey(), es);
       }
     }
   }
 
 
   void initSectorMapper() throws IOException {
     HashSet species = new HashSet();
     HashSet sectors = new HashSet();
 
     sectorMapper = new HashMap<String, SectorTable>();
     CsvSchema schema = CsvSchema.emptySchema().withHeader();
     ObjectMapper mapper = new CsvMapper();
 
     MappingIterator<Map> it = mapper.reader(Map.class).
       with(schema).readValues(new File(conf, "sector_map.csv"));
 
     Splitter splitter = Splitter.on(',').trimResults();
 
     while (it.hasNext()) {
       Map<String, String> row = (Map<String, String>) it.next();
       String sp = row.get("SPECIES");
       String es = row.get("SOURCE");
       String st = row.get("SECTOR");
       String ss = row.get("SOURCESECTORS");
 
       if (Strings.isNullOrEmpty(sp))
         continue;
       if (Strings.isNullOrEmpty(ss))
         continue;
 
       species.add(sp);
       sectors.add(st);
 
       String key = sp + "," + st;
       SectorTable sm = sectorMapper.get(key);
       if (sm == null) {
         sm = new SectorTable();
         sm.sectors = new HashMap<String, String[]>();
         sectorMapper.put(key, sm);
       }
 
       String[] s = Iterables.toArray(splitter.split(ss), String.class);
       sm.sectors.put(es, s);
     }
 
     this.sectors = Iterables.toArray(sectors, String.class);
     this.species = Iterables.toArray(species, String.class);
   }
 
 
   void initYearIndex() throws IOException {
     yearIndex = new HashMap<String,String>();
 
     CsvSchema schema = CsvSchema.emptySchema().withHeader();
     ObjectMapper mapper = new CsvMapper();
 
     MappingIterator<Map> it = mapper.reader(Map.class).
       with(schema).readValues(new File(conf, "year_source.csv"));
 
     beginYear = 99999;
     endYear = 1;
 
     while(it.hasNext()) {
 
       Map<String, String> row = (Map<String, String>)it.next();
       String year = row.get("YEAR");
       int nYear = Integer.parseInt(year);
       if (nYear > endYear) {
         endYear = nYear;
       }
 
       if (nYear < beginYear) {
         beginYear = nYear;
       }
 
       for (String es: emissions) {
         String k = row.get(es);
         if (k == null || k.equals("0"))
           continue;
 
         String key = es + "," + year;
         yearIndex.put(key, year);
       }
     }
 
     for (String es: emissions) {
       for (int y = beginYear; y <= endYear; ++y) {
         String year = Integer.toString(y);
         String key = es + "," + year;
 
 
         if (yearIndex.get(key) == null) {
           String tYear = null;
           String iYear;
           String sKey;
           String sYear;
 
           // scan forward
           for (int sy = y + 1; sy <= endYear; ++sy) {
             sYear = Integer.toString(sy);
             sKey = es + "," + sYear;
             iYear = yearIndex.get(sKey);
             if (iYear != null) {
               tYear = iYear;
               break;
             }
           }
 
           if (tYear != null) {
             yearIndex.put(key, tYear);
             continue;
           }
 
           for (int sy = y - 1; sy >= beginYear; --sy) {
             sYear = Integer.toString(sy);
             sKey = es + "," + sYear;
             iYear = yearIndex.get(sKey);
             if (iYear != null) {
               tYear = iYear;
               break;
             }
           }
 
           yearIndex.put(key, tYear);
         }
       }
     }
   }
 
   public void init() throws IOException, InvalidRangeException {
     initEmissionSources();
     initSectorMapper();
     initYearIndex();
   }
 
   public EmissionSource getEmissionSource(String emission) {
     return emissionSources.get(emission);
   }
 
 
   public String getYearIndex(String emission, String year) {
     return yearIndex.get(emission + "," + year);
   }
 
   public String[] getSourceSectors(String species, String sector, String emissionSource) {
    return sectorMapper.get(species + "," + sector).sectors.get(emissionSource);
   }
 
   public void loadTargetConfig() throws IOException {
     ObjectMapper mapper = new ObjectMapper();
     JavaType type = mapper.getTypeFactory().constructCollectionType(List.class, Target.class);
     targets = mapper.readValue(new File("cfg.target.js"), type);
     for (Target t: targets) {
       t.init();
     }
   }
 }
