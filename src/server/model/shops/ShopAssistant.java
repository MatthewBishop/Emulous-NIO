package server.model.shops;

import java.io.*;

import server.*;
import server.util.*;
import server.model.players.*;
import server.model.items.*;

public class ShopAssistant {

	private Client c;
	
	public ShopAssistant(Client client) {
		this.c = client;
	}
	
	/**
	*Shops
	**/
	
	public void openShop(int ShopID){		
		c.getItems().resetItems(3823);
		resetShop(ShopID);
		c.isShopping = true;
		c.myShopId = ShopID;
		c.getPA().sendFrame248(3824, 3822);
		c.getPA().sendFrame126(Server.shopHandler.ShopName[ShopID], 3901);
	}
	
	public void updatePlayerShop() {
		for (int i = 1; i < Config.MAX_PLAYERS; i++) {
			if (Server.playerHandler.players[i] != null) {
				if (Server.playerHandler.players[i].isShopping == true && Server.playerHandler.players[i].myShopId == c.myShopId && i != c.playerId) {
					Server.playerHandler.players[i].updateShop = true;
				}
			}
		}
	}
	
	
	public void updateshop(int i){
		resetShop(i);
	}
	
	public void resetShop(int ShopID) {
		int TotalItems = 0;
		for (int i = 0; i < Server.shopHandler.MaxShopItems; i++) {
			if (Server.shopHandler.ShopItems[ShopID][i] > 0) {
				TotalItems++;
			}
		}
		if (TotalItems > Server.shopHandler.MaxShopItems) {
			TotalItems = Server.shopHandler.MaxShopItems;
		}
		c.getOutStream().createFrameVarSizeWord(53);
		c.getOutStream().writeWord(3900);
		c.getOutStream().writeWord(TotalItems);
		int TotalCount = 0;
		for (int i = 0; i < Server.shopHandler.ShopItems.length; i++) {
			if (Server.shopHandler.ShopItems[ShopID][i] > 0 || i <= Server.shopHandler.ShopItemsStandard[ShopID]) {
				if (Server.shopHandler.ShopItemsN[ShopID][i] > 254) {
					c.getOutStream().writeByte(255); 					
					c.getOutStream().writeDWord_v2(Server.shopHandler.ShopItemsN[ShopID][i]);	
				} else {
					c.getOutStream().writeByte(Server.shopHandler.ShopItemsN[ShopID][i]);
				}
				if (Server.shopHandler.ShopItems[ShopID][i] > Config.ITEM_LIMIT || Server.shopHandler.ShopItems[ShopID][i] < 0) {
					Server.shopHandler.ShopItems[ShopID][i] = Config.ITEM_LIMIT;
				}
				c.getOutStream().writeWordBigEndianA(Server.shopHandler.ShopItems[ShopID][i]);
				TotalCount++;
			}
			if (TotalCount > TotalItems) {
				break;
			}
		}
		c.getOutStream().endFrameVarSizeWord();
		c.flushOutStream();		
	}
	
	
	public double getItemShopValue(int ItemID, int Type, int fromSlot) {
		double ShopValue = 1;
		double Overstock = 0;
		double TotPrice = 0;
		for (int i = 0; i < Config.ITEM_LIMIT; i++) {
			if (Server.itemHandler.ItemList[i] != null) {
				if (Server.itemHandler.ItemList[i].itemId == ItemID) {
					ShopValue = Server.itemHandler.ItemList[i].ShopValue;
				}
			}
		}
		Overstock = Server.shopHandler.ShopItemsN[c.myShopId][fromSlot] - Server.shopHandler.ShopItemsSN[c.myShopId][fromSlot];
		TotPrice = (ShopValue * 1.26875);
		if (Overstock > 0) {
			TotPrice += ((ShopValue / 1000) * (1.26875 * Overstock)); // SHOP BUY ADDS 
		} else if (Overstock < 0) { 
			TotPrice -= ((ShopValue / 1000) * (1.26875 * Overstock)); // SHOP SELLS MINUS
		}
		if (Server.shopHandler.ShopBModifier[c.myShopId] == 1) {
			TotPrice *= 1; 
			TotPrice *= 1;
			if (Type == 1) {
				TotPrice *= 1; 
			}
		} else if (Type == 1) {
			TotPrice *= 1; 
		}
		return TotPrice;
	}
	
