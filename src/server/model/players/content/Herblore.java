package server.model.players.content;

import java.util.HashMap;
import java.util.Map;

import server.*;
import server.model.players.*;

public class Herblore {

	public static enum Herb {
		GRIMY_GUAM(199, 249, 3, 2.5), 
		GRIMY_MARRENTILL(201, 251, 5, 3.8), 
		GRIMY_TARROMIN(203, 253, 11, 5.0),
		GRIMY_HARRALANDER(205, 255, 20, 6.3), 
		GRIMY_RANARR(207, 257, 25, 7.5), 
		GRIMY_TOADFLAX(3049, 2998, 30, 8.0), 
		GRIMY_IRIT(209, 259, 40, 8.8), 
		GRIMY_AVANTOE(211, 261, 48, 10.0), 
		GRIMY_KWUARM(213, 263, 54, 11.3), 
		GRIMY_SNAPDRAGON(2485, 3000, 59, 11.8), 
		GRIMY_CADANTINE(215, 265, 65, 12.5), 
		GRIMY_LANTADYME(1531, 2481, 67, 13.1), 
		GRIMY_DWARF_WEED(217, 267, 70, 13.8), 
		GRIMY_SNAKE_WEED(1525, 269, 75, 15.0);

		private int grimy;
		private int clean;
		private int level;
		private double xpReward;

		private Herb(int grimy, int clean, int level, double xpReward) {
			this.grimy = grimy;
			this.clean = clean;
			this.level = level;
			this.xpReward = xpReward;
		}
	}

	private static Map<Integer, Herb> herbCleaning = new HashMap<Integer, Herb>();

	static {
		for (Herb herb : Herb.values()) {
			herbCleaning.put(herb.grimy, herb);
		}
	}

	public static void idHerb(Client c, int id, int slot) {
		Herb herb = herbCleaning.get(id);
		if (herb != null) {
			if (herb.level > c.playerLevel[15]) {
				c.sendMessage("You need a herblore level of " + herb.level + " to identify this herb.");
				return;
			}
			c.getItems().deleteItem(id, slot, 1);
			c.getItems().addItem(herb.clean, 1);
			c.sendMessage("You identify the herb as a " + c.getItems().getItemName(herb.clean) + ".");
			c.getPA().addSkillXP((int)(herb.xpReward * Config.HERBLORE_EXP), 15);
			c.getPA().refreshSkill(15);
		}
	}
}