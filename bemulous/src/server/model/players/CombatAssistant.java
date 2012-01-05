package server.model.players;

import java.io.*;
import server.util.*;
import server.model.npcs.*;
import server.*;
import server.model.items.*;


public class CombatAssistant{

	private Client c;
	public CombatAssistant(Client Client) {
		this.c = Client;
	}
	
	/**
	* Attack Npcs
	*/
	
	public void attackNpc(int i) {
		
		if (Server.npcHandler.npcs[i] != null) {
			if (Server.npcHandler.npcs[i].isDead) {
				c.usingMagic = false;
				c.faceUpdate(0);
				c.npcIndex = 0;
				return;
			}
			
			if(c.respawnTimer > 0) {
				c.npcIndex = 0;
				return;
			}
			if(c.attackTimer <= 0) {
				c.attackTimer = getAttackDelay(c.getItems().getItemName(c.playerEquipment[c.playerWeapon]).toLowerCase());
				boolean usingBow = false;
				boolean usingArrows = false;
				boolean usingOtherRangeWeapons = false;
				c.rangeItemUsed = 0;
				c.projectileStage = 0;

				if(!c.usingMagic) {
					for (int bowId : c.BOWS) {
						if(c.playerEquipment[c.playerWeapon] == bowId) {
							usingBow = true;
							for (int arrowId : c.ARROWS) {
								if(c.playerEquipment[c.playerArrows] == arrowId) {
									usingArrows = true;
								}
							}
						}
					}
					
					for (int otherRangeId : c.OTHER_RANGE_WEAPONS) {
						if(c.playerEquipment[c.playerWeapon] == otherRangeId) {
							usingOtherRangeWeapons = true;
						}
					}
				}
				
				if((!c.goodDistance(c.getX(), c.getY(), Server.npcHandler.npcs[i].getX(), Server.npcHandler.npcs[i].getY(), 2) && (usingHally() && !usingOtherRangeWeapons && !usingBow && !c.usingMagic)) ||(!c.goodDistance(c.getX(), c.getY(), Server.npcHandler.npcs[i].getX(), Server.npcHandler.npcs[i].getY(), 4) && (usingOtherRangeWeapons && !usingBow && !c.usingMagic)) || (!c.goodDistance(c.getX(), c.getY(), Server.npcHandler.npcs[i].getX(), Server.npcHandler.npcs[i].getY(), 1) && (!usingOtherRangeWeapons && !usingHally() && !usingBow && !c.usingMagic)) || ((!c.goodDistance(c.getX(), c.getY(), Server.npcHandler.npcs[i].getX(), Server.npcHandler.npcs[i].getY(), 6) && (usingBow || c.usingMagic)))) {
					c.attackTimer = 2;
					return;
				}
				
				if(!usingArrows && usingBow && (c.playerEquipment[c.playerWeapon] < 4212 || c.playerEquipment[c.playerWeapon] > 4223)) {
					c.sendMessage("You have run out of arrows!");
					c.stopMovement();
					c.npcIndex = 0;
					return;
				} 
				if(correctBowAndArrows() < c.playerEquipment[c.playerArrows] && Config.CORRECT_ARROWS && usingBow) {
					c.sendMessage("You can't use "+c.getItems().getItemName(c.playerEquipment[c.playerArrows]).toLowerCase()+"s with a "+c.getItems().getItemName(c.playerEquipment[c.playerWeapon]).toLowerCase()+".");
					c.stopMovement();
					c.npcIndex = 0;
					return;
				}
				
				if(usingBow || c.usingMagic || usingOtherRangeWeapons || (c.goodDistance(c.getX(), c.getY(), Server.npcHandler.npcs[i].getX(), Server.npcHandler.npcs[i].getY(), 2) && usingHally())) {
					c.stopMovement();
				}

				if(!checkMagicReqs(c.spellId)) {
					c.stopMovement();
					c.npcIndex = 0;
					return;
				}
				
				c.faceUpdate(i);
				if(c.usingSpecial) {
					if(checkSpecAmount(c.playerEquipment[c.playerWeapon])){
						activateSpecial(c.playerEquipment[c.playerWeapon], i);
						return;
					} else {
						c.sendMessage("You don't have the required special energy to use this attack.");
						c.usingSpecial = false;
						c.getItems().updateSpecialBar();
						c.npcIndex = 0;
						return;
					}
				}
				c.specMaxHitIncrease = 0;
				
				if(!c.usingMagic) {
					c.startAnimation(getWepAnim(c.getItems().getItemName(c.playerEquipment[c.playerWeapon]).toLowerCase()));
				} else {
					c.startAnimation(c.MAGIC_SPELLS[c.spellId][2]);
				}
				if(!usingBow && !c.usingMagic && !usingOtherRangeWeapons) { // melee hit delay
					c.hitDelay = getHitDelay(c.getItems().getItemName(c.playerEquipment[c.playerWeapon]).toLowerCase());
					c.projectileStage = 0;
					c.oldNpcIndex = i;
				}
				
				if(usingBow && !usingOtherRangeWeapons) { // range hit delay
					if(c.playerEquipment[c.playerWeapon] >= 4212 && c.playerEquipment[c.playerWeapon] <= 4223) {
						c.rangeItemUsed = c.playerEquipment[c.playerWeapon];
						c.crystalBowArrowCount++;
					} else {
						c.rangeItemUsed = c.playerEquipment[c.playerArrows];
						c.getItems().deleteArrow();	
					}	
					c.lastWeaponUsed = c.playerEquipment[c.playerWeapon];
					c.gfx100(getRangeStartGFX());	
					c.hitDelay = getHitDelay(c.getItems().getItemName(c.playerEquipment[c.playerWeapon]).toLowerCase());
					c.projectileStage = 1;
					c.oldNpcIndex = i;
				}
							
				
				if(usingOtherRangeWeapons) {	// knives, darts, etc hit delay		
					c.rangeItemUsed = c.playerEquipment[c.playerWeapon];
					c.getItems().deleteEquipment();
					c.gfx100(getRangeStartGFX());
					c.hitDelay = getHitDelay(c.getItems().getItemName(c.playerEquipment[c.playerWeapon]).toLowerCase());
					c.projectileStage = 1;
					c.oldNpcIndex = i;
				}

				if(c.usingMagic) {	// magic hit delay
					int pX = c.getX();
					int pY = c.getY();
					int nX = Server.npcHandler.npcs[i].getX();
					int nY = Server.npcHandler.npcs[i].getY();
					int offX = (pY - nY)* -1;
					int offY = (pX - nX)* -1;
					c.castingMagic = true;
					c.projectileStage = 2;
					if(c.MAGIC_SPELLS[c.spellId][3] > 0) {
						if(getStartGfxHeight() == 100) {
							c.gfx100(c.MAGIC_SPELLS[c.spellId][3]);
						} else {
							c.gfx0(c.MAGIC_SPELLS[c.spellId][3]);
						}
					}
					if(c.MAGIC_SPELLS[c.spellId][4] > 0) {
						c.getPA().createPlayersProjectile(pX, pY, offX, offY, 50, 78, c.MAGIC_SPELLS[c.spellId][4], getStartHeight(), getEndHeight(), i + 1, getStartDelay());
					}
					c.hitDelay = getHitDelay(c.getItems().getItemName(c.playerEquipment[c.playerWeapon]).toLowerCase());
					c.oldNpcIndex = i;
					c.oldSpellId = c.spellId;
					c.npcIndex = 0;
				}

				if(usingBow && Config.CRYSTAL_BOW_DEGRADES) { // crystal bow degrading
					if(c.playerEquipment[c.playerWeapon] == 4212) { // new crystal bow becomes full bow on the first shot
						c.getItems().wearItem(4214, 1, 3);
					}
					
					if(c.crystalBowArrowCount >= 250){
						switch(c.playerEquipment[c.playerWeapon]) {
							
							case 4223: // 1/10 bow
							c.getItems().wearItem(-1, 1, 3);
							c.sendMessage("Your crystal bow has fully degraded.");
							if(!c.getItems().addItem(4207, 1)) {
								Server.itemHandler.createGroundItem(c, 4207, c.getX(), c.getY(), 1, c.getId());
							}
							c.crystalBowArrowCount = 0;
							break;
							
							default:
							c.getItems().wearItem(++c.playerEquipment[c.playerWeapon], 1, 3);
							c.sendMessage("Your crystal bow degrades.");
							c.crystalBowArrowCount = 0;
							break;
							
						
						}
					}	
				}
			}
		}
	}
	

