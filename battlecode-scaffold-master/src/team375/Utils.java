package team375;

import battlecode.common.*;

public class Utils {
	public static int distInfinit(MapLocation a, MapLocation b){
		return Math.max(Math.abs(a.x-b.x), Math.abs(a.y-b.y));
	}
}
