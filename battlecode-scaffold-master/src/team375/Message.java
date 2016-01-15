package team375;

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
	 * MODE: 
	 * 0: go to (x,y)
	 * 1: get out of (x,y)
	 * 2: found object in (x,y)
	 * 3: run away from (x,y)
	 */
	
	/*
	 * OBJECT:
	 * 0: friendly archon
	 * 1: enemy archon
	 * 2: neutral archon
	 * 3: zombie den
	 * 4: big zombie
	 */
	
	/*
	 * ROBOTTYPE:
	 * 0: none
	 * 1: archon
	 * 2: soldier
	 * 3: scout
	 * 4: guard
	 * 5: viper
	 * 6: turret
	 * 7: TTM
	 * 8: soldier & viper
	 * 9: turret & TTM
	 * 10: soldier & guard & viper
	 * 11: soldier & scout & guard & viper
	 * 15: all robots
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
