package server.model.players;

import java.io.*;
import server.util.*;
import server.model.npcs.*;
import server.*;
import server.model.items.*;


public class PlayerAssistant{

	private Client c;
	public PlayerAssistant(Client Client) {
		this.c = Client;
	}
	
	public void sendFrame126(String s, int id) {
		if(c.getOutStream() != null && c != null ) {
			c.getOutStream().createFrameVarSizeWord(126);
			c.getOutStream().writeString(s);
			c.getOutStream().writeWordA(id);
			c.getOutStream().endFrameVarSizeWord();
			c.flushOutStream();
		}
	}
	
	public void setSkillLevel(int skillNum, int currentLevel, int XP) {
		if(c.getOutStream() != null && c != null) {
			c.getOutStream().createFrame(134);
			c.getOutStream().writeByte(skillNum);
			c.getOutStream().writeDWord_v1(XP);
			c.getOutStream().writeByte(currentLevel);
			c.flushOutStream();
		}
	}
	
	public void sendFrame106(int sideIcon) {
		if(c.getOutStream() != null && c != null) {
			c.getOutStream().createFrame(106);
			c.getOutStream().writeByteC(sideIcon);
			c.flushOutStream();
			requestUpdates();
		}
	}
	
	public void sendFrame107() {
		if(c.getOutStream() != null && c != null) {
			c.getOutStream().createFrame(107);
			c.flushOutStream();
		}
	}
	public void sendFrame36(int id, int state) {
		if(c.getOutStream() != null && c != null) {
			c.getOutStream().createFrame(36);
			c.getOutStream().writeWordBigEndian(id);
			c.getOutStream().writeByte(state);
			c.flushOutStream();
		}
	}
	
	public void showInterface(int interfaceid) {
		if(c.getOutStream() != null && c != null) {
			c.getOutStream().createFrame(97);
			c.getOutStream().writeWord(interfaceid);
			c.flushOutStream();
		}
	}
	
	public void sendFrame248(int MainFrame, int SubFrame) {
		if(c.getOutStream() != null && c != null) {
			c.getOutStream().createFrame(248);
			c.getOutStream().writeWordA(MainFrame);
			c.getOutStream().writeWord(SubFrame);
			c.flushOutStream();
		}
	}
	
	public void sendFrame246(int MainFrame, int SubFrame, int SubFrame2) {
		if(c.getOutStream() != null && c != null) {
			c.getOutStream().createFrame(246);
			c.getOutStream().writeWordBigEndian(MainFrame);
			c.getOutStream().writeWord(SubFrame);
			c.getOutStream().writeWord(SubFrame2);
			c.flushOutStream();
		}
	}
	
	public void sendFrame171(int MainFrame, int SubFrame) {
		if(c.getOutStream() != null && c != null) {
			c.getOutStream().createFrame(171);
			c.getOutStream().writeByte(MainFrame);
			c.getOutStream().writeWord(SubFrame);
			c.flushOutStream();
		}
	}
	
	public void sendFrame200(int MainFrame, int SubFrame) {
		if(c.getOutStream() != null && c != null) {
			c.getOutStream().createFrame(200);
			c.getOutStream().writeWord(MainFrame);
			c.getOutStream().writeWord(SubFrame);
			c.flushOutStream();
		}
	}
	
	public void sendFrame70(int i, int o, int id) {
		if(c.getOutStream() != null && c != null) {
			c.getOutStream().createFrame(70);
			c.getOutStream().writeWord(i);
			c.getOutStream().writeWordBigEndian(o);
			c.getOutStream().writeWordBigEndian(id);
			c.flushOutStream();
		}
	}

	public void sendFrame75(int MainFrame, int SubFrame) {
		if(c.getOutStream() != null && c != null) {
			c.getOutStream().createFrame(75);
			c.getOutStream().writeWordBigEndianA(MainFrame);
			c.getOutStream().writeWordBigEndianA(SubFrame);
			c.flushOutStream();
		}
	}
	
	public void sendFrame164(int Frame) {
		if(c.getOutStream() != null && c != null) {
			c.getOutStream().createFrame(164);
			c.getOutStream().writeWordBigEndian_dup(Frame);
			c.flushOutStream();
		}
	}
	
	public void setPrivateMessaging(int i) { // friends and ignore list status
		if(c.getOutStream() != null && c != null) {
	        c.getOutStream().createFrame(221);
	        c.getOutStream().writeByte(i);
			c.flushOutStream();
		}
    }
	
	public void setChatOptions(int publicChat, int privateChat, int tradeBlock) {
		if(c.getOutStream() != null && c != null) {
			c.getOutStream().createFrame(206);
			c.getOutStream().writeByte(publicChat);	
			c.getOutStream().writeByte(privateChat);	
			c.getOutStream().writeByte(tradeBlock);
			c.flushOutStream();
		}
	}
	
	public void sendFrame87(int id, int state) {
		if(c.getOutStream() != null && c != null) {
			c.getOutStream().createFrame(87);
			c.getOutStream().writeWordBigEndian_dup(id);	
			c.getOutStream().writeDWord_v1(state);
			c.flushOutStream();
		}
	}
	
	public void sendPM(long name, int rights, byte[] chatmessage, int messagesize) {
		if(c.getOutStream() != null && c != null) {
			c.getOutStream().createFrameVarSize(196);
			c.getOutStream().writeQWord(name);
			c.getOutStream().writeDWord(c.lastChatId++);
			c.getOutStream().writeByte(rights);
			c.getOutStream().writeBytes(chatmessage, messagesize, 0);
			c.getOutStream().endFrameVarSize();
			c.flushOutStream();
			String chatmessagegot = Misc.textUnpack(chatmessage, messagesize);
			String target = Misc.longToPlayerName(name);
		}		
	}
	
	public void createPlayerHints(int type, int id) {
		if(c.getOutStream() != null && c != null) {
			c.getOutStream().createFrame(254);
			c.getOutStream().writeByte(type);
			c.getOutStream().writeWord(id); 
			c.getOutStream().write3Byte(0);
			c.flushOutStream();
		}
	}

