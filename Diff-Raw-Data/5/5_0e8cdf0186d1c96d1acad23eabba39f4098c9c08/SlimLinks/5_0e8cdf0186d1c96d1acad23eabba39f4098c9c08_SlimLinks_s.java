 /*=========================================================================
  *
  *  PROJECT:  SlimRoms
  *            Team Slimroms (http://www.slimroms.net)
  *
  *  COPYRIGHT Copyright (C) 2013 Slimroms http://www.slimroms.net
  *            All rights reserved
  *
  *  LICENSE   http://www.gnu.org/licenses/gpl-2.0.html GNU/GPL
  *
  *  AUTHORS:     fronti90, mnazim, tchaari, kufikugel, blk_jack
  *  DESCRIPTION: SlimOTA keeps our rom up to date
  *
  *=========================================================================
  */
 
 package com.slim.ota;
 
 import com.slim.ota.R;
 
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.graphics.Color;
 import android.net.Uri;
 import android.os.Bundle;
 import android.app.Fragment;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.LinearLayout;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.slim.ota.updater.UpdateChecker;
 
 import java.io.BufferedReader;
 import java.io.DataInputStream;
 import java.io.FileInputStream;
 import java.io.InputStreamReader;
 
 public class SlimLinks extends Fragment {
 
     private LinearLayout mChangelog;
     private LinearLayout mDownload;
     private LinearLayout mDownloadGapps;
     private LinearLayout mFAQ;
     private LinearLayout mNews;
     private TextView mChangelogTitle;
     private TextView mChangelogSummary;
     private TextView mDownloadTitle;
     private TextView mDownloadSummary;
 
     private String mStrFileNameNew;
     private String mStrFileURLNew;
     private String mStrCurFile;
 
     public View onCreateView (LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
         View view = inflater.inflate(R.layout.slim_ota_links, container, false);
         return view;
     }
 
     private final View.OnClickListener mActionLayouts = new View.OnClickListener() {
         @Override
         public void onClick(View v) {
             if (v == mChangelog) {
                 launchUrl(getString(R.string.changelog_url));
             } else if (v == mDownload) {
                launchUrl(mStrFileURLNew);
             } else if (v == mDownloadGapps) {
                 launchUrl(getString(R.string.gapps_url));
             } else if (v == mFAQ) {
                 launchUrl(getString(R.string.faq_url));
             } else if (v == mNews) {
                 launchUrl(getString(R.string.news_url));
             }
         }
     };
 
     @Override
     public void onActivityCreated(Bundle savedInstanceState) {
         super.onActivityCreated(savedInstanceState);
 
         //set LinearLayouts and onClickListeners
 
         mChangelog = (LinearLayout) getView().findViewById(R.id.short_cut_changelog);
         mChangelogTitle = (TextView) getView().findViewById(R.id.short_cut_changelog_title);
         mChangelogSummary = (TextView) getView().findViewById(R.id.short_cut_changelog_summary);
         mChangelog.setOnClickListener(mActionLayouts);
 
         mDownload = (LinearLayout) getView().findViewById(R.id.short_cut_download);
         mDownloadTitle = (TextView) getView().findViewById(R.id.short_cut_download_title);
         mDownloadSummary = (TextView) getView().findViewById(R.id.short_cut_download_summary);
         mDownload.setOnClickListener(mActionLayouts);
 
         mDownloadGapps = (LinearLayout) getView().findViewById(R.id.short_cut_download_gapps);
         mDownloadGapps.setOnClickListener(mActionLayouts);
 
         mFAQ = (LinearLayout) getView().findViewById(R.id.short_cut_faq);
         mFAQ.setOnClickListener(mActionLayouts);
 
         mNews = (LinearLayout) getView().findViewById(R.id.short_cut_news);
         mNews.setOnClickListener(mActionLayouts);
 
         try {
             FileInputStream fstream = new FileInputStream("/system/build.prop");
             DataInputStream in = new DataInputStream(fstream);
             BufferedReader br = new BufferedReader(new InputStreamReader(in));
             String strLine;
             while ((strLine = br.readLine()) != null) {
                 String[] line = strLine.split("=");
                 if (line[0].equals("ro.modversion")) {
                     mStrCurFile = line[1];
                 }
             }
             in.close();
         } catch (Exception e) {
             Toast.makeText(getActivity().getBaseContext(), getString(R.string.system_prop_error),
                     Toast.LENGTH_LONG).show();
             e.printStackTrace();
         }
 
         SharedPreferences shPrefs = getActivity().getSharedPreferences("UpdateChecker", 0);
         mStrFileNameNew = shPrefs.getString("Filename", "");
         mStrFileURLNew = shPrefs.getString("DownloadUrl", "");
 
         updateView();
     }
 
     private void launchUrl(String url) {
         Uri uriUrl = Uri.parse(url);
         Intent urlIntent = new Intent(Intent.ACTION_VIEW, uriUrl);
         urlIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
         getActivity().startActivity(urlIntent);
     }
 
     public void updateView() {
         if (!mStrFileNameNew.equals("") && !(mStrFileNameNew.compareToIgnoreCase(mStrCurFile)<=0)) {
             mChangelogTitle.setTextColor(Color.GREEN);
             mChangelogSummary.setTextColor(Color.GREEN);
             mDownloadTitle.setTextColor(Color.GREEN);
             mDownloadSummary.setTextColor(Color.GREEN);
 
             mChangelogSummary.setText(getString(R.string.short_cut_changelog_summary_update_available));
             mDownloadSummary.setText(getString(R.string.short_cut_download_summary_update_available));
         }
     }
 }
