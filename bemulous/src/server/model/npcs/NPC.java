package server.model.npcs;

import server.util.*;
import server.*;

public class NPC {
	public int npcId;
	public int npcType;
	public int absX, absY;
	public int heightLevel;
	public int makeX, makeY, maxHit, defence, attack, moveX, moveY, direction, walkingType;
	public int spawnX, spawnY;
    public int viewX, viewY;
	public int attackType, projectileId, endGfx, spawnedBy, hitDelayTimer, HP, MaxHP, hitDiff, animNumber, actionTimer, enemyX, enemyY;
	public boolean applyDead, isDead, needRespawn, respawns;
	public boolean walkingHome, underAttack;
	public int freezeTimer, attackTimer, killerId, killedBy;
	public boolean randomWalk;
	public boolean dirUpdateRequired;
	public boolean animUpdateRequired;
	public boolean hitUpdateRequired;
	public boolean updateRequired;
	public boolean forcedChatRequired;
    public boolean faceToUpdateRequired;
	public String forcedText;
	
	public NPC(int _npcId, int _npcType) {
		npcId = _npcId;
		npcType = _npcType;
		direction = -1;
		isDead = false;
		applyDead = false;
		actionTimer = 0;
		randomWalk = true;
		
	}
	
	
	public void updateNPCMovement(Stream str) {
		if (direction == -1) {
			
			if (updateRequired) {
				
				str.writeBits(1, 1);
				str.writeBits(2, 0);
			} else {
				str.writeBits(1, 0);
			}
		} else {
			
			str.writeBits(1, 1);
			str.writeBits(2, 1);		
			str.writeBits(3, Misc.xlateDirectionToClient[direction]);
			if (updateRequired) {
				str.writeBits(1, 1);		
			} else {
				str.writeBits(1, 0);
			}
		}
	}

	/**
	* Text update
	**/
	
	public void forceChat(String text) {
		forcedText = text;
		forcedChatRequired = true;
		updateRequired = true;
	}
	
	/**
	*Graphics
	**/	
	
	public int mask80var1 = 0;
    public int mask80var2 = 0;
    protected boolean mask80update = false;
	
    public void appendMask80Update(Stream str) {
		str.writeWord(mask80var1);
	    str.writeDWord(mask80var2);
    }
	
	public void gfx100(int gfx){
		mask80var1 = gfx;
        mask80var2 = 6553600;
        mask80update = true;
		updateRequired = true;
	}
	
	public void gfx0(int gfx){
		mask80var1 = gfx;
        mask80var2 = 65536;
        mask80update = true;
		updateRequired = true;
	}
	
	public void appendAnimUpdate(Stream str) {
		str.writeWordBigEndian(animNumber);
		str.writeByte(1);
	}
	
	/**
	*
	Face
	*
	**/
	
	public int FocusPointX = -1, FocusPointY = -1;
	public int face = 0;
	
	private void appendSetFocusDestination(Stream str) {
        str.writeWordBigEndian(FocusPointX);
        str.writeWordBigEndian(FocusPointY);
    }
	
	public void turnNpc(int i, int j) {
        FocusPointX = 2 * i + 1;
        FocusPointY = 2 * j + 1;
        updateRequired = true;

    }
	
	public void appendFaceEntity(Stream str) {
		str.writeWord(face);
	}
        	
	public void facePlayer(int player) {
		face = player + 32768;
		dirUpdateRequired = true;
		updateRequired = true;
	}

	public void appendFaceToUpdate(Stream str) {
			str.writeWordBigEndian(viewX);
			str.writeWordBigEndian(viewY);
	}
	
	
	public void appendNPCUpdateBlock(Stream str) {
		if(!updateRequired) return ;		
		int updateMask = 0;
		if(animUpdateRequired) updateMask |= 0x10; 
		if(hitUpdateRequired2) updateMask |= 8;
		if(mask80update) updateMask |= 0x80;
		if(dirUpdateRequired) updateMask |= 0x20;
		if(forcedChatRequired) updateMask |= 1;
		if(hitUpdateRequired) updateMask |= 0x40;		
		if(FocusPointX != -1) updateMask |= 4;		
			
		str.writeByte(updateMask);
				
		if (animUpdateRequired) appendAnimUpdate(str);
		if (hitUpdateRequired2) appendHitUpdate2(str);
		if (mask80update)       appendMask80Update(str);
		if (dirUpdateRequired)  appendFaceEntity(str);
		if(forcedChatRequired) {
			str.writeString(forcedText);
		}
		if (hitUpdateRequired)  appendHitUpdate(str);
		if(FocusPointX != -1) appendSetFocusDestination(str);
		
	}

	public void clearUpdateFlags() {
		updateRequired = false;
		forcedChatRequired = false;
		hitUpdateRequired = false;
		hitUpdateRequired2 = false;
		animUpdateRequired = false;
		dirUpdateRequired = false;
		mask80update = false;
		forcedText = null;
		moveX = 0;
		moveY = 0;
		direction = -1;
		FocusPointX = -1;
		FocusPointY = -1;
	}

	
	public int getNextWalkingDirection() {
		int dir;
		dir = Misc.direction(absX, absY, (absX + moveX), (absY + moveY));
		if(dir == -1) return -1;
		dir >>= 1;
		absX += moveX;
		absY += moveY;
		return dir;
	}

	public void getNextNPCMovement(int i) {
		direction = -1;
		if(Server.npcHandler.npcs[i].freezeTimer == 0) {
			direction = getNextWalkingDirection();
		}
	}


	public void appendHitUpdate(Stream str) {		
		if (HP <= 0) {
			isDead = true;
		}
		str.writeByteC(hitDiff); 
		if (hitDiff > 0) {
			str.writeByteS(1); 
		} else {
			str.writeByteS(0); 
		}	
		str.writeByteS(HP); 
		str.writeByteC(MaxHP); 	
	}
	
	public int hitDiff2 = 0;
	public boolean hitUpdateRequired2 = false;
	
	public void appendHitUpdate2(Stream str) {		
		if (HP <= 0) {
			isDead = true;
		}
		str.writeByteA(hitDiff2); 
		if (hitDiff2 > 0) {
			str.writeByteC(1); 
		} else {
			str.writeByteC(0); 
		}	
		str.writeByteA(HP); 
		str.writeByte(MaxHP); 	
	}
	
	public int getX() {
		return absX;
	}
	
	public int getY() {
		return absY;
	}
}
