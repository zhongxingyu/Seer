 package ehe.insig.io;
 
 import ehe.insig.dataModel.HeisigItem;
 
 public class HeisigData {
 
 	//field definitions
 	public enum ColumnDefinition {
 		KANJI ("Kanji"), HEISIG_INDEX ("Heisig Index"), KEY_WORDS ("Key Words"), KANI_PRIMITIVES ("Kanji Primitives"), 
 		LESSON_NUMBER ("Lesson Number"), STROKE_COUNT ("Stroke Count"), INDEX_ORDINAL ("Index Ordinal");
 
 		private String display;
 		public String getDisplay() {
 			return display;
 		}
 		ColumnDefinition(String display){
 			this.display = display;
 		}
 		public static String getValueForField(HeisigItem heisigItem, ColumnDefinition columnDefinition) {
 			switch (columnDefinition) {
 				case LESSON_NUMBER :
 					return "" + heisigItem.getLessonNumber();
 				case INDEX_ORDINAL :
 					return "" + heisigItem.getIndexOrdinal();
 				case STROKE_COUNT :
 					return "" + heisigItem.getKanjiStrokeCount();
				case KEY_WORDS :
 					return heisigItem
 							.keywordsToString(HeisigData.INNER_SEPERATOR);
				case KANI_PRIMITIVES :
 					return heisigItem
 							.kanjiPrimitivesToString(HeisigData.INNER_SEPERATOR);
 				case KANJI :
 					return heisigItem.getKanji();
 				case HEISIG_INDEX :
 					return heisigItem.getHeisigIndex();
 			}
 			throw new AssertionError("Unknown column definition: "
 					+ columnDefinition);
 		}
 	}
 
 	//file markup
 	public static final String COMMENT = "#";
 	public static final String SEPERATOR = ",";
 	public static final String INNER_SEPERATOR = "~";
 	public static final Object CLOSE_SESAME = "\"";
 	public static final Object OPEN_SESAME = "\"";
 
 }
