 /*******************************************************************************
  * Copyright (c) 2011 Ericsson Research Canada
  * 
  * All rights reserved. This program and the accompanying materials are
  * made available under the terms of the Eclipse Public License v1.0 which
  * accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Description:
  * 
  * This class encapsulates the properties for the Review Decision model element
  * 
  * Contributors:
  *   Sebastien Dubois - Created for Mylyn Review R4E project
  *   
  ******************************************************************************/
 
 package org.eclipse.mylyn.reviews.r4e.ui.internal.properties.general;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.eclipse.mylyn.reviews.r4e.core.model.R4EParticipant;
 import org.eclipse.mylyn.reviews.r4e.core.model.R4EReviewDecision;
 import org.eclipse.mylyn.reviews.r4e.ui.internal.model.R4EUIModelElement;
 import org.eclipse.mylyn.reviews.r4e.ui.internal.model.R4EUIReviewBasic;
 import org.eclipse.mylyn.reviews.r4e.ui.internal.utils.R4EUIConstants;
 import org.eclipse.ui.views.properties.ComboBoxPropertyDescriptor;
 import org.eclipse.ui.views.properties.IPropertyDescriptor;
 import org.eclipse.ui.views.properties.PropertyDescriptor;
 
 /**
  * @author lmcdubo
  * @version $Revision: 1.0 $
  */
 public class ReviewDecisionProperties extends ModelElementProperties {
 
 	// ------------------------------------------------------------------------
 	// Constants
 	// ------------------------------------------------------------------------
 
 	/**
 	 * Field REVIEW_DECISION_MEETING_ID. (value is ""reviewElement.decisionMeeting"")
 	 */
 	protected static final String REVIEW_DECISION_MEETING_ID = "reviewElement.decisionMeeting";
 
 	/**
 	 * Field REVIEW_DECISION_MEETING_PROPERTY_DESCRIPTOR.
 	 */
 	protected static final PropertyDescriptor REVIEW_DECISION_MEETING_PROPERTY_DESCRIPTOR = new PropertyDescriptor(
 			REVIEW_DECISION_MEETING_ID, R4EUIConstants.DECISION_MEETING_LABEL);
 
 	/**
 	 * Field REVIEW_DECISION_PARTICIPANTS_ID. (value is ""reviewElement.decisionParticipants"")
 	 */
 	protected static final String REVIEW_DECISION_PARTICIPANTS_ID = "reviewElement.decisionParticipants";
 
 	/**
 	 * Field REVIEW_DECISION_PARTICIPANTS_PROPERTY_DESCRIPTOR.
 	 */
 	protected static final PropertyDescriptor REVIEW_DECISION_PARTICIPANTS_PROPERTY_DESCRIPTOR = new PropertyDescriptor(
 			REVIEW_DECISION_PARTICIPANTS_ID, R4EUIConstants.PARTICIPANTS_LABEL);
 
 	/**
 	 * Field REVIEW_EXIT_DECISION_ID. (value is ""reviewElement.exitDecision"")
 	 */
 	protected static final String REVIEW_EXIT_DECISION_ID = "reviewElement.exitDecision";
 
 	/**
 	 * Field REVIEW_EXIT_DECISION_PROPERTY_DESCRIPTOR.
 	 */
 	protected static final ComboBoxPropertyDescriptor REVIEW_EXIT_DECISION_PROPERTY_DESCRIPTOR = new ComboBoxPropertyDescriptor(
 			REVIEW_EXIT_DECISION_ID, R4EUIConstants.EXIT_DECISION_LABEL, R4EUIReviewBasic.getExitDecisionValues());
 
 	/**
 	 * Field REVIEW_DECISION_TIME_SPENT_ID. (value is ""reviewElement.decisionTimeSpent"")
 	 */
 	protected static final String REVIEW_DECISION_TIME_SPENT_ID = "reviewElement.decisionTimeSpent";
 
 	/**
 	 * Field REVIEW_DECISION_TIME_SPENT_PROPERTY_DESCRIPTOR.
 	 */
 	protected static final PropertyDescriptor REVIEW_DECISION_TIME_SPENT_PROPERTY_DESCRIPTOR = new PropertyDescriptor(
 			REVIEW_DECISION_TIME_SPENT_ID, R4EUIConstants.TIME_SPENT_LABEL);
 
 	/**
 	 * Field DESCRIPTORS.
 	 */
 	private static final IPropertyDescriptor[] DESCRIPTORS = { REVIEW_DECISION_MEETING_PROPERTY_DESCRIPTOR,
 			REVIEW_DECISION_PARTICIPANTS_PROPERTY_DESCRIPTOR, REVIEW_EXIT_DECISION_PROPERTY_DESCRIPTOR,
 			REVIEW_DECISION_TIME_SPENT_PROPERTY_DESCRIPTOR };
 
 	// ------------------------------------------------------------------------
 	// Constructors
 	// ------------------------------------------------------------------------
 
 	/**
 	 * Constructor for ReviewExtraProperties.
 	 * 
 	 * @param aElement
 	 *            R4EUIModelElement
 	 */
 	public ReviewDecisionProperties(R4EUIModelElement aElement) {
 		super(aElement);
 	}
 
 	// ------------------------------------------------------------------------
 	// Methods
 	// ------------------------------------------------------------------------
 
 	/**
 	 * Method getPropertyDescriptors.
 	 * 
 	 * @return IPropertyDescriptor[]
 	 * @see org.eclipse.ui.views.properties.IPropertySource#getPropertyDescriptors()
 	 */
 
 	@Override
 	public IPropertyDescriptor[] getPropertyDescriptors() {
 		return DESCRIPTORS;
 	}
 
 	/**
 	 * Method getPropertyValue.
 	 * 
 	 * @param aId
 	 *            Object
 	 * @return Object
 	 * @see org.eclipse.ui.views.properties.IPropertySource#getPropertyValue(Object)
 	 */
 	@Override
 	public Object getPropertyValue(Object aId) {
 		if (null != getElement()) {
 			if (REVIEW_DECISION_MEETING_ID.equals(aId)) {
 				return new ReviewMeetingProperties(getElement());
 			} else if (REVIEW_DECISION_PARTICIPANTS_ID.equals(aId)) {
 				final List<R4EParticipant> participants = ((R4EUIReviewBasic) getElement()).getParticipants();
 				final List<String> decisionParticipantIds = new ArrayList<String>();
 				for (R4EParticipant participant : participants) {
 					if (participant.isIsPartOfDecision()) {
 						decisionParticipantIds.add(participant.getId());
 					}
 				}
 				return decisionParticipantIds;
 			} else if (REVIEW_EXIT_DECISION_ID.equals(aId)) {
 				final R4EReviewDecision decision = ((R4EUIReviewBasic) getElement()).getReview().getDecision();
 				if (null != decision) {
 					return Integer.valueOf(decision.getValue().getValue());
				} else {
					return 0;
 				}
 			} else if (REVIEW_DECISION_TIME_SPENT_ID.equals(aId)) {
 				final R4EReviewDecision decision = ((R4EUIReviewBasic) getElement()).getReview().getDecision();
 				if (null != decision) {
 					return Integer.valueOf(decision.getSpentTime());
 				}
 			}
 		}
 		return null;
 	}
 }
