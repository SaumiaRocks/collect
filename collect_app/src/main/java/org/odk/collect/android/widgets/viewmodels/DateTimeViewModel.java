package org.odk.collect.android.widgets.viewmodels;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import org.joda.time.LocalDateTime;
import org.odk.collect.android.widgets.utilities.DateTimeWidgetUtils;

public class DateTimeViewModel extends ViewModel {
    private final MutableLiveData<LocalDateTime> selectedDate = new MutableLiveData<>();
    private final MutableLiveData<LocalDateTime> selectedTime = new MutableLiveData<>();

    private final DatePickerDialog.OnDateSetListener dateSetListener = (view, year, monthOfYear, dayOfMonth) -> {
        view.clearFocus();
        setSelectedDate(year, monthOfYear, dayOfMonth);
    };

    private final TimePickerDialog.OnTimeSetListener timeSetListener = (view, hourOfDay, minuteOfHour) -> {
        view.clearFocus();
        setSelectedTime(hourOfDay, minuteOfHour);
    };

    public LiveData<LocalDateTime> getSelectedDate() {
        return selectedDate;
    }

    public LiveData<LocalDateTime> getSelectedTime() {
        return selectedTime;
    }

    public void setSelectedDate(int year, int month, int day) {
        this.selectedDate.postValue(DateTimeWidgetUtils.getSelectedDate(new LocalDateTime().withDate(year, month + 1, day), LocalDateTime.now()));
    }

    public void setSelectedTime(int hourOfDay, int minuteOfHour) {
        selectedTime.postValue(new LocalDateTime().withTime(hourOfDay, minuteOfHour, 0, 0));
    }

    public DatePickerDialog.OnDateSetListener getOnDateSetListener() {
        return dateSetListener;
    }

    public TimePickerDialog.OnTimeSetListener getOnTimeSetListener() {
        return timeSetListener;
    }
}