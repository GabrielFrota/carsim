
public enum State {
	
	NONE, SET_TILES, SET_CAR, SIMULATION_ON, STEP_ON, CONFIG_SCREEN;
	
	private static State current; 
	
	public static void set(State s) {
		current = s;
	}
	
	public static State get() {
		return current;
	}
	
}
