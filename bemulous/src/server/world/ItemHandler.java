package server.world;

import java.io.*;
import java.util.*;

import server.*;
import server.model.players.*;
import server.util.*;
import server.model.items.*;

/**
* Handles ground items
**/

public class ItemHandler {

	public List<GroundItem> items = new ArrayList<GroundItem>();
	public static final int HIDE_TICKS = 45;
	
	public ItemHandler() {			
		for(int i = 0; i < Config.ITEM_LIMIT; i++) {
			ItemList[i] = null;
		}
		loadItemList("item.cfg");
	}
	
	/**
	* Adds item to list
	**/
	
	public void addItem(GroundItem item) {
		items.add(item);
	}
	
	/**
	* Removes item from list
	**/
	
	public void removeItem(GroundItem item) {
		items.remove(item);
	}
	
	/**
	* Item amount
	**/
	
	public int itemAmount(int itemId, int itemX, int itemY) {
		for(GroundItem i : items) {
			if(i.getItemId() == itemId && i.getItemX() == itemX && i.getItemY() == itemY) {
				return i.getItemAmount();
			}
		}
		return 0;
	}
	
	
	/**
	* Item exists
	**/
	
	public boolean itemExists(int itemId, int itemX, int itemY) {
		for(GroundItem i : items) {
			if(i.getItemId() == itemId && i.getItemX() == itemX && i.getItemY() == itemY) {
				return true;
			}
		}
		return false;
	}
	
	/**
	* Reloads any items if you enter a new region
	**/
	public void reloadItems(Client c) {	
		for(GroundItem i : items) {
			if(c != null){
				if (c.distanceToPoint(i.getItemX(), i.getItemY()) <= 60) {
					if(c.getId() == i.getItemController() && i.hideTicks > 0) {
						c.getItems().removeGroundItem(i.getItemId(), i.getItemX(), i.getItemY(), i.getItemAmount());
						c.getItems().createGroundItem(i.getItemId(), i.getItemX(), i.getItemY(), i.getItemAmount());
					}
					if(i.hideTicks == 0) {
						c.getItems().removeGroundItem(i.getItemId(), i.getItemX(), i.getItemY(), i.getItemAmount());
						c.getItems().createGroundItem(i.getItemId(), i.getItemX(), i.getItemY(), i.getItemAmount());
					}
				}
			}
		}
	}
	
	public void process() {
		for(GroundItem i : items) {
			if(i.hideTicks > 0) {
				i.hideTicks--;
			}
			if(i.hideTicks == 1) { // item can now be seen by others
				i.hideTicks = 0;
				createGlobalItem(i);
				i.removeTicks = HIDE_TICKS;
			}
			if(i.removeTicks > 0) {
				i.removeTicks--;
			}
			if(i.removeTicks == 1) {
				i.removeTicks = 0;
				removeGlobalItem(i, i.getItemId(), i.getItemX(), i.getItemY(), i.getItemAmount());
				break;
			}
		}
	}
	
	
	
	/**
	* Creates the ground item 
	**/
	
	public void createGroundItem(Client c, int itemId, int itemX, int itemY, int itemAmount, int playerId) {
		if(itemId > 0) {
			c.getItems().createGroundItem(itemId, itemX, itemY, itemAmount);
			GroundItem item = new GroundItem(itemId, itemX, itemY, itemAmount, c.playerId, HIDE_TICKS);
			addItem(item);
		}
	}
	
	
	/**
	* Shows items for everyone who is within 60 squares
	**/
	public void createGlobalItem(GroundItem i) {
		for (Player p : Server.playerHandler.players){
			if(p != null) {
			Client person = (Client)p;
				if(person != null){
					if(person.getId() != i.getItemController()) {
						if (person.distanceToPoint(i.getItemX(), i.getItemY()) <= 60) {
							person.getItems().createGroundItem(i.getItemId(), i.getItemX(), i.getItemY(), i.getItemAmount());
						}
					}
				}
			}
		}
	}
			

			
	/**
	* Removing the ground item
	**/
	