	public int getItemShopValue(int itemId) {
		for (int i = 0; i < Config.ITEM_LIMIT; i++) {
			if (Server.itemHandler.ItemList[i] != null) {
				if (Server.itemHandler.ItemList[i].itemId == itemId) {
					return (int)Server.itemHandler.ItemList[i].ShopValue;
				}
			}	
		}
		return 0;
	}
	
	
	
	/**
	*buy item from shop (Shop Price)
	**/
	
	public void buyFromShopPrice(int removeId, int removeSlot){
		int ShopValue = (int)Math.floor(getItemShopValue(removeId, 0, removeSlot));
		String ShopAdd = "";
		if (ShopValue >= 1000 && ShopValue < 1000000) {
			ShopAdd = " (" + (ShopValue / 1000) + "K)";
		} else if (ShopValue >= 1000000) {
			ShopAdd = " (" + (ShopValue / 1000000) + " million)";
		}
		c.sendMessage(c.getItems().getItemName(removeId)+": currently costs "+ShopValue+" coins"+ShopAdd);
	}
	
	
	
	/**
	*Sell item to shop (Shop Price)
	**/
	public void sellToShopPrice(int removeId, int removeSlot) {
		for (int i : Config.ITEM_SELLABLE) {
			if (i == removeId) {
				c.sendMessage("You can't sell "+c.getItems().getItemName(removeId).toLowerCase()+".");
				return;
			} 
		}
		boolean IsIn = false;
		if (Server.shopHandler.ShopSModifier[c.myShopId] > 1) {
			for (int j = 0; j <= Server.shopHandler.ShopItemsStandard[c.myShopId]; j++) {
				if (removeId == (Server.shopHandler.ShopItems[c.myShopId][j] - 1)) {
					IsIn = true;
					break;
				}
			}
		} else {
			IsIn = true;
		}
		if (IsIn == false) {
			c.sendMessage("You can't sell "+c.getItems().getItemName(removeId).toLowerCase()+" to this store.");
		} else {
			int ShopValue = (int)Math.floor(getItemShopValue(removeId, 1, removeSlot)/2);
			String ShopAdd = "";
			if (ShopValue >= 1000 && ShopValue < 1000000) {
				ShopAdd = " (" + (ShopValue / 1000) + "K)";
			} else if (ShopValue >= 1000000) {
				ShopAdd = " (" + (ShopValue / 1000000) + " million)";
			}
			c.sendMessage(c.getItems().getItemName(removeId)+": shop will buy for "+ShopValue+" coins"+ShopAdd);
		}
	}
	
	
	
	public boolean sellItem(int itemID, int fromSlot, int amount) {
		for (int i : Config.ITEM_SELLABLE) {
			if (i == itemID) {
				c.sendMessage("You can't sell "+c.getItems().getItemName(itemID).toLowerCase()+".");
				return false;
			} 
		}
		if(c.playerRights == 2 && !Config.ADMIN_CAN_SELL_ITEMS) {
			c.sendMessage("Selling items as an admin has been disabled.");
			return false;
		}
		
		if (amount > 0 && itemID == (c.playerItems[fromSlot] - 1)) {
			if (Server.shopHandler.ShopSModifier[c.myShopId] > 1) {
				boolean IsIn = false;
				for (int i = 0; i <= Server.shopHandler.ShopItemsStandard[c.myShopId]; i++) {
					if (itemID == (Server.shopHandler.ShopItems[c.myShopId][i] - 1)) {
						IsIn = true;
						break;
					}
				}
				if (IsIn == false) {
					c.sendMessage("You can't sell "+c.getItems().getItemName(itemID).toLowerCase()+" to this store.");
					return false;
				}
			}

			if (amount > c.playerItemsN[fromSlot] && (Item.itemIsNote[(c.playerItems[fromSlot] - 1)] == true || Item.itemStackable[(c.playerItems[fromSlot] - 1)] == true)) {
				amount = c.playerItemsN[fromSlot];
			} else if (amount > c.getItems().getItemAmount(itemID) && Item.itemIsNote[(c.playerItems[fromSlot] - 1)] == false && Item.itemStackable[(c.playerItems[fromSlot] - 1)] == false) {
				amount = c.getItems().getItemAmount(itemID);
			}
			double ShopValue;
			double TotPrice;
			int TotPrice2;
			int Overstock;
			for (int i = amount; i > 0; i--) {
				TotPrice2 = (int)Math.floor(getItemShopValue(itemID, 1, fromSlot)/2);
				if (c.getItems().freeSlots() > 0) {
					if (Item.itemIsNote[itemID] == false) {
						c.getItems().deleteItem(itemID, c.getItems().getItemSlot(itemID), 1);
					} else {
						c.getItems().deleteItem(itemID, fromSlot, 1);
					}
					c.getItems().addItem(995, TotPrice2);
					addShopItem(itemID, 1);
				} else {
					c.sendMessage("You don't have enough space in your inventory.");
					break;
				}
			}
			c.getItems().resetItems(3823);
			resetShop(c.myShopId);
			updatePlayerShop();
			return true;
		}
		return true;
	}
	
