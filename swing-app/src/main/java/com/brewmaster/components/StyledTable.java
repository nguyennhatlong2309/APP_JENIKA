package com.brewmaster.components;

import com.brewmaster.theme.AppTheme;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;

/**
 * Dark-themed JTable với header, alternating hover, và status badges
 */
public class StyledTable extends JTable {

    public StyledTable(TableModel model) {
        super(model);
        applyStyle();
    }

    public StyledTable(Object[][] data, String[] cols) {
        super(data, cols);
        applyStyle();
    }

    private void applyStyle() {
        setBackground(AppTheme.SURFACE_LOW);
        setForeground(AppTheme.ON_SURFACE);
        setSelectionBackground(AppTheme.withAlpha(AppTheme.PRIMARY, 40));
        setSelectionForeground(AppTheme.ON_SURFACE);
        setGridColor(AppTheme.OUTLINE_VARIANT);
        setShowGrid(false);
        setIntercellSpacing(new Dimension(0, 0));
        setRowHeight(AppTheme.ROW_HEIGHT);
        setFont(AppTheme.FONT_BODY_MD);
        setShowHorizontalLines(true);
        setFillsViewportHeight(true);
        setAutoCreateRowSorter(true);

        // Header style
        JTableHeader header = getTableHeader();
        header.setBackground(AppTheme.SURFACE_HIGH);
        header.setForeground(AppTheme.ON_SURFACE_VAR);
        header.setFont(AppTheme.FONT_LABEL);
        header.setPreferredSize(new Dimension(0, 44));
        header.setReorderingAllowed(false);
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, AppTheme.OUTLINE_VARIANT));

        // Custom header renderer
        DefaultTableCellRenderer headerRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v,
                    boolean sel, boolean foc, int r, int c) {
                super.getTableCellRendererComponent(t, v, sel, foc, r, c);
                setBackground(AppTheme.SURFACE_HIGH);
                setForeground(AppTheme.ON_SURFACE_VAR);
                setFont(AppTheme.FONT_LABEL);
                setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 1, 0, AppTheme.OUTLINE_VARIANT),
                    BorderFactory.createEmptyBorder(0, 12, 0, 12)
                ));
                setHorizontalAlignment(LEFT);
                if (v != null) setText(v.toString().toUpperCase());
                return this;
            }
        };
        for (int i = 0; i < getColumnCount(); i++) {
            getColumnModel().getColumn(i).setHeaderRenderer(headerRenderer);
        }

        // Default cell renderer with row alternation
        setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v,
                    boolean sel, boolean foc, int row, int col) {
                super.getTableCellRendererComponent(t, v, sel, foc, row, col);
                setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 1, 0, AppTheme.withAlpha(AppTheme.OUTLINE_VARIANT, 80)),
                    BorderFactory.createEmptyBorder(0, 12, 0, 12)
                ));
                setFont(AppTheme.FONT_BODY_MD);

                if (sel) {
                    setBackground(AppTheme.withAlpha(AppTheme.PRIMARY, 35));
                    setForeground(AppTheme.ON_SURFACE);
                } else {
                    setBackground(AppTheme.SURFACE_LOW);
                    setForeground(AppTheme.ON_SURFACE);
                }
                return this;
            }
        });
    }

    @Override
    public Dimension getPreferredSize() {
        Dimension d = super.getPreferredSize();
        if (getParent() instanceof JViewport) {
            int totalPreferredWidth = 0;
            for (int i = 0; i < getColumnCount(); i++) {
                totalPreferredWidth += getColumnModel().getColumn(i).getPreferredWidth();
            }
            d.width = Math.max(d.width, totalPreferredWidth);
        }
        return d;
    }

    @Override
    public boolean getScrollableTracksViewportWidth() {
        if (getParent() instanceof JViewport) {
            return getPreferredSize().width < getParent().getWidth();
        }
        return super.getScrollableTracksViewportWidth();
    }

    /** Tạo JScrollPane wrapper cho table với dark styling */
    public JScrollPane wrapInScrollPane() {
        JScrollPane scroll = new JScrollPane(this);
        scroll.setBackground(AppTheme.SURFACE_LOW);
        scroll.getViewport().setBackground(AppTheme.SURFACE_LOW);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.setOpaque(true);
        return scroll;
    }
}
