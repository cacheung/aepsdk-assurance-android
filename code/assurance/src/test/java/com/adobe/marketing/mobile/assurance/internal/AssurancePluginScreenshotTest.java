/*
  Copyright 2022 Adobe. All rights reserved.
  This file is licensed to you under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License. You may obtain a copy
  of the License at http://www.apache.org/licenses/LICENSE-2.0
  Unless required by applicable law or agreed to in writing, software distributed under
  the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR REPRESENTATIONS
  OF ANY KIND, either express or implied. See the License for the specific language
  governing permissions and limitations under the License.
*/

package com.adobe.marketing.mobile.assurance.internal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.PixelCopy;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import com.adobe.marketing.mobile.services.ServiceProvider;
import com.adobe.marketing.mobile.services.AppContextService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import com.adobe.marketing.mobile.assurance.internal.AssuranceConstants.UILogColorVisibility;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 28)
public class AssurancePluginScreenshotTest {

    private static final String PAYLOAD_BLOBID = "blobId";
    private static final String PAYLOAD_MIMETYPE = "mimeType";
    private static final String PAYLOAD_ERROR = "error";

    private MockedStatic<AssuranceBlob> mockedStaticAssuranceBlob;
    private AssurancePluginScreenshot assurancePluginScreenshot;
    private AssuranceSession mockSession;
    private AssuranceEvent mockAssuranceEvent;
    private ServiceProvider mockServiceProvider;
    private Display mockDisplay;
    private AppContextService mockAppContextService;

    @Before
    public void testSetup() {
        mockSession = Mockito.mock(AssuranceSession.class);
        mockAssuranceEvent = Mockito.mock(AssuranceEvent.class);
        mockedStaticAssuranceBlob = Mockito.mockStatic(AssuranceBlob.class);
        mockServiceProvider = Mockito.mock(ServiceProvider.class);
        mockDisplay = Mockito.mock(Display.class);
        mockAppContextService = Mockito.mock(AppContextService.class);

        // create plugin instance to test
        assurancePluginScreenshot = new AssurancePluginScreenshot();
        assurancePluginScreenshot.onRegistered(mockSession);
    }

    @Test
    public void test_getVendorName() {
        // test
        String vendor = assurancePluginScreenshot.getVendor();
        assertEquals(vendor, AssuranceTestConstants.VENDOR_ASSURANCE_MOBILE);
    }

    @Test
    public void test_getControlType() {
        // test
        String vendor = assurancePluginScreenshot.getControlType();
        assertEquals(vendor, AssuranceTestConstants.ControlType.SCREENSHOT);
    }

    @Test
    public void test_OnRegister() {
        // test
        assurancePluginScreenshot.onRegistered(mockSession);

        // verify
        assertEquals(mockSession, assurancePluginScreenshot.getParentSession());
    }

    @Test
    public void test_noOpMethods_ShouldNotCrash() {
        // test
        assurancePluginScreenshot.onSessionConnected();
        assurancePluginScreenshot.onSessionDisconnected(0);
    }

    @Test
    public void test_onTakeScreenShotEventReceived() {
        // prepare
        Activity mockActivity = Mockito.mock(Activity.class);
        Window mockWindow = Mockito.mock(Window.class);
        View mockView = Mockito.mock(View.class);
        WindowManager mockWindowManager = Mockito.mock(WindowManager.class);
        DisplayMetrics mockDisplayMetrics = new DisplayMetrics();
        mockDisplayMetrics.widthPixels = 1080;
        mockDisplayMetrics.heightPixels = 1920;

        try (MockedStatic<ServiceProvider> mockedServiceProvider = mockStatic(ServiceProvider.class)) {
            mockedServiceProvider.when(ServiceProvider::getInstance).thenReturn(mockServiceProvider);
            when(mockServiceProvider.getAppContextService()).thenReturn(mockAppContextService);
            when(mockAppContextService.getCurrentActivity()).thenReturn(mockActivity);
            when(mockActivity.getWindow()).thenReturn(mockWindow);
            when(mockWindow.getDecorView()).thenReturn(mockView);
            when(mockView.getRootView()).thenReturn(mockView);
            when(mockView.getWidth()).thenReturn(1080);
            when(mockView.getHeight()).thenReturn(1920);
            when(mockActivity.getWindowManager()).thenReturn(mockWindowManager);
            when(mockWindowManager.getDefaultDisplay()).thenReturn(mockDisplay);
            doAnswer(invocation -> {
                DisplayMetrics metrics = invocation.getArgument(0);
                metrics.widthPixels = 1080;
                metrics.heightPixels = 1920;
                return null;
            }).when(mockDisplay).getMetrics(any(DisplayMetrics.class));

            // test
            assurancePluginScreenshot.onEventReceived(mockAssuranceEvent);

            // verify
            verify(mockActivity, times(2)).getWindow();
            verify(mockWindow).getDecorView();
            verify(mockView).getRootView();
            verify(mockView, times(2)).getWidth();
            verify(mockView, times(2)).getHeight();
            verify(mockView).getLocationOnScreen(any(int[].class));
        }
    }

