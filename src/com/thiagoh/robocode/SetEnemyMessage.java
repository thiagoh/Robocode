package com.thiagoh.robocode;

import org.apache.log4j.Logger;

public class SetEnemyMessage implements Message {

	private static Logger log = Logger.getLogger(SetEnemyMessage.class);

	private RobotOrdered teammate;
	private String enemy;

	public SetEnemyMessage(RobotOrdered teammate, String enemy) {
		this.teammate = teammate;
		this.enemy = enemy;
	}

	public RobotOrdered getTeammate() {
		return teammate;
	}

	public void setTeammate(RobotOrdered teammate) {
		this.teammate = teammate;
	}

	public String getEnemy() {
		return enemy;
	}

	public void setEnemy(String enemy) {
		this.enemy = enemy;
	}

	public void execute(ImRobot imRobot) {

		RobotOrdered robotOrdered = imRobot.firstOrderedRobot();

		if (robotOrdered.equals(getTeammate())) {

			imRobot.putAttribute("enemy", getEnemy());

			log.info("setting enemy: " + getEnemy());
		}
	}
}
