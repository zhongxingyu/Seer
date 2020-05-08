 package com.github.mnicky.bible4j.cli;
 
 import java.util.Arrays;
 
 import com.github.mnicky.bible4j.formats.BibleExporterException;
 import com.github.mnicky.bible4j.formats.BibleImporterException;
 import com.github.mnicky.bible4j.storage.BibleStorage;
 import com.github.mnicky.bible4j.storage.BibleStorageException;
 
 public class CommandParserLauncher {
     
     private final BibleStorage storage;
 
     private boolean helpRequested = false;
     
     static final String BIBLE_READ_COMMAND = "read";
     static final String BIBLE_SEARCH_COMMAND = "search";
     static final String IMPORT_COMMAND = "import";
     static final String EXPORT_COMMAND = "export";
     static final String NOTES_COMMAND = "note";
     static final String DICTIONARY_COMMAND = "dict";
     static final String DAILY_READINGS_COMMAND = "daily";
     static final String BOOKMARKS_COMMAND = "bkmark";
     static final String HELP_COMMAND = "help";
     
     public CommandParserLauncher(BibleStorage bibleStorage) {
 	this.storage = bibleStorage;
     }
     
     public void launch(String[] args) throws BibleStorageException, BibleImporterException, BibleExporterException {
 	
 	CommandParser parser = getCommandParser(args);
 	
 	if (parser != null) {
 	    if (helpRequested)
 		parser.printHelp();
 	    else
 		parser.parse(Arrays.copyOfRange(args, 1, args.length));
 	}
 	else
 	    printHelp();
     }
 
 
 
     private CommandParser getCommandParser(String[] args) {
 	
 	if (args[0].equalsIgnoreCase(BIBLE_READ_COMMAND))
 	    return new ReadCommandParser(storage);
 	
 	else if (args[0].equalsIgnoreCase(BIBLE_SEARCH_COMMAND))
 	    return new SearchCommandParser(storage);
 	
 	else if (args[0].equalsIgnoreCase(IMPORT_COMMAND))
 	    return new ImportCommandParser(storage);
 	
 	else if (args[0].equalsIgnoreCase(EXPORT_COMMAND))
 	    return new ExportCommandParser(storage);
 	
 	else if (args[0].equalsIgnoreCase(NOTES_COMMAND))
 	    return new NotesCommandParser(storage);
 	
 	else if (args[0].equalsIgnoreCase(DICTIONARY_COMMAND))
 	    return new DictionaryCommandParser(storage);
 	
 	else if (args[0].equalsIgnoreCase(DAILY_READINGS_COMMAND))
 	    return new DailyReadingsCommandParser(storage);
 	
 	else if (args[0].equalsIgnoreCase(BOOKMARKS_COMMAND))
 	    return new BookmarksCommandParser(storage);
 	
 	else if (args[0].equalsIgnoreCase(HELP_COMMAND) && !helpRequested) {
 	    helpRequested  = true;
 	    if (args.length > 1)
 		return getCommandParser(Arrays.copyOfRange(args, 1, args.length));
 	}
 
 	return null;
     }
 
 
     
     private void printHelp() {
 	System.out.println("Use '" + HELP_COMMAND + " COMMAND' for help.");
 	System.out.println();
 	System.out.println("Possible commands:");
 	System.out.println(BIBLE_READ_COMMAND + "\t read the Bible");
 	System.out.println(BIBLE_SEARCH_COMMAND + "\t search the Bible");
 	System.out.println(NOTES_COMMAND + "\t add notes to the Bible text");
 	System.out.println(BOOKMARKS_COMMAND + "\t bookmark Bible passage");
 	System.out.println(DAILY_READINGS_COMMAND + "\t view daily readings");
 	System.out.println(DICTIONARY_COMMAND + "\t look up a word in a Biblical dictionary");
 	System.out.println(IMPORT_COMMAND + "\t import the Bible");
 	System.out.println(EXPORT_COMMAND + "\t export the Bible");
     }
     
     
     //for testing purposes
     public static void main(String[] args) throws BibleStorageException, BibleImporterException, BibleExporterException {
 	CommandParserLauncher cpl = new CommandParserLauncher(null);
	String[] params = {"help", "note"};
 	cpl.launch(params);
     }
     
 }
