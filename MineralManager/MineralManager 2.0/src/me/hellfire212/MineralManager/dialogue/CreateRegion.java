package me.hellfire212.MineralManager.dialogue;


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

import me.hellfire212.MineralManager.Commands;
import me.hellfire212.MineralManager.Coordinate;
import me.hellfire212.MineralManager.MineralManager;
import me.hellfire212.MineralManager.Selection;

import org.bukkit.ChatColor;
import org.bukkit.conversations.BooleanPrompt;
import org.bukkit.conversations.ConversationAbandonedEvent;
import org.bukkit.conversations.ConversationAbandonedListener;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.ConversationFactory;
import org.bukkit.conversations.ConversationPrefix;
import org.bukkit.conversations.FixedSetPrompt;
import org.bukkit.conversations.Prompt;
import org.bukkit.conversations.RegexPrompt;
import org.bukkit.entity.Player;

public class CreateRegion implements ConversationAbandonedListener {
	private ConversationFactory conversationFactory;
	private RegionNamePrompt namePrompt;
	private FinishCreatePrompt finishCreatePrompt;
	private NumberPrompt levelNumberPrompt;
	private MineralManager plugin;
	private ConfigurationChoosePrompt choosePrompt;
	private RegionSelectPrompt regionPrompt;

	public CreateRegion(MineralManager plugin) {
		this.plugin = plugin;
		this.namePrompt = new RegionNamePrompt();
		this.conversationFactory = new ConversationFactory(plugin)
			.withModality(false)
			.withPrefix(new RegionConversationPrefix())
			.withFirstPrompt(new RegionTypePrompt(namePrompt))
			.withTimeout(60)
			.withEscapeSequence("/quit")
			.addConversationAbandonedListener(this);
		this.levelNumberPrompt = new NumberPrompt("Level Number?", "level");
		this.regionPrompt = new RegionSelectPrompt(namePrompt);
		this.choosePrompt = new ConfigurationChoosePrompt();
		this.finishCreatePrompt = new FinishCreatePrompt();
		levelNumberPrompt.setNext(choosePrompt);
	}
	
	public void begin(Player p) {
		conversationFactory.buildConversation(p).begin();
	}
	
	private String formatHelp(String choice, String description) {
		return String.format("   %s%s: %s%s", ChatColor.GREEN, choice, ChatColor.BLUE, description);
	}
	
	static String promptText(String s) {
		return String.format("%s%s", ChatColor.BLUE, s);
	}
	
	private String betterChoicesFormat(List<String> fixedSet) {
		StringBuilder builder = new StringBuilder();
		builder.append(ChatColor.AQUA);
		builder.append("[");
		builder.append(ChatColor.GREEN);
		builder.append(fixedSet.get(0));
		for (String s: fixedSet.subList(1, fixedSet.size())) {
			builder.append(String.format("%s, %s%s", ChatColor.AQUA, ChatColor.GREEN, s));
		}
		builder.append(ChatColor.AQUA);
		builder.append("]");
		return builder.toString();
	}
	
	private class RegionConversationPrefix implements ConversationPrefix {
		@Override
		public String getPrefix(ConversationContext context) {
			return String.format("%s%sMM%s> ", ChatColor.BOLD, ChatColor.YELLOW, ChatColor.RESET);
		}

	}

	private class RegionTypePrompt extends FixedSetPrompt {
		private RegionNamePrompt namePrompt;
		private NumberPrompt cubeSelectBegin;

		private final String[] HELP_TEXT = {
			formatHelp("world", "This entire world."),
			formatHelp("cube", "a cube selection centered where you are standing"),
			formatHelp("region", "Make a cube by selecting opposing corners"), // FIXME
			formatHelp("lasso", "Walk around amassing points.")
		};

		public RegionTypePrompt(RegionNamePrompt namePrompt) {
			super("world", "cube", "region", "lasso", "help");
			this.namePrompt = namePrompt;
			this.cubeSelectBegin = setupCubeSelect();
		}

		@Override
		public String getPromptText(ConversationContext context) {
			return promptText("How do you want to select a region? \n    ") + betterChoicesFormat(this.fixedSet);
		}

