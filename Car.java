import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.util.LinkedList;
import java.util.Queue;

public class Car {

	private static Path2D carRect;
	private static int width;
	private static int height;
	private static double direction;
	private static int speed;

	private static Path2D leftInfraredLine;
	private static int leftInfraredDist;
	private static Path2D rightInfraredLine;
	private static int rightInfraredDist;
	private static int infraredRange;
	private static int infraredThreshold;

	private static Path2D ultrasonicLine;
	private static double ultrasonicDirection;
	private static int ultrasonicDist;
	private static int ultrasonicDistLeft;
	private static int ultrasonicDistRight;
	private static int ultrasonicRange;
	private static int ultrasonicThreshold;

	private static double rad45 = Math.toRadians(45);
	private static double rad10 = Math.toRadians(10);
	private static double rad5 = Math.toRadians(5);
	
	static class CycleParams {
		public double rotation;
		public double ultrasonicRotation;
		public int speed;
		
		public CycleParams(double rotation, double ultrasonicRotation, int speed) {
			this.rotation = rotation;
			this.ultrasonicRotation = ultrasonicRotation;
			this.speed = speed;
		}
	}
	
	private static final Queue<CycleParams> paramsQueue = new LinkedList<CycleParams>();
	
	private static void enqueueParams(CycleParams cp, int cnt) {
		for (int i = 0; i < cnt; i++)
			paramsQueue.add(cp);
	}
	
	private static void enqueueUltrasonicRotate() {
		ultrasonicDistLeft = 0;
		ultrasonicDistRight = 0;
		enqueueParams(new CycleParams(0, -rad10, 0), 7);
		enqueueParams(new CycleParams(0, -rad5, 0), 1);
	}
	
	private static void ultrasonicCheck() {
		if (ultrasonicDistLeft == 0 && speed == 0) {
			ultrasonicDistLeft = ultrasonicDist;
			enqueueParams(new CycleParams(0, rad10, 0), 7 * 2);
			enqueueParams(new CycleParams(0, rad5, 0), 1 * 2);
		} else if (ultrasonicDistRight == 0 && speed == 0) { 
			ultrasonicDistRight = ultrasonicDist;
			enqueueParams(new CycleParams(0, -rad10, 0), 7);
			enqueueParams(new CycleParams(0, -rad5, 0), 1);
		}
	}
	
	private static void rotateLeft() {
		enqueueParams(new CycleParams(-rad5, 0, 0), 14);
		enqueueParams(new CycleParams(-rad5, 0, 1), 1);
	}
	
	private static void rotateRight() {
		enqueueParams(new CycleParams(rad5, 0, 0), 14);
		enqueueParams(new CycleParams(rad5, 0, 1), 1);
	}

