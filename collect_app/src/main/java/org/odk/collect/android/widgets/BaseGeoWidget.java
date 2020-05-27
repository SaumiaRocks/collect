package org.odk.collect.android.widgets;

import android.app.Activity;
import android.content.Context;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import org.javarosa.form.api.FormEntryPrompt;
import org.odk.collect.android.R;
import org.odk.collect.android.formentry.questions.QuestionDetails;
import org.odk.collect.android.listeners.PermissionListener;
import org.odk.collect.android.utilities.MultiClickGuard;
import org.odk.collect.android.utilities.ThemeUtils;
import org.odk.collect.android.widgets.interfaces.ButtonWidget;
import org.odk.collect.android.widgets.interfaces.GeoWidget;

import static org.odk.collect.android.formentry.questions.WidgetViewUtils.getCenteredAnswerTextView;

public abstract class BaseGeoWidget extends QuestionWidget implements GeoWidget {
    public Button startGeoButton;
    public TextView answerDisplay;
    protected boolean readOnly;

    public BaseGeoWidget(Context context, QuestionDetails questionDetails) {
        super(context, questionDetails);
    }

    @Override
    protected View onCreateAnswerView(Context context, FormEntryPrompt prompt, int answerFontSize) {
        ViewGroup answerView = (ViewGroup) LayoutInflater.from(context).inflate(R.layout.base_geo_widget_layout, null);

        answerDisplay = getCenteredAnswerTextView(getContext(), getAnswerFontSize());
        answerDisplay = answerView.findViewById(R.id.geo_answer_text);
        answerDisplay.setTextColor(new ThemeUtils(context).getColorOnSurface());
        answerDisplay.setTextSize(TypedValue.COMPLEX_UNIT_DIP, answerFontSize);

        startGeoButton = answerView.findViewById(R.id.start_geo_button);
        if (readOnly) {
            startGeoButton.setVisibility(GONE);
        } else {
            startGeoButton.setText(getDefaultButtonLabel());
            startGeoButton.setTextSize(TypedValue.COMPLEX_UNIT_DIP, answerFontSize);

            startGeoButton.setOnClickListener(v -> {
                if (MultiClickGuard.allowClick(QuestionWidget.class.getName())) {
                    ((ButtonWidget) this).onButtonClick(R.id.geo_answer_text);
                }
            });
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
        String answerText = answer.toString();
        answerDisplay.setText(getAnswerToDisplay(answerText));
        updateButtonLabelsAndVisibility(!answerText.isEmpty());
        widgetValueChanged();
    }

    public void onButtonClick(int buttonId) {
        getPermissionUtils().requestLocationPermissions((Activity) getContext(), new PermissionListener() {
            @Override
            public void granted() {
                waitForData();
                startGeoActivity();
            }

            @Override
            public void denied() {
            }
        });
    }
}
