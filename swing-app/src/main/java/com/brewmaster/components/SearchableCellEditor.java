package com.brewmaster.components;

import javax.swing.*;
import javax.swing.table.TableCellEditor;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * TableCellEditor tùy biến dành riêng cho SearchableComboBox để hoạt động trơn tru trong JTable
 * mà không bị gián đoạn giữa chừng khi cập nhật danh sách tìm kiếm.
 */
public class SearchableCellEditor extends AbstractCellEditor implements TableCellEditor {
    private final SearchableComboBox comboBox;

    public SearchableCellEditor(SearchableComboBox comboBox) {
        this.comboBox = comboBox;
        
        // Chỉ lưu kết quả và kết thúc chỉnh sửa khi người dùng chọn một sản phẩm thực tế
        comboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Nếu đang trong quá trình gõ phím lọc dữ liệu, ta bỏ qua sự kiện actionPerformed
                if (!comboBox.isFiltering()) {
                    stopCellEditing();
                }
            }
        });
    }

    public SearchableComboBox getComboBox() {
        return comboBox;
    }

    @Override
    public Object getCellEditorValue() {
        return comboBox.getSelectedItem();
    }

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        // Reset bộ lọc để hiển thị toàn bộ danh sách sản phẩm lúc bắt đầu kích hoạt ô nhập
        comboBox.resetFilter();
        comboBox.setSelectedItem(value);
        return comboBox;
    }
}
