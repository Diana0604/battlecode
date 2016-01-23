package team375;

import java.util.Random;
import battlecode.common.*;

public class Message {
	private long array;
	private MapLocation sender;
	private int mode;
	private int object;
	private int robotType;
	private int x;
	private int y; //X i Y es calculen respecte la posicio del que envia ( (128,128) -> posicio del que envia) 
	// Per enviar: x = rc.getlocation - location + 128
	// Per rebre: location = rc.getlocaion + 128 - x
	private int destID;
	private int typeControl;
	private int idControl;
	private int senderArchon; //1 si es un archon, 0 si es un scout
	
	/*
	 * Constants del mode
	 */
	static int GO_TO = 0;
	static int GET_OUT = 1;
	static int FOUND = 2;
	static int ESCAPE = 3;
	static int LOCATION = 4;
	static int SHOOT = 5;
	static int STAGE2 = 6;
	static int CLEAR_RUBBLE = 7;
	static int REG_DEN = 8;
	
	/*
	 * Constants del object
	 */
	static int MY_ARCHON = 1;
	static int ENEMY_ARCHON = 2;
	static int NEUTRAL_ARCHON = 3;
	static int DEN = 4;
	static int BIG_ZOMBIE = 5;
	static int CORNER = 6;
	
	/*
	 * Constants del robotType
	 */	
	static int NONE = 0;
	static int ARCHON = 1;
	static int SOLDIER = 2; 
	static int SCOUT = 3;
	static int GUARD = 4;
	static int VIPER = 5;
	static int TURRET = 6;
	static int TTM = 7;
	static int SOLDIER_VIPER = 8;
	static int TURRET_TTM = 9;
	static int SOLDIER_GUARD_VIPER = 10;
	static int SOLDIER_SCOUT_GUARD_VIPER = 11;
	static int ALL_EXCEPT_ARCHON = 12;
	static int ALL_EXCEPT_SCOUT = 13;
	static int ALL = 15;
	
	
	/*
	 * DISTRIBUCIO DE BITS:
	 * i1:
	 * b63-b61: unused
	 * b60-b57: mode
	 * b56-b53: object
	 * b52: type control
	 * b51-b48: robot type
	 * b47-b40: x
	 * b39-b32: y
	 * 
	 * i2:
	 * b16: senderArchon
	 * b15: id control
	 * b14-b0: dest id
	 */

	/*
	 * Object nomes tindra sentit si mode = 2 (si ha trobat un objecte)
	 * 
	 * Diferencia entre mode = 1 i mode = 3: mode= 1 es quan p. ex. l'archon no es pot moure i diu 
	 * als soldats que s'apartin del cami. mode = 3 es quan esta ple de zombies i els soldats s'han d'allunyar d'alla
	 */

	public Message(MapLocation sender2, int i1, int i2){
		sender = sender2;
		array = intsToLong(i1, i2);
		destID = selectBits(array, 0, 14);
		idControl = selectBits(array, 15, 15);
		senderArchon = selectBits(array, 16, 16);
		y = selectBits(array, 32, 39);
		x = selectBits(array, 40, 47);
		robotType = selectBits(array, 48, 51);
		typeControl = selectBits(array, 52, 52);
		object = selectBits(array, 53, 56);
		mode = selectBits(array, 57, 60);
	}
	
	public Message(MapLocation sender2, int mode2, int object2, int robotType2, int x2, int y2, int destID2, int typeControl2, int idControl2, int senderArchon2){
		sender = sender2;
		mode = mode2 % 16;
		object = object2 % 16;
		robotType = robotType2 % 16;
		x = sender2.x - x2 + 128;
		y = sender2.y - y2 + 128;
		destID = destID2 % 32768;
		typeControl = typeControl2 % 2;
		idControl = idControl2 % 2;
		senderArchon = senderArchon2 % 2;
		computeArray();
	}
		
