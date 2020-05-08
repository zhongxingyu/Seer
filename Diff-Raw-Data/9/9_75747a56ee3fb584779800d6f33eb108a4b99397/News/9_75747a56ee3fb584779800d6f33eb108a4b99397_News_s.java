 /*	
 * Copyright 2007 Andee
  * Copyright 2010 Mex (ellism88@gmail.com)
  * 
  *   This program is free software: you can redistribute it and/or modify
  *   it under the terms of the GNU General Public License as published by
  *   the Free Software Foundation, either version 3 of the License, or
  *   (at your option) any later version.
  *
  *   This program is distributed in the hope that it will be useful,
  *   but WITHOUT ANY WARRANTY; without even the implied warranty of
  *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *   GNU General Public License for more details.
  *
  *   You should have received a copy of the GNU General Public License
  *   along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package News;
 
 import java.io.BufferedReader;
 import java.io.FileReader;
 import java.util.ArrayList;
 

import com.PluggableBot.PluggableBot;
 import com.PluggableBot.plugin.DefaultPlugin;
 
 
 /**
  * Plugin to provide times style headlines.
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
 						if (words.length > 5) {
 						phrases.add(new Phrase(words[1], words[2], words[3],
 								words[4]));
 						} else {
 							System.out.println("Failed to load '" + line + "' too short");
 						}
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
 		if (verb.tense == Tense.present) {
 			return phrase.present;
 		} else if (verb.tense == Tense.past) {
 			return phrase.past;
 		} else if (verb.tense == Tense.active) {
 			return phrase.active;
 		}
 		return null;
 	}
 
 	/**
 	 * Returns a Daily Mail Headline as a string
 	 * 
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
 
 	@Override
 	public void onMessage(String channel, String sender, String login,
 			String hostname, String message) {
 		if (message.startsWith("!news")) {
 			bot.Message(channel, sender + ": " + getHeadline());
 		}
 	}
 
 	@Override
 	public String getHelp() {
 		return "Type !news to get a random news headline!";
 	}
 
 }
