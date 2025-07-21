package com.example.basketballshoesandroidshop.Activity;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.basketballshoesandroidshop.Domain.FeedbackModel;
import com.example.basketballshoesandroidshop.R;
import com.example.basketballshoesandroidshop.Utils.SessionManager;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class RatingDialogFragment extends DialogFragment {

    private static final String ARG_ORDER_ID = "orderId";
    private static final String ARG_ITEM_ID = "itemId";

    private String orderId;
    private String itemId;

    public static RatingDialogFragment newInstance(String orderId, String itemId) {
        RatingDialogFragment fragment = new RatingDialogFragment();
        Bundle args = new Bundle();
        args.putString(ARG_ORDER_ID, orderId);
        args.putString(ARG_ITEM_ID, itemId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            orderId = getArguments().getString(ARG_ORDER_ID);
            itemId = getArguments().getString(ARG_ITEM_ID);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_rating, container, false);

        RatingBar ratingBar = view.findViewById(R.id.ratingBar);
        EditText etComment = view.findViewById(R.id.etComment);
        Button btnCancel = view.findViewById(R.id.btnCancel);
        Button btnSubmit = view.findViewById(R.id.btnSubmit);

        btnCancel.setOnClickListener(v -> dismiss());

        btnSubmit.setOnClickListener(v -> {
            float rating = ratingBar.getRating();
            String comment = etComment.getText().toString().trim();

            if (rating == 0) {
                Toast.makeText(getContext(), "Vui lòng chọn số sao đánh giá", Toast.LENGTH_SHORT).show();
                return;
            }

            submitFeedback(rating, comment);
        });

        return view;
    }

    private void submitFeedback(float rating, String comment) {
        DatabaseReference feedbackRef = FirebaseDatabase.getInstance().getReference("Feedback");

        // Tạo một ID duy nhất cho feedback
        String feedbackId = feedbackRef.push().getKey();
        // 1. Khởi tạo SessionManager
        SessionManager sessionManager = new SessionManager(getContext());
        // 2. Lấy userId từ session
        String userId = sessionManager.getUserFromSession().getEmail();


        // Lấy userId, tạm thời hardcode

        long createdAt = System.currentTimeMillis();

        FeedbackModel feedback = new FeedbackModel(userId, itemId, orderId, rating, comment, createdAt);

        if (feedbackId != null) {
            feedbackRef.child(feedbackId).setValue(feedback)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(getContext(), "Cảm ơn bạn đã đánh giá!", Toast.LENGTH_SHORT).show();
                        dismiss();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(getContext(), "Gửi đánh giá thất bại.", Toast.LENGTH_SHORT).show();
                    });
        }
    }
}
