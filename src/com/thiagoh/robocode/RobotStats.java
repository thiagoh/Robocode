package com.thiagoh.robocode;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Logger;

public class RobotStats implements Serializable {

	private static Logger log = Logger.getLogger(RobotStats.class.getName());

	private int bulletsHit;
	private Map<String, Double> powerMap;
	private Map<Integer, String> bulletsFiredMap;
	private Map<String, Integer> bulletsMissedMap;

	public RobotStats() {

		bulletsHit = 0;
		powerMap = new HashMap<String, Double>();
		bulletsFiredMap = new HashMap<Integer, String>();
		bulletsMissedMap = new HashMap<String, Integer>();
	}

	public Integer getBulletsMissedCount(String robotname) {

		return bulletsMissedMap.get(robotname);
	}

	public void setBulletMissedCount(String robotname, int missedCount) {

		bulletsMissedMap.put(robotname, missedCount);
	}

	public Double getPower(String robotname) {

		return powerMap.get(robotname);
	}

	public void setPower(String robotname, Double power) {

		powerMap.put(robotname, power);
	}

	public int getBulletsHit() {

		return bulletsHit;
	}

	public String retrieveBulletFired(double heading) {

		return bulletsFiredMap.remove(heading);
	}

	public void putBulletFired(double heading, String robotname) {

		log.info("inserting: " + heading);

		bulletsFiredMap.put((int) heading, robotname);
	}

	public void increaseBulletPower() {

		for (Entry<String, Double> entry : powerMap.entrySet()) {

			powerMap.put(entry.getKey(), entry.getValue() * 1.5);
		}
	}
}