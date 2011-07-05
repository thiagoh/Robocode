package com.thiagoh.robocode;

import java.io.Serializable;

public interface RobotOrdered extends Comparable<RobotOrdered>, Serializable {

	public String getName();

	public double getIndex();
}
