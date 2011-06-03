package com.thiagoh.robocode;

import robocode.Robot;

public class Util {

	public static Route goToXY(Robot robot, double x, double y) {

		double rx = robot.getX();
		double ry = robot.getY();

		double catetoOposto = x > rx ? x - rx : rx - x;
		double catetoAdjacente = y > ry ? y - ry : ry - y;

		double teta = Math.toDegrees(Math.atan2(catetoOposto, catetoAdjacente));
		double heading = 0;

		if (x < rx && y < ry)
			heading = 180 + teta;
		else if (x < rx && y > ry)
			heading = 360 - teta;
		else if (x > rx && y > ry)
			heading = teta;
		else
			heading = 180 - teta;

		double distance = Math.sqrt(Math.pow(catetoOposto, 2) + Math.pow(catetoAdjacente, 2));

		return new RouteImpl(turnToDirection(robot, heading), distance);
	}

	public static Direction escapeFromWall(Robot robot) {

		Axis[] nearAxis = Util.getNearestAxis(robot);

		Direction xTurnTo = nearAxis[0] == Axis.RIGHT ? Direction.WEST : Direction.EAST;
		Direction yTurnTo = nearAxis[1] == Axis.UP ? Direction.SOUTH : Direction.NORTH;
		Direction turnTo = null;

		if (yTurnTo == Direction.NORTH)
			turnTo = xTurnTo == Direction.WEST ? Direction.NORTH_WEST : Direction.NORTH_EAST;
		else
			turnTo = xTurnTo == Direction.WEST ? Direction.SOUTH_WEST : Direction.SOUTH_EAST;

		return turnTo;
	}

	public static double[] howNearAxis(Robot robot) {

		double x = robot.getX();
		double y = robot.getY();
		double width = robot.getBattleFieldWidth();
		double height = robot.getBattleFieldHeight();

		double yAxisDistance = y > height / 2 ? height - y : y;
		double xAxisDistance = x > width / 2 ? width - x : x;

		return new double[] { xAxisDistance, yAxisDistance };
	}

	public static Axis[] getNearestAxis(Robot robot) {

		double x = robot.getX();
		double y = robot.getY();
		double width = robot.getBattleFieldWidth();
		double height = robot.getBattleFieldHeight();

		Axis yAxis = y > height / 2 ? Axis.UP : Axis.DOWN;
		Axis xAxis = x > width / 2 ? Axis.RIGHT : Axis.LEFT;

		return new Axis[] { xAxis, yAxis };
	}

	public static Turning turnToDirection(Robot robot, double toDegrees) {
		return turnToDirection(robot.getHeading(), toDegrees);
	}

