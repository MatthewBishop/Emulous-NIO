package server.model.players.packets;

import server.util.*;
import server.model.players.*;


/**
 * Dialogue
 **/
public class Dialogue implements PacketType {

	@Override
	public void processPacket(Client c, int packetType, int packetSize) {
		
		switch(c.dialogueId) { // temp until i cbf to make it cfg or another format
			case 0: 
			c.getPA().removeAllWindows();
			break;
			
			case 1:
			c.getPA().sendOption2("Yea! I'm fearless!",  "No way! That looks scary!");
			c.dialogueId = 0;
			break;
		}
		
	}

}
