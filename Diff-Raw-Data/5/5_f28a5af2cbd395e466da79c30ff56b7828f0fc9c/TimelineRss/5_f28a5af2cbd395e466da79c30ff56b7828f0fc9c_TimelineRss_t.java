 /**
  *     Copyright (C) 2011 Julien SMADJA <julien dot smadja at gmail dot com>
  *
  *     Licensed under the Apache License, Version 2.0 (the "License");
  *     you may not use this file except in compliance with the License.
  *     You may obtain a copy of the License at
  *
  *             http://www.apache.org/licenses/LICENSE-2.0
  *
  *     Unless required by applicable law or agreed to in writing, software
  *     distributed under the License is distributed on an "AS IS" BASIS,
  *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *     See the License for the specific language governing permissions and
  *     limitations under the License.
  */
 
 package com.anzymus.neogeo.hiscores.service.rss;
 
 import java.io.IOException;
 import java.io.StringWriter;
 import java.util.ArrayList;
 import java.util.List;
 import com.anzymus.neogeo.hiscores.converter.ScoreConverter;
 import com.anzymus.neogeo.hiscores.domain.Score;
 import com.anzymus.neogeo.hiscores.domain.Timeline;
 import com.anzymus.neogeo.hiscores.domain.TimelineItem;
 import com.anzymus.neogeo.hiscores.domain.UnlockedTitle;
 import com.sun.syndication.feed.synd.SyndContent;
 import com.sun.syndication.feed.synd.SyndContentImpl;
 import com.sun.syndication.feed.synd.SyndEntry;
 import com.sun.syndication.feed.synd.SyndEntryImpl;
 import com.sun.syndication.feed.synd.SyndFeed;
 import com.sun.syndication.feed.synd.SyndFeedImpl;
 import com.sun.syndication.io.FeedException;
 import com.sun.syndication.io.SyndFeedOutput;
 
 public class TimelineRss {
 
     private static final String FEED_TYPE = "rss_2.0";
 
     private static final String DESCRIPTION_TYPE = "text/plain";
 
     private SyndFeed syndFeed = new SyndFeedImpl();
 
     public String encoding = "UTF-8";
 
     private Timeline timeline;
 
     private ScoreConverter scoreConverter = new ScoreConverter();
 
     public TimelineRss(Timeline timeline) {
         this.timeline = timeline;
     }
 
     @Override
     public String toString() {
         addFeedInformations();
         addEntries();
         try {
             return buildXmlContent(encoding).trim();
         } catch (IOException e) {
             e.printStackTrace();
         } catch (FeedException e) {
             e.printStackTrace();
         }
         return "";
     }
 
     private void addFeedInformations() {
         syndFeed.setFeedType(FEED_TYPE);
         syndFeed.setTitle("Neo Geo Hi-Scores - Timeline");
         syndFeed.setLink("http://www.neogeo-hiscores.com");
         syndFeed.setDescription("Last activity of Neo Geo Hi-Scores");
     }
 
     private void addEntries() {
         List<SyndEntry> entries = new ArrayList<SyndEntry>();
         syndFeed.setEntries(entries);
         List<TimelineItem> items = timeline.getItems();
         for (TimelineItem timelineItem : items) {
             SyndEntry entry = createEntry(timelineItem);
             entries.add(entry);
         }
     }
 
     private SyndEntry createEntry(TimelineItem item) {
         if (item.getScore() != null) {
             return createScoreEntry(item);
         } else {
             return createUnlockedTitleEntry(item);
         }
     }
 
     private SyndEntry createScoreEntry(TimelineItem item) {
         Score score = item.getScore();
         String scoreValue = scoreConverter.getAsString(score.getValue());
         String playerName = score.getPlayer().getFullname();
 
         SyndEntry entry = new SyndEntryImpl();
         SyndContent description = new SyndContentImpl();
         String title = "";
         title += playerName;
         title += " did " + scoreValue;
         title += " on " + score.getGame().getName();
         title += " (" + score.getLevel() + ")";
         entry.setTitle(title);
        entry.setLink("http://www.neogeo-hiscores.com/faces/score/edit.xhtml?scoreId=" + score.getId());
         entry.setPublishedDate(score.getCreationDate());
         description.setType(DESCRIPTION_TYPE);
         description.setValue(title);
         entry.setDescription(description);
         return entry;
     }
 
     private SyndEntry createUnlockedTitleEntry(TimelineItem item) {
         UnlockedTitle unlockedTitle = item.getUnlockedTitle();
         String titleLabel = unlockedTitle.getTitle().getLabel();
         String playerName = unlockedTitle.getPlayer().getFullname();
 
         String title = "";
         title += playerName;
         title += " unlocked title " + titleLabel;
 
         SyndEntry entry = new SyndEntryImpl();
         SyndContent description = new SyndContentImpl();
         entry.setTitle(title);
        entry.setLink("http://www.neogeo-hiscores.com/faces/player/view.xhtml?fullname=" + playerName);
         entry.setPublishedDate(unlockedTitle.getUnlockDate());
         description.setType(DESCRIPTION_TYPE);
         description.setValue(title);
         entry.setDescription(description);
         return entry;
     }
 
     private String buildXmlContent(String encoding) throws IOException, FeedException {
         StringWriter writer = new StringWriter();
         SyndFeedOutput output = new SyndFeedOutput();
         output.output(syndFeed, writer);
         writer.close();
         String xmlContent = writer.getBuffer().toString();
         return new String(xmlContent.getBytes(), encoding);
     }
 
 }
