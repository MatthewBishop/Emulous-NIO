package server.model.players.packets;

import server.*;
import server.util.*;
import server.model.players.*;

/**
 * Change Regions
 */
public class ChangeRegions implements PacketType {

	@Override
	public void processPacket(Client c, int packetType, int packetSize) {
		Server.objectHandler.updateObjects(c);
		Server.itemHandler.reloadItems(c);
		c.saveFile = true;
		
		if(c.skullTimer > 0) {
			c.isSkulled = true;	
			c.headIconPk = 1;
			c.getPA().requestUpdates();
		}

	}
		
}
