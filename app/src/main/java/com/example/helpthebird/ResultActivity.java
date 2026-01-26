package com.example.helpthebird;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Process;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class ResultActivity extends AppCompatActivity {

    private TextView textViewResultInfo, textViewMyScore, textViewHighestScore;
    private Button buttonAgain;
    private int score;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_result);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        textViewHighestScore = findViewById(R.id.textViewHighestScore);
        textViewMyScore = findViewById(R.id.textViewMyScore);
        textViewResultInfo = findViewById(R.id.textViewResultInfo);
        buttonAgain = findViewById(R.id.buttonAgain);

        score = getIntent().getIntExtra("score", 0);
        textViewMyScore.setText("Your score : " + score);

        sharedPreferences = this.getSharedPreferences("Score", Context.MODE_PRIVATE);
        int highestScore = sharedPreferences.getInt("highestScore", 0);

        if (score > highestScore) {
            sharedPreferences.edit().putInt("highestScore", score).apply();
            highestScore = score;
            textViewResultInfo.setText("New High Score!");
        } else if (score == highestScore) {
            textViewResultInfo.setText("You matched the High Score!");
        } else {
            textViewResultInfo.setText("Good effort!");
        }

        textViewHighestScore.setText("Highest Score: " + highestScore);

        buttonAgain.setOnClickListener(view -> {
            Intent intent = new Intent(ResultActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        Log.d("ResultActivity", "Score received: " + score + ", Highest Score: " + highestScore);
    }


    @Override
    public void onBackPressed() {
        // Show quit confirmation dialog when back button is pressed
        AlertDialog.Builder builder = new AlertDialog.Builder(ResultActivity.this);
        builder.setTitle("Help The Innocent Bird");
        builder.setMessage("Are you sure you want to quit the game?");
        builder.setCancelable(false);

        builder.setNegativeButton("Quit game", (dialogInterface, i) -> {
            moveTaskToBack(true);
            Process.killProcess(Process.myPid());
            System.exit(0);
        });

        builder.setPositiveButton("Cancel", (dialogInterface, which) -> dialogInterface.cancel());

        AlertDialog alertDialog = builder.create();
        alertDialog.show();

        // Call the super method to ensure default behavior is preserved
        super.onBackPressed();
    }


}