	public void createObjectHints(int x, int y, int height, int pos) {
		if(c.getOutStream() != null && c != null) {
			c.getOutStream().createFrame(254);
			c.getOutStream().writeByte(pos);
			c.getOutStream().writeWord(x);
			c.getOutStream().writeWord(y);
			c.getOutStream().writeByte(height);
			c.flushOutStream();
		}
	}
	
	public void loadPM(long playerName, int world) {
		if(c.getOutStream() != null && c != null) {
			if(world != 0) {
	            world += 9;
			} else if(!Config.WORLD_LIST_FIX) {
				world += 1;
			}	
			c.getOutStream().createFrame(50);
			c.getOutStream().writeQWord(playerName);
			c.getOutStream().writeByte(world);
			c.flushOutStream();
		}
	}
	
	public void removeAllWindows() {
		if(c.getOutStream() != null && c != null) {
			c.getOutStream().createFrame(219);
			c.flushOutStream();
		}
	}
	
	public void walkableInterface(int id) {
		if(c.getOutStream() != null && c != null) {
			c.getOutStream().createFrame(208);
	        c.getOutStream().writeWordBigEndian_dup(id);
			c.flushOutStream();
		}
	}
	
	public int mapStatus = 0;
	public void sendFrame99(int state) { // used for disabling map
		if(c.getOutStream() != null && c != null) {
			if(mapStatus != state) {
				mapStatus = state;
				c.getOutStream().createFrame(99);
		        c.getOutStream().writeByte(state);
				c.flushOutStream();
			}
		}
	}
	
	public void sendOption(String s, String s1) {
		sendFrame126("Select an Option", 2470);
	 	sendFrame126(s, 2471);
		sendFrame126(s1, 2472);
		sendFrame126("Click here to continue", 2473);
		sendFrame164(13758);
	}	
	
	public void sendOption2(String s, String s1) {
		sendFrame126("Select an Option", 2460);
		sendFrame126(s, 2461);
		sendFrame126(s1, 2462);
		sendFrame164(2459);
	}
	
	public void sendOption(String s, String s1, String s2, String s3) {
		sendFrame126("Select an Option", 2481);
		sendFrame126(s, 2482);
		sendFrame126(s1, 2483);
		sendFrame126(s2, 2484);
		sendFrame126(s3, 2485);
		sendFrame164(2480);
	}

	public void sendStatement(String s) { // 1 line click here to continue chat box interface
		sendFrame126(s, 357);
		sendFrame126("Click here to continue", 358);
		sendFrame164(356);
	}
	
	/**
	* Reseting animations for everyone
	**/

	public void frame1() {
		for(int i = 0; i < Config.MAX_PLAYERS; i++) {
			if(Server.playerHandler.players[i] != null) {
				Client person = (Client)Server.playerHandler.players[i];
				if(person != null) {
					if(person.getOutStream() != null && !person.disconnected) {
						if(c.distanceToPoint(person.getX(), person.getY()) <= 25){	
							person.getOutStream().createFrame(1);
							person.flushOutStream();
							person.getPA().requestUpdates();
						}
					}
				}
			}
		}
	}
	
	/**
	* Creating projectile
	**/
	public void createProjectile(int x, int y, int offX, int offY, int angle, int speed, int gfxMoving, int startHeight, int endHeight, int lockon, int time) {      
		if(c.getOutStream() != null && c != null) {
			c.getOutStream().createFrame(85);
	        c.getOutStream().writeByteC((y - (c.getMapRegionY() * 8)) - 2);
	        c.getOutStream().writeByteC((x - (c.getMapRegionX() * 8)) - 3);
	        c.getOutStream().createFrame(117);
	        c.getOutStream().writeByte(angle);
	        c.getOutStream().writeByte(offY);
	        c.getOutStream().writeByte(offX);
	        c.getOutStream().writeWord(lockon);
	        c.getOutStream().writeWord(gfxMoving);
	        c.getOutStream().writeByte(startHeight);
	        c.getOutStream().writeByte(endHeight);
	        c.getOutStream().writeWord(time);
		    c.getOutStream().writeWord(speed);
			c.getOutStream().writeByte(16);
			c.getOutStream().writeByte(64);
			c.flushOutStream();
		}
    }
	
	// projectiles for everyone within 25 squares
	public void createPlayersProjectile(int x, int y, int offX, int offY, int angle, int speed, int gfxMoving, int startHeight, int endHeight, int lockon, int time) {
		for(int i = 0; i < Config.MAX_PLAYERS; i++) {
			if(Server.playerHandler.players[i] != null) {
				Client person = (Client)Server.playerHandler.players[i];
				if(person != null) {
					if(person.getOutStream() != null) {
						if(person.distanceToPoint(x, y) <= 25){	
							person.getPA().createProjectile(x, y, offX, offY, angle, speed, gfxMoving, startHeight, endHeight, lockon, time);	
						}
					}
				}	
			}
		}
	}
	

	/**
	** GFX
	**/
	public void stillGfx(int id, int x, int y, int height, int time) {
		if(c.getOutStream() != null && c != null) {
			c.getOutStream().createFrame(85);
			c.getOutStream().writeByteC(y - (c.getMapRegionY() * 8));
			c.getOutStream().writeByteC(x - (c.getMapRegionX() * 8));
			c.getOutStream().createFrame(4);
			c.getOutStream().writeByte(0);
			c.getOutStream().writeWord(id);
			c.getOutStream().writeByte(height);
			c.getOutStream().writeWord(time);
			c.flushOutStream();
		}
	}
	
	//creates gfx for everyone
	public void createPlayersStillGfx(int id, int x, int y, int height, int time) {
		for(int i = 0; i < Config.MAX_PLAYERS; i++) {
			if(Server.playerHandler.players[i] != null) {
				Client person = (Client)Server.playerHandler.players[i];
				if(person != null) {
					if(person.getOutStream() != null) {
						if(person.distanceToPoint(x, y) <= 25){	
							person.getPA().stillGfx(id, x, y, height, time);
						}
					}
				}	
			}
		}
	}
	
