package com.example.sportproject;

import androidx.appcompat.app.AppCompatActivity;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Handler;
import android.os.Bundle;
import android.widget.ProgressBar;

public class MainActivity extends AppCompatActivity {

    ProgressBar splashProgress;
    int SPLASH_TIME = 3000; //This is 3 seconds


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //exécuter la barre de progression
        splashProgress = findViewById(R.id.splashProgress);
        playProgress();

        //Code pour démarrer Splash
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                //On passe à la page suivante
                Intent mySuperIntent = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(mySuperIntent);

                // finish() permet de quitter l'application lorsque on appuie sur le bouton de retour de la page d'accueil qui est MainActivity
                finish();

            }
        }, SPLASH_TIME);

    }

    //cette méthode pour exécuter la barre de progression pendant 5 secondes
    private void playProgress() {
        ObjectAnimator.ofInt(splashProgress, "progress", 100)
                .setDuration(4000)
                .start();
    }
}
