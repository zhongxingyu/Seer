 /*
  *	Copyright 2006-2012, Rathravane LLC
  *
  *	Licensed under the Apache License, Version 2.0 (the "License");
  *	you may not use this file except in compliance with the License.
  *	You may obtain a copy of the License at
  *	
  *	http://www.apache.org/licenses/LICENSE-2.0
  *	
  *	Unless required by applicable law or agreed to in writing, software
  *	distributed under the License is distributed on an "AS IS" BASIS,
  *	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *	See the License for the specific language governing permissions and
  *	limitations under the License.
  */
 package com.rathravane.drumlin.app.htmlForms;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.util.ArrayList;
 import java.util.List;
 
 import com.rathravane.till.collections.rrMultiMap;
 import com.rathravane.till.data.stringUtils;
 
 public class multipartMimeReader
 {
 	public multipartMimeReader ( String boundary, mimePartFactory mpf )
 	{
 		fBoundaryLine = "--" + boundary;
 		fBoundaryEndMarker = fBoundaryLine + "--";
 		fPartIndex = -1;
 		fInPartHeader = false;
 		fPartHeaders = null;
 		fPartFactory = mpf;
 		fCurrentPart = null;
 		fAllParts = new ArrayList<mimePart> ();
 	}
 
 	public void read ( InputStream in ) throws IOException
 	{
 		// FIXME: this can't be read as text.
 		final BufferedReader reader = new BufferedReader ( new InputStreamReader ( in ) );
 		String line = "";
 		while ( (line = reader.readLine ()) != null )
 		{
 			if ( line.equals ( fBoundaryLine ) )
 			{
 				onPartBoundary ( ++fPartIndex );
 				fInPartHeader = true;
 				fPartHeaders = new rrMultiMap<String,String> ();
 			}
 			else if ( line.equals ( fBoundaryEndMarker ) )
 			{
 				onPartBoundary ( ++fPartIndex );
 				onStreamEnd ();
 				break;
 			}
 			else if ( fPartIndex == -1 )
 			{
 				// header info, discard
 			}
 			else if ( fInPartHeader && line.length() == 0 )
 			{
 				// switch from header info to body
 				fInPartHeader = false;
 				onPartHeaders ( fPartHeaders );
 			}
 			else if ( fInPartHeader )
 			{
 				// part header line
 				final int colon = line.indexOf ( ':' );
 				if ( colon == -1 )
 				{
 					// weird. ignore.
 				}
 				else
 				{
 					final String key = line.substring ( 0, colon ).trim ().toLowerCase ();
 					final String val = line.substring ( colon + 1 ).trim ();
 					fPartHeaders.put ( key, val );
 				}
 			}
 			else
 			{
 				// part bytes
 				onPartBytes ( line );
 			}
 		}
 	}
 	
 	public List<mimePart> getParts ()
 	{
 		return fAllParts;
 	}
 
 	public static rrMultiMap<String,String> parseContentDisposition ( String cd )
 	{
 		// e.g. Content-Disposition: form-data; name="image1"; filename="GrandCanyon.jpg"
 		final rrMultiMap<String,String> result = new rrMultiMap<String,String> ();
 		final String[] parts = cd.split ( ";" );
 		if ( parts.length > 0 )
 		{
 			// first part is special -- it's the disposition (e.g. "attachment")
 			result.put ( "disposition", parts[0] );
 			for ( int i=1; i<parts.length; i++ )
 			{
 				// these are name="value"
 				final int eq = parts[i].indexOf ( '=' );
 				if ( eq > -1 )
 				{
					final String name = parts[i].substring ( 0, eq );
					final String val = stringUtils.dequote ( parts[i].substring ( eq + 1 ) );
 					result.put ( name, val );
 				}
 				else
 				{
 					// just dump it in as name and value
 					result.put ( parts[i], parts[i] );
 				}
 			}
 		}
 		return result;
 	}
 
 	/**
 	 * Called on each part boundary. They occur after the pre-part heading and before the first part,
 	 * then after each part (including the last one).
 	 * @param i
 	 * @throws IOException 
 	 */
 	protected void onPartBoundary ( int i ) throws IOException
 	{
 		closeCurrentPart ();
 	}
 
 	/**
 	 * Called when finished reading a part's header section
 	 * @param headers
 	 * @throws IOException 
 	 */
 	protected void onPartHeaders ( rrMultiMap<String,String> headers ) throws IOException
 	{
 		closeCurrentPart ();
 		fCurrentPart = fPartFactory.createPart ( headers );
 	}
 
 	/**
 	 * Called multiple times during the read of a part's body.
 	 * @param line
 	 * @throws IOException 
 	 */
 	protected void onPartBytes ( String line ) throws IOException
 	{
 		if ( fCurrentPart != null )
 		{
 			fCurrentPart.write ( line );
 		}
 	}
 
 	/**
 	 * Called when the multipart stream is complete
 	 */
 	protected void onStreamEnd ()
 	{
 	}
 
 	private void closeCurrentPart () throws IOException
 	{
 		if ( fCurrentPart != null )
 		{
 			fCurrentPart.close ();
 			fAllParts.add ( fCurrentPart );
 			fCurrentPart = null;
 		}
 	}
 	
 	private final String fBoundaryLine;
 	private final String fBoundaryEndMarker;
 	private int fPartIndex;
 	private boolean fInPartHeader;
 	private rrMultiMap<String,String> fPartHeaders;
 	private final mimePartFactory fPartFactory;
 	private mimePart fCurrentPart;
 	private final ArrayList<mimePart> fAllParts;
 }
