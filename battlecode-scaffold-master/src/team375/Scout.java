package team375;

import battlecode.common.*;
import java.util.ArrayList;


public class Scout  extends RobotPlayer{

	static ArrayList<Integer> seenUnits = new ArrayList<>();
	static int lastUnitSeen;
	static Direction currentDir;
	static int turnsLeft;
	static Boolean hasMoved;
	static int[][] danger;
	
	final static int BROADCAST_DISTANCE = 6400;	//no arriba a tot el mapa si es molt gros, pero sino puja molt el cooldown
	final static int MAX_TURNS = 30;
	final static int MAX_SEEN_UNITS = 5;
	
	private static void calculateDanger(){
		for (RobotInfo info: nearbyEnemies){
    		RobotInfo[] friendlyTargets = rc.senseNearbyRobots(info.location, info.type.attackRadiusSquared, myTeam);
    		for (int i = -1; i < 2; i++){
    			for (int j = -1; j<2; j++){
    				MapLocation loc = rc.getLocation().add(i, j);
    				if (info.type.attackRadiusSquared < info.location.distanceSquaredTo(loc)) continue;
    				if (friendlyTargets.length <= 1) danger[i+1][j+1] += info.attackPower/info.type.attackDelay;
    			}
    		}
    	}
    	for (RobotInfo info: nearbyZombies){
    		for (int i = -1; i < 2; i++){
    			for (int j = -1; j<2; j++){
    				MapLocation loc = rc.getLocation().add(i, j);
    				if (info.type.attackRadiusSquared < info.location.distanceSquaredTo(loc)&& info.location.distanceSquaredTo(loc) > 8) continue;
    				danger[i+1][j+1] += info.attackPower/info.type.attackDelay;
    			}
    		}
    	}
	}
	
	public static Direction safestDirection(){
    	Direction dir = Direction.NORTH;
    	int safeDir = -1;
    	int lowestDanger = 100;
    	for (int i = 0; i < 8; i++){
    		if (!rc.canMove(dir)) {
    			dir = dir.rotateLeft();
    			continue;
    		}
    		int aux = danger[dir.dx+1][dir.dy+1];
    		if (aux < lowestDanger){
    			lowestDanger = aux;
    			safeDir = i;
    		}
    		dir = dir.rotateLeft();
    	}
    	if (safeDir < 0) return Direction.NONE;
    	Direction bestDir = Direction.NORTH;
    	for (int i = 0; i < safeDir; i++) bestDir = bestDir.rotateLeft();
    	//System.out.println("La millor direccio es "+bestDir);
    	return bestDir;
    }
	
	private static void selectDirection() throws GameActionException{
		int mindanger = 999;
		for (int i = 0; i < 3; i++){
			for (int j = 0; j < 3; j++){
				if (danger[i][j] < mindanger) mindanger = danger[i][j];
			}
		}
		
		if (currentDir == null){
			currentDir = directions[rand.nextInt(8)];
			turnsLeft = MAX_TURNS;
		}
		
		if (!rc.canMove(currentDir) && rc.onTheMap(rc.getLocation().add(currentDir))) {
			currentDir = currentDir.rotateLeft();
			if (!rc.canMove(currentDir) && rc.onTheMap(rc.getLocation().add(currentDir))) currentDir = currentDir.rotateRight().rotateRight();
		}
		
		
		while (!rc.canMove(currentDir) || turnsLeft <= 0 || danger[currentDir.dx+1][currentDir.dy+1] > mindanger){
			currentDir = directions[rand.nextInt(8)];
			turnsLeft = MAX_TURNS;
		}
	}
	
	private static Boolean canMove(){
		if (hasMoved) return false;
		for (Direction dir: directions){
			if (rc.canMove(dir)) return true;
		}
		return false;
	}
	
	private static void addUnitToSeenList(int id){
		seenUnits.add(id);
		if (seenUnits.size() > MAX_SEEN_UNITS){
			seenUnits.remove(0);
		}
	}
	
