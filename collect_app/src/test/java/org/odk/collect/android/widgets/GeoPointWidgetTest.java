package org.odk.collect.android.widgets;

import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import org.javarosa.core.model.QuestionDef;
import org.javarosa.core.model.data.StringData;
import org.javarosa.form.api.FormEntryPrompt;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.odk.collect.android.R;
import org.odk.collect.android.activities.GeoPointActivity;
import org.odk.collect.android.activities.GeoPointMapActivity;
import org.odk.collect.android.fakes.FakePermissionUtils;
import org.odk.collect.android.formentry.questions.QuestionDetails;
import org.odk.collect.android.geo.MapConfigurator;
import org.odk.collect.android.listeners.WidgetValueChangedListener;
import org.odk.collect.android.widgets.utilities.GeoWidgetUtils;
import org.robolectric.RobolectricTestRunner;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.odk.collect.android.utilities.WidgetAppearanceUtils.MAPS;
import static org.odk.collect.android.utilities.WidgetAppearanceUtils.PLACEMENT_MAP;
import static org.odk.collect.android.widgets.GeoPointWidget.ACCURACY_THRESHOLD;
import static org.odk.collect.android.widgets.GeoPointWidget.DEFAULT_LOCATION_ACCURACY;
import static org.odk.collect.android.widgets.GeoPointWidget.DRAGGABLE_ONLY;
import static org.odk.collect.android.widgets.GeoPointWidget.LOCATION;
import static org.odk.collect.android.widgets.GeoPointWidget.READ_ONLY;
import static org.odk.collect.android.widgets.support.QuestionWidgetHelpers.mockValueChangedListener;
import static org.odk.collect.android.widgets.support.QuestionWidgetHelpers.promptWithAnswer;
import static org.odk.collect.android.widgets.support.QuestionWidgetHelpers.promptWithAppearance;
import static org.odk.collect.android.widgets.support.QuestionWidgetHelpers.promptWithAppearanceAndAnswer;
import static org.odk.collect.android.widgets.support.QuestionWidgetHelpers.promptWithReadOnly;
import static org.odk.collect.android.widgets.support.QuestionWidgetHelpers.widgetTestActivity;
import static org.robolectric.Shadows.shadowOf;

/**
 * @author James Knight
 */

@RunWith(RobolectricTestRunner.class)
public class GeoPointWidgetTest {

    private final FakePermissionUtils permissionUtils = new FakePermissionUtils();
    private final List<double[]> answerDoubles = getRandomDoubleArrayList();
    private final String answer = stringFromDoubleList(answerDoubles);

    private QuestionDef questionDef;
    private MapConfigurator mapConfigurator;

    @Before
    public void setup() {
        questionDef = mock(QuestionDef.class);
        mapConfigurator = mock(MapConfigurator.class);
        when(questionDef.getAdditionalAttribute(anyString(), anyString())).thenReturn(null);
    }

    @Test
    public void getAnswer_whenPromptAnswerDoesNotHaveAnswer_returnsNull() {
        GeoPointWidget widget = createWidget(promptWithAnswer(null));
        assertThat(widget.getAnswer(), equalTo(null));
        assertThat(widget.getAnswerToDisplay(null), equalTo(""));
    }

    @Test
    public void getAnswer_whenPromptAnswerDoesNotHaveConvertibleString_returnsNull() throws NumberFormatException {
        GeoPointWidget widget = createWidget(promptWithAnswer(new StringData("blah")));
        assertThat(widget.getAnswer().getDisplayText(), equalTo(""));
    }

    @Test
    public void getAnswer_whenPromptHasAnswer_returnsAnswer() {
        GeoPointWidget widget = createWidget(promptWithAnswer(new StringData(answer)));
        assertThat(widget.getAnswer().getDisplayText(), equalTo(answer));
    }

    @Test
    public void clearAnswer_clearsWidgetAnswer() {
        GeoPointWidget widget = createWidget(promptWithAnswer(new StringData(answer)));
        widget.clearAnswer();
        assertThat(widget.getAnswer(), nullValue());
    }

    @Test
    public void clearAnswer_callsValueChangeListeners() {
        GeoPointWidget widget = createWidget(promptWithAnswer(null));
        WidgetValueChangedListener valueChangedListener = mockValueChangedListener(widget);
        widget.clearAnswer();
        verify(valueChangedListener).widgetValueChanged(widget);
    }

