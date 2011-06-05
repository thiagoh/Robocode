package com.thiagoh.robocode;

import java.awt.Color;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import org.apache.commons.lang.ArrayUtils;

import robocode.BulletHitEvent;
import robocode.BulletMissedEvent;
import robocode.HitByBulletEvent;
import robocode.HitRobotEvent;
import robocode.HitWallEvent;
import robocode.Rules;
import robocode.ScannedRobotEvent;
import robocode.StatusEvent;
import robocode.TurnCompleteCondition;
import robocode.util.Utils;

public class CrazyRobot extends ImRobot {

	private double xAxisDistancePermitted = 50;
	private double yAxisDistancePermitted = 50;
	private Map<String, Double> powerMap;
	private Map<String, Double> bulletsHitMap;
	private Map<Integer, String> bulletsFiredMap;
	private Map<String, Integer> bulletsMissedMap;

	private Map<String, StrategySucess> advanceGunStrategyMap;
	private Map<String, StrategySucess> beCloseToEnemyStrategyMap;

	public CrazyRobot() {

	}

	public void run() {

		powerMap = new HashMap<String, Double>();
		bulletsHitMap = new HashMap<String, Double>();
		bulletsFiredMap = new HashMap<Integer, String>();
		bulletsMissedMap = new HashMap<String, Integer>();
		advanceGunStrategyMap = new HashMap<String, StrategySucess>();
		beCloseToEnemyStrategyMap = new HashMap<String, StrategySucess>();

		setBodyColor(Color.red);
		setGunColor(Color.gray);
		setRadarColor(Color.darkGray);

		Route route = goToXY(300, 300);

		turn(route.getTurning());
		ahead(route.getDistance());

		while (true) {

			movingForward = true;

			turnRadarRight(360);
		}
	}

	public void onBulletMissed(BulletMissedEvent event) {

		System.out.println("looking for: " + event.getBullet().getHeading());

		String target = bulletsFiredMap.remove((int) event.getBullet().getHeading());

		if (target != null) {

			Double power = powerMap.get(target);

			power = power * 0.7;

			if (power < Rules.MIN_BULLET_POWER)
				power = Rules.MIN_BULLET_POWER;

			System.out.println("decrease power: " + power + " " + target);

			powerMap.put(target, power);

			Integer missedCount = bulletsMissedMap.get(target);

			if (missedCount == null)
				missedCount = 0;

			bulletsMissedMap.put(target, ++missedCount);
		}

		StrategySucess beCloseToEnemyStrategy = beCloseToEnemyStrategyMap.get(target);

		if (beCloseToEnemyStrategy != null && beCloseToEnemyStrategy.getUsage())
			beCloseToEnemyStrategy.fault();

		StrategySucess advanceGunStrategy = advanceGunStrategyMap.get(target);

		if (advanceGunStrategy != null && advanceGunStrategy.getUsage())
			advanceGunStrategy.fault();
	}

