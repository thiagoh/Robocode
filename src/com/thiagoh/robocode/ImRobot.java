package com.thiagoh.robocode;

import robocode.TeamRobot;

public abstract class ImRobot extends TeamRobot {

	protected boolean movingForward;
	private double startedEnergy;
	private RobotStats info;

	public ImRobot() {

		super();

		info = new RobotStats();
	}

	public void init() {

		startedEnergy = getEnergy();
		movingForward = true;
	}

	public RobotStats getStats() {
		return info;
	}

	public double getStartedEnergy() {
		return startedEnergy;
	}

	public Route goToXY(double x, double y) {

		double rx = getX();
		double ry = getY();

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
		else if (x > rx && y < ry)
			heading = 180 - teta;
		else if (x == rx && y > ry)
			heading = 0;
		else if (x == rx && y < ry)
			heading = 180;
		else if (x > rx && y == ry)
			heading = 90;
		else if (x < rx && y == ry)
			heading = 270;

		double distance = Math.sqrt(Math.pow(catetoOposto, 2) + Math.pow(catetoAdjacente, 2));

		return new RouteImpl(turnToDirection(heading), distance);
	}

	public double[] howNearAxis() {

		double x = getX();
		double y = getY();
		double width = getBattleFieldWidth();
		double height = getBattleFieldHeight();

		double yAxisDistance = y > height / 2 ? height - y : y;
		double xAxisDistance = x > width / 2 ? width - x : x;

		return new double[] { xAxisDistance, yAxisDistance };
	}

	public Axis[] getNearestAxis() {

		double x = getX();
		double y = getY();
		double width = getBattleFieldWidth();
		double height = getBattleFieldHeight();

		Axis yAxis = y > height / 2 ? Axis.UP : Axis.DOWN;
		Axis xAxis = x > width / 2 ? Axis.RIGHT : Axis.LEFT;

		return new Axis[] { xAxis, yAxis };
	}

	public Turning turnToDirection(double toDegrees) {
		return turnToDirection(getHeading(), toDegrees);
	}

	public Turning turnToDirection(double headingDegrees, double toDegrees) {

		if (toDegrees == 360)
			toDegrees = Axis.UP.getValue();

		Quadrant quadrantFrom = Util.getQuadrant(headingDegrees);
		Quadrant quadrantTo = Util.getQuadrant(toDegrees);

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

		if (turning == null) {
			System.out.println("headingDegrees: " + headingDegrees + " toDegrees: " + toDegrees);
			System.out.println("quadrantFrom: " + quadrantFrom + " quadrantTo: " + quadrantTo);
		}

		return turning;
	}

	public Turning turnToDirection(Direction direction) {
		return turnToDirection(getHeading(), direction);
	}

	public Turning turnToDirection(double headingDegrees, Direction direction) {

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

	public void turn(Turning turning) {
		turn(turning.getRotation(), turning.getValue());
	}

	public void turn(Rotation rotation, double degrees) {

		if (Rotation.LEFT == rotation)
			turnLeft(degrees);
		else
			turnRight(degrees);
	}

	public void turnGun(Turning turning) {
		turnGun(turning.getRotation(), turning.getValue());
	}

	public void turnGun(Rotation rotation, double degrees) {

		if (Rotation.LEFT == rotation)
			turnGunLeft(degrees);
		else
			turnGunRight(degrees);
	}

	public void setTurn(Turning turning) {
		setTurn(turning.getRotation(), turning.getValue());
	}

	public void setTurn(Rotation rotation, double degrees) {

		if (Rotation.LEFT == rotation)
			setTurnLeft(degrees);
		else
			setTurnRight(degrees);
	}

}
