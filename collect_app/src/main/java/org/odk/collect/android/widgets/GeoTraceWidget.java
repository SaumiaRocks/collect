/*
 * Copyright (C) 2015 GeoODK
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package org.odk.collect.android.widgets;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.data.StringData;
import org.javarosa.form.api.FormEntryPrompt;
import org.odk.collect.android.R;
import org.odk.collect.android.activities.GeoPolyActivity;
import org.odk.collect.android.formentry.questions.QuestionDetails;
import org.odk.collect.android.geo.MapConfigurator;
import org.odk.collect.android.listeners.PermissionListener;
import org.odk.collect.android.utilities.MultiClickGuard;
import org.odk.collect.android.utilities.ThemeUtils;
import org.odk.collect.android.widgets.interfaces.BinaryDataReceiver;
import org.odk.collect.android.widgets.utilities.WaitingForDataRegistry;

import static org.odk.collect.android.utilities.ApplicationConstants.RequestCodes;

/**
 * GeoTraceWidget allows the user to collect a trace of GPS points as the
 * device moves along a path.
 *
 * @author Jon Nordling (jonnordling@gmail.com)
 */
@SuppressLint("ViewConstructor")
public class GeoTraceWidget extends QuestionWidget implements BinaryDataReceiver {
    private final MapConfigurator mapConfigurator;
    private final WaitingForDataRegistry waitingForDataRegistry;

    protected Button startGeoButton;
    protected TextView answerDisplay;
    protected boolean readOnly;

    public GeoTraceWidget(Context context, QuestionDetails questionDetails, MapConfigurator mapConfigurator, WaitingForDataRegistry waitingForDataRegistry) {
        super(context, questionDetails);
        this.mapConfigurator = mapConfigurator;
        this.waitingForDataRegistry = waitingForDataRegistry;
    }

    @Override
    protected View onCreateAnswerView(Context context, FormEntryPrompt prompt, int answerFontSize) {
        ViewGroup answerView = (ViewGroup) LayoutInflater.from(context).inflate(R.layout.base_geo_widget_layout, null);

        answerDisplay = answerView.findViewById(R.id.geo_answer_text);
        answerDisplay.setTextColor(new ThemeUtils(context).getColorOnSurface());
        answerDisplay.setTextSize(TypedValue.COMPLEX_UNIT_DIP, answerFontSize);

        startGeoButton = answerView.findViewById(R.id.simple_button);

        readOnly = getFormEntryPrompt().isReadOnly();
        if (readOnly) {
            startGeoButton.setVisibility(GONE);
        } else {
            startGeoButton.setText(getDefaultButtonLabel());
            startGeoButton.setTextSize(TypedValue.COMPLEX_UNIT_DIP, answerFontSize);

            startGeoButton.setOnClickListener(v -> onButtonClick());
        }

        String answerText = prompt.getAnswerText();
        boolean dataAvailable = false;

        if (answerText != null && !answerText.isEmpty()) {
            dataAvailable = true;
            setBinaryData(answerText);
        }

        updateButtonLabelsAndVisibility(dataAvailable);
        return answerView;
    }

    @Override
    public void clearAnswer() {
        answerDisplay.setText(null);
        updateButtonLabelsAndVisibility(false);
        widgetValueChanged();
    }

    @Override
    public void setOnLongClickListener(OnLongClickListener l) {
        startGeoButton.setOnLongClickListener(l);
        answerDisplay.setOnLongClickListener(l);
    }

    @Override
    public void cancelLongPress() {
        super.cancelLongPress();
        startGeoButton.cancelLongPress();
        answerDisplay.cancelLongPress();
    }

    @Override
    public void setBinaryData(Object answer) {
        answerDisplay.setText((String) answer);
        updateButtonLabelsAndVisibility((String) answer != null);
        widgetValueChanged();
    }

    @Override
    public IAnswerData getAnswer() {
        String s = answerDisplay.getText().toString();
        return !s.equals("")
                ? new StringData(s)
                : null;
    }

    private void onButtonClick() {
        if (MultiClickGuard.allowClick(QuestionWidget.class.getName())) {
            getPermissionUtils().requestLocationPermissions((Activity) getContext(), new PermissionListener() {
                @Override
                public void granted() {
                    waitingForDataRegistry.waitForData(getFormEntryPrompt().getIndex());
                    startGeoActivity();
                }

                @Override
                public void denied() {
                }
            });
        }
    }

    private String getDefaultButtonLabel() {
        return getContext().getString(R.string.get_trace);
    }

    public void updateButtonLabelsAndVisibility(boolean dataAvailable) {
        startGeoButton.setText(dataAvailable ? R.string.geotrace_view_change_location : R.string.get_trace);
    }

    public void startGeoActivity() {
        Context context = getContext();
        if (mapConfigurator.isAvailable(context)) {
            Intent intent = new Intent(context, GeoPolyActivity.class)
                    .putExtra(GeoPolyActivity.ANSWER_KEY, answerDisplay.getText().toString())
                    .putExtra(GeoPolyActivity.OUTPUT_MODE_KEY, GeoPolyActivity.OutputMode.GEOTRACE);
            ((Activity) getContext()).startActivityForResult(intent, RequestCodes.GEOTRACE_CAPTURE);
        } else {
            mapConfigurator.showUnavailableMessage(context);
        }
    }

}
