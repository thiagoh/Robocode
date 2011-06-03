package com.thiagoh.robocode;

import robocode.AdvancedRobot;

public abstract class ImRobot extends AdvancedRobot {

	protected boolean movingForward;

	public void turn(Turning turning) {
		turn(turning.getRotation(), turning.getValue());
	}

	public void turn(Rotation rotation, double degrees) {

		if (Rotation.LEFT == rotation)
			turnLeft(degrees);
		else
			turnRight(degrees);
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
