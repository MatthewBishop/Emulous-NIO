package server.model.players;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.security.SecureRandom;

import server.*;
import server.util.*;
import server.model.items.*;
import server.model.shops.*;

public class Client extends Player {

	private static enum State {
		CONNECTED, LOGGING_IN, LOGGED_IN,
	}

	private State state = State.CONNECTED;

	public Stream inStream = null, outStream = null;
	private ItemAssistant itemAssistant = new ItemAssistant(this);
	private ShopAssistant shopAssistant = new ShopAssistant(this);
	private TradeAndDuel tradeAndDuel = new TradeAndDuel(this);
	private PlayerAssistant playerAssistant = new PlayerAssistant(this);
	private CombatAssistant combatAssistant = new CombatAssistant(this);
	private ActionHandler actionHandler = new ActionHandler(this);
	public Cryption inStreamDecryption = null, outStreamDecryption = null;
	public int lowMemoryVersion = 0;
	public int timeOutCounter = 0;
	public int returnCode = 2;

	private final ByteBuffer inData;
	public final SocketChannel socketChannel;
	private final SelectionKey key;

	public Client(SelectionKey key) {
		super();
		this.key = key;
		this.socketChannel = (SocketChannel) key.channel();
		this.inData = ByteBuffer.allocateDirect(512);
		outStream = new Stream(new byte[Config.BUFFER_SIZE]);
		outStream.currentOffset = 0;
		inStream = new Stream(new byte[Config.BUFFER_SIZE]);
		inStream.currentOffset = 0;
	}

