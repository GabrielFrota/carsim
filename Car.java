import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;
import java.awt.geom.PathIterator;

public class Car {

	private static Path2D carRect;
	private static int width;
	private static int height;
	private static double direction;
	private static double rotation;
	private static int speed;

	private static Path2D leftInfraredLine;
	private static int leftInfraredDist;
	private static Path2D rightInfraredLine;
	private static int rightInfraredDist;
	private static int infraredRange;
	private static int infraredThreshold;

	private static Path2D ultrasonicLine;
	private static double ultrasonicDirection;
	private static double ultrasonicRotation;
	private static int ultrasonicDist;
	private static int ultrasonicRange;
	private static int ultrasonicThreshold;

	private static double rad45 = Math.toRadians(45);
	private static double rad30 = Math.toRadians(30);
	private static double rad15 = Math.toRadians(15);
	private static double rad5 = Math.toRadians(5);

	public static void oneCycle() {
		//		uint16_t left_infrared, right_infrared;
		//		uint16_t ultra_front, ultra_left, ultra_right;
		//		left_infrared = hbot.mInfraredAvoidance->GetInfraredAvoidanceLeftValue();
		//		right_infrared = hbot.mInfraredAvoidance->GetInfraredAvoidanceRightValue();
		//		ultra_front = hbot.mUltrasonic->GetUltrasonicFrontDistance();
		//		/*DEBUG_LOG(DEBUG_LEVEL, "========== \n");
		//		DEBUG_LOG(DEBUG_LEVEL, "left_infrared=%d \n", left_infrared);
		//		DEBUG_LOG(DEBUG_LEVEL, "right_infrared=%d \n", right_infrared);
		//		DEBUG_LOG(DEBUG_LEVEL, "ultra_front=%d \n", ultra_front);*/

		//		if ((right_infrared >= IA_THRESHOLD) && (left_infrared <= IA_THRESHOLD))
		//		{
		//			hbot.SetSpeed(80);
		//			hbot.Drive(10);
		//		}
		//		else if ((right_infrared < IA_THRESHOLD) && (left_infrared > IA_THRESHOLD))
		//		{
		//			hbot.SetSpeed(80);
		//			hbot.Drive(170);
		//		}
		//		else
		//		{
		//			hbot.SetSpeed(40);
		//			hbot.GoForward();
		//		}
		//
		//		while (ultra_front < UL_LIMIT_MID)
		//		{
		//			if (hbot.GetSpeed() != E_STOP)
		//			{
		//				hbot.SetSpeed(40);
		//				hbot.GoBack();
		//				delay(200);
		//				hbot.KeepStop();
		//			}
		//			uint16_t ultra_right = hbot.mUltrasonic->GetUltrasonicRightDistance();
		//			uint16_t ultra_left = hbot.mUltrasonic->GetUltrasonicLeftDistance();
		//
		//			if (ultra_right >= ultra_left)
		//			{
		//				hbot.SetSpeed(100);
		//				hbot.TurnRight();
		//				delay(310);
		//			}
		//			if (ultra_left > ultra_right)
		//			{
		//				hbot.SetSpeed(100);
		//				hbot.TurnLeft();
		//				delay(310);
		//			}
		//			if (ultra_left <= UL_LIMIT_MIN && ultra_right <= UL_LIMIT_MIN)
		//			{
		//				hbot.SetSpeed(100);
		//				hbot.TurnLeft();
		//				delay(660);
		//			}
		//			hbot.KeepStop();
		//			ultra_front = hbot.mUltrasonic->GetUltrasonicFrontDistance();
		//		}

//		if (ultrasonicRotation != 0) {
//			if (Math.abs(ultrasonicRotation) / rad15 >= 1) {
//				double rot = Math.signum(ultrasonicRotation) > 0 ? rad15 : -rad15;
//				ultrasonicRotation -= rot;
//				rotateUltrasonic(rot);
//			}
//			else {
//				double reminder = ultrasonicRotation % rad15;
//				rotation -= reminder;
//				rotateUltrasonic(reminder);
//			}
//		}

		if (rotation != 0) {
			if (Math.abs(rotation) / rad5 >= 1) {
				double rot = Math.signum(rotation) > 0 ? rad5 : -rad5;
				rotation -= rot;
				rotateCar(rot);
			}
			else {
				double reminder = rotation % rad5;
				rotation -= reminder;
				rotateCar(reminder);
			}
		}

		double x = 0;
		double y = -speed;
		var at = AffineTransform.getTranslateInstance(x * Math.cos(direction) - y * Math.sin(direction),
				x * Math.sin(direction) + y * Math.cos(direction));
		carRect.transform(at);

		double[] leftInfraredP1 = getFirstMoveTo(carRect);
		leftInfraredLine.reset();
		leftInfraredLine.moveTo(leftInfraredP1[0], leftInfraredP1[1]);
		leftInfraredLine.lineTo(leftInfraredP1[0], leftInfraredP1[1] - infraredRange);
		leftInfraredLine.closePath();
		at = AffineTransform.getRotateInstance(direction - rad45, leftInfraredP1[0], leftInfraredP1[1]);
		leftInfraredLine.transform(at);
		double[] leftInfraredP2 = getFirstLineTo(leftInfraredLine);
		leftInfraredDist = bresenham((int) leftInfraredP1[0], (int) leftInfraredP1[1], (int) leftInfraredP2[0],
				(int) leftInfraredP2[1]);

		double[] rightInfraredP1 = getSecondMoveTo(carRect);
		rightInfraredLine.reset();
		rightInfraredLine.moveTo(rightInfraredP1[0], rightInfraredP1[1]);
		rightInfraredLine.lineTo(rightInfraredP1[0], rightInfraredP1[1] - infraredRange);
		rightInfraredLine.closePath();
		at = AffineTransform.getRotateInstance(direction + rad45, rightInfraredP1[0], rightInfraredP1[1]);
		rightInfraredLine.transform(at);
		double[] rightInfraredP2 = getFirstLineTo(rightInfraredLine);
		rightInfraredDist = bresenham((int) rightInfraredP1[0], (int) rightInfraredP1[1], (int) rightInfraredP2[0],
				(int) rightInfraredP2[1]);

		double[] ultrasonicP1 = new double[2];
		ultrasonicP1[0] = (leftInfraredP1[0] + rightInfraredP1[0]) / 2;
		ultrasonicP1[1] = (leftInfraredP1[1] + rightInfraredP1[1]) / 2;
		ultrasonicLine.reset();
		ultrasonicLine.moveTo(ultrasonicP1[0], ultrasonicP1[1]);
		ultrasonicLine.lineTo(ultrasonicP1[0], ultrasonicP1[1] - ultrasonicRange);
		ultrasonicLine.closePath();
		at = AffineTransform.getRotateInstance(direction + ultrasonicDirection, ultrasonicP1[0], ultrasonicP1[1]);
		ultrasonicLine.transform(at);
		double[] ultrasonicP2 = getFirstLineTo(ultrasonicLine);
		ultrasonicDist = bresenham((int) ultrasonicP1[0], (int) ultrasonicP1[1], (int) ultrasonicP2[0],
				(int) ultrasonicP2[1]);

		if (leftInfraredDist <= infraredThreshold && rightInfraredDist > infraredThreshold) {
			rotation = rad30;
		}
		else if (rightInfraredDist <= infraredThreshold && leftInfraredDist > infraredThreshold) {
			rotation = -rad30;
		}

//		if (speed != 0 && ultrasonicDist <= ultrasonicThreshold) {
//			speed = 0;
//			ultrasonicRotation = -rad45;
//		}

	}

