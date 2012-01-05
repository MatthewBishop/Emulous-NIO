package server.model.npcs;
import java.io.*;
import server.model.players.*;
import server.util.*;
import server.*;

public class NPCHandler {
	public static int maxNPCs = 10000;
	public static int maxListedNPCs = 10000;
	public static int maxNPCDrops = 10000;
	public static NPC npcs[] = new NPC[maxNPCs];
	public static NPCList NpcList[] = new NPCList[maxListedNPCs];

	public NPCHandler() {
		for(int i = 0; i < maxNPCs; i++) {
			npcs[i] = null;
		}
		for(int i = 0; i < maxListedNPCs; i++) {
			NpcList[i] = null;
		}
		
		loadNPCList("./data/npc.cfg");
		loadAutoSpawn("./data/spawn-config.cfg");
	}
	
	
	
	/**
	* Summon npc, barrows, etc
	**/
	public void spawnNpc(Client c, int npcType, int x, int y, int heightLevel, int WalkingType, int HP, int maxHit, int attack, int defence, boolean attackPlayer) {
		// first, search for a free slot
		int slot = -1;
		for (int i = 1; i < maxNPCs; i++) {
			if (npcs[i] == null) {
				slot = i;
				break;
			}
		}
		if(slot == -1) return;		// no free slot found
		NPC newNPC = new NPC(slot, npcType);
		newNPC.absX = x;
		newNPC.absY = y;
		newNPC.makeX = x;
		newNPC.makeY = y;
		newNPC.heightLevel = heightLevel;
		newNPC.walkingType = WalkingType;
		newNPC.HP = HP;
		newNPC.MaxHP = HP;
		newNPC.maxHit = maxHit;
		newNPC.attack = attack;
		newNPC.defence = defence;
		newNPC.spawnedBy = c.getId();
		if(attackPlayer) {
			newNPC.underAttack = true;
			if(c!= null) {
				if(server.model.minigames.Barrows.COFFIN_AND_BROTHERS[c.randomCoffin][1] != newNPC.npcType) {
					if(newNPC.npcType == 2025 || newNPC.npcType == 2026 || newNPC.npcType == 2027 || newNPC.npcType == 2028 || newNPC.npcType == 2029 || newNPC.npcType == 2030) {
						newNPC.forceChat("You dare disturb my rest!");
					}
				}
				if(server.model.minigames.Barrows.COFFIN_AND_BROTHERS[c.randomCoffin][1] == newNPC.npcType) {
					newNPC.forceChat("You dare steal from us!");
				}
				
				newNPC.killerId = c.playerId;
			}
		}
		npcs[slot] = newNPC;
	}
	
	
	
	/**
	* Emotes
	**/
	
	public static int getAttackEmote(int i) {
		switch(Server.npcHandler.npcs[i].npcType) {		

			case 2035: //spider
			return 143;	
			
			case 2033: //rat
			return 138;	
			
			case 2031: // bloodveld
			return 2070;
			
			case 101: // goblin
			return 309;	
			
			case 81: // cow
			return 0x03B;
			
			case 21: // hero
			return 451;	
			
			case 41: // chicken
			return 55;	
			
			case 9: // guard
			case 32: // guard
			case 20: // paladin
			return 451;	
			
			case 1338: // dagannoth
			case 1340:
			case 1342:
			return 1341;
		
			case 19: // white knight
			return 406;
			
			case 111: // ice giant
			return 128;
			
			case 125: // ice warrior
			return 451;
			
			case 2028: // karil
			return 2075;
					
			case 2025: // ahrim
			return 729;
			
			case 2026: // dharok
			return 2067;
			
			case 2027: // guthan
			return 2080;
			
			case 2029: // torag
			return 0x814;
			
			case 2030: // verac
			return 2062;
			default:
			return 0x326;		
		}
	}	

	
	public int getDeadEmote(int i) {
		switch(npcs[i].npcType) {						
			case 2035: //spider
			return 146;
			
			case 2033: //rat
			return 141;
			
			case 2031: // bloodvel
			return 2073;
			
			case 101: //goblin
			return 313;
			
			case 81: // cow
			return 0x03E;
			
			case 41: // chicken
			return 57;
			
			case 1338: // dagannoth
			case 1340:
			case 1342:
			return 1342;
			
			case 111: // ice giant
			return 131;
			
			case 125: // ice warrior
			return 843;
			
			default:
			return 2304;
		}
	}
	
