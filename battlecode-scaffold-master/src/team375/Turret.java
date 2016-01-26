package team375;

import battlecode.common.*;

public class Turret extends RobotPlayer {

	//GENERAL
	
	//TTM
	
	//TURRET
	private static final int MAXENCALLAT = 8; //per provar
	
	private static boolean hasMoved;
	private static int encallat;
	private static boolean diagonal;
	private static boolean primer;
	
	private static boolean TTM;
	private static MapLocation loc;
	
	private static Direction ref;
	private static int inici;
	private static boolean dins;
	static MapLocation Corner;
	static boolean d1;
	
	public static final int LEFT = 0;
	public static final int RIGHT = 1;
	public static int ori;
	//busca on esta l'archon que l'ha creat
	public static void buscaRef()
	{
		try
		{
			Direction hor, ver;
			if (!rc.onTheMap(rc.getLocation().add(Direction.NORTH, 3))){
				ver = Direction.NORTH;
			}else if (!rc.onTheMap(rc.getLocation().add(Direction.SOUTH, 3))){
				ver = Direction.SOUTH;
			}else {
				ver = null;
				//System.out.println("El scout no pot veure la cantonada erreur");
			}
			if (!rc.onTheMap(rc.getLocation().add(Direction.EAST, 3))){
				hor = Direction.EAST;
			}else if (!rc.onTheMap(rc.getLocation().add(Direction.WEST, 3))){
				hor = Direction.WEST;
			}else {
				hor = null;
				//System.out.println("El scout no pot veure la cantonada erreur");
			}
			
			ref = Utils.addDirections(hor, ver);
			if(ref != null)
			{
				if (!rc.onTheMap(rc.getLocation().add(ref.rotateLeft(), 3))){
					if (!rc.onTheMap(rc.getLocation().add(ref.rotateRight(), 3))){
						int xmax = 3, ymax = 3;
						while (!rc.onTheMap(rc.getLocation().add(ref.rotateLeft(), xmax--)));
						while (!rc.onTheMap(rc.getLocation().add(ref.rotateRight(), ymax--)));
						Corner = rc.getLocation().add(ref.rotateLeft(), xmax+1).add(ref.rotateRight(), ymax + 1);
						}
				}
			}
			
			
			if(Corner != null)
			{
				if(loc.x - loc.y == Corner.x - Corner.y)
				{
					d1 = true;
					diagonal = true;
				}
				if (loc.x + loc.y == Corner.x + Corner.y) 
				{
					diagonal = true;
				}
				if(ref == Direction.NORTH_WEST || ref == Direction.SOUTH_EAST) d1 = true;
				if(ref == Direction.NORTH_EAST || ref == Direction.SOUTH_WEST) d1 = false;
				ref = ref.opposite();
			}
		
			
		} catch(GameActionException e)
		{
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
	}
	public static MapLocation readSignals()
	{
		Signal signals[] = rc.emptySignalQueue();
		//ArrayList<Integer> seen = new ArrayList<Integer>();
		MapLocation enemy = null;
		for(Signal s : signals)
		{
			if(s.getTeam() != myTeam) continue;
			if(s.getMessage() == null) continue;
			//if(seen.contains(s.getID())) continue;
			int[] coded = s.getMessage();
			Message m = new Message(s.getLocation(), coded[0], coded[1]);
			int mode = m.getMode();
			if(mode != Message.SHOOT) continue;
			int x = m.getX();
			int y = m.getY();
			MapLocation e = new MapLocation(x,y);
			if(rc.canAttackLocation(e)) 
			{
				enemy = new MapLocation(x, y);
				//seen.add(s.getID());
			}
		}
		//if(seen.isEmpty()) return null;
		//Signal[] possible = (Signal[]) .toArray();
		return enemy;
	}
	
	public static void playTurret() {
		try {
            attackRange = rc.getType().attackRadiusSquared;
            encallat = 0;
            primer = true;
            TTM = false;
            inici  = 2;
            dins = true;
            diagonal = false;
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }

        while (true) {
        	loc = rc.getLocation();
        	if(rc.getTeam().equals(Team.A)) rc.setIndicatorString(0, "holi");
        	hasMoved = false;
            // This is a loop to prevent the run() method from returning. Because of the Clock.yield()
            // at the end of it, the loop will iterate once per game round.
            try {
                if(TTM && rc.isCoreReady())
            	{
            		if(dins)
            		{
            			if(!diagonal)
            			{
            				if(!hasMoved && d1)
            				{
            					if(loc.y - loc.x > Corner.y - Corner.x)
            					{
            						MapLocation m = new MapLocation(loc.x + 1, loc.y);
            						Direction dir = loc.directionTo(m);
            						if(!hasMoved && rc.canMove(dir))
            						{
            							hasMoved = true;
            							rc.move(dir);
            						}
            						if(!hasMoved)
            						{
            							m = new MapLocation(loc.x, loc.y - 1);
                						dir = loc.directionTo(m);
                						if(rc.canMove(dir))
                						{
                							hasMoved = true;
                							rc.move(dir);
                						}
            						}
            					}
            					
            					if(loc.y - loc.x <  Corner.y - Corner.x)
            					{
            						MapLocation m = new MapLocation(loc.x - 1, loc.y);
            						Direction dir = loc.directionTo(m);
            						if(!hasMoved && rc.canMove(dir))
            						{
            							hasMoved = true;
            							rc.move(dir);
            						}
            						
            						if(!hasMoved)
            						{
            							m = new MapLocation(loc.x, loc.y + 1);
            							dir = loc.directionTo(m);
            							if(rc.canMove(dir))
            							{
            								hasMoved = true;
            								rc.move(dir);
            							}
            						}
            					}
            					
            					if(loc.x - loc.y == Corner.x - Corner.y) diagonal = true;
            				}
            				
            				if(!hasMoved && !d1)
            				{
            					if(loc.x + loc.y > Corner.x + Corner.y)
            					{
            						MapLocation m = new MapLocation(loc.x - 1, loc.y);
            						Direction dir = loc.directionTo(m);
            						if(!hasMoved && rc.canMove(dir))
            						{
            							hasMoved = true;
            							rc.move(dir);
            						}
            						
            						if(!hasMoved)
            						{
            							m = new MapLocation(loc.x, loc.y - 1);
            							dir = loc.directionTo(m);
                						if(rc.canMove(dir))
                						{
                							hasMoved = true;
                							rc.move(dir);
                						}
            						}
            					}
            					
            					if(loc.x + loc.y < Corner.x + Corner.y)
            					{
            						MapLocation m = new MapLocation(loc.x + 1, loc.y);
            						Direction dir = loc.directionTo(m);
            						if(!hasMoved && rc.canMove(dir))
            						{
            							hasMoved = true;
            							rc.move(dir);
            						}
            						
            						if(!hasMoved)
            						{
            							m = new MapLocation(loc.x, loc.y + 1);
            							dir = loc.directionTo(m);
                						if(rc.canMove(dir))
                						{
                							hasMoved = true;
                							rc.move(dir);
                						}
            						}
            					}
            					
            					if(loc.x + loc.y == Corner.x + Corner.y) diagonal = true;
            				}
            			}
            			
            			if(!hasMoved)
            			{
            				//if(inici > 0)
            				{
            					if(rc.canMove(ref))
            					{
            						//--inici;
            						//if(!diagonal) --inici;
            						//if(inici <= 0) 
            						if(diagonal && loc.distanceSquaredTo(Corner) >= 3)
            						{
            							dins = false;
            						}
            						hasMoved = true;
            						rc.move(ref);
            					}
            				}
            			}
            			
            			if(!hasMoved)
            			{
            				if(rc.canMove(ref.rotateRight())) 
            				{
            					hasMoved = true;
            					rc.move(ref.rotateRight());
            				}
            				else 
            				{
            					
            					if (rc.canMove(ref.rotateLeft())) 
            					{
            						hasMoved = true;
            						rc.move(ref.rotateLeft());
            					}
            				}
            			}
            		}
            		
            		else 
            		
            		{
            			if(diagonal && !hasMoved)
            			{
            				int fate = rand.nextInt(1000);
            				if(fate%2 == 0)
            				{
	            				Direction dir = ref.rotateLeft().rotateLeft().rotateLeft();
	            				if(rc.canMove(dir)) 
	            				{
	            					diagonal = false;
	            					hasMoved = true;
	            					ref = dir;
	            					ori = LEFT;
	            					rc.move(dir);
	            					
	            				}
	            				if(!hasMoved)
	            				{
	            					dir = ref.rotateRight().rotateRight().rotateRight();
	                				if(rc.canMove(dir)) 
	                				{
	                					ref = dir;
	                					diagonal = false;
	                					hasMoved = true;
	                					ori = RIGHT;
	                					rc.move(dir);
	                				}
	            				}
            				}
            				else
            				{
            					Direction dir = ref.rotateRight().rotateRight().rotateRight();
	            				if(rc.canMove(dir)) 
	            				{
	            					diagonal = false;
	            					hasMoved = true;
	            					ref = dir;
	            					rc.move(dir);
	            					ori = RIGHT;
	            				}
	            				if(!hasMoved)
	            				{
	            					dir = ref.rotateLeft().rotateLeft().rotateLeft();
	                				if(rc.canMove(dir)) 
	                				{
	                					ref = dir;
	                					diagonal = false;
	                					hasMoved = true;
	                					ori = LEFT;
	                					rc.move(dir);
	                				}
	            				}
            				}
            				if(!hasMoved)
            				{
            					if(rc.canMove(ref)) rc.move(ref);
            				}
            			}
            			
            			
            			if(!diagonal && !hasMoved)
            			{
            				int x = loc.x - Corner.x;
            				int y = loc.y - Corner.y;
            				if(x < 0) x = -x;
            				if(y < 0) y = -y;
            				if(ori == LEFT)
            				{
            					MapLocation m = loc.add(ref.rotateLeft());
            					
            					if(rc.canSenseLocation(m) && ((!d1 && y > 3) || (d1 && x > 3)))
            					{
            						if(rc.canMove(ref.rotateLeft()))
            						{
            							hasMoved = true;
            							rc.move(ref.rotateLeft());
            						}
            					}
            				}
            				else
            				{
            					MapLocation m = loc.add(ref.rotateRight());
            					if(rc.canSenseLocation(m) && (!d1 && x > 3) || (d1 && y > 3))
            					{
            						if(rc.canMove(ref.rotateRight()))
            						{
            							hasMoved = true;
            							rc.move(ref.rotateRight());
            						}
            					}
            				}
            				
            				
            				if(!hasMoved && rc.canMove(ref))
            				{
            					hasMoved = true;
            					rc.move(ref);
            				}
            				else
            				{
            					if(!hasMoved && rc.canSenseLocation(loc.add(ref)))
            					{
            						RobotInfo ri = rc.senseRobotAtLocation(loc.add(ref));
            						if(ri != null && ri.type.equals(RobotType.TURRET) && ri.team.equals(myTeam))
            						{
            							TTM = false;
            							hasMoved = true;
                    					rc.unpack();
            						}
            					} 
            					if(!hasMoved && !rc.onTheMap(loc.add(ref)))
            					{
            						TTM = false;
            						hasMoved = true;
                					rc.unpack();
            					}
            					
            					if(!hasMoved && encallat > MAXENCALLAT)
            					{
            						TTM = false;
            						hasMoved = true;
                					rc.unpack();
            					}
            					
            					if(!hasMoved) ++encallat;
            					
            				}
            			}
            			
            		}
            		
            		
            	}
                if(!TTM)
	            {
	           		if(primer) 
	           		{
	           			buscaRef();
	           			if(Corner != null)
	           			{
	           				TTM = true;
	           				rc.pack();
	           			}
	           			primer = false;
	           		}
	           		else 
	           		{
	           			MapLocation objective = readSignals();
	           			if(objective != null)
	           			{
	           				if(rc.isWeaponReady()) rc.attackLocation(objective);
	           			}
	           			else
	           			{
		           			if (!TTM && rc.isWeaponReady()) {
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
	           }
                Clock.yield();
            } catch (Exception e) {
                System.out.println(e.getMessage());
                e.printStackTrace();
            }
        }
	}
}