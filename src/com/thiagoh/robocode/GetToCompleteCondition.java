package com.thiagoh.robocode;

import robocode.AdvancedRobot;
import robocode.Condition;

public class GetToCompleteCondition extends Condition {

	private double x;
	private double y;
	private AdvancedRobot robot;

	public GetToCompleteCondition(AdvancedRobot robot, double x, double y) {
		this.robot = robot;
	}

	public GetToCompleteCondition(AdvancedRobot robot, double x, double y, int priority) {
		this.robot = robot;
		this.priority = priority;
	}

	public boolean test() {
		return (robot.getX() == x && robot.getY() == y);
	}

	public void cleanup() {
		robot = null;
	}
}
