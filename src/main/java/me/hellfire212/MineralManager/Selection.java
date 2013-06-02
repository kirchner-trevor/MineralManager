package me.hellfire212.MineralManager;

import java.awt.Shape;

public final class Selection {
	private final double floor;
	private final double ceil;
	private final Shape shape;
	
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

	public Shape getShape() {
		return shape;
	}
	
	public enum Type {
		WORLD,
		CUBE,
		REGION,
		LASSO
	}
}
