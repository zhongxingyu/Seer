 /*   Copyright 2012 Mario Bhmer
  *
  *   Licensed under Creative Commons Attribution-NonCommercial-ShareAlike 3.0 Unported (CC BY-NC-SA 3.0) 
  *   you may not use this file except in compliance with the License.
  *   You may obtain a copy of the License at
  *
  *       http://creativecommons.org/licenses/by-nc-sa/3.0/
  */
 package com.blogspot.marioboehmer.thingibrowse.network;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import com.blogspot.marioboehmer.thingibrowse.domain.Thing;
 import com.blogspot.marioboehmer.thingibrowse.domain.ThingResultListItem;
 
 /**
  * A HTML parser/processor which parses {@link Thing} and
  * {@link ThingResultListItem} specific data from the thingiverse platform.
  * 
  * @author Mario Bhmer
  */
 public class ThingiverseHTMLParser {
 
 	private static String thingTag = "<div class=\"thing-float\">";
 	private static String imageUrlTagStart = "<img src=\"";
 	private static String urlStart = "<a href=\"";
 	private static String urlTagEnd = "\"";
 	private static String thingUrlTagStart = "<div class=\"thing-name\"><a href=\"";
 	private static String textTagStart = "\">";
 	private static String textTagEnd = "</a>";
 	private static String paragraphTagStart = "<p>";
 	private static String paragraphTagEnd = "</p>";
 	private static String thingCreatorUrlTagStart = "Created by <a href=\"";
 	private static String thingTimeTagStart = "<div class=\"thing-time\">";
 	private static String divTagStart = "<div>";
 	private static String divTagEnd = "</div>";
 	private static String lastPageIndexstartTagOffset = "\"Next\"";
 	private static String newThingsLastPageIndexstartTag = "<a href=\"/newest/page:";
 	private static String popularThingsLastPageIndexstartTag = "<a href=\"/popular/page:";
 	private static String thingDetailsTitleTagOffset = "<div id=\"thing-meta\">";
 	private static String thingDetailsTitleTagStart = "<h1>";
 	private static String thingDetailsTitleTagEnd = "</h1>";
 	private static String thingDetailsCreatedByTagOffset = "<div class=\"byline\">";
 	private static String thingDetailsCreatorImageUrlOffset = "<div id=\"thing-creator\">";
 	private static String thingDetailsCreatedDateStart = "Created on ";
 	private static String thingDetailsCreatedDateEnd = "</";
 	private static String thingDetailsDescriptionTagStart = "<div id=\"thing-description\">";
 	private static String thingDetailsImageUrlTagOffset = "<div id=\"thing-gallery-main\">";
 	private static String thingDetailsAdditionalImageUrlTagOffset = "<div class=\"thing-image-thumb\">";
 	private static String thingDetailsAdditionalImageUrlsTagStart = "<div id=\"thing-gallery-thumbs\">";
 	private static String thingDetailsAdditionalImageUrlsTagEnd = "<div id=\"thing-info\">";
 	private static String thingDetailsInstructionsTagOffset = "<h4>Instructions</h4>";
 	private static String thingDetailsFilesTagOffset = "<div class=\"thing-status\">";
 	private static String thingDetailsFilesTagOffset2 = "<div class=\"thing-file\"";
 	private static String thingTitleTagStart = "title=\"";
 	private static String largeImageTagOffset = "<div class=\"main-content\"";
 	private static String mediumImageTagOffset = "<b>card</b>";
 
 	public static ArrayList<ThingResultListItem> getThingResultListItems(
 			String html) {
 		ArrayList<ThingResultListItem> resultListItems = new ArrayList<ThingResultListItem>();
 		List<Integer> indices = new ArrayList<Integer>();
 		int startIndex = 0;
 		while ((startIndex = html.indexOf(thingTag, startIndex)) > 0) {
 			startIndex = startIndex + thingTag.length();
 			indices.add(startIndex);
 		}
 		for (int x = 0; x < indices.size(); x++) {
 			String thingHtml;
 			if (x + 1 < indices.size()) {
 				thingHtml = html.substring(indices.get(x).intValue(),
 						indices.get(x + 1));
 			} else {
 				thingHtml = html.substring(indices.get(x));
 			}
 			resultListItems.add(getResultListItemFromHtmlSnippet(thingHtml));
 
 		}
 		return resultListItems;
 	}
 
 	public static Thing getThing(String html) {
 		Map<String, String[]> thingFiles = new HashMap<String, String[]>();
 		List<String> thingAllImageUrls = new ArrayList<String>();
 
 		// ThingTitle
 		String thingTitle = getStringForTags(thingDetailsTitleTagStart,
 				thingDetailsTitleTagEnd,
 				getStringForTags(thingDetailsTitleTagOffset, null, html));
 
 		// ThingCreatedBy
 		String thingCreatedBy = getStringForTags(textTagStart, textTagEnd,
 				getStringForTags(thingDetailsCreatedByTagOffset, null, html));
 
 		// ThingCreatorImageUrl
 		String thingCreatorImageUrl = getStringForTags(
 				imageUrlTagStart,
 				urlTagEnd,
 				getStringForTags(thingDetailsCreatorImageUrlOffset,
 						thingDetailsCreatedByTagOffset, html));
 
 		// ThingCreatorUrl
 		String thingCreatorUrl = getStringForTags(urlStart, urlTagEnd,
 				getStringForTags(thingDetailsCreatedByTagOffset, null, html));
 
 		// ThingDate
 		String thingDate = getStringForTags(thingDetailsCreatedDateStart,
 				thingDetailsCreatedDateEnd, html).replace("\t", "");
 
 		// ThingDescription
 		String thingDescription = getStringForTags(
 				thingDetailsDescriptionTagStart, divTagEnd, html).replace("\t", "");
 
 		// ThingImageUrl
 		String thingImageUrl = getStringForTags(imageUrlTagStart, urlTagEnd,
 				getStringForTags(thingDetailsImageUrlTagOffset, null, html));
 
 		// ThingLargeImageUrl
 		String thingLargeImageUrl = "http://www.thingiverse.com"
 				+ getStringForTags(
 						urlStart,
 						urlTagEnd,
 						getStringForTags(thingDetailsImageUrlTagOffset, null,
 								html));
 
 		// ThingAllImageUrls
 		thingAllImageUrls.add(thingLargeImageUrl);
 		int thingImageUrlsStartIndex = 0;
 		String additionalImagesHtmlSnippet = getStringForTags(
 				thingDetailsAdditionalImageUrlsTagStart,
 				thingDetailsAdditionalImageUrlsTagEnd, html);
 		while ((thingImageUrlsStartIndex = additionalImagesHtmlSnippet.indexOf(
 				thingDetailsAdditionalImageUrlTagOffset,
 				thingImageUrlsStartIndex)) > -1) {
 			thingImageUrlsStartIndex += thingDetailsAdditionalImageUrlTagOffset
 					.length();
 			String thingAdditionalImageSubstring = additionalImagesHtmlSnippet
 					.substring(thingImageUrlsStartIndex);
 			String thingAdditionalImage = "http://www.thingiverse.com"
 					+ getStringForTags(urlStart, urlTagEnd,
 							thingAdditionalImageSubstring);
 			thingAllImageUrls.add(thingAdditionalImage);
 		}
 
 		// ThingInstructions
 		String thingInstructions = getStringForTags(paragraphTagStart,
 				paragraphTagEnd,
 				getStringForTags(thingDetailsInstructionsTagOffset, null, html)).replace("\t", "");
 
 		// ThingFiles
 		int thingFileStartIndex = 0;
 		while ((thingFileStartIndex = html.indexOf(thingDetailsFilesTagOffset2,
 				thingFileStartIndex)) > -1) {
 			thingFileStartIndex += thingDetailsFilesTagOffset.length();
 			String thingFileSubstring = html.substring(thingFileStartIndex);
 			String fileUrl = "http://www.thingiverse.com"
 					+ getStringForTags(urlStart, urlTagEnd, thingFileSubstring);
 			String fileName = getStringForTags(thingTitleTagStart,
 					textTagStart, thingFileSubstring);
 			String fileImageUrl = getStringForTags(imageUrlTagStart, urlTagEnd,
 					thingFileSubstring);
 			String fileSize = getStringForTags(divTagStart, divTagEnd,
 					getStringForTags(textTagEnd, null, thingFileSubstring))
 					.replace("\n", "").replace("\t", "");
 			thingFiles.put(fileUrl, new String[] { fileName, fileSize,
 					fileImageUrl });
 		}
 
 		return new Thing(thingTitle, thingCreatedBy, thingCreatorImageUrl,
 				thingDate, thingDescription, thingCreatorUrl, thingImageUrl,
 				thingLargeImageUrl, thingInstructions, thingFiles,
 				thingAllImageUrls);
 	}
 
 	private static String getStringForTags(String startTag, String endTag,
 			String html) {
 		int startIndex = -1;
 		int endIndex = 0;
 		if ((startIndex = html.indexOf(startTag)) > -1) {
 			startIndex += startTag.length();
 			if (endTag != null) {
 				endIndex = html.indexOf(endTag, startIndex);
				return html.substring(startIndex, endIndex);
 			} else {
				return html.substring(startIndex);
 			}
 		}
 		return "";
 	}
 
 	public static int getNewThingsLastPageIndex(String html) {
 		return Integer.parseInt(getStringForTags(
 				newThingsLastPageIndexstartTag, urlTagEnd,
 				getStringForTags(lastPageIndexstartTagOffset, null, html)));
 	}
 
 	public static int getPopularThingsLastPageIndex(String html) {
 		return Integer.parseInt(getStringForTags(
 				popularThingsLastPageIndexstartTag, urlTagEnd,
 				getStringForTags(lastPageIndexstartTagOffset, null, html)));
 	}
 
 	private static ThingResultListItem getResultListItemFromHtmlSnippet(
 			String htmlSnippet) {
 		// thingImageUrl
 		String thingImageUrl = getStringForTags(imageUrlTagStart, urlTagEnd,
 				htmlSnippet);
 
 		// thingUrl
 		String thingUrl = getStringForTags(thingUrlTagStart, urlTagEnd,
 				htmlSnippet);
 
 		// thingTitle
 		String thingTitle = getStringForTags(thingTitleTagStart, urlTagEnd,
 				htmlSnippet);
 
 		// thingCreatorUrl
 		String thingCreatorUrl = getStringForTags(thingCreatorUrlTagStart,
 				urlTagEnd, htmlSnippet);
 
 		// thingCreatedBy
 		String thingCreatedBy = "by "
 				+ getStringForTags(
 						textTagStart,
 						textTagEnd,
 						getStringForTags(thingCreatorUrlTagStart, null,
 								htmlSnippet));
 
 		// thingTime
 		String thingTime = getStringForTags(thingTimeTagStart, divTagEnd,
 				htmlSnippet).replace("\t", "").replace("\n", "");
 
 		return new ThingResultListItem(thingTitle, thingCreatedBy, thingTime,
 				thingUrl, thingCreatorUrl, thingImageUrl);
 	}
 
 	public static String getLargeImageUrl(String html) {
 		return getStringForTags(imageUrlTagStart, urlTagEnd,
 				getStringForTags(largeImageTagOffset, null, html));
 	}
 
 	public static String getMediumImageUrl(String html) {
 		return getStringForTags(imageUrlTagStart, urlTagEnd,
 				getStringForTags(mediumImageTagOffset, null, html));
 	}
 }
