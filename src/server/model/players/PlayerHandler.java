package server.model.players;

// crash-patched by blakeman8192

import java.io.*;
import server.util.*;
import server.model.npcs.*;
import server.*;
public class PlayerHandler{

	
	
	public Player players[] = new Player[Config.MAX_PLAYERS];
	public int playerSlotSearchStart = 1;			
	
	public static String messageToAll = "";
	public static int playerCount=0;
	public static String playersCurrentlyOn[] = new String[Config.MAX_PLAYERS];

	public PlayerHandler() {
		for(int i = 0; i < Config.MAX_PLAYERS; i++) {
			players[i] = null;
		}
	}

	public void addClient(int slot, Client newClient) {
		if(newClient == null) 
			return;
		players[slot] = newClient;
		players[slot].playerId = slot;
		playerSlotSearchStart = slot + 1;
		if(playerSlotSearchStart > Config.MAX_PLAYERS) playerSlotSearchStart = 1;
	}
	
	public int getFreeSlot() {
		int slot = -1, i = 1;
		do {
			if(players[i] == null) {
				slot = i;
				break;
			}
			i++;
			if(i >= Config.MAX_PLAYERS) i = 0;		// wrap around
		} while(i <= Config.MAX_PLAYERS);
		return slot;
	}
	
	public void destruct() {
		for(int i = 0; i < Config.MAX_PLAYERS; i++) {
			if(players[i] == null) continue;
			players[i].destruct();
			players[i] = null;
		}
	}

	public static int getPlayerCount() {
		return Thread.activeCount()-2;
	}

	public void updatePlayerNames(){
		for(int i = 0; i < Config.MAX_PLAYERS; i++) {
			if(players[i] != null) {
				playersCurrentlyOn[i] = players[i].playerName;
			} else {
				playersCurrentlyOn[i] = "";
			}
		}
	}

	public static boolean isPlayerOn(String playerName){
		for(int i = 0; i < Config.MAX_PLAYERS; i++) {
			if(playersCurrentlyOn[i] != null){
				if(playersCurrentlyOn[i].equalsIgnoreCase(playerName)) {
					return true;
				}
			}
		}
		return false;
	}

	public void process() {
		updatePlayerNames();
		if (messageToAll.length() > 0) {
			int msgTo=1;
			do {
				if(players[msgTo] != null) {
					players[msgTo].globalMessage=messageToAll;
				}
				msgTo++;
			} while(msgTo < Config.MAX_PLAYERS);
			messageToAll="";
		}

		for(int i = 0; i < Config.MAX_PLAYERS; i++) {
			try {
			if(players[i] == null || !players[i].isActive) continue;		
			players[i].process();
			players[i].postProcessing();
			players[i].getNextPlayerMovement();
			
			
			if(players[i].disconnected) {
				if(players[i].inTrade) {
					Client o = (Client) Server.playerHandler.players[players[i].tradeWith];
					if(o != null) {
						o.getTradeAndDuel().declineTrade();
					}
				}
				if(players[i].duelStatus == 5) {
					Client o = (Client) Server.playerHandler.players[players[i].duelingWith];
					if(o != null) {
						o.getTradeAndDuel().duelVictory();
					}
				} else if (players[i].duelStatus <= 4 && players[i].duelStatus >= 1) {
					Client o = (Client) Server.playerHandler.players[players[i].duelingWith];
					if(o != null) {
						o.getTradeAndDuel().declineDuel();
					}
				}
				if(PlayerSave.saveGame(players[i])){ System.out.println("Game saved for player "+players[i].playerName); } else { System.out.println("Could not save for "+players[i].playerName); };
				removePlayer(players[i]);
				players[i] = null;
			}
			} catch (Exception ex) {
				System.out.println("Crash avoided! Disconnecting player: " + players[i].playerName);
				players[i].disconnected = true;
			}
		}

		
		for(int i = 0; i < Config.MAX_PLAYERS; i++) {
			try {
			if(players[i] == null || !players[i].isActive) continue;

			if(players[i].disconnected) {
				if(players[i].inTrade) {
					Client o = (Client) Server.playerHandler.players[players[i].tradeWith];
					if(o != null) {
						o.getTradeAndDuel().declineTrade();
					}
				}
				if(players[i].duelStatus == 5) {
					Client o1 = (Client) Server.playerHandler.players[players[i].duelingWith];
					if(o1 != null) {
						o1.getTradeAndDuel().duelVictory();
					}
				} else if (players[i].duelStatus <= 4 && players[i].duelStatus >= 1) {
					Client o1 = (Client) Server.playerHandler.players[players[i].duelingWith];
					if(o1 != null) {
						o1.getTradeAndDuel().declineDuel();
					}
				}
				
				
				if(PlayerSave.saveGame(players[i])){ System.out.println("Game saved for player "+players[i].playerName); } else { System.out.println("Could not save for "+players[i].playerName); };
				removePlayer(players[i]);
				players[i] = null;
			}
			else {

					players[i].update();
			}
			} catch (Exception ex) {
				System.out.println("Crash avoided! Disconnecting player: " + players[i].playerName);
				players[i].disconnected = true;
			}
		}

		
		for(int i = 0; i < Config.MAX_PLAYERS; i++) {
		try {
			if(players[i] == null || !players[i].isActive) continue;

			players[i].clearUpdateFlags();
		} catch (Exception ex) {
			System.out.println("Crash avoided! Disconnecting player: " + players[i].playerName);
			players[i].disconnected = true;
		}
		}
	}
	public void updateNPC(Player plr, Stream str) {
		updateBlock.currentOffset = 0;
		
		str.createFrameVarSizeWord(65);
		str.initBitAccess();
		
		str.writeBits(8, plr.npcListSize);
		int size = plr.npcListSize;
		plr.npcListSize = 0;
		for(int i = 0; i < size; i++) {
			if(plr.RebuildNPCList == false && plr.withinDistance(plr.npcList[i]) == true) {
				plr.npcList[i].updateNPCMovement(str);
				plr.npcList[i].appendNPCUpdateBlock(updateBlock);
				plr.npcList[plr.npcListSize++] = plr.npcList[i];
			} else {
				int id = plr.npcList[i].npcId;
				plr.npcInListBitmap[id>>3] &= ~(1 << (id&7));		
				str.writeBits(1, 1);
				str.writeBits(2, 3);		
			}
		}

		
		for(int i = 0; i < NPCHandler.maxNPCs; i++) {
			if(Server.npcHandler.npcs[i] != null) {
				int id = Server.npcHandler.npcs[i].npcId;
				if (plr.RebuildNPCList == false && (plr.npcInListBitmap[id>>3]&(1 << (id&7))) != 0) {
					
				} else if (plr.withinDistance(Server.npcHandler.npcs[i]) == false) {
					
				} else {
					plr.addNewNPC(Server.npcHandler.npcs[i], str, updateBlock);
				}
			}
		}
		
		plr.RebuildNPCList = false;

		if(updateBlock.currentOffset > 0) {
			str.writeBits(14, 16383);	
			str.finishBitAccess();
			str.writeBytes(updateBlock.buffer, updateBlock.currentOffset, 0);
		} else {
			str.finishBitAccess();
		}
		str.endFrameVarSizeWord();
	}

