package com.danielbenami_tomermaalumi.ex3;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import java.util.ArrayList;

public class ContactAdapter extends ArrayAdapter<Contact>
{
    public ContactAdapter(Context context, ArrayList<Contact> list)
    {
        super(context, 0, list);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        convertView = LayoutInflater.from(getContext()).inflate(R.layout.contact_row, parent, false);

        Contact currentContact = getItem(position);

        TextView name = convertView.findViewById(R.id.nameID);
        name.setText(currentContact.getName());

        TextView phone = convertView.findViewById(R.id.phoneID);
        String phoneStr = currentContact.getPhone();
        if(phoneStr == null)
            phone.setText("");
        else
            phone.setText(phoneStr);

        ImageView iconView = convertView.findViewById(R.id.imageID);
        if(phoneStr.matches(""))
        {
            iconView.setImageResource(R.drawable.ic_phone_no);
        }
        else
        {
            iconView.setImageResource(R.drawable.ic_phone_yes);
        }
        return convertView;
    }
}
