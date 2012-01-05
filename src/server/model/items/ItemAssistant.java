package server.model.items;

import java.io.*;

import server.*;
import server.util.*;
import server.model.players.*;

public class ItemAssistant {

	private Client c;
	
	public ItemAssistant(Client client) {
		this.c = client;
	}
		
	/**
	Items
	**/
	
	public void resetItems(int WriteFrame) {
		if(c.getOutStream() != null && c != null) {
			c.getOutStream().createFrameVarSizeWord(53);
			c.getOutStream().writeWord(WriteFrame);
			c.getOutStream().writeWord(c.playerItems.length);
			for (int i = 0; i < c.playerItems.length; i++) {
				if(c.playerItemsN[i] > 254) {
					c.getOutStream().writeByte(255); 		
					c.getOutStream().writeDWord_v2(c.playerItemsN[i]);
				} else {
					c.getOutStream().writeByte(c.playerItemsN[i]);
				}
				c.getOutStream().writeWordBigEndianA(c.playerItems[i]); 
			}
			c.getOutStream().endFrameVarSizeWord();
			c.flushOutStream();
		}
	}

	public void sendItemsKept() {
		if(c.getOutStream() != null && c != null ) {
	        c.getOutStream().createFrameVarSizeWord(53);
			c.getOutStream().writeWord(6963);
			c.getOutStream().writeWord(c.itemKeptId.length);
			for (int i = 0; i < c.itemKeptId.length; i++) {
				if(c.playerItemsN[i] > 254) {
					c.getOutStream().writeByte(255); 
					c.getOutStream().writeDWord_v2(1);
				} else {
					c.getOutStream().writeByte(1);
				}
				if(c.itemKeptId[i] > 0) {
				   c.getOutStream().writeWordBigEndianA(c.itemKeptId[i]+1);
				} else {
					c.getOutStream().writeWordBigEndianA(0);
				}
			}
	        c.getOutStream().endFrameVarSizeWord();   
			c.flushOutStream();
		}
    }
	
	
	/**
	* Item kept on death
	**/
	
	public void keepItem(int keepItem, boolean deleteItem) { 	
		int value = 0; 
		int item = 0;
		int slotId = 0;
		boolean itemInInventory = false;
		for(int i = 0; i < c.playerItems.length; i++) {
			if(c.playerItems[i]-1 > 0) {
				int inventoryItemValue = c.getShops().getItemShopValue(c.playerItems[i] - 1);
				if(inventoryItemValue > value && (!c.invSlot[i])) {
					value = inventoryItemValue;
					item = c.playerItems[i] - 1;
					slotId = i;
					itemInInventory = true;			
				}
			}
		}
		for(int i1 = 0; i1 < c.playerEquipment.length; i1++) {
			if(c.playerEquipment[i1] > 0) {
				int equipmentItemValue = c.getShops().getItemShopValue(c.playerEquipment[i1]);
				if(equipmentItemValue > value && (!c.equipSlot[i1])) {
					value = equipmentItemValue;
					item = c.playerEquipment[i1];
					slotId = i1;
					itemInInventory = false;			
				}
			}
		}	
		if(itemInInventory) {
			c.invSlot[slotId] = true;
			if(deleteItem) {					
				deleteItem(c.playerItems[slotId]-1, getItemSlot(c.playerItems[slotId]-1), 1);
			}
		} else {
			c.equipSlot[slotId] = true;
			if(deleteItem) {
				deleteEquipment(item, slotId);
			}		
		}
		c.itemKeptId[keepItem] = item;	
	}
		
	/**
	* Reset items kept on death
	**/
	
	public void resetKeepItems() {
		for(int i = 0; i < c.itemKeptId.length; i++) {
			c.itemKeptId[i] = -1;
		}
		for(int i1 = 0; i1 < c.invSlot.length; i1++) {
			c.invSlot[i1] = false;
		}
		for(int i2 = 0; i2 < c.equipSlot.length; i2++) {
			c.equipSlot[i2] = false;
		}		
	}
	
	/**
	* delete all items
	**/
	
	public void deleteAllItems() {	
		for(int i1 = 0; i1 < c.playerEquipment.length; i1++) {
			deleteEquipment(c.playerEquipment[i1], i1);
		}
		for(int i = 0; i < c.playerItems.length; i++) {
			deleteItem(c.playerItems[i]-1, getItemSlot(c.playerItems[i]-1), c.playerItemsN[i]);
		}
	}
	
	
	/**
	* Drop all items for your killer
	**/
	
	public void dropAllItems() {
		Client o = (Client) Server.playerHandler.players[c.killerId];
		if(o != null) {	
			if(o.playerRights == 2 && !Config.ADMIN_DROP_ITEMS) {
				return;
			}
		}
		
		for(int i = 0; i < c.playerItems.length; i++) {
			if(o != null) {
				Server.itemHandler.createGroundItem(o, c.playerItems[i] -1, c.getX(), c.getY(), c.playerItemsN[i], c.killerId);
			} else {
				Server.itemHandler.createGroundItem(c, c.playerItems[i] -1, c.getX(), c.getY(), c.playerItemsN[i], c.playerId);
			}
		} 
		for(int e = 0; e < c.playerEquipment.length; e++) {
			if(o != null) {
				Server.itemHandler.createGroundItem(o, c.playerEquipment[e], c.getX(), c.getY(), c.playerEquipmentN[e], c.killerId);
			} else {
				Server.itemHandler.createGroundItem(c, c.playerEquipment[e], c.getX(), c.getY(), c.playerEquipmentN[e], c.playerId);
			}
		}
		if(o != null) {	
			Server.itemHandler.createGroundItem(o, 526, c.getX(), c.getY(), 1, c.killerId);
		}	
	}
	
	/**
	*Add Item
	**/
	
	public boolean addItem(int item, int amount) {
		
		if (!Item.itemStackable[item] || (amount < 1)) {
			amount = 1;
		}
		if(item <= 0) {
			return false;
		}

		if ((((freeSlots() >= 1) || playerHasItem(item, 1)) && Item.itemStackable[item]) || ((freeSlots() > 0) && !Item.itemStackable[item])) {
			for (int i = 0; i < c.playerItems.length; i++) {
				if ((c.playerItems[i] == (item + 1)) && Item.itemStackable[item]
						&& (c.playerItems[i] > 0)) {
					c.playerItems[i] = (item + 1);
					if (((c.playerItemsN[i] + amount) < Config.MAXITEM_AMOUNT)
							&& ((c.playerItemsN[i] + amount) > -1)) {
						c.playerItemsN[i] += amount;
					} else {
						c.playerItemsN[i] = Config.MAXITEM_AMOUNT;
					}
					if(c.getOutStream() != null && c != null ) {	
						c.getOutStream().createFrameVarSizeWord(34);
						c.getOutStream().writeWord(3214);
						c.getOutStream().writeByte(i);
						c.getOutStream().writeWord(c.playerItems[i]);
						if (c.playerItemsN[i] > 254) {
							c.getOutStream().writeByte(255);
							c.getOutStream().writeDWord(c.playerItemsN[i]);
						} else {
							c.getOutStream().writeByte(c.playerItemsN[i]);
						}
						c.getOutStream().endFrameVarSizeWord();
						c.flushOutStream();
					}
					i = 30;
					return true;
				}
			}
			for (int i = 0; i < c.playerItems.length; i++) {
				if (c.playerItems[i] <= 0) {
					c.playerItems[i] = item + 1;
					if ((amount < Config.MAXITEM_AMOUNT) && (amount > -1)) {
						c.playerItemsN[i] = amount;
					} else {
						c.playerItemsN[i] = Config.MAXITEM_AMOUNT;
					}
					if(c.getOutStream() != null && c != null ) {
						c.getOutStream().createFrameVarSizeWord(34);
						c.getOutStream().writeWord(3214);
						c.getOutStream().writeByte(i);
						c.getOutStream().writeWord(c.playerItems[i]);
						if (c.playerItemsN[i] > 254) {
							c.getOutStream().writeByte(255);
							c.getOutStream().writeDWord(c.playerItemsN[i]);
						} else {
							c.getOutStream().writeByte(c.playerItemsN[i]);
						}
						c.getOutStream().endFrameVarSizeWord();
						c.flushOutStream();
					}
					i = 30;
					return true;
				}
			}
			return false;
		} else {
			c.sendMessage("Not enough space in your inventory.");
			return false;
		}
	}
	
