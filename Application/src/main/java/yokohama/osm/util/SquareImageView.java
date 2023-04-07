package yokohama.osm.util;

import android.content.Context;
import android.util.AttributeSet;
import androidx.appcompat.widget.AppCompatImageView;

public class SquareImageView extends AppCompatImageView {
    public SquareImageView(Context context) {
        super(context);
        setScaleType(ScaleType.CENTER_CROP);
//        setScaleType(ScaleType.FIT_CENTER);
    }
    public SquareImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setScaleType(ScaleType.CENTER_CROP);
//        setScaleType(ScaleType.FIT_CENTER);
    }
    public SquareImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
//        setScaleType(ScaleType.CENTER_CROP);
//        setScaleType(ScaleType.FIT_CENTER);
    }
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // 常に横幅と同じ縦幅を持った矩形のサイズを要求する
        setMeasuredDimension(widthMeasureSpec, widthMeasureSpec);
        ///setMeasuredDimension(widthMeasureSpec, (widthMeasureSpec / 2));
        //setMeasuredDimension(heightMeasureSpec, heightMeasureSpec);
        //setMeasuredDimension(widthMeasureSpec, heightMeasureSpec);
        //super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }
}
