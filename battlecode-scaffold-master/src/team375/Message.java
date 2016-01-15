package team375;

import java.util.Random;

public class Message {
	private int mode;
	private int object;
	private int robotType;
	private int x;
	private int y;
	private int messageID;
	private int destID;
	private int typeControl;
	private int idControl;
	
	static int GO_TO = 0;
	static int GET_OUT = 1;
	static int FOUND = 2;
	static int ESCAPE = 3;
	static int POSITION = 4;
	
	static int MY_ARCHON = 1;
	static int ENEMY_ARCHON = 2;
	static int NEUTRAL_ARCHON = 3;
	static int DEN = 4;
	static int BIG_ZOMBIE = 5;
	
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
	static int ALL = 15;
	
	
	/*
	 * DISTRIBUCIO DE BITS:
	 * i1:
	 * b31-b27: unused
	 * b26-b23: mode
	 * b22-b19: object
	 * b18: type control
	 * b17-b14: robot type
	 * b13-b7: x
	 * b6-b0: y
	 * 
	 * i2:
	 * b30-b16: message id
	 * b15: id control
	 * b14-b0: dest id
	 */
	
	/*
	 * MESSAGE ID: random, identificador unic del missatge. servira quan els scouts facin de repetidors (?)
	 */

	/*
	 * Object nomes tindra sentit si mode = 2 (si ha trobat un objecte)
	 * 
	 * Diferencia entre mode = 1 i mode = 3: mode= 1 es quan p. ex. l'archon no es pot moure i diu 
	 * als soldats que s'apartin del cami. mode = 3 es quan esta ple de zombies i els soldats s'han d'allunyar d'alla
	 */
	
	
	public Message(int i1, int i2){
		y = i1 % 128;
		i1 = i1 /128;
		x = i1 % 128;
		i1 = i1 / 128;
		robotType = i1%16;
		i1 = i1 / 16;
		typeControl = i1%2;
		i1 = i1 / 2;
		object = i1 % 16;
		i1 = i1/16;
		mode = i1 % 16;
		
		destID = i2 % 32768;
		i2 = i2/32768;
		idControl = i2 % 2;
		i2 = i2/2;
		messageID = i2 % 32768;
	}
	
	public Message(int mode2, int object2, int robotType2, int x2, int y2, int destID2, int typeControl2, int idControl2){
		mode = mode2 % 16;
		object = object2 % 16;
		robotType = robotType2 % 16;
		x = x2 % 128;
		y = y2 % 128;
		destID = destID2 % 32768;
		typeControl = typeControl2 % 2;
		idControl = idControl2 % 2;
		Random rand = new Random(mode+object+robotType+x+y+destID+typeControl+idControl);
		messageID = rand.nextInt(32768);
	}
	
	public Message(int mode2, int object2, int robotType2, int x2, int y2, int messageID2, int destID2, int typeControl2, int idControl2){
		mode = mode2 % 16;
		object = object2 % 16;
		robotType = robotType2 % 16;
		x = x2 % 128;
		y = y2 % 128;
		messageID = messageID2 % 32768;
		destID = destID2 % 32768;
		typeControl = typeControl2 % 2;
		idControl = idControl2 % 2;
	}
	
	public int[] encode(){
		int i1, i2;
		i1 = i2 = 0;
		i1+=mode;
		i1*=16;
		i1+=object;
		i1*=2;
		i1+=typeControl;
		i1*=16;
		i1+=robotType;
		i1*=128;
		i1+=x;
		i1*=128;
		i1+=y;
		
		i2+=messageID;
		i2*=2;
		i2+=idControl;
		i2*=32768;
		i2+=destID;
		
		
		int[] ret = {i1, i2};
		return ret;
	}
	
	public int getMode() {return mode;}
	
	public int getObject() {return object;}
	
	public int getTypeControl() {return typeControl;}
	
	public int getRobotType() {return robotType;}
	
	public int getX() {return x;}
	
	public int getY() {return y;}
	
	public int getMessageID() {return messageID;}
	
	public int getidControl() {return idControl;}
	
	public int getid() {return destID;}
}