	public void delayedHit(int i) { // npc hit delay
		if (Server.npcHandler.npcs[i] != null) {
			if (Server.npcHandler.npcs[i].isDead) {
				c.npcIndex = 0;
				return;
			}
			Server.npcHandler.npcs[i].facePlayer(c.playerId);
			
			if(c.projectileStage == 0) { // melee hit damage
				applyNpcMeleeDamage(i, 1);
				if(c.doubleHit) {
					applyNpcMeleeDamage(i, 2);
				}				
			}

			if(!c.castingMagic && c.projectileStage > 0) { // range hit damage
				int damage = Misc.random(calculateRangeMaxHit());
				
				if (Misc.random(Server.npcHandler.npcs[i].defence) > 30 + Misc.random(calculateRangeAttack())) {
					damage = 0;
				}
				
				if (Server.npcHandler.npcs[i].HP - damage < 0) { 
					damage = Server.npcHandler.npcs[i].HP;
				}
				if(c.fightMode == 3) {
					c.getPA().addSkillXP((damage*Config.RANGE_EXP_RATE/3), 4); 
					c.getPA().addSkillXP((damage*Config.RANGE_EXP_RATE/3), 1);				
					c.getPA().addSkillXP((damage*Config.RANGE_EXP_RATE/3), 3);
					c.getPA().refreshSkill(1);
					c.getPA().refreshSkill(3);
					c.getPA().refreshSkill(4);
				} else {
					c.getPA().addSkillXP((damage*Config.RANGE_EXP_RATE), 4); 
					c.getPA().addSkillXP((damage*Config.RANGE_EXP_RATE/4), 3);
					c.getPA().refreshSkill(3);
					c.getPA().refreshSkill(4);
				}
				boolean dropArrows = true;
						
				for(int noArrowId : c.NO_ARROW_DROP) {
					if(c.lastWeaponUsed == noArrowId) {
						dropArrows = false;
						break;
					}
				}
				if(dropArrows) {
					c.getItems().dropArrowNpc();	
				}
				Server.npcHandler.npcs[i].underAttack = true;
				Server.npcHandler.npcs[i].killerId = c.playerId;
				Server.npcHandler.npcs[i].hitDiff = damage;
				Server.npcHandler.npcs[i].HP -= damage;
				c.killingNpcIndex = c.oldNpcIndex;
				c.totalDamageDealt += damage;
				Server.npcHandler.npcs[i].hitUpdateRequired = true;	
				Server.npcHandler.npcs[i].updateRequired = true;

			} else if (c.projectileStage > 0) { // magic hit damage
				int damage = Misc.random(c.MAGIC_SPELLS[c.oldSpellId][6]);
				if(godSpells()) {
					if(System.currentTimeMillis() - c.godSpellDelay < Config.GOD_SPELL_CHARGE) {
						damage += Misc.random(10);
					}
				}
				boolean magicFailed = false;
				c.npcIndex = 0;
				if (Misc.random(Server.npcHandler.npcs[i].defence) > 10+ Misc.random(calculateMagicAttack())) {
					damage = 0;
					magicFailed = true;
				}
				
				if (Server.npcHandler.npcs[i].HP - damage < 0) { 
					damage = Server.npcHandler.npcs[i].HP;
				}
				
				c.getPA().addSkillXP((c.MAGIC_SPELLS[c.oldSpellId][7] + damage*Config.MAGIC_EXP_RATE), 6); 
				c.getPA().addSkillXP((c.MAGIC_SPELLS[c.oldSpellId][7] + damage*Config.MAGIC_EXP_RATE/4), 3);
				c.getPA().refreshSkill(3);
				c.getPA().refreshSkill(6);
				
				if(getEndGfxHeight() == 100 && !magicFailed){ // end GFX
					Server.npcHandler.npcs[i].gfx100(c.MAGIC_SPELLS[c.oldSpellId][5]);
				} else if (!magicFailed){
					Server.npcHandler.npcs[i].gfx0(c.MAGIC_SPELLS[c.oldSpellId][5]);
				}
				
				if(magicFailed) {	
					Server.npcHandler.npcs[i].gfx100(85);
				}			
				if(!magicFailed) {
					int freezeDelay = getFreezeTime();//freeze 
					if(freezeDelay > 0 && Server.npcHandler.npcs[i].freezeTimer == 0) {
						Server.npcHandler.npcs[i].freezeTimer = freezeDelay;
					}
					switch(c.MAGIC_SPELLS[c.oldSpellId][0]) { 
						case 12901:
						case 12919: // blood spells
						case 12911:
						case 12929:
						int heal = Misc.random(damage / 2);
						if(c.playerLevel[3] + heal >= c.getPA().getLevelForXP(c.playerXP[3])) {
							c.playerLevel[3] = c.getPA().getLevelForXP(c.playerXP[3]);
						} else {
							c.playerLevel[3] += heal;
						}
						c.getPA().refreshSkill(3);
						break;
					}

				}
				Server.npcHandler.npcs[i].underAttack = true;
				Server.npcHandler.npcs[i].killerId = c.playerId;
				if(c.MAGIC_SPELLS[c.oldSpellId][6] != 0) {
					Server.npcHandler.npcs[i].hitDiff = damage;
					Server.npcHandler.npcs[i].HP -= damage;
					Server.npcHandler.npcs[i].hitUpdateRequired = true;
					c.totalDamageDealt += damage;
				}
				c.killingNpcIndex = c.oldNpcIndex;			
				Server.npcHandler.npcs[i].updateRequired = true;
				c.usingMagic = false;
				c.castingMagic = false;
				c.oldSpellId = 0;
			}
		}
	
		if(c.bowSpecShot <= 0) {
			c.oldNpcIndex = 0;
			c.projectileStage = 0;
			c.doubleHit = false;
			c.lastWeaponUsed = 0;
			c.bowSpecShot = 0;
		}
		if(c.bowSpecShot >= 2) {
			c.bowSpecShot = 0;
			c.attackTimer = getAttackDelay(c.getItems().getItemName(c.playerEquipment[c.playerWeapon]).toLowerCase());
		}
		if(c.bowSpecShot == 1) {
			fireProjectileNpc();
			c.hitDelay = 2;
			c.bowSpecShot = 0;
		}
	}
	
	
	public void applyNpcMeleeDamage(int i, int damageMask) {
		int damage = Misc.random(calculateMeleeMaxHit());
		if (Server.npcHandler.npcs[i].HP - damage < 0) { 
			damage = Server.npcHandler.npcs[i].HP;
		}
		if (Misc.random(Server.npcHandler.npcs[i].defence) > 10+ Misc.random(calculateMeleeAttack())) {
			damage = 0;
		}
		if(c.fightMode == 3) {
			c.getPA().addSkillXP((damage*Config.MELEE_EXP_RATE/3), 0); 
			c.getPA().addSkillXP((damage*Config.MELEE_EXP_RATE/3), 1);
			c.getPA().addSkillXP((damage*Config.MELEE_EXP_RATE/3), 2); 				
			c.getPA().addSkillXP((damage*Config.MELEE_EXP_RATE/3), 3);
			c.getPA().refreshSkill(0);
			c.getPA().refreshSkill(1);
			c.getPA().refreshSkill(2);
			c.getPA().refreshSkill(3);
		} else {
			c.getPA().addSkillXP((damage*Config.MELEE_EXP_RATE), c.fightMode); 
			c.getPA().addSkillXP((damage*Config.MELEE_EXP_RATE/4), 3);
			c.getPA().refreshSkill(c.fightMode);
			c.getPA().refreshSkill(3);
		}

		Server.npcHandler.npcs[i].underAttack = true;
		Server.npcHandler.npcs[i].killerId = c.playerId;
		c.killingNpcIndex = c.npcIndex;
		switch(damageMask) {
			case 1:
			Server.npcHandler.npcs[i].hitDiff = damage;
			Server.npcHandler.npcs[i].HP -= damage;
			c.totalDamageDealt += damage;
			Server.npcHandler.npcs[i].hitUpdateRequired = true;	
			Server.npcHandler.npcs[i].updateRequired = true;
			break;
		
			case 2:
			Server.npcHandler.npcs[i].hitDiff2 = damage;
			Server.npcHandler.npcs[i].HP -= damage;
			c.totalDamageDealt += damage;
			Server.npcHandler.npcs[i].hitUpdateRequired2 = true;	
			Server.npcHandler.npcs[i].updateRequired = true;
			c.doubleHit = false;
			break;
			
		}
	}
	
	public void fireProjectileNpc() {
		if(c.oldNpcIndex > 0) {
			if(Server.npcHandler.npcs[c.oldNpcIndex] != null) {
				c.projectileStage = 2;
				int pX = c.getX();
				int pY = c.getY();
				int nX = Server.npcHandler.npcs[c.oldNpcIndex].getX();
				int nY = Server.npcHandler.npcs[c.oldNpcIndex].getY();
				int offX = (pY - nY)* -1;
				int offY = (pX - nX)* -1;
				c.getPA().createPlayersProjectile(pX, pY, offX, offY, 50, getProjectileSpeed(), getRangeProjectileGFX(), 43, 31, c.oldNpcIndex + 1, getProjectileShowDelay());
			}
		}
	}

	
	/**
	* Attack Players, same as npc tbh xD
	**/
	
