package server.model.players;

// crash-patched by blakeman8192

import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.CountDownLatch;

import server.*;

public class PlayerHandler {

	private static ExecutorService asyncUpdateThreadPool;

	static {
		asyncUpdateThreadPool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
	}

	public Player players[] = new Player[Config.MAX_PLAYERS];
	public int playerSlotSearchStart = 1;

	public static String messageToAll = "";
	public static int playerCount = 0;
	public static String playersCurrentlyOn[] = new String[Config.MAX_PLAYERS];

	public PlayerHandler() {
		for (int i = 0; i < Config.MAX_PLAYERS; i++) {
			players[i] = null;
		}
	}

	public void addClient(int slot, Client newClient) {
		if (newClient == null)
			return;
		players[slot] = newClient;
		players[slot].playerId = slot;
		playerSlotSearchStart = slot + 1;
		if (playerSlotSearchStart > Config.MAX_PLAYERS)
			playerSlotSearchStart = 1;
	}

	public int getFreeSlot() {
		int slot = -1, i = 1;
		do {
			if (players[i] == null) {
				slot = i;
				break;
			}
			i++;
			if (i >= Config.MAX_PLAYERS)
				i = 0; // wrap around
		} while (i <= Config.MAX_PLAYERS);
		return slot;
	}

	public void destruct() {
		for (int i = 0; i < Config.MAX_PLAYERS; i++) {
			if (players[i] == null)
				continue;
			players[i].destruct();
			players[i] = null;
		}
	}

	public static int getPlayerCount() {
		return Thread.activeCount() - 2;
	}

	public void updatePlayerNames() {
		for (int i = 0; i < Config.MAX_PLAYERS; i++) {
			if (players[i] != null) {
				playersCurrentlyOn[i] = players[i].playerName;
			} else {
				playersCurrentlyOn[i] = "";
			}
		}
	}

	public static boolean isPlayerOn(String playerName) {
		for (int i = 0; i < Config.MAX_PLAYERS; i++) {
			if (playersCurrentlyOn[i] != null) {
				if (playersCurrentlyOn[i].equalsIgnoreCase(playerName)) {
					return true;
				}
			}
		}
		return false;
	}

	public void process() throws Exception {
		updatePlayerNames();
		if (messageToAll.length() > 0) {
			int msgTo = 1;
			do {
				if (players[msgTo] != null) {
					players[msgTo].globalMessage = messageToAll;
				}
				msgTo++;
			} while (msgTo < Config.MAX_PLAYERS);
			messageToAll = "";
		}

		for (int i = 0; i < Config.MAX_PLAYERS; i++) {
			try {
				if (players[i] == null || !players[i].isActive)
					continue;
				players[i].process();
				players[i].postProcessing();
				players[i].getNextPlayerMovement();

				if (players[i].disconnected) {
					removePlayer(i);
				}
			} catch (Exception ex) {
				System.out.println("Crash avoided! Disconnecting player: " + players[i].playerName);
				players[i].disconnected = true;
			}
		}

		final CountDownLatch latch = new CountDownLatch(Config.MAX_PLAYERS);
		for (int i = 0; i < Config.MAX_PLAYERS; i++) {
			try {		
				if (players[i] == null || !players[i].isActive) {
					latch.countDown();
					continue;
				} else if (players[i].disconnected) {
					removePlayer(i);
					latch.countDown();
				} else {
					final int r = i;
					asyncUpdateThreadPool.submit(new Runnable() {
						public void run() {
							Updating.updatePlayer(players[r], ((Client) players[r]).outStream);
							Updating.updateNPC(players[r], ((Client) players[r]).outStream);
							((Client) players[r]).flushOutStream();
							latch.countDown();
						}
					});		
				}
			} catch (Exception ex) {
				System.out.println("Crash avoided! Disconnecting player: " + players[i].playerName);
				players[i].disconnected = true;
			}
		}
		latch.await();
		for (int i = 0; i < Config.MAX_PLAYERS; i++) {
			try {
				if (players[i] == null || !players[i].isActive)
					continue;

				players[i].clearUpdateFlags();
			} catch (Exception ex) {
				System.out.println("Crash avoided! Disconnecting player: " + players[i].playerName);
				players[i].disconnected = true;
			}
		}
	}

	private void removePlayer(int i) {
		if (players[i].inTrade) {
			Client o = (Client) Server.playerHandler.players[players[i].tradeWith];
			if (o != null) {
				o.getTradeAndDuel().declineTrade();
			}
		}
		if (players[i].duelStatus == 5) {
			Client o = (Client) Server.playerHandler.players[players[i].duelingWith];
			if (o != null) {
				o.getTradeAndDuel().duelVictory();
			}
		} else if (players[i].duelStatus <= 4 && players[i].duelStatus >= 1) {
			Client o = (Client) Server.playerHandler.players[players[i].duelingWith];
			if (o != null) {
				o.getTradeAndDuel().declineDuel();
			}
		}
		System.out.println(PlayerSave.saveGame(players[i]) ? "Game saved for player " + players[i].playerName : "Could not save for " + players[i].playerName);
		if (players[i].privateChat != 2) {
			for (int index = 1; index < Config.MAX_PLAYERS; index++) {
				if (players[index] == null || players[index].isActive == false)
					continue;
				Client o = (Client) Server.playerHandler.players[index];
				if (o != null) {
					o.getPA().updatePM(players[i].playerId, 0);
				}
			}
		}
		players[i].destruct();
		players[i] = null;
	}

}