	/**
	* Objects, add and remove
	**/
	public void object(int objectId, int objectX, int objectY, int face, int objectType) {
		if(c.getOutStream() != null && c != null) {
			c.getOutStream().createFrame(85);
			c.getOutStream().writeByteC(objectY - (c.getMapRegionY() * 8));
			c.getOutStream().writeByteC(objectX - (c.getMapRegionX() * 8));
			c.getOutStream().createFrame(101);
			c.getOutStream().writeByteC((objectType<<2) + (face&3));
			c.getOutStream().writeByte(0);
		
			if (objectId != -1) { // removing
				c.getOutStream().createFrame(151);
				c.getOutStream().writeByteS(0);
				c.getOutStream().writeWordBigEndian(objectId);
				c.getOutStream().writeByteS((objectType<<2) + (face&3));
			}
			c.flushOutStream();
		}	
	}
	

	public void writeQuestTab() {
		sendFrame126(Config.SERVER_NAME+" Info", 640);
		sendFrame126("@whi@Items kept on death", 7332);
		sendFrame126("@whi@Duel Arena", 7333);
		sendFrame126("", 7334);
		sendFrame126("", 7336);	
	}
	
	/**
	* Show option, attack, trade, follow etc
	**/
	public String optionType = "null";
	public void showOption(int i, int l, String s, int a) {
		if(c.getOutStream() != null && c != null) {
			if(!optionType.equalsIgnoreCase(s)) {
				optionType = s;
				c.getOutStream().createFrameVarSize(104);
				c.getOutStream().writeByteC(i);
				c.getOutStream().writeByteA(l);
				c.getOutStream().writeString(s);
				c.getOutStream().endFrameVarSize();
				c.flushOutStream();
			}
		}
	}
	
	/**
	* Open bank
	**/
	public void openUpBank(){
		if(c.getOutStream() != null && c != null) {
			c.getItems().resetItems(5064);
			c.getItems().rearrangeBank();
			c.getItems().resetBank();
			c.getItems().resetTempItems();
			c.getOutStream().createFrame(248);
			c.getOutStream().writeWordA(5292);
			c.getOutStream().writeWord(5063);
			c.flushOutStream();
		}
	}
	
	/**
	* Private Messaging
	**/	
	public void logIntoPM() {
		setPrivateMessaging(2);
		for(int i1 = 0; i1 < Config.MAX_PLAYERS; i1++) {
			if(Server.playerHandler.players[i1] != null && Server.playerHandler.players[i1].isActive) {
				Client o = (Client)Server.playerHandler.players[i1];
				if(o != null) {
					o.getPA().updatePM(c.playerId, 1);
				}
			}
		}
		boolean pmLoaded = false;

		for(int i = 0; i < c.friends.length; i++) {
			if(c.friends[i] != 0)  {
				for(int i2 = 1; i2 < Config.MAX_PLAYERS; i2++) {
					if (Server.playerHandler.players[i2] != null && Server.playerHandler.players[i2].isActive && Misc.playerNameToInt64(Server.playerHandler.players[i2].playerName) == c.friends[i])  {
						Client o = (Client)Server.playerHandler.players[i2];
						if(o != null) {
							if (c.playerRights >= 2 || Server.playerHandler.players[i2].privateChat == 0 || (Server.playerHandler.players[i2].privateChat == 1 && o.getPA().isInPM(Misc.playerNameToInt64(c.playerName)))) {
			 		 			loadPM(c.friends[i], 1);
			 		 			pmLoaded = true;
							}
							break;
						}
					}
				}
				if(!pmLoaded) {	
					loadPM(c.friends[i], 0);
				}
				pmLoaded = false;
			}
			for(int i1 = 1; i1 < Config.MAX_PLAYERS; i1++) {
    			if(Server.playerHandler.players[i1] != null && Server.playerHandler.players[i1].isActive) {
					Client o = (Client)Server.playerHandler.players[i1];
					if(o != null) {
						o.getPA().updatePM(c.playerId, 1);
					}
				}
			}
		}
	}
	
	
	public void updatePM(int pID, int world) { // used for private chat updates
		if(Server.playerHandler.players[pID] == null || Server.playerHandler.players[pID].playerName == null || Server.playerHandler.players[pID].playerName.equals("null")){
			return;
		}
		Client o = (Client)Server.playerHandler.players[pID];
		if(o == null) {
			return;
		}
        long l = Misc.playerNameToInt64(Server.playerHandler.players[pID].playerName);

        if (Server.playerHandler.players[pID].privateChat == 0) {
            for (int i = 0; i < c.friends.length; i++) {
                if (c.friends[i] != 0) {
                    if (l == c.friends[i]) {
                        loadPM(l, world);
                        return;
                    }
                }
            }
        } else if (Server.playerHandler.players[pID].privateChat == 1) {
            for (int i = 0; i < c.friends.length; i++) {
                if (c.friends[i] != 0) {
                    if (l == c.friends[i]) {
                        if (o.getPA().isInPM(Misc.playerNameToInt64(c.playerName))) {
                            loadPM(l, world);
                            return;
                        } else {
                            loadPM(l, 0);
                            return;
                        }
                    }
                }
            }
        } else if (Server.playerHandler.players[pID].privateChat == 2) {
            for (int i = 0; i < c.friends.length; i++) {
                if (c.friends[i] != 0) {
                    if (l == c.friends[i] && c.playerRights < 2) {
                        loadPM(l, 0);
                        return;
                    }
                }
            }
        }
    }
	
	public boolean isInPM(long l) {
        for (int i = 0; i < c.friends.length; i++) {
            if (c.friends[i] != 0) {
                if (l == c.friends[i]) {
                    return true;
                }
            }
        }
        return false;
    }
		
	/**
	* Burying bones
	**/
	
	public void buryBone(int buryXp, int delay, int itemId, int itemSlot) {
		c.attackTimer = c.getCombat().getAttackDelay(c.getItems().getItemName(c.playerEquipment[c.playerWeapon]).toLowerCase());
		if(System.currentTimeMillis() - c.buryDelay > delay) {
			if(c.getItems().playerHasItem(itemId, 1, itemSlot)) {
				c.buryDelay = System.currentTimeMillis();
				addSkillXP(buryXp * Config.BONE_EXP, 5);
				c.startAnimation(827);
				c.getItems().deleteItem(itemId, itemSlot, 1);
				c.sendMessage("You bury the "+c.getItems().getItemName(itemId).toLowerCase()+".");
				refreshSkill(5);
				requestUpdates();
				
			}
		}	
	}
	
