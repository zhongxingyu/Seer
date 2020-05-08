 package serverstorage;
 
 public class HistoryElement {
 	int client_id;
 	int version_number;
	int client_version_number; // this was handle to eliminate duplicates due to network issues
 	char character_pressed;
 	int position;
 	public int getClient_id() {
 		return client_id;
 	}
 	public void setClient_id(int client_id) {
 		this.client_id = client_id;
 	}
 	public int getVersion_number() {
 		return version_number;
 	}
 	public void setVersion_number(int version_number) {
 		this.version_number = version_number;
 	}
	public int getClient_version_number() {
		return client_version_number;
	}
	public void setClient_version_number(int client_version_number) {
		this.client_version_number = client_version_number;
	}
	
 	public char getCharacter_pressed() {
 		return character_pressed;
 	}
 	public void setCharacter_pressed(char character_pressed) {
 		this.character_pressed = character_pressed;
 	}
 	public int getPosition() {
 		return position;
 	}
 	public void setPosition(int position) {
 		this.position = position;
 	}
 	
 	
 }
