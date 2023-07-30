package com.vardoinkus;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.util.HashMap;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.GraphicsObject;
import net.runelite.api.NPC;
import net.runelite.api.Perspective;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayUtil;

public class DukeSucellusOverlay extends Overlay
{
	private final Client client;
	private final DukeSucellusPlugin plugin;

	private static final Color ALARM_BORDER = new Color( 0, 220, 255, 180);
	private static final Color ALARM_FILL = new Color( 0, 220, 255, 40);
	private static final Color SMOKE_BORDER = new Color( 120, 255, 0, 180);
	private static final Color SMOKE_FILL = new Color( 120, 255, 0, 30);
	private static final Color ROCK_BORDER = new Color( 255, 0, 0, 180);
	private static final Color ROCK_FILL = new Color( 255, 0, 0, 30);

	@Inject
	private DukeSucellusOverlay(Client client, DukeSucellusPlugin plugin)
	{
		setPosition(OverlayPosition.DYNAMIC);
		setLayer(OverlayLayer.ABOVE_SCENE);
		this.client = client;
		this.plugin = plugin;
	}

	@Override
	public Dimension render(Graphics2D g)
	{
		if (!plugin.inDuke)
		{
			return null;
		}

		if (plugin.showAlarms)
		{
			for (HashMap.Entry<NPC, Integer> entry : plugin.alarms.entrySet())
			{
				renderTile(g, entry.getKey().getLocalLocation(), ALARM_BORDER, 1, ALARM_FILL);
			}
		}

		if (plugin.showSmoke)
		{
			for (HashMap.Entry<NPC, Integer> entry : plugin.smokes.entrySet())
			{
				renderTileAoe(g, entry.getKey().getLocalLocation(), SMOKE_BORDER, 1, SMOKE_FILL);
			}
		}

		if (plugin.showRocks)
		{
			for (HashMap.Entry<GraphicsObject, Integer> entry : plugin.rocks.entrySet())
			{
				renderTile(g, entry.getKey().getLocation(), ROCK_BORDER, 1, ROCK_FILL);
			}
		}

		return null;
	}

	private void renderTile(final Graphics2D graphics, final LocalPoint dest, final Color color, final double borderWidth, final Color fillColor)
	{
		final Polygon poly = Perspective.getCanvasTilePoly(client, dest);

		if (poly == null)
		{
			return;
		}

		OverlayUtil.renderPolygon(graphics, poly, color, fillColor, new BasicStroke((float) borderWidth));
	}

	private void renderTileAoe(final Graphics2D graphics, final LocalPoint dest, final Color color, final double borderWidth, final Color fillColor)
	{
		final Polygon poly = Perspective.getCanvasTileAreaPoly(client, dest, 3);

		if (poly == null)
		{
			return;
		}

		OverlayUtil.renderPolygon(graphics, poly, color, fillColor, new BasicStroke((float) borderWidth));
	}
}