	public void attackPlayer(int i) {
		
		if (Server.playerHandler.players[i] != null) {
			
			if (Server.playerHandler.players[i].isDead) {
				resetPlayerAttack();
				return;
			}
			
			if(c.respawnTimer > 0 || Server.playerHandler.players[i].respawnTimer > 0) {
				resetPlayerAttack();
				return;
			}
			
			if(!c.getCombat().checkReqs()) {
				return;
			}
			
			if(!c.goodDistance(Server.playerHandler.players[i].getX(), Server.playerHandler.players[i].getY(), c.getX(), c.getY(), 25)) {
				resetPlayerAttack();
				return;
			}

			if(Server.playerHandler.players[i].respawnTimer > 0) {
				Server.playerHandler.players[i].playerIndex = 0;
				resetPlayerAttack();
				return;
			}
				
			if(c.attackTimer <= 0) {
				c.usingBow = false;
				c.specEffect = 0;
				c.usingRangeWeapon = false;
				c.rangeItemUsed = 0;
				c.attackTimer = getAttackDelay(c.getItems().getItemName(c.playerEquipment[c.playerWeapon]).toLowerCase());
				boolean usingBow = false;
				boolean usingArrows = false;
				boolean usingOtherRangeWeapons = false;
				c.projectileStage = 0;

				if(!c.usingMagic) {
					for (int bowId : c.BOWS) {
						if(c.playerEquipment[c.playerWeapon] == bowId) {
							usingBow = true;
							for (int arrowId : c.ARROWS) {
								if(c.playerEquipment[c.playerArrows] == arrowId) {
									usingArrows = true;
								}
							}
						}
					}				
				
					for (int otherRangeId : c.OTHER_RANGE_WEAPONS) {
						if(c.playerEquipment[c.playerWeapon] == otherRangeId) {
							usingOtherRangeWeapons = true;
						}
					}
				}

				if(c.duelRule[9]){
				boolean canUseWeapon = false;
					for(int funWeapon: Config.FUN_WEAPONS) {
						if(c.playerEquipment[c.playerWeapon] == funWeapon) {
							canUseWeapon = true;
						}
					}
					if(!canUseWeapon) {
						c.sendMessage("You can only use fun weapons in this duel!");
						resetPlayerAttack();
						return;
					}
				}

				if(c.duelRule[2] && (usingBow || usingOtherRangeWeapons)) {
					c.sendMessage("Range has been disabled in this duel!");
					return;
				}
				if(c.duelRule[3] && (!usingBow && !usingOtherRangeWeapons)) {
					c.sendMessage("Melee has been disabled in this duel!");
					return;
				}
				
				if((!c.goodDistance(c.getX(), c.getY(), Server.playerHandler.players[i].getX(), Server.playerHandler.players[i].getY(), 4) && (usingOtherRangeWeapons && !usingBow && !c.usingMagic)) 
				|| (!c.goodDistance(c.getX(), c.getY(), Server.playerHandler.players[i].getX(), Server.playerHandler.players[i].getY(), 2) && (!usingOtherRangeWeapons && usingHally() && !usingBow && !c.usingMagic))
				|| (!c.goodDistance(c.getX(), c.getY(), Server.playerHandler.players[i].getX(), Server.playerHandler.players[i].getY(), getRequiredDistance()) && (!usingOtherRangeWeapons && !usingHally() && !usingBow && !c.usingMagic)) 
				|| (!c.goodDistance(c.getX(), c.getY(), Server.playerHandler.players[i].getX(), Server.playerHandler.players[i].getY(), 6) && (usingBow || c.usingMagic))) {
					c.attackTimer = 2;
					return;
				}			
				if(!usingArrows && usingBow && (c.playerEquipment[c.playerWeapon] < 4212 || c.playerEquipment[c.playerWeapon] > 4223)) {
					c.sendMessage("You have run out of arrows!");
					c.stopMovement();
					resetPlayerAttack();
					return;
				} 
				if(correctBowAndArrows() < c.playerEquipment[c.playerArrows] && Config.CORRECT_ARROWS && usingBow) {
					c.sendMessage("You can't use "+c.getItems().getItemName(c.playerEquipment[c.playerArrows]).toLowerCase()+"s with a "+c.getItems().getItemName(c.playerEquipment[c.playerWeapon]).toLowerCase()+".");
					c.stopMovement();
					resetPlayerAttack();
					return;
				}
				
				if(usingBow || c.usingMagic || usingOtherRangeWeapons || usingHally()) {
					c.stopMovement();
				}
				
				if(!checkMagicReqs(c.spellId)) {
					c.stopMovement();
					resetPlayerAttack();
					return;
				}
				
				c.faceUpdate(i+32768);
				
				if(c.duelStatus != 5) {
					if(!c.attackedPlayers.contains(c.playerIndex) && !Server.playerHandler.players[c.playerIndex].attackedPlayers.contains(c.playerId)) {
						c.attackedPlayers.add(c.playerIndex);
						c.isSkulled = true;
						c.skullTimer = Config.SKULL_TIMER;
						c.headIconPk = 1;
						c.getPA().requestUpdates();
					} 
				}
				
				if(c.usingSpecial) {
					if(c.duelRule[10] && c.duelStatus == 5) {
						c.sendMessage("Special attacks have been disabled during this duel!");
						c.usingSpecial = false;
						c.getItems().updateSpecialBar();
						resetPlayerAttack();
						return;
					}
					if(checkSpecAmount(c.playerEquipment[c.playerWeapon])){
						activateSpecial(c.playerEquipment[c.playerWeapon], i);
						c.followId = Server.playerHandler.players[c.playerIndex].playerId;
						return;
					} else {
						c.sendMessage("You don't have the required special energy to use this attack.");
						c.usingSpecial = false;
						c.getItems().updateSpecialBar();
						c.playerIndex = 0;
						return;
					}	
				}
				
				if(!c.usingMagic) {
					c.startAnimation(getWepAnim(c.getItems().getItemName(c.playerEquipment[c.playerWeapon]).toLowerCase()));
				} else {
					c.startAnimation(c.MAGIC_SPELLS[c.spellId][2]);
				}
				
				if(!usingBow && !c.usingMagic && !usingOtherRangeWeapons) { // melee hit delay
					c.followId = Server.playerHandler.players[c.playerIndex].playerId;
					c.getPA().followPlayer();
					c.hitDelay = getHitDelay(c.getItems().getItemName(c.playerEquipment[c.playerWeapon]).toLowerCase());
					c.projectileStage = 0;
					c.oldPlayerIndex = i;
				}
								
				if(usingBow && !usingOtherRangeWeapons) { // range hit delay
					if(c.playerEquipment[c.playerWeapon] >= 4212 && c.playerEquipment[c.playerWeapon] <= 4223) {
						c.rangeItemUsed = c.playerEquipment[c.playerWeapon];
						c.crystalBowArrowCount++;
					} else {
						c.rangeItemUsed = c.playerEquipment[c.playerArrows];
						c.getItems().deleteArrow();	
					}
					c.usingBow = true;
					c.followId = Server.playerHandler.players[c.playerIndex].playerId;
					c.getPA().followPlayer();
					c.lastWeaponUsed = c.playerEquipment[c.playerWeapon];
					c.gfx100(getRangeStartGFX());	
					c.hitDelay = getHitDelay(c.getItems().getItemName(c.playerEquipment[c.playerWeapon]).toLowerCase());
					c.projectileStage = 1;
					c.oldPlayerIndex = i;
				}
											
				if(usingOtherRangeWeapons) {	// knives, darts, etc hit delay
					c.rangeItemUsed = c.playerEquipment[c.playerWeapon];
					c.getItems().deleteEquipment();
					c.usingRangeWeapon = true;
					c.followId = Server.playerHandler.players[c.playerIndex].playerId;
					c.getPA().followPlayer();
					c.gfx100(getRangeStartGFX());
					c.hitDelay = getHitDelay(c.getItems().getItemName(c.playerEquipment[c.playerWeapon]).toLowerCase());
					c.projectileStage = 1;
					c.oldPlayerIndex = i;
				}

				if(c.usingMagic) {	// magic hit delay
					int pX = c.getX();
					int pY = c.getY();
					int nX = Server.playerHandler.players[i].getX();
					int nY = Server.playerHandler.players[i].getY();
					int offX = (pY - nY)* -1;
					int offY = (pX - nX)* -1;
					c.castingMagic = true;
					c.projectileStage = 2;
					if(c.MAGIC_SPELLS[c.spellId][3] > 0) {
						if(getStartGfxHeight() == 100) {
							c.gfx100(c.MAGIC_SPELLS[c.spellId][3]);
						} else {
							c.gfx0(c.MAGIC_SPELLS[c.spellId][3]);
						}
					}
					if(c.MAGIC_SPELLS[c.spellId][4] > 0) {
						c.getPA().createPlayersProjectile(pX, pY, offX, offY, 50, 78, c.MAGIC_SPELLS[c.spellId][4], getStartHeight(), getEndHeight(), -i - 1, getStartDelay());
					}
					c.hitDelay = getHitDelay(c.getItems().getItemName(c.playerEquipment[c.playerWeapon]).toLowerCase());
					c.oldPlayerIndex = i;
					c.oldSpellId = c.spellId;
					c.playerIndex = 0;
				}

				if(usingBow && Config.CRYSTAL_BOW_DEGRADES) { // crystal bow degrading
					if(c.playerEquipment[c.playerWeapon] == 4212) { // new crystal bow becomes full bow on the first shot
						c.getItems().wearItem(4214, 1, 3);
					}
					
					if(c.crystalBowArrowCount >= 250){
						switch(c.playerEquipment[c.playerWeapon]) {
							
							case 4223: // 1/10 bow
							c.getItems().wearItem(-1, 1, 3);
							c.sendMessage("Your crystal bow has fully degraded.");
							if(!c.getItems().addItem(4207, 1)) {
								Server.itemHandler.createGroundItem(c, 4207, c.getX(), c.getY(), 1, c.getId());
							}
							c.crystalBowArrowCount = 0;
							break;
							
							default:
							c.getItems().wearItem(++c.playerEquipment[c.playerWeapon], 1, 3);
							c.sendMessage("Your crystal bow degrades.");
							c.crystalBowArrowCount = 0;
							break;			
						}
					}	
				}
			}
		}
	}
	
	
	public void playerDelayedHit(int i) {
		if (Server.playerHandler.players[i] != null) {
			if (Server.playerHandler.players[i].isDead) {
				c.playerIndex = 0;
				return;
			}
			
			if (Server.playerHandler.players[i].respawnTimer > 0) {
				c.faceUpdate(0);
				c.playerIndex = 0;
				return;
			}
			Client o = (Client) Server.playerHandler.players[i];
			o.getPA().removeAllWindows();
			if(o.attackTimer <= 3 || o.attackTimer == 0 && o.playerIndex == 0) { // block animation
				o.startAnimation(o.getCombat().getBlockEmote());
			}
			if(o.inTrade) {
				o.getTradeAndDuel().declineTrade();
			}
			if(c.projectileStage == 0) { // melee hit damage								
				applyPlayerMeleeDamage(i, 1);
				if(c.doubleHit) {
					applyPlayerMeleeDamage(i, 2);
				}	
			}
			
			if(!c.castingMagic && c.projectileStage > 0) { // range hit damage
				int damage = Misc.random(calculateRangeMaxHit());
				if(Misc.random(10+o.getCombat().calculateRangeDefence()) > Misc.random(10+calculateRangeAttack())) {
					damage = 0;
				}
				if(o.prayerActive[13]) { // if prayer active reduce damage by half 
					damage = damage / 2;
				}
				if (Server.playerHandler.players[i].playerLevel[3] - damage < 0) { 
					damage = Server.playerHandler.players[i].playerLevel[3];
				}
				if(c.fightMode == 3) {
					c.getPA().addSkillXP((damage*Config.RANGE_EXP_RATE/3), 4); 
					c.getPA().addSkillXP((damage*Config.RANGE_EXP_RATE/3), 1);				
					c.getPA().addSkillXP((damage*Config.RANGE_EXP_RATE/3), 3);
					c.getPA().refreshSkill(1);
					c.getPA().refreshSkill(3);
					c.getPA().refreshSkill(4);
				} else {
					c.getPA().addSkillXP((damage*Config.RANGE_EXP_RATE), 4); 
					c.getPA().addSkillXP((damage*Config.RANGE_EXP_RATE/4), 3);
					c.getPA().refreshSkill(3);
					c.getPA().refreshSkill(4);
				}
				boolean dropArrows = true;
						
				for(int noArrowId : c.NO_ARROW_DROP) {
					if(c.lastWeaponUsed == noArrowId) {
						dropArrows = false;
						break;
					}
				}
				if(dropArrows) {
					c.getItems().dropArrowPlayer();	
				}
				Server.playerHandler.players[i].logoutDelay = System.currentTimeMillis();
				Server.playerHandler.players[i].underAttackBy = c.playerId;
				Server.playerHandler.players[i].killerId = c.playerId;
				Server.playerHandler.players[i].hitDiff = damage;
				Server.playerHandler.players[i].playerLevel[3] -= damage;
				Server.playerHandler.players[i].singleCombatDelay = System.currentTimeMillis();
				c.totalPlayerDamageDealt += damage;
				c.killedBy = Server.playerHandler.players[i].playerId;
				o.getPA().refreshSkill(3);
				Server.playerHandler.players[i].hitUpdateRequired = true;	
				Server.playerHandler.players[i].updateRequired = true;			
			
			} else if (c.projectileStage > 0) { // magic hit damage
				int damage = Misc.random(c.MAGIC_SPELLS[c.spellId][6]);
				if(godSpells()) {
					if(System.currentTimeMillis() - c.godSpellDelay < Config.GOD_SPELL_CHARGE) {
						damage += Misc.random(10);
					}
				}
				boolean magicFailed = false;
				c.playerIndex = 0;
				if(Misc.random(10+o.getCombat().calculateMagicDefence()) > Misc.random(10+calculateMagicAttack())) {
					damage = 0;
					magicFailed = true;
				}
				if(o.prayerActive[12]) { // if prayer active reduce damage by half 
					damage = damage / 2;
				}
				if (Server.playerHandler.players[i].playerLevel[3] - damage < 0) { 
					damage = Server.playerHandler.players[i].playerLevel[3];
				}				
				c.getPA().addSkillXP((c.MAGIC_SPELLS[c.oldSpellId][7] + damage*Config.MAGIC_EXP_RATE), 6); 
				c.getPA().addSkillXP((c.MAGIC_SPELLS[c.oldSpellId][7] + damage*Config.MAGIC_EXP_RATE/4), 3);
				c.getPA().refreshSkill(3);
				c.getPA().refreshSkill(6);
				
				if(getEndGfxHeight() == 100 && !magicFailed){ // end GFX
					Server.playerHandler.players[i].gfx100(c.MAGIC_SPELLS[c.oldSpellId][5]);
				} else if (!magicFailed){
					Server.playerHandler.players[i].gfx0(c.MAGIC_SPELLS[c.oldSpellId][5]);
				}
				
				if(magicFailed) {	
					Server.playerHandler.players[i].gfx100(85);
				}
				
				if(!magicFailed) {	
					int freezeDelay = getFreezeTime();//freeze time
					if(freezeDelay > 0 && Server.playerHandler.players[i].freezeTimer == -6) { 
						Server.playerHandler.players[i].freezeTimer = freezeDelay;
					}
					
					if(System.currentTimeMillis() - Server.playerHandler.players[i].reduceStat > 35000) {
						Server.playerHandler.players[i].reduceStat = System.currentTimeMillis();
						switch(c.MAGIC_SPELLS[c.oldSpellId][0]) { 
							case 12987:
							Server.playerHandler.players[i].playerLevel[0] -= ((o.getPA().getLevelForXP(Server.playerHandler.players[i].playerXP[0]) * 10) / 100);
							break;
							
							case 13011:
							Server.playerHandler.players[i].playerLevel[0] -= ((o.getPA().getLevelForXP(Server.playerHandler.players[i].playerXP[0]) * 10) / 100);
							break;
							
							case 12999:
							Server.playerHandler.players[i].playerLevel[0] -= ((o.getPA().getLevelForXP(Server.playerHandler.players[i].playerXP[0]) * 15) / 100);
							break;
							
							case 13023:
							Server.playerHandler.players[i].playerLevel[0] -= ((o.getPA().getLevelForXP(Server.playerHandler.players[i].playerXP[0]) * 15) / 100);
							break;
						}
					}
					
					switch(c.MAGIC_SPELLS[c.oldSpellId][0]) { 	
						case 12445: //teleblock
						o.teleBlockDelay = System.currentTimeMillis();
						break;
						
						case 12901:
						case 12919: // blood spells
						case 12911:
						case 12929:
						int heal = Misc.random(damage / 2);
						if(c.playerLevel[3] + heal >= c.getPA().getLevelForXP(c.playerXP[3])) {
							c.playerLevel[3] = c.getPA().getLevelForXP(c.playerXP[3]);
						} else {
							c.playerLevel[3] += heal;
						}
						c.getPA().refreshSkill(3);
						break;
						
						case 1153:						
						Server.playerHandler.players[i].playerLevel[0] -= ((o.getPA().getLevelForXP(Server.playerHandler.players[i].playerXP[0]) * 5) / 100);
						o.sendMessage("Your attack level has been reduced!");
						Server.playerHandler.players[i].reduceSpellDelay[c.reduceSpellId] = System.currentTimeMillis();
						o.getPA().refreshSkill(0);
						break;
						
						case 1157:
						Server.playerHandler.players[i].playerLevel[2] -= ((o.getPA().getLevelForXP(Server.playerHandler.players[i].playerXP[2]) * 5) / 100);
						o.sendMessage("Your strength level has been reduced!");
						Server.playerHandler.players[i].reduceSpellDelay[c.reduceSpellId] = System.currentTimeMillis();						
						o.getPA().refreshSkill(2);
						break;
						
						case 1161:
						Server.playerHandler.players[i].playerLevel[1] -= ((o.getPA().getLevelForXP(Server.playerHandler.players[i].playerXP[1]) * 5) / 100);
						o.sendMessage("Your defence level has been reduced!");
						Server.playerHandler.players[i].reduceSpellDelay[c.reduceSpellId] = System.currentTimeMillis();					
						o.getPA().refreshSkill(1);
						break;
						
						case 1542:
						Server.playerHandler.players[i].playerLevel[1] -= ((o.getPA().getLevelForXP(Server.playerHandler.players[i].playerXP[1]) * 10) / 100);
						o.sendMessage("Your defence level has been reduced!");
						Server.playerHandler.players[i].reduceSpellDelay[c.reduceSpellId] =  System.currentTimeMillis();
						o.getPA().refreshSkill(1);
						break;
						
						case 1543:
						Server.playerHandler.players[i].playerLevel[2] -= ((o.getPA().getLevelForXP(Server.playerHandler.players[i].playerXP[2]) * 10) / 100);
						o.sendMessage("Your strength level has been reduced!");
						Server.playerHandler.players[i].reduceSpellDelay[c.reduceSpellId] = System.currentTimeMillis();
						o.getPA().refreshSkill(2);
						break;
						
						case 1562:					
						Server.playerHandler.players[i].playerLevel[0] -= ((o.getPA().getLevelForXP(Server.playerHandler.players[i].playerXP[0]) * 10) / 100);
						o.sendMessage("Your attack level has been reduced!");
						Server.playerHandler.players[i].reduceSpellDelay[c.reduceSpellId] = System.currentTimeMillis();					
						o.getPA().refreshSkill(0);
						break;
					}					
				}
				
				Server.playerHandler.players[i].logoutDelay = System.currentTimeMillis();
				Server.playerHandler.players[i].underAttackBy = c.playerId;
				Server.playerHandler.players[i].killerId = c.playerId;
				Server.playerHandler.players[i].singleCombatDelay = System.currentTimeMillis();
				if(c.MAGIC_SPELLS[c.oldSpellId][6] != 0) {
					Server.playerHandler.players[i].hitDiff = damage;
					Server.playerHandler.players[i].playerLevel[3] -= damage;
					c.totalPlayerDamageDealt += damage;
					Server.playerHandler.players[i].hitUpdateRequired = true;	
				}
				c.killedBy = Server.playerHandler.players[i].playerId;	
				o.getPA().refreshSkill(3);				
				Server.playerHandler.players[i].updateRequired = true;
				c.usingMagic = false;
				c.castingMagic = false;
				c.oldSpellId = 0;
			}
		}	
		c.getPA().requestUpdates();
		
		if(c.bowSpecShot <= 0) {
			c.oldPlayerIndex = 0;	
			c.projectileStage = 0;
			c.lastWeaponUsed = 0;
			c.doubleHit = false;
			c.bowSpecShot = 0;
		}
		if(c.bowSpecShot >= 2) {
			c.bowSpecShot = 0;
			c.attackTimer = getAttackDelay(c.getItems().getItemName(c.playerEquipment[c.playerWeapon]).toLowerCase());
		}
		if(c.bowSpecShot == 1) {
			fireProjectilePlayer();
			c.hitDelay = 2;
			c.bowSpecShot = 0;
		}
	}
	
	
	public void applyPlayerMeleeDamage(int i, int damageMask){
		Client o = (Client) Server.playerHandler.players[i];
		if(o == null) {
			return;
		}
		int damage = Misc.random(calculateMeleeMaxHit());			
		if(Misc.random(10+o.getCombat().calculateMeleeDefence()) > Misc.random(10+calculateMeleeAttack())) {
			damage = 0;
		}	
		if(o.prayerActive[14]) { // if prayer active reduce damage by half 
			damage = damage / 2;
		}	
		if (Server.playerHandler.players[i].playerLevel[3] - damage < 0) { 
			damage = Server.playerHandler.players[i].playerLevel[3];
		}

		switch(c.specEffect) {
			case 1: // dragon scimmy special
			if(damage > 0) {
				if(o.prayerActive[12] || o.prayerActive[13] || o.prayerActive[14]) {
					o.headIcon = 0;
					o.getPA().sendFrame36(c.PRAYER_GLOW[12], 0);
					o.getPA().sendFrame36(c.PRAYER_GLOW[13], 0);
					o.getPA().sendFrame36(c.PRAYER_GLOW[14], 0);					
				}
				o.sendMessage("You have been injured!");
				o.stopPrayerDelay = System.currentTimeMillis();
				o.prayerActive[12] = false;
				o.prayerActive[13] = false;
				o.prayerActive[14] = false;
				o.getPA().requestUpdates();
			
			}
			break;
		}
		c.specEffect = 0;
		if(c.fightMode == 3) {
			c.getPA().addSkillXP((damage*Config.MELEE_EXP_RATE/3), 0); 
			c.getPA().addSkillXP((damage*Config.MELEE_EXP_RATE/3), 1);
			c.getPA().addSkillXP((damage*Config.MELEE_EXP_RATE/3), 2); 				
			c.getPA().addSkillXP((damage*Config.MELEE_EXP_RATE/3), 3);
			c.getPA().refreshSkill(0);
			c.getPA().refreshSkill(1);
			c.getPA().refreshSkill(2);
			c.getPA().refreshSkill(3);
		} else {
			c.getPA().addSkillXP((damage*Config.MELEE_EXP_RATE), c.fightMode); 
			c.getPA().addSkillXP((damage*Config.MELEE_EXP_RATE/4), 3);
			c.getPA().refreshSkill(c.fightMode);
			c.getPA().refreshSkill(3);
		}
		Server.playerHandler.players[i].logoutDelay = System.currentTimeMillis();
		Server.playerHandler.players[i].underAttackBy = c.playerId;
		Server.playerHandler.players[i].killerId = c.playerId;	
		Server.playerHandler.players[i].singleCombatDelay = System.currentTimeMillis();
		c.killedBy = Server.playerHandler.players[i].playerId;
		
		switch(damageMask) {
			case 1:
			Server.playerHandler.players[i].hitDiff = damage;
			Server.playerHandler.players[i].playerLevel[3] -= damage;
			c.totalPlayerDamageDealt += damage;
			Server.playerHandler.players[i].hitUpdateRequired = true;	
			Server.playerHandler.players[i].updateRequired = true;
			o.getPA().refreshSkill(3);
			break;
		
			case 2:
			Server.playerHandler.players[i].hitDiff2 = damage;
			Server.playerHandler.players[i].playerLevel[3] -= damage;
			c.totalPlayerDamageDealt += damage;
			Server.playerHandler.players[i].hitUpdateRequired2 = true;	
			Server.playerHandler.players[i].updateRequired = true;	
			c.doubleHit = false;
			o.getPA().refreshSkill(3);
			break;
			
		}		
	}
	
