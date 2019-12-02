package com.chromaclypse.handytools.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import com.chromaclypse.api.messages.Text;

public class Echo implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command command, String prefix, String[] arguments) {
		String input = Text.format().colorize(String.join(" ", arguments));
		
		sender.sendMessage(Text.format().wrap(Integer.MAX_VALUE, input).toArray(new String[0]));
		
		return true;
	}

}