	//agafa els bits de x entre start i end, tots dos inclosos. start indica el bit de menor pes, i end el de major
	//Per exemple, si x = 0b10110010, start = 2, end = 5, retornaria 0b1100 (10110010 -> 10 1100 10 -> 1100)
	private int selectBits(long x, int start, int end){
		if (start > end) return 0;
		if (start < 0) return 0;
		if (end > 63) return 0;
		long shifted = x >> start;
		long masked = shifted & (((long) 1 << (end-start+1)) - (long)1);
		return (int) masked;
	}
	
	private long setBits(long l, int start, int end, long val){
		for (int i = start; i <= end; i++){
			l = l & ~((long)1 << i); //posa a 0 el bit i
		}
		
		long v2 = val % (1 << end-start+1);
		l += v2 << start;		 
		return l;
	}
	
	private long intsToLong(int i1, int i2){
		long ret = i1;
		ret = ret << 32;
		ret += i2;
		return ret;
	}
	
	private int[] longToInts(long l){
		int[] ret = new int[2];
		ret[0] = selectBits(l, 32, 63);
		ret[1] = selectBits(l, 0, 31);
		return ret;
	}
	
	private void computeArray(){
		array = 0;
		array = setBits(array,0,14,destID);
		array = setBits(array,15,15,idControl);
		array = setBits(array,16,16,senderArchon);
		array = setBits(array,32,39,y);
		array = setBits(array,40,47,x);
		array = setBits(array,48,51,robotType);
		array = setBits(array,52,52,typeControl);
		array = setBits(array,53,56,object);
		array = setBits(array,57,60,mode);
	}
	
	public int[] encode(){
		return longToInts(array);
	}
	
	public int getMode() {return mode;}
	
	public int getObject() {return object;}
	
	public int getTypeControl() {return typeControl;}
	
	public int getRobotType() {return robotType;}
	
	public int getX() {return sender.x - x + 128;}
	
	public int getY() {return sender.y - y + 128;}
	
	public int getidControl() {return idControl;}
	
	public int getid() {return destID;}
	
	public int getSenderArchon() {return senderArchon;}
	
	public long getArray() {return array;}
	
	public Boolean toArchon() {return robotType == ARCHON || robotType == ALL_EXCEPT_SCOUT || robotType == ALL;}
	
	public Boolean toScout() {return robotType == SCOUT || robotType == SOLDIER_SCOUT_GUARD_VIPER || 
									 robotType == ALL_EXCEPT_ARCHON || robotType == ALL;}
	
	public Boolean toSoldier() {return robotType == SOLDIER || robotType == SOLDIER_VIPER || robotType == SOLDIER_GUARD_VIPER ||
							           robotType == SOLDIER_SCOUT_GUARD_VIPER || robotType == ALL_EXCEPT_ARCHON || robotType == ALL_EXCEPT_SCOUT ||
							           robotType == ALL;}
	
	public Boolean toGuard() {return robotType == GUARD || robotType == SOLDIER_GUARD_VIPER || robotType == SOLDIER_SCOUT_GUARD_VIPER ||
									 robotType == ALL_EXCEPT_ARCHON || robotType == ALL_EXCEPT_SCOUT || robotType == ALL;}
	
	public Boolean toViper() {return robotType == VIPER || robotType == SOLDIER_VIPER || robotType == SOLDIER_GUARD_VIPER || 
									 robotType == SOLDIER_SCOUT_GUARD_VIPER || robotType == ALL_EXCEPT_ARCHON || 
									 robotType == ALL_EXCEPT_SCOUT || robotType == ALL;}
	
	public Boolean toTurret() {return robotType == TURRET || robotType == TURRET_TTM || robotType == ALL_EXCEPT_ARCHON || 
			 						  robotType == ALL_EXCEPT_SCOUT || robotType == ALL;}
	
	public Boolean toTTM() {return robotType == TTM || robotType == TURRET_TTM || robotType == ALL_EXCEPT_ARCHON || 
			  					   robotType == ALL_EXCEPT_SCOUT || robotType == ALL;}
}
