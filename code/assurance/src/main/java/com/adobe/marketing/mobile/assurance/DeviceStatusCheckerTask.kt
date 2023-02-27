/*
 * Copyright 2022 Adobe. All rights reserved.
 * This file is licensed to you under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License. You may obtain a copy
 * of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software distributed under
 * the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR REPRESENTATIONS
 * OF ANY KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */

package com.adobe.marketing.mobile.assurance

import com.adobe.marketing.mobile.AdobeCallback
import com.adobe.marketing.mobile.assurance.AssuranceConstants.AssuranceQuickConnectError
import com.adobe.marketing.mobile.services.HttpConnecting
import com.adobe.marketing.mobile.services.HttpMethod
import com.adobe.marketing.mobile.services.NetworkRequest
import com.adobe.marketing.mobile.services.Networking
import com.adobe.marketing.mobile.services.NetworkingConstants
import org.json.JSONObject
import javax.net.ssl.HttpsURLConnection

internal class DeviceStatusCheckerTask(
    private val orgId: String,
    private val clientId: String,
    private val networkService: Networking,
    private val callback: AdobeCallback<Response<HttpConnecting, AssuranceQuickConnectError>>
) : Runnable {

    override fun run() {
        val networkRequest = buildRequest()

        networkService.connectAsync(networkRequest) { response ->
            if (response == null) {
                callback.call(Response.Failure(AssuranceQuickConnectError.UNEXPECTED_ERROR))
            }

            val responseCode = response.responseCode
            if (!(responseCode == HttpsURLConnection.HTTP_CREATED || responseCode == HttpsURLConnection.HTTP_OK)) {
                callback.call(Response.Failure(AssuranceQuickConnectError.REQUEST_FAILED))
            } else {
                callback.call(Response.Success(response))
            }

            response.close()
        }
    }

    private fun buildRequest(): NetworkRequest {
        val url = "${DeviceRegistrationManager.BASE_DEVICE_API_URL}/${DeviceRegistrationManager.DEVICE_API_PATH_STATUS}"

        val body: Map<String, String> = mapOf(
            DeviceRegistrationManager.KEY_ORG_ID to orgId,
            DeviceRegistrationManager.KEY_CLIENT_ID to clientId
        )

        val headers: Map<String, String> = mapOf(
            NetworkingConstants.Headers.ACCEPT to NetworkingConstants.HeaderValues.CONTENT_TYPE_JSON_APPLICATION,
            NetworkingConstants.Headers.CONTENT_TYPE to NetworkingConstants.HeaderValues.CONTENT_TYPE_JSON_APPLICATION
        )
        val jsonBody = JSONObject(body)
        val bodyBytes = jsonBody.toString().toByteArray()
        return NetworkRequest(
            url,
            HttpMethod.POST,
            bodyBytes,
            headers,
            DeviceRegistrationManager.CONNECTION_TIMEOUT_MS,
            DeviceRegistrationManager.READ_TIMEOUT_MS
        )
    }
}
