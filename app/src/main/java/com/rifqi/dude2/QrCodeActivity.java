package com.rifqi.dude2;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;

public class QrCodeActivity extends AppCompatActivity {
    private Button scan_code, back;
    private ImageView mImageView;
    private Activity mActivity;
    private Bitmap generatedBitmap;
    private String fileName;

    private String currentUserID;
    private FirebaseAuth mAuth;
    private DatabaseReference RootRef;

    private TextView textView2,textView3;
    private View view1;

    private String data;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qr_code);

        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        RootRef = FirebaseDatabase.getInstance().getReference();

        scan_code = (Button) findViewById(R.id.scan_code);
        back = (Button) findViewById(R.id.back);
        mImageView = (ImageView) findViewById(R.id.outputBitmap);
        textView2 = (TextView) findViewById(R.id.textView2);
        textView3 = (TextView) findViewById(R.id.textView3);
        view1 = (View) findViewById(R.id.view);

        if (currentUserID == null){
            mImageView.setImageResource(R.drawable.ic_holder);
        } else {
            try {
                generateQRcode(currentUserID);
            } catch (WriterException e){
                e.printStackTrace();
            }
        }
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                QrCodeActivity.this.finish();
            }
        });
        scan_code.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent callingIntent = new Intent(QrCodeActivity.this, ScannerQrActivity.class);
                startActivity(callingIntent);
            }
        });
    }

        private void generateQRcode(String s) throws WriterException{
        fileName = s;
        BitMatrix result;
        result = new MultiFormatWriter().encode(s, BarcodeFormat.QR_CODE,1080,1080,null);
        int w = result.getWidth();
        int h = result.getHeight();
        int[] pixels = new int[w * h];
        for (int y = 0; y <h; y++){
            int offset = y * w;
            for (int x = 0; x < w; x++){
                pixels[offset + x] = result.get(x,y) ? Color.BLACK : Color.WHITE;
            }
        }
        Bitmap bitmap = Bitmap.createBitmap(w,h, Bitmap.Config.ARGB_8888);
        bitmap.setPixels(pixels,0,1080,0,0,w,h);
        generatedBitmap = bitmap;
        mImageView.setImageBitmap(bitmap);
    }
}