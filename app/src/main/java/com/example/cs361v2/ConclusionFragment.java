package com.example.cs361v2;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

public class ConclusionFragment extends Fragment {

    private TextView conclusionTextView;
    private Button backButton;
    private TextView selectedDateTextView;
    private ImageView statusImageView;

    @SuppressLint("StringFormatInvalid")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_conclusion, container, false);

        // Find the TextViews and Button by their IDs
        conclusionTextView = view.findViewById(R.id.conclusionTextView);
        backButton = view.findViewById(R.id.backButton);
        selectedDateTextView = view.findViewById(R.id.selectedDateTextView);
        statusImageView = view.findViewById(R.id.statusImageView); // New TextView for selected date

        // Get the data passed from the previous Fragment or Activity
        if (getArguments() != null) {
            int dailyTotal = getArguments().getInt("DAILY_TOTAL", 0);
            double goal = getArguments().getDouble("GOAL", 0.0);  // ใช้ getDouble แทน getInt
            String statusMessage = getArguments().getString("STATUS_MESSAGE", "No status");
            String selectedDate = getArguments().getString("SELECTED_DATE", "Unknown Date");  // Receive selected date

            // Display the values in the TextViews
            String message = getString(R.string.daily_total_message, dailyTotal, goal, statusMessage);
            conclusionTextView.setText(message);
            selectedDateTextView.setText(getString(R.string.selected_datecon, selectedDate));  // Display selected date

            // Display status image based on the comparison of dailyTotal and goal
            if (dailyTotal > goal) {
                statusImageView.setImageResource(R.drawable.sad_face);
            } else if (dailyTotal == goal) {
                statusImageView.setImageResource(R.drawable.neutral_face);
            } else {
                statusImageView.setImageResource(R.drawable.happy_face);
            }
        }

        // Set up the Back button to navigate back
        backButton.setOnClickListener(v -> getParentFragmentManager().popBackStack());

        return view;
    }

}


