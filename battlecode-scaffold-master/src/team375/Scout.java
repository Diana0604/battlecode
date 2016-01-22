package team375;

import battlecode.common.*;
import java.util.ArrayList;
import java.util.HashMap;


public class Scout extends RobotPlayer{

	static ArrayList<Integer> seenUnits = new ArrayList<>();
	static ArrayList<MapLocation> myArchonsLocation = new ArrayList<>();
	static ArrayList<Integer> myArchonsID = new ArrayList<>();
	static ArrayList<Integer> myArchonsLastSeen = new ArrayList<>();
	static ArrayList<MapLocation> pastLocations = new ArrayList<>();
	static Direction currentDir;
	static Direction nextCorner;
	static Boolean hasMoved;
	static int[][] danger;
	static HashMap<Direction, MapLocation> corners = new HashMap<>();
	static MapLocation leader;
	
	final static int BROADCAST_DISTANCE = 6400;	//no arriba a tot el mapa si es molt gros, pero sino puja molt el cooldown
	final static int MAX_TURNS = 30;
	final static int MAX_SEEN_UNITS = 5;
	final static int EARLY_GAME_END = 400;
	
	/*
	 * Coses a fer
	 * -millorar perill scouts amb zombies (fer que s'allunyin com mes millor fins a distancia 8)
	 */
	
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
	
	private static Direction randomDiagonalDirection(){
		Direction dir;
		if (!corners.containsKey(Direction.NORTH_EAST)){
			dir = Direction.NORTH_EAST;
		}else if (!corners.containsKey(Direction.NORTH_WEST)){
			dir = Direction.NORTH_WEST;
		}else if (!corners.containsKey(Direction.SOUTH_EAST)){
			dir = Direction.SOUTH_EAST;
		}else if (!corners.containsKey(Direction.SOUTH_WEST)){
			dir = Direction.SOUTH_WEST;
		}else return null;
		
		if (corners.size() <= 4){
			while (corners.containsKey(dir)){
				dir = directions[rand.nextInt(4)*2+1];
			}
		}else{
			//Falta posar que vagi cap al lider
			dir = directions[rand.nextInt(8)];
		}
		return dir;
	}

	
	private static void searchCorners() throws GameActionException{
		//System.out.println("Entra "+Clock.getBytecodeNum());
		//Ens posem com a objectiu trobar una cantonada, si no en teniem ja
		if (nextCorner == null || !nextCorner.isDiagonal() || corners.containsKey(nextCorner)){
			//System.out.println("Tria nova direccio");
			nextCorner = randomDiagonalDirection(); 
		}
		//System.out.println("Vaig amb direccio " + nextCorner+"  "+Clock.getBytecodeNum());
		//Comprova que no vegi la cantonada
		if (!rc.onTheMap(rc.getLocation().add(nextCorner.rotateLeft(), 7))){
			if (!rc.onTheMap(rc.getLocation().add(nextCorner.rotateRight(), 7))){
				int xmax = 7, ymax = 7;
				while (!rc.onTheMap(rc.getLocation().add(nextCorner.rotateLeft(), xmax--)));
				while (!rc.onTheMap(rc.getLocation().add(nextCorner.rotateRight(), ymax--)));
				corners.put(nextCorner, rc.getLocation().add(nextCorner.rotateLeft(), xmax+1).add(nextCorner.rotateRight(), ymax+1));
				System.out.println("He trobat cantonada a la posicio "+corners.get(nextCorner).x+","+corners.get(nextCorner).y);
				System.out.println("En direccio "+nextCorner);
				Message m = new Message(rc.getLocation(), Message.FOUND, Message.CORNER, Message.ALL, corners.get(nextCorner).x, corners.get(nextCorner).y, 0, 0, 0, 0);	
				int[] coded = m.encode();
				rc.broadcastMessageSignal(coded[0], coded[1], BROADCAST_DISTANCE);
			}
		}
		
		//System.out.println("Cantonades trobades: "+corners.size()+"  "+Clock.getBytecodeNum());
		
		Direction[] dirs = {nextCorner, nextCorner.rotateLeft(), nextCorner.rotateRight(), nextCorner.rotateLeft().rotateLeft(),
						    nextCorner.rotateRight().rotateRight(), nextCorner.rotateLeft().rotateLeft().rotateLeft(),
						    nextCorner.rotateRight().rotateRight().rotateRight(), nextCorner.opposite()};
		int i = 0;
		while (i < 8){
			if (rc.canMove(dirs[i]) && !pastLocations.contains(rc.getLocation().add(dirs[i])) && danger[dirs[i].dx+1][dirs[i].dy+1] == 0){
				currentDir = dirs[i];
				i = 9;
			}
			i++;
		}
		if (i == 8) currentDir = null;
	}
	
	private static void returnToLeader(){
		Direction dirToLeader = rc.getLocation().directionTo(leader);
		
		Direction[] dirs = {dirToLeader, dirToLeader.rotateLeft(), dirToLeader.rotateRight(), dirToLeader.rotateLeft().rotateLeft(),
							dirToLeader.rotateRight().rotateRight(), dirToLeader.rotateLeft().rotateLeft().rotateLeft(),
							dirToLeader.rotateRight().rotateRight().rotateRight(), dirToLeader.opposite()};
		
		int i = 0;
		while (i < 8){
			if (rc.canMove(dirs[i]) && !pastLocations.contains(rc.getLocation().add(dirs[i]))){
				currentDir = dirs[i];
				i = 9;
			}
			i++;
		}
		if (i == 8) currentDir = null;
	}
	
