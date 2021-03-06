package com.thiagoh.robocode;

import robocode.HitByBulletEvent;
import robocode.HitRobotEvent;
import robocode.HitWallEvent;
import robocode.ScannedRobotEvent;
import robocode.StatusEvent;

public class CornerRobot extends ImRobot {

	public CornerRobot() {

	}

	public void run() {

		int count = 0;
		while (true) {

			if (count < 2) {

				movingForward = true;

				Route route = goToXY(60, 60);

				turn(route.getTurning());
				ahead(route.getDistance());

				route = goToXY(60, getBattleFieldHeight() - 60);

				turn(route.getTurning());
				ahead(route.getDistance());

				route = goToXY(getBattleFieldWidth() - 60, getBattleFieldHeight() - 60);

				turn(route.getTurning());
				ahead(route.getDistance());

				route = goToXY(getBattleFieldWidth() - 60, 60);

				turn(route.getTurning());
				ahead(route.getDistance());

				count++;
				
			} else {
				
				doNothing();
			}
		}
	}

	public void onHitRobot(HitRobotEvent event) {

	}

	public void onHitWall(HitWallEvent event) {

	}

	public void onHitBullet(HitByBulletEvent event) {

	}

	public void onStatus(StatusEvent e) {

	}

	public void onScannedRobot(ScannedRobotEvent event) {

	}
}