    @Test
    public void test_onSuccessful_ScreenShotUpload() {
        // prepare
        Activity mockActivity = Mockito.mock(Activity.class);
        Window mockWindow = Mockito.mock(Window.class);
        View mockView = Mockito.mock(View.class);
        WindowManager mockWindowManager = Mockito.mock(WindowManager.class);
        DisplayMetrics mockDisplayMetrics = new DisplayMetrics();
        mockDisplayMetrics.widthPixels = 1080;
        mockDisplayMetrics.heightPixels = 1920;

        try (MockedStatic<ServiceProvider> mockedServiceProvider = mockStatic(ServiceProvider.class);
             MockedStatic<PixelCopy> mockedPixelCopy = mockStatic(PixelCopy.class)) {
            
            mockedServiceProvider.when(ServiceProvider::getInstance).thenReturn(mockServiceProvider);
            when(mockServiceProvider.getAppContextService()).thenReturn(mockAppContextService);
            when(mockAppContextService.getCurrentActivity()).thenReturn(mockActivity);
            when(mockActivity.getWindow()).thenReturn(mockWindow);
            when(mockWindow.getDecorView()).thenReturn(mockView);
            when(mockView.getRootView()).thenReturn(mockView);
            when(mockView.getWidth()).thenReturn(1080);
            when(mockView.getHeight()).thenReturn(1920);
            when(mockActivity.getWindowManager()).thenReturn(mockWindowManager);
            when(mockWindowManager.getDefaultDisplay()).thenReturn(mockDisplay);
            doAnswer(invocation -> {
                DisplayMetrics metrics = invocation.getArgument(0);
                metrics.widthPixels = 1080;
                metrics.heightPixels = 1920;
                return null;
            }).when(mockDisplay).getMetrics(any(DisplayMetrics.class));

            mockedPixelCopy.when(() -> PixelCopy.request(
                    any(Window.class),
                    any(Rect.class),
                    any(Bitmap.class),
                    any(PixelCopy.OnPixelCopyFinishedListener.class),
                    any(Handler.class)
            )).thenAnswer(invocation -> {
                PixelCopy.OnPixelCopyFinishedListener listener = invocation.getArgument(3);
                listener.onPixelCopyFinished(PixelCopy.SUCCESS);
                return null;
            });

            final ArgumentCaptor<AssuranceBlob.BlobUploadCallback> assuranceBlobCallbackCaptor =
                    ArgumentCaptor.forClass(AssuranceBlob.BlobUploadCallback.class);
            final ArgumentCaptor<AssuranceEvent> assuranceEventCaptor =
                    ArgumentCaptor.forClass(AssuranceEvent.class);

            // test
            assurancePluginScreenshot.onEventReceived(mockAssuranceEvent);

            // verify upload method call
            mockedStaticAssuranceBlob.verify(
                    () ->
                            AssuranceBlob.upload(
                                    any(byte[].class),
                                    anyString(),
                                    any(AssuranceSession.class),
                                    assuranceBlobCallbackCaptor.capture()),
                    times(1));

            // test 2 - Call Success callback
            assuranceBlobCallbackCaptor.getValue().onSuccess("sampleBlobID");

            // verify if screenshot event is queued
            verify(mockSession, times(1))
                    .logLocalUI(AssuranceConstants.UILogColorVisibility.LOW, "Screenshot taken");
            verify(mockSession, times(1)).queueOutboundEvent(assuranceEventCaptor.capture());
            AssuranceEvent queuedEvent = assuranceEventCaptor.getValue();
            assertNotNull(queuedEvent);
            assertEquals(AssuranceTestConstants.AssuranceEventType.BLOB, queuedEvent.type);
            assertEquals("sampleBlobID", queuedEvent.payload.get(PAYLOAD_BLOBID));
            assertEquals("image/png", queuedEvent.payload.get(PAYLOAD_MIMETYPE));
        }
    }

