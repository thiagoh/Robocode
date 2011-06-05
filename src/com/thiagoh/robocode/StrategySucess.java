package com.thiagoh.robocode;

public class StrategySucess {

	private boolean use;
	private boolean active;
	private String name;
	private int successScore;
	private int faultScore;
	private double boundary;
	private int scanAfterNumFaults;

	public StrategySucess(String name, double boundary) {

		this(name, boundary, 5);
	}

	public StrategySucess(String name, double boundary, int scanAfterNumFaults) {

		this.use = false;
		this.active = true;
		this.name = name;
		this.successScore = 0;
		this.faultScore = 0;
		this.boundary = boundary;
		this.scanAfterNumFaults = scanAfterNumFaults;
	}

	public boolean getUsage() {
		return this.use;
	}

	public void setUsage(boolean usage) {
		this.use = usage;
	}

	public boolean isActive() {
		return active;
	}

	public void activate() {
		this.active = true;
	}

	public void deactivate() {
		this.active = false;
	}

	public String getName() {
		return name;
	}

	public void goal() {

		if (!isActive())
			return;

		successScore++;
	}

	public void fault() {

		if (!isActive())
			return;

		faultScore++;

		if (faultScore > scanAfterNumFaults
				&& (successScore <= 0 || ((faultScore * 100) / successScore) - 100 > boundary))
			deactivate();
	}
}
