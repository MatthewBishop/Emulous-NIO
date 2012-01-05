package server.model.players.skills;

import server.*;
import server.util.*;
import server.model.players.*;

/**
* Herblore
**/

public class Herblore {
		
	
	public static void idHerb(Client c, int id, int slot, int level, int endId) {
		if(level > c.playerLevel[15]) {
			c.sendMessage("You need a herblore level of "+level+" to identify this herb.");
			return;
		}
		c.getItems().deleteItem(id, slot, 1);
		c.getItems().addItem(endId, 1);
		c.sendMessage("You identify the herb as a "+c.getItems().getItemName(endId)+".");
		c.getPA().addSkillXP(level * Config.HERBLORE_EXP, 15);
		c.getPA().refreshSkill(15);
	}

	
}