    @Test
    public void test_onFailure_ToUploadScreenShot() {
        // prepare
        Activity mockActivity = Mockito.mock(Activity.class);
        Window mockWindow = Mockito.mock(Window.class);
        View mockView = Mockito.mock(View.class);
        WindowManager mockWindowManager = Mockito.mock(WindowManager.class);
        DisplayMetrics mockDisplayMetrics = new DisplayMetrics();
        mockDisplayMetrics.widthPixels = 1080;
        mockDisplayMetrics.heightPixels = 1920;
        Handler mockHandler = Mockito.mock(Handler.class);

        try (MockedStatic<ServiceProvider> mockedServiceProvider = mockStatic(ServiceProvider.class);
             MockedStatic<PixelCopy> mockedPixelCopy = mockStatic(PixelCopy.class)) {
            
            mockedServiceProvider.when(ServiceProvider::getInstance).thenReturn(mockServiceProvider);
            when(mockServiceProvider.getAppContextService()).thenReturn(mockAppContextService);
            when(mockAppContextService.getCurrentActivity()).thenReturn(mockActivity);
            when(mockActivity.getWindow()).thenReturn(mockWindow);
            when(mockWindow.getDecorView()).thenReturn(mockView);
            when(mockView.getRootView()).thenReturn(mockView);
            when(mockView.getWidth()).thenReturn(1080);
            when(mockView.getHeight()).thenReturn(1920);
            when(mockActivity.getWindowManager()).thenReturn(mockWindowManager);
            when(mockWindowManager.getDefaultDisplay()).thenReturn(mockDisplay);
            doAnswer(invocation -> {
                DisplayMetrics metrics = invocation.getArgument(0);
                metrics.widthPixels = 1080;
                metrics.heightPixels = 1920;
                return null;
            }).when(mockDisplay).getMetrics(any(DisplayMetrics.class));

            mockedPixelCopy.when(() -> PixelCopy.request(
                    any(Window.class),
                    any(Rect.class),
                    any(Bitmap.class),
                    any(PixelCopy.OnPixelCopyFinishedListener.class),
                    any(Handler.class)
            )).thenAnswer(invocation -> {
                PixelCopy.OnPixelCopyFinishedListener listener = invocation.getArgument(3);
                listener.onPixelCopyFinished(PixelCopy.ERROR_SOURCE_INVALID);
                return null;
            });

            // test
            assurancePluginScreenshot.onEventReceived(mockAssuranceEvent);

            // verify
            verify(mockSession, times(1))
                    .logLocalUI(AssuranceConstants.UILogColorVisibility.LOW, "Screenshot capture failed");
            verify(mockSession, times(1)).queueOutboundEvent(any(AssuranceEvent.class));
        }
    }

