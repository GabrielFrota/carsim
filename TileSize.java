
public enum TileSize {

	X16(16), X32(32), X48(48), X64(64);
	
	private static TileSize current;
	
	private int value;
	
	private TileSize(int val) {
		value = val;
	}
	
	public static void set(TileSize ts) {
		current = ts;
	}
	
	public static int get() {
		return current.value;
	}
	
}
