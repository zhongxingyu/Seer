 /*******************************************************************************
  * Copyright (c) 2010, 2011 Obeo.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     Obeo - initial API and implementation
  *******************************************************************************/
 package org.eclipse.mylyn.docs.intent.parser.descriptionunit;
 
 import org.eclipse.emf.common.util.BasicEList;
 import org.eclipse.emf.common.util.EList;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.mylyn.docs.intent.core.descriptionunit.DescriptionBloc;
 import org.eclipse.mylyn.docs.intent.core.descriptionunit.DescriptionUnit;
 import org.eclipse.mylyn.docs.intent.core.descriptionunit.DescriptionUnitFactory;
 import org.eclipse.mylyn.docs.intent.core.document.IntentDocumentFactory;
 import org.eclipse.mylyn.docs.intent.core.document.IntentSectionOrParagraphReference;
 import org.eclipse.mylyn.docs.intent.core.genericunit.GenericUnitFactory;
 import org.eclipse.mylyn.docs.intent.core.genericunit.IntentSectionReferenceInstruction;
 import org.eclipse.mylyn.docs.intent.core.genericunit.LabelDeclaration;
 import org.eclipse.mylyn.docs.intent.core.genericunit.LabelDeclarationReference;
 import org.eclipse.mylyn.docs.intent.core.genericunit.LabelReferenceInstruction;
 import org.eclipse.mylyn.docs.intent.core.genericunit.TypeLabel;
 import org.eclipse.mylyn.docs.intent.core.genericunit.UnitInstruction;
 import org.eclipse.mylyn.docs.intent.markup.builder.ModelDocumentBuilder;
 import org.eclipse.mylyn.docs.intent.markup.markup.Block;
 import org.eclipse.mylyn.docs.intent.markup.markup.Container;
 import org.eclipse.mylyn.docs.intent.markup.markup.Document;
 import org.eclipse.mylyn.docs.intent.markup.markup.MarkupFactory;
 import org.eclipse.mylyn.docs.intent.markup.markup.Section;
 import org.eclipse.mylyn.docs.intent.markup.markup.StructureElement;
 import org.eclipse.mylyn.docs.intent.parser.IntentKeyWords;
 import org.eclipse.mylyn.docs.intent.parser.modelingunit.ParseException;
 import org.eclipse.mylyn.wikitext.core.parser.MarkupParser;
 import org.eclipse.mylyn.wikitext.textile.core.TextileLanguage;
 
 /**
  * Parser used for parsing a Description Unit of an IntentDocument.
  * 
  * @author <a href="mailto:alex.lagarde@obeo.fr">Alex Lagarde</a>
  */
 public class DescriptionUnitParser {
 
 	/**
 	 * All the tokens that represent a instruction relative to Intent (section reference, label...).
 	 */
 	private static String[] intentTokens = {IntentKeyWords.INTENT_FCT_EXPLICIT_LABEL_DECLARATION,
 			IntentKeyWords.INTENT_FCT_LAZY_LABEL_DECLARATION, IntentKeyWords.INTENT_FCT_REFERENCE,
 	};
 
 	/**
 	 * Internal implementation of the Description Unit parser (using the Markup metaModel).
 	 */
 	private MarkupParser internalMarkupParser;
 
 	/**
 	 * Internal builder of the description unit parser.
 	 */
 	private ModelDocumentBuilder builder;
 
 	private int offSet;
 
 	/**
 	 * DescriptionUnitParser constructor.
 	 */
 	public DescriptionUnitParser() {
 		this.internalMarkupParser = new MarkupParser(new TextileLanguage());
 		builder = new ModelDocumentBuilder();
 		internalMarkupParser.setBuilder(builder);
 	}
 
 	/**
 	 * Parse the given textual form of a description unit.
 	 * 
 	 * @param descriptionUnitToParse
 	 *            the textual from of a description unit to parse
 	 * @return a DescriptionUnit corresponding to the given textual form
 	 * @throws ParseException
 	 *             if the given String isn't well formed
 	 */
 	public DescriptionUnit parse(String descriptionUnitToParse) throws ParseException {
 
 		DescriptionUnit descriptionUnit = DescriptionUnitFactory.eINSTANCE.createDescriptionUnit();
 
 		String remainingContentToParse = descriptionUnitToParse;
 		String currentlyParsedSentence = null;
 		offSet = 0;
 
 		// while there is still content to parse
 		while ((remainingContentToParse.length() > 0) && (offSet != -1)) {
 
 			// We get the offset of the next instruction relative to Intent (section
 			// reference, label...)
 			offSet = getNextOffset(remainingContentToParse);
 
 			// If any offset found
 			if (offSet != -1) {
 
 				// We first get the content before this instruction
 				currentlyParsedSentence = remainingContentToParse.substring(0, offSet);
 				// And add the corresponding description Bloc to the description Unit (if this left part isn't
 				// empty)
 				createDescriptionBlocs(descriptionUnit, currentlyParsedSentence.trim());
 
 				// We the construct this Intent instruction
 				currentlyParsedSentence = remainingContentToParse.substring(offSet);
 				offSet = constructInstructionsFromSentence(descriptionUnit, currentlyParsedSentence);
 				remainingContentToParse = remainingContentToParse.substring(offSet);
 				// We first get the text before this
 
 			} else {
 				// Here we don't have any instruction relative to Intent anymore
 				// If the currentParseSentence is not empty, we add a descriptionBloc to the description Unit
 				createDescriptionBlocs(descriptionUnit, remainingContentToParse.trim());
 
 				offSet = -1;
 			}
 		}
 
 		return descriptionUnit;
 	}
 
 	/**
 	 * Adds the description Blocs described by the given descriptionBlocToParse to the given descriptionUnit
 	 * (if the content to parse isn't empty).
 	 * 
 	 * @param descriptionUnit
 	 *            the description unit currently being constructed
 	 * @param descriptionBlocToParse
 	 *            a String describing description Blocs (sections, paragraphs, lists) in the WikiText syntax.
 	 */
 	private void createDescriptionBlocs(DescriptionUnit descriptionUnit, String descriptionBlocToParse) {
 		if (descriptionBlocToParse.trim().length() > 0) {
 
 			internalMarkupParser.parse(descriptionBlocToParse);
 
 			// For each roots of the parsed document
 			EList<StructureElement> blocksToCreate = new BasicEList<StructureElement>();
 			for (EObject descriptionRoot : builder.getRoots()) {
 
 				// We inspect this root
 				if (descriptionRoot instanceof Document) {
 					// For each element contained in this document
 					for (StructureElement content : ((Document)descriptionRoot).getContent()) {
 
 						// We add it to the block to create list
 						if ((content instanceof Block) || (content instanceof Section)) {
 							blocksToCreate.add(content);
 						}
 					}
 				}
 
 			}
 
 			// For each block to create
 			int blocCount = 0;
 			for (StructureElement blockToCreate : blocksToCreate) {
 
 				// We create a container with no semantic value and it the block to this container
 				Container container = MarkupFactory.eINSTANCE.createSimpleContainer();
 				container.getContent().add(blockToCreate);
 
 				// We create the description bloc
 				DescriptionBloc descriptionBloc = DescriptionUnitFactory.eINSTANCE.createDescriptionBloc();
 				descriptionBloc.setDescriptionBloc(container);
 				// We determine if the block is a line breaker
 				blocCount++;
 				if (isLineBreaker(blockToCreate) && (blocCount < blocksToCreate.size())) {
 					descriptionBloc.setLineBreak(true);
 				}
 				// We finally add the created description Bloc to the description unit
 				descriptionUnit.getInstructions().add(descriptionBloc);
 			}
 		}
 	}
 
 	/**
 	 * Indicates if the given bloc is a line breaker.
 	 * 
 	 * @param blockToCreate
 	 *            the bloc to determine if it's a line breaker
 	 * @return true if the given bloc is a line breaker, false otherwise
 	 */
 	private boolean isLineBreaker(StructureElement blockToCreate) {
 
 		return true;
 	}
 
 	/**
 	 * Construct the instruction description by the given parsedSentence and add it to the DescriptionUnit.
 	 * 
 	 * @param descriptionUnit
 	 *            the description unit currently being constructed
 	 * @param parsedSentence
 	 *            a String starting with the description of a Intent instruction (@see, \@lazylabel...).
 	 * @return the offset following the parsed instruction
 	 */
 	private int constructInstructionsFromSentence(DescriptionUnit descriptionUnit, String parsedSentence) {
 		int endOffset = -1;
 
 		// We create the right instruction
 		if (parsedSentence.startsWith(IntentKeyWords.INTENT_FCT_LAZY_LABEL_DECLARATION)) {
 			String parsedSentenceWithoutLabelDeclaration = parsedSentence.replaceFirst(
 					IntentKeyWords.INTENT_FCT_LAZY_LABEL_DECLARATION, "");
 			offSet += IntentKeyWords.INTENT_FCT_LAZY_LABEL_DECLARATION.length();
 			endOffset = constructLabelInstruction(descriptionUnit, parsedSentenceWithoutLabelDeclaration,
 					TypeLabel.LAZY);
 		}
 		if (parsedSentence.startsWith(IntentKeyWords.INTENT_FCT_EXPLICIT_LABEL_DECLARATION)) {
 			String parsedSentenceWithoutLabelDeclaration = parsedSentence.replaceFirst(
 					IntentKeyWords.INTENT_FCT_EXPLICIT_LABEL_DECLARATION, "");
 			offSet += IntentKeyWords.INTENT_FCT_EXPLICIT_LABEL_DECLARATION.length();
 			endOffset = constructLabelInstruction(descriptionUnit, parsedSentenceWithoutLabelDeclaration,
 					TypeLabel.EXPLICIT);
 		}
 		if (parsedSentence.startsWith(IntentKeyWords.INTENT_FCT_REFERENCE)) {
 			String parsedSentenceWithoutReferenceDeclaration = parsedSentence.replaceFirst(
 					IntentKeyWords.INTENT_FCT_REFERENCE, "");
 			offSet += IntentKeyWords.INTENT_FCT_REFERENCE.length();
 			endOffset = constructReferenceInstruction(descriptionUnit,
 					parsedSentenceWithoutReferenceDeclaration);
 		}
 		return endOffset;
 	}
 
 	/**
 	 * Add the reference instruction described by the given parsedSentence to the given Description Unit.
 	 * 
 	 * @param descriptionUnit
 	 *            the description unit currently being constructed
 	 * @param parsedSentence
 	 *            a String starting with the a reference instruction
 	 * @return the offset following the parsed instruction
 	 */
 	private int constructReferenceInstruction(DescriptionUnit descriptionUnit, String parsedSentence) {
 		int initialOffset = offSet;
 
 		UnitInstruction referenceInstruction = null;
 		// Step 1 : extracting the "Reference" value
 		String referenceValue = extractFirstString(parsedSentence);
 
 		String parsendSentenceWithoutReferenceValue = parsedSentence.substring(offSet - initialOffset);
 		String textToPrint = extractFirstString(parsendSentenceWithoutReferenceValue);
 
 		if (textToPrint != null) {
 			// We consider that it is a SectionReference
 			referenceInstruction = GenericUnitFactory.eINSTANCE.createIntentSectionReferenceInstruction();
 			((IntentSectionReferenceInstruction)referenceInstruction).setTextToPrint(textToPrint);
 			IntentSectionOrParagraphReference reference = IntentDocumentFactory.eINSTANCE
 					.createIntentSectionOrParagraphReference();
 			reference.setIntentHref(referenceValue);
 			((IntentSectionReferenceInstruction)referenceInstruction).setReferencedObject(reference);
 		} else {
 			// We consider that it is a LabelReference
 			referenceInstruction = GenericUnitFactory.eINSTANCE.createLabelReferenceInstruction();
 			((LabelReferenceInstruction)referenceInstruction).setType(TypeLabel.EXPLICIT);
 			LabelDeclarationReference reference = GenericUnitFactory.eINSTANCE
 					.createLabelDeclarationReference();
 			reference.setIntentHref(referenceValue);
 			((LabelReferenceInstruction)referenceInstruction).setReferencedLabel(reference);
 		}
 
 		if (parsedSentence.length() > (offSet - initialOffset)) {
 			if (parsedSentence.charAt(offSet - initialOffset) == '\n') {
 				offSet++;
 				referenceInstruction.setLineBreak(true);
 			}
		} else {
			referenceInstruction.setLineBreak(true);
 		}
 		descriptionUnit.getInstructions().add(referenceInstruction);
 		return offSet;
 	}
 
 	/**
 	 * Add the label definition described by the given parsedSentence to the given Description Unit.
 	 * 
 	 * @param descriptionUnit
 	 *            the description unit currently being constructed
 	 * @param parsedSentence
 	 *            a String starting with the description of a a label definition
 	 * @param typeLabel
 	 *            the type of the label (LAZY or EXPLICIT)
 	 * @return the offset following the parsed instruction
 	 */
 	private int constructLabelInstruction(DescriptionUnit descriptionUnit, String parsedSentence,
 			TypeLabel typeLabel) {
 		int initialOffset = offSet;
 		String labelValue = extractFirstString(parsedSentence);
 		String parsendSentenceWithoutLabelValueDeclaration = parsedSentence.substring(offSet - initialOffset);
 		String textToPrint = extractFirstString(parsendSentenceWithoutLabelValueDeclaration);
 
 		LabelDeclaration labelDeclaration = GenericUnitFactory.eINSTANCE.createLabelDeclaration();
 		labelDeclaration.setType(typeLabel);
 		labelDeclaration.setLabelValue(labelValue);
 		if (textToPrint != null) {
 			labelDeclaration.setTextToPrint(textToPrint);
 		}
 		if (parsedSentence.length() > (offSet - initialOffset)) {
 			if (parsedSentence.charAt(offSet - initialOffset) == '\n') {
 				offSet++;
 				labelDeclaration.setLineBreak(true);
 			}
		} else {
			labelDeclaration.setLineBreak(true);
 		}
 		descriptionUnit.getInstructions().add(labelDeclaration);
 		return offSet;
 	}
 
 	/**
 	 * Returns the first String encapsulated in quotes contained in the given parsedSentence. <br/>
 	 * Example : extractFirstString('"first\"String" myFirstString") = 'first"String';
 	 * 
 	 * @param parsedSentence
 	 *            the sentence to analyse
 	 * @return the first String encapsulated in quotes contained in the given parsedSentence
 	 */
 	public String extractFirstString(String parsedSentence) {
 		String firstString = null;
 		int temporaryOffset = offSet;
 		boolean foundFirstString = false;
 		if (parsedSentence.contains("\"") || parsedSentence.contains("'")) {
 
 			firstString = "";
 			char beginQuote = ' ';
 			char previousCharacter = ' ';
 
 			for (int i = 0; i < parsedSentence.length() && !foundFirstString; i++) {
 				temporaryOffset++;
 				char curentChar = parsedSentence.charAt(i);
 				// If the character is a quote (simple or double)
 				if ((curentChar == '"') || (curentChar == '\'')) {
 					// If it wasn't prefixed by a backslash
 					if (previousCharacter != '\\') {
 						// If there was no begin Quote
 						// CHECKSTYLE:OFF
 						if (beginQuote == ' ') {
 							// We define the current character as the begin quote
 							beginQuote = curentChar;
 						} else {
 							// If there was a begin quote and the current Character is the same Symbol
 							// for instance : 'X' or "X" but not 'X"
 							if (curentChar == beginQuote) {
 								foundFirstString = true;
 							} else {
 								firstString += curentChar;
 								previousCharacter = curentChar;
 							}
 						}
 						// CHECKSTYLE:ON
 					} else {
 						firstString += curentChar;
 						previousCharacter = curentChar;
 					}
 				} else {
 					// If the curent character is a backslash
 					if (curentChar == '\\') {
 						// If the previous one was a backslash
 						if (previousCharacter == '\\') {
 							// We add the backslash character in the first String
 							firstString += curentChar;
 							previousCharacter = ' ';
 						} else {
 							previousCharacter = '\\';
 						}
 					} else {
 						if (beginQuote != ' ') {
 							// In all other cases, we add the current character to the constructed string
 							firstString += curentChar;
 							previousCharacter = curentChar;
 						}
 					}
 
 				}
 			}
 
 		}
 
 		if (foundFirstString) {
 			offSet = temporaryOffset;
 			return firstString;
 		}
 		return null;
 	}
 
 	/**
 	 * Returns the next Offset containing a flow breaker token in the given String.
 	 * 
 	 * @param currentlyParsedContent
 	 *            the String to inspect
 	 * @return the next Offset containing useful informations in the given String, -1 if no valid character
 	 *         can be found
 	 */
 	private int getNextOffset(String currentlyParsedContent) {
 
 		// We calculate the offset of the next occurrence of each flowBreaking tokens
 		Integer[] possibleNextOffsets = new Integer[intentTokens.length];
 
 		for (int i = 0; i < intentTokens.length; i++) {
 			possibleNextOffsets[i] = currentlyParsedContent.indexOf(intentTokens[i]);
 		}
 
 		// We return the offset of the first token encountered
 		return getNextOffSetInTable(possibleNextOffsets);
 	}
 
 	/**
 	 * Returns the offSet to consider in the given table of all detected offsets.
 	 * 
 	 * @param possibleNextOffsets
 	 *            table of all detected offsets
 	 * @return the offSet to consider in the given table of all detected offsets
 	 */
 	private int getNextOffSetInTable(Integer[] possibleNextOffsets) {
 		int nextOffset = -1;
 		for (int i = 0; i < possibleNextOffsets.length; i++) {
 			if ((possibleNextOffsets[i] > -1)
 					&& ((nextOffset == -1) || (possibleNextOffsets[i] < nextOffset))) {
 				nextOffset = possibleNextOffsets[i];
 			}
 		}
 		return nextOffset;
 	}
 }
