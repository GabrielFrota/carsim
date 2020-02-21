import javax.swing.SwingUtilities;

public class Animator implements Runnable {
	
	private DrawingPanel dp;
	
	public Animator(DrawingPanel panel) {
		dp = panel;
	}

	@Override public void run() {
		while (true) {
			if (State.get() != State.SIMULATION_ON) 
				return;
			try {
				SwingUtilities.invokeAndWait(dp);
				Thread.sleep(20);
			} 
			catch (Exception ex) {}		
		}	
	}
	
}