	public String itemType(int item) {
		for (int i=0; i < Item.capes.length;i++) {
			if(item == Item.capes[i])
			  return "cape";
		}
		for (int i=0; i < Item.hats.length;i++) {
			if(item == Item.hats[i])
			  return "hat";
		}
		for (int i=0; i< Item.boots.length;i++) {
			if(item == Item.boots[i])
			  return "boots";
		}
		for (int i=0; i< Item.gloves.length;i++) {
			if(item == Item.gloves[i])
			  return "gloves";
		}
		for (int i=0; i< Item.shields.length;i++) {
			if(item == Item.shields[i])
			  return "shield";
		}
		for (int i=0; i< Item.amulets.length;i++) {
			if(item == Item.amulets[i])
			  return "amulet";
		}
		for (int i=0; i< Item.arrows.length;i++) {
			if(item == Item.arrows[i])
			  return "arrows";
		}
		for (int i=0; i< Item.rings.length;i++) {
			if(item == Item.rings[i])
			  return "ring";
		}
		for (int i=0; i< Item.body.length;i++) {
			if(item == Item.body[i])
			  return "body";
		}
		for (int i=0; i< Item.legs.length;i++) {
			if(item == Item.legs[i])
			  return "legs";
		}
		return "weapon";
	}
	
	/**
	*Bonuses
	**/

	public final String[] BONUS_NAMES = {
		"Stab", "Slash", "Crush", "Magic", "Range", "Stab", "Slash",
		"Crush", "Magic", "Range", "Strength", "Prayer"
	};
	public void resetBonus() {
		for (int i = 0; i < c.playerBonus.length; i++) {
			c.playerBonus[i] = 0;
		}
	}
	public void getBonus() {
		for (int i = 0; i < c.playerEquipment.length; i++) {
			if (c.playerEquipment[i] > -1) {
				for (int j = 0; j < Config.ITEM_LIMIT; j++) {
					if (Server.itemHandler.ItemList[j] != null){
							if (Server.itemHandler.ItemList[j].itemId == c.playerEquipment[i]) {
							for (int k = 0; k < c.playerBonus.length; k++) {
								c.playerBonus[k] += Server.itemHandler.ItemList[j].Bonuses[k];
							}
							break;
						}
					}
				}
			}
		}
	}
	public void writeBonus() {
		int offset = 0;
		String send = "";
		for (int i = 0; i < c.playerBonus.length; i++) {
			if (c.playerBonus[i] >= 0) {
				send = BONUS_NAMES[i]+": +"+c.playerBonus[i];
			} else {
				send = BONUS_NAMES[i]+": -"+java.lang.Math.abs(c.playerBonus[i]);
			}

			if (i == 10) {
				offset = 1;
			}
			c.getPA().sendFrame126(send, (1675+i+offset));
		}

	}
	
	
	/**
	*Wear Item
	**/

	public void sendWeapon(int Weapon, String WeaponName) {
		String WeaponName2 = WeaponName.replaceAll("Bronze", "");
		WeaponName2 = WeaponName2.replaceAll("Iron", "");
		WeaponName2 = WeaponName2.replaceAll("Steel", "");
		WeaponName2 = WeaponName2.replaceAll("Black", "");
		WeaponName2 = WeaponName2.replaceAll("Mithril", "");
		WeaponName2 = WeaponName2.replaceAll("Adamant", "");
		WeaponName2 = WeaponName2.replaceAll("Rune", "");
		WeaponName2 = WeaponName2.replaceAll("Granite", "");
		WeaponName2 = WeaponName2.replaceAll("Dragon", "");
		WeaponName2 = WeaponName2.replaceAll("Crystal", "");
		WeaponName2 = WeaponName2.trim();
		if (WeaponName.equals("Unarmed")) {
			c.setSidebarInterface(0, 5855); //punch, kick, block
			c.getPA().sendFrame126(WeaponName, 5857);
		} else if (WeaponName.endsWith("whip")) {
			c.setSidebarInterface(0, 12290); //flick, lash, deflect
			c.getPA().sendFrame246(12291, 200, Weapon);
			c.getPA().sendFrame126(WeaponName, 12293);
		} else if (WeaponName.endsWith("bow") || WeaponName.endsWith("10")|| WeaponName.endsWith("full") || WeaponName.startsWith("seercull")) {
			c.setSidebarInterface(0, 1764); //accurate, rapid, longrange
			c.getPA().sendFrame246(1765, 200, Weapon);
			c.getPA().sendFrame126(WeaponName, 1767);
		} else if (WeaponName.startsWith("Staff") || WeaponName.endsWith("staff")) {
			c.setSidebarInterface(0, 328); //spike, impale, smash, block
			c.getPA().sendFrame246(329, 200, Weapon);
			c.getPA().sendFrame126(WeaponName, 331);
		} else if (WeaponName2.startsWith("dart") || WeaponName2.startsWith("knife") || WeaponName2.startsWith("javelin") || WeaponName.equalsIgnoreCase("toktz-xil-ul")) {
			c.setSidebarInterface(0, 4446); //accurate, rapid, longrange
			c.getPA().sendFrame246(4447, 200, Weapon);
			c.getPA().sendFrame126(WeaponName, 4449);
		} else if (WeaponName2.startsWith("dagger") || WeaponName2.contains("sword")) {
			c.setSidebarInterface(0, 2276); //stab, lunge, slash, block
			c.getPA().sendFrame246(2277, 200, Weapon);
			c.getPA().sendFrame126(WeaponName, 2279);
		} else if (WeaponName2.startsWith("pickaxe")) {
			c.setSidebarInterface(0, 5570); //spike, impale, smash, block
			c.getPA().sendFrame246(5571, 200, Weapon);
			c.getPA().sendFrame126(WeaponName, 5573);
		} else if (WeaponName2.startsWith("axe") || WeaponName2.startsWith("battleaxe")) {
			c.setSidebarInterface(0, 1698); //chop, hack, smash, block
			c.getPA().sendFrame246(1699, 200, Weapon);
			c.getPA().sendFrame126(WeaponName, 1701);
		} else if (WeaponName2.startsWith("halberd")) {
			c.setSidebarInterface(0, 8460); //jab, swipe, fend
			c.getPA().sendFrame246(8461, 200, Weapon);
			c.getPA().sendFrame126(WeaponName, 8463);
		} else if (WeaponName2.startsWith("Scythe")) {
			c.setSidebarInterface(0, 8460); //jab, swipe, fend
			c.getPA().sendFrame246(8461, 200, Weapon);
			c.getPA().sendFrame126(WeaponName, 8463);
		} else if (WeaponName2.startsWith("spear")) {
			c.setSidebarInterface(0, 4679); //lunge, swipe, pound, block
			c.getPA().sendFrame246(4680, 200, Weapon);
			c.getPA().sendFrame126(WeaponName, 4682);
		} else if (WeaponName2.toLowerCase().contains("mace")){
			c.setSidebarInterface(0, 3796);
			c.getPA().sendFrame246(3797, 200, Weapon);
			c.getPA().sendFrame126(WeaponName, 3799);

		} else if (c.playerEquipment[c.playerWeapon] == 4153) {
			c.setSidebarInterface(0, 425); //war hamer equip.
			c.getPA().sendFrame246(426, 200, Weapon);
			c.getPA().sendFrame126(WeaponName, 428);		
		} else {
			c.setSidebarInterface(0, 2423); //chop, slash, lunge, block
			c.getPA().sendFrame246(2424, 200, Weapon);
			c.getPA().sendFrame126(WeaponName, 2426);
		}
		
	}
	