	/**
	* Magic on items
	**/
	
	public void magicOnItems(int slot, int itemId, int spellId) {
		
		switch(spellId) {
			case 1162: // low alch
			if(System.currentTimeMillis() - c.alchDelay > 1200) {	
				if(!c.getCombat().checkMagicReqs(49)) {
					break;
				}
				if(itemId == 995) {
					c.sendMessage("You can't alch coins");
					break;
				}
				c.getItems().deleteItem(itemId, slot, 1);
				c.getItems().addItem(995, c.getShops().getItemShopValue(itemId)/2);
				c.startAnimation(c.MAGIC_SPELLS[49][2]);
				c.gfx100(c.MAGIC_SPELLS[49][3]);
				c.alchDelay = System.currentTimeMillis();
				sendFrame106(6);
				addSkillXP(c.MAGIC_SPELLS[49][7], 6);
				refreshSkill(6);
			}
			break;
			
			case 1178: // high alch
			if(System.currentTimeMillis() - c.alchDelay > 3000) {	
				if(!c.getCombat().checkMagicReqs(50)) {
					break;
				}
				if(itemId == 995) {
					c.sendMessage("You can't alch coins");
					break;
				}
				c.getItems().deleteItem(itemId, slot, 1);
				c.getItems().addItem(995, c.getShops().getItemShopValue(itemId));
				c.startAnimation(c.MAGIC_SPELLS[50][2]);
				c.gfx100(c.MAGIC_SPELLS[50][3]);
				c.alchDelay = System.currentTimeMillis();
				sendFrame106(6);
				addSkillXP(c.MAGIC_SPELLS[50][7], 6);
				refreshSkill(6);
			}
			break;
			
		}
	}
	
	/**
	* Dieing
	**/
	
	public void applyDead() {	
		c.respawnTimer = 15;
		c.isDead = false;
		
		if(c.duelStatus != 6) {
			c.killerId = c.getCombat().getKillerId(c.playerId);
			Client o = (Client) Server.playerHandler.players[c.killerId];
			if(o != null) {
				o.sendMessage("You have defeated "+c.playerName+"!");
				if(o.duelStatus == 5) {
					o.duelStatus++;
				}
			}
		}
		c.faceUpdate(0);
		c.npcIndex = 0;
		c.playerIndex = 0;
		c.stopMovement();
		if(c.duelStatus <= 4) {
			c.sendMessage("Oh dear you are dead!");
		} else if(c.duelStatus != 6) {
			c.sendMessage("You have lost the duel!");
		}
		c.attackTimer = 10;
	}
	
	public void giveLife() {
		c.isDead = false;
		c.faceUpdate(-1);
		c.freezeTimer = 0;	
		if(c.duelStatus <= 4) { // if we are not in a duel we must be in wildy so remove items
			c.getItems().resetKeepItems();
			if((c.playerRights == 2 && Config.ADMIN_DROP_ITEMS) || c.playerRights != 2) {
				if(!c.isSkulled) {	// what items to keep
					c.getItems().keepItem(0, true);
					c.getItems().keepItem(1, true);	
					c.getItems().keepItem(2, true);
				}	
				if(c.prayerActive[8]) {
					c.getItems().keepItem(3, true);
				}
				c.getItems().dropAllItems(); // drop all items
				c.getItems().deleteAllItems(); // delete all items
				
				if(!c.isSkulled) { // add the kept items once we finish deleting and dropping them	
					for (int i1 = 0; i1 < 3; i1++) {
						if(c.itemKeptId[i1] > 0) {
							c.getItems().addItem(c.itemKeptId[i1], 1);
						}
					}
				}	
				if(c.prayerActive[8]) { // if we have protect items 
					if(c.itemKeptId[3] > 0) {
						c.getItems().addItem(c.itemKeptId[3], 1);
					}
				}
			}
			c.getItems().resetKeepItems();
		}
		c.getCombat().resetPrayers();
		for (int i = 0; i < 20; i++) {
			c.playerLevel[i] = getLevelForXP(c.playerXP[i]);
			c.getPA().refreshSkill(i);
		}
		
		if(c.duelStatus <= 4) { // if we are not in a duel repawn to wildy
			movePlayer(Config.RESPAWN_X, Config.RESPAWN_Y, 0);
			c.isSkulled = false;
			c.skullTimer = 0;
			c.attackedPlayers.clear();
		} else { // we are in a duel, respawn outside of arena
			Client o = (Client) Server.playerHandler.players[c.killerId];
			if(o != null) {
				o.getPA().createPlayerHints(10, -1);
				if(o.duelStatus == 6) {
					o.getTradeAndDuel().duelVictory();
				}
			}
			movePlayer(Config.DUELING_RESPAWN_X+(Misc.random(Config.RANDOM_DUELING_RESPAWN)), Config.DUELING_RESPAWN_Y+(Misc.random(Config.RANDOM_DUELING_RESPAWN)), 0);
			if(c.duelStatus != 6) { // if we have won but have died, don't reset the duel status.
				c.getTradeAndDuel().resetDuel();
			}
		}
		c.getCombat().resetPlayerAttack();
		resetAnimation();
		frame1();
	}
		
	/**
	* Location change for digging, levers etc
	**/
	
	public void changeLocation() {
		switch(c.newLocation) {
			case 1:
			sendFrame99(2);
			movePlayer(3578,9706,-1);
			break;
			case 2:
			sendFrame99(2);
			movePlayer(3568,9683,-1);
			break;
			case 3:
			sendFrame99(2);
			movePlayer(3557,9703,-1);
			break;
			case 4:
			sendFrame99(2);
			movePlayer(3556,9718,-1);
			break;
			case 5:
			sendFrame99(2);
			movePlayer(3534,9704,-1);
			break;
			case 6:
			sendFrame99(2);
			movePlayer(3546,9684,-1);
			break;
		}
		c.newLocation = 0;
	}
	
	
	/**
	* Teleporting
	**/
	
