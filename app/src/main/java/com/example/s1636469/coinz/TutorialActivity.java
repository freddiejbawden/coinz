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
        tutorialText = (TextView) findViewById(R.id.tutorial_info);
        tutorialText.setText(getString(R.string.tutorial_text_1));

        continueButton = (Button) findViewById(R.id.continue_button);
        continueButton.setText(getString(R.string.tutorial_button_1));
        continueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateText();
            }
        });
        progressBar = (ProgressBar) findViewById(R.id.tut_progress);
    }

    private void updateText() {
        section++;
        if (section > 3) {
            progressBar.setVisibility(View.VISIBLE);

            tutorialText.setVisibility(View.INVISIBLE);
            continueButton.setVisibility(View.INVISIBLE);

            Intent i = new Intent(TutorialActivity.this, MainActivity.class);
            startActivity(i);
        } else {
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
