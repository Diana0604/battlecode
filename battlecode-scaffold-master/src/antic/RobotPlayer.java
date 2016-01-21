package antic;

import battlecode.common.*;

import java.util.Random;

public class RobotPlayer {
    /**
     * run() is the method that is called when a robot is instantiated in the Battlecode world.
     * If this method returns, the robot dies!
     **/
    static Direction[] directions = {Direction.NORTH, Direction.NORTH_EAST, Direction.EAST, Direction.SOUTH_EAST,
            Direction.SOUTH, Direction.SOUTH_WEST, Direction.WEST, Direction.NORTH_WEST};
    static RobotType[] robotTypes = {RobotType.SCOUT, RobotType.SOLDIER, RobotType.SOLDIER, RobotType.SOLDIER,
            RobotType.GUARD, RobotType.GUARD, RobotType.VIPER, RobotType.TURRET};
    static RobotController rc;
    static Random rand;
    static int attackRange;
    static int visionRange;
    static Team myTeam;
    static Team enemyTeam;
    static MapLocation targetLocation;
	static RobotInfo[] nearbyFriends;
    static RobotInfo[] nearbyEnemies;
    static RobotInfo[] nearbyZombies;
    static RobotInfo[] nearbyNeutrals;
    
    
	
    public static void run(RobotController rc2) {
        // You can instantiate variables here.
    	rc = rc2;
        rand = new Random(rc.getID());
        attackRange = rc.getType().attackRadiusSquared;
        visionRange = rc.getType().sensorRadiusSquared;
        myTeam = rc.getTeam();
        enemyTeam = myTeam.opponent();
        RobotType rt = rc.getType();
        if (rt == RobotType.ARCHON) {
            Archon.playArchon();
        } else if (rt == RobotType.SOLDIER){
        	Soldier.playSoldier();
        } else if (rt == RobotType.SCOUT){
        	Scout.playScout();
        } else if (rt == RobotType.GUARD){
        	Guard.playGuard();
        } else if (rt == RobotType.VIPER){
        	Viper.playViper();
        } else if (rt == RobotType.TURRET){
        	Turret.playTurret();
        } else if (rt == RobotType.TTM){
        	TTM.playTTM();
        } 
    }
}