    @Test
    @Config(sdk = 25) // Android 7.1.1
    public void test_onUnsupportedAndroidVersion() {
        // prepare
        Activity mockActivity = Mockito.mock(Activity.class);
        try (MockedStatic<ServiceProvider> mockedServiceProvider = mockStatic(ServiceProvider.class)) {
            mockedServiceProvider.when(ServiceProvider::getInstance).thenReturn(mockServiceProvider);
            when(mockServiceProvider.getAppContextService()).thenReturn(mockAppContextService);
            when(mockAppContextService.getCurrentActivity()).thenReturn(mockActivity);

            // test
            assurancePluginScreenshot.onEventReceived(mockAssuranceEvent);

            // verify
            verify(mockSession, times(1))
                    .logLocalUI(AssuranceConstants.UILogColorVisibility.LOW, "Screenshot not supported on Android versions below 8.0 (API 26)");
            verify(mockSession, times(1)).queueOutboundEvent(any(AssuranceEvent.class));
        }
    }

    @Test
    public void test_onNullActivity() {
        // prepare
        try (MockedStatic<ServiceProvider> mockedServiceProvider = mockStatic(ServiceProvider.class)) {
            mockedServiceProvider.when(ServiceProvider::getInstance).thenReturn(mockServiceProvider);
            when(mockServiceProvider.getAppContextService()).thenReturn(mockAppContextService);
            when(mockAppContextService.getCurrentActivity()).thenReturn(null);

            // test
            assurancePluginScreenshot.onEventReceived(mockAssuranceEvent);

            // verify no interactions with session
            verify(mockSession, never()).logLocalUI(any(AssuranceConstants.UILogColorVisibility.class), anyString());
            verify(mockSession, never()).queueOutboundEvent(any(AssuranceEvent.class));
        }
    }

    @Test
    public void test_onNullParentSession() {
        // prepare
        Activity mockActivity = Mockito.mock(Activity.class);
        try (MockedStatic<ServiceProvider> mockedServiceProvider = mockStatic(ServiceProvider.class)) {
            mockedServiceProvider.when(ServiceProvider::getInstance).thenReturn(mockServiceProvider);
            when(mockServiceProvider.getAppContextService()).thenReturn(mockAppContextService);
            when(mockAppContextService.getCurrentActivity()).thenReturn(mockActivity);

            // Set parent session to null
            assurancePluginScreenshot.onSessionTerminated();

            // test
            assurancePluginScreenshot.onEventReceived(mockAssuranceEvent);

            // verify no interactions with session
            verify(mockSession, never()).logLocalUI(any(AssuranceConstants.UILogColorVisibility.class), anyString());
            verify(mockSession, never()).queueOutboundEvent(any(AssuranceEvent.class));
        }
    }

    @Test
    public void test_onBlobUploadFailure() {
        // prepare
        Activity mockActivity = Mockito.mock(Activity.class);
        Window mockWindow = Mockito.mock(Window.class);
        View mockView = Mockito.mock(View.class);
        WindowManager mockWindowManager = Mockito.mock(WindowManager.class);
        DisplayMetrics mockDisplayMetrics = new DisplayMetrics();
        mockDisplayMetrics.widthPixels = 1080;
        mockDisplayMetrics.heightPixels = 1920;

        try (MockedStatic<ServiceProvider> mockedServiceProvider = mockStatic(ServiceProvider.class);
             MockedStatic<PixelCopy> mockedPixelCopy = mockStatic(PixelCopy.class)) {
            
            mockedServiceProvider.when(ServiceProvider::getInstance).thenReturn(mockServiceProvider);
            when(mockServiceProvider.getAppContextService()).thenReturn(mockAppContextService);
            when(mockAppContextService.getCurrentActivity()).thenReturn(mockActivity);
            when(mockActivity.getWindow()).thenReturn(mockWindow);
            when(mockWindow.getDecorView()).thenReturn(mockView);
            when(mockView.getRootView()).thenReturn(mockView);
            when(mockView.getWidth()).thenReturn(1080);
            when(mockView.getHeight()).thenReturn(1920);
            when(mockActivity.getWindowManager()).thenReturn(mockWindowManager);
            when(mockWindowManager.getDefaultDisplay()).thenReturn(mockDisplay);
            doAnswer(invocation -> {
                DisplayMetrics metrics = invocation.getArgument(0);
                metrics.widthPixels = 1080;
                metrics.heightPixels = 1920;
                return null;
            }).when(mockDisplay).getMetrics(any(DisplayMetrics.class));

            // Mock successful PixelCopy
            mockedPixelCopy.when(() -> PixelCopy.request(
                    any(Window.class),
                    any(Rect.class),
                    any(Bitmap.class),
                    any(PixelCopy.OnPixelCopyFinishedListener.class),
                    any(Handler.class)
            )).thenAnswer(invocation -> {
                PixelCopy.OnPixelCopyFinishedListener listener = invocation.getArgument(3);
                listener.onPixelCopyFinished(PixelCopy.SUCCESS);
                return null;
            });

            final ArgumentCaptor<AssuranceBlob.BlobUploadCallback> assuranceBlobCallbackCaptor =
                    ArgumentCaptor.forClass(AssuranceBlob.BlobUploadCallback.class);

            // test
            assurancePluginScreenshot.onEventReceived(mockAssuranceEvent);

            // verify upload method call
            mockedStaticAssuranceBlob.verify(
                    () ->
                            AssuranceBlob.upload(
                                    any(byte[].class),
                                    anyString(),
                                    any(AssuranceSession.class),
                                    assuranceBlobCallbackCaptor.capture()),
                    times(1));

            // Simulate blob upload failure
            assuranceBlobCallbackCaptor.getValue().onFailure("Upload failed");

            // verify error handling
            verify(mockSession, times(1))
                    .logLocalUI(AssuranceConstants.UILogColorVisibility.LOW, "Error while taking screenshot - Description: Upload failed");
            verify(mockSession, times(1)).queueOutboundEvent(any(AssuranceEvent.class));
        }
    }