	public void startTeleport(int x, int y, int height, String teleportType) {
	
		if(c.duelStatus == 5) {
			c.sendMessage("You can't teleport during a duel!");
			return;
		}
		if(c.inWild() && c.wildLevel >= Config.NO_TELEPORT_WILD_LEVEL) {
			c.sendMessage("You can't teleport on or above level "+Config.NO_TELEPORT_WILD_LEVEL+" in the wilderness.");
			return;
		}
		if(System.currentTimeMillis() - c.teleBlockDelay < Config.TELEBLOCK_DELAY) {
			c.sendMessage("You are teleblocked and can't teleport.");
			return;
		}
		if(!c.isDead && c.teleTimer == 0) {			
			c.stopMovement();
			removeAllWindows();			
			c.teleX = x;
			c.teleY = y;
			c.npcIndex = 0;
			c.playerIndex = 0;
			c.faceUpdate(0);
			c.teleHeight = height;
			if(teleportType.equalsIgnoreCase("modern")) {
				c.startAnimation(714);
				c.teleTimer = 11;
				c.teleGfx = 308;
				c.teleEndAnimation = 715;
			} 
			if(teleportType.equalsIgnoreCase("ancient")) {
				c.startAnimation(1979);
				c.teleGfx = 0;
				c.teleTimer = 9;
				c.teleEndAnimation = 0;
				c.gfx0(392);
			}
			
		}
	}

	public void processTeleport() {
		c.teleportToX = c.teleX;
		c.teleportToY = c.teleY;
		c.heightLevel = c.teleHeight;
		if(c.teleEndAnimation > 0) {
			c.startAnimation(c.teleEndAnimation);
		}
	}
		
	public void movePlayer(int x, int y, int h) {
		c.teleportToX = x;
        c.teleportToY = y;
		c.heightLevel = h;
		requestUpdates();
	}
	
	/**
	* Following
	**/
	
	public void followPlayer() {
		if(Server.playerHandler.players[c.followId] == null || Server.playerHandler.players[c.followId].isDead) {
			c.followId = 0;
			return;
		}		
		if(c.freezeTimer > 0) {
			return;
		}
		int otherX = Server.playerHandler.players[c.followId].getX();
		int otherY = Server.playerHandler.players[c.followId].getY();
		boolean withinDistance = c.goodDistance(otherX, otherY, c.getX(), c.getY(), 2);
		boolean hallyDistance = c.goodDistance(otherX, otherY, c.getX(), c.getY(), 2);
		boolean bowDistance = c.goodDistance(otherX, otherY, c.getX(), c.getY(), 4);
		boolean rangeWeaponDistance = c.goodDistance(otherX, otherY, c.getX(), c.getY(), 2);
		if(!c.goodDistance(otherX, otherY, c.getX(), c.getY(), 25)) {
			c.followId = 0;
			return;
		}
		c.faceUpdate(c.followId+32768);
		
		if (otherY == c.getY() && otherX == c.getX()) {			
			switch(Misc.random(3))  {
				case 0:
				walkTo(0, getMove(c.getX(), otherX - 1));
				break;
				
				case 1:
				walkTo(0, getMove(c.getX(), otherX + 1));
				break;
				
				case 2:
				walkTo(0, getMove(c.getY(), otherY + 1));
				break;
				
				case 3:
				walkTo(0, getMove(c.getY(), otherY - 1));
				break;
			}
		}
		if(c.usingBow && bowDistance) {
			return;
		}
		
		if(c.getCombat().usingHally() && hallyDistance) {
			return;
		}
		
		if(c.usingRangeWeapon && rangeWeaponDistance) {
			return;
		}
		if(c.isRunning2 && !withinDistance) {
			if(otherY > c.getY() && otherX == c.getX()) {
				walkTo(0, getMove(c.getY(), otherY - 1) + getMove(c.getY(), otherY - 1));
			} else if(otherY < c.getY() && otherX == c.getX()) {
				walkTo(0, getMove(c.getY(), otherY + 1) + getMove(c.getY(), otherY + 1));
			} else if(otherX > c.getX() && otherY == c.getY()) {
				walkTo(getMove(c.getX(), otherX - 1) + getMove(c.getX(), otherX - 1), 0);
			} else if(otherX < c.getX() && otherY == c.getY()) {
				walkTo(getMove(c.getX(), otherX + 1) + getMove(c.getX(), otherX + 1), 0);
			} else if(otherX < c.getX() && otherY < c.getY()) {
				walkTo(getMove(c.getX(), otherX + 1) + getMove(c.getX(), otherX + 1), getMove(c.getY(), otherY + 1) + getMove(c.getY(), otherY + 1));
			} else if(otherX > c.getX() && otherY > c.getY()) {
				walkTo(getMove(c.getX(), otherX - 1) + getMove(c.getX(), otherX - 1), getMove(c.getY(), otherY - 1) + getMove(c.getY(), otherY - 1));
			} else if(otherX < c.getX() && otherY > c.getY()) {
				walkTo(getMove(c.getX(), otherX + 1) + getMove(c.getX(), otherX + 1), getMove(c.getY(), otherY - 1) + getMove(c.getY(), otherY - 1));
			} else if(otherX > c.getX() && otherY < c.getY()) {
				walkTo(getMove(c.getX(), otherX + 1) + getMove(c.getX(), otherX + 1), getMove(c.getY(), otherY - 1) + getMove(c.getY(), otherY - 1));
			} 		
		} else {
			if(otherY > c.getY() && otherX == c.getX()) {
				walkTo(0, getMove(c.getY(), otherY - 1));
			} else if(otherY < c.getY() && otherX == c.getX()) {
				walkTo(0, getMove(c.getY(), otherY + 1));
			} else if(otherX > c.getX() && otherY == c.getY()) {
				walkTo(getMove(c.getX(), otherX - 1), 0);
			} else if(otherX < c.getX() && otherY == c.getY()) {
				walkTo(getMove(c.getX(), otherX + 1), 0);
			} else if(otherX < c.getX() && otherY < c.getY()) {
				walkTo(getMove(c.getX(), otherX + 1), getMove(c.getY(), otherY + 1));
			} else if(otherX > c.getX() && otherY > c.getY()) {
				walkTo(getMove(c.getX(), otherX - 1), getMove(c.getY(), otherY - 1));
			} else if(otherX < c.getX() && otherY > c.getY()) {
				walkTo(getMove(c.getX(), otherX + 1), getMove(c.getY(), otherY - 1));
			} else if(otherX > c.getX() && otherY < c.getY()) {
				walkTo(getMove(c.getX(), otherX - 1), getMove(c.getY(), otherY + 1));
			} 
		}
	}
	
