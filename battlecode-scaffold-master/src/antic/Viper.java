package antic;

import battlecode.common.Clock;
import battlecode.common.Direction;
import battlecode.common.GameConstants;
import battlecode.common.MapLocation;
import battlecode.common.RobotInfo;
import battlecode.common.Team;

public class Viper extends RobotPlayer {
	private static int[][][] eucl = { { { 6, 6, 6, 6, 6, 6, 6, 5, 6} , { 5, 6, 6, 6, 6, 6, 5, 4, 6} , { 5, 6, 6, 6, 6, 5, 4, 4, 5} , { 5, 6, 6, 6, 5, 4, 4, 4, 5} , { 5, 6, 6, 6, 5, 4, 4, 4, 5} , { 5, 6, 6, 6, 5, 4, 4, 4, 5} , { 6, 6, 6, 6, 5, 4, 4, 5, 5} , { 6, 6, 6, 6, 5, 4, 5, 6, 6} , { 6, 6, 6, 6, 6, 5, 6, 6, 6} } , { { 5, 6, 6, 6, 6, 6, 5, 4, 6} , { 4, 5, 6, 6, 6, 5, 4, 3, 5} , { 4, 5, 5, 6, 5, 4, 3, 3, 4} , { 4, 5, 5, 5, 4, 3, 3, 3, 4} , { 4, 5, 5, 5, 4, 3, 3, 3, 4} , { 4, 5, 5, 5, 4, 3, 3, 3, 4} , { 5, 6, 5, 5, 4, 3, 3, 4, 4} , { 6, 6, 6, 5, 4, 3, 4, 5, 5} , { 6, 6, 6, 6, 5, 4, 5, 6, 6} } , { { 4, 5, 6, 6, 6, 6, 5, 4, 5} , { 3, 4, 5, 6, 5, 5, 4, 3, 4} , { 3, 4, 4, 5, 4, 4, 3, 2, 3} , { 3, 4, 4, 4, 3, 3, 2, 1, 3} , { 3, 4, 4, 4, 3, 2, 1, 2, 3} , { 3, 4, 4, 4, 3, 1, 2, 3, 3} , { 4, 5, 4, 4, 3, 2, 3, 4, 3} , { 5, 6, 5, 4, 3, 3, 4, 5, 4} , { 6, 6, 6, 5, 4, 4, 5, 6, 5} } , { { 4, 4, 5, 6, 6, 6, 5, 4, 5} , { 3, 3, 4, 5, 5, 5, 4, 3, 4} , { 2, 3, 3, 4, 4, 4, 3, 1, 3} , { 1, 3, 3, 3, 3, 3, 1, 0, 2} , { 2, 3, 3, 3, 2, 1, 0, 1, 1} , { 3, 3, 3, 3, 1, 0, 1, 3, 2} , { 4, 4, 3, 3, 2, 1, 3, 4, 3} , { 5, 5, 4, 3, 3, 3, 4, 5, 4} , { 6, 6, 5, 4, 4, 4, 5, 6, 5} } , { { 4, 4, 5, 6, 6, 6, 5, 4, 5} , { 3, 3, 4, 5, 5, 5, 4, 3, 4} , { 1, 2, 3, 4, 4, 4, 3, 2, 3} , { 0, 1, 2, 3, 3, 3, 2, 1, 1} , { 1, 2, 1, 2, 1, 2, 1, 2, 0} , { 3, 3, 2, 1, 0, 1, 2, 3, 1} , { 4, 4, 3, 2, 1, 2, 3, 4, 3} , { 5, 5, 4, 3, 3, 3, 4, 5, 4} , { 6, 6, 5, 4, 4, 4, 5, 6, 5} } , { { 4, 4, 5, 6, 6, 6, 5, 4, 5} , { 3, 3, 4, 5, 5, 5, 4, 3, 4} , { 2, 1, 3, 4, 4, 4, 3, 3, 3} , { 1, 0, 1, 3, 3, 3, 3, 3, 2} , { 2, 1, 0, 1, 2, 3, 3, 3, 1} , { 3, 3, 1, 0, 1, 3, 3, 3, 2} , { 4, 4, 3, 1, 2, 3, 3, 4, 3} , { 5, 5, 4, 3, 3, 3, 4, 5, 4} , { 6, 6, 5, 4, 4, 4, 5, 6, 5} } , { { 4, 4, 5, 6, 6, 6, 6, 5, 5} , { 3, 3, 4, 5, 5, 6, 5, 4, 4} , { 3, 2, 3, 4, 4, 5, 4, 4, 3} , { 3, 1, 2, 3, 3, 4, 4, 4, 3} , { 3, 2, 1, 2, 3, 4, 4, 4, 3} , { 3, 3, 2, 1, 3, 4, 4, 4, 3} , { 4, 4, 3, 2, 3, 4, 4, 5, 3} , { 5, 5, 4, 3, 3, 4, 5, 6, 4} , { 6, 6, 5, 4, 4, 5, 6, 6, 5} } , { { 5, 4, 5, 6, 6, 6, 6, 6, 6} , { 4, 3, 4, 5, 6, 6, 6, 5, 5} , { 4, 3, 3, 4, 5, 6, 5, 5, 4} , { 4, 3, 3, 3, 4, 5, 5, 5, 4} , { 4, 3, 3, 3, 4, 5, 5, 5, 4} , { 4, 3, 3, 3, 4, 5, 5, 5, 4} , { 5, 4, 3, 3, 4, 5, 5, 6, 4} , { 6, 5, 4, 3, 4, 5, 6, 6, 5} , { 6, 6, 5, 4, 5, 6, 6, 6, 6} } , { { 6, 5, 6, 6, 6, 6, 6, 6, 6} , { 5, 4, 5, 6, 6, 6, 6, 6, 6} , { 5, 4, 4, 5, 6, 6, 6, 6, 5} , { 5, 4, 4, 4, 5, 6, 6, 6, 5} , { 5, 4, 4, 4, 5, 6, 6, 6, 5} , { 5, 4, 4, 4, 5, 6, 6, 6, 5} , { 6, 5, 4, 4, 5, 6, 6, 6, 5} , { 6, 6, 5, 4, 5, 6, 6, 6, 6} , { 6, 6, 6, 5, 6, 6, 6, 6, 6} } };
	private static int[] perills;
	public static void playViper() {
		try {
            // Any code here gets executed exactly once at the beginning of the game.
            attackRange = rc.getType().attackRadiusSquared;
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
            	int bc1, bc2;
            	int dos = 4, tres = 3, quatre = 4, cinc = 5, sis = 6;
            	boolean cert = true, fals = false;
            	int a = 3000, b= -547380, c = 1238473, d= 999, res;
            	boolean bol;
            	//int perills[][] = {{1, 2, 3, 4, 5, 6, 7, 8, 9},{1, 2, 3, 4, 5, 6, 7, 8, 9}};
            	if (rc.getRoundNum() == 2) {
            		System.out.printf("\n==============PROVADOR DE BYTECODE==============\n");
            		bc1 = Clock.getBytecodeNum();
            		
            		perills = eucl[dos][tres];
            		
            		bc2 = Clock.getBytecodeNum();
            		System.out.printf("Bytecodes: %d\n", bc2-bc1-1);
            		
            	}
            	
            	
            	
            	
                /*int fate = rand.nextInt(1000);

                boolean shouldAttack = false;

                // If this robot type can attack, check for enemies within range and attack one
                if (attackRange > 0) {
                    RobotInfo[] enemiesWithinRange = rc.senseNearbyRobots(attackRange, enemyTeam);
                    RobotInfo[] zombiesWithinRange = rc.senseNearbyRobots(attackRange, Team.ZOMBIE);
                    if (enemiesWithinRange.length > 0) {
                        shouldAttack = true;
                        // Check if weapon is ready
                        if (rc.isWeaponReady()) {
                            rc.attackLocation(enemiesWithinRange[rand.nextInt(enemiesWithinRange.length)].location);
                        }
                    } else if (zombiesWithinRange.length > 0) {
                        shouldAttack = true;
                        // Check if weapon is ready
                        if (rc.isWeaponReady()) {
                            rc.attackLocation(zombiesWithinRange[rand.nextInt(zombiesWithinRange.length)].location);
                        }
                    }
                }

                if (!shouldAttack) {
                    if (rc.isCoreReady()) {
                        if (fate < 600) {
                            // Choose a random direction to try to move in
                            Direction dirToMove = directions[fate % 8];
                            // Check the rubble in that direction
                            if (rc.senseRubble(rc.getLocation().add(dirToMove)) >= GameConstants.RUBBLE_OBSTRUCTION_THRESH) {
                                // Too much rubble, so I should clear it
                                rc.clearRubble(dirToMove);
                                // Check if I can move in this direction
                            } else if (rc.canMove(dirToMove)) {
                                // Move
                                rc.move(dirToMove);
                            }
                        }
                    }
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
