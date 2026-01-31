package de.ambertation.wunderlib.configs;


import de.ambertation.wunderlib.WunderLib;
import de.ambertation.wunderlib.utils.Version;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import com.google.gson.*;

import java.util.*;
import java.util.function.Supplier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class AbstractConfig<C extends AbstractConfig<C>> {
    public static final String MODIFY_VERSION = "modify_version";
    public static final String CREATE_VERSION = "create_version";
    static final Gson JSON_BUILDER = new GsonBuilder().setPrettyPrinting()
                                                      .create();
    public final String category;
    public final Identifier location;
    private final List<C.Value<?, ?>> knownValues = new LinkedList<>();
    private JsonObject root;
    private boolean modified;
    private final Version.ModVersionProvider versionProvider;

    public AbstractConfig(Version.ModVersionProvider versionProvider, String category) {
        this(versionProvider, versionProvider.getNamespace(), category);
    }

    public AbstractConfig(Version.ModVersionProvider versionProvider, String namespace, String category) {
        this(versionProvider, versionProvider.mk(category), namespace + "." + category);
    }

    protected AbstractConfig(Version.ModVersionProvider versionProvider, Identifier location, String category) {
        this.category = category;
        this.versionProvider = versionProvider;
        this.location = location;
    }

    void setModified() {
        modified = true;
    }

    public int getMaxOrder() {
        int highestOrder = 0;
        for (AbstractConfig<?>.Value<?, ?> v : knownValues) {
            if (v.order > highestOrder) {
                highestOrder = v.order;
            }
        }
        return highestOrder;
    }

    private void registerValue(C.Value v) {
        knownValues.remove(v);
        knownValues.add(v);
        v.order = getMaxOrder() + 1;
        v.parentFile = (C) this;
    }

    private JsonElement getValue(ConfigToken t, boolean addIfMissing) {
        JsonObject obj = getPathElement(t.path, addIfMissing);
        if (!obj.has(t.key)) return null;

        return obj.get(t.key);
    }

    private void setValue(ConfigToken t, JsonElement value) {
        if (!value.equals(getValue(t, true))) {
            setModified();
        }

        JsonObject obj = getPathElement(t.path, true);
        obj.add(t.key, value);
    }

    private void removeValue(ConfigToken t) {
        JsonObject o = getPathElement(t.path, false);
        if (o.has(t.key)) {
            WunderLib.LOGGER.info("Removing Config " + t.path + "." + t.key);
            o.remove(t.key);
            setModified();
        }
    }

    private JsonObject getPathElement(String path, boolean addIfMissing) {
        if (path == null || path.trim().isEmpty()) {
            return root;
        }

        String[] names = path.split("\\.");
        JsonObject obj = root;

        for (final String p : names) {
            if (obj.has(p)) {
                obj = obj.get(p).getAsJsonObject();
            } else {
                JsonObject newObject = new JsonObject();
                if (addIfMissing) {
                    obj.add(p, newObject);
                }
                obj = newObject;
            }
        }
        return obj;
    }

    boolean isModified() {
        return modified && this.root != null;
    }

    protected abstract @Nullable JsonObject loadRootElement();
    protected abstract boolean saveRootElement(String root);
    protected abstract boolean isReadOnly();

    public void loadFromDisc() {
        this.root = this.loadRootElement();
        modified = false;
        if (this.root == null) {
            this.root = new JsonObject();
            this.root.add(CREATE_VERSION, new JsonPrimitive(versionProvider.getModVersion().toString()));
        }
    }

    public void save() {
        save(false);
    }

    public void save(boolean force) {
        if (!isModified() && !force) return;

        if (this.isReadOnly()) {
            modified = false;
            return;
        }

        this.root.add(MODIFY_VERSION, new JsonPrimitive(versionProvider.getModVersion().toString()));
        try {
            if (this.saveRootElement(JSON_BUILDER.toJson(root))) {
                modified = false;
            }
        } catch (Exception ex) {
            WunderLib.LOGGER.error("Unable to save Config '{}'.", location.toString(), ex);
        }
    }

    private String getVersionString(String name) {
        JsonObject mod = getPathElement("", true);
        if (mod == null) return "1.0.0";
        JsonPrimitive p = mod.getAsJsonPrimitive(name);
        if (p == null) return versionProvider.getModVersion().toString();
        if (!p.isString()) return "1.0.0";

        return p.getAsString();
    }

    public Version lastModifiedVersion() {
        return new Version(getVersionString(MODIFY_VERSION));
    }

    public Version createdVersion() {
        return new Version(getVersionString(CREATE_VERSION));
    }

    //---- Client Code

    /**
     * return all Values stored in the config file
     *
     * @return all stored values
     */
    @OnlyIn(Dist.CLIENT)
    public List<C.Value<?, ?>> getAllValues() {
        return knownValues;
    }

    /**
     * Returns all Values that are visible in the UI
     *
     * @return All visible Values
     */
    @OnlyIn(Dist.CLIENT)
    public List<AbstractConfig<?>.Value<?, ?>> getAllVisibleValues() {
        List<AbstractConfig<?>.Value<?, ?>> values = new ArrayList<>();
        for (AbstractConfig<?>.Value<?, ?> v : knownValues) {
            if (!v.hiddenInUI) {
                values.add(v);
            }
        }
        return values;
    }

    /**
     * Returns all Values that are visible in the UI and are in the given group
     *
     * @param group The group to filter for
     * @return All visible Values in the given group
     */
    @OnlyIn(Dist.CLIENT)
    public List<AbstractConfig<?>.Value<?, ?>> getAllVisibleValues(Group group) {
        List<AbstractConfig<?>.Value<?, ?>> values = new ArrayList<>();
        for (AbstractConfig<?>.Value<?, ?> v : knownValues) {
            if (!v.hiddenInUI && v.group == group) {
                values.add(v);
            }
        }
        values.sort(Comparator.comparingInt(o -> o.order));
        return values;
    }

    /**
     * A static method that returns all visible Values that are in the given group
     *
     * @param group       The group to filter for
     * @param configFiles an array of config files
     * @return All visible Values in the given group
     */
    @OnlyIn(Dist.CLIENT)
    public static List<AbstractConfig<?>.Value<?, ?>> getAllVisibleValues(
            Group group,
            List<AbstractConfig<?>> configFiles
    ) {
        final List<AbstractConfig<?>.Value<?, ?>> values = new ArrayList<>();
        for (AbstractConfig<?> c : configFiles) {
            for (AbstractConfig<?>.Value<?, ?> v : c.knownValues) {
                if (!v.hiddenInUI && v.group == group) {
                    values.add(v);
                }
            }
        }
        values.sort(Comparator.comparingInt(o -> o.order));
        return values;
    }


    /**
     * Returns all groups that are used in this config file. The groups are sorted by their order.
     *
     * @return All stored groups
     */
    @OnlyIn(Dist.CLIENT)
    public List<Group> getAllGroups() {
        List<Group> groups = new ArrayList<>();
        for (AbstractConfig<?>.Value<?, ?> v : knownValues) {
            if (v.group != null && !groups.contains(v.group)) {
                groups.add(v.group);
            }
        }
        groups.sort(Comparator.comparingInt(o -> o.order));
        return groups;
    }

    /**
     * A static method that retuns all groups that are used in the given config files. The groups are sorted by their order.
     *
     * @param configFiles an array of config files
     * @return All stored groups
     */
    @OnlyIn(Dist.CLIENT)
    public static List<Group> getAllGroups(List<AbstractConfig<?>> configFiles) {
        List<Group> groups = new ArrayList<>();
        for (AbstractConfig<?> c : configFiles) {
            for (AbstractConfig<?>.Value<?, ?> v : c.knownValues) {
                if (v.group != null && !groups.contains(v.group)) {
                    groups.add(v.group);
                }
            }
        }
        groups.sort(Comparator.comparingInt(o -> o.order));
        return groups;
    }

    @OnlyIn(Dist.CLIENT)
    public static String getAllCategories(List<AbstractConfig<?>> configFiles) {
        StringBuilder sb = new StringBuilder();
        for (AbstractConfig<?> c : configFiles) {
            if (!sb.isEmpty()) sb.append(",");
            sb.append(c.category);
        }
        return sb.toString();
    }

    //---- Client Code End

    public AbstractConfig<?>.Value<?, ?> getValue(String path, String key) {
        for (AbstractConfig<?>.Value<?, ?> v : knownValues) {
            if (v.token.path().equals(path) && v.token.key.equals(key)) {
                return v;
            }
        }
        return null;
    }

    public record ConfigToken<T>(String path, String key, T defaultValue) {
        @Override
        public String toString() {
            return "ConfigToken{" +
                    "path='" + path + '\'' +
                    ", key='" + key + '\'' +
                    ", defaultValue=" + defaultValue +
                    '}';
        }
    }

    public record Group(@NotNull String modID, @NotNull String title, int order) {
        @Override
        public String toString() {
            return "Group{" +
                    "title='" + title + '\'' +
                    ", order=" + order +
                    '}';
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Group group = (Group) o;
            return title.equals(group.title);
        }

        @Override
        public int hashCode() {
            return Objects.hash(title);
        }
    }

    public abstract class Value<T, R extends Value<T, R>> {
        public interface AfterChange<C extends AbstractConfig<C>, T, R extends AbstractConfig<C>.Value<T, R>> {
            void changed(R value);
        }

        @NotNull
        public final ConfigToken<T> token;
        @Nullable
        protected Supplier<Boolean> isValidSupplier;
        private boolean hiddenInUI = false;
        @Nullable
        private BooleanValue enabledInUI = null;
        private boolean deprecated = false;
        @Nullable
        private Group group;
        private int order;
        @Nullable
        private C parentFile;
        @Nullable
        private List<AfterChange<C, T, R>> afterChangeNotifications;


        public Value(String path, String key, T defaultValue) {
            this(new ConfigToken(path, key, defaultValue), false);
        }

        public Value(String path, String key, T defaultValue, boolean isDeprecated) {
            this(new ConfigToken(path, key, defaultValue), isDeprecated);
        }

        public Value(ConfigToken token) {
            this(token, false);
        }

        public Value(ConfigToken token, boolean isDeprecated) {
            this.deprecated = isDeprecated; //make sure this is set before get, otherwise deprecated values will get added to the config!

            this.token = token;
            this.group = null;
            get(); //has the side effect of initializing the default value
            registerValue(this);
        }

        public R hideInUI() {
            hiddenInUI = true;
            return (R) this;
        }

        public R setGroup(Group group) {
            this.group = group;
            return (R) this;
        }

        public R setOrder(int order) {
            this.order = order;
            return (R) this;
        }

        public R setDependency(BooleanValue value) {
            this.enabledInUI = value;
            return (R) this;
        }

        public boolean isHiddenInUI() {
            return hiddenInUI;
        }

        public boolean isDeprecated() {
            return deprecated;
        }

        @Nullable
        public Group getGroup() {
            return group;
        }

        public int getOrder() {
            return order;
        }

        @Nullable
        public C getParentFile() {
            return (C) parentFile;
        }

        public boolean hasDependency() {
            return enabledInUI != null;
        }

        @Nullable
        public BooleanValue getDependency() {
            return enabledInUI;
        }

        @Nullable
        public Supplier<Boolean> getIsValidSupplier() {
            return isValidSupplier;
        }

        public final T getRaw() {
            JsonElement el = getValue(token, !deprecated);
            if (el == null) {
                if (!deprecated) set(token.defaultValue);
                return token.defaultValue;
            }
            return convert(el);
        }

        public T get() {
            return getRaw();
        }

        public void remove() {
            removeValue(token);
        }

        public void migrate(Value<T, R> newConfig) {
            newConfig.set(get());
            remove();
        }

        public void notifyAfterChange(AfterChange<C, T, R> notification) {
            if (afterChangeNotifications == null) {
                afterChangeNotifications = new ArrayList<>();
            }
            afterChangeNotifications.add(notification);
        }

        protected abstract T convert(@NotNull JsonElement el);

        @NotNull
        protected abstract JsonElement convert(T value);

        @NotNull
        protected abstract T parseString(@NotNull String value);

        public void set(T value) {
            if (deprecated) throw new IllegalStateException("'" + token.path() + "." +
                    token.key + "' is deprecated and can no-longer be used");
            setValue(token, convert(value));

            if (afterChangeNotifications != null) {
                for (AfterChange<C, T, R> n : afterChangeNotifications) {
                    n.changed((R) this);
                }
            }
        }

        public boolean valueEquals(String value) {
            return get().equals(parseString(value));
        }

        @Override
        public String toString() {
            return this.getClass().getSimpleName() + "{" +
                    "token=" + token +
                    ", value='" + get() + '\'' +
                    '}';
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof AbstractConfig<?>.Value<?, ?> value)) return false;
            return token.equals(value.token);
        }

        @Override
        public int hashCode() {
            return Objects.hash(token);
        }
    }

    public class IntValue extends Value<Integer, IntValue> {
        private int min = Integer.MIN_VALUE;
        private int max = Integer.MAX_VALUE;

        public IntValue(String path, String key, int defaultValue) {
            super(path, key, defaultValue);
        }

        protected IntValue(ConfigToken t) {
            super(t);
        }

        public IntValue(String path, String key, int defaultValue, boolean isDeprecated) {
            super(path, key, defaultValue, isDeprecated);
        }

        protected IntValue(ConfigToken t, boolean isDeprecated) {
            super(t, isDeprecated);
        }

        @Override
        protected Integer convert(@NotNull JsonElement el) {
            return Math.min(max, Math.max(min, el.getAsInt()));
        }

        @Override
        protected @NotNull JsonElement convert(Integer value) {
            return new JsonPrimitive(value);
        }

        @Override
        protected @NotNull Integer parseString(String value) {
            try {
                return Math.min(max, Math.max(min, Integer.parseInt(value)));
            } catch (NumberFormatException ex) {
                return 0;
            }
        }

        public IntValue hideInUI() {
            return (IntValue) super.hideInUI();
        }

        public IntValue min(int min) {
            this.min = min;
            return this;
        }

        public int getMin() {
            return min;
        }

        public IntValue max(int max) {
            this.max = max;
            return this;
        }

        public int getMax() {
            return max;
        }

        public int getClamped() {
            return Math.min(max, Math.max(min, get()));
        }
    }

    public class FloatValue extends Value<Float, FloatValue> {
        private float min = -Float.MAX_VALUE;
        private float max = Float.MAX_VALUE;

        public FloatValue(String path, String key, float defaultValue) {
            super(path, key, defaultValue);
        }

        protected FloatValue(ConfigToken t) {
            super(t);
        }

        public FloatValue(String path, String key, float defaultValue, boolean isDeprecated) {
            super(path, key, defaultValue, isDeprecated);
        }

        protected FloatValue(ConfigToken t, boolean isDeprecated) {
            super(t, isDeprecated);
        }

        @Override
        protected Float convert(@NotNull JsonElement el) {
            return Math.min(max, Math.max(min, el.getAsFloat()));
        }

        @Override
        protected @NotNull JsonElement convert(Float value) {
            return new JsonPrimitive(value);
        }

        @Override
        protected @NotNull Float parseString(String value) {
            try {
                return Math.min(max, Math.max(min, Float.parseFloat(value)));
            } catch (NumberFormatException ex) {
                return Float.NaN;
            }
        }

        public FloatValue hideInUI() {
            return (FloatValue) super.hideInUI();
        }

        public FloatValue min(float min) {
            this.min = min;
            return this;
        }

        public FloatValue max(float max) {
            this.max = max;
            return this;
        }

        public float getMin() {
            return min;
        }

        public float getMax() {
            return max;
        }
    }

    public class BooleanValue extends Value<Boolean, BooleanValue> {
        public BooleanValue(String path, String key, boolean defaultValue) {
            super(path, key, defaultValue);
        }

        protected BooleanValue(ConfigToken t) {
            super(t);
        }

        public BooleanValue(String path, String key, boolean defaultValue, boolean isDeprecated) {
            super(path, key, defaultValue, isDeprecated);
        }

        protected BooleanValue(ConfigToken t, boolean isDeprecated) {
            super(t, isDeprecated);
        }

        @Override
        protected Boolean convert(@NotNull JsonElement el) {
            return el.getAsBoolean();
        }

        @Override
        protected @NotNull JsonElement convert(Boolean value) {
            return new JsonPrimitive(value);
        }

        @Override
        protected @NotNull Boolean parseString(String value) {
            try {
                return Boolean.parseBoolean(value);
            } catch (NumberFormatException ex) {
                return false;
            }
        }

        /**
         * This value will only be enabled if the given condition is true. If exactly one condition is supplied
         * then the condition will be used as the dependency. If multiple conditions are supplied then the
         * dependency will be the AND of all the conditions.
         *
         * @param condition the conditions to enable this value
         * @return a new BooleanValue that is only enabled if the conditions are true
         */
        public BooleanValue and(BooleanValue... condition) {
            final BooleanValue res = and(() -> Arrays.stream(condition)
                                                     .map(c -> c.get())
                                                     .reduce(true, (p, c) -> p && c));
            if (condition.length == 1) res.setDependency(condition[0]);
            return res;
        }

        public BooleanValue and(Supplier<Boolean> condition) {
            BooleanValue self = this;
            BooleanValue res = new BooleanValue(token, isDeprecated()) {
                @Override
                public Boolean get() {
                    return condition.get() && self.get();
                }

                @Override
                public void set(Boolean value) {
                    self.set(value);
                }
            };
            res.isValidSupplier = condition;
            return res;
        }

        public BooleanValue or(BooleanValue... condition) {
            return or(() -> Arrays.stream(condition).map(c -> c.get()).reduce(true, (p, c) -> p || c));
        }

        public BooleanValue or(Supplier<Boolean> condition) {
            BooleanValue self = this;
            BooleanValue res = new BooleanValue(token, isDeprecated()) {
                @Override
                public Boolean get() {
                    return condition.get() || self.get();
                }

                @Override
                public void set(Boolean value) {
                    self.set(value);
                }
            };
            res.isValidSupplier = condition;
            return res;
        }

        public BooleanValue hideInUI() {
            return (BooleanValue) super.hideInUI();
        }
    }

    public class StringValue extends Value<String, StringValue> {
        public StringValue(String path, String key, String defaultValue) {
            super(path, key, defaultValue);
        }

        protected StringValue(ConfigToken<String> t) {
            super(t);
        }

        public StringValue(String path, String key, String defaultValue, boolean isDeprecated) {
            super(path, key, defaultValue, isDeprecated);
        }

        protected StringValue(ConfigToken<String> t, boolean isDeprecated) {
            super(t, isDeprecated);
        }

        @Override
        protected String convert(@NotNull JsonElement el) {
            return el.getAsString();
        }

        protected @NotNull JsonElement convert(String value) {
            return new JsonPrimitive(value);
        }

        protected @NotNull String parseString(String value) {
            return value;
        }

        public StringValue hideInUI() {
            return (StringValue) super.hideInUI();
        }
    }

    public class BlockPosValue extends Value<BlockPos, BlockPosValue> {
        public BlockPosValue(String path, String key, BlockPos defaultValue) {
            super(path, key, defaultValue);
        }

        public BlockPosValue(String path, String key, BlockPos defaultValue, boolean isDeprecated) {
            super(path, key, defaultValue, isDeprecated);
        }

        public BlockPosValue(ConfigToken token) {
            super(token);
        }

        public BlockPosValue(ConfigToken token, boolean isDeprecated) {
            super(token, isDeprecated);
        }

        @Override
        protected BlockPos convert(@NotNull JsonElement el) {
            JsonObject o = el.getAsJsonObject();
            return new BlockPos(o.get("x").getAsInt(), o.get("y").getAsInt(), o.get("z").getAsInt());
        }

        @Override
        protected @NotNull JsonElement convert(BlockPos value) {
            JsonObject o = new JsonObject();
            o.addProperty("x", value.getX());
            o.addProperty("y", value.getY());
            o.addProperty("z", value.getZ());
            return o;
        }

        @Override
        protected @NotNull BlockPos parseString(@NotNull String value) {
            //parse value using GSON
            return convert(new Gson().fromJson(value, JsonObject.class));
        }
    }
}

