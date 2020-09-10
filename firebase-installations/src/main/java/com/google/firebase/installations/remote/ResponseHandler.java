// Copyright 2020 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.firebase.installations.remote;

import androidx.annotation.GuardedBy;
import com.google.firebase.installations.Utils;
import java.util.concurrent.TimeUnit;

/**
 * The {@link ResponseHandler} class calculates the next allowed request time. Also, decides whether
 * a network request to FIS servers is allowed to execute.
 *
 * @hide
 */
class ResponseHandler {
  private static final long BACKOFF_TIME_24H_IN_MILLIS = TimeUnit.HOURS.toMillis(24);
  private static final long BACKOFF_TIME_30_MINS_IN_MILLIS = TimeUnit.MINUTES.toMillis(30);
  private final Utils utils;

  @GuardedBy("this")
  private long nextAllowedRequestTime = Long.MAX_VALUE;

  @GuardedBy("this")
  private int attemptCount = 0;

  ResponseHandler(Utils utils) {
    this.utils = utils;
  }

  public synchronized void setNextAllowedRequestTime(int responseCode) {
    if (responseCode == 200) {
      resetAllowedRequestTime();
      return;
    }
    attemptCount++;
    long backOffTime = getBackoffTime(responseCode);
    nextAllowedRequestTime = utils.currentTimeInMillis() + backOffTime;
  }

  private synchronized void resetAllowedRequestTime() {
    nextAllowedRequestTime = Long.MAX_VALUE;
    attemptCount = 0;
  }

  private synchronized long getBackoffTime(int responseCode) {
    // Fixed 24 hours silence period for non-retryable errors. Read more: b/160751425.
    if (isNonRetryableError(responseCode)) {
      return BACKOFF_TIME_24H_IN_MILLIS;
    }
    // Quickly increasing dynamically configured back-off strategy for Retryable errors. Read more:
    // https://cloud.google.com/storage/docs/exponential-backoff.
    return (long)
        Math.min(
            Math.pow(2, attemptCount) + utils.getRandomMillis(), BACKOFF_TIME_30_MINS_IN_MILLIS);
  }

  // Response codes classified as non-retryable for FIS API, all other response codes will be
  // treated as retryable-errors. Read more: go/fis-api-error-code-classification.
  private static boolean isNonRetryableError(int responseCode) {
    return responseCode == 400 || responseCode == 403;
  }

  public boolean isRequestAllowed() {
    long currentTime = utils.currentTimeInMillis();
    boolean isRequestAllowed = attemptCount == 0 || currentTime > nextAllowedRequestTime;

    if (currentTime > nextAllowedRequestTime) {
      synchronized (this) {
        nextAllowedRequestTime = Long.MAX_VALUE;
      }
    }
    return isRequestAllowed;
  }
}