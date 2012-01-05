package server.model.players.packets;

import server.util.*;
import server.model.players.*;

/**
 * Chat
 **/
public class Chat implements PacketType {

	@Override
	public void processPacket(Client c, int packetType, int packetSize) {
		c.chatTextEffects = c.getInStream().readUnsignedByteS();
		c.chatTextColor = c.getInStream().readUnsignedByteS();
        c.chatTextSize = (byte)(c.packetSize - 2);
        c.inStream.readBytes_reverseA(c.chatText, c.chatTextSize, 0);
        c.chatTextUpdateRequired = true;
	}	
}
