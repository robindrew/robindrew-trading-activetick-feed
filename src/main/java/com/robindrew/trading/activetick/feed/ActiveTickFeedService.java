package com.robindrew.trading.activetick.feed;

import com.robindrew.common.service.AbstractService;
import com.robindrew.common.service.component.heartbeat.HeartbeatComponent;
import com.robindrew.common.service.component.logging.LoggingComponent;
import com.robindrew.common.service.component.properties.PropertiesComponent;
import com.robindrew.common.service.component.stats.StatsComponent;
import com.robindrew.trading.activetick.feed.activetick.ActiveTickComponent;
import com.robindrew.trading.activetick.feed.jetty.JettyComponent;

public class ActiveTickFeedService extends AbstractService {

	/**
	 * Entry point for the TurnEngine Admin Client Service.
	 */
	public static void main(String[] args) {
		ActiveTickFeedService service = new ActiveTickFeedService(args);
		service.startAsync();
	}

	private final HeartbeatComponent heartbeat = new HeartbeatComponent();
	private final PropertiesComponent properties = new PropertiesComponent();
	private final LoggingComponent logging = new LoggingComponent();
	private final StatsComponent stats = new StatsComponent();
	private final JettyComponent jetty = new JettyComponent();
	private final ActiveTickComponent activeTick = new ActiveTickComponent();

	public ActiveTickFeedService(String[] args) {
		super(args);
	}

	@Override
	protected void startupService() throws Exception {
		start(properties);
		start(logging);
		start(heartbeat);
		start(stats);
		start(jetty);
		start(activeTick);
	}

	@Override
	protected void shutdownService() throws Exception {
		stop(heartbeat);
	}
}
