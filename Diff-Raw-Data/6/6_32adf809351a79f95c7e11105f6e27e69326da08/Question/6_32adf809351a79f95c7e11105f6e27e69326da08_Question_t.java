 package com.higheranimals.measurewords;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 import android.os.Parcel;
 import android.os.Parcelable;
 
 final public class Question implements Parcelable {
     private final int id;
     private final String nounHanzi;
     private final String nounPinyin;
     private final String nounEnglish;
     private final int correctIndex;
     private final List<Answer> answers;
 
     public Question(int id, String nounHanzi, String nounPinyin,
             String nounEnglish, Answer correct, List<Answer> incorrect) {
         this.id = id;
         this.nounHanzi = nounHanzi;
         this.nounPinyin = nounPinyin;
         this.nounEnglish = nounEnglish;
         List<Answer> tmpAnswers = new ArrayList<Answer>(incorrect);
         tmpAnswers.add(correct);
         Collections.shuffle(tmpAnswers);
         answers = Collections.unmodifiableList(tmpAnswers);
         correctIndex = answers.indexOf(correct);
     }
 
     public List<Answer> getAnswers() {
         return Collections.unmodifiableList(answers);
     }
 
     public boolean isCorrectChoice(int i) {
         return i == correctIndex;
     }
 
     public int getCorrectIndex() {
         return correctIndex;
     }
 
     public String getNounHanzi() {
         return nounHanzi;
     }
 
     public String getNounPinyin() {
         return nounPinyin;
     }
 
     public String getNounEnglish() {
         return nounEnglish;
     }
 
     public static final Parcelable.Creator<Question> CREATOR = new Parcelable.Creator<Question>() {
 
         @Override
         public Question createFromParcel(Parcel source) {
             return new Question(source);
         }
 
         @Override
         public Question[] newArray(int size) {
             return new Question[size];
         }
 
     };
 
     private Question(Parcel source) {
         id = source.readInt();
         nounHanzi = source.readString();
         nounPinyin = source.readString();
         nounEnglish = source.readString();
         correctIndex = source.readInt();
         ClassLoader answerClassLoader = Answer.class.getClassLoader();
         Parcelable[] parcels = source.readParcelableArray(answerClassLoader);
         List<Answer> tmpList = new ArrayList<Answer>();
         for (int i = 0; i < parcels.length; i++) {
             tmpList.add((Answer) parcels[i]);
         }
         answers = Collections.unmodifiableList(tmpList);
     }
 
     @Override
     public int describeContents() {
         return 0;
     }
 
     @Override
     public void writeToParcel(Parcel dest, int flags) {
         dest.writeInt(id);
         dest.writeString(nounHanzi);
         dest.writeString(nounPinyin);
         dest.writeString(nounEnglish);
         dest.writeInt(correctIndex);
        Parcelable[] parcelables = new Parcelable[answers.size()];
        for (int i = 0; i < answers.size(); ++i) {
            parcelables[i] = answers.get(i);
        }
        dest.writeParcelableArray(parcelables, 0);
     }
 
     public Answer getCorrectAnswer() {
         return answers.get(correctIndex);
     }
 
     public boolean isCorrectAnswer(Answer a) {
         return getCorrectAnswer().equals(a);
     }
 }
