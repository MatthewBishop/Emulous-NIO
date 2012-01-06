package server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

import server.util.*;
import server.model.players.*;
import server.model.npcs.*;
import server.world.*;

/**
 * Main class and also the hypothetical heart of the Server.
 * Handles the initialization of the server, and also connection listening.
 * 
 * @author Advocatus, Blakeman8192(I stole some of the code from him, don't tell anyone), Hayzie(maker of emulous), and all who came before him. 
 * 
 */
public class Server implements Runnable {

	public static ItemHandler itemHandler = new ItemHandler();
	public static PlayerHandler playerHandler = new PlayerHandler();
	public static NPCHandler npcHandler = new NPCHandler();
	public static ShopHandler shopHandler = new ShopHandler();
	public static ObjectHandler objectHandler = new ObjectHandler();

	private Selector selector;
	private InetSocketAddress address;
	private ServerSocketChannel serverChannel;
	public boolean shutdownServer = false;

	/**
	 * Initializes a new server instance, and initializes a new connection listener.
	 */
	public Server() {
		try {
			address = new InetSocketAddress(Config.SERVER_PORT);
			selector = Selector.open();
			serverChannel = ServerSocketChannel.open();
			serverChannel.configureBlocking(false);
			serverChannel.socket().bind(address);
			serverChannel.register(selector, SelectionKey.OP_ACCEPT);
			System.out.println("Starting server on " + address);
		} catch (IOException e) {
			e.printStackTrace();
		} 
	}

	public static void main(java.lang.String args[]) {
		new Thread(new Server()).start();
		ConnectionHandler.initialize();
	}

	private void accept() throws IOException {
		SocketChannel socket;
		/*
		 * Here we use a for loop so that we can accept multiple clients per
		 * cycle for lower latency. We limit the amount of clients that we
		 * accept per cycle to better combat potential denial of service type
		 * attacks.
		 */
		for (int i = 0; i < 10; i++) {
			socket = serverChannel.accept();
			if (socket == null) {
				break;
			}
			String connectingHost = socket.socket().getInetAddress().getHostAddress();
			if (!ConnectionHandler.floodProtection(connectingHost) && !ConnectionHandler.containsIp(connectingHost) && !ConnectionHandler.isIpBanned(connectingHost)) {
				ConnectionHandler.addIp(connectingHost);
				socket.configureBlocking(false);
				SelectionKey key = socket.register(selector, SelectionKey.OP_READ);
				Client client = new Client(key);
				key.attach(client);
				System.out.println("ClientHandler: Accepted from " + connectingHost);
			} else {
				Misc.println("ClientHandler: Rejected " + connectingHost);
				socket.close();
				continue;
			}
		}
	}

	@Override
	public void run() {
		long lastTicks = System.currentTimeMillis();
		while (!shutdownServer) {
			try {
				selector.selectNow();
				for (SelectionKey selectionKey : selector.selectedKeys()) {
					if (selectionKey.isAcceptable()) {
						accept();
					}
					if (selectionKey.isReadable()) {
						((Player) selectionKey.attachment()).packetProcess();
					}
				}
				itemHandler.process();
				playerHandler.process();
				npcHandler.process();
				shopHandler.process();
				objectHandler.process();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			long timeSpent = System.currentTimeMillis() - lastTicks;
			if (timeSpent >= Config.CYCLE_TIME) {
				timeSpent = Config.CYCLE_TIME;
			}
			try {
				Thread.sleep(Config.CYCLE_TIME - timeSpent);
			} catch (java.lang.Exception _ex) {
			}
			lastTicks = System.currentTimeMillis();
		}
		playerHandler.destruct();
	}

}
