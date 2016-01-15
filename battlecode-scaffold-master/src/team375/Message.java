package team375;

public class Message {
	int mode;
	int robotType;
	int x;
	int y;
	int id;
	int typeControl;
	int idControl;
	
	/*
	 * i1:
	 * b31-b23: unused
	 * b22-b19: mode
	 * b18: type control
	 * b17-b14: robot type
	 * b13-b7: x
	 * b6-b0: y
	 * 
	 * i2:
	 * b15: id control
	 * b14-b0: id2
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
		mode = i1 % 16;
		
		id = i2 % 32768;
		i2 = i2/32768;
		idControl = i2 % 2;
	}
	
	public Message(int mode2, int robotType2, int x2, int y2, int id2, int typeControl2, int idControl2){
		mode = mode2 % 16;
		robotType = robotType2 % 16;
		x = x2 % 128;
		y = y2 % 128;
		id = id2 % 32768;
		typeControl = typeControl2 % 2;
		idControl = idControl2 % 4;
	}
	
	public int[] encode(){
		int i1, i2;
		i1 = i2 = 0;
		i1+=mode;
		i1*=2;
		i1+=typeControl;
		i1*=16;
		i1+=robotType;
		i1*=128;
		i1+=x;
		i1*=128;
		i1+=y;
		
		i2+=idControl;
		i2*=32768;
		i2+=id;
		
		
		int[] ret = {i1, i2};
		return ret;
	}
	
	public int getMode() {return mode;}
	
	public int getTypeControl() {return typeControl;}
	
	public int getRobotType() {return robotType;}
	
	public int getX() {return x;}
	
	public int getY() {return y;}
	
	public int getidControl() {return idControl;}
	
	public int getid() {return id;}
}
