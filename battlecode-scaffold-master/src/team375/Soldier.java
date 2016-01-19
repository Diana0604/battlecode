package team375;

import battlecode.common.Clock;
import battlecode.common.Direction;
import battlecode.common.GameConstants;
import battlecode.common.RobotInfo;
import battlecode.common.RobotType;
import battlecode.common.Signal;
import battlecode.common.Team;
import battlecode.common.MapLocation;

public class Soldier extends RobotPlayer {

	
	private static int[][][] taxi = { { { 5, 6, 6, 6, 6, 6, 5, 4, 5} , { 5, 6, 6, 6, 5, 5, 4, 4, 5} , { 5, 6, 6, 6, 5, 4, 4, 4, 5} , { 5, 6, 6, 6, 5, 4, 4, 4, 5} , { 5, 6, 6, 6, 5, 4, 4, 4, 5} , { 5, 6, 6, 6, 5, 4, 4, 4, 5} , { 5, 6, 6, 6, 5, 4, 4, 4, 5} , { 5, 6, 6, 6, 5, 4, 4, 5, 5} , { 6, 6, 6, 6, 5, 4, 5, 6, 5} } , { { 4, 5, 5, 6, 6, 6, 5, 4, 5} , { 4, 5, 5, 5, 5, 5, 4, 3, 4} , { 4, 5, 5, 5, 4, 4, 3, 3, 4} , { 4, 5, 5, 5, 4, 3, 3, 3, 4} , { 4, 5, 5, 5, 4, 3, 3, 3, 4} , { 4, 5, 5, 5, 4, 3, 3, 3, 4} , { 4, 5, 5, 5, 4, 3, 3, 4, 4} , { 5, 5, 5, 5, 4, 3, 4, 5, 4} , { 6, 6, 5, 5, 4, 4, 5, 6, 5} } , { { 4, 4, 5, 6, 6, 6, 5, 4, 5} , { 3, 4, 4, 5, 5, 5, 4, 3, 4} , { 3, 4, 4, 4, 4, 4, 3, 2, 3} , { 3, 4, 4, 4, 3, 3, 2, 1, 3} , { 3, 4, 4, 4, 3, 2, 1, 2, 3} , { 3, 4, 4, 4, 3, 1, 2, 3, 3} , { 4, 4, 4, 4, 3, 2, 3, 4, 3} , { 5, 5, 4, 4, 3, 3, 4, 5, 4} , { 6, 6, 5, 4, 4, 4, 5, 6, 5} } , { { 4, 4, 5, 6, 6, 6, 5, 4, 5} , { 3, 3, 4, 5, 5, 5, 4, 3, 4} , { 2, 3, 3, 4, 4, 4, 3, 1, 3} , { 1, 3, 3, 3, 3, 3, 1, 1, 2} , { 2, 3, 3, 3, 2, 1, 1, 1, 1} , { 3, 3, 3, 3, 1, 1, 1, 3, 2} , { 4, 4, 3, 3, 2, 1, 3, 4, 3} , { 5, 5, 4, 3, 3, 3, 4, 5, 4} , { 6, 6, 5, 4, 4, 4, 5, 6, 5} } , { { 4, 4, 5, 6, 6, 6, 5, 4, 5} , { 3, 3, 4, 5, 5, 5, 4, 3, 4} , { 1, 2, 3, 4, 4, 4, 3, 2, 3} , { 1, 1, 2, 3, 3, 3, 2, 1, 1} , { 1, 2, 1, 2, 1, 2, 1, 2, 1} , { 3, 3, 2, 1, 1, 1, 2, 3, 1} , { 4, 4, 3, 2, 1, 2, 3, 4, 3} , { 5, 5, 4, 3, 3, 3, 4, 5, 4} , { 6, 6, 5, 4, 4, 4, 5, 6, 5} } , { { 4, 4, 5, 6, 6, 6, 5, 4, 5} , { 3, 3, 4, 5, 5, 5, 4, 3, 4} , { 2, 1, 3, 4, 4, 4, 3, 3, 3} , { 1, 1, 1, 3, 3, 3, 3, 3, 2} , { 2, 1, 1, 1, 2, 3, 3, 3, 1} , { 3, 3, 1, 1, 1, 3, 3, 3, 2} , { 4, 4, 3, 1, 2, 3, 3, 4, 3} , { 5, 5, 4, 3, 3, 3, 4, 5, 4} , { 6, 6, 5, 4, 4, 4, 5, 6, 5} } , { { 4, 4, 5, 6, 6, 6, 5, 4, 5} , { 3, 3, 4, 5, 5, 5, 4, 4, 4} , { 3, 2, 3, 4, 4, 4, 4, 4, 3} , { 3, 1, 2, 3, 3, 4, 4, 4, 3} , { 3, 2, 1, 2, 3, 4, 4, 4, 3} , { 3, 3, 2, 1, 3, 4, 4, 4, 3} , { 4, 4, 3, 2, 3, 4, 4, 4, 3} , { 5, 5, 4, 3, 3, 4, 4, 5, 4} , { 6, 6, 5, 4, 4, 4, 5, 6, 5} } , { { 4, 4, 5, 6, 6, 6, 5, 5, 5} , { 4, 3, 4, 5, 5, 5, 5, 5, 4} , { 4, 3, 3, 4, 4, 5, 5, 5, 4} , { 4, 3, 3, 3, 4, 5, 5, 5, 4} , { 4, 3, 3, 3, 4, 5, 5, 5, 4} , { 4, 3, 3, 3, 4, 5, 5, 5, 4} , { 4, 4, 3, 3, 4, 5, 5, 5, 4} , { 5, 5, 4, 3, 4, 5, 5, 5, 4} , { 6, 6, 5, 4, 4, 5, 5, 6, 5} } , { { 5, 4, 5, 6, 6, 6, 6, 6, 5} , { 5, 4, 4, 5, 5, 6, 6, 6, 5} , { 5, 4, 4, 4, 5, 6, 6, 6, 5} , { 5, 4, 4, 4, 5, 6, 6, 6, 5} , { 5, 4, 4, 4, 5, 6, 6, 6, 5} , { 5, 4, 4, 4, 5, 6, 6, 6, 5} , { 5, 4, 4, 4, 5, 6, 6, 6, 5} , { 5, 5, 4, 4, 5, 6, 6, 6, 5} , { 6, 6, 5, 4, 5, 6, 6, 6, 5} } };