	public void walkTo(int i, int j) {
        c.newWalkCmdSteps = 0;
        if(++c.newWalkCmdSteps > 50)
            c.newWalkCmdSteps = 0;
        int k = c.getX() + i;
        k -= c.mapRegionX * 8;
        c.newWalkCmdX[0] = c.newWalkCmdY[0] = 0;
        int l = c.getY() + j;
        l -= c.mapRegionY * 8;

        for(int n = 0; n < c.newWalkCmdSteps; n++) {
            c.newWalkCmdX[n] += k;
            c.newWalkCmdY[n] += l;
        }
    }
	
	public int getMove(int place1,int place2) { 
		if ((place1 - place2) == 0) {
            return 0;
		} else if ((place1 - place2) < 0) {
			return 1;
		} else if ((place1 - place2) > 0) {
			return -1;
		}
        return 0;
   	 }
	
	/**
	* reseting animation
	**/
	public void resetAnimation() {
		c.getCombat().getPlayerAnimIndex(c.getItems().getItemName(c.playerEquipment[c.playerWeapon]).toLowerCase());
		requestUpdates();
	}

	public void requestUpdates() {
		c.updateRequired = true;
		c.appearanceUpdateRequired = true;
	}
	
	public void levelUp(int skill) {
		int totalLevel = (getLevelForXP(c.playerXP[0]) + getLevelForXP(c.playerXP[1]) + getLevelForXP(c.playerXP[2]) + getLevelForXP(c.playerXP[3]) + getLevelForXP(c.playerXP[4]) + getLevelForXP(c.playerXP[5]) + getLevelForXP(c.playerXP[6]) + getLevelForXP(c.playerXP[7]) + getLevelForXP(c.playerXP[8]) + getLevelForXP(c.playerXP[9]) + getLevelForXP(c.playerXP[10]) + getLevelForXP(c.playerXP[11]) + getLevelForXP(c.playerXP[12]) + getLevelForXP(c.playerXP[13]) + getLevelForXP(c.playerXP[14]) + getLevelForXP(c.playerXP[15]) + getLevelForXP(c.playerXP[16]) + getLevelForXP(c.playerXP[17]) + getLevelForXP(c.playerXP[18]) + getLevelForXP(c.playerXP[19]) + getLevelForXP(c.playerXP[20]));
		sendFrame126("Total Lvl: "+totalLevel, 3984);
		switch(skill) {
			case 0:
			sendFrame126("Congratulations, you just advanced an attack level!", 6248);
			sendFrame126("Your attack level is now "+getLevelForXP(c.playerXP[skill])+".", 6249);
			c.sendMessage("Congratulations, you just advanced an attack level.");	
			sendFrame164(6247);
			break;
			
			case 1:
            sendFrame126("Congratulations, you just advanced a defence level!", 6254);
            sendFrame126("Your defence level is now "+getLevelForXP(c.playerXP[skill])+".", 6255);
            c.sendMessage("Congratulations, you just advanced a defence level.");
			sendFrame164(6253);
			break;
			
			case 2:
            sendFrame126("Congratulations, you just advanced a strength level!", 6207);
            sendFrame126("Your strength level is now "+getLevelForXP(c.playerXP[skill])+".", 6208);
            c.sendMessage("Congratulations, you just advanced a strength level.");
			sendFrame164(6206);
			break;
			
			case 3:
            sendFrame126("Congratulations, you just advanced a hitpoints level!", 6217);
            sendFrame126("Your hitpoints level is now "+getLevelForXP(c.playerXP[skill])+".", 6218);
            c.sendMessage("Congratulations, you just advanced a hitpoints level.");
			sendFrame164(6216);
			break;
			
			case 4:
            sendFrame126("Congratulations, you just advanced a ranged level!", 5453);
            sendFrame126("Your ranged level is now "+getLevelForXP(c.playerXP[skill])+".", 6114);
            c.sendMessage("Congratulations, you just advanced a ranging level.");
			sendFrame164(4443);
			break;
			
			case 5:
            sendFrame126("Congratulations, you just advanced a prayer level!", 6243);
            sendFrame126("Your prayer level is now "+getLevelForXP(c.playerXP[skill])+".", 6244);
            c.sendMessage("Congratulations, you just advanced a prayer level.");
			sendFrame164(6242);
			break;
			
			case 6:
            sendFrame126("Congratulations, you just advanced a magic level!", 6212);
            sendFrame126("Your magic level is now "+getLevelForXP(c.playerXP[skill])+".", 6213);
            c.sendMessage("Congratulations, you just advanced a magic level.");
			sendFrame164(6211);
			break;
			
			case 7:
            sendFrame126("Congratulations, you just advanced a cooking level!", 6227);
            sendFrame126("Your cooking level is now "+getLevelForXP(c.playerXP[skill])+".", 6228);
            c.sendMessage("Congratulations, you just advanced a cooking level.");
			sendFrame164(6226);
			break;
			
			case 8:
			sendFrame126("Congratulations, you just advanced a woodcutting level!", 4273);
			sendFrame126("Your woodcutting level is now "+getLevelForXP(c.playerXP[skill])+".", 4274);
			c.sendMessage("Congratulations, you just advanced a woodcutting level.");
			sendFrame164(4272);
            break;
			
            case 9:
            sendFrame126("Congratulations, you just advanced a fletching level!", 6232);
            sendFrame126("Your fletching level is now "+getLevelForXP(c.playerXP[skill])+".", 6233);
            c.sendMessage("Congratulations, you just advanced a fletching level.");
			sendFrame164(6231);
            break;
			
			case 10:
            sendFrame126("Congratulations, you just advanced a fishing level!", 6259);
            sendFrame126("Your fishing level is now "+getLevelForXP(c.playerXP[skill])+".", 6260);
            c.sendMessage("Congratulations, you just advanced a fishing level.");
			sendFrame164(6258);
			break;
			
			case 11:
			sendFrame126("Congratulations, you just advanced a fire making level!", 4283);
			sendFrame126("Your firemaking level is now "+getLevelForXP(c.playerXP[skill])+".", 4284);
			c.sendMessage("Congratulations, you just advanced a fire making level.");
			sendFrame164(4282);
            break;
			
            case 12:
			sendFrame126("Congratulations, you just advanced a crafting level!", 6264);
			sendFrame126("Your crafting level is now "+getLevelForXP(c.playerXP[skill])+".", 6265);
			c.sendMessage("Congratulations, you just advanced a crafting level.");
			sendFrame164(6263);
            break;
			
			case 13:
			sendFrame126("Congratulations, you just advanced a smithing level!", 6222);
			sendFrame126("Your smithing level is now "+getLevelForXP(c.playerXP[skill])+".", 6223);
			c.sendMessage("Congratulations, you just advanced a smithing level.");
			sendFrame164(6221);
			break;
			
			case 14:
			sendFrame126("Congratulations, you just advanced a mining level!", 4417);
			sendFrame126("Your mining level is now "+getLevelForXP(c.playerXP[skill])+".", 4438);
			c.sendMessage("Congratulations, you just advanced a mining level.");
			sendFrame164(4416);
            break;
			
			case 15:
            sendFrame126("Congratulations, you just advanced a herblore level!", 6238);
            sendFrame126("Your herblore level is now "+getLevelForXP(c.playerXP[skill])+".", 6239);
            c.sendMessage("Congratulations, you just advanced a herblore level.");
			sendFrame164(6237);
            break;
			
			case 16:
			sendFrame126("Congratulations, you just advanced a agility level!", 4278);
			sendFrame126("Your agility level is now "+getLevelForXP(c.playerXP[skill])+".", 4279);
			c.sendMessage("Congratulations, you just advanced an agility level.");
			sendFrame164(4277);
            break;
			
			case 17:
			sendFrame126("Congratulations, you just advanced a thieving level!", 4263);
			sendFrame126("Your theiving level is now "+getLevelForXP(c.playerXP[skill])+".", 4264);
            c.sendMessage("Congratulations, you just advanced a thieving level.");
			sendFrame164(4261);
			break;
			
			case 18:
			sendFrame126("Congratulations, you just advanced a slayer level!", 12123);
			sendFrame126("Your slayer level is now "+getLevelForXP(c.playerXP[skill])+".", 12124);
			c.sendMessage("Congratulations, you just advanced a slayer level.");
			sendFrame164(12122);
            break;
            
            case 20:
			sendFrame126("Congratulations, you just advanced a runecrafting level!", 4268);
			sendFrame126("Your runecrafting level is now "+getLevelForXP(c.playerXP[skill])+".", 4269);
			c.sendMessage("Congratulations, you just advanced a runecrafting level.");
			sendFrame164(4267);
            break;

		}
	}
	
