 package com.sirolf2009.monopolie.communication.packet;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.util.ArrayList;
 import java.util.List;
 
 import com.sirolf2009.monopolie.Host;
 import com.sirolf2009.monopolie.Monopoly;
 import com.sirolf2009.monopolie.street.Street;
 import com.sirolf2009.monopolie.team.Team;
 
 public class PacketStreet extends Packet {
 
 	public int x;
 	public int y;
 	public int houses;
 	public int size;
 	public String name;
 	public Team owningTeam;
 	public List<Team> visitingTeams;
 
 	public PacketStreet() {}
 
 	public PacketStreet(Street street) {
 		this.x = street.x;
 		this.y = street.y;
 		this.houses = street.houses;
 		this.size = street.size;
 		this.name = street.name;
 		this.owningTeam = street.owningTeam;
 		this.visitingTeams = street.visitingTeams;
 	}
 
 	@Override
 	protected void write(PrintWriter out) {
 		out.println(x);
 		out.println(y);
 		out.println(houses);
 		out.println(size);
 		out.println(name);
 		out.println(owningTeam != null);
 		if(owningTeam != null) {
 			PacketTeam owningTeamPacket = new PacketTeam(owningTeam);
 			owningTeamPacket.write(out);
 		}
 		out.println(visitingTeams.size());
 		for(Team team : visitingTeams) {
 			PacketTeam visitingTeamPacket = new PacketTeam(team);
 			visitingTeamPacket.write(out);
 		}
 	}
 
 	@Override
 	public void receive(BufferedReader in) throws IOException {
		System.out.println("Receiving street");
 		x = Integer.parseInt(in.readLine());
 		y = Integer.parseInt(in.readLine());
 		houses = Integer.parseInt(in.readLine());
 		size = Integer.parseInt(in.readLine());
 		name = in.readLine();
 		String isOwnedString = in.readLine();
 		boolean isOwned = Boolean.valueOf(isOwnedString);
 		if(isOwned) {
 			PacketTeam owningTeamPacket = new PacketTeam();
 			owningTeamPacket.receive(in);
 			owningTeam = owningTeamPacket.getTeam();
 		}
 		int visitingTeamsSize = Integer.parseInt(in.readLine());
 		visitingTeams = new ArrayList<Team>();
 		for(int i = 0; i < visitingTeamsSize; i++) {
 			PacketTeam visitingTeamPacket = new PacketTeam();
 			visitingTeamPacket.receive(in);
 			visitingTeams.add(visitingTeamPacket.getTeam());
 		}
 	}
 
 	@Override
 	public void receivedOnClient(Monopoly monopoly) {
 		monopoly.setStreet(name, getStreet());
		System.out.println(owningTeam);
 		monopoly.updateLocalTeamInfo();
 		monopoly.updateStreetButtons();
 	}
 	
 	@Override
 	public void receivedOnServer(Host host) {
 		host.setStreet(name, getStreet());
 		host.updateTeamInfoIfDisplayed(owningTeam);
 	}
 
 	public Street getStreet() {
 		Street street = new Street();
 		street.x = x;
 		street.y = y;
 		street.houses = houses;
 		street.size = size;
 		street.name = name;
 		street.owningTeam = owningTeam;
 		street.visitingTeams = visitingTeams;
 		return street;
 	}
 
 }