	public static void oneCycle() {
		if (carRect == null)
			return;
		
		CycleParams params = null;
		if (!paramsQueue.isEmpty()) {
			params = paramsQueue.remove();
			
			if (params.rotation != 0) {
				direction = getDirectionVal(direction, params.rotation);
				AffineTransform at = AffineTransform.getRotateInstance(params.rotation, 
						carRect.getBounds2D().getCenterX(), carRect.getBounds2D().getCenterY());
				carRect.transform(at);
			}
			if (params.ultrasonicRotation != 0) {
				ultrasonicDirection = getDirectionVal(ultrasonicDirection, params.ultrasonicRotation);
				double[] p1 = getFirstMoveTo(ultrasonicLine);
				AffineTransform at = AffineTransform.getRotateInstance(
						params.ultrasonicRotation, p1[0], p1[1]);
				ultrasonicLine.transform(at);
			}
			speed = params.speed;		
		}
			
		double x = 0;
		double y = -speed;
		AffineTransform at = AffineTransform.getTranslateInstance(
				x * Math.cos(direction) - y * Math.sin(direction),
				x * Math.sin(direction) + y * Math.cos(direction));
		carRect.transform(at);
					
		PathIterator pi = carRect.getPathIterator(null);
		double[][] points = new double[4][2];
		int i = 0;
		while (!pi.isDone()) {
			double[] coords = new double[6];
			int segType = pi.currentSegment(coords);
			if (segType == PathIterator.SEG_MOVETO) {
				points[i][0] = coords[0];
				points[i][1] = coords[1];
				i++;
			}
			pi.next();
		}
		int[] arr = bresenham((int) points[0][0], (int) points[0][1], 
				(int) points[1][0], (int) points[1][1]);
		boolean frontCollision = arr[0] != (int) points[1][0] 
				|| arr[1] != (int) points[1][1];
		arr = bresenham((int) points[1][0], (int) points[1][1], 
				(int) points[2][0], (int) points[2][1]);
		boolean rightCollision = arr[0] != (int) points[2][0] 
				|| arr[1] != (int) points[2][1];
		arr = bresenham((int) points[2][0], (int) points[2][1], 
				(int) points[3][0], (int) points[3][1]);
		boolean backCollision = arr[0] != (int) points[3][0] 
				|| arr[1] != (int) points[3][1];
		arr = bresenham((int) points[3][0], (int) points[3][1], 
				(int) points[0][0], (int) points[0][1]);
		boolean leftCollision = arr[0] != (int) points[0][0] 
				|| arr[1] != (int) points[0][1];
		
		if (frontCollision || rightCollision || backCollision || leftCollision) {
			paramsQueue.clear();
			if (frontCollision) {
				enqueueParams(new CycleParams((params != null ? Math.signum(params.rotation) * -rad10 : -rad10), 0, -2), 2);
			} else if (leftCollision) {
				enqueueParams(new CycleParams((params != null ? Math.signum(params.rotation) * -rad10 : rad10), 0, -1), 2);
			} else if (rightCollision) {
				enqueueParams(new CycleParams((params != null ? Math.signum(params.rotation) * -rad10 : -rad10), 0, -1), 2);
			} else if (backCollision) {
				enqueueParams(new CycleParams((params != null ? Math.signum(params.rotation) * -rad10 : rad10), 0, 2), 2);
			}
			enqueueUltrasonicRotate();
		}
		
		double[] leftInfraredP1 = getFirstMoveTo(carRect);
		Point2D leftInfraredP2 = new Point2D.Double(leftInfraredP1[0], leftInfraredP1[1] - infraredRange);
		at = AffineTransform.getRotateInstance(direction - rad45, leftInfraredP1[0], leftInfraredP1[1]);
		at.transform(leftInfraredP2, leftInfraredP2);
		arr = bresenham((int) leftInfraredP1[0], (int) leftInfraredP1[1], 
				(int) leftInfraredP2.getX(), (int) leftInfraredP2.getY()); 
		leftInfraredLine.reset();
		leftInfraredLine.moveTo(leftInfraredP1[0], leftInfraredP1[1]);
		leftInfraredLine.lineTo(arr[0], arr[1]);
		leftInfraredLine.closePath();
		leftInfraredDist = arr[2];
		
		double[] rightInfraredP1 = getSecondMoveTo(carRect);
		Point2D rightInfraredP2 = new Point2D.Double(rightInfraredP1[0], rightInfraredP1[1] - infraredRange);
		at = AffineTransform.getRotateInstance(direction + rad45, rightInfraredP1[0], rightInfraredP1[1]);
		at.transform(rightInfraredP2, rightInfraredP2);
		arr = bresenham((int) rightInfraredP1[0], (int) rightInfraredP1[1], 
				(int) rightInfraredP2.getX(), (int) rightInfraredP2.getY());
		rightInfraredLine.reset();
		rightInfraredLine.moveTo(rightInfraredP1[0], rightInfraredP1[1]);
		rightInfraredLine.lineTo(arr[0], arr[1]);
		rightInfraredLine.closePath();
		rightInfraredDist = arr[2];

		double[] ultrasonicP1 = new double[2];
		ultrasonicP1[0] = (leftInfraredP1[0] + rightInfraredP1[0]) / 2;
		ultrasonicP1[1] = (leftInfraredP1[1] + rightInfraredP1[1]) / 2;
		Point2D ultrasonicP2 = new Point2D.Double(ultrasonicP1[0], ultrasonicP1[1] - ultrasonicRange);
		at = AffineTransform.getRotateInstance(direction + ultrasonicDirection, ultrasonicP1[0], ultrasonicP1[1]);
		at.transform(ultrasonicP2, ultrasonicP2);
		arr = bresenham((int) ultrasonicP1[0], (int) ultrasonicP1[1], (int) ultrasonicP2.getX(),
				(int) ultrasonicP2.getY());
		ultrasonicLine.reset();
		ultrasonicLine.moveTo(ultrasonicP1[0], ultrasonicP1[1]);
		ultrasonicLine.lineTo(arr[0], arr[1]);
		ultrasonicLine.closePath();
		ultrasonicDist = arr[2];
		
		if (!paramsQueue.isEmpty())
			return;
		
		ultrasonicCheck();
				
		if (leftInfraredDist <= infraredThreshold && rightInfraredDist > infraredThreshold
			&& speed != 0) {
			enqueueParams(new CycleParams(rad5, 0, speed), 1);
		} else if (rightInfraredDist <= infraredThreshold && leftInfraredDist > infraredThreshold
					&& speed != 0) {
			enqueueParams(new CycleParams(-rad5, 0, speed), 1);
		}

		if (speed != 0 && ultrasonicDist <= ultrasonicThreshold) {
			enqueueUltrasonicRotate();
		}
		
		if (speed == 0 && ultrasonicDistLeft != 0 && ultrasonicDistRight != 0) {
			if (ultrasonicDistLeft >= ultrasonicDistRight) {
				rotateLeft();
			} else {
				rotateRight();
			}
		}
	}

