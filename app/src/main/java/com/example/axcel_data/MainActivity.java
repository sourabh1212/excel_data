package com.example.axcel_data;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.FileUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(grantResults.length >0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
        {

            if(ContextCompat.checkSelfPermission(this,Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){

                if(isExternalStorageAvailable())
                {
                    Intent chooseFile = new Intent(Intent.ACTION_GET_CONTENT);
                    chooseFile.setType("*/*");
                    chooseFile = Intent.createChooser(chooseFile, "Choose a file");
                    startActivityForResult(chooseFile, PICKFILE_RESULT_CODE);

                }else{
                    Toast.makeText(getApplicationContext(),"External Storage not available",Toast.LENGTH_SHORT).show();
                }

            }else{
                Toast.makeText(getApplicationContext(),"not permitted",Toast.LENGTH_SHORT).show();
            }

        }
    }

    private String[] FilePathStrings;
    private String[] FileNameStrings;
    private File[] ListFile;
    File file;
    List<color> color;
    TextView text;

    Button fileButn;

    int PICKFILE_RESULT_CODE = 1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);




        color = new ArrayList<>();

        fileButn = findViewById(R.id.dataButn);
        text = findViewById(R.id.text);

        fileButn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(ContextCompat.checkSelfPermission(getApplicationContext(),Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},1);
                }else{
                    if(isExternalStorageAvailable())
                    {
                        Intent chooseFile = new Intent(Intent.ACTION_GET_CONTENT);
                        chooseFile.setType("*/*");
                        chooseFile = Intent.createChooser(chooseFile, "Choose a file");
                        startActivityForResult(chooseFile, PICKFILE_RESULT_CODE);

                    }else{
                        Toast.makeText(getApplicationContext(),"External Storage not available",Toast.LENGTH_SHORT).show();
                    }

                }


                }




        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode==PICKFILE_RESULT_CODE)
        {
            Uri uri = data.getData();
            //String[] split = uri.getPath().split(":");

            Log.i("path ",uri.getPath());

            String src = null;
//            try {
            try {
                src = getPath(getApplicationContext(),uri);
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
            //              src = Environment.getExternalStorageDirectory()+"/"+ split[1];//PathUtil.getPath(getApplicationContext(),uri);
//            } catch (URISyntaxException e) {
//                e.printStackTrace();
//            }
            File file = new File(src);

            readExcelData(file);
        }
    }

    private void readExcelData(File file) {
        InputStream inputStream;
        XSSFWorkbook workbook;
        XSSFSheet sheet;

        try {


            inputStream = new FileInputStream(file);

            //--------------------------------------------------------------
            workbook = new XSSFWorkbook(inputStream);
            sheet = workbook.getSheetAt(0);
            int rowCount = sheet.getLastRowNum();
            FormulaEvaluator formulaEvaluator = workbook.getCreationHelper().createFormulaEvaluator();

            StringBuilder sb = new StringBuilder();
            int noOfCarton = 0;
            HashMap<Integer,String> sizes = new HashMap<>();

            for(int r=0;r<rowCount;r++)
            {
                Row row = sheet.getRow(r);

                if(row!=null)
                {


                    int colCount = row.getLastCellNum();
                    System.out.println("row :"+r+" col :"+colCount);


                    HashMap<String,Integer> size = new HashMap<>();

                    for(int c=0;c<colCount;c++)
                    {

                        Cell cell = row.getCell(c);
                        if(cell!=null){


                            if(r==0 && c>1)
                            {
                                sizes.put(c,cell.getStringCellValue());

                            }
                            if(c==0 && r>0)
                            {
                                noOfCarton+=cell.getNumericCellValue();

                            }
                            if(r>0 && c>1)
                            {
                                Log.d(sizes.get(c),String.valueOf(cell.getNumericCellValue()));
                                size.put(sizes.get(c),(int)cell.getNumericCellValue());

                            }


                        }

                    }

                    if(r>0)
                    {
                        color.add(new color(row.getCell(1).getStringCellValue(),size));
                    }





                }


            }

            System.out.println("no of carton  "+noOfCarton);

            for(int key : sizes.keySet())
            {
                System.out.println(sizes.get(key));
            }

            for(int i=0;i<color.size();i++)
            {
                Log.d("Tag",color.get(i).getName());
                text.setText(text.getText()+"\n"+color.get(i).getName());
                for(String xx : color.get(i).getSizes().keySet())
                {
                    Log.d("TagTagtAH",xx+" "+color.get(i).getSizes().get(xx).toString());
                    text.setText(text.getText()+" "+xx+" "+color.get(i).getSizes().get(xx).toString());
                }

            }





        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static boolean isExternalStorageReadOnly() {
        String extStorageState = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(extStorageState)) {
            return true;
        }
        return false;
    }

    private static boolean isExternalStorageAvailable() {
        String extStorageState = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(extStorageState)) {
            return true;
        }
        return false;
    }

    public static String getPath(Context context, Uri uri) throws URISyntaxException {
        if ("content".equalsIgnoreCase(uri.getScheme())) {
            String[] projection = { "_data" };
            Cursor cursor = null;

            try {
                cursor = context.getContentResolver().query(uri, projection, null, null, null);
                int column_index = cursor.getColumnIndexOrThrow("_data");
                if (cursor.moveToFirst()) {
                    return cursor.getString(column_index);
                }
            } catch (Exception e) {
                // Eat it
            }
        }
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        return null;
    }
}