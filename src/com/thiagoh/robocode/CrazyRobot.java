package com.thiagoh.robocode;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.ArrayUtils;
import org.apache.log4j.Logger;

import robocode.BulletHitEvent;
import robocode.BulletMissedEvent;
import robocode.HitByBulletEvent;
import robocode.HitRobotEvent;
import robocode.HitWallEvent;
import robocode.MessageEvent;
import robocode.MoveCompleteCondition;
import robocode.RobotDeathEvent;
import robocode.Rules;
import robocode.ScannedRobotEvent;
import robocode.StatusEvent;
import robocode.TurnCompleteCondition;
import robocode.util.Utils;

public class CrazyRobot extends ImRobot {

	private static Logger log = Logger.getLogger(CrazyRobot.class);

	private final static double DEFAULT_DISTANCE_TO_MOVE = 100;

	public static final Double INITIAL_BULLET_POWER = 1.5;

	private double distanceToMove = DEFAULT_DISTANCE_TO_MOVE;
	private double xAxisDistancePermitted = 50;
	private double yAxisDistancePermitted = 50;
	private long lastBulletFiredTime;

	private Map<String, StrategySucess> advanceGunStrategyMap;
	private Map<String, StrategySucess> beCloseToEnemyStrategyMap;

	public CrazyRobot() {

	}

	public void run() {

		init();

		advanceGunStrategyMap = new HashMap<String, StrategySucess>();
		beCloseToEnemyStrategyMap = new HashMap<String, StrategySucess>();

		broadcast(new SetColorsMessage(Color.red, Color.black, Color.darkGray, Color.orange));

		while (true) {

			movingForward = true;

			if (getTime() - lastBulletFiredTime > 100) {

				/*
				 * Caso o robot esteja há muitas rodadas sem atirar ele deverá
				 * andar numa maior distância e numa angulação randomica para
				 * procurar robots adversários
				 */

				// removeAttribute("enemy");
				distanceToMove += 50;
				Route r = goToXY(Math.random() * getBattleFieldWidth(), Math.random() * getBattleFieldHeight());
				setTurn(r.getTurning());
				setAhead(r.getDistance());
				waitFor(new MoveCompleteCondition(this, 50));

			} else {

				doNothing();
				// ahead(distanceToMove);
				// back(distanceToMove);
			}

			setTurnRadarRight(10000);
		}
	}

	public void onHitRobot(HitRobotEvent event) {

		reverseDirection();
	}

	public void onHitWall(HitWallEvent event) {

		reverseDirection();
	}

	private void reverseDirection() {

		setTurnRight(90);

		if (movingForward) {

			setBack(400);
			movingForward = false;

		} else {

			setAhead(400);
			movingForward = true;
		}

		waitFor(new TurnCompleteCondition(this, 30));
	}

	public void onBulletMissed(BulletMissedEvent event) {

		// log.info("Looking for: " + event.getBullet().getHeading());

		String target = getStats().retrieveBulletFired(event.getBullet().getHeading());

		if (target != null) {

			Double power = getStats().getPower(target);

			power = power * 0.7;

			if (power < INITIAL_BULLET_POWER)
				power = INITIAL_BULLET_POWER;

			// log.info("Decrease power: " + power + " " + target);

			getStats().setPower(target, power);

			Integer missedCount = getStats().getBulletsMissedCount(target);

			if (missedCount == null)
				missedCount = 0;

			getStats().setBulletMissedCount(target, ++missedCount);
		}

		StrategySucess beCloseToEnemyStrategy = beCloseToEnemyStrategyMap.get(target);

		if (beCloseToEnemyStrategy != null && beCloseToEnemyStrategy.getUsage())
			beCloseToEnemyStrategy.fault();

		StrategySucess advanceGunStrategy = advanceGunStrategyMap.get(target);

		if (advanceGunStrategy != null && advanceGunStrategy.getUsage())
			advanceGunStrategy.fault();
	}

	public void onBulletHit(BulletHitEvent event) {

		Double power = getStats().getPower(event.getName());

		if (power == null)
			power = INITIAL_BULLET_POWER;

		power = power * 2.5;

		// log.info("Increase power: " + power + " " + event.getName());

		getStats().setPower(event.getName(), power);

		StrategySucess beCloseToEnemyStrategy = beCloseToEnemyStrategyMap.get(event.getName());

		if (beCloseToEnemyStrategy != null && beCloseToEnemyStrategy.getUsage())
			beCloseToEnemyStrategy.goal();

		StrategySucess advanceGunStrategy = advanceGunStrategyMap.get(event.getName());

		if (advanceGunStrategy != null && advanceGunStrategy.getUsage())
			advanceGunStrategy.goal();
	}