	public void removeGroundItem(Client c, int itemId, int itemX, int itemY, boolean add){
		for(GroundItem i : items) {
			if(i.getItemId() == itemId && i.getItemX() == itemX && i.getItemY() == itemY) {
				if(i.hideTicks > 0 && i.getItemController() == c.getId()) {
					if(add) {
						if(c.getItems().addItem(i.getItemId(), i.getItemAmount())) {   
							removeControllersItem(i, c, i.getItemId(), i.getItemX(), i.getItemY(), i.getItemAmount());
							break;
						}
					} else {
						removeControllersItem(i, c, i.getItemId(), i.getItemX(), i.getItemY(), i.getItemAmount());
						break;
					}
				} else {
					if(add) {
						if(c.getItems().addItem(i.getItemId(), i.getItemAmount())) {  
							removeGlobalItem(i, i.getItemId(), i.getItemX(), i.getItemY(), i.getItemAmount());
							break;
						}
					} else {
						removeGlobalItem(i, i.getItemId(), i.getItemX(), i.getItemY(), i.getItemAmount());
						break;
					}
				}
			}
		}
	}
	
	/**
	* Remove item for just the item controller (item not global yet)
	**/
	
	public void removeControllersItem(GroundItem i, Client c, int itemId, int itemX, int itemY, int itemAmount) {
		c.getItems().removeGroundItem(itemId, itemX, itemY, itemAmount);
		removeItem(i);
	}
	
	/**
	* Remove item for everyone within 60 squares
	**/
	
	public void removeGlobalItem(GroundItem i, int itemId, int itemX, int itemY, int itemAmount) {
		for (Player p : Server.playerHandler.players){
			if(p != null) {
			Client person = (Client)p;
				if(person != null){
					if (person.distanceToPoint(itemX, itemY) <= 60) {
						person.getItems().removeGroundItem(itemId, itemX, itemY, itemAmount);
						removeItem(i);	
					}
				}
			}
		}
	}
		
	

	/**
	*Item List
	**/
	
	public ItemList ItemList[] = new ItemList[Config.ITEM_LIMIT];
	

	public void newItemList(int ItemId, String ItemName, String ItemDescription, double ShopValue, double LowAlch, double HighAlch, int Bonuses[]) {
		// first, search for a free slot
		int slot = -1;
		for (int i = 0; i < 9999; i++) {
			if (ItemList[i] == null) {
				slot = i;
				break;
			}
		}

		if(slot == -1) return;		// no free slot found
		ItemList newItemList = new ItemList(ItemId);
		newItemList.itemName = ItemName;
		newItemList.itemDescription = ItemDescription;
		newItemList.ShopValue = ShopValue;
		newItemList.LowAlch = LowAlch;
		newItemList.HighAlch = HighAlch;
		newItemList.Bonuses = Bonuses;
		ItemList[slot] = newItemList;
	}

	public boolean loadItemList(String FileName) {
		String line = "";
		String token = "";
		String token2 = "";
		String token2_2 = "";
		String[] token3 = new String[10];
		boolean EndOfFile = false;
		int ReadMode = 0;
		BufferedReader characterfile = null;
		try {
			characterfile = new BufferedReader(new FileReader("./data/"+FileName));
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
				if (token.equals("item")) {
					int[] Bonuses = new int[12];
					for (int i = 0; i < 12; i++) {
						if (token3[(6 + i)] != null) {
							Bonuses[i] = Integer.parseInt(token3[(6 + i)]);
						} else {
							break;
						}
					}
					newItemList(Integer.parseInt(token3[0]), token3[1].replaceAll("_", " "), token3[2].replaceAll("_", " "), Double.parseDouble(token3[4]), Double.parseDouble(token3[4]), Double.parseDouble(token3[6]), Bonuses);
				}
			} else {
				if (line.equals("[ENDOFITEMLIST]")) {
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
