 /*
  * Copyright (c) 2011, dan.clark@nekocode.org
  *
  * Licensed under FreeBSD license.  See README for details.
  */
 
 package org.nekocode.itunes.remote.connection;
 
 import java.util.HashMap;
 import java.util.Map;
 import java.util.concurrent.locks.Lock;
 import java.util.concurrent.locks.ReentrantLock;
 
 import static org.nekocode.itunes.remote.connection.ContentCodeType.*;
 
 /**
  * An enumeration of identifiers that iTunes can return in its responses, given more helpful names.
  * <p>
  * A listing of these content codes can be requested from iTunes.  However, this listing appears to be incomplete.
  * http://localhost:3689/content-codes
  * <p>
  * Useful reference: http://tapjam.net/daap/
  */
 public enum ContentCode {
     // CA = DACP
     // CM = DMCP
     // A = DAAP
     // M = DMAP
 
     // List Nodes
     ///////////////////////////////////////////////////////////////////
     DAAP_CONTENT_CODES_RESPONSE("mccr", "dmap.contentcodesresponse", LIST),
 
     DAAP_ALBUM_GROUPING("agal", LIST),
     DAAP_DATABASE_BROWSE("abro", "daap.databasebrowse", LIST),
     DAAP_BROWSE_ALBUM_LISTING("abal", "daap.daap.browsealbumlisting", LIST),
     DAAP_BROWSE_ARTIST_LISTING("abar", "daap.browseartistlisting", LIST),
     DAAP_BROWSE_COMPOSER_LISTING("abcp", "daap.browsecomposerlisting", LIST),
     DAAP_BROWSE_GENRE_LISTING("abgn", "daap.browsegenrelisting", LIST),
     DAAP_PLAYLIST_SONGS("apso", "daap.playlistsongs", LIST),
     DAAP_DATABASE_PLAYLISTS("aply", "daap.databaseplaylists", LIST),
     DAAP_SERVER_DATABASES("avdb", "daap.serverdatabases", LIST),
     DAAP_DATABASE_SONGS("adbs", "daap.databasesongs", LIST),
 
     DACP_CACI("caci", LIST), // controlint?
     DACP_CASP("casp", LIST), // speakers?
     
     DMCP_STATUS("cmst", LIST),
     DMCP_CMGT("cmgt", LIST), // get property response?
 
     DMAP_LOGIN_RESPONSE("mlog", "dmap.loginresponse", LIST),
     DMAP_LIST("mlcl", "dmap.listing", LIST),
     DMAP_MSHL("mshl", LIST), // really exists?
     DMAP_DELETED_ID_LIST("mudl", "dmap.deletedidlisting", LIST),
 
     // Internal Multi-Nodes
     ///////////////////////////////////////////////////////////////////
     DMAP_LIST_ITEM("mlit", "dmap.listingitem", MULTI_INTERNAL),
     DMAP_DICTIONARY("mdcl", "dmap.dictionary", MULTI_INTERNAL),
 
     // String Nodes
     ///////////////////////////////////////////////////////////////////
     DAAP_SONG_ALBUM_ARTIST("asaa", "daap.songalbumartist", STRING),
     DAAP_SONG_ALBUM("asal", "daap.songalbum", STRING),
     DAAP_SONG_ARTIST("asar", "daap.songartist", STRING),
     DAAP_SONG_GROUPING("agrp", "daap.songgrouping", STRING),
     DAAP_SONG_GENRE("asgn", "daap.songgenre", STRING),
     DAAP_SONG_DESCRIPTION("asdt", "daap.songdescription", STRING),
     DAAP_SONG_COMMENT("ascm", "daap.songcomment", STRING),
     DAAP_SONG_COMPOSER("ascp", "daap.songcomposer", STRING),
     DAAP_SONG_EQUALIZER_PRESET("aseq", "daap.songeqpreset", STRING),
     DAAP_SONG_FORMAT("asfm", "daap.songformat", STRING),
     DAAP_SONG_DATA_URL("asul", "daap.songdataurl", STRING),
     DAAP_SONG_CATEGORY("asct", "daap.songcategory", STRING),
     DAAP_SONG_CONTENT_DESCRIPTION("ascn", "daap.songcontentdescription", STRING),
     DAAP_SONG_LONG_CONTENT_DESCRIPTION("aslc", "daap.songlongcontentdescription", STRING),
     DAAP_SONG_KEYWORDS("asky", "daap.songkeywords", STRING),
 
     ITUNES_SERIES_NAME("aeSN", "com.apple.itunes.series-name", STRING),
     ITUNES_NETWORK_NAME("aeNN", "com.apple.itunes.network-name", STRING),
     ITUNES_EPISODE_NUMBER_STRING("aeEN", "com.apple.itunes.episode-num-str", STRING),
 
     DACP_NOW_PLAYING_NAME("cann", STRING),
     DACP_NOW_PLAYING_ARTIST("cana", STRING),
     DACP_NOW_PLAYING_GENRE("cang", STRING),
     DACP_NOW_PLAYING_ALBUM("canl", STRING),
 
     DMAP_ITEM_NAME("minm", "dmap.itemname", STRING),
 
     // Date Fields
     ///////////////////////////////////////////////////////////////////
     DAAP_SONG_DATE_ADDED("asda", "daap.songdateadded", DATE),
     DAAP_SONG_DATE_MODIFIED("asdm", "daap.songdatemodified", DATE),
 
     // Integer Fields
     ///////////////////////////////////////////////////////////////////
     DAAP_SONG_ALBUM_ID("asai", INTEGER),
     DAAP_SONG_USER_RATING("asur", "daap.songuserrating", BYTE),
     DAAP_BEATS_PER_MINUTE("asbt", "daap.songbeatsperminute", SHORT),
     DAAP_SONG_BITRATE("asbr", "daap.songbitrate", SHORT),
     DAAP_SONG_CODEC_TYPE("ascd", "daap.songcodectype", INTEGER),
     DAAP_SONG_CODEC_SUBTYPE("ascs", "daap.songcodecsubtype", INTEGER),
     DAAP_SONG_COMPILATION("asco", "daap.songcompilation", BOOLEAN),
     DAAP_SONG_DISC_COUNT("asdc", "daap.songdisccount", SHORT),
     DAAP_SONG_DISC_NUMBER("asdn", "daap.songdiscnumber", SHORT),
     DAAP_SONG_DISABLED("asdb", "daap.songdisabled", BOOLEAN),
     DAAP_SONG_RELATIVE_VOLUME("asrv", "daap.songrelativevolume", BOOLEAN),
     DAAP_SONG_SAMPLE_RATE("assr", "daap.songsamplerate", INTEGER),
     DAAP_SONG_SIZE("assz", "daap.songsize", INTEGER),
     DAAP_SONG_START_TIME("asst", "daap.songstarttime", INTEGER),
     DAAP_SONG_STOP_TIME("assp", "daap.songstoptime", INTEGER),
     DAAP_SONG_TIME("astm", "daap.songtime", INTEGER),
     DAAP_SONG_TRACK_COUNT("astc", "daap.songtrackcount", SHORT),
     DAAP_SONG_TRACK_NUMBER("astn", "daap.songtracknumber", SHORT),
     DAAP_SONG_YEAR("asyr", "daap.songyear", SHORT),
     DAAP_SONG_DATA_KIND("asdk", "daap.songdatakind", BYTE),
     DAAP_SONG_CONTENT_RATING("ascr", "daap.songcontentrating", BYTE),
     DAAP_SONG_GAPLESS("asgp", "daap.songgapless", BYTE),
     DAAP_ATSM("atsm", INTEGER), // ??
     DAAP_BASE_PLAYLIST("abpl", "daap.baseplaylist", BYTE),
     DAAP_PLAYLIST_SHUFFLE_MODE("apsm", "daap.playlistshufflemode", BYTE),
     DAAP_PLAYLIST_REPEAT_MODE("aprm", "daap.playlistrepeatmode", BYTE),
 
     ITUNES_AEIM("aeIM", INTEGER),
     ITUNES_MEDIA_KIND("aeMk", "com.apple.itunes.mediakind", BYTE),
 
     DACP_PLAY_STATUS("caps", INTEGER), // 3 = paused, 4 = playing
     DACP_SHUFFLE_STATUS("cash", INTEGER), // 0 = off, 1 = on
     DACP_REPEAT_STATUS("carp", INTEGER), // 0 = off, 1 = one, 2 = all
     DACP_CAVC("cavc", INTEGER),
     DACP_ALBUM_SHUFFLE_STATUS("caas", INTEGER), // 1 = by song?  disabled?
     DACP_CAAR("caar", INTEGER), // album repeat?
     DACP_REMAINING_TIME("cant", INTEGER), // ms
     DACP_TOTAL_TIME("cast", INTEGER), // ms
     DACP_CASU("casu", INTEGER), // boolean?
     DACP_CAFS("cafs", INTEGER), // boolean?
     DACP_CAFE("cafe", INTEGER), // boolean?
     DACP_CAVS("cavs", INTEGER), // visualizer state? boolean?
     DACP_CAVE("cave", INTEGER), // boolean?
 
     DMCP_REVISION_NUMBER("cmsr", INTEGER),
     DMCP_MEDIA_KIND("cmmk", INTEGER),
     DMCP_CURRENT_VOLUME("cmvo", INTEGER),
 
     DMAP_STATUS("mstt", "dmap.status", INTEGER),
     DMAP_SESSION_ID("mlid", "dmap.sessionid", INTEGER),
     DMAP_UPDATE_TYPE("muty", "dmap.updatetype", BYTE),
     DMAP_SEARCH_ITEMS_FOUND("mtco", "dmap.specifiedtotalcount", INTEGER),
     DMAP_SEARCH_ITEMS_RETURNED("mrco", "dmap.returnedcount", INTEGER),
     DMAP_ITEM_KIND("mikd", "dmap.itemkind", BYTE),
     DMAP_ITEM_ID("miid", "dmap.itemid", INTEGER),
     DMAP_CONTAINER_ITEM_ID("mcti", "dmap.containeritemid", INTEGER),
     DMAP_PERSISTENT_ID("mper", "dmap.persistentid", LONG),
     DMAP_ITEM_COUNT("mimc", "dmap.itemcount", INTEGER),
     DMAP_MSMA("msma", INTEGER), // speaker machine address?
    DMAP_MDBK("mdbk", INTEGER), // database type?  library/radio/shared library?
     DMAP_CONTAINER_COUNT("mctc", "dmap.containercount", INTEGER),
     DMAP_MEDS("meds", INTEGER),
 
     DMAP_CONTENT_CODES_NAME("mcna", "dmap.contentcodesname", STRING),
     DMAP_CONTENT_CODES_NUMBER("mcnm", "dmap.contentcodesnumber", INTEGER),
     DMAP_CONTENT_CODES_TYPE("mcty", "dmap.contentcodestype", SHORT),
 
     // special identifiers
     ///////////////////////////////////////////////////////////////////
     NOW_PLAYING("canp", SPECIAL),
     ITEM_ID("itid", INTEGER), // the item id extracted from canp
     ;
     private static Lock idMapLock = new ReentrantLock();
     private static Map<String, ContentCode> idMap;
     /**
      * The 4-character identifier used by iTunes.
      */
     public final String id;
     private final ContentCodeType type;
     private final String fullName;
 
     private ContentCode(String identifier, ContentCodeType type) {
         this.id = identifier;
         this.type = type;
         this.fullName = null;
     }
 
     private ContentCode(String identifier, String fullName, ContentCodeType type) {
         this.id = identifier;
         this.type = type;
         this.fullName = fullName;
     }
 
     public boolean isStringType() {
         return type == ContentCodeType.STRING;
     }
 
     public boolean isInternalNodeType() {
         return type == ContentCodeType.LIST;
     }
 
     public boolean isInternalMultiNodeType() {
         return type == ContentCodeType.MULTI_INTERNAL;
     }
 
     public boolean isIntegerType() {
         switch (type) {
             case UNSIGNED_BYTE:
             case UNSIGNED_SHORT:
             case BYTE:
             case SHORT:
             case INTEGER:
             case LONG:
                 return true;
             default:
                 return false;
         }
     }
 
     public boolean isSpecialType() {
         return type == ContentCodeType.SPECIAL;
     }
 
     public String getFullName() {
         return fullName;
     }
 
     /**
      * Returns the ContentCode associated with the 4-character iTunes identifier.
      * There are also a few special non-iTunes identifiers allowed.
      *
      * @param identifier 4-character iTunes identifier.
      * @return ResponseIdentifier, or null if this 4-character identifier is not recognized
      */
     public static ContentCode getById(String identifier) {
         // first, need to lazy-initialize the idMap
         //     because this is an enum, we can't populate the map during the constructor calls
         if (idMap == null) {
             idMapLock.lock();
             if (idMap == null) {
                 idMap = new HashMap<String, ContentCode>();
                 for (ContentCode ri : values()) {
                     if (idMap.containsKey(ri.id)) {
                         // this is to detect duplicates in the above list
                         throw new RuntimeException("Duplicate id detected: " + ri.id);
                     }
                     idMap.put(ri.id, ri);
                 }
             }
             idMapLock.unlock();
         }
         return idMap.get(identifier);
     }
 
     /*
     as of iTunes 10.2.0.34
 
 miid dmap.itemid INTEGER
 minm dmap.itemname STRING
 mikd dmap.itemkind BYTE
 mper dmap.persistentid LONG
 mcon dmap.container LIST
 mcti dmap.containeritemid INTEGER
 mpco dmap.parentcontainerid INTEGER
 mstt dmap.status INTEGER
 msts dmap.statusstring STRING
 mimc dmap.itemcount INTEGER
 mctc dmap.containercount INTEGER
 mrco dmap.returnedcount INTEGER
 mtco dmap.specifiedtotalcount INTEGER
 f?ch dmap.haschildcontainers BYTE
 mlcl dmap.listing LIST
 mlit dmap.listingitem LIST
 mbcl dmap.bag LIST
 mdcl dmap.dictionary LIST
 msrv dmap.serverinforesponse LIST
 msau dmap.authenticationmethod BYTE
 msas dmap.authenticationschemes INTEGER
 mslr dmap.loginrequired BYTE
 mpro dmap.protocolversion VERSION
 msal dmap.supportsautologout BYTE
 msup dmap.supportsupdate BYTE
 mspi dmap.supportspersistentids BYTE
 msex dmap.supportsextensions BYTE
 msbr dmap.supportsbrowse BYTE
 msqy dmap.supportsquery BYTE
 msix dmap.supportsindex BYTE
 msrs dmap.supportsresolve BYTE
 mstm dmap.timeoutinterval INTEGER
 msdc dmap.databasescount INTEGER
 mstc dmap.utctime DATE
 msto dmap.utcoffset INTEGER_SECONDS
 mlog dmap.loginresponse LIST
 mlid dmap.sessionid INTEGER
 mupd dmap.updateresponse LIST
 musr dmap.serverrevision INTEGER
 muty dmap.updatetype BYTE
 mudl dmap.deletedidlisting LIST
 mccr dmap.contentcodesresponse LIST
 mcnm dmap.contentcodesnumber INTEGER
 mcna dmap.contentcodesname STRING
 mcty dmap.contentcodestype SHORT
 ated daap.supportsextradata SHORT
 asgr daap.supportsgroups SHORT
 apro daap.protocolversion VERSION
 avdb daap.serverdatabases LIST
 abro daap.databasebrowse LIST
 adbs daap.databasesongs LIST
 aply daap.databaseplaylists LIST
 apso daap.playlistsongs LIST
 arsv daap.resolve LIST
 arif daap.resolveinfo LIST
 abal daap.browsealbumlisting LIST
 abar daap.browseartistlisting LIST
 abcp daap.browsecomposerlisting LIST
 abgn daap.browsegenrelisting LIST
 aePP com.apple.itunes.is-podcast-playlist BYTE
 asal daap.songalbum STRING
 asar daap.songartist STRING
 asbr daap.songbitrate SHORT
 ascm daap.songcomment STRING
 asco daap.songcompilation BYTE
 ascp daap.songcomposer STRING
 asda daap.songdateadded DATE
 asdm daap.songdatemodified DATE
 asdc daap.songdisccount SHORT
 asdn daap.songdiscnumber SHORT
 aseq daap.songeqpreset STRING
 asgn daap.songgenre STRING
 asdt daap.songdescription STRING
 asrv daap.songrelativevolume UNSIGNED_BYTE
 assr daap.songsamplerate INTEGER
 assz daap.songsize INTEGER
 asst daap.songstarttime INTEGER
 assp daap.songstoptime INTEGER
 astm daap.songtime INTEGER
 astc daap.songtrackcount SHORT
 astn daap.songtracknumber SHORT
 asur daap.songuserrating BYTE
 asyr daap.songyear SHORT
 asfm daap.songformat STRING
 minm dmap.itemname STRING
 asdb daap.songdisabled BYTE
 aeNV com.apple.itunes.norm-volume INTEGER
 aeSP com.apple.itunes.smart-playlist BYTE
 asdk daap.songdatakind BYTE
 asul daap.songdataurl STRING
 asbt daap.songbeatsperminute SHORT
 abpl daap.baseplaylist BYTE
 agrp daap.songgrouping STRING
 aeSI com.apple.itunes.itms-songid INTEGER
 aeAI com.apple.itunes.itms-artistid INTEGER
 aePI com.apple.itunes.itms-playlistid INTEGER
 aeCI com.apple.itunes.itms-composerid INTEGER
 aeGI com.apple.itunes.itms-genreid INTEGER
 ascd daap.songcodectype INTEGER
 ascs daap.songcodecsubtype INTEGER
 aeSF com.apple.itunes.itms-storefrontid INTEGER
 apsm daap.playlistshufflemode BYTE
 aprm daap.playlistrepeatmode BYTE
 aePC com.apple.itunes.is-podcast BYTE
 asct daap.songcategory STRING
 ascn daap.songcontentdescription STRING
 aslc daap.songlongcontentdescription STRING
 asky daap.songkeywords STRING
 ascr daap.songcontentrating BYTE
 aeHV com.apple.itunes.has-video BYTE
 aeMK com.apple.itunes.mediakind BYTE
 aeSN com.apple.itunes.series-name STRING
 aeNN com.apple.itunes.network-name STRING
 aeEN com.apple.itunes.episode-num-str STRING
 aeES com.apple.itunes.episode-sort INTEGER
 aeSU com.apple.itunes.season-num INTEGER
 aeGH com.apple.itunes.gapless-heur INTEGER
 asaa daap.songalbumartist STRING
 aeGD com.apple.itunes.gapless-enc-dr INTEGER
 aeGU com.apple.itunes.gapless-dur LONG
 aeGR com.apple.itunes.gapless-resy LONG
 aeGE com.apple.itunes.gapless-enc-del INTEGER
 0 com.apple.itunes.req-fplay BYTE
 asgp daap.songgapless BYTE
 aePS com.apple.itunes.special-playlist BYTE
 ased daap.songextradata SHORT
 asdr daap.songdatereleased DATE
 asdp daap.songdatepurchased DATE
 ashp daap.songhasbeenplayed BYTE
 assn daap.sortname STRING
 assa daap.sortartist STRING
 assl daap.sortalbumartist STRING
 assu daap.sortalbum STRING
 assc daap.sortcomposer STRING
 asss daap.sortseriesname STRING
 asbk daap.bookmarkable BYTE
 asbo daap.songbookmark INTEGER
 aspu daap.songpodcasturl STRING
 aeCR com.apple.itunes.content-rating STRING
 asai daap.songalbumid LONG
 asls daap.songlongsize LONG
 aeSG com.apple.itunes.saved-genius BYTE
 meds dmap.editcommandssupported INTEGER
 aeHD com.apple.itunes.is-hd-video BYTE
 ceJV com.apple.itunes.jukebox-vote INTEGER
 ceJC com.apple.itunes.jukebox-client-vote UNSIGNED_BYTE
 ceJS com.apple.itunes.jukebox-score UNSIGNED_SHORT
 ceJI com.apple.itunes.jukebox-current INTEGER
 aeSE com.apple.itunes.store-pers-id LONG
 aeDR com.apple.itunes.drm-user-id LONG
 aeND com.apple.itunes.non-drm-user-id LONG
 aeK1 com.apple.itunes.drm-key1-id LONG
 aeK2 com.apple.itunes.drm-key2-id LONG
 aeDV com.apple.itunes.drm-versions INTEGER
 aeDP com.apple.itunes.drm-platform-id INTEGER
 aeXD com.apple.itunes.xid STRING
 aeMk com.apple.itunes.extended-media-kind INTEGER
 aeAD com.apple.itunes.adam-ids-array LIST
 aeMX com.apple.itunes.movie-info-xml STRING
 aspc daap.songuserplaycount INTEGER
 agac daap.groupalbumcount INTEGER
 asri daap.songartistid LONG
 aeCS com.apple.itunes.artworkchecksum INTEGER
 aeRS com.apple.itunes.rental-start INTEGER
 aeRD com.apple.itunes.rental-duration INTEGER
 aeRP com.apple.itunes.rental-pb-start INTEGER
 aeRU com.apple.itunes.rental-pb-duration INTEGER
 aspl daap.songdateplayed DATE
 asvc daap.songprimaryvideocodec INTEGER
 mrpr dmap.remotepersistentid LONG
 0    com.apple.itunes.playlist-contains-media-type INTEGER
 aeMC com.apple.itunes.playlist-contains-media-type-count INTEGER
 agma daap.groupmatchedqueryalbumcount INTEGER
 agmi daap.groupmatchedqueryitemcount INTEGER
 askp daap.songuserskipcount INTEGER
 asac daap.songartworkcount SHORT
 askd daap.songlastskipdate DATE
 aeSV com.apple.itunes.music-sharing-version INTEGER
      */
 }