	public void onHitByBullet(HitByBulletEvent event) {

		if (!isTeammate(event.getName()))
			getStats().setPower(event.getName(), event.getBullet().getPower());

		double bearing = event.getBearing();

		setTurn(turnToDirection(Utils.normalAbsoluteAngleDegrees(bearing)));
		setAhead(100);

		waitFor(new TurnCompleteCondition(this));
	}

	private boolean escapingFromWall = false;

	public void onStatus(StatusEvent e) {

		if (escapingFromWall)
			return;

		double[] axisDistance = howNearAxis();

		// log.info("(" + getX() + "," + getY() + ")");
		// log.info("to: " + Util.getDirection(robot));
		// log.info(axisDistance[0] + " / " + axisDistance[1]);

		if (axisDistance[0] <= xAxisDistancePermitted || axisDistance[1] <= yAxisDistancePermitted) {

			Axis[] nearestAxis = getNearestAxis();
			Direction direction = Util.getDirection(this);

			if (!movingForward)
				direction = direction.invert();

			// log.info("Direction: " + direction + " / xAxis: " +
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

				Turning turning = turnToDirection(Util.escapeFromWall(this));

				stop(true);
				turn(turning);
				ahead(100);
				escapingFromWall = false;
			}
		}
	}

	public void onRobotDeath(RobotDeathEvent event) {

		if (isTeammate(event.getName())) {

			if (firstOrderedRobot().equals(new RobotOrderedImpl(event.getName())))
				getOrderedRobots().remove(firstOrderedRobot());

			return;
		}

		String enemy = (String) getAttribute("enemy");

		if (enemy != null && enemy.equals(event.getName())) {

			log.info("Current enemy dead");
			removeAttribute("enemy");
		}
	}

	public void onScannedRobot(ScannedRobotEvent event) {

		if (isTeammate(event.getName()))
			return;

		String enemy = (String) getAttribute("enemy");

		RobotOrdered ordered = new RobotOrderedImpl(getName(), getIndex());

		if (firstOrderedRobot().equals(ordered)) {

			if (enemy == null) {

				Message m = new SetEnemyMessage(ordered, event.getName());
				broadcast(m);
				m.execute(this);
				log.info("LEADER");
			}
		}

		if (enemy == null) {

			enemy = event.getName();

		} else {

			if (!enemy.equals(event.getName()))
				return;
		}

		Double power = getStats().getPower(enemy);

		if (log.isDebugEnabled())
			log.debug("Current POWER is " + power);

		if (power == null)
			power = INITIAL_BULLET_POWER;

		if (event.getDistance() < 50 && getEnergy() > 50)
			power += power * 0.8;

		getStats().setPower(enemy, power);

		double bearing = event.getBearing();

		if (bearing < 0)
			bearing = 360 + bearing;

		StrategySucess advanceGunStrategy = advanceGunStrategyMap.get(enemy);

		if (advanceGunStrategy == null) {

			advanceGunStrategy = new StrategySucess("Avancar arma projetando movimento futuro do inimigo", 100);
			advanceGunStrategyMap.put(enemy, advanceGunStrategy);
		}

		TurnStrategy strategy = new AdvanceGunStrategy(advanceGunStrategy);
		bearing = strategy.getBearing(bearing, event.getDistance());

		double toDegrees = Utils.normalAbsoluteAngleDegrees(bearing + getHeading());

		Turning turning = turnToDirection(getGunHeading(), toDegrees);

		turnGun(turning);
		fireAntStamp(power);
		getStats().putBulletFired(toDegrees, enemy);

		StrategySucess beCloseToEnemyStrategy = beCloseToEnemyStrategyMap.get(enemy);

		if (beCloseToEnemyStrategy == null) {

			beCloseToEnemyStrategy = new StrategySucess("Chegar perto do inimigo quando errar mtos tiros", 100);
			beCloseToEnemyStrategyMap.put(enemy, beCloseToEnemyStrategy);
		}

		MoveStrategy moveStrategy = new MoveCloseToEnemyStrategy(this, beCloseToEnemyStrategy);
		moveStrategy.ahead(event.getBearing(), event.getDistance(), enemy);
	}

	private void fireAntStamp(double power) {

		fire(power);

		lastBulletFiredTime = getTime();
		distanceToMove = DEFAULT_DISTANCE_TO_MOVE;
	}

	public void onMessageReceived(MessageEvent event) {

		log.info(event.getSender() + " sent a message: " + event.getMessage().getClass());

		Message message = (Message) event.getMessage();
		message.execute(this);
	}
}
