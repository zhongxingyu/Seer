 /* 
  * surveyforge-core - Copyright (C) 2006 OPEN input - http://www.openinput.com/
  *
  * This program is free software; you can redistribute it and/or modify it 
  * under the terms of the GNU General Public License as published by the 
  * Free Software Foundation; either version 2 of the License, or (at your 
  * option) any later version.
  *
  * This program is distributed in the hope that it will be useful, but WITHOUT 
  * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or 
  * FITNESS FOR A PARTICULAR PURPOSE. 
  * See the GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License along 
  * with this program; if not, write to 
  *   the Free Software Foundation, Inc., 
  *   59 Temple Place, Suite 330, 
  *   Boston, MA 02111-1307 USA
  *   
  * $Id$
  */
 package org.surveyforge.core.survey;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.List;
 
 import javax.persistence.CascadeType;
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.FetchType;
 import javax.persistence.GeneratedValue;
 import javax.persistence.Id;
 import javax.persistence.JoinColumn;
 import javax.persistence.ManyToOne;
 import javax.persistence.OneToMany;
 import javax.persistence.OneToOne;
 import javax.persistence.Table;
 import javax.persistence.UniqueConstraint;
 
 import org.hibernate.annotations.GenericGenerator;
 import org.hibernate.annotations.IndexColumn;
 import org.surveyforge.core.metadata.Register;
 import org.surveyforge.util.InternationalizedString;
 
 /**
  * A questionnaire is a list of {@link Question}s used to recollect the data into a register. The questionnaire is included in almost
  * one {@link Study}.
  * 
  * @author jsegura
  */
 @Entity
 @Table(uniqueConstraints = {@UniqueConstraint(columnNames = {"identifier", "study_id"})})
 public class Questionnaire implements Serializable
   {
   private static final long          serialVersionUID = 6844066269698434310L;
 
   @SuppressWarnings("unused")
   @Id
   @Column(length = 50)
   @GeneratedValue(generator = "system-uuid")
   @GenericGenerator(name = "system-uuid", strategy = "uuid")
   private String                     id;
   /** Version for optimistic locking. */
   @SuppressWarnings("unused")
   @javax.persistence.Version
   private int                        lockingVersion;
 
   /**
    * A questionnaire is identified by an identifier, which is unique in the context of a statistical activity. It may typically be an
    * abbreviation of its title or a systematic number.
    */
   @Column(nullable = false, length = 50)
   private String                     identifier;
   /** A questionnaire has a title as provided by the owner or maintenance unit. */
   @Column(length = 250)
   private String                     title            = "";
   /**
    * Detailed description of the questionnaire. The questionnaire description typically describes the underlying concept of the
    * questionnaire and basic principles.
    */
   @Column(length = 500)
   private String                     description      = "";
   /** A questionnaire consists of a number of questionnaire elements. Each element refers to a question. */
   @OneToMany(fetch = FetchType.LAZY, cascade = {CascadeType.ALL})
   @IndexColumn(name = "questionnairesIndex")
   @JoinColumn(name = "questionnaire_id")
   private List<QuestionnaireElement> elements         = new ArrayList<QuestionnaireElement>( );
   /** A questionnaire may have its content organized in pages. */
   @OneToMany(fetch = FetchType.LAZY, cascade = {CascadeType.ALL})
   @IndexColumn(name = "pageIndex")
   // @JoinColumn(name = "questionnaire_page_id")
   @JoinColumn(name = "questionnaire_page_id")
   private List<Feed>                 pageFeeds        = new ArrayList<Feed>( );
   /** A questionnaire may have its content organized in sections. */
   @OneToMany(fetch = FetchType.LAZY, cascade = {CascadeType.ALL})
   @IndexColumn(name = "sectionIndex")
   @JoinColumn(name = "questionnaire_section_id")
   private List<SectionFeed>          sectionFeeds     = new ArrayList<SectionFeed>( );
 
   /** A questionnaire corresponds logically to a register, which describes the content of the data collection. */
   @OneToOne(fetch = FetchType.LAZY)
   private Register                   register;
 
   /** A questionnaire is included in a Study. */
   @ManyToOne
   @JoinColumn(name = "study_id", insertable = false, updatable = false)
   private Study                      study;
 
 
   protected Questionnaire( )
     {}
 
   /**
    * Creates a new Questionnaire included in a Study, corresponding to a Register and identified bu identifier.
    * 
    * @param study The {@link Study} this questionnaire belongs to.
    * @param register The {@link Register} this questionnarie belongs to.
    * @param identifier The identifier to set.
    * @throws NullPointerException If the study is <code>null</code> or if the register is <code>null</code> or if the identifier is
    *           <code>null</code> or is empty.
    */
  public Questionnaire( String identifier, Study study, Register register )
     {
    this.setIdentifier( identifier );
     this.setStudy( study );
     this.setRegister( register );
     }
 
   /**
    * Returns the identifier of the Questionnaire.
    * 
    * @return Returns the identifier.
    */
   public String getIdentifier( )
     {
     return this.identifier;
     }
 
   /**
    * Sets the identifier of the Questionnaire.
    * 
    * @param identifier The identifier to set.
    * @throws NullPointerException If the register is <code>null</code> or if the identifier is <code>null</code> or is empty.
    */
   public void setIdentifier( String identifier )
     {
     if( identifier != null && !identifier.equals( "" ) )
       this.identifier = identifier;
     else
       throw new NullPointerException( );
     }
 
   /**
    * Returns the title of the questionnaire.
    * 
    * @return Returns the title.
    */
   public String getTitle( )
     {
     return this.title;
     }
 
   /**
    * Sets the title of the Questionnaire.
    * 
    * @param title The title to set.
    * @throws NullPointerException If the title is <code>null</code>.
    */
   public void setTitle( String title )
     {
     if( title != null )
       this.title = title;
     else
       throw new NullPointerException( );
     }
 
   /**
    * Returns the description of the questionnaire.
    * 
    * @return Returns the description.
    */
   public String getDescription( )
     {
     return this.description;
     }
 
   /**
    * Sets the description of the Questionnaire.
    * 
    * @param description The description to set.
    * @throws NullPointerException If the description is <code>null</code>
    */
   public void setDescription( String description )
     {
     if( description != null )
       this.description = description;
     else
       throw new NullPointerException( );
     }
 
   /**
    * Return the list of {@link QuestionnaireElement}s included in this questionnaire.
    * 
    * @return Returns the elements.
    */
   public List<QuestionnaireElement> getElements( )
     {
     return Collections.unmodifiableList( this.elements );
     }
 
 
   /**
    * Adds a new {@link Element} to he Questionnaire.
    * 
    * @param element The element to add.
    * @throws NullPointerException If the element is <code>null</code>
    */
   public void addElement( QuestionnaireElement element )
     {
     if( element != null )
       {
       this.elements.add( element );
       if( this.pageFeeds.isEmpty( ) ) this.createPageFeed( element );
       if( this.sectionFeeds.isEmpty( ) ) this.createSectionFeed( element );
       }
     else
       throw new NullPointerException( );
     }
 
   /**
    * Removes a new {@link Element} to the questionnaire.
    * 
    * @param element The element to remove.
    * @throws NullPointerException If the element is <code>null</code>
    */
   public void delElement( QuestionnaireElement element )
     {
     if( element != null )
       {
       int indexToRemove = this.elements.indexOf( element );
       if( indexToRemove != -1 )
         {
         Comparator<Feed> feedComparator = new Questionnaire.FeedComparator( );
 
         Feed pageFeedToRemove = new Feed( element );
         Feed[] pageFeeds = this.getPageFeeds( ).toArray( new Feed[] {} );
         int pageFeedPosition = Arrays.binarySearch( pageFeeds, pageFeedToRemove, feedComparator );
         if( pageFeedPosition >= 0 )
           {
           this.pageFeeds.remove( pageFeedPosition );
           if( indexToRemove < this.elements.size( ) - 1 ) this.createPageFeed( this.elements.get( indexToRemove + 1 ) );
           }
 
         SectionFeed sectionFeedToRemove = new SectionFeed( element );
         SectionFeed[] sectionFeeds = this.getSectionFeeds( ).toArray( new SectionFeed[] {} );
         int sectionFeedPosition = Arrays.binarySearch( sectionFeeds, sectionFeedToRemove, feedComparator );
         if( sectionFeedPosition >= 0 )
           {
           SectionFeed oldSectionFeed = this.sectionFeeds.remove( sectionFeedPosition );
           if( indexToRemove < this.elements.size( ) - 1 )
             this.createSectionFeed( this.elements.get( indexToRemove + 1 ), oldSectionFeed.getInternationalizedTitle( ) );
           }
 
         this.elements.remove( element );
         }
       }
     else
       throw new NullPointerException( );
     }
 
   public Feed createPageFeed( QuestionnaireElement firstElement )
     {
     if( firstElement != null )
       {
       Comparator<Feed> feedComparator = new Questionnaire.FeedComparator( );
       Feed[] pageFeeds = this.getPageFeeds( ).toArray( new Feed[] {} );
 
       Feed pageFeed = new Feed( firstElement );
       int insertionPoint = Arrays.binarySearch( pageFeeds, pageFeed, feedComparator );
       if( insertionPoint < 0 ) // there was no page feed at the specified element
         {
         this.pageFeeds.add( -insertionPoint - 1, pageFeed );
         return pageFeed;
         }
       else
         return this.pageFeeds.get( insertionPoint );
       }
     else
       throw new NullPointerException( );
     }
 
   public void removePageFeed( Feed pageFeed )
     {
     this.pageFeeds.remove( pageFeed );
     }
 
   public List<Feed> getPageFeeds( )
     {
     return Collections.unmodifiableList( this.pageFeeds );
     }
 
   public SectionFeed createSectionFeed( QuestionnaireElement firstElement )
     {
     return this.createSectionFeed( firstElement, null );
     }
 
   public SectionFeed createSectionFeed( QuestionnaireElement firstElement, InternationalizedString title )
     {
     if( firstElement != null )
       {
       Comparator<Feed> feedComparator = new Questionnaire.FeedComparator( );
       Feed[] sectionFeeds = this.getSectionFeeds( ).toArray( new SectionFeed[] {} );
 
       SectionFeed sectionFeed = new SectionFeed( firstElement, title );
       int insertionPoint = Arrays.binarySearch( sectionFeeds, sectionFeed, feedComparator );
       if( insertionPoint < 0 ) // there was no section feed at the specified element
         {
         this.sectionFeeds.add( -insertionPoint - 1, sectionFeed );
         return sectionFeed;
         }
       else
         return this.sectionFeeds.get( insertionPoint );
       }
     else
       throw new NullPointerException( );
     }
 
   public void removeSectionFeed( SectionFeed sectionFeed )
     {
     this.sectionFeeds.remove( sectionFeed );
     }
 
   public List<SectionFeed> getSectionFeeds( )
     {
     return Collections.unmodifiableList( this.sectionFeeds );
     }
 
   public List<SectionFeed> getSectionsInPage( Feed pageFeed )
     {
     int pageNumber = this.pageFeeds.indexOf( pageFeed );
     if( pageNumber != -1 )
       {
       QuestionnaireElement firstElement = pageFeed.getFirstElement( );
       QuestionnaireElement lastElement = null;
       if( pageNumber != this.pageFeeds.size( ) - 1 ) // We aren't in the last page
         {
         lastElement = this.pageFeeds.get( pageNumber + 1 ).getFirstElement( );
         }
 
       SectionFeed[] sectionFeeds = this.getSectionFeeds( ).toArray( new SectionFeed[] {} );
       Comparator<Feed> sectionFeedComparator = new Questionnaire.FeedComparator( );
       int firstSectionIndex = Arrays.binarySearch( sectionFeeds, new SectionFeed( firstElement ), sectionFeedComparator );
       if( firstSectionIndex < 0 ) firstSectionIndex = -firstSectionIndex - 2;
       int lastSectionIndex = this.getSectionFeeds( ).size( );
       if( lastElement != null )
         {
         lastSectionIndex = Arrays.binarySearch( sectionFeeds, new SectionFeed( lastElement ), sectionFeedComparator );
         if( lastSectionIndex < 0 ) lastSectionIndex = -lastSectionIndex - 1;
         }
 
       return Collections.unmodifiableList( this.sectionFeeds.subList( firstSectionIndex, lastSectionIndex ) );
       }
     else
       throw new IllegalArgumentException( );
     }
 
   public List<QuestionnaireElement> getElementsInPageAndSection( Feed pageFeed, SectionFeed sectionFeed )
     {
     if( this.getSectionsInPage( pageFeed ).contains( sectionFeed ) )
       {
       int pageNumber = this.pageFeeds.indexOf( pageFeed );
       int sectionNumber = this.sectionFeeds.indexOf( sectionFeed );
 
       int firstElementIndex = Math.max( this.elements.indexOf( pageFeed.getFirstElement( ) ), this.elements.indexOf( sectionFeed
           .getFirstElement( ) ) );
 
       int lastElementInPageIndex = (pageNumber != this.pageFeeds.size( ) - 1) ? this.elements.indexOf( this.pageFeeds.get(
           pageNumber + 1 ).getFirstElement( ) ) : Integer.MAX_VALUE;
       int lastElementInSectionIndex = (sectionNumber != this.sectionFeeds.size( ) - 1) ? this.elements.indexOf( this.sectionFeeds.get(
           sectionNumber + 1 ).getFirstElement( ) ) : Integer.MAX_VALUE;
       int lastElementIndex = Math.min( Math.min( lastElementInPageIndex, lastElementInSectionIndex ), this.elements.size( ) );
 
       return Collections.unmodifiableList( this.elements.subList( firstElementIndex, lastElementIndex ) );
       }
     else
       throw new IllegalArgumentException( );
     }
 
   /**
    * Returns the {@link Register} of the questionnaire.
    * 
    * @return Returns the register.
    */
   public Register getRegister( )
     {
     return this.register;
     }
 
   /**
    * Sets the register of the Questionnaire.
    * 
    * @param register The register to set.
    * @throws NullPointerException If the register is <code>null</code>
    */
   public void setRegister( Register register )
     {
     if( register != null )
       this.register = register;
     else
       throw new NullPointerException( );
     }
 
   /**
    * Return the study his questionnaire belongs to.
    * 
    * @return Returns the study.
    */
   public Study getStudy( )
     {
     return this.study;
     }
 
   /**
    * Sets the study of the Questionnaire.
    * 
    * @param register The register to set.
    * @throws NullPointerException If the study is <code>null</code>
    */
   public void setStudy( Study study )
     {
     if( study != null )
       {
       this.study = study;
       this.study.addQuestionnaire( this );
       }
     else
       throw new NullPointerException( );
     }
 
   @Override
   public boolean equals( Object object )
     {
     Questionnaire otherQuestionnaire = (Questionnaire) object;
     return this.getStudy( ).equals( otherQuestionnaire.getStudy( ) )
         && this.getIdentifier( ).equals( otherQuestionnaire.getIdentifier( ) );
     }
 
   @Override
   public int hashCode( )
     {
     return this.getStudy( ).hashCode( ) ^ this.getIdentifier( ).hashCode( );
     }
 
   private class FeedComparator implements Comparator<Feed>, Serializable
     {
     private static final long serialVersionUID = 4718037652095754746L;
 
     public int compare( Feed feed1, Feed feed2 )
       {
       Integer firstPosition = Questionnaire.this.getElements( ).indexOf( feed1.getFirstElement( ) );
       Integer secondPosition = Questionnaire.this.getElements( ).indexOf( feed2.getFirstElement( ) );
       return firstPosition.compareTo( secondPosition );
       }
     }
   }
