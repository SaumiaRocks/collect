package org.odk.collect.android.widgets;

import android.view.View;

import com.google.android.material.textfield.TextInputEditText;

import net.bytebuddy.utility.RandomString;

import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.data.StringData;
import org.javarosa.form.api.FormEntryPrompt;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.odk.collect.android.formentry.questions.QuestionDetails;
import org.odk.collect.android.listeners.WidgetValueChangedListener;
import org.robolectric.RobolectricTestRunner;

import static junit.framework.TestCase.assertEquals;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.verify;
import static org.odk.collect.android.widgets.support.QuestionWidgetHelpers.mockValueChangedListener;
import static org.odk.collect.android.widgets.support.QuestionWidgetHelpers.promptWithAnswer;
import static org.odk.collect.android.widgets.support.QuestionWidgetHelpers.promptWithReadOnly;
import static org.odk.collect.android.widgets.support.QuestionWidgetHelpers.widgetTestActivity;

/**
 * @author James Knight
 */
@RunWith(RobolectricTestRunner.class)
public class StringWidgetTest {

    @Test
    public void usingReadOnlyOptionShouldMakeAllClickableElementsDisabled() {
        StringWidget widget = createWidget(promptWithReadOnly());
        assertThat(widget.getEditText().getVisibility(), equalTo(View.VISIBLE));
        assertThat(widget.getEditText().isEnabled(), equalTo(Boolean.FALSE));
    }

    @Test
    public void getAnswerShouldReturnNullIfPromptDoesNotHaveExistingAnswer() {
        assertNull(createWidget(promptWithAnswer(null)).getAnswer());
    }

    @Test
    public void getAnswerShouldReturnNewAnswerWhenTextFieldIsUpdated() {
        // Make sure it starts null:
        StringWidget widget = createWidget(promptWithAnswer(null));
        IAnswerData answer = getNextAnswer();
        widget.answerText.setText(answer.getDisplayText());

        assertEquals(widget.getAnswerText(), answer.getDisplayText());
    }

    @Test
    public void getAnswerShouldReturnExistingAnswerIfPromptHasExistingAnswer() {
        StringWidget widget = createWidget(promptWithAnswer(getNextAnswer()));
        IAnswerData answer = widget.getAnswer();

        assertEquals(widget.getAnswerText(), answer.getDisplayText());
    }

    @Test
    public void updatingTextFieldShouldUpdateAnswer() {
        StringWidget widget = createWidget(promptWithAnswer(null));
        TextInputEditText editText = widget.getEditText();

        editText.setText(getNextAnswer().getDisplayText());
        assertEquals(widget.getAnswerText(), editText.getText().toString());
    }

    @Test
    public void callingClearShouldRemoveTheExistingAnswer() {
        StringWidget widget = createWidget(promptWithAnswer(getNextAnswer()));
        widget.clearAnswer();

        assertEquals(widget.getAnswerText(), "");
    }

    @Test
    public void clearAnswerShouldCallValueChangeListeners() {
        StringWidget widget = createWidget(promptWithAnswer(null));
        WidgetValueChangedListener valueChangedListener = mockValueChangedListener(widget);

        widget.clearAnswer();
        verify(valueChangedListener).widgetValueChanged(widget);
    }

    @Test
    public void updatingTextFieldShouldCallValueChangeListeners() {
        StringWidget widget = createWidget(promptWithAnswer(null));
        WidgetValueChangedListener valueChangedListener = mockValueChangedListener(widget);
        TextInputEditText editText = widget.getEditText();

        editText.setText(getNextAnswer().getDisplayText());
        verify(valueChangedListener).widgetValueChanged(widget);
    }

    private StringWidget createWidget(FormEntryPrompt prompt) {
        return new StringWidget(widgetTestActivity(), new QuestionDetails(prompt, "formAnalyticsID"), prompt.isReadOnly());
    }

    public StringData getNextAnswer() {
        return new StringData(RandomString.make());
    }
}