	/**
	* Attack delays
	**/
	public int getNpcDelay(int i) {
		switch(npcs[i].npcType) {
			case 2025:
			case 2028:
			return 7;
			
			case 1: // man
			return 6;
			
			default:
			return 6;
		}
	}
	
	/**
	* Hit delays
	**/
	public int getHitDelay(int i) {
		switch(npcs[i].npcType) {
			case 2025:
			return 4;
			case 2028:
			return 3;

			default:
			return 2;
		}
	}
		
	/**
	* Npc respawn time
	**/
	public int getRespawnTime(int i) {
		switch(npcs[i].npcType) {
			
			default:
			return 3;
		}
	}
	
	
	
	
	public void newNPC(int npcType, int x, int y, int heightLevel, int WalkingType, int HP, int maxHit, int attack, int defence) {
		// first, search for a free slot
		int slot = -1;
		for (int i = 1; i < maxNPCs; i++) {
			if (npcs[i] == null) {
				slot = i;
				break;
			}
		}

		if(slot == -1) return;		// no free slot found

		NPC newNPC = new NPC(slot, npcType);
		newNPC.absX = x;
		newNPC.absY = y;
		newNPC.makeX = x;
		newNPC.makeY = y;
		newNPC.heightLevel = heightLevel;
		newNPC.walkingType = WalkingType;
		newNPC.HP = HP;
		newNPC.MaxHP = HP;
		newNPC.maxHit = maxHit;
		newNPC.attack = attack;
		newNPC.defence = defence;
		npcs[slot] = newNPC;
	}

	public void newNPCList(int npcType, String npcName, int combat, int HP) {
		// first, search for a free slot
		int slot = -1;
		for (int i = 0; i < maxListedNPCs; i++) {
			if (NpcList[i] == null) {
				slot = i;
				break;
			}
		}

		if(slot == -1) return;		// no free slot found

		NPCList newNPCList = new NPCList(npcType);
		newNPCList.npcName = npcName;
		newNPCList.npcCombat = combat;
		newNPCList.npcHealth = HP;
		NpcList[slot] = newNPCList;
	}

	

