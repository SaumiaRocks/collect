package org.odk.collect.android.widgets.support;

import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.form.api.FormEntryPrompt;
import org.odk.collect.android.fakes.FakePermissionUtils;
import org.odk.collect.android.listeners.WidgetValueChangedListener;
import org.odk.collect.android.support.MockFormEntryPromptBuilder;
import org.odk.collect.android.support.RobolectricHelpers;
import org.odk.collect.android.support.TestScreenContextActivity;
import org.odk.collect.android.widgets.QuestionWidget;

import static org.mockito.Mockito.mock;

public class QuestionWidgetHelpers {

    private QuestionWidgetHelpers() {

    }

    public static TestScreenContextActivity widgetTestActivity() {
        return RobolectricHelpers.buildThemedActivity(TestScreenContextActivity.class).get();
    }

    public static WidgetValueChangedListener mockValueChangedListener(QuestionWidget widget) {
        WidgetValueChangedListener valueChangedListener = mock(WidgetValueChangedListener.class);
        widget.setValueChangedListener(valueChangedListener);
        return valueChangedListener;
    }

    public static void stubLocationPermissions(FakePermissionUtils permissionUtils, QuestionWidget widget, boolean isGranted) {
        permissionUtils.setPermissionGranted(isGranted);
        widget.setPermissionUtils(permissionUtils);
    }

    public static FormEntryPrompt promptWithAnswer(IAnswerData answer) {
        return new MockFormEntryPromptBuilder()
                .withAnswer(answer)
                .build();
    }

    public static FormEntryPrompt promptWithReadOnly() {
        return new MockFormEntryPromptBuilder()
                .withReadOnly(true)
                .build();
    }

    public static FormEntryPrompt promptWithReadOnlyAndAnswer(IAnswerData answer) {
        return new MockFormEntryPromptBuilder()
                .withReadOnly(true)
                .withAnswer(answer)
                .build();
    }

    public static FormEntryPrompt promptWithAppearanceAndReadOnly(String appearance, Boolean readOnly) {
        return new MockFormEntryPromptBuilder()
                .withReadOnly(readOnly)
                .withAppearance(appearance)
                .build();
    }

    public static FormEntryPrompt promptWithAppearanceAndAnswer(String appearance, IAnswerData answer) {
        return new MockFormEntryPromptBuilder()
                .withAnswer(answer)
                .withAppearance(appearance)
                .build();
    }
}
