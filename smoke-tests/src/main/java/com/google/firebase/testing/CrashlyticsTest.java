// Copyright 2021 Google LLC
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

package com.google.firebase.testing;

import static com.google.common.truth.Truth.assertThat;

import com.google.firebase.crashlytics.FirebaseCrashlytics;

import androidx.test.runner.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public final class CrashlyticsTest {

  @Test
  public void setCustomKeys() {
    FirebaseCrashlytics.getInstance().setCustomKey("TestKey", "TestValue");

    // TODO There's not a public API to confirm the values were properly set,
    // but they can be checked via reflection.

  }

  @Test
  public void log() {
    FirebaseCrashlytics.getInstance().log("This is a log message");

    // TODO hook into the FirebaseAnalytics listener to confirm that the event is logged to analytics.
  }

  @Test
  public void didCrashOnPreviousExecution() {
    assertThat(FirebaseCrashlytics.getInstance().didCrashOnPreviousExecution()).isFalse();
  }

}