	public void fireProjectilePlayer() {
		if(c.oldPlayerIndex > 0) {
			if(Server.playerHandler.players[c.oldPlayerIndex] != null) {
				c.projectileStage = 2;
				int pX = c.getX();
				int pY = c.getY();
				int oX = Server.playerHandler.players[c.oldPlayerIndex].getX();
				int oY = Server.playerHandler.players[c.oldPlayerIndex].getY();
				int offX = (pY - oY)* -1;
				int offY = (pX - oX)* -1;	
				c.getPA().createPlayersProjectile(pX, pY, offX, offY, 50, getProjectileSpeed(), getRangeProjectileGFX(), 43, 31, - c.oldPlayerIndex - 1, getProjectileShowDelay());
			}
		}
	}
	

	
	/**Prayer**/
		
	public void activatePrayer(int i) {
		if(c.duelRule[7]){
			for(int p = 0; p < c.PRAYER.length; p++) { // reset prayer glows 
				c.prayerActive[p] = false;
				c.getPA().sendFrame36(c.PRAYER_GLOW[p], 0);	
			}
			c.sendMessage("Prayer has been disabled in this duel!");
			return;
		}
	
		if(c.playerLevel[5] > 0 || !Config.PRAYER_POINTS_REQUIRED){
			if(c.getPA().getLevelForXP(c.playerXP[5]) >= c.PRAYER_LEVEL_REQUIRED[i] || !Config.PRAYER_LEVEL_REQUIRED) {
				boolean headIcon = false;
				switch(i) {
					case 0:
					if(c.prayerActive[i] == false) {
						c.getPA().sendFrame36(c.PRAYER_GLOW[3], 0);
						c.prayerActive[3] = false;
						c.getPA().sendFrame36(c.PRAYER_GLOW[9], 0);
						c.prayerActive[9] = false;
					}
					break;
					
					case 1:
					if(c.prayerActive[i] == false) {
						c.getPA().sendFrame36(c.PRAYER_GLOW[4], 0);
						c.prayerActive[4] = false;
						c.getPA().sendFrame36(c.PRAYER_GLOW[10], 0);
						c.prayerActive[10] = false;
					}
					break;
					
					case 2:
					if(c.prayerActive[i] == false) {
						c.getPA().sendFrame36(c.PRAYER_GLOW[5], 0);
						c.prayerActive[5] = false;
						c.getPA().sendFrame36(c.PRAYER_GLOW[11], 0);
						c.prayerActive[11] = false;
					}
					break;
					
					case 3:
					if(c.prayerActive[i] == false) {
						c.getPA().sendFrame36(c.PRAYER_GLOW[0], 0);
						c.prayerActive[0] = false;
						c.getPA().sendFrame36(c.PRAYER_GLOW[9], 0);
						c.prayerActive[9] = false;
					}
					break;
					case 4:
					if(c.prayerActive[i] == false) {
						c.getPA().sendFrame36(c.PRAYER_GLOW[1], 0);
						c.prayerActive[1] = false;
						c.getPA().sendFrame36(c.PRAYER_GLOW[10], 0);
						c.prayerActive[10] = false;
					}
					break;
					case 5:
					if(c.prayerActive[i] == false) {
						c.getPA().sendFrame36(c.PRAYER_GLOW[2], 0);
						c.prayerActive[2] = false;
						c.getPA().sendFrame36(c.PRAYER_GLOW[11], 0);
						c.prayerActive[11] = false;
					}
					break;
					case 8:

					break;
						
					case 9:
					if(c.prayerActive[i] == false) {
						c.getPA().sendFrame36(c.PRAYER_GLOW[0], 0);
						c.prayerActive[0] = false;
						c.getPA().sendFrame36(c.PRAYER_GLOW[3], 0);
						c.prayerActive[3] = false;
					}
					break;
					
					case 10:
					if(c.prayerActive[i] == false) {
						c.getPA().sendFrame36(c.PRAYER_GLOW[1], 0);
						c.prayerActive[1] = false;
						c.getPA().sendFrame36(c.PRAYER_GLOW[4], 0);
						c.prayerActive[4] = false;
					}
					break;
					
					case 11:
					if(c.prayerActive[i] == false) {
						c.getPA().sendFrame36(c.PRAYER_GLOW[2], 0);
						c.prayerActive[2] = false;
						c.getPA().sendFrame36(c.PRAYER_GLOW[5], 0);
						c.prayerActive[5] = false;
					}
					break;

					case 12:					
					case 13:
					case 14:
					if(System.currentTimeMillis() - c.stopPrayerDelay < 5000) {
						c.sendMessage("You have been injured and can't use this prayer!");
						c.getPA().sendFrame36(c.PRAYER_GLOW[12], 0);
						c.getPA().sendFrame36(c.PRAYER_GLOW[13], 0);
						c.getPA().sendFrame36(c.PRAYER_GLOW[14], 0);
						return;
					}
					case 15:
					case 16:
					case 17:
					headIcon = true;		
					for(int p = 12; p < 18; p++) {
						if(i != p) {
							c.prayerActive[p] = false;
							c.getPA().sendFrame36(c.PRAYER_GLOW[p], 0);
						}
					}				
					break;
				}
				
				if(!headIcon) {
					if(c.prayerActive[i] == false) {
						c.prayerActive[i] = true;
						c.getPA().sendFrame36(c.PRAYER_GLOW[i], 1);					
					} else {
						c.prayerActive[i] = false;
						c.getPA().sendFrame36(c.PRAYER_GLOW[i], 0);
					}
				} else {
					if(c.prayerActive[i] == false) {
						c.prayerActive[i] = true;
						c.getPA().sendFrame36(c.PRAYER_GLOW[i], 1);
						c.headIcon = c.PRAYER_HEAD_ICONS[i];
						c.getPA().requestUpdates();
					} else {
						c.prayerActive[i] = false;
						c.getPA().sendFrame36(c.PRAYER_GLOW[i], 0);
						c.headIcon = 0;
						c.getPA().requestUpdates();
					}
				}
			} else {
				c.getPA().sendFrame36(c.PRAYER_GLOW[i],0);
				c.getPA().sendFrame126("You need a @blu@Prayer level of "+c.PRAYER_LEVEL_REQUIRED[i]+" to use "+c.PRAYER_NAME[i]+".", 357);
				c.getPA().sendFrame126("Click here to continue", 358);
				c.getPA().sendFrame164(356);
			}
		} else {
			c.getPA().sendFrame36(c.PRAYER_GLOW[i],0);
			c.sendMessage("You have run out of prayer points!");
		}	
				
	}
		
