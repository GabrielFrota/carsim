import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Properties;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.WindowConstants;

public class ConfigFrame extends JFrame {

	private static final long serialVersionUID = 1L;
	
	private final MainFrame mainF;
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
	private final JCheckBox collisionC = new JCheckBox("collision check on");
	private final JCheckBox sensorC = new JCheckBox("sensor check on");
	private final JLabel collisionAngleL = new JLabel("collision rotation angle:");
	private final JTextField collisionF = new JTextField(size);
	
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
	
	public ConfigFrame(MainFrame mf, int width, int height) {
		super();
		mainF = mf;
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		setTitle("Configurations");
		setSize(width, height);
		setLocationByPlatform(true);
		setLayout(new FlowLayout());
		
		Dimension dim = new Dimension(width, Config.FRAME_PANEL_HEIGHT + 20);
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
		carP.add(collisionAngleL);
		carP.add(collisionF);
		carP.add(collisionC);
		carP.add(sensorC);
			
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
	
		ultrasonicP.setPreferredSize(dim);
		ultrasonicP.setBorder(BorderFactory.createTitledBorder("Ultrasonic Settings"));
		add(ultrasonicP);
		ultrasonicP.add(ultrasonicRangeL);
		ultrasonicP.add(ultrasonicRangeF);
		ultrasonicP.add(ultrasonicThresholdL);
		ultrasonicP.add(ultrasonicThresholdF);
		ultrasonicP.add(ultrasonicArcL);
		ultrasonicP.add(ultrasonicArcF);
	
		buttonsP.setPreferredSize(dim);
		add(buttonsP);
		buttonsP.add(saveConfB);
		saveConfB.addActionListener(e -> saveConfig());
		buttonsP.add(loadConfB);
		loadConfB.addActionListener(e -> loadConfig());
		buttonsP.add(saveWorldB);
		saveWorldB.addActionListener(e -> saveWorld());
		buttonsP.add(loadWorldB);
		loadWorldB.addActionListener(e -> loadWorld());
		okB.addActionListener(e -> {
			if (validateFields())
				this.dispose();		
		});
		buttonsP.add(okB);
		loadFields();
		
		setVisible(true);
	}
	
	private boolean validateFields() {
		try {
			Config.CAR_WIDTH = Integer.parseInt(widthF.getText(), 10);
			Config.CAR_HEIGHT = Integer.parseInt(heightF.getText(), 10);
			Config.CAR_SPEED = Integer.parseInt(speedF.getText());
			Config.CAR_DIRECTION = Math.toRadians(Double.parseDouble(directF.getText()));
			Config.CAR_ROTATION_STEP = Math.toRadians(Double.parseDouble(rotationF.getText()));	
			Config.CAR_COLLISION_CHECK_ON = collisionC.isSelected();
			Config.CAR_SENSOR_ON = sensorC.isSelected();
			Config.CAR_COLLISION_STEP = Math.toRadians(Double.parseDouble(collisionF.getText()));
			
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
			return true;
		} catch (NumberFormatException nfe) {
			JOptionPane.showMessageDialog(this, nfe.getMessage(), 
					"Invalid value", JOptionPane.ERROR_MESSAGE);
			return false;
		}
	}
	
	private String fieldToString(Field f) throws IllegalAccessException {
		if (f.getType() == Integer.TYPE)
			return Integer.toString((Integer)f.get(null));
		else if (f.getType() == Double.TYPE)
			return Double.toString((Double)f.get(null));
		else if (f.getType() == Boolean.TYPE)
			return Boolean.toString((Boolean)f.get(null));
		else
			throw new AssertionError("Unexpected field type for parameter");
	}
	
	private void saveConfig() {
		if (validateFields()) {
			JFileChooser jfc = new JFileChooser(".");
			if (jfc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
				try {
					Properties props = new Properties();
					for (Field f : Config.class.getFields()) {
						if (Modifier.isPublic(f.getModifiers())) 
							props.put(f.getName(), fieldToString(f));		
					}
					BufferedWriter out = new BufferedWriter(new FileWriter(jfc.getSelectedFile()));
					props.store(out, "configuration parameters for CarSim, default file format from the java language\n"
							+ "these are the values of all public static members for Config class");
				} catch (Exception ex) {
					JOptionPane.showMessageDialog(this, ex.getMessage(), 
							"Error during file write", JOptionPane.ERROR_MESSAGE);
				}
			}
		}
	}
	
	private void loadConfig() {
		JFileChooser jfc = new JFileChooser(".");
		if (jfc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
			try {
				Properties props = new Properties();
				BufferedReader in = new BufferedReader(new FileReader(jfc.getSelectedFile()));
				props.load(in);
				for (Field f : Config.class.getFields()) {
					if (Modifier.isPublic(f.getModifiers()))
						Config.setFieldFromProps(f, props);
				}
				loadFields();
			} catch (Exception ex) {
				JOptionPane.showMessageDialog(this, ex.getMessage(), 
						"Error during file read", JOptionPane.ERROR_MESSAGE); 
			}
		}
	}
	
	private void saveWorld() {
		JFileChooser jfc = new JFileChooser(".");
		if (jfc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
			try {
				boolean success = ImageIO.write(World.get(), "bmp", jfc.getSelectedFile());
				if (!success) 
					throw new IOException();
			} catch (Exception ex) {
				JOptionPane.showMessageDialog(this, ex.getMessage(), 
						"Error during file write", JOptionPane.ERROR_MESSAGE); 
			}
		}		
	}
	
	private void loadWorld() {
		JFileChooser jfc = new JFileChooser(".");
		if (jfc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
			try {
				BufferedImage im = ImageIO.read(jfc.getSelectedFile());
				if (im == null)
					throw new IllegalArgumentException("Invalid world file");				
				World.set(im);
				mainF.repaint();
			} catch (Exception ex) {
				JOptionPane.showMessageDialog(this, ex.getMessage(), 
						"Error during file read", JOptionPane.ERROR_MESSAGE); 
			}
		}
	}
	
	private void loadFields() {
		widthF.setText(Integer.toString(Config.CAR_WIDTH));
		heightF.setText(Integer.toString(Config.CAR_HEIGHT));
		speedF.setText(Integer.toString(Config.CAR_SPEED));
		directF.setText(Double.toString(Math.toDegrees(Config.CAR_DIRECTION)));
		rotationF.setText(Double.toString(Math.toDegrees(Config.CAR_ROTATION_STEP)));
		collisionF.setText(Double.toString(Math.toDegrees(Config.CAR_COLLISION_STEP)));
		collisionC.setSelected(Config.CAR_COLLISION_CHECK_ON);
		sensorC.setSelected(Config.CAR_SENSOR_ON);
		infraredRangeF.setText(Integer.toString(Config.INFRARED_RANGE));
		infraredThreshF.setText(Integer.toString(Config.INFRARED_THRESHOLD));
		infraredAngleLeftF.setText(Double.toString(Math.toDegrees(Config.INFRARED_ANGLE_LEFT)));
		infraredAngleRightF.setText(Double.toString(Math.toDegrees(Config.INFRARED_ANGLE_RIGHT)));
		ultrasonicRangeF.setText(Integer.toString(Config.ULTRASONIC_RANGE));
		ultrasonicThresholdF.setText(Integer.toString(Config.ULTRASONIC_THRESHOLD));
		ultrasonicArcF.setText(Double.toString(Math.toDegrees(Config.ULTRASONIC_ROTATION_ARC)));
	}
		
}
