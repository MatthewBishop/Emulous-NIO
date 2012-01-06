package server.model.players.content;

import server.model.players.Client;

public class Food {

	/*
	 * Should be 1800 (3 ticks), but hayzie had 1600.
	 */
	private static final int EATING_DELAY = 1600;

	private static int[][] foodData = new int[][] { 
		{ 373, 14 }, // SWORDFISH
		{ 339, 7 }, // COD
		{ 2309, 2 }, // BREAD
		{ 351, 8 }, // PIKE
		{ 315, 3 }, // SHRIMPS
		{ 379, 12 }, // LOBSTER
		{ 1901, 5 }, // CHOCOLATE SLICE
		{ 355, 6 }, // MACKEREL
		{ 365, 13 }, // BASS
		{ 385, 20 }, // SHARK
		{ 333, 7 }, // TROUT
		{ 1891, 4 }, // CAKE
		{ 391, 22 }, // MANTA RAY
		{ 1893, 4 }, // 2/3 CAKE
		{ 361, 10 }, // TUNA
		{ 1895, 4 }, // SLICE OF CAKE
		{ 329, 9 }, // SALMON
	};

	private static int getHeal(int itemId) {
		for (int i = 0; i < foodData.length; i++) {
			if (itemId == foodData[i][0]) {
				return foodData[i][1];
			}
		}
		return -1;
	}

	public static void handleConsumption(Client c, int itemId, int itemSlot) {
		int heal = getHeal(itemId);
		if (heal != -1) {
			c.attackTimer = c.getCombat().getAttackDelay(c.getItems().getItemName(c.playerEquipment[c.playerWeapon]).toLowerCase());
			if (c.duelRule[6]) {
				c.sendMessage("Food has been disabled in this duel!");
				return;
			}
			if (!c.isDead && System.currentTimeMillis() - c.foodDelay > EATING_DELAY) {
				if (c.getItems().playerHasItem(itemId, 1, itemSlot)) {
					if (c.getLevelForXP(c.playerXP[3]) > c.playerLevel[3]) {
						c.sendMessage("You eat the " + c.getItems().getItemName(itemId).toLowerCase() + " and it restores some health.");
					} else {
						c.sendMessage("You eat the " + c.getItems().getItemName(itemId).toLowerCase() + ".");
					}
					c.foodDelay = System.currentTimeMillis();
					if (c.playerLevel[3] + heal >= c.getLevelForXP(c.playerXP[3])) {
						c.playerLevel[3] = c.getLevelForXP(c.playerXP[3]);
					} else {
						c.playerLevel[3] += heal;
					}
					c.startAnimation(829);
					c.getItems().deleteItem(itemId, itemSlot, 1);
					switch (itemId) {
						case 1891:
							c.getItems().addItem(1893, 1);
						break;
						case 1893:
							c.getItems().addItem(1895, 1);
						break;
					}
					c.getPA().refreshSkill(3);
					c.getPA().requestUpdates();
				}
			}
		}
	}
}
