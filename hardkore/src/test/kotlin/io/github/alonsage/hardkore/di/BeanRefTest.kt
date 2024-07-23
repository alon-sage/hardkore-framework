package io.github.alonsage.hardkore.di

import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.util.*
import kotlin.reflect.typeOf
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class BeanRefTest {
    @Test
    fun `platform type cleaned`() {
        val platformType = UUID::randomUUID.returnType
        assertTrue('!' in platformType.toString())

        val cleansedType = cleanPlatformType(platformType)
        assertFalse('!' in cleansedType.toString())
    }

    @Nested
    inner class ScalarType {
        @Test
        fun `type stored correctly`() {
            assertEquals(typeOf<Map<String, Int>>(), beanRef<Map<String, Int>>().kType)
        }

        @Test
        fun `nullable type stored correctly`() {
            assertEquals(typeOf<Map<String, Int>?>(), beanRef<Map<String, Int>?>().kType)
        }

        @Test
        fun `default qualifier is null`() {
            assertNull(beanRef<String>().qualifier)
        }

        @Test
        fun `qualifier is stored correctly`() {
            val qualifier = UUID.randomUUID()
            assertEquals(qualifier, beanRef<UUID>(qualifier).qualifier)
        }

        @Test
        fun `object qualifier works`() {
            val qualifier = object {}
            assertEquals(qualifier, beanRef<List<*>>(qualifier).qualifier)
        }
    }

    @Nested
    inner class SetType {
        @Test
        fun `type stored correctly`() {
            assertEquals(typeOf<Set<UUID>>(), setBeanRef<UUID>().kType)
        }

        @Test
        fun `nullable type stored correctly`() {
            assertEquals(typeOf<Set<UUID?>>(), setBeanRef<UUID?>().kType)
        }

        @Test
        fun `default qualifier is null`() {
            assertNull(setBeanRef<String>().qualifier)
        }

        @Test
        fun `qualifier is stored correctly`() {
            val qualifier = UUID.randomUUID()
            assertEquals(qualifier, setBeanRef<UUID>(qualifier).qualifier)
        }

        @Test
        fun `object qualifier works`() {
            val qualifier = object {}
            assertEquals(qualifier, setBeanRef<List<*>>(qualifier).qualifier)
        }
    }

    @Nested
    inner class ListType {
        @Test
        fun `type stored correctly`() {
            assertEquals(typeOf<List<UUID>>(), listBeanRef<UUID>().kType)
        }

        @Test
        fun `nullable type stored correctly`() {
            assertEquals(typeOf<List<UUID?>>(), listBeanRef<UUID?>().kType)
        }

        @Test
        fun `default qualifier is null`() {
            assertNull(listBeanRef<String>().qualifier)
        }

        @Test
        fun `qualifier is stored correctly`() {
            val qualifier = UUID.randomUUID()
            assertEquals(qualifier, listBeanRef<UUID>(qualifier).qualifier)
        }

        @Test
        fun `object qualifier works`() {
            val qualifier = object {}
            assertEquals(qualifier, listBeanRef<List<*>>(qualifier).qualifier)
        }
    }

    @Nested
    inner class MapType {
        @Test
        fun `type stored correctly`() {
            assertEquals(typeOf<Map<BigDecimal, UUID>>(), mapBeanRef<BigDecimal, UUID>().kType)
        }

        @Test
        fun `nullable type stored correctly`() {
            assertEquals(typeOf<Map<BigDecimal?, UUID>>(), mapBeanRef<BigDecimal?, UUID>().kType)
            assertEquals(typeOf<Map<BigDecimal, UUID?>>(), mapBeanRef<BigDecimal, UUID?>().kType)
            assertEquals(typeOf<Map<BigDecimal?, UUID?>>(), mapBeanRef<BigDecimal?, UUID?>().kType)
        }

        @Test
        fun `default qualifier is null`() {
            assertNull(mapBeanRef<String, Boolean>().qualifier)
        }

        @Test
        fun `qualifier is stored correctly`() {
            val qualifier = UUID.randomUUID()
            assertEquals(qualifier, mapBeanRef<UUID, String>(qualifier).qualifier)
        }

        @Test
        fun `object qualifier works`() {
            val qualifier = object {}
            assertEquals(qualifier, mapBeanRef<Set<*>, List<*>>(qualifier).qualifier)
        }
    }
}