package com.example.basketballshoesandroidshop.Activity;

import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.basketballshoesandroidshop.Adapter.MessageAdapter;
import com.example.basketballshoesandroidshop.Domain.ChatMessage;
import com.example.basketballshoesandroidshop.Domain.ItemsModel;
import com.example.basketballshoesandroidshop.Domain.OrderModel;
import com.example.basketballshoesandroidshop.R;
import com.example.basketballshoesandroidshop.Repository.MainRepository;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ChatActivity extends AppCompatActivity {

    private RecyclerView messagesRecyclerView;
    private EditText messageInput;
    private ImageView sendButton;
    private ImageView backButton;
    private View typingIndicator;

    private MessageAdapter messageAdapter;
    private List<ChatMessage> messageList;
    private MainRepository repository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        // Khởi tạo repository
        repository = new MainRepository();

        initViews();
        setupRecyclerView();
        setupClickListeners();

        // Hiển thị tin nhắn chào mừng từ bot
        showWelcomeMessage();
    }

    private void initViews() {
        messagesRecyclerView = findViewById(R.id.messagesRecyclerView);
        messageInput = findViewById(R.id.messageInput);
        sendButton = findViewById(R.id.sendButton);
        backButton = findViewById(R.id.backButton);
        typingIndicator = findViewById(R.id.typingIndicator);
    }

    private void setupRecyclerView() {
        messageList = new ArrayList<>();
        messageAdapter = new MessageAdapter(messageList, this);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true); // Scroll to bottom

        messagesRecyclerView.setLayoutManager(layoutManager);
        messagesRecyclerView.setAdapter(messageAdapter);
    }

    private void setupClickListeners() {
        // Send button click
        sendButton.setOnClickListener(v -> sendMessage());

        // Back button click
        backButton.setOnClickListener(v -> finish());

        // Enter key trong EditText
        messageInput.setOnEditorActionListener((v, actionId, event) -> {
            sendMessage();
            return true;
        });
    }

    private void sendMessage() {
        String messageText = messageInput.getText().toString().trim();

        if (TextUtils.isEmpty(messageText)) {
            return;
        }

        // Tạo tin nhắn từ user
        ChatMessage userMessage = ChatMessage.createUserMessage(
                "user_001",
                "You",
                messageText
        );

        // Thêm vào danh sách và hiển thị
        addMessage(userMessage);

        // Clear input
        messageInput.setText("");

        // Hiển thị typing indicator và response từ bot
        showBotTyping();
        generateBotResponse(messageText);
    }

    private void addMessage(ChatMessage message) {
        messageList.add(message);
        messageAdapter.notifyItemInserted(messageList.size() - 1);

        // Scroll to bottom
        messagesRecyclerView.scrollToPosition(messageList.size() - 1);
    }

    private void showBotTyping() {
        typingIndicator.setVisibility(View.VISIBLE);

        // Scroll to show typing indicator
        messagesRecyclerView.scrollToPosition(messageList.size() - 1);
    }

    private void hideBotTyping() {
        typingIndicator.setVisibility(View.GONE);
    }

    private void generateBotResponse(String userMessage) {
        // Simulate bot thinking time (1-3 seconds)
        new Handler().postDelayed(() -> {
            hideBotTyping();
            getBotResponseWithFirebase(userMessage);
        }, 1500); // 1.5 second delay
    }

    private void getBotResponseWithFirebase(String userMessage) {
        String message = userMessage.toLowerCase();

        // Phân loại intent và xử lý với Firebase
        if (message.contains("start") || message.contains("hello") || message.contains("hi")) {
            sendBotMessage(getWelcomeResponse());

        } else if (message.contains("category") || message.contains("danh mục") || message.contains("loại")) {
            handleCategoryInquiry();

        } else if (message.contains("men") || message.contains("nam") || message.contains("đàn ông")) {
            handleMenProductInquiry();

        } else if (message.contains("women") || message.contains("nữ") || message.contains("phụ nữ")) {
            handleWomenProductInquiry();

        } else if (message.contains("shoes") || message.contains("giày")) {
            handleShoesInquiry();

        } else if (message.contains("kids") || message.contains("trẻ em") || message.contains("bé")) {
            handleKidsInquiry();

        } else if (message.contains("casual") || message.contains("thường ngày")) {
            handleSpecificProduct("casual");

        } else if (message.contains("plaid") || message.contains("sọc")) {
            handleSpecificProduct("plaid");

        } else if (message.contains("blazer") || message.contains("áo vest")) {
            handleSpecificProduct("blazer");

        } else if (message.contains("shirt") || message.contains("áo")) {
            handleSpecificProduct("shirt");

        } else if (message.contains("brown") || message.contains("nâu") || message.contains("màu nâu")) {
            handleSpecificProduct("brown");

        } else if (message.contains("giá") || message.contains("tiền") || message.contains("price")) {
            handlePriceInquiry();

        } else if (message.contains("sale") || message.contains("giảm giá") || message.contains("khuyến mãi")) {
            handleSaleInquiry();

        } else if (message.contains("cảm ơn") || message.contains("thank")) {
            sendBotMessage("😊 Không có gì! Tôi luôn sẵn sàng hỗ trợ bạn.\n\nCòn gì khác tôi có thể giúp không?");

        } else {
            sendBotMessage(getDefaultResponse());
        }
    }

    // Helper method để gửi bot message
    private void sendBotMessage(String message) {
        ChatMessage botMessage = ChatMessage.createBotMessage(message);
        addMessage(botMessage);
    }

    // Xử lý câu hỏi về danh mục
    private void handleCategoryInquiry() {
        String response = "📂 **Danh mục sản phẩm của chúng tôi:**\n\n" +
                "👫 **All** - Tất cả sản phẩm\n" +
                "👩 **Women** - Dành cho nữ\n" +
                "👨 **Men** - Dành cho nam\n" +
                "👟 **Shoes** - Giày dép\n" +
                "👶 **Kids** - Trẻ em\n\n" +
                "Bạn muốn xem sản phẩm danh mục nào? Hãy gõ tên danh mục! 😊";

        sendBotMessage(response);
    }

    // Xử lý sản phẩm nam
    private void handleMenProductInquiry() {
        sendBotMessage("👨 Đang tìm sản phẩm dành cho nam...");

        repository.loadPopular().observe(this, items -> {
            if (items != null && !items.isEmpty()) {
                List<ItemsModel> menItems = new ArrayList<>();

                for (ItemsModel item : items) {
                    // categoryId = 2 là Men theo Firebase data
                    if (item.getCategoryId() == 2) {
                        menItems.add(item);
                    }
                }

                if (!menItems.isEmpty()) {
                    String response = formatMenProductsResponse(menItems);
                    sendBotMessage(response);
                } else {
                    sendBotMessage("😔 Hiện tại không có sản phẩm nam nào. Vui lòng thử lại sau!");
                }
            }
        });
    }

    // Xử lý sản phẩm nữ
    private void handleWomenProductInquiry() {
        sendBotMessage("👩 Đang tìm sản phẩm dành cho nữ...");

        repository.loadPopular().observe(this, items -> {
            if (items != null && !items.isEmpty()) {
                List<ItemsModel> womenItems = new ArrayList<>();

                for (ItemsModel item : items) {
                    // categoryId = 1 là Women theo Firebase data
                    if (item.getCategoryId() == 1) {
                        womenItems.add(item);
                    }
                }

                if (!womenItems.isEmpty()) {
                    String response = formatWomenProductsResponse(womenItems);
                    sendBotMessage(response);
                } else {
                    sendBotMessage("😔 Hiện tại chưa có sản phẩm nữ. Sẽ cập nhật sớm nhất!");
                }
            }
        });
    }

    // Xử lý sản phẩm giày
    private void handleShoesInquiry() {
        sendBotMessage("👟 Đang tìm sản phẩm giày...");

        repository.loadPopular().observe(this, items -> {
            if (items != null && !items.isEmpty()) {
                List<ItemsModel> shoeItems = new ArrayList<>();

                for (ItemsModel item : items) {
                    // categoryId = 3 là Shoes theo Firebase data
                    if (item.getCategoryId() == 3) {
                        shoeItems.add(item);
                    }
                }

                if (!shoeItems.isEmpty()) {
                    String response = formatShoesResponse(shoeItems);
                    sendBotMessage(response);
                } else {
                    sendBotMessage("😔 Hiện tại chưa có sản phẩm giày. Sẽ cập nhật sớm!");
                }
            }
        });
    }

    // Xử lý sản phẩm trẻ em
    private void handleKidsInquiry() {
        sendBotMessage("👶 Đang tìm sản phẩm trẻ em...");

        repository.loadPopular().observe(this, items -> {
            if (items != null && !items.isEmpty()) {
                List<ItemsModel> kidsItems = new ArrayList<>();

                for (ItemsModel item : items) {
                    // categoryId = 4 là Kids theo Firebase data
                    if (item.getCategoryId() == 4) {
                        kidsItems.add(item);
                    }
                }

                if (!kidsItems.isEmpty()) {
                    String response = formatKidsResponse(kidsItems);
                    sendBotMessage(response);
                } else {
                    sendBotMessage("😔 Hiện tại chưa có sản phẩm trẻ em. Sẽ cập nhật sớm!");
                }
            }
        });
    }

    // Xử lý tìm kiếm sản phẩm cụ thể
    private void handleSpecificProduct(String keyword) {
        sendBotMessage("🔍 Đang tìm sản phẩm có chứa \"" + keyword + "\"...");

        repository.loadPopular().observe(this, items -> {
            if (items != null && !items.isEmpty()) {
                List<ItemsModel> foundItems = new ArrayList<>();

                for (ItemsModel item : items) {
                    String title = item.getTitle().toLowerCase();
                    if (title.contains(keyword.toLowerCase())) {
                        foundItems.add(item);
                    }
                }

                if (!foundItems.isEmpty()) {
                    String response = formatSpecificProductResponse(foundItems, keyword);
                    sendBotMessage(response);
                } else {
                    sendBotMessage("😔 Không tìm thấy sản phẩm \"" + keyword + "\". Bạn thử tìm với từ khóa khác nhé!");
                }
            }
        });
    }


    // Format response cho sản phẩm nam
    private String formatMenProductsResponse(List<ItemsModel> menItems) {
        StringBuilder response = new StringBuilder();
        response.append("👨 **Sản phẩm dành cho Nam:**\n\n");

        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));

        for (int i = 0; i < Math.min(3, menItems.size()); i++) {
            ItemsModel item = menItems.get(i);
            response.append(i + 1).append(". **").append(item.getTitle()).append("**\n");
            response.append("💰 ").append(currencyFormat.format(item.getPrice()));

            // Hiển thị giá cũ nếu có
            if (item.getOldPrice() > item.getPrice()) {
                response.append(" (~~").append(currencyFormat.format(item.getOldPrice())).append("~~)");
            }

            response.append(" | ⭐ ").append(item.getRating()).append("/5");
            response.append(" | 💬 ").append(item.getReview()).append(" đánh giá\n\n");
        }

        response.append("Bạn quan tâm đến sản phẩm nào? 😊");
        return response.toString();
    }

    // Format response cho sản phẩm nữ
    private String formatWomenProductsResponse(List<ItemsModel> womenItems) {
        StringBuilder response = new StringBuilder();
        response.append("👩 **Sản phẩm dành cho Nữ:**\n\n");

        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));

        for (int i = 0; i < Math.min(3, womenItems.size()); i++) {
            ItemsModel item = womenItems.get(i);
            response.append(i + 1).append(". **").append(item.getTitle()).append("**\n");
            response.append("💰 ").append(currencyFormat.format(item.getPrice()));

            if (item.getOldPrice() > item.getPrice()) {
                response.append(" (~~").append(currencyFormat.format(item.getOldPrice())).append("~~)");
            }

            response.append(" | ⭐ ").append(item.getRating()).append("/5");
            response.append(" | 💬 ").append(item.getReview()).append(" đánh giá\n\n");
        }

        response.append("Bạn muốn tìm hiểu thêm về sản phẩm nào? 💕");
        return response.toString();
    }

    // Format response cho giày
    private String formatShoesResponse(List<ItemsModel> shoeItems) {
        StringBuilder response = new StringBuilder();
        response.append("👟 **Bộ sưu tập Giày:**\n\n");

        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));

        for (int i = 0; i < shoeItems.size(); i++) {
            ItemsModel item = shoeItems.get(i);
            response.append("🔥 **").append(item.getTitle()).append("**\n");
            response.append("💰 ").append(currencyFormat.format(item.getPrice()));

            if (item.getOldPrice() > item.getPrice()) {
                double discount = ((item.getOldPrice() - item.getPrice()) / item.getOldPrice()) * 100;
                response.append(" (-").append(String.format("%.0f", discount)).append("%)");
            }

            response.append(" | ⭐ ").append(item.getRating()).append("/5\n\n");
        }

        response.append("👟 Chất lượng cao, thiết kế thời trang!");
        return response.toString();
    }

    // Format response cho trẻ em
    private String formatKidsResponse(List<ItemsModel> kidsItems) {
        StringBuilder response = new StringBuilder();
        response.append("👶 **Sản phẩm cho Trẻ em:**\n\n");

        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));

        for (int i = 0; i < kidsItems.size(); i++) {
            ItemsModel item = kidsItems.get(i);
            response.append("🌟 **").append(item.getTitle()).append("**\n");
            response.append("💰 ").append(currencyFormat.format(item.getPrice()));

            if (item.getOldPrice() > item.getPrice()) {
                response.append(" (~~").append(currencyFormat.format(item.getOldPrice())).append("~~)");
            }

            response.append(" | ⭐ ").append(item.getRating()).append("/5\n\n");
        }

        response.append("👶 Sản phẩm an toàn cho bé yêu!");
        return response.toString();
    }

    // Cập nhật xử lý câu hỏi về giá với data thực
    private void handlePriceInquiry() {
        sendBotMessage("💰 Đang phân tích thông tin giá...");

        repository.loadPopular().observe(this, items -> {
            if (items != null && !items.isEmpty()) {
                // Tính toán thống kê từ data thực
                double minPrice = items.stream().mapToDouble(ItemsModel::getPrice).min().orElse(0);
                double maxPrice = items.stream().mapToDouble(ItemsModel::getPrice).max().orElse(0);
                double avgPrice = items.stream().mapToDouble(ItemsModel::getPrice).average().orElse(0);

                NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));

                String response = "💰 **Thông tin giá sản phẩm:**\n\n" +
                        "📊 **Thống kê giá hiện tại:**\n" +
                        "• Thấp nhất: " + currencyFormat.format(minPrice) + "\n" +
                        "• Cao nhất: " + currencyFormat.format(maxPrice) + "\n" +
                        "• Trung bình: " + currencyFormat.format(avgPrice) + "\n\n" +
                        "🎯 **Phân khúc giá:**\n" +
                        "• Budget: Dưới " + currencyFormat.format(40) + "\n" +
                        "• Tầm trung: " + currencyFormat.format(40) + " - " + currencyFormat.format(60) + "\n" +
                        "• Cao cấp: Trên " + currencyFormat.format(60) + "\n\n" +
                        "🎁 **Ưu đãi hiện tại:**\n" +
                        "• Giảm 15-30% nhiều sản phẩm\n" +
                        "• Miễn phí ship đơn > 500K\n\n" +
                        "Bạn muốn xem sản phẩm tầm giá nào?";

                sendBotMessage(response);
            }
        });
    }

    // Cập nhật xử lý sale với data thực
    private void handleSaleInquiry() {
        sendBotMessage("🔥 Đang kiểm tra khuyến mãi...");

        repository.loadPopular().observe(this, items -> {
            if (items != null && !items.isEmpty()) {
                List<ItemsModel> saleItems = new ArrayList<>();

                for (ItemsModel item : items) {
                    if (item.getOldPrice() > item.getPrice()) {
                        saleItems.add(item);
                    }
                }

                if (!saleItems.isEmpty()) {
                    StringBuilder response = new StringBuilder();
                    response.append("🔥 **FLASH SALE - Giảm giá HOT!**\n\n");

                    NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));

                    for (ItemsModel item : saleItems) {
                        double discount = ((item.getOldPrice() - item.getPrice()) / item.getOldPrice()) * 100;

                        response.append("💥 **").append(item.getTitle()).append("**\n");
                        response.append("~~").append(currencyFormat.format(item.getOldPrice())).append("~~");
                        response.append(" → ").append(currencyFormat.format(item.getPrice()));
                        response.append(" **(-").append(String.format("%.0f", discount)).append("%)**\n");
                        response.append("⭐ ").append(item.getRating()).append("/5 | 💬 ").append(item.getReview()).append(" reviews\n\n");
                    }

                    response.append("⏰ **Số lượng có hạn!** Đặt hàng ngay!");
                    sendBotMessage(response.toString());
                } else {
                    sendBotMessage("🎁 **Khuyến mãi đặc biệt:**\n\n" +
                            "🔥 Giảm giá up to 30%\n" +
                            "🚚 Miễn phí ship toàn quốc\n" +
                            "💳 Trả góp 0% lãi suất\n\n" +
                            "Gõ tên sản phẩm để kiểm tra giá!");
                }
            }
        });
    }

    // Format response cho sản phẩm cụ thể
    private String formatSpecificProductResponse(List<ItemsModel> items, String keyword) {
        StringBuilder response = new StringBuilder();
        response.append("🎯 **Kết quả tìm kiếm \"").append(keyword).append("\":**\n\n");

        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));

        for (int i = 0; i < items.size(); i++) {
            ItemsModel item = items.get(i);
            response.append("✨ **").append(item.getTitle()).append("**\n");
            response.append("💰 ").append(currencyFormat.format(item.getPrice()));

            if (item.getOldPrice() > item.getPrice()) {
                response.append(" (~~").append(currencyFormat.format(item.getOldPrice())).append("~~)");
            }

            response.append("\n⭐ ").append(item.getRating()).append("/5");
            response.append(" | 💬 ").append(item.getReview()).append(" đánh giá\n");
            response.append("🏷️ ID: ").append(item.getId()).append("\n\n");
        }

        response.append("Bạn muốn xem thêm thông tin sản phẩm nào?");
        return response.toString();
    }

    // Format response cho đơn hàng
    private String formatOrdersResponse(ArrayList<OrderModel> orders) {
        StringBuilder response = new StringBuilder();
        response.append("📦 **Đơn hàng của bạn:**\n\n");

        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));

        // Hiển thị 3 đơn hàng gần nhất
        int count = Math.min(3, orders.size());
        for (int i = 0; i < count; i++) {
            OrderModel order = orders.get(i);
            response.append("🛍️ **Đơn #").append(order.getOrderId()).append("**\n");
            response.append("💰 ").append(currencyFormat.format(order.getTotalPrice()));
            response.append(" | 📋 ").append(order.getOrderStatus()).append("\n\n");
        }

        if (orders.size() > 3) {
            response.append("... và ").append(orders.size() - 3).append(" đơn hàng khác\n\n");
        }

        response.append("💡 Vào mục \"Đơn hàng\" trong app để xem chi tiết!");

        return response.toString();
    }

    // Helper methods
    private String getCurrentUserId() {
        return "user_001"; // Placeholder
    }

    // Cập nhật welcome và default responses
    private String getWelcomeResponse() {
        return "🚀 **Chào mừng đến Basketball Shoes!**\n\n" +
                "🤖 Tôi có thể giúp bạn:\n\n" +
                "📂 **\"category\"** - Xem danh mục\n" +
                "👨 **\"men\"** - Sản phẩm nam\n" +
                "👩 **\"women\"** - Sản phẩm nữ  \n" +
                "👟 **\"shoes\"** - Bộ sưu tập giày\n" +
                "👶 **\"kids\"** - Sản phẩm trẻ em\n" +
                "💰 **\"giá\"** - Thông tin giá cả\n" +
                "🔥 **\"sale\"** - Khuyến mãi hot\n\n" +
                "Hoặc gõ tên sản phẩm để tìm kiếm! 😊";
    }

    private String getDefaultResponse() {
        return "🤔 **Tôi có thể giúp bạn tìm:**\n\n" +
                "🔍 **Theo danh mục:**\n" +
                "• \"men\" - Sản phẩm nam\n" +
                "• \"women\" - Sản phẩm nữ\n" +
                "• \"shoes\" - Giày dép\n" +
                "• \"kids\" - Trẻ em\n\n" +
                "🔍 **Theo tên sản phẩm:**\n" +
                "• \"casual\" - Giày casual\n" +
                "• \"plaid\" - Áo plaid coat\n" +
                "• \"blazer\" - Áo blazer\n" +
                "• \"brown\" - Giày màu nâu\n\n" +
                "Hoặc gõ \"start\" để xem menu! 🚀";
    }

    private void showWelcomeMessage() {
        // Hiển thị tin nhắn chào mừng sau 500ms
        new Handler().postDelayed(() -> {
            ChatMessage welcomeMessage = ChatMessage.createBotMessage(
                    "👋 Chào mừng bạn đến với Basketball Shoes!\n\n" +
                            "Tôi là trợ lý ảo, sẵn sàng hỗ trợ bạn 24/7.\n\n" +
                            "Hãy nhập \"start\" để bắt đầu! 🚀"
            );
            addMessage(welcomeMessage);
        }, 500);
    }
}