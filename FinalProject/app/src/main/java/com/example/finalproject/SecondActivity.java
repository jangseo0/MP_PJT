package com.example.finalproject;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

public class SecondActivity extends AppCompatActivity {

    private static final int REQUEST_IMAGE_PICK = 1;

    private EditText edtTitle, edtAuthor, edtPages, edtReadPages;
    private RatingBar edtRating;
    private ImageView bookImage;
    private Button addImageButton, submitButton;
    private Uri selectedImageUri;
    private static final String FILE_NAME = "book_data.json";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second);
        setTitle("도서 추가");

        edtTitle = findViewById(R.id.bookTitleEditText);
        edtAuthor = findViewById(R.id.bookAuthorEditText);
        edtPages = findViewById(R.id.totalPagesEditText);
        edtReadPages = findViewById(R.id.pagesReadEditText);
        edtRating = findViewById(R.id.ratingBar);
        submitButton = findViewById(R.id.submitButton);
        bookImage = findViewById(R.id.bookImage);
        addImageButton = findViewById(R.id.addImageButton);

        // 이미지 선택 버튼 클릭 리스너 설정
        addImageButton.setOnClickListener(v -> openImagePicker());

        // '돌아가기' 버튼 클릭 이벤트 (데이터 저장 및 반환)
        submitButton.setOnClickListener(v -> saveBookDataAndReturn());
    }

    // 이미지 선택을 위한 인텐트 열기
    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        // MIME 타입을 이미지로 제한
        intent.setType("image/*");
        startActivityForResult(intent, REQUEST_IMAGE_PICK);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // 이미지 선택 결과 처리
        if (requestCode == REQUEST_IMAGE_PICK && resultCode == RESULT_OK && data != null) {
            Uri originalUri = data.getData();
            if (originalUri != null) {
                // 이미지 내부 저장소로 복사
                Uri internalUri = copyImageToInternalStorage(originalUri);
                if (internalUri != null) {
                    selectedImageUri = internalUri;
                    bookImage.setImageURI(selectedImageUri);
                } else {
                    Toast.makeText(this, "이미지 저장에 실패했습니다.", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    // 이미지를 내부 저장소로 복사하는 메서드
    private Uri copyImageToInternalStorage(Uri uri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(uri);
            if (inputStream == null) return null;

            // 고유한 파일 이름 생성
            String fileName = "book_" + System.currentTimeMillis() + ".jpg";
            File file = new File(getFilesDir(), fileName);
            FileOutputStream fos = new FileOutputStream(file);

            byte[] buffer = new byte[1024];
            int len;
            while ((len = inputStream.read(buffer)) > 0) {
                fos.write(buffer, 0, len);
            }

            fos.close();
            inputStream.close();

            return Uri.fromFile(file);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // 도서 정보 저장 및 반환 메서드
    private void saveBookDataAndReturn() {
        String title = edtTitle.getText().toString().trim();
        String author = edtAuthor.getText().toString().trim();
        String totalPagesStr = edtPages.getText().toString().trim();
        String readPagesStr = edtReadPages.getText().toString().trim();
        float rating = edtRating.getRating();

        // 입력 검증
        if (title.isEmpty() || author.isEmpty() || totalPagesStr.isEmpty() || readPagesStr.isEmpty()) {
            Toast.makeText(this, "모든 필드를 입력해주세요.", Toast.LENGTH_SHORT).show();
            return;
        }

        int totalPages = Integer.parseInt(totalPagesStr);
        int readPages = Integer.parseInt(readPagesStr);

        // 진행률 계산
        double progress = (readPages / (double) totalPages) * 100;

        // Book 객체 생성
        Book newBook = new Book(title, author, totalPages, readPages, rating, progress, selectedImageUri != null ? selectedImageUri.toString() : null);

        // Intent를 통해 MainActivity로 결과 반환
        Intent resultIntent = new Intent();
        resultIntent.putExtra("bookData", newBook);
        setResult(Activity.RESULT_OK, resultIntent);
        finish();
    }
}
