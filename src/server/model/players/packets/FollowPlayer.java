package server.model.players.packets;

import server.*;
import server.util.*;
import server.model.players.*;

/**
 * Follow Player
 **/
public class FollowPlayer implements PacketType {
	
	@Override
	public void processPacket(Client c, int packetType, int packetSize) {
		int followPlayer = c.getInStream().readUnsignedWordBigEndian();
		if(Server.playerHandler.players[followPlayer] == null) {
			return;
		}
		c.followId = followPlayer;
	}	
}
