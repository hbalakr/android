package com.bh.verbconjugation;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

public class Tab1 extends Fragment {
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		if (container == null) {
			return null;
		}
		View v = (LinearLayout)inflater.inflate(R.layout.tab1, container, false);	
		return v;
	}
	public void setText(String text){
        TextView textView = (TextView) getView().findViewById(R.id.textView1);
        textView.setText(text);
    }
}
