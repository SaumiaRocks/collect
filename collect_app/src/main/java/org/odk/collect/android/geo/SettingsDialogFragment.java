package org.odk.collect.android.geo;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.RadioGroup;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import org.odk.collect.android.R;

public class SettingsDialogFragment extends DialogFragment {

    private static final int[] INTERVAL_OPTIONS = {
            1, 5, 10, 20, 30, 60, 300, 600, 1200, 1800
    };

    private static final int[] ACCURACY_THRESHOLD_OPTIONS = {
            0, 3, 5, 10, 15, 20
    };

    private View autoOptions;
    private RadioGroup radioGroup;
    private Spinner autoInterval;
    private Spinner accuracyThreshold;

    protected SettingsDialogCallback callback;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (context instanceof SettingsDialogCallback) {
            callback = (SettingsDialogCallback) context;
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        super.onCreateDialog(savedInstanceState);

        View settingsView = getActivity().getLayoutInflater().inflate(R.layout.geopoly_dialog, null);
        radioGroup = settingsView.findViewById(R.id.radio_group);
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                callback.updateRecordingMode(group, checkedId);
                updateSettingsDialog();
            }
        });

        autoOptions = settingsView.findViewById(R.id.auto_options);
        autoInterval = settingsView.findViewById(R.id.auto_interval);
        autoInterval.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                callback.setIntervalIndex(position);
            }

            @Override public void onNothingSelected(AdapterView<?> parent) { }
        });

        String[] options = new String[INTERVAL_OPTIONS.length];
        for (int i = 0; i < INTERVAL_OPTIONS.length; i++) {
            options[i] = formatInterval(INTERVAL_OPTIONS[i]);
        }
        populateSpinner(autoInterval, options);

        accuracyThreshold = settingsView.findViewById(R.id.accuracy_threshold);
        accuracyThreshold.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                callback.setAccuracyThresholdIndex(position);
            }

            @Override public void onNothingSelected(AdapterView<?> parent) { }
        });

        options = new String[ACCURACY_THRESHOLD_OPTIONS.length];
        for (int i = 0; i < ACCURACY_THRESHOLD_OPTIONS.length; i++) {
            options[i] = formatAccuracyThreshold(ACCURACY_THRESHOLD_OPTIONS[i]);
        }
        populateSpinner(accuracyThreshold, options);

        radioGroup.check(callback.getCheckedId());
        updateSettingsDialog();

        return new AlertDialog.Builder(getActivity())
                .setTitle(getString(R.string.input_method))
                .setView(settingsView)
                .setPositiveButton(getString(R.string.start), (dialog, id) -> {
                    callback.startInput();
                    dialog.cancel();
                    dismiss();
                })
                .setNegativeButton(R.string.cancel, (dialog, id) -> {
                    dialog.cancel();
                    dismiss();
                })
                .create();
    }

    private void updateSettingsDialog() {
        autoOptions.setVisibility(radioGroup.getCheckedRadioButtonId() == R.id.automatic_mode ? View.VISIBLE : View.GONE);
        autoInterval.setSelection(callback.getIntervalIndex());
        accuracyThreshold.setSelection(callback.getAccuracyThresholdIndex());
    }

    /** Formats a time interval as a whole number of seconds or minutes. */
    private String formatInterval(int seconds) {
        int minutes = seconds / 60;
        return minutes > 0 ?
                getResources().getQuantityString(R.plurals.number_of_minutes, minutes, minutes) :
                getResources().getQuantityString(R.plurals.number_of_seconds, seconds, seconds);
    }

    /** Populates a Spinner with the option labels in the given array. */
    private void populateSpinner(Spinner spinner, String[] options) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                getActivity(), android.R.layout.simple_spinner_item, options);
        adapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
    }

    /** Formats an entry in the accuracy threshold dropdown. */
    private String formatAccuracyThreshold(int meters) {
        return meters > 0 ?
                getResources().getQuantityString(R.plurals.number_of_meters, meters, meters) :
                getString(R.string.none);
    }

    public interface SettingsDialogCallback {

        void startInput();
        void updateRecordingMode(RadioGroup group, int checkedId);

        int getCheckedId();
        int getIntervalIndex();
        int getAccuracyThresholdIndex();

        void setIntervalIndex(int intervalIndex);
        void setAccuracyThresholdIndex(int accuracyThresholdIndex);
    }
}