	public static Turning turnToDirection(double headingDegrees, double toDegrees) {

		Quadrant quadrantFrom = getQuadrant(headingDegrees);
		Quadrant quadrantTo = getQuadrant(toDegrees);

		Turning turning = null;

		if (quadrantFrom == quadrantTo) {

			if (headingDegrees > toDegrees)
				turning = new TurningImpl(Rotation.LEFT, headingDegrees - toDegrees);
			else
				turning = new TurningImpl(Rotation.RIGHT, toDegrees - headingDegrees);

		} else if (Quadrant.FIRST == quadrantFrom && Quadrant.FOURTH == quadrantTo || Quadrant.THIRD == quadrantFrom
				&& Quadrant.SECOND == quadrantTo || Quadrant.FOURTH == quadrantFrom && Quadrant.THIRD == quadrantTo) {

			// turnRight

			turning = new TurningImpl(Rotation.RIGHT, toDegrees - headingDegrees);

		} else if (Quadrant.FOURTH == quadrantFrom && Quadrant.FIRST == quadrantTo || Quadrant.SECOND == quadrantFrom
				&& Quadrant.THIRD == quadrantTo || Quadrant.THIRD == quadrantFrom && Quadrant.FOURTH == quadrantTo) {

			// turnLeft
			turning = new TurningImpl(Rotation.LEFT, headingDegrees - toDegrees);

		} else if (Quadrant.FIRST == quadrantFrom && Quadrant.SECOND == quadrantTo) {

			// turnLeft
			turning = new TurningImpl(Rotation.LEFT, headingDegrees + 360 - toDegrees);

		} else if (Quadrant.SECOND == quadrantFrom && Quadrant.FIRST == quadrantTo) {

			// turnRight
			turning = new TurningImpl(Rotation.RIGHT, 360 - headingDegrees + toDegrees);

		} else if (Quadrant.SECOND == quadrantFrom && Quadrant.FOURTH == quadrantTo || Quadrant.THIRD == quadrantFrom
				&& Quadrant.FIRST == quadrantTo) {

			if (headingDegrees > toDegrees + 180)
				turning = new TurningImpl(Rotation.RIGHT, 360 - headingDegrees + toDegrees);
			else
				turning = new TurningImpl(Rotation.LEFT, headingDegrees - toDegrees);

		} else if (Quadrant.FOURTH == quadrantFrom && Quadrant.SECOND == quadrantTo || Quadrant.FIRST == quadrantFrom
				&& Quadrant.THIRD == quadrantTo) {

			if (toDegrees > headingDegrees + 180)
				turning = new TurningImpl(Rotation.LEFT, 360 - toDegrees + headingDegrees);
			else
				turning = new TurningImpl(Rotation.RIGHT, toDegrees - headingDegrees);

		} else if (Quadrant.AXIS == quadrantFrom && Quadrant.AXIS != quadrantTo) {

			if (Axis.UP.getValue() == headingDegrees) {

				if (Quadrant.FIRST == quadrantTo || Quadrant.FOURTH == quadrantTo)
					turning = new TurningImpl(Rotation.RIGHT, toDegrees);
				else
					turning = new TurningImpl(Rotation.LEFT, 360 - toDegrees);

			} else if (Axis.RIGHT.getValue() == headingDegrees) {

				if (Quadrant.FIRST == quadrantTo)
					turning = new TurningImpl(Rotation.LEFT, Axis.RIGHT.getValue() - toDegrees);
				else if (Quadrant.SECOND == quadrantTo)
					turning = new TurningImpl(Rotation.LEFT, 360 - toDegrees + 90);
				else
					turning = new TurningImpl(Rotation.RIGHT, toDegrees - Axis.RIGHT.getValue());

			} else if (Axis.DOWN.getValue() == headingDegrees) {

				if (Quadrant.SECOND == quadrantTo || Quadrant.THIRD == quadrantTo)
					turning = new TurningImpl(Rotation.RIGHT, toDegrees - Axis.DOWN.getValue());
				else
					turning = new TurningImpl(Rotation.LEFT, Axis.DOWN.getValue() - toDegrees);

			} else if (Axis.LEFT.getValue() == headingDegrees) {

				if (Quadrant.SECOND == quadrantTo)
					turning = new TurningImpl(Rotation.RIGHT, 360 - toDegrees);
				else if (Quadrant.FIRST == quadrantTo)
					turning = new TurningImpl(Rotation.RIGHT, 90 + toDegrees);
				else
					turning = new TurningImpl(Rotation.LEFT, Axis.LEFT.getValue() - toDegrees);
			}

		} else if (Quadrant.AXIS != quadrantFrom && Quadrant.AXIS == quadrantTo) {

			if (Axis.UP.getValue() == toDegrees) {

				if (Quadrant.FIRST == quadrantFrom || Quadrant.FOURTH == quadrantFrom)
					turning = new TurningImpl(Rotation.LEFT, headingDegrees);
				else
					turning = new TurningImpl(Rotation.RIGHT, 360 - headingDegrees);

			} else if (Axis.RIGHT.getValue() == toDegrees) {

				if (Quadrant.FIRST == quadrantFrom)
					turning = new TurningImpl(Rotation.RIGHT, Axis.RIGHT.getValue() - headingDegrees);
				else if (Quadrant.SECOND == quadrantFrom)
					turning = new TurningImpl(Rotation.RIGHT, 360 - headingDegrees + Axis.RIGHT.getValue());
				else
					turning = new TurningImpl(Rotation.LEFT, Axis.RIGHT.getValue() - headingDegrees);

			} else if (Axis.DOWN.getValue() == toDegrees) {

				if (Quadrant.SECOND == quadrantFrom || Quadrant.THIRD == quadrantFrom)
					turning = new TurningImpl(Rotation.LEFT, headingDegrees - Axis.DOWN.getValue());
				else
					turning = new TurningImpl(Rotation.RIGHT, Axis.DOWN.getValue() - headingDegrees);

			} else if (Axis.LEFT.getValue() == toDegrees) {

				if (Quadrant.SECOND == quadrantFrom)
					turning = new TurningImpl(Rotation.LEFT, headingDegrees - Axis.LEFT.getValue());
				else if (Quadrant.FIRST == quadrantFrom)
					turning = new TurningImpl(Rotation.LEFT, 90 + headingDegrees);
				else
					turning = new TurningImpl(Rotation.RIGHT, Axis.LEFT.getValue() - headingDegrees);
			}

		} else if (Quadrant.AXIS == quadrantFrom && Quadrant.AXIS == quadrantTo) {

			if ((Axis.UP.getValue() == headingDegrees && Axis.RIGHT.getValue() == toDegrees)
					|| (Axis.RIGHT.getValue() == headingDegrees && Axis.DOWN.getValue() == toDegrees)
					|| (Axis.DOWN.getValue() == headingDegrees && Axis.LEFT.getValue() == toDegrees)
					|| (Axis.LEFT.getValue() == headingDegrees && Axis.UP.getValue() == toDegrees)) {

				turning = new TurningImpl(Rotation.RIGHT, 90);

			} else if ((Axis.UP.getValue() == headingDegrees && Axis.LEFT.getValue() == toDegrees)
					|| (Axis.RIGHT.getValue() == headingDegrees && Axis.UP.getValue() == toDegrees)
					|| (Axis.DOWN.getValue() == headingDegrees && Axis.RIGHT.getValue() == toDegrees)) {

				turning = new TurningImpl(Rotation.LEFT, 90);

			} else {

				turning = new TurningImpl(Rotation.LEFT, 180);
			}
		}

		return turning;
	}

	public static Turning turnToDirection(Robot robot, Direction direction) {
		return turnToDirection(robot.getHeading(), direction);
	}

	public static Turning turnToDirection(double headingDegrees, Direction direction) {

		double to = 0;

		if (Direction.NORTH == direction)
			to = Axis.UP.getValue();
		else if (Direction.SOUTH == direction)
			to = Axis.DOWN.getValue();
		else if (Direction.EAST == direction)
			to = Axis.RIGHT.getValue();
		else if (Direction.WEST == direction)
			to = Axis.LEFT.getValue();
		else if (Direction.NORTH_EAST == direction)
			to = 45;
		else if (Direction.SOUTH_EAST == direction)
			to = Axis.RIGHT.getValue() + 45;
		else if (Direction.SOUTH_WEST == direction)
			to = Axis.DOWN.getValue() + 45;
		else if (Direction.NORTH_WEST == direction)
			to = Axis.LEFT.getValue() + 45;

		return turnToDirection(headingDegrees, to);
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
