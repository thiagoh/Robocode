package com.thiagoh.robocode;

public class TurningImpl implements Turning {

	private Rotation rotation;
	private double value;

	public TurningImpl() {
		
	}

	public TurningImpl(Rotation rotation, double value) {
		this.rotation = rotation;
		this.value = value;
	}

	public Rotation getRotation() {
		return rotation;
	}

	public void setRotation(Rotation rotation) {
		this.rotation = rotation;
	}

	public double getValue() {
		return value;
	}

	public void setValue(double value) {
		this.value = value;
	}

}
