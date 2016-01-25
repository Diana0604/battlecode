package team375;

import java.util.ArrayList;

import battlecode.common.*;

// FICAR ALGO DE PERILL AL VOLTANT DELS ARCHONS PQ ELS SOLDATS ELS DEIXIN PASSAR

public class Archon extends RobotPlayer{
	
	//Arraylist amb les posicions dels dens que coneix
	static ArrayList<MapLocation> dens = new ArrayList<>();
	
	//Arraylist amb les posicions dels archons neutrals que coneix
	static ArrayList<MapLocation> neutralArchons = new ArrayList<>();
	
	static ArrayList<MapLocation> corners = new ArrayList<>();
	
	//Array 3x3 amb els perills adjacents
    static int[][] danger;
    
    //Aixo es per dir que si perill > danger_threshhold llavors fuig
    static final int DANGER_THRESHHOLD = 1;
    
    //si aquest archon es el lider
    static Boolean leader;
    
    //tipus del proxim robot que construira
    static RobotType nextRobotType = RobotType.SCOUT;
    
    //Calcula el perill i el posa en la array de 3x3
    public static void calculateDanger(){
    	M0 = 1; M1 = 0; M2 = 1; M3 = 0; M4 = 1; M5 = 0; M6 = 1; M7 = 0; M8 = 2;
		for (int j = 0; j < nenemies; ++j) {
			RobotInfo rob = enemies[j];
			dists = eucl[rob.location.x-loc.x + 5][rob.location.y-loc.y + 5];
			perills = null;
			switch(rob.type) {
				case SOLDIER: perills = eSoldier; break;
				case GUARD: perills = eGuard; break;
				case TURRET: perills = eTurret; break;
				case VIPER: perills = eViper; break;
				case ARCHON: perills = eArchon; break;
				default: break;
			}
			if (perills != null) sumarM();
		}
    	for (int j = 0; j < nzombies; ++j) {
			RobotInfo rob = zombies[j];
			dists = eucl[rob.location.x-loc.x + 5][rob.location.y-loc.y + 5];
			perills = null;
			switch(rob.type) {
			case STANDARDZOMBIE: perills = sZombie; break;
			case FASTZOMBIE: perills = fZombie; break;
			case BIGZOMBIE: perills = bZombie; break;
			case RANGEDZOMBIE: perills = rZombie; break;
			case ZOMBIEDEN: perills = dZombie[zombies_aprop]; break;
			default: break;
			}
			if (perills != null) sumarM();
		}
    	for (int j = 0; j < nallies; ++j) {
			RobotInfo rob = allies[j];
			dists = eucl[rob.location.x-loc.x + 5][rob.location.y-loc.y + 5];
			perills = null;
			if (rob.type == RobotType.SOLDIER) perills = aSoldier;
			else if (rob.type == RobotType.ARCHON) perills = aArchon;
			if (perills != null) sumarM();
		}
    	for (int j = 0; j < nneutrals; ++j) {
			RobotInfo rob = neutrals[j];
			dists = eucl[rob.location.x-loc.x + 5][rob.location.y-loc.y + 5];
			perills = null;
			perills = xNeutral;
			if (perills != null) sumarM();
		}
		
		int [] Maux = {M0, M1, M2, M3, M4, M5, M6, M7, M8};
		M = Maux;
		
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
    	else {
    		if (stage < 4) nextRobotType = RobotType.SOLDIER;
    		else nextRobotType = RobotType.TURRET;
    	}
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
    			else if (mode == Message.STAGE2) {
    				stage = 2;
    				targetLocation = new MapLocation(x,y);
    			}
    			else if (mode == Message.GO_TURTLE) {
    				if (targetLocation.distanceSquaredTo(loc) <= 2) {
    					stage = 4;
    				}
    			}
    		}else{
    			//L'ha enviat un scout
    			//System.out.println("rep missatge de scout");
    			if (mode == Message.FOUND){
    				MapLocation objecte = new MapLocation(x,y);
    				if (object == Message.DEN){
    					//Si un scout li diu que ha trobat un den, l'afegeix a la llista
    					if (!dens.contains(objecte)){
    						dens.add(objecte);
    					}
    				}
    				else if (object == Message.NEUTRAL_ARCHON){
    					//Si un scout li diu que ha trobat un archon neutral, l'afegeix a la llista
    					if (!neutralArchons.contains(objecte)){
    						neutralArchons.add(objecte);
    					}
    				}
    				else if (object == Message.CORNER) {
    					if (!corners.contains(objecte)){
    						corners.add(objecte);
    					}
    				}
    			}
    		}
    	}
    }
    
    //Si envia signals es perque es lider. Diu a tots els robots propers d'anar a la target location que te
    private static void sendSignals() throws GameActionException {
    	if (targetLocation != null && stage <= 2){
    		enviarGoto(targetLocation);
    	}
    }
    
    private static void enviarGoto(MapLocation location) throws GameActionException {
    	int mode = Message.GO_TO;
		int object = Message.NONE;
		int typeControl = Message.NONE;
		int robotType = Message.NONE;
		int x = location.x;
		int y = location.y;
		int idControl = Message.NONE;
		int id = Message.NONE;
		Message m = new Message(rc.getLocation(), mode, object, robotType, x, y, id, typeControl, idControl, 1);
		int[] coded = m.encode();
		rc.broadcastMessageSignal(coded[0], coded[1], 2*rc.getType().sensorRadiusSquared);
    }
    
    private static void enviarSoldatsDens() throws GameActionException {
    	int distancia_maxima = (int)((double)distancia_gran/1.4/2);
    	dens.sort(((d1,d2)->(targetLocation.distanceSquaredTo(d1)-targetLocation.distanceSquaredTo(d2))));
    	int mode = Message.REG_DEN;
		int object = Message.NONE;
		int typeControl = 1;
		int robotType = Message.SOLDIER;
		int idControl = Message.NONE;
		int id = Message.NONE;
    	for (int i = 0; i < Math.min(20,dens.size()); ++i) {
    		if (targetLocation.distanceSquaredTo(dens.get(i)) < distancia_maxima) {
    			int x = dens.get(i).x;
    			int y = dens.get(i).y;
    			Message m = new Message(rc.getLocation(), mode, object, robotType, x, y, id, typeControl, idControl, 1);
    			int[] coded = m.encode();
    			rc.broadcastMessageSignal(coded[0], coded[1], 2*visionRange);
    		}
    	}
    }

    
    private static void enviarSignalStage2() throws GameActionException {
    	int mode = Message.STAGE2;
		int object = Message.NONE;
		int typeControl = Message.NONE;
		int robotType = Message.NONE;
		int x = targetLocation.x;
		int y = targetLocation.y;
		int idControl = Message.NONE;
		int id = Message.NONE;
		Message m = new Message(rc.getLocation(), mode, object, robotType, x, y, id, typeControl, idControl, 1);
		int[] coded = m.encode();
		rc.broadcastMessageSignal(coded[0], coded[1], 12800);
    }

    private static void enviarClearRubble() throws GameActionException {
    	int mode = Message.CLEAR_RUBBLE;
		int object = Message.NONE;
		int typeControl = Message.NONE;
		int robotType = Message.NONE;
		int x = targetLocation.x;
		int y = targetLocation.y;
		int idControl = Message.NONE;
		int id = Message.NONE;
		Message m = new Message(rc.getLocation(), mode, object, robotType, x, y, id, typeControl, idControl, 1);
		int[] coded = m.encode();
		rc.broadcastMessageSignal(coded[0], coded[1], visionRange);
    }
    
    private static void enviarGoTurtle() throws GameActionException {
    	int mode = Message.GO_TURTLE;
		int object = Message.NONE;
		int typeControl = Message.NONE;
		int robotType = Message.NONE;
		int x = targetLocation.x;
		int y = targetLocation.y;
		int idControl = Message.NONE;
		int id = Message.NONE;
		Message m = new Message(rc.getLocation(), mode, object, robotType, x, y, id, typeControl, idControl, 1);
		int[] coded = m.encode();
		rc.broadcastMessageSignal(coded[0], coded[1], 2*visionRange);
    }
    
    
    private static void trobarAltraCorner() {
    	int x = corners.get(0).x^corners.get(1).x^corners.get(2).x;
    	int y = corners.get(0).y^corners.get(1).y^corners.get(2).y;
    	corners.add(new MapLocation(x,y));
    }

    private static void decidirCorner() throws GameActionException {
    	calcDistanciaGran(corners.toArray());
    	int dist = 1000000;
    	MapLocation millor = null;
    	for (MapLocation i:corners) {
    		int nova = i.distanceSquaredTo(targetLocation);
    		if (nova < dist) {
    			dist = nova;
    			millor = i;
    		}
    	}
    	if (leader) {
    		if (millor != null) targetLocation = millor;
        	stage = 2;
        	rc.broadcastSignal(visionRange);
        	rc.broadcastSignal(visionRange);
        	enviarSignalStage2();
    	}
    }
    
    private static boolean voltantNet() {
    	if (ndens > 0) return false;
    	MapLocation[] ml = MapLocation.getAllMapLocationsWithinRadiusSq(loc, visionRange);
		for (MapLocation i:ml) {
			if (rc.senseRubble(i) >= 50) {
				rc.setIndicatorString(1, "("+i.x+","+i.y+")");
				return false;
			}
		}
		return true;
    }
    
    private static void netejarAdjacent() throws GameActionException {
    	double maxima = 0;
		int millor = 0;
		for (int i = 0; i < 8; ++i) {
			MapLocation nova = loc.add(directions[i]);
			if (!rc.onTheMap(nova)) continue;
			double rubble = rc.senseRubble(nova);
			if (rubble > maxima) {
				millor = i;
				maxima = rubble;
			}
		}
		if (maxima >= 50) rc.clearRubble(directions[millor]);
    }
    
    private static void calcDistanciaGran(Object[] archons) {
    	distancia_gran = 0;
    	for (Object i:archons) {
    		for (Object j:archons) {
    			int dist = ((MapLocation)i).distanceSquaredTo((MapLocation)j);
    			if (dist > distancia_gran) distancia_gran = dist;
    		}
    	}
    }
    
    
	private static int inversaDirections (Direction d) {
		switch(d) {
		case NORTH: return 0;
		case NORTH_EAST: return 1;
		case EAST: return 2;
		case SOUTH_EAST: return 3;
		case SOUTH: return 4;
		case SOUTH_WEST: return 5;
		case WEST: return 6;
		case NORTH_WEST: return 7;
		default: return 8;
		}
	}
	
	private static void sumarM() {
		M0 += perills[dists[0]];
		M1 += perills[dists[1]];
		M2 += perills[dists[2]];
		M3 += perills[dists[3]];
		M4 += perills[dists[4]];
		M5 += perills[dists[5]];
		M6 += perills[dists[6]];
		M7 += perills[dists[7]];
		M8 += perills[dists[8]];
	}
    
	private static void construirArrays() {
		nallies = 0; nenemies = 0; nzombies = 0; nneutrals = 0; ndens = 0;
    	robots = rc.senseNearbyRobots();
    	allies = rc.senseNearbyRobots(6,myTeam);
    	boolean pocs_amics = (allies.length == 0);
    	if (pocs_amics) allies = new RobotInfo[robots.length];
    	else nallies = allies.length;
    	enemies = new RobotInfo[robots.length];
    	zombies = new RobotInfo[robots.length];
    	neutrals = new RobotInfo[robots.length];
    	
    	for (int i = 0; i < robots.length; ++i) {
    		if (robots[i].team == myTeam) {
    			if (pocs_amics) allies[nallies++] = robots[i];
    		}
    		else if (robots[i].team == enemyTeam) enemies[nenemies++] = robots[i];
    		else if (robots[i].team == Team.ZOMBIE) {
    			if (robots[i].type == RobotType.ZOMBIEDEN) {
    				if (!dens.contains(robots[i].location)) dens.add(robots[i].location);
    				++ndens;
    			}
    			zombies[nzombies++] = robots[i];
    		}
    		else neutrals[nneutrals++] = robots[i];
    	}
	}
	
	private static void variablesCombat() throws GameActionException {
		if (nenemies+nzombies > ndens) {
			ls = null;
			if (enCombat < torns_combat) rc.broadcastSignal(visionRange);
			enCombat = torns_combat;
		}
		else {
			if (enCombat > 0) --enCombat;
			if (buscantCombat == 0) ls = null;
			else --buscantCombat;
		}
	}

	private static void variablesRondes() {
		torn = rc.getRoundNum();
		zombies_aprop = 0;
		if (proxima_zombies < rondes_zombies.length) {
			if (rondes_zombies[proxima_zombies] <= torn) {
				proxima_zombies++;
			}
			if (proxima_zombies < rondes_zombies.length) {
    			int dif = rondes_zombies[proxima_zombies] - torn;
    			if (dif < 15) zombies_aprop = 1;
			}
		}
	}
	
	private static boolean buildRobot(RobotType robtype) throws GameActionException {
		Direction dirToBuild = directions[rand.nextInt(8)];
        for (int i = 0; i < 8; i++) {
            // If possible, build in this direction
            if ((stage < 3 || targetLocation.distanceSquaredTo(loc.add(dirToBuild)) > 2) && rc.canBuild(dirToBuild, robtype)) {
                rc.build(dirToBuild, robtype);
                chooseNextRobotType();
            	rc.setIndicatorString(0,"Ha construit un soldat");
		        rc.broadcastSignal(visionRange);
		        rc.broadcastSignal(visionRange);
		        if (stage == 3) enviarClearRubble();
                return true;
            } else {
                // Rotate the direction to try
                dirToBuild = dirToBuild.rotateLeft();
            }
        }
        return false;
	}
	
	private static boolean buildTurtle() throws GameActionException {
		Direction dirToBuild = directions[rand.nextInt(8)];
        for (int i = 0; i < 8; i++) {
            // If possible, build in this direction
            if (targetLocation.distanceSquaredTo(loc.add(dirToBuild)) > 2 && rc.canBuild(dirToBuild, RobotType.TURRET)) {
                rc.build(dirToBuild, nextRobotType);
                chooseNextRobotType();
            	rc.setIndicatorString(0,"Ha construit un soldat");
		        rc.broadcastSignal(visionRange);
		        rc.broadcastSignal(visionRange);
                return true;
            } else {
                // Rotate the direction to try
                dirToBuild = dirToBuild.rotateLeft();
            }
        }
        return false;
	}
	
	private static void addTargetPriority() {
		molt_aprop = false;
    	if (enCombat == 0) {
    		if (ls != null) {
				int dir = inversaDirections(loc.directionTo(ls));
				M[dir] += 80;
				M[(dir+1)%8] += 75;
				M[(dir+7)%8] += 75;
			}
			else if (targetLocation != null) {
				rc.setIndicatorString(2, "Desti: ("+targetLocation.x+","+targetLocation.y+")");
				int dir = inversaDirections(loc.directionTo(targetLocation));
				M[dir] += 80;
				M[(dir+1)%8] += 75;
				M[(dir+7)%8] += 75;
				if (targetLocation.distanceSquaredTo(loc) < 5) molt_aprop = true;
			}
		}
	}
	
	private static void addRubblePriority() {
		urgencia = (enCombat > 0) || ls != null; // si estem en combat o anem a ajudar a algu (llavors no netegem rubble, sino que la esquivem)
    	if (!molt_aprop) {
    		if (rc.senseRubble(loc) >= 50) M[8] -= 30;
    		for (int k = 0; k < 8; ++k) {
    			double rubble = rc.senseRubble(loc.add(directions[k]));
    			if (urgencia && rubble >= 100) M[k] -= 1000000;
    			else if (rubble >= 50) M[k] -= 30;
    		}
    	}
	}
	
	private static void addPartsPriority() {
		if (!urgencia) {
    		M[8] += rc.senseParts(loc)*0.3;
    		for (int k = 0; k < 8; ++k) {
    			M[k] += rc.senseParts(loc.add(directions[k]))*0.3;
    		}
    	}
	}
	
	private static Direction chooseDirection() {
		int millor = 8;
		for (int i = 0; i < 8; i++) {
			if (rc.canMove(directions[i]) || (!urgencia && rc.senseRubble(loc.add(directions[i])) >= 100)) {
				if (M[i] > M[millor]) millor = i;
			}
		}
		if (millor == 8) return null;
    	return directions[millor];
	}
	
	private static int[][][] eucl = { { {7,7,7,7,7,7,7,6,7} , {6,7,7,7,7,7,6,6,7} , {6,7,7,7,7,6,6,5,6} , {6,7,7,7,6,6,5,5,6} , {6,7,7,7,6,5,5,5,6} , {6,7,7,7,6,5,5,5,6} , {6,7,7,7,6,5,5,5,6} , {6,7,7,7,6,5,5,6,6} , {7,7,7,7,6,5,6,6,6} , {7,7,7,7,6,6,6,7,7} , {7,7,7,7,7,6,7,7,7} } , { {6,7,7,7,7,7,6,6,7} , {6,6,7,7,7,6,6,5,6} , {5,6,6,7,6,6,5,4,6} , {5,6,6,6,6,5,4,4,5} , {5,6,6,6,5,4,4,4,5} , {5,6,6,6,5,4,4,4,5} , {5,6,6,6,5,4,4,4,5} , {6,6,6,6,5,4,4,5,5} , {6,7,6,6,5,4,5,6,6} , {7,7,7,6,6,5,6,6,6} , {7,7,7,7,6,6,6,7,7} } , { {6,6,7,7,7,7,6,5,6} , {5,6,6,7,6,6,5,4,6} , {4,5,6,6,6,5,4,3,5} , {4,5,5,6,5,4,3,3,4} , {4,5,5,5,4,3,3,3,4} , {4,5,5,5,4,3,3,3,4} , {4,5,5,5,4,3,3,3,4} , {5,6,5,5,4,3,3,4,4} , {6,6,6,5,4,3,4,5,5} , {6,7,6,6,5,4,5,6,6} , {7,7,7,6,6,5,6,7,6} } , { {5,6,6,7,7,7,6,5,6} , {4,5,6,6,6,6,5,4,5} , {3,4,5,6,5,5,4,3,4} , {3,4,4,5,4,4,3,2,3} , {3,4,4,4,3,3,2,1,3} , {3,4,4,4,3,2,1,2,3} , {3,4,4,4,3,1,2,3,3} , {4,5,4,4,3,2,3,4,3} , {5,6,5,4,3,3,4,5,4} , {6,6,6,5,4,4,5,6,5} , {7,7,6,6,5,5,6,7,6} } , { {5,5,6,7,7,7,6,5,6} , {4,4,5,6,6,6,5,4,5} , {3,3,4,5,5,5,4,3,4} , {2,3,3,4,4,4,3,1,3} , {1,3,3,3,3,3,1,0,2} , {2,3,3,3,2,1,0,1,1} , {3,3,3,3,1,0,1,3,2} , {4,4,3,3,2,1,3,4,3} , {5,5,4,3,3,3,4,5,4} , {6,6,5,4,4,4,5,6,5} , {7,7,6,5,5,5,6,7,6} } , { {5,5,6,7,7,7,6,5,6} , {4,4,5,6,6,6,5,4,5} , {3,3,4,5,5,5,4,3,4} , {1,2,3,4,4,4,3,2,3} , {0,1,2,3,3,3,2,1,1} , {1,2,1,2,1,2,1,2,0} , {3,3,2,1,0,1,2,3,1} , {4,4,3,2,1,2,3,4,3} , {5,5,4,3,3,3,4,5,4} , {6,6,5,4,4,4,5,6,5} , {7,7,6,5,5,5,6,7,6} } , { {5,5,6,7,7,7,6,5,6} , {4,4,5,6,6,6,5,4,5} , {3,3,4,5,5,5,4,3,4} , {2,1,3,4,4,4,3,3,3} , {1,0,1,3,3,3,3,3,2} , {2,1,0,1,2,3,3,3,1} , {3,3,1,0,1,3,3,3,2} , {4,4,3,1,2,3,3,4,3} , {5,5,4,3,3,3,4,5,4} , {6,6,5,4,4,4,5,6,5} , {7,7,6,5,5,5,6,7,6} } , { {5,5,6,7,7,7,6,6,6} , {4,4,5,6,6,6,6,5,5} , {3,3,4,5,5,6,5,4,4} , {3,2,3,4,4,5,4,4,3} , {3,1,2,3,3,4,4,4,3} , {3,2,1,2,3,4,4,4,3} , {3,3,2,1,3,4,4,4,3} , {4,4,3,2,3,4,4,5,3} , {5,5,4,3,3,4,5,6,4} , {6,6,5,4,4,5,6,6,5} , {7,7,6,5,5,6,6,7,6} } , { {6,5,6,7,7,7,7,6,6} , {5,4,5,6,6,7,6,6,6} , {4,3,4,5,6,6,6,5,5} , {4,3,3,4,5,6,5,5,4} , {4,3,3,3,4,5,5,5,4} , {4,3,3,3,4,5,5,5,4} , {4,3,3,3,4,5,5,5,4} , {5,4,3,3,4,5,5,6,4} , {6,5,4,3,4,5,6,6,5} , {6,6,5,4,5,6,6,7,6} , {7,7,6,5,6,6,7,7,6} } , { {6,6,6,7,7,7,7,7,7} , {6,5,6,6,7,7,7,6,6} , {5,4,5,6,6,7,6,6,6} , {5,4,4,5,6,6,6,6,5} , {5,4,4,4,5,6,6,6,5} , {5,4,4,4,5,6,6,6,5} , {5,4,4,4,5,6,6,6,5} , {6,5,4,4,5,6,6,6,5} , {6,6,5,4,5,6,6,7,6} , {7,6,6,5,6,6,7,7,6} , {7,7,6,6,6,7,7,7,7} } , { {7,6,7,7,7,7,7,7,7} , {6,6,6,7,7,7,7,7,7} , {6,5,6,6,7,7,7,7,6} , {6,5,5,6,6,7,7,7,6} , {6,5,5,5,6,7,7,7,6} , {6,5,5,5,6,7,7,7,6} , {6,5,5,5,6,7,7,7,6} , {6,6,5,5,6,7,7,7,6} , {7,6,6,5,6,7,7,7,6} , {7,7,6,6,6,7,7,7,7} , {7,7,7,6,7,7,7,7,7} } };

	private static final int[] aSoldier = {-1000000, 30, 30, 25, 20, 15, 10, -10};
	private static final int[] eArchon = {-1000000, 0, 0, 0, 0, 0, 0, 0};
	private static final int[] eGuard = {-1000000, -1500, -1250, -500, -10, -8, -6, -4};
	private static final int[] eTurret = {-1000000, -4000, -3800, -3750, -3500, -3250, -3000, -2500};
	private static final int[] eViper = {-1000000, -8000, -7800, -7500, -7000, -6500, -6000, -5500};
	private static final int[] sZombie = {-1000000, -1500, -1250, -500, -10, -8, -6, -4};
	private static final int[] fZombie = {-1000000, -3000, -2750, -1000, -20, -10, -6, -4};
	private static final int[] bZombie = {-1000000, -8300, -8200, -4000, -20, -10, -6, -4};
	private static final int[] xNeutral = {-1000000, 60, 60, 45, 30, 15, 0, 0};
	private static final int[] aArchon = {-1000000, 50, 50, 45, 40, 20, 0, 0};
	private static final int[] eSoldier = {-1000000, -2100, -2000, -1500, -1250, -1000, -500, 0};
	private static final int[] rZombie = {-1000000, -3100, -3000, -2000, -1500, -1000, -500, 0};
	private static final int[][] dZombie = {{-1000000, -200, -200, -100, -20, -10, 0, 0}, {-1000000, -2000, -2000, -1000, -500, -300, -150, 0}};
	
	private static final int torns_combat = 5;
	private static int molts_soldats;
	private static int torn_limit = 200;
	private static int distancia_gran = 0;
	
	private static int stage = 1;
	private static RobotInfo[] robots, allies, enemies, neutrals, zombies;
	private static int nallies, nenemies, nzombies, nneutrals, ndens;
	private static int torn, zombies_aprop;
	private static int[] M;
    private static int M0, M1, M2, M3, M4, M5, M6, M7, M8;
    private static int[] perills, dists;
    private static MapLocation loc;
	private static int [] rondes_zombies;
	private static int proxima_zombies = 0;
	private static int enCombat = 0, buscantCombat = 0;
	private static MapLocation ls = null; //location del signal
	private static boolean molt_aprop, urgencia;
    
	
    
	public static void playArchon(){
		try {
			MapLocation[] archonsMeus = rc.getInitialArchonLocations(myTeam);
			MapLocation[] archonsSeus = rc.getInitialArchonLocations(enemyTeam);
			MapLocation[] archons = new MapLocation[archonsMeus.length*2];
			for (int i = 0; i < archonsMeus.length; i++) {
				archons[2*i] = archonsMeus[i];
				archons[2*i+1] = archonsSeus[i];
			}
			molts_soldats = 8*archonsMeus.length + 4;
			calcDistanciaGran(archons);
			rondes_zombies = rc.getZombieSpawnSchedule().getRounds();
			targetLocation = escollirLider();
			// Si aquest es l'archon a on tothom es dirigeix, sera el lider
			if (rc.getLocation().x == targetLocation.x && rc.getLocation().y == targetLocation.y) rc.broadcastSignal(visionRange);
			
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }

        while (true) {
            try {
            	if (stage < 4) {
	                loc = rc.getLocation();
	                construirArrays();
	                variablesCombat();
	            	variablesRondes();
	                
	        	 // Ordre d'importancia:
	           	 // 1. Si hi ha un robot neutral, l'activa
	           	 // 2. Si pot fabricar un robot, el fabrica
	           	 // 3. Es mou/neteja segons prioritats/perills de robots propers, objectius, rubble i parts
	           	 // Sempre: intenta reparar soldats propers	
	            	
	            	readSignals();
	            	if (stage == 1) {
		            	if (rc.getRoundNum() < torn_limit) {
		            		if (corners.size() == 3) trobarAltraCorner();
		            		if (corners.size() == 4) decidirCorner();
		            	}
		            	else {
		            		decidirCorner();
		            	}
	            	}
	            	else if (stage == 2) {
	            		if (targetLocation.distanceSquaredTo(loc) <= 2) {
	            			stage = 3;
	            			enviarClearRubble();
	            		}
	            	}
	            	else if (stage == 3) {
	            		if (targetLocation.distanceSquaredTo(loc) <= 5) {
		            		if (voltantNet()) {
		            			stage = 4;
		            			enviarGoTurtle();
		            			enviarSoldatsDens();
		            			rc.setIndicatorString(2, ""+dens.size());
		            			continue; // acabem el torn
		            		}
		            		else {
		            			if (rc.isCoreReady()) netejarAdjacent();
		            		}
	            		}
	            	}

	            	
	            	if (leader) sendSignals();
	            	
	                if (rc.isCoreReady()) {
		            	Boolean hasMoved = false;
		            	RobotInfo[] adjacentNeutrals = rc.senseNearbyRobots(2, Team.NEUTRAL);
		            	//Si esta al costat d'un robot neutral, l'activa
		            	if (adjacentNeutrals.length != 0){
		                	rc.activate(adjacentNeutrals[0].location);
		                	rc.setIndicatorString(0,"Ha activat un robot neutral");
		                	hasMoved = true;
		                }
		            	//Si pot fabricar un robot, el fabrica
	                    if (!hasMoved && rc.getRobotCount() < molts_soldats && rc.hasBuildRequirements(nextRobotType)) {
		                	hasMoved = buildRobot(nextRobotType);
		                }
	                    //Es mou (o es queda quiet) a la casella amb mes prioritat
		            	if (!hasMoved) {
		            		calculateDanger();
		            		addTargetPriority();
		    	            addRubblePriority();
			            	addPartsPriority();
		    	            Direction dir = chooseDirection();
		    	            rc.setIndicatorString(0, ""+M[7]+" "+M[0]+" "+M[1]+" "+M[6]+" "+M[8]+" "+M[2]+" "+M[5]+" "+M[4]+" "+M[3]);
		    				if (dir != null) {
		    	    			if (rc.senseRubble(loc.add(dir)) >= 100) rc.clearRubble(dir);
		    	    			else rc.move(dir);
		    	    		}
		            	}
	                    if (!hasMoved) rc.setIndicatorString(0,"No ha fet res aquest torn");
	                }
	                else {
	                	rc.setIndicatorString(0,"Tenia core delay");
	                }
	
            	}
            	else {
            		//MODO TURTLE
            		if (nextRobotType == RobotType.SOLDIER) chooseNextRobotType();
            		if (rc.isCoreReady() && rc.hasBuildRequirements(RobotType.TURRET)) {
	                	buildTurtle();
	        			if (targetLocation != null) {
	        				rc.setIndicatorString(2, "Desti: ("+targetLocation.x+","+targetLocation.y+")");
	        				Direction d = loc.directionTo(targetLocation);
	        				if (rc.canMove(d)) rc.move(d);
	        				else if (rc.canMove(d.rotateLeft())) rc.move(d.rotateLeft());
	        				else if (rc.canMove(d.rotateRight())) rc.move(d.rotateRight());
	        			}
	                }
        			enviarGoTurtle();
        			
            		
            		
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
                rc.setIndicatorString(0, "stage " + stage);
                Clock.yield();
            } catch (Exception e) {
                System.out.println(e.getMessage());
                e.printStackTrace();
            }
        }
	}
}