	public void process() {
		for (int i = 0; i < maxNPCs; i++) {
			if (npcs[i] == null) continue;
			npcs[i].clearUpdateFlags();
			
		}
                
		for (int i = 0; i < maxNPCs; i++) {
			if (npcs[i] != null) {
				if (npcs[i].actionTimer > 0) {
					npcs[i].actionTimer--;
				}
				
				if (npcs[i].freezeTimer > 0) {
					npcs[i].freezeTimer--;
				}
				
				if (npcs[i].hitDelayTimer > 0) {
					npcs[i].hitDelayTimer--;
				}
				
				if (npcs[i].hitDelayTimer == 1) {
					npcs[i].hitDelayTimer = 0;
					applyDamage(i);
				}
				
				if(npcs[i].attackTimer > 0) {
					npcs[i].attackTimer--;
				}
					
				if(npcs[i].spawnedBy > 0) { // delete summons npc
					if(Server.playerHandler.players[npcs[i].spawnedBy] == null 
					|| Server.playerHandler.players[npcs[i].spawnedBy].heightLevel != npcs[i].heightLevel	
					|| Server.playerHandler.players[npcs[i].spawnedBy].respawnTimer > 0 
					|| !Server.playerHandler.players[npcs[i].spawnedBy].goodDistance(npcs[i].getX(), npcs[i].getY(), Server.playerHandler.players[npcs[i].spawnedBy].getX(), Server.playerHandler.players[npcs[i].spawnedBy].getY(), 20)) {
						
						if(Server.playerHandler.players[npcs[i].spawnedBy] != null) {
							for(int o = 0; o < Server.playerHandler.players[npcs[i].spawnedBy].barrowsNpcs.length; o++){
								if(npcs[i].npcType == Server.playerHandler.players[npcs[i].spawnedBy].barrowsNpcs[o][0]) {
									Server.playerHandler.players[npcs[i].spawnedBy].barrowsNpcs[o][1] = 0;
										
								}
							}
						}
						npcs[i] = null;
					}
				}
					
				if (npcs[i] == null) continue;
				
				/**
				* Attacking player
				**/
				
				if(npcs[i].underAttack && !npcs[i].walkingHome) {
					if(!npcs[i].isDead) {
						int p = npcs[i].killerId;
						if(Server.playerHandler.players[p] != null) {
							Client c = (Client) Server.playerHandler.players[p];					
							followPlayer(i, c.playerId);
							if (npcs[i] == null) continue;
							if(npcs[i].attackTimer == 0) {
								if(c != null) {
									attackPlayer(c, i);
								} else {
									npcs[i].killerId = 0;
									npcs[i].underAttack = false;
									npcs[i].facePlayer(0);
								}
							}
						} else {
							npcs[i].killerId = 0;
							npcs[i].underAttack = false;
							npcs[i].facePlayer(0);
						}
					}
				}
				
				
		
				/**
				* Random walking and walking home
				**/
				if (npcs[i] == null) continue;
				if((!npcs[i].underAttack || npcs[i].walkingHome) && npcs[i].randomWalk && !npcs[i].isDead) {
					npcs[i].facePlayer(0);
					npcs[i].killerId = 0;	
					if(npcs[i].spawnedBy == 0) {
						if((npcs[i].absX > npcs[i].makeX + Config.NPC_RANDOM_WALK_DISTANCE) || (npcs[i].absX < npcs[i].makeX - Config.NPC_RANDOM_WALK_DISTANCE) || (npcs[i].absY > npcs[i].makeY + Config.NPC_RANDOM_WALK_DISTANCE) || (npcs[i].absY < npcs[i].makeY - Config.NPC_RANDOM_WALK_DISTANCE)) {
							npcs[i].walkingHome = true;
						}
					}

					if (npcs[i].walkingHome && npcs[i].absX == npcs[i].makeX && npcs[i].absY == npcs[i].makeY) {
						npcs[i].walkingHome = false;
					} else if(npcs[i].walkingHome) {
						npcs[i].moveX = GetMove(npcs[i].absX, npcs[i].makeX);
			      		npcs[i].moveY = GetMove(npcs[i].absY, npcs[i].makeY);
						npcs[i].getNextNPCMovement(i); 
						npcs[i].updateRequired = true;
					}
					if(npcs[i].walkingType == 1) {
						if(Misc.random(6)== 1 && !npcs[i].walkingHome) {
							int MoveX = 0;
							int MoveY = 0;			
							int Rnd = Misc.random(9);
							if (Rnd == 1) {
								MoveX = 1;
								MoveY = 1;
							} else if (Rnd == 2) {
								MoveX = -1;
							} else if (Rnd == 3) {
								MoveY = -1;
							} else if (Rnd == 4) {
								MoveX = 1;
							} else if (Rnd == 5) {
								MoveY = 1;
							} else if (Rnd == 6) {
								MoveX = -1;
								MoveY = -1;
							} else if (Rnd == 7) {
								MoveX = -1;
								MoveY = 1;
							} else if (Rnd == 8) {
								MoveX = 1;
								MoveY = -1;
							}
										
							if (MoveX == 1) {
								if (npcs[i].absX + MoveX < npcs[i].makeX + 1) {
									npcs[i].moveX = MoveX;
								} else {
									npcs[i].moveX = 0;
								}
							}
							
							if (MoveX == -1) {
								if (npcs[i].absX - MoveX > npcs[i].makeX - 1)  {
									npcs[i].moveX = MoveX;
								} else {
									npcs[i].moveX = 0;
								}
							}
							
							if(MoveY == 1) {
								if(npcs[i].absY + MoveY < npcs[i].makeY + 1) {
									npcs[i].moveY = MoveY;
								} else {
									npcs[i].moveY = 0;
								}
							}
							
							if(MoveY == -1) {
								if(npcs[i].absY - MoveY > npcs[i].makeY - 1)  {
									npcs[i].moveY = MoveY;
								} else {
									npcs[i].moveY = 0;
								}
							}
										
							npcs[i].getNextNPCMovement(i); 
							npcs[i].updateRequired = true;
						}
					}
				}
		
				
				if (npcs[i].isDead == true) {
					if (npcs[i].actionTimer == 0 && npcs[i].applyDead == false && npcs[i].needRespawn == false) {
						npcs[i].updateRequired = true;
						npcs[i].facePlayer(0);
						npcs[i].killedBy = getNpcKillerId(i);
						npcs[i].animNumber = getDeadEmote(i); // dead emote
						npcs[i].animUpdateRequired = true;
						npcs[i].freezeTimer = 0;
						npcs[i].applyDead = true;
						npcs[i].actionTimer = 4; // delete time
					} else if (npcs[i].actionTimer == 0 && npcs[i].applyDead == true &&  npcs[i].needRespawn == false) {
						
						npcs[i].needRespawn = true;
						npcs[i].actionTimer = getRespawnTime(i); // respawn time
						dropItems(i); // npc drops items!
						npcs[i].absX = npcs[i].makeX;
						npcs[i].absY = npcs[i].makeY;				
						npcs[i].HP = npcs[i].MaxHP;
						npcs[i].animNumber = 0x328;
						npcs[i].updateRequired = true;
						npcs[i].animUpdateRequired = true;
					} else if (npcs[i].actionTimer == 0 && npcs[i].needRespawn == true) {
					
						if(npcs[i].spawnedBy > 0) {
							npcs[i] = null;
						} else {
							int old1 = npcs[i].npcType;
							if (old1 == 1267 ||old1 == 1265) {
								old1 += 1;
							}
							int old2 = npcs[i].makeX;
							int old3 = npcs[i].makeY;
							int old4 = npcs[i].heightLevel;
							int old5 = npcs[i].walkingType;
							int old6 = npcs[i].MaxHP;
							int old7 = npcs[i].maxHit;
							int old8 = npcs[i].attack;	
							int old9 = npcs[i].defence;
							
							npcs[i] = null;
							newNPC(old1, old2, old3, old4, old5, old6, old7, old8, old9);
						}
					}
				}
			}
		}
	}
        
	
	/**
	* Npc killer id?
	**/
	
