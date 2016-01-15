package team375;

import battlecode.common.MapLocation;

public class Utils {
	
	//al final no cal ferla servir, hi ha una d'igual que es loc1.distanceSquaredTo(loc2)
	public static int squareDist(MapLocation loc1, MapLocation loc2){
    	return (loc1.x - loc2.x)*(loc1.x-loc2.x) + (loc1.y-loc2.y)*(loc1.y-loc2.y);
    }
}