	//private static int[][][] eucl = { { { 41, 34, 25, 18, 25, 34, 41, 50, 32} , { 32, 25, 18, 13, 20, 29, 34, 41, 25} , { 25, 18, 13, 10, 17, 26, 29, 34, 20} , { 20, 13, 10, 9, 16, 25, 26, 29, 17} , { 17, 10, 9, 10, 17, 26, 25, 26, 16} , { 16, 9, 10, 13, 20, 29, 26, 25, 17} , { 17, 10, 13, 18, 25, 34, 29, 26, 20} , { 20, 13, 18, 25, 32, 41, 34, 29, 25} , { 25, 18, 25, 34, 41, 50, 41, 34, 32} } , { { 34, 29, 20, 13, 18, 25, 32, 41, 25} , { 25, 20, 13, 8, 13, 20, 25, 32, 18} , { 18, 13, 8, 5, 10, 17, 20, 25, 13} , { 13, 8, 5, 4, 9, 16, 17, 20, 10} , { 10, 5, 4, 5, 10, 17, 16, 17, 9} , { 9, 4, 5, 8, 13, 20, 17, 16, 10} , { 10, 5, 8, 13, 18, 25, 20, 17, 13} , { 13, 8, 13, 20, 25, 32, 25, 20, 18} , { 18, 13, 20, 29, 34, 41, 32, 25, 25} } , { { 29, 26, 17, 10, 13, 18, 25, 34, 20} , { 20, 17, 10, 5, 8, 13, 18, 25, 13} , { 13, 10, 5, 2, 5, 10, 13, 18, 8} , { 8, 5, 2, 1, 4, 9, 10, 13, 5} , { 5, 2, 1, 2, 5, 10, 9, 10, 4} , { 4, 1, 2, 5, 8, 13, 10, 9, 5} , { 5, 2, 5, 10, 13, 18, 13, 10, 8} , { 8, 5, 10, 17, 20, 25, 18, 13, 13} , { 13, 10, 17, 26, 29, 34, 25, 18, 20} } , { { 26, 25, 16, 9, 10, 13, 20, 29, 17} , { 17, 16, 9, 4, 5, 8, 13, 20, 10} , { 10, 9, 4, 1, 2, 5, 8, 13, 5} , { 5, 4, 1, 0, 1, 4, 5, 8, 2} , { 2, 1, 0, 1, 2, 5, 4, 5, 1} , { 1, 0, 1, 4, 5, 8, 5, 4, 2} , { 2, 1, 4, 9, 10, 13, 8, 5, 5} , { 5, 4, 9, 16, 17, 20, 13, 8, 10} , { 10, 9, 16, 25, 26, 29, 20, 13, 17} } , { { 25, 26, 17, 10, 9, 10, 17, 26, 16} , { 16, 17, 10, 5, 4, 5, 10, 17, 9} , { 9, 10, 5, 2, 1, 2, 5, 10, 4} , { 4, 5, 2, 1, 0, 1, 2, 5, 1} , { 1, 2, 1, 2, 1, 2, 1, 2, 0} , { 0, 1, 2, 5, 4, 5, 2, 1, 1} , { 1, 2, 5, 10, 9, 10, 5, 2, 4} , { 4, 5, 10, 17, 16, 17, 10, 5, 9} , { 9, 10, 17, 26, 25, 26, 17, 10, 16} } , { { 26, 29, 20, 13, 10, 9, 16, 25, 17} , { 17, 20, 13, 8, 5, 4, 9, 16, 10} , { 10, 13, 8, 5, 2, 1, 4, 9, 5} , { 5, 8, 5, 4, 1, 0, 1, 4, 2} , { 2, 5, 4, 5, 2, 1, 0, 1, 1} , { 1, 4, 5, 8, 5, 4, 1, 0, 2} , { 2, 5, 8, 13, 10, 9, 4, 1, 5} , { 5, 8, 13, 20, 17, 16, 9, 4, 10} , { 10, 13, 20, 29, 26, 25, 16, 9, 17} } , { { 29, 34, 25, 18, 13, 10, 17, 26, 20} , { 20, 25, 18, 13, 8, 5, 10, 17, 13} , { 13, 18, 13, 10, 5, 2, 5, 10, 8} , { 8, 13, 10, 9, 4, 1, 2, 5, 5} , { 5, 10, 9, 10, 5, 2, 1, 2, 4} , { 4, 9, 10, 13, 8, 5, 2, 1, 5} , { 5, 10, 13, 18, 13, 10, 5, 2, 8} , { 8, 13, 18, 25, 20, 17, 10, 5, 13} , { 13, 18, 25, 34, 29, 26, 17, 10, 20} } , { { 34, 41, 32, 25, 18, 13, 20, 29, 25} , { 25, 32, 25, 20, 13, 8, 13, 20, 18} , { 18, 25, 20, 17, 10, 5, 8, 13, 13} , { 13, 20, 17, 16, 9, 4, 5, 8, 10} , { 10, 17, 16, 17, 10, 5, 4, 5, 9} , { 9, 16, 17, 20, 13, 8, 5, 4, 10} , { 10, 17, 20, 25, 18, 13, 8, 5, 13} , { 13, 20, 25, 32, 25, 20, 13, 8, 18} , { 18, 25, 32, 41, 34, 29, 20, 13, 25} } , { { 41, 50, 41, 34, 25, 18, 25, 34, 32} , { 32, 41, 34, 29, 20, 13, 18, 25, 25} , { 25, 34, 29, 26, 17, 10, 13, 18, 20} , { 20, 29, 26, 25, 16, 9, 10, 13, 17} , { 17, 26, 25, 26, 17, 10, 9, 10, 16} , { 16, 25, 26, 29, 20, 13, 10, 9, 17} , { 17, 26, 29, 34, 25, 18, 13, 10, 20} , { 20, 29, 34, 41, 32, 25, 18, 13, 25} , { 25, 34, 41, 50, 41, 34, 25, 18, 32} } };
	private static int[] aSoldier = {-1000000, -5, -5, 0, 0, 0, 0};
	private static int[] eSoldier = {-1000000, -2100, -2000, -1000, 20, 10, 0};
	private static int[] sZombie = {-1000000, -1500, -1250, -500, 20, 10, 0};
	
	
	private static int taxista (MapLocation a, MapLocation b) {
		return Math.max(Math.abs(a.x-b.x),Math.abs(a.y-b.y));
	}
	
