package com.thiagoh.robocode;

import org.apache.commons.lang.ArrayUtils;

import robocode.HitByBulletEvent;
import robocode.HitRobotEvent;
import robocode.HitWallEvent;
import robocode.ScannedRobotEvent;
import robocode.StatusEvent;
import robocode.TurnCompleteCondition;

public class CrazyStrategyImpl implements WalkingStrategy {

	private double xAxisDistancePermitted = 50;
	private double yAxisDistancePermitted = 50;
	private ImRobot robot;

	public CrazyStrategyImpl(ImRobot robot) {

		this.robot = robot;
	}

	public void main() {

		robot.movingForward = true;

		Turning turning = Util.turnToDirection(robot, 92);

		robot.turn(turning);
		robot.ahead(100);

		turning = Util.turnToDirection(robot, 182);

		robot.turn(turning);
		robot.ahead(100);
		robot.turnRadarLeft(360);

		Route route = Util.goToXY(robot, 300, 300);

		robot.turn(route.getTurning());
		robot.ahead(route.getDistance());
		robot.turnRadarLeft(360);

		route = Util.goToXY(robot, 60, 60);

		robot.turn(route.getTurning());
		robot.ahead(route.getDistance());
		robot.turnRadarLeft(360);

		route = Util.goToXY(robot, 60, 560);

		robot.turn(route.getTurning());
		robot.ahead(route.getDistance());
		robot.turnRadarLeft(360);

		route = Util.goToXY(robot, 660, 560);

		robot.turn(route.getTurning());
		robot.ahead(route.getDistance());
		robot.turnRadarLeft(360);

		// Turning turn = Util.turnToDirection(robot.getHeading(),
		// Direction.NORTH);
		//
		// robot.setTurn(turn);
		//
		// robot.waitFor(new TurnCompleteCondition(robot));
		//
		// turn = Util.turnToDirection(robot.getHeading(), Direction.EAST);
		//
		// robot.setTurn(turn);
		//
		// robot.waitFor(new TurnCompleteCondition(robot));
		//
		// turn = Util.turnToDirection(robot.getHeading(), Direction.SOUTH);
		//
		// robot.setTurn(turn);
		//
		// robot.waitFor(new TurnCompleteCondition(robot));
		//
		// turn = Util.turnToDirection(robot.getHeading(), Direction.WEST);
		//
		// robot.setTurn(turn);
		//
		// robot.waitFor(new TurnCompleteCondition(robot));
	}

	public void onHitRobot(HitRobotEvent event) {

		reverseDirection();
	}

	public void onHitWall(HitWallEvent event) {

		reverseDirection();
	}

	private void reverseDirection() {

		if (robot.movingForward) {

			robot.setBack(40000);
			robot.movingForward = false;

		} else {

			robot.setAhead(40000);
			robot.movingForward = true;
		}
	}

	public void onHitBullet(HitByBulletEvent event) {

		double bearing = event.getBearing();

		System.out.println(bearing);

		if ((bearing <= 180 && bearing >= 170) || (bearing >= -180 && bearing <= -170)) {

			// turn
			Turning turning = Util.turnToDirection(robot, Util.escapeFromWall(robot));

			robot.setTurn(turning);
		}

		// go ahead
		double distance = robot.getWidth() * Math.tan(bearing);

		robot.setAhead(distance);

		robot.waitFor(new TurnCompleteCondition(robot));
	}

	private boolean escapingFromWall = false;

	public void onStatus(StatusEvent e) {

		if (escapingFromWall)
			return;

		double[] axisDistance = Util.howNearAxis(robot);

		// System.out.println("(" + robot.getX() + "," + robot.getY() + ")");

		// System.out.println("to: " + Util.getDirection(robot));

		// System.out.println(axisDistance[0] + " / " + axisDistance[1]);

		if (axisDistance[0] <= xAxisDistancePermitted || axisDistance[1] <= yAxisDistancePermitted) {

			Axis[] nearestAxis = Util.getNearestAxis(robot);
			Direction direction = Util.getDirection(robot);

			if (!robot.movingForward)
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

				Turning turning = Util.turnToDirection(robot, Util.escapeFromWall(robot));

				robot.stop(true);
				robot.turn(turning);
				robot.ahead(100);
				robot.resume();
				escapingFromWall = false;
			}
		}
	}

	public void onScannedRobot(ScannedRobotEvent event) {

		robot.turnGunRight(event.getBearing());
		robot.setFire(2);
	}
}
