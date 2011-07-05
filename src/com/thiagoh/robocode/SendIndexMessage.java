package com.thiagoh.robocode;

public class SendIndexMessage implements Message {

	private RobotOrdered robotOrdered;

	public SendIndexMessage(RobotOrdered robotOrdered) {
		this.robotOrdered = robotOrdered;
	}

	public RobotOrdered getRobotOrdered() {
		return robotOrdered;
	}

	public void setRobotOrdered(RobotOrdered robotOrdered) {
		this.robotOrdered = robotOrdered;
	}

	public void execute(ImRobot imRobot) {

		imRobot.getOrderedRobots().add(getRobotOrdered());
	}
}
