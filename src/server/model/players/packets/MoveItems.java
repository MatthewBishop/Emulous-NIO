package server.model.players.packets;

import server.util.*;
import server.model.players.*;

import server.model.items.*;

/**
 * Move Items
 **/
public class MoveItems implements PacketType {

	@Override
	public void processPacket(Client c, int packetType, int packetSize) {
		int somejunk = c.getInStream().readUnsignedWordA(); //junk
		int itemFrom =  c.getInStream().readUnsignedWordA();// slot1
		int itemTo = (c.getInStream().readUnsignedWordA() -128);// slot2
		c.getItems().moveItems(itemFrom, itemTo, somejunk);
	}
}
