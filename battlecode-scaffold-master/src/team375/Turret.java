package team375;

import battlecode.common.Clock;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
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
	
	//busca on esta l'archon que l'ha creat
	public static void buscaRef()
	{
		try
		{
			for(Direction dir : directions)
			{
				if(!dir.isDiagonal()) continue;
				if(!rc.canSense(loc.add(dir))) continue; 
				//TODO en principi les hauria de sense totes, i de fet aquest es un cas especial que segurament no s'ha construit a la diagonal. Pensar que fer
				RobotInfo ri = rc.senseRobotAtLocation(loc.add(dir));
				if(ri.equals(null)) continue;
				if(!ri.type.equals(RobotType.ARCHON)) continue;
				if(!ri.team.equals(myTeam)) continue;
				ref = dir;
				break;
			}
			
			if(ref.equals(null)) ref = Direction.NORTH; //TODO parche per neutrals
			
		} catch(GameActionException e)
		{
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
	}

	//busca si s'ha de moure a dreta/esquerra
	public static void tryLeft()
	{
		try 
		{
			Direction left = ref;
			
			for(int i = 0; i < 2 && !set; ++i)
			{
				left.rotateLeft();
				if(!rc.canSense(loc.add(left))) continue; //TODO de nou, no hauria de passar mai
				RobotInfo ri = rc.senseRobotAtLocation(loc.add(left));
				if(!ri.type.equals(null) && ri.team.equals(myTeam) && ri.type.equals(RobotType.TURRET)) continue;
				rotation = LEFT;
				set = true;
				if(rc.canMove(left))
				{
					hasMoved = true;
					diagonal = false;
					rc.move(left); //TODO afegir que provi si li'n falten o massa lio? (despres al set ho fara, puc fer si no que d'alguna manera li digui al 
					//primer set que comenci a provar en right/left
				}
				break;
			}
		} catch(GameActionException e)
		{
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
	}
	public static void tryRight()
	{
		try 
		{
			Direction right = ref;
			
			for(int i = 0; i < 2 && !set; ++i)
			{
				right.rotateRight();
				if(!rc.canSense(loc.add(right))) continue; //TODO de nou, no hauria de passar mai
				RobotInfo ri = rc.senseRobotAtLocation(loc.add(right));
				if(!ri.type.equals(null) && ri.team.equals(myTeam) && ri.type.equals(RobotType.TURRET)) continue;
				rotation = RIGHT;
				set = true;
				if(rc.canMove(right))
				{
					hasMoved = true;
					diagonal = false;
					rc.move(right); //TODO afegir que provi si li'n falten o massa lio? (despres al set ho fara, puc fer si no que d'alguna manera li digui al 
					//primer set que comenci a provar en right/left
				}
				break;
			}
		} catch(GameActionException e)
		{
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
	}
	
	//intenta sortir de la diagonal
	public static void goLeft()
	{
		try
		{
			Direction dir = ref;
			for(int i = 0; i < 2; ++i)
			{
				dir = dir.rotateLeft();
				if(rc.canMove(dir))
				{
					encallat = 0;
					diagonal = false;
					hasMoved = true;
					rc.move(dir);
					break;
				}
			}
			//aqui es podria pensar que potser un robot no es mou a un lloc perque en aquell moment hi ha rubble podria esperar i 
			//anarhi despres.
			//pero en realitat si no s'hi mou i es mou mes enlla, despres una altra turret ja hi arribara TODO comentar
			
			if(!hasMoved) ++encallat;
			
		} catch(Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
	}
	public static void goRight()
	{
		try
		{
			Direction dir = ref;
			for(int i = 0; i < 2; ++i)
			{
				dir = dir.rotateRight();
				if(rc.canMove(dir))
				{
					encallat = 0;
					diagonal = false;
					hasMoved = true;
					rc.move(dir);
					break;
				}
			}
			//aqui es podria pensar que potser un robot no es mou a un lloc perque en aquell moment hi ha rubble podria esperar i 
			//anarhi despres.
			//pero en realitat si no s'hi mou i es mou mes enlla, despres una altra turret ja hi arribara TODO comentar
			
			if(!hasMoved) ++encallat;
			
		} catch(Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
	}
	
	public static void playTurret() {
		try {
            attackRange = rc.getType().attackRadiusSquared;
            set = false;
            encallat = 0;
            primer = true;
            TTM = false;
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }

        while (true) {
        	loc = rc.getLocation();
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
	            			buscaRef();
	            		} //TODO en ccas que per algo no estigui a la diagonal, fer algo 
	            		
	            		if(diagonal)
	            		{
	            			if(!set) tryLeft();
	            			if(!set) tryRight();
	            			
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
	            				if(rotation == LEFT) goLeft();
	            				if(rotation == RIGHT) goRight();
	            				
	            				if(encallat > MAXENCALLAT)
	            				{
	            					if(rc.canMove(ref.opposite()))
	            					{
	            						encallat = 0;
	            						set = false;
	            						hasMoved = true;
	            						rc.move(ref.opposite()); //si fa MOLT que estic alla no val la pena seguir-ho intentant
	            					}
	            					
	            					if(!hasMoved) //aixo voldria dir que la turret no s'ha construit quan tocava o algo aixi
	            					{
	            						TTM = false;
	            						rc.unpack();
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
	            				}
	            				else ++turretsFound;
	            			}
	            			
	            			Direction dir = null;
	            			
	            			if(!hasMoved)
	            			{
	            				if(rotation == RIGHT) dir = ref.rotateRight();
	            				else dir = ref.rotateLeft();
	            				if(rc.canMove(dir))
	            				{
	            					hasMoved = true; 
	            					encallat = 0;
	            					rc.move(dir);
	            				}
	            			}
	            			
	            			if(!hasMoved)
	            			{
	            				if(rc.canSenseLocation(loc.add(dir)))
	            				{
	            					RobotInfo ri = rc.senseRobotAtLocation(loc.add(dir));
	            					if(!ri.equals(null) && ri.team.equals(myTeam) && (ri.type.equals(RobotType.TURRET) || ri.type.equals(RobotType.SCOUT)))
	            					{
	            						++turretsFound;
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

