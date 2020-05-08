 package ehe.insig.io;
 
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.OutputStreamWriter;
 import java.util.ArrayList;
 import java.util.List;
 
 import ehe.insig.dataModel.HeisigItem;
 import ehe.insig.io.HeisigData.ColumnDefinition;
 
 public class HeisigDataWriter {
 
 	public void writeHeisigData(String path, List<HeisigItem> kanjiList)
 			throws IOException {
 		List<ColumnDefinition> columnDefinitions = new ArrayList<ColumnDefinition>();
 		addDefaultColumnPositions(columnDefinitions);
 
 		File file = new File(path);
 		if (!file.exists()) {
 			file.createNewFile();
 		}
 		BufferedWriter bufferedWriter = new BufferedWriter(
 				new OutputStreamWriter(new FileOutputStream(file), "UTF8"));
 
 		boolean firstPass = true;
 		//write header
 		String header = generateHeaderEntry(columnDefinitions);
 
 		bufferedWriter.write(HeisigData.COMMENT);
 		bufferedWriter.write(header);
 		bufferedWriter.write("\n");
 
 		//write data
 		firstPass = true;
 		for (HeisigItem heisigItem : kanjiList) {
 			if (firstPass == true) {
 				firstPass = false;
 			} else {
 				bufferedWriter.write("\n");
 			}
 			bufferedWriter.write(generateFileEntry(columnDefinitions, heisigItem));
 		}
 		bufferedWriter.close();
 	}
 
 	private void addDefaultColumnPositions(List<ColumnDefinition> columnDefinitions) {
 		columnDefinitions.add(ColumnDefinition.HEISIG_INDEX);
 		columnDefinitions.add(ColumnDefinition.KANJI);
 		columnDefinitions.add(ColumnDefinition.KEY_WORDS);
 		columnDefinitions.add(ColumnDefinition.KANI_PRIMITIVES);
 		columnDefinitions.add(ColumnDefinition.STROKE_COUNT);
 		columnDefinitions.add(ColumnDefinition.INDEX_ORDINAL);
 		columnDefinitions.add(ColumnDefinition.LESSON_NUMBER);
 	}
 
 	/**
 	 * @return an entry describing the fields
 	 */
 	private String generateHeaderEntry(List<ColumnDefinition> columnDefinitions) {
 		StringBuilder stringBuilder = new StringBuilder();
 
 		boolean firstPass = true;
 		for (ColumnDefinition columnDefinition : columnDefinitions) {
 			if (firstPass == true) {
 				firstPass = false;
 			} else {
 				stringBuilder.append(HeisigData.SEPERATOR);
 			}
 			generateValue(columnDefinition.getDisplay(), stringBuilder);
 		}
 		return stringBuilder.toString();
 	}
 
 	/**
 	 * Index Kanji Keyword(s) primitive(s) kanji stroke count index ordinal
 	 * lesson number
 	 * 
 	 * @param heisigItem
 	 * @return
 	 */
 	private String generateFileEntry(List<ColumnDefinition> columnDefinitions,
 			HeisigItem heisigItem) {
 		StringBuilder stringBuilder = new StringBuilder();
 
 		boolean firstPass = true;
 		for (ColumnDefinition columnDefinition : columnDefinitions) {
 			if (firstPass == true) {
 				firstPass = false;
 			} else {
 				stringBuilder.append(HeisigData.SEPERATOR);
 			}
 			generateValue(ColumnDefinition.getValueForField(heisigItem,
 					columnDefinition), stringBuilder);
 		}
 		return stringBuilder.toString();
 	}
 
 	private void generateValue(Object value, StringBuilder stringBuilder) {
 		stringBuilder.append(HeisigData.OPEN_SESAME);
 		stringBuilder.append(value);
 		stringBuilder.append(HeisigData.CLOSE_SESAME);
 	}
 }
