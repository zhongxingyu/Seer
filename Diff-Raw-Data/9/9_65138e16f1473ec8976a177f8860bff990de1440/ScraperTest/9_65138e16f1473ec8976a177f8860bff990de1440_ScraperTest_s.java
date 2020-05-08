 /*
  * Copyright (c) Mattia Barbon <mattia@barbon.org>
  * distributed under the terms of the MIT license
  */
 
package org.barbon.mangaget.scrape;
 
 import android.content.Context;
 
 import android.os.Environment;
 
 import android.test.InstrumentationTestCase;
 
 import java.io.File;
 
 import java.util.List;
 
 import org.barbon.mangaget.data.DB;
 
 import org.barbon.mangaget.tests.DummyDownloader;
 import org.barbon.mangaget.tests.R;

import org.barbon.mangaget.scrape.animea.Scraper;

 import org.barbon.mangaget.tests.Utils;
 
 public class ScraperTest extends InstrumentationTestCase {
     private DB db;
     private Context testContext;
     private File tempDir;
 
     private class OperationProgress
             implements Scraper.OnOperationStatus {
         public boolean started, complete;
 
         @Override
         public void operationStarted() {
             started = true;
         }
 
         @Override
         public void operationComplete(boolean success) {
             complete = true;
         }
     }
 
     private class DownloadProgress
             implements Scraper.OnChapterDownloadProgress {
         public boolean started, progress, complete;
 
         @Override
         public void downloadStarted() {
             started = true;
         }
 
         @Override
         public void downloadProgress(int current, int total) {
             progress = true;
         }
 
         @Override
         public void downloadComplete(boolean success) {
             complete = true;
         }
     }
 
     private class SearchProgress
             implements Scraper.OnSearchResults {
         public boolean complete;
 
         @Override
         public void resultsUpdated() {
             complete = true;
         }
     }
 
     @Override
     public void setUp() throws Exception {
         Utils.setupTestEnvironment(this);
 
         testContext = getInstrumentation().getContext()
             .createPackageContext("org.barbon.mangaget.tests", 0);
         db = DB.getInstance(null);
 
         // download directory
         File externalStorage = Environment.getExternalStorageDirectory();
         tempDir = new File(externalStorage, "MangaGetTest");
         tempDir.mkdir();
     }
 
     private long setUpTestManga() {
         return db.insertManga(
             "Title", "MangaGetTest/Dummy-%chapter%.cbz",
             "http://manga.animea.net/papillon-hana-to-chou.html");
     }
 
     private long setUpTestChapter(long mangaId) {
         return db.insertChapter(
             mangaId, 1, 45, "Chapter title",
             "http://manga.animea.net/papillon-hana-to-chou-chapter-1-page-1.html");
     }
 
     private Downloader.DownloadDestination getPage(int resource, String base) {
         Downloader.DownloadDestination dest =
             new Downloader.DownloadDestination();
 
         dest.stream = testContext.getResources().openRawResource(resource);
         dest.encoding = "utf-8";
         dest.baseUrl = base;
 
         return dest;
     }
 
     @Override
     public void tearDown() throws Exception {
     }
 
     public void testScrapeResults() {
         String searchPage =
             "http://manga.animea.net/search.html?title=";
         Scraper.SearchResultPage res =
             Scraper.scrapeSearchResults(getPage(R.raw.animea_results_html,
                                                 searchPage));
 
         assertEquals(2, res.currentPage);
         assertEquals("http://manga.animea.net/search.html?title=&completed=0&yor_range=0&yor=&type=any&author=&artist=&genre%5BAction%5D=2&genre%5BAdventure%5D=0&genre%5BComedy%5D=0&genre%5BDoujinshi%5D=0&genre%5BDrama%5D=0&genre%5BEcchi%5D=0&genre%5BFantasy%5D=0&genre%5BGender_Bender%5D=0&genre%5BHarem%5D=0&genre%5BHistorical%5D=0&genre%5BHorror%5D=0&genre%5BJosei%5D=0&genre%5BMartial_Arts%5D=0&genre%5BMature%5D=0&genre%5BMecha%5D=0&genre%5BMystery%5D=0&genre%5BPsychological%5D=0&genre%5BRomance%5D=0&genre%5BSchool_Life%5D=0&genre%5BSci-fi%5D=0&genre%5BSeinen%5D=0&genre%5BShotacon%5D=0&genre%5BShoujo%5D=0&genre%5BShoujo_Ai%5D=0&genre%5BShounen%5D=0&genre%5BShounen_Ai%5D=0&genre%5BSlice_of_Life%5D=0&genre%5BSmut%5D=0&genre%5BSports%5D=0&genre%5BSupernatural%5D=0&genre%5BTragedy%5D=0&genre%5BYaoi%5D=0&genre%5BYuri%5D=0&page=%d", res.pagingUrl);
         assertEquals(48, res.urls.size());
         assertEquals(48, res.titles.size());
 
         assertEquals("http://manga.animea.net/2-kaime-no-hajimete-no-koi.html",
                      res.urls.get(0));
         assertEquals("http://manga.animea.net/a-house-in-venice.html",
                      res.urls.get(47));
 
         assertEquals("2 Kaime no Hajimete no Koi",
                      res.titles.get(0));
         assertEquals("A House in Venice",
                      res.titles.get(47));
     }
 
     public void testScrapeEmptyResults() {
         String searchPage =
             "http://manga.animea.net/search.html?title=pavillon";
         Scraper.SearchResultPage res =
             Scraper.scrapeSearchResults(getPage(R.raw.animea_results_empty_html,
                                                 searchPage));
 
         assertEquals(-1, res.currentPage);
         assertEquals(null, res.pagingUrl);
         assertEquals(0, res.urls.size());
         assertEquals(0, res.titles.size());
     }
 
     public void testScrapeMangaPage() {
         String mangaPage = "http://manga.animea.net/";
         List<Scraper.ChapterInfo> res =
             Scraper.scrapeMangaPage(getPage(R.raw.animea_papillon_chapters_html,
                                             mangaPage));
 
         assertEquals(29, res.size());
         assertEquals("http://manga.animea.net/papillon-hana-to-chou-chapter-1-page-1.html",
                      res.get(0).url);
         assertEquals("http://manga.animea.net/papillon-hana-to-chou-chapter-29-page-1.html",
                      res.get(28).url);
     }
 
     public void testMangaUpdate() throws Throwable {
         final long mangaId = setUpTestManga();
         final Scraper scraper = Scraper.getInstance(testContext);
         final OperationProgress progress = new OperationProgress();
 
         class UiTask implements Runnable {
             @Override
             public void run() {
                 scraper.updateManga(
                     mangaId, progress);
             }
         }
 
         UiTask uiTask = new UiTask();
 
         runTestOnUiThread(uiTask);
 
         while (!progress.complete)
             Thread.sleep(500);
 
         assertTrue(progress.started);
         assertTrue(progress.complete);
 
         // TODO test manga chapters have been updated
     }
 
     public void testFullDownload() throws Throwable {
         final long mangaId = setUpTestManga();
         final long chapterId = setUpTestChapter(mangaId);
         final String targetCbz = new File(tempDir, "Dummy-1.cbz")
             .getAbsolutePath();
         final String tempDirString = tempDir.getAbsolutePath();
         final Scraper scraper = Scraper.getInstance(testContext);
         final DownloadProgress progress = new DownloadProgress();
 
         class UiTask implements Runnable {
             @Override
             public void run() {
                 scraper.downloadChapter(
                     chapterId, targetCbz, tempDirString, progress);
             }
         }
 
         UiTask uiTask = new UiTask();
 
         runTestOnUiThread(uiTask);
 
         while (!progress.complete)
             Thread.sleep(500);
 
         System.out.println("Temp dir: " + tempDirString);
         System.out.println("Target: " + targetCbz);
 
         assertTrue(progress.started);
         assertTrue(progress.progress);
         assertTrue(progress.complete);
         assertTrue(new File(targetCbz).exists());
     }
 
     // TODO test partial downloads
     //      page URLs already filled in
     //      all pages downloaded
     //      page marked downloaded but no file there
 
     public void testSearchPager() throws Throwable {
         final Scraper scraper = Scraper.getInstance(testContext);
         final SearchProgress progress = new SearchProgress();
 
         class UiTask implements Runnable {
             public Scraper.ResultPager pager;
 
             @Override
             public void run() {
                 pager = scraper.searchManga("", progress);
                 assertEquals(0, pager.getCount()); // start the download
             }
         }
 
         UiTask uiTask = new UiTask();
 
         runTestOnUiThread(uiTask);
 
         while (!progress.complete)
             Thread.sleep(500);
 
         assertTrue(progress.complete);
         assertEquals(48, uiTask.pager.getCount());
         assertEquals("2 Kaime no Hajimete no Koi",
                      uiTask.pager.getItem(0).title);
         assertEquals("2-kaime-no-hajimete-no-koi",
                      uiTask.pager.getItem(0).pattern);
     }
 }
