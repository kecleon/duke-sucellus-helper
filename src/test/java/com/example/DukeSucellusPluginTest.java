package com.example;

import com.vardoinkus.DukeSucellusPlugin;
import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class DukeSucellusPluginTest
{
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(DukeSucellusPlugin.class);
		RuneLite.main(args);
	}
}