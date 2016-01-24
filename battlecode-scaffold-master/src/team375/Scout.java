package team375;

import battlecode.common.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;


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
	static int stage;
	
	final static int BROADCAST_DISTANCE = 12800;	//no arriba a tot el mapa si es molt gros, pero sino puja molt el cooldown
	final static int MAX_TURNS = 30;
	final static int MAX_SEEN_UNITS = 5;
	
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
    	/*for (int i = 0; i < 3; i++){
    		for (int j = 0; j < 3; j++){
    			System.out.print(danger[i][j]+" ");
    		}
    		System.out.println("");
    	}*/
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
	
	private static MapLocation baricentre(Team a){
		MapLocation[] locs = rc.getInitialArchonLocations(a);
		int x=0, y=0;
		for (MapLocation arch: locs){
			x += arch.x;
			y += arch.y;
		}
		return new MapLocation(x/locs.length,y/locs.length);
	}
	
	private static Direction randomDiagonalDirection(){
		Direction dir;
		if (nextCorner == null){
			dir = baricentre(enemyTeam).directionTo(baricentre(myTeam));
			//System.out.println("d1 = "+dir);
			if (!dir.isDiagonal()){
				//System.out.println("Entra al if");
				if (rand.nextInt(2) == 0) dir = dir.rotateLeft();
				else dir = dir.rotateRight();
			}
		}else dir = nextCorner.opposite();
		if (corners.size() <= 4){
			while (corners.containsKey(dir)){
				dir = dir.rotateLeft().rotateLeft();
			}
		}else{
			return null;
		}
		return dir;
	}

	
	private static void searchCorners() throws GameActionException{
		//System.out.println("Entra "+Clock.getBytecodeNum());
		if (corners.size() == 3) {
			calcLastCorner();
			return;
		}
		
		if (corners.size() == 2){
			if (corners.containsKey(Direction.NORTH_EAST) && corners.containsKey(Direction.SOUTH_WEST)){ 
				corners.put(Direction.NORTH_WEST, new MapLocation(corners.get(Direction.SOUTH_WEST).x, corners.get(Direction.NORTH_EAST).y));
				corners.put(Direction.SOUTH_EAST, new MapLocation(corners.get(Direction.NORTH_EAST).x, corners.get(Direction.SOUTH_WEST).y));
				return;
			}
			if (corners.containsKey(Direction.NORTH_WEST) && corners.containsKey(Direction.SOUTH_EAST)){
				corners.put(Direction.NORTH_EAST, new MapLocation(corners.get(Direction.SOUTH_EAST).x, corners.get(Direction.NORTH_WEST).y));
				corners.put(Direction.SOUTH_WEST, new MapLocation(corners.get(Direction.NORTH_WEST).x, corners.get(Direction.SOUTH_EAST).y));
				return;
			}
		}
		
		//Ens posem com a objectiu trobar una cantonada, si no en teniem ja
		//Per defecte sera la direccio oposada, no es random al final xd
		if (nextCorner == null || !nextCorner.isDiagonal() || corners.containsKey(nextCorner)){
			nextCorner = randomDiagonalDirection(); 
			//System.out.println("Tria nova direccio "+nextCorner);
		}
		//System.out.println("Vaig amb direccio " + nextCorner+"  "+Clock.getBytecodeNum());
		//Comprova que no vegi la cantonada
		if (!rc.onTheMap(rc.getLocation().add(nextCorner.rotateLeft(), 7))){
			if (!rc.onTheMap(rc.getLocation().add(nextCorner.rotateRight(), 7))){
				int xmax = 7, ymax = 7;
				while (!rc.onTheMap(rc.getLocation().add(nextCorner.rotateLeft(), xmax--)));
				while (!rc.onTheMap(rc.getLocation().add(nextCorner.rotateRight(), ymax--)));
				corners.put(nextCorner, rc.getLocation().add(nextCorner.rotateLeft(), xmax+1).add(nextCorner.rotateRight(), ymax+1));
				//System.out.println("He trobat cantonada a la posicio "+corners.get(nextCorner).x+","+corners.get(nextCorner).y);
				//System.out.println("En direccio "+nextCorner);
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
			if (rc.canMove(dirs[i]) && danger[dirs[i].dx+1][dirs[i].dy+1] == 0 && !pastLocations.contains(rc.getLocation().add(dirs[i]))){
				currentDir = dirs[i];
				i = 9;
			}
			i++;
		}
		if (i == 8) currentDir = null;
	}
	
	private static void calcLastCorner(){
		if (!corners.containsKey(Direction.NORTH_EAST)){
			corners.put(Direction.NORTH_EAST, new MapLocation(corners.get(Direction.SOUTH_EAST).x, corners.get(Direction.NORTH_WEST).y));
		}else if (!corners.containsKey(Direction.NORTH_WEST)){
			corners.put(Direction.NORTH_WEST, new MapLocation(corners.get(Direction.SOUTH_WEST).x, corners.get(Direction.NORTH_EAST).y));
		}else if (!corners.containsKey(Direction.SOUTH_EAST)){
			corners.put(Direction.SOUTH_EAST, new MapLocation(corners.get(Direction.NORTH_EAST).x, corners.get(Direction.SOUTH_WEST).y));
		}else if (!corners.containsKey(Direction.SOUTH_WEST)){
			corners.put(Direction.SOUTH_WEST, new MapLocation(corners.get(Direction.NORTH_WEST).x, corners.get(Direction.SOUTH_EAST).y));
			
		}
	}
	
	private static void returnToLeader(){
		Direction dirToLeader = rc.getLocation().directionTo(targetLocation);
		
		Direction[] dirs = {dirToLeader, dirToLeader.rotateLeft(), dirToLeader.rotateRight(), dirToLeader.rotateLeft().rotateLeft(),
							dirToLeader.rotateRight().rotateRight(), dirToLeader.rotateLeft().rotateLeft().rotateLeft(),
							dirToLeader.rotateRight().rotateRight().rotateRight(), dirToLeader.opposite()};
		
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
		//System.out.println("x,y = "+x+","+y+"  dx,dy = "+dx+","+dy);
		if (dx == 0){
			if (rc.onTheMap(rc.getLocation().add(1,0))){ //Si puc anar mes a l'est
				dx = -1;
			}else dx = 1;
		}
		if (dy == 0){
			if (rc.onTheMap(rc.getLocation().add(0,1))){ //Si puc anar mes al sud
				dy = -1;
			}else dy = 1;
		}
		if (dx < 0){
			if (dy < 0){
				d = Direction.NORTH_WEST;
			}else d = Direction.SOUTH_WEST;
		}else{
			if (dy < 0){
				d = Direction.NORTH_EAST;
			}else d = Direction.SOUTH_EAST;
		}
		//System.out.println("He rebut la direccio " +d);
		corners.put(d, new MapLocation(x,y));
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
    		
			if (m.getSenderArchon() == 1) {
				if (mode == Message.STAGE2){
					targetLocation = new MapLocation(x,y);
					stage = 2;
				}
			}else{			
				if (mode == Message.FOUND && object == Message.CORNER){
					addCorner(x,y);
				}
			}
			
		}
	}
	
	private static void sendSignalsStage1() throws GameActionException{
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

        //System.out.println("Despres de neutrals "+Clock.getBytecodeNum());
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

        //System.out.println("Despres de dens "+Clock.getBytecodeNum());
		if (rc.getRoundNum() % 5 == rc.getID() % 5){
			for (Direction d: corners.keySet()){
	
				Message m = new Message(rc.getLocation(), Message.FOUND, Message.CORNER, Message.ALL, corners.get(d).x, corners.get(d).y, 0, 0, 0, 0);	
				int[] coded = m.encode();
				rc.broadcastMessageSignal(coded[0], coded[1], 2*rc.getType().sensorRadiusSquared);
			}
		}
	}
	
	public static double getPriority(RobotInfo ri){
		if (ri.type == RobotType.ZOMBIEDEN) return 0.01;
		double atac, dist, hp;
		if (ri.type == RobotType.VIPER) atac = 14; // = 42/3 = atac/cd
		else if (ri.type.attackDelay == 0) atac = 0;
		else atac = ri.type.attackPower/ri.type.attackDelay;
		dist = ri.location.distanceSquaredTo(targetLocation);
		hp = ri.health;
		//System.out.println("Ha calculat una prioritat de "+atac+" "+hp+" "+dist);
		return atac*hp/dist;
	}
	
	public static void sendSignalsStage2() throws GameActionException{
		nearbyEnemies = rc.senseNearbyRobots(visionRange,enemyTeam);
        nearbyZombies = rc.senseNearbyRobots(visionRange,Team.ZOMBIE); 
        HashMap<RobotInfo, Double> priority = new HashMap<>();
        for (RobotInfo ri: nearbyEnemies) {
    		priority.put(ri, getPriority(ri));
    	}
        for (RobotInfo ri: nearbyZombies) {
    		priority.put(ri, getPriority(ri));
    		//System.out.println(ri.type+" te prioritat "+getPriority(ri));
    	}
        int sentSignals = 0;
        while (sentSignals < 20 && !priority.isEmpty()){
        	RobotInfo ri = null;
        	double x = -1;
        	//System.out.println("mida = "+priority.size()+", enemics zombies: "+nearbyEnemies.length+" "+nearbyZombies.length);
        	for (RobotInfo info: priority.keySet()){
        		//System.out.println("Entro al for "+info.type);
        		if (priority.get(info) > x){
        			//System.out.print(priority.get(info)+" ");
        			x = priority.get(info);
        			ri = info;
        		}
        		//System.out.println("abcd");
        	}
        	//if (ri == null) System.out.println("ri null");
        	Message m = new Message(rc.getLocation(), Message.SHOOT, 0, Message.ALL, ri.location.x, ri.location.y, 0,0,0,0);
        	int[] coded = m.encode();
        	rc.broadcastMessageSignal(coded[0], coded[1], 2*rc.getType().sensorRadiusSquared);
        	sentSignals++;
        	priority.remove(ri);
        }
	}
	
	public static void playScout() {
		try {
			targetLocation = escollirLider();
			stage = 1;
        } catch (Exception e) {
            // Throwing an uncaught exception makes the robot die, so we need to catch exceptions.
            // Caught exceptions will result in a bytecode penalty.
            System.out.println(e.getMessage());
            e.printStackTrace();
        }

        while (true) {
	        try {
                if (stage == 1){
		        	rc.setIndicatorString(0, "");
		        	nearbyEnemies = rc.senseNearbyRobots(visionRange,enemyTeam);
	                nearbyZombies = rc.senseNearbyRobots(visionRange,Team.ZOMBIE);
	                nearbyNeutrals = rc.senseNearbyRobots(visionRange,Team.NEUTRAL);
	                nearbyFriends = rc.senseNearbyRobots(visionRange,myTeam);
	                readSignals();
	                if (rc.isCoreReady()) {
	                	hasMoved = false;
	                	danger = new int[3][3];
	                	//calculateDanger();
	                	
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
	                    	}else {
	                    		returnToLeader();
	                    		rc.setIndicatorString(0, "Ja ha trobat les 4 cantonades");
	                    	}
	                    	/*System.out.println("Cantonades trobades: "+corners.size());
	                    	for (Direction d:corners.keySet()) System.out.print(d+": "+corners.get(d)+" ");
	                    	System.out.println("");*/
	                    	if (currentDir != null) {
	                    		//System.out.print("Em moc amb direccio ");
	                    		//System.out.println(currentDir);
	                    		if (canMove()) {
	                    			rc.move(currentDir); //Cal tornar a fer el if perque si troba una cantonada envia un missatge i li dona core delay
	                    			pastLocations.add(rc.getLocation());
	                    			if (pastLocations.size() > 5) pastLocations.remove(0);
	                    		}
	                    	}
	                    	hasMoved = true;
	                    }                    
	                }
	    			rc.setIndicatorString(0, "Es mou cap a la direccio "+nextCorner);
	    			String s1 = "", s2 = "", s3 = "", s4 = "";
	    			if (corners.containsKey(Direction.NORTH_EAST)) s1 = "NE";
	    			if (corners.containsKey(Direction.NORTH_WEST)) s2 = "NW";
	    			if (corners.containsKey(Direction.SOUTH_EAST)) s3 = "SE";
	    			if (corners.containsKey(Direction.SOUTH_WEST)) s4 = "SW";
	    			rc.setIndicatorString(1, s1+" "+s2+" "+s3+" "+s4);
	                sendSignalsStage1();
                }else if (stage == 2){
                	rc.setIndicatorString(0, "");
                	if (rc.isCoreReady()){
	                	if (rc.getLocation().distanceSquaredTo(targetLocation) > 18){
	                		returnToLeader();
	                		rc.move(currentDir);
	                	}
                	}
                	if (rc.getLocation().distanceSquaredTo(targetLocation) > 18){
                		sendSignalsStage2();
                	}
                }
                Clock.yield();
            } catch (Exception e) {
                System.out.println(e.getMessage());
                e.printStackTrace();
            }
        }
	}
}
