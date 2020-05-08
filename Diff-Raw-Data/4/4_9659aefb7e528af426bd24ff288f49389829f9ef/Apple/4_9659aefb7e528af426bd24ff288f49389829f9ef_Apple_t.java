 /*
  * Copyright (C) 2011 Mohamed MOUNIROU
  *
  * This program is free software; you can redistribute it and/or
  * modify it under the terms of the GNU General Public License
  * as published by the Free Software Foundation; either version 2
  * of the License, or (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
  */
 package com.mmounirou.spotirss.provider;
 
 import java.util.Set;
 
 import javax.annotation.Nullable;
 
 import org.apache.commons.lang.StringUtils;
 
 import com.mmounirou.spotirss.rss.Track;
 import com.mmounirou.spotirss.tools.StringTools;
 
 public class Apple implements EntryToTrackConverter
 {
 
 	@Override
 	@Nullable
 	public Track apply(@Nullable String strTitle)
 	{
 		int rankPos = strTitle.indexOf(".");
 		String strRank = strTitle.substring(0, rankPos);
 
 		String strArtistAndSong = strTitle.substring(rankPos + 1);
 
		String strTempSong = strArtistAndSong.substring(0, strArtistAndSong.lastIndexOf(" - "));
		String strTempArtist = strArtistAndSong.substring(strArtistAndSong.lastIndexOf(" - ") + 2);
 
 		String strSong = strTempSong;
 
 		strSong = StringUtils.remove(strSong, String.format("(%s)", StringUtils.substringBetween(strSong, "(", ")")));
 		strSong = StringUtils.remove(strSong, String.format("[%s]", StringUtils.substringBetween(strSong, "[", "]")));
 
 		String strArtist = strTempArtist;
 		String strfeaturing = StringUtils.trimToEmpty(StringUtils.substringBetween(strTempSong, "(", ")"));
 		if (strfeaturing.contains("feat."))
 		{
 			strArtist = strArtist + " " + strfeaturing;
 		}
 
 		strfeaturing = StringUtils.trimToEmpty(StringUtils.substringBetween(strTempSong, "[", "]"));
 		if (strfeaturing.contains("feat."))
 		{
 			strArtist = strArtist + " " + strfeaturing;
 		}
 		
 		final Set<String> artistNames =StringTools.split(strArtist, new String[] { "Featuring", "Feat\\.","feat\\.", "&",","});
 
 		return new Track(Integer.parseInt(strRank), artistNames, strSong);
 
 	}
 
 	public static void main(String[] args)
 	{
 		System.out.println(new Apple().apply("74. Hands in the Air (feat. Ne-Yo) [From \"Step Up Revolution\"] - Timbaland"));
 	}
 }
