package me.hellfire212.MineralManager.dialogue;

import org.bukkit.conversations.Conversable;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.Prompt;
import org.bukkit.entity.Player;

public abstract class SlashCommandAllowedPrompt extends CaseInsensitiveFixedSetPrompt {
	public SlashCommandAllowedPrompt(String ... choices) {
		super(choices);
	}
	
	@Override
	public boolean isInputValid(ConversationContext ctx, String s) {
		if (s.startsWith("/")) return true;
		return super.isInputValid(ctx, s);
	}

	@Override
	protected Prompt acceptValidatedInput(ConversationContext context, String input) {
		Conversable c = context.getForWhom();
		Player player = null;
		if (c instanceof Player) {
			player = (Player) c;
			if (input.startsWith("/")) {
				return handleSlashCommand(context, player, input);
			}
		}
		return handleNormalInput(context, player, input);
		
	}
	
	protected Prompt handleSlashCommand(ConversationContext context, Player player, String input) {
		context.setSessionData("inslash", true);
		player.performCommand(input.substring(1));
		context.setSessionData("inslash", null);
		return this;
	}
	
	protected abstract Prompt handleNormalInput(ConversationContext context, Player player, String input);

}
