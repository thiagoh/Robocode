package com.thiagoh.robocode;

public class RouteImpl implements Route {

	private Turning turning;
	private double distance;

	public RouteImpl(Turning turning, double distance) {

		this.turning = turning;
		this.distance = distance;
	}

	public Turning getTurning() {
		return turning;
	}

	public void setTurning(Turning turning) {
		this.turning = turning;
	}

	public double getDistance() {
		return distance;
	}

	public void setDistance(double distance) {
		this.distance = distance;
	}

}
