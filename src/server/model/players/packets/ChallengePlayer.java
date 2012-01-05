package server.model.players.packets;

import server.*;
import server.util.*;
import server.model.players.*;

/**
 * Challenge Player
 **/
public class ChallengePlayer implements PacketType {

	@Override
	public void processPacket(Client c, int packetType, int packetSize) {		
		switch(packetType) {
			case 128:
			int answerPlayer = c.getInStream().readUnsignedWord();
			if(Server.playerHandler.players[answerPlayer] == null) {
				return;
			}			
			
			if(c.arenas() || c.duelStatus == 5) {
				c.sendMessage("You can't challenge inside the arena!");
				return;
			}

			c.getTradeAndDuel().requestDuel(answerPlayer);
			break;
		}		
	}	
}
