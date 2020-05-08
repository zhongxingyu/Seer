 package info.plagiatsjaeger;
 
 import info.plagiatsjaeger.interfaces.IComparer;
 import info.plagiatsjaeger.interfaces.OnCompareFinishedListener;
 import info.plagiatsjaeger.types.CompareResult;
 import info.plagiatsjaeger.types.Settings;
 
 import java.util.ArrayList;
 
 import org.apache.log4j.Logger;
 
 
 /**
  * Die klasse stellt Funktionen zum Vergleichen von Texten zur Verfuegung. Als
  * Schnittstelle dient dass Interface {@link IComparer}.
  * 
  * @author Andreas
  */
 public class MyComparer implements IComparer
 {
	// TODO: default Werte eintragen
	private static int					NUM_WORDS_TO_COMPARE;
	private static double				THRESHOLD;
	private static int					MAX_WORDS_BETWEEN_RESULTS;
 
 	private String[]					_checkWords;
 	private String[]					_sourceWords;
 
 	private String						_currentLink;
 	private int							_currentDocId;
 
 	private OnCompareFinishedListener	_onCompareFinishedListener;
 
 	private int							_rId;
 
 	private boolean						_isInSources;
 
 	private static final Logger			_logger	= Logger.getLogger(MyComparer.class.getName());
 
 	/**
 	 * Legt einen neuen Comparer fuer einen Report an.
 	 * 
 	 * @param rId
 	 */
 	public MyComparer(int rId)
 	{
 		_rId = rId;
 		Settings settings = Settings.getInstance();
 		MAX_WORDS_BETWEEN_RESULTS = settings.getCompareJumpLength();
 		NUM_WORDS_TO_COMPARE = settings.getCompareSentenceLength();
 		THRESHOLD = ((double) settings.getThreshold()) / 100;
 	}
 
 	@Override
 	public void compareText(String checkText, String sourceText, String link)
 	{
 		_currentLink = link;
 		compareText(checkText, sourceText);
 	}
 
 	@Override
 	public void compareText(String checkText, String sourceText, int docId)
 	{
 		_currentDocId = docId;
 		_logger.info("CompareText for Document: " + docId);
 		compareText(checkText, sourceText);
 	}
 
 	/**
 	 * Vergleicht 2 Text miteinander und liefert die Uebereinstimmungen als
 	 * ArrayList von {@link CompareResult CompareResults} zurueck.
 	 * 
 	 * @param checkText
 	 * @param sourceText
 	 * @return
 	 */
 	private ArrayList<CompareResult> compareText(String checkText, String sourceText)
 	{
 		_logger.info("Compare started");
 		ArrayList<CompareResult> result = new ArrayList<CompareResult>();
 		ArrayList<CompareResult> unmergedCompareResults = new ArrayList<CompareResult>();
 
 		WordProcessing wordProcessing = new WordProcessing();
 
 		_checkWords = wordProcessing.splitToWords(checkText);
 		_sourceWords = wordProcessing.splitToWords(sourceText);
 
 		int checkResultStart = -1;
 		int checkResultEnd = -1;
 		int sourceResultStart = -1;
 		int sourceResultEnd = -1;
 
 		StringBuilder sbCheckText = new StringBuilder();
 		StringBuilder sbSourceText = new StringBuilder();
 
 		boolean resultFound = false;
 		double sumSimilarity = 0.0;
 		int countSimilarity = 0;
 
 		for (int iCheck = 0; iCheck < _checkWords.length; iCheck++)
 		{
 			sbCheckText.delete(0, sbCheckText.length());
 			int jCheck = 0;
 			for (; (jCheck < NUM_WORDS_TO_COMPARE) && ((iCheck + jCheck) < _checkWords.length); jCheck++)
 			{
 				sbCheckText.append(_checkWords[iCheck + jCheck]).append(" ");
 			}
 
 			double similarity = 0.0;
 			for (int iSource = 0; iSource < _sourceWords.length; iSource++)
 			{
 				sbSourceText.delete(0, sbSourceText.length());
 				int jSource = 0;
 				for (; (jSource < NUM_WORDS_TO_COMPARE) && ((iSource + jSource) < _sourceWords.length); jSource++)
 				{
 					sbSourceText.append(_sourceWords[iSource + jSource]).append(" ");
 				}
 
 				resultFound = false;
 				sumSimilarity = 0.0;
 				countSimilarity = 0;
 				while ((similarity = compareStrings(sbCheckText.toString(), sbSourceText.toString())) >= THRESHOLD)
 				{
 					resultFound = true;
 					if (checkResultStart < 0)
 					{
 						checkResultStart = iCheck;
 					}
 					if (sourceResultStart < 0)
 					{
 						sourceResultStart = iSource;
 					}
 					_logger.info("Found Part with: " + similarity);
 					checkResultEnd = iCheck + jCheck + 1;
 					sourceResultEnd = iSource + jSource + 1;
 					sumSimilarity += similarity;
 					countSimilarity++;
 
 					sbCheckText.delete(0, _checkWords[iCheck].length() + 1);
 					sbSourceText.delete(0, _sourceWords[iSource].length() + 1);
 					if (_checkWords.length > (iCheck + NUM_WORDS_TO_COMPARE) && _sourceWords.length > (iSource + NUM_WORDS_TO_COMPARE))
 					{
 						sbCheckText.append(_checkWords[iCheck + NUM_WORDS_TO_COMPARE]).append(" ");
 						sbSourceText.append(_sourceWords[iSource + NUM_WORDS_TO_COMPARE]).append(" ");
 					}
 					else
 					{
 						break;
 					}
 					iCheck++;
 					iSource++;
 				}
 				if (resultFound)
 				{
 					_logger.info("SubResult found");
					_logger.info("Threshold: " + THRESHOLD + " " + sumSimilarity + "/" + countSimilarity + "=" + ((sumSimilarity/countSimilarity)));
					
 					CompareResult compareResult = new CompareResult(_rId, checkResultStart, checkResultEnd, sourceResultStart, sourceResultEnd, sumSimilarity / countSimilarity);
 					unmergedCompareResults.add(compareResult);
 
 					checkResultStart = -1;
 					sourceResultStart = -1;
 					checkResultEnd = -1;
 					sourceResultEnd = -1;
 					break;
 				}
 			}
 			iCheck += NUM_WORDS_TO_COMPARE - 1;
 		}
 
 		StringBuilder resultText = new StringBuilder();
 		// Compareresults zusammenfuegen und Trefferlinks schreiben.
 		boolean init = true;
 
 		// Sonderfall abfangen: genau 1 Treffer gefunden, d.H. nichts zu mergen
 		if (unmergedCompareResults.size() == 1)
 		{
 			CompareResult compareResultCheck = unmergedCompareResults.get(0);
 			for (int wordCounter = compareResultCheck.getSourceStart(); wordCounter < compareResultCheck.getSourceEnd() && wordCounter < _sourceWords.length; wordCounter++)
 			{
 				resultText.append(_sourceWords[wordCounter]).append(" ");
 			}
 			compareResultCheck.setSourceText(resultText.toString());
 			compareResultCheck.setIsInSources(_isInSources);
 			result.add(compareResultCheck);
 		}
 		for (int resultCounter = 0; resultCounter < unmergedCompareResults.size() - 1; resultCounter++)
 		{
 			_logger.info("Merge Results: " + resultCounter);
 			if (init)
 			{
 				if (checkText.contains(_currentLink.replace("http://", "").replace("www.", "")))
 				{
 					_isInSources = true;
 				}
 				init = false;
 			}
 			CompareResult compareResultCheck = unmergedCompareResults.get(resultCounter);
 			CompareResult compareResultSource = unmergedCompareResults.get(resultCounter + 1);
 
 			// Zusammenhaengende Text erkennen und start/end aktualisieren
 			sumSimilarity = compareResultCheck.getSimilarity();
 			countSimilarity = 1;
 			while ((compareResultCheck.getCheckEnd() >= (compareResultSource.getCheckStart() - MAX_WORDS_BETWEEN_RESULTS)) && (compareResultCheck.getSourceEnd() >= (compareResultSource.getSourceStart() - MAX_WORDS_BETWEEN_RESULTS)))
 			{
 				sumSimilarity += compareResultSource.getSimilarity();
 				countSimilarity++;
 				compareResultCheck.setCheckEnd(compareResultSource.getCheckEnd());
 				compareResultCheck.setSourceEnd(compareResultSource.getSourceEnd());
 				resultCounter++;
 				if (resultCounter < unmergedCompareResults.size() - 1)
 				{
 					compareResultSource = unmergedCompareResults.get(resultCounter + 1);
 				}
 				else
 				{
 					break;
 				}
 			}
 			compareResultCheck.setSimilarity(sumSimilarity / countSimilarity);
 			// plagiatsText fuer plagStart/plagEnd setzen.
 			resultText.delete(0, resultText.length());
 			for (int wordCounter = compareResultCheck.getSourceStart(); wordCounter < _sourceWords.length && wordCounter < compareResultCheck.getSourceEnd(); wordCounter++)
 			{
 				resultText.append(_sourceWords[wordCounter]).append(" ");
 			}
 			compareResultCheck.setSourceText(resultText.toString());
 			compareResultCheck.setIsInSources(_isInSources);
 
 			_logger.info("### TREFFER #######################");
 			_logger.info("Source:       " + _currentLink);
 			_logger.info("Source:       " + _currentDocId);
 			_logger.info("Start-Ende:   " + compareResultCheck.getCheckStart() + "-" + compareResultCheck.getCheckEnd());
 			_logger.info("Text:         " + compareResultCheck.getSourceText());
 			_logger.info("Aehnlichkeit: " + compareResultCheck.getSimilarity());
 			_logger.info("###################################");
 			if (_currentDocId <= 0)
 			{
 				_logger.info("Found link: " + _currentLink + " with Similarity " + compareResultCheck.getSimilarity());
 			}
 			else
 			{
 				_logger.info("Found document: " + _currentDocId + " with Similarity " + compareResultCheck.getSimilarity());
 			}
 			result.add(compareResultCheck);
 		}
 		if (_currentDocId <= 0)
 		{
 			_logger.info("OnCompareFinished");
 			_onCompareFinishedListener.onCompareResultFound(result, _currentLink);
 		}
 		else
 		{
 			_logger.info("OnCompareFinished");
 			_onCompareFinishedListener.onCompareResultFound(result, _currentDocId);
 		}
 		return result;
 	}
 
 	/**
 	 * Vergleicht zwei eingebene Texte und berechnet deren Uebereinstimmg.
 	 * 
 	 * @return Aehnlichkeit der zwei eingegebenen Texte 0.0-1.0
 	 */
 	public double compareStrings(String text1, String text2)
 	{
 		ArrayList<String> pairs1 = wordLetterPairs(text1.toUpperCase());
 		ArrayList<String> pairs2 = wordLetterPairs(text2.toUpperCase());
 		int similarityCounter = 0;
 		int completeSize = pairs1.size() + pairs2.size();
 		for (int i = 0; i < pairs1.size(); i++)
 		{
 			Object pair1 = pairs1.get(i);
 			for (int j = 0; j < pairs2.size(); j++)
 			{
 				Object pair2 = pairs2.get(j);
 				if (pair1.equals(pair2))
 				{
 					similarityCounter++;
 					pairs2.remove(j);
 					break;
 				}
 			}
 		}
 		// 2.0 => Skalierung auf Werte zwischen 0 und 1
 		return (2.0 * similarityCounter) / completeSize;
 	}
 
 	/**
 	 * Trennt den kompletten Text in Zeichenpaare auf. (Leerzeichen werden
 	 * entfernt)
 	 * 
 	 * @param text
 	 * @return ArrayList<String> mit je 2 Zeichen
 	 */
 	private ArrayList<String> wordLetterPairs(String text)
 	{
 		ArrayList<String> result = new ArrayList<String>();
 		// Tokenize the string and put the tokens/words into an array
 		String[] words = text.split("\\s");
 		// For each word
 		for (int wordCounter = 0; wordCounter < words.length; wordCounter++)
 		{
 			// Find the pairs of characters
 			String[] pairsInWord = letterPairs(words[wordCounter]);
 			for (int p = 0; p < pairsInWord.length; p++)
 			{
 				result.add(pairsInWord[p]);
 			}
 		}
 		return result;
 	}
 
 	/**
 	 * Trennt ein eingebenen Text(Wort) in Zeichenpaare auf(Alle Zeichen werden
 	 * gleich behandelt)
 	 * 
 	 * @param word
 	 * @return
 	 */
 	private String[] letterPairs(String word)
 	{
 		String[] result = new String[0];
 		int numPairs = word.length() - 1;
 		if (numPairs > 0)
 		{
 			result = new String[numPairs];
 			for (int letterCounter = 0; letterCounter < numPairs; letterCounter++)
 			{
 				result[letterCounter] = word.substring(letterCounter, letterCounter + 2);
 			}
 		}
 		return result;
 	}
 
 	@Override
 	public void setOnCompareFinishedListener(OnCompareFinishedListener listener)
 	{
 		_onCompareFinishedListener = listener;
 	}
 }