	/**
	*Weapon Requirements
	**/
	
	public void getRequirements(String itemName, int itemId) {
		c.attackLevelReq = c.defenceLevelReq = c.strengthLevelReq = c.rangeLevelReq = c.magicLevelReq = 0;
		if(itemName.contains("mystic")) {
			if(itemName.contains("staff")) {
				c.magicLevelReq = 20;
				c.attackLevelReq = 40;
			} else {
				c.magicLevelReq = 20;
				c.defenceLevelReq = 20;
			}
		}
		if(itemName.contains("splitbark")) {
			c.magicLevelReq = 40;
			c.defenceLevelReq = 40;
		}
		if(itemName.contains("green")) {
			if(itemName.contains("d'hide")) {
				c.rangeLevelReq = 20;
			}
		}
		if(itemName.contains("blue")) {
			if(itemName.contains("d'hide")) {
				c.rangeLevelReq = 30;
			}
		}
		if(itemName.contains("red")) {
			if(itemName.contains("d'hide")) {
				c.rangeLevelReq = 40;
			}
		}
		if(itemName.contains("black")) {
			if(itemName.contains("d'hide")) {
				c.rangeLevelReq = 50;
			}
		}
		if(itemName.contains("bronze")) {
			if(!itemName.contains("knife") && !itemName.contains("dart") && !itemName.contains("javelin") && !itemName.contains("thrownaxe")) {
				c.attackLevelReq = c.defenceLevelReq = 1;
			}
			return;
		}
		if(itemName.contains("iron")) {
			if(!itemName.contains("knife") && !itemName.contains("dart") && !itemName.contains("javelin") && !itemName.contains("thrownaxe")) {
				c.attackLevelReq = c.defenceLevelReq = 1;
			}	
			return;
		}
		if(itemName.contains("steel")) {	
			if(!itemName.contains("knife") && !itemName.contains("dart") && !itemName.contains("javelin") && !itemName.contains("thrownaxe")) {
				c.attackLevelReq = c.defenceLevelReq = 5;
			}
			return;
		}
		if(itemName.contains("black")) {
			if(!itemName.contains("knife") && !itemName.contains("dart") && !itemName.contains("javelin") && !itemName.contains("thrownaxe")) {
				c.attackLevelReq = c.defenceLevelReq = 10;
			}
			return;
		}
		if(itemName.contains("mithril")) {
			if(!itemName.contains("knife") && !itemName.contains("dart") && !itemName.contains("javelin") && !itemName.contains("thrownaxe")) {
				c.attackLevelReq = c.defenceLevelReq = 20;
			}
			return;
		}
		if(itemName.contains("adamant")) {
			if(!itemName.contains("knife") && !itemName.contains("dart") && !itemName.contains("javelin") && !itemName.contains("thrownaxe")) {
				c.attackLevelReq = c.defenceLevelReq = 30;
			}
			return;
		}
		if(itemName.contains("rune")) {
			if(!itemName.contains("knife") && !itemName.contains("dart") && !itemName.contains("javelin") && !itemName.contains("thrownaxe")) {
				c.attackLevelReq = c.defenceLevelReq = 40;
			}
			return;
		}
		if(itemName.contains("dragon")) {
			c.attackLevelReq = c.defenceLevelReq = 60;
			return;
		}
		if(itemName.contains("crystal")) {
			if(itemName.contains("shield")) {	
				c.defenceLevelReq = 70;
			} else {
				c.rangeLevelReq = 70;
			}
			return;
		}
		if(itemName.contains("ahrim")) {
			if(itemName.contains("staff")) {
				c.magicLevelReq = 70;
				c.attackLevelReq = 70;
			} else {
				c.magicLevelReq = 70;
				c.defenceLevelReq = 70;
			}
		}
		if(itemName.contains("karil")) {
			if(itemName.contains("crossbow")) {
				c.rangeLevelReq = 70;
			} else {
				c.rangeLevelReq = 70;
				c.defenceLevelReq = 70;
			}
		}		
		if(itemName.contains("verac") || itemName.contains("guthan") || itemName.contains("dharok") || itemName.contains("torag")) {

			if(itemName.contains("hammers")) {
				c.attackLevelReq = 70;
				c.strengthLevelReq = 70;
			} else if(itemName.contains("axe")) {
				c.attackLevelReq = 70;
				c.strengthLevelReq = 70;
			} else if(itemName.contains("warspear")) {
				c.attackLevelReq = 70;
				c.strengthLevelReq = 70;
			} else if(itemName.contains("flail")) {
				c.attackLevelReq = 70;
				c.strengthLevelReq = 70;
			} else {
				c.defenceLevelReq = 70;
			}
		}
			
		switch(itemId) {
			case 4151: // if you don't want to use names 
			c.attackLevelReq = 70;
			return;
			
			case 6724: // seercull
			c.rangeLevelReq = 60; // idk if that is correct
			return;
		}
	}
	
	/**
	*two handed weapon check
	**/
	public boolean is2handed(String itemName, int itemId) {
		if(itemName.contains("ahrim") || itemName.contains("karil") || itemName.contains("verac") || itemName.contains("guthan") || itemName.contains("dharok") || itemName.contains("torag")) {
			return true;
		}
		if(itemName.contains("longbow") || itemName.contains("shortbow")) {
			return true;
		}
		if(itemName.contains("crystal")) {
			return true;
		}
	
		switch(itemId) {
			case 6724: // seercull
			return true;
		}
		return false;
	}
	
	/**
	* Weapons special bar, adds the spec bars to weapons that require them
	* and removes the spec bars from weapons which don't require them
	**/
	
	public void addSpecialBar(int weapon) {
		switch(weapon) {
			
			case 4151: // whip
			c.getPA().sendFrame171(0, 12323);
			specialAmount(weapon, c.specAmount, 12335);
			break;
			
			case 859: // magic bows
			case 861:
			c.getPA().sendFrame171(0, 7549);
			specialAmount(weapon, c.specAmount, 7561);
			break;
			
			case 4587: // dscimmy
			c.getPA().sendFrame171(0, 7599);
			specialAmount(weapon, c.specAmount, 7611);
			break;
			
			case 3204: // d hally
			c.getPA().sendFrame171(0, 8493);
			specialAmount(weapon, c.specAmount, 8505);
			break;
			
			case 1377: // d battleaxe
			c.getPA().sendFrame171(0, 7499);
			specialAmount(weapon, c.specAmount, 7511);
			break;
			
			case 4153: // gmaul
			c.getPA().sendFrame171(0, 7474);
			specialAmount(weapon, c.specAmount, 7486);
			break;
	
			case 1215:// dragon dagger
			case 1231:
			case 5680:
			case 5698:
			case 1305: // dragon long
			c.getPA().sendFrame171(0, 7574); 
			specialAmount(weapon, c.specAmount, 7586);
			break;
			
			case 1434: // dragon mace
			c.getPA().sendFrame171(0, 7624);
			specialAmount(weapon, c.specAmount, 7636);
			break;
			
			default:
			c.getPA().sendFrame171(1, 7624); // mace interface
			c.getPA().sendFrame171(1, 7474); // hammer, gmaul
			c.getPA().sendFrame171(1, 7499); // axe
			c.getPA().sendFrame171(1, 7549);  // bow interface
			c.getPA().sendFrame171(1, 7574); // sword interface
			c.getPA().sendFrame171(1, 7599); // scimmy sword interface, for most swords
			c.getPA().sendFrame171(1, 8493);
			c.getPA().sendFrame171(1, 12323); // whip interface
			break;		
		}
	}
	
	/**
	* Specials bar filling amount
	**/
	
