package me.hellfire212.MineralManager;

import java.awt.geom.Point2D;
import java.util.ArrayList;

public class Selection {
	
	private ArrayList<Point2D.Double> boundaries = null;
	private double floor = 0.0;
	private double ceil = 0.0;
	
	public Selection() {
		boundaries = new ArrayList<Point2D.Double>();
	}
	
	public Selection(ArrayList<Point2D.Double> b, double f, double c) {
		boundaries = b;
		floor = f;
		ceil = c;
	}
	
	public void changeSelection(ArrayList<Point2D.Double> b, double f, double c) {
		boundaries = b;
		floor = f;
		ceil = c;
	}
	
	public ArrayList<Point2D.Double> getBoundaries() {
		return  boundaries;
	}
	
	public double getFloor() {
		return floor;
	}
	
	public double getCeil() {
		return ceil;
	}
}