	public void refreshSkill(int i) {
		switch (i) {
			case 0:
			sendFrame126("" + c.playerLevel[0] + "", 4004);
			sendFrame126("" + getLevelForXP(c.playerXP[0]) + "", 4005);
			sendFrame126("" + c.playerXP[0] + "", 4044);
			sendFrame126("" + getXPForLevel(getLevelForXP(c.playerXP[0]) + 1) + "", 4045);
			break;
			
			case 1:
			sendFrame126("" + c.playerLevel[1] + "", 4008);
			sendFrame126("" + getLevelForXP(c.playerXP[1]) + "", 4009);
			sendFrame126("" + c.playerXP[1] + "", 4056);
			sendFrame126("" + getXPForLevel(getLevelForXP(c.playerXP[1]) + 1) + "", 4057);
			break;
			
			case 2:
			sendFrame126("" + c.playerLevel[2] + "", 4006);
			sendFrame126("" + getLevelForXP(c.playerXP[2]) + "", 4007);
			sendFrame126("" + c.playerXP[2] + "", 4050);
			sendFrame126("" + getXPForLevel(getLevelForXP(c.playerXP[2]) + 1) + "", 4051);
			break;
			
			case 3:
			sendFrame126("" + c.playerLevel[3] + "", 4016);
			sendFrame126("" + getLevelForXP(c.playerXP[3]) + "", 4017);
			sendFrame126("" + c.playerXP[3] + "", 4080);
			sendFrame126("" + getXPForLevel(getLevelForXP(c.playerXP[3])+1) + "", 4081);
			break;
			
			case 4:
			sendFrame126("" + c.playerLevel[4] + "", 4010);
			sendFrame126("" + getLevelForXP(c.playerXP[4]) + "", 4011);
			sendFrame126("" + c.playerXP[4] + "", 4062);
			sendFrame126("" + getXPForLevel(getLevelForXP(c.playerXP[4]) + 1) + "", 4063);
			break;
			
			case 5:
			sendFrame126("" + c.playerLevel[5] + "", 4012);
			sendFrame126("" + getLevelForXP(c.playerXP[5]) + "", 4013);
			sendFrame126("" + c.playerXP[5] + "", 4068);
			sendFrame126("" + getXPForLevel(getLevelForXP(c.playerXP[5]) + 1) + "", 4069);
			sendFrame126("Prayer: "+c.playerLevel[5]+"/"+getLevelForXP(c.playerXP[5])+"", 687);//Prayer frame
			break;
			
			case 6:
			sendFrame126("" + c.playerLevel[6] + "", 4014);
			sendFrame126("" + getLevelForXP(c.playerXP[6]) + "", 4015);
			sendFrame126("" + c.playerXP[6] + "", 4074);
			sendFrame126("" + getXPForLevel(getLevelForXP(c.playerXP[6]) + 1) + "", 4075);
			break;
			
			case 7:
			sendFrame126("" + c.playerLevel[7] + "", 4034);
			sendFrame126("" + getLevelForXP(c.playerXP[7]) + "", 4035);
			sendFrame126("" + c.playerXP[7] + "", 4134);
			sendFrame126("" +getXPForLevel(getLevelForXP(c.playerXP[7]) + 1) + "", 4135);
			break;
			
			case 8:
			sendFrame126("" + c.playerLevel[8] + "", 4038);
			sendFrame126("" + getLevelForXP(c.playerXP[8]) + "", 4039);
			sendFrame126("" + c.playerXP[8] + "", 4146);
			sendFrame126("" + getXPForLevel(getLevelForXP(c.playerXP[8]) + 1) + "", 4147);
			break;
			
			case 9:
			sendFrame126("" + c.playerLevel[9] + "", 4026);
			sendFrame126("" + getLevelForXP(c.playerXP[9]) + "", 4027);
			sendFrame126("" + c.playerXP[9] + "", 4110);
			sendFrame126("" + getXPForLevel(getLevelForXP(c.playerXP[9]) + 1) + "", 4111);
			break;
			
			case 10:
			sendFrame126("" + c.playerLevel[10] + "", 4032);
			sendFrame126("" + getLevelForXP(c.playerXP[10]) + "", 4033);
			sendFrame126("" + c.playerXP[10] + "", 4128);
			sendFrame126("" + getXPForLevel(getLevelForXP(c.playerXP[10]) + 1) + "", 4129);
			break;
			
			case 11:
			sendFrame126("" + c.playerLevel[11] + "", 4036);
			sendFrame126("" + getLevelForXP(c.playerXP[11]) + "", 4037);
			sendFrame126("" + c.playerXP[11] + "", 4140);
			sendFrame126("" + getXPForLevel(getLevelForXP(c.playerXP[11]) + 1) + "", 4141);
			break;
			
			case 12:
			sendFrame126("" + c.playerLevel[12] + "", 4024);
			sendFrame126("" + getLevelForXP(c.playerXP[12]) + "", 4025);
			sendFrame126("" + c.playerXP[12] + "", 4104);
			sendFrame126("" + getXPForLevel(getLevelForXP(c.playerXP[12]) + 1) + "", 4105);
			break;
			
			case 13:
			sendFrame126("" + c.playerLevel[13] + "", 4030);
			sendFrame126("" + getLevelForXP(c.playerXP[13]) + "", 4031);
			sendFrame126("" + c.playerXP[13] + "", 4122);
			sendFrame126("" + getXPForLevel(getLevelForXP(c.playerXP[13]) + 1) + "", 4123);
			break;
			
			case 14:
			sendFrame126("" + c.playerLevel[14] + "", 4028);
			sendFrame126("" + getLevelForXP(c.playerXP[14]) + "", 4029);
			sendFrame126("" + c.playerXP[14] + "", 4116);
			sendFrame126("" + getXPForLevel(getLevelForXP(c.playerXP[14]) + 1)+ "", 4117);
			break;
			
			case 15:
			sendFrame126("" + c.playerLevel[15] + "", 4020);
			sendFrame126("" + getLevelForXP(c.playerXP[15]) + "", 4021);
			sendFrame126("" + c.playerXP[15] + "", 4092);
			sendFrame126("" + getXPForLevel(getLevelForXP(c.playerXP[15]) + 1) + "", 4093);
			break;
			
			case 16:
			sendFrame126("" + c.playerLevel[16] + "", 4018);
			sendFrame126("" + getLevelForXP(c.playerXP[16]) + "", 4019);
			sendFrame126("" + c.playerXP[16] + "", 4086);
			sendFrame126("" + getXPForLevel(getLevelForXP(c.playerXP[16]) + 1) + "", 4087);
			break;
			
			case 17:
			sendFrame126("" + c.playerLevel[17] + "", 4022);
			sendFrame126("" + getLevelForXP(c.playerXP[17]) + "", 4023);
			sendFrame126("" + c.playerXP[17] + "", 4098);
			sendFrame126("" + getXPForLevel(getLevelForXP(c.playerXP[17]) + 1) + "", 4099);
			break;
			
			case 18:
			sendFrame126("" + c.playerLevel[18] + "", 12166);
			sendFrame126("" + getLevelForXP(c.playerXP[18]) + "", 12167);
			sendFrame126("" + c.playerXP[18] + "", 12171);
			sendFrame126("" + getXPForLevel(getLevelForXP(c.playerXP[18]) + 1) + "", 12172);
			break;
			
			case 19:
			sendFrame126("" + c.playerLevel[19] + "", 13926);
			sendFrame126("" + getLevelForXP(c.playerXP[19]) + "", 13927);
			sendFrame126("" + c.playerXP[19] + "", 13921);
			sendFrame126("" + getXPForLevel(getLevelForXP(c.playerXP[19]) + 1) + "", 13922);
			break;
			
			case 20:
			sendFrame126("" + c.playerLevel[20] + "", 4152);
			sendFrame126("" + getLevelForXP(c.playerXP[20]) + "", 4153);
			sendFrame126("" + c.playerXP[20] + "", 4157);
			sendFrame126("" + getXPForLevel(getLevelForXP(c.playerXP[20]) + 1) + "", 4158);
			break;
		}
	}
	
