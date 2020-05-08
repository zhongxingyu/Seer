 package cz.cuni.mff.xrg.intlib.extractor.legislation.decisions.uriGenerator.link;
 
 import cz.cuni.mff.xrg.intlib.extractor.legislation.decisions.UriGenerator;
 import java.text.MessageFormat;
 import java.util.*;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 import org.slf4j.LoggerFactory;
 import org.w3c.dom.*;
 
 /**
  * On class to represent any document
  * 
  * @author Jakub Starka
  */
 public class Work implements Comparable<Work> {
     
     private static final org.slf4j.Logger log = LoggerFactory.getLogger(
             Work.class);
     
     // Label to separate section
     public static final String EMPTY_SECTION_LABEL = "-";
 
     // Unrecognized links
     public static int unrecognized = 0;
     // Recognized links
     public static int recognized = 0;
     // Recognized links
     public static int institutions = 0;
     // Nedostatecne links
     public static int nedostatecne = 0;
     // Missing values in links
     public static int missing = 0;
     
     public static int lastShortcutUsed = 0;
     public static int lastNumberUsed = 0;
     public static int startShortTrimmed = 0;
     public static int endShortTrimmed = 0;
     public static int expressionEndTrimmed = 0;
     public static int az_expanded = 0;
     
     public static String lastFoundShortcut = null;
     public static int lastFoundYear = 0;
     public static String lastFoundNumber = null;
 
     private static String [] startShortcutTrimmings = new String [] 
         {"části věty před středníkem", 
     	 "části věty za středníkem",
          "část věty za středníkem", 
          "část věty před středníkem",
          "část první věty za středníkem", 
          "věty před středníkem", 
          "věty za středníkem", 
          "a násl.", 
          "druhá věta"};
     private static String [] endShortcutTrimmings = new String [] 
         {" v", 
          " ).", 
          "/.", 
          " s", 
          " o", 
          " s e", 
          " )", 
          "a b", 
          " ) a", 
          "téhož zákona",
          "zákona",
          "cit. zákona", 
          "cit. zák.",
          "cit. vyhl.",
          "druhý odstavec",
          "citovaného předpisu"
     };
 
     
     // Document attributes
     protected String countryOfIssue;
     protected WorkType typeOfWork;
     protected Integer year;
     protected String number;
     protected String section;
     protected String subsection;
     protected String paraNumber;
     protected String subparaNumber;
     protected String source;
     protected String version;
     protected String language;
     
     // Validity tree for this document    
     protected TreeSet<Date> validity = new TreeSet<>();
     
     // -------------------------------------------------------------------------
     // Getters
     
     private static String getCountry(String group) {
         return group;
     }
     
     private static String getNumber(String group) {
         group = group.replaceAll("ž", "z")
                 .replaceAll("š", "s")
                 .replaceAll("č", "c")
                 .replaceAll("ř", "r")
                 .replaceAll("ď", "d")
                 .replaceAll("ť", "t")
                 .replaceAll("ň", "n")
                 .replaceAll("á", "a")
                 .replaceAll("[ěé]", "e")
                 .replaceAll("í", "i")
                 .replaceAll("ó", "o")
                 .replaceAll("[úů]", "u")
                 .replaceAll("ý", "y")
                 .replaceAll(" ", "-")
                 .replaceAll("/", "-")
                 .replaceAll("-[-]+", "-")
                 .replaceAll("[^0-9a-z -]", "")
                 .replaceAll("-$", "");
         return group;
     }
     
     protected static WorkType getWorkType(String group) {
         return WorkType.valueOf(group);
     }
     
     private static Integer getYear(String group) {
         int year = Integer.parseInt(group);
         if (year < 50) {
             year += 2000;
         } else if (year < 100) {
             year += 1900;
         }
         return year;
     }
     
     private static String getSection(String group) {
         return group;
     }
     
     private static String getSubsection(String group) {
         return group;
     }
     
     private static String getPara(String group) {
         return group;
     }
     
     private static String getSubpara(String group) {
         return group;
     }
     
     
     public Date getLastDate(Date current) {
         return validity.floor(current);
     }
     
     public String getCountryOfIssue() {
         return countryOfIssue;
     }
 
     public void setCountryOfIssue(String countryOfIssue) {
         this.countryOfIssue = countryOfIssue;
     }
 
     public String getNumber() {
         return number;
     }
 
     public void setNumber(String number) {
         this.number = number;
     }
 
     public String getParaNumber() {
         return paraNumber;
     }
     
     
     public void setParaNumber(String paraNumber) {
         this.paraNumber = paraNumber;
     }
 
     public String getSection() {
         return section;
     }
 
     public void setSection(String section) {
         this.section = this.clean(section);
     }
 
     public String getSubsection() {
         return subsection;
     }
 
     public void setSubsection(String subsection) {
         this.subsection = subsection;
     }
 
     public WorkType getTypeOfWork() {
         return typeOfWork;
     }
 
     public void setTypeOfWork(WorkType typeOfWork) {
         this.typeOfWork = typeOfWork;
     }
 
     public Integer getYear() {
         return year;
     }
 
     public void setYear(Integer year) {
         this.year = year;
     }
     
     public void setLanguage(String language) {
         this.language = language;
     }
 
     public void setVersion(String version) {
         this.version = version;
     }
     
     // -------------------------------------------------------------------------
     // URI String generators
     
     /**
      * Generates URI of this document.
      * 
      * @return String containing URI of this document.
      */
     @Override
     public String toString() {
         String formatString = createFormatString();
         if (formatString == null) {
             return null;
         }
          log.info("URIFormat is: " + MessageFormat.format(formatString, countryOfIssue, typeOfWork, year, number, section, subsection, paraNumber, subparaNumber));
         return MessageFormat.format(formatString, countryOfIssue, typeOfWork, year, number, section, subsection, paraNumber, subparaNumber);
        
     }
     
     /**
      * Generates relative URI of this document.
      * 
      * @return 
      */
     public String toRelativeString() {
         return MessageFormat.format(createRelativeFormatString(), section, subsection, paraNumber, subparaNumber);
     }
     
     /**
      * Generates expression URI of this document.
      * 
      * @return 
      */    
     public String toExpressionString() {
         log.info("URIFormat is: " + MessageFormat.format(createExpressionFormatString(), countryOfIssue, typeOfWork, year, number, language, version, section, subsection, paraNumber, subparaNumber));
 
         return MessageFormat.format(createExpressionFormatString(), countryOfIssue, typeOfWork, year, number, language, version, section, subsection, paraNumber, subparaNumber);
     }
     
     /**
      * Generates format string based on non-empty attributes.
      * 
      * @return 
      */
     public String createFormatString() {
         
         //TODO for decisions, small letter are sometimes used: 2011/18-co-567-2011
 
         StringBuilder uriFormat;
         
         if (typeOfWork == WorkType.DECISION || typeOfWork == WorkType.JUDGMENT ) {
             uriFormat = new StringBuilder(Configuration.getPrefixMap().get("work") + "{0}/{1}/{2,number,#}/{3}");
         }
         else {
             uriFormat = new StringBuilder(Configuration.getPrefixMap().get("work") + "{0}/{1}/{2,number,#}/{3}-{2,number,#}");
         }
        
         
         //StringBuilder uriFormat = new StringBuilder(Configuration.getPrefixMap().get("work") + "{0}/{1}/{2,number,#}/{3}");
         if (countryOfIssue == null || typeOfWork == null || year == null || number == null) {
             Logger.getLogger("IntLib").log(Level.WARNING, "Nedostatecne udaje pro vytvoreni odkazu: ''{0}''", source);
             Work.nedostatecne++;
             return null;
         }
         
         if (section != null) {
             uriFormat.append("/section/{4}");
         }
         
         if (subsection != null) {
             uriFormat.append("/{5}");
         }
         
         if (paraNumber != null) {
             uriFormat.append("/{6}");
         }
         
         if (subparaNumber != null) {
             uriFormat.append("/{7}");
         }
         
         return uriFormat.toString();
     }
     
     /**
      * Generates expression format string based on non-empty attributes.
      * 
      * @return 
      */
     public String createExpressionFormatString() {
         
         //TODO for decisions, small letter are sometimes used: 2011/18-co-567-2011
         StringBuilder uriFormat;
          if (typeOfWork == WorkType.DECISION || typeOfWork == WorkType.JUDGMENT ) {
             uriFormat = new StringBuilder(Configuration.getPrefixMap().get("expression") + "{0}/{1}/{2,number,#}/{3}/version/{4}/{5}");
         }
         else {
             //sample: http://linked.opendata.cz/resource/legislation/cz/expression/2006/137-2006
             //http://linked.opendata.cz/resource/legislation/cz/expression/2006/137-2006/version/cz/328-2006 (expression novelizovane predpisem 328/2006)
             //TODO first expression should be without /version/cz/xx
              //TODO version/cz/number of expression novelizing it (not date)
             uriFormat = new StringBuilder(Configuration.getPrefixMap().get("expression") + "{0}/expression/{2,number,#}/{3}-{2,number,#}/version/{4}/{5}");
         } 
        
         
         //StringBuilder uriFormat = new StringBuilder(Configuration.getPrefixMap().get("expression") + "{0}/{1}/{2,number,#}/{3}/version/{4}/{5}");
         if (countryOfIssue == null || typeOfWork == null || year == null || number == null) {
             Logger.getLogger("IntLib").log(Level.INFO, "Nedostatecne udaje pro vytvoreni odkazu: '" + source + "'");
             Work.nedostatecne++;
             return null;
         }
         
         if (section != null) {
             uriFormat.append("/section/{6}");
         }
         
         if (subsection != null) {
             uriFormat.append("/{7}");
         }
         
         if (paraNumber != null) {
             uriFormat.append("/{8}");
         }
         
         if (subparaNumber != null) {
             uriFormat.append("/{9}");
         }
         
         return uriFormat.toString();
     }
     
     /**
      * Generates relative format string based on non-empty attributes.
      * 
      * @return 
      */
     public String createRelativeFormatString() {
         StringBuilder uriFormat = new StringBuilder("");
         
         if (section != null) {
             uriFormat.append("/section/{0}");
         }
         
         if (subsection != null) {
             uriFormat.append("/{1}");
         }
         
         if (paraNumber != null) {
             uriFormat.append("/{2}");
         }
         
         
         if (subparaNumber != null) {
             uriFormat.append("/{3}");
         }
         
         return uriFormat.toString();
     }
     
     // -------------------------------------------------------------------------
     // Constructors
     
     protected Work() {}
     
     public Work(
             String source, 
             String countryOfIssue, 
             WorkType typeOfWork, 
             Integer year, 
             String number, 
             String section, 
             String subsection, 
             String paraNumber,
             String subparaNumber,
             String version,
             String language
     ) {
         this.setValues(source, countryOfIssue, typeOfWork, year, number, section, subsection, paraNumber, subparaNumber, version, language);        
     }
     
     public void setValues(
             String source, 
             String countryOfIssue, 
             WorkType typeOfWork, 
             Integer year, 
             String number, 
             String section, 
             String subsection, 
             String paraNumber,
             String subparaNumber,
             String version,
             String language
     ) {
         this.source = source;
         this.countryOfIssue = countryOfIssue;
         this.typeOfWork = typeOfWork;
         this.year = year;
         this.number = number;
         this.section = clean(section);
         this.subsection = clean(subsection);
         this.paraNumber = clean(paraNumber);
         this.subparaNumber = clean(subparaNumber);
         
         this.version = version;
         this.language = language;
     }
     
     // ------------------------------------------------------------------------- 
     // Factory methods
     
     /**
      * Create new list of works based on the expression string and Work type
      * 
      * @param expression
      * @param type
      * @return 
      */
     public static LinkedList<Work> parse(String expression, WorkType type, LawDocument law) {
         return parse(expression, type, law, null);
     }
     
     private static Work parseSingle(
             String expression, 
             WorkType type, 
             LawDocument law, 
             Integer referedId, 
             String country, 
             Integer year, 
             String number, 
             String section, 
             String subsection, 
             String para, 
             String subpara, 
             String shortcut)
     {
         Work w = new Work(
                 expression,
                 country, 
                 type,
                 year, 
                 number, 
                 section, 
                 subsection, 
                 para,
                 subpara,
                 null,
                 null
         );
 
         // apply refers_to attribute if available
         if (referedId != null) {
             Work referal = law.getAbbreviationMap().getWork(referedId);
             if (referal != null) {
                 w.apply(referal);
             }
         }
 
         try {
 
             if (!( shortcut == null || shortcut.isEmpty())) {
                 if (shortcut.contains(")") && shortcut.indexOf(")") > 0) shortcut = shortcut.substring(0, shortcut.indexOf(")"));
                 if (Configuration.getAliasMap().containsKey(shortcut)) {
                     // use shortcuts in configuration file                          
                     Configuration.getAliasMap().get(shortcut).apply(w);
                 } else if (Configuration.getAliasMap().containsKey(shortcut.substring(0, shortcut.length() - 1))) {
                     // use shortcuts in configuration file with normalization
                     Configuration.getAliasMap().get(shortcut.substring(0, shortcut.length() - 1)).apply(w);
                 } else {
                     Work abbreviationWork = law.getAbbreviationMap().getWork(shortcut);
                     if (abbreviationWork != null) {
                         Logger.getLogger(Work.class.getName()).log(Level.INFO, "Abreviated shortcut used: {0} ({1}/{2})", new Object[]{shortcut, abbreviationWork.getNumber(), abbreviationWork.getYear()});
                         w.apply(abbreviationWork);
                     } else {
 
                         // if we did not find the shortcut in explicit map, try to find it in the names map
                         Work shortcutWork = law.getCustomShortcuts().get(shortcut);
                         if (shortcutWork != null) {
                             //Logger.getLogger(Work.class.getName()).log(Level.INFO, "Custom shortcut used: {0} ({1}/{2})", new Object[]{shortcut, shortcutWork.getNumber(), shortcutWork.getYear()});
                             w.apply(shortcutWork);
                         } else {
                             // if we did not find the shortcut in explicit map, try to find it in the names map
                             shortcutWork = law.getGenericShortcuts().get(shortcut);
                             if (shortcutWork != null) {
                                 //Logger.getLogger(Work.class.getName()).log(Level.INFO, "Generic shortcut used: {0} ({1}/{2})", new Object[]{shortcut, shortcutWork.getNumber(), shortcutWork.getYear()});
                                 w.apply(shortcutWork);
                             }
                         } 
                     }
                 }
             }
         } catch (IllegalArgumentException ex) {
         }
         return w;
     }
     
     private static Integer fixYear(String yearstr)
     {
         Integer year;
         try {
             year = Integer.parseInt(yearstr);
             
         } catch (Exception e) { 
             Logger.getLogger("intlib").log(Level.WARNING, "Year \"{0}\" cannot be parsed", yearstr);
             return 0;}
         if (year < 100)
         {
             if (year < 30) year += 2000;
             else year += 1900;
         }
 
         return year;
     }
     
     /**
      * Create new list of works based on the expression string, Work type and optional referred Work
      * 
      * @param expression
      * @param type
      * @param referedId
      * @return 
      */
     public static LinkedList<Work> parse(String expression, WorkType type, LawDocument law, Integer referedId) {
         // results
         LinkedList<Work> result = new LinkedList<>();
         String originalExpression = expression;
         expression = expression.toLowerCase();
         
         String country = null;
         country = "cz";
         Integer year = null;
         String number = null;
         String section = null;
         String subsection = null;
         String para = null;
         String subpara = null;
         String shortcut = null;
         
         //START OF STRING PARSING
         //Logger.getLogger("intlib").log(Level.INFO, "Parsing expression: \"{0}\"", expression);
         if (expression.contains("§"))
         {
             //Has to be Act reference
             //Logger.getLogger("intlib").log(Level.INFO, "\"{0}\" identified as an act reference: contains a §", expression);
             expression = expression.replaceAll("§§", "§");
             for (String replacement : endShortcutTrimmings)
             {
                 if (expression.endsWith(replacement))
                 {
                     //Logger.getLogger("intlib").log(Level.INFO, "Trimming expression (end): \"{0}\"", replacement);
                     expression = expression.substring(0, expression.length() - replacement.length());
                     expressionEndTrimmed++;
                     break;
                 }
             }
             
             String shortcutRegex = ".*[0-9\\)\\\\/][^ ]* ?([^0-9\\)\\\\/]*)";
             String sectionsSubstring ;
             String shortcutCandidate = expression.replaceAll(shortcutRegex, "$1").trim();
             if (expression.matches(shortcutRegex) && !shortcutCandidate.isEmpty())
             {
                 shortcut = shortcutCandidate;
                 
                 for (String replacement : startShortcutTrimmings)
                 {
                     if (shortcut.startsWith(replacement))
                     {
                         //Logger.getLogger("intlib").log(Level.INFO, "Trimming shortcut (start): \"{0}\"", replacement);
                         shortcut = shortcut.substring(replacement.length());
                         startShortTrimmed++;
                         break;
                     }
                 }
                 for (String replacement : endShortcutTrimmings)
                 {
                     if (shortcut.endsWith(replacement))
                     {
                         //Logger.getLogger("intlib").log(Level.INFO, "Trimming shortcut (end): \"{0}\"", replacement);
                         shortcut = shortcut.substring(0, shortcut.length() - replacement.length());
                         endShortTrimmed++;
                         break;
                     }
                 }
                 if (shortcut.matches("vět[aouy]u? .*"))
                 {
                     shortcut = shortcut.replaceAll("vět[oauy]u? [^ ]+ (.*)", "$1");
                     //Logger.getLogger("intlib").log(Level.INFO, "\"Věta\" not supported, trimmed.", shortcut);
                 }
                 
                 if (shortcut.matches("[Ss]b\\."))
                 {
                     //Logger.getLogger("intlib").log(Level.INFO, expression);
                     sectionsSubstring = expression.replaceAll("(.*) [^ ]+ č\\.?.*","$1");
                     try {
                         String actref;
                         if (expression.lastIndexOf("č") == -1)
                         {
 
                             actref = expression.substring(0,expression.lastIndexOf("zák"));
 
                         }
                         else actref = expression.substring(expression.lastIndexOf("č"));
                         number = actref.replaceAll("[^0-9]*([0-9]+)/([0-9]+)[sS]?b?\\.?[^0-9]*","$1");
                         year = fixYear(actref.replaceAll("[^/]*([0-9]+)/([0-9]+)[^0-9]*","$2"));
                         shortcut = null;
                         lastFoundShortcut = null;
                         lastFoundYear = year;
                         lastFoundNumber = number;
                      } catch(StringIndexOutOfBoundsException e) {
                             log.warn(e.getLocalizedMessage());
                      }
                 }
                 else
                 {
                     //TODO: Add shortuct start trimmings delete
                 	sectionsSubstring = expression.substring(0, expression.lastIndexOf(shortcut)).trim();
                     if (!shortcut.isEmpty())
                     {
                         lastFoundShortcut = shortcut;
                         lastFoundYear = 0;
                         lastFoundNumber = null;
                     }
                 }
             }
             else if (expression.matches(".*[Ss]b\\."))
             {
                 //Logger.getLogger("intlib").log(Level.INFO, expression);
                 sectionsSubstring = expression.replaceAll("(.*) [^ ]+ č\\.?.*","$1");
                 String actref;
                 if (expression.lastIndexOf("č") == -1)
                 {
                     actref = expression.substring(0,expression.lastIndexOf("zák"));
                 }
                 else actref = expression.substring(expression.lastIndexOf("č"));
                 number = actref.replaceAll("[^0-9]*([0-9]+)/([0-9]+)[sS]?b?\\.?[^0-9]*","$1");
                 year = fixYear(actref.replaceAll("[^/]*([0-9]+)/([0-9]+)[^0-9]*","$2"));
                 shortcut = null;
                 lastFoundShortcut = null;
                 lastFoundYear = year;
                 lastFoundNumber = number;
             }
             else
             {
                 sectionsSubstring = expression;
                 if (lastFoundShortcut != null)
                 {
                     shortcut = lastFoundShortcut;
                     lastShortcutUsed++;
                     //Logger.getLogger("intlib").log(Level.INFO, "Shortcut not found. Using last found shortcut: \"{0}\" for expression: \"{1}\"", new Object[]{shortcut, expression});
                 }
                 else if (lastFoundYear > 0)
                 {
                     year = lastFoundYear;
                     number = lastFoundNumber;
                     lastNumberUsed++;
                     //Logger.getLogger("intlib").log(Level.INFO, "Shortcut not found. Using last found number and year: \"{0}\" for expression: \"{1}\"", new Object[]{number + "/" + year, expression});
                 }
                 
             }
             
             //Logger.getLogger("intlib").log(Level.INFO, "\"{0}\" identified as \"shortcut\"", shortcut);
             int lastParaIndex = -1;
             do
             {
                 subsection = null;
                 para = null;
                 int paraIndex = sectionsSubstring.indexOf('§', lastParaIndex);
                 int nextParaIndex = sectionsSubstring.indexOf('§', paraIndex + 1);
                 int paraEndIndex;
                 String currentSectionSubstring;
                 if (nextParaIndex == -1)
                 {
                     paraEndIndex = sectionsSubstring.length() - 1;
                     currentSectionSubstring = sectionsSubstring.substring(paraIndex);
                 }
                 else
                 {
                     paraEndIndex = nextParaIndex - 1;
                     currentSectionSubstring = sectionsSubstring.substring(paraIndex, paraEndIndex);
                 }
                 //Logger.getLogger("intlib").log(Level.INFO, "Parsing section: \"{0}\" ", currentSectionSubstring);
                 
                 //Get paragraph number
                 section = currentSectionSubstring.replaceAll("[^§]*§ ?([0-9a-z]+).*","$1");
                // Logger.getLogger("intlib").log(Level.INFO, "§ {0}", section);
                 
                 String subsectionsSubstring = currentSectionSubstring.replaceAll("[^§]*§ ?([0-9a-z]+) (.*)","$2");
                 //Logger.getLogger("intlib").log(Level.INFO, "Parsing subsections: \"{0}\" ", subsectionsSubstring);
                 
                 //repeat the same for "odst. 2" and then for "písm. a )"
                 int lastOdstIndex = 0;
                 do
                 {
                     para = null;
                     int odstIndex = subsectionsSubstring.indexOf("odst.", lastOdstIndex);
                     if (odstIndex == -1)
                     {
                         //Logger.getLogger("intlib").log(Level.INFO, "Did not find subsection. Creating URI.");
                         result.add(parseSingle(expression, type, law, referedId, country, year, number, section, subsection, para, subpara, shortcut));
                         break;
                     }
                     int nextOdstIndex = subsectionsSubstring.indexOf("odst.", odstIndex + 1);
                     int odstEndIndex;
                     String currentSubsectionSubstring;
                     if (nextOdstIndex == -1)
                     {
                         odstEndIndex = subsectionsSubstring.length() - 1;
                         currentSubsectionSubstring = subsectionsSubstring.substring(odstIndex);
                     }
                     else
                     {
                         odstEndIndex = nextOdstIndex - 1;
                         currentSubsectionSubstring = subsectionsSubstring.substring(odstIndex, odstEndIndex);
                     }
                     //Logger.getLogger("intlib").log(Level.INFO, "Parsing subsection: \"{0}\" ", currentSubsectionSubstring);
 
                     //Get odst. number
                     subsection = currentSubsectionSubstring.replaceAll("[^o]*odst\\. ?([0-9a-z]+).*","$1");
                     //Logger.getLogger("intlib").log(Level.INFO, "odstavec {0}", subsection);
 
                     String parasSubstring = currentSubsectionSubstring.replaceAll("[^o]*odst\\. ?([0-9a-z]+) (.*)","$2");
                     //Logger.getLogger("intlib").log(Level.INFO, "Parsing paras: \"{0}\" ", parasSubstring);
 
                     //repeat the same for "písm. a )"
                     if (parasSubstring.contains("písm."))
                     {
                         int lastPismIndex = 0;
                         String lastpism = null;
                         while (lastPismIndex > -1 && lastPismIndex < parasSubstring.length())
                         {
                             // )
                             String separator = ")";
                             if (parasSubstring.contains("/")) 
                             {
                                 separator = "/";
                             }
                             int pismIndex;
                             if (lastPismIndex + 1 == parasSubstring.length() || (pismIndex = parasSubstring.indexOf(separator, lastPismIndex + 1)) == -1) break;
                             
                             String currentParaSubstring = parasSubstring.substring(lastPismIndex, pismIndex);
                             //Logger.getLogger("intlib").log(Level.INFO, "Parsing para: \"{0}\" ", currentParaSubstring);
 
                             //Get písm. number
                             para = currentParaSubstring.replaceAll(".*p?í?s?m?\\.? ?([^ \\)\\\\/]+).*","$1");
                             
                             if (currentParaSubstring.contains("až"))
                             {
                                 Logger.getLogger("intlib").log(Level.INFO, "písm. {0}", para);
                                 if (lastpism != null)
                                 {
                                     char lastpismchar = lastpism.charAt(0);
                                     char pism = para.charAt(0);
                                     char currentchar = (char) (lastpismchar + 1);
                                     Logger.getLogger("intlib").log(Level.INFO, "Písm. Až found from {0} to {1} in {2}", new Object[] {lastpismchar, pism, parasSubstring});
                                     while (currentchar <= pism)
                                     {
                                         Logger.getLogger("intlib").log(Level.INFO, "Generating písm {0}", currentchar);
                                         result.add(parseSingle(expression, type, law, referedId, country, year, number, section, subsection, String.valueOf(currentchar), subpara, shortcut));
                                         currentchar++;
                                         az_expanded++;
                                     }
                                 }
                             }
                             else
                             {
                                 //Logger.getLogger("intlib").log(Level.INFO, "písm. {0}", para);
 
                                 result.add(parseSingle(expression, type, law, referedId, country, year, number, section, subsection, para, subpara, shortcut));
                                 lastpism = para;
                             }
                             lastPismIndex = pismIndex;
                         }
                     }
                     else if (parasSubstring.contains("č."))
                     {
                         String parsingRegEx = "[^0-9]+([^/]+)/([0-9]+)(.*)";
                         String substr;
                         if (expression.startsWith("č.")) substr = expression.substring(2);
                         else substr = expression.substring(expression.lastIndexOf("č."));
                         if (substr.matches(parsingRegEx))
                         {
                             String num = substr.replaceAll(parsingRegEx, "$1");
                             String yearstr = substr.replaceAll(parsingRegEx, "$2");
                             String rest = substr.replaceAll(parsingRegEx, "$3");
                             //shortcut = rest;
                             year = fixYear(yearstr);
                             number = num;
 
                             result.add(parseSingle(expression, type, law, referedId, country, year, number, section, subsection, para, subpara, shortcut));
                             lastFoundYear = year;
                             lastFoundNumber = number;
                             lastFoundShortcut = null;
                         }
                         else
                         {
                             Logger.getLogger("intlib").log(Level.WARNING, "Not parsable reference: {0}", expression);
                         }
                     }
                     else
                     {
                         //Logger.getLogger("intlib").log(Level.INFO, "Not found \"písm\", creating URI.", parasSubstring);
                         para = null;
                         result.add(parseSingle(expression, type, law, referedId, country, year, number, section, subsection, para, subpara, shortcut));
                     }
                     lastOdstIndex = nextOdstIndex;
                 } while (lastOdstIndex > -1);
                 lastParaIndex = nextParaIndex;
             } while (lastParaIndex > -1);
             
         }
        else if (expression.contains("sp. zn"))
         {
             //Logger.getLogger("intlib").log(Level.INFO, "\"{0}\" identified as \"spisová značka\": contains \"sp. zn.\"", expression);
            String spzn = originalExpression.replaceAll(".*[Ss][Pp]\\. ?[Zz][Nn]\\.?(.*)", "$1").trim();
             //Logger.getLogger("intlib").log(Level.INFO, "Parsing \"spisová značka\" {0}", spzn);
             String first = spzn.replaceAll("([^ ]+) [^ ]+ [^ ]+", "$1");//.toLowerCase();
             String second = spzn.replaceAll("[^ ]+ ([^ ]+) [^ ]+", "$1");//.toLowerCase();
             String third = spzn.replaceAll("[^ ]+ [^ ]+ ([^ ]+)", "$1");
             String yearstr = null;
             if (third.contains("/")) yearstr = third.replaceAll("[^/]+/(.+)", "$1");
             if (yearstr != null)
             {
                 year = fixYear(yearstr);
             }
             
             else
             {
                 Logger.getLogger("intlib").log(Level.WARNING, "No year in sp. zn. {0}", expression);
             }
             //Logger.getLogger("intlib").log(Level.INFO, "Parsed sp. zn. \"{0}\" \"{1}\" \"{2}\", year {3}", new Object[] {first, second, third, year});
             number = clean((first + " " + second + " " + third));
             
             
             result.add(parseSingle(expression, type, law, referedId, country, year, number, section, subsection, para, subpara, shortcut));
         }
         else if (expression.trim().startsWith("čl."))
         {
             //Has to be Listina or Ustava reference
             //Logger.getLogger("intlib").log(Level.INFO, "\"{0}\" identified as an act reference: contains a §", expression);
             String rest = expression;
             int clindex;
             do {
                 String firstSectionsSubstring = rest.replaceAll("((čl\\.|[0-9]+| |,|odst\\.|a|písm.)+).*", "$1");
                 rest = rest.substring(firstSectionsSubstring.length());
 
                 clindex = rest.indexOf("čl.");
                 String firstShortcut;
                 if (clindex == -1) {
                     firstShortcut = rest;
                     rest = "";
                 }
                 else {
                     firstShortcut = rest.substring(0,rest.indexOf("čl."));
                     rest = rest.substring(rest.indexOf("čl."));
                 }
                 if (firstShortcut.matches("(.*) a ")) firstShortcut = firstShortcut.replaceAll("(.*) a ", "$1");
                 if (firstShortcut.matches("(.*), ")) firstShortcut = firstShortcut.replaceAll("(.*), ", "$1");
                 if (firstShortcut.matches("(.*) a v ")) firstShortcut = firstShortcut.replaceAll("(.*) a v ", "$1");
                 
                 String currentExpression = firstSectionsSubstring + " " + firstShortcut;
                 
                 
                 String shortcutRegex = ".*[0-9\\)\\\\/][^ ]* ?([^0-9\\)\\\\/]*)";
                 String sectionsSubstring ;
                 String shortcutCandidate = expression.replaceAll(shortcutRegex, "$1").trim();
                 if (expression.matches(shortcutRegex) && !shortcutCandidate.isEmpty())
                 {
                     shortcut = shortcutCandidate;
 
                     for (String replacement : startShortcutTrimmings)
                     {
                         if (shortcut.startsWith(replacement))
                         {
                             //Logger.getLogger("intlib").log(Level.INFO, "Trimming shortcut (start): \"{0}\"", replacement);
                             shortcut = shortcut.substring(replacement.length());
                             startShortTrimmed++;
                             break;
                         }
                     }
                     for (String replacement : endShortcutTrimmings)
                     {
                         if (shortcut.endsWith(replacement))
                         {
                             //Logger.getLogger("intlib").log(Level.INFO, "Trimming shortcut (end): \"{0}\"", replacement);
                             shortcut = shortcut.substring(0, shortcut.length() - replacement.length());
                             endShortTrimmed++;
                             break;
                         }
                     }
                     if (shortcut.matches("vět[aouy]u? .*"))
                     {
                         shortcut = shortcut.replaceAll("vět[oauy]u? [^ ]+ (.*)", "$1");
                         //Logger.getLogger("intlib").log(Level.INFO, "\"Věta\" not supported, trimmed.", shortcut);
                     }
 
                     if (shortcut.matches("[Ss]b\\."))
                     {
                         //Logger.getLogger("intlib").log(Level.INFO, expression);
                         sectionsSubstring = expression.replaceAll("(.*) [^ ]+ č\\.?.*","$1");
                         String actref;
                         if (expression.lastIndexOf("č") == -1)
                         {
                             actref = expression.substring(0,expression.lastIndexOf("zák"));
                         }
                         else actref = expression.substring(expression.lastIndexOf("č"));
                         number = actref.replaceAll("[^0-9]*([0-9]+)/([0-9]+)[sS]?b?\\.?[^0-9]*","$1");
                         year = fixYear(actref.replaceAll("[^/]*([0-9]+)/([0-9]+)[^0-9]*","$2"));
                         shortcut = null;
                         lastFoundShortcut = null;
                         lastFoundYear = year;
                         lastFoundNumber = number;
                     }
                     else
                     {
                         sectionsSubstring = expression.substring(0, expression.lastIndexOf(shortcut)).trim();
                         if (!shortcut.isEmpty())
                         {
                             lastFoundShortcut = shortcut;
                             lastFoundYear = 0;
                             lastFoundNumber = null;
                         }
                     }
                 }
                 else if (expression.matches(".*[Ss]b\\."))
                 {
                     //Logger.getLogger("intlib").log(Level.INFO, expression);
                     sectionsSubstring = expression.replaceAll("(.*) [^ ]+ č\\.?.*","$1");
                     String actref;
                     if (expression.lastIndexOf("č") == -1)
                     {
                         actref = expression.substring(0,expression.lastIndexOf("zák"));
                     }
                     else actref = expression.substring(expression.lastIndexOf("č"));
                     number = actref.replaceAll("[^0-9]*([0-9]+)/([0-9]+)[sS]?b?\\.?[^0-9]*","$1");
                     year = fixYear(actref.replaceAll("[^/]*([0-9]+)/([0-9]+)[^0-9]*","$2"));
                     shortcut = null;
                     lastFoundShortcut = null;
                     lastFoundYear = year;
                     lastFoundNumber = number;
                 }
                 else
                 {
                     sectionsSubstring = expression;
                     if (lastFoundShortcut != null)
                     {
                         shortcut = lastFoundShortcut;
                         lastShortcutUsed++;
                         //Logger.getLogger("intlib").log(Level.INFO, "Shortcut not found. Using last found shortcut: \"{0}\" for expression: \"{1}\"", new Object[]{shortcut, expression});
                     }
                     else if (lastFoundYear > 0)
                     {
                         year = lastFoundYear;
                         number = lastFoundNumber;
                         lastNumberUsed++;
                         //Logger.getLogger("intlib").log(Level.INFO, "Shortcut not found. Using last found number and year: \"{0}\" for expression: \"{1}\"", new Object[]{number + "/" + year, expression});
                     }
 
                 }
                 
                 
 
                 //Logger.getLogger("intlib").log(Level.INFO, "\"{0}\" identified as \"shortcut\"", shortcut);
                 int lastParaIndex = -1;
                 do
                 {
                     subsection = null;
                     para = null;
                     int paraIndex = sectionsSubstring.indexOf("čl.", lastParaIndex);
                     int nextParaIndex = sectionsSubstring.indexOf("čl.", paraIndex + 3);
                     int paraEndIndex;
                     String currentSectionSubstring;
                     if (nextParaIndex == -1)
                     {
                         paraEndIndex = sectionsSubstring.length() - 1;
                         currentSectionSubstring = sectionsSubstring.substring(paraIndex);
                     }
                     else
                     {
                         paraEndIndex = nextParaIndex - 1;
                         currentSectionSubstring = sectionsSubstring.substring(paraIndex, paraEndIndex);
                     }
                     //Logger.getLogger("intlib").log(Level.INFO, "Parsing section: \"{0}\" ", currentSectionSubstring);
 
                     //Get paragraph number
                     section = currentSectionSubstring.replaceAll(".*čl. ?([0-9a-z]+).*","$1");
                    // Logger.getLogger("intlib").log(Level.INFO, "§ {0}", section);
 
                     String subsectionsSubstring = currentSectionSubstring.replaceAll(".*čl. ?([0-9a-z]+) (.*)","$2");
                     //Logger.getLogger("intlib").log(Level.INFO, "Parsing subsections: \"{0}\" ", subsectionsSubstring);
 
                     //repeat the same for "odst. 2" and then for "písm. a )"
                     int lastOdstIndex = 0;
                     do
                     {
                         para = null;
                         int odstIndex = subsectionsSubstring.indexOf("odst.", lastOdstIndex);
                         if (odstIndex == -1)
                         {
                             //Logger.getLogger("intlib").log(Level.INFO, "Did not find subsection. Creating URI.");
                             result.add(parseSingle(currentExpression, type, law, referedId, country, year, number, section, subsection, para, subpara, shortcut));
                             break;
                         }
                         int nextOdstIndex = subsectionsSubstring.indexOf("odst.", odstIndex + 1);
                         int odstEndIndex;
                         String currentSubsectionSubstring;
                         if (nextOdstIndex == -1)
                         {
                             odstEndIndex = subsectionsSubstring.length() - 1;
                             currentSubsectionSubstring = subsectionsSubstring.substring(odstIndex);
                         }
                         else
                         {
                             odstEndIndex = nextOdstIndex - 1;
                             currentSubsectionSubstring = subsectionsSubstring.substring(odstIndex, odstEndIndex);
                         }
                         //Logger.getLogger("intlib").log(Level.INFO, "Parsing subsection: \"{0}\" ", currentSubsectionSubstring);
                         if (currentSubsectionSubstring.endsWith(" a"))
                         {
                             currentSubsectionSubstring = currentSubsectionSubstring.substring(0, currentSubsectionSubstring.length()-2);
                         }
 
                         //Get odst. number
                         subsection = currentSubsectionSubstring.replaceAll("[^o]*odst\\. ?([0-9a-z]+).*","$1");
                         //Logger.getLogger("intlib").log(Level.INFO, "odstavec {0}", subsection);
 
                         String parasSubstring = currentSubsectionSubstring.replaceAll("[^o]*odst\\. ?([0-9a-z]+) (.*)","$2");
                         //Logger.getLogger("intlib").log(Level.INFO, "Parsing paras: \"{0}\" ", parasSubstring);
 
                         //repeat the same for "písm. a )"
                         if (parasSubstring.contains("písm."))
                         {
                             int lastPismIndex = 0;
                             while (lastPismIndex > -1 && lastPismIndex < parasSubstring.length())
                             {
                                 // )
                                 String separator = ")";
                                 if (parasSubstring.contains("/")) 
                                 {
                                     separator = "/";
                                 }
                                 int pismIndex;
                                 if (lastPismIndex + 1 == parasSubstring.length() || (pismIndex = parasSubstring.indexOf(separator, lastPismIndex + 1)) == -1) break;
 
                                 String currentParaSubstring = parasSubstring.substring(lastPismIndex, pismIndex);
                                 //Logger.getLogger("intlib").log(Level.INFO, "Parsing para: \"{0}\" ", currentParaSubstring);
 
                                 //Get písm. number
                                 para = currentParaSubstring.replaceAll(".*p?í?s?m?\\.? ?([^ \\)\\\\/]+).*","$1");
                                 //Logger.getLogger("intlib").log(Level.INFO, "písm. {0}", para);
 
                                 result.add(parseSingle(currentExpression, type, law, referedId, country, year, number, section, subsection, para, subpara, shortcut));
 
                                 lastPismIndex = pismIndex;
                             }
                         }
                         else if (parasSubstring.startsWith("a "))
                         {
                             para = null;
                             result.add(parseSingle(currentExpression, type, law, referedId, country, year, number, section, subsection, para, subpara, shortcut));
                             subsection = parasSubstring.replaceAll("a (.*)", "$1");
                             result.add(parseSingle(currentExpression, type, law, referedId, country, year, number, section, subsection, para, subpara, shortcut));
                         }
                         else
                         {
                             //Logger.getLogger("intlib").log(Level.INFO, "Not found \"písm\", creating URI.", parasSubstring);
                             para = null;
                             result.add(parseSingle(currentExpression, type, law, referedId, country, year, number, section, subsection, para, subpara, shortcut));
                         }
                         lastOdstIndex = nextOdstIndex;
                     } while (lastOdstIndex > -1);
                     lastParaIndex = nextParaIndex;
                 } while (lastParaIndex > -1);
                 //Logger.getLogger("intlib").log(Level.WARNING, "Článek (ústavy/listiny) not supported yet", expression);
             } while (clindex > -1);
         }
         else if (expression.trim().matches("[Čč]\\.? ?[Jj]\\..*"))
         {
             //Logger.getLogger("intlib").log(Level.WARNING, "Číslo jednací not supported yet", expression);
             String spzn = originalExpression.replaceAll(".*[Čč]\\. ?[Jj]\\.(.*)", "$1").trim();
             //Logger.getLogger("intlib").log(Level.INFO, "Parsing \"spisová značka\" {0}", spzn);
             String first = spzn.replaceAll("([^ ]+) [^ ]+ [^ ]+", "$1");
             String second = spzn.replaceAll("[^ ]+ ([^ ]+) [^ ]+", "$1");
             String third = spzn.replaceAll("[^ ]+ [^ ]+ ([^ ]+)", "$1");
             String yearstr = null;
             if (third.contains("/")) yearstr = third.replaceAll("[^/]+/([0-9]+).*", "$1");
             if (yearstr != null)
             {
                 year = fixYear(yearstr);
             }
             
             else
             {
                 Logger.getLogger("intlib").log(Level.WARNING, "No year in č.j. {0}", expression);
             }
             //Logger.getLogger("intlib").log(Level.INFO, "Parsed č.j. \"{0}\" \"{1}\" \"{2}\", year {3}", new Object[] {first, second, third, year});
             number = clean((first + " " + second + " " + third));
             
             
             result.add(parseSingle(expression, type, law, referedId, country, year, number, section, subsection, para, subpara, shortcut));
             
             
         }
         else if (expression.trim().matches("((.*) č\\.(.*)|č\\.(.*))"))
         {
             //Logger.getLogger("intlib").log(Level.INFO, "Simple decision reference identified: {0}", expression);
             String parsingRegEx = "[^0-9]+([^/]+)/([0-9]+)(.*)";
             String substr;
             if (expression.startsWith("č.")) substr = expression.substring(2);
             else substr = expression.substring(expression.lastIndexOf("č."));
             if (substr.matches(parsingRegEx))
             {
                 String num = substr.replaceAll(parsingRegEx, "$1");
                 String yearstr = substr.replaceAll(parsingRegEx, "$2");
                 String rest = substr.replaceAll(parsingRegEx, "$3");
                 shortcut = rest;
                 year = fixYear(yearstr);
                 number = num;
 
                 result.add(parseSingle(expression, type, law, referedId, country, year, number, section, subsection, para, subpara, shortcut));
             }
             else
             {
                 Logger.getLogger("intlib").log(Level.WARNING, "Not parsable reference: {0}", expression);
             }
         }
         else if (expression.matches(".*([0-9]+)/[0-9]+.*"))
         {
             //Logger.getLogger("intlib").log(Level.INFO, "Simple reference identified: {0}", expression);
             String parsingRegEx = "[^0-9]*([0-9]+)/([0-9]+)([^0-9]*)";
             String num = expression.replaceAll(parsingRegEx, "$1");
             String yearstr = expression.replaceAll(parsingRegEx, "$2");
             
             year = fixYear(yearstr);
             number = num;
             
             result.add(parseSingle(expression, type, law, referedId, country, year, number, section, subsection, para, subpara, shortcut));           
         }
         else
         {
             log.warn("Expression \"{0}\" not recognized, trying as shortcut", expression);
             //Logger.getLogger("intlib").log(Level.INFO, "Expression \"{0}\" not recognized, trying as shortcut", expression);
             shortcut = expression;
             result.add(parseSingle(expression, type, law, referedId, country, year, number, section, subsection, para, subpara, shortcut));
             lastFoundShortcut = shortcut;
             lastFoundNumber = null;
             lastFoundYear = 0;
         }
 
         //END OF STRING PARSING
         return result;
     }
     
     /**
      * Apply give Work to current work. It means fullfil all blank attributes with attributes from given Work.
      * 
      * @param w 
      */
     private void apply(Work w) {
         if (w.year != null) {
             if (this.getYear() == null) {
                 this.setYear(w.year);
             }
         } 
     
         if (w.number != null) {
             if (this.getNumber() == null) {
                 this.setNumber(w.number);
             }
         } 
     }
 
     // -------------------------------------------------------------------------
     // Helpers
 
     /**
      * Clean given String, i.e. replace all non-ascii characters with their czech equivalents. Replace all non-alphanumeric characters by -.
      * 
      * @param data
      * @return 
      */
     protected static String clean(String data) {
         if (data == null) {
             return null;
         }
         data = data.replaceAll("ž", "z")
                 .replaceAll("š", "s")
                 .replaceAll("č", "c")
                 .replaceAll("ř", "r")
                 .replaceAll("ď", "d")
                 .replaceAll("ť", "t")
                 .replaceAll("ň", "n")
                 .replaceAll("á", "a")
                 .replaceAll("Ž", "z")
                 .replaceAll("Š", "S")
                 .replaceAll("Č", "C")
                 .replaceAll("Ř", "R")
                 .replaceAll("Ď", "D")
                 .replaceAll("Ť", "T")
                 .replaceAll("Ň", "N")
                 .replaceAll("Á", "A")
                 .replaceAll("[ěé]", "e")
                 .replaceAll("[ĚÉ]", "E")
                 .replaceAll("í", "i")
                 .replaceAll("Í", "I")
                 .replaceAll("ó", "o")
                 .replaceAll("Ó", "O")
                 .replaceAll("[úů]", "u")
                 .replaceAll("[ÚŮ]", "U")
                 .replaceAll("ý", "y")
                 .replaceAll("Ý", "Y")
                 .replaceAll("[/\\\\]", "-")
                 .replaceAll(" ", "-")
                 .replaceAll("^-", "")
                 .replaceAll("-$", "")
                 .replaceAll("-[-]+", "-")
                 .replaceAll("[^0-9a-zA-Z -]", "");
         
         if (data.isEmpty()) {
             data = EMPTY_SECTION_LABEL;
         }
         return data;
     }
     
     /**
      * Find first element of given nam in element subtree.
      * 
      * @param el
      * @param tagName
      * @return 
      */
     protected static NodeList getFirstElementsByTagName(Element el, String tagName) {
         
         NodeList nl = new NodeList() {
 
             private LinkedList<Node> list = new LinkedList<>();
             
             public NodeList load(Element el, String tagName) {
                 
                 NodeList nl = el.getChildNodes();
                 
                 for (int i = 0; i < nl.getLength(); i ++) {
                     if (nl.item(i).getNodeType() == Node.ELEMENT_NODE) {
                         if (nl.item(i).getNodeName().equals(tagName)) {
                             list.add(nl.item(i));
                         } else {
                             NodeList x = getFirstElementsByTagName((Element)nl.item(i), tagName);
                             for (int j = 0; j < x.getLength(); j ++) {
                                 list.add(x.item(j));
                             }
                         }
                         
                     }
                 }
                 
                 return this;
             }
             
             @Override
             public Node item(int i) {
                 return list.get(i);
             }
 
             @Override
             public int getLength() {
                 return list.size();
             }
         }.load(el, tagName);
         
         return nl;
         
     }
     
     // -------------------------------------------------------------------------
     // Document transformation
     
     /**
      * Comparision operator based on the year and nubmer of work.
      * 
      * @param t
      * @return 
      */
     @Override
     public int compareTo(Work t) {
         
         if (this == t) return 0;
         
         if (!this.getTypeOfWork().equals(t.getTypeOfWork())) {
             return this.getTypeOfWork().compareTo(t.getTypeOfWork());
         }
         
         if (this.getYear() < t.getYear()) return -1;
         if (this.getYear() > t.getYear()) return 1;
         
         String thisNumber = this.getNumber();
         String tNumber = t.getNumber();
         
         /*if (thisNumber.contains("-")) {
             String[] numberParts = thisNumber.split("-");
             thisNumber = numberParts[0];
         }
         
         String tNumber = t.getNumber();
         
         if (tNumber.contains("-")) {
             String[] numberParts = thisNumber.split("-");
             tNumber = numberParts[0];
         }*/
         
         return thisNumber.compareTo(tNumber);
     }
     
     
 }
