package org.opentripplanner.ext.siri.updater;

import jakarta.xml.bind.JAXBException;
import java.time.Duration;
import org.opentripplanner.framework.io.OtpHttpClient;
import org.opentripplanner.updater.spi.HttpHeaders;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.org.siri.siri20.Siri;

/**
 * Load real-time updates from SIRI-SX and SIRI-ET feeds over HTTP.
 */
public class SiriHttpLoader {

  private static final Logger LOG = LoggerFactory.getLogger(SiriHttpLoader.class);
  private final HttpHeaders requestHeaders;
  private final String url;
  private final Duration timeout;
  private final Duration previewInterval;

  public SiriHttpLoader(String url, Duration timeout, HttpHeaders requestHeaders) {
    this(url, timeout, requestHeaders, null);
  }

  public SiriHttpLoader(
    String url,
    Duration timeout,
    HttpHeaders requestHeaders,
    Duration previewInterval
  ) {
    this.url = url;
    this.timeout = timeout;
    this.requestHeaders = requestHeaders;
    this.previewInterval = previewInterval;
  }

  /**
   * Send a SIRI-SX service request and unmarshal the response as JAXB.
   */
  public Siri fetchSXFeed(String requestorRef) throws JAXBException {
    RequestTimer requestTimer = new RequestTimer("SX");
    requestTimer.init();
    String sxServiceRequest = SiriHelper.createSXServiceRequestAsXml(requestorRef);
    requestTimer.serviceRequestCreated();
    return fetchFeed(sxServiceRequest, requestTimer, requestorRef);
  }

  /**
   * Send a SIRI-ET service request and unmarshal the response as JAXB.
   */
  public Siri fetchETFeed(String requestorRef) throws JAXBException {
    RequestTimer requestTimer = new RequestTimer("ET");
    requestTimer.init();
    String etServiceRequest = SiriHelper.createETServiceRequestAsXml(requestorRef, previewInterval);
    requestTimer.serviceRequestCreated();
    return fetchFeed(etServiceRequest, requestTimer, requestorRef);
  }

  private Siri fetchFeed(String serviceRequest, RequestTimer requestTimer, String requestorRef) {
    try (OtpHttpClient otpHttpClient = new OtpHttpClient(timeout, timeout)) {
      return otpHttpClient.postXmlAndMap(
        url,
        serviceRequest,
        timeout,
        requestHeaders.asMap(),
        is -> {
          requestTimer.responseFetched();
          Siri siri = SiriHelper.unmarshal(is);
          requestTimer.responseUnmarshalled();
          return siri;
        }
      );
    } finally {
      LOG.info(
        "Updating SIRI-{} [{}]: Create req: {} ms, Fetching data: {} ms, Unmarshalling: {} ms",
        requestTimer.feedType,
        requestorRef,
        requestTimer.creating(),
        requestTimer.fetching(),
        requestTimer.unmarshalling()
      );
    }
  }

  private static final class RequestTimer {

    private final String feedType;
    private long initAt;
    private long createdAt;
    private long fetchedAt;
    private long unmarshalledAt;

    RequestTimer(String feedType) {
      this.feedType = feedType;
    }

    void init() {
      initAt = System.currentTimeMillis();
    }

    void serviceRequestCreated() {
      createdAt = System.currentTimeMillis();
    }

    void responseFetched() {
      fetchedAt = System.currentTimeMillis();
    }

    void responseUnmarshalled() {
      unmarshalledAt = System.currentTimeMillis();
    }

    /**
     *
     * @return time spent creating the request body.
     */
    long creating() {
      return createdAt - initAt;
    }

    /**
     *
     * @return time spent fetching the response.
     */
    long fetching() {
      return fetchedAt - createdAt;
    }

    /**
     *
     * @return time spent unmarshalling the response.
     */
    long unmarshalling() {
      return unmarshalledAt - fetchedAt;
    }
  }
}
