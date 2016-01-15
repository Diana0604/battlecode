package team375;

import battlecode.common.Clock;
import battlecode.common.Direction;
import battlecode.common.GameConstants;
import battlecode.common.MapLocation;
import battlecode.common.RobotInfo;
import battlecode.common.RobotType;
import battlecode.common.Signal;
import battlecode.common.Team;

public class Archon extends RobotPlayer{
	static RobotInfo[] nearbyFriends;
    static RobotInfo[] nearbyEnemies;
    static RobotInfo[] nearbyZombies;
    static RobotInfo[] nearbyNeutrals;
    static int[][] danger;
    static final int DANGER_THRESHHOLD = 1;
	

    
    
    
    //Retorna true si l'archon detecta enemics que li poden disparar, i cap enemic li pot disparar a un enemic seu
    public static int getDanger(MapLocation loc){
    	int ret = 0;
    	for (RobotInfo i: nearbyEnemies){
    		if (i.type.attackRadiusSquared < Utils.squareDist(i.location, loc)) continue; //canviar el radi a la visio?
    		if (rc.senseNearbyRobots(i.location, i.type.attackRadiusSquared, myTeam).length == 1) {
    			//arreglar-ho per torretes
    			//si l'unica unitat del meu equip que veuen es l'archon, posar-hi perill
    			ret++;
    		}
    	}
    	for (RobotInfo i: nearbyZombies){
    		if (i.type.attackRadiusSquared < Utils.squareDist(i.location, loc)) continue;
    		//si em poden disparar els zombies, posar-hi perill
    		ret++;
    	}
    	return ret;
    }
    
    public static void calculateDanger(){
    	for (int i = -1; i < 2; i++){
    		for (int j = -1; j < 2; j++){
    			danger[i+1][j+1] = getDanger(rc.getLocation().add(i,j));
    		}
    	}
    }
    
    public static Direction safestDirection(){
    	Direction dir = Direction.NORTH;
    	int safeDir = -1;
    	int lowestDanger = 100;
    	for (int i = 0; i < 8; i++){
    		MapLocation loc = rc.getLocation().add(dir);
    		if (!rc.canMove(dir)) {
    			dir = dir.rotateLeft();
    			continue;
    		}
    		if (safeDir == -1) safeDir = -2;
    		int aux = getDanger(loc);
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
    
	
	public static void playArchon(){
		try {
            // Any code here gets executed exactly once at the beginning of the game.
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
                Signal[] signals = rc.emptySignalQueue();
                nearbyFriends = rc.senseNearbyRobots(visionRange,myTeam);
                nearbyEnemies = rc.senseNearbyRobots(visionRange,enemyTeam);
                nearbyZombies = rc.senseNearbyRobots(visionRange,Team.ZOMBIE);
                nearbyNeutrals = rc.senseNearbyRobots(visionRange,Team.NEUTRAL);
                danger = new int[3][3];
                calculateDanger();
                
                if (rc.isCoreReady()) {
                	/*
                	 * Ordre d'importancia:
                	 * 1. Si hi ha un robot neutral, l'activa
                	 * 2. Si pot fabricar un robot, el fabrica
                	 * 3. Si esta en perill, fuig
                	 * 4. Recull parts adjacents
                	 * 5. Neteja rubble adjacent
                	 * Sempre: intenta reparar soldats propers	
                	 */
                	Boolean hasMoved = false;
                	RobotInfo[] adjacentNeutrals = rc.senseNearbyRobots(2, Team.NEUTRAL);
                    
                	if (adjacentNeutrals.length != 0){
                    	rc.activate(adjacentNeutrals[0].location);
                    	rc.setIndicatorString(0,"Ha activat un robot neutral");
                    	hasMoved = true;
                    }
                    
                    if (!hasMoved && rc.hasBuildRequirements(RobotType.SOLDIER)) {
                    	//De moment nomes fabrica soldiers
                        Direction dirToBuild = directions[rand.nextInt(8)];
                        for (int i = 0; i < 8; i++) {
                            // If possible, build in this direction
                            if (rc.canBuild(dirToBuild, RobotType.SOLDIER)) {
                                rc.build(dirToBuild, RobotType.SOLDIER);
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
                    	if (dir != Direction.NONE){
                    		hasMoved = true;
                    		rc.move(safestDirection());
                        	rc.setIndicatorString(0,"Hi havia perill i ha fugit");
                    	}/*
                    	System.out.println("Danger: ");
                        for (int i2 = 0; i2 < 3; i2++){
                        	for (int j = 0; j < 3; j++){
                        		System.out.print(danger[i2][j] + " ");
                        	}
                        	System.out.println("");
                        }
                        System.out.println("");*/
                        
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
