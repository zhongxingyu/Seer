 package com.supersecure.pl;
 
 public class Database {
 
 	private final static Database instance = new Database();
 
 	private final Language[] languages = new Language[9999];
 
 	public void init() {
 		add("C");
 		add("Java");
 		add("C#");
 		add("C++");
 		add("Erlang");
 		add("Ruby");
 	}
 
 	public String add(String name) {
 		for (int i = 0; i < languages.length; i++) {
 			if (languages[i] == null) {
 				languages[i] = new Language(name, 0, 0.0);
 				break;
 			}
 		}
 		return "new language: " + name;
 	}
 
 	public static Database getInstance() {
 		return instance;
 	}
 
 	public String delete(String name) {
 		for (int i = 0; i < languages.length; i++) {
 			if (languages[i].name.equals(name)) {
 				languages[i] = null;
 				return "deleted: " + languages;
 			}
 		}
 		return "nothing to delete";
 	}
 
 	public String rate(String name, int rating) {
 		for (int i = 0; i < languages.length; i++) {
 			if (languages[i] != null && languages[i].name.equals(name)) {
 				languages[i].rating_sum += rating;
 				++languages[i].votes;
 				return languages[i] + "";
 			}
 		}
 		return "language not found";
 	}
 
 	/**
	 * Helps if you are not completely secure about a name of a specifiv
	 * language. This methode searches in all know languages for <b>similar</b>
 	 * named ones.
 	 * 
 	 * In this context <b>similar</b> means the name could be derived from the
 	 * given string with just one modification. A modification is:
 	 * <ul>
 	 * <li>change one letter (Abc is similar to Abd)</li>
 	 * <li>remove one letter (Helllo is similar to Hello)</li>
 	 * <li>add one letter (Alx is similar to Alex)</li>
 	 * </ul>
 	 * 
 	 * @param string
 	 *            the search string, never <code>null</code>.
 	 * @return the quantity of similar named languages, never <code>null</code>.
 	 */
 	public String[] findSimilarNamedLanguages(String string) {
 		throw new UnsupportedOperationException("Not yet implemented!");
 	}
 
 	public String top() {
 		double max_rating = -1;
 		Language top = null;
 		for (int i = 0; i < languages.length; i++) {
 			if (languages[i] != null && languages[i].getRating() > max_rating) {
 				top = languages[i];
 			}
 		}
 
 		return (top == null) ? "no language is already rated" : top.toString();
 	}
 
 	public String find(String name) {
 		for (int i = 0; i < languages.length; i++) {
 			if (languages[i].name.equals(name)) {
 				return languages[i] + "";
 			}
 		}
 		return "language not found";
 	}
 
 	private final static class Language {
 
 		int votes;
 
 		String name;
 
 		double rating_sum;
 
 		private double getRating() {
 			return rating_sum / votes;
 		}
 
 		@Override
 		public String toString() {
 			return "Name: " + name + " rating: " + getRating();
 		}
 
 		public Language(String name, int votes, double rating_sum) {
 			super();
 			this.votes = votes;
 			this.name = name;
 			this.rating_sum = rating_sum;
 		}
 
 	}
 }
