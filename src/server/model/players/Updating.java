package server.model.players;

import server.Config;
import server.Server;
import server.model.items.Item;
import server.model.npcs.NPC;
import server.model.npcs.NPCHandler;
import server.util.Misc;
import server.util.Stream;

public class Updating {

	public static void updatePlayer(Player player, Stream str) {
		Stream updateBlock = new Stream(new byte[Config.BUFFER_SIZE]);
		updateBlock.currentOffset = 0;
		updateThisPlayerMovement(player, str);
		boolean saveChatTextUpdate = player.chatTextUpdateRequired;
		player.chatTextUpdateRequired = false;
		appendPlayerUpdateBlock(player, updateBlock);
		player.chatTextUpdateRequired = saveChatTextUpdate;
		str.writeBits(8, player.playerListSize);
		int size = player.playerListSize;
		player.playerListSize = 0;
		for (int i = 0; i < size; i++) {
			if (!player.didTeleport && !player.playerList[i].didTeleport && player.withinDistance(player.playerList[i])) {
				updatePlayerMovement(player.playerList[i], str);
				appendPlayerUpdateBlock(player.playerList[i], updateBlock);
				player.playerList[player.playerListSize++] = player.playerList[i];
			} else {
				int id = player.playerList[i].playerId;
				player.playerInListBitmap[id >> 3] &= ~(1 << (id & 7));
				str.writeBits(1, 1);
				str.writeBits(2, 3);
			}
		}

		for (int i = 0; i < Config.MAX_PLAYERS; i++) {
			if (Server.playerHandler.players[i] == null || !Server.playerHandler.players[i].isActive || Server.playerHandler.players[i] == player)
				continue;
			int id = Server.playerHandler.players[i].playerId;
			if ((player.playerInListBitmap[id >> 3] & (1 << (id & 7))) != 0)
				continue;
			if (!player.withinDistance(Server.playerHandler.players[i]))
				continue;
			addNewPlayer(player, Server.playerHandler.players[i], str, updateBlock);
		}

		if (updateBlock.currentOffset > 0) {
			str.writeBits(11, 2047);
			str.finishBitAccess();

			str.writeBytes(updateBlock.buffer, updateBlock.currentOffset, 0);
		} else
			str.finishBitAccess();

		str.endFrameVarSizeWord();
	}

	private static void addNewPlayer(Player player, Player other, Stream str, Stream updateBlock) {
		int id = other.playerId;
		player.playerInListBitmap[id >> 3] |= 1 << (id&7);
		player.playerList[player.playerListSize++] = other;
		str.writeBits(11, id);	
		str.writeBits(1, 1);	
		boolean savedFlag = other.appearanceUpdateRequired;
		boolean savedUpdateRequired = other.updateRequired;
		other.appearanceUpdateRequired = true;
		other.updateRequired = true;
		appendPlayerUpdateBlock(other, updateBlock);
		other.appearanceUpdateRequired = savedFlag;
		other.updateRequired = savedUpdateRequired;
		str.writeBits(1, 1);							
		int z = other.absY-player.absY;
		if(z < 0) z += 32;
		str.writeBits(5, z);	
		z = other.absX-player.absX;
		if(z < 0) z += 32;
		str.writeBits(5, z);
	}
	
	private static void appendPlayerUpdateBlock(Player player, Stream str){
		if(!player.updateRequired && !player.chatTextUpdateRequired) return;		// nothing required
		int updateMask = 0;
		if(player.mask100update) {
			updateMask |= 0x100;
		}
		if(player.animationRequest != -1) {
			updateMask |= 8;
		}
		if(player.forcedChatUpdateRequired) {
			updateMask |= 4;
		}
		if(player.chatTextUpdateRequired) {
			updateMask |= 0x80;
		}
		if(player.appearanceUpdateRequired) {
			updateMask |= 0x10;
		}
		if(player.faceUpdateRequired) {
			updateMask |= 1;
		}
		if(player.FocusPointX != -1) { 
			updateMask |= 2;
		}
		if (player.hitUpdateRequired) {
			updateMask |= 0x20;
		}

		if(player.hitUpdateRequired2) {
			updateMask |= 0x200;
		}
		
		if(updateMask >= 0x100) {
			updateMask |= 0x40;	
			str.writeByte(updateMask & 0xFF);
			str.writeByte(updateMask >> 8);
		} else {	
			str.writeByte(updateMask);
		}

		// now writing the various update blocks itself - note that their order crucial
		if(player.mask100update) {   
			appendMask100Update(player, str);
		}
		if(player.animationRequest != -1) {
			appendAnimationRequest(player, str);	
		}
		if(player.forcedChatUpdateRequired) {
			appendForcedChat(player, str);
		}
		if(player.chatTextUpdateRequired) {
			appendPlayerChatText(player, str);
		}
		if(player.faceUpdateRequired) {
			appendFaceUpdate(player, str);
		}
		if(player.appearanceUpdateRequired) { 
			appendPlayerAppearance(player, str);
		}		
		if(player.FocusPointX != -1) { 
			appendSetFocusDestination(player, str);
		}
		if(player.hitUpdateRequired) {
			appendHitUpdate(player, str); 
		}
		if(player.hitUpdateRequired2) {
			appendHitUpdate2(player, str); 
		}
	}
	