	/**
	*Specials
	**/
	
	public void activateSpecial(int weapon, int i){
		if(Server.npcHandler.npcs[i] == null && c.npcIndex > 0) {
			return;
		}
		if(Server.playerHandler.players[i] == null && c.playerIndex > 0) {
			return;
		}
		c.doubleHit = false;
		c.specEffect = 0;
		c.projectileStage = 0;
		c.specMaxHitIncrease = 2;
		if(c.npcIndex > 0) {
			c.oldNpcIndex = i;
		} else if (c.playerIndex > 0){
			c.oldPlayerIndex = i;
		}
		switch(weapon) {
			
			case 1305: // dragon long
			c.gfx100(248);
			c.startAnimation(1058);
			c.hitDelay = getHitDelay(c.getItems().getItemName(c.playerEquipment[c.playerWeapon]).toLowerCase());
			break;
			
			case 1215: // dragon daggers
			case 1231:
			case 5680:
			case 5698:
			c.gfx100(252);
			c.startAnimation(1062);
			c.hitDelay = getHitDelay(c.getItems().getItemName(c.playerEquipment[c.playerWeapon]).toLowerCase());
			c.doubleHit = true;
			break;
			
			case 4151: // whip
			if(Server.npcHandler.npcs[i] != null) {
				Server.npcHandler.npcs[i].gfx100(341);
			}
			if(Server.playerHandler.players[i] != null) {
				Server.playerHandler.players[i].gfx100(341);
			}
			c.startAnimation(1658);
			c.hitDelay = getHitDelay(c.getItems().getItemName(c.playerEquipment[c.playerWeapon]).toLowerCase());
			break;
			
			case 3204: // d hally
			c.gfx100(282);
			c.startAnimation(1203);
			c.hitDelay = getHitDelay(c.getItems().getItemName(c.playerEquipment[c.playerWeapon]).toLowerCase());
			if(Server.npcHandler.npcs[i] != null && c.npcIndex > 0) {
				if(!c.goodDistance(c.getX(), c.getY(), Server.npcHandler.npcs[i].getX(), Server.npcHandler.npcs[i].getY(), 1)){
					c.doubleHit = true;
				}
			}
			if(Server.playerHandler.players[i] != null && c.playerIndex > 0) {
				if(!c.goodDistance(c.getX(), c.getY(), Server.playerHandler.players[i].getX(),Server.playerHandler.players[i].getY(), 1)){
					c.doubleHit = true;
				}
			}
			break;
			
			case 4153: // maul
			c.startAnimation(1667);
			c.hitDelay = getHitDelay(c.getItems().getItemName(c.playerEquipment[c.playerWeapon]).toLowerCase());
			attackNpc(i);
			c.gfx100(337);
			break;
			
			case 4587: // dscimmy
			c.gfx100(347);
			c.specEffect = 1;
			c.startAnimation(1872);
			c.hitDelay = getHitDelay(c.getItems().getItemName(c.playerEquipment[c.playerWeapon]).toLowerCase());
			break;
			
			case 1434: // mace
			c.startAnimation(1060);
			c.gfx100(251);
			c.specMaxHitIncrease = 3;
			c.hitDelay = getHitDelay(c.getItems().getItemName(c.playerEquipment[c.playerWeapon]).toLowerCase())+1;
			break;
			
			case 859: // magic long
			c.usingBow = true;
			c.bowSpecShot = 3;
			c.rangeItemUsed = c.playerEquipment[c.playerArrows];
			c.getItems().deleteArrow();	
			c.lastWeaponUsed = weapon;
			c.startAnimation(426);
			c.gfx100(250);	
			c.hitDelay = getHitDelay(c.getItems().getItemName(c.playerEquipment[c.playerWeapon]).toLowerCase());
			c.projectileStage = 1;
			break;
			
			case 861: // magic short	
			c.usingBow = true;			
			c.bowSpecShot = 1;
			c.rangeItemUsed = c.playerEquipment[c.playerArrows];
			c.getItems().deleteArrow();	
			c.lastWeaponUsed = weapon;
			c.startAnimation(1074);
			c.hitDelay = 3;
			c.projectileStage = 1;
			break;
		}
		c.usingSpecial = false;
		c.getItems().updateSpecialBar();
	}
	
	
	public boolean checkSpecAmount(int weapon) {
		switch(weapon) {
			case 1215:
			case 1231:
			case 5680:
			case 5698:
			case 1305:
			case 1434:
			if(c.specAmount >= 2.5) {
				c.specAmount -= 2.5;
				c.getItems().addSpecialBar(weapon);
				return true;
			}
			return false;
			
			case 4153:
			case 4151:
			if(c.specAmount >= 5) {
				c.specAmount -= 5;
				c.getItems().addSpecialBar(weapon);
				return true;
			}
			return false;
			
			case 3204:
			if(c.specAmount >= 3) {
				c.specAmount -= 3;
				c.getItems().addSpecialBar(weapon);
				return true;
			}
			return false;
			
			case 1377:
			if(c.specAmount >= 10) {
				c.specAmount -= 10;
				c.getItems().addSpecialBar(weapon);
				return true;
			}
			return false;
			
			case 4587:
			case 859:
			case 861:
			if(c.specAmount >= 5.5) {
				c.specAmount -= 5.5;
				c.getItems().addSpecialBar(weapon);
				return true;
			}
			return false;

			
			default:
			return true; // incase u want to test a weapon
		}
	}
	
