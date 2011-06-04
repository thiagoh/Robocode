package com.thiagoh.robocode;

import robocode.HitByBulletEvent;
import robocode.HitRobotEvent;
import robocode.HitWallEvent;
import robocode.ScannedRobotEvent;
import robocode.StatusEvent;

public class CornerRobot extends ImRobot {

	public CornerRobot() {

	}

	public void main() {

		movingForward = true;

		Route route = goToXY(60, 60);

		turn(route.getTurning());
		ahead(route.getDistance());

		route = goToXY(60, 600);

		turn(route.getTurning());
		ahead(route.getDistance());

		route = goToXY(660, 660);

		turn(route.getTurning());
		ahead(route.getDistance());

		route = goToXY(660, 60);

		turn(route.getTurning());
		ahead(route.getDistance());
	}

	public void onHitRobot(HitRobotEvent event) {

		reverseDirection();
	}

	public void onHitWall(HitWallEvent event) {

		reverseDirection();
	}

	private void reverseDirection() {

		if (movingForward) {

			setBack(40000);
			movingForward = false;

		} else {

			setAhead(40000);
			movingForward = true;
		}
	}

	public void onHitBullet(HitByBulletEvent event) {

	}

	public void onStatus(StatusEvent e) {

	}

	public void onScannedRobot(ScannedRobotEvent event) {

	}
}
