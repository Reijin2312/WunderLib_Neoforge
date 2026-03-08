package de.ambertation.wunderlib.ui.layout.components;

import de.ambertation.wunderlib.ui.ColorHelper;
import de.ambertation.wunderlib.ui.layout.values.Value;

import net.minecraft.network.chat.Component;


import java.util.LinkedList;
import java.util.List;

public class Tabs extends AbstractVerticalStack<Tabs> {
    @FunctionalInterface
    public interface OnPageChange {
        void now(Tabs tabs, int pageIndex);
    }

    private final HorizontalStack buttons;
    private final Container content;

    private final List<Container> pageList = new LinkedList<>();
    private final List<Button> buttonList = new LinkedList<>();

    private int visiblePageIndex = 0;

    private OnPageChange onPageChange;


    public Tabs(Value width, Value height) {
        super(width, height);

        buttons = new HorizontalStack(Value.fill(), Value.fit());
        content = new Container(Value.fill(), Value.fill());

        this.add(buttons);
        this.add(content);

        setBackgroundColor(ColorHelper.CONTAINER_BACKGROUND);
        setPadding(4);
    }

    public int pageCount() {
        return buttonList.size();
    }

    @Override
    public int size() {
        return buttonList.size();
    }

    @Override
    public boolean isEmpty() {
        return buttonList.isEmpty();
    }

    public Tabs addPage(Component title, LayoutComponent<?, ?> content) {
        final Container c = new Container(Value.fill(), Value.fill());
        c.addChild(content);
        this.content.addChild(c);
        pageList.add(c);
        if (!buttons.isEmpty()) buttons.addSpacer(4);
        Button b = new Button(Value.fit(), Value.fit(), title).alignBottom();
        final int index = buttonList.size();
        b.onPress((bt) -> {
            for (Container cc : pageList) {
                cc.setVisible(cc == c);
            }

            for (Button bb : buttonList) {
                bb.glow = bb == b;
            }

            visiblePageIndex = index;

            if (onPageChange != null) {
                onPageChange.now(this, index);
            }
        });
        buttons.add(b);
        buttonList.add(b);


        return this;
    }

    public Tabs onPageChange(OnPageChange e) {
        this.onPageChange = e;
        return this;
    }

    public Button getButton(int idx) {
        return buttonList.get(idx);
    }

    public Tabs setBackgroundColor(int color) {
        content.setBackgroundColor(color);
        return this;
    }

    public int getBackgroundColor() {
        return content.getBackgroundColor();
    }

    public Tabs setOutlineColor(int color) {
        content.setOutlineColor(color);
        return this;
    }

    public int getOutlineColor() {
        return content.getOutlineColor();
    }

    public Tabs setPadding(int padding) {
        content.setPadding(padding);
        return this;
    }

    public Tabs setPadding(int left, int top, int right, int bottom) {
        content.setPadding(left, top, right, bottom);
        return this;
    }


    @Override
    public int getContentWidth() {
        return content.getContentWidth();
    }

    public Tabs selectPage(int idx) {
        for (int i = 0; i < pageList.size(); i++) {
            pageList.get(i).setVisible(i == idx);
            buttonList.get(i).glow = i == idx;
        }
        visiblePageIndex = idx;
        return this;
    }

    public int getSelectedPage() {
        return visiblePageIndex;
    }

    public Tabs addComponent(LayoutComponent<?, ?> title) {
        buttons.add(title);
        return this;
    }


    @Override
    protected void onBoundsChanged() {
        super.onBoundsChanged();
        selectPage(visiblePageIndex);
    }

    @Override
    public Tabs addFiller() {
        buttons.addFiller();
        return this;
    }

    @Override
    public Tabs addSpacer(int size) {
        buttons.addSpacer(size);
        return this;
    }

    @Override
    public Tabs addSpacer(float percentage) {
        buttons.addSpacer(percentage);
        return this;
    }
}
