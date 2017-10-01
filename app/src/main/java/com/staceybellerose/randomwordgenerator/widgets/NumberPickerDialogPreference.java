package com.staceybellerose.randomwordgenerator.widgets;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Parcel;
import android.os.Parcelable;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.NumberPicker;
import android.widget.TextView;

import com.staceybellerose.randomwordgenerator.R;

/**
 * A DialogPreference that provides a user with the means to select an integer
 * from a NumberPicker, and persist it.
 *
 * https://gist.github.com/Rolinh/9960021
 *
 * Copyright 2013 Luke Horvat
 * Copyright 2014 Robin Hahling
 * Copyright 2017 Stacey Adams
 *
 * MIT License
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to
 * deal in the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or
 * sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL
 * THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 *
 */
public class NumberPickerDialogPreference extends DialogPreference
{
    /**
     * The default minimum selectable value for the number picker
     */
    private static final int DEFAULT_MIN_VALUE = 0;
    /**
     * The default maximum selectable value for the number picker
     */
    private static final int DEFAULT_MAX_VALUE = 100;
    /**
     * The default value of the number picker
     */
    private static final int DEFAULT_VALUE = 0;

    /**
     * The minimum selectable value for the number picker
     */
    private int mMinValue;
    /**
     * The maximum selectable value for the number picker
     */
    private int mMaxValue;
    /**
     * The selected value from the number picker
     */
    private int mValue;
    /**
     * The resource ID of the plurals resource for summary line
     */
    private int mSummaryPlural;
    /**
     * The number picker widget used to drive this preference
     */
    private NumberPicker mNumberPicker;

    /**
     * Constructor
     *
     * @param context The activity context
     */
    public NumberPickerDialogPreference(final Context context)
    {
        super(context, null);
        init(context, null);
    }

    /**
     * Constructor
     *
     * @param context The activity context
     * @param attrs The style attributes
     */
    public NumberPickerDialogPreference(final Context context, final AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    /**
     * Initialize the preference object
     *
     * @param context The activity context
     * @param attrs The style attributes
     */
    private void init(final Context context, final AttributeSet attrs) {
        // get attributes specified in XML
        TypedArray typedArray = context.getTheme().obtainStyledAttributes(attrs,
                R.styleable.NumberPickerDialogPreference, 0, 0);
        try
        {
            setMinValue(typedArray.getInteger(R.styleable.NumberPickerDialogPreference_min, DEFAULT_MIN_VALUE));
            setMaxValue(typedArray.getInteger(R.styleable.NumberPickerDialogPreference_android_max, DEFAULT_MAX_VALUE));
            mSummaryPlural = typedArray.getResourceId(R.styleable.NumberPickerDialogPreference_summaryPlural, 0);
        }
        finally
        {
            typedArray.recycle();
        }

        // set layout
        setDialogLayoutResource(R.layout.preference_number_picker_dialog);
        setPositiveButtonText(android.R.string.ok);
        setNegativeButtonText(android.R.string.cancel);
        setDialogIcon(null);
    }

    @Override
    protected void onSetInitialValue(final boolean restore, final Object defaultValue)
    {
        setValue(restore ? getPersistedInt(DEFAULT_VALUE) : (Integer) defaultValue);
    }

    @Override
    protected Object onGetDefaultValue(final TypedArray typedArray, final int index)
    {
        return typedArray.getInt(index, DEFAULT_VALUE);
    }

    @Override
    protected void onBindDialogView(final View view)
    {
        super.onBindDialogView(view);

        TextView dialogMessageText = (TextView) view.findViewById(R.id.text_dialog_message);
        dialogMessageText.setText(getDialogMessage());

        mNumberPicker = (NumberPicker) view.findViewById(R.id.number_picker);
        mNumberPicker.setMinValue(mMinValue);
        mNumberPicker.setMaxValue(mMaxValue);
        mNumberPicker.setValue(mValue);

        // prevent keyboard from showing up
        mNumberPicker.setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS);
    }

    /**
     * Get the minimum selectable value of the number picker
     *
     * @return The minimum selectable value
     */
    private int getMinValue()
    {
        return mMinValue;
    }

    /**
     * Set the minimum selectable value of the number picker
     *
     * @param minValue The minimum selectable value
     */
    private void setMinValue(final int minValue)
    {
        mMinValue = minValue;
        setValue(Math.max(mValue, mMinValue));
    }

    /**
     * Get the maximum selectable value of the number picker
     *
     * @return The maximum selectable value
     */
    private int getMaxValue()
    {
        return mMaxValue;
    }

    /**
     * Set the maximum selectable value of the number picker
     *
     * @param maxValue The maximum selectable value
     */
    private void setMaxValue(final int maxValue)
    {
        mMaxValue = maxValue;
        setValue(Math.min(mValue, mMaxValue));
    }

