import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
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
	
	@Override 
	public void run() {			
		Car.oneCycle();
		repaint();
		getToolkit().sync();
	}
	
	public DrawingPanel() {
		super();
		
		addMouseListener(new MouseAdapter() {
			@Override 
			public void mouseClicked(MouseEvent e) {
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
			@Override 
			public void mouseMoved(MouseEvent e) {
				if (State.get() == State.SET_TILES 
					|| State.get() == State.SET_CAR) {
					mouseX = e.getX();
					mouseY = e.getY();
					repaint();
				}
			}
			@Override 
			public void mouseDragged(MouseEvent e) {
				if (State.get() == State.SET_TILES) {
					if (SwingUtilities.isLeftMouseButton(e)) 
						World.setTile(e.getX(), e.getY(), TileSize.get());
					else
						World.removeTile(e.getX(), e.getY(), TileSize.get());
					mouseX = 0;
					mouseY = 0;
					repaint();
				}
			}
		});
	}
	
	@Override 
	public void paintComponent(Graphics g) {
		super.paintComponent(g);		
		Graphics2D g2d = (Graphics2D) g;

		if (World.get() != null) {
			g.drawImage(World.get(), 0, 0, null);
		}
		
		if (mouseX != 0 && mouseY != 0) {
			if (State.get() == State.SET_TILES) 		
				g.drawRect(mouseX, mouseY, TileSize.get(), TileSize.get());
			else if (State.get() == State.SET_CAR) {
				Path2D carRect = new Path2D.Double();
				double halfW = Config.CAR_WIDTH / 2;
				double halfH = Config.CAR_HEIGHT / 2;
				carRect.moveTo(mouseX - halfW, mouseY - halfH);
				carRect.lineTo(mouseX + halfW, mouseY - halfH);
				carRect.moveTo(mouseX + halfW, mouseY - halfH);
				carRect.lineTo(mouseX + halfW, mouseY + halfH);
				carRect.moveTo(mouseX + halfW, mouseY + halfH);
				carRect.lineTo(mouseX - halfW, mouseY + halfH);
				carRect.moveTo(mouseX - halfW, mouseY + halfH);
				carRect.lineTo(mouseX - halfW, mouseY - halfH);
				carRect.closePath();
				AffineTransform at = AffineTransform.getRotateInstance(Config.CAR_DIRECTION, 
						carRect.getBounds2D().getCenterX(), carRect.getBounds2D().getCenterY());
				carRect.transform(at);
				g2d.draw(carRect);
			}
		}
				
		Path2D rect = Car.getCarRect();
		if (rect != null) {
			Stroke prevStroke = g2d.getStroke();
			Color prevColor = g2d.getColor();
			g2d.setColor(Color.BLUE);
			g2d.setStroke(bold);
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
		
		int col = Car.getStats();
		g2d.setColor(Color.RED);
		g2d.setStroke(bold);
		Font newFont = g2d.getFont().deriveFont(g2d.getFont().getSize() * 1.4F);
		g2d.setFont(newFont);
		g2d.drawString("Collision count: " + Integer.toString(col), 10, 20);
	}
		
}
