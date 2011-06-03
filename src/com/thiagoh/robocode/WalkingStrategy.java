package com.thiagoh.robocode;

import robocode.HitByBulletEvent;
import robocode.HitRobotEvent;
import robocode.HitWallEvent;
import robocode.ScannedRobotEvent;
import robocode.StatusEvent;

public interface WalkingStrategy {

	public void main();

	public void onStatus(StatusEvent e);

	public void onHitWall(HitWallEvent event);

	public void onHitRobot(HitRobotEvent event);

	public void onHitBullet(HitByBulletEvent event);

	public void onScannedRobot(ScannedRobotEvent event);
}
