package com.brewmaster.panels;

import com.brewmaster.db.DatabaseManager;
import com.brewmaster.theme.AppTheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.PreparedStatement;

public class PartnerSearchDialog extends JDialog {
    private JTextField tfSearch;
    private JTable table;
    private DefaultTableModel tableModel;
    private TableRowSorter<DefaultTableModel> sorter;
    
    /**
     * "Khách hàng" | "Nhà cung cấp" | "khach_hang" | "nha_cung_cap" | null (tất cả)
     */
    private final String partnerType;
    private String selectedName = null;
    private Integer selectedId = null;
    private String selectedSdt = null;
    private boolean confirmed = false;

    public PartnerSearchDialog(Frame owner, String partnerType) {
        super(owner, "Tìm Kiếm " + getDialogTitle(partnerType), true);
        this.partnerType = partnerType;
        initUI();
        loadData();
    }

    /** Tiêu đề dialog dựa theo type */
    private static String getDialogTitle(String type) {
        if ("khach_hang".equals(type) || "Khách hàng".equals(type)) return "Khách Hàng";
        if ("nha_cung_cap".equals(type) || "Nhà cung cấp".equals(type)) return "Nhà Cung Cấp";
        return "Đối Tác";
    }

    /** Chuẩn hoá type → giá trị hiển thị dùng trong SET MySQL */
    private String getDisplayLoai() {
        if ("khach_hang".equals(partnerType) || "Khách hàng".equals(partnerType)) return "Khách hàng";
        if ("nha_cung_cap".equals(partnerType) || "Nhà cung cấp".equals(partnerType)) return "Nhà cung cấp";
        return null; // không lọc
    }

    public boolean isConfirmed() { return confirmed; }
    public String getSelectedName() { return selectedName; }
    public Integer getSelectedId() { return selectedId; }
    public String getSelectedSdt() { return selectedSdt; }

