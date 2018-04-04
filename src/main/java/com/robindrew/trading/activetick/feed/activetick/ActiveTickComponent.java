package com.robindrew.trading.activetick.feed.activetick;

import static com.robindrew.common.dependency.DependencyFactory.getDependency;
import static com.robindrew.common.dependency.DependencyFactory.setDependency;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.robindrew.common.mbean.IMBeanRegistry;
import com.robindrew.common.mbean.annotated.AnnotatedMBeanRegistry;
import com.robindrew.common.properties.map.type.IProperty;
import com.robindrew.common.properties.map.type.StringProperty;
import com.robindrew.common.service.component.AbstractIdleComponent;
import com.robindrew.trading.IInstrument;
import com.robindrew.trading.activetick.feed.activetick.session.SessionManager;
import com.robindrew.trading.platform.ITradingPlatform;
import com.robindrew.trading.platform.streaming.IStreamingService;
import com.robindrew.trading.price.precision.PricePrecision;
import com.robindrew.trading.price.tick.io.stream.sink.PriceTickFileSink;
import com.robindrew.trading.provider.activetick.platform.AtConnection;
import com.robindrew.trading.provider.activetick.platform.AtCredentials;
import com.robindrew.trading.provider.activetick.platform.AtInstrument;
import com.robindrew.trading.provider.activetick.platform.AtTradingPlatform;
import com.robindrew.trading.provider.activetick.platform.history.AtHistoryService;
import com.robindrew.trading.provider.activetick.platform.streaming.AtInstrumentPriceStream;
import com.robindrew.trading.provider.activetick.platform.streaming.AtStreamingService;

public class ActiveTickComponent extends AbstractIdleComponent {

	private static final Logger log = LoggerFactory.getLogger(ActiveTickComponent.class);

	private static final IProperty<String> propertyApiKey = new StringProperty("activetick.api.key");
	private static final IProperty<String> propertyUsername = new StringProperty("activetick.username");
	private static final IProperty<String> propertyPassword = new StringProperty("activetick.password");

	@Override
	protected void startupComponent() throws Exception {
		IMBeanRegistry registry = new AnnotatedMBeanRegistry();

		String apiKey = propertyApiKey.get();
		String username = propertyUsername.get();
		String password = propertyPassword.get();

		AtCredentials credentials = new AtCredentials(apiKey, username, password);
		log.info("User: {}", credentials.getUsername());

		log.info("Creating Session Manager");
		SessionManager sessionManager = new SessionManager(credentials);
		registry.register(sessionManager);

		// Connection
		AtConnection connection = new AtConnection(credentials);
		log.info("Connecting ...");
		connection.connect();
		connection.login();

		// History
		AtHistoryService history = new AtHistoryService(connection);

		// Streaming
		AtStreamingService streaming = new AtStreamingService(connection);

		// Platform
		AtTradingPlatform platform = new AtTradingPlatform(streaming, history);
		setDependency(ITradingPlatform.class, platform);

		log.info("Subscribing ...");
		createStreamingSubscriptions();
	}

	private void createStreamingSubscriptions() {

		// Currencies
		createStreamingSubscription(AtInstrument.AUD_USD, new PricePrecision(2, 900, 90000));
		createStreamingSubscription(AtInstrument.EUR_JPY, new PricePrecision(2, 900, 90000));
		createStreamingSubscription(AtInstrument.EUR_USD, new PricePrecision(2, 900, 90000));
		createStreamingSubscription(AtInstrument.GBP_USD, new PricePrecision(2, 900, 90000));
		createStreamingSubscription(AtInstrument.USD_CHF, new PricePrecision(2, 900, 90000));
		createStreamingSubscription(AtInstrument.USD_JPY, new PricePrecision(2, 900, 90000));
	}

	private void createStreamingSubscription(IInstrument instrument, PricePrecision precision) {
		ITradingPlatform platform = getDependency(ITradingPlatform.class);

		// Create the underlying stream
		AtInstrumentPriceStream priceStream = new AtInstrumentPriceStream(instrument);

		// Create the output file
		PriceTickFileSink priceFileSink = new PriceTickFileSink(instrument, new File("c:/temp/prices/activetick"));
		priceFileSink.start();

		// Register the stream to make it available through the platform
		IStreamingService streamingService = platform.getStreamingService();
		streamingService.register(priceStream);

		// Register all the sinks
		priceStream.register(priceFileSink);
	}

	@Override
	protected void shutdownComponent() throws Exception {
		// TODO: Cancel all subscriptions here
	}

}
