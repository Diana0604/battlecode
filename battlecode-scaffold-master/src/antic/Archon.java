package antic;

import java.util.ArrayList;

import battlecode.common.*;

public class Archon extends RobotPlayer{
	
	//Arraylist amb les posicions dels dens que coneix
	static ArrayList<MapLocation> dens = new ArrayList<>();
	
	//Arraylist amb les posicions dels archons neutrals que coneix
	static ArrayList<MapLocation> neutralArchons = new ArrayList<>();
	
	//Array 3x3 amb els perills adjacents
    static int[][] danger;
    
    //Aixo es per dir que si perill > danger_threshhold llavors fuig
    static final int DANGER_THRESHHOLD = 1;
    
    //si aquest archon es el lider
    static Boolean leader;
    
    //tipus del proxim robot que construira
    static RobotType nextRobotType = RobotType.SCOUT;
    
    //Retorna el perill que hi ha en una direccio
    private static int directionDanger(Direction dir){
    	return danger[dir.dx+1][dir.dy+1];
    }
    
    //Calcula el perill i el posa en la array de 3x3
    //Si vols aixo ho pots canviar i fer-ho igual que amb els soldats
    //L'unic diferent es que els enemics, si poden atacar a un altre robot que no sigui aquest, segurament l'atacaran, per tant no li poso perill
    public static void calculateDanger(){
    	for (RobotInfo info: nearbyEnemies){
    		RobotInfo[] friendlyTargets = rc.senseNearbyRobots(info.location, info.type.attackRadiusSquared, myTeam);
    		for (int i = -1; i < 2; i++){
    			for (int j = -1; j<2; j++){
    				MapLocation loc = rc.getLocation().add(i, j);
    				if (info.type.attackRadiusSquared < info.location.distanceSquaredTo(loc)) continue;
    				//Si nomes poden atacar a aquest archon, poso perill
    				if (friendlyTargets.length <= 1) danger[i+1][j+1] += info.attackPower/info.type.attackDelay;
    			}
    		}
    	}
    	for (RobotInfo info: nearbyZombies){
    		for (int i = -1; i < 2; i++){
    			for (int j = -1; j<2; j++){
    				MapLocation loc = rc.getLocation().add(i, j);
    				if (info.type.attackRadiusSquared < info.location.distanceSquaredTo(loc) && info.location.distanceSquaredTo(loc) > 8) continue;
    				//Si estas en rang de que t'ataquin, poses perill
    				//Si no estas en rang pero estas a distancia^2 < 8, poses la meitat de perill
    				if (info.type.attackRadiusSquared < info.location.distanceSquaredTo(loc)) danger[i+1][j+1] += info.attackPower/info.type.attackDelay;
    				else danger[i+1][j+1]+= info.attackPower/info.type.attackDelay/2;
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
    
    //Tria un robot per construir
    public static void chooseNextRobotType(){
    	int n = rand.nextInt(12); // 1 de cada 12 robots seran scouts
    	if (n == 0) nextRobotType = RobotType.SCOUT;
    	else nextRobotType = RobotType.SOLDIER;
    }
    
    //Llegeix les signals que li han arribat
    private static void readSignals(){
    	leader = true; //per defecte
    	Signal[] signals = rc.emptySignalQueue();
    	for (Signal s: signals){
    		//Si el signal es de l'altre equip o es cutre, l'ignora
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
			int idControl = m.getidControl();
			int id = m.getid();
			
			//Si el signal distingeix per tipus, i no esta dirigit als archons, l'ignora
			if (typeControl == 1){
				if (!m.toArchon()) continue;
			}
			
			//Si el signal distingeix per ID del receptor i no esta dirigit a ell, l'ignora
			if (idControl == 1 && id != rc.getID()) continue;
    		
			
    		if (m.getSenderArchon() == 1){
    			//Ha rebut signal d'un archon, per tant no es el lier
    			leader = false;
    			if (mode == Message.GO_TO){
    				//si l'archon li ordena d'anar a un lloc, el posa com a objectiu
    				targetLocation = new MapLocation(x,y);
    			}
    		}else{
    			//L'ha enviat un scout
    			//System.out.println("rep missatge de scout");
    			if (mode == Message.FOUND){
    				if (object == Message.DEN){
    					//Si un scout li diu que ha trobat un den, l'afegeix a la llista
    					if (!dens.contains(object)){
    						dens.add(new MapLocation(x,y));
    					}
    				}
    				if (object == Message.NEUTRAL_ARCHON){
    					//Si un scout li diu que ha trobat un archon neutral, l'afegeix a la llista
    					if (!neutralArchons.contains(object)){
    						neutralArchons.add(new MapLocation(x,y));
    					}
    				}
    			}
    		}
    	}
    }
    
    //Si envia signals es perque es lider. Diu a tots els robots propers d'anar a la target location que te
    private static void sendSignals() throws GameActionException{
    	if (targetLocation != null){
    		int mode = Message.GO_TO;
			int object = Message.NONE;
			int typeControl = Message.NONE;
			int robotType = Message.NONE;
			int x = targetLocation.x;
			int y = targetLocation.y;
			int idControl = Message.NONE;
			int id = Message.NONE;
			Message m = new Message(rc.getLocation(), mode, object, robotType, x, y, id, typeControl, idControl, 1);
			int[] coded = m.encode();
			rc.broadcastMessageSignal(coded[0], coded[1], 2*rc.getType().sensorRadiusSquared);
    	}
    }
    
    //Actualitza la target location
    private static void updateTargetLocation() throws GameActionException{
    	//Si veu que a on hi havia un den ara no hi ha res, esborra el den de la llista
    	for (int i = 0; i < dens.size(); i++){
    		MapLocation m = dens.get(i);
    		//System.out.print(m.x+","+m.y+"  ");
    		if (rc.canSense(m)){
    			if (rc.senseRobotAtLocation(m) == null) dens.remove(m);
    			else if (rc.senseRobotAtLocation(m).type != RobotType.ZOMBIEDEN) dens.remove(m);
    		}
    	}
    	
    	//Si veu que a on hi havia un archon neutral ara no hi ha res, esborra l'archon neutral de la llista
    	for (int i = 0; i < neutralArchons.size(); i++){
    		MapLocation m = neutralArchons.get(i);
    		//System.out.print(m.x+","+m.y+"  ");
    		if (rc.canSense(m)){
    			if (rc.senseRobotAtLocation(m) == null) neutralArchons.remove(m);
    			else if (rc.senseRobotAtLocation(m).type != RobotType.ARCHON) neutralArchons.remove(m);
    		}
    	}
    	
    	
    	//Posa a closestDen el den que te mes proper
    	MapLocation closestDen = null;
    	int dist = 1000000;
    	for (int i = 0; i < dens.size(); i++){
    		MapLocation m = dens.get(i);
    		if (rc.getLocation().distanceSquaredTo(m) < dist){
    			dist = rc.getLocation().distanceSquaredTo(m);
    			closestDen = m;
    		}
    	}
    	
    	//Posa a closestNeutralArchon l'archon neutral que te mes proper
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
    			//Si te un den i un archon neutral, anira a pel den nomes si la distancia^2 es 4 vegades menor que al archon neutral
    			if (rc.getLocation().distanceSquaredTo(closestDen)*4 < rc.getLocation().distanceSquaredTo(closestNeutralArchon)){
    				targetLocation = closestDen;
    			}else targetLocation = closestNeutralArchon;
    		}
    	}
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
			
			//Si no ha rebut cap missatge, vol dir que es el primer, i li diu a tots els archons que vagin cap a ell
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
					Message m = new Message(rc.getLocation(), mode, object,robotType,rc.getLocation().x, rc.getLocation().y, destID, typeControl, idControl, 1);
					int[] coded = m.encode();
					rc.broadcastMessageSignal(coded[0], coded[1], maxdist+1);
				}
			}else{
				//Si n'ha rebut, va cap a la location que diu el missatge (que es la location del archon lider)
				int[] coded = found.getMessage();
				targetLocation = new MapLocation(found.getLocation().x, found.getLocation().y);
			}
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }

