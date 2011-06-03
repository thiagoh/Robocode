package com.thiagoh.robocode;

import robocode.HitByBulletEvent;
import robocode.HitRobotEvent;
import robocode.HitWallEvent;
import robocode.ScannedRobotEvent;
import robocode.StatusEvent;

public class StrategyRobot extends ImRobot {

	private WalkingStrategy walkingStrategy;

	public StrategyRobot() {

		this.walkingStrategy = new CrazyStrategyImpl(this);
	}

	public void run() {

		while (true) {

			walkingStrategy.main();
		}
	}

	public void onStatus(StatusEvent event) {

		walkingStrategy.onStatus(event);
	}

	public void onHitWall(HitWallEvent event) {

		walkingStrategy.onHitWall(event);
	}

	public void onHitRobot(HitRobotEvent event) {

		walkingStrategy.onHitRobot(event);
	}

	public void onHitByBullet(HitByBulletEvent event) {

		walkingStrategy.onHitBullet(event);
	}

	public void onScannedRobot(ScannedRobotEvent event) {

		walkingStrategy.onScannedRobot(event);
	}
}