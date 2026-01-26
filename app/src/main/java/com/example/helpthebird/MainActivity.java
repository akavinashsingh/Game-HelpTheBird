package com.example.helpthebird;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    private ImageView bird, enemy1, enemy2, enemy3, coin, volume;
    private Button buttonStart;
    private Animation animation;
    private MediaPlayer mediaPlayer;
    private boolean isMuted = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.constraintLayout), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        bird = findViewById(R.id.bird);
        enemy1 = findViewById(R.id.enemy1);
        enemy2 = findViewById(R.id.enemy2);
        enemy3 = findViewById(R.id.enemy3);
        coin = findViewById(R.id.coin);
        volume = findViewById(R.id.volume);
        buttonStart = findViewById(R.id.buttonStart);

        // Load and apply animation
        animation = AnimationUtils.loadAnimation(MainActivity.this, R.anim.scale_animation);
        bird.setAnimation(animation);
        enemy1.setAnimation(animation);
        enemy2.setAnimation(animation);
        enemy3.setAnimation(animation);
        coin.setAnimation(animation);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Initialize and start MediaPlayer
        mediaPlayer = MediaPlayer.create(MainActivity.this, R.raw.game_sound);
        mediaPlayer.setLooping(true); // Loop the sound
        mediaPlayer.start();

        // Handle volume control
        volume.setOnClickListener(view -> {
            if (!isMuted) {
                mediaPlayer.setVolume(0, 0);
                volume.setImageResource(R.drawable.baseline_volume_off_24);
                isMuted = true;
            } else {
                mediaPlayer.setVolume(1, 1);
                volume.setImageResource(R.drawable.baseline_volume_up_24);
                isMuted = false;
            }
        });

        // Handle start button click
        buttonStart.setOnClickListener(view -> {
            if (mediaPlayer != null) {
                mediaPlayer.reset(); // Consider releasing mediaPlayer here if necessary
                mediaPlayer.release();
                mediaPlayer = null;
            }
            volume.setImageResource(R.drawable.baseline_volume_up_24);
            Intent intent = new Intent(MainActivity.this, GameActivity.class);
            startActivity(intent);
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Stop and release MediaPlayer to avoid memory leaks
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }
}
