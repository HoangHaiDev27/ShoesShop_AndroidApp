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
    // ... các view khác

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tracking);

        userId = getIntent().getStringExtra("USER_ID");
        orderId = getIntent().getStringExtra("ORDER_ID");

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        databaseReference = FirebaseDatabase.getInstance().getReference();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
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
                // Lấy danh sách tọa độ từ Firebase
                List<LatLng> routePoints = new ArrayList<>();
                DataSnapshot coordinatesSnapshot = snapshot.child("routeCoordinates");
                for (DataSnapshot coordSnapshot : coordinatesSnapshot.getChildren()) {
                    Double lat = coordSnapshot.child("lat").getValue(Double.class);
                    Double lng = coordSnapshot.child("lng").getValue(Double.class);
                    if (lat != null && lng != null) {
                        routePoints.add(new LatLng(lat, lng));
                    }
                }

                // Lấy danh sách các sự kiện vận chuyển
                // ... code để lấy trackingEvents và đưa vào RecyclerView ...

                // Vẽ lên bản đồ
                drawRouteOnMap(routePoints);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // ✅ Đây là phương thức bị thiếu
                // Viết code xử lý khi có lỗi xảy ra (ví dụ: mất quyền truy cập)
                // Bạn nên in lỗi ra để dễ dàng gỡ rối
                Log.e("FirebaseError", "Lỗi khi đọc dữ liệu.", databaseError.toException());
            }
        });
    }

    private void drawRouteOnMap(List<LatLng> routePoints) {
        if (routePoints.isEmpty() || mMap == null) return;

        // Thêm các marker
        mMap.addMarker(new MarkerOptions().position(routePoints.get(0)).title("Điểm đi"));
        mMap.addMarker(new MarkerOptions().position(routePoints.get(routePoints.size() - 1)).title("Điểm đến"));

        // Vẽ đường đi
        PolylineOptions polylineOptions = new PolylineOptions()
                .addAll(routePoints)
                .width(10)
                .color(Color.BLUE);
        mMap.addPolyline(polylineOptions);

        // Di chuyển camera
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for (LatLng point : routePoints) {
            builder.include(point);
        }
        mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 100));
    }
}