    private void initUI() {
        setSize(600, 500);
        setMinimumSize(new Dimension(500, 400));
        setLocationRelativeTo(getOwner());
        getContentPane().setBackground(AppTheme.SURFACE_HIGH);
        setLayout(new BorderLayout());

        // Header / Search Bar Panel
        JPanel topPanel = new JPanel(new BorderLayout(10, 0));
        topPanel.setBackground(AppTheme.SURFACE_HIGH);
        topPanel.setBorder(new EmptyBorder(14, 20, 10, 20));

        JPanel searchFieldPanel = new JPanel(new BorderLayout(8, 0));
        searchFieldPanel.setOpaque(false);

        JLabel lblSearch = new JLabel("Tìm kiếm:");
        lblSearch.setFont(AppTheme.FONT_LABEL);
        lblSearch.setForeground(AppTheme.ON_SURFACE_VAR);

        tfSearch = new JTextField();
        tfSearch.setFont(AppTheme.FONT_BODY_MD);
        tfSearch.setBackground(AppTheme.SURFACE_MED);
        tfSearch.setForeground(AppTheme.ON_SURFACE);
        tfSearch.setCaretColor(AppTheme.ON_SURFACE);
        tfSearch.putClientProperty("JTextField.placeholderText", "Nhập tên hoặc số điện thoại để tìm...");
        tfSearch.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(AppTheme.OUTLINE_VARIANT, 1),
                new EmptyBorder(6, 10, 6, 10)));

        searchFieldPanel.add(lblSearch, BorderLayout.WEST);
        searchFieldPanel.add(tfSearch, BorderLayout.CENTER);

        JButton addBtn = new JButton("<html><font face='Segoe UI Emoji'>➕</font> Thêm mới</html>");
        addBtn.setFont(AppTheme.FONT_LABEL.deriveFont(Font.BOLD, 12f));
        addBtn.setForeground(AppTheme.ON_PRIMARY);
        addBtn.setBackground(AppTheme.PRIMARY_CONTAINER);
        addBtn.setBorder(new EmptyBorder(7, 16, 7, 16));
        addBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        addBtn.setFocusPainted(false);
        addBtn.addActionListener(e -> onAddNewPartner());

        topPanel.add(searchFieldPanel, BorderLayout.CENTER);
        topPanel.add(addBtn, BorderLayout.EAST);
        add(topPanel, BorderLayout.NORTH);

        // Center / Table Panel
        String[] cols = {"ID", "Tên đối tác", "Số điện thoại"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(tableModel);
        table.setRowHeight(34);
        table.setFont(AppTheme.FONT_BODY_MD);
        table.setBackground(AppTheme.SURFACE_MED);
        table.setForeground(AppTheme.ON_SURFACE);
        table.setGridColor(AppTheme.OUTLINE_VARIANT);
        table.setShowHorizontalLines(true);
        table.setShowVerticalLines(false);
        table.setSelectionBackground(AppTheme.withAlpha(AppTheme.PRIMARY, 40));
        table.setSelectionForeground(AppTheme.ON_SURFACE);
        table.setFocusable(false);

        sorter = new TableRowSorter<>(tableModel);
        table.setRowSorter(sorter);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(AppTheme.OUTLINE_VARIANT, 1));
        scrollPane.getViewport().setBackground(AppTheme.SURFACE_MED);

        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setOpaque(false);
        centerPanel.setBorder(new EmptyBorder(4, 20, 10, 20));
        centerPanel.add(scrollPane, BorderLayout.CENTER);
        add(centerPanel, BorderLayout.CENTER);

        // Footer Panel
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 12));
        footer.setBackground(AppTheme.SURFACE_HIGH);
        footer.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, AppTheme.OUTLINE_VARIANT));

        JButton cancelBtn = new JButton("Hủy");
        cancelBtn.setFont(AppTheme.FONT_LABEL);
        cancelBtn.setForeground(AppTheme.ON_SURFACE_VAR);
        cancelBtn.setBackground(AppTheme.SURFACE_HIGH);
        cancelBtn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(AppTheme.OUTLINE_VARIANT, 1),
                new EmptyBorder(7, 20, 7, 20)));
        cancelBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        cancelBtn.setFocusPainted(false);
        cancelBtn.addActionListener(e -> dispose());

        JButton okBtn = new JButton("<html><font face='Segoe UI Emoji'>✔️</font> Chọn</html>");
        okBtn.setFont(AppTheme.FONT_LABEL.deriveFont(Font.BOLD, 12f));
        okBtn.setForeground(AppTheme.ON_PRIMARY);
        okBtn.setBackground(AppTheme.PRIMARY_CONTAINER);
        okBtn.setBorder(new EmptyBorder(8, 22, 8, 22));
        okBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        okBtn.setFocusPainted(false);
        okBtn.addActionListener(e -> confirmSelection());

        footer.add(cancelBtn);
        footer.add(okBtn);
        add(footer, BorderLayout.SOUTH);

        // Listeners
        tfSearch.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { filter(); }
            @Override public void removeUpdate(DocumentEvent e) { filter(); }
            @Override public void changedUpdate(DocumentEvent e) { filter(); }
        });

        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    confirmSelection();
                }
            }
        });

        // Enter on search field selects first match
        tfSearch.addActionListener(e -> {
            if (table.getRowCount() > 0) {
                table.setRowSelectionInterval(0, 0);
                confirmSelection();
            }
        });
    }

    private void filter() {
        String text = tfSearch.getText().trim();
        if (text.isEmpty()) {
            sorter.setRowFilter(null);
        } else {
            sorter.setRowFilter(RowFilter.regexFilter("(?i)" + text));
        }
    }

    private void loadData() {
        tableModel.setRowCount(0);
        try {
            Connection conn = DatabaseManager.getInstance().getConnection();
            String sql = "SELECT id, ten, sdt FROM doi_tac ORDER BY ten";
            try (Statement st = conn.createStatement();
                 ResultSet rs = st.executeQuery(sql)) {
                while (rs.next()) {
                    tableModel.addRow(new Object[]{
                            rs.getInt("id"),
                            rs.getString("ten"),
                            rs.getString("sdt") != null ? rs.getString("sdt") : ""
                    });
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }


    private void onAddNewPartner() {
        PartnerDialog dialog = new PartnerDialog((Frame) getOwner(), partnerType);
        dialog.setVisible(true);
        if (dialog.isSaved()) {
            Integer newId = dialog.getGeneratedId();
            loadData();
            if (newId != null) {
                // Find and select the newly added row
                for (int i = 0; i < tableModel.getRowCount(); i++) {
                    if (newId.equals(tableModel.getValueAt(i, 0))) {
                        int viewRow = table.convertRowIndexToView(i);
                        if (viewRow >= 0) {
                            table.setRowSelectionInterval(viewRow, viewRow);
                            table.scrollRectToVisible(table.getCellRect(viewRow, 0, true));
                        }
                        break;
                    }
                }
            }
        }
    }

    private void confirmSelection() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một đối tác từ danh sách.", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int modelRow = table.convertRowIndexToModel(selectedRow);
        selectedId = (Integer) tableModel.getValueAt(modelRow, 0);
        selectedName = (String) tableModel.getValueAt(modelRow, 1);
        selectedSdt = (String) tableModel.getValueAt(modelRow, 2);
        confirmed = true;
        dispose();
    }
}
