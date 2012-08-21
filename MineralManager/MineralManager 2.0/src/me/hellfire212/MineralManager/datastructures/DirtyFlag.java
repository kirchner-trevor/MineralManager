package me.hellfire212.MineralManager.datastructures;

/** Simple way to have a shared dirty flag without needing to keep strong references around. */
public final class DirtyFlag {
	private boolean dirty;
	
	public DirtyFlag() {}
	
	public boolean isDirty() {
		return dirty;
	}
	
	public void flagDirty() {
		dirty = true;
	}
	
	public void clear() {
		dirty = false;
	}
}
