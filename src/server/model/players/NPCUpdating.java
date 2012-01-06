package server.model.players;

import java.util.Iterator;

import server.Config;
import server.Server;
import server.model.npcs.NPC;
import server.model.npcs.NPCHandler;
import server.util.Misc;
import server.util.Stream;

public class NPCUpdating {
	public static void updateNPC(Player player, Stream str) {
		Stream updateBlock = new Stream(new byte[Config.BUFFER_SIZE]);
		updateBlock.currentOffset = 0;
		str.createFrameVarSizeWord(65);
		str.initBitAccess();
		str.writeBits(8, player.localNpcs.size());
		for(Iterator<NPC> it$ = player.localNpcs.iterator(); it$.hasNext();) {
			NPC npc = it$.next();
			if (player.withinDistance(npc)) {
				updateNPCMovement(npc, str);
				appendNPCUpdateBlock(npc, updateBlock);
			} else {
				it$.remove();
				str.writeBits(1, 1);
				str.writeBits(2, 3);
			}
		}
		for (int i = 0; i < NPCHandler.maxNPCs; i++) {
			if(player.localNpcs.size() >= 255) {
				break;
			}
			if (Server.npcHandler.npcs[i] == null)
				continue;
			if (player.localNpcs.contains(Server.npcHandler.npcs[i])) 
				continue;
			if (!player.withinDistance(Server.npcHandler.npcs[i]))
				continue;
			player.localNpcs.add(Server.npcHandler.npcs[i]);
			addNewNPC(player, Server.npcHandler.npcs[i], str);
			appendNPCUpdateBlock(Server.npcHandler.npcs[i], updateBlock);
		}
		if (updateBlock.currentOffset > 0) {
			str.writeBits(14, 16383);
			str.finishBitAccess();
			str.writeBytes(updateBlock.buffer, updateBlock.currentOffset, 0);
		} else {
			str.finishBitAccess();
		}
		str.endFrameVarSizeWord();
	}

	private static void addNewNPC(Player player, NPC npc, Stream str) {
		str.writeBits(14, npc.npcId);
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
		str.writeBits(1, npc.updateRequired ? 1 : 0);
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
