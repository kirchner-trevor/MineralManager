package me.hellfire212.MineralManager.dialogue;

import me.hellfire212.MineralManager.Coordinate;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.conversations.Conversable;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.FixedSetPrompt;
import org.bukkit.conversations.Prompt;
import org.bukkit.entity.Player;

/** Create a "Region" type selection */
class RegionSelectPrompt extends FixedSetPrompt {
	private Prompt next;

	public RegionSelectPrompt(Prompt next) {
		super("begin", "start", "end", "help", "check");
		this.next = next;
	}

	@Override
	public String getPromptText(ConversationContext ctx) {
		if (ctx.getSessionData("region.start") == null) {
			return CreateRegion.promptText("Go to the start corner of the box and type start to mark the start location.");
		} else {
			String endText = "type " + ChatColor.GREEN + "end" + ChatColor.BLUE + " to mark the end location"; 
			if (ctx.getSessionData("brc") == null) {
				ctx.setSessionData("brc", true);
				return (
					CreateRegion.promptText("Walk to the opposite corner and " + endText) 
					+ ChatColor.AQUA 
					+ "\n - You can also type check to see where the box would end"
					+ ChatColor.RED
					+ "\n - Remember that to make a 3D box you will need to go to the"
					+ "\n   top corner if you started at the bottom corner, or "
					+ "\n   vice versa."
				);
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
			
			if (s.equals("begin") || s.equals("start")) {
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
			ctx.getForWhom().sendRawMessage(ChatColor.RED + "You're not a player, whaaat?");
			return Prompt.END_OF_CONVERSATION;
		}
	}
	
	protected void regionCheck(ConversationContext ctx, Coordinate current) {
		StringBuilder b = new StringBuilder();
		b.append(ChatColor.AQUA);
		b.append("If you were to end the region here, you'd have a region ");
		Location loc = current.getLocation();
		Location begin = ((Coordinate)ctx.getSessionData("region.start")).getLocation();
		b.append(String.format("%dx%d wide, %d tall from (%d, %d, %d) to (%d, %d, %d)",
				Math.abs(loc.getBlockX() - begin.getBlockX()),
				Math.abs(loc.getBlockZ() - begin.getBlockZ()),
				Math.abs(loc.getBlockY() - begin.getBlockY()),
				loc.getBlockX(), loc.getBlockY(), loc.getBlockZ(),
				begin.getBlockZ(), begin.getBlockY(), begin.getBlockZ()
		));
		ctx.getForWhom().sendRawMessage(b.toString());
	}
	
}