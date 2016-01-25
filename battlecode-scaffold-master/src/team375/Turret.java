package team375;

import battlecode.common.*;

public class Turret extends RobotPlayer {

	private static final int LEFT = 1;
	private static final int RIGHT = 0;
	private static final int MAXENCALLAT = 5; //per provar
	private static final int FIGHT = 2;
	
	private static boolean hasMoved;
	private static int encallat;
	private static boolean set;
	private static int rotation;
	private static boolean diagonal;
	private static boolean primer;
	
	private static boolean TTM;
	private static MapLocation loc;
	
	private static int pack;
	
	
	//DEF
	private static Direction ref;
	private static int inici;
	private static boolean turretsFound;
	public static boolean dins;

	
	//busca on esta l'archon que l'ha creat
	public static void buscaRef()
	{
		try
		{
			for(Direction dir : directions)
			{
				if(!rc.canSense(loc.add(dir))) continue; 
				RobotInfo ri = rc.senseRobotAtLocation(loc.add(dir));
				if(ri == null) continue;
				if(!ri.type.equals(RobotType.ARCHON)) continue;
				if(!ri.team.equals(myTeam)) continue;
				if(!dir.isDiagonal()) 
				{
					diagonal = false;
					ref = dir.opposite();
					break;
				}
				ref = dir.opposite();
				diagonal = true;
			}
			
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
			//if(left == null) left = Direction.NORTH;
			for(int i = 0; i < 2 && !set; ++i)
			{
				left = left.rotateLeft();
				if(!rc.canSense(loc.add(left))) continue; //TODO de nou, no hauria de passar mai
				RobotInfo ri = rc.senseRobotAtLocation(loc.add(left));
				if(!(ri == null) && ri.team.equals(myTeam) && ri.type.equals(RobotType.TURRET)) continue;
				rotation = LEFT;
				set = true;
				if(rc.isCoreReady() && rc.canMove(left))
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
				right = right.rotateRight();
				if(!rc.canSense(loc.add(right))) continue; //TODO de nou, no hauria de passar mai
				RobotInfo ri = rc.senseRobotAtLocation(loc.add(right));
				if(!(ri == null) && ri.team.equals(myTeam) && ri.type.equals(RobotType.TURRET)) continue;
				rotation = RIGHT;
				set = true;
				if(rc.isCoreReady() && rc.canMove(right))
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
				if(rc.isCoreReady() && rc.canMove(dir))
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
				if(rc.isCoreReady() && rc.canMove(dir))
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
	
	public static MapLocation readSignals()
	{
		Signal signals[] = rc.emptySignalQueue();
		//ArrayList<Integer> seen = new ArrayList<Integer>();
		MapLocation enemy = null;
		for(Signal s : signals)
		{
			if(s.getTeam() != myTeam) continue;
			if(s.getMessage() == null) 
			{
				MapLocation driver = s.getLocation();
				if(loc.isAdjacentTo(driver))
				{
					Direction d = loc.directionTo(driver);
					if(d.isDiagonal())
					{
						TTM = true;
						diagonal = true;
						dins = false;
						ref = d.opposite();
						inici = -1;
						try {
							rc.pack();
						} catch (GameActionException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						return null;
					}
				}
				continue;
			}
			if(TTM) continue;
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
            set = false;
            encallat = 0;
            primer = true;
            TTM = false;
            inici  = 3;
            //ref = Direction.NORTH;
            pack = 0;
            dins = true;
            turretsFound = false;
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }

        while (true) {
        	loc = rc.getLocation();
        	hasMoved = false;
            // This is a loop to prevent the run() method from returning. Because of the Clock.yield()
            // at the end of it, the loop will iterate once per game round.
            try {
                if(TTM && rc.isCoreReady())
            	{
            		if(primer)
            		{
            			primer = false;
            			buscaRef();
            			if(ref == null)
            			{
            				hasMoved = true;
            				TTM = false;
            				rc.unpack();
            			}
            		}
            		
            		if(dins)
            		{
	            		if(!hasMoved && !diagonal && inici > 0)
	            		{
	            			int turrets = 0;
	            			Direction t1 = null;
	            			Direction t2 = null;
	            			for(Direction d : directions)
	            			{
	            				if(d.isDiagonal()) continue;
	            				if(rc.canSense(loc.add(d)))
	            				{
	            					RobotInfo ri = rc.senseRobotAtLocation(loc.add(d));
	            					if(ri != null && ri.team.equals(myTeam) && ri.type.equals(RobotType.TURRET))
	            					{
	            						if(turrets == 0) t1 = d;
	            						else t2 = d;
	            						++turrets;
	            						
	            					}
	            				}
	            			}
	            			
	            			if(turrets == 2)
	            			{
	            				if(t1.equals(t2.rotateLeft().rotateLeft()))
	            				{
	            					ref = t2.rotateLeft();
	            					diagonal = true;
	            					inici = 1;
	            				}
	            				if(t1.equals(t2.rotateRight().rotateRight()))
	            				{
	            					ref = t2.rotateRight();
	            					diagonal = true;
	            					inici = 1;
	            				}
	            			}
	            		}
	            		
	            		if(diagonal && inici == 0)
	            		{
	            			dins = false;
	            		}
	            		
	            		if(!hasMoved && (inici > 0)) //TODO treure diagonal (prova)
	            		{
	            			if(rc.canMove(ref)) 
	            			{
	            				hasMoved = true;
	            				--inici;
	            				encallat = 0;
	            				rc.move(ref);
	            			}
	            		}
	            		
	            		
	            		
	            		if(diagonal && inici > 0 && !hasMoved)
	            		{
	            			rc.broadcastSignal(2);
	            		}
	            		
	            		if(!hasMoved && inici > 0)
	            		{
	            			if(!diagonal)
	            			{
	            				if(rc.canMove(ref.rotateLeft()))
	            				{
	            					hasMoved = true;
	            					--inici;
	            					encallat = 0;
	            					rc.move(ref.rotateLeft());
	            					
	            				}
	            				//MapLocation m = loc.add(ref.rotateLeft());
	            				if(!hasMoved && rc.canMove(ref.rotateRight()))
	            				{
	            					hasMoved = true;
	            					--inici;
	            					encallat = 0;
	            					rc.move(ref.rotateRight());
	            					
	            				}
	            			}
	            		}
            		
	            		if(!hasMoved && !diagonal && inici == 0)
	            		{
	            			TTM = false;
	            			hasMoved = true;
	            			rc.unpack();
	            		}
            		
	            		if(!diagonal && !hasMoved && inici > 0 && turretsFound) 
	            		{
	            			if(rc.canMove(ref.rotateLeft().rotateLeft()))
	            			{
	            				ref = ref.rotateLeft().rotateLeft();
	            			}
	            			
	            			else
	                		{
	                			if(rc.canMove(ref.rotateRight().rotateRight()))
	                			{
	                				ref = ref.rotateRight().rotateRight();
	                			}
	                		}
	            			inici = -1;
	            		}
	            		
	            		if(!diagonal && !hasMoved && inici > 0 && !turretsFound)  //TODO AQUI PERDO UN TORN
	            		{
	            			if(rc.canMove(ref.rotateLeft().rotateLeft()))
	            			{
	            				ref = ref.rotateLeft().rotateLeft();
	            				inici = 5;
	            			}
	            			
	            			else
	                		{
	                			if(rc.canMove(ref.rotateRight().rotateRight()))
	                			{
	                				ref = ref.rotateRight().rotateRight();
	                				inici = 5;
	                			}
	                		}
	            			turretsFound = true;
	            		}
            		
            		
	            		if(inici == -1) 
	            		{
	            			if(!rc.onTheMap(loc.add(ref))) ref = ref.opposite();
	            			inici = 5; //TODO no es mou ara 
	            		}
	            		
	            		if(!hasMoved && !diagonal && inici == -1)
	            		{
	            			if(rc.canMove(ref))
	            			{
	            				hasMoved = true;
	            				encallat = 0;
	            				rc.move(ref);
	            			}
	            		}
            		}
            		
            		else 
            		{
            			rc.setIndicatorString(0, "ESTIC FORA");
            			System.out.println("ALGU ESTA FORAAAAAAAAAAAAAAAAAAAAAAAAAAAAA");
            			if(diagonal)
            			{
            				Direction dir = ref.rotateLeft().rotateLeft().rotateLeft();
            				if(rc.canMove(dir)) 
            				{
            					diagonal = false;
            					hasMoved = true;
            					ref = dir;
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
                					rc.move(dir);
                				}
            				}
            				
            				if(!hasMoved)
            				{
            					if(rc.canMove(ref)) rc.move(ref);
            				}
            			}
            			
            			if(!diagonal)
            			{
            				if(rc.canMove(ref))
            				{
            					rc.move(ref);
            				}
            				else
            				{
            					TTM = false;
            					rc.unpack();
            				}
            			}
            		}
            		
            		
            	}
                if(!TTM)
	            {
	           		if(primer) 
	           		{
	           			++pack;
	           			TTM = true;
	           			rc.pack();
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