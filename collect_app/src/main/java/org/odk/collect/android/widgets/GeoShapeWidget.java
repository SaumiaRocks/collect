/*
 * Copyright (C) 2014 GeoODK
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
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;

import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.data.StringData;
import org.javarosa.form.api.FormEntryPrompt;

import org.odk.collect.android.R;
import org.odk.collect.android.activities.GeoPolyActivity;
import org.odk.collect.android.databinding.GeoWidgetAnswerBinding;
import org.odk.collect.android.formentry.questions.QuestionDetails;
import org.odk.collect.android.widgets.interfaces.WidgetDataReceiver;
import org.odk.collect.android.widgets.interfaces.GeoWidgetListener;
import org.odk.collect.android.widgets.utilities.GeoWidgetUtils;
import org.odk.collect.android.widgets.utilities.WaitingForDataRegistry;

import static org.odk.collect.android.utilities.ApplicationConstants.RequestCodes.GEOSHAPE_CAPTURE;

@SuppressLint("ViewConstructor")
public class GeoShapeWidget extends QuestionWidget implements WidgetDataReceiver {
    GeoWidgetAnswerBinding binding;
    Bundle bundle;

    private final WaitingForDataRegistry waitingForDataRegistry;
    private final GeoWidgetListener geoWidgetListener;

    public GeoShapeWidget(Context context, QuestionDetails questionDetails, WaitingForDataRegistry waitingForDataRegistry, GeoWidgetListener geoWidgetListener) {
        super(context, questionDetails);
        this.waitingForDataRegistry = waitingForDataRegistry;
        this.geoWidgetListener = geoWidgetListener;

        String answerText = getFormEntryPrompt().getAnswerText();
        boolean dataAvailable = false;
        if (answerText != null && !answerText.isEmpty()) {
            binding.geoAnswerText.setText(answerText);
            dataAvailable = true;
        }
        geoWidgetListener.setButtonLabelAndVisibility(binding, getFormEntryPrompt().isReadOnly(), dataAvailable,
                R.string.geoshape_view_read_only, R.string.geoshape_view_change_location, R.string.get_shape);
    }

    @Override
    protected View onCreateAnswerView(Context context, FormEntryPrompt prompt, int answerFontSize) {
        binding = GeoWidgetAnswerBinding.inflate(((Activity) context).getLayoutInflater());

        binding.simpleButton.setTextSize(TypedValue.COMPLEX_UNIT_DIP, answerFontSize);
        binding.geoAnswerText.setTextSize(TypedValue.COMPLEX_UNIT_DIP, answerFontSize);

        binding.simpleButton.setOnClickListener(v -> {
            bundle = GeoWidgetUtils.getGeoPolyBundle(binding.geoAnswerText.getText().toString(),
                    GeoPolyActivity.OutputMode.GEOSHAPE, prompt.isReadOnly());
            geoWidgetListener.onButtonClicked(context, prompt.getIndex(), getPermissionUtils(), null,
                    waitingForDataRegistry, GeoPolyActivity.class, bundle, GEOSHAPE_CAPTURE);
        });

        return binding.getRoot();
    }

    @Override
    public IAnswerData getAnswer() {
        String stringAnswer = binding.geoAnswerText.getText().toString();
        return stringAnswer.isEmpty() ? null : new StringData(stringAnswer);
    }

    @Override
    public void clearAnswer() {
        binding.geoAnswerText.setText(null);
        geoWidgetListener.setButtonLabelAndVisibility(binding, getFormEntryPrompt().isReadOnly(), false,
                R.string.geoshape_view_read_only, R.string.geoshape_view_change_location, R.string.get_shape);
        widgetValueChanged();
    }

    @Override
    public void setOnLongClickListener(OnLongClickListener l) {
        binding.simpleButton.setOnLongClickListener(l);
        binding.geoAnswerText.setOnLongClickListener(l);
    }

    @Override
    public void cancelLongPress() {
        super.cancelLongPress();
        binding.simpleButton.cancelLongPress();
        binding.geoAnswerText.cancelLongPress();
    }

    @Override
    public void setData(Object answer) {
        binding.geoAnswerText.setText(answer.toString());
        geoWidgetListener.setButtonLabelAndVisibility(binding, getFormEntryPrompt().isReadOnly(), !answer.toString().isEmpty(),
                R.string.geoshape_view_read_only, R.string.geoshape_view_change_location, R.string.get_shape);
        widgetValueChanged();
    }
}
