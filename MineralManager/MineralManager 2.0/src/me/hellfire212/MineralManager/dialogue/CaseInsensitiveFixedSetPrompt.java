package me.hellfire212.MineralManager.dialogue;

import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.FixedSetPrompt;

public abstract class CaseInsensitiveFixedSetPrompt extends FixedSetPrompt {

	public CaseInsensitiveFixedSetPrompt(String ... choices) {
		super(choices);
	}

	@Override
	public boolean isInputValid(ConversationContext ctx, String s) {
		return super.isInputValid(ctx, s.toLowerCase());
	}

}