		@Override
		protected Prompt acceptValidatedInput(ConversationContext context, String s) {
			if (s.equals("help")) {
				for (String line: HELP_TEXT) {
					context.getForWhom().sendRawMessage(line);
				}
				return this;
			} else {
				Selection.Type region_type = Selection.Type.valueOf(s.toUpperCase());
				context.setSessionData("region_type", region_type);
				switch (region_type) {
				case WORLD:
					return namePrompt;
				case CUBE:
					return cubeSelectBegin;
				case REGION:
					return regionPrompt;
				case LASSO:
					context.getForWhom().sendRawMessage(ChatColor.RED + "Sorry, Lasso selection is still a work in progress.");
					return this;
				}
					
			}
				
			return Prompt.END_OF_CONVERSATION; // FIXME
		}
		
	}
	
	private class RegionNamePrompt extends RegexPrompt {
		public RegionNamePrompt() {
			super(Pattern.compile("^[\\w_-]+$"));
		}

		@Override
		public String getPromptText(ConversationContext ctx) {
			if (ctx.getSessionData("shownSelection") == null) {
				showSelectionInfo(ctx);
				ctx.setSessionData("shownSelection", true);
			}
			return promptText("What do you want to name this region?");
		}

		@Override
		protected Prompt acceptValidatedInput(ConversationContext c, String input) {
			c.setSessionData("name", input);
			//c.getForWhom().sendRawMessage(String.format("Blarg %s %s %s", c.getSessionData("region_type"), c.getSessionData("cube.horizontal"), c.getSessionData("cube.vertical")));
			return choosePrompt;
		}

		@Override
		protected String getFailedValidationText(ConversationContext context,
				String invalidInput) {
			return "" + ChatColor.RED + "Region name must not contain spaces";
		}

		@Override
		protected Object clone() throws CloneNotSupportedException {
			// TODO Auto-generated method stub
			return super.clone();
		}
		
	}
	
	private class ConfigurationChoosePrompt implements Prompt {
		private ArrayList<String> choices;
		public ConfigurationChoosePrompt() {

		}

		@Override
		public Prompt acceptInput(ConversationContext context, String input) {
			if (choices.contains(input.toLowerCase())) {
				context.setSessionData("config", plugin.getConfigurationMap().get(input.toLowerCase()));
				return finishCreatePrompt;
			} else {
				context.getForWhom().sendRawMessage(ChatColor.RED + "Must be one of the provided configuration names.");
				return this;
			}
		}

		@Override
		public String getPromptText(ConversationContext context) {
			this.choices = new ArrayList<String>(plugin.getConfigurationMap().keySet());
			Collections.sort(choices);
			return promptText("Which configuration to use?\n ") + betterChoicesFormat(choices);
		}
		
		@Override
		public boolean blocksForInput(ConversationContext context) {
			return true;
		}

		
	}
	
	private class FinishCreatePrompt extends BooleanPrompt {

		@Override
		public String getPromptText(ConversationContext context) {
			return promptText("Create this region?");
		}

		@Override
		protected Prompt acceptValidatedInput(ConversationContext context, boolean input) {
			// TODO Auto-generated method stub
			return null;
		}
		
	}

	@Override
	public void conversationAbandoned(ConversationAbandonedEvent e) {
		if (!e.gracefulExit()) {
			e.getContext().getForWhom().sendRawMessage(String.format("%sRegion Create cancelled", ChatColor.RED));
		}
		
	}

	public NumberPrompt setupCubeSelect() {
		NumberPrompt horizontal = new NumberPrompt("Horizontal Radius?", "cube.horizontal");
		NumberPrompt vertical = new NumberPrompt("Vertical Radius?", "cube.vertical");
		horizontal.setNext(vertical);
		vertical.setNext(namePrompt);
		return horizontal;
	}

	public void showSelectionInfo(ConversationContext ctx) {
		Selection.Type r_type = (Selection.Type) ctx.getSessionData("region_type");
		//String message;
		Selection sel = null;
		Player player = (Player) ctx.getForWhom();
		String outPrefix = "" + ChatColor.AQUA;
		switch (r_type) {
		case CUBE:
			int horizontal = ((Number) ctx.getSessionData("cube.horizontal")).intValue();
			int vertical = ((Number) ctx.getSessionData("cube.vertical")).intValue();
			sel = Commands.selectCube(plugin, player, horizontal, vertical);
			break;
		case REGION:
			Coordinate start = (Coordinate) ctx.getSessionData("region.start");
			Coordinate end = (Coordinate) ctx.getSessionData("region.end");
			sel = Commands.actuallySelectRegion(plugin, player, start, end, outPrefix);
			break;
		case LASSO:
			sel = Commands.finishLasso(plugin, player, outPrefix);
			break;
		case WORLD:
			sel = Commands.selectWorld(plugin, player, null);
			break;
		}
		ctx.setSessionData("selection", sel);
		
	}
	
}
