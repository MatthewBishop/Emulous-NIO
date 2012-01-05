package server.model.players.packets;

import server.util.*;
import server.*;
import server.model.players.*;

/**
 * Click NPC
 */
public class ClickNPC implements PacketType {
	public static final int ATTACK_NPC = 72, MAGE_NPC = 131, FIRST_CLICK = 155, SECOND_CLICK = 17;
	@Override
	public void processPacket(Client c, int packetType, int packetSize) {
		c.npcIndex = 0;
		c.playerIndex = 0;
		switch(packetType) {
			
			/**
			* Attack npc melee or range
			**/
			case ATTACK_NPC:
			c.npcIndex = c.getInStream().readUnsignedWordA();
			
			if(Server.npcHandler.npcs[c.npcIndex] == null){
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
			
			if(usingBow && c.goodDistance(c.getX(), c.getY(), Server.npcHandler.npcs[c.npcIndex].getX(), Server.npcHandler.npcs[c.npcIndex].getY(), 6)) {
				c.stopMovement();
			}
			
			if(usingOtherRangeWeapons && c.goodDistance(c.getX(), c.getY(), Server.npcHandler.npcs[c.npcIndex].getX(), Server.npcHandler.npcs[c.npcIndex].getY(), 4)) {
				c.stopMovement();
			}
			if(!usingArrows && usingBow && c.playerEquipment[c.playerWeapon] < 4212 &&  c.playerEquipment[c.playerWeapon] > 4223) {
				c.sendMessage("You have run out of arrows!");
				break;
			} 
			if(c.getCombat().correctBowAndArrows() < c.playerEquipment[c.playerArrows] && Config.CORRECT_ARROWS && usingBow) {
				c.sendMessage("You can't use "+c.getItems().getItemName(c.playerEquipment[c.playerArrows]).toLowerCase()+"s with a "+c.getItems().getItemName(c.playerEquipment[c.playerWeapon]).toLowerCase()+".");
				c.npcIndex = 0;
				break;
			}
			
			if(c.npcDroppingItems) {
				Misc.println("dropping");
				break;
			}
			c.getCombat().attackNpc(c.npcIndex);
			
			break;
			
			/**
			* Attack npc with magic
			**/
			case MAGE_NPC:
			c.usingSpecial = false;
			c.getItems().updateSpecialBar();
			
			c.npcIndex = c.getInStream().readSignedWordBigEndianA();
			int castingSpellId = c.getInStream().readSignedWordA();
			c.usingMagic = false;
			
			if(Server.npcHandler.npcs[c.npcIndex] == null ){
				break;
			}
			
			if(Server.npcHandler.npcs[c.npcIndex].MaxHP == 0){
				c.sendMessage("You can't attack this npc.");
				break;
			}
			
			for(int i = 0; i < c.MAGIC_SPELLS.length; i++){
				if(castingSpellId == c.MAGIC_SPELLS[i][0]) {
					c.spellId = i;
					c.usingMagic = true;
					break;
				}
			}
			if(castingSpellId == 1171) { // crumble undead
				for (int npc : Config.UNDEAD_NPCS) {
					if(Server.npcHandler.npcs[c.npcIndex].npcType != npc) {
					 c.sendMessage("You can only attack undead monsters with this spell.");
					 c.usingMagic = false;
					 c.stopMovement();
					 break;
					}
				}
			}
			if(!c.getCombat().checkMagicReqs(c.spellId)) {
				c.stopMovement();
				break;
			}
			
			if(c.usingMagic) {
				if(c.goodDistance(c.getX(), c.getY(), Server.npcHandler.npcs[c.npcIndex].getX(), Server.npcHandler.npcs[c.npcIndex].getY(), 6)) {
					c.stopMovement();
				}
				c.getCombat().attackNpc(c.npcIndex);
			}
	
			break;
			
			case FIRST_CLICK:
			c.npcIndex = (Misc.hexToInt(c.getInStream().buffer, 0, packetSize) / 1000);
			c.npcType = Server.npcHandler.npcs[c.npcIndex].npcType;
			if(c.goodDistance(Server.npcHandler.npcs[c.npcIndex].getX(), Server.npcHandler.npcs[c.npcIndex].getY(), c.getX(), c.getY(), 1)) {
				c.getActions().firstClickNpc(c.npcType);	
			} else {
				c.clickNpcType = 1;
			}
			break;
			
			case SECOND_CLICK:
			c.npcIndex = ((Misc.hexToInt(c.getInStream().buffer, 0, packetSize) / 1000) - 128);
			c.npcType = Server.npcHandler.npcs[c.npcIndex].npcType;
			if(c.goodDistance(Server.npcHandler.npcs[c.npcIndex].getX(), Server.npcHandler.npcs[c.npcIndex].getY(), c.getX(), c.getY(), 1)) {
				c.getActions().secondClickNpc(c.npcType);	
			} else {
				c.clickNpcType = 2;
			}
			break;
			
		}

	}
}
