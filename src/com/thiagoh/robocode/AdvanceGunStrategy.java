package com.thiagoh.robocode;

import java.util.LinkedList;

public class AdvanceGunStrategy implements TurnStrategy {

	private LinkedList<Double[]> bearingClockwise = new LinkedList<Double[]>();
	private LinkedList<Double[]> bearingAntiClockwise = new LinkedList<Double[]>();

	private StrategySucess strategySucess;

	public AdvanceGunStrategy(StrategySucess strategySucess) {

		this.strategySucess = strategySucess;
	}

	public double getBearing(double currentBearing, double robotDistance) {

		if (!strategySucess.isActive())
			return currentBearing;

		bearingClockwise.push(new Double[] { currentBearing, robotDistance });
		bearingAntiClockwise.push(new Double[] { currentBearing, robotDistance });

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
					lastBearing = last[0];
				}

				cur = bearingClockwise.pollLast();
				curBearing = cur[0];

				if (!after(curBearing, lastBearing, true))
					alwaysBigger = false;
			}

			bearingClockwise = bearingClockwiseClone;
			bearingClockwise.pollLast();

			if (alwaysBigger) {

				strategySucess.setUsage(true);
				currentBearing += 20;

			} else {

				strategySucess.setUsage(false);
				boolean alwaysSmaller = true;

				for (int i = 1; i <= 3; i++) {

					lastBearing = curBearing;

					if (i == 1) {

						last = bearingAntiClockwise.pollLast();
						lastBearing = last[0];
					}

					cur = bearingAntiClockwise.pollLast();
					curBearing = cur[0];

					if (after(curBearing, lastBearing, false))
						alwaysSmaller = false;
				}

				if (alwaysSmaller) {

					strategySucess.setUsage(true);
					currentBearing -= 20;
				}
			}
		}

		if (currentBearing > 360)
			currentBearing = currentBearing - 360;

		return currentBearing;
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