	public void specialAmount(int weapon, double specAmount, int barId) {
		c.specBarId = barId;
		c.getPA().sendFrame70(specAmount >= 10 ? 150 : 0, 0, (--barId));
        c.getPA().sendFrame70(specAmount >= 9 ? 150 : 0, 0, (--barId));
        c.getPA().sendFrame70(specAmount >= 8 ? 150 : 0, 0, (--barId));
		c.getPA().sendFrame70(specAmount >= 7 ? 150 : 0, 0, (--barId));
		c.getPA().sendFrame70(specAmount >= 6 ? 150 : 0, 0, (--barId));
		c.getPA().sendFrame70(specAmount >= 5 ? 150 : 0, 0, (--barId));
		c.getPA().sendFrame70(specAmount >= 4 ? 150 : 0, 0, (--barId));
		c.getPA().sendFrame70(specAmount >= 3 ? 150 : 0, 0, (--barId));
		c.getPA().sendFrame70(specAmount >= 2 ? 150 : 0, 0, (--barId));
		c.getPA().sendFrame70(specAmount >= 1 ? 150 : 0, 0, (--barId));	
		updateSpecialBar();
		sendWeapon(weapon, getItemName(weapon));
	}
	
	/**
	* Special attack text and what to highlight or blackout
	**/
	
	public void updateSpecialBar() {
		if(c.usingSpecial) {
			c.getPA().sendFrame126(
			""+(c.specAmount >= 2 ?  "@yel@S P" : "@bla@S P")
			+""+(c.specAmount >= 3 ?  "@yel@ E" : "@bla@ E") 
			+""+(c.specAmount >= 4 ?  "@yel@ C I" : "@bla@ C I")
			+""+(c.specAmount >= 5 ?  "@yel@ A L" : "@bla@ A L") 
			+""+(c.specAmount >= 6 ?  "@yel@  A" : "@bla@  A") 
			+""+(c.specAmount >= 7 ?  "@yel@ T T" : "@bla@ T T") 
			+""+(c.specAmount >= 8 ?  "@yel@ A" : "@bla@ A") 
			+""+(c.specAmount >= 9 ?  "@yel@ C" : "@bla@ C") 
			+""+(c.specAmount >= 10 ?  "@yel@ K" : "@bla@ K") , c.specBarId);
		} else {
			c.getPA().sendFrame126("@bla@S P E C I A L  A T T A C K", c.specBarId);
		}
	}
	
	
	/**
	*Wear Item
	**/
	
	public boolean wearItem(int wearID, int slot) {
		int targetSlot=0;
		boolean canWearItem = true;
		if(c.playerItems[slot] == (wearID+1)) {				
			getRequirements(getItemName(wearID).toLowerCase(), wearID);							
			if(itemType(wearID).equalsIgnoreCase("cape")) {
				targetSlot=1;
			} else if(itemType(wearID).equalsIgnoreCase("hat")) {
				targetSlot=0;
			} else if(itemType(wearID).equalsIgnoreCase("amulet")) {
				targetSlot=2;
			} else if(itemType(wearID).equalsIgnoreCase("arrows")) {
				targetSlot=13;
			} else if(itemType(wearID).equalsIgnoreCase("body")) {
				targetSlot=4;
			} else if(itemType(wearID).equalsIgnoreCase("shield")) {
				targetSlot=5;
			} else if(itemType(wearID).equalsIgnoreCase("legs")) {
				targetSlot=7;
			} else if(itemType(wearID).equalsIgnoreCase("gloves")) {
				targetSlot=9;
			} else if(itemType(wearID).equalsIgnoreCase("boots")) {
				targetSlot=10;	
			} else if(itemType(wearID).equalsIgnoreCase("ring")) {
				targetSlot=12;
			} else {
				targetSlot = 3;
			}
			
			if(c.duelRule[11] && targetSlot == 0) {
				c.sendMessage("Wearing hats has been disabled in this duel!");
				return false;
			}
			if(c.duelRule[12] && targetSlot == 1) {
				c.sendMessage("Wearing capes has been disabled in this duel!");
				return false;
			}
			if(c.duelRule[13]  && targetSlot == 2) {
				c.sendMessage("Wearing amulets has been disabled in this duel!");
				return false;
			}
			if(c.duelRule[14]  && targetSlot == 3) {
				c.sendMessage("Wielding weapons has been disabled in this duel!");
				return false;
			}
			if(c.duelRule[15]  && targetSlot == 4) {
				c.sendMessage("Wearing bodies has been disabled in this duel!");
				return false;
			}
			if(c.duelRule[16] && targetSlot == 5) {
				c.sendMessage("Wearing shield has been disabled in this duel!");
				return false;
			}
			if(c.duelRule[17]  && targetSlot == 7) {
				c.sendMessage("Wearing legs has been disabled in this duel!");
				return false;
			}
			if(c.duelRule[18]  && targetSlot == 9) {
				c.sendMessage("Wearing gloves has been disabled in this duel!");
				return false;
			}
			if(c.duelRule[19]  && targetSlot == 10) {
				c.sendMessage("Wearing boots has been disabled in this duel!");
				return false;
			}
			if(c.duelRule[20]  && targetSlot == 12) {
				c.sendMessage("Wearing rings has been disabled in this duel!");
				return false;
			}
			if(c.duelRule[21]  && targetSlot == 13) {
				c.sendMessage("Wearing arrows has been disabled in this duel!");
				return false;
			}

			if(Config.itemRequirements) {
				if(targetSlot == 10 || targetSlot == 7 || targetSlot == 5 || targetSlot == 4 || targetSlot == 0 || targetSlot == 9 || targetSlot == 10) {
					if(c.defenceLevelReq > 0) {
						if(c.getPA().getLevelForXP(c.playerXP[1]) < c.defenceLevelReq) {
							c.sendMessage("You need a defence level of "+c.defenceLevelReq+" to wear this item.");
							canWearItem = false;
						}
					}
					if(c.rangeLevelReq > 0) {
						if(c.getPA().getLevelForXP(c.playerXP[4]) < c.rangeLevelReq) {
							c.sendMessage("You need a range level of "+c.rangeLevelReq+" to wear this item.");
							canWearItem = false;
						}
					}
					if(c.magicLevelReq > 0) {
						if(c.getPA().getLevelForXP(c.playerXP[6]) < c.magicLevelReq) {
							c.sendMessage("You need a magic level of "+c.magicLevelReq+" to wear this item.");
							canWearItem = false;
						}
					}
				}
				if(targetSlot == 3) {
					if(c.attackLevelReq > 0) {
						if(c.getPA().getLevelForXP(c.playerXP[0]) < c.attackLevelReq) {
							c.sendMessage("You need an attack level of "+c.attackLevelReq+" to wield this weapon.");
							canWearItem = false;
						}
					}
					if(c.rangeLevelReq > 0) {
						if(c.getPA().getLevelForXP(c.playerXP[4]) < c.rangeLevelReq) {
							c.sendMessage("You need a range level of "+c.rangeLevelReq+" to wield this weapon.");
							canWearItem = false;
						}
					}
					if(c.magicLevelReq > 0) {
						if(c.getPA().getLevelForXP(c.playerXP[6]) < c.magicLevelReq) {
							c.sendMessage("You need a magic level of "+c.magicLevelReq+" to wield this weapon.");
							canWearItem = false;
						}
					}
				}
			}

			if(!canWearItem) {
				return false;
			}
			
			int wearAmount = c.playerItemsN[slot];
			if (wearAmount < 1) {
				return false;
			}
			
			if(slot >= 0 && wearID >= 0) {
				if((is2handed(getItemName(c.playerEquipment[3]).toLowerCase(), c.playerEquipment[3]) && (targetSlot == 3 || targetSlot == 5))
				|| (is2handed(getItemName(wearID).toLowerCase(), wearID) && targetSlot == 3)) {	
					if (c.playerEquipment[5] >= 0) {
						if(freeSlots() > 0) {
							removeItem(c.playerEquipment[5], 5);
						} else {
							c.sendMessage("You don't have enough space in your inventory.");
							return false;
						}
					}
					if (c.playerEquipment[3] >= 0) {
						if(freeSlots() > 0) {
							removeItem(c.playerEquipment[3], 3);
						} else {
							c.sendMessage("You don't have enough space in your inventory.");
							return false;
						}
					}
				}
				deleteItem(wearID, slot, wearAmount);
				if (c.playerEquipment[targetSlot] != wearID && c.playerEquipment[targetSlot] >= 0) {
					addItem(c.playerEquipment[targetSlot], c.playerEquipmentN[targetSlot]);
				}
				else if (Item.itemStackable[wearID] && c.playerEquipment[targetSlot] == wearID) {
					wearAmount = c.playerEquipmentN[targetSlot] + wearAmount;
				}
				else if (c.playerEquipment[targetSlot] >= 0) {
					addItem(c.playerEquipment[targetSlot], c.playerEquipmentN[targetSlot]);
				}					
			}
			if(targetSlot == 3) {
				c.usingSpecial = false;
				addSpecialBar(wearID);
			}
			if(c.getOutStream() != null && c != null ) {
				c.getOutStream().createFrameVarSizeWord(34);
				c.getOutStream().writeWord(1688);
				c.getOutStream().writeByte(targetSlot);
				c.getOutStream().writeWord(wearID+1);

				if (wearAmount > 254) {
					c.getOutStream().writeByte(255);
					c.getOutStream().writeDWord(wearAmount);
				} else {
					c.getOutStream().writeByte(wearAmount);
				}
				
				c.getOutStream().endFrameVarSizeWord();
				c.flushOutStream();
			}
			c.playerEquipment[targetSlot]=wearID;
			c.playerEquipmentN[targetSlot]=wearAmount;
			sendWeapon(c.playerEquipment[c.playerWeapon], getItemName(c.playerEquipment[c.playerWeapon]));
			resetBonus();
			getBonus();
			writeBonus();
			c.getCombat().getPlayerAnimIndex(c.getItems().getItemName(c.playerEquipment[c.playerWeapon]).toLowerCase());
	        c.updateRequired = true; 
			c.appearanceUpdateRequired = true;
			return true;
		} else {
			return false;
		}
	}
	
	
	public void wearItem(int wearID, int wearAmount, int targetSlot) {	
		if(c.getOutStream() != null && c != null ) {
			c.getOutStream().createFrameVarSizeWord(34);
			c.getOutStream().writeWord(1688);
			c.getOutStream().writeByte(targetSlot);
			c.getOutStream().writeWord(wearID+1);

			if (wearAmount > 254) {
				c.getOutStream().writeByte(255);
				c.getOutStream().writeDWord(wearAmount);
			} else {
				c.getOutStream().writeByte(wearAmount);
			}		
			c.getOutStream().endFrameVarSizeWord();
			c.flushOutStream();
			c.playerEquipment[targetSlot]=wearID;
			c.playerEquipmentN[targetSlot]=wearAmount;
			c.getItems().sendWeapon(c.playerEquipment[c.playerWeapon], c.getItems().getItemName(c.playerEquipment[c.playerWeapon]));
			c.getItems().resetBonus();
			c.getItems().getBonus();
			c.getItems().writeBonus();
			c.getCombat().getPlayerAnimIndex(c.getItems().getItemName(c.playerEquipment[c.playerWeapon]).toLowerCase());
			c.updateRequired = true; 
			c.appearanceUpdateRequired = true;
		}
	}
	

