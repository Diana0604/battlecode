package team375;

import battlecode.common.Clock;
import battlecode.common.Direction;
import battlecode.common.GameConstants;
import battlecode.common.MapLocation;
import battlecode.common.RobotInfo;
import battlecode.common.RobotType;
import battlecode.common.Team;

public class Turret extends RobotPlayer {

	private static final int LEFT = 1;
	private static final int RIGHT = 0;
	private static final int MAXENCALLAT = 5; //per provar
	private static final int FIGHT = 2;
	
	private static int turretsFound;
	private static boolean hasMoved;
	private static int encallat;
	private static boolean set;
	private static int rotation;
	private static boolean diagonal;
	private static boolean primer;
	private static Direction ref;
	private static boolean TTM;
	private static MapLocation loc;
	
	public static void playTurret() {
		try {
            attackRange = rc.getType().attackRadiusSquared;
            loc = rc.getLocation();
            set = false;
            TTM = false;
            encallat = 0;
            primer = true;
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }

        while (true) {
            // This is a loop to prevent the run() method from returning. Because of the Clock.yield()
            // at the end of it, the loop will iterate once per game round.
            try {
                // If this robot type can attack, check for enemies within range and attack one
            	
            	if(TTM)
            	{
            		if(rc.isCoreReady())
            		{
            			hasMoved = false;
            		
	            		if(primer) 
	            		{
	            			primer = false;
	            			diagonal = true;
	            			for(Direction dir : directions)
	            			{
	            				if(!dir.isDiagonal()) continue;
	            				if(!rc.canSense(loc.add(dir))) continue;
	            				RobotInfo ri = rc.senseRobotAtLocation(loc.add(dir));
	            				if(ri.equals(null)) continue;
	            				if(!ri.team.equals(myTeam)) continue;
	            				if(!ri.type.equals(RobotType.ARCHON)) continue;
	            				if(ri.team.equals(myTeam) && ri.type.equals(RobotType.ARCHON))
	            				{
	            					ref = dir;
	            					break;
	            				}
	            			}
	            			
	            			if(ref.equals(null)) ref = Direction.NORTH; //TODO parche per neutrals
	            		} //TODO en ccas que per algo no estigui a la diagonal, fer algo 
	            		
	            		if(diagonal)
	            		{
	            			Direction left = ref.rotateLeft();
	            			
	            			if(!set)
	            			{
	            				for(int i = 0; i < 2 && !set; ++i)
	            				{
			            			if(rc.canSense(loc.add(left)))
			            			{
			            				RobotInfo ri = rc.senseRobotAtLocation(loc.add(left));
			            				if(ri.equals(null) || !ri.team.equals(myTeam) || !ri.type.equals(RobotType.TURRET)) 
			            				{
			            					rotation = LEFT;
			            					set = true;
			            					break;
			            				}
			            			}
			            			left = left.rotateLeft();
	            				}
	            			}
	            			
	            			if(!set)
	            			{
	            				Direction right = ref.rotateRight();
	            				
	            				for(int i = 0; i < 2 && !set; ++i)
	            				{
		            				if(rc.canSense(loc.add(right))) 
		            				{
		            					RobotInfo ri = rc.senseRobotAtLocation(loc.add(right));
		            					if(ri.equals(null) || !ri.team.equals(myTeam) || !ri.type.equals(RobotType.TURRET)) 
		                				{
		                					rotation = RIGHT;
		                					set = true;
		                					break;
		                				}
		            					right = right.rotateRight();
		            				}
	            				}
	            			}
	            			
	            			if(!set)
	            			{
	            				if(rc.canMove(ref.opposite())) 
	            				{
	            					hasMoved = true;
	            					encallat = 0;
	            					rc.move(ref.opposite());
	            				}
	            				else ++encallat;
	            			}
	            			
	            			//TODO si arriba a MAXENCALLAT a la diagonal liada parda perque crea embus :S
	            			
	            			if(set)
	            			{
	            				if(rotation == LEFT)
	            				{
	            					Direction dir = ref.rotateLeft();
	            					for(int i = 0; i < 3 && !hasMoved; ++i)
	            					{
		            					if(rc.canMove(dir))
		            					{
		            						encallat = 0;
		            						diagonal = false;
		            						hasMoved = true;
		            						rc.move(dir);
		            						break;
		            					} 
		            					//aqui es podria pensar que potser un robot no es mou a un lloc perque en aquell moment hi ha rubble podria esperar i 
		            					//anarhi despres.
		            					//pero en realitat si no s'hi mou i es mou mes enlla, despres una altra turret ja hi arribara TODO comentar
		            					dir = dir.rotateLeft();
	            					}
	            				}
	            				
	            				if(rotation == RIGHT)
	            				{
	            					Direction dir = ref.rotateRight();
	            					for(int i = 0; i < 3 && !hasMoved; ++i)
	            					{
		            					if(rc.canMove(dir))
		            					{
		            						encallat = 0;
		            						diagonal = false;
		            						hasMoved = true;
		            						rc.move(dir);
		            						break;
		            					}
		            					dir = dir.rotateRight();
	            					}
	            				}
	            				
	            				if(!hasMoved) 
	            				{ 
	            					++encallat;
		            				if(encallat > MAXENCALLAT) 
		            				{
		            					if(rc.canMove(ref.opposite())) 
		            					{
		            						rc.move(ref.opposite()); //en plan si fa MOLT que estic alla no val la pena seguir-ho intentant
		            						encallat = 0;
		                					set = false;
		                					hasMoved = true;
		            					}
		            					else {}; //TODO aqui hauriem de fer algo, tot i que es una situacio chunga, ja pensare
		            				}
	            				}
	            			}
	            		}
	            		
	            		if(!diagonal)
	            		{
	            			turretsFound = 0;
	            			
	            			if(rc.canMove(ref))
	            			{
	            				hasMoved = true;
	            				encallat = 0;
	            				rc.move(ref);
	            			}
	            			
	            			if(!hasMoved)
	            			{
	            				if(rc.canSenseLocation(loc.add(ref)))
	            				{
	            					RobotInfo ri = rc.senseRobotAtLocation(loc.add(ref));
	            					if(!ri.equals(null) && ri.team.equals(myTeam) && (ri.type.equals(RobotType.TURRET) || ri.type.equals(RobotType.ARCHON)))
	            					{
	            						++turretsFound;
	            					}
	            					else
	            					{
	            						if(rc.canMove(ref))
	            						{
	            							hasMoved = true;
	            							encallat = 0;
	            							rc.move(ref);
	            						}
	            					}
	            				}
	            				else ++turretsFound;
	            			}
	            			
	            			if(!hasMoved)
	            			{
	            				Direction dir;
	            				if(rotation == RIGHT) dir = ref.rotateRight();
	            				else dir = ref.rotateLeft();
	            				
	            				if(rc.canSenseLocation(loc.add(dir)))
	            				{
	            					RobotInfo ri = rc.senseRobotAtLocation(loc.add(dir));
	            					if(!ri.equals(null) && ri.team.equals(myTeam) && (ri.type.equals(RobotType.TURRET) || ri.type.equals(RobotType.SCOUT)))
	            					{
	            						++turretsFound;
	            					}
	            					else
	            					{
	            						if(rc.canMove(dir))
	            						{
	            							hasMoved = true;
	            							encallat = 0;
	            							rc.move(dir);
	            						}
	            					}
	            				}
	            				else ++turretsFound;
	            			}
	            			
	            			if(!hasMoved)
	            			{
	            				if(turretsFound >= FIGHT)
	            				{
	            					hasMoved = true;
	            					encallat = 0;
	            					TTM = false;
	            					rc.unpack();
	            				}
	            			}
	            			
	            			if(!hasMoved)
	            			{
	            				++encallat;
	            				if(encallat > MAXENCALLAT)
	            				{
	            					hasMoved = true;
	            					encallat = 0;
	            					TTM = false;
	            					rc.unpack();
	            				}
	            			}
	            		}
            		}
            	}
            	
	            	else 
	            	{
	            		if(primer) 
	            		{
	            			TTM = true;
	            			rc.pack();
	            		}
	            		else 
	            		{
	            			if (rc.isWeaponReady()) {
		                    RobotInfo[] enemiesWithinRange = rc.senseNearbyRobots(attackRange, enemyTeam);
		                    RobotInfo[] zombiesWithinRange = rc.senseNearbyRobots(attackRange, Team.ZOMBIE);
		                    if (enemiesWithinRange.length > 0) {
		                        for (RobotInfo enemy : enemiesWithinRange) {
		                            // Check whether the enemy is in a valid attack range (turrets have a minimum range)
		                            if (rc.canAttackLocation(enemy.location)) {
		                                rc.attackLocation(enemy.location);
		                                break;
		                            }
		                        }
		                    } else if (zombiesWithinRange.length > 0) {
		                        for (RobotInfo zombie : zombiesWithinRange) {
		                            if (rc.canAttackLocation(zombie.location)) {
		                                rc.attackLocation(zombie.location);
		                                break;
		                            }
		                        }
		                    }
	            		}
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