	static private boolean enCombat = false;
	
	
	static boolean jo = false;
	static int compt_bc = 0;
	static int compt_no = 0;
	public static void playSoldier() {
		try {
			if (rc.getLocation().x == 426 && rc.getLocation().y == 157) jo = true;
            // Any code here gets executed exactly once at the beginning of the game.
            attackRange = rc.getType().attackRadiusSquared;
        } catch (Exception e) {
            // Throwing an uncaught exception makes the robot die, so we need to catch exceptions.
            // Caught exceptions will result in a bytecode penalty.
            System.out.println(e.getMessage());
            e.printStackTrace();
        }

        while (true) {
        	/**
        	 * Coses a tenir en compte:
        	 * * Quan una torre ataca i no la veiem
        	 * * Quan una rubble ens barra el pas al camí recte
        	 * * Quan esta a poca vida en combat, retirar-se (si no esta infectat, almenys)
        	 */
        	
        	
            // This is a loop to prevent the run() method from returning. Because of the Clock.yield()
            // at the end of it, the loop will iterate once per game round.
            try {
            	
            	String sortida = "";
            	
            	if (rc.getRoundNum() > 40) jo = false;
            	int torn = rc.getRoundNum();
            									if (jo) sortida+="torn "+torn+" bytecodes " + Clock.getBytecodesLeft() + " ";

            	RobotInfo[] robots = rc.senseNearbyRobots();
            	RobotInfo[] allies = new RobotInfo[robots.length];
            	RobotInfo[] enemies = new RobotInfo[robots.length];
            	RobotInfo[] zombies = new RobotInfo[robots.length];

            	int nallies = 0, nenemies = 0, nzombies = 0;
            	for (int i = 0; i < robots.length; ++i) {
            		if (robots[i].team == myTeam) allies[nallies++] = robots[i];
            		else if (robots[i].team == enemyTeam) enemies[nenemies++] = robots[i];
            		else if (robots[i].team == Team.ZOMBIE) zombies[nzombies++] = robots[i];
            	}
            	if (jo) sortida+= Clock.getBytecodesLeft() + " ";
            	
            	if (!enCombat) {
            		if (nenemies+nzombies > 0) {
            			enCombat = true;
            			rc.broadcastSignal(visionRange);
            		}
            	}
            	
            	Signal[] sig = rc.emptySignalQueue();
    			for (int i = 0; i < sig.length; ++i) {
    				if (sig[i].getMessage() == null) {
    					if (i == sig.length-1 || (sig[i].getID() != sig[i+1].getID())) { // si no es un senyal doble
    						
    					}
    					else ++i; // no llegir el segon senyal dels dos
    				}
    			}

            									
        		int[] M = {0, 0, 0, 0, 0, 0, 0, 0, 0};
            	int millor;
            	
            	if (rc.isCoreReady()) {
	        		int BC1 = Clock.getBytecodeNum();
            		for (int j = 0; j < robots.length; ++j) {
            			sortida+= "dins " + Clock.getBytecodesLeft() + " ";
            			RobotInfo rob = robots[j];
            			int x = rc.getLocation().x;
            			int y = rc.getLocation().y;
            			int dx = rob.location.x-x;
            			int dy = rob.location.y-y;
            			int offsetDX = dx + 4;
            			int offsetDY = dy + 4;
            			if (rob.team == myTeam) {
            				if (rob.type == RobotType.SOLDIER) {
            					for (int k = 0; k < 9; ++k) {
            						M[k] += aSoldier[taxi[offsetDX][offsetDY][k]];
            					}
            				}
            			}
            			else if (rob.team == enemyTeam) {
            				if (rob.type == RobotType.SOLDIER) {
            					for (int k = 0; k < 9; ++k) {
            						M[k] += eSoldier[taxi[offsetDX][offsetDY][k]];
            					}
            				}
            			}
            			else if (rob.team == Team.ZOMBIE) {
            				if (rob.type == RobotType.STANDARDZOMBIE) {
            					for (int k = 0; k < 9; ++k) {
            						M[k] += sZombie[taxi[offsetDX][offsetDY][k]];
            					}
            				}
            			}
            		}
            			int BC2 = Clock.getBytecodeNum();
    	            	++compt_no;
    	            	if (compt_no < 50) {
    	            		compt_bc += BC2-BC1;
    	            		System.out.printf("\nDiferencia de bc: %d\n", BC2-BC1);
    		            }
    	            	else if (compt_no == 50) System.out.printf("\nMitjana de diferencia de bc: %f\n", (double)compt_bc/compt_no);
    	            	
	            	
	            	millor = 8;
            		for (int i = 0; i < 8; i++) {
            			if (rc.canMove(directions[i])) {
            				if (M[i] > M[millor]) millor = i;
            			}
            		}

            		if (false && jo) {
            			System.out.printf("\n===================== Torn %d =============\n", rc.getRoundNum());
            			System.out.printf("(%d,%d):\n", rc.getLocation().x, rc.getLocation().y);
            			System.out.printf("%d %d %d\n", M[7],M[0],M[1]);
            			System.out.printf("%d %d %d\n", M[6],M[8],M[2]);
            			System.out.printf("%d %d %d\n", M[5],M[4],M[3]);
            		}
                	
            		if (millor < 8) {
            			rc.move(directions[millor]);
            		}
	            	
            	}
        		if (rc.isWeaponReady() && (nzombies > 0 || nenemies > 0)) {
        											if (jo) sortida+="atacar "+ Clock.getBytecodesLeft() + " ";
                	
            		RobotInfo obj = null;
            		int proper = 0, debil = 0, dist = 2*visionRange;
            		double vida = 1000000;
            		for (int i = 0; i < nenemies; ++i) {
            			if (enemies[i].health < vida && rc.canAttackLocation(enemies[i].location)) {
            				vida = enemies[i].health;
            				debil = i;
            			}
            			if (taxista(rc.getLocation(),enemies[i].location) < dist && rc.canAttackLocation(enemies[i].location)) {
            				dist = taxista(rc.getLocation(),enemies[i].location);
            				proper = i;
            			}
            		}
            		if (vida != 1000000) obj = enemies[debil];
            		if (obj == null) {
	            		for (int i = 0; i < nzombies; ++i) {
	            			if (zombies[i].health < vida && rc.canAttackLocation(zombies[i].location)) {
	            				vida = zombies[i].health;
	            				debil = i;
	            			}
	            			if (taxista(rc.getLocation(),zombies[i].location) < dist && rc.canAttackLocation(zombies[i].location)) {
	            				dist = taxista(rc.getLocation(),zombies[i].location);
	            				proper = i;
	            			}
	            		}
	            		if (vida != 1000000) obj = zombies[debil];
            		}
            		if (obj != null) {
            			rc.attackLocation(obj.location);
            		}
            	}
        										if (jo) sortida+="final "+ Clock.getBytecodesLeft() + " ";
        										if (jo) System.out.print(sortida+"\n");
            	
            	/*
                int fate = rand.nextInt(1000);
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
        		//if (torn != rc.getRoundNum()) System.out.print("================Desgraciaaaaaaa===============\n");
                Clock.yield();
            } catch (Exception e) {
                System.out.println(e.getMessage());
                e.printStackTrace();
            }
        }
	}

}