	/**
	*Remove Item
	**/
	public void removeItem(int wearID, int slot) {
		if(c.getOutStream() != null && c != null) {
			if(c.playerEquipment[slot] > -1){
				if(addItem(c.playerEquipment[slot], c.playerEquipmentN[slot])) {
					c.playerEquipment[slot]=-1;
					c.playerEquipmentN[slot]=0;
					sendWeapon(c.playerEquipment[c.playerWeapon], getItemName(c.playerEquipment[c.playerWeapon]));
					resetBonus();
					getBonus();
					writeBonus();
					c.getCombat().getPlayerAnimIndex(c.getItems().getItemName(c.playerEquipment[c.playerWeapon]).toLowerCase());
					c.getOutStream().createFrame(34);
					c.getOutStream().writeWord(6);
					c.getOutStream().writeWord(1688);
					c.getOutStream().writeByte(slot);
					c.getOutStream().writeWord(0);
					c.getOutStream().writeByte(0);
					c.flushOutStream();
					c.updateRequired = true; 
					c.appearanceUpdateRequired = true;
				}
			}
		}
	}
		
	/**
	*BANK
	*/
	
	public void rearrangeBank() {
		int totalItems = 0;
		int highestSlot = 0;
		for (int i = 0; i < Config.BANK_SIZE; i++) {
			if (c.bankItems[i] != 0) { 
				totalItems ++;
				if (highestSlot <= i) {	
					highestSlot = i;
				}
			}  
		}
		
		for (int i = 0; i <= highestSlot; i++) {
			if (c.bankItems[i] == 0) {
				boolean stop = false;
			
			for (int k = i; k <= highestSlot; k++) {
				if (c.bankItems[k] != 0 && !stop) {
					int spots = k - i;
						for (int j = k; j <= highestSlot; j++) {
							c.bankItems[j-spots] = c.bankItems[j];
							c.bankItemsN[j-spots] = c.bankItemsN[j];
							stop = true;
							c.bankItems[j] = 0; c.bankItemsN[j] = 0; 
						}
					}
				}					
			}
		}
		
	int totalItemsAfter = 0;
	for (int i = 0; i < Config.BANK_SIZE; i++) {
		if (c.bankItems[i] != 0) { 
		totalItemsAfter ++; 
		} 
	}
		
	if (totalItems != totalItemsAfter) 
		c.disconnected = true;
	}
	
	
	public void resetBank(){
		c.getOutStream().createFrameVarSizeWord(53);
		c.getOutStream().writeWord(5382); // bank
		c.getOutStream().writeWord(Config.BANK_SIZE);
        for (int i=0; i<Config.BANK_SIZE; i++){
			if (c.bankItemsN[i] > 254){
				c.getOutStream().writeByte(255);
				c.getOutStream().writeDWord_v2(c.bankItemsN[i]);
			} else {
				c.getOutStream().writeByte(c.bankItemsN[i]); 	
			}
			if (c.bankItemsN[i] < 1) {
				c.bankItems[i] = 0;
			}
			if (c.bankItems[i] > Config.ITEM_LIMIT || c.bankItems[i] < 0) {
				c.bankItems[i] = Config.ITEM_LIMIT;
			}
			c.getOutStream().writeWordBigEndianA(c.bankItems[i]); 
		}
		c.getOutStream().endFrameVarSizeWord();
		c.flushOutStream();
	}
	
	
	public void resetTempItems(){
		int itemCount = 0;
		for (int i = 0; i < c.playerItems.length; i++) {
			if (c.playerItems[i] > -1) {
				itemCount=i;
			}
		}
		c.getOutStream().createFrameVarSizeWord(53);
		c.getOutStream().writeWord(5064);
		c.getOutStream().writeWord(itemCount+1); 
		for (int i = 0; i < itemCount+1; i++) {
			if (c.playerItemsN[i] > 254) {
				c.getOutStream().writeByte(255); 						
				c.getOutStream().writeDWord_v2(c.playerItemsN[i]);
			} else {
				c.getOutStream().writeByte(c.playerItemsN[i]);
			}
			if (c.playerItems[i] > Config.ITEM_LIMIT || c.playerItems[i] < 0) {
				c.playerItems[i] = Config.ITEM_LIMIT;
			}
			c.getOutStream().writeWordBigEndianA(c.playerItems[i]); 
		}
		c.getOutStream().endFrameVarSizeWord();	
		c.flushOutStream();
	}
	
	
	public boolean bankItem(int itemID, int fromSlot, int amount){
		if (c.playerItemsN[fromSlot]<=0){
			return false;
		}
		if (!Item.itemIsNote[c.playerItems[fromSlot]-1]) {
			if (c.playerItems[fromSlot] <= 0) {
				return false;
			}
			if (Item.itemStackable[c.playerItems[fromSlot]-1] || c.playerItemsN[fromSlot] > 1) {
				int toBankSlot = 0;
				boolean alreadyInBank=false;
			    for (int i=0; i< Config.BANK_SIZE; i++) {
						if (c.bankItems[i] == c.playerItems[fromSlot]) {
							if (c.playerItemsN[fromSlot]<amount)
									amount = c.playerItemsN[fromSlot];
							alreadyInBank = true;
							toBankSlot = i;
							i=Config.BANK_SIZE+1;
						}
				}

				if (!alreadyInBank && freeBankSlots() > 0) {
						for (int i=0; i<Config.BANK_SIZE; i++) {
							if (c.bankItems[i] <= 0) {
									toBankSlot = i;
									i=Config.BANK_SIZE+1;
							}
						}
						c.bankItems[toBankSlot] = c.playerItems[fromSlot];
						if (c.playerItemsN[fromSlot]<amount){
							amount = c.playerItemsN[fromSlot];
						}
						if ((c.bankItemsN[toBankSlot] + amount) <= Config.MAXITEM_AMOUNT && (c.bankItemsN[toBankSlot] + amount) > -1) {
							c.bankItemsN[toBankSlot] += amount;
						} else {
							c.sendMessage("Bank full!");
							return false;
						}
						deleteItem((c.playerItems[fromSlot]-1), fromSlot, amount);
						resetTempItems();
						resetBank();
						return true;
				}
				else if (alreadyInBank) {
						if ((c.bankItemsN[toBankSlot] + amount) <= Config.MAXITEM_AMOUNT && (c.bankItemsN[toBankSlot] + amount) > -1) {
							c.bankItemsN[toBankSlot] += amount;
						} else {
							c.sendMessage("Bank full!");
							return false;
						}
						deleteItem((c.playerItems[fromSlot]-1), fromSlot, amount);
						resetTempItems();
						resetBank();
						return true;
				} else {
						c.sendMessage("Bank full!");
						return false;
				}
			} else {
				itemID = c.playerItems[fromSlot];
				int toBankSlot = 0;
				boolean alreadyInBank=false;
			    for (int i=0; i<Config.BANK_SIZE; i++) {
						if (c.bankItems[i] == c.playerItems[fromSlot]) {
							alreadyInBank = true;
							toBankSlot = i;
							i=Config.BANK_SIZE+1;
						}
				}
				if (!alreadyInBank && freeBankSlots() > 0) {
			       	for (int i=0; i<Config.BANK_SIZE; i++) {
						if (c.bankItems[i] <= 0) {
								toBankSlot = i;
								i=Config.BANK_SIZE+1;
						}
					}
						int firstPossibleSlot=0;
						boolean itemExists = false;
						while (amount > 0) {
							itemExists = false;
							for (int i=firstPossibleSlot; i<c.playerItems.length; i++) {
									if ((c.playerItems[i]) == itemID) {
										firstPossibleSlot = i;
										itemExists = true;
										i=30;
									}
							}
							if (itemExists) {
									c.bankItems[toBankSlot] = c.playerItems[firstPossibleSlot];
									c.bankItemsN[toBankSlot] += 1;
									deleteItem((c.playerItems[firstPossibleSlot]-1), firstPossibleSlot, 1);
									amount--;
							} else {
									amount=0;
							}
						}
						resetTempItems();
						resetBank();
						return true;
				} else if (alreadyInBank) {
						int firstPossibleSlot=0;
						boolean itemExists = false;
						while (amount > 0) {
							itemExists = false;
							for (int i=firstPossibleSlot; i<c.playerItems.length; i++) {
								if ((c.playerItems[i]) == itemID) {
									firstPossibleSlot = i;
									itemExists = true;
									i=30;
								}
							}
							if (itemExists) {
									c.bankItemsN[toBankSlot] += 1;
									deleteItem((c.playerItems[firstPossibleSlot]-1), firstPossibleSlot, 1);
									amount--;
							} else {
									amount=0;
							}
						}
						resetTempItems();
						resetBank();
						return true;
				} else {
						c.sendMessage("Bank full!");
						return false;
				}
			}
		}
		else if (Item.itemIsNote[c.playerItems[fromSlot]-1] && !Item.itemIsNote[c.playerItems[fromSlot]-2]) {
			if (c.playerItems[fromSlot] <= 0) {
				return false;
			}
			if (Item.itemStackable[c.playerItems[fromSlot]-1] || c.playerItemsN[fromSlot] > 1) {
				int toBankSlot = 0;
				boolean alreadyInBank=false;
			    for (int i=0; i<Config.BANK_SIZE; i++) {
						if (c.bankItems[i] == (c.playerItems[fromSlot]-1)) {
							if (c.playerItemsN[fromSlot]<amount)
									amount = c.playerItemsN[fromSlot];
						alreadyInBank = true;
						toBankSlot = i;
						i=Config.BANK_SIZE+1;
						}
				}

				if (!alreadyInBank && freeBankSlots() > 0) {
			       	for (int i=0; i<Config.BANK_SIZE; i++) {
						if (c.bankItems[i] <= 0) {
								toBankSlot = i;
								i=Config.BANK_SIZE+1;
						}
					}
					c.bankItems[toBankSlot] = (c.playerItems[fromSlot]-1);
					if (c.playerItemsN[fromSlot]<amount){
						amount = c.playerItemsN[fromSlot];
					}
					if ((c.bankItemsN[toBankSlot] + amount) <= Config.MAXITEM_AMOUNT && (c.bankItemsN[toBankSlot] + amount) > -1) {
						c.bankItemsN[toBankSlot] += amount;
					} else {
						return false;
					}
					deleteItem((c.playerItems[fromSlot]-1), fromSlot, amount);
					resetTempItems();
					resetBank();
					return true;
				}
				else if (alreadyInBank) {
					if ((c.bankItemsN[toBankSlot] + amount) <= Config.MAXITEM_AMOUNT && (c.bankItemsN[toBankSlot] + amount) > -1) {
						c.bankItemsN[toBankSlot] += amount;
					} else {
						return false;
					}
					deleteItem((c.playerItems[fromSlot]-1), fromSlot, amount);
					resetTempItems();
					resetBank();
					return true;
				} else {
						c.sendMessage("Bank full!");
						return false;
				}
			} else {
				itemID = c.playerItems[fromSlot];
				int toBankSlot = 0;
				boolean alreadyInBank=false;
			    for (int i=0; i<Config.BANK_SIZE; i++) {
					if (c.bankItems[i] == (c.playerItems[fromSlot]-1)) {
						alreadyInBank = true;
						toBankSlot = i;
						i=Config.BANK_SIZE+1;
					}
				}
				if (!alreadyInBank && freeBankSlots() > 0) {
			       	for (int i=0; i<Config.BANK_SIZE; i++) {
						if (c.bankItems[i] <= 0){
								toBankSlot = i;
								i=Config.BANK_SIZE+1;
						}
					}
						int firstPossibleSlot=0;
						boolean itemExists = false;
						while (amount > 0) {
							itemExists = false;
							for (int i=firstPossibleSlot; i<c.playerItems.length; i++) {
								if ((c.playerItems[i]) == itemID) {
									firstPossibleSlot = i;
									itemExists = true;
									i=30;
								}
							}
							if (itemExists) {
									c.bankItems[toBankSlot] = (c.playerItems[firstPossibleSlot]-1);
									c.bankItemsN[toBankSlot] += 1;
									deleteItem((c.playerItems[firstPossibleSlot]-1), firstPossibleSlot, 1);
									amount--;
							} else {
									amount=0;
							}
						}
						resetTempItems();
						resetBank();
						return true;
				}
				else if (alreadyInBank) {
						int firstPossibleSlot=0;
						boolean itemExists = false;
						while (amount > 0) {
							itemExists = false;
							for (int i=firstPossibleSlot; i<c.playerItems.length; i++) {
								if ((c.playerItems[i]) == itemID) {
									firstPossibleSlot = i;
									itemExists = true;
									i=30;
								}
							}
							if (itemExists) {
									c.bankItemsN[toBankSlot] += 1;
									deleteItem((c.playerItems[firstPossibleSlot]-1), firstPossibleSlot, 1);
									amount--;
							} else {
									amount=0;
							}
						}
						resetTempItems();
						resetBank();
						return true;
				} else {
						c.sendMessage("Bank full!");
						return false;
				}
			}
		} else {
			c.sendMessage("Item not supported "+(c.playerItems[fromSlot]-1));
			return false;
		}
	}
	
	
	public int freeBankSlots(){
		int freeS=0;
        for (int i=0; i < Config.BANK_SIZE; i++) {
			if (c.bankItems[i] <= 0) {
				freeS++;
			}
		}
		return freeS;
	}
	
	
	public void fromBank(int itemID, int fromSlot, int amount) {
		if (amount > 0) {
		  if (c.bankItems[fromSlot] > 0) {
			if (!c.takeAsNote) {
			  if (Item.itemStackable[c.bankItems[fromSlot]-1]) {
				if (c.bankItemsN[fromSlot] > amount) {
				  if (addItem((c.bankItems[fromSlot]-1), amount)) {
					c.bankItemsN[fromSlot] -= amount;
					resetBank();
					c.getItems().resetItems(5064);
				  }
				} else {
				  if (addItem((c.bankItems[fromSlot]-1), c.bankItemsN[fromSlot])) {
					c.bankItems[fromSlot] = 0;
					c.bankItemsN[fromSlot] = 0;
					resetBank();
					c.getItems().resetItems(5064);
				  }
				}
			  } else {
				while (amount > 0) {
				  if (c.bankItemsN[fromSlot] > 0) {
					if (addItem((c.bankItems[fromSlot]-1), 1)) {
					  c.bankItemsN[fromSlot] += -1;
					  amount--;
					} else {
					  amount = 0;
					}
				  } else {
					amount = 0;
				  }
				}
				resetBank();
				c.getItems().resetItems(5064);
			  }
			} else if (c.takeAsNote && Item.itemIsNote[c.bankItems[fromSlot]]) {
			  if (c.bankItemsN[fromSlot] > amount) {
				if (addItem(c.bankItems[fromSlot], amount)) {
				  c.bankItemsN[fromSlot] -= amount;
				  resetBank();
				  c.getItems().resetItems(5064);
				}
			  } else {
				if (addItem(c.bankItems[fromSlot], c.bankItemsN[fromSlot])) {
				  c.bankItems[fromSlot] = 0;
				  c.bankItemsN[fromSlot] = 0;
				  resetBank();
				  c.getItems().resetItems(5064);
				}
			  }
			} else {
			  c.sendMessage("This item can't be withdrawn as a note.");
			  if (Item.itemStackable[c.bankItems[fromSlot]-1]) {
				if (c.bankItemsN[fromSlot] > amount) {
				  if (addItem((c.bankItems[fromSlot]-1), amount)) {
					c.bankItemsN[fromSlot] -= amount;
					resetBank();
					c.getItems().resetItems(5064);
				  }
				} else {
				  if (addItem((c.bankItems[fromSlot]-1), c.bankItemsN[fromSlot])) {
					c.bankItems[fromSlot] = 0;
					c.bankItemsN[fromSlot] = 0;
					resetBank();
					c.getItems().resetItems(5064);
				  }
				}
			  } else {
				while (amount > 0) {
				  if (c.bankItemsN[fromSlot] > 0) {
					if (addItem((c.bankItems[fromSlot]-1), 1)) {
					  c.bankItemsN[fromSlot] += -1;
					  amount--;
					} else {
					  amount = 0;
					}
				  } else {
					amount = 0;
				  }
				}
				resetBank();
				c.getItems().resetItems(5064);
			  }
			}
		  }
		}
	}
  
