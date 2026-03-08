package de.ambertation.wunderlib.configs;

import de.ambertation.wunderlib.utils.Version;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import java.util.Hashtable;
import java.util.Map;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ConfigResource extends AbstractConfig<ConfigResource> {
    private static final String MERGE_STRATEGY = "merge";
    private JsonObject resourceRoot;

    private ConfigResource(Version.ModVersionProvider versionProvider, String path, String category) {
        super(
                versionProvider,
                versionProvider.mk("config/" + versionProvider.getNamespace() + "/" + path + ".json"),
                versionProvider.getNamespace() + "." + path
        );
    }

    private static Map<String, ConfigResource> CACHED_CONFIGS = new Hashtable<>();

    public static void invalidateCache() {
        CACHED_CONFIGS.clear();
    }

    public static ConfigResource create(Version.ModVersionProvider versionProvider, String path) {
        return CACHED_CONFIGS.computeIfAbsent(
                versionProvider.getNamespace() + "." + path,
                (c) -> new ConfigResource(versionProvider, path, c)
        );
    }

    public void setRootElement(String root) {
        this.resourceRoot = JSON_BUILDER.fromJson(root, JsonObject.class);
    }

    public void setRootElement(JsonObject root) {
        if (this.resourceRoot == null) {
            this.resourceRoot = root;
        } else {
            this.resourceRoot = merge(this.resourceRoot, root);
        }
    }

    private enum MergeStrategy {
        USE_FIRST,
        REPLACE,
        OR,
        AND;

        public static MergeStrategy fromString(String s) {
            if (s == null || s.isEmpty() || s.isBlank())
                return USE_FIRST;
            return MergeStrategy.valueOf(s.trim().toUpperCase());
        }
    }

    protected static @Nullable JsonObject merge(@Nullable JsonObject oldRoot, @Nullable JsonObject newRoot) {
        final MergeStrategy rootStrategy = MergeStrategy.fromString(newRoot != null && newRoot.has(MERGE_STRATEGY)
                ? newRoot.get(MERGE_STRATEGY).getAsString()
                : null);

        return merge(rootStrategy, oldRoot, newRoot);
    }

    private static @NotNull JsonPrimitive merge(
            MergeStrategy rootStrategy,
            @NotNull JsonPrimitive oldValue,
            @NotNull JsonPrimitive newValue
    ) {
        if (oldValue.isBoolean() && newValue.isBoolean()) {
            if (rootStrategy == MergeStrategy.OR) {
                return new JsonPrimitive(oldValue.getAsBoolean() || newValue.getAsBoolean());
            } else if (rootStrategy == MergeStrategy.AND) {
                return new JsonPrimitive(oldValue.getAsBoolean() && newValue.getAsBoolean());
            }
        }
        return newValue;
    }

    private static @NotNull JsonArray merge(
            @NotNull JsonArray oldValue,
            @NotNull JsonArray newValue
    ) {
        for (JsonElement e : newValue) {
            oldValue.add(e);
        }
        return oldValue;
    }

    private static @Nullable JsonObject merge(
            MergeStrategy rootStrategy,
            @Nullable JsonObject oldRoot,
            @Nullable JsonObject newRoot
    ) {
        if (oldRoot == null) return newRoot;
        if (newRoot == null) return oldRoot;

        if (newRoot.has(MERGE_STRATEGY)) {
            rootStrategy = MergeStrategy.fromString(newRoot.get(MERGE_STRATEGY).getAsString());
        }

        for (Map.Entry<String, JsonElement> e : newRoot.entrySet()) {
            if (e.getKey().equals("merge")) continue;
            if (!oldRoot.has(e.getKey())) {
                oldRoot.add(e.getKey(), e.getValue());
            } else {
                final JsonElement newE = e.getValue();
                final JsonElement oldE = oldRoot.get(e.getKey());
                if (rootStrategy == MergeStrategy.USE_FIRST) {
                    //do nothing
                } else if (rootStrategy == MergeStrategy.REPLACE) {
                    oldRoot.add(e.getKey(), newE);
                } else if (oldE.isJsonObject() && newE.isJsonObject()) {
                    oldRoot.add(e.getKey(), merge(rootStrategy, oldE.getAsJsonObject(), newE.getAsJsonObject()));
                } else if (oldE.isJsonPrimitive() && newE.isJsonPrimitive()) {
                    oldRoot.add(e.getKey(), merge(rootStrategy, oldE.getAsJsonPrimitive(), newE.getAsJsonPrimitive()));
                } else if (oldE.isJsonArray() && newE.isJsonArray()) {
                    oldRoot.add(e.getKey(), merge(oldE.getAsJsonArray(), newE.getAsJsonArray()));
                } else if (oldE.isJsonArray()) {
                    JsonArray ar = new JsonArray();
                    ar.add(newE);
                    oldRoot.add(e.getKey(), merge(oldE.getAsJsonArray(), ar));
                }
            }
        }

        return oldRoot;
    }

    @Override
    protected boolean isReadOnly() {
        return true;
    }

    @Override
    protected @Nullable JsonObject loadRootElement() {
        return this.resourceRoot;
    }

    @Override
    protected boolean saveRootElement(String root) {
        //Resource configs are not saved anywhere, they are read only
        return true;
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("ConfigResource{");
        sb.append("\n      category='").append(category).append('\'');
        sb.append(",\n      location='").append(location).append('\'');
        sb.append(",\n      resourceRoot=").append(resourceRoot);
        sb.append("\n}");
        return sb.toString();
    }
}
