package me.hellfire212.MineralManager.dialogue;

import me.hellfire212.MineralManager.Coordinate;
import me.hellfire212.MineralManager.utils.ChatMagic;

import org.bukkit.Location;
import org.bukkit.conversations.Conversable;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.FixedSetPrompt;
import org.bukkit.conversations.Prompt;
import org.bukkit.entity.Player;

/** Create a "Region" type selection */
class RegionSelectPrompt extends FixedSetPrompt {
	private Prompt next;
	private static String endText = ChatMagic.colorize("type {VERB}end {TEXT}to mark the end location");
	private static String verboseEndText = ChatMagic.colorize(
			"{TEXT}Walk to the opposite corner and " + endText 
			+ "\n - You can also type {VERB}check{TEXT} to see where the box would end"
			+ "\n - {RED}NOTE{TEXT}, to make a 3D box you will need to go to the"
			+ "\n   top corner if you started at the bottom corner, or "
			+ "\n   vice versa."
	);

	public RegionSelectPrompt(Prompt next) {
		super("begin", "start", "end", "help", "check");
		this.next = next;
	}
	
	@Override
	public boolean isInputValid(ConversationContext ctx, String s) {
		if (s.startsWith("/")) return true;
		return super.isInputValid(ctx, s.toLowerCase());
	}

	@Override
	public String getPromptText(ConversationContext ctx) {
		if (ctx.getSessionData("region.start") == null) {
			return ChatMagic.colorize("{TEXT}Go to the start corner of the box and type {VERB}start{TEXT} to mark the start location.");
		} else {
			if (ctx.getSessionData("brc") == null) {
				ctx.setSessionData("brc", true);
				return verboseEndText;
			} else {
				return CreateRegion.promptText(endText);
			}
		}
	}

	@Override
	protected Prompt acceptValidatedInput(ConversationContext ctx, String s) {
		Conversable c = ctx.getForWhom();
		//Location loc = null;
		if (c instanceof Player) {
			Player player = (Player) c;
			Location loc = player.getLocation();
			Coordinate current = new Coordinate(loc);
			boolean has_start = (ctx.getSessionData("region.start") != null);
			if (s.startsWith("/")) {
				player.performCommand(s.substring(1));
				return this;
			} else if (s.equals("begin") || s.equals("start")) {
				ctx.getForWhom().sendRawMessage(String.format(
						"Marked beginning point x=%d, y=%d, z=%d",
						loc.getBlockX(),
						loc.getBlockY(),
						loc.getBlockZ()
				));
				ctx.setSessionData("region.start", current);
			} else if (s.equals("end")) {
				if (has_start) {
					ctx.setSessionData("region.end", current);
				} else {
					ctx.getForWhom().sendRawMessage("Must set a begin point first.");
				}
			} else if (s.equals("check")) {
				if (has_start) {
					regionCheck(ctx, current);
				}
			}
			if (has_start && ctx.getSessionData("region.end") != null) {
				return next;
			}
			return this;
		} else {
			ChatMagic.send(ctx.getForWhom(), "{RED}You're not a player, whaaat?");
			return Prompt.END_OF_CONVERSATION;
		}
	}
	
	protected void regionCheck(ConversationContext ctx, Coordinate current) {
		Location loc = current.getLocation();
		Location begin = ((Coordinate)ctx.getSessionData("region.start")).getLocation();
		String tpl = (
				  "{AQUA}If you were to end the region here, you'd have a region\n"
				+ "   {RED}%dx%d {AQUA}wide, {RED}%d {AQUA}tall \n"
				+ "   from (%d, %d, %d) to (%d, %d, %d)"
		);
		ChatMagic.send(ctx.getForWhom(), tpl,
				Math.abs(loc.getBlockX() - begin.getBlockX()),
				Math.abs(loc.getBlockZ() - begin.getBlockZ()),
				Math.abs(loc.getBlockY() - begin.getBlockY()),
				loc.getBlockX(), loc.getBlockY(), loc.getBlockZ(),
				begin.getBlockX(), begin.getBlockY(), begin.getBlockZ()
		);
	}
	
}