	public void resetPlayerAttack() {
		c.usingMagic = false;
		c.faceUpdate(0);
		c.followId = 0;
		c.playerIndex = 0;
	}
	
	public int getCombatDifference(int combat1, int combat2) {
		if(combat1 > combat2) {
			return (combat1 - combat2);
		}
		if(combat2 > combat1) {
			return (combat2 - combat1);
		}	
		return 0;
	}
	
	/**
	*Get killer id 
	**/
	
	public int getKillerId(int playerId) {
		int oldDamage = 0;
		int count = 0;
		int killerId = 0;
		for (int i = 1; i < Config.MAX_PLAYERS; i++) {	
			if (Server.playerHandler.players[i] != null) {
				if(Server.playerHandler.players[i].killedBy == playerId) {
					if(Server.playerHandler.players[i].totalPlayerDamageDealt > oldDamage) {
						oldDamage = Server.playerHandler.players[i].totalPlayerDamageDealt;
						killerId = i;
					}
					Server.playerHandler.players[i].totalPlayerDamageDealt = 0;
					Server.playerHandler.players[i].killedBy = 0;
				}	
			}
		}				
		return killerId;
	}
		
	public int getPrayerDelay() {
		c.usingPrayer = false;
		int delay = 10000;	
		for(int i = 0; i < c.prayerActive.length; i++) {
			if(c.prayerActive[i] == true) {
				c.usingPrayer = true;
				delay -= c.PRAYER_DRAIN_RATE[i];
			}
		}
		delay += c.playerBonus[11]*500;
		return delay;
	}
	
	public void reducePrayerLevel() {
		c.prayerDelay = System.currentTimeMillis();
		if(c.playerLevel[5] - 1 > 0) {
			c.playerLevel[5] -= 1;
		} else {
			c.sendMessage("You have run out of prayer points!");
			c.playerLevel[5] = 0;
			resetPrayers();
			c.prayerId = -1;	
		}
		c.getPA().refreshSkill(5);
	}
	
	public void resetPrayers() {
		for(int i = 0; i < c.prayerActive.length; i++) {
			c.prayerActive[i] = false;
			c.getPA().sendFrame36(c.PRAYER_GLOW[i], 0);
		}
		c.headIcon = 0;
		c.getPA().requestUpdates();
	}
	
	/**
	* Wildy and duel info
	**/
	
	public boolean checkReqs() {
		if(Server.playerHandler.players[c.playerIndex] == null) {
			return false;
		}
		if(Server.playerHandler.players[c.playerIndex].inDuelArena() && c.duelStatus != 5 && !c.usingMagic) {
			if(c.arenas() || c.duelStatus == 5) {
				c.sendMessage("You can't challenge inside the arena!");
				return false;
			}
			c.getTradeAndDuel().requestDuel(c.playerIndex);
			return false;
		}
		if(c.duelStatus == 5 && Server.playerHandler.players[c.playerIndex].duelStatus == 5) {
			if(Server.playerHandler.players[c.playerIndex].duelingWith == c.getId()) {
				return true;
			} else {
				c.sendMessage("This isn't your opponent!");
				return false;
			}
		}
		if(!Server.playerHandler.players[c.playerIndex].inWild()) {
			c.sendMessage("That player is not in the wilderness.");
			c.stopMovement();
			c.getCombat().resetPlayerAttack();
			return false;
		}
		if(Config.COMBAT_LEVEL_DIFFERENCE) {
			int combatDif1 = c.getCombat().getCombatDifference(c.combatLevel, Server.playerHandler.players[c.playerIndex].combatLevel);
			if(combatDif1 > c.wildLevel || combatDif1 > Server.playerHandler.players[c.playerIndex].wildLevel) {
				c.sendMessage("Your combat level difference is too great to attack that player here.");
				c.stopMovement();
				c.getCombat().resetPlayerAttack();
				return false;
			}
		}
		
		if(Config.SINGLE_AND_MULTI_ZONES) {
			if(!Server.playerHandler.players[c.playerIndex].inMulti()) {	// single combat zones
				if(Server.playerHandler.players[c.playerIndex].underAttackBy != c.playerId  && Server.playerHandler.players[c.playerIndex].underAttackBy != 0) {
					c.sendMessage("That player is already in combat.");
					c.stopMovement();
					c.getCombat().resetPlayerAttack();
					return false;
				}
				if(Server.playerHandler.players[c.playerIndex].playerId != c.underAttackBy && c.underAttackBy != 0) {
					c.sendMessage("You are already in combat.");
					c.stopMovement();
					c.getCombat().resetPlayerAttack();
					return false;
				}
			}
		}
		return true;
	}
	
	/**
	*Weapon stand, walk, run, etc emotes
	**/
	
	public void getPlayerAnimIndex(String weaponName){
		c.playerStandIndex = 0x328;
		c.playerTurnIndex = 0x337;
		c.playerWalkIndex = 0x333;
		c.playerTurn180Index = 0x334;
		c.playerTurn90CWIndex = 0x335;
		c.playerTurn90CCWIndex = 0x336;
		c.playerRunIndex = 0x338;
	
		if(weaponName.contains("halberd") || weaponName.contains("guthan")) {
			c.playerStandIndex = 809;
			c.playerWalkIndex = 1146;
			c.playerRunIndex = 1210;
			return;
		}	
		if(weaponName.contains("dharok")) {
			c.playerStandIndex = 0x811;
			c.playerWalkIndex = 0x67F;
			c.playerRunIndex = 0x680;
			return;
		}	
		if(weaponName.contains("ahrim")) {
			c.playerStandIndex = 809;
			c.playerWalkIndex = 1146;
			c.playerRunIndex = 1210;
			return;
		}
		if(weaponName.contains("verac")) {
			c.playerStandIndex = 1832;
			c.playerWalkIndex = 1830;
			c.playerRunIndex = 1831;
			return;
		}
		if(weaponName.contains("karil")) {
			c.playerStandIndex = 2074;
			c.playerWalkIndex = 2076;
			c.playerRunIndex = 2077;
			return;
		}
		if(weaponName.contains("2h sword")) {
			c.playerStandIndex = 2561;
			c.playerWalkIndex = 2064;
			c.playerRunIndex = 2563;
			return;
		}						
		if(weaponName.contains("bow")) {
			c.playerStandIndex = 808;
			c.playerWalkIndex = 819;
			c.playerRunIndex = 824;
			return;
		}

		switch(c.playerEquipment[c.playerWeapon]) {	
			case 4151:
			c.playerStandIndex = 1832;
			c.playerWalkIndex = 1660;
			c.playerRunIndex = 1661;
			break;
			case 4153:
			c.playerStandIndex = 1662;
			c.playerWalkIndex = 1663;
			c.playerRunIndex = 1664;
			break;
			case 1305:
			c.playerStandIndex = 809;
			break;
		}
	}
	
