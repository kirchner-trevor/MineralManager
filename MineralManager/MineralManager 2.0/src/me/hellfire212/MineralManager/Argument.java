package me.hellfire212.MineralManager;

public class Argument {
	
	private final Class<? extends Object> type;
	private final String description;
	
	public Argument(Class<? extends Object> type, String description) {
		this.type = type;
		this.description = description;
	}
	
	public Class<? extends Object> getType() {
		return type;
	}
	
	public String getDescription() {
		return description;
	}
}