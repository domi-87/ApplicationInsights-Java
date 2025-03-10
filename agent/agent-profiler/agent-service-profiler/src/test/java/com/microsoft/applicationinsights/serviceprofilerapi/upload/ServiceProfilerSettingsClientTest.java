/*
 * ApplicationInsights-Java
 * Copyright (c) Microsoft Corporation
 * All rights reserved.
 *
 * MIT License
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this
 * software and associated documentation files (the ""Software""), to deal in the Software
 * without restriction, including without limitation the rights to use, copy, modify, merge,
 * publish, distribute, sublicense, and/or sell copies of the Software, and to permit
 * persons to whom the Software is furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED *AS IS*, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
 * PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE
 * FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 */
package com.microsoft.applicationinsights.serviceprofilerapi.upload;

import java.io.IOException;
import java.net.URISyntaxException;

import com.microsoft.applicationinsights.profiler.ProfilerConfiguration;
import com.microsoft.applicationinsights.serviceprofilerapi.client.ClientClosedException;
import com.microsoft.applicationinsights.serviceprofilerapi.client.ServiceProfilerClientV2;
import com.microsoft.applicationinsights.serviceprofilerapi.config.ServiceProfilerSettingsClient;
import io.reactivex.Maybe;
import org.junit.*;
import org.mockito.Mockito;

public class ServiceProfilerSettingsClientTest {

    @Test
    public void badServiceResponseDoesNotProvideReturn() throws ClientClosedException, IOException, URISyntaxException {
        ServiceProfilerClientV2 serviceProfilerClient = Mockito.mock(ServiceProfilerClientV2.class);

        Mockito.when(serviceProfilerClient.getSettings(Mockito.any())).thenReturn("");

        ServiceProfilerSettingsClient settingsClient = new ServiceProfilerSettingsClient(serviceProfilerClient);
        Maybe<ProfilerConfiguration> result = settingsClient.pullSettings();

        try {
            result.blockingGet();
        } catch (Exception e) {
            //expected
            return;
        }
        Assert.fail("Exception not thrown");
    }
}
