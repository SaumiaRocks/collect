package org.odk.collect.android.widgets.utilities;

import android.content.ComponentName;
import android.content.Intent;

import org.javarosa.core.model.FormIndex;
import org.javarosa.core.model.QuestionDef;
import org.javarosa.core.model.data.GeoPointData;
import org.javarosa.core.model.data.StringData;
import org.javarosa.form.api.FormEntryPrompt;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.odk.collect.android.activities.GeoPointActivity;
import org.odk.collect.android.activities.GeoPointMapActivity;
import org.odk.collect.android.activities.GeoPolyActivity;
import org.odk.collect.android.fakes.FakePermissionUtils;
import org.odk.collect.android.support.TestScreenContextActivity;
import org.odk.collect.android.utilities.WidgetAppearanceUtils;
import org.odk.collect.android.widgets.support.FakeWaitingForDataRegistry;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.shadows.ShadowActivity;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.odk.collect.android.utilities.ApplicationConstants.RequestCodes.GEOSHAPE_CAPTURE;
import static org.odk.collect.android.utilities.ApplicationConstants.RequestCodes.GEOTRACE_CAPTURE;
import static org.odk.collect.android.utilities.ApplicationConstants.RequestCodes.LOCATION_CAPTURE;
import static org.odk.collect.android.widgets.support.GeoWidgetHelpers.assertGeoPointBundleArgumentEquals;
import static org.odk.collect.android.widgets.support.GeoWidgetHelpers.assertGeoPolyBundleArgumentEquals;
import static org.odk.collect.android.widgets.support.GeoWidgetHelpers.getRandomDoubleArray;
import static org.odk.collect.android.widgets.support.QuestionWidgetHelpers.promptWithAnswer;
import static org.odk.collect.android.widgets.support.QuestionWidgetHelpers.promptWithReadOnly;
import static org.odk.collect.android.widgets.support.QuestionWidgetHelpers.widgetTestActivity;
import static org.odk.collect.android.widgets.utilities.ActivityGeoDataRequester.ACCURACY_THRESHOLD;
import static org.robolectric.Shadows.shadowOf;

@RunWith(RobolectricTestRunner.class)
public class ActivityGeoDataRequesterTest {
    private final FakePermissionUtils permissionUtils = new FakePermissionUtils();
    private final ActivityGeoDataRequester activityGeoDataRequester = new ActivityGeoDataRequester(permissionUtils);
    private final FakeWaitingForDataRegistry waitingForDataRegistry = new FakeWaitingForDataRegistry();
    private final GeoPointData answer = new GeoPointData(getRandomDoubleArray());

    private TestScreenContextActivity testActivity;
    private ShadowActivity shadowActivity;
    private FormEntryPrompt prompt;
    private FormIndex formIndex;
    private QuestionDef questionDef;

    @Before
    public void setUp() {
        testActivity = widgetTestActivity();
        shadowActivity = shadowOf(testActivity);

        prompt = promptWithReadOnly();
        formIndex = mock(FormIndex.class);
        questionDef = mock(QuestionDef.class);

        permissionUtils.setPermissionGranted(true);
        when(prompt.getQuestion()).thenReturn(questionDef);
        when(prompt.getIndex()).thenReturn(formIndex);
    }

    @Test
    public void whenPermissionIsNotGranted_requestGeoPoint_doesNotLaunchAnyIntent() {
        permissionUtils.setPermissionGranted(false);
        activityGeoDataRequester.requestGeoPoint(testActivity, prompt, waitingForDataRegistry);

        assertNull(shadowActivity.getNextStartedActivity());
        assertTrue(waitingForDataRegistry.waiting.isEmpty());
    }

    @Test
    public void whenPermissionIsNotGranted_requestGeoShape_doesNotLaunchAnyIntent() {
        permissionUtils.setPermissionGranted(false);
        activityGeoDataRequester.requestGeoShape(testActivity, prompt, waitingForDataRegistry);

        assertNull(shadowActivity.getNextStartedActivity());
        assertTrue(waitingForDataRegistry.waiting.isEmpty());
    }

