 package orion.esp;
 
 import java.io.UnsupportedEncodingException;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 import java.util.UUID;
 
 import pegasus.eventbus.client.Envelope;
 
 import com.google.common.base.Predicate;
 import com.google.common.collect.Iterables;
 import com.google.common.collect.Lists;
 import com.google.common.collect.Maps;
 import com.google.common.collect.Sets;
 import com.google.gson.Gson;
 import com.google.gson.GsonBuilder;
 
 public class EnvelopeUtils {
 
 
     private final static Gson gson_pp = new GsonBuilder().setPrettyPrinting().create();
     private final static Gson gson = new Gson();
 
     public static String toFormattedJson(String line) {
         HashMap map = toMapJson(line);
         String mapstr = gson_pp.toJson(map);
         return mapstr;
     }
 
     public static HashMap toMapJson(String line) {
         return gson.fromJson(line, HashMap.class);
     }
 
     public static String getBodyValue(Envelope env, String key) {
         try {
             String bodyJson = new String(env.getBody(), "UTF-8");
             HashMap map = EnvelopeUtils.toMapJson(bodyJson);
             Object queryText = map.get(key);
             return "" + queryText;
         } catch (Exception e) {
             return null;
         }
     }
 
     public static String toJson(Envelope env) {
         return gson.toJson(env, Envelope.class);
     }
 
     public static String toPrettyJson(Envelope env) {
         return gson_pp.toJson(env, Envelope.class);
     }
 
     public static String envelopeToReadableJson(Envelope env) {
         byte[] body = env.getBody();
         String bodyJson = "";
         try {
             bodyJson = new String(body, "UTF-8");
         } catch (UnsupportedEncodingException e) {
             e.printStackTrace();
         }
         String bodyformatted = toFormattedJson(bodyJson);
         JsonString json = new JsonString().start()
                 .add("EVENT_TYPE", env.getEventType())
                 .add("REPLYTO", env.getReplyTo())
                 .add("TOPIC", env.getTopic())
                 .add("ID", env.getId())
                 .add("CORRELATION_ID", env.getCorrelationId())
                 .add("TIMESTAMP", env.getTimestamp())
                 .add("HEADERS", env.getHeaders())
                 .add("BODY_SIZE", body.length)
                 .add("BODY_SRC", body)
                 .add("BODY_JSON", bodyJson)
                 .add("BODY", bodyformatted)
                 ;
         return json.end().toString();
     }
 
     public static Envelope fromJson(String line) {
         Envelope envelope = gson.fromJson(line, Envelope.class);
         return envelope;
     }
 
     public static String makeSearchTermList(String query) {
         Iterable<String> searchTerms = EnvelopeUtils.getSearchTerms(query);
         StringBuffer sb = new StringBuffer();
         String sep = "";
         for (String term : searchTerms) {
             if (term.length() > 0) {
                 sb.append(sep + term);
                 sep = ",";
             }
         }
         return sb.toString();
     }
 
     public static Iterable<String> getSearchTerms(String initquery) {
         Predicate<String> filter = new Predicate<String>() {
             @Override
             public boolean apply(String input) {
                 if (input.length() == 0) return false;
                if (EnvelopeUtils.STOPWORDS.contains(input)) return false;
                 return true;
             }
         };
 
 	// normalize to lower case and remove all punctuation
         String query = initquery.toLowerCase();
         query = query.replaceAll("[^a-z0-9 ]", "");
         ArrayList<String> allTerms = Lists.newArrayList(query.split(" "));
         Iterable<String> filteredTerms = Iterables.filter(allTerms, filter);
         return filteredTerms;
     }
 
    public final static Set<String> STOPWORDS = makeStopWordSet();
 
     private static HashSet<String> makeStopWordSet() {
         HashSet<String> words = Sets.newHashSet();
         String[] STOPWORDLIST = { "a", "about", "above", "above", "across", "after", "afterwards", "again", "against", "all", "almost", "alone", "along", "already", "also", "although", "always",
                 "am", "among", "amongst", "amoungst", "amount", "an", "and", "another", "any", "anyhow", "anyone", "anything", "anyway", "anywhere", "are", "around", "as", "at", "back", "be",
                 "became", "because", "become", "becomes", "becoming", "been", "before", "beforehand", "behind", "being", "below", "beside", "besides", "between", "beyond", "bill", "both", "bottom",
                 "but", "by", "call", "can", "cannot", "cant", "co", "con", "could", "couldnt", "cry", "de", "describe", "detail", "do", "done", "down", "due", "during", "each", "eg", "eight",
                 "either", "eleven", "else", "elsewhere", "empty", "enough", "etc", "even", "ever", "every", "everyone", "everything", "everywhere", "except", "few", "fifteen", "fify", "fill", "find",
                 "fire", "first", "five", "for", "former", "formerly", "forty", "found", "four", "from", "front", "full", "further", "get", "give", "go", "had", "has", "hasnt", "have", "he", "hence",
                 "her", "here", "hereafter", "hereby", "herein", "hereupon", "hers", "herself", "him", "himself", "his", "how", "however", "hundred", "ie", "if", "in", "inc", "indeed", "interest",
                 "into", "is", "it", "its", "itself", "keep", "last", "latter", "latterly", "least", "less", "ltd", "made", "many", "may", "me", "meanwhile", "might", "mill", "mine", "more",
                 "moreover", "most", "mostly", "move", "much", "must", "my", "myself", "name", "namely", "neither", "never", "nevertheless", "next", "nine", "no", "nobody", "none", "noone", "nor",
                 "not", "nothing", "now", "nowhere", "of", "off", "often", "on", "once", "one", "only", "onto", "or", "other", "others", "otherwise", "our", "ours", "ourselves", "out", "over", "own",
                 "part", "per", "perhaps", "please", "put", "rather", "re", "same", "see", "seem", "seemed", "seeming", "seems", "serious", "several", "she", "should", "show", "side", "since",
                 "sincere", "six", "sixty", "so", "some", "somehow", "someone", "something", "sometime", "sometimes", "somewhere", "still", "such", "system", "take", "ten", "than", "that", "the",
                 "their", "them", "themselves", "then", "thence", "there", "thereafter", "thereby", "therefore", "therein", "thereupon", "these", "they", "thickv", "thin", "third", "this", "those",
                 "though", "three", "through", "throughout", "thru", "thus", "to", "together", "too", "top", "toward", "towards", "twelve", "twenty", "two", "un", "under", "until", "up", "upon", "us",
                 "very", "via", "was", "we", "well", "were", "what", "whatever", "when", "whence", "whenever", "where", "whereafter", "whereas", "whereby", "wherein", "whereupon", "wherever",
                 "whether", "which", "while", "whither", "who", "whoever", "whole", "whom", "whose", "why", "will", "with", "within", "without", "would", "yet", "you", "your", "yours", "yourself",
                 "yourselves", "the" };
         for (String word : STOPWORDLIST) {
             words.add(word);
         }
         return words;
     }
 }
