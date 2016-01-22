package team375;

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
	
    private static int M0, M1, M2, M3, M4, M5, M6, M7, M8;
    private static int[] perills, dists;
    private static MapLocation loc;
	private static int [] rondes_zombies;
	private static int proxima_zombies = 0;
	private static int enCombat = 0, buscantCombat = 0;
	private static MapLocation ls = null; //location del signal
    
	
    
	public static void playArchon(){
		try {
			rondes_zombies = rc.getZombieSpawnSchedule().getRounds();
			
			targetLocation = escollirLider();
			
			
			// Si aquest es l'archon a on tothom es dirigeix, sera el lider
			if (rc.getLocation().x == targetLocation.x && rc.getLocation().y == targetLocation.y) leader = true;
			else leader = false;
			if (leader) rc.broadcastSignal(visionRange);
			
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }

        while (true) {
            try {
            	//nearbyFriends = rc.senseNearbyRobots(visionRange, myTeam);
            	//nearbyEnemies = rc.senseNearbyRobots(visionRange,enemyTeam);
                //nearbyZombies = rc.senseNearbyRobots(visionRange,Team.ZOMBIE);
                //nearbyNeutrals = rc.senseNearbyRobots(visionRange,Team.NEUTRAL);
                loc = rc.getLocation();
                
                int nallies = 0, nenemies = 0, nzombies = 0, nneutrals = 0, dens = 0;
            	RobotInfo[] robots = rc.senseNearbyRobots();
            	RobotInfo[] allies = rc.senseNearbyRobots(6,myTeam);
            	boolean pocs_amics = (allies.length == 0);
            	if (pocs_amics) allies = new RobotInfo[robots.length];
            	else nallies = allies.length;
            	RobotInfo[] enemies = new RobotInfo[robots.length];
            	RobotInfo[] zombies = new RobotInfo[robots.length];
            	RobotInfo[] neutrals = new RobotInfo[robots.length];
            	
            	for (int i = 0; i < robots.length; ++i) {
            		if (robots[i].team == myTeam) {
            			if (pocs_amics) allies[nallies++] = robots[i];
            		}
            		else if (robots[i].team == enemyTeam) enemies[nenemies++] = robots[i];
            		else if (robots[i].team == Team.ZOMBIE) {
            			if (robots[i].type == RobotType.ZOMBIEDEN) ++dens;
            			zombies[nzombies++] = robots[i];
            		}
            		else neutrals[nneutrals++] = robots[i];
            	}
                
            	if (nenemies+nzombies > dens) {
        			ls = null;
        			if (enCombat < torns_combat) rc.broadcastSignal(visionRange);
        			enCombat = torns_combat;
        		}
    			else {
    				if (enCombat > 0) --enCombat;
    				if (buscantCombat == 0) ls = null;
    				else --buscantCombat;
    			}
            	
                int torn = rc.getRoundNum();
    			int zombies_aprop = 0;
    			if (proxima_zombies < rondes_zombies.length) {
	    			if (rondes_zombies[proxima_zombies] <= torn) {
	    				proxima_zombies++;
	    			}
	    			if (proxima_zombies < rondes_zombies.length) {
		    			int dif = rondes_zombies[proxima_zombies] - torn;
		    			if (dif < 15) zombies_aprop = 1;
	    			}
    			}
                
            	if (rc.isCoreReady()) {
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
            		
            		int [] M = {M0, M1, M2, M3, M4, M5, M6, M7, M8};
	        		
    	            boolean molt_aprop = false;
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
	            	
	            	boolean urgencia = (enCombat > 0) || ls != null;
	            	if (!molt_aprop) {
	            		if (rc.senseRubble(loc) >= 50) M[8] -= 30;
	            		for (int k = 0; k < 8; ++k) {
	            			double rubble = rc.senseRubble(loc.add(directions[k]));
	            			if (urgencia && rubble >= 100) M[k] -= 1000000;
	            			else if (rubble >= 50) M[k] -= 30;
	            		}
	            	}
	            	
	            	int millor = 8;
            		for (int i = 0; i < 8; i++) {
            			if (rc.canMove(directions[i]) || (!urgencia && rc.senseRubble(loc.add(directions[i])) >= 100)) {
            				if (M[i] > M[millor]) millor = i;
            			}
            		}
                	rc.setIndicatorString(0, ""+M[7]+" "+M[0]+" "+M[1]+" "+M[6]+" "+M[8]+" "+M[2]+" "+M[5]+" "+M[4]+" "+M[3]);
            			
            		if (millor < 8) {
            			Direction dir = directions[millor];
            			if (rc.senseRubble(loc.add(dir)) >= 100) rc.clearRubble(dir);
            			else rc.move(directions[millor]);
            		}
	            	
            	}
            	
            	
            	
            	
            	
            	
            	
            	
            	
            	
            	
            	boolean hasMoved = false;
            	/*
            	
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
                */
                if (rc.isCoreReady()) {
                	//
                	 // Ordre d'importancia:
                	 // 1. Si hi ha un robot neutral, l'activa
                	 // 2. Si pot fabricar un robot, el fabrica
                	 // 3. Si esta en perill, fuig
                	 // 4. Recull parts adjacents
                	 // 5. Va a la target location (si no hi es ja)
                	 // 5. Neteja rubble adjacent
                	 // Sempre: intenta reparar soldats propers	
                	 /*
                	Boolean hasMoved = false;
                	RobotInfo[] adjacentNeutrals = rc.senseNearbyRobots(2, Team.NEUTRAL);
                    //if (targetLocation != null) System.out.println("TargetLocation = "+targetLocation.x+" "+targetLocation.y);
                	
                	//Si esta al costat d'un robot neutral, l'activa
                	if (adjacentNeutrals.length != 0){
                    	rc.activate(adjacentNeutrals[0].location);
                    	rc.setIndicatorString(0,"Ha activat un robot neutral");
                    	hasMoved = true;
                    }
                    */
                	//Si pot fabricar un robot, el fabrica
                    if (enCombat == 0 && !hasMoved && rc.hasBuildRequirements(nextRobotType)) {
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
                        rc.broadcastSignal(visionRange);
                        rc.broadcastSignal(visionRange);
                    } 
                }
                    /*
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
                */
                Clock.yield();
            } catch (Exception e) {
                System.out.println(e.getMessage());
                e.printStackTrace();
            }
        }
	}
}