	private Stream updateBlock = new Stream(new byte[Config.BUFFER_SIZE]);
	
	public void updatePlayer(Player plr, Stream str) {
		updateBlock.currentOffset = 0;
		plr.updateThisPlayerMovement(str);		
		boolean saveChatTextUpdate = plr.chatTextUpdateRequired;
		plr.chatTextUpdateRequired = false;
		plr.appendPlayerUpdateBlock(updateBlock);
		plr.chatTextUpdateRequired = saveChatTextUpdate;
		str.writeBits(8, plr.playerListSize);
		int size = plr.playerListSize;
		plr.playerListSize = 0;	
		for(int i = 0; i < size; i++) {			
			if(!plr.didTeleport && !plr.playerList[i].didTeleport && plr.withinDistance(plr.playerList[i])) {
				plr.playerList[i].updatePlayerMovement(str);
				plr.playerList[i].appendPlayerUpdateBlock(updateBlock);
				plr.playerList[plr.playerListSize++] = plr.playerList[i];
			} else {
				int id = plr.playerList[i].playerId;
				plr.playerInListBitmap[id>>3] &= ~(1 << (id&7));
				str.writeBits(1, 1);
				str.writeBits(2, 3);
			}
		}
	
		for(int i = 0; i < Config.MAX_PLAYERS; i++) {
			if(players[i] == null || !players[i].isActive || players[i] == plr) continue;
			int id = players[i].playerId;
			if((plr.playerInListBitmap[id>>3]&(1 << (id&7))) != 0) continue;	
			if(!plr.withinDistance(players[i])) continue;		
			plr.addNewPlayer(players[i], str, updateBlock);
		}

		if(updateBlock.currentOffset > 0) {
			str.writeBits(11, 2047);	
			str.finishBitAccess();

			
			str.writeBytes(updateBlock.buffer, updateBlock.currentOffset, 0);
		}
		else str.finishBitAccess();

		str.endFrameVarSizeWord();
	}

	private void removePlayer(Player plr) {	
		if(plr.privateChat != 2) { 
			for(int i = 1; i < Config.MAX_PLAYERS; i++) {
				if (players[i] == null || players[i].isActive == false) continue;
				Client o = (Client)Server.playerHandler.players[i];
				if(o != null) {
					o.getPA().updatePM(plr.playerId, 0);
				}
			}
		}
		plr.destruct();
	}

	
}
