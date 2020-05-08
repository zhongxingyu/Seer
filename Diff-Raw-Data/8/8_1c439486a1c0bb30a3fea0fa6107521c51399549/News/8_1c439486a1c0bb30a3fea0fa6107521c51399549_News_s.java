 /*
  * News.java
  *
  */
 
 import java.io.BufferedReader;
 import java.io.FileReader;
 import java.util.ArrayList;
 
 import AndrewCassidy.PluggableBot.DefaultPlugin;
 import AndrewCassidy.PluggableBot.PluggableBot;
 
 /**
  * 
  * @author Murmew
  * @author Mex
  */
 public class News extends DefaultPlugin {
 
 	public PluggableBot bot;
 	private java.util.Random random = new java.util.Random();
 
 	private ArrayList<Verb> verbs = new ArrayList<Verb>();
 	private ArrayList<Noun> nouns = new ArrayList<Noun>();
 	private ArrayList<Phrase> phrases = new ArrayList<Phrase>();
 	private ArrayList<String> objects = new ArrayList<String>();
 
 	public News() {
 		loadWords();
 	}
 	
 	private void loadWords() {
 		try {
 			FileReader fr = new FileReader("News");
 			BufferedReader br = new BufferedReader(fr);
 			String line;
 			while ((line = br.readLine()) != null) {
 				try {
 					String[] words = line.split(";");
 					if (words[0].equals("verb")) {
 						Tense t = words[3].equals("present") ? Tense.present
 								: words[3].equals("active") ? Tense.active
 										: Tense.past;
 
 						verbs.add(new Verb(words[1], words[2], t));
 					} else if (words[0].equals("noun")) {
 						Perspective p = words[3].equals("third") ? Perspective.third
 								: Perspective.first;
 						Plurality s = words[3].equals("singular") ? Plurality.singular
 								: Plurality.singular;
 
 						nouns.add(new Noun(words[1], p, s));
 					} else if (words[0].equals("phrase")) {
 						phrases.add(new Phrase(words[1], words[2],words[3],words[4]));
 					} else if (words[0].equals("object")) {
 						objects.add(words[1]);
 					}
 				} catch (Exception e) {
 					System.out.println("Failed to add line " + line);
 					e.printStackTrace();
 				}
 			}
 		} catch (Exception e) {
 			throw new IllegalStateException("Unable to load pounce file.");
 		}
 
 		System.out.println("---------verbs" + verbs.size());
 		System.out.println("---------nouns" + nouns.size());
 		System.out.println("---------phrases" + phrases.size());
 		System.out.println("---------objects" + objects.size());
 	}
 
 
 
 	/**
 	 * Return a random string given an array
 	 */
 	public Object getRandom(ArrayList<? extends Object> a) {
 		return a.get(random.nextInt(a.size()));
 	}
 
 	/**
 	 * Matches a verb with a subject (noun)
 	 */
 	private String match_verb_and_noun(Noun noun, Verb verb) {
 		if (noun.plurality == Plurality.singular
 				&& noun.perspective == Perspective.third) {
 			return (verb.singular);
 		} else {
 			return (verb.plural);
 		}
 	}
 
 	/**
 	 * Matches the transitive verb's tense with that of the verb
 	 */
 	private String match_verb_and_tense(Verb verb, Phrase phrase) {
		if (verb.tense.equals("present")) {
 			return phrase.present;
		} else if (verb.tense.equals("past")) {
 			return phrase.past;
		} else if (verb.tense.equals("active")) {
 			return phrase.active;
 		}
 		return null;
 	}
 
 	/**
 	 *  Returns a Daily Mail Headline as a string
 	 * @return
 	 */
 	public String getHeadline() {
 		String sentence = "";
 
 		Noun subject = (Noun) getRandom(nouns);
 		Phrase phrase = (Phrase) getRandom(phrases);
 		Verb verb = (Verb) getRandom(verbs);
 		String object = (String) getRandom(objects);
 
 		sentence += match_verb_and_noun(subject, verb);
 		sentence += " " + subject.word;
 		sentence += " " + match_verb_and_tense(verb, phrase);
 		sentence += " " + object;
 
 		if (phrase.object != "") {
 			sentence += " " + phrase.object;
 		}
 
 		return sentence.toUpperCase() + "?";
 	}
 
 	public void onMessage(String channel, String sender, String login,
 			String hostname, String message) {
 		if (message.startsWith("!news")) {
 			PluggableBot.Message(channel, sender + ": " + getHeadline());
 		}
 	}
 
 	public String getHelp() {
 		return "Type !news to get a random news headline!";
 	}
 
 }
