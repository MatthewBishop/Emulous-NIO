package server.model.players.packets;

import server.*;
import server.util.*;
import server.model.players.*;

/**
 * Attack Player
 **/
public class AttackPlayer implements PacketType {

	public static final int ATTACK_PLAYER = 73, MAGE_PLAYER = 249;
	@Override
	public void processPacket(Client c, int packetType, int packetSize) {
		c.playerIndex = 0;
		c.npcIndex = 0;
		switch(packetType) {		
			
			/**
			* Attack player
			**/
			case ATTACK_PLAYER:
			c.playerIndex = c.getInStream().readSignedWordBigEndian();
			if(Server.playerHandler.players[c.playerIndex] == null ){
				break;
			}
			
			if(c.respawnTimer > 0) {
				break;
			}
			
			if(!c.getCombat().checkReqs()) {
				break;
			}
			
			c.usingMagic = false;
			boolean usingBow = false;
			boolean usingOtherRangeWeapons = false;
			boolean usingArrows = false;
			for (int bowId : c.BOWS) {
				if(c.playerEquipment[c.playerWeapon] == bowId) {
					usingBow = true;
					for (int arrowId : c.ARROWS) {
						if(c.playerEquipment[c.playerArrows] == arrowId) {
							usingArrows = true;
						}
					}
				}
			}
			for (int otherRangeId : c.OTHER_RANGE_WEAPONS) {
				if(c.playerEquipment[c.playerWeapon] == otherRangeId) {
					usingOtherRangeWeapons = true;
				}
			}
			if(c.duelStatus == 5) {	
				if(c.duelCount > 0) {
					c.sendMessage("The duel hasn't started yet!");
					return;
				}
				if(c.duelRule[9]){
					boolean canUseWeapon = false;
					for(int funWeapon: Config.FUN_WEAPONS) {
						if(c.playerEquipment[c.playerWeapon] == funWeapon) {
							canUseWeapon = true;
						}
					}
					if(!canUseWeapon) {
						c.sendMessage("You can only use fun weapons in this duel!");
						return;
					}
				}
				
				if(c.duelRule[2] && (usingBow || usingOtherRangeWeapons)) {
					c.sendMessage("Range has been disabled in this duel!");
					return;
				}
				if(c.duelRule[3] && (!usingBow && !usingOtherRangeWeapons)) {
					c.sendMessage("Melee has been disabled in this duel!");
					return;
				}
			}
			
			if(usingBow && c.goodDistance(c.getX(), c.getY(), Server.playerHandler.players[c.playerIndex].getX(), Server.playerHandler.players[c.playerIndex].getY(), 6)) {
				c.usingBow = true;
				c.stopMovement();
			}
			
			if(usingOtherRangeWeapons && c.goodDistance(c.getX(), c.getY(), Server.playerHandler.players[c.playerIndex].getX(), Server.playerHandler.players[c.playerIndex].getY(), 4)) {
				c.usingRangeWeapon = true;
				c.stopMovement();
			}
			if(!usingArrows && usingBow && c.playerEquipment[c.playerWeapon] < 4212 &&  c.playerEquipment[c.playerWeapon] > 4223) {
				c.sendMessage("You have run out of arrows!");
				return;
			} 
			if(c.getCombat().correctBowAndArrows() < c.playerEquipment[c.playerArrows] && Config.CORRECT_ARROWS && usingBow) {
				c.sendMessage("You can't use "+c.getItems().getItemName(c.playerEquipment[c.playerArrows]).toLowerCase()+"s with a "+c.getItems().getItemName(c.playerEquipment[c.playerWeapon]).toLowerCase()+".");
				c.playerIndex = 0;
				return;
			}
			c.getCombat().attackPlayer(c.playerIndex);
			break;
			
			
			/**
			* Attack player with magic
			**/
			case MAGE_PLAYER:
			c.usingSpecial = false;
			c.getItems().updateSpecialBar();

			c.playerIndex = c.getInStream().readSignedWordA();
			int castingSpellId = c.getInStream().readSignedWordBigEndian();
			c.usingMagic = false;

			if(Server.playerHandler.players[c.playerIndex] == null ){
				break;
			}

			if(c.respawnTimer > 0) {
				break;
			}
			
			for(int i = 0; i < c.MAGIC_SPELLS.length; i++){
				if(castingSpellId == c.MAGIC_SPELLS[i][0]) {
					c.spellId = i;
					c.usingMagic = true;
					break;
				}
			}
			
			if(!c.getCombat().checkReqs()) {
				break;
			}
			if(c.duelStatus == 5) {	
				if(c.duelCount > 0) {
					c.sendMessage("The duel hasn't started yet!");
					return;
				}
				if(c.duelRule[4]) {
					c.sendMessage("Magic has been disabled in this duel!");
					return;
				}
			}
			
			for(int r = 0; r < c.REDUCE_SPELLS.length; r++){	// reducing spells, confuse etc
				if(Server.playerHandler.players[c.playerIndex].REDUCE_SPELLS[r] == c.MAGIC_SPELLS[c.spellId][0]) {
					if((System.currentTimeMillis() - Server.playerHandler.players[c.playerIndex].reduceSpellDelay[r]) < Server.playerHandler.players[c.playerIndex].REDUCE_SPELL_TIME[r]) {
						c.sendMessage("That player is currently immune to this spell.");
						c.usingMagic = false;
						c.stopMovement();
						c.getCombat().resetPlayerAttack();
					}
					break;
				}			
			}

			
			if(System.currentTimeMillis() - Server.playerHandler.players[c.playerIndex].teleBlockDelay < Config.TELEBLOCK_DELAY) {
				c.sendMessage("That player is currently immune to this spell.");
				c.usingMagic = false;
				c.stopMovement();
				c.getCombat().resetPlayerAttack();
			}
			
			if(!c.getCombat().checkMagicReqs(c.spellId)) {
				c.stopMovement();
				c.getCombat().resetPlayerAttack();
				break;
			}
	 
			if(c.usingMagic) {
				if(c.goodDistance(c.getX(), c.getY(), Server.playerHandler.players[c.playerIndex].getX(), Server.playerHandler.players[c.playerIndex].getY(), 6)) {
					c.stopMovement();
				}
				c.getCombat().attackPlayer(c.playerIndex);
			}
			break;
		
		}
			
		
	}
		
}
