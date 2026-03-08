package de.ambertation.wunderlib.ui.vanilla;

import de.ambertation.wunderlib.configs.AbstractConfig;
import de.ambertation.wunderlib.ui.ColorHelper;
import de.ambertation.wunderlib.ui.layout.components.*;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;


import java.util.LinkedList;
import java.util.List;
import org.jetbrains.annotations.Nullable;

public class ConfigScreen extends LayoutScreenWithIcon {
    protected final List<AbstractConfig<?>> configFiles;
    protected final List<OnCheckboxChangeEvent> checkboxListeners;

    public ConfigScreen(
            Component component,
            Identifier icon, List<AbstractConfig<?>> configs
    ) {
        this(EMPTY_SCREEN, icon, component, configs);
    }

    public ConfigScreen(
            @Nullable Screen parent,
            Identifier icon, Component component, List<AbstractConfig<?>> configs
    ) {
        this(setScreenOnClose(parent), icon, component, configs);
    }

    public ConfigScreen(
            @Nullable Screen parent,
            Identifier icon,
            Component component,
            List<AbstractConfig<?>> configs,
            int topPadding,
            int bottomPadding,
            int sidePadding
    ) {
        this(setScreenOnClose(parent), icon, component, configs, topPadding, bottomPadding, sidePadding);
    }

    public ConfigScreen(
            Runnable onClose,
            Identifier icon, Component component, List<AbstractConfig<?>> configs
    ) {
        this(onClose, icon, component, configs, 20, 10, 20, 15);
    }

    public ConfigScreen(
            @Nullable Runnable onClose,
            Identifier icon,
            Component component,
            List<AbstractConfig<?>> configs,
            int topPadding,
            int bottomPadding,
            int sidePadding
    ) {
        this(onClose, icon, component, configs, topPadding, bottomPadding, sidePadding, 15);
    }

    public ConfigScreen(
            @Nullable Runnable onClose,
            Identifier icon,
            Component component,
            List<AbstractConfig<?>> configs,
            int topPadding,
            int bottomPadding,
            int sidePadding,
            int titleSpacing
    ) {
        super(onClose, icon, component, topPadding, bottomPadding, sidePadding, titleSpacing);
        this.configFiles = configs;
        this.checkboxListeners = new LinkedList<>();
    }

    @Override
    protected LayoutComponent<?, ?> initContent() {
        VerticalStack content = new VerticalStack(fill(), fit()).centerHorizontal();
        content.addSpacer(4);
        content.add(fromConfigFiles(this.configFiles));

        VerticalStack verticalScroll = new VerticalStack(fill(), fill()).setDebugName("vertical scroll");
        verticalScroll.addScrollable(content);

        Container main = new Container(fill(), fill()).addChild(verticalScroll);
        main.setBackgroundColor(0x77000000);
        main.setPadding(4, 0, 0, 0);

        HorizontalStack buttons = new HorizontalStack(fill(), fixed(20)).setDebugName("buttons");
        buttons.addFiller();
        buttons.addButton(fit(), fit(), CommonComponents.GUI_DONE).onPress((bt) -> this.closeScreen());

        VerticalStack all = new VerticalStack(fill(), fill()).setDebugName("all");
        all.add(main);
        all.addSpacer(3);
        all.add(buttons);

        return all;
    }

    protected LayoutComponent<?, ?> fromConfigFiles(List<AbstractConfig<?>> files) {
        final List<AbstractConfig.Group> groups = AbstractConfig.getAllGroups(files);

        final VerticalStack content = new VerticalStack(fill(), fit())
                .alignTop()
                .setDebugName("Config - " + AbstractConfig.getAllCategories(files));

        for (AbstractConfig.Group group : groups) {
            addGroupHeader(content, group);
            content.indent(8).add(fromConfigGroup(group, files));
            addGroupFooter(content, group);
        }

        return content;
    }

    protected void addGroupHeader(VerticalStack content, AbstractConfig.Group group) {
        final Component text = getGroupTitle(group);
        Style s = text.getStyle().withBold(true);

        content.add(new Text(fit(), fit(), text.copy().setStyle(s)).alignLeft());
        content.addSpacer(4);
    }

    protected void addGroupFooter(VerticalStack content, AbstractConfig.Group group) {
        content.addSpacer(8);
    }

    protected LayoutComponent<?, ?> finalizeGroupContent(AbstractConfig.Group group, VerticalStack content) {
        return content.alignLeft();
    }


