package server;

public class Config {

	public static final String SERVER_NAME = "Emulous";
	public static final int ITEM_LIMIT = 20000; // item id limit, different clients have more items like silab which goes past 15000
	public static final int MAXITEM_AMOUNT = 2000000000;
	public static final int BANK_SIZE = 200;
	public static final int MAX_PLAYERS = 512;
	
	public static final int CONNECTION_DELAY = 1000; // how long one ip can keep connecting
	public static final int IPS_ALLOWED = 2; // how many ips are allowed
		
	public static final boolean WORLD_LIST_FIX = false; // change to true if you want to stop that world--8 thing, but it can cause the screen to freeze on silabsoft client
	
	public static final int[] ITEM_SELLABLE 		=	{995}; // what items can't be sold in any store
	public static final int[] ITEM_TRADEABLE 		= 	{}; // what items can't be traded or staked
	public static final int[] UNDROPPABLE_ITEMS 	= 	{}; // what items can't be dropped
	
	public static final int[] FUN_WEAPONS	=	{2460,2461,2462,2463,2464,2465,2466,2467,2468,2469,2470,2471,2471,2473,2474,2475,2476,2477}; // fun weapons for dueling
	
	public static final boolean ADMIN_CAN_TRADE = false; //can admins trade?
	public static final boolean ADMIN_CAN_SELL_ITEMS = false; // can admins sell items?
	public static final boolean ADMIN_DROP_ITEMS = false; // can admin drop items?
	
	public static final int START_LOCATION_X = 3254; // start here
	public static final int START_LOCATION_Y = 3420;
	public static final int RESPAWN_X = 3254; // when dead respawn here
	public static final int RESPAWN_Y = 3420;
	public static final int DUELING_RESPAWN_X = 3362; // when dead in duel area spawn here
	public static final int DUELING_RESPAWN_Y = 3263;
	public static final int RANDOM_DUELING_RESPAWN = 5; // random coords
	
	public static final int NO_TELEPORT_WILD_LEVEL = 20; // level you can't tele on and above
	public static final int SKULL_TIMER = 1200; // how long does the skull last? seconds x 2
	public static final int TELEBLOCK_DELAY = 20000; // how long does teleblock last for.
	public static final boolean SINGLE_AND_MULTI_ZONES = true; // multi and single zones?
	public static final boolean COMBAT_LEVEL_DIFFERENCE = true; // wildy levels and combat level differences matters
	
	public static final boolean itemRequirements = true; // attack, def, str, range or magic levels required to wield weapons or wear items?
		
	public static final int MELEE_EXP_RATE = 300; // damage * exp rate
	public static final int RANGE_EXP_RATE = 300;
	public static final int MAGIC_EXP_RATE = 300;
	public static final int BONE_EXP = 600; // bone * bone_exp, search in ClickItem class
	public static final int HERBLORE_EXP = 25; // level required * 15
	
	public static final int INCREASE_SPECIAL_AMOUNT = 10000; // how fast your special bar refills
	public static final boolean PRAYER_POINTS_REQUIRED = true; // you need prayer points to use prayer
	public static final boolean PRAYER_LEVEL_REQUIRED = true; // need prayer level to use different prayers
	public static final boolean MAGIC_LEVEL_REQUIRED = true; // need magic level to cast spell
	public static final int GOD_SPELL_CHARGE = 30000; // how long does god spell charge last?
	public static final boolean RUNES_REQUIRED = true; // magic rune required?
	public static final boolean CORRECT_ARROWS = true; // correct arrows for bows?
	public static final boolean CRYSTAL_BOW_DEGRADES = true; // magic rune required?
	
	public static final int SAVE_TIMER = 120; // save every 1 minute
	public static final int NPC_RANDOM_WALK_DISTANCE = 3; // the square created , 3x3 so npc can't move out of that box when randomly walking
	public static final int NPC_FOLLOW_DISTANCE = 6; // how far can the npc follow you from it's spawn point, 													
	public static final int[] UNDEAD_NPCS = {90,91,92,93,94,103,104,73,74,75,76,77}; // undead npcs

	/**
	* NPC DROPS
	**/
	//{npc Type, item id , amount, chance}
	public static final int[][] NPC_DROPS = {
	
	// example with man 
	//{{npc type, red party hat, amount , chance( higher number = more rare)}, 
	// if you want the a npc to always drop an item make the chance 0
	{1,1038,1,10}, {1,526,1,0}, {1,995,120,2}, // man
	{19,526,1,0} // knight
	};
	
	
	
	/**
	* Teleport Spells
	**/
	// modern
	public static final int VARROCK_X = 3210;
	public static final int VARROCK_Y = 3424;
	
	public static final int LUMBY_X = 3222;
	public static final int LUMBY_Y = 3218;

    public static final int FALADOR_X = 2964;
	public static final int FALADOR_Y = 3378;

	public static final int CAMELOT_X = 2757;
	public static final int CAMELOT_Y = 3477;
	
	public static final int ARDOUGNE_X = 2662;
	public static final int ARDOUGNE_Y = 3305;
	
	public static final int WATCHTOWER_X = 2549;
	public static final int WATCHTOWER_Y = 3113;
	
	public static final int TROLLHEIM_X = 2549;
	public static final int TROLLHEIM_Y = 3113;
 
	// ancient
	
	public static final int PADDEWWA_X = 3098;
	public static final int PADDEWWA_Y = 9884;
	
	public static final int SENNTISTEN_X = 3322;
	public static final int SENNTISTEN_Y = 3336;

    public static final int KHARYRLL_X = 3492;
	public static final int KHARYRLL_Y = 3471;

	public static final int LASSAR_X = 3006;
	public static final int LASSAR_Y = 3471;
	
	public static final int DAREEYAK_X = 3161;
	public static final int DAREEYAK_Y = 3671;
	
	public static final int CARRALLANGAR_X = 3156;
	public static final int CARRALLANGAR_Y = 3666;
	
	public static final int ANNAKARL_X = 3288;
	public static final int ANNAKARL_Y = 3886;
	
	public static final int GHORROCK_X = 2977;
	public static final int GHORROCK_Y = 3873;
 
	public static final int TIMEOUT = 20;
	public static final int CYCLE_TIME = 500;
	public static final int BUFFER_SIZE = 10000;

	
}