	private static void appendMask100Update(Player player, Stream str) {
		str.writeWordBigEndian(player.mask100var1);
        str.writeDWord(player.mask100var2);
    }
	
	private static void appendAnimationRequest(Player player, Stream str) {
		str.writeWordBigEndian((player.animationRequest==-1) ? 65535 : player.animationRequest);
		str.writeByteC(player.animationWaitCycles);
	}
	
	private static void appendForcedChat(Player player, Stream str) {
		str.writeString(player.forcedText);
    }
	
	private static void appendPlayerChatText(Player player, Stream str) {
		str.writeWordBigEndian(((player.chatTextColor&0xFF) << 8) + (player.chatTextEffects&0xFF));
		str.writeByte(player.playerRights);
		str.writeByteC(player.chatTextSize);		
		str.writeBytes_reverse(player.chatText, player.chatTextSize, 0);
	}
	
	private static void appendFaceUpdate(Player player, Stream str) {
		str.writeWordBigEndian(player.face);
	}
	
	private static void appendPlayerAppearance(Player player, Stream str) {
		Stream playerProps = new Stream(new byte[100]);
		playerProps.currentOffset = 0;

		playerProps.writeByte(player.playerAppearance[0]);			
			
		playerProps.writeByte(player.headIcon);
		//playerProps.writeByte(headIconPk);
		//playerProps.writeByte(headIconHints);	
		
		if (player.playerEquipment[player.playerHat] > 1) {
			playerProps.writeWord(0x200 + player.playerEquipment[player.playerHat]);
		} else {
			playerProps.writeByte(0);
		}

		if (player.playerEquipment[player.playerCape] > 1) {
			playerProps.writeWord(0x200 + player.playerEquipment[player.playerCape]);
		} else {
			playerProps.writeByte(0);
		}

		if (player.playerEquipment[player.playerAmulet] > 1) {
			playerProps.writeWord(0x200 + player.playerEquipment[player.playerAmulet]);
		} else {
			playerProps.writeByte(0);
		}

		if (player.playerEquipment[player.playerWeapon] > 1) {
			playerProps.writeWord(0x200 + player.playerEquipment[player.playerWeapon]);
		} else {
			playerProps.writeByte(0);
		}

		if (player.playerEquipment[player.playerChest] > 1) {
			playerProps.writeWord(0x200 + player.playerEquipment[player.playerChest]);
		} else {
			playerProps.writeWord(0x100+player.playerAppearance[2]);
		}
		
		if (player.playerEquipment[player.playerShield] > 1) {
			playerProps.writeWord(0x200 + player.playerEquipment[player.playerShield]);
		} else {
			playerProps.writeByte(0);
		}
		
		if (!Item.isPlate(player.playerEquipment[player.playerChest])) {
			playerProps.writeWord(0x100+player.playerAppearance[3]);
		} else {
			playerProps.writeByte(0);
		}
		
		if (player.playerEquipment[player.playerLegs] > 1) {
			playerProps.writeWord(0x200 + player.playerEquipment[player.playerLegs]);
		} else {
			playerProps.writeWord(0x100+player.playerAppearance[5]);
		}
		
		if (!Item.isFullHelm(player.playerEquipment[player.playerHat]) && !Item.isFullMask(player.playerEquipment[player.playerHat])) {
			playerProps.writeWord(0x100 + player.playerAppearance[1]);		
		} else {
			playerProps.writeByte(0);
		}

		if (player.playerEquipment[player.playerHands] > 1) {
			playerProps.writeWord(0x200 + player.playerEquipment[player.playerHands]);
		} else {
			playerProps.writeWord(0x100+player.playerAppearance[4]);
		}
		
		if (player.playerEquipment[player.playerFeet] > 1) {
			playerProps.writeWord(0x200 + player.playerEquipment[player.playerFeet]);
		} else {
			 playerProps.writeWord(0x100+player.playerAppearance[6]);
		}
			 
		if (!Item.isFullHelm(player.playerEquipment[player.playerHat]) && !Item.isFullMask(player.playerEquipment[player.playerHat]) &&(player.playerAppearance[0] != 1)) {
			playerProps.writeWord(0x100 + player.playerAppearance[7]);
		} else {
			playerProps.writeByte(0);
		}
		
		playerProps.writeByte(player.playerAppearance[8]);	
		playerProps.writeByte(player.playerAppearance[9]);	
		playerProps.writeByte(player.playerAppearance[10]);	
		playerProps.writeByte(player.playerAppearance[11]);	
		playerProps.writeByte(player.playerAppearance[12]);	
		playerProps.writeWord(player.playerStandIndex);		// standAnimIndex
		playerProps.writeWord(player.playerTurnIndex);		// standTurnAnimIndex
		playerProps.writeWord(player.playerWalkIndex);		// walkAnimIndex
		playerProps.writeWord(player.playerTurn180Index);		// turn180AnimIndex
		playerProps.writeWord(player.playerTurn90CWIndex);		// turn90CWAnimIndex
		playerProps.writeWord(player.playerTurn90CCWIndex);		// turn90CCWAnimIndex
		playerProps.writeWord(player.playerRunIndex);		// runAnimIndex	

		playerProps.writeQWord(Misc.playerNameToInt64(player.playerName));

		int mag = (int) ((player.getLevelForXP(player.playerXP[6])) * 1.5);
		int ran = (int) ((player.getLevelForXP(player.playerXP[4])) * 1.5);
		int attstr = (int) ((double) (player.getLevelForXP(player.playerXP[0])) + (double) (player.getLevelForXP(player.playerXP[2])));

		player.combatLevel = 0;
		if (ran > attstr) {
			player.combatLevel = (int) (((player.getLevelForXP(player.playerXP[1])) * 0.25)
					+ ((player.getLevelForXP(player.playerXP[3])) * 0.25)
					+ ((player.getLevelForXP(player.playerXP[5])) * 0.125) + ((player.getLevelForXP(player.playerXP[4])) * 0.4875));
		} else if (mag > attstr) {
			player.combatLevel = (int) (((player.getLevelForXP(player.playerXP[1])) * 0.25)
					+ ((player.getLevelForXP(player.playerXP[3])) * 0.25)
					+ ((player.getLevelForXP(player.playerXP[5])) * 0.125) + ((player.getLevelForXP(player.playerXP[6])) * 0.4875));
		} else {
			player.combatLevel = (int) (((player.getLevelForXP(player.playerXP[1])) * 0.25)
					+ ((player.getLevelForXP(player.playerXP[3])) * 0.25)
					+ ((player.getLevelForXP(player.playerXP[5])) * 0.125)
					+ ((player.getLevelForXP(player.playerXP[0])) * 0.325) + ((player.getLevelForXP(player.playerXP[2])) * 0.325));
		}
		playerProps.writeByte(player.combatLevel);		// combat level		
		playerProps.writeWord(0);		
		str.writeByteC(playerProps.currentOffset);		
		str.writeBytes(playerProps.buffer, playerProps.currentOffset, 0);
 	}
	