    protected LayoutComponent<?, ?> fromConfigGroup(AbstractConfig.Group group, List<AbstractConfig<?>> configFiles) {
        final List<AbstractConfig<?>.Value<?, ?>> sortedValues = AbstractConfig.getAllVisibleValues(group, configFiles);
        final VerticalStack content = new VerticalStack(fill(), fit())
                .setDebugName("Group - " + group.title());

        final List<AbstractConfig<?>.Value<?, ?>> toplevelValues = getDependantValues(null, sortedValues);
        fromValues(group, sortedValues, toplevelValues, content);

        return finalizeGroupContent(group, content);
    }

    private void fromValues(
            AbstractConfig.Group group,
            List<AbstractConfig<?>.Value<?, ?>> allValues,
            List<AbstractConfig<?>.Value<?, ?>> values,
            VerticalStack content
    ) {
        for (AbstractConfig<?>.Value<?, ?> value : values) {
            final LayoutElement element = fromConfig(group, value);
            if (element != null) {
                content.add(element.component);
                content.addSpacer(2);

                final List<AbstractConfig<?>.Value<?, ?>> dependantValues = getDependantValues(value, allValues);
                if (!dependantValues.isEmpty()) {
                    final VerticalStack subContent = new VerticalStack(fill(), fit())
                            .setDebugName("Dependencies - " + value.token.path() + "." + value.token.key());
                    fromValues(group, allValues, dependantValues, subContent);

                    final HorizontalStack indent = new HorizontalStack(fill(), fit())
                            .setDebugName("Dependency - " + value.token.path() + "." + value.token.key());
                    indent.addSpacer(element.bestIndent);
                    indent.add(subContent);
                    content.add(indent);
                    content.addSpacer(8);
                }
            }
        }

    }

    protected LayoutElement fromConfig(AbstractConfig.Group group, AbstractConfig<?>.Value<?, ?> value) {
        if (value instanceof AbstractConfig<?>.BooleanValue b) {
            return fromBooleanConfig(group, b);
        }
        if (value instanceof AbstractConfig<?>.FloatValue f) {
            return fromFloatConfig(group, f);
        }
        if (value instanceof AbstractConfig<?>.IntValue f) {
            return fromIntConfig(group, f);
        }
        return null;
    }

    protected LayoutElement fromIntConfig(AbstractConfig.Group group, AbstractConfig<?>.IntValue value) {
        Component desc = getValueDescription(value);
        final VerticalStack stack = new VerticalStack(fill(), fit());
        final Range<Integer> slider = stack
                .addRange(fill(), fit(), getValueTitle(value), value.getMin(), value.getMax(), value.getRaw())
                .onChange((sl, f) -> {
                    value.set(f);
                    onChangeIntRange(value, sl, f);
                })
                .setDebugName("Config - " + value.token.path() + "." + value.token.key());

        if (value.hasDependency()) {
            checkboxListeners.add((changedValue, cb, state) -> {
                if (value.getDependency().equals(changedValue)) {
                    slider.setEnabled(state);
                }
            });
        }

        if (desc != null) {
            var indented = new HorizontalStack(fill(), fit())
                    .setDebugName("Config - " + value.token.path() + "." + value.token.key() + ".descIndent");
            indented.addSpacer(2);
            indented.addMultilineText(fill(), fit(), MultiLineText.parse(desc))
                    .alignLeft()
                    .setColor(ColorHelper.GRAY);

            stack.addSpacer(2);
            stack.add(indented);
            stack.addSpacer(8);
        }

        return new LayoutElement(stack, 8);
    }

    protected LayoutElement fromFloatConfig(AbstractConfig.Group group, AbstractConfig<?>.FloatValue value) {
        Component desc = getValueDescription(value);
        final VerticalStack stack = new VerticalStack(fill(), fit());
        final Range<Float> slider = stack
                .addRange(fill(), fit(), getValueTitle(value), value.getMin(), value.getMax(), value.getRaw())
                .onChange((sl, f) -> {
                    value.set(f);
                    onChangeFloatRange(value, sl, f);
                })
                .setDebugName("Config - " + value.token.path() + "." + value.token.key());

        if (value.hasDependency()) {
            checkboxListeners.add((changedValue, cb, state) -> {
                if (value.getDependency().equals(changedValue)) {
                    slider.setEnabled(state);
                }
            });
        }

        if (desc != null) {
            var indented = new HorizontalStack(fill(), fit())
                    .setDebugName("Config - " + value.token.path() + "." + value.token.key() + ".descIndent");
            indented.addSpacer(2);
            indented.addMultilineText(fill(), fit(), MultiLineText.parse(desc))
                    .alignLeft()
                    .setColor(ColorHelper.GRAY);

            stack.addSpacer(2);
            stack.add(indented);
            stack.addSpacer(8);
        }

        return new LayoutElement(stack, 8);
    }

