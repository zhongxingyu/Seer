 package wordRetrieval;
 
 import java.io.*;
 import java.util.*;
 import java.text.*;
 
 import ML.TfIdf;
 
 
 public class KeywordRetrieval
 {
     public static int[] posScoresArray;
     public static int[] negScoresArray;
     public static WordInfo[] result;
     /**
      * @param filter filter to apply to the keyword retrieval part.
      * @param index index for the comments file.
      * @param isApplyAction indicates if the keyword retrieval has to be done
      * for the apply action or for the generating product.
      *
      * If it is true then it takes the info from the comments designed for the
      * index because the file already exists.
      * If it is false the it takes the info from the new file generated.
      */
     public WordInfo[] run(String filter, int index) throws IOException {
 
         ArrayList adjectives = new ArrayList();
         ArrayList connectionWords = new ArrayList();
         ArrayList revWords = new ArrayList();
         ArrayList stopWords = new ArrayList();
         ArrayList everyWords = new ArrayList();
         ArrayList negQual = new ArrayList();
         ArrayList posQual = new ArrayList();
         ArrayList decAug = new ArrayList();
         ArrayList incAug = new ArrayList();
         ArrayList productWords = new ArrayList();
         Scanner s = null;
 
         //////////////////stopwords////////////////////////////////////
         //list of stopwords are read from the stopwords file
         //stopwords are stored in the stopwords array list
 
         stopWords = readWordList("src/wordRetrieval/resources/StopWords.txt");
         adjectives = readWordList("src/wordRetrieval/resources/Adjectives.txt");
         connectionWords = readWordList("src/wordRetrieval/resources/ConnectionWords.txt");
 
         /////////////////keyword rating code////////////////////////////////
         negQual = readWordList("src/wordRetrieval/resources/neg-qual.txt");
         posQual = readWordList("src/wordRetrieval/resources/pos-qual.txt");
 
         decAug = readWordList("src/wordRetrieval/resources/dec-aug.txt");
         incAug = readWordList("src/wordRetrieval/resources/inc-aug.txt");
 
         productWords = readWordList("src/wordRetrieval/resources/lotr.txt");
 
         ////////end keyword rating code///////////
 
 
         /////the custom regular expression///
         //We use a self defined regular expression to manipulate the result
         //The following keywords are defined:
         //[+] : any word but stopwords
         //[*] : any word
         //[a] : adjective
         //[c] : connection word
         //[;string] : any user define string
         ////////////////////////////////////
         //Example: we wish to find all occurences of an adjective + "quality"
         //[a][;quality]
         ///////////////////////
         //Any combination of two words
         //[*][*]
         String regExpression = filter;         //an adjective + "picture"
 
         int regExpressionLen = 0;
         int m = regExpression.indexOf("[", 0);
         while (m >= 0) {
             regExpressionLen++;
             m = regExpression.indexOf("[", m + 1);
         }
 
 
         String[] tokenArray;
         tokenArray = new String[regExpressionLen];
         //////////////////comments////////////////////////////////////
         int k = 0;
         int rNum = 0;
         String curRating = "";
 
         try {
             // Creates the file
             File file = new File("comments" + index + ".txt");
             // If the file exists then 
             if (file.exists()) {
                 // Do the process
                 s = new Scanner(new BufferedReader(new FileReader("comments" + index + ".txt")));
                 while (s.hasNext()) {
                     rNum++;
                     //Walk through the header of a comment
                     //Store the rating information
                     String curW = s.next();
                     if (curW.equals("Name:")) {
                         while (!curW.equals("Stars:"))
                             curW = s.next();
                         curRating = s.next();
                         while (!curW.equals("Comment:"))
                             curW = s.next();
                         curW = s.next();
                     }
 
                     String thisComment = "";
                     ArrayList curCommentWords = new ArrayList();
 
                     //Store the first word in the token array
                     curW = removeSpecialCharacters(curW.toLowerCase());
                     tokenArray[0] = curW;
 
                      //////keyword rating code/////
                     everyWords.add(curW + "|" + rNum + ":" + curRating);
 
                     //Store the remaining n-1 words in the token array
                     for (int i = 1; i < regExpressionLen; i++) {
                         curW = s.next();
                         curW = removeSpecialCharacters(curW.toLowerCase());
                         tokenArray[i] = curW;
 
                         //////keyword rating code/////
                         everyWords.add(curW + "|" + rNum + ":" + curRating);
                     }
 
                     //Walk through all words in the comment
                     while (!curW.equals("---")) {
                         int tPos = -1;
                         char curToken;
                         boolean match = true;
 
                         for (int i = 0; i < regExpressionLen; i++) {
                             tPos = regExpression.indexOf("[", tPos + 1);
                             curToken = regExpression.substring(tPos + 1, tPos + 2).charAt(0);
 
                             switch (curToken) {
                                 case '+':
                                     int stopLoc = Collections.binarySearch(stopWords, tokenArray[i]);
                                     if (stopLoc >= 0 || tokenArray[i].equals("") || tokenArray[i].equals(" "))
                                         match = false;
                                     break;
                                 case '*':
                                     break;
                                 case 'a':
                                     int adjLoc = Collections.binarySearch(adjectives, tokenArray[i]);
                                     if (adjLoc < 0 || tokenArray[i].equals("") || tokenArray[i].equals(" "))
                                         match = false;
                                     break;
                                 case 'c':
                                     int connLoc = Collections.binarySearch(connectionWords, tokenArray[i]);
                                     if (connLoc < 0 || tokenArray[i].equals("") || tokenArray[i].equals(" "))
                                         match = false;
                                     break;
                                 case ';':
                                     String cWord;
                                     if (regExpression.indexOf("[", tPos + 1) > 0)
                                         cWord = regExpression.substring(tPos + 2, regExpression.indexOf("[", tPos + 1) - 1);
                                     else
                                         cWord = regExpression.substring(tPos + 2, regExpression.length() - 1);
 
                                     if (!cWord.equals(tokenArray[i]))
                                         match = false;
                                     break;
                             }
                         }
 
                         if (match) {
                             String result = "";// = tokenArray[0] + " " + curWnext + " " + curWnext2;
                             for (int i = 0; i < regExpressionLen - 1; i++) {
                                 result = result + tokenArray[i] + " ";
                             }
 
                             result = result + tokenArray[regExpressionLen - 1];
                             String temp = "|" + rNum + ":" + curRating;
                             result = result.concat(temp);
 
 
                             if (tokenArray[regExpressionLen - 1].equals("")) {
                                 thisComment = thisComment + "a" + ";";
                                 curCommentWords.add("a" + "|" + rNum + ":" + curRating);
 
                             } else {
                                 thisComment = thisComment + tokenArray[regExpressionLen - 1] + ";";
                                 curCommentWords.add(result);
                             }
 
                             k = k + 1;
                         }
 
 
                         for (int i = 0; i < regExpressionLen - 1; i++)
                             tokenArray[i] = tokenArray[i + 1];
                         curW = s.next();
                         curW = removeSpecialCharacters(curW.toLowerCase());
                         tokenArray[regExpressionLen - 1] = curW;
  
                         //////keyword rating code/////
                         everyWords.add(curW + "|" + rNum + ":" + curRating);
                     }
                     /////tifidf code/////
                     /* Sort comments */
                     //ArrayList<String> comment_arr = new ArrayList<String>(thisComment.length());
                     String[] splitted_comment = thisComment.split(";");
                     //Arrays.sort(splitted_comment);
                     //comment_arr.addAll(Arrays.asList(splitted_comment));
                     //Collections.sort(comment_arr);
                     
                     for (int i = 0; i < curCommentWords.size(); i++) {
                         String iWord = curCommentWords.get(i).toString().substring(0, curCommentWords.get(i).toString().indexOf("|"));
 
                         double tfVal = TfIdf.termFrequencyInComment(splitted_comment, iWord);
                         revWords.add(curCommentWords.get(i).toString() + ";" + tfVal);
                     } /////end tifidf code/////
                 }
             }
 
         } finally {
             if (s != null)
                 s.close();
         }
 
         int numWords = 0;
         /////tifidf code/////
         Collections.sort(revWords);
         int commIdx = 0, prevCommIdx = -1;
 
         double invFreq = 0.0, tfscore = 0.0;
         String prevWord = "";
        int rLen = 0;

        if(revWords.size() > 50)
            rLen = 50;
        else
            rLen = revWords.size();

        for (int i=0; i < rLen; i++) {
             String idxStr = revWords.get(i).toString(); // ease the pain
             String word = idxStr.substring(0, idxStr.indexOf("|"));
             String[] termfreq = idxStr.split(";");
             commIdx = Integer.parseInt(idxStr.substring(idxStr.indexOf("|") + 1,
                     idxStr.indexOf(":")));
 
             // We only want to count tfidf for the same word in
             // the same comment once
             if (i == 0) {
                 invFreq = TfIdf.documentFrequency(revWords, word);
                 tfscore = TfIdf.tfidf_score(Double.parseDouble(termfreq[1]), invFreq);
                 prevCommIdx = commIdx;
                 prevWord = word;
             } else {
                 // Words equal and are located in the same comment
                 // -> set same value for each comment
                 if (word.equals(prevWord) && commIdx == prevCommIdx)
                     revWords.set(i, termfreq[0] + ";" + tfscore);
                 // Word or comment index differs, count a new tf-idf
                 else {
                     invFreq = TfIdf.documentFrequency(revWords, word);
                     tfscore = TfIdf.tfidf_score(Double.parseDouble(termfreq[1]), invFreq);
                     revWords.set(i, termfreq[0] + ";" + tfscore);
                 }
             }
         } // for
         /////end tifidf code/////
         System.out.println("number of reviews:" + rNum);
 
 
         String curWord = "";
         WordInfo[] countedWords = new WordInfo[k];
         int j = -1;
         k = 0;
 
         prevWord = "";
         int prevNum = 0;
         int len;
         
         len = revWords.size();
 
         while (k != len) {
             curWord = revWords.get(k).toString();
             //store the comment number of the current keyword
             int loc = curWord.indexOf("|");
             int ratingLoc = curWord.indexOf(":");
             int idfVal = curWord.indexOf(";");
             int num = Integer.parseInt(curWord.substring(loc + 1, ratingLoc));
 
             //store the rating of the current keyword
             //float rating = Float.parseFloat(curWord.substring(ratingLoc + 1, idfVal));
 
             float tfScore = Float.parseFloat(curWord.substring(idfVal+1));
 
             curWord = curWord.substring(0, loc);
             if(!curWord.equals("")) {
                 if (!curWord.equals(prevWord)) {
                     j++;
                     countedWords[j] = new WordInfo("", 0, "", 0.0, 0);
                     countedWords[j].setTheWord(curWord);
                     countedWords[j].setCount(1);
                     countedWords[j].setRating("");
                     countedWords[j].setTFScore(tfScore);
                     prevWord = curWord;
                     prevNum = 1;
                     numWords++;
                 } else {
                     if (num > prevNum) {
                         countedWords[j].setCount(countedWords[j].getCount() + 1);
                         countedWords[j].setRating("");
                         if(countedWords[j].getTFRating() < tfScore)
                             countedWords[j].setTFScore(tfScore);
                     }
                     prevNum = num;
                 }
             }
             k++;
         }
         k = 0;
 
         result = new WordInfo[numWords];
         for (int i = 0; i < numWords; i++) {
 
             result[i] = new WordInfo("", 0, "0/0: ", 0.0, 0);
             if(countedWords[i].getTheWord().toString().equals("a"))
                 result[i].setCount(1);
             else
                 result[i].setCount(countedWords[i].getCount());
             result[i].setTheWord(countedWords[i].getTheWord());
             result[i].setTFScore(countedWords[i].getTFRating());
             int foundW = -1;
             for (int z = 0; z < productWords.size(); z++) {
                 if (productWords.get(z).toString().substring(0, productWords.get(z).toString().indexOf(":")).equals(countedWords[i].getTheWord()))
                     foundW = z;
             }
             if(foundW >= 0)
             {
                 if(productWords.get(foundW).toString().substring(productWords.get(foundW).toString().indexOf(":") + 1).equals("+"))
                     result[i].setinProductList(1);
                 else
                     result[i].setinProductList(2);
             }
 
         }
         Arrays.sort(result);
         ///////////////keyword rating code/////////////////
 
         ArrayList pWords = new ArrayList();
 
         for (int i = 0; i < productWords.size(); i++)
             pWords.add(productWords.get(i).toString().substring(0, productWords.get(i).toString().indexOf(":")));
 
 
         posScoresArray = new int[numWords];
         negScoresArray = new int[numWords];
         if (regExpressionLen == 1) {
             for (int i = 0; i < 50; i++) {
                 int posCount = 0;
                 int negCount = 0;
                 int[] posQualWordsArray;
                 posQualWordsArray = new int[posQual.size()];
                 int[] negQualWordsArray;
                 negQualWordsArray = new int[negQual.size()];
                 int[] incAugWordsArray;
                 incAugWordsArray = new int[incAug.size()];
                 int[] decAugWordsArray;
                 decAugWordsArray = new int[decAug.size()];
 
 
                 String curW = result[i].getTheWord();
 
                 int prodWordPos = Collections.binarySearch(pWords, curW);
                 int wordType = 0;
 
                 if (prodWordPos < 0)
                     wordType = 0;
                 else {
                     String typeW = productWords.get(prodWordPos).toString().substring(productWords.get(prodWordPos).toString().indexOf(":")+1);
                     if (typeW.equals("+"))
                         wordType = 1;
                     else
                         wordType = 2;
                 }
 
 
                 for (int z = 1; z < everyWords.size(); z++) {
                     //if a word from the review is equal to the current word
                     if(everyWords.get(z).toString().substring(0, everyWords.get(z).toString().indexOf("|")).equals(curW)) {
                         //get the word before the current word (the adjective)
                         String cmpW = everyWords.get(z - 1).toString();
                         //get its review number
                         int revN = Integer.parseInt(cmpW.substring(cmpW.indexOf("|")+1, cmpW.indexOf(":")));
 
                         //test if the adjective is a positive qualifier
                         int posLoc = Collections.binarySearch(posQual, cmpW.substring(0, cmpW.indexOf("|")));
                         //if it is, and its the first one in this review
                         if (posLoc >= 0 && posQualWordsArray[posLoc] < revN) {
                             //good sound
                             posCount++;
                             posQualWordsArray[posLoc] = revN;
                         }
 
                         int negLoc = Collections.binarySearch(negQual, cmpW.substring(0, cmpW.indexOf("|")));
                         if (negLoc >= 0  && negQualWordsArray[negLoc] < revN) {
                             //bad sound
                             negCount++;
                             negQualWordsArray[negLoc] = revN;
                         }
 
                         posLoc = Collections.binarySearch(incAug, cmpW.substring(0, cmpW.indexOf("|")));
                         if (posLoc >= 0 && incAugWordsArray[posLoc] < revN) {
                             //much noise
                             if (wordType == 1)
                                 posCount++;
                             else if (wordType == 2)
                                 negCount++;
                             incAugWordsArray[posLoc] = revN;
                         }
 
                         negLoc = Collections.binarySearch(decAug, cmpW.substring(0, cmpW.indexOf("|")));
                         if (negLoc >= 0  && decAugWordsArray[negLoc] < revN) {
                             //little noise
                             if (wordType == 1)
                                 negCount++;
                             else if (wordType == 2)
                                 posCount ++;
                             incAugWordsArray[negLoc] = revN;
                         }
                     }
                 }
 
                 posScoresArray[i] = posCount;
                 negScoresArray[i] = negCount;
                 if (posScoresArray[i] > 0 || negScoresArray[i] > 0) {
                     double res = ((double) posScoresArray[i] / ((double) posScoresArray[i] + (double) negScoresArray[i])) * 100;
                     DecimalFormat df = new DecimalFormat("#.##");
                     String ratingString = posScoresArray[i] + "/" + negScoresArray[i] + ": " + df.format(res);
                     result[i].setRating(ratingString);
                 } else
                     result[i].setRating(posScoresArray[i] + "/" + negScoresArray[i] + ": ");
             }
         }
         ///////////////END keyword rating code/////////////////
 
         return result;
     }
 
 
     public static String removeSpecialCharacters(String curW)
     {
         if (!curW.equals("---")) {
             int len = curW.length();
             StringBuffer result = new StringBuffer();
             char[] cArray = curW.toCharArray();
             int i = 0;
 
             while (i < len) {
                 if (cArray[i] > 96 && cArray[i] < 123)
                     result.append(cArray[i]);
                 i++;
             }
 
             String str = new String(result);
             curW = str;
         }
 
         return curW;
     }
 
 
     /**
      * @param fname Filename to read
      * @return list of the words
      *
      * Reads filename, tokenizes it to ArrayList and sorts it.
      */
     public static ArrayList readWordList(String fname) throws IOException
     {
         ArrayList wordList = new ArrayList();
         Scanner s = null;
         try {
             s = new Scanner(new BufferedReader(new FileReader(fname)));
             while (s.hasNext())
                 wordList.add(s.next());
         } finally {
             if (s != null)
                 s.close();
         }
         Collections.sort(wordList);
 
         return wordList;
     }
 
 
 }
