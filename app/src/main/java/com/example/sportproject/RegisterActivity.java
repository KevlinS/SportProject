package com.example.sportproject;

import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;


public class RegisterActivity extends AppCompatActivity {

    //Declaration EditTexts
    EditText editTextUserName;
    EditText editTextEmail;
    EditText editTextPassword;

    //Declaration TextInputLayout
    TextInputLayout textInputLayoutUserName;
    TextInputLayout textInputLayoutEmail;
    TextInputLayout textInputLayoutPassword;

    //Declaration Button
    Button buttonRegister;

    //Declaration DatabaseHelper
    DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        databaseHelper = new DatabaseHelper(this);
        initTextViewLogin();
        initViews();
        buttonRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (validate()) {
                    String UserName = editTextUserName.getText().toString();
                    String Email = editTextEmail.getText().toString();
                    String Password = editTextPassword.getText().toString();

                    //Vérifiez dans la base de données qu'il y a un utilisateur associé à cet e-mail
                    if (!databaseHelper.isEmailExists(Email)) {

                        //L'e-mail n'existe pas. Ajouter un nouvel utilisateur à la base de données
                        databaseHelper.addUser(new User(null, UserName, Email, Password));
                        Snackbar.make(buttonRegister, "L'utilisateur a été créé avec succès! Veuillez vous connecter. ", Snackbar.LENGTH_LONG).show();
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                finish();
                            }
                        }, Snackbar.LENGTH_LONG);
                    }else {

                        //si l'e-mail existe déjà
                        Snackbar.make(buttonRegister, "L'utilisateur existe déjà avec le même e-mail", Snackbar.LENGTH_LONG).show();
                    }


                }
            }
        });
    }

    //cette méthode utilisée pour définir l'événement de Login TextView
    private void initTextViewLogin() {
        TextView textViewLogin = (TextView) findViewById(R.id.textViewLogin);
        textViewLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    //cette méthode est utilisée pour connecter les vues XML à ses objets
    private void initViews() {
        editTextEmail = (EditText) findViewById(R.id.editTextEmail);
        editTextPassword = (EditText) findViewById(R.id.editTextPassword);
        editTextUserName = (EditText) findViewById(R.id.editTextUserName);
        textInputLayoutEmail = (TextInputLayout) findViewById(R.id.textInputLayoutEmail);
        textInputLayoutPassword = (TextInputLayout) findViewById(R.id.textInputLayoutPassword);
        textInputLayoutUserName = (TextInputLayout) findViewById(R.id.textInputLayoutUserName);
        buttonRegister = (Button) findViewById(R.id.buttonRegister);

    }

    //Cette méthode est utilisée pour valider les entrées saisies par l'utilisateur
    public boolean validate() {
        boolean valid = false;

        //Récupère les valeurs des champs EditText
        String UserName = editTextUserName.getText().toString();
        String Email = editTextEmail.getText().toString();
        String Password = editTextPassword.getText().toString();

        //la validation du champ UserName
        if (UserName.isEmpty()) {
            valid = false;
            textInputLayoutUserName.setError("Veuillez saisir un nom d'utilisateur valide!");
        } else {
            if (UserName.length() > 5) {
                valid = true;
                textInputLayoutUserName.setError(null);
            } else {
                valid = false;
                textInputLayoutUserName.setError("Le nom d'utilisateur est trop court!");
            }
        }

        //la validation du champ e-mail
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