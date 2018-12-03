/*
 *  TutorialActivity
 *
 *  Provides instructions on how to play the game when first logging in
 */

package com.example.s1636469.coinz;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

public class TutorialActivity extends Activity {

    private int section = 1;
    private TextView tutorialText;
    private Button continueButton;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tutorial);

        // get references to UI elements
        tutorialText = findViewById(R.id.tutorial_info);
        continueButton = (Button) findViewById(R.id.continue_button);

        // Set up UI elements
        tutorialText.setText(getString(R.string.tutorial_text_1));

        continueButton.setText(getString(R.string.tutorial_button_1));
        continueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateText();
            }
        });

        progressBar = findViewById(R.id.tut_progress);
    }
    /*
     *  updateText
     *
     *  Cycle the tutorial text, if the user has reached the end, start the main activity
     */

    private void updateText() {
        section++;

        if (section > 3) {
            progressBar.setVisibility(View.VISIBLE);

            tutorialText.setVisibility(View.INVISIBLE);
            continueButton.setVisibility(View.INVISIBLE);

            // Start the main activity
            Intent i = new Intent(TutorialActivity.this, MainActivity.class);
            startActivity(i);
        } else {
            // Change the text displayed
            if (section == 2) {
                tutorialText.setText(getString(R.string.tutorial_text_2));
                continueButton.setText(getString(R.string.tutorial_button_2));
            } else if(section == 3) {
                tutorialText.setText(getString(R.string.tutorial_text_3));
                continueButton.setText(getString(R.string.tutorial_button_3));
            }
        }
    }
}
