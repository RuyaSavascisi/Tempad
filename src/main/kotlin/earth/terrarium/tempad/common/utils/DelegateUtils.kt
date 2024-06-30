package earth.terrarium.tempad.common.utils

import net.minecraft.core.component.DataComponentType
import net.minecraft.network.syncher.EntityDataAccessor
import net.minecraft.network.syncher.EntityDataSerializer
import net.minecraft.network.syncher.SynchedEntityData
import net.minecraft.world.entity.Entity
import net.neoforged.neoforge.attachment.AttachmentHolder
import net.neoforged.neoforge.attachment.AttachmentType
import net.neoforged.neoforge.common.MutableDataComponentHolder
import java.util.*
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

operator fun <T : Any> AttachmentHolder.get(key: AttachmentType<T>): T = this.getData(key)
operator fun <T : Any> AttachmentHolder.set(key: AttachmentType<T>, value: T) = this.setData(key, value)
operator fun <T : Any> AttachmentHolder.minusAssign(key: AttachmentType<T>) { this.removeData(key) }

class AttachmentDelegate<T : Any>(private val key: AttachmentType<T>) : ReadWriteProperty<AttachmentHolder, T> {
    override operator fun getValue(thisRef: AttachmentHolder, property: KProperty<*>): T = thisRef[key]
    override operator fun setValue(thisRef: AttachmentHolder, property: KProperty<*>, value: T) { thisRef[key] = value }
}

class OptionalAttachmentDelegate<T : Any>(private val key: AttachmentType<T>) :
    ReadWriteProperty<AttachmentHolder, T?> {
    override operator fun getValue(thisRef: AttachmentHolder, property: KProperty<*>): T? = thisRef.getExistingData(key).orElse(null)
    override operator fun setValue(thisRef: AttachmentHolder, property: KProperty<*>, value: T?) {
        if (value == null) {
            thisRef -= key
        } else {
            thisRef[key] = value
        }
    }
}

class ComponentDelegate<T : Any>(private val key: DataComponentType<T>, private val default: T) {
    operator fun getValue(thisRef: MutableDataComponentHolder, property: KProperty<*>): T = thisRef[key] ?: default
    operator fun setValue(thisRef: MutableDataComponentHolder, property: KProperty<*>, value: T) { thisRef[key] = value }
    fun clear(thisRef: MutableDataComponentHolder) {
        thisRef.remove(key)
    }
}

inline fun <reified T : Entity, U> createDataKey(serializer: EntityDataSerializer<U>): EntityDataAccessor<U> =
    SynchedEntityData.defineId(T::class.java, serializer)


class DataDelegate<T : Any>(private val key: EntityDataAccessor<T>) : ReadWriteProperty<Entity, T> {
    override operator fun getValue(thisRef: Entity, property: KProperty<*>): T = thisRef.entityData[key]
    override operator fun setValue(thisRef: Entity, property: KProperty<*>, value: T) = thisRef.entityData.set(key, value)
}

class OptionalDataDelegate<T : Any>(private val key: EntityDataAccessor<Optional<T>>) : ReadWriteProperty<Entity, T?> {
    override operator fun getValue(thisRef: Entity, property: KProperty<*>): T? = thisRef.entityData[key].orElse(null)
    override operator fun setValue(thisRef: Entity, property: KProperty<*>, value: T?) = thisRef.entityData.set(key, Optional.ofNullable(value))
}