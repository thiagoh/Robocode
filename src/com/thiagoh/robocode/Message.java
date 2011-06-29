package com.thiagoh.robocode;

import java.io.Serializable;

public interface Message extends Serializable {

	public void execute(ImRobot imRobot);
}
