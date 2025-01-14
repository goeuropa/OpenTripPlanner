package org.opentripplanner.routing.api.request.framework;

import java.time.Duration;
import java.util.Objects;
import org.opentripplanner.framework.model.Cost;
import org.opentripplanner.framework.model.TimeAndCost;

/**
 * The time and cost penalty is used to calculate an extra penalty on time and cost.
 */
public record TimeAndCostPenalty(TimePenalty timePenalty, double costFactor) {
  public static final TimeAndCostPenalty ZERO = new TimeAndCostPenalty(TimePenalty.ZERO, 0.0);

  public TimeAndCostPenalty {
    costFactor = Units.normalizedFactor(costFactor, 0.0, 10.0);
    timePenalty = Objects.requireNonNull(timePenalty);

    if (timePenalty.isZero() && costFactor != 0.0) {
      throw new IllegalArgumentException(
        "When time-penalty is zero, the costFactor have no effect and should be zero as well."
      );
    }
  }

  public static TimeAndCostPenalty of(String timePenalty, double costFactor) {
    return new TimeAndCostPenalty(TimePenalty.of(timePenalty), costFactor);
  }

  /**
   * Calculate the time and the cost penalty.
   */
  public TimeAndCost calculate(int timeInSeconds) {
    Duration dt = timePenalty.calculate(timeInSeconds);
    return new TimeAndCost(dt, Cost.costOfSeconds(dt.toSeconds() * costFactor));
  }

  /**
   * Returns {@code true} if there is no penalty to apply.
   */
  public boolean isEmpty() {
    return ZERO.equals(this);
  }

  @Override
  public String toString() {
    var buf = new StringBuilder("(timePenalty: " + timePenalty);

    // CostFactor is optional
    if (costFactor != 0.0) {
      buf.append(", costFactor: ").append(Units.factorToString(costFactor));
    }
    return buf.append(")").toString();
  }
}
