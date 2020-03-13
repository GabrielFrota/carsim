import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.UIManager;

public class World {

	private static BufferedImage image;
	
	private static Color defaultColor = UIManager.getColor("Panel.background");
	
	public static void init(int width, int height) {
		File f = new File("world.bmp");
		if (f.exists()) {
			try {
				image = ImageIO.read(f);
				if (image == null) 
					throw new IOException();
			} catch (Exception ex) {
				throw new AssertionError(ex);
			}
		} else {
			image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
			Graphics2D g = image.createGraphics();
			g.setColor(defaultColor);
			g.fillRect(0, 0, width, height);
			for (int x = 0; x < width; x++) {
				image.setRGB(x, 0, 0);
				image.setRGB(x, height - 1, 0);
			}
			for (int y = 0; y < height; y++) {
				image.setRGB(0, y, 0);
				image.setRGB(width - 1, y, 0);
			}
			g.dispose();
		}		
	}
	
	public static BufferedImage get() {
		return image;
	}
	
	public static void setTile(int x, int y, int size) {
		Graphics2D g = image.createGraphics();
		g.setColor(Color.BLACK);
		g.fillRect(x, y, size, size);
		g.dispose();
	}
	
	public static void removeTile(int x, int y, int size) {
		Graphics2D g = image.createGraphics();
		g.setColor(defaultColor);
		g.fillRect(x, y, size, size);
		g.dispose();
	}
	
	public static void saveToFile() {
		try {
			boolean success = ImageIO.write(image, "bmp", new File("world.bmp"));
			if (!success) 
				throw new IOException();
		} catch (Exception ex) {
			throw new AssertionError(ex);
		}
	}
	
	public static boolean checkPoint(int x, int y) {
		if (x >= image.getWidth() || y >= image.getHeight()
			|| x < 0 || y < 0)
			return true;
		
		return image.getRGB(x, y) == Color.black.getRGB();
	}
	
}