    @Test
    public void usingReadOnlyOption_makesAllClickableElementsDisabled() {
        GeoPointWidget widget = createWidget(promptWithReadOnly());
        assertThat(widget.startGeoButton.getVisibility(), equalTo(View.GONE));
    }

    @Test
    public void whenPromptDoesNotHaveAnswer_textViewDisplaysEmptyString() {
        GeoPointWidget widget = createWidget(promptWithAnswer(null));
        assertThat(widget.answerDisplay.getText().toString(), equalTo(""));
    }

    @Test
    public void whenPromptHasAnswer_textViewDisplaysAnswer() {
        GeoPointWidget widget = createWidget(promptWithAnswer(new StringData(answer)));
        String[] parts = answer.split(" ");
        assertThat(widget.answerDisplay.getText().toString(), equalTo(widget.getContext().getString(
                R.string.gps_result,
                GeoWidgetUtils.convertCoordinatesIntoDegreeFormat(widget.getContext(), Double.parseDouble(parts[0]), "lat"),
                GeoWidgetUtils.convertCoordinatesIntoDegreeFormat(widget.getContext(), Double.parseDouble(parts[1]), "lon"),
                GeoWidgetUtils.truncateDouble(parts[2]),
                GeoWidgetUtils.truncateDouble(parts[3])
        )));
    }

    @Test
    public void whenWidgetHasAppearanceAndIsReadOnly_buttonShowsCorrectText() {
        when(mapConfigurator.isAvailable(any())).thenReturn(true);
        GeoPointWidget widget = createWidget(promptWithAppearance(PLACEMENT_MAP, true));
        assertThat(widget.startGeoButton.getText().toString(), equalTo(widget.getContext().getString(R.string.geopoint_view_read_only)));
    }

    @Test
    public void whenWidgetHasAppearanceAndNullAsAnswer_buttonShowsCorrectText() {
        when(mapConfigurator.isAvailable(any())).thenReturn(true);
        GeoPointWidget widget = createWidget(promptWithAppearanceAndAnswer(PLACEMENT_MAP, null));
        assertThat(widget.startGeoButton.getText(), equalTo(widget.getContext().getString(R.string.get_point)));
    }

    @Test
    public void whenWidgetHasAppearanceAndAnswer_buttonShowsCorrectText() {
        when(mapConfigurator.isAvailable(any())).thenReturn(true);
        GeoPointWidget widget = createWidget(promptWithAppearanceAndAnswer(MAPS, new StringData(answer)));
        assertThat(widget.startGeoButton.getText(), equalTo(widget.getContext().getString(R.string.view_change_location)));
    }

    @Test
    public void whenWidgetHasAnswer_buttonShowsCorrectText() {
        GeoPointWidget widget = createWidget(promptWithAnswer(new StringData(answer)));
        assertThat(widget.startGeoButton.getText(), equalTo(widget.getContext().getString(R.string.change_location)));
    }

    @Test
    public void whenWidgetHasNullAsAnswer_buttonShowsCorrectText() {
        GeoPointWidget widget = createWidget(promptWithAnswer(null));
        assertThat(widget.startGeoButton.getText(), equalTo(widget.getContext().getString(R.string.get_point)));
    }

    @Test
    public void whenPromptDoesNotHaveAnswer_buttonShouldLaunchCorrectIntent() {
        GeoPointWidget widget = createWidget(promptWithAnswer(null));
        stubAllRuntimePermissionsGranted(widget, true);
        widget.startGeoButton.performClick();

        Intent startedIntent = shadowOf(widgetTestActivity()).getNextStartedActivity();
        Bundle bundle = startedIntent.getExtras();

        assertThat(startedIntent.getComponent(), equalTo(new ComponentName(widgetTestActivity(), GeoPointActivity.class)));
        assertBundleArgumentEquals(bundle, null, DEFAULT_LOCATION_ACCURACY, false, true);
    }

