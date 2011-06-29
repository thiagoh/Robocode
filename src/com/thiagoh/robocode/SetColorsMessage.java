package com.thiagoh.robocode;

import java.awt.Color;

public class SetColorsMessage implements Message {

	private Color body;
	private Color gun;
	private Color radar;
	private Color bullet;

	public SetColorsMessage(Color body, Color gun, Color radar, Color bullet) {
		this.body = body;
		this.gun = gun;
		this.radar = radar;
		this.bullet = bullet;
	}

	public Color getBody() {
		return body;
	}

	public Color getGun() {
		return gun;
	}

	public Color getRadar() {
		return radar;
	}

	public Color getBullet() {
		return bullet;
	}

	public void execute(ImRobot imRobot) {

		imRobot.setBodyColor(body);
		imRobot.setGunColor(gun);
		imRobot.setRadarColor(radar);
		imRobot.setBulletColor(bullet);
	}

}
