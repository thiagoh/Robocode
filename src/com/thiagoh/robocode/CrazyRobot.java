package com.thiagoh.robocode;

import java.util.LinkedList;

import org.apache.commons.lang.ArrayUtils;

import robocode.HitByBulletEvent;
import robocode.HitRobotEvent;
import robocode.HitWallEvent;
import robocode.ScannedRobotEvent;
import robocode.StatusEvent;
import robocode.TurnCompleteCondition;

public class CrazyRobot extends ImRobot {

	private double xAxisDistancePermitted = 50;
	private double yAxisDistancePermitted = 50;

	public CrazyRobot() {

	}

	public void run() {

		while (true) {

			movingForward = true;

			Route route = goToXY(300, 300);

			turn(route.getTurning());
			ahead(route.getDistance());
			turnRadarRight(360);
		}
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

		double bearing = event.getBearing();

		if ((bearing <= 180 && bearing >= 170) || (bearing >= -180 && bearing <= -170)) {

			// turn
			Turning turning = turnToDirection(Util.escapeFromWall(this));

			setTurn(turning);
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
				resume();
				escapingFromWall = false;
			}
		}
	}

	private LinkedList<Double[]> bearingClockwise = new LinkedList<Double[]>();
	private LinkedList<Double[]> bearingAntiClockwise = new LinkedList<Double[]>();

	public void onScannedRobot(ScannedRobotEvent event) {

		double bearing = event.getBearing();

		if (bearing < 0)
			bearing = 360 + bearing;

		bearingClockwise.push(new Double[] { bearing, event.getDistance() });
		bearingAntiClockwise.push(new Double[] { bearing, event.getDistance() });

		if (bearingClockwise.size() >= 4) {

			Double[] last = null;
			Double[] cur = null;

			double lastBearing = 0;
			double curBearing = 0;
			boolean alwaysBigger = true;

			for (int i = 1; i <= 3; i++) {

				lastBearing = curBearing;

				if (i == 1) {

					last = bearingClockwise.pop();
					lastBearing = last[0];
				}

				cur = bearingClockwise.peek();
				curBearing = cur[0];

				if (after(curBearing, lastBearing, true))
					alwaysBigger = false;
			}

			// double a = cur[1];
			// double b = last[1];
			// double c = Math.sqrt(Math.pow(a, 2) + Math.pow(b, 2) - (2 * a * b
			// * Math.cos(lastBearing)));

			if (alwaysBigger) {

				System.out.println("ADD 30");
				bearing += 30;

			} else {

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
				// cur = bearingAntiClockwise.peek();
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

		turnGun(turnToDirection(getGunHeading(), bearing));
		setFire(0.1);
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
