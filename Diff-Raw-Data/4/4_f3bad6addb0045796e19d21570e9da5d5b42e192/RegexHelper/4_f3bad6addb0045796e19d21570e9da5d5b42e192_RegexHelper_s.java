 package org.semtex;
 
 import java.util.regex.Pattern;
 
 /**
  * Created by ZEUS on 30.08.13.
  */
 public class RegexHelper {
     private final static String STR_REGEX_TRIM = "[ ]*(\r|\n|\r\n|\n\r)[ ]*";
    // <[ ]*(td|TD)[ ]*((valign|VALIGN)="top")?[ ]*(class|CLASS)="(week_block|Week_Block|WEEK_BLOCK)"[ ]*(rowspan|ROWSPAN)="[0-9]+"[ ]*((bgcolor|BGCOLOR)="#[a-fA-F0-9]+")?[ ]*>
    private final static String STR_REGEX_BLOCKSTART = "<[ ]*(td|TD)[ ]*((valign|VALIGN)=\"top\")?[ ]*(class|CLASS)=\"(week_block|Week_Block|WEEK_BLOCK)\"[ ]*(rowspan|ROWSPAN)=\"[0-9]+\"[ ]*((bgcolor|BGCOLOR)=\"#[a-fA-F0-9]+\")?[ ]*>";
     private final static String STR_REGEX_TD_TAG = "(<(td|TD)|</(td|TD)[ ]*>)";
     private final static String STR_REGEX_CLOSING_TAG = "[ ]*</[A-Za-z]+[ ]*[>]?";
     // <(span|SPAN)[ ]+(class|CLASS)="(tooltip|TOOLTIP)"[^>]*>((.|\n)*?)</(span|SPAN)[ ]*>
     private final static String STR_REGEX_TOOLTIP = "<(span|SPAN)[ ]+(class|CLASS)=\"(tooltip|TOOLTIP)\"[^>]*>((.|\n)*?)</(span|SPAN)[ ]*>";
     // <(div|DIV)[ ]*>(Mo|Di|Mi|Do|Fr|Sa|So)?[ ]*([0-9]{1,2}.[0-9]{1,2}.[0-9]{2,4})?[ ]*[0-2][0-9]:[0-5][0-9][ ]*-[ ]*[0-2][0-9]:[0-5][0-9]
     private final static String STR_REGEX_TIME_FULL = "<(div|DIV)[ ]*>(Mo|Di|Mi|Do|Fr|Sa|So)?[ ]*([0-9]{1,2}.[0-9]{1,2}.[0-9]{2,4})?[ ]*[0-2][0-9]:[0-5][0-9][ ]*-[ ]*[0-2][0-9]:[0-5][0-9]";
     // (?<=>)[ A-Za-z]+([0-9]{1,2}.[0-9]{1,2}.[0-9]{2,4})?[ ]*[0-2][0-9]:[0-5][0-9][ ]*-[ ]*[0-2][0-9]:[0-5][0-9]
     private final static String STR_REGEX_TIME_SMALLEST = "[0-2][0-9]:[0-5][0-9][ ]*-[ ]*[0-2][0-9]:[0-5][0-9]";
     private final static String STR_REGEX_TIME_SPLIT = "[ ]*-[ ]*";
     private final static String STR_REGEX_TIME_WEEKDAY = "(Mo|Di|Mi|Do|Fr|Sa|So)";
     //<(span|SPAN) (class|CLASS)="(resource|Resource|RESOURCE)">.*?</(span|SPAN)[ ]*?>
     private final static String STR_REGEX_RESOURCE_FULL = "<(span|SPAN) (class|CLASS)=\"(resource|Resource|RESOURCE)\">.*?</(span|SPAN)[ ]*?>";
     private final static String STR_REGEX_RESOURCE_SMALL = "(?<=>).*?(?=<)";
     //<(span|SPAN) (class|CLASS)="(person|Person|PERSON)"[ ]*>.*?</(span|SPAN)[ ]*?>
     private final static String STR_REGEX_PERSON_FULL = "<(span|SPAN) (class|CLASS)=\"(person|Person|PERSON)\"[ ]*>.*?</(span|SPAN)[ ]*?>";
     private final static String STR_REGEX_PERSON_SMALL = "(?<=>).*?(?=<)";
     // (?<=[0-9]{2}:[0-9]{2}<br/>)[^<]*(?=<)
     private final static String STR_REGEX_TITLE = "(?<=[0-9]{2}:[0-9]{2}<br/>)[^<]*(?=<)";
     private final static String STR_REGEX_COURSE_RESOURCE = "[A-Z]{2,6}[0-9]*[A-Z][0-9]*";
 
 
     public final static Pattern REGEX_TRIM = Pattern.compile(STR_REGEX_TRIM);
 
     public final static Pattern REGEX_BLOCKSTART = Pattern.compile(STR_REGEX_BLOCKSTART);
     public final static Pattern REGEX_TD_TAG = Pattern.compile(STR_REGEX_TD_TAG);
     public final static Pattern REGEX_CLOSING_TAG = Pattern.compile(STR_REGEX_CLOSING_TAG);
 
     public final static Pattern REGEX_TOOLTIP = Pattern.compile(STR_REGEX_TOOLTIP);
 
     public final static Pattern REGEX_TIME_FULL = Pattern.compile(STR_REGEX_TIME_FULL);
     public final static Pattern REGEX_TIME_SMALLEST = Pattern.compile(STR_REGEX_TIME_SMALLEST);
     public final static Pattern REGEX_TIME_SPLIT = Pattern.compile(STR_REGEX_TIME_SPLIT);
     public final static Pattern REGEX_TIME_WEEKDAY = Pattern.compile(STR_REGEX_TIME_WEEKDAY);
 
     public final static Pattern REGEX_RESOURCE_FULL = Pattern.compile(STR_REGEX_RESOURCE_FULL);
     public final static Pattern REGEX_RESOURCE_SMALL = Pattern.compile(STR_REGEX_RESOURCE_SMALL);
 
     public final static Pattern REGEX_PERSON_FULL = Pattern.compile(STR_REGEX_PERSON_FULL);
     public final static Pattern REGEX_PERSON_SMALL = Pattern.compile(STR_REGEX_PERSON_SMALL);
 
     public final static Pattern REGEX_TITLE = Pattern.compile(STR_REGEX_TITLE);
     public final static Pattern REGEX_COURSE_RESOURCE = Pattern.compile(STR_REGEX_COURSE_RESOURCE);
 
 
     public static String breakAndTrimText(String text)
     {
         return REGEX_TRIM.matcher(text).replaceAll("");
     }
 }
