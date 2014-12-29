package me.qiang.android.chongai.PopUpWindow;

import android.graphics.drawable.ColorDrawable;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.PopupWindow;

import me.qiang.android.chongai.R;

/**
 * Created by qiang on 12/29/2014.
 */
public class CreateNewStatePopupWindow extends PopupWindow {
    public CreateNewStatePopupWindow(View convertView, int width, int height) {
        super(convertView, width, height, true);
        setBackgroundDrawable(new ColorDrawable(0xFFF0F8FF));
        setTouchable(true);
        setOutsideTouchable(true);
        setTouchInterceptor(new View.OnTouchListener()
        {
            @Override
            public boolean onTouch(View v, MotionEvent event)
            {
                Log.i("POPUPWINDOW", "onTouchListener");
                if (event.getAction() == MotionEvent.ACTION_OUTSIDE || v.getId() == R.id.pop_up_bg)
                {
                    dismiss();
                    return true;
                }
                return false;
            }
        });

    }
}
