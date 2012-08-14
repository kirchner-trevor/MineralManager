package me.hellfire212.MineralManager.utils;

import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;


public final class ShapeUtils {
	private static final Point2D.Double ORIGIN = new Point2D.Double(0, 0);
	
	public static Shape shapeFromBounds(final List<Point2D> points) {
		int maxp = points.size() - 1;
		List<Point2D> myPoints = points;
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
	
	/**
	 * Create a rectangle from a list of Point2D's.
	 * @param points a list of points.
	 * @return a new Rectangle2D instance.
	 */
	private static Rectangle2D rectangleFromPoints(List<Point2D> points) {
		double minX = points.get(0).getX();
		double minY = points.get(0).getY();
		double maxX = points.get(0).getX();
		double maxY = points.get(0).getY();
		for (Point2D p: points) {
			if (p.getX() < minX) minX = p.getX();
			if (p.getX() > maxX) maxX = p.getX();
			if (p.getY() < minY) minY = p.getY();
			if (p.getY() > maxY) maxY = p.getY();
		}
		return new Rectangle2D.Double(minX, minY, maxX-minX, maxY-minY);
	}

	/** Returns true if the given points make up a rectangle.
	 * 
	 *  This is done by noticing a simple property of rectangles, which is
	 *  that the four points comprise of only two unique X and two unique Y coords.
	 */
	private static boolean isRectanglePoints(List<Point2D> points) {
		HashSet<Integer> xPoints = new HashSet<Integer>();
		HashSet<Integer> yPoints = new HashSet<Integer>();
		for (Point2D p: points) {
			xPoints.add((int) p.getX());
			yPoints.add((int) p.getY());
		}
		return (xPoints.size() == 2 && yPoints.size() == 2);
	}
	
	private static Polygon polygonFromPoints(List<Point2D> myPoints) {
		Polygon poly = new Polygon();
		for (Point2D p: myPoints) {
			poly.addPoint((int)Math.round(p.getX()), (int)Math.round(p.getY()));
		}
		return poly;
	}
	
	public static Map<String, Object> serializeShape(Shape s) {
		if (s instanceof Rectangle2D) {
			return serializeRectangle((Rectangle2D) s);
		} else if (s instanceof Polygon) {
			return serializePolygon((Polygon) s);
		}
		return null;
	}

	private static Map<String, Object> serializePolygon(Polygon poly) {
		List<Integer> xPoints = new ArrayList<Integer>();
		List<Integer> yPoints = new ArrayList<Integer>();
		for (int i = 0; i < poly.npoints; i++) {
			xPoints.add(poly.xpoints[i]);
			yPoints.add(poly.ypoints[i]);
		}
		HashMap<String, Object> m = new HashMap<String, Object>();
		m.put("type", "POLY");
		m.put("xpoints", xPoints);
		m.put("ypoints", yPoints);
		return m;
	}

	private static Map<String, Object> serializeRectangle(Rectangle2D s) {
		HashMap<String, Object> m = new HashMap<String, Object>();
		m.put("type", "RECT");
		m.put("x", s.getX());
		m.put("y", s.getY());
		m.put("width", s.getWidth());
		m.put("height", s.getHeight());
		return m;
	}
	
	public static Shape deserializeShape(Map<String, Object> m) {
		Object type = m.get("type");
		if ("POLY".equals(type)) {
			return deserializePolygon(m);
		} else if ("RECT".equals(type)) {
			return deserializeRectangle(m);
		}
		return null;
	}

	private static Rectangle deserializeRectangle(Map<String, Object> m) {
		int x = ((Number) m.get("x")).intValue();
		int y = ((Number) m.get("y")).intValue();
		int w = ((Number) m.get("width")).intValue();
		int h = ((Number) m.get("height")).intValue();
		return new Rectangle(x, y, w, h);
	}

	private static Polygon deserializePolygon(Map<String, Object> m) {
		Polygon p = new Polygon();
		Object ox = m.get("xpoints");
		Object oy = m.get("ypoints");
		if (ox instanceof List<?> && oy instanceof List<?>) {
			List<?> lx = (List<?>) ox;
			List<?> ly = (List<?>) oy;
			for (int i = 0; i < lx.size(); i++) {
				Integer x = (Integer) lx.get(i);
				Integer y = (Integer) ly.get(i);
				p.addPoint(x, y);
			}
		}
		return p;
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

	public static String describeShape(Shape shape) {
		if (shape instanceof Rectangle2D) {
			Rectangle2D r = (Rectangle2D) shape;
			return String.format("Cuboid %dx%d", Math.round(r.getWidth()), Math.round(r.getHeight()));
		} else if (shape instanceof Polygon) {
			return String.format("Polygon (%d points)", ((Polygon) shape).npoints);
		}
		return "unknown shape";
	}

	/** Add a point to a polygon, converting datatypes as needed.
	 * 
	 * @param poly a Polygon
	 * @param p a Point2D
	 */
	public static void addPolyPoint(Polygon poly, Point2D p) {
		poly.addPoint((int) Math.round(p.getX()), (int) Math.round(p.getY()));
	}

	public static ArrayList<Point2D> reduceBoundaries(ArrayList<Point2D> boundaries) {
		int size = boundaries.size();
		int index = 0;
		while(index + 2 < size) {
			if(ShapeUtils.isBetween(boundaries.get(index), boundaries.get(index + 2), boundaries.get(index + 1))) {
				boundaries.remove(index + 1);
				size--;
			} else {
				index++;
			}
		}
		return boundaries;
	}

	public static boolean isBetween(Point2D a, Point2D b, Point2D c) {
		double epsilon = 0.05; //Threshold to determine whether a point is "on" the line.
		double cyMINUSay = c.getY() - a.getY();
		double bxMINUSax = b.getX() - a.getX();
		double cxMINUSax = c.getX() - a.getX();
		double byMINUSay = b.getY() - a.getY();
		double crossProduct = cyMINUSay * bxMINUSax - cxMINUSax * byMINUSay;
		if(Math.abs(crossProduct) > epsilon) {
			return false;
		}
		double dotProduct = cxMINUSax * bxMINUSax + cyMINUSay * byMINUSay;
		if(dotProduct < 0.0) {
			return false;
		}
		double squaredLengthBA = bxMINUSax * bxMINUSax + byMINUSay * byMINUSay;
		if(dotProduct > squaredLengthBA) {
			return false;
		}
		return true;
	}
	
	/** I cannot be constructed */
	private ShapeUtils() {}
}
