package com.example.basketballshoesandroidshop.Activity;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.basketballshoesandroidshop.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.maps.android.PolyUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class TrackingActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private String userId, orderId;
    private DatabaseReference databaseReference;
    private RequestQueue requestQueue; // Hàng đợi để thực hiện các yêu cầu mạng

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tracking);

        // Khởi tạo hàng đợi Volley
        requestQueue = Volley.newRequestQueue(this);

        userId = getIntent().getStringExtra("USER_ID");
        orderId = getIntent().getStringExtra("ORDER_ID");

        // Bắt sự kiện cho nút quay về
        ImageButton btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> onBackPressed());

        // Lấy Map Fragment
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        databaseReference = FirebaseDatabase.getInstance().getReference();
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        if (userId != null && orderId != null) {
            fetchTrackingData();
        }
    }

    private void fetchTrackingData() {
        DatabaseReference orderRef = databaseReference.child("Orders").child(userId).child(orderId);
        orderRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                DataSnapshot coordinatesSnapshot = snapshot.child("routeCoordinates");

                // Lấy các sự kiện vận chuyển và hiển thị trên bottom sheet (code tương tự)
                // ...

                if (coordinatesSnapshot.getChildrenCount() >= 2) {
                    try {
                        // Lấy điểm đầu và cuối
                        DataSnapshot startNode = coordinatesSnapshot.child("0");
                        DataSnapshot endNode = coordinatesSnapshot.child(String.valueOf(coordinatesSnapshot.getChildrenCount() - 1));

                        LatLng origin = new LatLng(startNode.child("lat").getValue(Double.class), startNode.child("lng").getValue(Double.class));
                        LatLng destination = new LatLng(endNode.child("lat").getValue(Double.class), endNode.child("lng").getValue(Double.class));

                        // Gọi hàm mới để lấy lộ trình và vẽ
                        getDirectionsAndDrawRoute(origin, destination);
                    } catch (Exception e) {
                        Toast.makeText(TrackingActivity.this, "Lỗi định dạng tọa độ.", Toast.LENGTH_SHORT).show();
                        Log.e("TrackingActivity", "Lỗi khi đọc tọa độ", e);
                    }
                } else {
                    Toast.makeText(TrackingActivity.this, "Không đủ dữ liệu tọa độ để vẽ.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(TrackingActivity.this, "Lỗi tải dữ liệu đơn hàng.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getDirectionsAndDrawRoute(LatLng origin, LatLng destination) {

        String apiKey = getString(R.string.map_api);
        String url = "https://maps.googleapis.com/maps/api/directions/json" +
                "?origin=" + origin.latitude + "," + origin.longitude +
                "&destination=" + destination.latitude + "," + destination.longitude +
                "&key=" + apiKey;

        // Tạo một yêu cầu chuỗi bằng Volley
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                response -> {
                    // Xử lý khi có kết quả trả về thành công
                    try {
                        JSONObject jsonResponse = new JSONObject(response);
                        JSONArray routes = jsonResponse.getJSONArray("routes");

                        if (routes.length() > 0) {
                            JSONObject route = routes.getJSONObject(0);
                            JSONObject overviewPolyline = route.getJSONObject("overview_polyline");
                            String encodedPolyline = overviewPolyline.getString("points");

                            // Dùng thư viện Maps Utils để giải mã chuỗi polyline
                            List<LatLng> decodedPath = PolyUtil.decode(encodedPolyline);

                            // Vẽ đường đi lên bản đồ
                            drawPolylineOnMap(decodedPath, origin, destination);
                        } else {
                            Toast.makeText(this, "Không tìm thấy lộ trình.", Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(this, "Lỗi phân tích dữ liệu lộ trình.", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    // Xử lý khi có lỗi mạng
                    Log.e("TrackingActivity", "Lỗi Volley: " + error.toString());
                    Toast.makeText(this, "Lỗi khi gọi API Directions.", Toast.LENGTH_SHORT).show();
                });

        // Thêm yêu cầu vào hàng đợi
        requestQueue.add(stringRequest);
    }

    private void drawPolylineOnMap(List<LatLng> path, LatLng origin, LatLng destination) {
        if (path.isEmpty() || mMap == null) return;

        // Vẽ đường đi
        mMap.addPolyline(new PolylineOptions().addAll(path).width(12).color(Color.BLUE).geodesic(true));

        // Thêm marker cho điểm đầu và cuối
        mMap.addMarker(new MarkerOptions().position(origin).title("Điểm đi"));
        mMap.addMarker(new MarkerOptions().position(destination).title("Điểm đến"));

        // Zoom camera để thấy toàn bộ lộ trình
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for (LatLng point : path) {
            builder.include(point);
        }
        LatLngBounds bounds = builder.build();
        int padding = 100; // Khoảng đệm từ mép màn hình (pixels)
        mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, padding));
    }
}