  	public int itemAmount(int itemID){
		int tempAmount=0;
        for (int i=0; i < c.playerItems.length; i++) {
			if (c.playerItems[i] == itemID) {
				tempAmount+=c.playerItemsN[i];
			}
		}
		return tempAmount;
	}
	
	
	
	/**
	*Update Equip tab
	**/

	
	public void setEquipment(int wearID, int amount, int targetSlot) {
		c.getOutStream().createFrameVarSizeWord(34);
		c.getOutStream().writeWord(1688);
		c.getOutStream().writeByte(targetSlot);
		c.getOutStream().writeWord(wearID+1);
		if (amount > 254) {
			c.getOutStream().writeByte(255);
			c.getOutStream().writeDWord(amount);
		} else {
			c.getOutStream().writeByte(amount);	
		}
		c.getOutStream().endFrameVarSizeWord();
		c.flushOutStream();
		c.playerEquipment[targetSlot]=wearID;
		c.playerEquipmentN[targetSlot]=amount;
		c.updateRequired = true; 
		c.appearanceUpdateRequired = true;
	}
	
	
	/**
	*Move Items
	**/
	
	public void moveItems(int from, int to, int moveWindow) {
		if (moveWindow == 3724) {
			int tempI;
			int tempN;
			tempI = c.playerItems[from];
			tempN = c.playerItemsN[from];

			c.playerItems[from] = c.playerItems[to];
			c.playerItemsN[from] = c.playerItemsN[to];
			c.playerItems[to] = tempI;
			c.playerItemsN[to] = tempN;
		}

		if (moveWindow == 34453 && from >= 0 && to >= 0 && from < Config.BANK_SIZE && to < Config.BANK_SIZE) {
			int tempI;
			int tempN;
			tempI = c.bankItems[from];
			tempN = c.bankItemsN[from];

			c.bankItems[from] = c.bankItems[to];
			c.bankItemsN[from] = c.bankItemsN[to];
			c.bankItems[to] = tempI;
			c.bankItemsN[to] = tempN;
		}

		if (moveWindow == 34453) {
			resetBank();
		}
		if (moveWindow == 18579) {
		}
			resetTempItems();
		if (moveWindow == 3724) {
			resetItems(3214);
		}

	}
	
