package team375;

import battlecode.common.*;

public class Utils {
	public static int distInfinit(MapLocation a, MapLocation b){
		return Math.max(Math.abs(a.x-b.x), Math.abs(a.y-b.y));
	}
	 
	//Suma dues direccions si una es horitzontal i l'altra vertical. Si no es compleix aixo, retorna null
	//ES MOLT CUTRE HO SE SORRY
	public static Direction addDirections(Direction a, Direction b){
		if (a == null || b == null){
			//System.out.println("Error, intentes sumar direccions nuls");
			return null;
		}
		if (a.isDiagonal() || b.isDiagonal()){
			System.out.println("Error, intentes sumar direccions diagonals");
			return null;
		}
		if (a == Direction.NORTH){
			if (b == Direction.EAST) return Direction.NORTH_EAST;
			if (b == Direction.WEST) return Direction.NORTH_WEST;
			else return null;
		}
		if (a == Direction.SOUTH){
			if (b == Direction.EAST) return Direction.SOUTH_EAST;
			if (b == Direction.WEST) return Direction.SOUTH_WEST;
			else return null;
		}
		if (a == Direction.EAST){
			if (b == Direction.NORTH) return Direction.NORTH_EAST;
			if (b == Direction.SOUTH) return Direction.SOUTH_EAST;
			else return null;
		}
		if (a == Direction.WEST){
			if (b == Direction.NORTH) return Direction.NORTH_WEST;
			if (b == Direction.SOUTH) return Direction.SOUTH_WEST;
			else return null;
		}
		return null;
	}
}
