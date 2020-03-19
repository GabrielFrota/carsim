import java.awt.Dimension;
import java.awt.FlowLayout;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.WindowConstants;

public class ConfigFrame extends JFrame {

	private static final long serialVersionUID = 1L;
	
	private final JPanel carP = new JPanel();
	private final JPanel infraredP = new JPanel();
	private final JPanel ultrasonicP = new JPanel();
	private final JPanel buttonsP = new JPanel();
	
	private final int size = 6;
	private final JLabel widthL = new JLabel("width:");
	private final JTextField widthF = new JTextField(size);
	private final JLabel heightL = new JLabel("height:");
	private final JTextField heightF = new JTextField(size);
	private final JLabel speedL = new JLabel("speed:");
	private final JTextField speedF = new JTextField(size);
	private final JLabel directL = new JLabel("direction:");
	private final JTextField directF = new JTextField(size);
	private final JLabel rotationL = new JLabel("rotation step:");
	private final JTextField rotationF = new JTextField(size);
	
	private final JLabel infraredRangeL = new JLabel("maximum range:");
	private final JTextField infraredRangeF = new JTextField(size);
	private final JLabel infraredThreshL = new JLabel("threshold value:");
	private final JTextField infraredThreshF = new JTextField(size);
	private final JLabel infraredAngleLeftL = new JLabel("left angle:");
	private final JTextField infraredAngleLeftF = new JTextField(size);
	private final JLabel infraredAngleRightL = new JLabel("right angle:");
	private final JTextField infraredAngleRightF = new JTextField(size);
	
	private final JLabel ultrasonicRangeL = new JLabel("maximum range:");
	private final JTextField ultrasonicRangeF = new JTextField(size);
	private final JLabel ultrasonicThresholdL = new JLabel("threshold value:");
	private final JTextField ultrasonicThresholdF = new JTextField(size);
	private final JLabel ultrasonicArcL = new JLabel("rotation arc size:");
	private final JTextField ultrasonicArcF = new JTextField(size);
	
	private final JButton saveConfB = new JButton("Save config to file");
	private final JButton loadConfB = new JButton("Load config from file");
	private final JButton saveWorldB = new JButton("Save world to file");
	private final JButton loadWorldB = new JButton("Load world from file");
	private final JButton okB = new JButton("OK");
	
