 package universite.toulouse.moodlexmlapi.tartopom.impl.model;
 
 import javax.xml.bind.annotation.XmlAttribute;
 import javax.xml.bind.annotation.XmlElement;
 
 /**
  * Conteneur Xml de type Answer
  * @author vermu0041
  *
  */
 public class Answer {
 
     private String fraction;
     private String text;
     private String tolerance;
     private String toleranceType;
     private String correctAnswerFormat;
     private String correctAnswerLength;
     private EnclosedText feedback;
 
     /**
      * @return fraction
      */
     @XmlAttribute(name="fraction")
     public String getFraction() {
         return fraction;
     }
 
     /**
      * @return text
      */
     @XmlElement(name="text")
     public String getText() {
         return text;
     }
 
     /**
      * @return tolerance
      */
     @XmlElement(name="tolerance")
     public String getTolerance() {
         return tolerance;
     }
 
     /**
      * @return toleranceType
      */
     @XmlElement(name="tolerancetype")
     public String getToleranceType() {
         return toleranceType;
     }
 
     /**
      * @return correctAnswerFormat
      */
     @XmlElement(name="correctanswerformat")
     public String getCorrectAnswerFormat() {
         return correctAnswerFormat;
     }
 
     /**
      * @return correctAnswerLength
      */
     @XmlElement(name="correctanswerlength")
     public String getCorrectAnswerLength() {
         return correctAnswerLength;
     }
 
     /**
      * @return feedback
      */
     @XmlElement(name="feedback")
     public EnclosedText getFeedback() {
         return feedback;
     }
 
     /**
      * @param fraction String
      */
     public void setFraction(String fraction) {
         this.fraction = fraction;
     }
 
     /**
      * @param text String
      */
     public void setText(String text) {
         this.text = text;
     }
 
     /**
      * @param tolerance String
      */
     public void setTolerance(String tolerance) {
         this.tolerance = tolerance;
     }
 
     /**
      * @param toleranceType String
      */
     public void setToleranceType(String toleranceType) {
         this.toleranceType = toleranceType;
     }
 
     /**
      * @param correctAnswerFormat String
      */
     public void setCorrectAnswerFormat(String correctAnswerFormat) {
         this.correctAnswerFormat = correctAnswerFormat;
     }
 
     /**
      * @param correctAnswerLength String
      */
     public void setCorrectAnswerLength(String correctAnswerLength) {
         this.correctAnswerLength = correctAnswerLength;
     }
 
     /**
      * @param feedback EnclosedText
      */
     public void setFeedback(EnclosedText feedback) {
         this.feedback = feedback;
     }
 }
