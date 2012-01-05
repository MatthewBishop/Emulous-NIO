package server;

import java.io.*;
import java.util.ArrayList;

public class ConnectionHandler {

	public static ArrayList <String>connectedIps = new ArrayList<String> ();
	public static ArrayList <String>bannedIps = new ArrayList<String> ();
	public static ArrayList <String>bannedNames = new ArrayList<String> ();
	public static long connectionTime = 0;
	
	
	/**
	* Adds the banned usernames and ips from the text file to the ban list
	**/
	public static void initialize() {
		banUsers();
		banIps();
	}

	/**
	* Adding IP
	**/
	public static void addIp(String IP) {
		connectionTime = System.currentTimeMillis();
		connectedIps.add(IP);
	}
		
	/**
	* Removing IP
	**/
	public static void removeIp(String IP) {
		connectionTime = System.currentTimeMillis();
		connectedIps.remove(IP);
	}
	
	/**
	* Contains IP
	**/
	public static boolean containsIp(String IP) {
		int connections = 0;	
		for(String con : connectedIps) {
			if(con.equalsIgnoreCase(IP)) {
				connections++;
			}
		}	
		if(connections >= Config.IPS_ALLOWED) {
			return true;
		}
		return false;		
	}
	
	
	/**
	* Adding Ban IP
	**/
	public static void addIpToBanList(String IP) {
		bannedIps.add(IP);
	}
	
	
	/**
	* Removing Ban IP
	**/
	public static void removeIpFromBanList(String IP) {
		bannedIps.remove(IP);
	}
	
	/**
	* Contains Ban IP
	**/
	public static boolean isIpBanned(String IP) {
		if(bannedIps.contains(IP)) {
			return true;
		}
		return false;
	}
	
	
	/**
	* Flooding
	**/
	public static boolean floodProtection(String IP) {
		if(connectedIps.lastIndexOf(IP) == connectedIps.size()-1) {
			if(System.currentTimeMillis() - connectionTime > Config.CONNECTION_DELAY) {
				return false;
			} else {
				return true;
			}
		}
		return false;
	}
	
	
	/**
	* Adding banned username
	**/
	public static void addNameToBanList(String name) {
		bannedNames.add(name.toLowerCase());
	}
	
	
	/**
	* Removing banned username
	**/
	public static void removeNameFromBanList(String name) {
		bannedNames.remove(name.toLowerCase());
	}
	
	/**
	* Contains banned username
	**/
	public static boolean isNamedBanned(String name) {
		if(bannedNames.contains(name.toLowerCase())) {
			return true;
		}
		return false;
	}
	
	
	/**
	* Reads all usernames from text file then adds them all to the ban list
	**/
	public static void banUsers() {
		try {
			BufferedReader in = new BufferedReader(new FileReader("./data/UsersBanned.txt"));
			String data = null;
			try {
				while ((data = in.readLine()) != null) {
					addNameToBanList(data);
				}
			} finally {
				in.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	* Writes the username into the text file - when using the ::ban playername command
	**/
	public static void addNameToFile(String Name) {
		try {
			BufferedWriter out = new BufferedWriter(new FileWriter("./data/UsersBanned.txt", true));
		    try {
				out.newLine();
				out.write(Name);
		    } finally {
				out.close();
		    }
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	/**
	* Reads all the Ips from text file then adds them all to ban list
	**/
	public static void banIps() {
		try {
			BufferedReader in = new BufferedReader(new FileReader("./data/IpsBanned.txt"));
			String data = null;
			try {
				while ((data = in.readLine()) != null) {
					addIpToBanList(data);
				}
			} finally {
				in.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	/**
	* Writes the IP into the text file - use ::ipban username
	**/
	public static void addIpToFile(String Name) {
		try {
			BufferedWriter out = new BufferedWriter(new FileWriter("./data/IpsBanned.txt", true));
		    try {
				out.newLine();
				out.write(Name);
		    } finally {
				out.close();
		    }
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	

}