	public int getNpcKillerId(int npcId) {
		int oldDamage = 0;
		int count = 0;
		int killerId = 0;
		for (int p = 1; p < Config.MAX_PLAYERS; p++)  {	
			if (Server.playerHandler.players[p] != null) {
				if(Server.playerHandler.players[p].killingNpcIndex == npcId) {
					if(Server.playerHandler.players[p].totalDamageDealt > oldDamage) {
						oldDamage = Server.playerHandler.players[p].totalDamageDealt;
						killerId = p;
					}
					Server.playerHandler.players[p].totalDamageDealt = 0;
				}	
			}
		}				
		return killerId;
	}
		
	
	/**
	* Dropping Items!
	**/
	
	public void dropItems(int i) {
		int npc = 0;
		Client c = (Client)Server.playerHandler.players[npcs[i].killedBy];
		if(c != null) {
			for(int o = 0; o < c.barrowsNpcs.length; o++){
				if(npcs[i].npcType == c.barrowsNpcs[o][0]) {
					c.barrowsNpcs[o][1] = 2; // 2 for dead
					c.barrowsKillCount++;
				}
			}
			for(npc = 0; npc < Config.NPC_DROPS.length; npc++){
				if(npcs[i].npcType == Config.NPC_DROPS[npc][0]) {
					if(Misc.random(Config.NPC_DROPS[npc][3]) == 0) {
						Server.itemHandler.createGroundItem(c, Config.NPC_DROPS[npc][1], npcs[i].absX, npcs[i].absY, Config.NPC_DROPS[npc][2], c.playerId);
					}
				}
			}
		}
	}
	
	
	/**
	* Npc Follow Player
	**/
	