    @Test
    public void whenPermissionIsNotGranted_requestGeoTrace_doesNotLaunchAnyIntent() {
        permissionUtils.setPermissionGranted(false);
        activityGeoDataRequester.requestGeoTrace(testActivity, prompt, waitingForDataRegistry);

        assertNull(shadowActivity.getNextStartedActivity());
        assertTrue(waitingForDataRegistry.waiting.isEmpty());
    }

    @Test
    public void whenPermissionIGranted_requestGeoPoint_setsFormIndexWaitingForData() {
        activityGeoDataRequester.requestGeoPoint(testActivity, prompt, waitingForDataRegistry);
        assertTrue(waitingForDataRegistry.waiting.contains(formIndex));
    }

    @Test
    public void whenPermissionIGranted_requestGeoShape_setsFormIndexWaitingForData() {
        activityGeoDataRequester.requestGeoShape(testActivity, prompt, waitingForDataRegistry);
        assertTrue(waitingForDataRegistry.waiting.contains(formIndex));
    }

    @Test
    public void whenPermissionIGranted_requestGeoTrace_setsFormIndexWaitingForData() {
        activityGeoDataRequester.requestGeoTrace(testActivity, prompt, waitingForDataRegistry);
        assertTrue(waitingForDataRegistry.waiting.contains(formIndex));
    }

    @Test
    public void whenPromptIsReadOnly_requestGeoPoint_launchesCorrectIntent() {
        activityGeoDataRequester.requestGeoPoint(testActivity, prompt, waitingForDataRegistry);
        Intent startedIntent = shadowActivity.getNextStartedActivity();

        assertEquals(startedIntent.getComponent(), new ComponentName(testActivity, GeoPointActivity.class));
        assertGeoPointBundleArgumentEquals(startedIntent.getExtras(), null, GeoWidgetUtils.DEFAULT_LOCATION_ACCURACY,
                true, false);

        assertEquals(shadowActivity.getNextStartedActivityForResult().requestCode, LOCATION_CAPTURE);
    }

    @Test
    public void whenPromptHasAnswerAndAccuracyValue_requestGeoPoint_launchesCorrectIntent() {
        FormEntryPrompt prompt = promptWithAnswer(answer);
        when(prompt.getQuestion()).thenReturn(questionDef);
        when(questionDef.getAdditionalAttribute(null, ACCURACY_THRESHOLD)).thenReturn("10");

        activityGeoDataRequester.requestGeoPoint(testActivity, prompt, waitingForDataRegistry);
        Intent startedIntent = shadowActivity.getNextStartedActivity();

        assertEquals(startedIntent.getComponent(), new ComponentName(testActivity, GeoPointActivity.class));
        assertGeoPointBundleArgumentEquals(startedIntent.getExtras(), GeoWidgetUtils.getLocationParamsFromStringAnswer(
                answer.getDisplayText()), 10.0, false, false);

        assertEquals(shadowActivity.getNextStartedActivityForResult().requestCode, LOCATION_CAPTURE);
    }

    @Test
    public void whenPromptHasMapsAppearance_requestGeoPoint_launchesCorrectIntent() {
        FormEntryPrompt prompt = promptWithAnswer(null);
        when(prompt.getQuestion()).thenReturn(questionDef);
        when(prompt.getAppearanceHint()).thenReturn(WidgetAppearanceUtils.MAPS);

        activityGeoDataRequester.requestGeoPoint(testActivity, prompt, waitingForDataRegistry);
        Intent startedIntent = shadowActivity.getNextStartedActivity();

        assertEquals(startedIntent.getComponent(), new ComponentName(testActivity, GeoPointMapActivity.class));
        assertGeoPointBundleArgumentEquals(startedIntent.getExtras(), null, GeoWidgetUtils.DEFAULT_LOCATION_ACCURACY, false, false);

        assertEquals(shadowActivity.getNextStartedActivityForResult().requestCode, LOCATION_CAPTURE);
    }