	private static void appendSetFocusDestination(Player player, Stream str) {
        str.writeWordBigEndianA(player.FocusPointX);
        str.writeWordBigEndian(player.FocusPointY);
    }
	
	private static void appendHitUpdate(Player player, Stream str) {
		try {
			str.writeByte(player.hitDiff); // What the perseon got 'hit' for
			if (player.hitDiff > 0) {
				str.writeByteA(1); // 0: red hitting - 1: blue hitting
			} else {
				str.writeByteA(0); // 0: red hitting - 1: blue hitting
			}
			if (player.playerLevel[3] <= 0) {
				player.playerLevel[3] = 0;
				player.isDead = true;	
			}
			str.writeByteC(player.playerLevel[3]); // Their current hp, for HP bar
			str.writeByte(player.getLevelForXP(player.playerXP[3])); // Their max hp, for HP bar
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	
	private static void appendHitUpdate2(Player player, Stream str) {
		try {
			str.writeByte(player.hitDiff2); // What the perseon got 'hit' for
			if (player.hitDiff2 > 0) {
				str.writeByteS(1); // 0: red hitting - 1: blue hitting
			} else {
				str.writeByteS(0); // 0: red hitting - 1: blue hitting
			}
			if (player.playerLevel[3] <= 0) {
				player.playerLevel[3] = 0;
				player.isDead = true;	
			}
			str.writeByte(player.playerLevel[3]); // Their current hp, for HP bar
			str.writeByteC(player.getLevelForXP(player.playerXP[3])); // Their max hp, for HP bar
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	private static void updateThisPlayerMovement(Player player, Stream str) {
		if(player.mapRegionDidChange) {
			str.createFrame(73);
			str.writeWordA(player.mapRegionX+6);	
			str.writeWord(player.mapRegionY+6);
		}

		if(player.didTeleport) {
			str.createFrameVarSizeWord(81);
			str.initBitAccess();
			str.writeBits(1, 1);
			str.writeBits(2, 3);			
			str.writeBits(2, player.heightLevel);
			str.writeBits(1, 1);			
			str.writeBits(1, (player.updateRequired) ? 1 : 0);
			str.writeBits(7, player.currentY);
			str.writeBits(7, player.currentX);
			return ;
		}
		

		if(player.dir1 == -1) {
			// don't have to update the character position, because we're just standing
			str.createFrameVarSizeWord(81);
			str.initBitAccess();
			player.isMoving = false;
			if(player.updateRequired) {
				// tell client there's an update block appended at the end
				str.writeBits(1, 1);
				str.writeBits(2, 0);
			} else {
				str.writeBits(1, 0);
			}
			if (player.DirectionCount < 50) {
				player.DirectionCount++;
			}
		} else {
			player.DirectionCount = 0;
			str.createFrameVarSizeWord(81);
			str.initBitAccess();
			str.writeBits(1, 1);

			if(player.dir2 == -1) {
				player.isMoving = true;
				str.writeBits(2, 1);		
				str.writeBits(3, Misc.xlateDirectionToClient[player.dir1]);
				if(player.updateRequired) str.writeBits(1, 1);		
				else str.writeBits(1, 0);
			}
			else {
				player.isMoving = true;
				str.writeBits(2, 2);		
				str.writeBits(3, Misc.xlateDirectionToClient[player.dir1]);
				str.writeBits(3, Misc.xlateDirectionToClient[player.dir2]);
				if(player.updateRequired) str.writeBits(1, 1);		
				else str.writeBits(1, 0);
			}
		}

	}

	
	private static void updatePlayerMovement(Player player, Stream str) {
		if(player.dir1 == -1) {
			if(player.updateRequired || player.chatTextUpdateRequired) {
				
				str.writeBits(1, 1);
				str.writeBits(2, 0);
			}
			else str.writeBits(1, 0);
		}
		else if(player.dir2 == -1) {
			
			str.writeBits(1, 1);
			str.writeBits(2, 1);
			str.writeBits(3, Misc.xlateDirectionToClient[player.dir1]);
			str.writeBits(1, (player.updateRequired || player.chatTextUpdateRequired) ? 1: 0);
		}
		else {
			
			str.writeBits(1, 1);
			str.writeBits(2, 2);
			str.writeBits(3, Misc.xlateDirectionToClient[player.dir1]);
			str.writeBits(3, Misc.xlateDirectionToClient[player.dir2]);
			str.writeBits(1, (player.updateRequired || player.chatTextUpdateRequired) ? 1: 0);
		}
	}
	
	public static void updateNPC(Player player, Stream str) {
		Stream updateBlock = new Stream(new byte[Config.BUFFER_SIZE]);
		updateBlock.currentOffset = 0;

		str.createFrameVarSizeWord(65);
		str.initBitAccess();

		str.writeBits(8, player.npcListSize);
		int size = player.npcListSize;
		player.npcListSize = 0;
		for (int i = 0; i < size; i++) {
			if (player.RebuildNPCList == false && player.withinDistance(player.npcList[i]) == true) {
				updateNPCMovement(player.npcList[i], str);
				appendNPCUpdateBlock(player.npcList[i], updateBlock);
				player.npcList[player.npcListSize++] = player.npcList[i];
			} else {
				int id = player.npcList[i].npcId;
				player.npcInListBitmap[id >> 3] &= ~(1 << (id & 7));
				str.writeBits(1, 1);
				str.writeBits(2, 3);
			}
		}

		for (int i = 0; i < NPCHandler.maxNPCs; i++) {
			if (Server.npcHandler.npcs[i] != null) {
				int id = Server.npcHandler.npcs[i].npcId;
				if (player.RebuildNPCList == false && (player.npcInListBitmap[id >> 3] & (1 << (id & 7))) != 0) {

				} else if (player.withinDistance(Server.npcHandler.npcs[i]) == false) {

				} else {
					addNewNPC(player, Server.npcHandler.npcs[i], str, updateBlock);
				}
			}
		}

		player.RebuildNPCList = false;

		if (updateBlock.currentOffset > 0) {
			str.writeBits(14, 16383);
			str.finishBitAccess();
			str.writeBytes(updateBlock.buffer, updateBlock.currentOffset, 0);
		} else {
			str.finishBitAccess();
		}
		str.endFrameVarSizeWord();
	}

	private static void addNewNPC(Player player, NPC npc, Stream str, Stream updateBlock) {
		int id = npc.npcId;
		player.npcInListBitmap[id >> 3] |= 1 << (id & 7);
		player.npcList[player.npcListSize++] = npc;
		str.writeBits(14, id);
		int z = npc.absY - player.absY;
		if (z < 0)
			z += 32;
		str.writeBits(5, z);
		z = npc.absX - player.absX;
		if (z < 0)
			z += 32;
		str.writeBits(5, z);
		str.writeBits(1, 0);
		str.writeBits(12, npc.npcType);
		boolean savedUpdateRequired = npc.updateRequired;
		npc.updateRequired = true;
		appendNPCUpdateBlock(npc, updateBlock);
		npc.updateRequired = savedUpdateRequired;
		str.writeBits(1, 1);
	}
	
	private static void updateNPCMovement(NPC npc, Stream str) {
		if (npc.direction == -1) {
			
			if (npc.updateRequired) {
				
				str.writeBits(1, 1);
				str.writeBits(2, 0);
			} else {
				str.writeBits(1, 0);
			}
		} else {
			
			str.writeBits(1, 1);
			str.writeBits(2, 1);		
			str.writeBits(3, Misc.xlateDirectionToClient[npc.direction]);
			if (npc.updateRequired) {
				str.writeBits(1, 1);		
			} else {
				str.writeBits(1, 0);
			}
		}
	}
	
	private static void appendNPCUpdateBlock(NPC npc, Stream str) {
		if(!npc.updateRequired) return ;		
		int updateMask = 0;
		if(npc.animUpdateRequired) updateMask |= 0x10; 
		if(npc.hitUpdateRequired2) updateMask |= 8;
		if(npc.mask80update) updateMask |= 0x80;
		if(npc.dirUpdateRequired) updateMask |= 0x20;
		if(npc.forcedChatRequired) updateMask |= 1;
		if(npc.hitUpdateRequired) updateMask |= 0x40;		
		if(npc.FocusPointX != -1) updateMask |= 4;		
			
		str.writeByte(updateMask);
				
		if (npc.animUpdateRequired) appendAnimUpdate(npc, str);
		if (npc.hitUpdateRequired2) appendHitUpdate2(npc, str);
		if (npc.mask80update)       appendMask80Update(npc, str);
		if (npc.dirUpdateRequired)  appendFaceEntity(npc, str);
		if(npc.forcedChatRequired) {
			str.writeString(npc.forcedText);
		}
		if (npc.hitUpdateRequired)  appendHitUpdate(npc, str);
		if(npc.FocusPointX != -1) 
			appendSetFocusDestination(npc, str);
		
	}
	
    private static void appendMask80Update(NPC npc, Stream str) {
		str.writeWord(npc.mask80var1);
	    str.writeDWord(npc.mask80var2);
    }
    
	private static void appendAnimUpdate(NPC npc, Stream str) {
		str.writeWordBigEndian(npc.animNumber);
		str.writeByte(1);
	}
	
	private static void appendSetFocusDestination(NPC npc, Stream str) {
        str.writeWordBigEndian(npc.FocusPointX);
        str.writeWordBigEndian(npc.FocusPointY);
    }
	
	private static void appendFaceEntity(NPC npc, Stream str) {
		str.writeWord(npc.face);
	}
	
	private static void appendFaceToUpdate(NPC npc, Stream str) {
		str.writeWordBigEndian(npc.viewX);
		str.writeWordBigEndian(npc.viewY);
}
	
	private static void appendHitUpdate(NPC npc, Stream str) {		
		if (npc.HP <= 0) {
			npc.isDead = true;
		}
		str.writeByteC(npc.hitDiff); 
		if (npc.hitDiff > 0) {
			str.writeByteS(1); 
		} else {
			str.writeByteS(0); 
		}	
		str.writeByteS(npc.HP); 
		str.writeByteC(npc.MaxHP); 	
	}
	
	private static void appendHitUpdate2(NPC npc, Stream str) {		
		if (npc.HP <= 0) {
			npc.isDead = true;
		}
		str.writeByteA(npc.hitDiff2); 
		if (npc.hitDiff2 > 0) {
			str.writeByteC(1); 
		} else {
			str.writeByteC(0); 
		}	
		str.writeByteA(npc.HP); 
		str.writeByte(npc.MaxHP); 	
	}
}
