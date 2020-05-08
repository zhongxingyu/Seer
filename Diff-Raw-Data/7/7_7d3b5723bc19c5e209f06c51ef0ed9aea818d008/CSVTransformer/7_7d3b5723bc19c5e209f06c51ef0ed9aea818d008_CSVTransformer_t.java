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
 
 package controllers.transformer;
 
 import java.io.UnsupportedEncodingException;
 import java.text.SimpleDateFormat;
 import java.util.Collection;
 import java.util.Date;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import models.Answer;
 import models.NdgResult;
 import models.Question;
 import models.Survey;
 import models.constants.QuestionTypesConsts;
 
 public class CSVTransformer extends ResultsTransformer {
 
     private static final String sep = "|";
 
     public CSVTransformer( Survey survey, Boolean exportWithImages ) {
         super( survey, exportWithImages );
     }
 
     public CSVTransformer( Survey survey, Collection<NdgResult> results, Boolean exportWithImages ) {
         super( survey, results, exportWithImages );
     }
 
     @Override
     public byte[] getBytes() {
         StringBuilder buffer = new StringBuilder();
         if ( results == null ) {
             results = survey.resultCollection;
         }
 
         /** Header **/
         buffer.append( "ResultId" ).append( sep ).append( "SurveyId" ).append( sep )
               .append( "Title" ).append( sep ).append( "Start time" ).append( sep )
               .append( "End time" ).append( sep ).append( "Date Sent" ).append( sep )
               .append( "User" ).append( sep ).append( "Lat" ).append( sep )
              .append( "Lon" ).append( sep ).append( "Phone Number" ).append( sep );
 
         /** Header Fields**/
         for ( Question question :survey.getQuestions() ) {
             buffer.append( question.label );
             buffer.append( sep );
         }
         buffer.append( "\n" );
 
         SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss Z");
 
         /** Content **/
         for ( NdgResult result :results ) {
             buffer.append( result.resultId ).append( sep ).append( result.survey.surveyId ).append( sep )
                   .append( result.title ).append( sep ).append( dateFormat.format(result.startTime) ).append( sep )
                   .append( dateFormat.format(result.endTime) ).append( sep );
 
             if(result.dateSent != null) {
                 buffer.append( dateFormat.format(result.dateSent) ).append( sep );
             }
             else {
                 buffer.append( "" ).append( sep );
             }
            buffer.append( result.ndgUser.username ).append( sep ).append( result.latitude ).append( sep ).append( result.longitude ).append( sep ).append( result.ndgUser.phoneNumber ).append( sep );
 
             for ( Question question :survey.getQuestions() ) {//to ensure right answer order
                 question.answerCollection.retainAll( result.answerCollection );//only one should left, hope that it does not modify results
                 if ( question.answerCollection.isEmpty() ) {
                     buffer.append( "" );
                     buffer.append( sep );
                 } else if ( question.answerCollection.size() == 1 ) {
                     Answer answer = question.answerCollection.iterator().next();
                     if ( answer.question.questionType.typeName.equalsIgnoreCase( QuestionTypesConsts.IMAGE ) ) {//TODO handle other binary data
                         buffer.append( storeImagesAndGetValueToExport( survey.surveyId, result.resultId, answer.id, answer.binaryData ) );
                         buffer.append( sep );
                     } else {
                         String value = answer.textData;
                         value = value.trim().replaceAll( "\n", "" );
                         buffer.append( value );
                         buffer.append( sep );
                     }
                 } else {
                     Logger.getAnonymousLogger().log( Level.WARNING, "to many answers. ResID={0}questioId={1}answerCount={2}", new Object[]{ result.resultId, question.id, question.answerCollection.size() } );
                 }
             }
             buffer.append( "\n" );
         }
         try {
             return buffer.toString().getBytes( "UTF-8" );
         } catch ( UnsupportedEncodingException ex ) {
             Logger.getLogger( CSVTransformer.class.getName() ).log( Level.SEVERE, null, ex );
         }
         return "".getBytes();
     }
 }