    @Test
    public void whenPromptHasPlacementMapAppearance_requestGeoPoint_launchesCorrectIntent() {
        FormEntryPrompt prompt = promptWithAnswer(null);
        when(prompt.getQuestion()).thenReturn(questionDef);
        when(prompt.getAppearanceHint()).thenReturn(WidgetAppearanceUtils.PLACEMENT_MAP);

        activityGeoDataRequester.requestGeoPoint(testActivity, prompt, waitingForDataRegistry);
        Intent startedIntent = shadowActivity.getNextStartedActivity();

        assertEquals(startedIntent.getComponent(), new ComponentName(testActivity, GeoPointMapActivity.class));
        assertGeoPointBundleArgumentEquals(startedIntent.getExtras(), null, GeoWidgetUtils.DEFAULT_LOCATION_ACCURACY, false, true);

        assertEquals(shadowActivity.getNextStartedActivityForResult().requestCode, LOCATION_CAPTURE);
    }

    @Test
    public void whenPromptIsReadOnly_requestGeoShape_launchesCorrectIntent() {
        activityGeoDataRequester.requestGeoShape(testActivity, prompt, waitingForDataRegistry);
        Intent startedIntent = shadowActivity.getNextStartedActivity();

        assertEquals(startedIntent.getComponent(), new ComponentName(testActivity, GeoPolyActivity.class));
        assertGeoPolyBundleArgumentEquals(startedIntent.getExtras(), null, GeoPolyActivity.OutputMode.GEOSHAPE, true);

        assertEquals(shadowActivity.getNextStartedActivityForResult().requestCode, GEOSHAPE_CAPTURE);
    }

    @Test
    public void whenPromptHasAnswerAndAccuracyValue_requestGeoShape_launchesCorrectIntent() {
        FormEntryPrompt prompt = promptWithAnswer(new StringData("blah"));
        when(questionDef.getAdditionalAttribute(null, ACCURACY_THRESHOLD)).thenReturn("10");

        activityGeoDataRequester.requestGeoShape(testActivity, prompt, waitingForDataRegistry);
        Intent startedIntent = shadowActivity.getNextStartedActivity();

        assertEquals(startedIntent.getComponent(), new ComponentName(testActivity, GeoPolyActivity.class));
        assertGeoPolyBundleArgumentEquals(startedIntent.getExtras(), "blah", GeoPolyActivity.OutputMode.GEOSHAPE, false);

        assertEquals(shadowActivity.getNextStartedActivityForResult().requestCode, GEOSHAPE_CAPTURE);
    }

    @Test
    public void whenPromptIsReadOnly_requestGeoTrace_launchesCorrectIntent() {
        activityGeoDataRequester.requestGeoTrace(testActivity, prompt, waitingForDataRegistry);
        Intent startedIntent = shadowActivity.getNextStartedActivity();

        assertEquals(startedIntent.getComponent(), new ComponentName(testActivity, GeoPolyActivity.class));
        assertGeoPolyBundleArgumentEquals(startedIntent.getExtras(), null, GeoPolyActivity.OutputMode.GEOTRACE, true);

        assertEquals(shadowActivity.getNextStartedActivityForResult().requestCode, GEOTRACE_CAPTURE);
    }

    @Test
    public void whenPromptHasAnswerAndAccuracyValue_requestGeoTrace_launchesCorrectIntent() {
        FormEntryPrompt prompt = promptWithAnswer(new StringData("blah"));
        when(questionDef.getAdditionalAttribute(null, ACCURACY_THRESHOLD)).thenReturn("10");

        activityGeoDataRequester.requestGeoTrace(testActivity, prompt, waitingForDataRegistry);
        Intent startedIntent = shadowActivity.getNextStartedActivity();

        assertEquals(startedIntent.getComponent(), new ComponentName(testActivity, GeoPolyActivity.class));
        assertGeoPolyBundleArgumentEquals(startedIntent.getExtras(), "blah", GeoPolyActivity.OutputMode.GEOTRACE, false);

        assertEquals(shadowActivity.getNextStartedActivityForResult().requestCode, GEOTRACE_CAPTURE);
    }
}
