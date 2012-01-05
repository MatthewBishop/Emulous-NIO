package server.model.players;

import server.*;
import server.util.*;
import server.model.players.*;
import server.model.objects.*;

public class ActionHandler {
	
	private Client c;
	public ActionHandler(Client Client) {
		this.c = Client;
	}
	
	
	public void firstClickObject(int objectType) {
		c.clickObjectType = 0;
		switch(objectType) {
			
			//barrow
			case 6707: // verac
			c.getPA().movePlayer(3556, 3298, 0);
			break;
			
			case 6823:
			if(server.model.minigames.Barrows.selectCoffin(c, objectType)) {
				return;
			}
			if(c.barrowsNpcs[0][1] == 0) {
				Server.npcHandler.spawnNpc(c, 2030, c.getX(), c.getY()-1, -1, 0, 120, 7, 70, 70, true);
				c.barrowsNpcs[0][1] = 1;
			} else {
				c.sendMessage("You have already searched in this sarcophagus.");
			}
			break;

			case 6706: // torag 
			c.getPA().movePlayer(3553, 3283, 0);
			break;
			
			case 6772:
			if(server.model.minigames.Barrows.selectCoffin(c, objectType)) {
				return;
			}
			if(c.barrowsNpcs[1][1] == 0) {
				Server.npcHandler.spawnNpc(c, 2029, c.getX()+1, c.getY(), -1, 0, 120, 6, 70, 70, true);
				c.barrowsNpcs[1][1] = 1;
			} else {
				c.sendMessage("You have already searched in this sarcophagus.");
			}
			break;
			
			
			case 6705: // karil stairs
			c.getPA().movePlayer(3565, 3276, 0);
			break;
			case 6822:
			if(server.model.minigames.Barrows.selectCoffin(c, objectType)) {
				return;
			}
			if(c.barrowsNpcs[2][1] == 0) {
				Server.npcHandler.spawnNpc(c, 2028, c.getX(), c.getY()-1, -1, 0, 90, 6, 50, 50, true);
				c.barrowsNpcs[2][1] = 1;
			} else {
				c.sendMessage("You have already searched in this sarcophagus.");
			}
			break;
			
			case 6704: // guthan stairs
			c.getPA().movePlayer(3578, 3284, 0);
			break;
			case 6773:
			if(server.model.minigames.Barrows.selectCoffin(c, objectType)) {
				return;
			}
			if(c.barrowsNpcs[3][1] == 0) {
				Server.npcHandler.spawnNpc(c, 2027, c.getX(), c.getY()-1, -1, 0, 120, 7, 70, 70, true);
				c.barrowsNpcs[3][1] = 1;
			} else {
				c.sendMessage("You have already searched in this sarcophagus.");
			}
			break;
			
			case 6703: // dharok stairs
			c.getPA().movePlayer(3574, 3298, 0);
			break;
			case 6771:
			if(server.model.minigames.Barrows.selectCoffin(c, objectType)) {
				return;
			}
			if(c.barrowsNpcs[4][1] == 0) {
				Server.npcHandler.spawnNpc(c, 2026, c.getX(), c.getY()-1, -1, 0, 120, 7, 80, 80, true);
				c.barrowsNpcs[4][1] = 1;
			} else {
				c.sendMessage("You have already searched in this sarcophagus.");
			}
			break;
			
			case 6702: // ahrim stairs
			c.getPA().movePlayer(3565, 3290, 0);
			break;
			case 6821:
			if(server.model.minigames.Barrows.selectCoffin(c, objectType)) {
				return;
			}
			if(c.barrowsNpcs[5][1] == 0) {
				Server.npcHandler.spawnNpc(c, 2025, c.getX(), c.getY()-1, -1, 0, 90, 5, 50, 50, true);
				c.barrowsNpcs[5][1] = 1;
			} else {
				c.sendMessage("You have already searched in this sarcophagus.");
			}
			break;
			
			case 1276: // tree example
			c.sendMessage("You chop the tree.");
			Objects stump = new Objects(1343, c.objectX, c.objectY, 0, -1, 10, 0);
			Server.objectHandler.addObject(stump);
			Server.objectHandler.placeObject(stump);
			Objects tree = new Objects(c.objectId, c.objectX, c.objectY, 0, -1, 10, 10);
			Server.objectHandler.addObject(tree);
			break;
			
			// DOORS
			case 1530:
			case 1531:
			case 1533:
			case 1534:
			case 11712:
			case 11711:
			case 11707:
			case 11708:
			case 6725:
			case 6726:
			case 3198:
			case 3197:
			Server.objectHandler.doorHandling(objectType, c.objectX, c.objectY, 0);	
			break;
			
			case 3203: //dueling forfeit
			Client o = (Client) Server.playerHandler.players[c.duelingWith];				
			if(o == null) {
				c.getTradeAndDuel().resetDuel();
				c.getPA().movePlayer(Config.DUELING_RESPAWN_X+(Misc.random(Config.RANDOM_DUELING_RESPAWN)), Config.DUELING_RESPAWN_Y+(Misc.random(Config.RANDOM_DUELING_RESPAWN)), 0);
				break;
			}
			if(c.duelRule[0]) {
				c.sendMessage("Forfeiting the duel has been disabled!");
				break;
			}
			if(o != null) {
				o.getPA().movePlayer(Config.DUELING_RESPAWN_X+(Misc.random(Config.RANDOM_DUELING_RESPAWN)), Config.DUELING_RESPAWN_Y+(Misc.random(Config.RANDOM_DUELING_RESPAWN)), 0);
				c.getPA().movePlayer(Config.DUELING_RESPAWN_X+(Misc.random(Config.RANDOM_DUELING_RESPAWN)), Config.DUELING_RESPAWN_Y+(Misc.random(Config.RANDOM_DUELING_RESPAWN)), 0);
				o.duelStatus = 6;
				o.getTradeAndDuel().duelVictory();
				c.getTradeAndDuel().resetDuel();
				c.getTradeAndDuel().resetDuelItems();
				o.sendMessage("The other player has forfeited the duel!");
				c.sendMessage("You forfeit the duel!");
				break;
			}
			
			break;
			
			case 3193:
			case 2213:
			c.getPA().openUpBank();
			break;
			
			case 409:
			if(c.playerLevel[5] < c.getPA().getLevelForXP(c.playerXP[5])) {
				c.startAnimation(645);
				c.playerLevel[5] = c.getPA().getLevelForXP(c.playerXP[5]);
				c.sendMessage("You recharge your prayer points.");
				c.getPA().refreshSkill(5);
			} else {
				c.sendMessage("You already have full prayer points.");
			}
			break;

		}
	}
	
	public void secondClickObject(int objectType) {
		c.clickObjectType = 0;
		switch(objectType) {
			case 2213:
			c.getPA().openUpBank();
			break;

		}
	}
	
	
	public void thirdClickObject(int objectType) {
		c.clickObjectType = 0;
		switch(objectType) {

		}
	}
	
	public void firstClickNpc(int npcType) {
		c.clickNpcType = 0;
		switch(npcType) {

		}
	}
	
	public void secondClickNpc(int npcType) {
		c.clickNpcType = 0;
		switch(npcType) {
			case 522:
			c.getShops().openShop(1);
			break;

		}
	}
	
	
	

}