	public int GetMove(int Place1,int Place2) { 
		if ((Place1 - Place2) == 0) {
            return 0;
		} else if ((Place1 - Place2) < 0) {
			return 1;
		} else if ((Place1 - Place2) > 0) {
			return -1;
		}
        	return 0;
   	 }
	
	
	public void followPlayer(int i, int playerId) {
		if (Server.playerHandler.players[playerId] == null) {
			return;
		}
		if (Server.playerHandler.players[playerId].respawnTimer > 0) {
			npcs[i].facePlayer(0);
			npcs[i].randomWalk = true; 
	      	npcs[i].underAttack = false;	
			return;
		}
		
		int playerX = Server.playerHandler.players[playerId].absX;
		int playerY = Server.playerHandler.players[playerId].absY;
		npcs[i].randomWalk = false;
		if((npcs[i].spawnedBy > 0) || ((npcs[i].absX < npcs[i].makeX + Config.NPC_FOLLOW_DISTANCE) && (npcs[i].absX > npcs[i].makeX - Config.NPC_FOLLOW_DISTANCE) && (npcs[i].absY < npcs[i].makeY + Config.NPC_FOLLOW_DISTANCE) && (npcs[i].absY > npcs[i].makeY - Config.NPC_FOLLOW_DISTANCE))) {
			if(npcs[i].heightLevel == Server.playerHandler.players[playerId].heightLevel) {
				if(Server.playerHandler.players[playerId] != null && npcs[i] != null) {
					if(playerY < npcs[i].absY) {
						npcs[i].moveX = GetMove(npcs[i].absX, playerX);
						npcs[i].moveY = GetMove(npcs[i].absY, playerY+1);
					} else if(playerY > npcs[i].absY) {
						npcs[i].moveX = GetMove(npcs[i].absX, playerX);
						npcs[i].moveY = GetMove(npcs[i].absY, playerY-1);
					} else if(playerX < npcs[i].absX) {
						npcs[i].moveX = GetMove(npcs[i].absX, playerX+1);
						npcs[i].moveY = GetMove(npcs[i].absY, playerY);
					} else if(playerX > npcs[i].absX)  {
						npcs[i].moveX = GetMove(npcs[i].absX, playerX-1);
						npcs[i].moveY = GetMove(npcs[i].absY, playerY);
					} else if(playerX == npcs[i].absX || playerY == npcs[i].absY) {
						int o = Misc.random(3);
						switch(o) {
							case 0:
							npcs[i].moveX = GetMove(npcs[i].absX, playerX);
							npcs[i].moveY = GetMove(npcs[i].absY, playerY+1);
							break;
							
							case 1:
							npcs[i].moveX = GetMove(npcs[i].absX, playerX);
							npcs[i].moveY = GetMove(npcs[i].absY, playerY-1);
							break;
							
							case 2:
							npcs[i].moveX = GetMove(npcs[i].absX, playerX+1);
							npcs[i].moveY = GetMove(npcs[i].absY, playerY);
							break;
							
							case 3:
							npcs[i].moveX = GetMove(npcs[i].absX, playerX-1);
							npcs[i].moveY = GetMove(npcs[i].absY, playerY);
							break;
						}	
					}
		      		npcs[i].getNextNPCMovement(i);                                              
		      		npcs[i].updateRequired = true;
				}	
			}
		} else {
			npcs[i].facePlayer(0);
			npcs[i].randomWalk = true; 
	      	npcs[i].underAttack = false;	
		}
	}
	
	
	/**
	* load spell
	**/
	public void loadSpell(int i) {
		switch(npcs[i].npcType) {
			case 2025:
			npcs[i].attackType = 2;
			int r = Misc.random(3);
			if(r == 0) {
				npcs[i].gfx100(158);
				npcs[i].projectileId = 159;
				npcs[i].endGfx = 160;
			}
			if(r == 1) {
				npcs[i].gfx100(161);
				npcs[i].projectileId = 162;
				npcs[i].endGfx = 163;
			}
			if(r == 2) {
				npcs[i].gfx100(164);
				npcs[i].projectileId = 165;
				npcs[i].endGfx = 166;
			}
			if(r == 3) {
				npcs[i].gfx100(155);
				npcs[i].projectileId = 156;
				npcs[i].endGfx = 157;
			}
			break;
			
			
			case 2028:
			npcs[i].projectileId = 27;
			break;
		}
	}
		
	/**
	* Distanced required to attack
	**/
	
	public int distanceRequired(int i) {
		switch(npcs[i].npcType) {
			case 2025:
			case 2028:
			return 6;
			
			default:
			return 1;
		}
	}
	
	public int getProjectileSpeed(int i) {
		switch(npcs[i].npcType) {
			case 2025:
			return 85;
			
			case 2028:
			return 80;
			
			default:
			return 70;
		}
	}
	
	/**
	*NPC Attacking Player
	**/
	
