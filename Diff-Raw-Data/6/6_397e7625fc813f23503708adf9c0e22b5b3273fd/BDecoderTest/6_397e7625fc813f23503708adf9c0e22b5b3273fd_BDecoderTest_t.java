 /*******************************************************************************
  * TorrentUtils.java
  *
  * Copyright (c) 2012 SeedBoxer Team.
  *
  * This file is part of SeedBoxer.
  *
  * SeedBoxer is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * SeedBoxer is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with SeedBoxer.  If not, see <http ://www.gnu.org/licenses/>.
  ******************************************************************************/
 package net.seedboxer.bencode;
 
 import java.io.BufferedInputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.util.Map;
 
 import org.junit.Test;
 
 public class BDecoderTest {
 
 	@Test
 	public void shouldReadRealTorrentFile() throws IOException {
 
 		BDecoder decoder = new BDecoder();
		Map bdecode = decoder.decodeStream(new BufferedInputStream( new FileInputStream(new File("src/test/resources/AFEF3A536782DFFCE34C38BA0405FBB44E3CFDAC.torrent"))));
 
 		System.out.println(bdecode);
 
 		Map map = (Map) bdecode.get("info");
 	}
 
 }
