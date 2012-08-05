package me.hellfire212.MineralManager.dialogue;


import java.util.regex.Pattern;

import me.hellfire212.MineralManager.MineralManager;

import org.bukkit.ChatColor;
import org.bukkit.conversations.ConversationAbandonedEvent;
import org.bukkit.conversations.ConversationAbandonedListener;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.ConversationFactory;
import org.bukkit.conversations.ConversationPrefix;
import org.bukkit.conversations.FixedSetPrompt;
import org.bukkit.conversations.NumericPrompt;
import org.bukkit.conversations.Prompt;
import org.bukkit.conversations.RegexPrompt;
import org.bukkit.entity.Player;

public class CreateRegion implements ConversationAbandonedListener {
	private ConversationFactory conversationFactory;
	private RegionNamePrompt namePrompt;

	public CreateRegion(MineralManager plugin) {
		this.conversationFactory = new ConversationFactory(plugin)
			.withModality(false)
			.withPrefix(new RegionConversationPrefix())
			.withFirstPrompt(new RegionTypePrompt())
			.withTimeout(60)
			.addConversationAbandonedListener(this);
		this.namePrompt = new RegionNamePrompt();
	}
	
	public void begin(Player p) {
		conversationFactory.buildConversation(p).begin();
	}
	
	private String formatHelp(String choice, String description) {
		return String.format("   %s%s: %s%s", ChatColor.GREEN, choice, ChatColor.BLUE, description);
	}
	
	private String promptText(String s) {
		return String.format("%s%s", ChatColor.BLUE, s);
	}
	
	private class RegionConversationPrefix implements ConversationPrefix {
		@Override
		public String getPrefix(ConversationContext context) {
			return String.format("%s%sMM%s> ", ChatColor.BOLD, ChatColor.YELLOW, ChatColor.RESET);
		}

	}

	private class RegionTypePrompt extends FixedSetPrompt {
		private final String[] HELP_TEXT = {
			formatHelp("global", "This entire world."),
			formatHelp("cube", "a cube selection centered where you are standing"),
			formatHelp("region", "Something Something mhrm mhrrr"), // FIXME
			formatHelp("lasso", "Walk around amassing points.")
		};

		public RegionTypePrompt() {
			super("global", "cube", "region", "lasso", "help");
		}

		@Override
		public String getPromptText(ConversationContext context) {
			return "How do you want to select a region? " + this.formatFixedSet();
		}

		@Override
		protected Prompt acceptValidatedInput(ConversationContext context, String s) {
			if (s.equals("help")) {
				for (String line: HELP_TEXT) {
					context.getForWhom().sendRawMessage(line);
				}
				return this;
			} else {
				context.setSessionData("region_type", s);
				Prompt next = namePrompt;
				if (s.equals("global")) {
					return next;
				} else if (s.equals("cube")) {
					NumberPrompt horizontal = new NumberPrompt("Horizontal Radius?", "cube.horizontal");
					NumberPrompt vertical = new NumberPrompt("Vertical Radius?", "cube.vertical");
					horizontal.setNext(vertical);
					vertical.setNext(next);
					return horizontal;
				} else if (s.equals("region")) {
					context.getForWhom().sendRawMessage("Sorry, Region selection still work in progress.");
				} else if (s.equals("lasso")) {
					context.getForWhom().sendRawMessage("Sorry, Lasso selection still work in progress.");
				}
					
			}
				
			return Prompt.END_OF_CONVERSATION; // FIXME
		}
		
	}
	
	private class NumberPrompt extends NumericPrompt {
		private String ptext;
		private Prompt next;
		private String storeAs;

		public NumberPrompt(String ptext, String storeAs) {
			this.ptext = ptext;
			this.storeAs = storeAs;
		}
		
		public void setNext(Prompt next) {
			this.next = next;
		}

		@Override
		public String getPromptText(ConversationContext context) {
			return ptext;
		}

		@Override
		protected Prompt acceptValidatedInput(ConversationContext context, Number num) {
			context.setSessionData(storeAs, num);
			return next;
		}
		
	}
	
	private class RegionNamePrompt extends RegexPrompt {
		public RegionNamePrompt() {
			super(Pattern.compile("^[\\w_-]+$"));
		}

		@Override
		public String getPromptText(ConversationContext arg0) {
			return promptText("What do you want to name this region?");
		}

		@Override
		protected Prompt acceptValidatedInput(ConversationContext c, String arg1) {
			c.getForWhom().sendRawMessage(String.format("Blarg %s %s %s", c.getSessionData("region_type"), c.getSessionData("cube.horizontal"), c.getSessionData("cube.vertical")));
			return Prompt.END_OF_CONVERSATION;
		}
		
	}

	@Override
	public void conversationAbandoned(ConversationAbandonedEvent arg0) {
		// TODO Auto-generated method stub
		
	}
	
}
