package com.example.sportproject;

import android.content.Intent;
import android.os.Bundle;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;
import android.text.Html;
import android.text.Spanned;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {

    //Declaration EditTexts
    EditText editTextEmail;
    EditText editTextPassword;

    //Declaration TextInputLayout
    TextInputLayout textInputLayoutEmail;
    TextInputLayout textInputLayoutPassword;

    //Declaration Button
    Button buttonLogin;

    //Declaration DatabaseHelper
    DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        databaseHelper = new DatabaseHelper(this);
        initCreateAccountTextView();
        initViews();

        buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //Vérifier que l'entrée d'utilisateur est correcte ou non
                if (validate()) {

                    //Récupère les valeurs des champs EditText
                    String Email = editTextEmail.getText().toString();
                    String Password = editTextPassword.getText().toString();

                    //Authentifier l'utilisateur
                    User currentUser = databaseHelper.Authenticate(new User(null, null, Email, Password));

                    //Vérifier que l'authentification est réussie ou non
                    if (currentUser != null) {
                        Snackbar.make(buttonLogin, "Connexion réussie!", Snackbar.LENGTH_LONG).show();

                        //L'utilisateur connecté a lancé avec succès
                       Intent intent=new Intent(LoginActivity.this,HomeActivity.class);
                        startActivity(intent);
                        finish();
                    } else {

                        //Échec de la connexion de l'utilisateur
                        Snackbar.make(buttonLogin, "Échec de la connexion, veuillez réessayer", Snackbar.LENGTH_LONG).show();

                    }
                }
            }
        });


    }

    //cette méthode utilisée pour définir le TextView et cliquer sur l'événement (plusieurs couleurs
    // pour TextView pas encore prises en charge dans Xml)
    private void initCreateAccountTextView() {
        TextView textViewCreateAccount = (TextView) findViewById(R.id.textViewCreateAccount);
        textViewCreateAccount.setText(fromHtml("<font color='#ffffff'>Je n'ai pas encore de compte. </font><font color='#0c0099'>créer un compte</font>"));
        textViewCreateAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });
    }

    //cette méthode est utilisée pour connecter les vues XML à ses objets
    private void initViews() {
        editTextEmail = (EditText) findViewById(R.id.editTextEmail);
        editTextPassword = (EditText) findViewById(R.id.editTextPassword);
        textInputLayoutEmail = (TextInputLayout) findViewById(R.id.textInputLayoutEmail);
        textInputLayoutPassword = (TextInputLayout) findViewById(R.id.textInputLayoutPassword);
        buttonLogin = (Button) findViewById(R.id.buttonLogin);

    }

    //Cette méthode sert à gérer la méthode fromHtml
    @SuppressWarnings("deprecation")
    public static Spanned fromHtml(String html) {
        Spanned result;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            result = Html.fromHtml(html, Html.FROM_HTML_MODE_LEGACY);
        } else {
            result = Html.fromHtml(html);
        }
        return result;
    }

    //Cette méthode est utilisée pour valider les entrées saisies par l'utilisateur
    public boolean validate() {
        boolean valid = false;

        //Récupère les valeurs des champs EditText
        String Email = editTextEmail.getText().toString();
        String Password = editTextPassword.getText().toString();

        //la validation du champ E-mail
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(Email).matches()) {
            valid = false;
            textInputLayoutEmail.setError("Veuillez saisir un e-mail valide!");
        } else {
            valid = true;
            textInputLayoutEmail.setError(null);
        }

        //la validation du champ mot de passe
        if (Password.isEmpty()) {
            valid = false;
            textInputLayoutPassword.setError("Veuillez entrer un mot de passe valide!");
        } else {
            if (Password.length() > 5) {
                valid = true;
                textInputLayoutPassword.setError(null);
            } else {
                valid = false;
                textInputLayoutPassword.setError("Le mot de passe est trop court!");
            }
        }

        return valid;
    }


}