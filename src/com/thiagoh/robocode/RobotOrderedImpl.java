package com.thiagoh.robocode;

public class RobotOrderedImpl implements RobotOrdered {

	private String name;
	private double index;

	public RobotOrderedImpl(String name) {
		this(name, 0);
	}

	public RobotOrderedImpl(String name, double index) {
		this.name = name;
		this.index = index;
	}

	public int hashCode() {
		return getName().hashCode();
	}

	public boolean equals(Object obj) {

		if (obj instanceof RobotOrdered == false)
			return false;

		RobotOrdered robot = (RobotOrdered) obj;

		return robot.getName().equals(getName());
	}

	public int compareTo(RobotOrdered o) {

		return Double.compare(getIndex(), o.getIndex());
	}

	public String getName() {
		return name;
	}

	public double getIndex() {
		return index;
	}

	public String toString() {
		return getName();
	}
}
