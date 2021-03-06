package com.thiagoh.robocode;

public class MoveCloseToEnemyStrategy implements MoveStrategy {

	private ImRobot imRobot;
	private StrategySucess strategySucess;

	public MoveCloseToEnemyStrategy(ImRobot imRobot, StrategySucess strategySucess) {

		this.imRobot = imRobot;
		this.strategySucess = strategySucess;
	}

	public void ahead(double bearingTo, double distance, String enemyName) {

		if (!strategySucess.isActive())
			return;

		Integer missedCount = imRobot.getStats().getBulletsMissedCount(enemyName);

		if (missedCount != null && missedCount >= 3) {

			imRobot.getStats().setBulletMissedCount(enemyName, 0);

			strategySucess.setUsage(true);

			if (distance >= 80) {

				imRobot.getStats().incleasePower(enemyName, 50);
				imRobot.turnRight(bearingTo);
				imRobot.setAhead(distance * 0.7);
			}

		} else {

			strategySucess.setUsage(false);
		}
	}
}
