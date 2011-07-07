package com.thiagoh.robocode;

public enum Quadrant {

//	@formatter:off
	
	FIRST,
	
	SECOND, 
	
	THIRD, 
	
	FOURTH, 
	
	AXIS;
	
//	@formatter:on

	public boolean after(Quadrant q, boolean clockwise) {

		if (clockwise) {

			if (FIRST.equals(this) && SECOND.equals(q)) {
				return true;
			} else if (FOURTH.equals(this) && FIRST.equals(q)) {
				return true;
			} else if (THIRD.equals(this) && FOURTH.equals(q)) {
				return true;
			} else if (SECOND.equals(this) && THIRD.equals(q)) {
				return true;
			} else
				return false;

		} else {

			if (FIRST.equals(this) && FOURTH.equals(q)) {
				return true;
			} else if (SECOND.equals(this) && FIRST.equals(q)) {
				return true;
			} else if (THIRD.equals(this) && SECOND.equals(q)) {
				return true;
			} else if (FOURTH.equals(this) && THIRD.equals(q)) {
				return true;
			} else
				return false;
		}
	}
}
