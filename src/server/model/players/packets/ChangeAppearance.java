package server.model.players.packets;

import server.*;
import server.util.*;
import server.model.players.*;

/**
 * Change appearance
 **/
public class ChangeAppearance implements PacketType {

	@Override
	public void processPacket(Client c, int packetType, int packetSize) {
		int gender = c.getInStream().readSignedByte();
		int head = c.getInStream().readSignedByte();
		int jaw = c.getInStream().readSignedByte();
		int torso = c.getInStream().readSignedByte();
		int arms = c.getInStream().readSignedByte();
		int hands = c.getInStream().readSignedByte();
		int legs = c.getInStream().readSignedByte();
		int feet = c.getInStream().readSignedByte();
		int hairColour = c.getInStream().readSignedByte();
		int torsoColour = c.getInStream().readSignedByte();
		int legsColour = c.getInStream().readSignedByte();
		int feetColour = c.getInStream().readSignedByte();
		int skinColour = c.getInStream().readSignedByte();
		
		c.playerAppearance[0] = gender; // gender
		c.playerAppearance[1] = head; // head
		c.playerAppearance[2] = torso;// Torso
		c.playerAppearance[3] = arms; // arms
		c.playerAppearance[4] = hands; // hands
		c.playerAppearance[5] = legs; // legs
		c.playerAppearance[6] = feet; // feet
		c.playerAppearance[7] = jaw; // beard
		c.playerAppearance[8] = hairColour; // hair colour
		c.playerAppearance[9] = torsoColour; // torso colour
		c.playerAppearance[10] = legsColour; // legs colour
		c.playerAppearance[11] = feetColour; // feet colour
		c.playerAppearance[12] = skinColour; // skin colour

		c.getPA().removeAllWindows();
		c.getPA().requestUpdates();
	}	
}
