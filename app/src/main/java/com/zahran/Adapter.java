package com.zahran;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.AbsoluteSizeSpan;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.zahran.data.PersonEntity;
import java.util.List;

public class Adapter extends ArrayAdapter<PersonEntity> {

    public Adapter(@NonNull Context context, @NonNull List<PersonEntity> objects) {
        super(context, 0, objects);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_item, parent, false);
        }

        PersonEntity person = getItem(position);
        TextView mName = convertView.findViewById(R.id.list_item_name);
        TextView id = convertView.findViewById(R.id.list_item_id);

        String name = person != null ? person.getName() : null;
        String nickName = person != null ? person.getNickName() : null;
        String finalNickName = "(" + nickName + ")";
        Integer isFamily = person != null ? person.getIsFamily() : null;


        SpannableString span1 = new SpannableString(name);
        span1.setSpan(new AbsoluteSizeSpan(28 , true), 0, name != null ? name.length() : 0, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        CharSequence finalText = TextUtils.concat(span1);

        if (nickName != null && !nickName.isEmpty()) {
            SpannableString span2 = new SpannableString(finalNickName);
            span2.setSpan(new AbsoluteSizeSpan(18, true), 0, finalNickName.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            finalText = TextUtils.concat(span1, " ", span2);
        }

        mName.setText(finalText);


        String number = String.format("%d", position + 1);
        id.setText(number);

        if (isFamily != null && isFamily == 1) {
            mName.setCompoundDrawablesWithIntrinsicBounds(convertView.getResources().getDrawable(R.drawable.ic_view_list) ,
                    null , null , null);
        }
        return convertView;
    }
}