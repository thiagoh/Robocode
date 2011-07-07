package com.thiagoh.robocode;

import static robocode.util.Utils.normalRelativeAngleDegrees;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

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
import robocode.TurnCompleteCondition;
import robocode.util.Utils;

public class CrazyRobot extends ImRobot {

	private static Logger log = Logger.getLogger(CrazyRobot.class);

	private static double DEFAULT_DISTANCE_TO_MOVE;
	private static double INITIAL_BULLET_POWER = 3.0;

	private double distanceToMove = DEFAULT_DISTANCE_TO_MOVE;

	private long lastBulletFiredTime;

	private Map<String, StrategySucess> beCloseToEnemyStrategyMap;

	public CrazyRobot() {

	}

	public void run() {

		init();
		setAdjustGunForRobotTurn(true);
		setAdjustRadarForGunTurn(true);

		double battleFieldWidth = getBattleFieldWidth();
		double battleFieldheight = getBattleFieldHeight();

		DEFAULT_DISTANCE_TO_MOVE = battleFieldWidth * 0.13;

		double[][] xyCorners = { { 50, getBattleFieldHeight() - 50 },
				{ getBattleFieldWidth() - 50, getBattleFieldWidth() - 50 }, { 50, 50 },
				{ getBattleFieldWidth() - 50, 50 } };

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

				removeAttribute("enemy");
				distanceToMove += 50;
				Route r = goToXY(Math.random() * battleFieldWidth, Math.random() * battleFieldheight);
				setTurn(r.getTurning());
				setAhead(r.getDistance());
				waitFor(new MoveCompleteCondition(this, 50));

			} else {

				int ix = (int) Math.floor(getIndex() * 4);

				if (Math.abs(xyCorners[ix][0] - getX()) > 40 || Math.abs(xyCorners[ix][1] - getY()) > 40) {

					Route r = goToXY(xyCorners[ix][0], xyCorners[ix][1]);
					setTurn(r.getTurning());
					setAhead(r.getDistance());
					waitFor(new TurnCompleteCondition(this));

				} else {

					setAhead(distanceToMove);
					setBack(distanceToMove);
					waitFor(new MoveCompleteCondition(this));
				}
			}

			setTurnRadarRight(10000);
		}
	}

	public void onHitRobot(HitRobotEvent event) {

		if (isTeammate(event.getName())) {

			reverseDirection();
			return;
		}

		double enemyBearing = this.getHeading() + event.getBearing();

		double enemyX = getX() + Math.sin(Math.toRadians(enemyBearing));
		double enemyY = getY() + Math.cos(Math.toRadians(enemyBearing));

		double dx = enemyX - this.getX();
		double dy = enemyY - this.getY();

		double theta = Math.toDegrees(Math.atan2(dx, dy));

		getStats().setPower(event.getName(), Rules.MAX_BULLET_POWER);
		turnGunRight(normalRelativeAngleDegrees(theta - getGunHeading()));
		fireAntStamp(Rules.MAX_BULLET_POWER);
		// setTurnRight(enemyBearing);
	}

	public void onHitWall(HitWallEvent event) {

		Turning turning = turnToDirection(Util.escapeFromWall(this));

		stop(true);
		setTurn(turning);
		setAhead(100);
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

		execute();
	}

	public void onBulletMissed(BulletMissedEvent event) {

		// log.info("Looking for: " + event.getBullet().getHeading());

		String target = getStats().retrieveBulletFired(event.getBullet().getHeading());

		if (target != null) {

			Double power = getStats().getPower(target) * 0.7;

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
	}

	public void onHitByBullet(HitByBulletEvent event) {

		if (!isTeammate(event.getName()))
			getStats().setPower(event.getName(), event.getBullet().getPower());

		double bearing = event.getBearing();

		setTurn(turnToDirection(Utils.normalAbsoluteAngleDegrees(bearing)));
		setAhead(100);
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

		// if (isLeader()) {
		//
		// if (enemy == null) {
		//
		// Message m = new SetEnemyMessage(getOrdered(), event.getName());
		// broadcast(m);
		// m.execute(this);
		// log.info("LEADER");
		// }
		// }

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
			power = Rules.MAX_BULLET_POWER;

		getStats().setPower(enemy, power);

		double bearing = Utils.normalAbsoluteAngleDegrees(event.getBearing());
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

	private void fireAntStamp(final double power) {
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