	private static void sendSignals() throws GameActionException{
		for (RobotInfo ri: nearbyNeutrals){
			if (ri.type == RobotType.ARCHON && !seenUnits.contains(ri.ID)){
				addUnitToSeenList(ri.ID);
				int mode = Message.FOUND;
				int object = Message.NEUTRAL_ARCHON;
				int robotType = Message.ARCHON;
				int x = rc.getLocation().x - ri.location.x + 128;
				int y = rc.getLocation().y - ri.location.y + 128;
				int destID = 0;
				int typeControl = 1;
				int idControl = 0;
				Message m = new Message(rc.getLocation(),mode, object,robotType,x,y, destID, typeControl, idControl);
				int[] coded = m.encode();
				rc.broadcastMessageSignal(coded[0], coded[1], BROADCAST_DISTANCE);
			}
		}
		for (RobotInfo ri: nearbyZombies){
			if (ri.type == RobotType.ZOMBIEDEN && !seenUnits.contains(ri.ID)){
				addUnitToSeenList(ri.ID);
				int mode = Message.FOUND;
				int object = Message.DEN;
				int robotType = Message.ARCHON;
				int x = rc.getLocation().x - ri.location.x + 128;
				int y = rc.getLocation().y - ri.location.y + 128;
				int destID = 0;
				int typeControl = 1;
				int idControl = 0;
				Message m = new Message(rc.getLocation(),mode, object,robotType,x,y, destID, typeControl, idControl);
				int[] coded = m.encode();
				rc.broadcastMessageSignal(coded[0], coded[1], BROADCAST_DISTANCE);
			}
		}
		for (RobotInfo ri: nearbyEnemies){
			if (ri.type == RobotType.ARCHON && !seenUnits.contains(ri.ID)){
				addUnitToSeenList(ri.ID);
				int mode = Message.FOUND;
				int object = Message.ENEMY_ARCHON;
				int robotType = Message.ARCHON;
				int x = rc.getLocation().x - ri.location.x + 128;
				int y = rc.getLocation().y - ri.location.y + 128;
				int destID = 0;
				int typeControl = 1;
				int idControl = 0;
				Message m = new Message(rc.getLocation(),mode, object,robotType,x,y, destID, typeControl, idControl);
				int[] coded = m.encode();
				rc.broadcastMessageSignal(coded[0], coded[1], BROADCAST_DISTANCE);
			}
		}
		
	}
	
	public static void playScout() {
		try {
            // Any code here gets executed exactly once at the beginning of the game.
        } catch (Exception e) {
            // Throwing an uncaught exception makes the robot die, so we need to catch exceptions.
            // Caught exceptions will result in a bytecode penalty.
            System.out.println(e.getMessage());
            e.printStackTrace();
        }

        while (true) {
	        try {
                nearbyEnemies = rc.senseNearbyRobots(visionRange,enemyTeam);
                nearbyZombies = rc.senseNearbyRobots(visionRange,Team.ZOMBIE);
                nearbyNeutrals = rc.senseNearbyRobots(visionRange,Team.NEUTRAL);
                if (rc.isCoreReady()) {
                	hasMoved = false;
                	danger = new int[3][3];
                	calculateDanger();
                	
                	if (danger[1][1] > 0 ){
                		Direction dir = safestDirection();
                    	if (dir != Direction.NONE){
                    		hasMoved = true;
                    		rc.move(dir);
                        	rc.setIndicatorString(0,"Hi havia perill i ha fugit");
                    	}
                	}
                	
                    if (canMove()) {
                    	selectDirection();
                    	rc.move(currentDir);
                    	hasMoved = true;
                    	turnsLeft--;
                    }                    
                }
                
                sendSignals();
                
                Clock.yield();
            } catch (Exception e) {
                System.out.println(e.getMessage());
                e.printStackTrace();
            }
        }
	}
}
