package server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

import server.util.*;
import server.model.players.*;
import server.model.npcs.*;
import server.world.*;

public class Server/* implements Runnable*/ {

	public Server() {
	}

	public static void main(java.lang.String args[]) {
		try {
			address = new InetSocketAddress(ServerlistenerPort);
			// Initialize the networking objects.
			selector = Selector.open();
			serverChannel = ServerSocketChannel.open();
			// ... and configure them!
			serverChannel.configureBlocking(false);
			serverChannel.socket().bind(address);
			serverChannel.register(selector, SelectionKey.OP_ACCEPT);
			System.out.println("Starting server on " + address);
		} catch (ClosedChannelException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	//	clientHandler = new Server();
		ConnectionHandler.initialize();
	//	(new Thread(clientHandler)).start();
		long lastTicks = System.currentTimeMillis();
		while (!shutdownServer) {
			// First, handle all network events.
			try {
				selector.selectNow();
				for (SelectionKey selectionKey : selector.selectedKeys()) {
					if (selectionKey.isAcceptable()) {
						accept(); // Accept a new connection.
					}
					if (selectionKey.isReadable()) {
						// Tell the client to handle the packet.
						((Player) selectionKey.attachment()).packetProcess();
					}
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			itemHandler.process();
			playerHandler.process();
			npcHandler.process();
			shopHandler.process();
			objectHandler.process();
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
	//	clientHandler.killServer();
	//	clientHandler = null;

	}

	private static void accept() throws IOException {
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
				// No more connections to accept (as this one was invalid).
				break;
			}
			String connectingHost = socket.socket().getInetAddress().getHostAddress();
			if (!ConnectionHandler.floodProtection(connectingHost) && !ConnectionHandler.containsIp(connectingHost) && !ConnectionHandler.isIpBanned(connectingHost)) {
				ConnectionHandler.addIp(connectingHost);
				// Set up the new connection.
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
	
	private static Selector selector;
	private static InetSocketAddress address;
	private static ServerSocketChannel serverChannel;

	public static Server clientHandler = null;
	public static ServerSocket clientListener = null;
	public static boolean shutdownServer = false;
	public static boolean shutdownClientHandler;
	public static int ServerlistenerPort = 43594;
	public static ItemHandler itemHandler = new ItemHandler();
	public static PlayerHandler playerHandler = new PlayerHandler();
	public static NPCHandler npcHandler = new NPCHandler();
	public static ShopHandler shopHandler = new ShopHandler();
	public static ObjectHandler objectHandler = new ObjectHandler();
/*
	public void run() {
		try {
			shutdownClientHandler = false;
			clientListener = new ServerSocket(ServerlistenerPort, 1, null);
			Misc.println("Starting " + Config.SERVER_NAME + " on " + clientListener.getInetAddress().getHostAddress() + ":" + clientListener.getLocalPort());
			while (true) {
				java.net.Socket s = clientListener.accept();
				s.setTcpNoDelay(true);
				String connectingHost = s.getInetAddress().getHostName();
				if (!ConnectionHandler.floodProtection(connectingHost) && !ConnectionHandler.containsIp(connectingHost) && !ConnectionHandler.isIpBanned(connectingHost)) {
					ConnectionHandler.addIp(connectingHost);
					Misc.println("ClientHandler: Accepted from " + connectingHost + ":" + s.getPort());
					playerHandler.newPlayerClient(s, connectingHost);
				} else {
					Misc.println("ClientHandler: Rejected " + connectingHost + ":" + s.getPort());
					s.close();
				}
			}
		} catch (java.io.IOException ioe) {
			if (!shutdownClientHandler)
				Misc.println("Error: Unable to startup listener on " + ServerlistenerPort + " - port already in use?");
			else
				Misc.println("ClientHandler was shut down.");
		}
	}

	public void killServer() {
		try {
			shutdownClientHandler = true;
			if (clientListener != null)
				clientListener.close();
			clientListener = null;
		} catch (java.lang.Exception __ex) {
			__ex.printStackTrace();
		}
	}*/

}
