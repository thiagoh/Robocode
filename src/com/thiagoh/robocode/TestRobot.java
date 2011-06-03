package com.thiagoh.robocode;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import robocode.AdvancedRobot;
import robocode.BulletHitEvent;
import robocode.BulletMissedEvent;
import robocode.HitByBulletEvent;
import robocode.HitRobotEvent;
import robocode.HitWallEvent;
import robocode.RobotDeathEvent;
import robocode.ScannedRobotEvent;
import robocode.TurnCompleteCondition;

public class TestRobot extends AdvancedRobot {

	private double power;
	private double bearingTo;
	private double distanceTo;
	private boolean searchRobotToKill = true;
	private int bulletMissed = 0;
	private ScannedRobotEvent currentScannedRobotEvent;
	private String robotTarget;
	private Map<String, ScannedRobotEvent> scannedMap;

	private static final double frontSideSize = 10;

	public void run() {

		power = 1;
		scannedMap = new HashMap<String, ScannedRobotEvent>();

		while (true) {

			if (distanceTo >= 50)
				doNothing();

			turnRadarLeft(360);
		}

	}

	public void onRobotDeath(RobotDeathEvent event) {

		scannedMap.remove(event.getName());
	}

	public void onBulletMissed(BulletMissedEvent event) {

		power -= power * 0.4;

		if (power < 1)
			power = 1;

		bulletMissed++;

		if (bulletMissed >= 2) {
			searchRobotToKill = true;
			bulletMissed = 0;
		}
	}

	public void onHitByBullet(HitByBulletEvent event) {

		ScannedRobotEvent scannedEvent = scannedMap.get(event.getName());

		if (scannedEvent.getDistance() <= 300) {

			setTurnLeft(120);
			setBack(200);
			waitFor(new TurnCompleteCondition(this));

			searchRobotToKill = true;
			bulletMissed = 0;
		}
	}

	public void onHitRobot(HitRobotEvent event) {

		if (left) {

			setTurnLeft(180);

		} else {

			setTurnRight(180);
		}

		setBack(300);
		waitFor(new TurnCompleteCondition(this));

		if (left) {

			setTurnRight(60);

		} else {

			setTurnLeft(60);
		}
		waitFor(new TurnCompleteCondition(this));

		searchRobotToKill = true;
		bulletMissed = 0;
	}

	private boolean wallhitted = false;
	private boolean wallhittedAgain = false;
	private boolean left = true;

	public void onHitWall(HitWallEvent event) {

		if (wallhittedAgain) {

			left = false;
			wallhitted = false;
			wallhittedAgain = false;

		} else {

			if (wallhitted)
				wallhittedAgain = true;

			wallhitted = true;
		}

		if (left) {

			setTurnLeft(90);

		} else {

			setTurnRight(90);
		}

		waitFor(new TurnCompleteCondition(this));
	}

	public void onBulletHit(BulletHitEvent event) {

		power += power * 0.2;

		if (searchRobotToKill) {

			searchRobotToKill = false;
			System.out.println("Robot found!");
			currentScannedRobotEvent = scannedMap.get(event.getName());
		}

		robotTarget = currentScannedRobotEvent.getName();
		bearingTo = currentScannedRobotEvent.getBearing();
		distanceTo = currentScannedRobotEvent.getDistance();
	}

	public void onScannedRobot(ScannedRobotEvent event) {

		scannedMap.put(event.getName(), event);

		if (searchRobotToKill) {

			robotTarget = event.getName();
			bearingTo = event.getBearing();
			distanceTo = event.getDistance();
		}

		// System.out.println("distanceTo = " + distanceTo);
		// System.out.println("robotTarget = " + robotTarget);
		// System.out.println("bearingTo = " + bearingTo);

		double catetoOposto = Math.abs(Math.tan(event.getBearingRadians()) * event.getDistance());

		System.out.println("power = " + power);

		if (event.getBearing() < 180 && catetoOposto < (frontSideSize / 2)) {

			System.out.println("catetoOposto = " + catetoOposto);
			fire(power);
			return;

		} else {

			turnRight(bearingTo);
		}

		fire(power);
	}

	private ScannedRobotEvent selectTargetRobot() {

		ScannedRobotEvent closestRobot = null;

		for (Entry<String, ScannedRobotEvent> entry : scannedMap.entrySet()) {

			if (closestRobot == null) {

				closestRobot = entry.getValue();
				continue;

			} else {

				if (entry.getValue().getBearing() < closestRobot.getBearing())
					closestRobot = entry.getValue();
			}
		}

		System.out.println("Choosed Robot: " + closestRobot.getName());

		return closestRobot;
	}
}