    protected LayoutElement fromBooleanConfig(AbstractConfig.Group group, AbstractConfig<?>.BooleanValue value) {
        Component desc = getValueDescription(value);
        final Checkbox checkBox = new Checkbox(fit(), fit(), getValueTitle(value), value.getRaw(), desc == null)
                .onChange((cb, b) -> {
                    value.set(b);
                    onChange(value, cb, b);
                })
                .setDebugName("Config - " + value.token.path() + "." + value.token.key());

        if (value.hasDependency()) {
            checkboxListeners.add((changedValue, cb, state) -> {
                if (value.getDependency().equals(changedValue)) {
                    checkBox.setEnabled(state);
                }
            });
        }

        if (desc == null) {
            return new LayoutElement(checkBox, 8);
        } else {
            final VerticalStack descCol = new VerticalStack(fill(), fit())
                    .alignLeft()
                    .setDebugName("Config Labels- " + value.token.path() + "." + value.token.key());
            descCol.addSpacer(0);
            descCol.addText(fit(), fit(), getValueTitle(value)).alignLeft();
            descCol.addSpacer(2);
            descCol.addMultilineText(fill(), fit(), MultiLineText.parse(desc))
                   .alignLeft()
                   .setColor(ColorHelper.GRAY);
            descCol.addSpacer(4);

            final HorizontalStack row = new HorizontalStack(fill(), fit())
                    .setDebugName("Config - " + value.token.path() + "." + value.token.key());
            row.add(checkBox);
            //row.addSpacer(2);
            row.add(descCol);


            return new LayoutElement(row, 24);
        }
    }

    protected <T, R extends AbstractConfig<?>.Value<T, R>> Component getGroupTitle(
            AbstractConfig.Group group
    ) {
        return Component.translatable(
                "title.config.group." +
                        group.modID() +
                        "." +
                        group.title()
        );
    }

    protected <T, R extends AbstractConfig<?>.Value<T, R>> Component getValueDescription(
            AbstractConfig<?>.Value<T, R> option
    ) {
        StringBuilder sb = new StringBuilder("description.config.");
        if (option.getParentFile() != null) {
            sb.append(option.getParentFile().category).append(".");
        }
        sb.append(option.token.path()).append(".").append(option.token.key());
        final String key = sb.toString();
        if (!Language.getInstance().has(key)) return null;
        return Component.translatable(key);
    }

    protected <T, R extends AbstractConfig<?>.Value<T, R>> Component getValueTitle(
            AbstractConfig<?>.Value<T, R> option
    ) {
        StringBuilder sb = new StringBuilder("title.config.");
        if (option.getParentFile() != null) {
            sb.append(option.getParentFile().category).append(".");
        }
        sb.append(option.token.path()).append(".").append(option.token.key());

        return Component.translatable(sb.toString());
    }


    protected List<AbstractConfig<?>.Value<?, ?>> getDependantValues(
            AbstractConfig<?>.Value<?, ?> source,
            final List<AbstractConfig<?>.Value<?, ?>> sortedValues
    ) {
        final List<AbstractConfig<?>.Value<?, ?>> dependantValues = new LinkedList<>();
        for (AbstractConfig<?>.Value<?, ?> value : sortedValues) {
            if (value.getDependency() == source) {
                dependantValues.add(value);
            }
        }
        return dependantValues;
    }

    protected void onChange(AbstractConfig<?>.Value<?, ?> value, Checkbox cb, boolean newValue) {
        checkboxListeners.forEach(l -> l.onChange(value, cb, newValue));
    }

    protected void onChangeFloatRange(AbstractConfig<?>.Value<?, ?> value, Range<Float> r, Number newValue) {

    }

    protected void onChangeIntRange(AbstractConfig<?>.Value<?, ?> value, Range<Integer> r, Number newValue) {

    }

    protected record LayoutElement(LayoutComponent<?, ?> component, int bestIndent) {
    }

    @FunctionalInterface
    public interface OnCheckboxChangeEvent {
        void onChange(AbstractConfig<?>.Value<?, ?> value, Checkbox cb, boolean newValue);
    }
}