	public ConfigFrame(int width, int height) {
		super();	
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		setTitle("Configurations");
		setSize(width, height);
		setLocationByPlatform(true);
		setLayout(new FlowLayout());
		
		Dimension dim = new Dimension(width, Config.FRAME_PANEL_HEIGHT);
		carP.setPreferredSize(dim);
		carP.setBorder(BorderFactory.createTitledBorder("Car Settings"));
		add(carP);
		carP.add(widthL);
		carP.add(widthF);
		carP.add(heightL);
		carP.add(heightF);
		carP.add(speedL);
		carP.add(speedF);
		carP.add(directL);
		carP.add(directF);
		carP.add(rotationL);
		carP.add(rotationF);
		widthF.setText(Integer.toString(Config.CAR_WIDTH));
		heightF.setText(Integer.toString(Config.CAR_HEIGHT));
		speedF.setText(Integer.toString(Config.CAR_SPEED));
		directF.setText(Double.toString(Math.toDegrees(Config.CAR_DIRECTION)));
		rotationF.setText(Double.toString(Math.toDegrees(Config.CAR_ROTATION_STEP)));
		
		infraredP.setPreferredSize(dim);
		infraredP.setBorder(BorderFactory.createTitledBorder("Infrared Settings"));
		add(infraredP);
		infraredP.add(infraredRangeL);
		infraredP.add(infraredRangeF);
		infraredP.add(infraredThreshL);
		infraredP.add(infraredThreshF);
		infraredP.add(infraredAngleLeftL);
		infraredP.add(infraredAngleLeftF);
		infraredP.add(infraredAngleRightL);
		infraredP.add(infraredAngleRightF);
		infraredRangeF.setText(Integer.toString(Config.INFRARED_RANGE));
		infraredThreshF.setText(Integer.toString(Config.INFRARED_THRESHOLD));
		infraredAngleLeftF.setText(Double.toString(Math.toDegrees(Config.INFRARED_ANGLE_LEFT)));
		infraredAngleRightF.setText(Double.toString(Math.toDegrees(Config.INFRARED_ANGLE_RIGHT)));
		
		ultrasonicP.setPreferredSize(dim);
		ultrasonicP.setBorder(BorderFactory.createTitledBorder("Ultrasonic Settings"));
		add(ultrasonicP);
		ultrasonicP.add(ultrasonicRangeL);
		ultrasonicP.add(ultrasonicRangeF);
		ultrasonicP.add(ultrasonicThresholdL);
		ultrasonicP.add(ultrasonicThresholdF);
		ultrasonicP.add(ultrasonicArcL);
		ultrasonicP.add(ultrasonicArcF);
		ultrasonicRangeF.setText(Integer.toString(Config.ULTRASONIC_RANGE));
		ultrasonicThresholdF.setText(Integer.toString(Config.ULTRASONIC_THRESHOLD));
		ultrasonicArcF.setText(Double.toString(Math.toDegrees(Config.ULTRASONIC_ROTATION_ARC)));
		
		buttonsP.setPreferredSize(dim);
		add(buttonsP);
		buttonsP.add(saveConfB);
		buttonsP.add(loadConfB);
		buttonsP.add(saveWorldB);
		buttonsP.add(loadWorldB);
		
		okB.addActionListener(e -> {
			try {
				Config.CAR_WIDTH = Integer.parseInt(widthF.getText(), 10);
				Config.CAR_HEIGHT = Integer.parseInt(heightF.getText(), 10);
				Config.CAR_SPEED = Integer.parseInt(speedF.getText());
				Config.CAR_DIRECTION = Math.toRadians(Double.parseDouble(directF.getText()));
				Config.CAR_ROTATION_STEP = Math.toRadians(Double.parseDouble(rotationF.getText()));	
				
				Config.INFRARED_RANGE = Integer.parseInt(infraredRangeF.getText(), 10);
				Config.INFRARED_THRESHOLD = Integer.parseInt(infraredThreshF.getText(), 10);
				Config.INFRARED_ANGLE_LEFT = Math.toRadians(Double.parseDouble(infraredAngleLeftF.getText()));
				Config.INFRARED_ANGLE_RIGHT = Math.toRadians(Double.parseDouble(infraredAngleRightF.getText()));
				
				Config.ULTRASONIC_RANGE = Integer.parseInt(ultrasonicRangeF.getText(), 10);
				Config.ULTRASONIC_THRESHOLD = Integer.parseInt(ultrasonicThresholdF.getText(), 10);		
				Config.ULTRASONIC_ROTATION_ARC = Math.toRadians(Double.parseDouble(ultrasonicArcF.getText()));
				Config.ULTRASONIC_ROTATION_CNT = (int) (Config.ULTRASONIC_ROTATION_ARC 
						/ 2 / Config.ULTRASONIC_ROTATION_STEP);
				Config.ULTRASONIC_ROTATION_REMINDER = Config.ULTRASONIC_ROTATION_ARC / 2 
						% Config.ULTRASONIC_ROTATION_STEP;
				Config.CAR_ROTATION_CNT = (int) (Config.ULTRASONIC_ROTATION_ARC 
						/ 2 / Config.CAR_ROTATION_STEP);
				Config.CAR_ROTATION_REMINDER = Config.ULTRASONIC_ROTATION_ARC / 2 
						% Config.CAR_ROTATION_STEP;
				
				this.dispose();
			} catch (NumberFormatException nfe) {
				JOptionPane.showMessageDialog(this, nfe.getMessage(), 
						"Invalid value", JOptionPane.ERROR_MESSAGE);
			}
		});
		buttonsP.add(okB);
		
		setVisible(true);
	}
	
	public void fireOkEvent() {
		okB.doClick();
	}
	
}
