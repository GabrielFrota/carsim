import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Properties;

public class Config {
	
	private static double rad5 = Math.toRadians(5);
	private static double rad10 = Math.toRadians(10);
	private static double rad45 = Math.toRadians(45);
	private static double rad160 = Math.toRadians(160);
	
	public static int FRAME_WIDTH = 800;
	public static int FRAME_HEIGHT = 340;
	public static int FRAME_PANEL_HEIGHT = 60;
	
	public static volatile int ULTRASONIC_RANGE = 50;
	public static volatile int ULTRASONIC_THRESHOLD = 12;
	public static volatile double ULTRASONIC_ROTATION_STEP = rad10;
	public static volatile double ULTRASONIC_ROTATION_ARC = rad160;
	public static volatile int ULTRASONIC_ROTATION_CNT = (int) (ULTRASONIC_ROTATION_ARC / 2 / ULTRASONIC_ROTATION_STEP);
	public static volatile double ULTRASONIC_ROTATION_REMINDER = ULTRASONIC_ROTATION_ARC / 2 % ULTRASONIC_ROTATION_STEP;
	
	public static volatile int CAR_WIDTH = 16;
	public static volatile int CAR_HEIGHT = 32;
	public static volatile int CAR_SPEED = 1;
	public static volatile double CAR_DIRECTION = 0;
	public static volatile double CAR_ROTATION_STEP = rad5;
	public static volatile int CAR_ROTATION_CNT = (int) (Config.ULTRASONIC_ROTATION_ARC / 2 / Config.CAR_ROTATION_STEP);
	public static volatile double CAR_ROTATION_REMINDER = Config.ULTRASONIC_ROTATION_ARC / 2 % Config.CAR_ROTATION_STEP;
	public static volatile boolean CAR_COLLISION_CHECK_ON = true;
	public static volatile boolean CAR_SENSOR_ON = true;
	
	public static volatile int INFRARED_RANGE = 16;
	public static volatile int INFRARED_THRESHOLD = 12;
	public static volatile double INFRARED_ANGLE_LEFT = -rad45;
	public static volatile double INFRARED_ANGLE_RIGHT = rad45;
	
	public static void setFieldFromProps(Field f, Properties props) 
			throws IllegalAccessException, IllegalArgumentException {
		
		String p = (String) props.get(f.getName());
		if (p == null)
			throw new IllegalArgumentException("invalid properties file");
		
		if (f.getType() == Integer.TYPE)
			f.setInt(null, Integer.parseInt(p));
		else if (f.getType() == Double.TYPE)
			f.setDouble(null, Double.parseDouble(p));
		else if (f.getType() == Boolean.TYPE)
			f.setBoolean(null, Boolean.parseBoolean(p));
		else
			throw new AssertionError("Unexpected field type for parameter");
	}
	
	public static void init() {
		File f = new File("carsim.conf");
		if (f.exists()) {
			try {
				Properties props = new Properties();
				BufferedReader in = new BufferedReader(new FileReader(f));
				props.load(in);
				for (Field field : Config.class.getFields()) {
					if (Modifier.isPublic(field.getModifiers()))
						setFieldFromProps(field, props);
				}
			} catch (Exception ex) {
				throw new AssertionError(ex);
			}
		} 
	}
	
}
