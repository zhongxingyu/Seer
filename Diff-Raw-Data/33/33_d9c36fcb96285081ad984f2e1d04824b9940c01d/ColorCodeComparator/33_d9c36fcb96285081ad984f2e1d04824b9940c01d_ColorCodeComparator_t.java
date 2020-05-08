 package com.blogspot.nhu313.mastermind.colorcode;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 import com.blogspot.nhu313.mastermind.result.ResultCode;
 
 public class ColorCodeComparator {
 	
 	/**
 	 * @param code1 
 	 * @param code2
 	 * @return Result code that is sorted in the order of CORRECT, CORRECT_COLOR_ONLY, INCORRECT
 	 */
 	public List<ResultCode> isSameColors(ColorCode[] code1, ColorCode[] code2) {
 		if (!isValidateCompareObject(code1, code2)){
 			return null;
 		}
 		
 		final int codeSize = code1.length;
 
 		List<ResultCode> results = new ArrayList<ResultCode>();
 		List<ColorCode> originalColors = getColorList(code1);
 		List<ColorCode> otherColors = getColorList(code2);
 
 		removeSameItemInSamePosition(originalColors, otherColors);
 		int numberOfCorrectResult = codeSize - originalColors.size();
 		addResultCode(results, ResultCode.CORRECT, numberOfCorrectResult);
 
 		if (results.size() < codeSize){
 			int numberOfCorrectColorResult = countCorrectColorOnlyResult(originalColors, otherColors);
 			addResultCode(results, ResultCode.CORRECT_COLOR_ONLY, numberOfCorrectColorResult);
 
 			if (results.size() < codeSize){
 				int numberOfIncorrectResult = codeSize - results.size();
 				addResultCode(results, ResultCode.INCORRECT, numberOfIncorrectResult);
 			}
 		}
 		return results;
 	}
 
 	protected boolean isValidateCompareObject(ColorCode[] code1, ColorCode[] code2) {
 		if (code1 == null || code2 == null){
 			return false;
 		}
 		
 		if (code1.length != code2.length){
 			return false;
 		}
 		
 		if (code1.length < 1){
 			return false;
 		}
 		
 		return true;
 	}
 
 	protected int countCorrectColorOnlyResult(List<ColorCode> original, List<ColorCode> other) {
 		int numberOfCorrectColorResult = 0;
 		for (int i = 0; i < original.size(); i++){
 			ColorCode code = original.get(i);
 			if (other.contains(code)){
				other.remove(code);
 				numberOfCorrectColorResult++;
 			}
 		}
 		return numberOfCorrectColorResult;
 	}
 
 	private void addResultCode(List<ResultCode> results, ResultCode code, int size) {
 		for (int i = 0; i < size; i++){
 			results.add(code);
 		}
 	}
 
 	protected void removeSameItemInSamePosition(List<ColorCode> original, List<ColorCode> other) {
 		for (int i = 0; i < original.size(); i++){
 			if (original.get(i) == other.get(i)){
 				original.set(i, null);
 				other.set(i, null);
 			}
 		}
 		
 		original.removeAll(Collections.singleton(null));
 		other.removeAll(Collections.singleton(null));
 	}
 
 	private List<ColorCode> getColorList(ColorCode[] codes) {
 		//Can't use Arrays.asList() because it doesn't support remove operation
 		List<ColorCode> list = new ArrayList<ColorCode>();
 		for (ColorCode code : codes){
 			list.add(code);
 		}
 		
 		return list;
 	}
 }