	public void flushOutStream() {
		if (disconnected || outStream.currentOffset == 0)
			return;
		synchronized (this) {
			try {
				ByteBuffer buffer = ByteBuffer.allocate(outStream.currentOffset);
				buffer.put(outStream.buffer, 0, outStream.currentOffset);
				buffer.flip();
				socketChannel.write(buffer);
				outStream.currentOffset = 0;
				outStream.reset();
				notify();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private void directFlushOutStream() throws java.io.IOException {
		ByteBuffer buffer = ByteBuffer.allocate(outStream.currentOffset);
		buffer.put(outStream.buffer, 0, outStream.currentOffset);
		buffer.flip();
		socketChannel.write(buffer);
		outStream.currentOffset = 0;
	}

	public static final int packetSizes[] = { 0, 0, 0, 1, -1, 0, 0, 0, 0, 0, // 0
	0, 0, 0, 0, 8, 0, 6, 2, 2, 0, // 10
	0, 2, 0, 6, 0, 12, 0, 0, 0, 0, // 20
	0, 0, 0, 0, 0, 8, 4, 0, 0, 2, // 30
	2, 6, 0, 6, 0, -1, 0, 0, 0, 0, // 40
	0, 0, 0, 12, 0, 0, 0, 8, 0, 0, // 50
	8, 0, 0, 0, 0, 0, 0, 0, 0, 0, // 60
	6, 0, 2, 2, 8, 6, 0, -1, 0, 6, // 70
	0, 0, 0, 0, 0, 1, 4, 6, 0, 0, // 80
	0, 0, 0, 0, 0, 3, 0, 0, -1, 0, // 90
	0, 13, 0, -1, 0, 0, 0, 0, 0, 0,// 100
	0, 0, 0, 0, 0, 0, 0, 6, 0, 0, // 110
	1, 0, 6, 0, 0, 0, -1, 0, 2, 6, // 120
	0, 4, 6, 8, 0, 6, 0, 0, 0, 2, // 130
	0, 0, 0, 0, 0, 6, 0, 0, 0, 0, // 140
	0, 0, 1, 2, 0, 2, 6, 0, 0, 0, // 150
	0, 0, 0, 0, -1, -1, 0, 0, 0, 0,// 160
	0, 0, 0, 0, 0, 0, 0, 0, 0, 0, // 170
	0, 8, 0, 3, 0, 2, 0, 0, 8, 1, // 180
	0, 0, 12, 0, 0, 0, 0, 0, 0, 0, // 190
	2, 0, 0, 0, 0, 0, 0, 0, 4, 0, // 200
	4, 0, 0, 0, 7, 8, 0, 0, 10, 0, // 210
	0, 0, 0, 0, 0, 0, -1, 0, 6, 0, // 220
	1, 0, 0, 0, 6, 0, 6, 8, 1, 0, // 230
	0, 4, 0, 0, 0, 0, -1, 0, -1, 4,// 240
	0, 0, 6, 6, 0, 0, 0 // 250
	};

	private boolean processLogin() {
		isActive = false;
		long serverSessionKey = 0, clientSessionKey = 0;
		serverSessionKey = ((long) (java.lang.Math.random() * 99999999D) << 32) + (long) (java.lang.Math.random() * 99999999D);
		try {
			if (state == State.CONNECTED) {
				if (inData.remaining() < 2) {
					inData.compact();
					return false;
				}
				int request = inData.get() & 0xff;
				if (request != 14) {
					throw new Exception("Expect login byte 14 from client.");
				}
				@SuppressWarnings("unused")
				int namePart = inData.get();
				outStream.writeQWord(0);
				outStream.writeByte(0);
				outStream.writeQWord(serverSessionKey);//new SecureRandom().nextLong()
				directFlushOutStream();
				state = State.LOGGING_IN;
			} else if (state == State.LOGGING_IN) {
				if (inData.remaining() < 2) {
					inData.compact();
					return false;
				}
				int loginType = inData.get() & 0xFF;
				if (loginType != 16 && loginType != 18) {
					throw new Exception("Unexpected login type "+loginType);
				}
				int loginPacketSize = inData.get() & 0xFF;
				int loginEncryptPacketSize = loginPacketSize - (36 + 1 + 1 + 2);
				if (loginEncryptPacketSize <= 0) {
					throw new Exception("Zero RSA packet size");
				}
				if (inData.remaining() < loginPacketSize) {
					inData.flip();
					inData.compact();
					return false;
				}
				if ((inData.get() & 0xFF) != 255) {
					throw new Exception("Wrong login packet magic ID");
				}
				if (ByteBufferUtils.readSignedWord(inData) != 317) {
					throw new Exception("Wrong login packet client revision");
				}
				lowMemoryVersion = inData.get() & 0xFF;

				for (int i = 0; i < 9; i++) {
					Integer.toHexString(ByteBufferUtils.readDWord(inData));
				}

				loginEncryptPacketSize--;
				int tmp = inData.get() & 0xFF;
				if (loginEncryptPacketSize != tmp) {
					throw new Exception("Encrypted packet data length ("+loginEncryptPacketSize+") different from length byte thereof ("+tmp+")");
				}
				tmp = inData.get() & 0xFF;
				if (tmp != 10) {
					throw new Exception("Encrypted packet Id was "+tmp+" but expected 10");
				}
				clientSessionKey = ByteBufferUtils.readQWord(inData);
				serverSessionKey = ByteBufferUtils.readQWord(inData);
				int UID = ByteBufferUtils.readDWord(inData);

				if (UID == 0 || UID == 99735086) { // all for free bot and syi
													// will get ip banned
													// straight away
					ConnectionHandler.addIpToBanList(socketChannel.socket().getInetAddress().getHostAddress());
					ConnectionHandler.addIpToFile(socketChannel.socket().getInetAddress().getHostAddress());
					returnCode = 11;
					disconnected = true;
					saveFile = false;
				}

				playerName = ByteBufferUtils.readString(inData);
				playerName = playerName.replaceAll("_", " ");
				playerName = playerName.replaceAll("[^A-z 0-9]", " ");
				playerName = (playerName.substring(0, 1).toUpperCase() + playerName.substring(1).toLowerCase()).trim();
				if (playerName == null || playerName.length() == 0)
					throw new Exception("Blank username.");
				playerPass = ByteBufferUtils.readString(inData);
				Misc.println(playerName + " has connected - UID: " + UID);

				int sessionKey[] = new int[4];
				sessionKey[0] = (int) (clientSessionKey >> 32);
				sessionKey[1] = (int) clientSessionKey;
				sessionKey[2] = (int) (serverSessionKey >> 32);
				sessionKey[3] = (int) serverSessionKey;

				for (int i = 0; i < 4; i++) {
					Integer.toHexString(sessionKey[i]);
				}

				inStreamDecryption = new Cryption(sessionKey);
				for (int i = 0; i < 4; i++)
					sessionKey[i] += 50;

				for (int i = 0; i < 4; i++)
					Integer.toHexString(sessionKey[i]);

				outStreamDecryption = new Cryption(sessionKey);
				outStream.packetEncryption = outStreamDecryption;
				state = State.LOGGED_IN;
				returnCode = 2;
				int slot = Server.playerHandler.getFreeSlot();
				if(slot == -1) {
					// world full!
					returnCode = 7;
				} 
				if (ConnectionHandler.isNamedBanned(playerName)) {
					returnCode = 4;
					disconnected = true;
					saveFile = false;
				}
				if (Server.playerHandler.isPlayerOn(playerName)) {
					returnCode = 5;
					disconnected = true;
					saveFile = false;
				}

				int load = PlayerSave.loadGame((Player) key.attachment(), playerName, playerPass);
				if (load == 2) {
					returnCode = 3;
					disconnected = true;
					saveFile = false;
				}
				
				outStream.writeByte(returnCode);
				outStream.writeByte(playerRights > 2 ? 2 : playerRights);
				outStream.writeByte(0);
				directFlushOutStream();
				if(returnCode == 2) {
					isActive = true;
					initialize();
					Server.playerHandler.addClient(slot, this);
				}
				return true;
			}
		} catch (Exception e) {
			Misc.println("Login fail from " + socketChannel.socket().getInetAddress().getHostAddress());
			destruct();
			disconnected = true;
			return false;
		}
		return false;
	}

	public void destruct() {
		if(this.key == null) 
			return; // already shutdown
		try {
			Misc.println("ClientHandler: Client " + playerName + " disconnected.");
			disconnected = true;
			this.key.attach(null);
			this.socketChannel.close();
			inStream = null;
			outStream = null;
			isActive = false;
			synchronized (this) {
				notify();
			} // make sure this threads gets control so it can terminate
		} catch (java.io.IOException ioe) {
			ioe.printStackTrace();
		} finally {
			ConnectionHandler.removeIp(socketChannel.socket().getInetAddress().getHostAddress());
			key.cancel();
			super.destruct();
		}
	}

	public void sendMessage(String s) {
		if (getOutStream() != null) {
			outStream.createFrameVarSize(253);
			outStream.writeString(s);
			outStream.endFrameVarSize();
		}
	}

	public void setSidebarInterface(int menuId, int form) {
		if (getOutStream() != null) {
			outStream.createFrame(71);
			outStream.writeWord(form);
			outStream.writeByteA(menuId);
		}
	}

	public void initialize() {
		outStream.createFrame(249);
		outStream.writeByteA(1); // 1 for members, zero for free
		outStream.writeWordBigEndianA(playerId);
		for (int i = 0; i < 25; i++) {
			getPA().setSkillLevel(i, playerLevel[i], playerXP[i]);
			getPA().refreshSkill(i);
		}
		for (int p = 0; p < PRAYER.length; p++) { // reset prayer glows
			prayerActive[p] = false;
			getPA().sendFrame36(PRAYER_GLOW[p], 0);
		}
		getPA().sendFrame107(); // reset screen
		getPA().setChatOptions(0, 0, 0); // reset private messaging options
		setSidebarInterface(1, 3917);
		setSidebarInterface(2, 638);
		setSidebarInterface(3, 3213);
		setSidebarInterface(4, 1644);
		setSidebarInterface(5, 5608);
		if (playerMagicBook == 0) {
			setSidebarInterface(6, 1151); // modern
		} else {
			setSidebarInterface(6, 12855); // ancient
		}
		setSidebarInterface(7, 1);
		setSidebarInterface(8, 5065);
		setSidebarInterface(9, 5715);
		setSidebarInterface(10, 2449);
		setSidebarInterface(11, 4445);
		setSidebarInterface(12, 147);
		setSidebarInterface(13, 6299);
		setSidebarInterface(0, 2423);
		getPA().sendFrame36(43, 0);
		if (newPlayer) {
			getPA().showInterface(3559);
			newPlayer = false;
		}
		getPA().showOption(4, 0, "Trade With", 3);
		getPA().showOption(5, 0, "Follow", 4);
		getItems().resetItems(3214);
		getItems().sendWeapon(playerEquipment[playerWeapon], getItems().getItemName(playerEquipment[playerWeapon]));
		getItems().resetBonus();
		getItems().getBonus();
		getItems().writeBonus();
		getPA().writeQuestTab();
		getItems().setEquipment(playerEquipment[playerHat], 1, playerHat);
		getItems().setEquipment(playerEquipment[playerCape], 1, playerCape);
		getItems().setEquipment(playerEquipment[playerAmulet], 1, playerAmulet);
		getItems().setEquipment(playerEquipment[playerArrows], playerEquipmentN[playerArrows], playerArrows);
		getItems().setEquipment(playerEquipment[playerChest], 1, playerChest);
		getItems().setEquipment(playerEquipment[playerShield], 1, playerShield);
		getItems().setEquipment(playerEquipment[playerLegs], 1, playerLegs);
		getItems().setEquipment(playerEquipment[playerHands], 1, playerHands);
		getItems().setEquipment(playerEquipment[playerFeet], 1, playerFeet);
		getItems().setEquipment(playerEquipment[playerRing], 1, playerRing);
		getItems().setEquipment(playerEquipment[playerWeapon], playerEquipmentN[playerWeapon], playerWeapon);
		sendMessage("Welcome to " + Config.SERVER_NAME);
		getCombat().getPlayerAnimIndex(getItems().getItemName(playerEquipment[playerWeapon]).toLowerCase());
		getPA().logIntoPM();
		getItems().addSpecialBar(playerEquipment[playerWeapon]);
		saveTimer = Config.SAVE_TIMER;
		Updating.updatePlayer(this, outStream);
		Updating.updateNPC(this, outStream);
		flushOutStream();
	}

	public void logout() {
		if (System.currentTimeMillis() - logoutDelay > 10000) {
			outStream.createFrame(109);
		} else {
			sendMessage("You must wait a few seconds from being out of combat to logout.");
		}
	}

	public void packetProcess() {
		try {
			if (timeOutCounter++ > 20) {
				disconnected = true;
				return;
			}
			if (socketChannel.read(inData) == -1) {
				disconnected = true;
				return;
			}
			// Handle the received data.
			inData.flip();
			while (inData.hasRemaining()) {
				// Handle login if we need to.
				if (state != State.LOGGED_IN) {
					processLogin();
					break;
				}
				if (packetType == -1) {
					packetType = inData.get() & 0xff;
					if (inStreamDecryption != null)
						packetType = packetType - inStreamDecryption.getNextKey() & 0xff;
				}
				if (packetSize == -1) {
					packetSize = packetSizes[packetType];
					if (packetSize == -1) {
						if (!inData.hasRemaining()) {
							inData.flip();
							inData.compact();
							break;
						}
						packetSize = inData.get() & 0xff;
					}
				}
				if (inData.remaining() >= packetSize) {
					inStream.currentOffset = 0;
					inData.get(inStream.buffer, 0, packetSize);
					PacketHandler.processPacket(this, packetType, packetSize);// method
																				// that
																				// does
																				// actually
																				// interprete
																				// these
																				// packets
					// Reset for the next packet.
					packetType = -1;
					packetSize = -1;
					timeOutCounter = 0; // reset
				} else { // packet not completely arrived here yet
					inData.flip();
					inData.compact();
					break;
				}
			}
			// Clear everything for the next read.
			inData.clear();
		} catch (java.lang.Exception __ex) {
			__ex.printStackTrace();
			disconnected = true;
		}
	}

	public int packetSize = 0, packetType = -1;

	public boolean process() {

		if (System.currentTimeMillis() - duelDelay > 800 && duelCount > 0) {
			if (duelCount != 1) {
				forcedChat("" + (--duelCount));
				duelDelay = System.currentTimeMillis();
			} else {
				forcedChat("FIGHT!");
				duelCount = 0;
			}
		}

		if (System.currentTimeMillis() - specDelay > Config.INCREASE_SPECIAL_AMOUNT) {
			specDelay = System.currentTimeMillis();
			if (specAmount < 10) {
				specAmount++;
				getItems().addSpecialBar(playerEquipment[playerWeapon]);
			}
		}

		if (clickObjectType > 0 && goodDistance(objectX + objectXOffset, objectY + objectYOffset, getX(), getY(), objectDistance)) {
			if (clickObjectType == 1) {
				getActions().firstClickObject(objectId);
			}
			if (clickObjectType == 2) {
				getActions().secondClickObject(objectId);
			}
			if (clickObjectType == 3) {
				getActions().thirdClickObject(objectId);
			}
		}

		if ((clickNpcType > 0) && Server.npcHandler.npcs[npcIndex] != null) {
			if (goodDistance(getX(), getY(), Server.npcHandler.npcs[npcIndex].getX(), Server.npcHandler.npcs[npcIndex].getY(), 1)) {
				if (clickNpcType == 1) {
					getActions().firstClickNpc(npcType);
				}
				if (clickNpcType == 2) {
					getActions().secondClickNpc(npcType);
				}
			}
		}

		getPA().sendFrame126("Players Online: @WHI@" + PlayerHandler.getPlayerCount(), 663);
		if (walkingToItem) {
			if (getX() == pItemX && getY() == pItemY) {
				walkingToItem = false;
				Server.itemHandler.removeGroundItem(this, pItemId, pItemX, pItemY, true);
			}
		}

		if (followId > 0) {
			getPA().followPlayer();
		}

		if (System.currentTimeMillis() - prayerDelay > getCombat().getPrayerDelay() && usingPrayer) {
			getCombat().reducePrayerLevel();
		}

		if (System.currentTimeMillis() - singleCombatDelay > 5000) {
			underAttackBy = 0;
		}

		if (System.currentTimeMillis() - restoreStatsDelay > 40000) {
			restoreStatsDelay = System.currentTimeMillis();
			for (int level = 0; level < playerLevel.length; level++) {
				if (playerLevel[level] < getLevelForXP(playerXP[level])) {
					if (level != 5) { // prayer doesn't restore
						playerLevel[level] += 1;
						getPA().setSkillLevel(level, playerLevel[level], playerXP[level]);
						getPA().refreshSkill(level);
					}
				} else if (playerLevel[level] > getLevelForXP(playerXP[level])) {
					playerLevel[level] -= 1;
					getPA().setSkillLevel(level, playerLevel[level], playerXP[level]);
					getPA().refreshSkill(level);
				}
			}
		}

		if (System.currentTimeMillis() - teleGrabDelay > 1550 && usingMagic) {
			usingMagic = false;
			if (Server.itemHandler.itemExists(teleGrabItem, teleGrabX, teleGrabY)) {
				Server.itemHandler.removeGroundItem(this, teleGrabItem, teleGrabX, teleGrabY, true);
			}
		}

		if (inWild()) {
			int oldlevel = wildLevel;
			wildLevel = (((absY - 3520) / 8) + 1);
			getPA().walkableInterface(197);
			if (Config.SINGLE_AND_MULTI_ZONES) {
				if (inMulti()) {
					getPA().sendFrame126("@red@M @yel@Lvl: " + wildLevel, 199);
				} else {
					getPA().sendFrame126("@gre@S @yel@Lvl: " + wildLevel, 199);
				}
			} else {
				getPA().sendFrame126("@yel@Level: " + wildLevel, 199);
			}
			getPA().showOption(3, 0, "Attack", 1);
		} else if (inDuelArena()) {
			getPA().walkableInterface(201);
			if (duelStatus == 5) {
				getPA().showOption(3, 0, "Attack", 1);
			} else {
				getPA().showOption(3, 0, "Challenge", 1);
			}
		} else if (inBarrows()) {
			getPA().sendFrame99(2);
			getPA().sendFrame126("Kill Count: " + barrowsKillCount, 4536);
			getPA().walkableInterface(4535);

		} else {

			getPA().sendFrame99(0);
			getPA().walkableInterface(-1);
			getPA().showOption(3, 0, "Null", 1);
		}

		if (playerRights >= 2) {
			getPA().sendFrame126("X: " + absX + " Y: " + absY, 184);
		}

		if (isDead && respawnTimer == -6) {
			getPA().applyDead();
		}

		if (skullTimer > 0) {
			skullTimer--;
		}

		if (skullTimer == 1) {
			isSkulled = false;
			attackedPlayers.clear();
			headIconPk = 0;
			getPA().requestUpdates();
		}

		if (respawnTimer == 9) {
			respawnTimer--;
			getPA().giveLife();
		}
		if (respawnTimer == 14) {
			respawnTimer--;
			startAnimation(0x900);
		}
		if (respawnTimer > -6) {
			respawnTimer--;
		}
		if (freezeTimer > -6) {
			freezeTimer--;
		}
		if (hitDelay > 0) {
			hitDelay--;
		}
		if (teleTimer > 0) {
			teleTimer--;
		}

		if (teleTimer == 1 && newLocation > 0) {
			teleTimer = 0;
			getPA().changeLocation();
		}
		if (teleTimer == 5) {
			teleTimer--;
			getPA().processTeleport();
		}
		if (teleTimer == 9 && teleGfx > 0) {
			teleTimer--;
			gfx100(teleGfx);
		}
		if (wearItemTimer > 0) {
			wearItemTimer--;
		}
		if (wearItemTimer == 1) {
			getItems().wearItem(wearId, wearSlot);
		}

		if (hitDelay == 2 && projectileStage == 1) {
			if (Server.npcHandler.npcs[oldNpcIndex] != null) {
				getCombat().fireProjectileNpc();
			}
			if (Server.playerHandler.players[oldPlayerIndex] != null) {
				getCombat().fireProjectilePlayer();
			}
		}
		if (hitDelay == 1) {
			if (oldNpcIndex > 0) {
				getCombat().delayedHit(oldNpcIndex);
			}
			if (oldPlayerIndex > 0) {
				getCombat().playerDelayedHit(oldPlayerIndex);
			}

		}
		if (saveTimer > 0) {
			saveTimer--;
		}
		if (saveTimer == 1) {
			PlayerSave.saveGame(Server.playerHandler.players[playerId]);
			saveTimer = Config.SAVE_TIMER;
		}
		if (attackTimer > 0) {
			attackTimer--;
		}
		if (attackTimer == 1) {
			if (npcIndex > 0) {
				attackTimer = 0;
				getCombat().attackNpc(npcIndex);
			}
			if (playerIndex > 0) {
				attackTimer = 0;
				getCombat().attackPlayer(playerIndex);
			}
		}
		timeOutCounter++;
		if (timeOutCounter > Config.TIMEOUT) {
			Misc.println(playerName + " has timed out.");
			disconnected = true;
		}

		if (inTrade && tradeResetNeeded) {
			Client o = (Client) Server.playerHandler.players[tradeWith];
			if (o != null) {
				if (o.tradeResetNeeded) {
					getTradeAndDuel().resetTrade();
					o.getTradeAndDuel().resetTrade();
				}
			}
		}

		return false;
	}

	public Stream getInStream() {
		return inStream;
	}

	public int getPacketType() {
		return packetType;
	}

	public int getPacketSize() {
		return packetSize;
	}

	public Stream getOutStream() {
		return outStream;
	}

	public ItemAssistant getItems() {
		return itemAssistant;
	}

	public PlayerAssistant getPA() {
		return playerAssistant;
	}

	public ShopAssistant getShops() {
		return shopAssistant;
	}

	public TradeAndDuel getTradeAndDuel() {
		return tradeAndDuel;
	}

	public CombatAssistant getCombat() {
		return combatAssistant;
	}

	public ActionHandler getActions() {
		return actionHandler;
	}

}
