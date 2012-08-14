package me.hellfire212.MineralManager.dialogue;

import me.hellfire212.MineralManager.Commands;
import me.hellfire212.MineralManager.utils.ChatMagic;

import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.FixedSetPrompt;
import org.bukkit.conversations.Prompt;
import org.bukkit.entity.Player;

public class LassoSelectPrompt extends FixedSetPrompt {
	private final Prompt next;
	private final String LASSO_HELP = ChatMagic.colorize(
			"  {GREEN}Lasso Selection:{TEXT} This selection works by having you\n" +
			"  walk around and select a non-square or strange shaped region.\n" +
			""
	);
	
	private final String LASSO_SELECT_BEGIN = ChatMagic.colorize(
			"  {TEXT}type {VERB}start {TEXT}to begin lasso."
	);
	
	private final String LASSO_SELECT_RUNNING = ChatMagic.colorize(
			"  {TEXT}Put in {VERB}end {TEXT} when done selecting the region."
	);

	public LassoSelectPrompt(Prompt next) {
		super("start", "begin", "finish", "end");
		this.next = next;
	}

	@Override
	public String getPromptText(ConversationContext ctx) {
		if (ctx.getSessionData("lasso.running") == null) {
			if (ctx.getSessionData("lasso.helpshown") == null) {
				ctx.setSessionData("lasso.helpshown", true);
				return LASSO_HELP + LASSO_SELECT_BEGIN;
			} else {
				return LASSO_SELECT_BEGIN;
			}
		} else {
			return LASSO_SELECT_RUNNING;
		}
	}

	@Override
	protected Prompt acceptValidatedInput(ConversationContext ctx, String input) {
		if (input.equals("finish") || input.equals("end")) {
			return next;
		} else if (input.equals("start") || input.equals("begin")) {
			Commands.beginLasso((Player) ctx.getForWhom());
			return this;
		}
		return null;
	}

}
