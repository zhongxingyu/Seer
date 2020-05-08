 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.*;
 import parser.*;
 import writer.*;
 import event.*;
 import exception.*;
 import filter.*;
 
 
 public class TivooSystem
 {
 
     private List<Event> myOriginalList;
     private List<Event> myFilteredList;
     private Set<Writer> myWriters;
     private Set<Parser> myParsers;
     private FilterDecorator myHeadFilter;
     private static List<Parser> myParserList = new ArrayList<Parser>();
     static
     {
         myParserList.add(new DukeBasketballParser());
         myParserList.add(new DukeCalendarParser());
         myParserList.add(new GoogleCalendarParserChen());
         myParserList.add(new NFLParser());
         myParserList.add(new TVParser());
 
     }
 
     public TivooSystem ()
     {
         myOriginalList = new ArrayList<Event>();
         myFilteredList = new ArrayList<Event>();
         myParsers = new HashSet<Parser>();
         myWriters = new HashSet<Writer>();
         myHeadFilter = null;
     }
 
 
     /**
      * Loads file in to a corresponding parser. 
      * @param file
      */
     public void loadFile (File file)
     {
         boolean parserFound = false;
         for (Parser parser : myParserList)
         {
             try
             {
                 parser.loadFile(file);
                 myParsers.add(parser);
                 parserFound = true;
                 break;
             }
             catch (TivooUnrecognizedFeed e)
             {
                 continue;
             }
         }
         if (!parserFound)
         {
             throw new TivooUnrecognizedFeed();
         }
     }
 
 
     /**
      * Adds a instance of FilterByKeyword to myFilterList. 
      * @param keyword
      */
     public void addFilterByKeyword (String keyword)
     {
         FilterDecorator filter = new FilterByKeyword(keyword);
         addFilter(filter);
     }
 
     /**
      * Adds a instance of FilterByTimeFrame to myFilterList. 
      * @param startTime
      * @param endTime
      */
     public void addFilterByTimeFrame (String startTime, String endTime)
     {
         FilterDecorator filter = new FilterByTimeFrame(startTime, endTime);
         addFilter(filter);
     }
 
     /**
      * Adds a instance of FilterByKeywordSorting to myFilterList. 
      * @param keyword
      */
     public void addFilterByKeywordSorting (String keyword)
     {
         FilterByKeywordSorting filter = new FilterByKeywordSorting(keyword);
         addFilter(filter);
     }
 
     /**
      * Adds a instance of FilterByKeywordList to myFilterList. 
      * @param keywordList
      */
     public void addFilterByKeywordList (String[] keywordList)
     {
         FilterByKeywordList filter = new FilterByKeywordList(keywordList);
         addFilter(filter);
     }
 
     /**
      * Adds the input filter to myFilterList. 
      * @param filter
      */
     private void addFilter (FilterDecorator filter)
     {
         if (myHeadFilter == null)
         {
             myHeadFilter = filter;
         }
         else
         {
             filter.appendFilter(myHeadFilter);
             myHeadFilter = filter;
         }
     }
 
     /**
      * Adds a instance of SummaryAndDetailPagesWriter to myWriterList. 
      * @param directory
      */
     public void addSummaryAndDetailPagesWriter (String directory)
     {
         Writer writer = new SummaryAndDetailsPagesWriter(directory);
         addWriter(writer);
     }
 
     /**
      * Adds a instance of ConflictWriter to myWriterList. 
      * @param directory
      */
     public void addConflictWriter (String directory)
     {
         Writer writer = new ConflictWriter(directory);
         addWriter(writer);
     }
 
 	/**
      * Adds a instance of CalendarWriter to myWriterList. 
      * @param directory
      * @param startDate
      * @param timeFrame
      */
     public void addCalendarWriter (String directory,
                                    String startDate,
                                    String timeFrame)
     {
         Writer writer = new CalendarWriter(directory, startDate, timeFrame);
         addWriter(writer);
     }
 
     /**
      * Adds a instance of ListWriter to myWriterList. 
      * @param directory
      */
     public void addListWriter (String directory)
     {
         Writer writer = new ListWriter(directory);
         addWriter(writer);
     }
 
     /**
      * Adds the input writer to myWriterList. 
      * @param writer
      */
     private void addWriter (Writer writer)
     {
         myWriters.add(writer);
     }
 
     /**
      * Clears running history and makes the parsers parse input xml files, filters filter parsed event list, and writers 
      * output htmls according to filered events. 
      */
     public void perform ()
     {
 
         clear();
 
         parse();
 
         filter();
 
         output();
 
     }
 
     /**
      * Iterates over the selected parsers and makes each parser parse their input xmls. Parsed events will be stored in
      * myOriginalList.
      */
     private void parse ()
     {
         if (myParsers.size() == 0)
         {
             throw new TivooNoParserSelected();
         }
         for (Parser parser : myParsers)
         {
             parser.parse();
             myOriginalList.addAll(parser.getEventList());
         }
     }
 
     /**
      * Iterates over the linked filters and makes each filter filter events recursively. The output list of each filter
      * will be used as the input of its sub-filter. The output of the head filter will be stored in myFilteredList.
      */
     private void filter ()
     {
         if (myHeadFilter == null)
         {
             throw new TivooNoFilterSelected();
         }
         myHeadFilter.filter(myOriginalList);
         myFilteredList = myHeadFilter.getFilteredList();
     }
 
     /**
      * Iterates over the selected writers and makes each writer output pages using myFilteredList as input events.
      */
     private void output ()
     {
         if (myParsers.size() == 0)
         {
             throw new TivooNoWriterSelected();
         }
 
         for (Writer writer : myWriters)
         {
             writer.outputHTML(myFilteredList);
         }
     }
 
     /**
      * Clears myOriginalList and myFilteredList. 
      */
     private void clear ()
     {
         myOriginalList = new ArrayList<Event>();
         myFilteredList = new ArrayList<Event>();
     }
 }
