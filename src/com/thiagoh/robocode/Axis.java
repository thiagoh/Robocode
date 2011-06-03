package com.thiagoh.robocode;

public enum Axis {

	UP(0), LEFT(270), DOWN(180), RIGHT(90);

	private int value;

	private Axis(int value) {

		this.value = value;
	}

	public int getValue() {
		
		return value;
	}
}
