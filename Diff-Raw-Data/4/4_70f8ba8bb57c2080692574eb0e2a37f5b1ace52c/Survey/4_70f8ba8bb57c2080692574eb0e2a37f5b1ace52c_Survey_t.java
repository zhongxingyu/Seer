 /*
 *  Nokia Data Gathering
 *
 *  Copyright (C) 2011 Nokia Corporation
 *
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public
 *  License as published by the Free Software Foundation; either
 *  version 2.1 of the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/
 */
 
 package models;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Date;
 import java.util.List;
 import javax.persistence.CascadeType;
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.JoinColumn;
 import javax.persistence.ManyToOne;
 import javax.persistence.OneToMany;
 import javax.persistence.Table;
 import javax.persistence.Temporal;
 import javax.persistence.TemporalType;
 import org.hibernate.annotations.OnDelete;
 import org.hibernate.annotations.OnDeleteAction;
 import play.data.validation.Required;
 import play.db.jpa.Model;
 import models.constants.QuestionTypesConsts;
 
 @Entity
 @Table(name = "survey")
 public class Survey extends Model {
 
     @Required
     @Column(nullable = false, name="survey_id")
     public String surveyId;
 
     @Required
     @Column(nullable = false)
     public String title;
 
     public String lang;
 
     public Integer available;
 
     @Temporal(TemporalType.TIMESTAMP)
     @Column(name="upload_date")
     public Date uploadDate;
 
 
     @OneToMany(cascade = CascadeType.ALL, mappedBy = "survey")
     @OnDelete(action=OnDeleteAction.CASCADE)
     public List<Category> categoryCollection;
 
     @OneToMany(cascade = CascadeType.ALL, mappedBy = "survey")
     @OnDelete(action=OnDeleteAction.CASCADE)
     public Collection<TransactionLog> transactionLogCollection;
 
     @OneToMany(cascade = CascadeType.ALL, mappedBy = "survey")
     @OnDelete(action=OnDeleteAction.CASCADE)
     public Collection<NdgResult> resultCollection;
 
     @ManyToOne(optional = false)
     @JoinColumn(name = "ndg_user_id")
     public NdgUser ndgUser;
 
     public Survey() {
     }
 
     public Survey(String surveyId) {
         this.surveyId = surveyId;
     }
 
     public Survey(String surveyId, String title) {
         this.surveyId = surveyId;
         this.title = title;
     }
 
     public List<Question> getQuestions(){
         ArrayList<Question> questions = new ArrayList<Question>();
         for(Category category : categoryCollection){
             for(Question q : category.questionCollection){
                 questions.add(q);
             }
         }
         return questions;
     }
 
     @Override
     public void _delete() {
         super._delete();
         for (NdgResult current : resultCollection) {
             Collection<Answer> answers = current.answerCollection;
             Answer answer = answers.iterator().next();
 
             if ( answer.question.questionType.typeName.equalsIgnoreCase( QuestionTypesConsts.IMAGE ) ) {
                 answer.binaryData.getFile().delete();
             }
         }
     }
 
     @Override
     public String toString() {
         return "br.org.indt.ndg.server.persistence.structure.Surveys[ surveyId=" + getId() + " ]";
     }
 }
