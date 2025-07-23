package com.jobsearch.util;


import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;

import java.lang.reflect.Field;
import java.util.Collection;

public class SanitizerUtil {

    public static <T> T sanitizeInputRecursive(T obj) {
        if (obj == null) return null;

        Class<?> clazz = obj.getClass();

        // Jika objek adalah String, langsung sanitize dan return
        if (obj instanceof String) {
            return (T) Jsoup.clean((String) obj, Safelist.basic());
        }

        // Jika objek adalah Collection, sanitize setiap elemennya
        if (obj instanceof Collection<?>) {
            Collection<?> col = (Collection<?>) obj;
            col.forEach(SanitizerUtil::sanitizeInputRecursive);
            return obj;
        }

        // Untuk class biasa, cek semua fields
        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {
            field.setAccessible(true);
            try {
                Object value = field.get(obj);
                if (value == null) continue;

                if (value instanceof String) {
                    String cleanValue = Jsoup.clean((String) value, Safelist.basic());
                    field.set(obj, cleanValue);
                } else if (value instanceof Collection<?>) {
                    // sanitize tiap elemen di collection
                    Collection<?> col = (Collection<?>) value;
                    for (Object element : col) {
                        sanitizeInputRecursive(element);
                    }
                } else if (!field.getType().isPrimitive() && !field.getType().getName().startsWith("java.")) {
                    // rekursif sanitize object nested (bukan primitive dan bukan class Java standar)
                    sanitizeInputRecursive(value);
                }
                // Untuk tipe lain (primitive, enums dll) diabaikan
            } catch (IllegalAccessException e) {
                // ignore atau log error kalau perlu
                e.printStackTrace();
            }
        }

        return obj;
    }
}
