package com.brewmaster.components;

import com.brewmaster.theme.AppTheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

/**
 * Component chọn ngày tháng năm (dd/MM/yyyy)
 * Tích hợp FlatLaf trailing component & popup lịch tháng
 */
public class DatePicker extends JPanel {
    private final JTextField tfDate;
    private final JButton btnCalendar;
    private final JPopupMenu popup;
    private LocalDate selectedDate;
    private LocalDate displayDate;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private final List<ActionListener> listeners = new ArrayList<>();

    public DatePicker() {
        setLayout(new BorderLayout());
        setOpaque(false);

        tfDate = new JTextField();
        tfDate.setFont(AppTheme.FONT_BODY_MD);
        tfDate.putClientProperty("JTextField.placeholderText", "dd/MM/yyyy");
        tfDate.putClientProperty("JTextField.showClearButton", true);

        // Nút lịch emoji calendar
        btnCalendar = new JButton("📅");
        btnCalendar.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 13));
        btnCalendar.setBorder(new EmptyBorder(0, 6, 0, 6));
        btnCalendar.setFocusPainted(false);
        btnCalendar.setContentAreaFilled(false);
        btnCalendar.setBorderPainted(false);
        btnCalendar.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        // FlatLaf trailing component - wrap in JPanel to prevent FlatLaf styling it as a toolbar button
        JPanel btnWrapper = new JPanel(new BorderLayout());
        btnWrapper.setOpaque(false);
        btnWrapper.add(btnCalendar, BorderLayout.CENTER);
        tfDate.putClientProperty("JTextField.trailingComponent", btnWrapper);

        add(tfDate, BorderLayout.CENTER);

        // Tạo Popup Menu để hiển thị bảng lịch
        popup = new JPopupMenu();
        popup.setBorder(BorderFactory.createLineBorder(AppTheme.OUTLINE, 1));

        btnCalendar.addActionListener(e -> showPopup());

        // Lắng nghe khi người dùng gõ tay hoặc click nút Clear
        tfDate.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                parseInputDate();
            }
        });
        tfDate.addActionListener(e -> {
            parseInputDate();
            fireActionEvent();
        });

        tfDate.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { checkChange(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { checkChange(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { checkChange(); }

            private void checkChange() {
                String text = tfDate.getText().trim();
                if (text.isEmpty()) {
                    if (selectedDate != null) {
                        selectedDate = null;
                        fireActionEvent();
                    }
                    return;
                }
                if (text.length() == 10) {
                    try {
                        LocalDate date = LocalDate.parse(text, formatter);
                        if (!date.equals(selectedDate)) {
                            selectedDate = date;
                            fireActionEvent();
                        }
                    } catch (DateTimeParseException ignored) {
                        // Bỏ qua lỗi định dạng khi đang gõ dở
                    }
                }
            }
        });
    }

    public LocalDate getValue() {
        return selectedDate;
    }

    public void setValue(LocalDate date) {
        this.selectedDate = date;
        if (date != null) {
            tfDate.setText(date.format(formatter));
        } else {
            tfDate.setText("");
        }
    }

    public void addActionListener(ActionListener l) {
        listeners.add(l);
    }

    public void removeActionListener(ActionListener l) {
        listeners.remove(l);
    }

    private void fireActionEvent() {
        ActionEvent event = new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "dateChanged");
        for (ActionListener l : listeners) {
            l.actionPerformed(event);
        }
    }

    private void parseInputDate() {
        String text = tfDate.getText().trim();
        if (text.isEmpty()) {
            if (selectedDate != null) {
                selectedDate = null;
                fireActionEvent();
            }
            return;
        }
        try {
            LocalDate date = LocalDate.parse(text, formatter);
            if (!date.equals(selectedDate)) {
                selectedDate = date;
                fireActionEvent();
            }
        } catch (DateTimeParseException e) {
            // Định dạng sai -> Reset về ngày đã chọn trước đó (hoặc để trống)
            if (selectedDate != null) {
                tfDate.setText(selectedDate.format(formatter));
            } else {
                tfDate.setText("");
            }
        }
    }

    private void showPopup() {
        displayDate = (selectedDate != null) ? selectedDate : LocalDate.now();
        popup.removeAll();
        popup.add(buildCalendarPanelInline(popup));
        popup.show(tfDate, 0, tfDate.getHeight());
    }

    private JPanel buildCalendarPanelInline(JPopupMenu popup) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(AppTheme.SURFACE_LOW);
        panel.setPreferredSize(new Dimension(250, 240));
        panel.setBorder(new EmptyBorder(8, 8, 8, 8));

        // Header điều hướng tháng năm
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(0, 0, 8, 0));

        JButton btnPrev = new JButton("◀");
        btnPrev.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        btnPrev.setFocusPainted(false);
        btnPrev.setContentAreaFilled(false);
        btnPrev.setBorderPainted(false);
        btnPrev.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnPrev.setForeground(AppTheme.ON_SURFACE);

        JButton btnNext = new JButton("▶");
        btnNext.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        btnNext.setFocusPainted(false);
        btnNext.setContentAreaFilled(false);
        btnNext.setBorderPainted(false);
        btnNext.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnNext.setForeground(AppTheme.ON_SURFACE);

        JLabel lblMonthYear = new JLabel("", SwingConstants.CENTER);
        lblMonthYear.setFont(AppTheme.FONT_TITLE_SM);
        lblMonthYear.setForeground(AppTheme.ON_SURFACE);

        JPanel grid = new JPanel(new GridLayout(7, 7, 2, 2));
        grid.setOpaque(false);

        Runnable refreshGrid = new Runnable() {
            @Override
            public void run() {
                grid.removeAll();
                lblMonthYear.setText("Tháng " + displayDate.getMonthValue() + " / " + displayDate.getYear());

                // Header các thứ
                String[] dayHeaders = { "T2", "T3", "T4", "T5", "T6", "T7", "CN" };
                for (String dh : dayHeaders) {
                    JLabel lbl = new JLabel(dh, SwingConstants.CENTER);
                    lbl.setFont(AppTheme.FONT_LABEL);
                    lbl.setForeground(AppTheme.ON_SURFACE_VAR);
                    grid.add(lbl);
                }

                // Tính toán các ngày trong tháng
                LocalDate firstOfMonth = displayDate.withDayOfMonth(1);
                int startOffset = firstOfMonth.getDayOfWeek().getValue() - 1; // 0=T2, 6=CN
                int daysInMonth = displayDate.lengthOfMonth();

                LocalDate prevMonth = displayDate.minusMonths(1);
                int daysInPrevMonth = prevMonth.lengthOfMonth();

                LocalDate today = LocalDate.now();

                for (int i = 0; i < 42; i++) {
                    LocalDate cellDate;
                    boolean isCurrentMonth = true;

                    if (i < startOffset) {
                        int day = daysInPrevMonth - startOffset + i + 1;
                        cellDate = prevMonth.withDayOfMonth(day);
                        isCurrentMonth = false;
                    } else if (i < startOffset + daysInMonth) {
                        int day = i - startOffset + 1;
                        cellDate = displayDate.withDayOfMonth(day);
                    } else {
                        int day = i - startOffset - daysInMonth + 1;
                        cellDate = displayDate.plusMonths(1).withDayOfMonth(day);
                        isCurrentMonth = false;
                    }

                    JButton btnDay = new JButton(String.valueOf(cellDate.getDayOfMonth()));
                    btnDay.setFont(AppTheme.FONT_BODY_SM);
                    btnDay.setFocusPainted(false);
                    btnDay.setMargin(new Insets(2, 2, 2, 2));
                    btnDay.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                    btnDay.setBorderPainted(false);

                    // Styling ngày đặc biệt dùng cơ chế FlatLaf.style đồng bộ và tối ưu
                    if (cellDate.equals(selectedDate)) {
                        btnDay.putClientProperty("FlatLaf.style", "background: " + AppTheme.toHex(AppTheme.PRIMARY) + "; foreground: " + AppTheme.toHex(AppTheme.ON_PRIMARY) + "; hoverBackground: " + AppTheme.toHex(AppTheme.PRIMARY_DARK) + "; focusWidth: 0;");
                    } else if (cellDate.equals(today)) {
                        btnDay.putClientProperty("FlatLaf.style", "background: " + AppTheme.toHex(AppTheme.SECONDARY_CONT) + "; foreground: " + AppTheme.toHex(AppTheme.SECONDARY) + "; hoverBackground: " + AppTheme.toHex(AppTheme.SURFACE_HIGHEST) + "; focusWidth: 0;");
                        btnDay.setBorder(BorderFactory.createLineBorder(AppTheme.SECONDARY, 1));
                        btnDay.setBorderPainted(true);
                    } else {
                        if (isCurrentMonth) {
                            btnDay.putClientProperty("FlatLaf.style", "background: " + AppTheme.toHex(AppTheme.SURFACE_LOW) + "; foreground: " + AppTheme.toHex(AppTheme.ON_SURFACE) + "; hoverBackground: " + AppTheme.toHex(AppTheme.SURFACE_HIGHEST) + "; focusWidth: 0;");
                        } else {
                            btnDay.putClientProperty("FlatLaf.style", "background: " + AppTheme.toHex(AppTheme.SURFACE_LOW) + "; foreground: " + AppTheme.toHex(AppTheme.OUTLINE) + "; hoverBackground: " + AppTheme.toHex(AppTheme.SURFACE_HIGHEST) + "; focusWidth: 0;");
                        }
                    }

                    btnDay.addActionListener(e -> {
                        setValue(cellDate);
                        popup.setVisible(false);
                        fireActionEvent();
                    });

                    grid.add(btnDay);
                }
                grid.revalidate();
                grid.repaint();
            }
        };

        btnPrev.addActionListener(e -> {
            displayDate = displayDate.minusMonths(1);
            refreshGrid.run();
        });

        btnNext.addActionListener(e -> {
            displayDate = displayDate.plusMonths(1);
            refreshGrid.run();
        });

        refreshGrid.run();

        header.add(btnPrev, BorderLayout.WEST);
        header.add(lblMonthYear, BorderLayout.CENTER);
        header.add(btnNext, BorderLayout.EAST);
        panel.add(header, BorderLayout.NORTH);
        panel.add(grid, BorderLayout.CENTER);
        return panel;
    }

    public static void setupDatePicker(JTextField tf, boolean includeTime) {
        JButton btnCalendar = new JButton("📅");
        btnCalendar.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 13));
        btnCalendar.setBorder(new EmptyBorder(0, 6, 0, 6));
        btnCalendar.setFocusPainted(false);
        btnCalendar.setContentAreaFilled(false);
        btnCalendar.setBorderPainted(false);
        btnCalendar.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        JPanel btnWrapper = new JPanel(new BorderLayout());
        btnWrapper.setOpaque(false);
        btnWrapper.add(btnCalendar, BorderLayout.CENTER);
        tf.putClientProperty("JTextField.trailingComponent", btnWrapper);

        JPopupMenu popup = new JPopupMenu();
        popup.setBorder(BorderFactory.createLineBorder(AppTheme.OUTLINE, 1));

        btnCalendar.addActionListener(e -> {
            if (!tf.isEditable() || !tf.isEnabled()) {
                return;
            }
            String text = tf.getText().trim();
            LocalDate initialDate = LocalDate.now();
            String timePart = includeTime ? " 12:00" : "";
            
            if (!text.isEmpty()) {
                try {
                    if (includeTime) {
                        String[] parts = text.split(" ");
                        initialDate = LocalDate.parse(parts[0], DateTimeFormatter.ofPattern("dd/MM/yyyy"));
                        if (parts.length > 1) {
                            timePart = " " + parts[1];
                        }
                    } else {
                        initialDate = LocalDate.parse(text, DateTimeFormatter.ofPattern("dd/MM/yyyy"));
                    }
                } catch (Exception ex) {
                    initialDate = LocalDate.now();
                }
            } else {
                if (includeTime) {
                    timePart = " " + java.time.LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"));
                }
            }

            final String finalTimePart = timePart;
            
            JPanel calPanel = buildCalendarPanelForTextField(tf, initialDate, finalTimePart, includeTime, popup);
            popup.removeAll();
            popup.add(calPanel);
            popup.show(tf, 0, tf.getHeight());
        });
    }

    private static JPanel buildCalendarPanelForTextField(JTextField tf, LocalDate initialDate, String finalTimePart, boolean includeTime, JPopupMenu popup) {
        final LocalDate[] displayDateHolder = new LocalDate[] { initialDate };
        
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(AppTheme.SURFACE_LOW);
        panel.setPreferredSize(new Dimension(250, 240));
        panel.setBorder(new EmptyBorder(8, 8, 8, 8));

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(0, 0, 8, 0));

        JButton btnPrev = new JButton("◀");
        btnPrev.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        btnPrev.setFocusPainted(false);
        btnPrev.setContentAreaFilled(false);
        btnPrev.setBorderPainted(false);
        btnPrev.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnPrev.setForeground(AppTheme.ON_SURFACE);

        JButton btnNext = new JButton("▶");
        btnNext.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        btnNext.setFocusPainted(false);
        btnNext.setContentAreaFilled(false);
        btnNext.setBorderPainted(false);
        btnNext.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnNext.setForeground(AppTheme.ON_SURFACE);

        JLabel lblMonthYear = new JLabel("", SwingConstants.CENTER);
        lblMonthYear.setFont(AppTheme.FONT_TITLE_SM);
        lblMonthYear.setForeground(AppTheme.ON_SURFACE);

        JPanel grid = new JPanel(new GridLayout(7, 7, 2, 2));
        grid.setOpaque(false);

        Runnable refreshGrid = new Runnable() {
            @Override
            public void run() {
                grid.removeAll();
                LocalDate displayDate = displayDateHolder[0];
                lblMonthYear.setText("Tháng " + displayDate.getMonthValue() + " / " + displayDate.getYear());

                String[] dayHeaders = { "T2", "T3", "T4", "T5", "T6", "T7", "CN" };
                for (String dh : dayHeaders) {
                    JLabel lbl = new JLabel(dh, SwingConstants.CENTER);
                    lbl.setFont(AppTheme.FONT_LABEL);
                    lbl.setForeground(AppTheme.ON_SURFACE_VAR);
                    grid.add(lbl);
                }

                LocalDate firstOfMonth = displayDate.withDayOfMonth(1);
                int startOffset = firstOfMonth.getDayOfWeek().getValue() - 1;
                int daysInMonth = displayDate.lengthOfMonth();

                LocalDate prevMonth = displayDate.minusMonths(1);
                int daysInPrevMonth = prevMonth.lengthOfMonth();

                LocalDate today = LocalDate.now();

                for (int i = 0; i < 42; i++) {
                    LocalDate cellDate;
                    boolean isCurrentMonth = true;

                    if (i < startOffset) {
                        int day = daysInPrevMonth - startOffset + i + 1;
                        cellDate = prevMonth.withDayOfMonth(day);
                        isCurrentMonth = false;
                    } else if (i < startOffset + daysInMonth) {
                        int day = i - startOffset + 1;
                        cellDate = displayDate.withDayOfMonth(day);
                    } else {
                        int day = i - startOffset - daysInMonth + 1;
                        cellDate = displayDate.plusMonths(1).withDayOfMonth(day);
                        isCurrentMonth = false;
                    }

                    JButton btnDay = new JButton(String.valueOf(cellDate.getDayOfMonth()));
                    btnDay.setFont(AppTheme.FONT_BODY_SM);
                    btnDay.setFocusPainted(false);
                    btnDay.setMargin(new Insets(2, 2, 2, 2));
                    btnDay.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                    btnDay.setBorderPainted(false);

                    if (cellDate.equals(initialDate)) {
                        btnDay.putClientProperty("FlatLaf.style", "background: " + AppTheme.toHex(AppTheme.PRIMARY) + "; foreground: " + AppTheme.toHex(AppTheme.ON_PRIMARY) + "; hoverBackground: " + AppTheme.toHex(AppTheme.PRIMARY_DARK) + "; focusWidth: 0;");
                    } else if (cellDate.equals(today)) {
                        btnDay.putClientProperty("FlatLaf.style", "background: " + AppTheme.toHex(AppTheme.SECONDARY_CONT) + "; foreground: " + AppTheme.toHex(AppTheme.SECONDARY) + "; hoverBackground: " + AppTheme.toHex(AppTheme.SURFACE_HIGHEST) + "; focusWidth: 0;");
                        btnDay.setBorder(BorderFactory.createLineBorder(AppTheme.SECONDARY, 1));
                        btnDay.setBorderPainted(true);
                    } else {
                        if (isCurrentMonth) {
                            btnDay.putClientProperty("FlatLaf.style", "background: " + AppTheme.toHex(AppTheme.SURFACE_LOW) + "; foreground: " + AppTheme.toHex(AppTheme.ON_SURFACE) + "; hoverBackground: " + AppTheme.toHex(AppTheme.SURFACE_HIGHEST) + "; focusWidth: 0;");
                        } else {
                            btnDay.putClientProperty("FlatLaf.style", "background: " + AppTheme.toHex(AppTheme.SURFACE_LOW) + "; foreground: " + AppTheme.toHex(AppTheme.OUTLINE) + "; hoverBackground: " + AppTheme.toHex(AppTheme.SURFACE_HIGHEST) + "; focusWidth: 0;");
                        }
                    }

                    btnDay.addActionListener(e -> {
                        String formattedDate = cellDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
                        tf.setText(formattedDate + finalTimePart);
                        popup.setVisible(false);
                    });

                    grid.add(btnDay);
                }
                grid.revalidate();
                grid.repaint();
            }
        };

        btnPrev.addActionListener(e -> {
            displayDateHolder[0] = displayDateHolder[0].minusMonths(1);
            refreshGrid.run();
        });

        btnNext.addActionListener(e -> {
            displayDateHolder[0] = displayDateHolder[0].plusMonths(1);
            refreshGrid.run();
        });

        refreshGrid.run();

        header.add(btnPrev, BorderLayout.WEST);
        header.add(lblMonthYear, BorderLayout.CENTER);
        header.add(btnNext, BorderLayout.EAST);
        
        panel.add(header, BorderLayout.NORTH);
        panel.add(grid, BorderLayout.CENTER);
        return panel;
    }
}