	/**
	* Weapon emotes
	**/
	
	public int getWepAnim(String weaponName) {
		if(c.playerEquipment[c.playerWeapon] <= 0) {
			switch(c.fightMode) {
				case 0:
				return 422;			
				case 2:
				return 423;			
				case 1:
				return 451;
			}
		}
		if(weaponName.contains("knife") || weaponName.contains("dart") || weaponName.contains("javelin") || weaponName.contains("thrownaxe")){
			return 806;
		}
		if(weaponName.contains("halberd")) {
			return 440;
		}
		if(weaponName.startsWith("dragon dagger")) {
			return 402;
		}	
		if(weaponName.endsWith("dagger")) {
			return 412;
		}		
		if(weaponName.contains("2h sword")) {
			return 406;
		}		
		if(weaponName.contains("sword")) {
			return 451;
		}		
		if(weaponName.contains("bow")) {
			return 426;
		}
		switch(c.playerEquipment[c.playerWeapon]) { // if you don't want to use strings
			case 6522:
			return 2614;
			case 4153: // granite maul
			return 1665;
			case 4726: // guthan 
			return 2080;
			case 4747: // torag
			return 0x814;
			case 4718: // dharok
			return 2067;
			case 4710: // ahrim
			return 406;
			case 4755: // verac
			return 2062;
			case 4734: // karil
			return 2075;
			case 4151:
			return 1658;
			
			default:
			return 451;
		}
	}
	
	/**
	* Block emotes
	*/
	public int getBlockEmote() {
		switch(c.playerEquipment[c.playerWeapon]) {
			case 4755:
			return 2063;
			
			case 4153:
			return 1666;
			
			case 4151:
			return 1659;
			
			default:
			return 404;
		}
	}
			
	/**
	* Weapon and magic attack speed!
	**/
	
	public int getAttackDelay(String weaponName) {
		if(c.usingMagic) {
			switch(c.MAGIC_SPELLS[c.spellId][0]) {
				case 12871: // ice blitz
				case 13023: // shadow barrage
				case 12891: // ice barrage
				return 10;
				
				default:
				return 6;
			}
		} else {
	
			if(weaponName.contains("knife") || weaponName.contains("dart")){
				return 3;
			}
			if(weaponName.contains("thrownaxe")){
				return 6;
			}
			if(weaponName.contains("javelin")){
				return 7;
			}
			if(weaponName.contains("halberd")){
				return 7;
			}

			switch(c.playerEquipment[c.playerWeapon]) {
				case 6522: // Toktz-xil-ul
				return 6;

				case 4151:
				return 5;

				default:
				return 6;
			}
		}
	}
	
	/**
	* How long it takes to hit your enemy
	**/
	public int getHitDelay(String weaponName) {
		if(c.usingMagic) {
			switch(c.MAGIC_SPELLS[c.oldSpellId][0]) {			
				case 12891:
				return 3;
				
				default:
				return 4;
			}
		} else {

			if(weaponName.contains("knife") || weaponName.contains("dart") || weaponName.contains("javelin") || weaponName.contains("thrownaxe")){
				return 3;
			}
			if(weaponName.contains("karils")) {
				return 3;
			}
			if(weaponName.contains("bow")) {
				return 4;
			}

			switch(c.playerEquipment[c.playerWeapon]) {	
				case 6522: // Toktz-xil-ul
				return 3;

				default:
				return 2;
			}
		}
	}
	
	public int getRequiredDistance(){
		if(c.followId > 0 && c.freezeTimer <= 0 && c.isMoving) {
			return 3;
		} else {
			return 1;
		}
	}
	
	public boolean usingHally() {
		switch(c.playerEquipment[c.playerWeapon]) {
			case 3190:
			case 3192:
			case 3194:
			case 3196:
			case 3198:
			case 3200:
			case 3202:
			case 3204:
			return true;
			
			default:
			return false;
		}
	}
	
	/**
	* Melee
	**/
	
	public int calculateMeleeAttack() {
		int attack = 0;
		int attackBonus = 0;
		
		/**
		*not right cbf doing it atm
		**/
		switch(c.fightMode) {
			case 0:
			attackBonus = c.playerBonus[0];
			break;
			
			case 1:
			attackBonus = c.playerBonus[1];
			break;
			
			case 2:
			attackBonus = c.playerBonus[2];
			break;
			
			case 3:
			attackBonus = c.playerBonus[1];
			break;
		}
		int attacklvl = c.playerLevel[0]; 
		attackBonus = attackBonus;
		attacklvl = attacklvl;
		attack = attackBonus + attacklvl;
		
		if(c.prayerActive[2]) {
			attack += (attack * 5 / 100);
		}	
		if(c.prayerActive[5]) {
			attack += (attack * 10 / 100);
		}
		if(c.prayerActive[11]) {
			attack += (attack * 15 / 100);
		}
		if(c.specMaxHitIncrease > 0) {
			attack += (attack * 10 / 100);
		}
		
		return attack;
	}
	
	public int calculateMeleeMaxHit() {
		double maxHit = 0;
		int strBonus = c.playerBonus[10];
		int strength = c.playerLevel[2]; 
		
		if (c.fightMode == 0 || c.fightMode == 1) {
			maxHit += (double)(0.90 + (double)((double)(strBonus * strength) * 0.00175));
		} else if (c.fightMode == 2) { 
			maxHit += (double)(1 + (double)((double)(strBonus * strength) * 0.00175));
		} else if (c.fightMode == 3) { 
			maxHit += (double)(0.90 + (double)((double)(strBonus* strength) * 0.00175));
		}
		maxHit += (double)(strength * 0.12);
		if(c.prayerActive[1]) {
			maxHit += (maxHit * 5 / 100);
		}	
		if(c.prayerActive[4]) {
			maxHit += (maxHit * 10 / 100);
		}
		if(c.prayerActive[10]) {
			maxHit += (maxHit * 15 / 100);
		}
		if(c.specMaxHitIncrease > 0) {
			maxHit += c.specMaxHitIncrease;
		}

		return (int)Math.floor(maxHit);
	}
	

	public int calculateMeleeDefence() {
		int def = 0;
		int defBonus = c.playerBonus[6] + c.playerBonus[7] + c.playerBonus[8];
		int deflvl = c.playerLevel[1];
		
		defBonus = defBonus / 5;
		deflvl = deflvl / 5;
		def = defBonus + deflvl;
		
		if(c.prayerActive[0]) {
			def += (def * 5 / 100);
		}	
		if(c.prayerActive[3]) {
			def += (def * 10 / 100);
		}
		if(c.prayerActive[9]) {
			def += (def * 15 / 100);
		}
		return def;
	}
	

	/**
	* Range
	**/
	
	public int calculateRangeAttack() {
		int rangeAttack = 0;	
		int rangeAttackBonus = (c.playerBonus[4] / 6);
		int rangeAttackLevel = (c.playerLevel[4] / 2);	
		rangeAttack = rangeAttackLevel + rangeAttackBonus;
		return rangeAttack;
	}
	
	public int calculateRangeDefence() {
		int def = 0;
		int defBonus = c.playerBonus[9]; 
		int deflvl = c.playerLevel[4]; 
		defBonus = defBonus / 4;
		deflvl = deflvl / 4;
		def = defBonus + deflvl;
		if(c.prayerActive[0]) {
			def += (def * 5 / 100);
		}	
		if(c.prayerActive[3]) {
			def += (def * 10 / 100);
		}
		if(c.prayerActive[9]) {
			def += (def * 15 / 100);
		}
		return def;
	}
	
	public int calculateRangeMaxHit() {
		int range = c.playerLevel[4] / 10; //Range

		if(c.specMaxHitIncrease > 0) {
			return (range + getArrowBonus() + c.specMaxHitIncrease);
		}
		
		if(c.playerEquipment[c.playerWeapon] == 4214) {
			return (range + 7);
		} else {
			return (range + getArrowBonus());
		}	
	}
	
	public int getArrowBonus() {
		switch(c.playerEquipment[c.playerArrows]) {
			case 882:
			return 1;	
			
			case 884:
			return 2;
			
			case 886:
			return 3;

			case 888:
			return 4;
			
			case 890:
			return 5;
			
			case 892:
			return 6;
			
			case 4740:
			return 7;
			
			default:
			return 0;
		}
	}
	
	public int correctBowAndArrows() {
		switch(c.playerEquipment[c.playerWeapon]) {
			
			case 839:
			case 841:
			return 882;
			
			case 843:
			case 845:
			return 884;
			
			case 847:
			case 849:
			return 886;
			
			case 851:
			case 853:
			return 888;
			
			case 855:
			case 857:
			return 890;
			
			case 859:
			case 861:
			return 892;
			
			case 4734:
			case 4935:
			case 4936:
			case 4937:
			return 4740;
			
		}
		return -1;
	}
	
	public int getRangeStartGFX() {
		switch(c.rangeItemUsed) {
			            
			case 863:
			return 220;
			case 864:
			return 219;
			case 865:
			return 221;
			case 866: // knives
			return 223;
			case 867:
			return 224;
			case 868:
			return 225;
			case 869:
			return 222;
			
			case 806:
			return 232;
			case 807:
			return 233;
			case 808:
			return 234;
			case 809: // darts
			return 235;
			case 810:
			return 236;
			case 811:
			return 237;
			
			case 825:
			return 206;
			case 826:
			return 207;
			case 827: // javelin
			return 208;
			case 828:
			return 209;
			case 829:
			return 210;
			case 830:
			return 211;

			case 800:
			return 42;
			case 801:
			return 43;
			case 802:
			return 44; // axes
			case 803:
			return 45;
			case 804:
			return 46;
			case 805:
			return 48;
								
			case 882:
			return 19;
			
			case 884:
			return 18;
			
			case 886:
			return 20;

			case 888:
			return 21;
			
			case 890:
			return 22;
			
			case 892:
			return 24;
			
			case 4212:
			case 4214:
			case 4215:
			case 4216:
			case 4217:
			case 4218:
			case 4219:
			case 4220:
			case 4221:
			case 4222:
			case 4223:
			return 250;
			
		}
		return -1;
	}
		
