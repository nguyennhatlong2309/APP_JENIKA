package com.brewmaster.components;

import com.brewmaster.theme.AppTheme;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Custom JComboBox với tính năng tìm kiếm bộ lọc động và hỗ trợ tiếng Việt không dấu.
 * Phù hợp làm cell editor trong JTable.
 */
public class SearchableComboBox extends JComboBox<String> {
    private final List<String> originalItems;
    private boolean isFiltering = false;

    public SearchableComboBox(String[] items) {
        super(items);
        this.originalItems = new ArrayList<>();
        for (String item : items) {
            this.originalItems.add(item);
        }
        init();
    }

    public SearchableComboBox(List<String> items) {
        super(items.toArray(new String[0]));
        this.originalItems = new ArrayList<>(items);
        init();
    }

    private void init() {
        setEditable(true);
        
        // Cài đặt giao diện tối thống nhất với hệ thống màu của ứng dụng
        setBackground(AppTheme.SURFACE_MED);
        setForeground(AppTheme.ON_SURFACE);
        setFont(AppTheme.FONT_BODY_MD);

        JTextComponent editorComponent = (JTextComponent) getEditor().getEditorComponent();
        editorComponent.setBackground(AppTheme.SURFACE_MED);
        editorComponent.setForeground(AppTheme.ON_SURFACE);
        editorComponent.setCaretColor(AppTheme.ON_SURFACE);
        editorComponent.setFont(AppTheme.FONT_BODY_MD);
        editorComponent.setBorder(BorderFactory.createEmptyBorder(0, 4, 0, 4));

        // Lắng nghe sự kiện gõ phím của người dùng để lọc danh sách
        editorComponent.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                updateFilter();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                updateFilter();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                updateFilter();
            }

            private void updateFilter() {
                if (isFiltering) return;

                SwingUtilities.invokeLater(() -> {
                    String text = editorComponent.getText();
                    if (text == null) text = "";

                    // Nếu text khớp hoàn hảo với 1 phần tử có sẵn (nghĩa là đã chọn xong), không lọc lại
                    if (originalItems.contains(text)) {
                        return;
                    }

                    isFiltering = true;

                    List<String> filtered = new ArrayList<>();
                    String query = removeAccents(text.toLowerCase().trim());
                    
                    for (String item : originalItems) {
                        if (item == null) continue;
                        String itemClean = removeAccents(item.toLowerCase());
                        if (itemClean.contains(query)) {
                            filtered.add(item);
                        }
                    }

                    // Lưu vị trí con trỏ hiện tại
                    int caretPos = editorComponent.getCaretPosition();

                    // Cập nhật ComboBox Model
                    DefaultComboBoxModel<String> model = (DefaultComboBoxModel<String>) getModel();
                    model.removeAllElements();
                    for (String item : filtered) {
                        model.addElement(item);
                    }

                    // Khôi phục lại chữ đã gõ và vị trí con trỏ
                    editorComponent.setText(text);
                    try {
                        editorComponent.setCaretPosition(Math.min(caretPos, text.length()));
                    } catch (Exception ignored) {}

                    // Hiển thị hoặc ẩn popup tùy kết quả
                    if (!filtered.isEmpty()) {
                        if (isShowing() && !isPopupVisible()) {
                            setPopupVisible(true);
                        }
                    } else {
                        setPopupVisible(false);
                    }

                    isFiltering = false;
                });
            }
        });

        // Tự động bôi đen toàn bộ chữ khi kích hoạt để dễ đè lên, và hiển thị menu đầy đủ
        editorComponent.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                SwingUtilities.invokeLater(() -> {
                    resetFilter();
                    editorComponent.selectAll();
                    if (isShowing() && !isPopupVisible()) {
                        setPopupVisible(true);
                    }
                });
            }

            @Override
            public void focusLost(FocusEvent e) {
                String text = editorComponent.getText();
                if (originalItems.contains(text)) {
                    setSelectedItem(text);
                }
            }
        });

        // Nhấn Enter khi không chọn dòng nào cụ thể thì tự lấy kết quả khớp đầu tiên
        editorComponent.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    String text = editorComponent.getText();
                    if (!originalItems.contains(text) && getItemCount() > 0) {
                        String firstMatch = getItemAt(0);
                        setSelectedItem(firstMatch);
                        editorComponent.setText(firstMatch);
                    }
                }
            }
        });
    }

    /**
     * Khôi phục toàn bộ danh sách ban đầu
     */
    public void resetFilter() {
        isFiltering = true;
        String currentText = ((JTextComponent) getEditor().getEditorComponent()).getText();

        DefaultComboBoxModel<String> model = (DefaultComboBoxModel<String>) getModel();
        model.removeAllElements();
        for (String item : originalItems) {
            model.addElement(item);
        }

        if (originalItems.contains(currentText)) {
            setSelectedItem(currentText);
        } else {
            ((JTextComponent) getEditor().getEditorComponent()).setText(currentText);
        }
        isFiltering = false;
    }

    public boolean isFiltering() {
        return isFiltering;
    }

    /**
     * Loại bỏ dấu tiếng Việt để tìm kiếm không dấu chính xác
     */
    private String removeAccents(String src) {
        if (src == null) return "";
        String nfdNormalizedString = Normalizer.normalize(src, Normalizer.Form.NFD);
        Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        String out = pattern.matcher(nfdNormalizedString).replaceAll("");
        // Chuyển đ -> d và Đ -> D
        return out.replaceAll("đ", "d").replaceAll("Đ", "D");
    }
}
