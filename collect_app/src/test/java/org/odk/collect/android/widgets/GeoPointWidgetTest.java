package org.odk.collect.android.widgets;

import android.view.View;

import org.javarosa.core.model.data.GeoPointData;
import org.javarosa.form.api.FormEntryPrompt;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.odk.collect.android.R;
import org.odk.collect.android.formentry.questions.QuestionDetails;
import org.odk.collect.android.listeners.WidgetValueChangedListener;
import org.odk.collect.android.widgets.interfaces.GeoDataRequester;
import org.odk.collect.android.widgets.utilities.GeoWidgetUtils;
import org.odk.collect.android.widgets.utilities.WaitingForDataRegistry;
import org.robolectric.RobolectricTestRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.odk.collect.android.widgets.support.GeoWidgetHelpers.getRandomDoubleArray;
import static org.odk.collect.android.widgets.support.QuestionWidgetHelpers.mockValueChangedListener;
import static org.odk.collect.android.widgets.support.QuestionWidgetHelpers.promptWithAnswer;
import static org.odk.collect.android.widgets.support.QuestionWidgetHelpers.promptWithReadOnly;
import static org.odk.collect.android.widgets.support.QuestionWidgetHelpers.widgetTestActivity;

@RunWith(RobolectricTestRunner.class)
public class GeoPointWidgetTest {
    private final GeoPointData answer = new GeoPointData(getRandomDoubleArray());

    private GeoDataRequester geoDataRequester;
    private WaitingForDataRegistry waitingForDataRegistry;

    @Before
    public void setup() {
        geoDataRequester = mock(GeoDataRequester.class);
        waitingForDataRegistry = mock(WaitingForDataRegistry.class);
    }

    @Test
    public void usingReadOnlyOption_doesNotShowTheGeoButton() {
        GeoPointWidget widget = createWidget(promptWithReadOnly());
        assertEquals(widget.binding.simpleButton.getVisibility(), View.GONE);
    }

    @Test
    public void getAnswer_whenPromptDoesNotHaveAnswer_returnsNull() {
        GeoPointWidget widget = createWidget(promptWithAnswer(null));
        assertNull(widget.getAnswer());
    }

    @Test
    public void getAnswer_whenPromptHasAnswer_returnsAnswer() {
        GeoPointWidget widget = createWidget(promptWithAnswer(answer));
        assertEquals(widget.getAnswer().getDisplayText(), answer.getDisplayText());
    }

    @Test
    public void answerTextViewShouldShowCorrectAnswer() {
        GeoPointWidget widget = createWidget(promptWithAnswer(answer));
        assertEquals(widget.binding.geoAnswerText.getText(), GeoWidgetUtils.getAnswerToDisplay(widget.getContext(), answer.getDisplayText()));
    }

    @Test
    public void whenPromptDoesNotHaveHasAnswer_buttonShowsCorrectText() {
        GeoPointWidget widget = createWidget(promptWithAnswer(null));
        assertEquals(widget.binding.simpleButton.getText(), widget.getContext().getString(R.string.get_point));
    }

    @Test
    public void whenPromptHasAnswer_buttonShowsCorrectText() {
        GeoPointWidget widget = createWidget(promptWithAnswer(answer));
        assertEquals(widget.binding.simpleButton.getText(), widget.getContext().getString(R.string.change_location));
    }

    @Test
    public void clearAnswer_clearsWidgetAnswer() {
        GeoPointWidget widget = createWidget(promptWithAnswer(answer));
        widget.clearAnswer();

        assertEquals(widget.binding.geoAnswerText.getText(), "");
        assertEquals(widget.binding.simpleButton.getText(), widget.getContext().getString(R.string.get_point));
    }

    @Test
    public void clearAnswer_callsValueChangeListeners() {
        GeoPointWidget widget = createWidget(promptWithAnswer(null));
        WidgetValueChangedListener valueChangedListener = mockValueChangedListener(widget);
        widget.clearAnswer();

        verify(valueChangedListener).widgetValueChanged(widget);
    }

    @Test
    public void clickingButtonAndAnswerTextViewForLong_callsLongClickListeners() {
        View.OnLongClickListener listener = mock(View.OnLongClickListener.class);
        GeoPointWidget widget = createWidget(promptWithAnswer(null));

        widget.setOnLongClickListener(listener);
        widget.binding.simpleButton.performLongClick();
        widget.binding.geoAnswerText.performLongClick();

        verify(listener).onLongClick(widget.binding.simpleButton);
        verify(listener).onLongClick(widget.binding.geoAnswerText);
    }

    @Test
    public void setData_updatesWidgetDisplayedAnswer() {
        GeoPointWidget widget = createWidget(promptWithAnswer(null));
        widget.setBinaryData(answer.getDisplayText());
        assertEquals(widget.binding.geoAnswerText.getText(), GeoWidgetUtils.getAnswerToDisplay(widget.getContext(), answer.getDisplayText()));
    }

    @Test
    public void setData_whenDataIsNull_updatesButtonLabel() {
        GeoPointWidget widget = createWidget(promptWithAnswer(answer));
        widget.setBinaryData("");
        assertEquals(widget.binding.simpleButton.getText(), widget.getContext().getString(R.string.get_point));
    }

    @Test
    public void setData_whenDataIsNotNull_updatesButtonLabel() {
        GeoPointWidget widget = createWidget(promptWithAnswer(null));
        widget.setBinaryData(answer.getDisplayText());
        assertEquals(widget.binding.simpleButton.getText(), widget.getContext().getString(R.string.change_location));
    }

    @Test
    public void setData_callsValueChangeListener() {
        GeoPointWidget widget = createWidget(promptWithAnswer(null));
        WidgetValueChangedListener valueChangedListener = mockValueChangedListener(widget);
        widget.setBinaryData(answer.getDisplayText());

        verify(valueChangedListener).widgetValueChanged(widget);
    }

    @Test
    public void buttonClick_requestsGeoPoint() {
        FormEntryPrompt prompt = promptWithAnswer(answer);
        GeoPointWidget widget = createWidget(prompt);
        widget.binding.simpleButton.performClick();

        verify(geoDataRequester).requestGeoPoint(widget.getContext(), prompt, waitingForDataRegistry);
    }

    private GeoPointWidget createWidget(FormEntryPrompt prompt) {
        return new GeoPointWidget(widgetTestActivity(), new QuestionDetails(prompt, "formAnalyticsID"),
                waitingForDataRegistry, geoDataRequester);
    }
}