	public static void init(int x, int y) {
		direction = 0;
		speed = 1;
		width = 16;
		height = 32;
		carRect = new Path2D.Double();
		double halfW = width / 2;
		double halfH = height / 2;
		carRect.moveTo(x - halfW, y - halfH);
		carRect.lineTo(x + halfW, y - halfH);
		carRect.moveTo(x + halfW, y - halfH);
		carRect.lineTo(x + halfW, y + halfH);
		carRect.moveTo(x + halfW, y + halfH);
		carRect.lineTo(x - halfW, y + halfH);
		carRect.moveTo(x - halfW, y + halfH);
		carRect.lineTo(x - halfW, y - halfH);
		carRect.closePath();
		paramsQueue.clear();

		leftInfraredLine = new Path2D.Double();
		rightInfraredLine = new Path2D.Double();
		infraredRange = 16;
		infraredThreshold = 12;

		ultrasonicLine = new Path2D.Double();
		ultrasonicRange = 50;
		ultrasonicThreshold = 12;
		ultrasonicDirection = 0;
	}
	
	public static Path2D getUltrasonicLine() {
		return ultrasonicLine;
	}

	public static Path2D getCarRect() {
		return carRect;
	}

	public static Path2D getLeftInfraredLine() {
		return leftInfraredLine;
	}

	public static Path2D getRightInfraredLine() {
		return rightInfraredLine;
	}

//	private static double[] getFirstLineTo(Path2D p) {
//		PathIterator pi = p.getPathIterator(null);
//		double[] point = new double[2];
//		while (!pi.isDone()) {
//			double[] coords = new double[6];
//			int segType = pi.currentSegment(coords);
//			if (segType == PathIterator.SEG_LINETO) {
//				point[0] = coords[0];
//				point[1] = coords[1];
//				return point;
//			}
//			pi.next();
//		}
//		return null;
//	}

	private static double[] getFirstMoveTo(Path2D p) {
		PathIterator pi = p.getPathIterator(null);
		double[] point = new double[2];
		while (!pi.isDone()) {
			double[] coords = new double[6];
			int segType = pi.currentSegment(coords);
			if (segType == PathIterator.SEG_MOVETO) {
				point[0] = coords[0];
				point[1] = coords[1];
				return point;
			}
			pi.next();
		}
		return null;
	}

	private static double[] getSecondMoveTo(Path2D p) {
		PathIterator pi = p.getPathIterator(null);
		double[] point = new double[2];
		int cnt = 0;
		while (!pi.isDone()) {
			double[] coords = new double[6];
			int segType = pi.currentSegment(coords);
			if (segType == PathIterator.SEG_MOVETO) {
				if (cnt == 1) {
					point[0] = coords[0];
					point[1] = coords[1];
					return point;
				}
				cnt++;
			}
			pi.next();
		}
		return null;
	}
	
//	private static double getDecrementVal(double rotation, double decrement) {
//		if (Math.abs(rotation) / decrement >= 1) 
//			return Math.signum(rotation) > 0 ? decrement : -decrement;
//		else
//			return rotation % decrement;	
//	}
	
	private static double getDirectionVal(double direction, double theta) {
		if (Math.signum(theta) > 0) 
			return (direction + theta) % (2 * Math.PI);
		else {
			if (direction + theta > 0)
				return direction + theta;
			else
				return (2 * Math.PI) + (direction + theta);
		}
	}
	
	private static int[] bresenham(int x1, int y1, int x2, int y2) {
		// delta of exact value and rounded value of the dependent variable
		int d = 0;
		int dx = Math.abs(x2 - x1);
		int dy = Math.abs(y2 - y1);
		int dx2 = 2 * dx; // slope scaling factors to
		int dy2 = 2 * dy; // avoid floating point
		int ix = x1 < x2 ? 1 : -1; // increment direction
		int iy = y1 < y2 ? 1 : -1;
		int x = x1;
		int y = y1;
		int cnt = 0;

		if (dx >= dy) {
			while (true) {
				cnt++;
				if (World.checkPoint(x, y) || x == x2)
					break;
				x += ix;
				d += dy2;
				if (d > dx) {
					y += iy;
					d -= dx2;
				}
			}
		} else {
			while (true) {
				cnt++;
				if (World.checkPoint(x, y) || (y == y2))
					break;
				y += iy;
				d += dx2;
				if (d > dy) {
					x += ix;
					d -= dy2;
				}
			}
		}
		
		int[] ret = {x, y, cnt};
		return ret;
	}
	
}
