package com.staceybellerose.randomwordgenerator;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialogFragment;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ImageSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.staceybellerose.randomwordgenerator.utils.SdkLeakFixer;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Fragment to display help text
 */
public class WordListHelpFragment extends BottomSheetDialogFragment {

    /**
     * The Text View displaying the help text
     */
    @BindView(R.id.help_text)
    TextView mTextView;
    /**
     * Button to access list of sources
     */
    @BindView(R.id.button)
    Button mButton;
    /**
     * A span which attaches an icon to the Text View
     */
    private ImageSpan mImageSpan;

    /**
     * Create a new instance of the helper fragment
     *
     * @return a new fragment
     */
    public static WordListHelpFragment newInstance() {
        return new WordListHelpFragment();
    }

    @Override
    public void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        setStyle(BottomSheetDialogFragment.STYLE_NORMAL, R.style.AppTheme_BottomSheet);
        mImageSpan = new ImageSpan(getContext(), R.drawable.ic_favorite_green_500_18dp);
    }

    @Override
    public void onDestroy() {
        SdkLeakFixer.clearTextLineCache(); // prevent memory leak
        super.onDestroy();
    }

    @Nullable
    @Override
    public View onCreateView(final LayoutInflater inflater, @Nullable final ViewGroup container,
                             @Nullable final Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.word_list_help_bottom_sheet, container, false);
        ButterKnife.bind(this, view);
        final SpannableString text = new SpannableString(mTextView.getText());
        final int position = text.toString().indexOf("❤");
        text.setSpan(mImageSpan, position, position + 1, Spanned.SPAN_INCLUSIVE_INCLUSIVE);
        mTextView.setText(text);
        return view;
    }

    /**
     * Click method for help text and header
     */
    @OnClick({R.id.help_text, R.id.help_header})
    @SuppressWarnings("unused")
    public void onTextClick() {
        dismiss();
    }

    /**
     * Click method for button
     */
    @OnClick(R.id.button)
    @SuppressWarnings("unused")
    public void onButtonClick() {
        startActivity(new Intent(getActivity(), WordListSourceActivity.class));
    }
}
