package com.example.gprec_30.fragment_classes;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.gprec_30.R;

import org.json.JSONException;
import org.json.JSONObject;

public class HomeFragment extends Fragment {
    private TextView quoteTextView, authorTextView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.home_fragment, container, false);

        // Initialize the TextView
        quoteTextView = rootView.findViewById(R.id.quoteTextView);
        authorTextView = rootView.findViewById(R.id.author);
        // Fetch the quote of the day
        fetchQuote();

        return rootView;
    }

    private void fetchQuote() {
        // Instantiate the RequestQueue (this handles network requests)
        RequestQueue queue = Volley.newRequestQueue(getActivity());
        String url = "https://quotes-api-self.vercel.app/quote"; // URL to fetch the random quote

        // Create a JsonObjectRequest to fetch the quote
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            // Parse the JSON response to extract the quote and author
                            String quote = response.getString("quote");
                            String author = "- "+ response.getString("author");

                            // Set the quote and author in the TextView
                            quoteTextView.setText("\"" + quote + "\"");
                            authorTextView.setText(author);
                        } catch (JSONException e) {
                            Toast.makeText(getActivity(), "Failed to parse the quote", Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                error -> {
                        quoteTextView.setText("Welcome");
                });

        // Add the request to the RequestQueue
        queue.add(jsonObjectRequest);
    }
}

