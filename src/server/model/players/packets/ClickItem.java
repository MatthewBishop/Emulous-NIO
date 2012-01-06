package server.model.players.packets;

import server.util.*;
import server.model.players.*;
import server.model.players.content.Food;


/**
 * Clicking an item, bury bone, eat food etc
 **/
public class ClickItem implements PacketType {

	@Override
	public void processPacket(Client c, int packetType, int packetSize) {
		int junk = c.getInStream().readSignedWordBigEndianA();
		int itemSlot = c.getInStream().readUnsignedWordA();
		int itemId = c.getInStream().readUnsignedWordBigEndian();
		if (itemId != c.playerItems[itemSlot] - 1) {
			return;
		}
		Food.handleConsumption(c, itemId, itemSlot);
		switch(itemId) {
			case 952: // spade
			c.startAnimation(830);
			if (c.inArea(3553, 3301, 3561, 3294)) { // verac
				c.teleTimer = 3;
				c.newLocation = 1;
			  } else if (c.inArea(3550, 3287, 3557, 3278)) { // torag
				c.teleTimer = 3;
				c.newLocation = 2;
			  } else if (c.inArea(3561, 3292, 3568, 3285)) { // ahrim
				c.teleTimer = 3;
				c.newLocation = 3;
			  } else if (c.inArea(3570, 3302, 3579, 3293)) { // dharok
				c.teleTimer = 3;
				c.newLocation = 4;
			  } else if (c.inArea(3571, 3285, 3582, 3278)) { // guthan
				c.teleTimer = 3;
				c.newLocation = 5;
			  } else if (c.inArea(3562, 3279, 3569, 3273)) { // karil
				c.teleTimer = 3;
				c.newLocation = 6;
	        } 			
			break;
			
			// Start Herblore
			case 199: // Guam
			server.model.players.skills.Herblore.idHerb(c, itemId, itemSlot, 3, 249);
			break;
			
			case 201: //Marrentill
			server.model.players.skills.Herblore.idHerb(c, itemId, itemSlot, 5, 251);
			break;
			
			case 203: // Tarromin
			server.model.players.skills.Herblore.idHerb(c, itemId, itemSlot, 11, 253);
			break;
			
			case 205: // Harralander 
			server.model.players.skills.Herblore.idHerb(c, itemId, itemSlot, 20, 255);
			break;
			
			case 207: // Ranarr Weed 
			server.model.players.skills.Herblore.idHerb(c, itemId, itemSlot, 25, 257);
			break;
			
			case 3049: // Toadflax 
			server.model.players.skills.Herblore.idHerb(c, itemId, itemSlot, 30, 2998);
			break;
			
			case 209: // Irit Leaf 
			server.model.players.skills.Herblore.idHerb(c, itemId, itemSlot, 40, 259);
			break;
			
			case 211: // Avantoe
			server.model.players.skills.Herblore.idHerb(c, itemId, itemSlot, 48, 261);
			break;
			
			case 213: // Kwuarm 
			server.model.players.skills.Herblore.idHerb(c, itemId, itemSlot, 54, 263);
			break;
			
			case 2485: // Snapdragon 
			server.model.players.skills.Herblore.idHerb(c, itemId, itemSlot, 59, 3000);
			break;
			
			case 215: // Cadantine
			server.model.players.skills.Herblore.idHerb(c, itemId, itemSlot, 65, 265);
			break;
			
			case 1531: // Lantadyme
			server.model.players.skills.Herblore.idHerb(c, itemId, itemSlot, 67, 2481);
			break;
			
			case 217: // Dwarf Weed 
			server.model.players.skills.Herblore.idHerb(c, itemId, itemSlot, 70, 267);
			break;
			
			case 1525: // Torstol
			server.model.players.skills.Herblore.idHerb(c, itemId, itemSlot, 75, 269);
			break;
			
			//End Herblore
							
			case 526:
			c.getPA().buryBone(5, 1600, itemId, itemSlot);
			break;
			
			case 532:
			c.getPA().buryBone(10, 1600, itemId, itemSlot);
			break;
			
			case 536:
			c.getPA().buryBone(15, 1600, itemId, itemSlot);
			break;
			
		}
	}

}