	public boolean addShopItem(int itemID, int amount) {
		boolean Added = false;
		if (amount <= 0) {
			return false;
		}
		if (Item.itemIsNote[itemID] == true) {
			itemID = c.getItems().getUnnotedItem(itemID);
		}
		for (int i = 0; i < Server.shopHandler.ShopItems.length; i++) {
			if ((Server.shopHandler.ShopItems[c.myShopId][i] - 1) == itemID) {
				Server.shopHandler.ShopItemsN[c.myShopId][i] += amount;
				Added = true;
			}
		}
		if (Added == false) {
			for (int i = 0; i < Server.shopHandler.ShopItems.length; i++) {
				if (Server.shopHandler.ShopItems[c.myShopId][i] == 0) {
					Server.shopHandler.ShopItems[c.myShopId][i] = (itemID + 1);
					Server.shopHandler.ShopItemsN[c.myShopId][i] = amount;
					Server.shopHandler.ShopItemsDelay[c.myShopId][i] = 0;
					break;
				}
			}
		}
		return true;
	}
	
	public boolean buyItem(int itemID, int fromSlot, int amount) {
		if (amount > 0 && itemID == (Server.shopHandler.ShopItems[c.myShopId][fromSlot] - 1)) {
			if (amount > Server.shopHandler.ShopItemsN[c.myShopId][fromSlot]) {
				amount = Server.shopHandler.ShopItemsN[c.myShopId][fromSlot];
			}
			double ShopValue;
			double TotPrice;
			int TotPrice2;
			int Overstock;
			int Slot = 0;
			for (int i = amount; i > 0; i--) {
				TotPrice2 = (int)Math.floor(getItemShopValue(itemID, 0, fromSlot));
				Slot = c.getItems().getItemSlot(995);
				if (Slot == -1) {
					c.sendMessage("You don't have enough coins.");
					break;
				}
                        if(TotPrice2 <= 1) {
							TotPrice2 = (int)Math.floor(getItemShopValue(itemID, 0, fromSlot));
                        }
				if (c.playerItemsN[Slot] >= TotPrice2) {
					if (c.getItems().freeSlots() > 0) {
						c.getItems().deleteItem(995, c.getItems().getItemSlot(995), TotPrice2);
						c.getItems().addItem(itemID, 1);
						Server.shopHandler.ShopItemsN[c.myShopId][fromSlot] -= 1;
						Server.shopHandler.ShopItemsDelay[c.myShopId][fromSlot] = 0;
						if ((fromSlot + 1) > Server.shopHandler.ShopItemsStandard[c.myShopId]) {
							Server.shopHandler.ShopItems[c.myShopId][fromSlot] = 0;
						}
					} else {
						c.sendMessage("You don't have enough space in your inventory.");
						break;
					}
				} else {
					c.sendMessage("You don't have enough coins.");
					break;
				}
			}
			c.getItems().resetItems(3823);
			resetShop(c.myShopId);
			updatePlayerShop();
			return true;
		}
		return false;
	}
	
	

}