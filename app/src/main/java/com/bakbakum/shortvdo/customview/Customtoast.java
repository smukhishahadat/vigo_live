package com.bakbakum.shortvdo.customview;

import android.app.Activity;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.bakbakum.shortvdo.R;

import com.bakbakum.shortvdo.view.base.BaseActivity;

public class Customtoast extends BaseActivity {


    public void showCustomToast(String text, String color, int icon){
        View toast = LayoutInflater.from(this).inflate(R.layout.custom_toast,(ViewGroup) findViewById(R.id.custom_toast_layout));
        Toast custom = new Toast(this);
        custom.setView(toast);
        TextView message = toast.findViewById(R.id.tv_toast_message);
        //setting the color to linear layout
        LinearLayout linearLayoutcolor = toast.findViewById(R.id.custom_toast_layout);
        ImageView imageview = toast.findViewById(R.id.tv_toast_image);
        //setting the image icon
        imageview.setBackground(this.getResources().getDrawable(icon));
        linearLayoutcolor.setBackgroundColor(Color.parseColor(color));

        message.setText(text);
        custom.setDuration(Toast.LENGTH_SHORT);
        custom.show();
    }
}
