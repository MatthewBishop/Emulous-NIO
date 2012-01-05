package server.model.players;

import server.model.players.packets.*;

	
public interface PacketType {
	public void processPacket(Client c, int packetType, int packetSize);
}

