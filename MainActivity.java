package com.danielbenami_tomermaalumi.ex3;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.ListView;
import android.widget.AdapterView;
import java.util.ArrayList;
import android.util.Log;
import android.net.Uri;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity implements View.OnClickListener
{
    //SQL declaration
    private SQLiteDatabase contactsDB = null;
    public static final String MY_DB_NAME = "contacts.db";

    private EditText edtName, edtNum;
    private Button btnInsert, btnSearch;
    private ListView contactsListView;
    private ArrayList<Contact> contactsList;
    private ContactAdapter contactAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnInsert = findViewById(R.id.btnInsert);
        btnSearch = findViewById(R.id.btnSearch);

        edtName = findViewById(R.id.edtNameID);
        edtNum = findViewById(R.id.edtNumID);
        contactsListView = findViewById(R.id.listViewID);
        contactsList = new ArrayList<>();

        //creating/reloading a data base, printing contacts list
        DataBase();
        searchContacts();
        showContacts(contactsList);

        btnInsert.setOnClickListener(this);
        btnSearch.setOnClickListener(this);

        //in the function below, we "listen" to the contact's entity, if pressed, we move to dialer
        contactsListView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                if(contactData(position+1).matches(""))
                {
                    Toast.makeText(MainActivity.this, "Number Not Provided", Toast.LENGTH_SHORT).show();
                    return;
                }
                else
                {
                    dialContact(contactData(position+1));
                }
            }
        });
    }

    @Override
    public void onClick(View v)
    {
        switch (v.getId())
        {
            case R.id.btnInsert:
                insertContact();
                break;

            case R.id.btnSearch:
                searchContacts();
                break;
        }
    }

    //creating data base
    public void DataBase()
    {
        try
        {
            contactsDB = openOrCreateDatabase(MY_DB_NAME, MODE_PRIVATE, null);
            String sql = "CREATE TABLE IF NOT EXISTS contacts (id integer primary key, name VARCHAR , number VARCHAR);";
            contactsDB.execSQL(sql);
        }
        catch (Exception e)
        {
            Log.d("debug", "Error Creating Database");
        }
    }

    //here we print to the screen the contact list
    public void showContacts(ArrayList<Contact> contactsList)
    {
        contactAdapter = new ContactAdapter(this, contactsList);
        contactsListView.setAdapter(contactAdapter);
    }

    //creating and inserting a new contact to the list
    public void insertContact()
    {
        Contact contact;
        String contactName = edtName.getText().toString();
        String contactNumber = edtNum.getText().toString();

        if(contactNumber.matches("") && contactName.matches(""))
        {
            Toast.makeText(this, "Please insert Name and Number", Toast.LENGTH_SHORT).show();
            return;
        }

        if(contactName.matches(""))
        {
            Toast.makeText(this, "Please insert Name", Toast.LENGTH_SHORT).show();
            return;
        }

        //we check if the contact already exist, if so we update the number
        if(checkIfDuplicate(contactName, contactNumber))
        {
            Toast.makeText(this, contactName+ " Updated", Toast.LENGTH_SHORT).show();
            return;
        }

        //execute SQL statement to insert new data
        String sqlQuestion = "INSERT INTO contacts (name, number) VALUES ('" + contactName + "', '" + contactNumber + "');";
        contactsDB.execSQL(sqlQuestion);
        contact = new Contact(contactName, contactNumber);
        contactsList.add(contact);
        Toast.makeText(this, contactName + " Inserted successfully!", Toast.LENGTH_SHORT).show();
        edtName.setText("");
        edtNum.setText("");
        showContacts(contactsList);
    }

    //this method look for a specific/few names and/or numbers in the list
    public void searchContacts()
    {
        contactsList = new ArrayList<>();
        Contact contact;
        String sqlQuestion = "SELECT * FROM contacts";
        Cursor cursor = contactsDB.rawQuery(sqlQuestion, null);

        int nameColumn = cursor.getColumnIndex("name");
        int numberColumn = cursor.getColumnIndex("number");

        String tempName = edtName.getText().toString().toLowerCase();
        String tempNum = edtNum.getText().toString().toLowerCase();

        if (cursor.moveToFirst())
        {
            do {
                String name = cursor.getString(nameColumn).toLowerCase();
                String num = cursor.getString(numberColumn).toLowerCase();

                if(name.contains(tempName) && num.contains(tempNum))
                {
                    contact = new Contact(name, num);
                    contactsList.add(contact);
                }
            } while (cursor.moveToNext());
        }

        if (contactsList.isEmpty())
        {
            Toast.makeText(this, "No Contact Found", Toast.LENGTH_LONG).show();
        }

        showContacts(contactsList);
    }

    //a method that check if the contact already exist, if so we update the number
    public boolean checkIfDuplicate(String newName, String newNumber)
    {
        String sqlQuestion = "SELECT * FROM contacts";
        Cursor cursor = contactsDB.rawQuery(sqlQuestion, null);
        int nameColumn = cursor.getColumnIndex("name");

        if (cursor.moveToFirst())
        {
            do {
                String tempName = cursor.getString(nameColumn);
                if(newName.matches(tempName))
                {
                    String sqlQuestion2 = "UPDATE contacts " + "SET number = "+"'" + newNumber + "'" + "WHERE name = "+"'" + newName + "'";
                    contactsDB.execSQL(sqlQuestion2);
                    edtName.setText("");
                    edtNum.setText("");
                    searchContacts();
                    showContacts(contactsList);
                    return true;
                }
            } while (cursor.moveToNext());
        }
        return false;
    }

    //this method receives the position of the contact pressed, check if it has a number
    public String contactData(int contactPosition)
    {
        String sqlQuestion = "SELECT * FROM contacts";
        Cursor cursor = contactsDB.rawQuery(sqlQuestion, null);

        int idColumn = cursor.getColumnIndex("id");
        int numColumn = cursor.getColumnIndex("number");

        if (cursor.moveToFirst())
        {
            do {
                String id = cursor.getString(idColumn);
                String num = cursor.getString(numColumn);

                if(contactPosition == Integer.parseInt(id))
                    return num;

            } while (cursor.moveToNext());
        }
        return "";
    }

    public void dialContact(String phoneNumber)
    {
        Intent intent = new Intent(Intent.ACTION_DIAL);
        intent.setData(Uri.parse("tel:" + phoneNumber));
        startActivity(intent);
    }

    @Override
    protected void onDestroy()
    {
        contactsDB.close();
        super.onDestroy();
    }
}