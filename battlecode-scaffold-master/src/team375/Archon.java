package team375;

import java.util.ArrayList;

import battlecode.common.*;

public class Archon extends RobotPlayer{
	static ArrayList<MapLocation> dens = new ArrayList<>();
	static ArrayList<MapLocation> neutralArchons = new ArrayList<>();
    static int[][] danger;
    static final int DANGER_THRESHHOLD = 1;
    static Boolean leader;
    static RobotType nextRobotType = RobotType.SCOUT;
    
    private static int directionDanger(Direction dir){
    	return danger[dir.dx+1][dir.dy+1];
    }
    
    public static void calculateDanger(){
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
    				if (info.type.attackRadiusSquared < info.location.distanceSquaredTo(loc) && info.location.distanceSquaredTo(loc) > 8) continue;
    				danger[i+1][j+1] += info.attackPower/info.type.attackDelay;
    			}
    		}
    	}
    }
    
    //De les 8 direccions retorna la que tingui menys perill
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
    
    public static void chooseNextRobotType(){
    	int n = rand.nextInt(12); // 1 de cada 12 robots seran scouts
    	if (n == 0) nextRobotType = RobotType.SCOUT;
    	else nextRobotType = RobotType.SOLDIER;
    }
    
    private static void readSignals(){
    	leader = true; //per defecte
    	Signal[] signals = rc.emptySignalQueue();
    	for (Signal s: signals){
    		if (s.getTeam() != myTeam) continue;
    		if (s.getMessage() == null) continue;
    		int[] coded = s.getMessage();
    		Message m = new Message(s.getLocation(), coded[0], coded[1]);
    		int mode = m.getMode();
			int object = m.getObject();
			int typeControl = m.getTypeControl();
			int robotType = m.getRobotType();
			int x = m.getX();
			int y = m.getY();
			System.out.println(x+"  "+y+"  my pos = "+rc.getLocation().x+","+rc.getLocation().y);
			int idControl = m.getidControl();
			int id = m.getid();
			
			if (typeControl == 1){
				if (!m.toArchon()) continue;
			}
			if (idControl == 1 && id != rc.getID()) continue;
    		
			
    		if (m.getSenderArchon() == 1){
    			leader = false;
    			//L'ha enviat un archon
    			if (mode == Message.GO_TO){
    				targetLocation = new MapLocation(x,y);
    			}
    		}else{
    			//L'ha enviat un scout
    			//System.out.println("rep missatge de scout");
    			//System.out.println(mode+" "+object+" "+typeControl+" "+x+" "+y);
    			if (mode == Message.FOUND){
    				if (object == Message.DEN){
    					if (!dens.contains(object)){
    						dens.add(new MapLocation(x,y));
    					}
    				}
    				if (object == Message.NEUTRAL_ARCHON){
    					if (!neutralArchons.contains(object)){
    						neutralArchons.add(new MapLocation(x,y));
    					}
    				}
    			}
    		}
    	}
    }
                          
    private static void sendSignals() throws GameActionException{
    	if (targetLocation != null){
    		int mode = Message.GO_TO;
			int object = Message.NONE;
			int typeControl = Message.NONE;
			int robotType = Message.NONE;
			int x = rc.getLocation().x - targetLocation.x + 128;
			int y = rc.getLocation().y - targetLocation.y + 128;
			int idControl = Message.NONE;
			int id = Message.NONE;
			Message m = new Message(rc.getLocation(), mode, object, robotType, x, y, id, typeControl, idControl, 1);
			int[] coded = m.encode();
			rc.broadcastMessageSignal(coded[0], coded[1], rc.getType().sensorRadiusSquared);
    	}
    }
    
    private static void updateTargetLocation() throws GameActionException{
    	//ESBORRAR LOCS
    	if (dens == null) System.out.println("dens null");
    	else for (int i = 0; i < dens.size(); i++){
    		MapLocation m = dens.get(i);
    		//System.out.print(m.x+","+m.y+"  ");
    		if (rc.canSense(m)){
    			if (rc.senseRobotAtLocation(m) == null) dens.remove(m);
    			else if (rc.senseRobotAtLocation(m).type != RobotType.ZOMBIEDEN) dens.remove(m);
    		}
    	}
    	//System.out.println("aaa");
    	
    	if (neutralArchons == null) System.out.println("neutrals null");
    	else for (int i = 0; i < neutralArchons.size(); i++){
    		MapLocation m = neutralArchons.get(i);
    		//System.out.print(m.x+","+m.y+"  ");
    		if (rc.canSense(m)){
    			if (rc.senseRobotAtLocation(m) == null) neutralArchons.remove(m);
    			else if (rc.senseRobotAtLocation(m).type != RobotType.ARCHON) neutralArchons.remove(m);
    		}
    	}
    	//System.out.println("bbb");
    	
    	
    	
    	MapLocation closestDen = null;
    	int dist = 1000000;
    	for (int i = 0; i < dens.size(); i++){
    		MapLocation m = dens.get(i);
    		if (rc.getLocation().distanceSquaredTo(m) < dist){
    			dist = rc.getLocation().distanceSquaredTo(m);
    			closestDen = m;
    		}
    	}
    	
    	MapLocation closestNeutralArchon = null;
    	dist = 1000000;
    	for (int i = 0; i < neutralArchons.size(); i++){
    		MapLocation m = neutralArchons.get(i);
    		if (rc.getLocation().distanceSquaredTo(m) < dist){
    			dist = rc.getLocation().distanceSquaredTo(m);
    			closestNeutralArchon = m;
    		}
    	}
    	if (closestDen == null){//si no coneix cap den
    		if (closestNeutralArchon == null) { //si no coneix cap archon neutral
    			targetLocation = null;
    		}else targetLocation = closestNeutralArchon;
    	}else{
    		if (closestNeutralArchon == null) { //si no coneix cap archon neutral
    			targetLocation = closestDen;
    		}else{
    			if (rc.getLocation().distanceSquaredTo(closestDen)*4 < rc.getLocation().distanceSquaredTo(closestNeutralArchon)){
    				targetLocation = closestDen;
    			}else targetLocation = closestNeutralArchon;
    		}
    	}
    	//if (targetLocation != null) System.out.println(targetLocation.x+" "+targetLocation.y);
    }
    
	public static void playArchon(){
		try {
			Signal[] signals = rc.emptySignalQueue();
			Signal found = null;
			for (Signal i: signals){
				if (i.getTeam() == myTeam && i.getMessage() != null){
					found = i;
					break;
				}
			}
			
			if (found == null){
				MapLocation[] initArchons = rc.getInitialArchonLocations(myTeam);
				int maxdist = 0;
				for (MapLocation i: initArchons){
					if (rc.getLocation().distanceSquaredTo(i) > maxdist){
						maxdist = rc.getLocation().distanceSquaredTo(i);
					}
				}
				if (maxdist > 0){
					int mode = Message.GO_TO;
					int object = Message.NONE;
					int robotType = Message.ARCHON;
					int destID = 0;
					int typeControl = 1;
					int idControl = 0;
					Message m = new Message(rc.getLocation(), mode, object,robotType,128, 128, destID, typeControl, idControl, 1);
					int[] coded = m.encode();
					rc.broadcastMessageSignal(coded[0], coded[1], maxdist);
				}
			}else{
				int[] coded = found.getMessage();
				//Message m= new Message(found.getLocation(),coded[0], coded[1]);
				targetLocation = new MapLocation(found.getLocation().x, found.getLocation().y);
			}
        } catch (Exception e) {
            // Throwing an uncaught exception makes the robot die, so we need to catch exceptions.
            // Caught exceptions will result in a bytecode penalty.
            System.out.println(e.getMessage());
            e.printStackTrace();
        }

        while (true) {
            // This is a loop to prevent the run() method from returning. Because of the Clock.yield()
            // at the end of it, the loop will iterate once per game round.
            try {
                //nearbyFriends = rc.senseNearbyRobots(visionRange,myTeam);
                nearbyEnemies = rc.senseNearbyRobots(visionRange,enemyTeam);
                nearbyZombies = rc.senseNearbyRobots(visionRange,Team.ZOMBIE);
                nearbyNeutrals = rc.senseNearbyRobots(visionRange,Team.NEUTRAL);
                danger = new int[3][3];
                calculateDanger();
                readSignals();
                updateTargetLocation();
                if (leader)	sendSignals();
                
                
                if (rc.isCoreReady()) {
                	/*
                	 * Ordre d'importancia:
                	 * 1. Si hi ha un robot neutral, l'activa
                	 * 2. Si pot fabricar un robot, el fabrica
                	 * 3. Si esta en perill, fuig
                	 * 4. Recull parts adjacents
                	 * 5. Va a la target location (si no hi es ja)
                	 * 5. Neteja rubble adjacent
                	 * Sempre: intenta reparar soldats propers	
                	 */
                	Boolean hasMoved = false;
                	RobotInfo[] adjacentNeutrals = rc.senseNearbyRobots(2, Team.NEUTRAL);
                    //if (targetLocation != null) System.out.println("TargetLocation = "+targetLocation.x+" "+targetLocation.y);
                	
                	if (adjacentNeutrals.length != 0){
                    	rc.activate(adjacentNeutrals[0].location);
                    	rc.setIndicatorString(0,"Ha activat un robot neutral");
                    	hasMoved = true;
                    }
                    
                    if (!hasMoved && rc.hasBuildRequirements(nextRobotType)) {
                    	//De moment nomes fabrica soldiers
                        Direction dirToBuild = directions[rand.nextInt(8)];
                        for (int i = 0; i < 8; i++) {
                            // If possible, build in this direction
                            if (rc.canBuild(dirToBuild, nextRobotType)) {
                                rc.build(dirToBuild, nextRobotType);
                                chooseNextRobotType();
                            	rc.setIndicatorString(0,"Ha construit un soldat");
                                hasMoved = true;
                                break;
                            } else {
                                // Rotate the direction to try
                                dirToBuild = dirToBuild.rotateLeft();
                            }
                        }
                    } 
                    
                    if (!hasMoved && danger[1][1] >= DANGER_THRESHHOLD){
                    	Direction dir = safestDirection();
                    	if (dir != Direction.NONE && danger[dir.dx+1][dir.dy+1] < danger[1][1]){
                    		hasMoved = true;
                    		rc.move(dir);
                        	rc.setIndicatorString(0,"Hi havia perill i ha fugit");
                    	}
                    }
                    
                    if (!hasMoved){
                    	Direction dir = Direction.NORTH;
                    	for (int i = 0; i < 8; i++){
                    		if (rc.senseParts(rc.getLocation().add(dir)) > 0 && rc.canMove(dir)){
                    			rc.move(dir);
                    			hasMoved = true;
                            	rc.setIndicatorString(0,"Ha anat a agafar unes parts");
                    			break;
                    		}
                    		dir = dir.rotateLeft();
                    	}
                    }
                    //if (targetLocation == null) System.out.println("target null");
                    if (!hasMoved){
                    	if (targetLocation != null && targetLocation != rc.getLocation()){
                    		
                    		
                    		Direction dir = rc.getLocation().directionTo(targetLocation);
                    		if (directionDanger(dir) > 0 || !rc.canMove(dir)) dir = dir.rotateLeft();
                    		if (directionDanger(dir) > 0 || !rc.canMove(dir)) dir = dir.rotateRight().rotateRight();
                    		if (rc.canMove(dir) && directionDanger(dir) == 0) {
                    			rc.move(dir);
                    			rc.setIndicatorString(0, "Ha anat a la target location");
                    			hasMoved = true;
                    		}
                    	}
                    }
                    
                    if (!hasMoved){
                    	Direction dir = Direction.NORTH;
                    	for (int i = 0; i < 8; i++){
                    		if (rc.senseRubble(rc.getLocation().add(dir)) > GameConstants.RUBBLE_SLOW_THRESH){
                    			rc.clearRubble(dir);
                    			hasMoved = true;
                            	rc.setIndicatorString(0,"Ha netejat rubble");
                    			break;
                    		}
                    		dir = dir.rotateLeft();
                    	}
                    }
                    
                    
                    if (!hasMoved)
                    	rc.setIndicatorString(0,"No ha fet res aquest torn");
                    
                }else{
                	rc.setIndicatorString(0,"Tenia core delay");
                }

                
                RobotInfo[] healableRobots = rc.senseNearbyRobots(attackRange, myTeam);
                Boolean hasHealed = false;
                int i = 0;
                while (healableRobots.length > i && !hasHealed){
                	if (healableRobots[i].maxHealth == RobotType.ARCHON.maxHealth){
                		i++;
                		continue;
                	}
                	if (healableRobots[i].health != healableRobots[i].maxHealth){
                		rc.repair(healableRobots[i].location);
                		hasHealed = true;
                	}
                	i++;
                }
                
                Clock.yield();
            } catch (Exception e) {
                System.out.println(e.getMessage());
                e.printStackTrace();
            }
        }
	}
}