	private static Boolean canMove(){
		if (hasMoved) return false;
		if (!rc.isCoreReady()) return false;
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
	
	private static void addCorner(int x, int y) throws GameActionException{
		Direction d;
		int dx = x-rc.getLocation().x;
		int dy = y-rc.getLocation().y;
		if (dx == 0){
			if (rc.onTheMap(rc.getLocation().add(1,0))){
				dx = 1;
			}else dx = -1;
		}
		if (dy == 0){
			if (rc.onTheMap(rc.getLocation().add(0,1))){
				dy = 1;
			}else dy = -1;
		}
		if (dx < 0){
			if (dy < 0){
				d = Direction.SOUTH_WEST;
			}else d = Direction.NORTH_WEST;
		}else{
			if (dy < 0){
				d = Direction.SOUTH_EAST;
			}else d = Direction.NORTH_EAST;
		}
		corners.put(d, new MapLocation(x,y));
		System.out.println("He rebut la direccio " +d);
	}
	
	private static void readSignals() throws GameActionException{
		Signal[] signals = rc.emptySignalQueue();
		for (Signal s: signals){
    		if (s.getTeam() != myTeam) continue;
    		if (s.getMessage() == null) continue;
    		int[] coded = s.getMessage();
    		Message m = new Message(s.getLocation(), coded[0], coded[1]);
    		int mode = m.getMode();
			int object = m.getObject();
			int typeControl = m.getTypeControl();
			int x = m.getX();
			int y = m.getY();
			int idControl = m.getidControl();
			int id = m.getid();
			
			//Si el signal distingeix per tipus, i no esta dirigit als archons, l'ignora
			if (typeControl == 1){
				if (!m.toScout()) continue;
			}
			
			//Si el signal distingeix per ID del receptor i no esta dirigit a ell, l'ignora
			if (idControl == 1 && id != rc.getID()) continue;
    		
			if (m.getSenderArchon() == 1) continue;
			
			if (mode == Message.FOUND && object == Message.CORNER){
				addCorner(x,y);
			}
			
		}
	}
	
	private static void sendSignals() throws GameActionException{
		for (RobotInfo ri: nearbyNeutrals){
			if (ri.type == RobotType.ARCHON && !seenUnits.contains(ri.ID)){
				addUnitToSeenList(ri.ID);
				int mode = Message.FOUND;
				int object = Message.NEUTRAL_ARCHON;
				int robotType = Message.ARCHON;
				int x = ri.location.x;
				int y = ri.location.y;
				int destID = 0;
				int typeControl = 1;
				int idControl = 0;
				Message m = new Message(rc.getLocation(),mode, object,robotType,x,y, destID, typeControl, idControl,0);
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
				int x = ri.location.x;
				int y = ri.location.y;
				int destID = 0;
				int typeControl = 1;
				int idControl = 0;
				Message m = new Message(rc.getLocation(),mode, object,robotType,x,y, destID, typeControl, idControl,0);
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
				int x = ri.location.x;
				int y = ri.location.y;
				int destID = 0;
				int typeControl = 1;
				int idControl = 0;
				Message m = new Message(rc.getLocation(),mode, object,robotType,x,y, destID, typeControl, idControl,0);
				int[] coded = m.encode();
				rc.broadcastMessageSignal(coded[0], coded[1], BROADCAST_DISTANCE);
			}
		}
		
		
	}
	
	private static Boolean earlyGame(){
		return rc.getRoundNum() < EARLY_GAME_END;
	}
	
	public static void playScout() {
		try {
			leader = escollirLider();
        } catch (Exception e) {
            // Throwing an uncaught exception makes the robot die, so we need to catch exceptions.
            // Caught exceptions will result in a bytecode penalty.
            System.out.println(e.getMessage());
            e.printStackTrace();
        }

        while (true) {
	        try {
                if (earlyGame()){
		        	nearbyEnemies = rc.senseNearbyRobots(visionRange,enemyTeam);
	                nearbyZombies = rc.senseNearbyRobots(visionRange,Team.ZOMBIE);
	                nearbyNeutrals = rc.senseNearbyRobots(visionRange,Team.NEUTRAL);
	                nearbyFriends = rc.senseNearbyRobots(visionRange,myTeam);
	                readSignals();
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
	                    	//System.out.println("Es mou sense perill");
	                    	if (corners.size() < 4) {
	                    		searchCorners();
	                    	}else returnToLeader();
	                    	if (currentDir != null) {
	                    		//System.out.println("Em moc amb direccio "+currentDir);
	                    		if (canMove()) rc.move(currentDir); //Cal tornar a fer el if perque si troba una cantonada envia un missatge i li dona core delay
	                    	}
	                    	pastLocations.add(rc.getLocation());
	                    	if (pastLocations.size() > 20) pastLocations.remove(0);
	                    	hasMoved = true;
	                    }                    
	                }
	                sendSignals();
                }
                
                Clock.yield();
            } catch (Exception e) {
                System.out.println(e.getMessage());
                e.printStackTrace();
            }
        }
	}
}
