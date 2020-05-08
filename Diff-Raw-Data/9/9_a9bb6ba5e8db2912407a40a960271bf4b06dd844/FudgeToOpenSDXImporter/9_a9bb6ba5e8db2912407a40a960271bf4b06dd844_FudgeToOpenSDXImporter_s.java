 package org.fnppl.opensdx.dmi.wayin;
 
 import java.io.*;
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.*;
 
 import org.fnppl.opensdx.common.*;
 import org.fnppl.opensdx.dmi.GenreConverter;
 import org.fnppl.opensdx.xml.*;
 import org.fnppl.opensdx.security.*;
 
 /*
  * Copyright (C) 2010-2013 
  * 							fine people e.V. <opensdx@fnppl.org> 
  * 							Henning Thieß <ht@fnppl.org>
  * 
  * 							http://fnppl.org
  */
 
 /*
  * Software license
  *
  * As far as this file or parts of this file is/are software, rather than documentation, this software-license applies / shall be applied.
  *  
  * This file is part of openSDX
  * openSDX is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Lesser General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  * 
  * openSDX is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU Lesser General Public License
  * and GNU General Public License along with openSDX.
  * If not, see <http://www.gnu.org/licenses/>.
  *      
  */
 
 /*
  * Documentation license
  * 
  * As far as this file or parts of this file is/are documentation, rather than software, this documentation-license applies / shall be applied.
  * 
  * This file is part of openSDX.
  * Permission is granted to copy, distribute and/or modify this document 
  * under the terms of the GNU Free Documentation License, Version 1.3 
  * or any later version published by the Free Software Foundation; 
  * with no Invariant Sections, no Front-Cover Texts, and no Back-Cover Texts. 
  * A copy of the license is included in the section entitled "GNU 
  * Free Documentation License" resp. in the file called "FDL.txt".
  * 
  */
 
 public class FudgeToOpenSDXImporter extends OpenSDXImporterBase {
 	DateFormat ymd = new SimpleDateFormat("yyyy-MM-dd");
 	private Result ir = Result.succeeded();
 	// test?
 	boolean onlytest = true;
     boolean set_territory_exception_to_license_specific = true;
     
 	public FudgeToOpenSDXImporter(ImportType type, File impFile, File savFile) {
 		super(type, impFile, savFile);
 	}
 	
 	public FudgeToOpenSDXImporter(File impFile) {
 		super(ImportType.getImportType("fudge"), impFile, null);
 	}
 	
 	public Result formatToOpenSDXFile() {
 		try {			
 			
 			Feed feed = this.getImportFeed();
             
 			if(feed!=null) {			
 	            // write file
 				Document doc = Document.buildDocument(feed.toElement());
 				doc.writeToFile(this.saveFile);
 				
 			}
 		} catch (Exception e) {
 			// e.printStackTrace();			
 			ir.succeeded = false;
 			ir.errorMessage = e.getMessage();			
 			ir.exception = e;			
 		}	
 		
 		return ir;				
 	}
 	
 	private Feed getImportFeed() {
 		// do the import
 		Feed feed = null;
 		
 		try {	        
 			// (1) get XML-Data from import document
 	        Document impDoc = Document.fromFile(this.importFile);
 	        Element root = impDoc.getRootElement();
 	          
 	        // (2) get FeedInfo from import and create feedid and new FeedInfo for openSDX
 	        String feedid = UUID.randomUUID().toString();
 	        Calendar cal = Calendar.getInstance();
 	        
 	        String action = "insert";
 	        if(root.getChild("action")!=null) {
 	        	action = root.getChild("action").getChildTextNN("type");
 	        }
 	        
 	        // action types: insert, update, metadata_only, delete 
 	        //-> if delete don't take date, right? no.
 //	        if(!action.equals("delete")) {
 	        	cal.setTime(ymd.parse(root.getChild("action").getChildText("effective_date").substring(0, 9)));
 //	        }	        
 	        
 	        long creationdatetime = cal.getTimeInMillis();	        
 	        long effectivedatetime = cal.getTimeInMillis();
 	        
 	        String lic = root.getChild("contract").getChild("supplier").getChildTextNN("name");
 	        if (lic.length()==0) {
 	        	lic = "[NOT SET]";
 	        }
 	        
 	        ContractPartner sender = ContractPartner.make(ContractPartner.ROLE_SENDER, lic , "-1");
 	        ContractPartner licensor = ContractPartner.make(ContractPartner.ROLE_LICENSOR, lic, "-1");
 	        ContractPartner licensee = ContractPartner.make(ContractPartner.ROLE_LICENSEE,"-1","-1");
 	        
 	        FeedInfo feedinfo = FeedInfo.make(onlytest, feedid, creationdatetime, effectivedatetime, sender, licensor, licensee);
 	        
 	        // path to importfile
 	        String path = this.importFile.getParent();
 	        
 	        // (3) create new feed with feedinfo
 	        feed = Feed.make(feedinfo);              
 	
 	        // Information
         	String consumer_release_date = root.getChildTextNN("consumer_release_date");	        
 	        if(consumer_release_date.length()>0) {
 	        	cal.setTime(ymd.parse(consumer_release_date));
 	        }
 	        else {
 	        	// MUST: when not provided then today
 	        	cal.setTime(new Date());
 	        }
 	        // digitalReleaseDate
 	        long digitalReleaseDate = cal.getTimeInMillis();
         		
 	        String original_release_date = root.getChildTextNN("original_release_date");	        
 	        if(original_release_date.length()>0) {
 	        	cal.setTime(ymd.parse(original_release_date));
 	        }
 	        else {
 	        	// use consumer_release_date
 	        	cal.setTime(ymd.parse(consumer_release_date));
 	        }
 	        // physicalRelease
         	long physicalReleaseDate = cal.getTimeInMillis();     
         	
         	BundleInformation info = BundleInformation.make(physicalReleaseDate, digitalReleaseDate);
         	info.playlength(0); //DEFAULT VALUE
         	
         	// IDs of bundle -> more (?)
         	IDs bundleids = IDs.make();
         	if(root.getChild("upc_code")!=null) bundleids.upc(root.getChildTextNN("upc_code"));
         	if(root.getChild("contract").getChild("supplier").getChild("reference")!=null && root.getChild("contract").getChild("supplier").getChildText("reference").length() > 0) bundleids.licensor(root.getChild("contract").getChild("supplier").getChildTextNN("reference"));
         	if(root.getChild("catalog_number")!=null) bundleids.labelordernum(root.getChildTextNN("catalog_number"));
         	
         	// displayname
         	String displayname = root.getChildTextNN("name");
         	
         	// display_artistname
         	String display_artistname = root.getChildTextNN("display_artist");
         	
         	// license basis
         	Territorial territorial = Territorial.make();
         	
         	Vector<Element> territories = root.getChild("territories").getChildren("territory");
         	for (Iterator<Element> itTerritories = territories.iterator(); itTerritories.hasNext();) {
         		Element territory = itTerritories.next();        	
         		String tn = territory.getText();
         		if(tn.equals("WORLD")) tn = "WW";
         		territorial.allow(tn);
         	}
         	
         	// Release
         	cal.setTime(ymd.parse("2070-01-01"));
         	long canceldate = cal.getTimeInMillis();
         	if(action.equals("delete")) {
         		canceldate = effectivedatetime;
         	}
         	LicenseBasis license_basis = LicenseBasis.make(territorial, digitalReleaseDate, canceldate);
         	
         	//now streaming and download
         	boolean isDownloadAllowed = false;
         	boolean isStreamingAllowed = false;
         	if (root.getChild("usage_rights") != null) {
        		Vector<Element> usage_rights = root.getChildren("usage_rights");
 	        	for (Iterator<Element> itExceptions = usage_rights.iterator(); itExceptions.hasNext();) {
 	        		Element usage_right = itExceptions.next();        	
	        		
 	        		// rule for explicit physical release dates in territories
 	        		if("PermanentDownload".equals(usage_right.getText())) {
 	        			isDownloadAllowed = true;
 	        		}
 	        		if("SubscriptionStreaming".equals(usage_right.getText())) {
 	        			isStreamingAllowed = true;
 	        		}
 	        	}    
         	}
         	license_basis.streaming_allowed(isStreamingAllowed);
         	license_basis.download_allowed(isDownloadAllowed);
 
         	// license specifics -> empty!
         	LicenseSpecifics license_specifics = LicenseSpecifics.make();
         	
         	// init GenreConverter
         	GenreConverter gc = GenreConverter.getInstance(GenreConverter.FUDGE_TO_OPENSDX);        	
         	
         	// Translate territory_exceptions to rules
         	//TODO: CHECK IF THIS will happen
         	Date defaultReleaseDate = new Date(digitalReleaseDate);
         	boolean dateChanged = false;
         	if (root.getChild("territory_exceptions") != null) {
 	        	int num = 1;
 	        	Vector<Element> exceptions = root.getChild("territory_exceptions").getChildren("territory_exception");
 	        	for (Iterator<Element> itExceptions = exceptions.iterator(); itExceptions.hasNext();) {
 	        		Element exception = itExceptions.next();        	
 	        		// rule for explicit consumer_release_dates in territories
 	        		if(exception !=null && exception.getChild("consumer_release_date") != null) {
 		        		if (set_territory_exception_to_license_specific) {
 		        			System.out.println("LIC SPEC: territory: "+exception.getChildTextNN("territory"));
 		        			LicenseRule rule = LicenseRule.make(num, "territory", LicenseRule.OPERATOR_EQUALS, exception.getChildTextNN("territory"));
 			        		String exception_physicalReleaseDate = exception.getChildTextNN("consumer_release_date");
 			        		cal.setTime(ymd.parse(exception_physicalReleaseDate));
 			        		rule.addThenProclaim("digital_release_datetime", ""+cal.getTimeInMillis());
 			        		license_specifics.addRule(rule);
 		        		}
 		        		else{
 			        		Date tmp = ymd.parse(exception.getChildText("consumer_release_date"));
 		        			//if this releaseDate is LATER than "normal" releasedate, set this as NEW releasedate
 		        			if (tmp.after(defaultReleaseDate)) {
 		        				defaultReleaseDate = tmp;
 		        				dateChanged = true;
 		        			}	        			
 			        		num++;		        		
 		        		}
 	        		}
 	        	}     
         	}
         	if (dateChanged) {
         		license_basis.timeframe_from_datetime(defaultReleaseDate.getTime());
         		digitalReleaseDate = defaultReleaseDate.getTime();
         	}
         	
         	// receiver -> "MUST"
         	Receiver rec = Receiver.make(Receiver.TRANSFER_TYPE_OSDX_FILESERVER);
         	//TODO Only set for validation success
         	rec.servername("localhost");
         	rec.serveripv4("127.0.0.1");
         	rec.authtype("login");
         	feedinfo.receiver(rec);
         	
         	Bundle bundle = Bundle.make(bundleids, displayname, displayname, "", display_artistname, info, license_basis, license_specifics);
         	
         	// add Tags
         	ItemTags tags = ItemTags.make();   		
         	//TODO Change this for other genres
         	tags.addGenre(gc.convert(root.getChildTextNN("main_genre"), "Miscellaneous"));
         	if(root.getChild("main_subgenre")!=null) tags.addGenre(gc.convert(root.getChildTextNN("main_subgenre"), "Miscellaneous"));
         	
         	// explicit_lyrics
         	if(root.getChildTextNN("parental_advisory").length()>0) {
         		if(root.getChildTextNN("parental_advisory").toLowerCase().equals("false")) {
         			tags.explicit_lyrics(ItemTags.EXPLICIT_LYRICS_FALSE);  
         		}
         		else if(root.getChildTextNN("parental_advisory").toLowerCase().equals("true")) {
         			tags.explicit_lyrics(ItemTags.EXPLICIT_LYRICS_TRUE);  
         		}            		
         	}        	
         	
     		bundle.tags(tags);        	
         	
         	Contributor contributor = Contributor.make(root.getChildTextNN("label"), Contributor.TYPE_LABEL, IDs.make());
         	bundle.addContributor(contributor);
         	BundleTexts bt = BundleTexts.make();
         	
         	int index = 0;
         	Vector<Element> artists = root.getChild("artists").getChildren("artist");
         	for (Iterator<Element> itArtists = artists.iterator(); itArtists.hasNext();) {
         		Element artist = itArtists.next();        	
         	
         		contributor = Contributor.make(artist.getChildTextNN("name"), Contributor.TYPE_DISPLAY_ARTIST, IDs.make().licensor(artist.getChildTextNN("id")));
         		bundle.addContributor(contributor);
         		
         		if(artist.getChild("biography")!=null) {
         			bt.promotext(index, artist.getChildTextNN("biography"));
         			index++;
         		}
         	}
         	info.texts(bt);
          	
          	String copyright = "";
          	if(root.getChild("c_line_text")!=null) { 
          		copyright = root.getChildTextNN("c_line_text"); 
          	}
          	String production = null;
          	if(root.getChild("p_line_text")!=null) {
          		production = root.getChildTextNN("p_line_text"); 
          	}
          	
          	if(copyright.length()>0) {
 	         		contributor = Contributor.make(copyright, Contributor.TYPE_COPYRIGHT, IDs.make());
 	         		if(root.getChild("c_line_year")!=null) {
 	         			contributor.year(root.getChildTextNN("c_line_year"));
 	         		}
 	         		bundle.addContributor(contributor);  
          	}
          	
          	if(production.length()>0) {
 	         		contributor = Contributor.make(production, Contributor.TYPE_PRODUCTION, IDs.make());
 	         		if(root.getChild("p_line_year")!=null) {
 	         			contributor.year(root.getChildTextNN("p_line_year"));
 	         		}
 	         		bundle.addContributor(contributor);  
          	}         	
         	        	
         	// cover
         	Element cover = null;
         	if(root.getChild("cover_art")!=null) {
         		cover = root.getChild("cover_art").getChild("image");
         	}
         	if(cover != null) {
         		ItemFile itemfile = ItemFile.make(); 
         		itemfile.type("frontcover");
         		itemfile.filetype(cover.getChildTextNN("file_format"));
         		// check if file exist at path
         		String filename = cover.getChild("file").getChildTextNN("name");
         		String fpath = cover.getChild("file").getChildTextNN("path")+File.separator;
         		File f = new File(path+fpath+filename);
         		if (f != null && f.exists()) {
         			itemfile.setFile(f);        			
         			// set delivered path to file 
         			itemfile.setLocation(FileLocation.make(fpath+filename,fpath+filename));
         		} else {
         			//first check if we found it within that directory
         			//Get path of importFile
         			f = new File(importFile.getParentFile(), filename);
         			if (f != null && f.exists()) {
             			itemfile.setFile(f);        			
             			// set delivered path to file 
             			itemfile.setLocation(FileLocation.make(filename, fpath+filename));
         			}
         			else {
 	        			//file does not exist -> so we have to set the values "manually"	        			
 	        			//-> use filename for location
 	        			itemfile.setLocation(FileLocation.make(fpath+filename,fpath+filename));
 	        			//file size
 	        			if(cover.getChild("file").getChild("size")!=null) {
 	            			itemfile.bytes(Integer.parseInt(cover.getChild("file").getChildTextNN("size")));
 	            		}        		
         			}
         		}
         		
         		bundle.addFile(itemfile);
         	}
         	
         	Vector<Element> tracks = root.getChild("assets").getChild("tracks").getChildren("track");
         	for (Iterator<Element> itTracks = tracks.iterator(); itTracks.hasNext();) {
         		Element track = itTracks.next();
 
         		IDs trackids = IDs.make();
             	if(track.getChild("upc_code")!=null) trackids.upc(track.getChildTextNN("upc"));
             	if(track.getChild("isrc_code")!=null) trackids.isrc(track.getChildTextNN("isrc_code"));
         		
 	        	// displayname
 	        	String track_displayname = track.getChildTextNN("name");  
 	        	
 	        	// display_artistname
 	        	String track_display_artistname = track.getChildTextNN("display_artist");
 	        	
 	        	BundleInformation track_info = BundleInformation.make(physicalReleaseDate, digitalReleaseDate);		        	
 	        	
 	        	if(track.getChild("country_of_recording")!=null) track_info.origin_country(track.getChildTextNN("country_of_recording"));
 	        	
 	        	// num
 	        	if(track.getChildTextNN("sequence_number").length()>0) {
 	        		track_info.num(Integer.parseInt(track.getChildText("sequence_number")));
 	        	}
 	        	
 	        	// setnum
 	        	if(track.getChildTextNN("on_disc").length()>0) {
 	        		track_info.setnum(Integer.parseInt(track.getChildText("on_disc")));
 	        	} 
 	        	
 	        	// tracklength
         		if(track.getChildTextNN("duration").length()>0) {
         			track_info.playlength(Integer.parseInt(track.getChildText("duration")));     			
         		}
         		
         		// track license basis
         		LicenseBasis track_license_basis = LicenseBasis.make();
         		track_license_basis.as_on_bundle(true);
             	
 	        	// license specifics -> empty!
 	        	LicenseSpecifics track_license_specifics = LicenseSpecifics.make(); 
 	        	track_license_specifics.as_on_bundle(true);
 	        	
         		// license_basis of Bundle / license_specifics of Bundle / others (?)
 	        	Item item = Item.make(trackids, track_displayname, track_displayname, "", "audio", track_display_artistname, track_info, track_license_basis, track_license_specifics);
 	        	            	
         		// add contributor
         		Contributor track_contributor = Contributor.make(track_display_artistname, Contributor.TYPE_DISPLAY_ARTIST, IDs.make());
              	item.addContributor(track_contributor);  
              	
             	// add Tags
             	ItemTags track_tags = ItemTags.make();   		
             	track_tags.addGenre(gc.convert(track.getChildTextNN("main_genre"), "Miscellaneous"));
             	if(track.getChild("main_subgenre")!=null) track_tags.addGenre(gc.convert(track.getChildTextNN("main_subgenre"), "Miscellaneous"));
             	
             	// explicit_lyrics
             	if(track.getChildTextNN("parental_advisory").length()>0) {
             		if(track.getChildTextNN("parental_advisory").toLowerCase().equals("false")) {
             			track_tags.explicit_lyrics(ItemTags.EXPLICIT_LYRICS_FALSE);  
             		}
             		else if(track.getChildTextNN("parental_advisory").toLowerCase().equals("true")) {
             			track_tags.explicit_lyrics(ItemTags.EXPLICIT_LYRICS_TRUE);  
             		}            		
             	}
             	
             	item.tags(track_tags);	        	
 	        	
         		ItemFile itemfile = ItemFile.make();
         		itemfile.type("full");
         		
         		if(track.getChild("suggested_preview_length")!=null) { itemfile.setPrelistening_length(Integer.parseInt(track.getChildTextNN("suggested_preview_length"))); }
         		if(track.getChild("suggested_preview_start")!=null) { itemfile.setPrelistening_offset(Integer.parseInt(track.getChildTextNN("suggested_preview_start"))); }
         		
         		BundleTexts track_bt = BundleTexts.make();
         		
         		index = 0;
             	Vector<Element> track_artists = track.getChild("artists").getChildren("artist");
             	for (Iterator<Element> itTrackArtists = track_artists.iterator(); itTrackArtists.hasNext();) {
             		Element track_artist = itTrackArtists.next();        	
             	
             		contributor = Contributor.make(track_artist.getChildTextNN("name"), Contributor.TYPE_DISPLAY_ARTIST, IDs.make().licensor(track_artist.getChildTextNN("id")));
             		item.addContributor(contributor);
             		
             		if(track_artist.getChild("biography")!=null) {
             			track_bt.promotext(index, track_artist.getChildTextNN("biography"));
             			track_bt.promotext_language(index, "EN");
             			index++;
             		}            		
             	}        		
             	track_info.texts(track_bt);
             	
              	String track_copyright = "";
              	if(track.getChild("c_line_text")!=null) { track_copyright = track.getChildTextNN("c_line_text"); }
              	String track_production = "";
              	if(track.getChild("p_line_text")!=null) { track_production = track.getChildTextNN("p_line_text"); }
              	
              	if(track_copyright.length()>0) {
     	         		contributor = Contributor.make(track_copyright, Contributor.TYPE_COPYRIGHT, IDs.make());
     	         		if(track.getChild("c_line_year")!=null) {
     	         			contributor.year(track.getChildTextNN("c_line_year"));
     	         		}
     	         		item.addContributor(contributor);  
              	}
              	
              	if(track_production.length()>0) {
     	         		contributor = Contributor.make(track_production, Contributor.TYPE_PRODUCTION, IDs.make());
     	         		if(track.getChild("p_line_year")!=null) {
     	         			contributor.year(track.getChildTextNN("p_line_year"));
     	         		}
     	         		item.addContributor(contributor);  
              	}
              	
              	// ToDo: set all contributors! "rights_holder" & "rights_ownership"?!
             	
         		// check if file exist at path
              	Element res = track.getChild("resources");
              	if(res != null) {
              		String filename = res.getChild("audio").getChild("file").getChildTextNN("name");
              		String fpath = res.getChild("audio").getChild("file").getChildTextNN("path")+File.separator;
              		File f = new File(path+fpath+filename);      		
              		if(f!=null && f.exists()) {
              			itemfile.setFile(f); //this will also set the filesize and calculate the checksums
              			// set delivered path to file 
              			itemfile.setLocation(FileLocation.make(fpath+filename,fpath+filename));
 
              		} else {
              			//first check if we found it within that directory
             			//Get path of importFile
             			f = new File(importFile.getParentFile(), filename);
             			if (f != null && f.exists()) {
                 			itemfile.setFile(f);        			
                 			// set delivered path to file 
                 			itemfile.setLocation(FileLocation.make(filename, fpath+filename));
             			}
             			else {
             				//file does not exist -> so we have to set the values "manually"	
 	             			//-> use filename as location
 	             			itemfile.setLocation(FileLocation.make(fpath+filename,fpath+filename));
 	
 	             			//file size
 	             			if(track.getChild("resources").getChild("audio")!=null && track.getChild("resources").getChild("audio").getChild("file")!=null && track.getChild("resources").getChild("audio").getChild("file").getChild("size")!=null) {
 	             				itemfile.bytes(Integer.parseInt(track.getChild("resources").getChild("audio").getChild("file").getChildTextNN("size")));
 	             			} 
             			}
              		}
              		
              		item.addFile(itemfile);
              	}
         			        	
 	        	
 	        	bundle.addItem(item);
         	}
         	
         	tracks = root.getChild("assets").getChild("tracks").getChildren("classical_track");
         	for (Iterator<Element> itTracks = tracks.iterator(); itTracks.hasNext();) {
         		Element track = itTracks.next();
 
         		IDs trackids = IDs.make();
             	if(track.getChild("upc_code")!=null) trackids.upc(track.getChildTextNN("upc"));
             	if(track.getChild("isrc_code")!=null) trackids.isrc(track.getChildTextNN("isrc_code"));
         		
 	        	// displayname
 	        	String track_displayname = track.getChildTextNN("name");  
 	        	
 	        	// display_artistname
 	        	String track_display_artistname = track.getChildTextNN("display_artist");
 	        	
 	        	BundleInformation track_info = BundleInformation.make(physicalReleaseDate, digitalReleaseDate);			        	
 	        	
 	        	if(track.getChild("country_of_recording")!=null) track_info.origin_country(track.getChildTextNN("country_of_recording"));
 	        	
 	        	// num
 	        	if(track.getChildTextNN("sequence_number").length()>0) {
 	        		track_info.num(Integer.parseInt(track.getChildText("sequence_number")));
 	        	}
 	        	
 	        	// setnum
 	        	if(track.getChildTextNN("on_disc").length()>0) {
 	        		track_info.setnum(Integer.parseInt(track.getChildText("on_disc")));
 	        	} 
 	        	
 	        	// tracklength
         		if(track.getChildTextNN("duration").length()>0) {
         			track_info.playlength(Integer.parseInt(track.getChildText("duration")));     			
         		}
         		
         		// track license basis
         		LicenseBasis track_license_basis = LicenseBasis.make();
         		track_license_basis.as_on_bundle(true);
             	
 	        	// license specifics -> empty!
 	        	LicenseSpecifics track_license_specifics = LicenseSpecifics.make(); 
 	        	track_license_specifics.as_on_bundle(true);
 	        	
         		// license_basis of Bundle / license_specifics of Bundle / others (?)
 	        	Item item = Item.make(trackids, track_displayname, track_displayname, "", "audio", track_display_artistname, track_info, track_license_basis, track_license_specifics);
 	        	            	
         		// add contributor
         		Contributor track_contributor = Contributor.make(track_display_artistname, Contributor.TYPE_DISPLAY_ARTIST, IDs.make());
              	item.addContributor(track_contributor);  
              	
             	// add Tags
             	ItemTags track_tags = ItemTags.make();   		
             	track_tags.addGenre(gc.convert(track.getChildTextNN("main_genre"), "Miscellaneous"));
             	if(track.getChild("main_subgenre")!=null) track_tags.addGenre(gc.convert(track.getChildTextNN("main_subgenre"), "Miscellaneous"));
             	
             	// explicit_lyrics
             	if(track.getChildTextNN("parental_advisory").length()>0) {
             		if(track.getChildTextNN("parental_advisory").toLowerCase().equals("false")) {
             			track_tags.explicit_lyrics(ItemTags.EXPLICIT_LYRICS_FALSE);  
             		}
             		else if(track.getChildTextNN("parental_advisory").toLowerCase().equals("true")) {
             			track_tags.explicit_lyrics(ItemTags.EXPLICIT_LYRICS_TRUE);  
             		}            		
             	}
             	
             	item.tags(track_tags);	        	
 	        	
         		ItemFile itemfile = ItemFile.make();
         		itemfile.type("full");
         		
         		if(track.getChild("suggested_preview_length")!=null) { itemfile.setPrelistening_length(Integer.parseInt(track.getChildTextNN("suggested_preview_length"))); }
         		if(track.getChild("suggested_preview_start")!=null) { itemfile.setPrelistening_offset(Integer.parseInt(track.getChildTextNN("suggested_preview_start"))); }
         		
         		BundleTexts track_bt = BundleTexts.make();
         		
         		index = 0;        		
             	Vector<Element> track_artists = track.getChild("artists").getChildren("artist");
             	for (Iterator<Element> itTrackArtists = track_artists.iterator(); itTrackArtists.hasNext();) {
             		Element track_artist = itTrackArtists.next();        	
             	
             		contributor = Contributor.make(track_artist.getChildTextNN("name"), Contributor.TYPE_DISPLAY_ARTIST, IDs.make().licensor(track_artist.getChildTextNN("id")));
             		item.addContributor(contributor);
             		if(track_artist.getChild("biography")!=null) {
             			track_bt.promotext(index, track_artist.getChildTextNN("biography"));
             			track_bt.promotext_language(index, "EN");
             			index++;
             		}            		
             	}        		
             	track_info.texts(track_bt);        		
         		
              	String track_copyright = "";
              	if(track.getChild("c_line_text")!=null) { track_copyright = track.getChildTextNN("c_line_text"); }
              	String track_production = null;
              	if(track.getChild("p_line_text")!=null) { track_production = track.getChildTextNN("p_line_text"); }
              	
              	if(track_copyright.length()>0) {
     	         		contributor = Contributor.make(track_copyright, Contributor.TYPE_COPYRIGHT, IDs.make());
     	         		if(track.getChild("c_line_year")!=null) {
     	         			contributor.year(track.getChildTextNN("c_line_year"));
     	         		}
     	         		item.addContributor(contributor);  
              	}
              	
              	if(track_production!=null && track_production.length()>0) {
     	         		contributor = Contributor.make(track_production, Contributor.TYPE_PRODUCTION, IDs.make());
     	         		if(track.getChild("p_line_year")!=null) {
     	         			contributor.year(track.getChildTextNN("p_line_year"));
     	         		}
     	         		item.addContributor(contributor);  
              	}
              	
              	// ToDo: set all contributors! "rights_holder" & "rights_ownership"?!
             	
              	Vector<Element> cons = track.getChild("contributors").getChildren("contributor");
             	for (Iterator<Element> itCon = cons.iterator(); itCon.hasNext();) {
             		Element con = itCon.next();
             		
             		if(con.getChildTextNN("role").toLowerCase().equals("composer")) {
             			contributor = Contributor.make(con.getChildTextNN("name"), Contributor.TYPE_COMPOSER, IDs.make());	
             		}
             		else if(con.getChildTextNN("role").toLowerCase().equals("conductor")) {
             			contributor = Contributor.make(con.getChildTextNN("name"), Contributor.TYPE_CONDUCTOR, IDs.make());	
             		} 
             		else if(con.getChildTextNN("role").toLowerCase().equals("orchestra")) {
             			contributor = Contributor.make(con.getChildTextNN("name"), Contributor.TYPE_ORCHESTRA, IDs.make());	
             		}            		
             		
             		item.addContributor(contributor);
             	}
              	
         		// check if file exist at path
         		String filename = null;
         		String fpath = null;
         		File f = null;
         		if(track.getChild("resources")!=null) {
         			filename = track.getChild("resources").getChild("audio").getChild("file").getChildTextNN("name");
         			fpath = track.getChild("resources").getChild("audio").getChild("file").getChildTextNN("path")+File.separator;
         			f = new File(path+fpath+filename);
         		}
         		      		
         		if(f!=null && f.exists()) {
         			itemfile.setFile(f); //this will also set the filesize and calculate the checksums
         			
         			// set delivered path to file 
         			itemfile.setLocation(FileLocation.make(fpath+filename,fpath+filename));
         			
         		} 
         		else if(f!=null) {
         			//file does not exist -> so we have to set the values "manually"
         			//-> use filename as location
         			itemfile.setLocation(FileLocation.make(fpath+filename,fpath+filename));
         		
         			//file size
         			if(track.getChild("resources")!=null && track.getChild("resources").getChild("audio")!=null && track.getChild("resources").getChild("audio").getChild("file")!=null && track.getChild("resources").getChild("audio").getChild("file").getChild("size")!=null) {
             			itemfile.bytes(Integer.parseInt(track.getChild("resources").getChild("audio").getChild("file").getChildTextNN("size")));
             		}
         			
         			item.addFile(itemfile);
         		} 
 	        	
 	        	
 	        	bundle.addItem(item);
         	}
         	
         	if (root.getChild("assets") != null && root.getChild("assets").getChild("videos") != null) {
 	        	Vector<Element> videos = root.getChild("assets").getChild("videos").getChildren("video");
 	        	for (Iterator<Element> itVideos = videos.iterator(); itVideos.hasNext();) {
 	        		Element video = itVideos.next();
 	
 	        		IDs videoids = IDs.make();
 	            	if(video.getChild("upc_code")!=null) videoids.upc(video.getChildTextNN("upc"));
 	            	if(video.getChild("isrc_code")!=null) videoids.isrc(video.getChildTextNN("isrc_code"));
 	        		
 		        	// displayname
 		        	String track_displayname = video.getChildTextNN("name");  
 		        	
 		        	// display_artistname
 		        	String track_display_artistname = video.getChildTextNN("display_artist");
 		        	
 		        	BundleInformation video_info = BundleInformation.make(physicalReleaseDate, digitalReleaseDate);
 		        	
 		        	// num
 		        	if(video.getChildTextNN("sequence_number").length()>0) {
 		        		video_info.num(Integer.parseInt(video.getChildText("sequence_number")));
 		        	}
 		        	
 		        	// setnum
 		        	if(video.getChildTextNN("on_disc").length()>0) {
 		        		video_info.setnum(Integer.parseInt(video.getChildText("on_disc")));
 		        	} 
 		        	
 	        		// track license basis
 	        		LicenseBasis video_license_basis = LicenseBasis.make();
 	        		video_license_basis.as_on_bundle(true);
 	            	
 		        	// license specifics -> empty!
 		        	LicenseSpecifics video_license_specifics = LicenseSpecifics.make(); 
 		        	video_license_specifics.as_on_bundle(true);
 		        	
 	        		// license_basis of Bundle / license_specifics of Bundle / others (?)
 		        	Item item = Item.make(videoids, track_displayname, track_displayname, "", "video", track_display_artistname, video_info, video_license_basis, video_license_specifics);
 		        	            	
 	        		// add contributor
 	        		Contributor track_contributor = Contributor.make(video.getChildTextNN("display_artist"), Contributor.TYPE_DISPLAY_ARTIST, IDs.make());
 	             	item.addContributor(track_contributor);  
 	             	
 	            	// add Tags
 	            	ItemTags video_tags = ItemTags.make();   		
 	            	video_tags.addGenre(gc.convert(video.getChildTextNN("main_genre"), "Miscellaneous"));
 	            	if(video.getChild("main_subgenre")!=null) video_tags.addGenre(gc.convert(video.getChildTextNN("main_subgenre"), "Miscellaneous"));
 	            	
 	            	// explicit_lyrics
 	            	if(video.getChildTextNN("parental_advisory").length()>0) {
 	            		if(video.getChildTextNN("parental_advisory").toLowerCase().equals("false")) {
 	            			video_tags.explicit_lyrics(ItemTags.EXPLICIT_LYRICS_FALSE);  
 	            		}
 	            		else if(video.getChildTextNN("parental_advisory").toLowerCase().equals("true")) {
 	            			video_tags.explicit_lyrics(ItemTags.EXPLICIT_LYRICS_TRUE);  
 	            		}            		
 	            	}
 	            	
 	            	item.tags(video_tags);	        	
 		        	
 	        		ItemFile itemfile = ItemFile.make();
 	        		itemfile.type("full");
 	        		
 	            	Vector<Element> video_artists = video.getChild("artists").getChildren("artist");
 	            	for (Iterator<Element> itVideoArtists = video_artists.iterator(); itVideoArtists.hasNext();) {
 	            		Element video_artist = itVideoArtists.next();        	
 	            	
 	            		contributor = Contributor.make(video_artist.getChildTextNN("name"), Contributor.TYPE_DISPLAY_ARTIST, IDs.make().licensor(video_artist.getChildTextNN("id")));
 	            		item.addContributor(contributor);
 	            	}        		
 	             	
 	             	// ToDo: set all contributors! "rights_holder" & "rights_ownership"?!
 	            	
 	        		// check if file exist at path
 	        		String filename = video.getChild("resources").getChild("video").getChild("file").getChildTextNN("name");
 	        		String fpath = video.getChild("resources").getChild("video").getChild("file").getChildTextNN("path")+File.separator;
 	        		File f = new File(path+fpath+filename);      		
 	        		if(f!=null && f.exists()) {
 	        			itemfile.setFile(f); //this will also set the filesize and calculate the checksums
 	        			
 	        			// set delivered path to file 
 	        			itemfile.setLocation(FileLocation.make(fpath+filename,fpath+filename));
 	        			
 	        		} else {
 	        			//file does not exist -> so we have to set the values "manually"
 	        			
 	        			//-> use filename as location
 	        			itemfile.setLocation(FileLocation.make(fpath+filename,fpath+filename));
 	        		
 	        			//file size
 	        			if(video.getChild("resources").getChild("video")!=null && video.getChild("resources").getChild("video").getChild("file")!=null && video.getChild("resources").getChild("video").getChild("file").getChild("size")!=null) {
 	            			itemfile.bytes(Integer.parseInt(video.getChild("resources").getChild("video").getChild("file").getChildTextNN("size")));
 	            		}        		
 	        		} 
 	        		//file type
 	    			if(video.getChild("resources").getChild("video")!=null && video.getChild("resources").getChild("video").getChild("file_format")!=null) {
 	    				itemfile.filetype(video.getChild("resources").getChild("video").getChild("file").getChildTextNN("file_format"));
 	    			}
 	    			
 	        		//bitrate
 	    			if(video.getChild("resources").getChild("video")!=null && video.getChild("resources").getChild("video").getChild("bitrate")!=null) {
 	    				itemfile.bitrate(video.getChild("resources").getChild("video").getChild("file").getChildTextNN("bitrate"));
 	    			}   
 	    			
 	        		//file dimension
 	    			if(video.getChild("resources").getChild("video")!=null && video.getChild("resources").getChild("video").getChild("dimensions")!=null) {
 	    				String[] dim = video.getChild("resources").getChild("video").getChildTextNN("dimensions").split("x");
 	    				if(dim.length==2) {
 	    					itemfile.dimension(Integer.parseInt(dim[0]), Integer.parseInt(dim[1]));
 	    				}
 	    			}
 	    			
 	        		//codec
 	    			if(video.getChild("resources").getChild("video")!=null && video.getChild("resources").getChild("video").getChild("encoding")!=null) {
 	    				itemfile.codec(video.getChild("resources").getChild("video").getChildTextNN("encoding"));
 	    			}    			
 	    			
 		        	item.addFile(itemfile);
 		        	
 		        	bundle.addItem(item);
 	        	}  
         	}
 	         	
         	feed.addBundle(bundle);
 	        
 		} catch (Exception e) {
 			e.printStackTrace();
 			ir.succeeded = false;
 			ir.errorMessage = e.getMessage();			
 			ir.exception = e;			
 		}		        
         return feed;
 	}
 	
 	public Feed getFormatedFeedFromImport() {			
 		return this.getImportFeed();	
 	}
 	
 	public Result getIr() {
 		return ir;
 	}
 
 	public void setIr(Result ir) {
 		this.ir = ir;
 	}	
 
 }
