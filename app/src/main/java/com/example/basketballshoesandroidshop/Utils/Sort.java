package com.example.basketballshoesandroidshop.Utils;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.basketballshoesandroidshop.R;
import com.example.basketballshoesandroidshop.ViewModel.CatalogViewModel;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

//Cách dùng:
//Khai báo hàm này:
//private void showDialog() {
//        Sort sortFragment = new Sort();
//        Dialog dialog = new Dialog(sortFragment);
//        dialog.show(getChildFragmentManager(), "dialog_sort");
//        }
//Và gọi hàm trên khi cần show

public class Sort extends BottomSheetDialogFragment {
    List<Option> sorts = new ArrayList<>();
    int selectedIndex = 0;
    private CatalogViewModel catalogViewModel;
    private int categoryId;
    private String searchContent;

    public Sort() {
    }

    public void setSearchContent(String searchContent) {
        this.searchContent = searchContent;
    }

    public Sort(int categoryId, CatalogViewModel catalogViewModel, String searchContent) {
        this.categoryId = categoryId;
        this.catalogViewModel = catalogViewModel;
        this.searchContent = searchContent;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_sort, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (sorts.size() < 4) {

            sorts.add(new Option("Price: high to low", true));
            sorts.add(new Option("Price: low to high", false));
            sorts.add(new Option("Title: A-Z", false));
            sorts.add(new Option("Title: Z-A", false));
        }

        ListView listView = (ListView) view.findViewById(R.id.sort_lv);
        OptionAdapter adapter = new OptionAdapter(getContext(), sorts);
        listView.setAdapter(adapter);
        MaterialButton cancel = view.findViewById(R.id.sort_bt_cancel);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Option old = (Option) adapterView.getItemAtPosition(selectedIndex);
                old.setChecked(false);
                Option selected = (Option) adapterView.getItemAtPosition(i);
                selected.setChecked(!selected.isChecked());
                selectedIndex = i;
                adapter.notifyDataSetChanged();

                catalogViewModel.forceGet(categoryId, selectedIndex, searchContent);
                dismiss();
            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });
    }
}