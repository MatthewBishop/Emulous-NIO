package server.model.players.packets;

import server.*;
import server.util.*;
import server.model.players.*;

/**
 * Private messaging, friends etc
 **/
public class PrivateMessaging implements PacketType {

	public final int ADD_FRIEND = 188, SEND_PM = 126, REMOVE_FRIEND = 215, CHANGE_PM_STATUS = 95;
	@Override
	public void processPacket(Client c, int packetType, int packetSize) {
		switch(packetType) {
		
			case ADD_FRIEND:
			c.friendUpdate = true;
			long friendToAdd = c.getInStream().readQWord();
			boolean canAdd = true;

			for (int i1 = 0; i1 < c.friends.length; i1++) {
				if (c.friends[i1] != 0 && c.friends[i1] == friendToAdd) {
					canAdd = false;
					c.sendMessage(friendToAdd + " is already on your friends list.");
				}
			}
			if (canAdd == true) {
				for (int i1 = 0; i1 < c.friends.length; i1++) {
					if (c.friends[i1] == 0) {
						c.friends[i1] = friendToAdd;
						for (int i2 = 1; i2 < Config.MAX_PLAYERS; i2++) {
							if (Server.playerHandler.players[i2] != null && Server.playerHandler.players[i2].isActive && Misc.playerNameToInt64(Server.playerHandler.players[i2].playerName)== friendToAdd) {
								Client o = (Client)Server.playerHandler.players[i2];
								if(o != null) {
									if (c.playerRights >= 2 || Server.playerHandler.players[i2].privateChat == 0 || (Server.playerHandler.players[i2].privateChat == 1 && o.getPA().isInPM(Misc.playerNameToInt64(c.playerName)))) {
										c.getPA().loadPM(friendToAdd, 1);
										break;
									}
								}
							}
						}
						break;
					}
				}
			}
			break;
			
			case SEND_PM:
			long sendMessageToFriendId = c.getInStream().readQWord();
            byte pmchatText[] = new byte[100];
            int pmchatTextSize = (byte) (packetSize - 8);
			c.getInStream().readBytes(pmchatText, pmchatTextSize, 0);
            for (int i1 = 0; i1 < c.friends.length; i1++) {
                if (c.friends[i1] == sendMessageToFriendId) {
                    boolean pmSent = false;

                    for (int i2 = 1; i2 < Config.MAX_PLAYERS; i2++) {
                        if (Server.playerHandler.players[i2] != null && Server.playerHandler.players[i2].isActive && Misc.playerNameToInt64(Server.playerHandler.players[i2].playerName)== sendMessageToFriendId) {
                            Client o = (Client)Server.playerHandler.players[i2];
							if(o != null) {
								if (c.playerRights >= 2 || Server.playerHandler.players[i2].privateChat == 0 || (Server.playerHandler.players[i2].privateChat == 1 && o.getPA().isInPM(Misc.playerNameToInt64(c.playerName)))) {
	                                o.getPA().sendPM(Misc.playerNameToInt64(c.playerName), c.playerRights, pmchatText, pmchatTextSize);
	                                pmSent = true;
	                            }
							}
                            break;
                        }
                    }
                    if (!pmSent) {
						c.sendMessage("That player is currently offline.");
						break;
                    }
                }
            }
            break;	
			
			case REMOVE_FRIEND:
			c.friendUpdate = true;
            long friendToRemove = c.getInStream().readQWord();

            for (int i1 = 0; i1 < c.friends.length; i1++) {
                if (c.friends[i1] == friendToRemove) {
					for (int i2 = 1; i2 < Config.MAX_PLAYERS; i2++) {
						Client o = (Client)Server.playerHandler.players[i2];		
						if(o != null) {
							if(c.friends[i1] == Misc.playerNameToInt64(Server.playerHandler.players[i2].playerName)){
								o.getPA().updatePM(c.playerId, 0);
								break;
							}
						}
					}
					c.friends[i1] = 0;
                    break;
                }
            }
            break;
			
			
			case CHANGE_PM_STATUS:
            int tradeAndCompete = c.getInStream().readUnsignedByte();
            c.privateChat = c.getInStream().readUnsignedByte();
            int publicChat = c.getInStream().readUnsignedByte();
            for (int i1 = 1; i1 < Config.MAX_PLAYERS; i1++) {
			   if (Server.playerHandler.players[i1] != null && Server.playerHandler.players[i1].isActive == true) {
                    Client o = (Client)Server.playerHandler.players[i1];
					if(o != null) {
						o.getPA().updatePM(c.playerId, 1);
					}
                }
            }
            break;
			
		}
		
	}	
}
