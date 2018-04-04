package com.robindrew.trading.activetick.feed.jetty.page;

import static com.robindrew.common.dependency.DependencyFactory.getDependency;

import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import com.robindrew.common.http.servlet.executor.IVelocityHttpContext;
import com.robindrew.common.http.servlet.request.IHttpRequest;
import com.robindrew.common.http.servlet.response.IHttpResponse;
import com.robindrew.common.service.component.jetty.handler.page.AbstractServicePage;
import com.robindrew.trading.platform.ITradingPlatform;
import com.robindrew.trading.platform.streaming.IInstrumentPriceStream;
import com.robindrew.trading.platform.streaming.IStreamingService;

public class FeedsPage extends AbstractServicePage {

	public FeedsPage(IVelocityHttpContext context, String templateName) {
		super(context, templateName);
	}

	@Override
	protected void execute(IHttpRequest request, IHttpResponse response, Map<String, Object> dataMap) {
		super.execute(request, response, dataMap);

//		IIgSession session = getDependency(IIgSession.class);
//		dataMap.put("user", session.getCredentials().getUsername());
//		dataMap.put("environment", session.getEnvironment());

		ITradingPlatform platform = getDependency(ITradingPlatform.class);
		IStreamingService service = platform.getStreamingService();
		dataMap.put("feeds", getFeeds(service.getPriceStreams()));
	}

	private Set<Feed> getFeeds(Set<IInstrumentPriceStream> subscriptions) {
		Set<Feed> feeds = new TreeSet<>();
		for (IInstrumentPriceStream subscription : subscriptions) {
			feeds.add(new Feed(subscription));
		}
		return feeds;
	}

	public static class Feed implements Comparable<Feed> {

		private final IInstrumentPriceStream subscription;
		private final FeedPrice price;

		public Feed(IInstrumentPriceStream subscription) {
			this.subscription = subscription;
			this.price = new FeedPrice(subscription);
		}

		public String getId() {
			return subscription.getInstrument().getName().replace('.', '_');
		}
		
		public IInstrumentPriceStream getSubscription() {
			return subscription;
		}

		public FeedPrice getPrice() {
			return price;
		}

		@Override
		public int compareTo(Feed that) {
			return this.getPrice().compareTo(that.getPrice());
		}
	}
}
