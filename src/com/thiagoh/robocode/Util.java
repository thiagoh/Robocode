package com.thiagoh.robocode;

import robocode.Robot;

public class Util {

	public static Direction escapeFromWall(ImRobot robot) {

		Axis[] nearAxis = robot.getNearestAxis();

		Direction xTurnTo = nearAxis[0] == Axis.RIGHT ? Direction.WEST : Direction.EAST;
		Direction yTurnTo = nearAxis[1] == Axis.UP ? Direction.SOUTH : Direction.NORTH;
		Direction turnTo = null;

		if (yTurnTo == Direction.NORTH)
			turnTo = xTurnTo == Direction.WEST ? Direction.NORTH_WEST : Direction.NORTH_EAST;
		else
			turnTo = xTurnTo == Direction.WEST ? Direction.SOUTH_WEST : Direction.SOUTH_EAST;

		return turnTo;
	}

	public static Quadrant getQuadrant(double heading) {

		if (heading < Axis.RIGHT.getValue() && heading > Axis.UP.getValue()) {

			return Quadrant.FIRST;

		} else if (heading < Axis.DOWN.getValue() && heading > Axis.RIGHT.getValue()) {

			return Quadrant.FOURTH;

		} else if (heading < Axis.LEFT.getValue() && heading > Axis.DOWN.getValue()) {

			return Quadrant.THIRD;

		} else if (heading < 360 && heading > Axis.LEFT.getValue()) {

			return Quadrant.SECOND;

		} else {

			return Quadrant.AXIS;
		}
	}

	public static Direction getDirection(Robot robot) {

		return getDirection(robot.getHeading());
	}

	public static Direction getDirection(double headingDegrees) {

		if (headingDegrees <= 10 || headingDegrees >= 350) {

			return Direction.NORTH;

		} else if (headingDegrees < 80 && headingDegrees > 10) {

			return Direction.NORTH_EAST;

		} else if (headingDegrees <= 100 && headingDegrees >= 80) {

			return Direction.EAST;

		} else if (headingDegrees < 170 && headingDegrees > 100) {

			return Direction.SOUTH_EAST;

		} else if (headingDegrees <= 190 && headingDegrees >= 170) {

			return Direction.SOUTH;

		} else if (headingDegrees < 260 && headingDegrees > 190) {

			return Direction.SOUTH_WEST;

		} else if (headingDegrees <= 280 && headingDegrees >= 260) {

			return Direction.WEST;

		} else if (headingDegrees < 350 && headingDegrees > 280) {

			return Direction.NORTH_WEST;
		}

		return Direction.NORTH;
	}
}
