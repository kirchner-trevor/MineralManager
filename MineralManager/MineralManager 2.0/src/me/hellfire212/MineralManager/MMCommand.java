package me.hellfire212.MineralManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
public class MMCommand {

	private String command = null;
	private String usage = null;
	private List<Argument> arguments = new ArrayList<Argument>();
	private boolean subcommands = false;
	
	private static String error = "";
	
	public MMCommand() {
		command = "";
	}
	
	public MMCommand(String name) {
		command = name;
	}
	
	public MMCommand(String name, boolean hasSubcommands) {
		command = name;
		subcommands = hasSubcommands;
	}
	
	public MMCommand(String name, Argument arg0) {
		this(name);
		arguments.add(arg0);
	}
	
	public MMCommand(String name, Argument arg0, Argument arg1) {
		this(name, arg0);
		arguments.add(arg1);
	}
	
	public MMCommand(String name, Argument arg0, Argument arg1, Argument arg2) {
		this(name, arg0, arg1);
		arguments.add(arg2);
	}
	
	public MMCommand(String name, Argument arg0, Argument arg1, Argument arg2, Argument... args) {
		this(name, arg0, arg1, arg2);
		arguments.addAll(Arrays.asList(args));
	}
	
	public String getName() {
		return command;
	}
	
	public String getUsage() {
		if(usage == null) {
			String temp = command;
			for(Argument arg : arguments) {
				temp+= " <" + arg.getDescription() + ">";
			}
			usage = subcommands ? temp + " <...>" : temp;
		}
		return usage;
	}
	
	private Object parseObject(String arg) {
		Object object = null;
		try {
			object = Integer.parseInt(arg);
		} catch (NumberFormatException e) {
			try {
				object = Double.parseDouble(arg);
			} catch (NumberFormatException e1) {
				object = arg;
			}
		}
		return object;
	}
	
	public List<Object> validate(List<String> args) {
		int size = args.size();
		List<Object> returnList = new ArrayList<Object>();
		String userCommand = size-- > 0 ? args.get(0) : null;

		if(command.equalsIgnoreCase(userCommand)) {
			if(size == arguments.size() || subcommands) {
				if(!subcommands) {
					for(int index = 0; index < size; index++) {
						Object arg = parseObject(args.get(index + 1));
						if(!arguments.get(index).getType().isInstance(arg)) {
							return null;
						} 
						returnList.add(arg);
					}
				}
				error = "";
				return returnList;
			}
			error = getUsage();
		}
		return null;
	}
	
	public static String getError() {
		return error;
	}
}
