package server.model.players.packets;

import server.util.*;
import server.model.players.*;


/**
 * Wear Item
 **/
public class WearItem implements PacketType {

	@Override
	public void processPacket(Client c, int packetType, int packetSize) {
		c.wearId = c.getInStream().readUnsignedWord();
		c.wearSlot = c.getInStream().readUnsignedWordA();
		c.interfaceId = c.getInStream().readUnsignedWordA();
		
		if(c.wearItemTimer > 0 && c.attackTimer > 0) {
			return;
		}
		if(c.attackTimer > 4) {
			c.wearItemTimer = 2;
			c.attackTimer--;
			return;
		}
		c.getItems().wearItem(c.wearId, c.wearSlot);
	}

}
