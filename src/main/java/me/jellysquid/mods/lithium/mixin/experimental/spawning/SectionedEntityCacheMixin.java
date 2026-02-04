package me.jellysquid.mods.lithium.mixin.experimental.spawning;

import com.google.common.collect.AbstractIterator;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.objects.ObjectCollection;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import me.jellysquid.mods.lithium.common.world.ChunkAwareEntityIterable;
import net.minecraft.world.entity.EntityLike;
import net.minecraft.world.entity.EntityTrackingSection;
import net.minecraft.world.entity.SectionedEntityCache;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Iterator;

@Mixin(SectionedEntityCache.class)
public abstract class SectionedEntityCacheMixin<T extends EntityLike> implements ChunkAwareEntityIterable<T> {

    @Shadow
    @Final
    private Long2ObjectMap<EntityTrackingSection<T>> trackingSections;

    @Override
    public Iterable<T> lithiumIterateEntitiesInTrackedSections() {
        ObjectCollection<EntityTrackingSection<T>> sections = this.trackingSections.values();
        return () -> new EntityTrackingSectionIterator(sections.iterator());
    }

    private class EntityTrackingSectionIterator extends AbstractIterator<T> {
        private final ObjectIterator<EntityTrackingSection<T>> sectionsIterator;
        private Iterator<T> entityIterator;

        EntityTrackingSectionIterator(ObjectIterator<EntityTrackingSection<T>> sectionsIterator) {
            this.sectionsIterator = sectionsIterator;
        }

        @Nullable
        @Override
        protected T computeNext() {
            T nextEntity = tryGetNextFromCurrentIterator();
            if (nextEntity != null) {
                return nextEntity;
            }
            return findNextEntityInSections();
        }

        @Nullable
        private T tryGetNextFromCurrentIterator() {
            if (this.entityIterator != null && this.entityIterator.hasNext()) {
                return this.entityIterator.next();
            }
            return null;
        }

        @Nullable
        private T findNextEntityInSections() {
            while (this.sectionsIterator.hasNext()) {
                T entity = processNextSection();
                if (entity != null) {
                    return entity;
                }
            }
            return this.endOfData();
        }

        @Nullable
        private T processNextSection() {
            EntityTrackingSection<T> section = this.sectionsIterator.next();
            if (section.getStatus().shouldTrack() && !section.isEmpty()) {
                return initializeIteratorFromSection(section);
            }
            return null;
        }

        @Nullable
        private T initializeIteratorFromSection(EntityTrackingSection<T> section) {
            //noinspection unchecked
            @SuppressWarnings("unchecked")
            EntityTrackingSectionAccessor<T> accessor = (EntityTrackingSectionAccessor<T>) section;
            this.entityIterator = accessor.getCollection().iterator();
            if (this.entityIterator.hasNext()) {
                return this.entityIterator.next();
            }
            return null;
        }
    }

}