        while (true) {
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
                //System.out.println("Perill = "+danger[1][1]);
                
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
                	
                	//Si esta al costat d'un robot neutral, l'activa
                	if (adjacentNeutrals.length != 0){
                    	rc.activate(adjacentNeutrals[0].location);
                    	rc.setIndicatorString(0,"Ha activat un robot neutral");
                    	hasMoved = true;
                    }
                    
                	//Si pot fabricar un robot, el fabrica
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
                    
                    //Si esta en perill, fuig
                    if (!hasMoved && danger[1][1] >= DANGER_THRESHHOLD){
                    	Direction dir = safestDirection();
                    	if (dir != Direction.NONE && danger[dir.dx+1][dir.dy+1] < danger[1][1]){
                    		hasMoved = true;
                    		rc.move(dir);
                        	rc.setIndicatorString(0,"Hi havia perill i ha fugit");
                    	}
                    }
                    
                    //Si esta al costat d'unes parts, les agafa
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

                    //Si pot anar cap a la target location, hi va
                    //Si no pot directament, intenta les dues direccions del costat
                    if (!hasMoved){
                    	if (targetLocation != null && (!rc.canSense(targetLocation)||( rc.canSense(targetLocation) && rc.senseRobotAtLocation(targetLocation) != null && rc.senseRobotAtLocation(targetLocation).type == RobotType.ARCHON))){
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
                    
                    //Si pot netejar rubble en alguna direccio, la neteja
                    //Aixo ho vaig ficar perque fes algo pero no se si cal ni si es bo jajaja
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
                    /*
                    for(int i = 0; i < 3; i++){
                    	for (int j = 0; j < 3; j++){
                    		System.out.print(danger[i][j]+" ");
                    	}
                    	System.out.println("");
                    }
                    System.out.println("");	
                    */
                }else{
                	rc.setIndicatorString(0,"Tenia core delay");
                }

                //Si pot curar a algun robot, en cura a un de random
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