	public void attackPlayer(Client c, int i) {
		if(npcs[i] != null) {
			if(goodDistance(npcs[i].getX(), npcs[i].getY(), c.getX(), c.getY(), distanceRequired(i))) {

				if(c.respawnTimer <= 0) {	
					npcs[i].facePlayer(c.playerId);
					npcs[i].attackTimer = getNpcDelay(i);
					npcs[i].hitDelayTimer = getHitDelay(i);
					npcs[i].attackType = 0;
					loadSpell(i);
					if(npcs[i].projectileId > 0) {
						int nX = Server.npcHandler.npcs[i].getX();
						int nY = Server.npcHandler.npcs[i].getY();
						int pX = c.getX();
						int pY = c.getY();
						int offX = (nY - pY)* -1;
						int offY = (nX - pX)* -1;
						c.getPA().createPlayersProjectile(nX, nY, offX, offY, 50, getProjectileSpeed(i), npcs[i].projectileId, 43, 31, -c.getId() - 1, 65);
					}
					startAnimation(getAttackEmote(i), i);
					c.getPA().removeAllWindows();
				} 
			}			
		}
	}
	
	public void applyDamage(int i) {
		if(npcs[i] != null) {
			if(Server.playerHandler.players[npcs[i].killerId] == null) {
				return;
			}
			Client c = (Client) Server.playerHandler.players[npcs[i].killerId];
			
			if(c.attackTimer <= 3 || c.attackTimer == 0 && c.npcIndex == 0 && c.oldNpcIndex == 0) {
				c.startAnimation(c.getCombat().getBlockEmote());
			}
			if(c.respawnTimer <= 0) {	
				int damage = 0;
				if(npcs[i].attackType == 0) {
					damage = Misc.random(npcs[i].maxHit);
					if (10 + Misc.random(c.getCombat().calculateMeleeDefence()) > Misc.random(Server.npcHandler.npcs[i].attack)) {
						damage = 0;
					}				
					if(c.prayerActive[14]) { // protect from melee
						damage = 0;
					}				
					if (c.playerLevel[3] - damage < 0) { 
						damage = c.playerLevel[3];
					}
				}
				if(npcs[i].attackType == 1) { // range
					damage = Misc.random(npcs[i].maxHit);
					if (10 + Misc.random(c.getCombat().calculateRangeDefence()) > Misc.random(Server.npcHandler.npcs[i].attack)) {
						damage = 0;
					}					
					if(c.prayerActive[12]) { // protect from range
						damage = 0;
					}				
					if (c.playerLevel[3] - damage < 0) { 
						damage = c.playerLevel[3];
					}
				}
				
				if(npcs[i].attackType == 2) { // magic
					damage = Misc.random(npcs[i].maxHit);
					boolean magicFailed = false;
					if (10 + Misc.random(c.getCombat().calculateMagicDefence()) > Misc.random(Server.npcHandler.npcs[i].attack)) {
						damage = 0;
						magicFailed = true;
					}				
					if(c.prayerActive[13]) { // protect from magic
						damage = 0;
					}				
					if (c.playerLevel[3] - damage < 0) { 
						damage = c.playerLevel[3];
					}
					if(npcs[i].endGfx > 0 && !magicFailed) {
						c.gfx100(npcs[i].endGfx);
					} else {
						c.gfx100(85);
					}

				}
				
				c.logoutDelay = System.currentTimeMillis(); // logout delay
				c.hitDiff = damage;
				c.playerLevel[3] -= damage;
				c.getPA().refreshSkill(3);
				c.updateRequired = true;
				c.hitUpdateRequired = true;	

			}
		}
	}
		
		

	public void startAnimation(int animId, int i) {
		npcs[i].animNumber = animId;
		npcs[i].animUpdateRequired = true;
		npcs[i].updateRequired = true;
	}
	
