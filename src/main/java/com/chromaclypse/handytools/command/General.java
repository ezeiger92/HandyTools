package com.chromaclypse.handytools.command;

import com.chromaclypse.api.command.Context;
import com.chromaclypse.api.messages.Text;

public class General {
	public static boolean echo(Context context) {
		context.Sender().sendMessage(Text.format().wrap(Integer.MAX_VALUE, context.SplatArgs(0)).toArray(new String[0]));
		return true;
	}
}
