package org.elasticsearch.roc.utils;

import java.util.ArrayList;
import java.util.Iterator;

public class Lists {

    public static <T> T checkNotNull(T reference) {
        if (reference == null) throw new NullPointerException();
        return reference;
    }

    public static <E> ArrayList<E> newArrayList() {
        return new ArrayList<E>();
    }

    public static <E> ArrayList<E> newArrayList(Iterator<? extends E> elements) {
        checkNotNull(elements);
        ArrayList<E> list = newArrayList();
        while (elements.hasNext()) {
            list.add(elements.next());
        }
        return list;
    }

}