	public int getRangeProjectileGFX() {
		if(c.bowSpecShot > 0) {
			switch(c.rangeItemUsed) {
				default:
				return 249;
			}
		}
		switch(c.rangeItemUsed) {
			
			case 863:
			return 213;
			case 864:
			return 212;
			case 865:
			return 214;
			case 866: // knives
			return 216;
			case 867:
			return 217;
			case 868:
			return 218;	
			case 869:
			return 215;  

			case 806:
			return 226;
			case 807:
			return 227;
			case 808:
			return 228;
			case 809: // darts
			return 229;
			case 810:
			return 230;
			case 811:
			return 231;	

			case 825:
			return 200;
			case 826:
			return 201;
			case 827: // javelin
			return 202;
			case 828:
			return 203;
			case 829:
			return 204;
			case 830:
			return 205;	
			
			case 6522: // Toktz-xil-ul
			return 442;

			case 800:
			return 36;
			case 801:
			return 35;
			case 802:
			return 37; // axes
			case 803:
			return 38;
			case 804:
			return 39;
			case 805:
			return 40;

			case 882:
			return 10;
			
			case 884:
			return 9;
			
			case 886:
			return 11;

			case 888:
			return 12;
			
			case 890:
			return 13;
			
			case 892:
			return 15;
			
			case 4740: // bolt rack
			return 27;
			
			case 4212:
			case 4214:
			case 4215:
			case 4216:
			case 4217:
			case 4218:
			case 4219:
			case 4220:
			case 4221:
			case 4222:
			case 4223:
			return 249;
			
			
		}
		return -1;
	}
	
	public int getProjectileSpeed() {
		if(c.bowSpecShot == 1 || c.bowSpecShot == 2) {
			return 19;
		}
		switch(c.playerEquipment[c.playerWeapon]) {
			case 4734:
			case 4935:
			case 4936:
			case 4937:
			return 33;
			
			default:
			return 28;
		}
	}
	
	public int getProjectileShowDelay() {
		switch(c.playerEquipment[c.playerWeapon]) {
			case 863:
			case 864:
			case 865:
			case 866: // knives
			case 867:
			case 868:
			case 869:
			
			case 806:
			case 807:
			case 808:
			case 809: // darts
			case 810:
			case 811:
			
			case 825:
			case 826:
			case 827: // javelin
			case 828:
			case 829:
			case 830:
			
			case 800:
			case 801:
			case 802:
			case 803: // axes
			case 804:
			case 805:
			
			case 4734:
			case 4935:
			case 4936:
			case 4937:
			return 15; 
			
			default:
			return 5;
		}
	}
	
	/**
	*MAGIC
	**/
	
	public int calculateMagicAttack() {
		int magicalAttack = 0;	
		int magicalAttackBonus = (c.playerBonus[3] * 2);
		int magicalAttackLevel = (c.playerLevel[6] /2);	
		magicalAttack = magicalAttackLevel + magicalAttackBonus;
		return magicalAttack;
	}
	
	public int calculateMagicDefence() {
		int def = 0;
		int defBonus = c.playerBonus[8];
		int deflvl = c.playerLevel[6];
		defBonus = defBonus/2;
		deflvl = deflvl/2;
		def = defBonus + deflvl;		
		if(c.prayerActive[0]) {
			def += (def * 5 / 100);
		}	
		if(c.prayerActive[3]) {
			def += (def * 10 / 100);
		}
		if(c.prayerActive[9]) {
			def += (def * 15 / 100);
		}
		return def;
	}
	
	public boolean checkMagicReqs(int spell) {
		if(c.usingMagic && Config.RUNES_REQUIRED) { // check for runes
			if((!c.getItems().playerHasItem(c.MAGIC_SPELLS[spell][8], c.MAGIC_SPELLS[spell][9])) ||
				(!c.getItems().playerHasItem(c.MAGIC_SPELLS[spell][10], c.MAGIC_SPELLS[spell][11])) ||
				(!c.getItems().playerHasItem(c.MAGIC_SPELLS[spell][12], c.MAGIC_SPELLS[spell][13])) ||
				(!c.getItems().playerHasItem(c.MAGIC_SPELLS[spell][14], c.MAGIC_SPELLS[spell][15]))){
			c.sendMessage("You don't have the required runes to cast this spell.");
			return false;
			} 
		}

		if(c.usingMagic && c.playerIndex > 0) {
			if(Server.playerHandler.players[c.playerIndex] != null) {
				for(int r = 0; r < c.REDUCE_SPELLS.length; r++){	// reducing spells, confuse etc
					if(Server.playerHandler.players[c.playerIndex].REDUCE_SPELLS[r] == c.MAGIC_SPELLS[spell][0]) {
						c.reduceSpellId = r;
						if((System.currentTimeMillis() - Server.playerHandler.players[c.playerIndex].reduceSpellDelay[c.reduceSpellId]) > Server.playerHandler.players[c.playerIndex].REDUCE_SPELL_TIME[c.reduceSpellId]) {
							Server.playerHandler.players[c.playerIndex].canUseReducingSpell[c.reduceSpellId] = true;
						} else {
							Server.playerHandler.players[c.playerIndex].canUseReducingSpell[c.reduceSpellId] = false;
						}
						break;
					}			
				}
				if(!Server.playerHandler.players[c.playerIndex].canUseReducingSpell[c.reduceSpellId]) {
					c.sendMessage("That player is currently immune to this spell.");
					c.usingMagic = false;
					c.stopMovement();
					resetPlayerAttack();
					return false;
				}
			}
		}

		int staffRequired = getStaffNeeded();
		if(c.usingMagic && staffRequired > 0 && Config.RUNES_REQUIRED) { // staff required
			if(c.playerEquipment[c.playerWeapon] != staffRequired) {
				c.sendMessage("You need a "+c.getItems().getItemName(staffRequired).toLowerCase()+" to cast this spell.");
				return false;
			}
		}
		
		if(c.usingMagic && Config.MAGIC_LEVEL_REQUIRED) { // check magic level
			if(c.playerLevel[6] < c.MAGIC_SPELLS[spell][1]) {
				c.sendMessage("You need to have a magic level of " +c.MAGIC_SPELLS[spell][1]+" to cast this spell.");
				return false;
			}
		}
		if(c.usingMagic && Config.RUNES_REQUIRED) {
			if(c.MAGIC_SPELLS[spell][8] > 0) { // deleting runes
				c.getItems().deleteItem(c.MAGIC_SPELLS[spell][8], c.getItems().getItemSlot(c.MAGIC_SPELLS[spell][8]), c.MAGIC_SPELLS[spell][9]);
			}
			if(c.MAGIC_SPELLS[spell][10] > 0) {
				c.getItems().deleteItem(c.MAGIC_SPELLS[spell][10], c.getItems().getItemSlot(c.MAGIC_SPELLS[spell][10]), c.MAGIC_SPELLS[spell][11]);
			}
			if(c.MAGIC_SPELLS[spell][12] > 0) {
				c.getItems().deleteItem(c.MAGIC_SPELLS[spell][12], c.getItems().getItemSlot(c.MAGIC_SPELLS[spell][12]), c.MAGIC_SPELLS[spell][13]);
			}
			if(c.MAGIC_SPELLS[spell][14] > 0) {
				c.getItems().deleteItem(c.MAGIC_SPELLS[spell][14], c.getItems().getItemSlot(c.MAGIC_SPELLS[spell][14]), c.MAGIC_SPELLS[spell][15]);
			}
		}
		return true;
	}
	
	
	public int getFreezeTime() {
		switch(c.MAGIC_SPELLS[c.oldSpellId][0]) {
			case 1572:
			case 12861: // ice rush
			return 10;
						
			case 1582:
			case 12881: // ice burst
			return 15;
			
			case 1592:
			case 12871: // ice blitz
			return 20;
			
			case 12891: // ice barrage
			return 25;
			
			default:
			return 0;
		}
	}

	public int getStartHeight() {
		switch(c.MAGIC_SPELLS[c.spellId][0]) {
			case 1562: // stun
			return 25;
			
			case 12939:// smoke rush
			return 35;
			
			case 12987: // shadow rush
			return 38;
			
			case 12861: // ice rush
			return 15;
			
			case 12951:  // smoke blitz
			return 38;
			
			case 12999: // shadow blitz
			return 25;
			
			case 12911: // blood blitz
			return 25;
			
			default:
			return 43;
		}
	}
	
	public int getEndHeight() {
		switch(c.MAGIC_SPELLS[c.spellId][0]) {
			case 1562: // stun
			return 10;
			
			case 12939: // smoke rush
			return 20;
			
			case 12987: // shadow rush
			return 28;
			
			case 12861: // ice rush
			return 10;
			
			case 12951:  // smoke blitz
			return 28;
			
			case 12999: // shadow blitz
			return 15;
			
			case 12911: // blood blitz
			return 10;
				
			default:
			return 31;
		}
	}
	
	public int getStartDelay() {
		switch(c.MAGIC_SPELLS[c.spellId][0]) {
			case 1539:
			return 60;
			
			default:
			return 53;
		}
	}
	
	public int getStaffNeeded() {
		switch(c.MAGIC_SPELLS[c.spellId][0]) {
			case 1539:
			return 1409;
			
			case 12037:
			return 4170;
			
			case 1190:
			return 2415;
			
			case 1191:
			return 2416;
			
			case 1192:
			return 2417;
			
			default:
			return 0;
		}
	}
	
	public boolean godSpells() {
		switch(c.MAGIC_SPELLS[c.spellId][0]) {	
			case 1190:
			return true;
			
			case 1191:
			return true;
			
			case 1192:
			return true;
			
			default:
			return false;
		}
	}
		
	public int getEndGfxHeight() {
		switch(c.MAGIC_SPELLS[c.spellId][0]) {
			case 12987:	
			case 12901:		
			case 12861:
			case 12445:
			case 1192:
			case 13011:
			case 12919:
			case 12881:
			case 12999:
			case 12911:
			case 12871:
			case 13023:
			case 12929:
			case 12891:
			return 0;
			
			default:
			return 100;
		}
	}
	
	public int getStartGfxHeight() {
		switch(c.MAGIC_SPELLS[c.spellId][0]) {
			case 12871:
			case 12891:
			return 0;
			
			default:
			return 100;
		}
	}
	
}