	public int getXPForLevel(int level) {
		int points = 0;
		int output = 0;

		for (int lvl = 1; lvl <= level; lvl++) {
			points += Math.floor((double)lvl + 300.0 * Math.pow(2.0, (double)lvl / 7.0));
			if (lvl >= level)
				return output;
			output = (int)Math.floor(points / 4);
		}
		return 0;
	}

	public int getLevelForXP(int exp) {
		int points = 0;
		int output = 0;
		if (exp > 13034430)
			return 99;
		for (int lvl = 1; lvl <= 99; lvl++) {
			points += Math.floor((double) lvl + 300.0
					* Math.pow(2.0, (double) lvl / 7.0));
			output = (int) Math.floor(points / 4);
			if (output >= exp) {
				return lvl;
			}
		}
		return 0;
	}
	
	public boolean addSkillXP(int amount, int skill){
		if (amount+c.playerXP[skill] < 0 || c.playerXP[skill] > 2000000000) {
			return false;
		}
		int oldLevel = getLevelForXP(c.playerXP[skill]);
		c.playerXP[skill] += amount;
		if (oldLevel < getLevelForXP(c.playerXP[skill])) {
			if(skill != 3) { //hp doesn't level
				c.playerLevel[skill] = getLevelForXP(c.playerXP[skill]);
			} else {
				c.playerLevel[skill] += 1; // hp only increases by one
			}
			levelUp(skill);
			c.gfx100(199);
			requestUpdates();
		}
		setSkillLevel(skill, c.playerLevel[skill], c.playerXP[skill]);
		return true;
	}
	

}
