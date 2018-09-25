package com.staceybellerose.randomwordgenerator.widgets;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.preference.ListPreference;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;

import com.staceybellerose.randomwordgenerator.R;
import com.staceybellerose.randomwordgenerator.WordListDetailsActivity;

/**
 * Custom ListPreference that shows a help button on the right
 */
public class ListDetailsPreference extends ListPreference {

    /**
     * Constructor
     *
     * @param context The activity context
     */
    @SuppressWarnings("unused")
    public ListDetailsPreference(final Context context) {
        super(context);
    }

    /**
     * Constructor
     *
     * @param context The activity context
     * @param attrs The style attributes
     */
    @SuppressWarnings("unused")
    public ListDetailsPreference(final Context context, final AttributeSet attrs) {
        super(context, attrs);
    }

    /**
     * Constructor
     *
     * @param context The activity context
     * @param attrs The style attributes
     * @param defStyleAttr An attribute in the current theme that contains a reference to a style resource that
     *                     supplies default values for the view. Can be 0 to not look for defaults.
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @SuppressWarnings("unused")
    public ListDetailsPreference(final Context context, final AttributeSet attrs, final int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    /**
     * Constructor
     *
     * @param context The activity context
     * @param attrs The style attributes
     * @param defStyleAttr An attribute in the current theme that contains a reference to a style resource that
     *                     supplies default values for the view. Can be 0 to not look for defaults.
     * @param defStyleRes A resource identifier of a style resource that supplies default values for the view, used
     *                    only if defStyleAttr is 0 or can not be found in the theme. Can be 0 to not look for defaults.
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @SuppressWarnings("unused")
    public ListDetailsPreference(final Context context, final AttributeSet attrs, final int defStyleAttr,
                                 final int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected void onBindView(final View view) {
        super.onBindView(view);
        ImageView helpButton = view.findViewById(R.id.help_button);
        helpButton.setOnClickListener(view1 -> {
            Context context = view1.getContext();
            Intent intent = new Intent(context, WordListDetailsActivity.class);
            context.startActivity(intent);
        });
    }
}
