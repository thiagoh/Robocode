package com.thiagoh.robocode;

public class Range {

	private Double heading;

	public Range(double heading) {
		this.heading = heading;
	}

	public Double getHeading() {
		return heading;
	}

	public int hashCode() {
		return heading.hashCode();
	}

	public boolean equals(Object obj) {

		if (obj instanceof Range == false)
			return false;

		Range r = (Range) obj;

		if (r.getHeading() - getHeading() <= 10)
			return true;

		return false;
	}
}
