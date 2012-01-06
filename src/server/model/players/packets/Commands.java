package server.model.players.packets;

import server.util.*;
import server.*;
import server.model.players.*;


/**
 * Commands
 **/
public class Commands implements PacketType {

	
	@Override
	public void processPacket(Client c, int packetType, int packetSize) {
	String playerCommand = c.getInStream().readString();
	Misc.println("playerCommand: "+playerCommand);
	
		if(c.playerRights >= 0) {
		
			if (playerCommand.equalsIgnoreCase("switch")) {
				if(c.playerMagicBook == 1) {
					c.sendMessage("You switch to modern magic.");
					c.setSidebarInterface(6, 1151);
					c.playerMagicBook = 0;
				} else {
					c.sendMessage("You switch to ancient magic.");
					c.setSidebarInterface(6, 12855);
					c.playerMagicBook = 1;
				}
			}
			
			if (playerCommand.equalsIgnoreCase("players")) {
				c.sendMessage("There are currently "+PlayerHandler.getPlayerCount()+ " players online. Thread count: "+Thread.activeCount());
			}
			
		}
		
		if(c.playerRights >= (Config.SERVER_TESTING ? 0 : 3)) {
		
			if (playerCommand.startsWith("tele")) {
				try{
					String[] args = playerCommand.split(" ");
					c.teleportToX = Integer.parseInt(args[1]);
					c.teleportToY = Integer.parseInt(args[2]);
				} catch(Exception e) { 
					c.sendMessage("Wrong Syntax! Use as ::tele 3400,3500"); 
				}
			}
			
			if (playerCommand.equalsIgnoreCase("empty")) {
				c.getItems().removeAllItems();
			}
			
			if (playerCommand.equalsIgnoreCase("bank")) {
				c.getPA().openUpBank();
			}
			if (playerCommand.equalsIgnoreCase("heal")) {
				c.playerLevel[3] += 10000;
				c.getPA().refreshSkill(3);
			}
			if (playerCommand.equalsIgnoreCase("runes")) {
				for(int r = 554; r < 567; r++) {
					c.getItems().addItem(r, 1000);
				}
			}
			if (playerCommand.equalsIgnoreCase("master")) {
				for (int i = 0; i < 21; i++) {
					c.playerLevel[i] = 99;
					c.playerXP[i] = c.getPA().getXPForLevel(100);
					c.getPA().refreshSkill(i);	
					c.getPA().requestUpdates();
				}
			}

			if (playerCommand.equalsIgnoreCase("barrows")) {
				c.getPA().movePlayer(3564,3288,0);
				c.getItems().addItem(952, 1);
			}
			if (playerCommand.equalsIgnoreCase("hints")) {
				c.headIconHints = 2;
				c.getPA().requestUpdates();
			}
			if (playerCommand.equalsIgnoreCase("pk")) {
				c.headIconPk = 1;
				c.getPA().requestUpdates();
			}			
			
			if (playerCommand.startsWith("pickup")) {
				String[] args = playerCommand.split(" ");
				if (args.length == 3) {
					int newItemID = Integer.parseInt(args[1]);
					int newItemAmount = Integer.parseInt(args[2]);
					if ((newItemID <= 20000) && (newItemID >= 0)) {
						c.getItems().addItem(newItemID, newItemAmount);		
					} else {
						c.sendMessage("No such item.");
					}
				} else {
					c.sendMessage("Use as ::pickup 995 200");
				}
			}
			
			if (playerCommand.startsWith("ipban")) { // use as ::ipban name
				String[] args = playerCommand.split(" ");
				for(int i = 0; i < Config.MAX_PLAYERS; i++) {
					if(Server.playerHandler.players[i] != null) {
						if(Server.playerHandler.players[i].playerName.equalsIgnoreCase(args[1])) {
							ConnectionHandler.addIpToBanList(((Client)Server.playerHandler.players[i]).socketChannel.socket().getInetAddress().getHostAddress());
							ConnectionHandler.addIpToFile(((Client)Server.playerHandler.players[i]).socketChannel.socket().getInetAddress().getHostAddress());
							c.sendMessage("You have IP banned the user: "+Server.playerHandler.players[i].playerName+" with the host: "+((Client)Server.playerHandler.players[i]).socketChannel.socket().getInetAddress().getHostAddress());
							Server.playerHandler.players[i].disconnected = true;
						} 
					}
				}
			}
			
			if (playerCommand.startsWith("ban")) { // use as ::ban name
				String[] args = playerCommand.split(" ");
				ConnectionHandler.addNameToBanList(args[1]);
				ConnectionHandler.addNameToFile(args[1]);
				for(int i = 0; i < Config.MAX_PLAYERS; i++) {
					if(Server.playerHandler.players[i] != null) {
						if(Server.playerHandler.players[i].playerName.equalsIgnoreCase(args[1])) {
							Server.playerHandler.players[i].disconnected = true;
						} 
					}
				}
			}

			
			if (playerCommand.startsWith("anim")) {
				String[] args = playerCommand.split(" ");
				c.startAnimation(Integer.parseInt(args[1]));
				c.getPA().requestUpdates();
			}	
		}
	}
}
		
		
		
		
		
		
		

