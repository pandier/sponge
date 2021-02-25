/*
 * This file is part of Sponge, licensed under the MIT License (MIT).
 *
 * Copyright (c) SpongePowered <https://www.spongepowered.org>
 * Copyright (c) contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.spongepowered.common.mixin.api.mcp.commands.arguments.selector;

import com.google.common.base.Preconditions;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.advancement.Advancement;
import org.spongepowered.api.advancement.criteria.AdvancementCriterion;
import org.spongepowered.api.command.selector.Selector;
import org.spongepowered.api.command.selector.SelectorSortAlgorithm;
import org.spongepowered.api.command.selector.SelectorType;
import org.spongepowered.api.data.persistence.DataFormats;
import org.spongepowered.api.data.persistence.DataView;
import org.spongepowered.api.entity.EntityType;
import org.spongepowered.api.entity.living.player.gamemode.GameMode;
import org.spongepowered.api.registry.RegistryTypes;
import org.spongepowered.api.scoreboard.Score;
import org.spongepowered.api.scoreboard.Team;
import org.spongepowered.api.util.Range;
import org.spongepowered.api.util.Tristate;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.accessor.advancements.critereon.MinMaxBounds_FloatsAccessor;
import org.spongepowered.common.accessor.advancements.critereon.MinMaxBounds_IntsAccessor;
import org.spongepowered.common.bridge.commands.arguments.selector.EntitySelectorParserBridge;
import org.spongepowered.common.command.selector.SpongeSelectorSortAlgorithm;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.advancements.critereon.WrappedMinMaxBounds;
import net.minecraft.commands.arguments.selector.EntitySelector;
import net.minecraft.commands.arguments.selector.EntitySelectorParser;
import net.minecraft.core.Registry;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

@Mixin(EntitySelectorParser.class)
public abstract class EntitySelectorParserMixin_API implements Selector.Builder {

    // @formatter:off
    @Shadow @Final private StringReader reader;
    @Shadow private int maxResults;
    @Shadow private BiConsumer<Vec3, List<? extends Entity>> order;
    @Shadow private MinMaxBounds.Floats distance;
    @Shadow private MinMaxBounds.Ints level;
    @Shadow private boolean includesEntities;
    @Shadow private boolean worldLimited;
    @Shadow @Nullable private Double x;
    @Shadow @Nullable private Double y;
    @Shadow @Nullable private Double z;
    @Shadow @Nullable private Double deltaX;
    @Shadow @Nullable private Double deltaY;
    @Shadow @Nullable private Double deltaZ;
    @Shadow private WrappedMinMaxBounds rotX = WrappedMinMaxBounds.ANY;
    @Shadow private WrappedMinMaxBounds rotY = WrappedMinMaxBounds.ANY;
    @Shadow private Predicate<Entity> predicate;
    @Shadow private boolean currentEntity;
    @Shadow @Nullable private String playerName;
    @Shadow private int startPosition;
    @Shadow @Nullable private UUID entityUUID;
    @Shadow private BiFunction<SuggestionsBuilder, Consumer<SuggestionsBuilder>, CompletableFuture<Suggestions>> suggestions;
    @Shadow private boolean hasNameEquals;
    @Shadow private boolean hasNameNotEquals;
    @Shadow private boolean isLimited;
    @Shadow private boolean isSorted;
    @Shadow private boolean hasGamemodeEquals;
    @Shadow private boolean hasGamemodeNotEquals;
    @Shadow private boolean hasTeamEquals;
    @Shadow private boolean hasTeamNotEquals;
    @Shadow private net.minecraft.world.entity.@Nullable EntityType<?> type;
    @Shadow private boolean typeInverse;
    @Shadow private boolean hasScores;
    @Shadow private boolean hasAdvancements;
    @Shadow private boolean usesSelectors;

    @Shadow public abstract void shadow$setLimited(boolean value);
    @Shadow public abstract void shadow$setSorted(boolean value);
    @Shadow public abstract void shadow$setX(double xIn);
    @Shadow public abstract void shadow$setY(double yIn);
    @Shadow public abstract void shadow$setZ(double zIn);
    @Shadow public abstract void shadow$setDeltaX(double dxIn);
    @Shadow public abstract void shadow$setDeltaY(double dyIn);
    @Shadow public abstract void shadow$setDeltaZ(double dzIn);
    @Shadow public abstract void shadow$setIncludesEntities(boolean includeNonPlayers);
    @Shadow public abstract EntitySelector shadow$getSelector();
    @Shadow public abstract void shadow$addPredicate(Predicate<Entity> p_197401_1_);
    // @formatter:on

    @Nullable private Map<String, Range<@NonNull Integer>> api$scores;
    @Nullable private Object2BooleanOpenHashMap<String> api$advancement;
    @Nullable private Map<String, Object2BooleanOpenHashMap<String>> api$criterion;
    private boolean api$forceSelf;

    @Override
    public Selector.@NonNull Builder applySelectorType(final Supplier<? extends SelectorType> selectorType) {
        return this.applySelectorType(selectorType.get());
    }

    @Override
    public Selector.@NonNull Builder applySelectorType(@NonNull final SelectorType selectorType) {
        try {
            ((EntitySelectorParserBridge) this).bridge$parseSelector(selectorType);
        } catch (final CommandSyntaxException commandSyntaxException) {
            throw new IllegalArgumentException("Could not parse provided SelectorType", commandSyntaxException);
        }
        return this;
    }

    @Override
    public Selector.@NonNull Builder includeSelf() {
        this.api$forceSelf = true;
        return this;
    }

    @Override
    public Selector.@NonNull Builder setLimit(final int limit) {
        this.maxResults = limit;
        this.shadow$setLimited(limit != Integer.MAX_VALUE);
        return this;
    }

    @Override
    public Selector.@NonNull Builder setDistance(@NonNull final Range<@NonNull Double> range) {
        Preconditions.checkArgument(range.getMin() == null || range.getMin() >= 0, "min must be non-negative");
        Preconditions.checkArgument(range.getMax() == null || range.getMax() >= 0, "max must be non-negative");
        this.distance = MinMaxBounds_FloatsAccessor.invoker$new(
                this.api$floatFromDouble(range.getMin(), Function.identity()),
                this.api$floatFromDouble(range.getMax(), Function.identity()));
        return this;
    }

    @Override
    public Selector.@NonNull Builder setVolume(final org.spongepowered.math.vector.@NonNull Vector3d corner1,
                                               final org.spongepowered.math.vector.@NonNull Vector3d corner2) {
        final org.spongepowered.math.vector.Vector3d minPoint = corner1.min(corner2);
        final org.spongepowered.math.vector.Vector3d distance = corner1.max(corner2).sub(minPoint);
        this.shadow$setX(minPoint.getX());
        this.shadow$setY(minPoint.getY());
        this.shadow$setZ(minPoint.getZ());
        this.shadow$setDeltaX(distance.getX());
        this.shadow$setDeltaY(distance.getY());
        this.shadow$setDeltaZ(distance.getZ());
        return this;
    }

    @Override
    public Selector.@NonNull Builder setSortAlgorithm(@NonNull final Supplier<? extends SelectorSortAlgorithm> algorithm) {
        return this.setSortAlgorithm(algorithm.get());
    }

    @Override
    public Selector.@NonNull Builder setSortAlgorithm(@NonNull final SelectorSortAlgorithm algorithm) {
        Preconditions.checkArgument(algorithm instanceof SpongeSelectorSortAlgorithm, "Must be a SpongeSelectorSortAlgorithm");
        this.shadow$setSorted(true);
        this.order = ((SpongeSelectorSortAlgorithm) algorithm).getSortAlgorithm();
        return this;
    }

    @Override
    public Selector.@NonNull Builder advancement(@NonNull final Advancement advancement) {
        return this.api$advancement(advancement, false);
    }

    @Override
    public Selector.@NonNull Builder notAdvancement(@NonNull final Advancement advancement) {
        return this.api$advancement(advancement, true);
    }

    @Override
    public Selector.@NonNull Builder advancementCriterion(@NonNull final Advancement advancement, @NonNull final AdvancementCriterion criterion) {
        return this.api$advancementCriterion(advancement, criterion, false);
    }

    @Override
    public Selector.@NonNull Builder notAdvancementCriterion(@NonNull final Advancement advancement, @NonNull final AdvancementCriterion criterion) {
        return this.api$advancementCriterion(advancement, criterion, true);
    }

    @Override
    public Selector.@NonNull Builder setDataView(@NonNull final DataView view) {
        try {
            // TODO: ensure this works as expected
            final String json = DataFormats.JSON.get().write(view);
            this.api$handle("nbt", json);
            return this;
        } catch (@NonNull final IOException e) {
            throw new RuntimeException("Could not create JSON representation of DataView", e);
        }
    }

    @Override
    public Selector.@NonNull Builder notEntityType(@NonNull final Supplier<@NonNull EntityType<@NonNull ?>> type) {
        return this.notEntityType(type.get());
    }

    @Override
    public Selector.@NonNull Builder notEntityType(@NonNull final EntityType<@NonNull ?> type) {
        final ResourceKey key = (ResourceKey) (Object) Registry.ENTITY_TYPE.getKey((net.minecraft.world.entity.EntityType<?>) type);
        this.api$handle("type", "!" + key.asString());
        return this;
    }

    @Override
    public Selector.@NonNull Builder entityType(@NonNull final Supplier<@NonNull EntityType<@NonNull ?>> type, final boolean inherit) {
        return this.entityType(type.get(), inherit);
    }

    @Override
    public Selector.@NonNull Builder entityType(@NonNull final EntityType<@NonNull ?> type, final boolean inherit) {
        final ResourceKey key = (ResourceKey) (Object) Registry.ENTITY_TYPE.getKey((net.minecraft.world.entity.EntityType<?>) type);
        this.api$handle("type", String.format("%s%s", inherit ? "#" : "", key.asString()));
        return this;
    }

    @Override
    public Selector.@NonNull Builder setExperienceLevel(@NonNull final Range<@NonNull Integer> range) {
        Preconditions.checkArgument(range.getMin() == null || range.getMin() >= 0, "min must be non-negative");
        Preconditions.checkArgument(range.getMax() == null || range.getMax() >= 0, "max must be non-negative");
        this.level = MinMaxBounds_IntsAccessor.invoker$new(range.getMin(), range.getMax());
        this.shadow$setIncludesEntities(false);
        return this;
    }

    @Override
    public Selector.@NonNull Builder gameMode(@NonNull final Supplier<? extends GameMode> mode) {
        return this.gameMode(mode.get());
    }

    @Override
    public Selector.@NonNull Builder gameMode(@NonNull final GameMode mode) {
        final ResourceKey key = Sponge.getGame().registries().registry(RegistryTypes.GAME_MODE).valueKey(mode);
        this.api$handle("gamemode", key.getValue(), Tristate.FALSE);
        return this;
    }

    @Override
    public Selector.@NonNull Builder notGameMode(@NonNull final Supplier<? extends GameMode> mode) {
        return this.notGameMode(mode.get());
    }

    @Override
    public Selector.@NonNull Builder notGameMode(@NonNull final GameMode mode) {
        final ResourceKey key = Sponge.getGame().registries().registry(RegistryTypes.GAME_MODE).valueKey(mode);
        this.api$handle("gamemode", key.getValue(), Tristate.TRUE);
        return this;
    }

    @Override
    public Selector.@NonNull Builder noTeam() {
        this.api$handle("team", "", Tristate.TRUE);
        return this;
    }

    @Override
    public Selector.@NonNull Builder anyTeam() {
        this.api$handle("team", "", Tristate.FALSE);
        return this;
    }

    @Override
    public Selector.@NonNull Builder team(@NonNull final Team team) {
        this.api$handle("team", team.getName(), Tristate.FALSE);
        return this;
    }

    @Override
    public Selector.@NonNull Builder notTeam(@NonNull final Team team) {
        this.api$handle("team", team.getName(), Tristate.TRUE);
        return this;
    }

    @Override
    public Selector.@NonNull Builder name(@NonNull final String name) {
        this.api$handle("name", name, Tristate.FALSE);
        return this;
    }

    @Override
    public Selector.@NonNull Builder notName(@NonNull final String name) {
        this.api$handle("name", name, Tristate.TRUE);
        return this;
    }

    @Override
    public Selector.@NonNull Builder score(@NonNull final Score score, @NonNull final Range<@NonNull Integer> range) {
        if (this.api$scores == null) {
            this.api$scores = new HashMap<>();
        }
        // TODO: Check this is right.
        this.api$scores.put(score.getName().toString(), range);
        return this;
    }

    @Override
    public Selector.@NonNull Builder tag(@NonNull final String tag) {
        this.api$handle("tag", tag, Tristate.FALSE);
        return this;
    }

    @Override
    public Selector.@NonNull Builder notTag(@NonNull final String tag) {
        this.api$handle("tag", tag, Tristate.TRUE);
        return this;
    }

    @Override
    public Selector.@NonNull Builder setPitch(@NonNull final Range<@NonNull Double> range) {
        this.rotX = this.api$getWrappedBounds(range);
        return this;
    }

    @Override
    public Selector.@NonNull Builder setYaw(@NonNull final Range<@NonNull Double> range) {
        this.rotY = this.api$getWrappedBounds(range);
        return this;
    }

    @Override
    public Selector.@NonNull Builder filter(@NonNull final Predicate<org.spongepowered.api.entity.@NonNull Entity> filter) {
        this.shadow$addPredicate((Predicate<Entity>) (Object) filter);
        return this;
    }

    @Override
    @NonNull
    public Selector build() throws IllegalStateException {
        // Advancements and criteria
        if (this.api$advancement != null || this.api$criterion != null) {
            final List<String> entries = new ArrayList<>();
            if (this.api$advancement != null) {
                this.api$advancement.object2BooleanEntrySet().fastForEach(x -> entries.add(x.getKey() + "=" + x.getBooleanValue()));
            }
            if (this.api$criterion != null) {
                this.api$criterion.forEach((key, value) ->
                        value.object2BooleanEntrySet().fastForEach(x -> entries.add(key + "={" + x.getKey() + "=" + x.getBooleanValue() + "}")));
            }
            this.api$handle("advancements", "{" + String.join(",", entries) + "}");
            this.api$advancement = null;
            this.api$criterion = null;
        }

        if (this.api$scores != null) {
            final List<String> entries = new ArrayList<>();
            this.api$scores.forEach((key, range) -> entries.add(key + "=" + this.api$intRangeToStringRepresentation(range)));
            this.api$handle("scores", "{" + String.join(",", entries) + "}");
            this.api$scores = null;
        }

        if (this.api$forceSelf) {
            this.currentEntity = true;
        }

        return (Selector) this.shadow$getSelector();
    }

    @Override
    public Selector.@NonNull Builder reset() {
        this.order = EntitySelectorParser.ORDER_ARBITRARY;
        this.distance = MinMaxBounds.Floats.ANY;
        this.level = MinMaxBounds.Ints.ANY;
        this.includesEntities = false;
        this.worldLimited = false;
        this.x = null;
        this.y = null;
        this.z = null;
        this.deltaX = null;
        this.deltaY = null;
        this.deltaZ = null;
        this.rotX = WrappedMinMaxBounds.ANY;
        this.rotY = WrappedMinMaxBounds.ANY;
        this.predicate = x -> true;
        this.currentEntity = false;
        this.playerName = null;
        this.startPosition = 0;
        this.entityUUID = null;
        this.hasNameEquals = false;
        this.hasNameNotEquals = false;
        this.isLimited = false;
        this.isSorted = false;
        this.hasGamemodeEquals = false;
        this.hasGamemodeNotEquals = false;
        this.hasTeamEquals = false;
        this.hasTeamNotEquals = false;
        this.type = null;
        this.typeInverse = false;
        this.hasScores = false;
        this.hasAdvancements = false;
        this.usesSelectors = false;
        this.reader.setCursor(0);
        this.suggestions = EntitySelectorParser.SUGGEST_NOTHING;
        this.api$forceSelf = false;
        return this;
    }

    private Selector.@NonNull Builder api$advancement(@NonNull final Advancement advancement, final boolean inverted) {
        if (this.api$advancement == null) {
            this.api$advancement = new Object2BooleanOpenHashMap<>();
        }
        this.api$advancement.put(advancement.getKey().asString(), inverted);
        return this;
    }

    private Selector.@NonNull Builder api$advancementCriterion(@NonNull final Advancement advancement, @NonNull final AdvancementCriterion criterion,
            final boolean inverted) {
        if (this.api$criterion == null) {
            this.api$criterion = new HashMap<>();
        }
        this.api$criterion.computeIfAbsent(advancement.getKey().toString(), k -> new Object2BooleanOpenHashMap<>()).put(criterion.getName(), inverted);
        return this;
    }

    private void api$handle(@NonNull final String name, @NonNull final String value) {
        this.api$handle(name, value, Tristate.UNDEFINED);
    }

    private void api$handle(@NonNull final String name, @NonNull final String value, @NonNull final Tristate invert) {
        try {
            ((EntitySelectorParserBridge) this).bridge$handleValue(name, value, invert);
        } catch (final CommandSyntaxException ex) {
            throw new IllegalArgumentException("Could not create selector criteria based on input", ex);
        }
    }

    @Nullable
    private Float api$floatFromDouble(@Nullable final Double d, final Function<Float, Float> mapping) {
        if (d == null) {
            return null;
        }
        return mapping.apply(d.floatValue());
    }


    private WrappedMinMaxBounds api$getWrappedBounds(final Range<@NonNull Double> range) {
        final Float a = this.api$floatFromDouble(range.getMin(), Mth::wrapDegrees);
        final Float b = this.api$floatFromDouble(range.getMax(), Mth::wrapDegrees);
        if (a == null) {
            return new WrappedMinMaxBounds(null, b);
        }
        if (b == null) {
            return new WrappedMinMaxBounds(a, null);
        }
        if (a <= b) {
            return new WrappedMinMaxBounds(a, b);
        }
        return new WrappedMinMaxBounds(b, a);
    }

    private String api$intRangeToStringRepresentation(@NonNull final Range<@NonNull Integer> range) {
        if (range.getMin() != null && range.getMax() != null && range.getMin().intValue() == range.getMax().intValue()) {
            return String.valueOf(range.getMax().intValue());
        }

        return String.format("%s..%s",
                range.getMin() == null ? "" : String.valueOf(range.getMin().intValue()),
                range.getMax() == null ? "" : String.valueOf(range.getMax().intValue()));
    }

}
