 /*
  * Copyright 2012 Roland Gisler, GISLER iNFORMATiK, Switzerland.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package ch.gitik.ecos;
 
 /**
  * Abstrakte Basisklasse fuer Verbindung.
  * @author Roland Gisler
  */
 public abstract class AbstractConnection {
 
    public static final int ERROR_RESULT = 99999;
 
    /**
     * Synchrone Kommunikation mit der Zentrale.
     * @param command
     *           Befehl an die Zentrale.
     * @return Status bzw. Antwort.
     */
    public abstract String request(String command);
 
    /**
     * Prueft ob die Antwort den OK-Status hat.
     * @param answer
     *           Antwort die Ausgewertet wird.
     * @return true oder false.
     */
    protected static boolean isResultOk(final String answer) {
       return (getResultCode(answer) == 0);
    }
 
    /**
     * Prueft ob die Antwort vollständig ist.
     * @param answer
     *           Antwort die Ausgewertet wird.
     * @return true oder false.
     */
    protected static boolean isResultValid(final String answer) {
      return answer.contains("<REPLY") && answer.contains("<END");
    }
 
    /**
     * Liest den Antwort-Code aus einer Antwort.
     * @param answer
     *           Antwort die ausgewertet wird.
     * @return Resturncode.
     */
    protected static int getResultCode(final String answer) {
       int resultCode = ERROR_RESULT;
       if ((answer != null) && (answer.length() > 0)) {
          final int posStart = answer.indexOf("<END ") + 5;
          final int posEnd = answer.indexOf(' ', posStart);
          if ((posStart > 0) && (posEnd > posStart)) {
             final String resultCodeString = answer.substring(posStart, posEnd);
             resultCode = Integer.parseInt(resultCodeString);
          }
       }
       return resultCode;
    }
 
    /**
     * Liest ein Attribut aus einer Antwort.
     * @param answer
     *           Antwort die ausgewertet wird.
     * @param attribute
     *           Attribut das gesucht wird.
     * @return Resturncode.
     */
    protected static String getAttribute(final String answer, final String attribute) {
       final int posStart = answer.indexOf(attribute + "[") + attribute.length() + 1;
       final int posEnd = answer.indexOf(']', posStart);
       return answer.substring(posStart, posEnd);
    }
 }
