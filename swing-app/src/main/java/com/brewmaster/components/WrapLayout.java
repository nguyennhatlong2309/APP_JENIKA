package com.brewmaster.components;

import java.awt.*;

/**
 * Layout Manager tự động xuống dòng và co giãn chiều cao của panel
 * Giải quyết vấn đề FlowLayout bị che mất phần tử khi chiều rộng cửa sổ bị thu nhỏ
 */
public class WrapLayout extends FlowLayout {
    public WrapLayout() {
        super(FlowLayout.LEFT, 10, 5);
    }

    public WrapLayout(int align) {
        super(align);
    }

    public WrapLayout(int align, int hgap, int vgap) {
        super(align, hgap, vgap);
    }

    @Override
    public Dimension preferredLayoutSize(Container target) {
        return layoutSize(target, true);
    }

    @Override
    public Dimension minimumLayoutSize(Container target) {
        Dimension minimum = layoutSize(target, false);
        minimum.width -= (getHgap() + 1);
        return minimum;
    }

    private Dimension layoutSize(Container target, boolean preferred) {
        synchronized (target.getTreeLock()) {
            int targetWidth = target.getSize().width;

            if (targetWidth == 0) {
                Container parent = target.getParent();
                if (parent != null) {
                    targetWidth = parent.getSize().width;
                }
            }

            if (targetWidth == 0) {
                targetWidth = Integer.MAX_VALUE;
            }

            int hgap = getHgap();
            int vgap = getVgap();
            Insets insets = target.getInsets();
            int maxwidth = targetWidth - (insets.left + insets.right + hgap * 2);
            int nmembers = target.getComponentCount();
            int x = 0;
            int y = insets.top + vgap;
            int rowHeight = 0;

            for (int i = 0; i < nmembers; i++) {
                Component m = target.getComponent(i);
                if (m.isVisible()) {
                    Dimension d = preferred ? m.getPreferredSize() : m.getMinimumSize();
                    if (x == 0) {
                        x = d.width;
                        rowHeight = d.height;
                    } else if (x + hgap + d.width <= maxwidth) {
                        x += hgap + d.width;
                        rowHeight = Math.max(rowHeight, d.height);
                    } else {
                        x = d.width;
                        y += vgap + rowHeight;
                        rowHeight = d.height;
                    }
                }
            }
            y += vgap + rowHeight + insets.bottom;
            return new Dimension(targetWidth, y);
        }
    }

    @Override
    public void layoutContainer(Container target) {
        synchronized (target.getTreeLock()) {
            int targetWidth = target.getSize().width;
            if (targetWidth == 0) {
                super.layoutContainer(target);
                return;
            }

            int hgap = getHgap();
            int vgap = getVgap();
            Insets insets = target.getInsets();
            int maxwidth = targetWidth - (insets.left + insets.right + hgap * 2);
            int nmembers = target.getComponentCount();

            int x = insets.left + hgap;
            int y = insets.top + vgap;
            int rowHeight = 0;

            java.util.List<Component> rowComponents = new java.util.ArrayList<>();

            for (int i = 0; i < nmembers; i++) {
                Component m = target.getComponent(i);
                if (m.isVisible()) {
                    Dimension d = m.getPreferredSize();
                    if (rowComponents.isEmpty() || x + d.width <= insets.left + hgap + maxwidth) {
                        rowComponents.add(m);
                        x += d.width + hgap;
                        rowHeight = Math.max(rowHeight, d.height);
                    } else {
                        layoutRow(target, rowComponents, y, rowHeight);
                        rowComponents.clear();
                        rowComponents.add(m);
                        x = insets.left + hgap + d.width + hgap;
                        y += vgap + rowHeight;
                        rowHeight = d.height;
                    }
                }
            }
            if (!rowComponents.isEmpty()) {
                layoutRow(target, rowComponents, y, rowHeight);
            }
        }
    }

    private void layoutRow(Container target, java.util.List<Component> rowComponents, int y, int rowHeight) {
        int hgap = getHgap();
        int x = target.getInsets().left + hgap;

        int alignment = getAlignment();
        if (alignment == FlowLayout.CENTER || alignment == FlowLayout.RIGHT) {
            int totalWidth = 0;
            for (Component m : rowComponents) {
                totalWidth += m.getPreferredSize().width + hgap;
            }
            totalWidth -= hgap;
            int unusedWidth = target.getSize().width - target.getInsets().left - target.getInsets().right - totalWidth;
            if (alignment == FlowLayout.CENTER) {
                x += unusedWidth / 2;
            } else {
                x += unusedWidth;
            }
        }

        for (Component m : rowComponents) {
            Dimension d = m.getPreferredSize();
            int yOffset = (rowHeight - d.height) / 2;
            m.setBounds(x, y + yOffset, d.width, d.height);
            x += d.width + hgap;
        }
    }
}
