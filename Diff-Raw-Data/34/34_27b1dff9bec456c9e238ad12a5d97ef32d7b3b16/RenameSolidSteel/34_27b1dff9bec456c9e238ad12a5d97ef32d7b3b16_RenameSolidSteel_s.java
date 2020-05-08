 package ebs.music;
 
 import java.io.File;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 /**
  * Created by Aleksey Dubov
  * Date: 6/17/12
  * Time: 4:30 PM
  * Copyright (c) 2012
  */
 public class RenameSolidSteel {
	private static final SimpleDateFormat SRC1_DATE_FORMAT = new SimpleDateFormat("dd.mm.yy");
	private static final SimpleDateFormat SRC4_DATE_FORMAT = new SimpleDateFormat("dd_mm_yyyy");
	private static final SimpleDateFormat SRC6_DATE_FORMAT = new SimpleDateFormat("dd-MMM-yy");
	private static final SimpleDateFormat SRC8_DATE_FORMAT = new SimpleDateFormat("dd-mm-yy");
	private static final SimpleDateFormat SRC11_DATE_FORMAT = new SimpleDateFormat("dd-mm-yyyy");
 
 	private static final SolidSteelPattern[] SOLID_STEEL_PATTERNS = new SolidSteelPattern[]{
 			new SolidSteelPattern(Pattern.compile("(\\d\\d.\\d\\d.\\d\\d) - (.+) \\(pt.(\\d)\\)(\\..+)"), //1
 					SRC1_DATE_FORMAT, 1, 2, 3, 0, 4),
 			new SolidSteelPattern(Pattern.compile("(\\d\\d.\\d\\d.\\d\\d) - (.+) \\(pt.(\\d).+(\\d)\\)(\\..+)"), //2
 					SRC1_DATE_FORMAT, 1, 2, 3, 4, 5),
 			new SolidSteelPattern(Pattern.compile(".+(\\d\\d\\d\\d.\\d\\d.\\d\\d)\\.0(\\d|\\d.) (.+)\\)(\\..+)"), //3
 					MusicBase.TRUE_DATE_FORMAT, 1, 3, 2, 0, 4),
 			new SolidSteelPattern(Pattern.compile(".+ (\\d+.\\d+.\\d\\d\\d\\d).Part.(\\d)...(\\d) - (.+)(\\..+)"), //4
 					SRC4_DATE_FORMAT, 1, 4, 2, 3, 5),
 			new SolidSteelPattern(Pattern.compile(".+ (\\d\\d\\d\\d.\\d\\d.\\d\\d).+(\\d).(\\d). (.+)(\\..+)"), //5
 					MusicBase.TRUE_DATE_FORMAT, 1, 4, 2, 3, 5),
 			new SolidSteelPattern(Pattern.compile("(\\d\\d-...-\\d\\d).0(\\d) (.+).(\\..+)"), //6
 					SRC6_DATE_FORMAT, 1, 3, 2, 0, 4),
 			new SolidSteelPattern(Pattern.compile("0(\\d) - .+ - (\\d\\d.\\d\\d.\\d\\d) - (.+)(\\..+)"), //7
 					SRC1_DATE_FORMAT, 2, 3, 1, 0, 4),
 			new SolidSteelPattern(Pattern.compile("0(\\d)..+.-.(.+)..(\\d\\d-\\d\\d-\\d\\d).+(\\..+)"), //8
 					SRC8_DATE_FORMAT, 3, 2, 1, 0, 4),
 			new SolidSteelPattern(Pattern.compile("(\\d\\d.\\d\\d.\\d\\d) - (.+) \\(pt\\.(\\d).+(\\d)\\).+(\\..+)"), //9
 					SRC1_DATE_FORMAT, 1, 2, 3, 4, 5),
 			new SolidSteelPattern(Pattern.compile("(\\d\\d.\\d\\d.\\d\\d) - (.+)(\\..+)"), //10
 					SRC1_DATE_FORMAT, 1, 2, 0, 0, 3),
 			new SolidSteelPattern(Pattern.compile(".+(\\d\\d-\\d\\d-\\d\\d\\d\\d)-0(\\d) (.+).(\\..+)"), //11
 					SRC11_DATE_FORMAT, 1, 3, 2, 0, 4),
 	};
 
 	public static void main(String[] args) {
 		String basePath = new File(".").getAbsolutePath();
 
 		if (args.length == 0) {
 			System.out.println("No basePath specified! Working with current.");
 		}
 
 		System.out.println("Current basePath: " + basePath);
 
 		File baseDirectory = new File(basePath);
 		if (!baseDirectory.isDirectory()) {
 			System.out.println("Base directory is not a directory! Quiting...");
 			return;
 		}
 
 		File[] subFiles = baseDirectory.listFiles();
 		if (subFiles == null) {
 			System.out.println("Given directory is invalid! Nothing to check!");
 			return;
 		}
 
 		for (File subFile : subFiles) {
 			SolidSteelFileTagsBeagleBuddy fileTags = null;
 
 			try {
 				for (SolidSteelPattern pattern : SOLID_STEEL_PATTERNS) {
 					Matcher matcher = pattern.getPattern().matcher(subFile.getName());
 					while (matcher.find()) {
 						Date date = pattern.getDateFormat().parse(matcher.group(pattern.getDate()));
 						String part1 = pattern.getPart1() == 0 ? "" : matcher.group(pattern.getPart1());
 						String part2 = pattern.getPart2() == 0 ? "" : matcher.group(pattern.getPart2());
 						fileTags = new SolidSteelFileTagsBeagleBuddy(subFile.getAbsolutePath(), date,
 								matcher.group(pattern.getName()), part1, part2, matcher.group(pattern.getExtension()));
 					}
 					if (fileTags != null) {
 						break;
 					}
 				}
 			} catch (ParseException e) {
 				e.printStackTrace();
 				continue;
 			}
 
 			if (fileTags != null) {
 //				System.out.println(fileTags.getNewSolidSteelFileName());
 				fileTags.updateTags();
 			}
 		}
 	}
 }