    /**
     * Get the selected value of the number picker
     *
     * @return The selected value
     */
    private int getValue()
    {
        return mValue;
    }

    /**
     * Set the selected value of the number picker
     *
     * @param value The selected value
     */
    private void setValue(final int value)
    {
        int checkedValue = Math.max(Math.min(value, mMaxValue), mMinValue);

        if (checkedValue != mValue)
        {
            mValue = checkedValue;
            persistInt(checkedValue);
            setSummary();
            notifyChanged();
        }
    }

    /**
     * Set the Preference summary text based on the selected value, using
     */
    private void setSummary() {
        if (mSummaryPlural != 0) {
            setSummary(getContext().getResources().getQuantityString(mSummaryPlural, mValue, mValue));
        } else {
            setSummary(Integer.toString(mValue));
        }
    }

    @Override
    protected void onDialogClosed(final boolean positiveResult)
    {
        super.onDialogClosed(positiveResult);

        // when the user selects "OK", persist the new mValue
        if (positiveResult)
        {
            int numberPickerValue = mNumberPicker.getValue();
            if (callChangeListener(numberPickerValue))
            {
                setValue(numberPickerValue);
            }
        }
    }

    @Override
    protected Parcelable onSaveInstanceState()
    {
        // save the instance state so that it will survive screen orientation changes
        // and other events that may temporarily destroy it
        final Parcelable superState = super.onSaveInstanceState();

        // set the state's mValue with the class member that holds current setting mValue
        final SavedState myState = new SavedState(superState);
        myState.setMin(getMinValue());
        myState.setMax(getMaxValue());
        myState.setValue(getValue());

        return myState;
    }

    @Override
    protected void onRestoreInstanceState(final Parcelable state)
    {
        // check whether we saved the state in onSaveInstanceState()
        if (state == null || !state.getClass().equals(SavedState.class))
        {
            // didn't save the state, so call superclass
            super.onRestoreInstanceState(state);
            return;
        }

        // restore the state
        SavedState myState = (SavedState) state;
        setMinValue(myState.getMin());
        setMaxValue(myState.getMax());
        setValue(myState.getValue());

        super.onRestoreInstanceState(myState.getSuperState());
    }

    /**
     * Manage the saved state for this preference
     */
    private static class SavedState extends BaseSavedState
    {
        /**
         * Interface that must be implemented and provided as a public CREATOR
         * field that generates instances of your Parcelable class from a Parcel.
         */
        @SuppressWarnings("unused")
        public static final Parcelable.Creator<SavedState> CREATOR = new Parcelable.Creator<SavedState>()
        {
            @Override
            public SavedState createFromParcel(final Parcel source)
            {
                return new SavedState(source);
            }

            @Override
            public SavedState[] newArray(final int size)
            {
                return new SavedState[size];
            }
        };

        /**
         * The minimum selectable value
         */
        private int mMin;
        /**
         * The maximum selectable value
         */
        private int mMax;
        /**
         * The currently selected value
         */
        private int mValue;

        /**
         * Constructor called by derived classes when creating their SavedState objects
         *
         * @param superState The state of the superclass of this view
         */
        SavedState(final Parcelable superState)
        {
            super(superState);
        }

        /**
         * Constructor used when reading from a parcel. Reads the state of the superclass.
         *
         * @param source parcel to read from
         */
        SavedState(final Parcel source)
        {
            super(source);

            mMin = source.readInt();
            mMax = source.readInt();
            mValue = source.readInt();
        }

        /**
         * Get the minimum selectable value of the number picker
         *
         * @return The minimum selectable value
         */
        int getMin() {
            return mMin;
        }

        /**
         * Set the minimum selectable value of the number picker
         *
         * @param minValue The minimum selectable value
         */
        void setMin(final int minValue) {
            mMin = minValue;
        }

        /**
         * Get the maximum selectable value of the number picker
         *
         * @return The maximum selectable value
         */
        int getMax() {
            return mMax;
        }

        /**
         * Set the maximum selectable value of the number picker
         *
         * @param maxValue The maximum selectable value
         */
        void setMax(final int maxValue) {
            mMax = maxValue;
        }

        /**
         * Get the selected value of the number picker
         *
         * @return The selected value
         */
        int getValue() {
            return mValue;
        }

        /**
         * Set the selected value of the number picker
         *
         * @param value The selected value
         */
        void setValue(final int value) {
            mValue = value;
        }

        @Override
        public void writeToParcel(final Parcel dest, final int flags)
        {
            super.writeToParcel(dest, flags);

            dest.writeInt(mMin);
            dest.writeInt(mMax);
            dest.writeInt(mValue);
        }
    }
}