	public int getLevelForXP(int exp) {
		int points = 0;
		int output = 0;

		for (int lvl = 1; lvl <= 99; lvl++) {
			points += Math.floor((double)lvl + 300.0 * Math.pow(2.0, (double)lvl / 7.0));
			output = (int)Math.floor(points / 4);
			if (output >= exp)
				return lvl;
		}
		return 0;
	}
	public boolean goodDistance(int objectX, int objectY, int playerX, int playerY, int distance) {
		for (int i = 0; i <= distance; i++) {
		  for (int j = 0; j <= distance; j++) {
			if ((objectX + i) == playerX && ((objectY + j) == playerY || (objectY - j) == playerY || objectY == playerY)) {
				return true;
			} else if ((objectX - i) == playerX && ((objectY + j) == playerY || (objectY - j) == playerY || objectY == playerY)) {
				return true;
			} else if (objectX == playerX && ((objectY + j) == playerY || (objectY - j) == playerY || objectY == playerY)) {
				return true;
			}
		  }
		}
		return false;
	}
	
       
	public boolean loadAutoSpawn(String FileName) {
		String line = "";
		String token = "";
		String token2 = "";
		String token2_2 = "";
		String[] token3 = new String[10];
		boolean EndOfFile = false;
		int ReadMode = 0;
		BufferedReader characterfile = null;
		try {
			characterfile = new BufferedReader(new FileReader("./"+FileName));
		} catch(FileNotFoundException fileex) {
			Misc.println(FileName+": file not found.");
			return false;
		}
		try {
			line = characterfile.readLine();
		} catch(IOException ioexception) {
			Misc.println(FileName+": error loading file.");
			return false;
		}
		while(EndOfFile == false && line != null) {
			line = line.trim();
			int spot = line.indexOf("=");
			if (spot > -1) {
				token = line.substring(0, spot);
				token = token.trim();
				token2 = line.substring(spot + 1);
				token2 = token2.trim();
				token2_2 = token2.replaceAll("\t\t", "\t");
				token2_2 = token2_2.replaceAll("\t\t", "\t");
				token2_2 = token2_2.replaceAll("\t\t", "\t");
				token2_2 = token2_2.replaceAll("\t\t", "\t");
				token2_2 = token2_2.replaceAll("\t\t", "\t");
				token3 = token2_2.split("\t");
				if (token.equals("spawn")) {
					newNPC(Integer.parseInt(token3[0]), Integer.parseInt(token3[1]), Integer.parseInt(token3[2]), Integer.parseInt(token3[3]), Integer.parseInt(token3[4]), getNpcListHP(Integer.parseInt(token3[0])), Integer.parseInt(token3[5]), Integer.parseInt(token3[6]), Integer.parseInt(token3[7]));
				
				}
			} else {
				if (line.equals("[ENDOFSPAWNLIST]")) {
					try { characterfile.close(); } catch(IOException ioexception) { }
					return true;
				}
			}
			try {
				line = characterfile.readLine();
			} catch(IOException ioexception1) { EndOfFile = true; }
		}
		try { characterfile.close(); } catch(IOException ioexception) { }
		return false;
	}

	public int getNpcListHP(int npcId) {
		for (int i = 0; i < maxListedNPCs; i++) {
			if (NpcList[i] != null) {
				if (NpcList[i].npcId == npcId) {
					return NpcList[i].npcHealth;
				}
			}
		}
		return 0;
	}

	public boolean loadNPCList(String FileName) {
		String line = "";
		String token = "";
		String token2 = "";
		String token2_2 = "";
		String[] token3 = new String[10];
		boolean EndOfFile = false;
		int ReadMode = 0;
		BufferedReader characterfile = null;
		try {
			characterfile = new BufferedReader(new FileReader("./"+FileName));
		} catch(FileNotFoundException fileex) {
			Misc.println(FileName+": file not found.");
			return false;
		}
		try {
			line = characterfile.readLine();
		} catch(IOException ioexception) {
			Misc.println(FileName+": error loading file.");
			return false;
		}
		while(EndOfFile == false && line != null) {
			line = line.trim();
			int spot = line.indexOf("=");
			if (spot > -1) {
				token = line.substring(0, spot);
				token = token.trim();
				token2 = line.substring(spot + 1);
				token2 = token2.trim();
				token2_2 = token2.replaceAll("\t\t", "\t");
				token2_2 = token2_2.replaceAll("\t\t", "\t");
				token2_2 = token2_2.replaceAll("\t\t", "\t");
				token2_2 = token2_2.replaceAll("\t\t", "\t");
				token2_2 = token2_2.replaceAll("\t\t", "\t");
				token3 = token2_2.split("\t");
				if (token.equals("npc")) {
					newNPCList(Integer.parseInt(token3[0]), token3[1], Integer.parseInt(token3[2]), Integer.parseInt(token3[3]));
				}
			} else {
				if (line.equals("[ENDOFNPCLIST]")) {
					try { characterfile.close(); } catch(IOException ioexception) { }
					return true;
				}
			}
			try {
				line = characterfile.readLine();
			} catch(IOException ioexception1) { EndOfFile = true; }
		}
		try { characterfile.close(); } catch(IOException ioexception) { }
		return false;
	}

	

	

	
}