    @Test
    public void whenPromptHasAnswer_buttonShouldLaunchCorrectIntent() {
        GeoPointWidget widget = createWidget(promptWithAnswer(new StringData(answer)));
        stubAllRuntimePermissionsGranted(widget, true);
        widget.startGeoButton.performClick();

        Intent startedIntent = shadowOf(widgetTestActivity()).getNextStartedActivity();
        Bundle bundle = startedIntent.getExtras();

        assertThat(startedIntent.getComponent(), equalTo(new ComponentName(widgetTestActivity(), GeoPointActivity.class)));
        assertBundleArgumentEquals(bundle, GeoWidgetUtils.getLocationParamsFromStringAnswer(answer), DEFAULT_LOCATION_ACCURACY, false, true);
    }

    @Test
    public void whenPromptHasPlacementMapAppearance_buttonShouldLaunchCorrectIntent() {
        when(mapConfigurator.isAvailable(any())).thenReturn(true);
        GeoPointWidget widget = createWidget(promptWithAppearance(PLACEMENT_MAP, false));
        stubAllRuntimePermissionsGranted(widget, true);
        widget.startGeoButton.performClick();

        Intent startedIntent = shadowOf(widgetTestActivity()).getNextStartedActivity();
        Bundle bundle = startedIntent.getExtras();

        assertThat(startedIntent.getComponent(), equalTo(new ComponentName(widgetTestActivity(), GeoPointMapActivity.class)));
        assertBundleArgumentEquals(bundle, null, DEFAULT_LOCATION_ACCURACY, false, true);
    }

    @Test
    public void whenPromptHasMapsAppearance_buttonShouldLaunchCorrectIntent() {
        when(mapConfigurator.isAvailable(any())).thenReturn(true);
        GeoPointWidget widget = createWidget(promptWithAppearance(MAPS, false));
        stubAllRuntimePermissionsGranted(widget, true);
        widget.startGeoButton.performClick();

        Intent startedIntent = shadowOf(widgetTestActivity()).getNextStartedActivity();
        Bundle bundle = startedIntent.getExtras();

        assertThat(startedIntent.getComponent(), equalTo(new ComponentName(widgetTestActivity(), GeoPointMapActivity.class)));
        assertBundleArgumentEquals(bundle, null, DEFAULT_LOCATION_ACCURACY, false, false);
    }

    private GeoPointWidget createWidget(FormEntryPrompt prompt) {
        return new GeoPointWidget(widgetTestActivity(), new QuestionDetails(prompt, "formAnalyticsID"), questionDef, mapConfigurator);
    }

    private void assertBundleArgumentEquals(Bundle bundle, double[] location, double accuracyThreshold, boolean readOnly, boolean draggableOnly) {
        assertThat(bundle.getDoubleArray(LOCATION), equalTo(location));
        assertThat(bundle.getBoolean(READ_ONLY), equalTo(readOnly));
        assertThat(bundle.getBoolean(DRAGGABLE_ONLY), equalTo(draggableOnly));
        assertThat(bundle.getDouble(ACCURACY_THRESHOLD), equalTo(accuracyThreshold));
    }

    protected void stubAllRuntimePermissionsGranted(GeoPointWidget widget, boolean isGranted) {
        permissionUtils.setPermissionGranted(isGranted);
        widget.setPermissionUtils(permissionUtils);
    }

    private String stringFromDoubleList(List<double[]> doubleList) {
        StringBuilder b = new StringBuilder();
        boolean first = true;
        for (double[] doubles : doubleList) {
            if (!first) {
                b.append("; ");
            }
            first = false;
            b.append(stringFromDoubles(doubles));
        }
        return b.toString();
    }

    private String stringFromDoubles(double[] doubles) {
        StringBuilder b = new StringBuilder();
        for (int i = 0; i < doubles.length; i++) {
            b.append(doubles[i]);
            if (i != doubles.length - 1) {
                b.append(' ');
            }
        }

        return b.toString();
    }

    private ArrayList<double[]> getRandomDoubleArrayList() {
        Random random = new Random();
        ArrayList<double[]> doubleList = new ArrayList<>();

        int pointCount = Math.max(1, random.nextInt() % 5);
        for (int i = 0; i < pointCount; ++i) {
            doubleList.add(getRandomDoubleArray());
        }

        return doubleList;
    }

    private double[] getRandomDoubleArray() {
        Random random = new Random();
        return new double[]{
                random.nextDouble(),
                random.nextDouble(),
                random.nextDouble(),
                random.nextDouble()
        };
    }
}