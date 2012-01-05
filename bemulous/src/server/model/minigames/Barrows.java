package server.model.minigames;

import server.util.Misc;
import server.model.players.Client;
public class Barrows {

	public static final int[][] COFFIN_AND_BROTHERS = {
	{6823, 2030},
	{6772, 2029},
	{6822, 2028},
	{6773, 2027},
	{6771, 2026},
	{6821, 2025}
	
	};
	
	/**
	* Picking the random coffin
	**/
	public static int getRandomCoffin() {
		return Misc.random(COFFIN_AND_BROTHERS.length-1);
	}
	
	/**
	* Selects the coffin and shows the interface if coffin id matches random coffin
	**/
	public static boolean selectCoffin(Client c, int coffinId) {
		if(c.randomCoffin == 0) {
			c.randomCoffin = getRandomCoffin();
		}
		
		if(COFFIN_AND_BROTHERS[c.randomCoffin][0] == coffinId) {
			c.dialogueId = 1;
			c.getPA().sendStatement("You found a hidden tunnel! Do you want to enter it?");
			return true;
		}
		return false;
	}
	
	
}