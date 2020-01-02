package com.gbsoft.giuseppebotta.archivio;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.app.AlertDialog;
import android.text.format.DateFormat;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.InputStream;
import java.util.Date;

public class ArchivioSQLite extends Activity {

    EditText tv_nome, tv_max,tv_min,tv_pulsa;
    Button buttonAdd, buttonDeleteAll;

    private SQLiteAdapter mySQLiteAdapter;
    ListView listContent;
    int mMax,Mmin;

    SimpleCursorAdapter cursorAdapter;
    Cursor cursor;
    InputStream is = null;
    String nome,max,min,pulsa,quando,ora,giornOra;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        quando = DateFormat.format("dd-MM-yyyy", new Date(System.currentTimeMillis())).toString();
        ora = DateFormat.format("HH:mm", new Date(System.currentTimeMillis())).toString();
        giornOra = DateFormat.format("dd/MM/yyyy HH:mm", new Date(System.currentTimeMillis())).toString();


        tv_nome = (EditText)findViewById(R.id.tv_nome);
        tv_max = (EditText)findViewById(R.id.tv_max);
        tv_min = (EditText)findViewById(R.id.tv_min);
        tv_pulsa = (EditText)findViewById(R.id.tv_pulsa);

        buttonAdd = (Button)findViewById(R.id.add);
        buttonDeleteAll = (Button)findViewById(R.id.deleteall);

        buttonAdd.setText("Aggiungi");
        buttonDeleteAll.setText("Svuota il Db");


        tv_nome.requestFocus();



        listContent = (ListView)findViewById(R.id.contentlist);

        mySQLiteAdapter = new SQLiteAdapter(this);
        mySQLiteAdapter.openToWrite();

        cursor = mySQLiteAdapter.queueAll();
        String[] from = new String[]{SQLiteAdapter.KEY_ID, SQLiteAdapter.KEY_NOME, SQLiteAdapter.KEY_RILIEVI};
        int[] to = new int[]{R.id.id, R.id.text1, R.id.text2};


        cursorAdapter =
                new SimpleCursorAdapter(this, R.layout.row, cursor, from, to);
        listContent.setAdapter(cursorAdapter);
        listContent.setOnItemClickListener(listContentOnItemClickListener);

        buttonAdd.setOnClickListener(buttonAddOnClickListener);
        buttonDeleteAll.setOnClickListener(buttonDeleteAllOnClickListener);

    }

    Button.OnClickListener buttonAddOnClickListener
            = new Button.OnClickListener(){

        @Override
        public void onClick(View arg0) {

            if (tv_nome.getText().toString().equals("") || tv_max.getText().toString().equals("")
                    || tv_min.getText().toString().equals("")|| tv_pulsa.getText().toString().equals("")){
                Toast.makeText(ArchivioSQLite.this, "Devi inserire i dati richiesti", Toast.LENGTH_SHORT).show();
            }else{
                nome = tv_nome.getText().toString();
                max =  tv_max.getText().toString();
                min =  tv_min.getText().toString();
                pulsa =tv_pulsa.getText().toString();
                mySQLiteAdapter.insert(nome +" Sys: "+max+" Dia: "+min," Pul: "+pulsa+" -Data: "+quando+" -Ora: "+ora);
                hideKeyBoard();
                updateList();
                SvuotaEditText();
            }
        }

    };


    Button.OnClickListener buttonDeleteAllOnClickListener = new Button.OnClickListener(){

        @Override
        public void onClick(View arg0) {
            SvuotoDB();
        }
    };

    private ListView.OnItemClickListener listContentOnItemClickListener
            = new ListView.OnItemClickListener(){

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position,
                                long id) {


            Cursor cursor = (Cursor) parent.getItemAtPosition(position);
            final int item_id = cursor.getInt(cursor.getColumnIndex(SQLiteAdapter.KEY_ID));
            String item_content1 = cursor.getString(cursor.getColumnIndex(SQLiteAdapter.KEY_NOME));
            String item_content2 = cursor.getString(cursor.getColumnIndex(SQLiteAdapter.KEY_RILIEVI));

            AlertDialog.Builder myDialog
                    = new AlertDialog.Builder(ArchivioSQLite.this);

            myDialog.setTitle("Cancello dato?");

            TextView dialogTxt_id = new TextView(ArchivioSQLite.this);
            DrawerLayout.LayoutParams dialogTxt_idLayoutParams
                    = new DrawerLayout.LayoutParams(DrawerLayout.LayoutParams.WRAP_CONTENT, DrawerLayout.LayoutParams.WRAP_CONTENT);
            dialogTxt_id.setLayoutParams(dialogTxt_idLayoutParams);
            dialogTxt_id.setText("#" + String.valueOf(item_id));

            TextView dialogC1_id = new TextView(ArchivioSQLite.this);
            DrawerLayout.LayoutParams dialogC1_idLayoutParams
                    = new DrawerLayout.LayoutParams(DrawerLayout.LayoutParams.WRAP_CONTENT, DrawerLayout.LayoutParams.WRAP_CONTENT);
            dialogC1_id.setLayoutParams(dialogC1_idLayoutParams);
            dialogC1_id.setText(item_content1);

            TextView dialogC2_id = new TextView(ArchivioSQLite.this);
            DrawerLayout.LayoutParams dialogC2_idLayoutParams
                    = new DrawerLayout.LayoutParams(DrawerLayout.LayoutParams.WRAP_CONTENT, DrawerLayout.LayoutParams.WRAP_CONTENT);
            dialogC2_id.setLayoutParams(dialogC2_idLayoutParams);
            dialogC2_id.setText(item_content2);

            LinearLayout layout = new LinearLayout(ArchivioSQLite.this);
            layout.setOrientation(LinearLayout.VERTICAL);
            layout.addView(dialogTxt_id);
            layout.addView(dialogC1_id);
            layout.addView(dialogC2_id);
            myDialog.setView(layout);

            myDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                // do something when the button is clicked
                public void onClick(DialogInterface arg0, int arg1) {
                    mySQLiteAdapter.delete_byID(item_id);
                    updateList();
                }
            });

            myDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                // do something when the button is clicked
                public void onClick(DialogInterface arg0, int arg1) {

                }
            });

            myDialog.show();


        }};

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        mySQLiteAdapter.close();
    }



    private void updateList(){
        cursor.requery();
    }

    private  void SvuotoDB(){

        AlertDialog.Builder myDialog
                = new AlertDialog.Builder(ArchivioSQLite.this);

        myDialog.setTitle("Svuoto il dataBase?");

        myDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            // do something when the button is clicked
            public void onClick(DialogInterface arg0, int arg1) {
                mySQLiteAdapter.deleteAll();
                updateList();
            }
        });

        myDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            // do something when the button is clicked
            public void onClick(DialogInterface arg0, int arg1) {

            }
        });
        myDialog.show();
    }
    //nascondi tastiera:
    public void hideKeyBoard(){
        // nascondo la tastiera
        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(tv_nome.getWindowToken(), 0);
    }

    public void SvuotaEditText(){
        tv_max.setText("");
        tv_min.setText("");
        tv_pulsa.setText("");
    }
}