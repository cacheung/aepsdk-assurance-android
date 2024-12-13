/*
 * Copyright 2023 Adobe. All rights reserved.
 * This file is licensed to you under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License. You may obtain a copy
 * of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software distributed under
 * the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR REPRESENTATIONS
 * OF ANY KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */

package com.adobe.assurance_tvos_testapp.ui.views

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.adobe.assurance_tvos_testapp.AssuranceTestAppConstants
import com.adobe.assurance_tvos_testapp.ui.viewmodel.AssuranceTestAppViewModel
import com.adobe.marketing.mobile.Assurance
import com.adobe.marketing.mobile.MobileCore
import com.adobe.assurance_tvos_testapp.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
internal fun AssuranceScreen(
    scope: CoroutineScope,
    assuranceTestAppViewModel: AssuranceTestAppViewModel
) {
    Box(modifier = Modifier.testTag(AssuranceTestAppConstants.TEST_TAG_ASSURANCE_SCREEN)) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .verticalScroll(rememberScrollState())
        ) {

            Row(
                modifier = Modifier
                    .padding(vertical = 16.dp, horizontal = 8.dp)
                    .align(Alignment.CenterHorizontally)
            ) {
                AssuranceVersionLabel(version = Assurance.extensionVersion())
            }

            Row(
                modifier = Modifier
                    .padding(vertical = 16.dp, horizontal = 8.dp)
                    .align(Alignment.CenterHorizontally)
            ) {
                AppIdConfiguration()
            }

            Row(
                modifier = Modifier
                    .padding(vertical = 16.dp, horizontal = 8.dp)
                    .align(Alignment.CenterHorizontally)
            ) {
                AssuranceConnectionInput()
            }

            Row(
                modifier = Modifier
                    .padding(vertical = 16.dp)
                    .align(Alignment.CenterHorizontally)
            ) {
                AssuranceEventChunking(scope, assuranceTestAppViewModel)
            }

            Row(
                modifier = Modifier
                    .padding(vertical = 16.dp)
                    .align(Alignment.CenterHorizontally)
            ) {
            }
        }
    }
}

@Composable
private fun AssuranceVersionLabel(version: String) {
    Text(
        text = "Assurance v${version}",
        fontWeight = FontWeight.Bold,
        fontSize = 18.sp
    )
}

@Composable
private fun AssuranceConnectionInput() {
    Column {
        Button(
            onClick = { Assurance.startSession() },
            modifier = Modifier
                .fillMaxWidth()
                .testTag(AssuranceTestAppConstants.TEST_TAG_QUICK_CONNECT_BUTTON)
        ) {
            Text(text = stringResource(id = R.string.assurance_quick_connect_button_name))
        }
    }
}

@Composable
private fun AppIdConfiguration() {
    var appId by remember {
        mutableStateOf("")
    }
    Column {
        TextField(
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text
            ),
            value = appId,
            onValueChange = { appId = it },
            placeholder = { Text(text = stringResource(id = R.string.assurance_appId_input_hint)) },
            modifier = Modifier
                .fillMaxWidth()
                .testTag(AssuranceTestAppConstants.TEST_TAG_APP_ID_INPUT)
        )

        Button(
            onClick = {
                println("###---"+appId+"---");
                MobileCore.configureWithAppID(appId) },
            modifier = Modifier
                .fillMaxWidth()
                .testTag(AssuranceTestAppConstants.TEST_TAG_CONFIGURE_WITH_APP_ID_BUTTON)
        ) {
            Text(text = stringResource(id = R.string.assurance_appId_button_name))
        }
    }
}

@Composable
private fun AssuranceEventChunking(
    scope: CoroutineScope,
    assuranceTestAppViewModel: AssuranceTestAppViewModel
) {
    Column() {
        Row(modifier = Modifier.align(Alignment.CenterHorizontally)) {
            Text(
                text = stringResource(id = R.string.event_chunking_section_title),
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        }

        Row(horizontalArrangement = Arrangement.SpaceEvenly) {
            Button(
                onClick = {
                    scope.launch {
                        assuranceTestAppViewModel.sendEvent(
                            AssuranceTestAppConstants.SMALL_EVENT_PAYLOAD_FILE
                        )
                    }
                },
                modifier = Modifier.padding(4.dp)
            ) {
                Text(text = stringResource(id = R.string.send_small_payload_button_name))
            }

            Button(
                onClick = {
                    scope.launch {
                        assuranceTestAppViewModel.sendEvent(
                            AssuranceTestAppConstants.LARGE_EVENT_PAYLOAD_FILE
                        )
                    }
                },
                modifier = Modifier.padding(4.dp)
            ) {
                Text(text = stringResource(id = R.string.send_large_payload_button_name))
            }
        }

        Row(horizontalArrangement = Arrangement.SpaceAround) {
            Button(
                onClick = {
                    scope.launch {
                        assuranceTestAppViewModel.sendEvent(
                            AssuranceTestAppConstants.LARGE_HTML_PAYLOAD_FILE
                        )
                    }
                },
                modifier = Modifier.padding(4.dp)
            ) {
                Text(text = stringResource(id = R.string.send_html_payload_button_name))
            }
        }
    }
}