	/**
	*delete Item
	**/
	
	public void deleteEquipment(int i, int j) {
		if(Server.playerHandler.players[c.playerId] == null || c.disconnected) {
			return;
		}
		if(i < 0) {
			return;
		}
		
		c.playerEquipment[j] = -1;
		c.playerEquipmentN[j] = c.playerEquipmentN[j] - 1;
		c.getOutStream().createFrame(34);
		c.getOutStream().writeWord(6);
		c.getOutStream().writeWord(1688);
		c.getOutStream().writeByte(j);
		c.getOutStream().writeWord(0);
		c.getOutStream().writeByte(0);
		getBonus();
		if(j == c.playerWeapon) {
		 sendWeapon(-1, "Unarmed");
		}
		resetBonus();
		getBonus();
		writeBonus();
		c.updateRequired = true; 
		c.appearanceUpdateRequired = true;			
   	}
	
	public void deleteItem(int id, int slot, int amount) {
		if(id <= 0) {
			return;
		}
		if (c.playerItems[slot] == (id+1)) {
			if (c.playerItemsN[slot] > amount) {
				c.playerItemsN[slot] -= amount;
			} else {
				c.playerItemsN[slot] = 0;
				c.playerItems[slot] = 0;
			}
			resetItems(3214);
		}
	}
	
	/**
	* Delete Arrows
	**/
	public void deleteArrow() {
		if(c.playerEquipmentN[c.playerArrows] == 1) {
			c.getItems().deleteEquipment(c.playerEquipment[c.playerArrows], c.playerArrows);
		}
		if(c.playerEquipmentN[c.playerArrows] != 0) {
			c.getOutStream().createFrameVarSizeWord(34);
			c.getOutStream().writeWord(1688);
			c.getOutStream().writeByte(c.playerArrows);
			c.getOutStream().writeWord(c.playerEquipment[c.playerArrows]+1);
			if (c.playerEquipmentN[c.playerArrows] -1 > 254) {
				c.getOutStream().writeByte(255);
				c.getOutStream().writeDWord(c.playerEquipmentN[c.playerArrows] -1);
			} else {
				c.getOutStream().writeByte(c.playerEquipmentN[c.playerArrows] -1); 
			}
			c.getOutStream().endFrameVarSizeWord();
			c.flushOutStream();
			c.playerEquipmentN[c.playerArrows] -= 1;
		}  
		c.updateRequired = true; 
		c.appearanceUpdateRequired = true;
	}
	
