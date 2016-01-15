package team375;

import battlecode.common.MapLocation;

public class Utils {
	public static int squareDist(MapLocation loc1, MapLocation loc2){
    	return (loc1.x - loc2.x)*(loc1.x-loc2.x) + (loc1.y-loc2.y)*(loc1.y-loc2.y);
    }
}
