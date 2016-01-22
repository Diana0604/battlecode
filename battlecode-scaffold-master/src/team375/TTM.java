package team375;

import java.util.LinkedList;
import java.util.Queue;

import battlecode.common.Clock;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.GameConstants;
import battlecode.common.MapLocation;
import battlecode.common.RobotInfo;
import battlecode.common.RobotType;
import battlecode.common.Team;

public class TTM extends RobotPlayer {
		
	private static MapLocation destiny; //TODO This comes from turret
	private static LinkedList<MapLocation> path;
	private static RobotInfo[] nearbyR;
	private static Direction aproxGoodDirection; //TODO this comes from turret
	
	private static boolean pucAnar(MapLocation newNode)
	{
		if(!rc.canSense(newNode)) return false;
		if(rc.senseRubble(newNode) > GameConstants.RUBBLE_OBSTRUCTION_THRESH) return false; //TODO en funcio de la rubble?? hi haura soldats netejant??
		
		return true;
	}
	
	private static void bfs()
	{
		Queue<LinkedList<MapLocation>> q = new LinkedList<LinkedList<MapLocation>>();
		LinkedList<MapLocation> seen = new LinkedList<MapLocation>();
		LinkedList<MapLocation> current = new LinkedList<MapLocation>();
		current.add(rc.getLocation());
		LinkedList<MapLocation> newPath;
		
		for(int i = 0; i < nearbyR.length; ++i)
		{
			if(nearbyR[i].type == RobotType.ARCHON || nearbyR[i].type == RobotType.TURRET || nearbyR[i].type == RobotType.SCOUT || nearbyR[i].team == Team.NEUTRAL) 
				seen.add(nearbyR[i].location);
		}
		
		q.add(current);
		seen.add(current.peek());
		
		while(!q.isEmpty())
		{
			newPath = q.poll();
			MapLocation node = newPath.peekLast();
			if(node.equals(destiny)) { 
				path = newPath; //TODO check well done
				break;
			};
			
			for(int i = 0; i < 8; ++i)
			{
				MapLocation newNode = node.add(directions[i]);
				
				if(pucAnar(newNode) && !seen.contains(newNode))
				{
					seen.add(newNode);
					newPath.addFirst(newNode);
				}
			}
		}
	}
	
	private static void updateDirection() //TODO how
	{
		
	}
	
	public static void playTTM() {
		try {
            //attackRange = rc.getType().attackRadiusSquared;
			
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }

        while (true) {
            // This is a loop to prevent the run() method from returning. Because of the Clock.yield()
            // at the end of it, the loop will iterate once per game round.
        	nearbyR = rc.senseNearbyRobots();
            try {
            		//4. si no la veig, acostar-m'hi
            	if(rc.isCoreReady()) {
            		//1. si estic a la map location, transform
            		if(destiny.equals(rc.getLocation())) rc.unpack();
	            	else {
	            		Boolean hasMoved = false;
	            		
	            		//2. Si ja se com arribar-hi, continuo el cami
	            		if(path != null)
	            		{
	            			Direction dir = rc.getLocation().directionTo(path.peek());
	            			if(rc.canMove(dir)) {
	            				hasMoved = true;
	            				path.poll();
	            				rc.move(dir);
	            			}
	            			else { 
	            				//TODO! que passa si porto molta estona en un mateix lloc??
	            			}
	            		}
	            		
	            		//3. si la puc veure, miro com arribar-hi. Si se arribar-hi em guardo el cami i intento moure'm.
	            		if(!hasMoved && rc.canSense(destiny))
	            		{
	            			bfs();
	            			if(path != null) {
	            				hasMoved = true; // TODO podria fer un boolean de found i un de has moved diferent, de moment aixi (check is ready?)
	            				Direction dir = rc.getLocation().directionTo(path.peekFirst());
	    	            		if(rc.canMove(dir)) {
	    	            			path.pollFirst();
	    	            			rc.move(dir);
	    	            		}
	            			}
	            		}
	            		
	            		//3.1. si la puc veure pero no arribo, provo direccions amb preferencies TODO nomes funciona si a cantonada (actualitzar good direction)
	            		if(!hasMoved)
	            		{
	            			Direction left = aproxGoodDirection.rotateLeft();
	            			Direction right = aproxGoodDirection.rotateRight();
	            			
	            			MapLocation m1 = rc.getLocation().add(left);
	            			MapLocation m2 = rc.getLocation().add(right);
	            			
	            			if(rc.canSense(m1) && !rc.canMove(left))
	            			{
	            				RobotInfo ri = rc.senseRobotAtLocation(m1);
	            				if(ri.team.equals(myTeam) && (ri.type.equals(RobotType.TURRET) || ri.type.equals(RobotType.ARCHON)))
	            				{
	            					if(rc.canMove(right))
	            					{
	            						hasMoved = true;
	            						rc.move(right);
	            					}
	            				}
	            			}
	            			
	            			else if(rc.canSense(m2) && !rc.canMove(right))
	            			{
	            				RobotInfo ri = rc.senseRobotAtLocation(m2);
	            				if(ri.team.equals(myTeam) && (ri.type.equals(RobotType.TURRET) || ri.type.equals(RobotType.ARCHON)))
	            				{
	            					if(rc.canMove(left))
	            					{
	            						hasMoved = true;
	            						rc.move(left);
	            					}
	            				}
	            			}
	            			
	            			if(!hasMoved && rc.canMove(aproxGoodDirection))
	            			{
	            				hasMoved = true;
	            				rc.move(aproxGoodDirection);
	            			}
	            			
	            			for(int i = 1; i < 8; ++i)
	            			{
	            				if(directions[i].equals(aproxGoodDirection) || directions[i].equals(right) || directions[i].equals(left)) continue;
	            				if(rc.canMove(directions[i])) 
	            				{
	            					hasMoved = true;
	            					rc.move(directions[i]);
	            					break;
	            				}
	            			}
	            			
	            			hasMoved = true; //TODO no vull qe faci res si l'esta veient i no es pot moure, per mi que ja podria fer unpack perque vol dir que no arriba
	            		}
	            		
	            		//5. no l'esta veient
	            		if(!hasMoved && rc.canMove(aproxGoodDirection))
	            		{
	            			hasMoved = true;
	            			rc.move(aproxGoodDirection);
	            		}
	            		
	            		if(!hasMoved)
	            		{
	            			if(rc.canMove(aproxGoodDirection.rotateLeft())) rc.move(aproxGoodDirection.rotateLeft());
	            			else if (rc.canMove(aproxGoodDirection.rotateRight())) rc.move(aproxGoodDirection.rotateRight());
	            		}
	            		
	            		if(hasMoved) updateDirection(); //TODO how
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


/* COPIA TAL QUAL DE COM MOU EL PAU ELS ARCHONS (INSPIRATIONAL)
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
*/