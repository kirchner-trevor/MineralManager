package me.hellfire212.MineralManager;

import java.awt.Shape;

public class Selection {
	private double floor = 0.0;
	private double ceil = 0.0;
	private Shape shape = null;
	
	public Selection(Shape shape, double floor, double ceil) {
		this.shape = shape;
		this.floor = floor;
		this.ceil = ceil;
	}
	
	public double getFloor() {
		return floor;
	}
	
	public double getCeil() {
		return ceil;
	}
	
	public enum Type {
		WORLD,
		CUBE,
		REGION,
		LASSO
	}

	public Shape getShape() {
		return shape;
	}
}
