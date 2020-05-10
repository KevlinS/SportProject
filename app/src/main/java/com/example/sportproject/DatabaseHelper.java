package com.example.sportproject;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;


public class DatabaseHelper extends SQLiteOpenHelper {

    //NOM DE LA BDD
    public static final String DATABASE_NAME = "RunningAppBDD";

    //VERSION DE LA BDD
    public static final int DATABASE_VERSION = 1;

    //NOM DE LA TABLE
    public static final String TABLE_USERS = "users";

    //---COLONNES UTILISATEURS DE TABLE---
    //ID COLONNES @primaryKey
    public static final String KEY_ID = "id";

    //COLONNE NOM D'UTILISATEUR
    public static final String KEY_USER_NAME = "username";

    //COLONNE EMAIL
    public static final String KEY_EMAIL = "email";

    //COLONNE MOT DE PASSE
    public static final String KEY_PASSWORD = "password";

    //SQL pour créer la table d'utilisateurs
    public static final String SQL_TABLE_USERS = " CREATE TABLE " + TABLE_USERS
            + " ( "
            + KEY_ID + " INTEGER PRIMARY KEY, "
            + KEY_USER_NAME + " TEXT, "
            + KEY_EMAIL + " TEXT, "
            + KEY_PASSWORD + " TEXT"
            + " ) ";


    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        //Créer une table lorsque oncreate est appelé
        sqLiteDatabase.execSQL(SQL_TABLE_USERS);

    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        //déposer une table pour en créer une nouvelle si la version de la base de données est mise à jour
        sqLiteDatabase.execSQL(" DROP TABLE IF EXISTS " + TABLE_USERS);
    }

    //On peut ajouter des utilisateurs à la table des utilisateurs
    public void addUser(User user) {

        //obtenir une base de données accessible en écriture
        SQLiteDatabase db = this.getWritableDatabase();

        //créer des valeurs de contenu à insérer
        ContentValues values = new ContentValues();

        //mettre le nom d'utilisateur dans @values
        values.put(KEY_USER_NAME, user.userName);

        //mettre le mail dans @values
        values.put(KEY_EMAIL, user.email);

        //mettre le mot de passe dans @values
        values.put(KEY_PASSWORD, user.password);

        // insérer les values
        long todo_id = db.insert(TABLE_USERS, null, values);
    }

    public User Authenticate(User user) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_USERS,// sélection de la table
                new String[]{KEY_ID, KEY_USER_NAME, KEY_EMAIL, KEY_PASSWORD},
                KEY_EMAIL + "=?",
                new String[]{user.email},
                null, null, null);

        if (cursor != null && cursor.moveToFirst()&& cursor.getCount()>0) {
            User user1 = new User(cursor.getString(0), cursor.getString(1), cursor.getString(2), cursor.getString(3));

            //faire correspondre les deux mots de passe, vérifier qu'ils sont identiques ou non
            if (user.password.equalsIgnoreCase(user1.password)) {
                return user1;
            }
        }


        return null;
    }

    public boolean isEmailExists(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_USERS,// sélection de la table
                new String[]{KEY_ID, KEY_USER_NAME, KEY_EMAIL, KEY_PASSWORD},
                KEY_EMAIL + "=?",
                new String[]{email},//Where clause
                null, null, null);

        if (cursor != null && cursor.moveToFirst()&& cursor.getCount()>0) {
            //si le curseur a une valeur, alors dans la base de données utilisateur, il y a un utilisateur associé à cet e-mail , donc il va retourner vrai
            return true;
        }

        //si le mail n'existe pas, il va retourner false
        return false;
    }
}