	public void onBulletHit(BulletHitEvent event) {

		Double power = powerMap.get(event.getName());

		if (power == null)
			power = Rules.MIN_BULLET_POWER;

		power = power * 1.4;

		// System.out.println("increase power: " + power + " " +
		// event.getName());

		powerMap.put(event.getName(), power);

		StrategySucess beCloseToEnemyStrategy = beCloseToEnemyStrategyMap.get(event.getName());

		if (beCloseToEnemyStrategy != null && beCloseToEnemyStrategy.getUsage())
			beCloseToEnemyStrategy.goal();

		StrategySucess advanceGunStrategy = advanceGunStrategyMap.get(event.getName());

		if (advanceGunStrategy != null && advanceGunStrategy.getUsage())
			advanceGunStrategy.goal();
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

	public void onHitByBullet(HitByBulletEvent event) {

		powerMap.put(event.getName(), event.getBullet().getPower());

		double bearing = event.getBearing();

		if ((bearing <= 180 && bearing >= 170) || (bearing >= -180 && bearing <= -170)) {

			// turn
			setTurn(turnToDirection(Util.escapeFromWall(this)));
		}

		// go ahead
		double distance = getWidth() * Math.tan(bearing);

		setAhead(distance);

		waitFor(new TurnCompleteCondition(this));
	}

	private boolean escapingFromWall = false;

	public void onStatus(StatusEvent e) {

		if (escapingFromWall)
			return;

		double[] axisDistance = howNearAxis();

		// System.out.println("(" + getX() + "," + getY() + ")");

		// System.out.println("to: " + Util.getDirection(robot));

		// System.out.println(axisDistance[0] + " / " + axisDistance[1]);

		if (axisDistance[0] <= xAxisDistancePermitted || axisDistance[1] <= yAxisDistancePermitted) {

			Axis[] nearestAxis = getNearestAxis();
			Direction direction = Util.getDirection(this);

			if (!movingForward)
				direction = direction.invert();

			// System.out.println("Direction: " + direction + " / xAxis: " +
			// nearestAxis[0] + " / yAxis: "
			// + nearestAxis[1]);

			boolean xAxisUpToHit = axisDistance[0] <= xAxisDistancePermitted
					&& ((nearestAxis[0] == Axis.LEFT && ArrayUtils.contains(Direction.TO_WEST, direction)) || (nearestAxis[0] == Axis.RIGHT && ArrayUtils
							.contains(Direction.TO_EAST, direction)));

			boolean yAxisUpToHit = axisDistance[1] <= yAxisDistancePermitted
					&& (nearestAxis[1] == Axis.UP && ArrayUtils.contains(Direction.TO_NORTH, direction))
					|| (nearestAxis[1] == Axis.DOWN && ArrayUtils.contains(Direction.TO_SOUTH, direction));

			if (xAxisUpToHit || yAxisUpToHit) {

				escapingFromWall = true;
				// System.out.println("Vai bater!");

				Turning turning = turnToDirection(Util.escapeFromWall(this));

				stop(true);
				turn(turning);
				ahead(100);
				escapingFromWall = false;
			}
		}
	}

	private LinkedList<Double[]> bearingClockwise = new LinkedList<Double[]>();
	private LinkedList<Double[]> bearingAntiClockwise = new LinkedList<Double[]>();

	public void onScannedRobot(ScannedRobotEvent event) {

		Double power = powerMap.get(event.getName());

		System.out.println("power: " + power);

		if (power == null)
			power = Rules.MIN_BULLET_POWER;

		if (event.getDistance() < 50 && getEnergy() > 50)
			power += power * 0.8;

		powerMap.put(event.getName(), power);

		double bearing = event.getBearing();

		if (bearing < 0)
			bearing = 360 + bearing;

		System.out.println("bearing: " + bearing);

		StrategySucess advanceGunStrategy = advanceGunStrategyMap.get(event.getName());

		if (advanceGunStrategy == null) {

			advanceGunStrategy = new StrategySucess("Avancar arma projetando movimento futuro do inimigo", 100);
			advanceGunStrategyMap.put(event.getName(), advanceGunStrategy);
		}

		StrategySucess beCloseToEnemyStrategy = beCloseToEnemyStrategyMap.get(event.getName());

		if (beCloseToEnemyStrategy == null) {

			beCloseToEnemyStrategy = new StrategySucess("Chegar perto do inimigo quando errar mtos tiros", 100);
			beCloseToEnemyStrategyMap.put(event.getName(), beCloseToEnemyStrategy);
		}

		bearing = advanceGunStrategy.isActive() ? advanceStrategy(advanceGunStrategy, bearing, event.getDistance())
				: bearing;

		double toDegrees = Utils.normalAbsoluteAngleDegrees(bearing + getHeading());

		Turning turning = turnToDirection(getGunHeading(), toDegrees);

		turnGun(turning);
		setFire(power);

		if (beCloseToEnemyStrategy.isActive()) {

			Integer missedCount = bulletsMissedMap.get(event.getName());

			if (missedCount != null && missedCount >= 3) {

				bulletsMissedMap.put(event.getName(), 0);

				beCloseToEnemyStrategy.setUsage(true);

				if (event.getDistance() >= 80) {

					turnRight(event.getBearing());
					ahead(event.getDistance() * 0.5);
				}

			} else {

				beCloseToEnemyStrategy.setUsage(false);
			}
		}

		System.out.println("inserting: " + toDegrees);
		bulletsFiredMap.put((int) toDegrees, event.getName());

	}

	private double advanceStrategy(StrategySucess advanceGunStrategy, double bearing, double distance) {

		bearingClockwise.push(new Double[] { bearing, distance });
		bearingAntiClockwise.push(new Double[] { bearing, distance });

		LinkedList<Double[]> bearingClockwiseClone = new LinkedList<Double[]>(bearingClockwise);

		if (bearingClockwise.size() >= 4) {

			Double[] last = null;
			Double[] cur = null;

			double lastBearing = 0;
			double curBearing = 0;

			boolean alwaysBigger = true;
			for (int i = 1; i <= 3; i++) {

				lastBearing = curBearing;

				if (i == 1) {

					last = bearingClockwise.pollLast();
					// System.out.println("removing: " + last[0]);
					lastBearing = last[0];
				}

				cur = bearingClockwise.pollLast();
				// System.out.println("removing: " + cur[0]);
				curBearing = cur[0];

				if (!after(curBearing, lastBearing, true))
					alwaysBigger = false;
			}

			// double a = cur[1];
			// double b = last[1];
			// double c = Math.sqrt(Math.pow(a, 2) + Math.pow(b, 2) - (2 * a
			// * b
			// * Math.cos(lastBearing)));

			bearingClockwise = bearingClockwiseClone;
			bearingClockwise.pollLast();

			if (alwaysBigger) {

				advanceGunStrategy.setUsage(true);
				bearing += 20;

			} else {

				advanceGunStrategy.setUsage(false);

				// boolean alwaysSmaller = true;
				//
				// for (int i = 1; i <= 3; i++) {
				//
				// lastBearing = curBearing;
				// if (i == 1) {
				//
				// last = bearingAntiClockwise.pop();
				// lastBearing = last[0];
				// }
				//
				// cur = bearingAntiClockwise.pop();
				// curBearing = cur[0];
				//
				// if (after(curBearing, lastBearing, false))
				// alwaysBigger = false;
				// }
				//
				// if (alwaysSmaller) {
				//
				// bearing -= 20;
				// }
			}
		}

		if (bearing > 360)
			bearing = bearing - 360;

		return bearing;
	}

	private boolean after(double hAtT2, double hAtT1, boolean clockwiseDirection) {

		if (clockwiseDirection) {

			if (hAtT2 > hAtT1)
				return true;
			else
				return false;

		} else {

			if (hAtT2 < hAtT1)
				return true;
			else
				return false;
		}
	}
}
