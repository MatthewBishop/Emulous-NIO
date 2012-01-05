package server.model.players.packets;

import server.util.*;
import server.model.players.*;

import server.model.items.*;
/**
 * Bank X Items
 **/
public class BankX implements PacketType {

	public static final int PART1 = 135;
	public static final int	PART2 = 208;
	public int XremoveSlot, XinterfaceID, XremoveID, Xamount;
	@Override
	public void processPacket(Client c, int packetType, int packetSize) {
	
		if(packetType == PART1) {
			c.getOutStream().createFrame(27);
			XremoveSlot = c.getInStream().readSignedWordBigEndian();
			XinterfaceID = c.getInStream().readUnsignedWordA();
			XremoveID = c.getInStream().readSignedWordBigEndian();	
		}

		if(packetType == PART2) {			
			Xamount = c.getInStream().readDWord();
			switch(XinterfaceID) {
				case 5064:
				c.getItems().bankItem(c.playerItems[XremoveSlot] , XremoveSlot, Xamount);
				break;
				
				case 5382:
				c.getItems().fromBank(c.playerItems[XremoveSlot] , XremoveSlot, Xamount);
				break;
				
				case 3322:
				if(c.duelStatus <= 0) { 
                	c.getTradeAndDuel().tradeItem(XremoveID, XremoveSlot, Xamount);
            	} else {				
					c.getTradeAndDuel().stakeItem(XremoveID, XremoveSlot, Xamount);
				}  
				break;
				
				case 3415: 
				if(c.duelStatus <= 0) { 
                	c.getTradeAndDuel().fromTrade(XremoveID, XremoveSlot, Xamount);
				} 
				break;
				
				case 6669:
				c.getTradeAndDuel().fromDuel(XremoveID, XremoveSlot, Xamount);
				break;
				
				
			}
			
		}
	
	}

}
