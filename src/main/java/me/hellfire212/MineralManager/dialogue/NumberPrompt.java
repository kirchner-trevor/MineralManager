package me.hellfire212.MineralManager.dialogue;

import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.NumericPrompt;
import org.bukkit.conversations.Prompt;

class NumberPrompt extends NumericPrompt {
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
		return CreateRegion.promptText(ptext);
	}

	@Override
	protected Prompt acceptValidatedInput(ConversationContext context, Number num) {
		context.setSessionData(storeAs, num);
		return next;
	}
	
}