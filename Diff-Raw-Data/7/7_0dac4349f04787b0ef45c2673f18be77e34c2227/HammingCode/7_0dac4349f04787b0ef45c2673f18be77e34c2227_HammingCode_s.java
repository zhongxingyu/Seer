 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package kryptoprojekt.model;
 
 /**
  *
  * @author LiTTle, Mario
  */
 public class HammingCode extends Coder {
 
     private Matrix<PrimeFieldElement> controlMatrix;
     private Matrix<PrimeFieldElement> generatorMatrix;
     private Matrix<PrimeFieldElement> sourceCodeWord;
     private Matrix<PrimeFieldElement> encodedWord;
     private Matrix<PrimeFieldElement> syndrom;
     private Matrix<PrimeFieldElement> decodedWord;
     private Matrix<PrimeFieldElement> correctedDecodedWord;
     private int errorPosition;
     private boolean errorsFound;
     private final int galoisBase = 2;
 
     public String getDecodedWord() {
         return convertOneRowMatrixToString(decodedWord);
     }
 
     public int getErrorPos(){
         return errorPosition;
     }
 
     public String getEncodedWord() {
         return convertOneRowMatrixToString(encodedWord);
     }
 
     public Matrix<PrimeFieldElement> getGeneratorMatrix() {
         return generatorMatrix;
     }
 
     public String getSourceCodeWord() {
         return convertOneRowMatrixToString(sourceCodeWord);
     }
 
     public String getSyndrom() {
         return convertOneRowMatrixToString(syndrom);
     }
 
     public String getCorrectedDecodedWord() {
         return convertOneRowMatrixToString(correctedDecodedWord);
     }
 
     public boolean isErrorsFound() {
         return errorsFound;
     }
 
     public Matrix<PrimeFieldElement> getControlMatrix() {
         return this.controlMatrix;
     }
 
     /**
      * Encodes the sourceCodeWord which was set within constructor.
      * @throws NullPointerException if sourceCodeWord or generatorMatrix is not calculated yet
      * @return encoded String
      */
     @Override
     public String encode() throws NullPointerException {
         if (sourceCodeWord == null || generatorMatrix == null) {
             throw new NullPointerException("encodeException");
         }
         this.encodedWord = this.sourceCodeWord.multiply(this.generatorMatrix);
         return convertOneRowMatrixToString(this.encodedWord);
     }
 
     /**
      * Calculates the syndrom.
      */
     @Override
     public String calculateSyndrom(){
         if (encodedWord == null) {
             encode();
         }
         if (controlMatrix == null) {
             generateControlMatrix();
         }
         this.syndrom = this.encodedWord.multiply(this.controlMatrix);
         return convertOneRowMatrixToString(this.syndrom);
     }
 
     /**
      * Detects error(s) in the encodedWord. Uses the syndrom-attribute which has to be calculated before.
      * @throws NullPointerException if syndrom is not calculated yet
      * @throws RuntimeException if too many errors were found (-->unable to correct)
      */
     public void detectError() throws RuntimeException {
         PrimeFieldElement comparison = new PrimeFieldElement(0, this.galoisBase);
 
         if (this.syndrom == null) {
             throw new NullPointerException("detectErrorNullException");
         }
 
         //is there any error in the syndrom
         for (int i = 0; i < this.syndrom.getMatrixColumnCapacity(); i++) {
             if (comparison.compareTo(this.syndrom.get(0, i)) != 0) {
                 this.errorsFound = true;
             }
         }
 
         if (errorsFound) {
             int errorPos = getErrorPosition();
             this.errorPosition = errorPos;
             if (errorPos > -1) {
                 correctError(errorPos);
                 this.correctedDecodedWord = convertStringToOneRowMatrix(decode());
             } else {
                 throw new RuntimeException("detectErrorTooManyErrors");
             }
         }
     }
 
     /**
      * Corrects the error in encodedWord.
      * @param pos position of error in encodedWord
      * @throws NullPointerException if encodedWord is not calculated yet
      * @return corrected encodedWord
      */
     private String correctError(int pos) throws NullPointerException {
         if (encodedWord == null) {
             throw new NullPointerException("correctErrorException");
         }
         PrimeFieldElement comparison = new PrimeFieldElement(0, this.galoisBase);
 
         if (comparison.compareTo(this.encodedWord.get(0, pos)) == 0) {
             this.encodedWord.set(0, pos, new PrimeFieldElement(1, this.galoisBase));
         } else {
             this.encodedWord.set(0, pos, comparison);
         }
         return convertOneRowMatrixToString(encodedWord);
     }
 
     /**
      * Calculates errorPosition in encodedWord on base of syndrom and controlMatrix
      * @throws NullPointerException if syndrom or controlMatrix are not calculated yet
      * @return errorPosition or -1 if too many errors found
      */
     private int getErrorPosition() throws NullPointerException {
         if (controlMatrix == null || syndrom == null) {
             throw new NullPointerException("getErrorPositionException");
         }
         boolean check = true;
         boolean test = false;
         int position = 0;
 
         for (int y = 0; y < this.controlMatrix.getMatrixRowCapacity(); y++) {
             check = false;
             for (int i = 0; i < this.syndrom.getMatrixColumnCapacity(); i++) {
                 if (this.syndrom.get(0, i).compareTo(this.controlMatrix.get(y, i)) != 0) {
                     check = true;
                 }
             }
             if (check == false) {
                 test = true;
                 position = y;
             }
         }
 
         if (test == true) {
             return position;
         } else {
             return -1;
         }
     }
 
     /**
      * Decodes the encodedWord.
      * @throws NullPointerException if encodedWord is not calculated yet
      * @return decoded Word
      */
     @Override
     public String decode() {
         if (encodedWord == null) {
             throw new NullPointerException("decodeException");
         }
         KryptoType<PrimeFieldElement>[][] t = new KryptoType[1][this.codeWord.length()];
         for (int i = 0; i < this.codeWord.length(); i++) {
             t[0][i] = encodedWord.get(0, i);
         }
         this.decodedWord = new Matrix(t);
         return convertOneRowMatrixToString(this.decodedWord);
     }
 
     /**
      * Generates the generatorMatrix on base of given codeWordLength.
      * @return
      */
     public Matrix<PrimeFieldElement> generateGeneratorMatrix() {
         int codewordLength = codeWord.length();
         int columnCapacity = (int) Math.pow(2, codewordLength) - 1;
         KryptoType<PrimeFieldElement>[][] t = new KryptoType[codewordLength][columnCapacity];
 
         //initialise array with PrimeFieldElement-objects, value=0
         for (int i = 0; i < codewordLength; i++) {
             for (int j = 0; j < columnCapacity; j++) {
                 t[i][j] = new PrimeFieldElement(0, this.galoisBase);
             }
         }
 
         //generate identity matrix
         for (int i = 0; i < codewordLength; i++) {
             t[i][i] = new PrimeFieldElement(1, this.galoisBase);
         }
 
         //set parity bits
         for (int i = 0; i < codewordLength; i++) {
             for (int j = codewordLength; j < (int) Math.pow(2, codewordLength) - 1; j++) {
                if (i != j - codewordLength) {
                     t[i][j] = new PrimeFieldElement(1, this.galoisBase);
                 }
             }
         }
         this.generatorMatrix = new Matrix(t);
         return this.generatorMatrix;
     }
 
     /**
      * Generates the controlMatrix on base of given generatorMatrix.
      */
     public Matrix<PrimeFieldElement> generateControlMatrix() {
         if (this.generatorMatrix == null) {
             generateGeneratorMatrix();
         }
         int y = this.generatorMatrix.getMatrixColumnCapacity() - this.generatorMatrix.getMatrixRowCapacity();
         KryptoType<PrimeFieldElement> k[][] = new KryptoType[this.generatorMatrix.getMatrixColumnCapacity()][y];
         int n = codeWord.length();
 
         //initialize array with PrimeFieldElement-objects, value=0
         for (int i = 0; i < this.generatorMatrix.getMatrixColumnCapacity(); i++) {
             for (int j = 0; j < y; j++) {
                 k[i][j] = new PrimeFieldElement(0, this.galoisBase);
             }
         }
 
         //sets parity bits in control matrix
         for (int i = 0; i < this.codeWord.length(); i++) {
             for (int j = 0; j+n < this.generatorMatrix.getMatrixColumnCapacity(); j++) {
                 k[i][j] = this.generatorMatrix.get(i, j + n );
             }
         }
 
         //generates identity matrix
         for (int i = 0; i < y; i++) {
             k[i + codeWord.length()][i] = new PrimeFieldElement(1, this.galoisBase);
         }
         this.controlMatrix = new Matrix(k);
         return this.controlMatrix;
     }
 
     /**
      * Constructs a new object of HammingCode on base of given generatorMatrix with  galoisBase = 2 and the given codeWord.
      * Length of codeWord have to be the same as columnCapacity of given generatorMatrix.
      * @param generatorMatrix matrix, which consists of identity-matrix and parity-matrix
      * @param codeWord word, which has to be encoded
      * @throws IllegalArgumentException if length of given codeWord != columnCapacity of given generatorMatrix
      */
     public HammingCode(Matrix<PrimeFieldElement> generatorMatrix, String codeWord) throws IllegalArgumentException {
         this.codeWord = codeWord;
         this.sourceCodeWord = convertStringToOneRowMatrix(codeWord);
         if (Math.pow(2, codeWord.length()) - 2 == generatorMatrix.getMatrixColumnCapacity()) {
             this.generatorMatrix = generatorMatrix;
             generateControlMatrix();
         } else {
             if (Math.pow(2, codeWord.length()) - 1 == generatorMatrix.getMatrixColumnCapacity()) {
                 this.generatorMatrix = generatorMatrix;
                 generateControlMatrix();
             } else {
                 throw new IllegalArgumentException("noHammingMatrix");
             }
         }
     }
 
     /**
      * Constructs a new object of HammingCode on base of given generatorMatrix with galoisBase = 2 and the given codeWord.
      * @param codeWord word, which has to be encoded
      */
     public HammingCode(String codeWord) {
         this.codeWord = codeWord;
         this.sourceCodeWord = convertStringToOneRowMatrix(codeWord);
         generateGeneratorMatrix();
         generateControlMatrix();
     }
 
     /**
      * Converts a String into a matrix with one row and column quantity of given sourceCodeWordLength.
      * @param sourceCodeWord word which has to be converted into matrix
      * @return given String converted in Matrix<PrimeFieldElement>
      */
     private Matrix<PrimeFieldElement> convertStringToOneRowMatrix(String sourceCodeWord) {
         KryptoType<PrimeFieldElement> cWord[][] = new KryptoType[1][sourceCodeWord.length()];
         char[] t = sourceCodeWord.toCharArray();
         for (int i = 0; i < sourceCodeWord.length(); i++) {
             for (int j = 0; j < this.galoisBase; j++) //added 48 to j because of ascii-value of '0'
             {
                 if (t[i] == j + 48) {
                     cWord[0][i] = new PrimeFieldElement(j, this.galoisBase);
                     break;
                 }
             }
         }
         return new Matrix(cWord);
     }
 
     /**
      * Converts a one row matrix into a String.
      * @param sourceCodeWord matrix which has to be converted into a String
      * @return Matrix<PrimeFieldElement> converted into a String
      */
     private String convertOneRowMatrixToString(Matrix<PrimeFieldElement> sourceCodeWord) {
         String s = "";
         for (int i = 0; i < sourceCodeWord.getMatrixColumnCapacity(); i++) {
             char t = (char) (sourceCodeWord.get(0, i).getPrimeElemValue().intValue() + 48);
             s += t;
         }
         return s;
     }
 
     /**
      * Calculates the hamming distance between the one-row-vectors a and b. a and b must have the same length.
      * @param a first vector
      * @param b second vector
      * @return hamming distance
      * @throws IllegalArgumentException if a or b have more than one row
      * @throws IllegalArgumentException if a has different columnCapacity as b
      */
     public static Z hammingDistance(Matrix<PrimeFieldElement> a, Matrix<PrimeFieldElement> b) throws IllegalArgumentException {
         if (a.getMatrixRowCapacity() > 1 || b.getMatrixRowCapacity() > 1) {
             throw new IllegalArgumentException("hammingDistanceRowException");
         }
         if (a.getMatrixColumnCapacity() != b.getMatrixColumnCapacity()) {
             throw new IllegalArgumentException("hammingDistanceColumnException");
         }
         int distance = 0;
         for (int i = 0; i < a.getMatrixColumnCapacity(); i++) {
             if (a.get(0, i).compareTo(b.get(0, i)) != 0) {
                 distance++;
             }
         }
         return new Z(distance);
     }
 
     /**
      * Calculates the vector weight between the one-row-vectors a and the null vector.
      * @param a vector which should be calculated
      * @return calculated vector weight
      */
     public static Z vectorWeight(Matrix<PrimeFieldElement> a) {
         KryptoType<PrimeFieldElement> k[][] = new KryptoType[1][a.getMatrixColumnCapacity()];
         for (int i = 0; i < a.getMatrixColumnCapacity(); i++) {
             k[0][i] = new PrimeFieldElement(0, 2);
         }
         Matrix<PrimeFieldElement> b = new Matrix(k);
         return hammingDistance(a, b);
     }
 
     /**
      * Calculates a bitError based on the given probability
      * @param probability value between 0 and 1
      * @return new PrimeFieldElement with value 0 or 1
      */
     private PrimeFieldElement bitErrorProbability(double probability) {
         int rand = (int) Math.floor((Math.random() * 999) + 1);
         if (rand > probability * 1000) {
             return new PrimeFieldElement(0, this.galoisBase);
         } else {
             return new PrimeFieldElement(1, this.galoisBase);
         }
     }
 
     /**
      * Generates a bitError with given probability in encodedWord.
      * @param probability value between 0 and 1
      */
     public void generateBitError(double probability) {
         int length = encodedWord.getMatrixColumnCapacity();
         for (int i = 0; i < length; i++) {
             PrimeFieldElement tmp = encodedWord.get(0, i);
             encodedWord.set(0, i, tmp.add(bitErrorProbability(probability)));
         }
     }
 
     @Override
     public String toString() {
         return "";
     }
 
     /**
      * Creates a copy of a HammingCode object.
      * @return a copy of {@code this} object
      */
     public HammingCode copy() {
         HammingCode newHc = new HammingCode(this.generatorMatrix,this.codeWord);
 
         newHc.sourceCodeWord = this.sourceCodeWord.copy();
         newHc.encodedWord = this.encodedWord == null ? null : this.encodedWord.copy();
         newHc.syndrom = this.syndrom == null ? null : this.syndrom.copy();
         newHc.decodedWord = this.decodedWord == null ? null : this.decodedWord.copy();
         newHc.correctedDecodedWord = this.correctedDecodedWord == null ? null : this.correctedDecodedWord.copy();
         newHc.errorPosition = this.errorPosition;
         newHc.errorsFound = this.errorsFound;
 
         return newHc;
     }
 }
