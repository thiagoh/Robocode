package com.thiagoh.robocode;

public enum Direction {

	NORTH, EAST, SOUTH, WEST,

	NORTH_EAST, SOUTH_EAST,

	NORTH_WEST, SOUTH_WEST;

	public final static Direction[] TO_SOUTH = { SOUTH, SOUTH_EAST, SOUTH_WEST };

	public final static Direction[] TO_NORTH = { NORTH, NORTH_EAST, NORTH_WEST };

	public final static Direction[] TO_WEST = { WEST, NORTH_WEST, SOUTH_WEST };

	public final static Direction[] TO_EAST = { EAST, NORTH_EAST, SOUTH_EAST };

	public Direction invert() {

		if (this == NORTH)
			return SOUTH;
		else if (this == NORTH_EAST)
			return SOUTH_WEST;
		else if (this == NORTH_WEST)
			return SOUTH_EAST;
		else if (this == SOUTH)
			return NORTH;
		else if (this == SOUTH_EAST)
			return NORTH_WEST;
		else if (this == SOUTH_WEST)
			return NORTH_EAST;
		else if (this == EAST)
			return WEST;
		else
			return EAST;
	}
}