	public void deleteEquipment() {
		if(c.playerEquipmentN[c.playerWeapon] == 1) {
			c.getItems().deleteEquipment(c.playerEquipment[c.playerWeapon], c.playerWeapon);
		}
		if(c.playerEquipmentN[c.playerWeapon] != 0) {
			c.getOutStream().createFrameVarSizeWord(34);
			c.getOutStream().writeWord(1688);
			c.getOutStream().writeByte(c.playerWeapon);
			c.getOutStream().writeWord(c.playerEquipment[c.playerWeapon]+1);
			if (c.playerEquipmentN[c.playerWeapon] -1 > 254) {
				c.getOutStream().writeByte(255);
				c.getOutStream().writeDWord(c.playerEquipmentN[c.playerWeapon] -1);
			} else {
				c.getOutStream().writeByte(c.playerEquipmentN[c.playerWeapon] -1); 
			}
			c.getOutStream().endFrameVarSizeWord();
			c.flushOutStream();
			c.playerEquipmentN[c.playerWeapon] -= 1;
		}  
		c.updateRequired = true; 
		c.appearanceUpdateRequired = true;
	}
	
	/**
	* Dropping Arrows
	**/
	
	public void dropArrowNpc() {
		int enemyX = Server.npcHandler.npcs[c.oldNpcIndex].getX();
		int enemyY = Server.npcHandler.npcs[c.oldNpcIndex].getY();
		if(Misc.random(1) == 1) {
			if (Server.itemHandler.itemAmount(c.rangeItemUsed, enemyX, enemyY) == 0) {
				Server.itemHandler.createGroundItem(c, c.rangeItemUsed, enemyX, enemyY, 1, c.getId());
			} else if (Server.itemHandler.itemAmount(c.rangeItemUsed, enemyX, enemyY) != 0) {
				int amount = Server.itemHandler.itemAmount(c.rangeItemUsed, enemyX, enemyY);
				Server.itemHandler.removeGroundItem(c, c.rangeItemUsed, enemyX, enemyY, false);
				Server.itemHandler.createGroundItem(c, c.rangeItemUsed, enemyX, enemyY, amount+1, c.getId());
			}		
		}
	}	
	
	public void dropArrowPlayer() {
		int enemyX = Server.playerHandler.players[c.oldPlayerIndex].getX();
		int enemyY = Server.playerHandler.players[c.oldPlayerIndex].getY();
		if(Misc.random(1) == 1) {
			if (Server.itemHandler.itemAmount(c.rangeItemUsed, enemyX, enemyY) == 0) {
				Server.itemHandler.createGroundItem(c, c.rangeItemUsed, enemyX, enemyY, 1, c.getId());
			} else if (Server.itemHandler.itemAmount(c.rangeItemUsed, enemyX, enemyY) != 0) {
				int amount = Server.itemHandler.itemAmount(c.rangeItemUsed, enemyX, enemyY);
				Server.itemHandler.removeGroundItem(c, c.rangeItemUsed, enemyX, enemyY, false);
				Server.itemHandler.createGroundItem(c, c.rangeItemUsed, enemyX, enemyY, amount+1, c.getId());
			}		
		}
	}	
	
	
	public void removeAllItems() {
		for (int i = 0; i < c.playerItems.length; i++) {
			c.playerItems[i] = 0;
		}
		for (int i = 0; i < c.playerItemsN.length; i++) {
			c.playerItemsN[i] = 0;
		}
		resetItems(3214);
	}
	
	public int freeSlots(){
		int freeS=0;
        for (int i=0; i < c.playerItems.length; i++){
			if (c.playerItems[i] <= 0){
				freeS++;
			}
		}
		return freeS;
	}
	
	public int findItem(int id, int[] items, int[] amounts) {
		for (int i = 0; i < c.playerItems.length; i++) {
			if (((items[i] - 1) == id) && (amounts[i] > 0)) {
				return i;
			}
		}
		return -1;
	}
	
	public String getItemName(int ItemID) {
		for (int i = 0; i < Config.ITEM_LIMIT; i++) {
			if (Server.itemHandler.ItemList[i] != null) {
				if (Server.itemHandler.ItemList[i].itemId == ItemID) {
					return Server.itemHandler.ItemList[i].itemName;
				}
			}
		}
		return "Unarmed";
	}
	
	public int getItemSlot(int ItemID) {
		for (int i = 0; i < c.playerItems.length; i++) {
			if((c.playerItems[i] - 1) == ItemID){
				return i;
			}
		}	
		return -1;
	}
	
	public int getItemAmount(int ItemID) {
		int itemCount = 0;
		for (int i = 0; i < c.playerItems.length; i++) {
			if((c.playerItems[i] - 1) == ItemID) {
				itemCount++;
			}
		}
		return itemCount;
	}
	
	
	public boolean playerHasItem(int itemID, int amt, int slot) {
	    itemID++;
	    int found = 0;
		if (c.playerItems[slot] == (itemID)) {
			for (int i = 0; i < c.playerItems.length; i++)  {
				if (c.playerItems[i] == itemID)  {
					if(c.playerItemsN[i] >= amt) {
						return true;
					} else {
						found++;
					}
            	}
        	}
			if(found >= amt) {
				return true;
			}
        	return false;
		}
		return false;
	}
	
	
	public boolean playerHasItem(int itemID, int amt) {
	    itemID++;
	    int found = 0;
		for (int i = 0; i < c.playerItems.length; i++) {
            if (c.playerItems[i] == itemID) {
		    	if(c.playerItemsN[i] >= amt){
					return true;
				} else{
			    	found++;
				}
            }
        }
			if(found >= amt) {
				return true;
			}
        	return false;
	}
	
	public int getUnnotedItem(int ItemID) {
		int NewID = ItemID - 1;
		String NotedName = "";
		for (int i = 0; i < Config.ITEM_LIMIT; i++) {
			if (Server.itemHandler.ItemList[i] != null) {
				if (Server.itemHandler.ItemList[i].itemId == ItemID) {
					NotedName = Server.itemHandler.ItemList[i].itemName;
				}
			}
		}
		for (int i = 0; i < Config.ITEM_LIMIT; i++) {
			if (Server.itemHandler.ItemList[i] != null) {
				if (Server.itemHandler.ItemList[i].itemName == NotedName) {
					if (Server.itemHandler.ItemList[i].itemDescription.startsWith("Swap this note at any bank for a") == false) {
						NewID = Server.itemHandler.ItemList[i].itemId;
						break;
					}
				}
			}
		}
		return NewID;
	}
	
	
	/**
	*Drop Item
	**/
	
	public void createGroundItem(int itemID, int itemX, int itemY, int itemAmount) {
		c.getOutStream().createFrame(85);
		c.getOutStream().writeByteC((itemY - 8 * c.mapRegionY));
		c.getOutStream().writeByteC((itemX - 8 * c.mapRegionX));
		c.getOutStream().createFrame(44);
		c.getOutStream().writeWordBigEndianA(itemID);
		c.getOutStream().writeWord(itemAmount);
		c.getOutStream().writeByte(0);	
		c.flushOutStream();
	}
	
	/**
	*Pickup Item
	**/
	
	public void removeGroundItem(int itemID, int itemX, int itemY, int Amount) {
		c.getOutStream().createFrame(85);
		c.getOutStream().writeByteC((itemY - 8 * c.mapRegionY));
		c.getOutStream().writeByteC((itemX - 8 * c.mapRegionX));
		c.getOutStream().createFrame(156);
		c.getOutStream().writeByteS(0);
		c.getOutStream().writeWord(itemID);
		c.flushOutStream();
	}
	

}