package com.example.finalproject;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_ADD_BOOK = 1; // 요청 코드
    private List<Book> bookList = new ArrayList<>(); // 책 데이터 저장할 리스트
    private GridLayout gridLayout; // GridLayout 참조
    private List<ImageView> imageViewList = new ArrayList<>(); // 모든 ImageView를 저장할 리스트
    private static final String FILE_NAME = "book_data.json"; // 내부 저장소 이름

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // *** 수정된 부분 시작: GridLayout 초기화 및 ImageView 리스트 구성 ***
        gridLayout = findViewById(R.id.bookGridLayout);

        // GridLayout 내의 모든 ImageView를 리스트에 추가
        for (int i = 0; i < gridLayout.getChildCount(); i++) {
            View child = gridLayout.getChildAt(i);
            if (child instanceof ImageView) {
                imageViewList.add((ImageView) child);
            }
        }

        Button addBookButton = findViewById(R.id.addBookButton);
        addBookButton.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, SecondActivity.class);
            startActivityForResult(intent, REQUEST_ADD_BOOK);
        });

        loadSavedBookData();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_ADD_BOOK && resultCode == RESULT_OK && data != null) {
            Book newBook = (Book) data.getSerializableExtra("bookData");
            if (newBook != null) {
                if (bookList.size() >= imageViewList.size()) {
                    Toast.makeText(this, "더 이상 도서를 추가할 수 없습니다.", Toast.LENGTH_SHORT).show();
                    return;
                }

                bookList.add(newBook);
                Toast.makeText(this, "새 책이 추가되었습니다: " + newBook.getTitle(), Toast.LENGTH_SHORT).show();
                addBookToImageView(newBook, bookList.size() - 1);

                saveBookData(newBook);
            }
        }
    }

    private void addBookToImageView(Book book, int index) {
        ImageView targetImageView = imageViewList.get(index);
        if (book.getImageUri() != null) {
            Uri imageUri = Uri.parse(book.getImageUri());
            targetImageView.setImageURI(imageUri);

            // 이미지가 제대로 로드되지 않을 경우, placeholder 이미지 설정
            if (targetImageView.getDrawable() == null) {
                targetImageView.setImageResource(R.drawable.gray_background);
            }
        } else {
            targetImageView.setImageResource(R.drawable.gray_background);
        }

        targetImageView.setTag(book); // Book 객체를 태그로 설정

        targetImageView.setOnClickListener(v -> {
            Object tag = v.getTag();
            if (tag instanceof Book) {
                showBookInfoDialog((Book) tag);
            } else {
                Toast.makeText(this, "도서 정보를 불러오는데 실패했습니다.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showBookInfoDialog(Book book) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        // 커스텀 레이아웃 인플레이트
        View dialogView = getLayoutInflater().inflate(R.layout.book_details, null);
        builder.setView(dialogView);

        ImageView dialogBookImage = dialogView.findViewById(R.id.dialogBookImage);
        TextView dialogBookTitle = dialogView.findViewById(R.id.dialogBookTitle);
        TextView dialogBookAuthor = dialogView.findViewById(R.id.dialogBookAuthor);
        TextView dialogReadingProgress = dialogView.findViewById(R.id.dialogReadingProgress);
        RatingBar dialogRatingBar = dialogView.findViewById(R.id.dialogRatingBar);
        ProgressBar dialogProgressBar = dialogView.findViewById(R.id.dialogProgressBar);
        TextView progressPercentage = dialogView.findViewById(R.id.progressPercentage);

        // 뷰가 제대로 참조되었는지 확인
        if (dialogBookImage == null || dialogBookTitle == null || dialogBookAuthor == null ||
                dialogReadingProgress == null || dialogRatingBar == null || dialogProgressBar == null ||
                progressPercentage == null) {
            Toast.makeText(this, "도서 정보 표시 오류가 발생했습니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        // 데이터 설정
        if (book.getImageUri() != null) {
            Uri imageUri = Uri.parse(book.getImageUri());
            dialogBookImage.setImageURI(imageUri);
            // 이미지가 제대로 로드되지 않을 경우, placeholder 이미지 설정
            if (dialogBookImage.getDrawable() == null) {
                dialogBookImage.setImageResource(R.drawable.gray_background);
            }
        } else {
            dialogBookImage.setImageResource(R.drawable.gray_background);
        }

        dialogBookTitle.setText(book.getTitle());
        dialogBookAuthor.setText(book.getAuthor());
        dialogReadingProgress.setText(String.valueOf(book.getReadPages()));
        dialogRatingBar.setRating(book.getRating());

        // 독서 진행 상태 설정
        int progress = (int) book.getProgress();
        dialogProgressBar.setMax(100);
        dialogProgressBar.setProgress(progress);
        progressPercentage.setText(progress + "%");

        // 확인 버튼 추가
        builder.setPositiveButton("확인", (dialog, which) -> dialog.dismiss());

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void saveBookData(Book book) {
        try {
            JSONArray bookArray = loadBooksFromFile();
            JSONObject bookJSON = new JSONObject();
            bookJSON.put("title", book.getTitle());
            bookJSON.put("author", book.getAuthor());
            bookJSON.put("totalPages", book.getTotalPages());
            bookJSON.put("readPages", book.getReadPages());
            bookJSON.put("rating", book.getRating());
            bookJSON.put("progress", book.getProgress());
            bookJSON.put("imageUri", book.getImageUri());

            bookArray.put(bookJSON);

            // 저장된 데이터를 파일에 덮어쓰기
            FileOutputStream fos = openFileOutput(FILE_NAME, MODE_PRIVATE);
            fos.write(bookArray.toString().getBytes());
            fos.close();

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "도서 저장에 실패했습니다.", Toast.LENGTH_SHORT).show();
        }
    }

    // JSON 파일에서 도서 목록을 불러오는 메서드
    private JSONArray loadBooksFromFile() {
        JSONArray bookArray = new JSONArray();
        try {
            File file = new File(getFilesDir(), FILE_NAME);
            if (!file.exists()) {
                // 파일이 없으면 빈 배열 반환
                return bookArray;
            }

            FileInputStream fis = openFileInput(FILE_NAME);
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader reader = new BufferedReader(isr);
            StringBuilder stringBuilder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line);
            }
            fis.close();

            // JSON 배열로 변환
            bookArray = new JSONArray(stringBuilder.toString());

        } catch (Exception e) {
            e.printStackTrace();
        }
        return bookArray;
    }

    // 저장된 도서 데이터를 불러와서 이미지와 책 정보를 설정
    private void loadSavedBookData() {
        try {
            JSONArray bookArray = loadBooksFromFile();

            // 이미지와 책 정보를 화면에 표시
            for (int i = 0; i < bookArray.length(); i++) {
                if (i < imageViewList.size()) {
                    JSONObject bookJson = bookArray.getJSONObject(i);
                    String imageUri = bookJson.getString("imageUri");

                    // Book 객체 생성
                    Book book = new Book(
                            bookJson.getString("title"),
                            bookJson.getString("author"),
                            bookJson.getInt("totalPages"),
                            bookJson.getInt("readPages"),
                            (float) bookJson.getDouble("rating"),
                            bookJson.getDouble("progress"),
                            imageUri
                    );

                    // 리스트에 추가
                    bookList.add(book);

                    // ImageView에 설정
                    addBookToImageView(book, i);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "도서 데이터를 불러오는데 실패했습니다.", Toast.LENGTH_SHORT).show();
        }
    }
}
