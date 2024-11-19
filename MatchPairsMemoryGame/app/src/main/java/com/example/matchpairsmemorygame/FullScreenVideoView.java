// A custom VideoView class for displaying videos in full screen mode.
package com.example.matchpairsmemorygame;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.VideoView;

public class FullScreenVideoView extends VideoView {

    // Constructor with a single Context parameter.
    public FullScreenVideoView(Context context) {
        super(context); // Call the superclass constructor with the context.
    }

    // Constructor with Context and AttributeSet parameters.
    public FullScreenVideoView(Context context, AttributeSet attrs) {
        super(context, attrs); // Call the superclass constructor with the context and attribute set.
    }

    // Constructor with Context, AttributeSet, and defStyleAttr parameters.
    public FullScreenVideoView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr); // Call the superclass constructor with the context, attribute set, and default style attribute.
    }

    // Override the onMeasure method to set the dimensions of the VideoView to fill its parent.
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = getDefaultSize(0, widthMeasureSpec); // Get the default width based on the widthMeasureSpec.
        int height = getDefaultSize(0, heightMeasureSpec); // Get the default height based on the heightMeasureSpec.
        setMeasuredDimension(width, height); // Set the measured dimensions of the VideoView to the calculated width and height.
    }
}