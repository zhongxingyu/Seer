 package cc.warlock.core.stormfront.network;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.nio.CharBuffer;
 
 import cc.warlock.core.stormfront.client.IStormFrontClient;
 import cc.warlock.core.stormfront.client.internal.StormFrontClient;
 
 public class StormFrontReader extends InputStreamReader {
 
 	protected StringBuffer currentLine = new StringBuffer();
 	protected StormFrontConnection connection;
 	private boolean output = true;
 	private StringBuffer recentText = new StringBuffer();
 	
 	public StormFrontReader (StormFrontConnection connection, InputStream stream)
 	{
 		super(stream);
 		this.connection = connection;
 	}
 
 	@Override
 	public int read() throws IOException {
 		int c = super.read();
 		if (c != -1)
 		{
 			StormFrontClient client = (StormFrontClient) connection.getClient();
 			if(client.getGameMode().get().equals(IStormFrontClient.GameMode.CharacterManager)) {
 				recentText.append(c);
 				if(!this.ready()) {
 					client.getDefaultStream().send(recentText.toString());
 					recentText.setLength(0);
 				}
 			}
 			currentLine.append((char)c);
 			if (((char)c) == '\n')
 			{
 				connection.dataReady(currentLine.toString());
 				currentLine.setLength(0);
 			}
 		}
 		return c;
 	}
 
 	private void appendChars (char[] cbuf, int start, int bytesRead)
 	{
 		if (bytesRead > 0)
 		{
 			StormFrontClient client = (StormFrontClient) connection.getClient();
 			if(client.getGameMode().get().equals(IStormFrontClient.GameMode.CharacterManager)) {
 				if(output) {
 					recentText.append(cbuf, start, bytesRead);
					int end = recentText.indexOf("<mode");
					if(end >= 0) {
						recentText.setLength(end);
 						output = false;
 					}
 					try {
 						// output what text we have if no more is coming or we got to the end of our search
 						if(!this.ready() || !output) {
 							client.getDefaultStream().send(recentText.toString());
 							recentText.setLength(0);
 						}
 					} catch(Exception e) {
 						e.printStackTrace();
 					}
 				}
 			} else {
 				if(!output) {
 					output = true;
 					recentText.setLength(0);
 				}
 			}
 			currentLine.append(cbuf, start, bytesRead);
 			int newLineIndex = currentLine.indexOf("\n");
 			if(newLineIndex != -1) {
 				int tempLineIndex;
 				while ((tempLineIndex = currentLine.indexOf("\n", newLineIndex)) != -1)
 				{
 					newLineIndex = tempLineIndex + 1;
 				}
 			
 				String line = currentLine.substring(0, newLineIndex);
 				connection.dataReady(line);
 				currentLine.delete(0, newLineIndex);
 			}
 		}
 	}
 	
 	@Override
 	public int read(char[] cbuf) throws IOException {
 		int bytesRead = super.read(cbuf);
 		appendChars(cbuf, 0, bytesRead);
 		
 		return bytesRead;
 	}
 	
 	@Override
 	public int read(char[] cbuf, int off, int len) throws IOException {
 		int bytesRead = super.read(cbuf, off, len);
 		appendChars(cbuf, off, bytesRead);
 		
 		return bytesRead;
 	}
 	
 	@Override
 	public int read(CharBuffer target) throws IOException {
 		int bytesRead = super.read(target);
 		appendChars(target.array(), 0, bytesRead);
 		
 		return bytesRead;
 	}
 }
