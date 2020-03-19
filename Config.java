
public class Config {
	
	private static double rad5 = Math.toRadians(5);
	private static double rad10 = Math.toRadians(10);
	private static double rad45 = Math.toRadians(45);
	private static double rad160 = Math.toRadians(160);
	
	public static int FRAME_WIDTH = 800;
	public static int FRAME_HEIGHT = 300;
	public static int FRAME_PANEL_HEIGHT = 60;
	
	public static int CAR_WIDTH = 16;
	public static int CAR_HEIGHT = 32;
	public static int CAR_SPEED = 1;
	public static double CAR_DIRECTION = 0;
	public static double CAR_ROTATION_STEP = rad5;
	public static int CAR_ROTATION_CNT = (int) (Config.ULTRASONIC_ROTATION_ARC / 2 / Config.CAR_ROTATION_STEP);
	public static double CAR_ROTATION_REMINDER = Config.ULTRASONIC_ROTATION_ARC / 2 % Config.CAR_ROTATION_STEP;
	
	public static int INFRARED_RANGE = 16;
	public static int INFRARED_THRESHOLD = 12;
	public static double INFRARED_ANGLE_LEFT = -rad45;
	public static double INFRARED_ANGLE_RIGHT = rad45;
	
	public static int ULTRASONIC_RANGE = 50;
	public static int ULTRASONIC_THRESHOLD = 12;
	public static double ULTRASONIC_ROTATION_STEP = rad10;
	public static double ULTRASONIC_ROTATION_ARC = rad160;
	public static int ULTRASONIC_ROTATION_CNT = (int) (ULTRASONIC_ROTATION_ARC / 2 / ULTRASONIC_ROTATION_STEP);
	public static double ULTRASONIC_ROTATION_REMINDER = ULTRASONIC_ROTATION_ARC / 2 % ULTRASONIC_ROTATION_STEP;
	
}
