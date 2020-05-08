 package org.mediawiki;
 
 import java.io.BufferedInputStream;
 import java.io.BufferedOutputStream;
 import java.io.BufferedReader;
 import java.io.Console;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.FilenameFilter;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.io.PrintWriter;
 import java.io.Serializable;
 import java.text.ParseException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.Date;
 import java.util.Deque;
 import java.util.HashMap;
 import java.util.IdentityHashMap;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Locale;
 import java.util.Map;
 import java.util.Set;
 import java.util.TreeMap;
 import java.util.TreeSet;
 import java.util.concurrent.CancellationException;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 import java.util.regex.PatternSyntaxException;
 
 import org.mediawiki.MediaWiki.MediaWikiException;
 
 // TODO Make magic words like {{PAGENAME}}, {{PAGENAMEE}} work w/ expandInput()
 /**
  * A wiki shell that allows a user to perform actions on the wiki using the
  * command-line.
  */
 /*-
  This program is free software: you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation, either version 3 of the License, or
  (at your option) any later version.
 
  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU General Public License for more details.
 
  You should have received a copy of the GNU General Public License
  along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 public class WikiShell {
 	private static final Map<String, Command> builtinCommands = new TreeMap<String, Command>(String.CASE_INSENSITIVE_ORDER);
 
 	/**
 	 * Command prefixes are really commands, but only their <code>perform</code>
 	 * method is ever called.
 	 */
 	private static final Map<String, Command> commandPrefixes = new TreeMap<String, Command>(String.CASE_INSENSITIVE_ORDER);
 
 	private static final Map<String, PageListModifier> pageListModifiers = new TreeMap<String, PageListModifier>(String.CASE_INSENSITIVE_ORDER);
 
 	/**
 	 * The current username of the person using this shell. May be an IP address
 	 * or a username.
 	 */
 	private static String user = "";
 
 	/**
 	 * <code>true</code> if the user has refused to log in via
 	 * <code>checkLogin</code> during the entire current shell session.
 	 */
 	private static boolean refuseLogin;
 
 	/**
 	 * The current wiki's hostname or IP.
 	 */
 	private static String host = "";
 
 	/**
 	 * Small status string displayed while work is being done.
 	 */
 	private static String workLong = "";
 
 	/**
 	 * Current path of the shell. This is initially ~, but displays the number
 	 * of pages affecting commands in the subshell started with
 	 * <tt>ForPages</tt> (the <tt>for</tt> command).
 	 */
 	private static Deque<String> path = new LinkedList<String>();
 
 	/**
 	 * Whether the currently logged-in user is a sysop on the current wiki.
 	 */
 	private static boolean sysop = false;
 
 	private static BufferedReader keyboard = new BufferedReader(new InputStreamReader(System.in));
 
 	/**
 	 * @param args
 	 *            unused
 	 */
 	public static void main(String[] args) {
 		System.err.println("You may use 'cancel' at any time to cancel a command or exit a repeating mode.");
 		System.err.println("For a list of commands, use 'lines commands' at a $ or # prompt.");
 
 		final Connect connect = new Connect();
 
 		MediaWiki wiki = null;
 
 		// Load the initial MediaWiki from disk, saved when the application last
 		// exited.
 		work("Loading wiki information from disk...");
 		try {
 			try {
 				final ObjectInputStream loader = new ObjectInputStream(new BufferedInputStream(new FileInputStream(new File(System.getProperty("user.home"), ".wikishell-wiki"))));
 				try {
 					wiki = (MediaWiki) loader.readObject();
 				} finally {
 					loader.close();
 				}
 			} finally {
 				workEnd();
 				if (wiki != null) {
 					System.err.println("Loaded wiki information from disk");
 				}
 			}
 		} catch (final FileNotFoundException e) {
 			// The application didn't last exit, or the user deleted his/her
 			// wiki information file.
 		} catch (final ClassNotFoundException e) {
 			System.err.println("Warning: Wiki information cannot be loaded");
 			System.err.println(e.getClass().getName() + ": " + e.getLocalizedMessage());
 		} catch (final IOException e) {
 			System.err.println("Warning: Wiki information cannot be loaded");
 			System.err.println(e.getClass().getName() + ": " + e.getLocalizedMessage());
 		}
 
 		if (wiki == null) {
 			System.err.println("Enter your initial wiki information.");
 
 			try {
 				wikiDetails: while (true) /*- wiki detail input retry loop */{
 					final CommandContext context = new CommandContext();
 
 					// Use the command-line details only for the first wiki
 					// connection attempt. If it fails due to IOException or
 					// MediaWikiException, that can be retried, but if the user
 					// says no to retrying, come back here to ask for wiki
 					// details.
 					if (args.length >= 2) {
 						context.essentialInput = args[0];
 						context.auxiliaryInput = args[1];
 						args = new String[0];
 					} else {
 						connect.getEssentialInput(context);
 						connect.getAuxiliaryInput(context);
 					}
 
 					reconnect: while (true) /*- wiki connection retry loop */{
 						try {
 							connect.perform(context);
 							wiki = context.wiki;
 
 							break wikiDetails; // on success
 						} catch (final IOException e) {
 							System.err.println(e.getClass().getName() + ": " + e.getLocalizedMessage());
 							if (!inputBoolean("Retry? [Y/n] ", true)) {
 								break reconnect;
 							}
 						} catch (final MediaWiki.MediaWikiException e) {
 							System.err.println(e.getClass().getName() + ": " + e.getLocalizedMessage());
 							if (!inputBoolean("Retry? [Y/n] ", true)) {
 								break reconnect;
 							}
 						}
 					}
 				}
 			} catch (final CancellationException e) {
 				// Cancelled the initial connection process.
 				System.exit(1);
 				return;
 			} catch (final NullPointerException e) {
 				// End of file received during the initial connection process.
 				System.err.println();
 				return;
 			} catch (final IOException e) {
 				e.printStackTrace();
 				System.exit(1);
 				return;
 			}
 		}
 
 		path.addLast("~");
 
 		work("Registering commands...");
 		try {
 			{
 				final Exit e = new Exit();
 				for (final String s : Arrays.asList("exit", "quit")) {
 					builtinCommands.put(s, e);
 				}
 			}
 			{
 				final Connect c = new Connect();
 				for (final String s : Arrays.asList("connect", "open")) {
 					builtinCommands.put(s, c);
 				}
 			}
 			builtinCommands.put("login", new Login());
 			builtinCommands.put("logout", new Logout());
 			{
 				final SetCompression c = new SetCompression();
 				for (final String s : Arrays.asList("compress", "compression")) {
 					builtinCommands.put(s, c);
 				}
 			}
 			{
 				final SetMaximumLag m = new SetMaximumLag();
 				for (final String s : Arrays.asList("maximumlag", "maxlag")) {
 					builtinCommands.put(s, m);
 				}
 			}
 			{
 				final Namespaces n = new Namespaces();
 				for (final String s : Arrays.asList("namespaces", "ns")) {
 					builtinCommands.put(s, n);
 				}
 			}
 			builtinCommands.put("parse", new Parse());
 			{
 				final SpecialPages p = new SpecialPages();
 				for (final String s : Arrays.asList("specialpages", "specialpage", "specials", "special")) {
 					builtinCommands.put(s, p);
 				}
 			}
 			{
 				final PageInformation p = new PageInformation();
 				for (final String s : Arrays.asList("pageinfo", "page")) {
 					builtinCommands.put(s, p);
 				}
 			}
 			{
 				final UserInformation u = new UserInformation();
 				for (final String s : Arrays.asList("userinfo", "user")) {
 					builtinCommands.put(s, u);
 				}
 			}
 			{
 				final RevisionInformation r = new RevisionInformation();
 				for (final String s : Arrays.asList("revinfo", "rev", "history", "hist")) {
 					builtinCommands.put(s, r);
 				}
 			}
 			{
 				final FileRevisionInformation f = new FileRevisionInformation();
 				for (final String s : Arrays.asList("imagerevinfo", "imagerev", "imageinfo", "fileinfo", "filerevinfo", "filerev", "imagehistory", "imagehist", "filehistory", "filehist")) {
 					builtinCommands.put(s, f);
 				}
 			}
 			{
 				final InterlanguageLinks i = new InterlanguageLinks();
 				for (final String s : Arrays.asList("languages", "langs", "languagelinks", "langlinks", "interlanguage", "interlang")) {
 					builtinCommands.put(s, i);
 				}
 			}
 			{
 				final Links l = new Links();
 				for (final String s : Arrays.asList("links", "linksin", "wikilinks", "wikilinksin")) {
 					builtinCommands.put(s, l);
 				}
 			}
 			{
 				final ExternalLinks e = new ExternalLinks();
 				for (final String s : Arrays.asList("externallinks", "extlinks")) {
 					builtinCommands.put(s, e);
 				}
 			}
 			{
 				final PageCategories p = new PageCategories();
 				for (final String s : Arrays.asList("categories", "cats", "pagecategories", "pagecats")) {
 					builtinCommands.put(s, p);
 				}
 			}
 			{
 				final TransclusionsInPage t = new TransclusionsInPage();
 				for (final String s : Arrays.asList("transclusionsin", "templatesin", "templates")) {
 					builtinCommands.put(s, t);
 				}
 			}
 			{
 				final TransclusionsOfPage t = new TransclusionsOfPage();
 				for (final String s : Arrays.asList("transclusionsof", "transclusions")) {
 					builtinCommands.put(s, t);
 				}
 			}
 			{
 				final UsesOfImage u = new UsesOfImage();
 				for (final String s : Arrays.asList("usesof", "imageusage", "imageuses")) {
 					builtinCommands.put(s, u);
 				}
 			}
 			{
 				final LinksToPage l = new LinksToPage();
 				for (final String s : Arrays.asList("linksto", "whatlinkshere")) {
 					builtinCommands.put(s, l);
 				}
 			}
 			{
 				final CategoryInformation c = new CategoryInformation();
 				for (final String s : Arrays.asList("categoryinfo", "category", "catinfo", "cat")) {
 					builtinCommands.put(s, c);
 				}
 			}
 			builtinCommands.put("read", new Read());
 			{
 				final ReadRevision r = new ReadRevision();
 				for (final String s : Arrays.asList("readrev", "readrevision", "revisioncontent", "revcontent", "revision")) {
 					builtinCommands.put(s, r);
 				}
 			}
 			{
 				final DownloadFileRevision d = new DownloadFileRevision();
 				for (final String s : Arrays.asList("download", "downloadimage", "downloadfile", "saveimage", "savefile", "imagedownload", "filedownload", "imagesave", "filesave")) {
 					builtinCommands.put(s, d);
 				}
 			}
 			{
 				final AllCategories a = new AllCategories();
 				for (final String s : Arrays.asList("allcategories", "allcats", "listcategories", "listcats")) {
 					builtinCommands.put(s, a);
 				}
 			}
 			{
 				final AllFiles a = new AllFiles();
 				for (final String s : Arrays.asList("allimages", "listimages", "allfiles", "listfiles")) {
 					builtinCommands.put(s, a);
 				}
 			}
 			{
 				final AllPages a = new AllPages();
 				for (final String s : Arrays.asList("allpages", "listpages")) {
 					builtinCommands.put(s, a);
 				}
 			}
 			{
 				final CategoryMembers c = new CategoryMembers();
 				for (final String s : Arrays.asList("categorymembers", "catmembers")) {
 					builtinCommands.put(s, c);
 				}
 			}
 			{
 				final AllUsers a = new AllUsers();
 				for (final String s : Arrays.asList("allusers", "listusers")) {
 					builtinCommands.put(s, a);
 				}
 			}
 			builtinCommands.put("purge", new Purge());
 			{
 				final ExpandTemplatesInWikitext e = new ExpandTemplatesInWikitext();
 				for (final String s : Arrays.asList("expandtemplates", "expand")) {
 					builtinCommands.put(s, e);
 				}
 			}
 			builtinCommands.put("newsection", new NewSection());
 			{
 				final ReplaceText r = new ReplaceText();
 				for (final String s : Arrays.asList("replacetext", "replace")) {
 					builtinCommands.put(s, r);
 				}
 			}
 			builtinCommands.put("replaceregex", new ReplaceRegex());
 			{
 				final AutoReplaceText a = new AutoReplaceText();
 				for (final String s : Arrays.asList("autoreplacetext", "autoreplace")) {
 					builtinCommands.put(s, a);
 				}
 			}
 			builtinCommands.put("autoreplaceregex", new AutoReplaceRegex());
 			builtinCommands.put("append", new AppendText());
 			builtinCommands.put("prepend", new PrependText());
 			{
 				final CreatePage c = new CreatePage();
 				for (final String s : Arrays.asList("createpage", "create")) {
 					builtinCommands.put(s, c);
 				}
 			}
 			builtinCommands.put("replacepage", new ReplacePage());
 			{
 				final CreateOrReplacePage c = new CreateOrReplacePage();
 				for (final String s : Arrays.asList("putpage", "put")) {
 					builtinCommands.put(s, c);
 				}
 			}
 			{
 				final MovePage m = new MovePage();
 				for (final String s : Arrays.asList("movepage", "move")) {
 					builtinCommands.put(s, m);
 				}
 			}
 			{
 				final UploadFile u = new UploadFile();
 				for (final String s : Arrays.asList("uploadimage", "uploadfile", "upload")) {
 					builtinCommands.put(s, u);
 				}
 			}
 			builtinCommands.put("rollback", new Rollback());
 			builtinCommands.put("undo", new Undo());
 			builtinCommands.put("delete", new Delete());
 			builtinCommands.put("protect", new Protect());
 			{
 				final UserGroupModification u = new UserGroupModification();
 				for (final String s : Arrays.asList("usergroups", "userrights")) {
 					builtinCommands.put(s, u);
 				}
 			}
 			builtinCommands.put("repeat", new UntilCancelledRepeat());
 			builtinCommands.put("for", new ForPages());
 			builtinCommands.put("pages", new Pages());
 			builtinCommands.put("count", new CountPages());
 			builtinCommands.put("help", new Help());
 			builtinCommands.put("commands", new CommandList());
 			builtinCommands.put("make", new MakeCommand());
 
 			{
 				final Lines l = new Lines();
 				for (final String s : Arrays.asList("lines", "less", "more")) {
 					commandPrefixes.put(s, l);
 				}
 			}
 
 			pageListModifiers.put("list", new ListPages());
 			{
 				final AddPages a = new AddPages();
 				for (final String s : Arrays.asList("add", "union")) {
 					pageListModifiers.put(s, a);
 				}
 			}
 			{
 				final RemovePages r = new RemovePages();
 				for (final String s : Arrays.asList("remove", "difference", "subtract")) {
 					pageListModifiers.put(s, r);
 				}
 			}
 			pageListModifiers.put("intersect", new IntersectPages());
 			{
 				final RemoveDuplicates r = new RemoveDuplicates();
 				for (final String s : Arrays.asList("removedups", "removedupes", "removeduplicates")) {
 					pageListModifiers.put(s, r);
 				}
 			}
 			{
 				final NormalizeNames n = new NormalizeNames();
 				for (final String s : Arrays.asList("normalize", "normalise")) {
 					pageListModifiers.put(s, n);
 				}
 			}
 		} finally {
 			workEnd();
 		}
 
 		try {
 			prompt: while (true) /*- prompt reissue loop */{
 				updateStatus(wiki);
 				prompt();
 
 				final String line = input("");
 				String[] tokens = line.split(" +", 2);
 
 				final List<Command> prefixes = new ArrayList<Command>();
 				while ((tokens.length > 0) && (tokens[0].length() > 0) && commandPrefixes.containsKey(tokens[0])) {
 					prefixes.add(commandPrefixes.get(tokens[0]));
 					tokens = tokens.length == 2 ? tokens[1].split(" +", 2) : new String[] { "" };
 				}
 
 				if ((tokens.length > 0) && (tokens[0].length() > 0)) {
 					final Command command = getCommand(tokens[0]);
 					if (command != null) {
 						final CommandContext context = getCommandContext(tokens[0]);
 
 						context.arguments = tokens.length > 1 ? tokens[1] : "";
 						context.wiki = wiki;
 						command.parseArguments(context);
 						try {
 							command.getPageName(context);
 							if (!getTokenWithRetry(command, context)) {
 								continue prompt;
 							}
 							command.getEssentialInput(context);
 							command.getAuxiliaryInput(context);
 							while (true) /*- command retry loop */{
 								try {
 									command.confirm(context);
 									for (final Command prefix : prefixes) {
 										prefix.perform(context);
 									}
 									command.perform(context);
 
 									wiki = context.wiki;
 									break; // on success
 								} catch (final MediaWiki.MediaWikiException e) {
 									System.err.println(e.getClass().getName() + ": " + e.getLocalizedMessage());
 									if (!inputBoolean("Retry? [Y/n] ", true)) {
 										continue prompt;
 									}
 									// Reconfirm the command. For edit conflicts
 									// and the like, we also need to get a new
 									// token.
 									context.confirmation = null;
 									if (context.token != null) {
 										context.token = null;
 										if (!getTokenWithRetry(command, context)) {
 											continue prompt;
 										}
 									}
 								} catch (final IOException e) {
 									System.err.println(e.getClass().getName() + ": " + e.getLocalizedMessage());
 									if (!inputBoolean("Retry? [Y/n] ", true)) {
 										continue prompt;
 									}
 								} catch (final ParseException e) {
 									System.err.println(e.getClass().getName() + ": " + e.getLocalizedMessage());
 									if (!inputBoolean("Retry? [Y/n] ", true)) {
 										continue prompt;
 									}
 								}
 							}
 						} catch (final CancellationException ce) {
 							continue prompt;
 						}
 					} else {
 						System.err.println(tokens[0] + ": No such command");
 					}
 				}
 			}
 		} catch (final CancellationException e) {
 			// we'd go back one level here, but that's not strictly necessary
 		} catch (final NullPointerException e) {
 			System.err.println();
 		} catch (final IOException e) {
 			e.printStackTrace();
 		} finally {
 			try {
 				// Avoid saving empty files if we got to the finally through
 				// Ctrl+C/SIGTERM.
 				Runtime.getRuntime().removeShutdownHook(new Thread());
 			} catch (IllegalStateException ise) {
 				System.err.println("Java VM shutting down");
 				return;
 			}
 			// Save the current MediaWiki to disk to be restored when the
 			// application next starts.
 			work("Saving wiki information to disk...");
 			try {
 				try {
 					final ObjectOutputStream saver = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(new File(System.getProperty("user.home"), ".wikishell-wiki"))));
 					try {
 						saver.writeObject(wiki);
 					} finally {
 						saver.close();
 					}
 				} finally {
 					workEnd();
 				}
 			} catch (final IOException e) {
 				System.err.println("Warning: Wiki information cannot be saved");
 				System.err.println(e.getClass().getName() + ": " + e.getLocalizedMessage());
 			}
 		}
 	}
 
 	public static String input(final String prompt) throws IOException, NullPointerException, CancellationException {
 		System.err.print(prompt);
 		final String line = keyboard.readLine();
 		if (line.equals("cancel"))
 			throw new CancellationException();
 		return line;
 	}
 
 	public static String inputMandatory(final String prompt) throws IOException, NullPointerException, CancellationException {
 		final String line = input(prompt);
 		if (line.length() == 0) {
 			System.err.println("Mandatory input not provided");
 			throw new CancellationException();
 		}
 		return line;
 	}
 
 	public static String input(final String prompt, final String defaultValue) throws IOException, NullPointerException, CancellationException {
 		final String line = input(prompt);
 		return line.length() > 0 ? line : defaultValue;
 	}
 
 	public static Boolean inputBoolean(final String prompt, final Boolean defaultValue) throws IOException, NullPointerException, CancellationException {
 		return inputBoolean(prompt, 'y', 'n', defaultValue);
 	}
 
 	public static String inputMultiline() throws IOException, NullPointerException, CancellationException {
 		return inputMultiline("-- Text: Start (Type '-- Text: End' to end the text)", "-- Text: End");
 	}
 
 	public static String inputMultiline(String prompt, String endText) throws IOException, NullPointerException, CancellationException {
 		System.err.println(prompt);
 		String line;
 		final StringBuilder text = new StringBuilder();
 		while (!(line = input("")).equalsIgnoreCase(endText)) {
 			text.append(line);
 			text.append("\r\n");
 		}
 		return text.toString();
 	}
 
 	public static Boolean inputBoolean(final String prompt, final char yes, final char no, final Boolean defaultValue) throws IOException, NullPointerException, CancellationException {
 		final String line = input(prompt);
 		if (line.length() > 0) {
 			if ((line.charAt(0) == Character.toLowerCase(yes)) || (line.charAt(0) == Character.toUpperCase(yes)))
 				return true;
 			if ((line.charAt(0) == Character.toLowerCase(no)) || (line.charAt(0) == Character.toUpperCase(no)))
 				return false;
 			return defaultValue;
 		} else
 			return defaultValue;
 	}
 
 	private static final Pattern magicWordMatcher = Pattern.compile("\\{\\{((?:FULL|BASE|SUB|SUBJECT|ARTICLE|TALK)?PAGENAME(?:E)?)\\}\\}");
 
 	public static String expandInput(CommandContext context, final String input) throws IOException, MediaWiki.MediaWikiException {
 		StringBuffer result = new StringBuffer();
 		Matcher m = magicWordMatcher.matcher(input);
 		if (m.find()) {
 			try {
 				work("Getting namespaces...");
 				MediaWiki.Namespaces namespaces = context.wiki.getNamespaces();
 				work(String.format("Normalising '%s'...", context.pageName));
 				String normalized = context.wiki.normalizeTitle(context.pageName);
 				work("Expanding input...");
 				do {
 					if (m.group(1).equals("PAGENAME")) {
 						m.appendReplacement(result, namespaces.removeNamespacePrefix(normalized));
 					} else if (m.group(1).equals("PAGENAMEE")) {
 						m.appendReplacement(result, pagenamee(namespaces.removeNamespacePrefix(normalized)));
 					} else if (m.group(1).equals("FULLPAGENAME")) {
 						m.appendReplacement(result, normalized);
 					} else if (m.group(1).equals("FULLPAGENAMEE")) {
 						m.appendReplacement(result, pagenamee(normalized));
 					} else if (m.group(1).equals("BASEPAGENAME")) {
 						if (namespaces.getNamespaceForPage(normalized).allowsSubpages() && normalized.indexOf('/') != -1) {
 							String noNamespace = namespaces.removeNamespacePrefix(normalized);
 							m.appendReplacement(result, noNamespace.substring(0, noNamespace.indexOf('/')));
 						} else
 							m.appendReplacement(result, "");
 					} else if (m.group(1).equals("BASEPAGENAMEE")) {
 						if (namespaces.getNamespaceForPage(normalized).allowsSubpages() && normalized.indexOf('/') != -1) {
 							String noNamespace = namespaces.removeNamespacePrefix(normalized);
 							m.appendReplacement(result, pagenamee(noNamespace.substring(0, noNamespace.indexOf('/'))));
 						} else
 							m.appendReplacement(result, "");
 					} else if (m.group(1).equals("SUBPAGENAME")) {
 						if (namespaces.getNamespaceForPage(normalized).allowsSubpages() && normalized.indexOf('/') != -1) {
 							m.appendReplacement(result, normalized.substring(normalized.lastIndexOf('/') + 1));
 						} else
 							m.appendReplacement(result, "");
 					} else if (m.group(1).equals("SUBPAGENAMEE")) {
 						if (namespaces.getNamespaceForPage(normalized).allowsSubpages() && normalized.indexOf('/') != -1) {
 							m.appendReplacement(result, pagenamee(normalized.substring(normalized.lastIndexOf('/') + 1)));
 						} else
 							m.appendReplacement(result, "");
 					} else if (m.group(1).equals("SUBJECTPAGENAME") || m.group(1).equals("ARTICLEPAGENAME")) {
 						long namespaceID = namespaces.getNamespaceForPage(normalized).getID();
 						if (namespaceID >= 0) {
 							// Remove the bit that makes the namespace ID odd.
 							MediaWiki.Namespace subjectNamespace = namespaces.getNamespace(namespaceID & ~1);
 							if (subjectNamespace != null)
 								m.appendReplacement(result, subjectNamespace.getFullPageName(namespaces.removeNamespacePrefix(normalized)));
 							else
 								m.appendReplacement(result, "");
 						} else
 							m.appendReplacement(result, "");
 					} else if (m.group(1).equals("SUBJECTPAGENAMEE") || m.group(1).equals("ARTICLEPAGENAMEE")) {
 						long namespaceID = namespaces.getNamespaceForPage(normalized).getID();
 						if (namespaceID >= 0) {
 							// Remove the bit that makes the namespace ID odd.
 							MediaWiki.Namespace subjectNamespace = namespaces.getNamespace(namespaceID & ~1);
 							if (subjectNamespace != null)
 								m.appendReplacement(result, pagenamee(subjectNamespace.getFullPageName(namespaces.removeNamespacePrefix(normalized))));
 							else
 								m.appendReplacement(result, "");
 						} else
 							m.appendReplacement(result, "");
 					} else if (m.group(1).equals("TALKPAGENAME")) {
 						long namespaceID = namespaces.getNamespaceForPage(normalized).getID();
 						if (namespaceID >= 0) {
 							// Add the bit that makes the namespace ID odd.
 							MediaWiki.Namespace talkNamespace = namespaces.getNamespace(namespaceID & 1);
 							if (talkNamespace != null)
 								m.appendReplacement(result, talkNamespace.getFullPageName(namespaces.removeNamespacePrefix(normalized)));
 							else
 								m.appendReplacement(result, "");
 						} else
 							m.appendReplacement(result, "");
 					} else if (m.group(1).equals("TALKPAGENAMEE")) {
 						long namespaceID = namespaces.getNamespaceForPage(normalized).getID();
 						if (namespaceID >= 0) {
 							// Add the bit that makes the namespace ID odd.
 							MediaWiki.Namespace talkNamespace = namespaces.getNamespace(namespaceID & 1);
 							if (talkNamespace != null)
 								m.appendReplacement(result, pagenamee(talkNamespace.getFullPageName(namespaces.removeNamespacePrefix(normalized))));
 							else
 								m.appendReplacement(result, "");
 						} else
 							m.appendReplacement(result, "");
 					}
 				} while (m.find());
 			} finally {
 				workEnd();
 			}
 			m.appendTail(result);
 			return result.toString();
 		} else
 			return input;
 	}
 
 	public static String pagenamee(final String input) {
 		StringBuilder result = new StringBuilder(input.length());
 		for (int i = 0; i < input.length(); i++) {
 			switch (input.charAt(i)) {
 			case ' ':
 				result.append('_');
 				break;
 			case '"':
 				result.append("%22");
 				break;
 			case '%':
 				result.append("%25");
 				break;
 			case '&':
 				result.append("%26");
 				break;
 			case '\'':
 				result.append("%27");
 				break;
 			case '+':
 				result.append("%2B");
 				break;
 			case '=':
 				result.append("%3D");
 				break;
 			case '?':
 				result.append("%3F");
 				break;
 			case '\\':
 				result.append("%5C");
 				break;
 			case '^':
 				result.append("%5E");
 				break;
 			case '`':
 				result.append("%60");
 				break;
 			case '~':
 				result.append("%7E");
 				break;
 			default:
 				result.append(input.charAt(i));
 			}
 		}
 		return result.toString();
 	}
 
 	public static void updateStatus(final MediaWiki wiki) {
 		work("Updating status...");
 		try {
 			host = wiki.getHostName();
 			try {
 				final MediaWiki.CurrentUser currentUser = wiki.getCurrentUser();
 				user = currentUser.getUserName();
 				sysop = currentUser.isInGroups("sysop");
 			} catch (final Throwable t) {
 				user = "";
 				sysop = false;
 			}
 		} finally {
 			workEnd();
 		}
 	}
 
 	public static void work(final String workLong) {
 		System.err.print(workLong);
 		if (WikiShell.workLong.length() > workLong.length()) {
 			for (int i = 0; i < (WikiShell.workLong.length() - workLong.length()); i++) {
 				System.err.print(' ');
 			}
 		}
 		System.err.print('\r');
 		WikiShell.workLong = workLong;
 	}
 
 	public static void workEnd() {
 		for (int i = 0; i < workLong.length(); i++) {
 			System.err.print(' ');
 		}
 		System.err.print('\r');
 		workLong = "";
 	}
 
 	public static void prompt() {
 		System.err.print(user.length() > 0 ? user : "???");
 		System.err.print('@');
 		System.err.print(host);
 		System.err.print(' ');
 		boolean first = true;
 		for (final String pathElement : path) {
 			if (first) {
 				first = false;
 			} else {
 				System.err.print('/');
 			}
 			System.err.print(pathElement);
 		}
 		System.err.print(sysop ? '#' : '$');
 		System.err.print(' ');
 	}
 
 	public interface Command {
 		/**
 		 * Parses the arguments contained in <code>context.arguments</code> into
 		 * the various other variables of the <code>context</code>. This method
 		 * is called first for a given command invocation.
 		 * 
 		 * @param context
 		 *            The context to use for this command invocation.
 		 */
 		void parseArguments(CommandContext context);
 
 		/**
 		 * If no page name is in <code>context.pageName</code> from
 		 * <code>parseArguments</code> and this <tt>Command</tt> requires a page
 		 * name to work, requests that page name from the user. This method is
 		 * called second for a given command invocation (after
 		 * <code>parseArguments</code>).
 		 * 
 		 * @param context
 		 *            The context to use for this command invocation.
 		 * @throws IOException
 		 *             if reading the page name from the console throws
 		 *             <tt>IOException</tt>
 		 * @throws NullPointerException
 		 *             if end of file is reached while reading the page name
 		 *             from the console
 		 * @throws CancellationException
 		 *             if <code>"cancel"</code> is read from the console
 		 */
 		void getPageName(CommandContext context) throws IOException, NullPointerException, CancellationException;
 
 		/**
 		 * If this <tt>Command</tt> requires a token from the wiki to work,
 		 * requests that token from the wiki. This method is called third for a
 		 * given command invocation (after <code>getPageName</code>).
 		 * 
 		 * @param context
 		 *            The context to use for this command invocation.
 		 * @throws IOException
 		 *             if reading the token from the wiki throws
 		 *             <tt>IOException</tt>
 		 * @throws MediaWiki.MediaWikiException
 		 *             if reading the token from the wiki throws
 		 *             <tt>MediaWiki.MediaWikiException</tt>
 		 * @throws ParseException
 		 *             if reading the token from the wiki throws
 		 *             <tt>ParseException</tt>
 		 */
 		void getToken(CommandContext context) throws IOException, MediaWiki.MediaWikiException, ParseException;
 
 		/**
 		 * If no essential input is in <code>context.essentialInput</code> from
 		 * <code>parseArguments</code> and this <tt>Command</tt> requires some
 		 * to work, requests that input from the user. This method is called
 		 * fourth for a given command invocation (after <code>getPageName</code>
 		 * and <code>getToken</code>).
 		 * <p>
 		 * Essential input is distinguished from <i>auxiliary input</i> by the
 		 * fact that essential input is more about data than metadata. For
 		 * example, section or page text, the target of a <tt>Move</tt> or the
 		 * local file for an <tt>UploadFile</tt> would be essential.
 		 * 
 		 * @param context
 		 *            The context to use for this command invocation.
 		 * @throws IOException
 		 *             if reading input from the console throws
 		 *             <tt>IOException</tt>
 		 * @throws NullPointerException
 		 *             if end of file is reached while reading input from the
 		 *             console
 		 * @throws CancellationException
 		 *             if <code>"cancel"</code> is read from the console
 		 */
 		void getEssentialInput(CommandContext context) throws IOException, NullPointerException, CancellationException;
 
 		/**
 		 * If no auxiliary input is in <code>context.auxiliaryInput</code> from
 		 * <code>parseArguments</code> and this <tt>Command</tt> requires some
 		 * to work, requests that input from the user. This method is called
 		 * fifth for a given command invocation (after
 		 * <code>getEssentialInput</code>).
 		 * <p>
 		 * Auxiliary input is distinguished from <i>essential input</i> by the
 		 * fact that auxiliary input is more about metadata than data. For
 		 * example, edit summaries, action comments or would be auxiliary.
 		 * 
 		 * @param context
 		 *            The context to use for this command invocation.
 		 * @throws IOException
 		 *             if reading input from the console throws
 		 *             <tt>IOException</tt>
 		 * @throws NullPointerException
 		 *             if end of file is reached while reading input from the
 		 *             console
 		 * @throws CancellationException
 		 *             if <code>"cancel"</code> is read from the console
 		 */
 		void getAuxiliaryInput(CommandContext context) throws IOException, NullPointerException, CancellationException;
 
 		/**
 		 * If this <tt>Command</tt> requires a confirmation from the user,
 		 * requests that confirmation from the user. This method is called sixth
 		 * for a given command invocation (after <code>getAuxiliaryInput</code>
 		 * ).
 		 * <p>
 		 * Particularly destructive commands should require confirmations in
 		 * this manner. Text replacement commands may also use this to perform a
 		 * replacement confirmation sequence with the user.
 		 * 
 		 * @param context
 		 *            The context to use for this command invocation.
 		 * @throws IOException
 		 *             if reading the confirmation from the console throws
 		 *             <tt>IOException</tt>
 		 * @throws NullPointerException
 		 *             if end of file is reached while reading the confirmation
 		 *             from the console
 		 * @throws CancellationException
 		 *             if <code>"cancel"</code> is read from the console
 		 */
 		void confirm(CommandContext context) throws IOException, NullPointerException, CancellationException;
 
 		/**
 		 * Performs the action represented by this <tt>Command</tt> with
 		 * information gathered in the given <code>context</code>. This method
 		 * is called seventh for a given command invocation (after
 		 * <code>confirm</code>).
 		 * 
 		 * @param context
 		 *            The context to use for this command invocation.
 		 * @throws IOException
 		 *             if reading or writing on the wiki throws
 		 *             <tt>IOException</tt>
 		 * @throws MediaWiki.MediaWikiException
 		 *             if reading or writing on the wiki throws
 		 *             <tt>MediaWiki.MediaWikiException</tt>
 		 * @throws ParseException
 		 *             if reading or writing on the wiki throws
 		 *             <tt>ParseException</tt>
 		 */
 		void perform(CommandContext context) throws IOException, MediaWiki.MediaWikiException, ParseException;
 
 		/**
 		 * Displays usage information for this <tt>Command</tt> on the standard
 		 * error stream (<code>System.err</code>).
 		 * 
 		 * @throws IOException
 		 *             if writing to the standard error stream throws
 		 *             <tt>IOException</tt>
 		 */
 		void help() throws IOException;
 	}
 
 	public interface IterableCommand extends Command {
 		/**
 		 * Returns an <tt>Iterator&lt;String&gt;</tt> that goes over the page
 		 * names returned by this <tt>IterableCommand</tt> with information
 		 * gathered in the given <code>context</code>. This method is called
 		 * seventh for a given iterable command invocation (after
 		 * <code>confirm</code>).
 		 * 
 		 * @param context
 		 *            The context to use for this iterable command invocation.
 		 * @throws IOException
 		 *             if reading from the wiki throws <tt>IOException</tt>
 		 * @throws MediaWiki.MediaWikiException
 		 *             if reading from the wiki throws
 		 *             <tt>MediaWiki.MediaWikiException</tt>
 		 * @throws ParseException
 		 *             if reading from the wiki throws <tt>ParseException</tt>
 		 */
 		Iterator<String> iterator(CommandContext context) throws IOException, MediaWiki.MediaWikiException, ParseException;
 	}
 
 	public interface PageListModifier {
 		/**
 		 * Modifies the given list of pages. May ask for user input.
 		 * <p>
 		 * Depending on the implementation, this method could add, remove or
 		 * keep pages according to a new pattern or list, change page names
 		 * according to a pattern, or keep and remove pages according to user
 		 * input.
 		 * 
 		 * @param pages
 		 *            The list of pages to be modified.
 		 * @param context
 		 *            The context to use for this page list modification.
 		 * @throws IOException
 		 *             if reading input from the console throws
 		 *             <tt>IOException</tt>
 		 * @throws NullPointerException
 		 *             if end of file is reached while reading input from the
 		 *             console
 		 * @throws CancellationException
 		 *             if <code>"cancel"</code> is read from the console
 		 */
 		void modify(List<String> pages, PageListModifierContext context) throws IOException, NullPointerException, CancellationException;
 
 		/**
 		 * Displays usage information for this <tt>PageListModifier</tt> on the
 		 * standard error stream (<code>System.err</code>).
 		 * 
 		 * @throws IOException
 		 *             if writing to the standard error stream throws
 		 *             <tt>IOException</tt>
 		 */
 		void help() throws IOException;
 	}
 
 	public static abstract class AbstractCommand implements Command {
 		public void parseArguments(final CommandContext context) {}
 
 		public void getPageName(final CommandContext context) throws IOException, NullPointerException, CancellationException {}
 
 		public void getToken(final CommandContext context) throws IOException, MediaWiki.MediaWikiException, ParseException {}
 
 		public void getEssentialInput(final CommandContext context) throws IOException, NullPointerException, CancellationException {}
 
 		public void getAuxiliaryInput(final CommandContext context) throws IOException, NullPointerException, CancellationException {}
 
 		public void confirm(final CommandContext context) throws IOException, NullPointerException, CancellationException {}
 	}
 
 	public static class CommandContext implements Serializable {
 		private static final long serialVersionUID = 1L;
 
 		public transient String arguments;
 
 		public String pageName;
 
 		public transient Object token, confirmation, temporary;
 
 		public Object essentialInput, auxiliaryInput;
 
 		public transient MediaWiki wiki;
 
 		public transient PrintWriter output;
 
 		public CommandContext() {
 			output = new PrintWriter(System.out, true /*- auto-flush */);
 		}
 	}
 
 	public static class PageListModifierContext {
 		public transient String arguments;
 
 		public transient MediaWiki wiki;
 
 		public transient PrintWriter output;
 
 		public PageListModifierContext() {
 			output = new PrintWriter(System.out, true /*- auto-flush */);
 		}
 	}
 
 	public static class Help extends AbstractCommand {
 		@Override
 		public void parseArguments(final CommandContext context) {
 			final String arguments = context.arguments.trim();
 			if (arguments.length() > 0) {
 				context.arguments = "";
 				context.essentialInput = arguments;
 			}
 		}
 
 		@Override
 		public void getEssentialInput(final CommandContext context) throws IOException, NullPointerException, CancellationException {
 			if (context.essentialInput != null)
 				return;
 			context.essentialInput = inputMandatory("command to get help on: ");
 		}
 
 		public void perform(final CommandContext context) throws IOException, MediaWikiException, ParseException {
 			final String commandName = (String) context.essentialInput;
 
 			Command command = getCommand(commandName);
 
 			if (command == null) {
 				command = commandPrefixes.get(commandName);
 			}
 
 			if (command != null) {
 				command.help();
 			} else {
 				// Maybe it's a 'for' subshell command...
 				final PageListModifier modifier = pageListModifiers.get(commandName);
 				if (modifier != null) {
 					modifier.help();
 				} else {
 					System.err.println(context.essentialInput + ": No such command");
 				}
 			}
 		}
 
 		public void help() throws IOException {
 			System.err.println("Displays help on a command.");
 			System.err.println();
 			System.err.println("help [<command>] | help help (this text)");
 			System.err.println();
 			System.err.println("The command is mandatory and will be requested if not provided. For a list of commands, use the command called 'commands'.");
 		}
 	}
 
 	private static Command getCommand(final String name) {
 		// 1. Builtins. (Done in this order to prevent shadowing of builtins.)
 		final Command result = builtinCommands.get(name);
 		if (result != null)
 			return result;
 		// 2. Custom command.
 		try {
 			final ObjectInputStream loader = new ObjectInputStream(new BufferedInputStream(new FileInputStream(new File(new File(System.getProperty("user.home"), ".wikishell-cmds"), name + ".wcom"))));
 			try {
 				final String builtinName = loader.readUTF();
 				return builtinCommands.get(builtinName);
 			} finally {
 				loader.close();
 			}
 		} catch (final IOException ioe) {
 			// eat
 			return null;
 		}
 	}
 
 	private static CommandContext getCommandContext(final String name) {
 		// 1. Builtins. (Done in this order to prevent shadowing of builtins.)
 		final Command result = builtinCommands.get(name);
 		if (result != null)
 			return new CommandContext();
 		// 2. Custom command.
 		try {
 			final ObjectInputStream loader = new ObjectInputStream(new BufferedInputStream(new FileInputStream(new File(new File(System.getProperty("user.home"), ".wikishell-cmds"), name + ".wcom"))));
 			try {
 				loader.readUTF(); // Dummy read to skip builtin name
 				return (CommandContext) loader.readObject();
 			} finally {
 				loader.close();
 			}
 		} catch (final ClassNotFoundException shouldNotHappen) {
 			// We know that the CommandContext class exists. Eat.
 			return new CommandContext();
 		} catch (final IOException ioe) {
 			// eat
 			return new CommandContext();
 		}
 	}
 
 	public static class CommandList extends AbstractCommand {
 		@SuppressWarnings("unchecked")
 		public void perform(final CommandContext context) throws IOException, MediaWikiException, ParseException {
 			final Map<Object, StringBuilder> commandAliases = new IdentityHashMap<Object, StringBuilder>(builtinCommands.size() + commandPrefixes.size() + pageListModifiers.size());
 			final Map<StringBuilder, String> suffixes = new IdentityHashMap<StringBuilder, String>(builtinCommands.size() + commandPrefixes.size() + pageListModifiers.size());
 
 			for (final Map<String, ?> map : Arrays.asList(builtinCommands, commandPrefixes, pageListModifiers)) {
 				for (final Map.Entry<String, ?> entry : map.entrySet()) {
 					StringBuilder aliasesForCommand = commandAliases.get(entry.getValue());
 					if (aliasesForCommand == null) {
 						aliasesForCommand = new StringBuilder(entry.getKey());
 						commandAliases.put(entry.getValue(), aliasesForCommand);
 					} else {
 						aliasesForCommand.append(", ").append(entry.getKey());
 					}
 					suffixes.put(aliasesForCommand, map == builtinCommands ? "(builtin)" : map == commandPrefixes ? "(prefix)" : map == pageListModifiers ? "('for' subshell)" : "");
 				}
 			}
 
 			final File[] customCommandFiles = new File(System.getProperty("user.home"), ".wikishell-cmds").listFiles(new FilenameFilter() {
 				public boolean accept(final File dir, final String name) {
 					return name.endsWith(".wcom") && !builtinCommands.containsKey(name.substring(0, name.length() - ".wcom".length()));
 				}
 			});
 
 			final List<String> lines = new ArrayList<String>();
 			for (final StringBuilder line : commandAliases.values()) {
 				lines.add(line.toString() + " " + suffixes.get(line));
 			}
 
 			if (customCommandFiles != null) { // .wikishell-cmds exists
 				for (final File customCommandFile : customCommandFiles) {
 					lines.add(customCommandFile.getName().substring(0, customCommandFile.getName().length() - ".wcom".length()) + " (custom)");
 				}
 			}
 
 			Collections.sort(lines);
 
 			for (final String line : lines) {
 				context.output.println(line);
 				if (context.output.checkError())
 					return;
 			}
 		}
 
 		public void help() throws IOException {
 			System.err.println("Displays a list of available commands.");
 			System.err.println();
 			System.err.println("commands");
 			System.err.println();
 			System.err.println("You may use 'help <command>' to get usage information on each of the commands listed.");
 		}
 	}
 
 	public static class Lines extends AbstractCommand implements PageListModifier {
 		public class LinesWriter extends PrintWriter {
 			private final PrintWriter inner;
 
 			private int currentLine, currentColumn;
 
 			private final int maxLines = 23, maxColumns = 80;
 
 			private boolean error, ignoreLF;
 
 			public LinesWriter(final PrintWriter inner) {
 				super(inner);
 				this.inner = inner;
 			}
 
 			@Override
 			public void flush() {
 				inner.flush();
 			}
 
 			@Override
 			public void close() {
 				inner.close();
 			}
 
 			@Override
 			public boolean checkError() {
 				return error;
 			}
 
 			@Override
 			protected void clearError() {
 				error = false;
 			}
 
 			@Override
 			protected void setError() {
 				error = true;
 			}
 
 			@Override
 			public void write(final int c) {
 				try {
 					writeChar((char) c);
 				} catch (final IOException e) {
 					setError();
 				}
 			}
 
 			@Override
 			public void write(final char[] buf, final int off, final int len) {
 				try {
 					for (int i = off; i < (off + len); i++) {
 						writeChar(buf[i]);
 					}
 				} catch (final IOException e) {
 					setError();
 				}
 			}
 
 			@Override
 			public void write(final char[] buf) {
 				write(buf, 0, buf.length);
 			}
 
 			@Override
 			public void write(final String s, final int off, final int len) {
 				try {
 					for (int i = off; i < (off + len); i++) {
 						writeChar(s.charAt(i));
 					}
 				} catch (final IOException e) {
 					setError();
 				}
 			}
 
 			@Override
 			public void write(final String s) {
 				write(s, 0, s.length());
 			}
 
 			@Override
 			public void print(final boolean b) {
 				write(Boolean.toString(b));
 			}
 
 			@Override
 			public void print(final char c) {
 				write(c);
 			}
 
 			@Override
 			public void print(final int i) {
 				write(Integer.toString(i));
 			}
 
 			@Override
 			public void print(final long l) {
 				write(Long.toString(l));
 			}
 
 			@Override
 			public void print(final float f) {
 				write(Float.toString(f));
 			}
 
 			@Override
 			public void print(final double d) {
 				write(Double.toString(d));
 			}
 
 			@Override
 			public void print(final char[] s) {
 				write(s);
 			}
 
 			@Override
 			public void print(final String s) {
 				write(s);
 			}
 
 			@Override
 			public void print(final Object obj) {
 				write(obj != null ? obj.toString() : "null");
 			}
 
 			@Override
 			public void println() {
 				write(System.getProperty("line.separator"));
 			}
 
 			@Override
 			public void println(final boolean x) {
 				print(x);
 				if (!checkError()) {
 					println();
 				}
 			}
 
 			@Override
 			public void println(final char x) {
 				print(x);
 				if (!checkError()) {
 					println();
 				}
 			}
 
 			@Override
 			public void println(final int x) {
 				print(x);
 				if (!checkError()) {
 					println();
 				}
 			}
 
 			@Override
 			public void println(final long x) {
 				print(x);
 				if (!checkError()) {
 					println();
 				}
 			}
 
 			@Override
 			public void println(final float x) {
 				print(x);
 				if (!checkError()) {
 					println();
 				}
 			}
 
 			@Override
 			public void println(final double x) {
 				print(x);
 				if (!checkError()) {
 					println();
 				}
 			}
 
 			@Override
 			public void println(final char[] x) {
 				print(x);
 				if (!checkError()) {
 					println();
 				}
 			}
 
 			@Override
 			public void println(final String x) {
 				print(x);
 				if (!checkError()) {
 					println();
 				}
 			}
 
 			@Override
 			public void println(final Object x) {
 				print(x);
 				if (!checkError()) {
 					println();
 				}
 			}
 
 			@Override
 			public PrintWriter printf(final String format, final Object... args) {
 				write(String.format(format, args));
 				return this;
 			}
 
 			@Override
 			public PrintWriter printf(final Locale l, final String format, final Object... args) {
 				write(String.format(l, format, args));
 				return this;
 			}
 
 			@Override
 			public PrintWriter format(final String format, final Object... args) {
 				write(String.format(format, args));
 				return this;
 			}
 
 			@Override
 			public PrintWriter format(final Locale l, final String format, final Object... args) {
 				write(String.format(l, format, args));
 				return this;
 			}
 
 			@Override
 			public PrintWriter append(final CharSequence csq) {
 				return append(csq, 0, csq.length());
 			}
 
 			@Override
 			public PrintWriter append(final CharSequence csq, final int start, final int end) {
 				try {
 					for (int i = start; i < end; i++) {
 						writeChar(csq.charAt(i));
 					}
 				} catch (final IOException e) {
 					setError();
 				}
 				return this;
 			}
 
 			@Override
 			public PrintWriter append(final char c) {
 				return inner.append(c);
 			}
 
 			protected void writeChar(final char c) throws IOException {
 				if (c == '\r') {
 					currentColumn = 0;
 					if ((currentLine + 1) >= maxLines) {
 						endOfScreen();
 					}
 					currentLine++;
 					ignoreLF = true;
 					inner.print(c);
 					inner.flush();
 					return;
 				} else if (c == '\n') {
 					if (!ignoreLF) {
 						currentColumn = 0;
 						if ((currentLine + 1) >= maxLines) {
 							endOfScreen();
 						}
 						currentLine++;
 					}
 					inner.print(c);
 					inner.flush();
 				} else if (c == '\t') {
 					if ((currentColumn + 8) >= maxColumns) {
 						if ((currentLine + 1) >= maxLines) {
 							endOfScreen();
 						}
 						currentLine++;
 						currentColumn = 0;
 					}
 					currentColumn += 8;
 					inner.print(c);
 					inner.flush();
 				} else {
 					if ((currentColumn + 1) >= maxColumns) {
 						if ((currentLine + 1) >= maxLines) {
 							endOfScreen();
 						}
 						currentLine++;
 						currentColumn = 0;
 					}
 					currentColumn++;
 					inner.print(c);
 					inner.flush();
 				}
 				ignoreLF = false;
 			}
 
 			protected void endOfScreen() throws IOException {
 				System.err.println();
 				final String in = input("-- More: Enter for a line, Space Enter for a screen, q Enter to stop -- ", "");
 				if (in.length() == 0) {
 					currentLine--;
 				} else if (in.charAt(0) == ' ') {
 					currentLine = 0;
 				} else if ((in.charAt(0) == 'q') || (in.charAt(0) == 'Q'))
 					throw new IOException("cancelled");
 				else {
 					currentLine = 0;
 				}
 			}
 		}
 
 		public void perform(final CommandContext context) throws IOException, MediaWikiException, ParseException {
 			context.output = new LinesWriter(new PrintWriter(System.out, true /*- auto-flush */));
 		}
 
 		public void modify(final List<String> pages, final PageListModifierContext context) throws IOException, NullPointerException, CancellationException {
 			context.output = new LinesWriter(new PrintWriter(System.out, true /*- auto-flush */));
 		}
 
 		public void help() throws IOException {
 			System.err.println("Displays the output of a command in screenfuls with a pause in-between.");
 			System.err.println();
 			System.err.println("{lines | less} <command> [<arguments>]");
 		}
 	}
 
 	public static class Connect extends AbstractCommand {
 		@Override
 		public void parseArguments(final CommandContext context) {
 			final String[] arguments = context.arguments.split(" +", 3);
 			context.arguments = arguments.length == 3 ? arguments[2] : "";
 
 			if (arguments.length >= 1) {
 				context.essentialInput = arguments[0];
 			}
 			if (arguments.length >= 2) {
 				context.auxiliaryInput = arguments[1];
 			}
 		}
 
 		@Override
 		public void getEssentialInput(final CommandContext context) throws IOException, NullPointerException, CancellationException {
 			if (context.essentialInput != null)
 				return;
 			context.essentialInput = inputMandatory("wiki host or IP: ");
 		}
 
 		@Override
 		public void getAuxiliaryInput(final CommandContext context) throws IOException, NullPointerException, CancellationException {
 			if (context.auxiliaryInput != null)
 				return;
 			context.auxiliaryInput = input("script path [/]: ", "/");
 		}
 
 		public void perform(final CommandContext context) throws IOException, MediaWiki.MediaWikiException {
 			final MediaWiki newWiki = new MediaWiki((String) context.essentialInput, (String) context.auxiliaryInput).setUsingCompression(true);
 
 			workLong = "Connecting to " + newWiki.getHostName() + "...";
 			System.err.print(workLong + "\r");
 			try {
 				work("Getting namespaces...");
 				newWiki.getNamespaces();
 				work("Getting special page aliases...");
 				newWiki.getSpecialPageAliases();
 				work("Getting interwiki prefixes...");
 				newWiki.getInterwikiPrefixes();
 			} finally {
 				workEnd();
 			}
 			System.err.println("Connected.");
 
 			if (context.wiki != null) {
 				try {
 					context.wiki.logOut();
 				} catch (final Throwable t) {
 					// Eat
 				}
 			}
 
 			context.wiki = newWiki;
 		}
 
 		public void help() throws IOException {
 			System.err.println("Connects to a different wiki, initially logged out from that wiki.");
 			System.err.println();
 			System.err.println("{connect | open} [<wiki host or IP> [<script path>]]");
 			System.err.println();
 			System.err.println("Both parameters are mandatory and will be requested if not provided. The wiki host or IP determines where to connect; the script path determines where the API is.");
 		}
 	}
 
 	public static class Login extends AbstractCommand {
 		@Override
 		public void parseArguments(final CommandContext context) {
 			final String arguments = context.arguments.trim();
 			if (arguments.length() > 0) {
 				context.arguments = "";
 				context.essentialInput = arguments;
 			}
 		}
 
 		@Override
 		public void getEssentialInput(final CommandContext context) throws IOException, NullPointerException, CancellationException {
 			if (context.essentialInput != null)
 				return;
 			context.essentialInput = inputMandatory("username: ");
 		}
 
 		@Override
 		public void getAuxiliaryInput(final CommandContext context) throws IOException, NullPointerException, CancellationException {
 			if (context.auxiliaryInput != null) {
 				context.auxiliaryInput = null;
 				System.err.println("Security note: Some input was available to the 'login' command for use as a password. It has been cleared, and you will be asked for your password again.");
 			}
 			Object c = null;
 			try {
 				c = System.console();
 			} catch (final NoSuchMethodError nsme) {
 				// no console method in System
 			}
 
 			if (c == null) {
 				System.err.println("Security note: The following prompt will SHOW the password you are entering. Do not enter your password if someone else is watching your monitor.");
 				System.err.print("password: ");
 				context.auxiliaryInput = keyboard.readLine().toCharArray();
 			} else {
 				final Console console = (Console) c;
 				System.err.print("password: ");
 				context.auxiliaryInput = console.readPassword();
 			}
 		}
 
 		public void perform(final CommandContext context) throws IOException, MediaWiki.MediaWikiException {
 			work("Logging in...");
 			try {
 				context.wiki.logIn((String) context.essentialInput, (char[]) context.auxiliaryInput);
 			} finally {
 				workEnd();
 			}
 		}
 
 		public void help() throws IOException {
 			System.err.println("Logs in to the current wiki.");
 			System.err.println();
 			System.err.println("login [<username>]");
 			System.err.println();
 			System.err.println("The username parameter is mandatory and will be requested if not provided. The password will be requested without echo for security, and a note will appear if your system does not have that capability.");
 		}
 	}
 
 	public static class Logout extends AbstractCommand {
 		public void perform(final CommandContext context) throws IOException, MediaWiki.MediaWikiException {
 			work("Logging out...");
 			try {
 				context.wiki.logOut();
 			} finally {
 				workEnd();
 			}
 		}
 
 		public void help() throws IOException {
 			System.err.println("Logs out from the current wiki.");
 			System.err.println();
 			System.err.println("logout");
 		}
 	}
 
 	public static class SetCompression extends AbstractCommand {
 		@Override
 		public void parseArguments(final CommandContext context) {
 			if (context.arguments.trim().length() > 0) {
 				context.arguments = context.arguments.trim();
 				final char c = context.arguments.charAt(0);
 				if ((c == 'y') || (c == 'Y')) {
 					context.essentialInput = true;
 					context.arguments = context.arguments.substring(1);
 				} else if ((c == 'n') || (c == 'N')) {
 					context.essentialInput = false;
 					context.arguments = context.arguments.substring(1);
 				}
 			}
 		}
 
 		@Override
 		public void getEssentialInput(final CommandContext context) throws IOException, NullPointerException, CancellationException {
 			if (context.essentialInput != null)
 				return;
 			context.essentialInput = inputBoolean("request response compression from the wiki [Y/n]: ", true);
 		}
 
 		public void perform(final CommandContext context) throws IOException, MediaWiki.MediaWikiException {
 			context.wiki.setUsingCompression((Boolean) context.essentialInput);
 			System.err.println(context.wiki.isUsingCompression() ? "Response compression is now enabled" : "Response compression is now disabled");
 		}
 
 		public void help() throws IOException {
 			System.err.println("Sets or unsets response compression on the current wiki.");
 			System.err.println();
 			System.err.println("{compress | compression} {'y' | 'n'}");
 			System.err.println();
 			System.err.println("The parameter is mandatory and will be requested if not provided. 'y' enables response compression; 'n' disables it. Enabling response compression improves response time over low-bandwidth links, but disabling it may be necessary to prevent CPU starvation if accessing a local wiki.");
 		}
 	}
 
 	public static class SetMaximumLag extends AbstractCommand {
 		@Override
 		public void parseArguments(final CommandContext context) {
 			if (context.arguments.trim().length() > 0) {
 				context.arguments = context.arguments.trim();
 				if (context.arguments.equalsIgnoreCase("none")) {
 					context.essentialInput = new Object[] { null };
 					context.arguments = "";
 				} else {
 					try {
 						context.essentialInput = new Object[] { Integer.valueOf(context.arguments) };
 						context.arguments = "";
 					} catch (final NumberFormatException nfe) {
 						// Ask the user in getEssentialInput
 					}
 				}
 			}
 		}
 
 		@Override
 		public void getEssentialInput(final CommandContext context) throws IOException, NullPointerException, CancellationException {
 			if (context.essentialInput != null)
 				return;
 			final String input = inputMandatory("maximum database lag in seconds, or none to disable [n/none]: ");
 
 			if (input.equalsIgnoreCase("none")) {
 				context.essentialInput = new Object[] { null };
 			} else {
 				try {
 					context.essentialInput = new Object[] { Integer.valueOf(input) };
 				} catch (final NumberFormatException nfe) {
 					System.err.println("Invalid input");
 					throw new CancellationException();
 				}
 			}
 		}
 
 		public void perform(final CommandContext context) throws IOException, MediaWiki.MediaWikiException {
 			context.wiki.setMaxLag((Integer) ((Object[]) context.essentialInput)[0]);
 			System.err.println(context.wiki.getMaxLag() != null ? String.format("Maximum lag is now %d seconds", context.wiki.getMaxLag()) : "Maximum lag is now disabled");
 		}
 
 		public void help() throws IOException {
 			System.err.println("Sets or unsets the maximum allowed database lag on the current wiki.");
 			System.err.println();
 			System.err.println("{maximumlag | maxlag} <number> | 'none'");
 			System.err.println();
 			System.err.println("The parameter is mandatory and will be requested if not provided. When performing actions on the wiki, if the database replication lag is longer than the given number of seconds on the wiki, the action will not be performed. You may retry the action until the database catches up.");
 		}
 	}
 
 	public static abstract class AbstractPageReadCommand extends AbstractCommand {
 		@Override
 		public void parseArguments(final CommandContext context) {
 			final String arguments = context.arguments.trim();
 			if (arguments.length() > 0) {
 				context.arguments = "";
 				context.pageName = arguments;
 			}
 		}
 
 		@Override
 		public void getPageName(final CommandContext context) throws IOException, NullPointerException, CancellationException {
 			if (context.pageName != null)
 				return;
 			context.pageName = inputMandatory("full page name: ");
 		}
 	}
 
 	public static class PageInformation extends AbstractPageReadCommand {
 		public void perform(final CommandContext context) throws IOException, MediaWiki.MediaWikiException, ParseException {
 			Iterator<MediaWiki.Page> pi;
 			work("Getting page information...");
 			try {
 				pi = context.wiki.getPageInformation(context.pageName);
 			} finally {
 				workEnd();
 			}
 
 			MediaWiki.Page p;
 			if ((p = next(pi, "Getting page information...")) != null) {
 				context.output.println(p.toString());
 				if (context.output.checkError())
 					return;
 			} else {
 				System.err.println(context.pageName + ": No API reply");
 			}
 		}
 
 		public void help() throws IOException {
 			System.err.println("Displays information about the last revision of given page on the current wiki.");
 			System.err.println();
 			System.err.println("pageinfo [<page name>]");
 			System.err.println();
 			System.err.println("The page name is mandatory and will be requested if not provided.");
 		}
 	}
 
 	public static class InterlanguageLinks extends AbstractPageReadCommand {
 		public void perform(final CommandContext context) throws IOException, MediaWiki.MediaWikiException, ParseException {
 			Iterator<MediaWiki.InterlanguageLink> li;
 			work("Getting interlanguage links...");
 			try {
 				li = context.wiki.getInterlanguageLinks(context.pageName);
 			} finally {
 				workEnd();
 			}
 
 			MediaWiki.InterlanguageLink l;
 			while ((l = next(li, "Getting interlanguage links...")) != null) {
 				context.output.println(l.getURL());
 				if (context.output.checkError())
 					return;
 			}
 		}
 
 		public void help() throws IOException {
 			System.err.println("Displays URLs of interlanguage versions of the given page on associated wikis.");
 			System.err.println();
 			System.err.println("interlang[uage][links] [<page name>]");
 			System.err.println();
 			System.err.println("The page name is mandatory and will be requested if not provided.");
 		}
 	}
 
 	public static class LinkToStringIterator implements Iterator<String> {
 		private final Iterator<MediaWiki.Link> li;
 
 		public LinkToStringIterator(final Iterator<MediaWiki.Link> li) {
 			this.li = li;
 		}
 
 		public boolean hasNext() {
 			return li.hasNext();
 		}
 
 		public String next() {
 			final MediaWiki.Link l = li.next();
 			return l != null ? l.getFullPageName() : null;
 		}
 
 		public void remove() {
 			li.remove();
 		}
 	}
 
 	public static class PageDesignationToStringIterator implements Iterator<String> {
 		private final Iterator<MediaWiki.PageDesignation> pi;
 
 		public PageDesignationToStringIterator(final Iterator<MediaWiki.PageDesignation> pi) {
 			this.pi = pi;
 		}
 
 		public boolean hasNext() {
 			return pi.hasNext();
 		}
 
 		public String next() {
 			final MediaWiki.PageDesignation p = pi.next();
 			return p != null ? p.getFullPageName() : null;
 		}
 
 		public void remove() {
 			pi.remove();
 		}
 	}
 
 	public static class Links extends AbstractPageReadCommand implements IterableCommand {
 		public void perform(final CommandContext context) throws IOException, MediaWiki.MediaWikiException, ParseException {
 			final Iterator<String> si = iterator(context);
 
 			String s;
 			while ((s = next(si, "Getting links...")) != null) {
 				context.output.println(s);
 				if (context.output.checkError())
 					return;
 			}
 		}
 
 		public Iterator<String> iterator(final CommandContext context) throws IOException, MediaWiki.MediaWikiException, ParseException {
 			Iterator<MediaWiki.Link> li;
 			work("Getting links...");
 			try {
 				li = context.wiki.getLinks(context.pageName);
 			} finally {
 				workEnd();
 			}
 
 			return new LinkToStringIterator(li);
 		}
 
 		public void help() throws IOException {
 			System.err.println("Displays the pages linked to by the given page on the current wiki.");
 			System.err.println();
 			System.err.println("[wiki]links[in] [<page name>]");
 			System.err.println();
 			System.err.println("The page name is mandatory and will be requested if not provided.");
 			System.err.println();
 			System.err.println("This command can be used as input to the 'for' command.");
 		}
 	}
 
 	public static class ExternalLinks extends AbstractPageReadCommand {
 		public void perform(final CommandContext context) throws IOException, MediaWiki.MediaWikiException, ParseException {
 			Iterator<String> li;
 			work("Getting external links...");
 			try {
 				li = context.wiki.getExternalLinks(context.pageName);
 			} finally {
 				workEnd();
 			}
 
 			String l;
 			while ((l = next(li, "Getting external links...")) != null) {
 				context.output.println(l);
 				if (context.output.checkError())
 					return;
 			}
 		}
 
 		public void help() throws IOException {
 			System.err.println("Displays the Web pages linked to by the given page on the current wiki.");
 			System.err.println();
 			System.err.println("ext[ernal]links [<page name>]");
 			System.err.println();
 			System.err.println("The page name is mandatory and will be requested if not provided.");
 		}
 	}
 
 	public static class TransclusionsInPage extends AbstractPageReadCommand implements IterableCommand {
 		@Override
 		public void getPageName(final CommandContext context) throws IOException, NullPointerException, CancellationException {
 			if (context.pageName != null)
 				return;
 			context.pageName = inputMandatory("full transcluding page name: ");
 		}
 
 		public void perform(final CommandContext context) throws IOException, MediaWiki.MediaWikiException, ParseException {
 			final Iterator<String> si = iterator(context);
 
 			String s;
 			while ((s = next(si, "Getting transclusions...")) != null) {
 				context.output.println(s);
 				if (context.output.checkError())
 					return;
 			}
 		}
 
 		public Iterator<String> iterator(final CommandContext context) throws IOException, MediaWiki.MediaWikiException, ParseException {
 			Iterator<MediaWiki.Link> li;
 			work("Getting transclusions...");
 			try {
 				li = context.wiki.getTransclusions(context.pageName);
 			} finally {
 				workEnd();
 			}
 
 			return new LinkToStringIterator(li);
 		}
 
 		public void help() throws IOException {
 			System.err.println("Displays the pages transcluded by the given page on the current wiki.");
 			System.err.println();
 			System.err.println("{transclusionsin | templates[in]} [<page name>]");
 			System.err.println();
 			System.err.println("The page name is mandatory and will be requested if not provided.");
 			System.err.println();
 			System.err.println("This command can be used as input to the 'for' command.");
 		}
 	}
 
 	public static class TransclusionsOfPage extends AbstractPageReadCommand implements IterableCommand {
 		@Override
 		public void getPageName(final CommandContext context) throws IOException, NullPointerException, CancellationException {
 			if (context.pageName != null)
 				return;
 			context.pageName = inputMandatory("full transcluded page name: ");
 		}
 
 		@Override
 		public void getAuxiliaryInput(final CommandContext context) throws IOException, NullPointerException, CancellationException {
 			if (context.auxiliaryInput != null)
 				return;
 			final String namespaceString = input("require transcluders to be in namespace name or number (<all>): ", null);
 			final Boolean redirect = inputBoolean("require transcluders to be redirects or not [y/n] (<don't care>): ", null);
 			context.auxiliaryInput = new Object[] { namespaceString, redirect };
 		}
 
 		public void perform(final CommandContext context) throws IOException, MediaWiki.MediaWikiException, CancellationException, NullPointerException, ParseException {
 			final Iterator<String> si = iterator(context);
 
 			String s;
 			while ((s = next(si, "Getting pages...")) != null) {
 				context.output.println(s);
 				if (context.output.checkError())
 					return;
 			}
 		}
 
 		public Iterator<String> iterator(final CommandContext context) throws IOException, MediaWiki.MediaWikiException, ParseException {
 			long[] namespaceIDs = null;
 			final Object[] auxiliaryInput = (Object[]) context.auxiliaryInput;
 			final String namespaceString = (String) auxiliaryInput[0];
 			final Boolean redirect = (Boolean) auxiliaryInput[1];
 
 			if (namespaceString != null) {
 				final MediaWiki.Namespaces namespaces = context.wiki.getNamespaces();
 				MediaWiki.Namespace namespace;
 				try {
 					namespace = namespaces.getNamespace(Long.parseLong(namespaceString));
 				} catch (final NumberFormatException nfe) {
 					namespace = namespaces.getNamespace(namespaceString);
 				}
 				if (namespace != null) {
 					namespaceIDs = new long[] { namespace.getID() };
 				} else {
 					System.err.println(namespaceString + ": No such namespace");
 					throw new CancellationException();
 				}
 			}
 
 			final Iterator<MediaWiki.PageDesignation> pi;
 
 			work("Getting pages...");
 			try {
 				pi = context.wiki.getPagesTranscluding(context.pageName, redirect, namespaceIDs);
 			} finally {
 				workEnd();
 			}
 
 			return new PageDesignationToStringIterator(pi);
 		}
 
 		public void help() throws IOException {
 			System.err.println("Displays the pages that transclude the given page on the current wiki.");
 			System.err.println();
 			System.err.println("transclusions[of] [<page name>]");
 			System.err.println();
 			System.err.println("The page name is mandatory and will be requested if not provided. Most often, the page will be in the Template namespace, but Template is not implied, because it is possible to transclude a page from any namespace.");
 			System.err.println();
 			System.err.println("This command can be used as input to the 'for' command.");
 		}
 	}
 
 	public static class UsesOfImage extends AbstractPageReadCommand implements IterableCommand {
 		@Override
 		public void getPageName(final CommandContext context) throws IOException, NullPointerException, CancellationException {
 			if (context.pageName != null)
 				return;
 			context.pageName = inputMandatory("full image name: ");
 		}
 
 		@Override
 		public void getAuxiliaryInput(final CommandContext context) throws IOException, NullPointerException, CancellationException {
 			if (context.auxiliaryInput != null)
 				return;
 			final String namespaceString = input("report image uses in namespace name or number (<all>): ", null);
 			final Boolean redirect = inputBoolean("report image uses only in redirect pages or not [y/n] (<don't care>): ", null);
 			context.auxiliaryInput = new Object[] { namespaceString, redirect };
 		}
 
 		public void perform(final CommandContext context) throws IOException, MediaWiki.MediaWikiException, CancellationException, NullPointerException, ParseException {
 			final Iterator<String> si = iterator(context);
 
 			String s;
 			while ((s = next(si, "Getting pages...")) != null) {
 				context.output.println(s);
 				if (context.output.checkError())
 					return;
 			}
 		}
 
 		public Iterator<String> iterator(final CommandContext context) throws IOException, MediaWiki.MediaWikiException, ParseException {
 			final MediaWiki.Namespaces namespaces = context.wiki.getNamespaces();
 			if (namespaces.getNamespaceForPage(context.pageName).getID() != MediaWiki.StandardNamespace.FILE) {
 				context.pageName = namespaces.getNamespace(MediaWiki.StandardNamespace.FILE).getFullPageName(namespaces.removeNamespacePrefix(context.pageName));
 				System.err.println(String.format("Note: Page name changed to %s", context.pageName));
 			}
 
 			long[] namespaceIDs = null;
 			final Object[] auxiliaryInput = (Object[]) context.auxiliaryInput;
 			final String namespaceString = (String) auxiliaryInput[0];
 			final Boolean redirect = (Boolean) auxiliaryInput[1];
 
 			if (namespaceString != null) {
 				MediaWiki.Namespace namespace;
 				try {
 					namespace = namespaces.getNamespace(Long.parseLong(namespaceString));
 				} catch (final NumberFormatException nfe) {
 					namespace = namespaces.getNamespace(namespaceString);
 				}
 				if (namespace != null) {
 					namespaceIDs = new long[] { namespace.getID() };
 				} else {
 					System.err.println(namespaceString + ": No such namespace");
 					throw new CancellationException();
 				}
 			}
 
 			final Iterator<MediaWiki.PageDesignation> pi;
 
 			work("Getting pages...");
 			try {
 				pi = context.wiki.getPagesUsingImage(context.pageName, redirect, namespaceIDs);
 			} finally {
 				workEnd();
 			}
 
 			return new PageDesignationToStringIterator(pi);
 		}
 
 		public void help() throws IOException {
 			System.err.println("Displays the pages that use the given file as an image on the current wiki.");
 			System.err.println();
 			System.err.println("{usesof | imageusage | imageuses} [<file name>]");
 			System.err.println();
 			System.err.println("The file name is mandatory and will be requested if not provided.");
 			System.err.println();
 			System.err.println("This command can be used as input to the 'for' command.");
 		}
 	}
 
 	public static class LinksToPage extends AbstractPageReadCommand implements IterableCommand {
 		@Override
 		public void getPageName(final CommandContext context) throws IOException, NullPointerException, CancellationException {
 			if (context.pageName != null)
 				return;
 			context.pageName = inputMandatory("full target page name: ");
 		}
 
 		@Override
 		public void getAuxiliaryInput(final CommandContext context) throws IOException, NullPointerException, CancellationException {
 			if (context.auxiliaryInput != null)
 				return;
 			final String namespaceString = input("report linking pages in namespace name or number (<all>): ", null);
 			final Boolean redirect = inputBoolean("require linking pages to be redirects or not [y/n] (<don't care>): ", null);
 			context.auxiliaryInput = new Object[] { namespaceString, redirect };
 		}
 
 		public void perform(final CommandContext context) throws IOException, MediaWiki.MediaWikiException, CancellationException, NullPointerException, ParseException {
 			final Iterator<String> si = iterator(context);
 
 			String s;
 			while ((s = next(si, "Getting pages...")) != null) {
 				context.output.println(s);
 				if (context.output.checkError())
 					return;
 			}
 		}
 
 		public Iterator<String> iterator(final CommandContext context) throws IOException, MediaWiki.MediaWikiException, ParseException {
 			long[] namespaceIDs = null;
 			final Object[] auxiliaryInput = (Object[]) context.auxiliaryInput;
 			final Boolean redirect = (Boolean) auxiliaryInput[1];
 			final String namespaceString = (String) auxiliaryInput[0];
 
 			if (namespaceString != null) {
 				final MediaWiki.Namespaces namespaces = context.wiki.getNamespaces();
 				MediaWiki.Namespace namespace;
 				try {
 					namespace = namespaces.getNamespace(Long.parseLong(namespaceString));
 				} catch (final NumberFormatException nfe) {
 					namespace = namespaces.getNamespace(namespaceString);
 				}
 				if (namespace != null) {
 					namespaceIDs = new long[] { namespace.getID() };
 				} else {
 					System.err.println(namespaceString + ": No such namespace");
 					throw new CancellationException();
 				}
 			}
 
 			final Iterator<MediaWiki.PageDesignation> pi;
 
 			work("Getting pages...");
 			try {
 				pi = context.wiki.getPagesLinkingTo(context.pageName, redirect, namespaceIDs);
 			} finally {
 				workEnd();
 			}
 
 			return new PageDesignationToStringIterator(pi);
 		}
 
 		public void help() throws IOException {
 			System.err.println("Displays the pages that use the given file as an image on the current wiki.");
 			System.err.println();
 			System.err.println("{usesof | imageusage | imageuses} [<file name>]");
 			System.err.println();
 			System.err.println("The file name is mandatory and will be requested if not provided.");
 			System.err.println();
 			System.err.println("This command can be used as input to the 'for' command.");
 		}
 	}
 
 	public static class CategoryInformation extends AbstractPageReadCommand {
 		public void perform(final CommandContext context) throws IOException, MediaWiki.MediaWikiException, ParseException {
 			final MediaWiki.Namespaces namespaces = context.wiki.getNamespaces();
 			if (namespaces.getNamespaceForPage(context.pageName).getID() != MediaWiki.StandardNamespace.CATEGORY) {
 				context.pageName = namespaces.getNamespace(MediaWiki.StandardNamespace.CATEGORY).getFullPageName(namespaces.removeNamespacePrefix(context.pageName));
 				System.err.println(String.format("Note: Page name changed to %s", context.pageName));
 			}
 
 			Iterator<MediaWiki.Category> ci;
 			work("Getting category information...");
 			try {
 				ci = context.wiki.getCategoryInformation(context.pageName);
 			} finally {
 				workEnd();
 			}
 
 			final MediaWiki.Category c = next(ci);
 			if (c != null) {
 				context.output.println(c);
 				if (context.output.checkError())
 					return;
 			} else {
 				System.err.println(context.pageName + ": No such category");
 			}
 		}
 
 		public void help() throws IOException {
 			System.err.println("Displays information about the given category on the current wiki.");
 			System.err.println();
 			System.err.println("cat[egory][info] [<page name>]");
 			System.err.println();
 			System.err.println("The page name is mandatory and will be requested if not provided. The Category namespace is implied.");
 		}
 	}
 
 	public static class UserInformation extends AbstractPageReadCommand {
 		@Override
 		public void getPageName(final CommandContext context) throws IOException, NullPointerException, CancellationException {
 			if (context.pageName != null)
 				return;
 			context.pageName = inputMandatory("user name: ");
 		}
 
 		public void perform(final CommandContext context) throws IOException, MediaWiki.MediaWikiException, ParseException {
 			Iterator<MediaWiki.User> ui;
 			work("Getting user information...");
 			try {
 				ui = context.wiki.getUserInformation(context.pageName);
 			} finally {
 				workEnd();
 			}
 
 			MediaWiki.User u;
 			if ((u = next(ui, "Getting user information...")) != null) {
 				if (u.isMissing()) {
 					System.err.println(context.pageName + ": No such user");
 				} else {
 					displayUser(u, context.output);
 				}
 			} else {
 				System.err.println("No API reply");
 			}
 		}
 
 		public void help() throws IOException {
 			System.err.println("Displays information about the given user on the current wiki.");
 			System.err.println();
 			System.err.println("user[info] [<user name>]");
 			System.err.println();
 			System.err.println("The user name is mandatory and will be requested if not provided. The User namespace is not implied.");
 		}
 	}
 
 	public static class Namespaces extends AbstractPageReadCommand {
 		@Override
 		public void getPageName(final CommandContext context) throws IOException, NullPointerException, CancellationException {
 			if (context.pageName != null)
 				return;
 			context.pageName = input("namespace name or number (<all>): ");
 		}
 
 		public void perform(final CommandContext context) throws IOException, MediaWiki.MediaWikiException {
 			final MediaWiki.Namespaces namespaces = context.wiki.getNamespaces();
 			if (context.pageName.length() > 0) {
 				MediaWiki.Namespace namespace;
 				try {
 					namespace = namespaces.getNamespace(Long.parseLong(context.pageName));
 				} catch (final NumberFormatException nfe) {
 					namespace = namespaces.getNamespace(context.pageName);
 				}
 				if (namespace != null) {
 					context.output.format("%4d %-20s %-7s %-8s %-4s", namespace.getID(), namespace.getCanonicalName(), namespace.isContent() ? "content" : "", namespace.allowsSubpages() ? "subpages" : "", namespace.isCaseSensitive() ? "case" : "");
 					context.output.println();
 					if (context.output.checkError())
 						return;
 					for (final String alias : namespace.getAliases()) {
 						context.output.println("     " + alias);
 						if (context.output.checkError())
 							return;
 					}
 				} else {
 					System.err.println(context.pageName + ": No such namespace");
 				}
 			} else {
 				final List<MediaWiki.Namespace> namespacesList = new ArrayList<MediaWiki.Namespace>(namespaces.getList());
 				Collections.sort(namespacesList, new Comparator<MediaWiki.Namespace>() {
 					public int compare(final MediaWiki.Namespace o1, final MediaWiki.Namespace o2) {
 						return Long.valueOf(o1.getID()).compareTo(Long.valueOf(o2.getID()));
 					}
 				});
 				for (final MediaWiki.Namespace namespace : namespacesList) {
 					context.output.format("%4d %-20s %-7s %-8s %-4s", namespace.getID(), namespace.getCanonicalName(), namespace.isContent() ? "content" : "", namespace.allowsSubpages() ? "subpages" : "", namespace.isCaseSensitive() ? "case" : "");
 					context.output.println();
 					if (context.output.checkError())
 						return;
 					for (final String alias : namespace.getAliases()) {
 						context.output.println("     " + alias);
 						if (context.output.checkError())
 							return;
 					}
 				}
 			}
 		}
 
 		public void help() throws IOException {
 			System.err.println("Displays information about the namespaces on the current wiki.");
 			System.err.println();
 			System.err.println("{namespaces | ns} [<namespace number or name>]");
 			System.err.println();
 			System.err.println("The namespace number or name is optional. If provided, only information about the given namespace is displayed.");
 		}
 	}
 
 	public static class Parse extends AbstractCommand {
 		@Override
 		public void getEssentialInput(final CommandContext context) throws IOException, NullPointerException, CancellationException {
 			if (context.essentialInput != null)
 				return;
 			String text = inputMultiline();
 			context.essentialInput = text;
 		}
 
 		@Override
 		public void getAuxiliaryInput(final CommandContext context) throws IOException, NullPointerException, CancellationException {
 			if (context.auxiliaryInput != null)
 				return;
 			final String fullPageName = input("as if it were in this page (<API>): ", null);
 			final String languageCode = input("as if the page were written in this language [language code] (<none>): ", null);
 
 			context.auxiliaryInput = new Object[] { fullPageName, languageCode };
 		}
 
 		public void perform(final CommandContext context) throws IOException, MediaWiki.MediaWikiException, ParseException {
 			final String text = (String) context.essentialInput;
 			final Object[] auxiliaryInput = (Object[]) context.auxiliaryInput;
 			final String fullPageName = (String) auxiliaryInput[0], languageCode = (String) auxiliaryInput[1];
 
 			MediaWiki.ParseResult result;
 
 			work("Parsing...");
 			try {
 				result = context.wiki.parseWikitext(text, fullPageName, true /*- preSaveTransform */, languageCode);
 			} finally {
 				workEnd();
 			}
 
 			context.output.println("-- HTML text: Start");
 			if (context.output.checkError())
 				return;
 			context.output.println(result.getText());
 			if (context.output.checkError())
 				return;
 			context.output.println("-- HTML text: End");
 			if (context.output.checkError())
 				return;
 
 			context.output.println("Links to pages on this wiki" + (result.getPageLinks().isEmpty() ? " (none)" : ":"));
 			if (context.output.checkError())
 				return;
 			for (MediaWiki.Link l : result.getPageLinks()) {
 				context.output.println(l.getFullPageName());
 				if (context.output.checkError())
 					return;
 			}
 
 			context.output.println("Images used" + (result.getImages().isEmpty() ? " (none)" : ":"));
 			if (context.output.checkError())
 				return;
 			for (MediaWiki.Link l : result.getImages()) {
 				context.output.println(l.getFullPageName());
 				if (context.output.checkError())
 					return;
 			}
 
 			context.output.println("Member of categories" + (result.getCategoryMemberships().isEmpty() ? " (none)" : ":"));
 			if (context.output.checkError())
 				return;
 			Iterator<MediaWiki.CategoryMembership> ci = result.getCategoryMemberships().iterator();
 
 			MediaWiki.CategoryMembership c = next(ci);
 			if (c != null) {
 				context.output.print(c.getCategoryBaseName());
 				if (context.output.checkError())
 					return;
 
 				while ((c = next(ci)) != null) {
 					context.output.print(" | ");
 					if (context.output.checkError())
 						return;
 					context.output.print(c.getCategoryBaseName());
 					if (context.output.checkError())
 						return;
 				}
 				context.output.println();
 				if (context.output.checkError())
 					return;
 			}
 
 			context.output.println("Transclusions of templates on this wiki" + (result.getTransclusions().isEmpty() ? " (none)" : ":"));
 			if (context.output.checkError())
 				return;
 			for (MediaWiki.Link l : result.getTransclusions()) {
 				context.output.println(l.getFullPageName());
 				if (context.output.checkError())
 					return;
 			}
 
 			context.output.println("Interlanguage links" + (result.getInterlanguageLinks().isEmpty() ? " (none)" : ":"));
 			if (context.output.checkError())
 				return;
 			for (MediaWiki.InterlanguageLink l : result.getInterlanguageLinks()) {
 				context.output.println(l.getURL());
 				if (context.output.checkError())
 					return;
 			}
 
 			context.output.println("Web links" + (result.getTransclusions().isEmpty() ? " (none)" : ":"));
 			if (context.output.checkError())
 				return;
 			for (String s : result.getExternalLinks()) {
 				context.output.println(s);
 				if (context.output.checkError())
 					return;
 			}
 		}
 
 		public void help() throws IOException {
 			System.err.println("Parses wikitext to HTML on the current wiki.");
 			System.err.println();
 			System.err.println("parse");
 			System.err.println();
 			System.err.println("You will be asked to provide the wikitext to be parsed, followed by the full name of the page on which it would appear and the language code to be used.");
 		}
 	}
 
 	public static class SpecialPages extends AbstractPageReadCommand {
 		@Override
 		public void getPageName(final CommandContext context) throws IOException, NullPointerException, CancellationException {
 			if (context.pageName != null)
 				return;
 			context.pageName = input("special page name (<all>): ");
 		}
 
 		public void perform(final CommandContext context) throws IOException, MediaWiki.MediaWikiException {
 
 			final Map<String, String> specialPageAliases = context.wiki.getSpecialPageAliases();
 
 			if (context.pageName.length() > 0) {
 				final MediaWiki.Namespaces namespaces = context.wiki.getNamespaces();
 				if (namespaces.getNamespaceForPage(context.pageName).getID() != MediaWiki.StandardNamespace.SPECIAL) {
 					context.pageName = namespaces.getNamespace(MediaWiki.StandardNamespace.SPECIAL).getFullPageName(namespaces.removeNamespacePrefix(context.pageName));
 					System.err.println(String.format("Note: Page name changed to %s", context.pageName));
 				}
 
 				final String specialPage = specialPageAliases.get(context.pageName);
 				if (specialPage != null) {
 					if (specialPage.equals(context.pageName)) {
 						context.output.println(String.format("%s is a valid special page", context.pageName));
 					} else {
						context.output.println(String.format("%1$s is an alias of %2$s", context.pageName, specialPage));
 					}
 				} else {
 					System.err.println(context.pageName + ": No such special page");
 				}
 			} else {
 				final Map<String, Set<String>> reversedAliases = new HashMap<String, Set<String>>();
 				for (final Map.Entry<String, String> entry : specialPageAliases.entrySet()) {
 					final String canonicalName = entry.getValue(), alias = entry.getKey();
 
 					Set<String> aliasesForName = reversedAliases.get(canonicalName);
 					if (aliasesForName == null) {
 						aliasesForName = new TreeSet<String>();
 						reversedAliases.put(canonicalName, aliasesForName);
 					}
 					if (!alias.equals(canonicalName)) {
 						aliasesForName.add(alias);
 					}
 				}
 				for (final Map.Entry<String, Set<String>> entry : reversedAliases.entrySet()) {
 					context.output.println(entry.getKey());
 					if (context.output.checkError())
 						return;
 					for (final String alias : entry.getValue()) {
 						context.output.println(" |- " + alias);
 						if (context.output.checkError())
 							return;
 					}
 				}
 			}
 		}
 
 		public void help() throws IOException {
 			System.err.println("Displays information about the special pages on the current wiki.");
 			System.err.println();
 			System.err.println("special[page][s] [<special page name>]");
 			System.err.println();
 			System.err.println("The special page name is optional. If provided, only information about the given special page is displayed.");
 		}
 	}
 
 	public static class RevisionInformation extends AbstractPageReadCommand {
 		@Override
 		public void parseArguments(final CommandContext context) {
 			final String arguments = context.arguments.trim();
 			if (arguments.length() > 0) {
 				Object start = null, end = null;
 
 				final String[] tokens = arguments.split(" +");
 
 				int i = 0;
 				for (; (i < tokens.length) && (tokens[i].charAt(0) == '-'); i++) {
 					final String token = tokens[i];
 
 					if (token.equalsIgnoreCase("-start")) {
 						i++;
 						if (i < tokens.length) {
 							try {
 								start = MediaWiki.timestampToDate(tokens[i]);
 							} catch (final ParseException e) {
 								try {
 									start = Long.valueOf(tokens[i]);
 								} catch (final NumberFormatException nfe) {
 									System.err.println("Invalid input for -start");
 									throw new CancellationException();
 								}
 							}
 						}
 					} else if (token.equalsIgnoreCase("-end")) {
 						i++;
 						if (i < tokens.length) {
 							try {
 								end = MediaWiki.timestampToDate(tokens[i]);
 							} catch (final ParseException e) {
 								try {
 									end = Long.valueOf(tokens[i]);
 								} catch (final NumberFormatException nfe) {
 									System.err.println("Invalid input for -end");
 									throw new CancellationException();
 								}
 							}
 						}
 					}
 				}
 				context.auxiliaryInput = new Object[] { start, end };
 				if (i < tokens.length) {
 					final StringBuilder sb = new StringBuilder();
 					for (int j = i; j < tokens.length; j++) {
 						sb.append(tokens[j]);
 						sb.append(' ');
 					}
 					sb.deleteCharAt(sb.length() - 1);
 					context.pageName = sb.toString();
 				}
 			}
 		}
 
 		@Override
 		public void getAuxiliaryInput(final CommandContext context) throws IOException, NullPointerException, CancellationException {
 			if (context.auxiliaryInput != null)
 				return;
 			Object start = null, end = null;
 
 			String line = input("start at revision or time [n/y-m-dTh:m:sZ] (<none>): ");
 			if (line.length() > 0) {
 				try {
 					start = MediaWiki.timestampToDate(line);
 				} catch (final ParseException e) {
 					try {
 						start = Long.valueOf(line);
 					} catch (final NumberFormatException nfe) {
 						System.err.println("Invalid input");
 						throw new CancellationException();
 					}
 				}
 			}
 
 			line = input("  end at revision or time [n/y-m-dTh:m:sZ] (<none>): ");
 			if (line.length() > 0) {
 				try {
 					end = MediaWiki.timestampToDate(line);
 				} catch (final ParseException e) {
 					try {
 						end = Long.valueOf(line);
 					} catch (final NumberFormatException nfe) {
 						System.err.println("Invalid input");
 						throw new CancellationException();
 					}
 				}
 			}
 
 			context.auxiliaryInput = new Object[] { start, end };
 		}
 
 		public void perform(final CommandContext context) throws IOException, MediaWiki.MediaWikiException, ParseException {
 			final Object[] auxiliaryInput = (Object[]) context.auxiliaryInput;
 			final Object start = auxiliaryInput[0], end = auxiliaryInput[1];
 
 			Iterator<MediaWiki.Revision> ri;
 			work("Getting revisions...");
 			try {
 				if (((start == null) && (end == null)) || ((start instanceof Long) && (end == null)) || ((start == null) && (end instanceof Long)) || ((start instanceof Long) && (end instanceof Long))) {
 					ri = context.wiki.getRevisions(context.pageName, (Long) start, (Long) end);
 				} else if (((start instanceof Date) && (end == null)) || ((start == null) && (end instanceof Date)) || ((start instanceof Date) && (end instanceof Date))) {
 					ri = context.wiki.getRevisions(context.pageName, (Date) start, (Date) end);
 				} else {
 					System.err.println("Incorrect combination of revision IDs and timestamps");
 					throw new CancellationException();
 				}
 			} finally {
 				workEnd();
 			}
 
 			MediaWiki.Revision r;
 			if ((r = next(ri, "Getting revisions...")) == null) {
 				System.err.println("No matching revisions exist");
 			} else {
 				do {
 					context.output.println(String.format("%10d (%7d bytes) at %s %s %s%s", r.getRevisionID(), r.getLength(), MediaWiki.dateToISO8601(r.getTimestamp()), r.isMinor() ? "m" : " ", r.isUserNameHidden() ? "<user hidden>" : r.getUserName(), r.isAnonymous() ? ", anonymous" : ""));
 					if (context.output.checkError())
 						return;
 					context.output.println(String.format(" %s", r.isCommentHidden() ? "<comment hidden>" : r.getComment()));
 					if (context.output.checkError())
 						return;
 				} while ((r = next(ri, "Getting revisions...")) != null);
 			}
 		}
 
 		public void help() throws IOException {
 			System.err.println("Displays the history of the given page on the current wiki.");
 			System.err.println();
 			System.err.println("{revinfo | hist[ory]} [-start <time or revision ID>] [-end <time or revision ID>] [<page name>]");
 			System.err.println();
 			System.err.println("The page name is mandatory and will be requested if not provided.");
 			System.err.println("-start refers to the earliest timestamp or revision ID to return; -end refers to the most recent. Timestamps are in ISO 8601 format using GMT, i.e. year-mo-dyThr:mn:ssZ.");
 		}
 	}
 
 	public static class FileRevisionInformation extends AbstractPageReadCommand {
 		@Override
 		public void parseArguments(final CommandContext context) {
 			final String arguments = context.arguments.trim();
 			if (arguments.length() > 0) {
 				Object start = null, end = null;
 
 				final String[] tokens = arguments.split(" +");
 
 				int i = 0;
 				for (; (i < tokens.length) && (tokens[i].charAt(0) == '-'); i++) {
 					final String token = tokens[i];
 
 					if (token.equalsIgnoreCase("-start")) {
 						i++;
 						if (i < tokens.length) {
 							try {
 								start = MediaWiki.timestampToDate(tokens[i]);
 							} catch (final ParseException e) {
 								System.err.println("Invalid input for -start");
 								throw new CancellationException();
 							}
 						}
 					} else if (token.equalsIgnoreCase("-end")) {
 						i++;
 						if (i < tokens.length) {
 							try {
 								end = MediaWiki.timestampToDate(tokens[i]);
 							} catch (final ParseException e) {
 								System.err.println("Invalid input for -end");
 								throw new CancellationException();
 							}
 						}
 					}
 				}
 				context.auxiliaryInput = new Object[] { start, end };
 				if (i < tokens.length) {
 					final StringBuilder sb = new StringBuilder();
 					for (int j = i; j < tokens.length; j++) {
 						sb.append(tokens[j]);
 						sb.append(' ');
 					}
 					sb.deleteCharAt(sb.length() - 1);
 					context.pageName = sb.toString();
 				}
 			}
 		}
 
 		@Override
 		public void getAuxiliaryInput(final CommandContext context) throws IOException, NullPointerException, CancellationException {
 			if (context.auxiliaryInput != null)
 				return;
 			Object start = null, end = null;
 
 			String line = input("start at time [y-m-dTh:m:sZ] (<none>): ");
 			if (line.length() > 0) {
 				try {
 					start = MediaWiki.timestampToDate(line);
 				} catch (final ParseException e) {
 					System.err.println("Invalid input");
 					throw new CancellationException();
 				}
 			}
 
 			line = input("  end at time [y-m-dTh:m:sZ] (<none>): ");
 			if (line.length() > 0) {
 				try {
 					end = MediaWiki.timestampToDate(line);
 				} catch (final ParseException e) {
 					System.err.println("Invalid input");
 					throw new CancellationException();
 				}
 			}
 
 			context.auxiliaryInput = new Object[] { start, end };
 		}
 
 		public void perform(final CommandContext context) throws IOException, MediaWiki.MediaWikiException, ParseException {
 			final MediaWiki.Namespaces namespaces = context.wiki.getNamespaces();
 			if (namespaces.getNamespaceForPage(context.pageName).getID() != MediaWiki.StandardNamespace.FILE) {
 				context.pageName = namespaces.getNamespace(MediaWiki.StandardNamespace.FILE).getFullPageName(namespaces.removeNamespacePrefix(context.pageName));
 				System.err.println(String.format("Note: Page name changed to %s", context.pageName));
 			}
 
 			final Object[] auxiliaryInput = (Object[]) context.auxiliaryInput;
 			final Date start = (Date) auxiliaryInput[0], end = (Date) auxiliaryInput[1];
 
 			Iterator<MediaWiki.ImageRevision> iri;
 			work("Getting revisions...");
 			try {
 				iri = context.wiki.getImageRevisions(context.pageName, start, end);
 			} finally {
 				workEnd();
 			}
 
 			MediaWiki.ImageRevision ir;
 			if ((ir = next(iri, "Getting revisions...")) == null) {
 				System.err.println(context.pageName + ": No matching revisions exist");
 			} else {
 				do {
 					context.output.println(String.format("%s (%7d bytes) %4dx%4d   %s", MediaWiki.dateToISO8601(ir.getTimestamp()), ir.getLength(), ir.getWidth(), ir.getHeight(), ir.getUserName()));
 					if (context.output.checkError())
 						return;
 					context.output.println(String.format(" %s", ir.getURL()));
 					if (context.output.checkError())
 						return;
 					context.output.println(String.format(" %s", ir.getComment()));
 					if (context.output.checkError())
 						return;
 				} while ((ir = next(iri, "Getting revisions...")) != null);
 			}
 		}
 
 		public void help() throws IOException {
 			System.err.println("Displays the history of uploads to the given file on the current wiki.");
 			System.err.println();
 			System.err.println("{page | file}{rev[info] | hist[ory]} [-start <time or revision ID>] [-end <time or revision ID>] [<file name>]");
 			System.err.println();
 			System.err.println("The file name is mandatory and will be requested if not provided. The File namespace is implied.");
 			System.err.println("-start refers to the earliest timestamp or revision ID to return; -end refers to the most recent. Timestamps are in ISO 8601 format using GMT, i.e. year-mo-dyThr:mn:ssZ.");
 		}
 	}
 
 	public static class DownloadFileRevision extends AbstractPageReadCommand {
 		@Override
 		public void getAuxiliaryInput(final CommandContext context) throws IOException, NullPointerException, CancellationException {
 			if (context.auxiliaryInput != null)
 				return;
 			try {
 				final String dateString = input("as of [y-m-dTh:m:sZ] (<latest>): ", null);
 				final String newName = input(String.format("to this new name (relative to %s, <same>): ", System.getProperty("user.dir")), null);
 				context.auxiliaryInput = new Object[] { dateString != null ? MediaWiki.timestampToDate(dateString) : null, newName };
 			} catch (final ParseException e) {
 				System.err.println("Invalid input");
 				throw new CancellationException();
 			}
 		}
 
 		public void perform(final CommandContext context) throws IOException, MediaWiki.MediaWikiException, ParseException {
 			final MediaWiki.Namespaces namespaces = context.wiki.getNamespaces();
 			if (namespaces.getNamespaceForPage(context.pageName).getID() != MediaWiki.StandardNamespace.FILE) {
 				context.pageName = namespaces.getNamespace(MediaWiki.StandardNamespace.FILE).getFullPageName(namespaces.removeNamespacePrefix(context.pageName));
 				System.err.println(String.format("Note: Page name changed to %s", context.pageName));
 			}
 
 			Iterator<MediaWiki.ImageRevision> iri;
 
 			final Object[] auxiliaryInput = (Object[]) context.auxiliaryInput;
 			final Date timestamp = (Date) auxiliaryInput[0];
 			String newName = (String) auxiliaryInput[1];
 			if (newName == null) {
 				work("Normalising the file name...");
 				try {
 					newName = context.wiki.normalizeTitle(context.wiki.getNamespaces().removeNamespacePrefix(context.pageName));
 				} finally {
 					workEnd();
 				}
 			} else
 				newName = expandInput(context, newName); // magic words!
 
 			work("Getting revision...");
 			try {
 				iri = context.wiki.getImageRevisions(context.pageName, timestamp, timestamp);
 			} finally {
 				workEnd();
 			}
 
 			final MediaWiki.ImageRevision ir = next(iri);
 			if (ir != null) {
 				final InputStream wikiImageIn = ir.getContent();
 
 				final File localImage = new File(newName);
 
 				final FileOutputStream localImageOut = new FileOutputStream(localImage);
 				try {
 					final byte[] buf = new byte[4096];
 					long totalRead = 0;
 					int read;
 					while ((read = wikiImageIn.read(buf)) > 0) {
 						localImageOut.write(buf, 0, read);
 						totalRead += read;
 						System.err.format("\r%,d bytes downloaded", totalRead);
 					}
 				} finally {
 					localImageOut.close();
 				}
 				System.err.println();
 				System.err.println("Revision contents saved as " + newName);
 			} else {
 				System.err.println(context.pageName + ": " + MediaWiki.dateToISO8601(timestamp) + ": No such revision");
 			}
 		}
 
 		public void help() throws IOException {
 			System.err.println("Downloads a revision of the given file on the current wiki.");
 			System.err.println();
 			System.err.println("{download[{image | file}] | {image | file}save} [<file name>]");
 			System.err.println();
 			System.err.println("The file name is mandatory and will be requested if not provided. The File namespace is implied.");
 			System.err.println("You will be asked to provide the timestamp of the revision to download and a new name for the file. If the timestamp is not provided, the most recent revision will be downloaded. If the name is not provided, the file's display name (with spaces instead of underscores) will be used.");
 		}
 	}
 
 	public static class Read extends AbstractPageReadCommand {
 		public void perform(final CommandContext context) throws IOException, MediaWiki.MediaWikiException, ParseException {
 			Iterator<MediaWiki.Revision> ri;
 			work("Getting revision...");
 			try {
 				ri = context.wiki.getLastRevision(true, context.pageName);
 			} finally {
 				workEnd();
 			}
 
 			final MediaWiki.Revision r = next(ri);
 			if (r != null) {
 				if (r.isContentHidden()) {
 					System.err.println(context.pageName + ": Hidden content");
 				} else {
 					context.output.println(r.getContent());
 					if (context.output.checkError())
 						return;
 					context.output.println();
 				}
 			} else {
 				System.err.println(context.pageName + ": No such page");
 			}
 		}
 
 		public void help() throws IOException {
 			System.err.println("Displays the content of the given page on the current wiki.");
 			System.err.println();
 			System.err.println("read [<page name>]");
 			System.err.println();
 			System.err.println("The page name is mandatory and will be requested if not provided.");
 		}
 	}
 
 	public static class ReadRevision extends AbstractCommand {
 		@Override
 		public void parseArguments(final CommandContext context) {
 			final String arguments = context.arguments.trim();
 			if (arguments.length() > 0) {
 				try {
 					context.essentialInput = Long.valueOf(arguments);
 				} catch (final NumberFormatException nfe) {
 					// ask the user for the revision number later
 				}
 				context.arguments = "";
 			}
 		}
 
 		@Override
 		public void getEssentialInput(final CommandContext context) throws IOException, NullPointerException, CancellationException {
 			if (context.essentialInput != null)
 				return;
 			try {
 				context.essentialInput = Long.valueOf(inputMandatory("revision number: "));
 			} catch (final NumberFormatException nfe) {
 				System.err.println("Invalid input");
 				throw new CancellationException();
 			}
 		}
 
 		public void perform(final CommandContext context) throws IOException, MediaWiki.MediaWikiException, ParseException {
 			Iterator<MediaWiki.Revision> ri;
 			work("Getting revision...");
 			try {
 				ri = context.wiki.getRevisions(true, (Long) context.essentialInput);
 			} finally {
 				workEnd();
 			}
 
 			final MediaWiki.Revision r = next(ri);
 			if (r != null) {
 				if (r.isContentHidden()) {
 					System.err.println(context.essentialInput + ": Hidden content");
 				} else {
 					context.output.println(r.getContent());
 					if (context.output.checkError())
 						return;
 					context.output.println();
 				}
 			} else {
 				System.err.println(context.essentialInput + ": No such revision");
 			}
 		}
 
 		public void help() throws IOException {
 			System.err.println("Displays the content of the given revision on the current wiki.");
 			System.err.println();
 			System.err.println("{readrev[ision] | revision[content] | revcontent} [<revision number>]");
 			System.err.println();
 			System.err.println("The revision number is mandatory and will be requested if not provided.");
 		}
 	}
 
 	public static class PageCategories extends AbstractPageReadCommand implements IterableCommand {
 		public void perform(final CommandContext context) throws IOException, MediaWiki.MediaWikiException, ParseException {
 			Iterator<MediaWiki.CategoryMembership> ci;
 			work("Getting categories...");
 			try {
 				ci = context.wiki.getCategories(context.pageName);
 			} finally {
 				workEnd();
 			}
 
 			MediaWiki.CategoryMembership c = next(ci);
 			if (c != null) {
 				context.output.print(c.getCategoryBaseName());
 				if (context.output.checkError())
 					return;
 
 				while ((c = next(ci)) != null) {
 					context.output.print(" | ");
 					if (context.output.checkError())
 						return;
 					context.output.print(c.getCategoryBaseName());
 					if (context.output.checkError())
 						return;
 				}
 				context.output.println();
 			}
 		}
 
 		public Iterator<String> iterator(final CommandContext context) throws IOException, MediaWiki.MediaWikiException, ParseException {
 			Iterator<MediaWiki.CategoryMembership> ci;
 			work("Getting categories...");
 			try {
 				ci = context.wiki.getCategories(context.pageName);
 			} finally {
 				workEnd();
 			}
 
 			return new Itr(ci);
 		}
 
 		public static class Itr implements Iterator<String> {
 			final Iterator<MediaWiki.CategoryMembership> ci;
 
 			public Itr(final Iterator<MediaWiki.CategoryMembership> ci) {
 				this.ci = ci;
 			}
 
 			public boolean hasNext() {
 				return ci.hasNext();
 			}
 
 			public String next() {
 				final MediaWiki.CategoryMembership c = ci.next();
 				return c != null ? c.getCategoryName() : null;
 			}
 
 			public void remove() {
 				ci.remove();
 			}
 		}
 
 		public void help() throws IOException {
 			System.err.println("Displays the categories containing the given page on the current wiki.");
 			System.err.println();
 			System.err.println("[page]cat[egorie]s [<page name>]");
 			System.err.println();
 			System.err.println("The page name is mandatory and will be requested if not provided.");
 			System.err.println();
 			System.err.println("This command can be used as input to the 'for' command.");
 		}
 	}
 
 	public static class AllCategories extends AbstractCommand implements IterableCommand {
 		@Override
 		public void getAuxiliaryInput(final CommandContext context) throws IOException, NullPointerException, CancellationException {
 			if (context.auxiliaryInput != null)
 				return;
 			if (inputBoolean("Add some filters? [y/N] ", false)) {
 				final String start = input("      start at base name (<first>): ", null);
 				final String end = input("         end at base name (<last>): ", null);
 				final String prefix = input("      base name prefix (<nothing>): ", null);
 				final boolean ascendingOrder = inputBoolean("             ascending order [Y/n]: ", true);
 				try {
 					Long min = Long.valueOf(input("minimum number of members (<none>): ", "-1"));
 					if (min == -1) {
 						min = null;
 					}
 					Long max = Long.valueOf(input("maximum number of members (<none>): ", "-1"));
 					if (max == -1) {
 						max = null;
 					}
 
 					context.auxiliaryInput = new Object[] { start, end, prefix, ascendingOrder, min, max };
 				} catch (final NumberFormatException nfe) {
 					System.err.println("Invalid input");
 					throw new CancellationException();
 				}
 			} else {
 				context.auxiliaryInput = new Object[] { null, null, null, true, null, null };
 			}
 		}
 
 		public void perform(final CommandContext context) throws IOException, MediaWiki.MediaWikiException, CancellationException, NullPointerException, ParseException {
 			final Object[] auxiliaryInput = (Object[]) context.auxiliaryInput;
 			final String start = (String) auxiliaryInput[0], end = (String) auxiliaryInput[1], prefix = (String) auxiliaryInput[2];
 			final boolean ascendingOrder = (Boolean) auxiliaryInput[3];
 			final Long min = (Long) auxiliaryInput[4], max = (Long) auxiliaryInput[5];
 
 			Iterator<MediaWiki.Category> ci;
 			work("Getting categories...");
 			try {
 				ci = context.wiki.getAllCategories(start, end, prefix, ascendingOrder, min, max);
 			} finally {
 				workEnd();
 			}
 
 			MediaWiki.Category c;
 			while ((c = next(ci, "Getting categories...")) != null) {
 				context.output.println(c.toString());
 				if (context.output.checkError())
 					return;
 			}
 		}
 
 		public Iterator<String> iterator(final CommandContext context) throws IOException, MediaWiki.MediaWikiException, ParseException {
 			final Object[] auxiliaryInput = (Object[]) context.auxiliaryInput;
 			final String start = (String) auxiliaryInput[0], end = (String) auxiliaryInput[1], prefix = (String) auxiliaryInput[2];
 			final boolean ascendingOrder = (Boolean) auxiliaryInput[3];
 			final Long min = (Long) auxiliaryInput[4], max = (Long) auxiliaryInput[5];
 
 			Iterator<MediaWiki.Category> ci;
 			work("Getting categories...");
 			try {
 				ci = context.wiki.getAllCategories(start, end, prefix, ascendingOrder, min, max);
 			} finally {
 				workEnd();
 			}
 
 			return new Itr(ci);
 		}
 
 		public static class Itr implements Iterator<String> {
 			final Iterator<MediaWiki.Category> ci;
 
 			public Itr(final Iterator<MediaWiki.Category> ci) {
 				this.ci = ci;
 			}
 
 			public boolean hasNext() {
 				return ci.hasNext();
 			}
 
 			public String next() {
 				final MediaWiki.Category c = ci.next();
 				return c != null ? c.getFullPageName() : null;
 			}
 
 			public void remove() {
 				ci.remove();
 			}
 		}
 
 		public void help() throws IOException {
 			System.err.println("Displays the categories on the current wiki.");
 			System.err.println();
 			System.err.println("{all | list}cat[egorie]s");
 			System.err.println();
 			System.err.println("The list may be filtered by range (starting and ending names), prefix and member count, and be displayed in ascending or descending order.");
 			System.err.println();
 			System.err.println("This command can be used as input to the 'for' command.");
 		}
 	}
 
 	public static class AllFiles extends AbstractCommand implements IterableCommand {
 		@Override
 		public void getAuxiliaryInput(final CommandContext context) throws IOException, NullPointerException, CancellationException {
 			if (context.auxiliaryInput != null)
 				return;
 			if (inputBoolean("Add some filters? [y/N] ", false)) {
 				final String start = input("    start at base name (<first>): ", null);
 				final String prefix = input("    base name prefix (<nothing>): ", null);
 				final boolean ascendingOrder = inputBoolean("           ascending order [Y/n]: ", true);
 				try {
 					Long min = Long.valueOf(input("minimum length in bytes (<none>): ", "-1"));
 					if (min == -1) {
 						min = null;
 					}
 					Long max = Long.valueOf(input("maximum length in bytes (<none>): ", "-1"));
 					if (max == -1) {
 						max = null;
 					}
 					final String sha1 = input("     require SHA-1 hash (<none>): ", null);
 
 					context.auxiliaryInput = new Object[] { start, prefix, sha1, ascendingOrder, min, max };
 				} catch (final NumberFormatException nfe) {
 					System.err.println("Invalid input");
 					throw new CancellationException();
 				}
 			} else {
 				context.auxiliaryInput = new Object[] { null, null, null, true, null, null };
 			}
 		}
 
 		public void perform(final CommandContext context) throws IOException, MediaWiki.MediaWikiException, CancellationException, NullPointerException, ParseException {
 			final Object[] auxiliaryInput = (Object[]) context.auxiliaryInput;
 			final String start = (String) auxiliaryInput[0], prefix = (String) auxiliaryInput[1], sha1 = (String) auxiliaryInput[2];
 			final boolean ascendingOrder = (Boolean) auxiliaryInput[3];
 			final Long min = (Long) auxiliaryInput[4], max = (Long) auxiliaryInput[5];
 
 			Iterator<MediaWiki.ImageRevision> iri;
 			work("Getting files...");
 			try {
 				iri = context.wiki.getAllImages(start, prefix, ascendingOrder, min, max, sha1);
 			} finally {
 				workEnd();
 			}
 
 			MediaWiki.ImageRevision ir;
 			while ((ir = next(iri, "Getting files...")) != null) {
 				context.output.println(ir.getFullPageName());
 				if (context.output.checkError())
 					return;
 				context.output.println(String.format("%s (%7d bytes) %4dx%4d   %s", MediaWiki.dateToISO8601(ir.getTimestamp()), ir.getLength(), ir.getWidth(), ir.getHeight(), ir.getUserName()));
 				if (context.output.checkError())
 					return;
 				context.output.println(String.format(" %s", ir.getComment()));
 				if (context.output.checkError())
 					return;
 			}
 		}
 
 		public Iterator<String> iterator(final CommandContext context) throws IOException, MediaWiki.MediaWikiException, ParseException {
 			final Object[] auxiliaryInput = (Object[]) context.auxiliaryInput;
 			final String start = (String) auxiliaryInput[0], prefix = (String) auxiliaryInput[1], sha1 = (String) auxiliaryInput[2];
 			final boolean ascendingOrder = (Boolean) auxiliaryInput[3];
 			final Long min = (Long) auxiliaryInput[4], max = (Long) auxiliaryInput[5];
 
 			Iterator<MediaWiki.ImageRevision> iri;
 			work("Getting files...");
 			try {
 				iri = context.wiki.getAllImages(start, prefix, ascendingOrder, min, max, sha1);
 			} finally {
 				workEnd();
 			}
 
 			return new Itr(iri);
 		}
 
 		public static class Itr implements Iterator<String> {
 			final Iterator<MediaWiki.ImageRevision> iri;
 
 			public Itr(final Iterator<MediaWiki.ImageRevision> iri) {
 				this.iri = iri;
 			}
 
 			public boolean hasNext() {
 				return iri.hasNext();
 			}
 
 			public String next() {
 				final MediaWiki.ImageRevision ir = iri.next();
 				return ir != null ? ir.getFullPageName() : null;
 			}
 
 			public void remove() {
 				iri.remove();
 			}
 		}
 
 		public void help() throws IOException {
 			System.err.println("Displays the files on the current wiki.");
 			System.err.println();
 			System.err.println("{all | list}{file | image}s");
 			System.err.println();
 			System.err.println("The list may be filtered by starting name, prefix and file size, and be displayed in ascending or descending order.");
 			System.err.println();
 			System.err.println("This command can be used as input to the 'for' command.");
 		}
 	}
 
 	public static class AllPages extends AbstractCommand implements IterableCommand {
 		@Override
 		public void getEssentialInput(final CommandContext context) throws IOException, NullPointerException, CancellationException {
 			if (context.essentialInput != null)
 				return;
 			context.essentialInput = input("namespace name or number (<main>): ", "0");
 		}
 
 		@Override
 		public void getAuxiliaryInput(final CommandContext context) throws IOException, NullPointerException, CancellationException {
 			if (context.auxiliaryInput != null)
 				return;
 			if (inputBoolean("Add some filters? [y/N] ", false)) {
 				final String start = input("start at base name (<first>): ", null);
 				final String prefix = input("base name prefix (<nothing>): ", null);
 				final boolean ascendingOrder = inputBoolean("ascending order [Y/n]: ", true);
 				try {
 					Long min = Long.valueOf(input("minimum length in bytes (<none>): ", "-1"));
 					if (min == -1) {
 						min = null;
 					}
 					Long max = Long.valueOf(input("maximum length in bytes (<none>): ", "-1"));
 					if (max == -1) {
 						max = null;
 					}
 					final Boolean redirect = inputBoolean("restrict to redirects or non-redirects [y/n] (<don't care>): ", null);
 					final Boolean languageLinks = inputBoolean("restrict to pages having or not having language links [y/n] (<don't care>): ", null);
 					final String protectionAction = input("restrict to pages protected from an action [edit/move/...] (<none>): ", null);
 					String protectionType = null;
 					if (protectionAction != null) {
 						protectionType = input("except to those at this level [autoconfirmed/sysop/...] (<none>): ", null);
 					}
 
 					context.auxiliaryInput = new Object[] { start, prefix, protectionAction, protectionType, ascendingOrder, min, max, redirect, languageLinks };
 				} catch (final NumberFormatException nfe) {
 					System.err.println("Invalid input");
 					throw new CancellationException();
 				}
 			} else {
 				context.auxiliaryInput = new Object[] { null, null, null, null, true, null, null, null, null };
 			}
 		}
 
 		public void perform(final CommandContext context) throws IOException, MediaWiki.MediaWikiException, CancellationException, NullPointerException, ParseException {
 			final Iterator<String> si = iterator(context);
 
 			String s;
 			while ((s = next(si, "Getting pages...")) != null) {
 				context.output.println(s);
 				if (context.output.checkError())
 					return;
 			}
 		}
 
 		public Iterator<String> iterator(final CommandContext context) throws IOException, MediaWiki.MediaWikiException, ParseException {
 			final Object[] auxiliaryInput = (Object[]) context.auxiliaryInput;
 			long namespaceID;
 			final String start = (String) auxiliaryInput[0], prefix = (String) auxiliaryInput[1], protectionAction = (String) auxiliaryInput[2], protectionType = (String) auxiliaryInput[3];
 			final boolean ascendingOrder = (Boolean) auxiliaryInput[4];
 			final Long min = (Long) auxiliaryInput[5], max = (Long) auxiliaryInput[6];
 			final Boolean redirect = (Boolean) auxiliaryInput[7], languageLinks = (Boolean) auxiliaryInput[8];
 
 			final MediaWiki.Namespaces namespaces = context.wiki.getNamespaces();
 			{
 				final String namespaceString = (String) context.essentialInput;
 				MediaWiki.Namespace namespace;
 				try {
 					namespace = namespaces.getNamespace(Long.parseLong(namespaceString));
 				} catch (final NumberFormatException nfe) {
 					namespace = namespaces.getNamespace(namespaceString);
 				}
 				if (namespace != null) {
 					namespaceID = namespace.getID();
 				} else {
 					System.err.println(namespaceString + ": No such namespace");
 					throw new CancellationException();
 				}
 			}
 
 			final Iterator<MediaWiki.PageDesignation> pi;
 
 			work("Getting pages...");
 			try {
 				pi = context.wiki.getAllPages(start, prefix, namespaceID, ascendingOrder, min, max, redirect, languageLinks, protectionAction, protectionType);
 			} finally {
 				workEnd();
 			}
 
 			return new PageDesignationToStringIterator(pi);
 		}
 
 		public void help() throws IOException {
 			System.err.println("Displays the pages on the current wiki.");
 			System.err.println();
 			System.err.println("{all | list}pages");
 			System.err.println();
 			System.err.println("The list may be filtered by starting name, prefix, byte count, presence or absence of redirection, presence or absence of interlanguage links and presence or absence of a certain protection, and be displayed in ascending or descending order.");
 			System.err.println();
 			System.err.println("This command can be used as input to the 'for' command.");
 		}
 	}
 
 	public static class CategoryMembers extends AbstractPageReadCommand implements IterableCommand {
 		@Override
 		public void getAuxiliaryInput(final CommandContext context) throws IOException, NullPointerException, CancellationException {
 			if (context.auxiliaryInput != null)
 				return;
 			if (inputBoolean("Add some filters? [y/N] ", false)) {
 				final String namespaceString = input("members in namespace name or number (<all>): ", null);
 				final String startString = input("start at sort key or time (<first>): ", null);
 				Object start = null;
 				if (startString != null) {
 					try {
 						start = MediaWiki.timestampToDate(startString);
 					} catch (final ParseException pe) {
 						start = startString;
 					}
 				}
 				final String endString = input("end at sort key or time (<last>): ", null);
 				Object end = null;
 				if (endString != null) {
 					try {
 						end = MediaWiki.timestampToDate(endString);
 					} catch (final ParseException pe) {
 						end = endString;
 					}
 				}
 				final boolean ascendingOrder = inputBoolean("ascending or chronological order [Y/n]: ", true);
 
 				context.auxiliaryInput = new Object[] { namespaceString, start, end, ascendingOrder };
 			} else {
 				context.auxiliaryInput = new Object[] { null, null, null, true };
 			}
 		}
 
 		public void perform(final CommandContext context) throws IOException, MediaWiki.MediaWikiException, CancellationException, NullPointerException, ParseException {
 			final MediaWiki.Namespaces namespaces = context.wiki.getNamespaces();
 			if (namespaces.getNamespaceForPage(context.pageName).getID() != MediaWiki.StandardNamespace.CATEGORY) {
 				context.pageName = namespaces.getNamespace(MediaWiki.StandardNamespace.CATEGORY).getFullPageName(namespaces.removeNamespacePrefix(context.pageName));
 				System.err.println(String.format("Note: Page name changed to %s", context.pageName));
 			}
 
 			final Object[] auxiliaryInput = (Object[]) context.auxiliaryInput;
 			long[] namespaceIDs = null;
 			final Object start = auxiliaryInput[1], end = auxiliaryInput[2];
 			final boolean ascendingOrder = (Boolean) auxiliaryInput[3];
 			final String namespaceString = (String) auxiliaryInput[0];
 
 			if (namespaceString != null) {
 				MediaWiki.Namespace namespace;
 				try {
 					namespace = namespaces.getNamespace(Long.parseLong(namespaceString));
 				} catch (final NumberFormatException nfe) {
 					namespace = namespaces.getNamespace(namespaceString);
 				}
 				if (namespace != null) {
 					namespaceIDs = new long[] { namespace.getID() };
 				} else {
 					System.err.println(namespaceString + ": No such namespace");
 					throw new CancellationException();
 				}
 			}
 
 			final Iterator<MediaWiki.CategoryMember> ci;
 
 			work("Getting pages...");
 			try {
 				if (((start == null) && (end == null)) || ((start instanceof String) && (end == null)) || ((start == null) && (end instanceof String)) || ((start instanceof String) && (end instanceof String))) {
 					ci = context.wiki.getCategoryMembers(context.pageName, ascendingOrder, (String) start, (String) end, namespaceIDs);
 				} else if (((start instanceof Date) && (end == null)) || ((start == null) && (end instanceof Date)) || ((start instanceof Date) && (end instanceof Date))) {
 					ci = context.wiki.getCategoryMembers(context.pageName, ascendingOrder, (Date) start, (Date) end, namespaceIDs);
 				} else {
 					System.err.println("Incorrect combination of sort keys and timestamps");
 					throw new CancellationException();
 				}
 			} finally {
 				workEnd();
 			}
 
 			MediaWiki.CategoryMember c;
 			while ((c = next(ci, "Getting pages...")) != null) {
 				context.output.println(String.format("   %s", c.getFullPageName()));
 				if (context.output.checkError())
 					return;
 				context.output.println(String.format("as %-55s %s", c.getSortKey(), MediaWiki.dateToISO8601(c.getAdditionTime())));
 				if (context.output.checkError())
 					return;
 			}
 		}
 
 		public Iterator<String> iterator(final CommandContext context) throws IOException, MediaWiki.MediaWikiException, ParseException {
 			final MediaWiki.Namespaces namespaces = context.wiki.getNamespaces();
 			if (namespaces.getNamespaceForPage(context.pageName).getID() != MediaWiki.StandardNamespace.CATEGORY) {
 				context.pageName = namespaces.getNamespace(MediaWiki.StandardNamespace.CATEGORY).getFullPageName(namespaces.removeNamespacePrefix(context.pageName));
 				System.err.println(String.format("Note: Page name changed to %s", context.pageName));
 			}
 
 			final Object[] auxiliaryInput = (Object[]) context.auxiliaryInput;
 			final String namespaceString = (String) auxiliaryInput[0];
 			long[] namespaceIDs = null;
 			final Object start = auxiliaryInput[1], end = auxiliaryInput[2];
 			final boolean ascendingOrder = (Boolean) auxiliaryInput[3];
 
 			if (namespaceString != null) {
 				MediaWiki.Namespace namespace;
 				try {
 					namespace = namespaces.getNamespace(Long.parseLong(namespaceString));
 				} catch (final NumberFormatException nfe) {
 					namespace = namespaces.getNamespace(namespaceString);
 				}
 				if (namespace != null) {
 					namespaceIDs = new long[] { namespace.getID() };
 				} else {
 					System.err.println(namespaceString + ": No such namespace");
 					throw new CancellationException();
 				}
 			}
 
 			final Iterator<MediaWiki.CategoryMember> ci;
 
 			work("Getting pages...");
 			try {
 				if (((start == null) && (end == null)) || ((start instanceof String) && (end == null)) || ((start == null) && (end instanceof String)) || ((start instanceof String) && (end instanceof String))) {
 					ci = context.wiki.getCategoryMembers(context.pageName, ascendingOrder, (String) start, (String) end, namespaceIDs);
 				} else if (((start instanceof Date) && (end == null)) || ((start == null) && (end instanceof Date)) || ((start instanceof Date) && (end instanceof Date))) {
 					ci = context.wiki.getCategoryMembers(context.pageName, ascendingOrder, (Date) start, (Date) end, namespaceIDs);
 				} else {
 					System.err.println("Incorrect combination of sort keys and timestamps");
 					throw new CancellationException();
 				}
 			} finally {
 				workEnd();
 			}
 
 			return new Itr(ci);
 		}
 
 		public static class Itr implements Iterator<String> {
 			final Iterator<MediaWiki.CategoryMember> ci;
 
 			public Itr(final Iterator<MediaWiki.CategoryMember> ci) {
 				this.ci = ci;
 			}
 
 			public boolean hasNext() {
 				return ci.hasNext();
 			}
 
 			public String next() {
 				final MediaWiki.CategoryMember c = ci.next();
 				return c != null ? c.getFullPageName() : null;
 			}
 
 			public void remove() {
 				ci.remove();
 			}
 		}
 
 		public void help() throws IOException {
 			System.err.println("Displays the pages in the given category on the current wiki.");
 			System.err.println();
 			System.err.println("cat[egory]members [<category name>]");
 			System.err.println();
 			System.err.println("The category name is mandatory and will be requested if not provided. The Category namespace is implied.");
 			System.err.println("The list may be filtered by range (starting and ending sort key or starting and ending timestamp of categorisation), and be displayed in ascending or descending order.");
 			System.err.println();
 			System.err.println("This command can be used as input to the 'for' command.");
 		}
 	}
 
 	public static class AllUsers extends AbstractCommand implements IterableCommand {
 		@Override
 		public void getAuxiliaryInput(final CommandContext context) throws IOException, NullPointerException, CancellationException {
 			if (context.auxiliaryInput != null)
 				return;
 			if (inputBoolean("Add some filters? [y/N] ", false)) {
 				final String start = input("start at base name (<first>): ", null);
 				final String prefix = input("base names must start with (<nothing>): ", null);
 				final String group = input("users must belong to group [autoconfirmed/sysop/...] (<none>): ", null);
 				context.auxiliaryInput = new Object[] { start, prefix, group };
 			} else {
 				context.auxiliaryInput = new Object[3];
 			}
 		}
 
 		public void perform(final CommandContext context) throws IOException, MediaWiki.MediaWikiException, CancellationException, NullPointerException, ParseException {
 			final Object[] auxiliaryInput = (Object[]) context.auxiliaryInput;
 			final String start = (String) auxiliaryInput[0], prefix = (String) auxiliaryInput[1], group = (String) auxiliaryInput[2];
 
 			Iterator<MediaWiki.User> ui;
 			work("Getting users...");
 			try {
 				ui = context.wiki.getAllUsers(start, prefix, group);
 			} finally {
 				workEnd();
 			}
 
 			MediaWiki.User u;
 			while ((u = next(ui, "Getting users...")) != null) {
 				displayUser(u, context.output);
 			}
 		}
 
 		public Iterator<String> iterator(final CommandContext context) throws IOException, MediaWiki.MediaWikiException, ParseException {
 			final Object[] auxiliaryInput = (Object[]) context.auxiliaryInput;
 			final String start = (String) auxiliaryInput[0], prefix = (String) auxiliaryInput[1], group = (String) auxiliaryInput[2];
 
 			Iterator<MediaWiki.User> ui;
 			work("Getting users...");
 			try {
 				ui = context.wiki.getAllUsers(start, prefix, group);
 			} finally {
 				workEnd();
 			}
 
 			return new Itr(ui);
 		}
 
 		public static class Itr implements Iterator<String> {
 			final Iterator<MediaWiki.User> ui;
 
 			public Itr(final Iterator<MediaWiki.User> ui) {
 				this.ui = ui;
 			}
 
 			public boolean hasNext() {
 				return ui.hasNext();
 			}
 
 			public String next() {
 				final MediaWiki.User u = ui.next();
 				return u != null ? u.getUserName() : null;
 			}
 
 			public void remove() {
 				ui.remove();
 			}
 		}
 
 		public void help() throws IOException {
 			System.err.println("Displays the users on the current wiki.");
 			System.err.println();
 			System.err.println("{all | list}users");
 			System.err.println();
 			System.err.println("The list may be filtered by starting name, prefix and group membership.");
 			System.err.println();
 			System.err.println("This command can be used as input to the 'for' command.");
 		}
 	}
 
 	public static abstract class AbstractWriteCommand extends AbstractPageReadCommand {
 		@Override
 		public void getToken(final CommandContext context) throws IOException, MediaWiki.MediaWikiException, ParseException {
 			super.getToken(context);
 
 			checkLogin(context.wiki);
 		}
 	}
 
 	public static abstract class AbstractEditTokenCommand extends AbstractWriteCommand {
 		@Override
 		public void getToken(final CommandContext context) throws IOException, MediaWiki.MediaWikiException, ParseException {
 			super.getToken(context);
 
 			work("Getting edit token...");
 			try {
 				context.token = context.wiki.startEdit(context.pageName);
 			} finally {
 				workEnd();
 			}
 		}
 	}
 
 	public static class Purge extends AbstractCommand {
 		public void perform(final CommandContext context) throws IOException, MediaWikiException, ParseException {
 			work("Purging...");
 			try {
 				context.wiki.purge(context.pageName);
 			} finally {
 				workEnd();
 			}
 		}
 
 		public void help() throws IOException {
 			System.err.println("Purges the cache of a page on the current wiki.");
 			System.err.println();
 			System.err.println("purge [<page name>]");
 			System.err.println();
 			System.err.println("The page name is mandatory and will be requested if not provided.");
 		}
 	}
 
 	public static class ExpandTemplatesInWikitext extends AbstractCommand {
 		@Override
 		public void parseArguments(final CommandContext context) {
 			final String arguments = context.arguments.trim();
 			if (arguments.length() > 0) {
 				context.essentialInput = arguments;
 				context.arguments = "";
 			}
 		}
 
 		@Override
 		public void getEssentialInput(final CommandContext context) throws IOException, NullPointerException, CancellationException {
 			if (context.essentialInput != null)
 				return;
 			context.essentialInput = inputMultiline();
 		}
 
 		@Override
 		public void getAuxiliaryInput(final CommandContext context) throws IOException, NullPointerException, CancellationException {
 			if (context.auxiliaryInput != null)
 				return;
 			final String fullPageName = input("as if it were in this page (<API>): ", null);
 
 			context.auxiliaryInput = new Object[] { fullPageName };
 		}
 
 		public void perform(final CommandContext context) throws IOException, MediaWikiException, ParseException {
 			final String wikitext = (String) context.essentialInput, fullPageName = (String) ((Object[]) context.auxiliaryInput)[0];
 
 			String expansion;
 			work("Expanding templates...");
 			try {
 				expansion = context.wiki.expandTemplatesInWikitext(wikitext, fullPageName);
 			} finally {
 				workEnd();
 			}
 
 			context.output.println(expansion);
 		}
 
 		public void help() throws IOException {
 			System.err.println("Expands templates in wikitext on the current wiki.");
 			System.err.println();
 			System.err.println("expand[templates] [<single-line wikitext>]");
 			System.err.println();
 			System.err.println("The wikitext is mandatory and will be requested (as multi-line input) if not provided.");
 		}
 	}
 
 	public static class MakeCommand extends AbstractCommand {
 		@Override
 		public void parseArguments(final CommandContext context) {
 			final String[] arguments = context.arguments.split(" +", 3);
 			context.arguments = arguments.length == 3 ? arguments[2] : "";
 
 			if (arguments.length >= 1) {
 				context.essentialInput = arguments[0];
 			}
 			if (arguments.length >= 2) {
 				context.auxiliaryInput = arguments[1];
 			}
 		}
 
 		@Override
 		public void getEssentialInput(final CommandContext context) throws IOException, NullPointerException, CancellationException {
 			if (context.essentialInput != null)
 				return;
 			context.essentialInput = inputMandatory("  make a command named: ");
 		}
 
 		@Override
 		public void getAuxiliaryInput(final CommandContext context) throws IOException, NullPointerException, CancellationException {
 			if (context.auxiliaryInput != null)
 				return;
 			context.auxiliaryInput = inputMandatory("from the command named: ");
 		}
 
 		public void perform(final CommandContext context) throws IOException, MediaWikiException, ParseException {
 			final String presetCommandName = (String) context.essentialInput, builtinCommandName = (String) context.auxiliaryInput;
 
 			if (builtinCommands.containsKey(presetCommandName) || commandPrefixes.containsKey(presetCommandName) || pageListModifiers.containsKey(presetCommandName)) {
 				System.err.println(presetCommandName + ": A command of this name already exists");
 				throw new CancellationException();
 			}
 
 			final Command builtinCommand = builtinCommands.get(builtinCommandName);
 			if (builtinCommand == null) {
 				System.err.println(builtinCommandName + ": No such builtin command");
 				return;
 			} else if (builtinCommand instanceof Login) {
 				System.err.println("For security reasons, you cannot preset input into the 'login' command.");
 			}
 
 			final CommandContext storedContext = new CommandContext();
 
 			// 1. Ask the user if s/he wants to preset a page name.
 			boolean requestsPageName = false;
 			if (inputBoolean(String.format("Preset a page name into '%s', if applicable? [y/N] ", presetCommandName), false)) {
 				builtinCommand.getPageName(storedContext);
 				requestsPageName = storedContext.pageName != null;
 			}
 
 			// 2. If the command asks for essential input, ask the user if s/he
 			// wants to preset that.
 			builtinCommand.getEssentialInput(storedContext);
 			final boolean requestsEssentialInput = storedContext.essentialInput != null;
 			if (requestsEssentialInput) {
 				if (!inputBoolean(String.format(requestsPageName ? "Preset the settings after the page name into '%s'? [y/N] " : "Preset these settings into '%s'? [y/N] ", presetCommandName), false)) {
 					storedContext.essentialInput = null;
 				}
 			}
 
 			// 3. If the command asks for auxiliary input, ask the user if s/he
 			// wants to preset that.
 			builtinCommand.getAuxiliaryInput(storedContext);
 			if (storedContext.auxiliaryInput != null) {
 				if (!inputBoolean(String.format(requestsEssentialInput ? "Preset these additional settings into '%s'? [y/N] " : "Preset these settings into '%s'? [y/N] ", presetCommandName), false)) {
 					storedContext.auxiliaryInput = null;
 				}
 			}
 
 			final File directory = new File(System.getProperty("user.home"), ".wikishell-cmds");
 
 			directory.mkdir();
 
 			final ObjectOutputStream saver = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(new File(directory, presetCommandName + ".wcom"))));
 			try {
 				saver.writeUTF(builtinCommandName);
 				saver.writeObject(storedContext);
 			} finally {
 				saver.close();
 			}
 		}
 
 		public void help() throws IOException {
 			System.err.println("Creates a version of a command with some preset input.");
 			System.err.println();
 			System.err.println("make [<new command>] [<command>]");
 			System.err.println();
 			System.err.println("Both parameters are mandatory, and will be requested if not provided.");
 			System.err.println("Some, or all, of the input that needs to be provided to the main command can be preset into the new command, such that only the input that was not preset is requested when using the new command. If all of the input is preset, no input will be requested except for particularly destructive commands like 'delete', as well as the text replacement commands that require confirmation for each occurrence.");
 		}
 	}
 
 	public static class NewSection extends AbstractEditTokenCommand {
 		@Override
 		public void getEssentialInput(final CommandContext context) throws IOException, NullPointerException, CancellationException {
 			if (context.essentialInput != null)
 				return;
 			final String heading = input("heading (<none>): ");
 
 			String text = inputMultiline("-- Section: Start (Type '-- Section: End' to end the text)", "-- Section: End");
 			context.essentialInput = new Object[] { heading, text };
 		}
 
 		@Override
 		public void getAuxiliaryInput(final CommandContext context) throws IOException, NullPointerException, CancellationException {
 			if (context.auxiliaryInput != null)
 				return;
 			context.auxiliaryInput = new Object[] { inputBoolean("minor edit [y/n] (<per Special:Preferences>): ", null) };
 		}
 
 		public void perform(final CommandContext context) throws IOException, MediaWiki.MediaWikiException {
 			final Object[] essentialInput = (Object[]) context.essentialInput;
 			final String heading = (String) essentialInput[0], text = (String) essentialInput[1];
 			final Boolean minor = (Boolean) ((Object[]) context.auxiliaryInput)[0];
 			final MediaWiki.EditToken token = (MediaWiki.EditToken) context.token;
 
 			work("Performing edit...");
 			try {
 				context.wiki.createPageSection(token, heading, text, true /*- bot */, minor);
 			} finally {
 				workEnd();
 			}
 		}
 
 		public void help() throws IOException {
 			System.err.println("Creates a new section at the end of the given page on the current wiki.");
 			System.err.println();
 			System.err.println("newsection [<page name>]");
 			System.err.println();
 			System.err.println("The page name is mandatory and will be requested if not provided.");
 			System.err.println("You will be asked to provide the heading for the section (which may be empty, in which case the text is simply appended to the page), then its text, then whether the edit is minor. The edit summary is '/* HEADING */ new section' and cannot be changed.");
 		}
 	}
 
 	public static abstract class AbstractReplacementCommand extends AbstractEditTokenCommand {
 		@Override
 		public void getToken(final CommandContext context) throws IOException, MediaWiki.MediaWikiException, ParseException {
 			super.getToken(context);
 
 			work("Getting page content...");
 			try {
 				String content = null;
 				final Iterator<MediaWiki.Revision> ri = context.wiki.getLastRevision(true, context.pageName);
 				final MediaWiki.Revision r = next(ri);
 				if (ri != null) {
 					if (r.isContentHidden())
 						throw new MediaWiki.MediaWikiException("Hidden content");
 					else {
 						content = r.getContent();
 					}
 				} else
 					throw new MediaWiki.MissingPageException(context.pageName);
 
 				context.temporary = content;
 			} finally {
 				workEnd();
 			}
 		}
 
 		@Override
 		public void getAuxiliaryInput(final CommandContext context) throws IOException, NullPointerException, CancellationException {
 			if (context.auxiliaryInput != null)
 				return;
 			final String editSummary = input("edit summary: ", null);
 			final Boolean minor = inputBoolean("minor edit [y/n] (<per Special:Preferences>): ", null);
 
 			context.auxiliaryInput = new Object[] { editSummary, minor };
 		}
 
 		public void perform(final CommandContext context) throws IOException, MediaWiki.MediaWikiException {
 			if (context.confirmation instanceof String) {
 				final Object[] auxiliaryInput = (Object[]) context.auxiliaryInput;
 				final String editSummary = (String) auxiliaryInput[0];
 				final Boolean minor = (Boolean) auxiliaryInput[1];
 				final MediaWiki.EditToken token = (MediaWiki.EditToken) context.token;
 				final String text = (String) context.confirmation;
 
 				work("Performing edit...");
 				try {
 					context.wiki.replacePage(token, text, editSummary, true /*- bot */, minor);
 				} finally {
 					workEnd();
 				}
 			}
 		}
 	}
 
 	public static class ReplaceText extends AbstractReplacementCommand {
 		@Override
 		public void getEssentialInput(final CommandContext context) throws IOException, NullPointerException, CancellationException {
 			if (context.essentialInput != null)
 				return;
 			final String findText = inputMandatory("   find: ");
 			final String replaceText = input("replace: ");
 
 			final boolean caseSensitive = inputBoolean("case sensitive [y/N]: ", false);
 
 			context.essentialInput = new Object[] { findText, replaceText, caseSensitive };
 		}
 
 		@Override
 		public void confirm(final CommandContext context) throws IOException, NullPointerException, CancellationException {
 			final Object[] essentialInput = (Object[]) context.essentialInput;
 			final String findText = (String) essentialInput[0], replaceText = (String) essentialInput[1];
 			final boolean caseSensitive = (Boolean) essentialInput[2];
 			String content = (String) context.temporary;
 			context.temporary = null;
 
 			int modifications = 0;
 			// ciContent is in lower case for case-insensitive searches. Use
 			// 'content' to access the actual content that will be put back on
 			// the wiki.
 			String ciContent = caseSensitive ? content : content.toLowerCase();
 			// ciFindText is in lower case for case-insensitive searches.
 			final String ciFindText = caseSensitive ? findText : findText.toLowerCase();
 			final String ciReplaceText = caseSensitive ? replaceText : replaceText.toLowerCase();
 			int cur = 0, index;
 
 			while ((index = ciContent.indexOf(ciFindText, cur)) != -1) {
 				cur = index + findText.length();
 				final String before = content.substring(Math.max(0, index - 75), index);
 				final String match = content.substring(index, cur);
 				final String after = content.substring(cur, Math.min(content.length(), cur + 75));
 				System.err.println((before.length() < 75 ? "" : "[...]") + before + "-->" + match + "<--" + after + (after.length() < 75 ? "" : "[...]"));
 				if (inputBoolean("replace this occurrence [y/N]: ", false)) {
 					content = content.substring(0, index) + replaceText + content.substring(cur);
 
 					ciContent = ciContent.substring(0, index) + ciReplaceText + ciContent.substring(cur);
 
 					cur += replaceText.length() - findText.length();
 
 					modifications++;
 				}
 			}
 
 			if (modifications > 0) {
 				if (inputBoolean("Apply these modifications? [y/N] ", false)) {
 					context.confirmation = content;
 				} else {
 					context.confirmation = new Object();
 				}
 			} else {
 				System.err.println("Nothing to do");
 				context.confirmation = new Object();
 			}
 		}
 
 		public void help() throws IOException {
 			System.err.println("Finds and replaces text on the given page on the current wiki.");
 			System.err.println();
 			System.err.println("replace[text] [<page name>]");
 			System.err.println();
 			System.err.println("The page name is mandatory and will be requested if not provided.");
 			System.err.println("You will be asked to provide the search and replacement text, then the case-sensitivity, then an edit summary, then whether your edit is minor. Following this, you will be asked to confirm each replacement with context from the page.");
 		}
 	}
 
 	public static class ReplaceRegex extends AbstractReplacementCommand {
 		@Override
 		public void getEssentialInput(final CommandContext context) throws IOException, NullPointerException, CancellationException {
 			if (context.essentialInput != null)
 				return;
 			final String find = inputMandatory("find regex: ");
 			try {
 				Pattern.compile(find);
 			} catch (final PatternSyntaxException pse) {
 				System.err.println(pse.getClass().getName() + ": " + pse.getLocalizedMessage());
 				throw new CancellationException();
 			}
 			final String replaceText = input("replace (with $1, $2, ... or \\1, \\2 for captures): ");
 
 			final boolean caseSensitive = inputBoolean("case sensitive [y/N]: ", false);
 
 			final Pattern findRegex = Pattern.compile(find, caseSensitive ? 0 : Pattern.CASE_INSENSITIVE);
 
 			context.essentialInput = new Object[] { findRegex, replaceText };
 		}
 
 		@Override
 		public void confirm(final CommandContext context) throws IOException, NullPointerException, CancellationException {
 			final Object[] essentialInput = (Object[]) context.essentialInput;
 			final Pattern findRegex = (Pattern) essentialInput[0];
 			final String replaceText = (String) essentialInput[1];
 			final String content = (String) context.temporary;
 			context.temporary = null;
 
 			int modifications = 0;
 
 			final StringBuffer newContent = new StringBuffer();
 			final Matcher m = findRegex.matcher(content);
 			while (m.find()) {
 				final int index = m.start();
 				final int matchEnd = m.end();
 				final String before = content.substring(Math.max(0, index - 75), index);
 				final String match = content.substring(index, matchEnd);
 				final String after = content.substring(matchEnd, Math.min(content.length(), matchEnd + 75));
 				System.err.println((before.length() < 75 ? "" : "[...]") + before + "-->" + match + "<--" + after + (after.length() < 75 ? "" : "[...]"));
 				if (inputBoolean("replace this match [y/N]: ", false)) {
 					m.appendReplacement(newContent, replaceText);
 
 					modifications++;
 				}
 			}
 
 			if (modifications > 0) {
 				if (inputBoolean("Apply these modifications? [y/N] ", false)) {
 					m.appendTail(newContent);
 					context.confirmation = newContent.toString();
 				} else {
 					context.confirmation = new Object();
 				}
 			} else {
 				System.err.println("Nothing to do");
 				context.confirmation = new Object();
 			}
 		}
 
 		public void help() throws IOException {
 			System.err.println("Finds and replaces text on the given page on the current wiki using a regular expression.");
 			System.err.println();
 			System.err.println("replaceregex [<page name>]");
 			System.err.println();
 			System.err.println("The page name is mandatory and will be requested if not provided.");
 			System.err.println("You will be asked to provide the search regular expression and replacement text, then the case-sensitivity, then an edit summary, then whether your edit is minor. Following this, you will be asked to confirm each replacement with context from the page. Regular expressions are outside the scope of this documentation.");
 		}
 	}
 
 	public static class AutoReplaceText extends AbstractReplacementCommand {
 		@Override
 		public void getEssentialInput(final CommandContext context) throws IOException, NullPointerException, CancellationException {
 			if (context.essentialInput != null)
 				return;
 			final String findText = inputMandatory("   find: ");
 			final String replaceText = input("replace: ");
 
 			final boolean caseSensitive = inputBoolean("case sensitive [y/N]: ", false);
 
 			context.essentialInput = new Object[] { findText, replaceText, caseSensitive };
 		}
 
 		@Override
 		public void perform(final CommandContext context) throws IOException, MediaWiki.MediaWikiException {
 			final Object[] essentialInput = (Object[]) context.essentialInput;
 			final String findText = (String) essentialInput[0], replaceText = (String) essentialInput[1];
 			final boolean caseSensitive = (Boolean) essentialInput[2];
 			String content = (String) context.temporary;
 			context.temporary = null;
 
 			int modifications = 0;
 			// ciContent is in lower case for case-insensitive searches. Use
 			// 'content' to access the actual content that will be put back on
 			// the wiki.
 			String ciContent = caseSensitive ? content : content.toLowerCase();
 			// ciFindText is in lower case for case-insensitive searches.
 			final String ciFindText = caseSensitive ? findText : findText.toLowerCase();
 			final String ciReplaceText = caseSensitive ? replaceText : replaceText.toLowerCase();
 			int cur = 0, index;
 
 			while ((index = ciContent.indexOf(ciFindText, cur)) != -1) {
 				cur = index + findText.length();
 				content = content.substring(0, index) + replaceText + content.substring(cur);
 
 				ciContent = ciContent.substring(0, index) + ciReplaceText + ciContent.substring(cur);
 
 				cur += replaceText.length() - findText.length();
 
 				modifications++;
 			}
 
 			if (modifications > 0) {
 				final Object[] auxiliaryInput = (Object[]) context.auxiliaryInput;
 				final String editSummary = (String) auxiliaryInput[0];
 				final Boolean minor = (Boolean) auxiliaryInput[1];
 				final MediaWiki.EditToken token = (MediaWiki.EditToken) context.token;
 
 				work("Performing edit...");
 				try {
 					context.wiki.replacePage(token, content, editSummary, true /*- bot */, minor);
 				} finally {
 					workEnd();
 				}
 			} else {
 				System.err.println("Nothing to do");
 			}
 		}
 
 		public void help() throws IOException {
 			System.err.println("Finds and replaces text on the given page on the current wiki.");
 			System.err.println();
 			System.err.println("autoreplace[text] [<page name>]");
 			System.err.println();
 			System.err.println("The page name is mandatory and will be requested if not provided.");
 			System.err.println("You will be asked to provide the search and replacement text, then the case-sensitivity, then an edit summary, then whether your edit is minor. Following this, your selected replacement will automatically be applied to all occurrences of the search text on the page.");
 		}
 	}
 
 	public static class AutoReplaceRegex extends AbstractReplacementCommand {
 		@Override
 		public void getEssentialInput(final CommandContext context) throws IOException, NullPointerException, CancellationException {
 			if (context.essentialInput != null)
 				return;
 			final String find = inputMandatory("find regex: ");
 			try {
 				Pattern.compile(find);
 			} catch (final PatternSyntaxException pse) {
 				System.err.println(pse.getClass().getName() + ": " + pse.getLocalizedMessage());
 				throw new CancellationException();
 			}
 			final String replaceText = input("replace (with $1, $2, ... or \\1, \\2 for captures): ");
 
 			final boolean caseSensitive = inputBoolean("case sensitive [y/N]: ", false);
 
 			final Pattern findRegex = Pattern.compile(find, caseSensitive ? 0 : Pattern.CASE_INSENSITIVE);
 
 			context.essentialInput = new Object[] { findRegex, replaceText };
 		}
 
 		@Override
 		public void perform(final CommandContext context) throws IOException, MediaWiki.MediaWikiException {
 			final Object[] essentialInput = (Object[]) context.essentialInput;
 			final Pattern findRegex = (Pattern) essentialInput[0];
 			final String replaceText = (String) essentialInput[1];
 			final String content = (String) context.temporary;
 			context.temporary = null;
 
 			int modifications = 0;
 
 			final StringBuffer newContent = new StringBuffer();
 			final Matcher m = findRegex.matcher(content);
 			while (m.find()) {
 				m.appendReplacement(newContent, replaceText);
 
 				modifications++;
 			}
 
 			m.appendTail(newContent);
 
 			if (modifications > 0) {
 				final Object[] auxiliaryInput = (Object[]) context.auxiliaryInput;
 				final String editSummary = (String) auxiliaryInput[0];
 				final Boolean minor = (Boolean) auxiliaryInput[1];
 				final MediaWiki.EditToken token = (MediaWiki.EditToken) context.token;
 
 				work("Performing edit...");
 				try {
 					context.wiki.replacePage(token, newContent.toString(), editSummary, true /*- bot */, minor);
 				} finally {
 					workEnd();
 				}
 			} else {
 				System.err.println("Nothing to do");
 			}
 		}
 
 		public void help() throws IOException {
 			System.err.println("Finds and replaces text on the given page on the current wiki using a regular expression.");
 			System.err.println();
 			System.err.println("autoreplaceregex [<page name>]");
 			System.err.println();
 			System.err.println("The page name is mandatory and will be requested if not provided.");
 			System.err.println("You will be asked to provide the search regular expression and replacement text, then the case-sensitivity, then an edit summary, then whether your edit is minor. Following this, your selected replacement will automatically be applied to all occurrences of the search text on the page. Regular expressions are outside the scope of this documentation.");
 		}
 	}
 
 	public static abstract class AbstractTextWriteCommand extends AbstractEditTokenCommand {
 		@Override
 		public void getEssentialInput(final CommandContext context) throws IOException, NullPointerException, CancellationException {
 			if (context.essentialInput != null)
 				return;
 			context.essentialInput = inputMultiline();
 		}
 
 		@Override
 		public void getAuxiliaryInput(final CommandContext context) throws IOException, NullPointerException, CancellationException {
 			if (context.auxiliaryInput != null)
 				return;
 			final String editSummary = input("edit summary: ", null);
 			final Boolean minor = inputBoolean("minor edit [y/n] (<per Special:Preferences>): ", null);
 
 			context.auxiliaryInput = new Object[] { editSummary, minor };
 		}
 	}
 
 	public static class CreatePage extends AbstractTextWriteCommand {
 		public void perform(final CommandContext context) throws IOException, MediaWiki.MediaWikiException {
 			final Object[] auxiliaryInput = (Object[]) context.auxiliaryInput;
 			final String editSummary = (String) auxiliaryInput[0];
 			final Boolean minor = (Boolean) auxiliaryInput[1];
 			final MediaWiki.EditToken token = (MediaWiki.EditToken) context.token;
 			final String text = (String) context.essentialInput;
 
 			work("Performing edit...");
 			try {
 				context.wiki.createPage(token, text, editSummary, true /*- bot */, minor);
 			} finally {
 				workEnd();
 			}
 		}
 
 		public void help() throws IOException {
 			System.err.println("Creates a page on the current wiki.");
 			System.err.println();
 			System.err.println("create[page] [<page name>]");
 			System.err.println();
 			System.err.println("The page name is mandatory and will be requested if not provided.");
 			System.err.println("You will be asked to provide the text of the new page, then an edit summary, then whether your edit is minor. If the page exists when the command completes, you will get an error message.");
 		}
 	}
 
 	public static class ReplacePage extends AbstractTextWriteCommand {
 		public void perform(final CommandContext context) throws IOException, MediaWiki.MediaWikiException {
 			final Object[] auxiliaryInput = (Object[]) context.auxiliaryInput;
 			final String editSummary = (String) auxiliaryInput[0];
 			final Boolean minor = (Boolean) auxiliaryInput[1];
 			final MediaWiki.EditToken token = (MediaWiki.EditToken) context.token;
 			final String text = (String) context.essentialInput;
 
 			work("Performing edit...");
 			try {
 				context.wiki.replacePage(token, text, editSummary, true /*- bot */, minor);
 			} finally {
 				workEnd();
 			}
 		}
 
 		public void help() throws IOException {
 			System.err.println("Replaces the entire text of a page on the current wiki.");
 			System.err.println();
 			System.err.println("replacepage [<page name>]");
 			System.err.println();
 			System.err.println("The page name is mandatory and will be requested if not provided.");
 			System.err.println("You will be asked to provide the new text of the page, then an edit summary, then whether your edit is minor. If the page does not exist when the command completes, you will get an error message.");
 		}
 	}
 
 	public static class CreateOrReplacePage extends AbstractTextWriteCommand {
 		public void perform(final CommandContext context) throws IOException, MediaWiki.MediaWikiException {
 			final Object[] auxiliaryInput = (Object[]) context.auxiliaryInput;
 			final String editSummary = (String) auxiliaryInput[0];
 			final Boolean minor = (Boolean) auxiliaryInput[1];
 			final MediaWiki.EditToken token = (MediaWiki.EditToken) context.token;
 			final String text = (String) context.essentialInput;
 
 			work("Performing edit...");
 			try {
 				context.wiki.createOrReplacePage(token, text, editSummary, true /*- bot */, minor);
 			} finally {
 				workEnd();
 			}
 		}
 
 		public void help() throws IOException {
 			System.err.println("Creates a page or replaces its entire text on the current wiki.");
 			System.err.println();
 			System.err.println("putpage [<page name>]");
 			System.err.println();
 			System.err.println("The page name is mandatory and will be requested if not provided.");
 			System.err.println("You will be asked to provide the new text of the page, then an edit summary, then whether your edit is minor.");
 		}
 	}
 
 	public static class AppendText extends AbstractTextWriteCommand {
 		public void perform(final CommandContext context) throws IOException, MediaWiki.MediaWikiException {
 			final Object[] auxiliaryInput = (Object[]) context.auxiliaryInput;
 			final String editSummary = (String) auxiliaryInput[0];
 			final Boolean minor = (Boolean) auxiliaryInput[1];
 			final MediaWiki.EditToken token = (MediaWiki.EditToken) context.token;
 			final String text = (String) context.essentialInput;
 
 			work("Performing edit...");
 			try {
 				context.wiki.addText(token, text, true /*- at end */, editSummary, true /*- bot */, minor);
 			} finally {
 				workEnd();
 			}
 		}
 
 		public void help() throws IOException {
 			System.err.println("Adds text to the end of a page on the current wiki.");
 			System.err.println();
 			System.err.println("append [<page name>]");
 			System.err.println();
 			System.err.println("The page name is mandatory and will be requested if not provided.");
 			System.err.println("You will be asked to provide the text to add to the end of the page, then an edit summary, then whether your edit is minor.");
 		}
 	}
 
 	public static class PrependText extends AbstractTextWriteCommand {
 		public void perform(final CommandContext context) throws IOException, MediaWiki.MediaWikiException {
 			final Object[] auxiliaryInput = (Object[]) context.auxiliaryInput;
 			final String editSummary = (String) auxiliaryInput[0];
 			final Boolean minor = (Boolean) auxiliaryInput[1];
 			final MediaWiki.EditToken token = (MediaWiki.EditToken) context.token;
 			final String text = (String) context.essentialInput;
 
 			work("Performing edit...");
 			try {
 				context.wiki.addText(token, text, false /*- not at end */, editSummary, true /*- bot */, minor);
 			} finally {
 				workEnd();
 			}
 		}
 
 		public void help() throws IOException {
 			System.err.println("Adds text to the start of a page on the current wiki.");
 			System.err.println();
 			System.err.println("prepend [<page name>]");
 			System.err.println();
 			System.err.println("The page name is mandatory and will be requested if not provided.");
 			System.err.println("You will be asked to provide the text to add to the start of the page, then an edit summary, then whether your edit is minor.");
 		}
 	}
 
 	public static class MovePage extends AbstractWriteCommand {
 		@Override
 		public void getToken(final CommandContext context) throws IOException, MediaWiki.MediaWikiException, ParseException {
 			super.getToken(context);
 
 			work("Getting move token...");
 			try {
 				context.token = context.wiki.startMove(context.pageName);
 			} finally {
 				workEnd();
 			}
 		}
 
 		@Override
 		public void getEssentialInput(final CommandContext context) throws IOException, NullPointerException, CancellationException {
 			if (context.essentialInput != null)
 				return;
 			context.essentialInput = inputMandatory("full target page name: ");
 		}
 
 		@Override
 		public void getAuxiliaryInput(final CommandContext context) throws IOException, NullPointerException, CancellationException {
 			if (context.auxiliaryInput != null)
 				return;
 			final String moveReason = input("   move reason (<none>): ", null);
 
 			final boolean suppressRedirect = !inputBoolean(" leave a redirect [Y/n]: ", true);
 			final boolean moveTalk = inputBoolean("    move talk too [Y/n]: ", true);
 			final boolean moveSubpages = inputBoolean("move subpages too [Y/n]: ", true);
 
 			context.auxiliaryInput = new Object[] { moveReason, suppressRedirect, moveTalk, moveSubpages };
 		}
 
 		public void perform(final CommandContext context) throws IOException, MediaWiki.MediaWikiException {
 			final Object[] auxiliaryInput = (Object[]) context.auxiliaryInput;
 			final String newFullName = expandInput(context, (String) context.essentialInput);
 			String moveReason = (String) auxiliaryInput[0];
 			final boolean suppressRedirect = (Boolean) auxiliaryInput[1], moveTalk = (Boolean) auxiliaryInput[2], moveSubpages = (Boolean) auxiliaryInput[3];
 			final MediaWiki.EditToken token = (MediaWiki.EditToken) context.token;
 
 			work("Performing move...");
 			try {
 				context.wiki.endMove(token, newFullName, moveReason, suppressRedirect, moveTalk, moveSubpages);
 			} finally {
 				workEnd();
 			}
 		}
 
 		public void help() throws IOException {
 			System.err.println("Moves a page to another name on the current wiki.");
 			System.err.println();
 			System.err.println("move [<page name>]");
 			System.err.println();
 			System.err.println("The page name is mandatory and will be requested if not provided.");
 			System.err.println("You will be asked to provide the new name of the page, then a move reason, then whether you want to leave a redirect and move the talk page and subpages of the page.");
 		}
 	}
 
 	public static class UploadFile extends AbstractWriteCommand {
 		@Override
 		public void getToken(final CommandContext context) throws IOException, MediaWiki.MediaWikiException, ParseException {
 			super.getToken(context);
 
 			work("Getting upload token...");
 			try {
 				context.token = context.wiki.startUpload(context.wiki.getNamespaces().removeNamespacePrefix(context.pageName));
 			} finally {
 				workEnd();
 			}
 		}
 
 		@Override
 		public void getEssentialInput(final CommandContext context) throws IOException, NullPointerException, CancellationException {
 			if (context.essentialInput != null)
 				return;
 			context.essentialInput = inputMandatory(String.format("local file to upload (relative to %s): ", System.getProperty("user.dir")));
 		}
 
 		@Override
 		public void getAuxiliaryInput(final CommandContext context) throws IOException, NullPointerException, CancellationException {
 			if (context.auxiliaryInput != null)
 				return;
 			String pageText = null;
 
 			final String uploadComment = input("upload comment (<none>): ", null);
 			if (inputBoolean("type something else for the file description page if new? [y/N] ", false)) {
 				pageText = inputMultiline();
 			}
 
 			context.auxiliaryInput = new Object[] { uploadComment, pageText };
 		}
 
 		public void perform(final CommandContext context) throws IOException, MediaWiki.MediaWikiException {
 			final MediaWiki.Namespaces namespaces = context.wiki.getNamespaces();
 			if (namespaces.getNamespaceForPage(context.pageName).getID() != MediaWiki.StandardNamespace.MAIN) {
 				context.pageName = namespaces.removeNamespacePrefix(context.pageName);
 				System.err.println(String.format("Note: Page name changed to %s", context.pageName));
 			}
 
 			final Object[] auxiliaryInput = (Object[]) context.auxiliaryInput;
 			final String localName = expandInput(context, (String) context.essentialInput), uploadComment = (String) auxiliaryInput[0], pageText = (String) auxiliaryInput[1];
 			final MediaWiki.EditToken token = (MediaWiki.EditToken) context.token;
 
 			work("Performing upload...");
 			try {
 				context.wiki.endUpload(token, new FileInputStream(localName), uploadComment, pageText);
 			} finally {
 				workEnd();
 			}
 		}
 
 		public void help() throws IOException {
 			System.err.println("Uploads a new revision of a file to the current wiki.");
 			System.err.println();
 			System.err.println("upload[{file | image}] [<file name>]");
 			System.err.println();
 			System.err.println("The file name is mandatory and will be requested if not provided. The File namespace is implied.");
 			System.err.println("You will be asked to provide the name of the local file containing the data to upload, then an upload comment.");
 		}
 	}
 
 	public static class Rollback extends AbstractWriteCommand {
 		@Override
 		public void getToken(final CommandContext context) throws IOException, MediaWiki.MediaWikiException, ParseException {
 			super.getToken(context);
 
 			work("Getting rollback token...");
 			try {
 				context.token = context.wiki.startRollback(context.pageName);
 			} finally {
 				workEnd();
 			}
 		}
 
 		@Override
 		public void getAuxiliaryInput(final CommandContext context) throws IOException, NullPointerException, CancellationException {
 			if (context.auxiliaryInput != null)
 				return;
 			final String rollbackComment = input("rollback comment (<none>): ", null);
 			final boolean bot = inputBoolean("hide from Special:Recentchanges by marking edits as bot-made [Y/n]: ", true);
 
 			context.auxiliaryInput = new Object[] { rollbackComment, bot };
 		}
 
 		@Override
 		public void confirm(final CommandContext context) throws IOException, NullPointerException, CancellationException {
 			context.confirmation = inputBoolean("The last revision was by " + ((MediaWiki.RollbackToken) context.token).getUserName() + ". Roll it back [y/N]? ", false);
 		}
 
 		public void perform(final CommandContext context) throws IOException, MediaWiki.MediaWikiException {
 			if ((context.confirmation instanceof Boolean) && ((Boolean) context.confirmation)) {
 				final Object[] auxiliaryInput = (Object[]) context.auxiliaryInput;
 				final String rollbackComment = (String) auxiliaryInput[0];
 				final boolean bot = (Boolean) auxiliaryInput[1];
 				final MediaWiki.RollbackToken token = (MediaWiki.RollbackToken) context.token;
 
 				work("Performing rollback...");
 				try {
 					context.wiki.endRollback(token, rollbackComment, bot);
 				} finally {
 					workEnd();
 				}
 			}
 		}
 
 		public void help() throws IOException {
 			System.err.println("Rolls back edits by the last editor of a page on the current wiki.");
 			System.err.println();
 			System.err.println("rollback [<page name>]");
 			System.err.println();
 			System.err.println("The page name is mandatory and will be requested if not provided.");
 			System.err.println("You will be asked to provide a rollback reason (autogenerated if not provided) and to state whether you want the rollbed-back edits to be hidden from the default Special:Recentchanges (?hidebots=1).");
 		}
 	}
 
 	public static class Undo extends AbstractEditTokenCommand {
 		@Override
 		public void getEssentialInput(final CommandContext context) throws IOException, NullPointerException, CancellationException {
 			final String revisionIDString = inputMandatory("revision number to undo: ");
 			try {
 				context.essentialInput = Long.valueOf(revisionIDString);
 			} catch (final NumberFormatException nfe) {
 				System.err.println("Invalid input");
 				throw new CancellationException();
 			}
 		}
 
 		@Override
 		public void getAuxiliaryInput(final CommandContext context) throws IOException, NullPointerException, CancellationException {
 			if (context.auxiliaryInput != null)
 				return;
 			final String undoComment = input("undo comment (<none>): ", null);
 			final Boolean minor = inputBoolean("minor edit [y/n] (<per Special:Preferences>): ", null);
 
 			context.auxiliaryInput = new Object[] { undoComment, minor };
 		}
 
 		public void perform(final CommandContext context) throws IOException, MediaWiki.MediaWikiException {
 			final long revisionID = (Long) context.essentialInput;
 			final Object[] auxiliaryInput = (Object[]) context.auxiliaryInput;
 			final String undoComment = (String) auxiliaryInput[0];
 			final Boolean minor = (Boolean) auxiliaryInput[1];
 			final MediaWiki.EditToken token = (MediaWiki.EditToken) context.token;
 
 			work("Performing undo...");
 			try {
 				context.wiki.undoRevision(token, revisionID, undoComment, true /*- bot */, minor);
 			} finally {
 				workEnd();
 			}
 		}
 
 		public void help() throws IOException {
 			System.err.println("Undoes a revision of a page on the current wiki.");
 			System.err.println();
 			System.err.println("undo [<page name>]");
 			System.err.println();
 			System.err.println("The page name is mandatory and will be requested if not provided.");
 			System.err.println("You will be asked to provide a revision number to undo, an undo comment (autogenerated if not provided) and whether your edit to undo the revision is minor.");
 		}
 	}
 
 	public static class Delete extends AbstractWriteCommand {
 		@Override
 		public void getToken(final CommandContext context) throws IOException, MediaWiki.MediaWikiException, ParseException {
 			super.getToken(context);
 
 			work("Getting deletion token...");
 			try {
 				context.token = context.wiki.startDelete(context.pageName);
 			} finally {
 				workEnd();
 			}
 		}
 
 		@Override
 		public void getAuxiliaryInput(final CommandContext context) throws IOException, NullPointerException, CancellationException {
 			if (context.auxiliaryInput != null)
 				return;
 			context.auxiliaryInput = new Object[] { input("deletion reason (<none>): ", null) };
 		}
 
 		@Override
 		public void confirm(final CommandContext context) throws IOException, NullPointerException, CancellationException {
 			context.confirmation = inputBoolean(String.format("Are you sure you want to delete '%s'? [y/N] ", context.pageName), false);
 		}
 
 		public void perform(final CommandContext context) throws IOException, MediaWiki.MediaWikiException {
 			if ((context.confirmation instanceof Boolean) && ((Boolean) context.confirmation)) {
 				final String deletionReason = (String) ((Object[]) context.auxiliaryInput)[0];
 				final MediaWiki.EditToken token = (MediaWiki.EditToken) context.token;
 
 				work("Performing deletion...");
 				try {
 					context.wiki.endDelete(token, deletionReason);
 				} finally {
 					workEnd();
 				}
 			}
 		}
 
 		public void help() throws IOException {
 			System.err.println("Deletes a page from the current wiki.");
 			System.err.println();
 			System.err.println("delete [<page name>]");
 			System.err.println();
 			System.err.println("The page name is mandatory and will be requested if not provided.");
 			System.err.println("You will be asked to provide a deletion reason.");
 		}
 	}
 
 	public static class Protect extends AbstractWriteCommand {
 		@Override
 		public void getToken(final CommandContext context) throws IOException, MediaWiki.MediaWikiException, ParseException {
 			super.getToken(context);
 
 			work("Getting protection token...");
 			try {
 				context.token = context.wiki.startProtect(context.pageName);
 			} finally {
 				workEnd();
 			}
 		}
 
 		@Override
 		public void getEssentialInput(final CommandContext context) throws IOException, NullPointerException, CancellationException {
 			if (context.essentialInput != null)
 				return;
 			boolean another = true;
 			final Map<String, MediaWiki.Protection> protections = new TreeMap<String, MediaWiki.Protection>();
 
 			while (another) {
 				final boolean isProtect = inputBoolean("protect (p) or unprotect (u) an action [P/u]: ", 'p', 'u', true);
 				final String action = inputMandatory(String.format("action to %s [create/edit/move/upload/...]: ", isProtect ? "protect" : "unprotect"));
 
 				if (isProtect) {
 					final String level = input(String.format("require membership in what group to %s [Autoconfirmed/sysop/...]: ", action), "autoconfirmed");
 					final String expiryString = input("until when [y-m-dTh:m:sZ] (<indefinite>): ", null);
 
 					Date expiry;
 					try {
 						expiry = expiryString != null ? MediaWiki.timestampToDate(expiryString) : null;
 					} catch (final ParseException e) {
 						System.err.println("Invalid input");
 						throw new CancellationException();
 					}
 					protections.put(action, context.wiki.new Protection(level, expiry, false, null));
 				} else {
 					protections.put(action, null);
 				}
 
 				another = inputBoolean("Protect or unprotect another action? [y/N] ", false);
 			}
 
 			context.essentialInput = protections;
 		}
 
 		@Override
 		public void getAuxiliaryInput(final CommandContext context) throws IOException, NullPointerException, CancellationException {
 			if (context.auxiliaryInput != null)
 				return;
 			final String protectionReason = input("protection reason (<none>): ", null);
 			final boolean cascade = inputBoolean("automatically apply protections to transcluded pages [y/N]: ", false);
 
 			context.auxiliaryInput = new Object[] { protectionReason, cascade };
 		}
 
 		@Override
 		public void confirm(final CommandContext context) throws IOException, NullPointerException, CancellationException {
 			context.confirmation = inputBoolean(String.format("Are you sure you want to modify protections for '%s'? [y/N] ", context.pageName), false);
 		}
 
 		@SuppressWarnings("unchecked")
 		public void perform(final CommandContext context) throws IOException, MediaWiki.MediaWikiException {
 			if ((context.confirmation instanceof Boolean) && ((Boolean) context.confirmation)) {
 				final Object[] auxiliaryInput = (Object[]) context.auxiliaryInput;
 				final Map<String, MediaWiki.Protection> protections = (Map<String, MediaWiki.Protection>) context.essentialInput;
 				final String protectionReason = (String) auxiliaryInput[0];
 				final boolean cascade = (Boolean) auxiliaryInput[1];
 				final MediaWiki.EditToken token = (MediaWiki.EditToken) context.token;
 
 				work("Performing protection modification...");
 				try {
 					context.wiki.endProtect(token, protections, protectionReason, cascade);
 				} finally {
 					workEnd();
 				}
 			}
 		}
 
 		public void help() throws IOException {
 			System.err.println("Protects or unprotects a page on the current wiki from certain actions.");
 			System.err.println();
 			System.err.println("protect [<page name>]");
 			System.err.println();
 			System.err.println("The page name is mandatory and will be requested if not provided.");
 			System.err.println("You will be asked to provide a list of protections or unprotections to perform, then a protection reason.");
 			System.err.println();
 			System.err.println("For each action to protect, you will be asked to provide an expiry date for the protection in ISO 8601 format using GMT, i.e. year-mo-dyThr:mn:ssZ, as well as the required privilege to perform the action after the protection is done (e.g. autoconfirmed or sysop).");
 		}
 	}
 
 	public static class UserGroupModification extends AbstractWriteCommand {
 		@Override
 		public void parseArguments(final CommandContext context) {
 			final String arguments = context.arguments.trim();
 			if (arguments.length() > 0) {
 				final Set<String> addedGroups = new TreeSet<String>(), removedGroups = new TreeSet<String>();
 
 				final String[] tokens = arguments.split(" +");
 
 				int i = 0;
 				for (; (i < tokens.length) && ((tokens[i].charAt(0) == '-') || (tokens[i].charAt(0) == '+')); i++) {
 					final String token = tokens[i];
 
 					if (token.charAt(0) == '-') {
 						removedGroups.add(token.substring(1));
 					} else if (token.charAt(0) == '+') {
 						addedGroups.add(token.substring(1));
 					}
 				}
 				if (!addedGroups.isEmpty() || !removedGroups.isEmpty()) {
 					context.essentialInput = new Object[] { addedGroups, removedGroups };
 				}
 				if (i < tokens.length) {
 					final StringBuilder sb = new StringBuilder();
 					for (int j = i; j < tokens.length; j++) {
 						sb.append(tokens[j]);
 						sb.append(' ');
 					}
 					sb.deleteCharAt(sb.length() - 1);
 					context.pageName = sb.toString();
 				}
 			}
 		}
 
 		@Override
 		public void getPageName(final CommandContext context) throws IOException, NullPointerException, CancellationException {
 			if (context.pageName != null)
 				return;
 			context.pageName = inputMandatory("user name: ");
 		}
 
 		@Override
 		public void getToken(final CommandContext context) throws IOException, MediaWiki.MediaWikiException, ParseException {
 			super.getToken(context);
 
 			work("Getting user groups modification token...");
 			try {
 				context.token = context.wiki.startUserGroupModification(context.pageName);
 			} finally {
 				workEnd();
 			}
 		}
 
 		@Override
 		public void getEssentialInput(final CommandContext context) throws IOException, NullPointerException, CancellationException {
 			if (context.essentialInput != null)
 				return;
 			final Set<String> addedGroups = new TreeSet<String>(), removedGroups = new TreeSet<String>();
 
 			String line = inputMandatory("        group to add [+name] or remove [-name]: ");
 			while (line != null) {
 				if (line.charAt(0) == '-') {
 					removedGroups.add(line.substring(1));
 				} else if (line.charAt(0) == '+') {
 					addedGroups.add(line.substring(1));
 				} else {
 					System.err.println("Invalid input");
 					throw new CancellationException();
 				}
 				line = input("group to add [+name] or remove [-name] (<end>): ", null);
 			}
 
 			context.essentialInput = new Object[] { addedGroups, removedGroups };
 		}
 
 		@Override
 		public void getAuxiliaryInput(final CommandContext context) throws IOException, NullPointerException, CancellationException {
 			if (context.auxiliaryInput != null)
 				return;
 			context.auxiliaryInput = new Object[] { input("user rights modification reason (<none>): ", null) };
 		}
 
 		@SuppressWarnings("unchecked")
 		public void perform(final CommandContext context) throws IOException, MediaWiki.MediaWikiException {
 			final Object[] essentialInput = (Object[]) context.essentialInput;
 			final Set<String> addedGroups = (Set<String>) essentialInput[0], removedGroups = (Set<String>) essentialInput[1];
 			final String userRightsModificationReason = (String) ((Object[]) context.auxiliaryInput)[0];
 			final MediaWiki.UserGroupsToken token = (MediaWiki.UserGroupsToken) context.token;
 
 			work("Performing user groups modification...");
 			try {
 				context.wiki.endUserGroupModification(token, addedGroups, removedGroups, userRightsModificationReason);
 			} finally {
 				workEnd();
 			}
 		}
 
 		public void help() throws IOException {
 			System.err.println("Changes group memberships for a user on the current wiki.");
 			System.err.println();
 			System.err.println("{usergroups | userrights} [+<group name to add]* [-<group name to remove>]* [<user name>]");
 			System.err.println();
 			System.err.println("The user name and at least one group membership change are mandatory and will be requested if not provided. The User namespace is not implied.");
 			System.err.println("You will be asked to provide a user group modification reason. This command may silently fail if you are not logged in to a user that is able to change certain user group memberships. Use the 'userinfo' command to check if the user groups were added and removed correctly.");
 		}
 	}
 
 	public static class UntilCancelledRepeat implements Command {
 		public void parseArguments(final CommandContext context) {
 			final String arguments = context.arguments.trim();
 			if (arguments.length() > 0) {
 				final String[] tokens = arguments.split(" +", 2);
 
 				final Command command = getCommand(tokens[0]);
 				if (command != null) {
 					context.arguments = tokens.length >= 2 ? tokens[1] : "";
 					context.essentialInput = tokens[0];
 				} else {
 					System.err.println(tokens[0] + ": No such command");
 				}
 			}
 		}
 
 		public void getPageName(final CommandContext context) throws IOException, NullPointerException, CancellationException {}
 
 		public void getToken(final CommandContext context) throws IOException, MediaWiki.MediaWikiException {}
 
 		public void getEssentialInput(final CommandContext context) throws IOException, NullPointerException, CancellationException {
 			if (context.essentialInput != null)
 				return;
 
 			path.addLast("repeat");
 			try {
 				while (true) /*- command retry loop */{
 					updateStatus(context.wiki);
 					prompt();
 
 					final String line = input("");
 					final String[] tokens = line.split(" +", 2);
 
 					if ((tokens.length > 0) && (tokens[0].length() > 0)) {
 						final Command command = getCommand(tokens[0]);
 						if (command != null) {
 							context.arguments = tokens.length >= 2 ? tokens[1] : "";
 							context.essentialInput = tokens[0];
 							break;
 						} else {
 							System.err.println(tokens[0] + ": No such command");
 						}
 					}
 				}
 			} finally {
 				path.removeLast();
 			}
 		}
 
 		public void getAuxiliaryInput(final CommandContext context) throws IOException, NullPointerException, CancellationException {}
 
 		public void confirm(final CommandContext context) throws IOException, NullPointerException, CancellationException {}
 
 		public void perform(final CommandContext context) throws IOException, MediaWiki.MediaWikiException, ParseException {
 			final Command command = getCommand((String) context.essentialInput);
 			MediaWiki wiki = context.wiki;
 			final CommandContext repeatedContext = getCommandContext((String) context.essentialInput);
 			boolean requestsPageName, requestsEssentialInput, requestsAuxiliaryInput, requestsConfirmation;
 			boolean repeatEssentialInput = false, repeatAuxiliaryInput = false;
 
 			path.addLast("repeat");
 			try {
 				command: while (true) {
 					// The first invocation establishes which inputs are
 					// repeated.
 					// Further invocations use these repeated inputs.
 
 					repeatedContext.wiki = wiki;
 					repeatedContext.pageName = null;
 					repeatedContext.token = repeatedContext.confirmation = repeatedContext.temporary = repeatedContext.essentialInput = repeatedContext.auxiliaryInput = null;
 					repeatedContext.arguments = context.arguments;
 					context.arguments = "";
 					command.parseArguments(repeatedContext);
 					try {
 						command.getPageName(repeatedContext);
 						requestsPageName = repeatedContext.pageName != null;
 						if (!getTokenWithRetry(command, repeatedContext))
 							return;
 						command.getEssentialInput(repeatedContext);
 						requestsEssentialInput = repeatedContext.essentialInput != null;
 						if (requestsEssentialInput) {
 							repeatEssentialInput = inputBoolean(requestsPageName ? "Use the settings after the page name for all repetitions? [y/N] " : "Use these settings for all repetitions? [y/N] ", false);
 						}
 						command.getAuxiliaryInput(repeatedContext);
 						requestsAuxiliaryInput = repeatedContext.auxiliaryInput != null;
 						if (requestsAuxiliaryInput) {
 							repeatAuxiliaryInput = inputBoolean(requestsPageName && !requestsEssentialInput ? "Use the settings after the page name for all repetitions? [y/N] " : "Use these settings for all repetitions? [y/N] ", false);
 						}
 						while (true) /*- command retry loop */{
 							try {
 								command.confirm(repeatedContext);
 								requestsConfirmation = repeatedContext.confirmation != null;
 								command.perform(repeatedContext);
 
 								wiki = repeatedContext.wiki;
 								break command; // on success
 							} catch (final MediaWiki.MediaWikiException e) {
 								System.err.println(e.getClass().getName() + ": " + e.getLocalizedMessage());
 								if (!inputBoolean("Retry? [Y/n] ", true)) {
 									continue command;
 								}
 								// Reconfirm the command. For edit conflicts
 								// and the like, we also need to get a new
 								// token.
 								repeatedContext.confirmation = null;
 								if (repeatedContext.token != null) {
 									repeatedContext.token = null;
 									if (!getTokenWithRetry(command, repeatedContext)) {
 										continue command;
 									}
 								}
 							} catch (final IOException e) {
 								System.err.println(e.getClass().getName() + ": " + e.getLocalizedMessage());
 								if (!inputBoolean("Retry? [Y/n] ", true)) {
 									continue command;
 								}
 							} catch (final ParseException e) {
 								System.err.println(e.getClass().getName() + ": " + e.getLocalizedMessage());
 								if (!inputBoolean("Retry? [Y/n] ", true)) {
 									continue command;
 								}
 							}
 						}
 					} catch (final CancellationException ce) {
 						return;
 					}
 				}
 
 				// Now the further invocations. However, if no more input
 				// would be requested, don't just blindly repeat the command
 				// over and over!
 				if (!requestsPageName && (!requestsEssentialInput || repeatEssentialInput) && (!requestsAuxiliaryInput || repeatAuxiliaryInput) && !requestsConfirmation) {
 					System.err.println("Warning: No more input would be requested. Stopping repetition.");
 					return;
 				}
 
 				try {
 					command: while (true) /*- command until cancelled loop */{
 						repeatedContext.wiki = wiki;
 						repeatedContext.pageName = null;
 						repeatedContext.token = repeatedContext.confirmation = repeatedContext.temporary = null;
 						if (!repeatEssentialInput) {
 							repeatedContext.essentialInput = null;
 						}
 						if (!repeatAuxiliaryInput) {
 							repeatedContext.auxiliaryInput = null;
 						}
 						command.getPageName(repeatedContext);
 						if (!getTokenWithRetry(command, repeatedContext))
 							return;
 						if (!repeatEssentialInput) {
 							command.getEssentialInput(repeatedContext);
 						}
 						if (!repeatAuxiliaryInput) {
 							command.getAuxiliaryInput(repeatedContext);
 						}
 						while (true) /*- command retry loop */{
 							try {
 								command.confirm(repeatedContext);
 								command.perform(repeatedContext);
 
 								wiki = repeatedContext.wiki;
 								break; // on success
 							} catch (final MediaWiki.MediaWikiException e) {
 								System.err.println(e.getClass().getName() + ": " + e.getLocalizedMessage());
 								if (!inputBoolean("Retry? [Y/n] ", true)) {
 									continue command;
 								}
 								// Reconfirm the command. For edit conflicts
 								// and the like, we also need to get a new
 								// token.
 								repeatedContext.confirmation = null;
 								if (repeatedContext.token != null) {
 									repeatedContext.token = null;
 									if (!getTokenWithRetry(command, repeatedContext)) {
 										continue command;
 									}
 								}
 							} catch (final IOException e) {
 								System.err.println(e.getClass().getName() + ": " + e.getLocalizedMessage());
 								if (!inputBoolean("Retry? [Y/n] ", true)) {
 									continue command;
 								}
 							} catch (final ParseException e) {
 								System.err.println(e.getClass().getName() + ": " + e.getLocalizedMessage());
 								if (!inputBoolean("Retry? [Y/n] ", true)) {
 									continue command;
 								}
 							}
 						}
 					}
 				} catch (final CancellationException ce) {
 					return;
 				}
 			} finally {
 				path.removeLast();
 
 				context.wiki = wiki;
 			}
 		}
 
 		public void help() throws IOException {
 			System.err.println("Repeats a command until it is cancelled with 'cancel'.");
 			System.err.println();
 			System.err.println("repeat <command>");
 			System.err.println();
 			System.err.println("You may also repeat certain inputs from the command, for example to keep using the same move reason with 'repeat move'. However, if all inputs would be repeated, and the command does not require a page name or confirmation before its action, then the repetition will be stopped with a warning.");
 		}
 	}
 
 	public static class ForPages extends AbstractCommand {
 		protected static final long SCREEN_UPDATE_TIME_MILLISECS = 16;
 
 		@Override
 		public void parseArguments(final CommandContext context) {
 			final String arguments = context.arguments.trim();
 			if (arguments.length() > 0) {
 				final String[] tokens = arguments.split(" +", 2);
 
 				final Command command = getCommand(tokens[0]);
 				if (command != null) {
 					if (command instanceof IterableCommand) {
 						context.arguments = tokens.length >= 2 ? tokens[1] : "";
 						context.essentialInput = tokens[0];
 					} else {
 						System.err.println(tokens[0] + ": Command does not output a list of pages");
 					}
 				} else {
 					System.err.println(tokens[0] + ": No such command");
 				}
 			}
 		}
 
 		@Override
 		public void getEssentialInput(final CommandContext context) throws IOException, NullPointerException, CancellationException {
 			if (context.essentialInput != null)
 				return;
 
 			while (true) /*- command retry loop */{
 				final String line = input("use the output of this command as the list of pages: ");
 				final String[] tokens = line.split(" +", 2);
 
 				if ((tokens.length > 0) && (tokens[0].length() > 0)) {
 					final Command command = getCommand(tokens[0]);
 					if (command != null) {
 						if (command instanceof IterableCommand) {
 							context.arguments = tokens.length >= 2 ? tokens[1] : "";
 							context.essentialInput = tokens[0];
 						} else {
 							System.err.println(tokens[0] + ": Command does not output a list of pages");
 						}
 					} else {
 						System.err.println(tokens[0] + ": No such command");
 					}
 				}
 			}
 		}
 
 		public void perform(final CommandContext context) throws IOException, MediaWiki.MediaWikiException, ParseException {
 			MediaWiki wiki = context.wiki;
 			// 1. Gather the list of pages output by the command.
 			final List<String> pageNames = new LinkedList<String>();
 			final IterableCommand command = (IterableCommand) getCommand((String) context.essentialInput);
 			// For that, use a second context.
 			final CommandContext iteratingCommandContext = getCommandContext((String) context.essentialInput);
 
 			iteratingCommandContext.arguments = context.arguments;
 			iteratingCommandContext.wiki = wiki;
 			command.parseArguments(iteratingCommandContext);
 			try {
 				command.getPageName(iteratingCommandContext);
 				if (!getTokenWithRetry(command, iteratingCommandContext))
 					return;
 				command.getEssentialInput(iteratingCommandContext);
 				command.getAuxiliaryInput(iteratingCommandContext);
 				while (true) /*- command retry loop */{
 					try {
 						command.confirm(iteratingCommandContext);
 						final Iterator<String> i = command.iterator(iteratingCommandContext);
 						long n = 0, lastUpdateTime = System.currentTimeMillis();
 
 						try {
 							String element;
 							while ((element = next(i)) != null) {
 								pageNames.add(new String(element));
 								n++;
 								if ((lastUpdateTime + SCREEN_UPDATE_TIME_MILLISECS) < System.currentTimeMillis()) {
 									lastUpdateTime = System.currentTimeMillis();
 									work(String.format("Getting page names... %9d", n));
 								}
 							}
 						} finally {
 							workEnd();
 						}
 
 						wiki = iteratingCommandContext.wiki;
 						break; // on success
 					} catch (final MediaWiki.MediaWikiException e) {
 						System.err.println(e.getClass().getName() + ": " + e.getLocalizedMessage());
 						if (!inputBoolean("Retry? [Y/n] ", true))
 							return;
 						// Reconfirm the command. For edit conflicts
 						// and the like, we also need to get a new
 						// token.
 						iteratingCommandContext.confirmation = null;
 						if (iteratingCommandContext.token != null) {
 							iteratingCommandContext.token = null;
 							if (!getTokenWithRetry(command, iteratingCommandContext))
 								return;
 						}
 					} catch (final IOException e) {
 						System.err.println(e.getClass().getName() + ": " + e.getLocalizedMessage());
 						if (!inputBoolean("Retry? [Y/n] ", true))
 							return;
 					} catch (final ParseException e) {
 						System.err.println(e.getClass().getName() + ": " + e.getLocalizedMessage());
 						if (!inputBoolean("Retry? [Y/n] ", true))
 							return;
 					}
 				}
 			} catch (final CancellationException ce) {
 				return;
 			}
 
 			// 2. Using these pages, we issue a prompt until that one is
 			// cancelled.
 			path.add(pageNames.size() + " pages");
 			try {
 				prompt: while (true) /*- prompt reissue loop */{
 					updateStatus(wiki);
 					prompt();
 
 					final String line = input("");
 					String[] tokens = line.split(" +", 2);
 
 					final List<Command> prefixes = new ArrayList<Command>();
 					while ((tokens.length > 0) && (tokens[0].length() > 0) && commandPrefixes.containsKey(tokens[0])) {
 						prefixes.add(commandPrefixes.get(tokens[0]));
 						tokens = tokens.length == 2 ? tokens[1].split(" +", 2) : new String[] { "" };
 					}
 
 					if ((tokens.length > 0) && (tokens[0].length() > 0)) {
 						final Command repeatedCommand = getCommand(tokens[0]);
 						if (!pageNames.isEmpty() && (repeatedCommand != null)) {
 							final CommandContext repeatedContext = getCommandContext(tokens[0]);
 							// The first invocation establishes which inputs are
 							// repeated.
 							// Further invocations use these repeated inputs.
 							boolean requestsEssentialInput, requestsAuxiliaryInput, requestsConfirmation;
 							boolean repeatEssentialInput = false, repeatAuxiliaryInput = false;
 
 							repeatedContext.wiki = wiki;
 							repeatedContext.arguments = tokens.length >= 2 ? tokens[1] : "";
 							repeatedCommand.parseArguments(repeatedContext);
 							try {
 								System.err.println(pageNames.get(0));
 								repeatedContext.pageName = pageNames.get(0);
 								if (!getTokenWithRetry(repeatedCommand, repeatedContext)) {
 									continue prompt;
 								}
 								repeatedCommand.getEssentialInput(repeatedContext);
 								requestsEssentialInput = repeatedContext.essentialInput != null;
 								if (requestsEssentialInput) {
 									repeatEssentialInput = inputBoolean("Use these settings for all pages? [y/N] ", false);
 								}
 								repeatedCommand.getAuxiliaryInput(repeatedContext);
 								requestsAuxiliaryInput = repeatedContext.auxiliaryInput != null;
 								if (requestsAuxiliaryInput) {
 									repeatAuxiliaryInput = inputBoolean("Use these settings for all pages? [y/N] ", false);
 								}
 								while (true) /*- command retry loop */{
 									try {
 										repeatedCommand.confirm(repeatedContext);
 										requestsConfirmation = repeatedContext.confirmation != null;
 										for (final Command prefix : prefixes) {
 											prefix.perform(repeatedContext);
 										}
 										repeatedCommand.perform(repeatedContext);
 
 										wiki = repeatedContext.wiki;
 										break; // on success
 									} catch (final MediaWiki.MediaWikiException e) {
 										System.err.println(e.getClass().getName() + ": " + e.getLocalizedMessage());
 										if (!inputBoolean("Retry? [Y/n] ", true)) {
 											continue prompt;
 										}
 										// Reconfirm the command. For edit
 										// conflicts and the like, we also need
 										// to get a new token.
 										repeatedContext.confirmation = null;
 										if (repeatedContext.token != null) {
 											repeatedContext.token = null;
 											if (!getTokenWithRetry(repeatedCommand, repeatedContext)) {
 												continue prompt;
 											}
 										}
 									} catch (final IOException e) {
 										System.err.println(e.getClass().getName() + ": " + e.getLocalizedMessage());
 										if (!inputBoolean("Retry? [Y/n] ", true)) {
 											continue prompt;
 										}
 									} catch (final ParseException e) {
 										System.err.println(e.getClass().getName() + ": " + e.getLocalizedMessage());
 										if (!inputBoolean("Retry? [Y/n] ", true)) {
 											continue prompt;
 										}
 									}
 								}
 
 								// Now the further invocations. However, if no
 								// more input would be requested, ask for
 								// confirmation that the input applies correctly
 								// to the rest of the pages.
 								if (pageNames.size() > 1 && (!requestsEssentialInput || repeatEssentialInput) && (!requestsAuxiliaryInput || repeatAuxiliaryInput) && !requestsConfirmation) {
 									if (!requestsEssentialInput && !requestsAuxiliaryInput && !requestsConfirmation) {
 										if (!inputBoolean(String.format("Is it OK to continue with %d more pages? [y/N] ", pageNames.size() - 1), false)) {
 											continue prompt;
 										}
 									} else {
 										if (!inputBoolean(String.format("Is it OK to apply the same settings to %d more pages? [y/N] ", pageNames.size() - 1), false)) {
 											continue prompt;
 										}
 									}
 								}
 
 								for (int i = 1; i < pageNames.size(); i++) /*- command for pages loop */{
 									System.err.println(pageNames.get(i));
 									repeatedContext.pageName = pageNames.get(i);
 									repeatedContext.token = repeatedContext.confirmation = repeatedContext.temporary = null;
 									repeatedContext.wiki = wiki;
 									if (!repeatEssentialInput) {
 										repeatedContext.essentialInput = null;
 									}
 									if (!repeatAuxiliaryInput) {
 										repeatedContext.auxiliaryInput = null;
 									}
 									if (!getTokenWithRetry(repeatedCommand, repeatedContext)) {
 										continue prompt;
 									}
 									if (!repeatEssentialInput) {
 										repeatedCommand.getEssentialInput(repeatedContext);
 									}
 									if (!repeatAuxiliaryInput) {
 										repeatedCommand.getAuxiliaryInput(repeatedContext);
 									}
 									while (true) /*- command retry loop */{
 										try {
 											repeatedCommand.confirm(repeatedContext);
 											for (final Command prefix : prefixes) {
 												prefix.perform(repeatedContext);
 											}
 											repeatedCommand.perform(repeatedContext);
 
 											wiki = repeatedContext.wiki;
 											break; // on success
 										} catch (final MediaWiki.MediaWikiException e) {
 											System.err.println(e.getClass().getName() + ": " + e.getLocalizedMessage());
 											if (!inputBoolean("Retry? [Y/n] ", true)) {
 												continue prompt;
 											}
 											// Reconfirm the command. For edit
 											// conflicts and the like, we also
 											// need to get a new token.
 											repeatedContext.confirmation = null;
 											if (repeatedContext.token != null) {
 												repeatedContext.token = null;
 												if (!getTokenWithRetry(command, repeatedContext)) {
 													continue prompt;
 												}
 											}
 										} catch (final IOException e) {
 											System.err.println(e.getClass().getName() + ": " + e.getLocalizedMessage());
 											if (!inputBoolean("Retry? [Y/n] ", true)) {
 												continue prompt;
 											}
 										} catch (final ParseException e) {
 											System.err.println(e.getClass().getName() + ": " + e.getLocalizedMessage());
 											if (!inputBoolean("Retry? [Y/n] ", true)) {
 												continue prompt;
 											}
 										}
 									}
 								}
 							} catch (final CancellationException ce) {
 								continue prompt;
 							}
 						} else {
 							final PageListModifier modifier = pageListModifiers.get(tokens[0]);
 							if (modifier != null) {
 								final PageListModifierContext modContext = new PageListModifierContext();
 								modContext.arguments = tokens.length >= 2 ? tokens[1] : "";
 								modContext.wiki = wiki;
 								for (final Object prefix : prefixes)
 									if (prefix instanceof PageListModifier) {
 										((PageListModifier) prefix).modify(pageNames, modContext);
 									}
 								modifier.modify(pageNames, modContext);
 								// Modify the path to show the new page count.
 								path.removeLast();
 								path.add(pageNames.size() + " pages");
 								// Warn the user if all pages have just been
 								// removed.
 								if (pageNames.isEmpty()) {
 									System.err.println("Note: All pages have been removed; commands will not run on any pages");
 								}
 							} else if (!pageNames.isEmpty()) {
 								System.err.println(tokens[0] + ": No such command");
 							}
 						}
 					}
 				}
 			} finally {
 				path.removeLast();
 
 				context.wiki = wiki;
 			}
 		}
 
 		public void help() throws IOException {
 			System.err.println("Invokes a subshell that repeats commands on a set of pages.");
 			System.err.println();
 			System.err.println("for <command yielding list of pages>");
 			System.err.println();
 			System.err.println("The command may ask you for input, and, if successful, will display a new prompt of 'USER@HOST ~/COUNT pages'. Inside this subshell, all commands are repeated for the pages listed by this command.");
 			System.err.println();
 			System.err.println("You may also repeat certain inputs from all commands entered in the subshell, for example to keep using the same deletion reason with 'for transclusions Template:Copyrighted' 'delete'. If all inputs would be repeated, and the command does not require a page name or confirmation before its action, then a final confirmation will appear, and the settings will be applied for all remaining pages.");
 			System.err.println();
 			System.err.println("You may list the pages used by the subshell using the 'list' (or 'lines list' command, or alter the list of pages used for subsequent commands in the subshell with various commands. They are listed in 'lines commands' as belonging to the 'for' subshell commands.");
 		}
 	}
 
 	public static class Pages extends AbstractCommand implements IterableCommand {
 		@Override
 		public void parseArguments(final CommandContext context) {
 			final String[] tokens = context.arguments.split("\\|");
 			final List<String> pageNames = new ArrayList<String>(tokens.length);
 			for (int i = 0; i < tokens.length; i++) {
 				final String token = tokens[i].trim();
 				if (token.length() > 0) {
 					pageNames.add(token);
 				}
 			}
 			if (!pageNames.isEmpty()) {
 				context.essentialInput = pageNames;
 			}
 		}
 
 		@Override
 		public void getEssentialInput(final CommandContext context) throws IOException, NullPointerException, CancellationException {
 			if (context.essentialInput != null)
 				return;
 			final List<String> pageNames = new ArrayList<String>();
 			String line = inputMandatory("        full page name: ");
 			while (line != null) {
 				pageNames.add(line);
 				line = input("full page name (<end>): ", null);
 			}
 			context.essentialInput = pageNames;
 		}
 
 		public void perform(final CommandContext context) throws IOException, MediaWikiException, ParseException {
 			System.err.println("Note: The 'pages' command is intended to be used with 'for' and its page list manipulation commands");
 		}
 
 		@SuppressWarnings("unchecked")
 		public Iterator<String> iterator(final CommandContext context) throws IOException, MediaWikiException, ParseException {
 			return ((List<String>) context.essentialInput).iterator();
 		}
 
 		public void help() throws IOException {
 			System.err.println("Intended for use with 'for'. Outputs a user-defined list of pages.");
 			System.err.println();
 			System.err.println("pages [<pipe-separated page names>]");
 			System.err.println();
 			System.err.println("The page names are mandatory and will be requested (as multi-line input) if not provided.");
 		}
 	}
 
 	public static class ListPages implements PageListModifier {
 		public void modify(final List<String> pages, final PageListModifierContext context) throws IOException, NullPointerException, CancellationException {
 			for (final String page : pages) {
 				context.output.println(page);
 				if (context.output.checkError())
 					return;
 			}
 		}
 
 		public void help() throws IOException {
 			System.err.println("Displays the list of pages used by the current 'for' subshell.");
 			System.err.println();
 			System.err.println("list");
 		}
 	}
 
 	public static class RemoveDuplicates implements PageListModifier {
 		public void modify(final List<String> pages, final PageListModifierContext context) throws IOException, NullPointerException, CancellationException {
 			work("Sorting...");
 			try {
 				Collections.sort(pages);
 
 				work("Removing duplicates...");
 				for (int i = pages.size() - 2; i >= 0; i--) {
 					if (MediaWiki.titleToDisplayForm(pages.get(i + 1)).equals(MediaWiki.titleToDisplayForm(pages.get(i)))) {
 						pages.remove(i + 1);
 					}
 				}
 			} finally {
 				workEnd();
 			}
 		}
 
 		public void help() throws IOException {
 			System.err.println("Removes duplicates in the list of pages used by the current 'for' subshell.");
 			System.err.println();
 			System.err.println("removedup[{e | licate}]s");
 		}
 	}
 
 	public static abstract class MorePagesFromAList implements PageListModifier {
 		public void modify(final List<String> pages, final PageListModifierContext context) throws IOException, NullPointerException, CancellationException {
 			final String[] tokens = context.arguments.split(" +", 2);
 			final List<String> pageNames = new LinkedList<String>();
 			final Command c = getCommand(tokens[0]);
 			if (c == null) {
 				System.err.println(tokens[0] + ": No such command");
 				throw new CancellationException();
 			} else if (!(c instanceof IterableCommand)) {
 				System.err.println(tokens[0] + ": Command does not output a list of pages");
 				throw new CancellationException();
 			}
 			final IterableCommand command = (IterableCommand) c;
 			final CommandContext iteratingCommandContext = getCommandContext(tokens[0]);
 
 			iteratingCommandContext.arguments = tokens.length >= 2 ? tokens[1] : "";
 			iteratingCommandContext.wiki = context.wiki;
 			command.parseArguments(iteratingCommandContext);
 			try {
 				command.getPageName(iteratingCommandContext);
 				if (!getTokenWithRetry(command, iteratingCommandContext))
 					return;
 				command.getEssentialInput(iteratingCommandContext);
 				command.getAuxiliaryInput(iteratingCommandContext);
 				while (true) /*- command retry loop */{
 					try {
 						command.confirm(iteratingCommandContext);
 						final Iterator<String> i = command.iterator(iteratingCommandContext);
 						long n = 0, lastUpdateTime = System.currentTimeMillis();
 
 						try {
 							String element;
 							while ((element = next(i)) != null) {
 								pageNames.add(new String(element));
 								n++;
 								if ((lastUpdateTime + ForPages.SCREEN_UPDATE_TIME_MILLISECS) < System.currentTimeMillis()) {
 									lastUpdateTime = System.currentTimeMillis();
 									work(String.format("Getting page names... %9d", n));
 								}
 							}
 
 							combine(pages, pageNames);
 						} finally {
 							workEnd();
 						}
 
 						break; // on success
 					} catch (final MediaWiki.MediaWikiException e) {
 						System.err.println(e.getClass().getName() + ": " + e.getLocalizedMessage());
 						if (!inputBoolean("Retry? [Y/n] ", true))
 							return;
 						// Reconfirm the command. For edit conflicts
 						// and the like, we also need to get a new
 						// token.
 						iteratingCommandContext.confirmation = null;
 						if (iteratingCommandContext.token != null) {
 							iteratingCommandContext.token = null;
 							if (!getTokenWithRetry(command, iteratingCommandContext))
 								return;
 						}
 					} catch (final IOException e) {
 						System.err.println(e.getClass().getName() + ": " + e.getLocalizedMessage());
 						if (!inputBoolean("Retry? [Y/n] ", true))
 							return;
 					} catch (final ParseException e) {
 						System.err.println(e.getClass().getName() + ": " + e.getLocalizedMessage());
 						if (!inputBoolean("Retry? [Y/n] ", true))
 							return;
 					}
 				}
 			} catch (final CancellationException ce) {
 				return;
 			}
 		}
 
 		/**
 		 * Modifies <code>oldPages</code> according to a new list of gathered
 		 * pages.
 		 * 
 		 * @param oldPages
 		 *            The list to modify.
 		 * @param newPages
 		 *            The list of pages gathered, to be removed, added or
 		 *            intersected.
 		 */
 		public abstract void combine(List<String> oldPages, List<String> newPages);
 	}
 
 	public static class AddPages extends MorePagesFromAList {
 		@Override
 		public void combine(final List<String> oldPages, final List<String> newPages) {
 			work("Adding pages...");
 			oldPages.addAll(newPages);
 		}
 
 		public void help() throws IOException {
 			System.err.println("Adds more pages to the list used by the current 'for' subshell.");
 			System.err.println();
 			System.err.println("{add | union} <command> [<arguments>]");
 			System.err.println();
 			System.err.println("Pages returned by the command will be added to the list. Duplicates added by this command are not removed. For that, see the 'removeduplicates' command.");
 		}
 	}
 
 	public static class RemovePages extends MorePagesFromAList {
 		@Override
 		public void combine(final List<String> oldPages, final List<String> newPages) {
 			work("Removing pages...");
 			oldPages.removeAll(newPages);
 		}
 
 		public void help() throws IOException {
 			System.err.println("Removes pages from the list used by the current 'for' subshell.");
 			System.err.println();
 			System.err.println("{remove | difference} <command> [<arguments>]");
 			System.err.println();
 			System.err.println("Pages returned by the command will be removed from the list.");
 		}
 	}
 
 	public static class IntersectPages extends MorePagesFromAList {
 		@Override
 		public void combine(final List<String> oldPages, final List<String> newPages) {
 			work("Intersecting pages...");
 			oldPages.retainAll(newPages);
 		}
 
 		public void help() throws IOException {
 			System.err.println("Intersects the list that is used by the current 'for' subshell with another.");
 			System.err.println();
 			System.err.println("intersect <command> [<arguments>]");
 			System.err.println();
 			System.err.println("Only pages present in both the list returned by the command and the list used by the current 'for' subshell will be kept.");
 		}
 	}
 
 	public static class NormalizeNames implements PageListModifier {
 		public void modify(final List<String> pages, final PageListModifierContext context) throws IOException, NullPointerException, CancellationException {
 			while (true) /*- retry loop */{
 				try {
 					work("Normalising page names...");
 
 					try {
 						context.wiki.normalizeTitles(pages);
 					} finally {
 						workEnd();
 					}
 
 					break; // on success
 				} catch (final MediaWiki.MediaWikiException e) {
 					System.err.println(e.getClass().getName() + ": " + e.getLocalizedMessage());
 					if (!inputBoolean("Retry? [Y/n] ", true))
 						return;
 				} catch (final IOException e) {
 					System.err.println(e.getClass().getName() + ": " + e.getLocalizedMessage());
 					if (!inputBoolean("Retry? [Y/n] ", true))
 						return;
 				}
 			}
 		}
 
 		public void help() throws IOException {
 			System.err.println("Normalise the names of pages used by the current 'for' subshell.");
 			System.err.println();
 			System.err.println("normali{s | z}e");
 		}
 	}
 
 	public static class CountPages extends ForPages {
 		@Override
 		public void perform(final CommandContext context) throws IOException, MediaWiki.MediaWikiException, ParseException {
 			MediaWiki wiki = context.wiki;
 			final IterableCommand command = (IterableCommand) getCommand((String) context.essentialInput);
 			final CommandContext iteratingCommandContext = getCommandContext((String) context.essentialInput);
 
 			iteratingCommandContext.arguments = context.arguments;
 			iteratingCommandContext.wiki = wiki;
 			command.parseArguments(iteratingCommandContext);
 			try {
 				command.getPageName(iteratingCommandContext);
 				if (!getTokenWithRetry(command, iteratingCommandContext))
 					return;
 				command.getEssentialInput(iteratingCommandContext);
 				command.getAuxiliaryInput(iteratingCommandContext);
 				while (true) /*- command retry loop */{
 					try {
 						command.confirm(iteratingCommandContext);
 						final Iterator<String> i = command.iterator(iteratingCommandContext);
 						long n = 0, lastUpdateTime = System.currentTimeMillis();
 
 						try {
 							while (next(i) != null) {
 								n++;
 								if ((lastUpdateTime + SCREEN_UPDATE_TIME_MILLISECS) < System.currentTimeMillis()) {
 									lastUpdateTime = System.currentTimeMillis();
 									work(String.format("Getting page names... %9d", n));
 								}
 							}
 						} finally {
 							workEnd();
 						}
 						context.output.println(n);
 
 						wiki = iteratingCommandContext.wiki;
 						break; // on success
 					} catch (final MediaWiki.MediaWikiException e) {
 						System.err.println(e.getClass().getName() + ": " + e.getLocalizedMessage());
 						if (!inputBoolean("Retry? [Y/n] ", true))
 							return;
 						// Reconfirm the command. For edit conflicts
 						// and the like, we also need to get a new
 						// token.
 						iteratingCommandContext.confirmation = null;
 						if (iteratingCommandContext.token != null) {
 							iteratingCommandContext.token = null;
 							if (!getTokenWithRetry(command, iteratingCommandContext))
 								return;
 						}
 					} catch (final IOException e) {
 						System.err.println(e.getClass().getName() + ": " + e.getLocalizedMessage());
 						if (!inputBoolean("Retry? [Y/n] ", true))
 							return;
 					} catch (final ParseException e) {
 						System.err.println(e.getClass().getName() + ": " + e.getLocalizedMessage());
 						if (!inputBoolean("Retry? [Y/n] ", true))
 							return;
 					}
 				}
 			} catch (final CancellationException ce) {
 				return;
 			}
 		}
 
 		@Override
 		public void help() throws IOException {
 			System.err.println("Counts the pages returned by a command.");
 			System.err.println();
 			System.err.println("count <command yielding list of pages>");
 			System.err.println();
 			System.err.println("For example, you may want to count all of the redirects on the current wiki using 'count allpages' with a redirect filter, or the number of transclusions of Template:Stub using 'count transclusions Template:Stub'.");
 		}
 	}
 
 	protected static void checkLogin(final MediaWiki wiki) throws IOException, NullPointerException, CancellationException {
 		if (refuseLogin)
 			return;
 		try {
 			if (wiki.getCurrentUser().isAnonymous()) {
 				if (inputBoolean("Would you like to log in? [y/N] ", false)) {
 					final Login login = new Login();
 
 					final CommandContext context = new CommandContext();
 					context.wiki = wiki;
 
 					login.getEssentialInput(context);
 					login.getAuxiliaryInput(context);
 
 					while (true) /*- command retry loop */{
 						try {
 							login.perform(context);
 
 							break; // on success
 						} catch (final MediaWiki.MediaWikiException e) {
 							System.err.println(e.getClass().getName() + ": " + e.getLocalizedMessage());
 							if (!inputBoolean("Retry? [Y/n] ", true)) {
 								break;
 							}
 						} catch (final IOException e) {
 							System.err.println(e.getClass().getName() + ": " + e.getLocalizedMessage());
 							if (!inputBoolean("Retry? [Y/n] ", true)) {
 								break;
 							}
 						}
 					}
 				} else if (inputBoolean("Would you like to stay anonymous for this session? [Y/n] ", true)) {
 					refuseLogin = true;
 				}
 			}
 		} catch (final MediaWiki.MediaWikiException e) {
 			System.err.println("Could not obtain current user information");
 		} catch (final IOException e) {
 			System.err.println("Could not obtain current user information");
 		}
 	}
 
 	protected static <T> T next(final Iterator<T> i, final String workLong) throws ParseException, MediaWiki.MediaWikiException, IOException {
 		work(workLong);
 		try {
 			return next(i);
 		} finally {
 			workEnd();
 		}
 	}
 
 	protected static <T> T next(final Iterator<T> i) throws ParseException, MediaWiki.MediaWikiException, IOException {
 		try {
 			return i.hasNext() ? i.next() : null;
 		} catch (final MediaWiki.IterationException ie) {
 			final Throwable t = ie.getCause();
 			if (t instanceof ParseException)
 				throw (ParseException) t;
 			else if (t instanceof MediaWiki.MediaWikiException)
 				throw (MediaWiki.MediaWikiException) t;
 			else if (t instanceof IOException)
 				throw (IOException) t;
 			else if (t instanceof RuntimeException)
 				throw (RuntimeException) t;
 			else if (t instanceof Error)
 				throw (Error) t;
 			else
 				throw (InternalError) new InternalError("Unexpected exception").initCause(t);
 		}
 	}
 
 	protected static boolean getTokenWithRetry(final Command command, final CommandContext context) throws IOException {
 		try {
 			while (true) {
 				try {
 					command.getToken(context);
 					return true; // on success
 				} catch (final MediaWiki.MediaWikiException e) {
 					System.err.println(e.getClass().getName() + ": " + e.getLocalizedMessage());
 					if (!inputBoolean("Retry? [Y/n] ", true))
 						return false;
 				} catch (final IOException e) {
 					System.err.println(e.getClass().getName() + ": " + e.getLocalizedMessage());
 					if (!inputBoolean("Retry? [Y/n] ", true))
 						return false;
 				} catch (final ParseException e) {
 					System.err.println(e.getClass().getName() + ": " + e.getLocalizedMessage());
 					if (!inputBoolean("Retry? [Y/n] ", true))
 						return false;
 				}
 			}
 		} catch (final CancellationException ce) {
 			return false;
 		}
 	}
 
 	protected static void displayUser(final MediaWiki.User user, final PrintWriter output) {
 		output.println(String.format("%20s  %s", user.getRegistrationDate() != null ? MediaWiki.dateToISO8601(user.getRegistrationDate()) : "", user.getUserName()));
 		if (output.checkError())
 			return;
 		if (!user.getGroups().isEmpty()) {
 			output.print(" groups: ");
 			if (output.checkError())
 				return;
 			final Iterator<String> gi = user.getGroups().iterator();
 
 			if (gi.hasNext()) {
 				output.print(gi.next());
 				if (output.checkError())
 					return;
 			}
 			while (gi.hasNext()) {
 				output.print(", ");
 				if (output.checkError())
 					return;
 				output.print(gi.next());
 				if (output.checkError())
 					return;
 			}
 			output.println();
 			if (output.checkError())
 				return;
 		}
 		if (!user.getRights().isEmpty()) {
 			output.print(" rights: ");
 			if (output.checkError())
 				return;
 			final Iterator<String> ri = user.getGroups().iterator();
 
 			if (ri.hasNext()) {
 				output.print(ri.next());
 				if (output.checkError())
 					return;
 			}
 			while (ri.hasNext()) {
 				output.print(", ");
 				if (output.checkError())
 					return;
 				output.print(ri.next());
 				if (output.checkError())
 					return;
 			}
 			output.println();
 			if (output.checkError())
 				return;
 		}
 		if (user.getBlockingUser() != null) {
 			output.println(String.format(" blocked by %s (%s)", user.getBlockingUser(), user.getBlockReason()));
 		}
 	}
 
 	public static class Exit extends AbstractCommand {
 		public void perform(final CommandContext context) throws IOException, MediaWikiException, ParseException {
 			throw new NullPointerException("exiting the shell");
 		}
 
 		public void help() throws IOException {
 			System.err.println("Exits the shell and any subshells started by the 'for' command.");
 			System.err.println();
 			System.err.println("exit | quit");
 		}
 	}
 }
