/*
 * Copyright (c) 2018 Abex
 * Copyright (c) 2018, Adam <Adam@sigterm.info>
 * Copyright (c) 2019, Wynadorn <https://github.com/Wynadorn>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.vardoinkus;

import com.google.common.collect.ImmutableSet;
import com.google.inject.Provides;
import org.apache.commons.lang3.ArrayUtils;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.GraphicsObject;
import net.runelite.api.NPC;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.GraphicsObjectCreated;
import net.runelite.api.events.NpcDespawned;
import net.runelite.api.events.NpcSpawned;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;

@PluginDescriptor(
	name = "Duke Sucellus Helper",
	description = "Helps with Duke Sucellus",
	tags = {"duke", "sucellus", "pvm"},
	enabledByDefault = false
)
public class DukeSucellusPlugin extends Plugin
{
	public static final String CONFIG_GROUP = "dukesucellushelper";
	private static final int DUKE_SUCELLUS_REGION_ID = 12132;
	private static final int DUKE_SUCELLUS_ID = 12191;
	private static final int FALLING_ROCK_ID = 1447;
	private static final int SMOKE_ID = 12198;
	private static final int SMOKE_PROJECTILE_ID = 2436;

	//timings are not consistent between projectile'd smoke and regular floor smoke, but once its active or 4 ticks pass we can remove indicator
	private static final int SMOKE_GRAPHIC_UNDER_ID = 2431;
	private static final int SMOKE_GRAPHIC_ACTIVE_ID = 2432;
	private static final Set<Integer> ALARM_IDS = ImmutableSet.of(
		12199,
		12200,
		12201
	);

	private static final int ALARM_TICKS = 4;
	private static final int SMOKE_TICKS = 4;
	private static final int ROCK_TIME_TICKS = 4;

	@Inject
	private Client client;

	@Inject
	private DukeSucellusConfig config;

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private DukeSucellusOverlay overlay;

	//config
	public boolean showAlarms;
	public boolean showSmoke;
	public boolean showRocks;

	//data
	public boolean inDuke = false;
	public int tickId = 0;
	public HashMap<NPC, Integer> alarms = new HashMap<>();
	public HashMap<NPC, Integer> smokes = new HashMap<>();
	public HashMap<GraphicsObject, Integer> rocks = new HashMap<>();

	@Provides
	DukeSucellusConfig getConfig(ConfigManager configManager)
	{
		return configManager.getConfig(DukeSucellusConfig.class);
	}

	@Override
	protected void startUp()
	{
		updateConfig();
		addOverlays();
	}

	@Override
	protected void shutDown()
	{
		removeOverlays();
	}

	private void addOverlays()
	{
		overlayManager.add(overlay);
	}

	private void removeOverlays()
	{
		overlayManager.remove(overlay);
	}

	@Subscribe
	public void onConfigChanged(ConfigChanged event)
	{
		if (!event.getGroup().equals(CONFIG_GROUP))
		{
			return;
		}

		updateConfig();
	}

	private void updateConfig()
	{
		showAlarms = config.showAlarms();
		showSmoke = config.showSmoke();
		showRocks = config.showRocks();
	}

	@Subscribe
	private void onNpcSpawned(NpcSpawned event)
	{
		if (!inDuke)
		{
			return;
		}

		NPC npc = event.getNpc();
		if (npc.getId() == SMOKE_ID)
		{
			smokes.put(npc, tickId + SMOKE_TICKS);
		}
		else if (ALARM_IDS.contains(npc.getId()))
		{
			alarms.put(npc, tickId + ALARM_TICKS);
		}
	}

	@Subscribe
	private void onGraphicsObjectCreated(GraphicsObjectCreated event)
	{
		if (!inDuke)
		{
			return;
		}

		if (event.getGraphicsObject().getId() == FALLING_ROCK_ID)
		{
			rocks.put(event.getGraphicsObject(), tickId + ROCK_TIME_TICKS);
		}
	}

	@Subscribe
	private void onGameTick(GameTick event)
	{
		inDuke = ArrayUtils.contains(client.getMapRegions(), DUKE_SUCELLUS_REGION_ID);
		if (!inDuke)
		{
			return;
		}

		tickId++;

		alarms.entrySet().removeIf(entry -> tickId > entry.getValue());
		smokes.entrySet().removeIf(entry -> tickId > entry.getValue());
		rocks.entrySet().removeIf(entry -> tickId > entry.getValue());
	}
}
