import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GraphicsEnvironment;
import java.awt.Insets;
import java.awt.Toolkit;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.WindowConstants;

import org.jnativehook.GlobalScreen;
import org.jnativehook.NativeHookException;
import org.jnativehook.keyboard.NativeKeyEvent;
import org.jnativehook.keyboard.NativeKeyListener;

public class MainFrame extends JFrame {
	
	private static final long serialVersionUID = 1L;
	
	private final DrawingPanel drawPanel = new DrawingPanel();

	private final JMenuBar menuBar = new JMenuBar();
	private final JMenu menuOptions = new JMenu("Options");
	private final JMenuItem mItemTiles = new JMenuItem("Set Tiles");
	private final JMenuItem mItemCar = new JMenuItem("Set Car");
	private final JMenuItem mItemStep = new JMenuItem("Step Mode");
	private final JMenuItem mItemStart = new JMenuItem("Start Simulation");
	private final JMenuItem mItemSaveConfig = new JMenuItem("Save Configuration");

	private final JMenu menuTileSize = new JMenu("Tile Size");
	private final JMenuItem mItem16x16 = new JMenuItem("16 x 16");
	private final JMenuItem mItem32x32 = new JMenuItem("32 x 32");
	private final JMenuItem mItem48x48 = new JMenuItem("48 x 48");
	private final JMenuItem mItem64x64 = new JMenuItem("64 x 64");
	private final Color defaultColor = UIManager.getColor("JMenuItem.background");

	private final Map<JMenuItem, TileSize> mapTileSize = new HashMap<JMenuItem, TileSize>();

	private final Map<JMenuItem, State> mapStates = new HashMap<JMenuItem, State>();
	
	private void mItemActionHandler(JMenuItem item) {
		for (Component c : item.getParent().getComponents()) {
			if (c instanceof JMenuItem && c.getBackground() == Color.RED) {
				c.setBackground(defaultColor);
				break;
			}
		}
		item.setBackground(Color.RED);
		if (mapStates.containsKey(item)) {
			State.set(mapStates.get(item));
		} else if (mapTileSize.containsKey(item)) {
			TileSize.set(mapTileSize.get(item));
		}
	}
	
	class Animator implements Runnable {	
		@Override public void run() {
			while (true) {
				if (State.get() != State.SIMULATION_ON) 
					return;
				try {
					SwingUtilities.invokeAndWait(drawPanel);
					Thread.sleep(20);
				} catch (InterruptedException ie) {
					Thread.currentThread().interrupt();
				} catch (InvocationTargetException ite) {
					throw new AssertionError(ite);
				}	
			}	
		}		
	}
	
	class KeyListener implements NativeKeyListener {
		@Override public void nativeKeyPressed(NativeKeyEvent ke) {
			if (ke.getKeyCode() == NativeKeyEvent.VC_P) {
				if (State.get() == State.SIMULATION_ON)
					mItemActionHandler(mItemStep);
				else if (State.get() == State.STEP_ON) {
					mItemActionHandler(mItemStart);
					new Thread(new Animator()).start();
				}
			}
		}
		@Override public void nativeKeyReleased(NativeKeyEvent arg) {}
		@Override public void nativeKeyTyped(NativeKeyEvent arg) {}	
	}
	
	public MainFrame() {
		super();
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		Insets in = Toolkit.getDefaultToolkit().getScreenInsets(GraphicsEnvironment
			.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration());
		
		setLayout(new FlowLayout());
		setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		setExtendedState(JFrame.MAXIMIZED_BOTH);
		setTitle("RobotCar Simulator");
		setLocationByPlatform(true);
		setSize(screenSize.width - in.left - in.right, screenSize.height - in.top - in.bottom);
		getRootPane().setBorder(BorderFactory.createEmptyBorder(6, 0, 0, 0));	
		
		mapTileSize.put(mItem16x16, TileSize.X16);
		mapTileSize.put(mItem32x32, TileSize.X32);
		mapTileSize.put(mItem48x48, TileSize.X48);
		mapTileSize.put(mItem64x64, TileSize.X64);
		mapStates.put(mItemTiles, State.SET_TILES);
		mapStates.put(mItemCar, State.SET_CAR);
		mapStates.put(mItemStart, State.SIMULATION_ON);
		mapStates.put(mItemStep, State.STEP_ON);
		
		setJMenuBar(menuBar);
		menuBar.add(menuOptions);
		menuOptions.add(mItemTiles);
		mItemTiles.addActionListener(e -> mItemActionHandler(mItemTiles));
		menuOptions.add(mItemCar);
		mItemCar.addActionListener(e -> mItemActionHandler(mItemCar));
		menuOptions.add(mItemStep);
		mItemStep.addActionListener(e -> mItemActionHandler(mItemStep));
		menuOptions.add(mItemStart);
		mItemStart.addActionListener(e -> {
			mItemActionHandler(mItemStart);
			new Thread(new Animator()).start();
		});
		menuOptions.add(mItemSaveConfig);
		mItemSaveConfig.addActionListener(e -> {
			if (JOptionPane.showConfirmDialog(this, "This will overwrite previous configuration files.\nAre you sure?",
					null, JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
				World.saveToFile();
			}
		});
		
		menuBar.add(menuTileSize);
		menuTileSize.add(mItem16x16);
		mItem16x16.addActionListener(e -> mItemActionHandler(mItem16x16));
		menuTileSize.add(mItem32x32);
		mItem32x32.addActionListener(e -> mItemActionHandler(mItem32x32));
		menuTileSize.add(mItem48x48);
		mItem48x48.addActionListener(e -> mItemActionHandler(mItem48x48));
		menuTileSize.add(mItem64x64);
		mItem64x64.addActionListener(e -> mItemActionHandler(mItem64x64));
		
		setVisible(true);
		Insets frameIn = getInsets();
		Dimension panelSize = new Dimension(this.getWidth() - 6 - 6, 
				this.getHeight() - frameIn.top - menuBar.getSize().height - 6 - 6 - 6);
		drawPanel.setPreferredSize(panelSize);
		add(drawPanel);
		setVisible(true);
			
		mItemActionHandler(mItemTiles);
		mItemActionHandler(mItem32x32);
		World.init(drawPanel.getWidth(), drawPanel.getHeight());
		
		try {
			GlobalScreen.registerNativeHook();
			Logger logger = Logger.getLogger(GlobalScreen.class.getPackage().getName());
			logger.setLevel(Level.WARNING);
			logger.setUseParentHandlers(false);
			GlobalScreen.addNativeKeyListener(new KeyListener());
		} catch (NativeHookException nhe) {
			throw new AssertionError(nhe);
		}
	}

	public static void main (String[] args) {
		@SuppressWarnings("unused") 
		MainFrame frame = new MainFrame();
	}

}
