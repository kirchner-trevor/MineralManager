package me.hellfire212.MineralManager.utils;

import java.awt.Polygon;
import java.awt.Shape;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.List;

public final class ShapeUtils {
	private static final Point2D.Double ORIGIN = new Point2D.Double(0, 0);
	
	public static Shape shapeFromBounds(final List<Point2D.Double> points) {
		int maxp = points.size() - 1;
		List<Point2D.Double> myPoints = points;
		// Strip off zeroes from beginning and end
		if (ORIGIN.equals(points.get(0)) && ORIGIN.equals(points.get(maxp))) {
			myPoints = points.subList(1, maxp);
		}
		// Strip off redundant coordinate at end
		if (myPoints.get(0).equals(myPoints.get(myPoints.size()-1))) {
			myPoints = myPoints.subList(0, myPoints.size()-1);
		}
		if (myPoints.size() == 4 && isRectanglePoints(myPoints)) {
			return rectangleFromPoints(myPoints);
		} else {
			return polygonFromPoints(myPoints);
		}
	}
	
	private static Rectangle2D rectangleFromPoints(List<Point2D.Double> points) {
		double minX = points.get(0).getX();
		double minY = points.get(0).getY();
		double h = 0;
		double w = 0;
		for (Point2D.Double p: points) {
			if (p.getX() < minX) {
				minX = p.getX();
			} else {
				w = Math.max(w, p.getX() - minX);
			}
			if (p.getY() < minY) {
				minY = p.getY();
			} else {
				h = Math.max(h, p.getY() - minY);
			}
		}
		return new Rectangle2D.Double(minX, minY, w, h);
	}

	private static boolean isRectanglePoints(List<Point2D.Double> points) {
		return false;
	}
	
	private static Polygon polygonFromPoints(List<Point2D.Double> points) {
		Polygon poly = new Polygon();
		for (Point2D.Double p: points) {
			poly.addPoint((int)Math.round(p.getX()), (int)Math.round(p.getY()));
		}
		return poly;
	}
	public static ArrayList<Point2D.Double> pointsFromCompactBounds(Object boundaries) {
		ArrayList<Point2D.Double> points = new ArrayList<Point2D.Double>();
		java.lang.Double x = null;
		int i = 0;
		if (boundaries instanceof Collection<?>) {
			for (Object location: (Collection<?>) boundaries) {
				if (location instanceof java.lang.Double) {
					if ((i++ % 2) == 0) {
						x = (java.lang.Double) location;
					} else {
						points.add(new Point2D.Double(x, (java.lang.Double) location));
					}
				} else {
					System.out.printf("Expected a Double, got a %s (printed %s)", location.getClass().toString(), location.toString());
				}
			}
			return points;
		}
		return null;
	}

	/** I cannot be constructed */
	private ShapeUtils() {}
}
