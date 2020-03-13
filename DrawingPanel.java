import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Path2D;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;

public class DrawingPanel extends JPanel implements Runnable {

	private static final long serialVersionUID = 1L;
	
	private final Stroke dashed = new BasicStroke(2, 
			BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{8}, 0);
	private final Stroke bold = new BasicStroke(2);

	private int mouseX;
	private int mouseY;
	
	@Override public void run() {			
		Car.oneCycle();
		repaint();
		getToolkit().sync();
	}
	
	public DrawingPanel() {
		super();
		
		addMouseListener(new MouseAdapter() {
			@Override public void mouseClicked(MouseEvent e) {
				switch (State.get()) {
				case SET_TILES:
					if (SwingUtilities.isLeftMouseButton(e)) {
						World.setTile(e.getX(), e.getY(), TileSize.get());
						repaint();
					} else if (SwingUtilities.isRightMouseButton(e)) {
						World.removeTile(e.getX(), e.getY(), TileSize.get());
						repaint();
					}
					break;
				case SET_CAR:
					Car.init(e.getX(), e.getY());
					repaint();
					break;
				case STEP_ON:
					Car.oneCycle();
					repaint();
				default:
					return;
				}
			}
		});
		
		addMouseMotionListener(new MouseAdapter() {
			@Override public void mouseMoved(MouseEvent e) {
				if (State.get() == State.SET_TILES) {
					mouseX = e.getX();
					mouseY = e.getY();
					repaint();
				}
			}
		});
	}
	
	@Override public void paintComponent(Graphics g) {
		super.paintComponent(g);		
		Graphics2D g2d = (Graphics2D) g;

		if (World.get() != null) {
			g.drawImage(World.get(), 0, 0, null);
		}
		
		if (mouseX != 0 && mouseY != 0
			&& State.get() == State.SET_TILES) {		
			g.drawRect(mouseX, mouseY, TileSize.get(), TileSize.get());
		}
		
		Path2D rect = Car.getCarRect();
		if (rect != null) {
			Stroke prevStroke = g2d.getStroke();
			Color prevColor = g2d.getColor();
			g2d.setStroke(bold);
			g2d.setColor(Color.BLUE);
			g2d.draw(rect);
			g2d.setStroke(prevStroke);
			g2d.setColor(prevColor);
		}
		
		Path2D line = Car.getUltrasonicLine();
		if (line != null) {
			Stroke prevStroke = g2d.getStroke();
			Color prevColor = g2d.getColor();
			g2d.setStroke(dashed);
			g2d.setColor(Color.RED);
			g2d.draw(line);
			line = Car.getLeftInfraredLine();
			g2d.setColor(Color.GREEN);
			g2d.draw(line);
			line = Car.getRightInfraredLine();
			g2d.draw(line);
			g2d.setStroke(prevStroke);
			g2d.setColor(prevColor);
		}
	}
	
}
