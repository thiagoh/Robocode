package com.thiagoh.robocode;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import org.apache.commons.lang.ArrayUtils;
import org.drools.KnowledgeBase;
import org.drools.KnowledgeBaseFactory;
import org.drools.builder.KnowledgeBuilder;
import org.drools.builder.KnowledgeBuilderError;
import org.drools.builder.KnowledgeBuilderErrors;
import org.drools.builder.KnowledgeBuilderFactory;
import org.drools.builder.ResourceType;
import org.drools.io.ResourceFactory;
import org.drools.logger.KnowledgeRuntimeLogger;
import org.drools.logger.KnowledgeRuntimeLoggerFactory;
import org.drools.runtime.StatefulKnowledgeSession;

import robocode.BulletHitEvent;
import robocode.BulletMissedEvent;
import robocode.HitByBulletEvent;
import robocode.HitRobotEvent;
import robocode.HitWallEvent;
import robocode.MessageEvent;
import robocode.MoveCompleteCondition;
import robocode.Rules;
import robocode.ScannedRobotEvent;
import robocode.StatusEvent;
import robocode.TurnCompleteCondition;
import robocode.util.Utils;

public class CrazyRobot extends ImRobot {

	private static Logger log = Logger.getLogger(CrazyRobot.class.getName());

	private final static double DEFAULT_DISTANCE_TO_MOVE = 100;
	private double distanceToMove = DEFAULT_DISTANCE_TO_MOVE;
	private double xAxisDistancePermitted = 50;
	private double yAxisDistancePermitted = 50;
	private long lastBulletFiredTime;

	private Map<String, StrategySucess> advanceGunStrategyMap;
	private Map<String, StrategySucess> beCloseToEnemyStrategyMap;

	private KnowledgeBase knowledgeBase;
	private StatefulKnowledgeSession knowledgeSession;
	private KnowledgeRuntimeLogger logger;

	public CrazyRobot() {

	}

	public void run() {

		init();

		try {

			knowledgeBase = readKnowledgeBase();

			knowledgeSession = knowledgeBase.newStatefulKnowledgeSession();

			logger = KnowledgeRuntimeLoggerFactory.newFileLogger(knowledgeSession, "test");

			knowledgeSession.setGlobal("log", log);

			knowledgeSession.insert(this);
			knowledgeSession.insert(getStats());

		} catch (Exception e) {

			log.severe(e.getMessage());
		}

		advanceGunStrategyMap = new HashMap<String, StrategySucess>();
		beCloseToEnemyStrategyMap = new HashMap<String, StrategySucess>();

		setBodyColor(Color.red);
		setGunColor(Color.black);
		setRadarColor(Color.darkGray);
		setBulletColor(Color.orange);

		while (true) {

			movingForward = true;

			if (getTime() - lastBulletFiredTime > 100) {

				distanceToMove += 50;
				Route r = goToXY(Math.random() * getBattleFieldWidth(), Math.random() * getBattleFieldHeight());
				setTurn(r.getTurning());
				setAhead(r.getDistance());
				waitFor(new MoveCompleteCondition(this, 50));

			} else {

				ahead(distanceToMove);
				back(distanceToMove);
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

	private KnowledgeBase readKnowledgeBase() throws Exception {

		synchronized (CrazyRobot.class) {

			KnowledgeBuilder knowledgeBuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();

			knowledgeBuilder.add(ResourceFactory.newClassPathResource("Sample.drl"), ResourceType.DRL);

			KnowledgeBuilderErrors errors = knowledgeBuilder.getErrors();

			if (errors.size() > 0) {

				for (KnowledgeBuilderError error : errors)
					System.err.println(error);

				throw new IllegalArgumentException("Could not parse knowledge.");
			}

			KnowledgeBase kbase = KnowledgeBaseFactory.newKnowledgeBase();

			kbase.addKnowledgePackages(knowledgeBuilder.getKnowledgePackages());

			return kbase;
		}
	}

	public void onBulletMissed(BulletMissedEvent event) {

		log.info("Looking for: " + event.getBullet().getHeading());

		String target = getStats().retrieveBulletFired((int) event.getBullet().getHeading());

		if (target != null) {

			Double power = getStats().getPower(target);

			power = power * 0.7;

			if (power < Rules.MIN_BULLET_POWER)
				power = Rules.MIN_BULLET_POWER;

			log.info("Decrease power: " + power + " " + target);

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
			power = Rules.MIN_BULLET_POWER;

		power = power * 1.4;

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

		if (event.getName().indexOf(getClass().getSimpleName()) > 0)
			return;

		getStats().setPower(event.getName(), event.getBullet().getPower());

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

		if (knowledgeSession == null)
			log.fine("knowledgeSession is NULL");
		else {

			knowledgeSession.fireAllRules();
			// log.info("####################################### FIRE ALL RULES");
		}

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

	public void onScannedRobot(ScannedRobotEvent event) {

		if (isTeammate(event.getName()))
			return;

		Double power = getStats().getPower(event.getName());

		// log.info("Current POWER is " + power);

		if (power == null)
			power = Rules.MIN_BULLET_POWER;

		if (event.getDistance() < 50 && getEnergy() > 50)
			power += power * 0.8;

		getStats().setPower(event.getName(), power);

		double bearing = event.getBearing();

		if (bearing < 0)
			bearing = 360 + bearing;

		// log.info("Current BEARING is " + bearing);

		StrategySucess advanceGunStrategy = advanceGunStrategyMap.get(event.getName());

		if (advanceGunStrategy == null) {

			advanceGunStrategy = new StrategySucess("Avancar arma projetando movimento futuro do inimigo", 100);
			advanceGunStrategyMap.put(event.getName(), advanceGunStrategy);
		}

		TurnStrategy strategy = new AdvanceGunStrategy(advanceGunStrategy);
		bearing = strategy.getBearing(bearing, event.getDistance());

		double toDegrees = Utils.normalAbsoluteAngleDegrees(bearing + getHeading());

		Turning turning = turnToDirection(getGunHeading(), toDegrees);

		turnGun(turning);
		fireAntStamp(power);
		getStats().putBulletFired(toDegrees, event.getName());

		StrategySucess beCloseToEnemyStrategy = beCloseToEnemyStrategyMap.get(event.getName());

		if (beCloseToEnemyStrategy == null) {

			beCloseToEnemyStrategy = new StrategySucess("Chegar perto do inimigo quando errar mtos tiros", 100);
			beCloseToEnemyStrategyMap.put(event.getName(), beCloseToEnemyStrategy);
		}

		MoveStrategy moveStrategy = new MoveCloseToEnemyStrategy(this, beCloseToEnemyStrategy);
		moveStrategy.ahead(event.getBearing(), event.getDistance(), event.getName());
	}

	private void fireAntStamp(double power) {

		fire(power);

		lastBulletFiredTime = getTime();
		distanceToMove = DEFAULT_DISTANCE_TO_MOVE;
	}

	public void onMessageReceived(MessageEvent event) {

		Message message = (Message) event.getMessage();
		message.execute(this);
	}
}