    @Test
    public void test_onExceptionDuringScreenshotCapture() {
        // prepare
        Activity mockActivity = Mockito.mock(Activity.class);
        Window mockWindow = Mockito.mock(Window.class);
        View mockView = Mockito.mock(View.class);
        WindowManager mockWindowManager = Mockito.mock(WindowManager.class);
        DisplayMetrics mockDisplayMetrics = new DisplayMetrics();
        mockDisplayMetrics.widthPixels = 1080;
        mockDisplayMetrics.heightPixels = 1920;

        try (MockedStatic<ServiceProvider> mockedServiceProvider = mockStatic(ServiceProvider.class)) {
            mockedServiceProvider.when(ServiceProvider::getInstance).thenReturn(mockServiceProvider);
            when(mockServiceProvider.getAppContextService()).thenReturn(mockAppContextService);
            when(mockAppContextService.getCurrentActivity()).thenReturn(mockActivity);
            when(mockActivity.getWindow()).thenReturn(mockWindow);
            when(mockWindow.getDecorView()).thenReturn(mockView);
            when(mockView.getRootView()).thenReturn(mockView);
            when(mockView.getWidth()).thenThrow(new RuntimeException("Test exception"));

            // test
            assurancePluginScreenshot.onEventReceived(mockAssuranceEvent);

            // verify error handling
            verify(mockSession, times(1))
                    .logLocalUI(UILogColorVisibility.LOW, "Screenshot capture failed");
            verify(mockSession, times(1)).queueOutboundEvent(any(AssuranceEvent.class));
        }
    }

    @Test
    public void test_onSessionTerminated() {
        // test
        assurancePluginScreenshot.onSessionTerminated();

        // verify
        assertEquals(null, assurancePluginScreenshot.getParentSession());
    }

    @Test
    public void test_onSessionConnected() {
        // test - should not throw any exception
        assurancePluginScreenshot.onSessionConnected();
    }

    @Test
    public void test_onSessionDisconnected() {
        // test - should not throw any exception
        assurancePluginScreenshot.onSessionDisconnected(0);
    }

    @Test
    public void test_getVendorAndControlType() {
        // test
        String vendor = assurancePluginScreenshot.getVendor();
        String controlType = assurancePluginScreenshot.getControlType();

        // verify
        assertEquals(AssuranceConstants.VENDOR_ASSURANCE_MOBILE, vendor);
        assertEquals(AssuranceConstants.ControlType.SCREENSHOT, controlType);
    }

    @After
    public void teardown() {
        mockedStaticAssuranceBlob.close();
    }
}
