package org.opentripplanner.routing.algorithm.raptoradapter.transit;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.opentripplanner.model.Frequency;
import org.opentripplanner.model.StopPattern;
import org.opentripplanner.model.StopTime;
import org.opentripplanner.model.TripPattern;
import org.opentripplanner.routing.trippattern.FrequencyEntry;
import org.opentripplanner.routing.trippattern.TripTimes;
import org.opentripplanner.transit.model._data.TransitModelForTest;
import org.opentripplanner.transit.model.network.Route;
import org.opentripplanner.transit.model.site.Stop;

class TripPatternForDateTest {

  private static final Stop STOP = TransitModelForTest.stopForTest("TEST:STOP", 0, 0);
  private static final TripTimes tripTimes = Mockito.mock(TripTimes.class);

  @Test
  void shouldExcludeAndIncludeBasedOnFrequency() {
    Route route = TransitModelForTest.route("1").build();

    var stopTime = new StopTime();
    stopTime.setStop(STOP);
    StopPattern stopPattern = new StopPattern(List.of(stopTime));
    TripPattern pattern = new TripPattern(TransitModelForTest.id("P1"), route, stopPattern);

    TripPatternWithRaptorStopIndexes tripPattern = new TripPatternWithRaptorStopIndexes(
      pattern,
      new int[0]
    );

    var f = new Frequency();
    var withFrequencies = new TripPatternForDate(
      tripPattern,
      List.of(tripTimes),
      List.of(new FrequencyEntry(f, tripTimes)),
      LocalDate.now()
    );

    assertNull(withFrequencies.newWithFilteredTripTimes(t -> false));
    assertNotNull(withFrequencies.newWithFilteredTripTimes(t -> true));
  }
}
