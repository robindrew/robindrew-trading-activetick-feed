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
import com.robindrew.trading.activetick.feed.activetick.session.SessionManager;
import com.robindrew.trading.platform.ITradingPlatform;
import com.robindrew.trading.platform.streaming.IInstrumentPriceStream;
import com.robindrew.trading.price.candle.io.stream.sink.PriceCandleFileSink;
import com.robindrew.trading.price.precision.PricePrecision;
import com.robindrew.trading.provider.activetick.platform.AtConnection;
import com.robindrew.trading.provider.activetick.platform.AtCredentials;
import com.robindrew.trading.provider.activetick.platform.AtInstrument;
import com.robindrew.trading.provider.activetick.platform.AtTradingPlatform;
import com.robindrew.trading.provider.activetick.platform.IAtInstrument;
import com.robindrew.trading.provider.activetick.platform.history.AtHistoryService;
import com.robindrew.trading.provider.activetick.platform.streaming.AtStreamingService;

public class ActiveTickComponent extends AbstractIdleComponent {

	private static final Logger log = LoggerFactory.getLogger(ActiveTickComponent.class);

	private static final IProperty<String> propertyApiKey = new StringProperty("activetick.api.key");
	private static final IProperty<String> propertyUsername = new StringProperty("activetick.username");
	private static final IProperty<String> propertyPassword = new StringProperty("activetick.password");
	private static final IProperty<String> propertyTickOutputDir = new StringProperty("tick.output.dir");

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
		AtTradingPlatform platform = new AtTradingPlatform(credentials, streaming, history);
		setDependency(ITradingPlatform.class, platform);

		log.info("Subscribing ...");
		createStreamingSubscriptions();
	}

	private void createStreamingSubscriptions() {

		// Currencies
		createStreamingSubscription(AtInstrument.SPOT_AUD_USD, new PricePrecision(2));
		createStreamingSubscription(AtInstrument.SPOT_EUR_JPY, new PricePrecision(2));
		createStreamingSubscription(AtInstrument.SPOT_EUR_USD, new PricePrecision(2));
		createStreamingSubscription(AtInstrument.SPOT_GBP_USD, new PricePrecision(2));
		createStreamingSubscription(AtInstrument.SPOT_USD_CHF, new PricePrecision(2));
		createStreamingSubscription(AtInstrument.SPOT_USD_JPY, new PricePrecision(2));
	}

	private void createStreamingSubscription(IAtInstrument instrument, PricePrecision precision) {
		AtTradingPlatform platform = getDependency(ITradingPlatform.class);

		// Create the output file
		PriceCandleFileSink priceFileSink = new PriceCandleFileSink(instrument, new File(propertyTickOutputDir.get()));
		priceFileSink.start();

		// Register all the sinks
		IInstrumentPriceStream<IAtInstrument> priceStream = platform.getStreamingService().getPriceStream(instrument);
		priceStream.register(priceFileSink);
	}

	@Override
	protected void shutdownComponent() throws Exception {
		// TODO: Cancel all subscriptions here
	}

}
