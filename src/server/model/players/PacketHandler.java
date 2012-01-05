package server.model.players;

import server.model.players.packets.*;

import java.util.HashMap;
import java.util.Map;


public class PacketHandler{

	private static Map<Integer, PacketType> packetId = new HashMap<Integer, PacketType>();
	static {
		SlientPacket sp = new SlientPacket();
		packetId.put(3, sp); // clicking another window
		packetId.put(202, sp);
		packetId.put(0, sp);
		packetId.put(77, sp);
		packetId.put(86, sp); // camera angle
		packetId.put(40, new Dialogue());
		ClickObject object = new ClickObject();
		packetId.put(132, object);
		packetId.put(252, object);
		packetId.put(70, object);
		ClickNPC npc = new ClickNPC();
		packetId.put(72, npc);
		packetId.put(131, npc);
		packetId.put(155, npc);
		packetId.put(17, npc);
		packetId.put(121, new ChangeRegions());
		packetId.put(122, new ClickItem());
		packetId.put(241, new ClickingInGame());
		packetId.put(4, new Chat());
		packetId.put(236, new PickupItem());
		packetId.put(87, new DropItem());
		packetId.put(185, new ClickingButtons());
		packetId.put(130, new ClickingStuff());
		packetId.put(103, new Commands());
		packetId.put(214, new MoveItems());
		packetId.put(237, new MagicOnItems());
		packetId.put(181, new MagicOnFloorItems());
		AttackPlayer attPlayer = new AttackPlayer();
		packetId.put(73, attPlayer);
		packetId.put(249, attPlayer);
		packetId.put(128, new ChallengePlayer());
		packetId.put(139, new Trade());
		packetId.put(39, new FollowPlayer());
		packetId.put(41, new WearItem());
		packetId.put(145, new RemoveItem());
		packetId.put(117, new Bank5());
		packetId.put(43, new Bank10());
		packetId.put(129, new BankAll());
		packetId.put(101, new ChangeAppearance());
		PrivateMessaging pm = new PrivateMessaging();
		packetId.put(188, pm);
		packetId.put(126, pm);
		packetId.put(215, pm);
		packetId.put(95, pm);
		BankX bx = new BankX();
		packetId.put(135, bx);
		packetId.put(208, bx);
		Walking w = new Walking();
		packetId.put(248, w);
		packetId.put(164, w);
		packetId.put(98, w);
	}


	public static void processPacket(Client c, int packetType, int packetSize) {	
		if(packetType == -1) {
			return;
		}
		PacketType p = packetId.get(packetType);
		if(p != null) {
			try {
				p.processPacket(c, packetType, packetSize);
			} catch(Exception e) {
				System.out.println("Error handling packet "+packetType+ " - size: "+packetSize);
			}
		} else {
			System.out.println("Unhandled packet type: "+packetType+ " - size: "+packetSize);
		}
	}
	

}
