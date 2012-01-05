package server.model.players.packets;

import server.util.*;
import server.Server;
import server.model.players.*;

/**
 * Walking packet
 **/
public class Walking implements PacketType {

	@Override
	public void processPacket(Client c, int packetType, int packetSize) {
				
		
		c.faceUpdate(0);
		c.npcIndex = 0;
		c.followId = 0;
		
		if(c.duelRule[1] && c.duelStatus == 5) {
			if(Server.playerHandler.players[c.duelingWith] != null) { 
				if(!c.goodDistance(c.getX(), c.getY(), Server.playerHandler.players[c.duelingWith].getX(), Server.playerHandler.players[c.duelingWith].getY(), 1) || c.attackTimer == 0) {
					c.sendMessage("Walking has been disabled in this duel!");
				}
			}
			c.playerIndex = 0;	
			return;		
		}
		if(c.freezeTimer > 0) {
			if(Server.playerHandler.players[c.playerIndex] != null) {
				if(c.goodDistance(c.getX(), c.getY(), Server.playerHandler.players[c.playerIndex].getX(), Server.playerHandler.players[c.playerIndex].getY(), 1)) {
					c.playerIndex = 0;	
					return;
				}
			}
			c.sendMessage("A magical force stops you from moving.");
			c.playerIndex = 0;	
			return;
		}
		
		c.playerIndex = 0;
		
		if((c.duelStatus >= 1 && c.duelStatus <= 4) || c.duelStatus == 6) {
			return;
		}
		
		
		if(c.respawnTimer > 3) {
			return;
		}
		if(c.inTrade) {
			return;
		}
		
		c.getPA().removeAllWindows();

		if(packetType == 248) {
			packetSize -= 14;
		}
		c.newWalkCmdSteps = packetSize - 5;
		if(c.newWalkCmdSteps % 2 != 0)
			Misc.println("Warning: walkTo("+packetType+") command malformed: "+Misc.Hex(c.getInStream().buffer, 0, packetSize));
		c.newWalkCmdSteps /= 2;
		if(++c.newWalkCmdSteps > c.walkingQueueSize) {
			Misc.println("Warning: walkTo("+packetType+") command contains too many steps ("+c.newWalkCmdSteps+").");
			c.newWalkCmdSteps = 0;
			return;
		}
		int firstStepX =  c.getInStream().readSignedWordBigEndianA()-c.getMapRegionX()*8;
		for(int i = 1; i < c.newWalkCmdSteps; i++) {
			c.newWalkCmdX[i] = c.getInStream().readSignedByte();
			c.newWalkCmdY[i] = c.getInStream().readSignedByte();
		}
		c.newWalkCmdX[0] = c.newWalkCmdY[0] = 0;
		int firstStepY = c.getInStream().readSignedWordBigEndian()-c.getMapRegionY()*8;
		c.newWalkCmdIsRunning =  c.getInStream().readSignedByteC() == 1;
		for(int i1 = 0; i1 < c.newWalkCmdSteps; i1++) {
			c.newWalkCmdX[i1] += firstStepX;
			c.newWalkCmdY[i1] += firstStepY;
			
		}
	}

}