	public static void init(int x, int y) {
		direction = 0;
		speed = 1;
		width = 16;
		height = 32;
		carRect = new Path2D.Double();
		carRect.moveTo(x - width / 2, y - height / 2);
		carRect.lineTo(x + width / 2, y - height / 2);
		carRect.moveTo(x + width / 2, y - height / 2);
		carRect.lineTo(x + width / 2, y + height / 2);
		carRect.moveTo(x + width / 2, y + height / 2);
		carRect.lineTo(x - width / 2, y + height / 2);
		carRect.moveTo(x - width / 2, y + height / 2);
		carRect.lineTo(x - width / 2, y - height / 2);
		carRect.closePath();

		leftInfraredLine = new Path2D.Double();
		rightInfraredLine = new Path2D.Double();
		infraredRange = 16;
		infraredThreshold = 12;

		ultrasonicLine = new Path2D.Double();
		ultrasonicRange = 50;
		ultrasonicThreshold = 12;
	}

	public static double[] getFirstLineTo(Path2D p) {
		PathIterator pi = p.getPathIterator(null);
		double[] point = new double[2];
		while (!pi.isDone()) {
			double[] coords = new double[6];
			int segType = pi.currentSegment(coords);
			if (segType == PathIterator.SEG_LINETO) {
				point[0] = coords[0];
				point[1] = coords[1];
				return point;
			}
			pi.next();
		}
		return null;
	}

	public static double[] getFirstMoveTo(Path2D p) {
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

	public static double[] getSecondMoveTo(Path2D p) {
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

	public static void rotateCar(double theta) {
		if (theta == 0)
			return;

		if (Math.signum(theta) > 0) {
			direction = (direction + theta) % (2 * Math.PI);
		}
		else {
			if (direction + theta > 0)
				direction += theta;
			else
				direction = (2 * Math.PI) + (direction + theta);
		}

		var at = AffineTransform.getRotateInstance(theta, carRect.getBounds2D().getCenterX(),
				carRect.getBounds2D().getCenterY());
		carRect.transform(at);
	}

	public static void rotateUltrasonic(double theta) {
		if (theta == 0)
			return;

		if (Math.signum(theta) > 0) {
			ultrasonicDirection = (ultrasonicDirection + theta) % (2 * Math.PI);
		}
		else {
			if (ultrasonicDirection + theta > 0)
				ultrasonicDirection += theta;
			else
				ultrasonicDirection = (2 * Math.PI) + (ultrasonicDirection + theta);
		}

		double[] p1 = getFirstMoveTo(ultrasonicLine);
		var at = AffineTransform.getRotateInstance(theta, p1[0], p1[1]);
		ultrasonicLine.transform(at);
	}

	private static int bresenham(int x1, int y1, int x2, int y2) {
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
		}
		else {
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

		return cnt;
	}

}
